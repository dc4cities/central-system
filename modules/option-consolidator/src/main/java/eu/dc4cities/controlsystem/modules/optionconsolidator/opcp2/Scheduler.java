/*
 * Copyright 2016 The DC4Cities author.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2;

import eu.dc4cities.configuration.goal.Objective;
import eu.dc4cities.configuration.goal.interval.TimeIntervalExpressionException;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.TimeSlotPower;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterPower;
import eu.dc4cities.controlsystem.model.easc.*;
import eu.dc4cities.controlsystem.model.erds.DataCenterForecast;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.modules.ConsolidatorException;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.reducer.Reducer;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.jscience.physics.amount.Amount;

import javax.xml.datatype.DatatypeConfigurationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.Converter.timeSlot;

/**
 * Created by fhermeni on 29/09/2015.
 */
public class Scheduler {

    private TimeSlotBasedEntity range;
    private List<Objective> objectives;
    private List<DataCenterForecast> forecasts;
    private List<EascPowerPlan> budgets;
    private List<EascActivitySpecifications> specs;
    private List<DataCenterPower> pastPower;
    private List<EascServiceLevels> pastSLOs;
    private List<EascMetrics> eascMetrics;
    private Statistics stats;

    private Reducer oWnd;

    public boolean profitBased = false;
    private boolean optimize = true;
    private Map<String, List<String>> replays;
    private boolean ippHeuristic;

    private List<DataCenterPowerPlan> ipp;

    public Scheduler(TimeSlotBasedEntity range, List<Objective> objectives,
                     List<DataCenterForecast> dataCenterForecasts,
                     List<DataCenterPowerPlan> ipps,
                     List<EascPowerPlan> budgets,
                     List<EascActivitySpecifications> activitySpecifications,
                     List<DataCenterPower> pastPowerUsage, List<EascServiceLevels> pastSLOs,
                     List<EascMetrics> eascMetrics,
                     Reducer r) {

        this.oWnd = r;
        this.range = range;
        this.objectives = objectives;
        this.forecasts = dataCenterForecasts;
        this.budgets = budgets;
        this.pastPower = pastPowerUsage;
        this.pastSLOs = pastSLOs;
        this.eascMetrics = eascMetrics;
        this.specs = activitySpecifications;
        this.replays = new HashMap<>();
        this.ipp = ipps;
    }

    public TimeSlotBasedEntity getRange() {
        return range;
    }

    public List<Objective> getObjectives() {
        return objectives;
    }

    public List<DataCenterForecast> getForecasts() {
        return forecasts;
    }

    public List<EascPowerPlan> getBudgets() {
        return budgets;
    }

    public List<EascActivitySpecifications> getSpecs() {
        return specs;
    }

    public List<DataCenterPower> getPastPower() {
        return pastPower;
    }

    public List<EascServiceLevels> getPastSLOs() {
        return pastSLOs;
    }

    public List<EascMetrics> getEascMetrics() {
        return eascMetrics;
    }

    public Statistics getStats() {
        return stats;
    }

    public List<EascActivityPlan> solve(int to) {

        List<OPCP2> pbs = new ArrayList<>();
        List<Solver> solvers = new ArrayList<>();
        List<Statistics> allStats = new ArrayList<>();
        int nb = 1;
        if (ipp != null) {
            nb++;
        }
        int nbSlots = Converter.timeSlot(range, range.getDateTo());
        List<MyActivity> activities = convert(range, specs, pastSLOs, eascMetrics);

        List<PowerSource> sources = convertForecasts(range, forecasts);
        List<PowerSource> pastUsage = pastPower(range, pastPower, forecasts);

        for (int i = 0; i < nb; i++) {
            final Statistics st = new Statistics().start();
            allStats.add(st);
            List<DynCost> costs;
            try {
                costs = convertCosts(objectives, range, pastUsage);
            } catch (TimeIntervalExpressionException | DatatypeConfigurationException ex) {
                stats.terminated();
                throw new ConsolidatorException("Unable to convert the objectives: ", ex);
            }

            try {
                /*if (oWnd == null) {
                    oWnd = new Pass();
                }
                oWnd = oWnd.copy();
                oWnd.reset();*/
                String name = range.getDateFrom().toString("dd/MM/YY");
                OPCP2 pb = new OPCP2(name + "#" + i, nbSlots, sources, activities, replays.isEmpty());

                costs.forEach(pb::add);
                injectBudgets(range, pb, budgets);

                pb.timeLimit(to);

                force(pb);
                //pb.pretty(false);
                if (i == 0) {
                    pb.ippHeuristic(null);
                } else if (ippHeuristic && ipp != null) {
                    pb.ippHeuristic(convert(ipp));
                }
                pb.profitBased = profitBased;
                pbs.add(pb);
                pb.getSolver().plugMonitor((IMonitorSolution) () -> {
                    //oWnd.reduce(pb);

                    st.newSolution(((IntVar) pb.getSolver().getObjectives()[0]).getValue());
                    for (int a = 0; a < pb.getAutomaton().size(); a++) {
                        String id = pb.getAutomaton().get(a).job.pretty();
                        List<State> perfs = new ArrayList<>();
                        for (int t = 0; t < nbSlots; t++) {
                            int stId = pb.getAllStates()[a][t].getValue();
                            State state = pb.getAutomaton().get(a).state(stId);
                            perfs.add(state);
                        }
                        st.setStates(id, perfs);
                    }
                });
                solvers.add(pb.getSolver());
            } catch (NullPointerException ex) {
                throw new ConsolidatorException("[" + range.getDateFrom().toString("dd/MM/YY") + "]: ", ex);
            }
        }

        SMF.prepareForParallelResolution(solvers);
        pbs.parallelStream().forEach(pb -> {
            pb.solve(optimize);
        });

        //We catch the solver that computed the best solution
        OPCP2 best = pbs.get(0);
        int bestObjective = Integer.MIN_VALUE;//pbs.get(0).val((IntVar) pbs.get(0).getSolver().getObjectives()[0]);
        int bestIdx = -1;
        ESat globalSat = pbs.get(0).getSolver().isFeasible();
        for (int i = 0; i < nb; i++) {
            OPCP2 pb = pbs.get(i);
            ESat res = pb.getSolver().isFeasible();
            if (res == ESat.TRUE) {
                globalSat = ESat.TRUE;
            }

            if (res == ESat.TRUE) {
                int v = pb.val((IntVar) pb.getSolver().getObjectives()[0]);
                if (bestObjective < v) {
                    best = pb;
                    bestObjective = v;
                    bestIdx = i;
                }
            }
        }
        //In case none succeeded
        if (!globalSat.equals(ESat.TRUE)) {
            stats = allStats.get(0);
            stats.terminated();
            if (globalSat.equals(ESat.FALSE)) {
                throw new ConsolidatorException("The problem has no solution. Bug ? Bounds that are too tight ? Overflow ?");
            } else {
                throw new ConsolidatorException("Unable to state a solution in the allowed " + to + " seconds.");
            }
        }
        //Get the result from the best solver and store the associated statistics
        List<EascActivityPlan> plans = makeResult(best, range, specs);
        stats = allStats.get(bestIdx).retainedPlan(plans);
        stats.finished(best.getSolver().hasReachedLimit());
        for (DynCost c : best.getDynamicCosts()) {
            c.store(stats);
        }
        return plans;
    }

    public void replays(Map<String, List<String>> r) {
        this.replays = r;
    }


    private void force(OPCP2 pb) {
        for (Map.Entry<String, List<String>> replay : replays.entrySet()) {
            String[] buf = replay.getKey().split(":");
            MyActivity a = pb.activity(buf[0], buf[1]);
            int aId = pb.activity(a);
            ActivityAutomaton ca = pb.getAutomaton().get(aId);
            for (int t = 0; t < pb.getNbSlots(); t++) {
                if (t >= replay.getValue().size()) {
                    continue;
                }
                String state = replay.getValue().get(t);
                if (state.isEmpty()) {
                    continue;
                }
                int st = ca.state(state);
                if (st < 0) {
                    List<String> possibles = new ArrayList<>();
                    for (int x = 0; x < ca.nbStates(); x++) {
                        possibles.add(ca.state(x).name());
                    }
                    throw new UnsupportedOperationException("Unknown state " + state + " supported=" + possibles);
                }
                pb.getSolver().post(ICF.arithm(pb.getAllStates()[aId][t], "=", st));
            }
        }
    }

    private List<DynCost> convertCosts(List<Objective> powerObjectives, TimeSlotBasedEntity range, List<PowerSource> pastPowerUsage) throws TimeIntervalExpressionException, DatatypeConfigurationException {
        List<DynCost> costs = new ArrayList<>();
        for (Objective o : powerObjectives) {
            costs.addAll(Converter.dynCost(o, range, pastPowerUsage));
        }
        return costs;
    }

    private void injectBudgets(TimeSlotBasedEntity timeRange, OPCP2 pb, List<EascPowerPlan> eascPowerPlans) {
        for (EascPowerPlan budget : eascPowerPlans) {
            int[] aIds = pb.activities(budget.getEascName());
            for (DataCenterPowerPlan q : budget.getDataCenterQuotas()) {
                for (Map.Entry<Integer, TimeSlotPower> e : q.getPowerQuotasMap().entrySet()) {
                    int pow = Converter.power(e.getValue().getPower());
                    try {
                        if (e.getKey() < pb.getNbSlots()) {
                            pb.setPowerQuota(e.getKey(), aIds, q.getDataCenterName(), pow);
                        }
                    } catch (ConsolidatorException ex) {
                        throw new ConsolidatorException("Unfeasible power budget of '" + pow + "' (un-reduced= " + (Converter.WATT_REDUCTION_FACTOR * pow) + " for " + budget.getEascName() + " at time-slot " + e.getKey() + ": " + ex.getMessage());
                    }
                }
                for (EnergyQuota quota : q.getEnergyQuotas()) {
                    int from = quota.getStartTimeSlot();
                    int to = quota.getEndTimeSlot();
                    try {
                        pb.setEnergyQuota(from,
                                to,
                                aIds,
                                q.getDataCenterName(),
                                Converter.energy(quota.getEnergy()));
                    } catch (ConsolidatorException ex) {
                        throw new ConsolidatorException("Unfeasible energy budget of '" + Converter.energy(quota.getEnergy()) + "' (reduced) for " + budget.getEascName() + "[" + from + ";" + to + "[ : " + ex.getMessage());
                    }

                }
            }
        }
    }

    /**
     * Convert the forecasts
     *
     * @param range               the scheduling window
     * @param dataCenterForecasts the forecasts to convert
     * @return the extracted power source
     */
    private List<PowerSource> convertForecasts(TimeSlotBasedEntity range, List<DataCenterForecast> dataCenterForecasts) {
        List<PowerSource> sources = new ArrayList<>();
        for (DataCenterForecast forecasts : dataCenterForecasts) {
            for (ErdsForecast fc : forecasts.getErdsForecasts()) {
                sources.add(Converter.powerSource(range, fc, forecasts.getDataCenterName()));
            }
        }
        return sources;
    }

    /**
     * Extract the past power consumption for given forecasts.
     *
     * @param range          the scheduling range
     * @param pastPowerUsage past data
     * @param dcForecasts    the forecasts
     * @return the formatted past power usage
     */
    private List<PowerSource> pastPower(TimeSlotBasedEntity range,
                                        List<DataCenterPower> pastPowerUsage,
                                        List<DataCenterForecast> dcForecasts) {
        List<PowerSource> res = new ArrayList<>();
        for (DataCenterPower past : pastPowerUsage) {
            boolean found = false;
            for (DataCenterForecast fc : dcForecasts) {
                if (fc.getDataCenterName().equals(past.getDataCenterName())) {
                    res.addAll(Converter.before(fc, past, range.getDateFrom()));
                    found = true;
                }
            }
            if (!found) {
                throw new ConsolidatorException("Unable to compute past power usage. No forecast for datacenter '" + past.getDataCenterName() + "'");
            }
        }
        return res;
    }

    private void insertWM(int t, State st, Activity a) throws ConsolidatorException {
        for (PerfLevel pl : st.perfLevels()) {
            WM w = pl.WM();
            boolean done = false;
            for (ActivityDataCenter aDc : a.getDataCenters()) {
                if (aDc.getDataCenterName().equals(w.getDc())) {
                    Work w2 = new Work(t, t + 1, w.name(), w.getValue(), pl.backend().getPower(), pl.backend().getBusinessPerformance());
                    aDc.getWorks().add(w2);
                    done = true;
                }
            }
            if (!done) {
                throw new ConsolidatorException("Unable to insert the WM " + w + " in a datacenter");
            }
        }
    }

    private List<EascActivityPlan> makeResult(OPCP2 pb, TimeSlotBasedEntity range, List<EascActivitySpecifications> eascs) {
        List<EascActivityPlan> res = new ArrayList<>();
        for (EascActivitySpecifications easc : eascs) {
            res.add(makeEascActivityPlan(pb, range, easc));
        }
        return res;
    }

    private EascActivityPlan makeEascActivityPlan(OPCP2 pb, TimeSlotBasedEntity range, EascActivitySpecifications easc) {
        EascActivityPlan p = new EascActivityPlan(easc.getEascName());
        p.copyIntervalFrom(range);
        for (ActivitySpecification spec : easc.getActivitySpecifications()) {
            p.addActivity(makeActivity(pb, range, easc.getEascName(), spec));
        }
        return p;
    }

    private Activity makeActivity(OPCP2 pb, TimeSlotBasedEntity range, String easc, ActivitySpecification spec) {
        Activity a = new Activity(spec.getActivityName());
        MyActivity myActivity = pb.activity(easc, spec.getActivityName());
        int aId = pb.activity(myActivity);
        for (DataCenterSpecification dcSpec : spec.getDataCenters()) {
            ActivityDataCenter dc = new ActivityDataCenter(dcSpec.getDataCenterName());
            a.getDataCenters().add(dc);
        }

        for (int t = 0; t < pb.getNbSlots(); t++) {
            int stateIdx = pb.val(pb.getAllStates()[aId][t]);
            State st = pb.getAutomaton().get(aId).state(stateIdx);
            insertWM(t, st, a);
        }

        int[] instantPerfs = pb.instantPerf(aId);
        for (InstantRevenue r : myActivity.instantRevenues()) {
            a.getServiceLevels().add(makeServiceLevel(range, r, instantPerfs));
        }

        for (CumulativeRevenue r : myActivity.cumulativeRevenues()) {
            int perf = pb.val(pb.perf(aId, r));
            a.getServiceLevels().add(makeServiceLevel(range, r, perf));
        }
        return a;
    }

    /**
     * Convert all the specifications to activities.
     *
     * @param range                      the scheduling window
     * @param eascActivitySpecifications the specifications
     * @param slos                       the current status for the ongoing SLOs
     * @param eascMetrics                the current metrics
     * @return the resulting activities
     */
    private List<MyActivity> convert(TimeSlotBasedEntity range,
                                     List<EascActivitySpecifications> eascActivitySpecifications,
                                     List<EascServiceLevels> slos,
                                     List<EascMetrics> eascMetrics) {
        List<MyActivity> activities = new ArrayList<>();
        for (EascActivitySpecifications easc : eascActivitySpecifications) {
            for (ActivitySpecification a : easc.getActivitySpecifications()) {
                //Extract the additional informations for the activity
                List<ServiceLevel> currentSLOs = currentServiceLevels(slos, easc.getEascName(), a.getActivityName());
                ActivityMetrics currentMetrics = currentMetrics(eascMetrics, easc.getEascName(), a.getActivityName());
                //and convert
                activities.add(Converter.myActivity(range, easc.getEascName(), a, currentSLOs, currentMetrics));
            }
        }
        return activities;
    }

    private List<PowerSource> convert(List<DataCenterPowerPlan> plans) {
        List<PowerSource> res = new ArrayList<>();
        for (DataCenterPowerPlan plan : plans) {
            PowerSourceSlot[] slots = new PowerSourceSlot[plan.getPowerQuotas().size()];
            for (int t = 0; t < plan.getPowerQuotas().size(); t++) {
                TimeSlotPower pow = plan.getPowerQuotas().get(t);
                slots[pow.getTimeSlot()] = new PowerSourceSlot(Converter.power(pow.getPower()), 0, 0);
            }
            PowerSource src = new PowerSource(plan.getDataCenterName(), slots);
            res.add(src);
        }
        return res;
    }

    private ServiceLevel makeServiceLevel(TimeSlotBasedEntity range, InstantRevenue r, int[] perfs) {
        int t = r.at();
        ServiceLevel l = new ServiceLevel(timeSlot(range, t), timeSlot(range, t + 1));
        l.setInstantBusinessPerformance(Amount.valueOf(perfs[t], r.unit()));
        return l;
    }

    private ServiceLevel makeServiceLevel(TimeSlotBasedEntity range, CumulativeRevenue r, int perf) {
        ServiceLevel l = new ServiceLevel(timeSlot(range, r.from()), timeSlot(range, r.to()));
        l.setCumulativeBusinessPerformance(Amount.valueOf(perf, r.unit()));
        return l;
    }

    /**
     * Get the current metrics for a given activity.
     *
     * @param eascMetrics the metrics to browse
     * @param easc        the activity easc
     * @param name        the activity name
     * @return the activity metrics. {@code null} if no activity match the identifiers
     */
    private ActivityMetrics currentMetrics(List<EascMetrics> eascMetrics, String easc, String name) {
        for (EascMetrics metrics : eascMetrics) {
            if (metrics.getEascName().equals(easc)) {
                for (ActivityMetrics am : metrics.getActivities()) {
                    if (am.getName().equals(name)) {
                        return am;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get the current service level for a given activity.
     *
     * @param eascs the service levels to browse
     * @param easc  the activity easc
     * @param name  the activity name
     * @return the activity service levels. Empty if no activity match the identifiers
     */
    private List<ServiceLevel> currentServiceLevels(List<EascServiceLevels> eascs, String easc, String name) {
        List<ServiceLevel> slos = new ArrayList<>();
        for (EascServiceLevels s : eascs) {
            if (s.getEascName().equals(easc)) {
                for (ActivityServiceLevels x : s.getActivityServiceLevels()) {
                    if (x.getActivityName().equals(name)) {
                        return x.getServiceLevels();
                    }
                }
            }
        }
        return slos;
    }

    public Statistics getLastStatistics() {
        return stats;
    }

    public void replay(Map<String, List<String>> replays) {
        this.replays = replays;
    }

    public void doOptimize(boolean o) {
        this.optimize = o;
    }

    public void ippHeuristic(boolean b) {
        this.ippHeuristic = b;
    }
}
