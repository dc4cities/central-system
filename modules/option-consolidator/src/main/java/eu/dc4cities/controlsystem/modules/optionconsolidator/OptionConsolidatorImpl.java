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

package eu.dc4cities.controlsystem.modules.optionconsolidator;

import eu.dc4cities.configuration.goal.Objective;
import eu.dc4cities.configuration.goal.interval.TimeIntervalExpressionException;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterPower;
import eu.dc4cities.controlsystem.model.easc.*;
import eu.dc4cities.controlsystem.model.erds.DataCenterForecast;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.modules.ConsolidatorException;
import eu.dc4cities.controlsystem.modules.OptionConsolidator;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.*;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.WorkingMode;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.converter.ActivityOptionToSimpleActivity;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.converter.ErdsForecastToPowerSource;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.Scheduler;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.Splitter;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.Statistics;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.reducer.Pass;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.reducer.Reducer;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.measure.quantity.Duration;
import javax.measure.unit.SI;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static javax.measure.unit.NonSI.MINUTE;

/**
 * Implementation of the OptionConsolidator.
 *
 *
 */
@Component
public class OptionConsolidatorImpl implements OptionConsolidator {

	public static final String HEURISTIC_INTERNAL = "internal";
	public static final String HEURISTIC_IPP = "ipp";
	
    private final Logger LOGGER = LoggerFactory.getLogger("OptionConsolidator");

    private int timeOut;
    private boolean ippHeuristic = true;

    private int verbosity = 0;
    private int nbSlots = -1;

    private Amount<Duration> slotDuration;

    private List<Objective> goals;

    private OptConstraint obj = new MinBrownEnergyUsage();

    public static final int WATT_REDUCTION_FACTOR = 4;

    private List<OldPowerSource> usage;

    private Statistics stats;

    private Reducer oWnd = new Pass();//new RangeFactorReducer(0.75);
    private boolean split = true;
    private int nbSlaves = Runtime.getRuntime().availableProcessors();

    private boolean optimize = true;
    private Map<String, List<String>> replays;
    public boolean profitBased = true;
    /**
     * New instance.
     */
    public OptionConsolidatorImpl() {
        //5 sec. by default
        timeOut = 5;
        goals = new ArrayList<>();
        replays = new HashMap<>();
    }

    @Override
    public List<EascActivityPlan> buildActivityPlans(TimeSlotBasedEntity range, List<Objective> objectives,
                                                     List<DataCenterForecast> dataCenterForecasts,
                                                     List<DataCenterPowerPlan> ipp,
                                                     List<EascPowerPlan> budgets,
                                                     List<EascActivitySpecifications> activitySpecifications,
                                                     List<DataCenterPower> pastPowerUsage, final List<EascServiceLevels> pastSLOs,
                                                     List<EascMetrics> eascMetrics) throws ConsolidatorException {

        List<Scheduler> schedulers;
        ExecutorService executor = null;
        try {
            List<Statistics> subStats = new ArrayList<>();
            if (split) {
                schedulers = Splitter.split(range, objectives, dataCenterForecasts, ipp, budgets, activitySpecifications, pastPowerUsage, pastSLOs, eascMetrics, oWnd);
            } else {
                schedulers = new ArrayList<>();
                Scheduler s = new Scheduler(range, objectives, dataCenterForecasts, ipp, budgets, activitySpecifications, pastPowerUsage, pastSLOs, eascMetrics, oWnd);
                s.profitBased = profitBased;
                schedulers.add(s);
            }
            executor = Executors.newFixedThreadPool(Math.min(schedulers.size(), nbSlaves));
            ExecutorCompletionService <List<EascActivityPlan>> gatherer = new ExecutorCompletionService<List<EascActivityPlan>>(executor);
            LOGGER.debug(schedulers.size() + " sub problem(s)");
            for (Scheduler s : schedulers) {
                s.ippHeuristic(ippHeuristic);
                s.doOptimize(optimize);
                s.replay(replays);
                s.profitBased = profitBased;
                gatherer.submit(() -> {return s.solve(timeOut);});
            }
            List<List<EascActivityPlan>> res = new ArrayList<>();
            for (int i = 0; i < schedulers.size(); i++) {
                res.add(gatherer.take().get());
            }
            for (Scheduler s : schedulers) {
                subStats.add(s.getLastStatistics());
            }
            stats = Merger.mergeStatistics(subStats);
            return Merger.merge(res);
        } catch (TimeIntervalExpressionException | ExecutionException | InterruptedException ex) {
            throw new ConsolidatorException(ex.getMessage(), ex);
        } finally {
        	if (executor != null) {
        		executor.shutdownNow();
        	}
        }
    }

    @Override
    public void replay(String activity, List<String> states) {
        replays.put(activity, states);
    }

    @Override
    public List<EascActivityPlan> selectOptionPlans(TimeSlotBasedEntity timeRange, List<ErdsForecast> erdsForecasts, List<EascOptionPlan> optionPlans) throws ConsolidatorException {
        try {
            stats = new Statistics().start();
            usage = new ArrayList<>();
            List<OldPowerSource> pwrSources = extractPowerSources(erdsForecasts);
            LOGGER.debug("Timeline: " + nbSlots + " x " + slotDuration);

            List<SimpleActivity> activities = extractSimpleActivities(optionPlans);
            if (activities.isEmpty()) {
                usage = new ArrayList<>();
                for (OldPowerSource s : pwrSources) {
                    OldPowerSourceSlot[] ss = new OldPowerSourceSlot[nbSlots];
                    for (int i = 0; i < nbSlots; i++) {
                        ss[i] = new OldPowerSourceSlot(Amount.valueOf(0, SI.WATT), s.getSlots()[i].getRenewablePct(), 0);
                    }
                    usage.add(new OldPowerSource(s.getName(), ss));
                }
                stats.terminated();
                return Collections.emptyList();
            }

            List<TimeSlot> timeSlots = new ArrayList<>(nbSlots);

            long offset = 0;

            for (int i = 0; i < nbSlots; i++) {
                TimeSlot ts = new TimeSlotImpl(slotDuration);
                ts.setStartDate(new DateTime(offset));
                offset += slotDuration.longValue(SI.MILLI(SI.SECOND));
            }

            int[] slots = new int[nbSlots];
            Arrays.fill(slots, (int) slotDuration.doubleValue(MINUTE));

            final OPCP pb;
            pb = new OPCP(slots, pwrSources, activities, WATT_REDUCTION_FACTOR);

            for (Objective o : goals) {
                List<OPCPDecorator> decorators = OPCPDecoratorFactory.create(o, timeSlots, timeRange.getDateFrom().toDate());
                for (OPCPDecorator d : decorators) {
                    d.decorate(pb);
                }
            }

            pb.getSolver().plugMonitor((IMonitorSolution) () -> {
                IMeasures m = pb.getSolver().getMeasures();
                if (m.hasObjective()) {
                    stats.newSolution(m.getBestSolutionValue());
                }
            });

            pb.setVerbosity(verbosity);
            pb.getSolver();
            obj.decorate(pb);
            ESat sat = pb.solve(timeOut);

            if (sat != ESat.TRUE) {
                if (sat == ESat.FALSE) {
                    stats.noSolution();
                } else {
                    stats.timeout();
                }
                return Collections.emptyList();
            }

            Map<String, EascActivityPlan> plans = new HashMap<>(optionPlans.size());

            for (EascOptionPlan op : optionPlans) {
                plans.put(op.getEascName(), newActivityPlan(op.getEascName(), timeRange));
            }
            for (SimpleActivity sa : activities) {
                String easc = sa.getEasc();
                EascActivityPlan ap = plans.get(easc);
                WorkingMode w = pb.getSelectedPlan(sa);
                ap.addActivity(w.getActivity());
            }
            List<EascActivityPlan> res = new ArrayList<>(plans.values());

            //Store statistics
            for (OldPowerSource s : pwrSources) {
                IntVar[] values = pb.getPowerUsage(s);
                OldPowerSourceSlot[] ss = new OldPowerSourceSlot[values.length - 1];
                for (int i = 1; i < values.length; i++) {
                    int val = values[i].getValue() * pb.getWattReducingFactor();
                    ss[i - 1] = new OldPowerSourceSlot(Amount.valueOf(val, SI.WATT), s.getSlots()[i].getRenewablePct(), 0);
                }
                usage.add(new OldPowerSource(s.getName(), ss));
            }
            if (pb.getSolver().hasReachedLimit()) {
                stats.timeout();
            } else {
                stats.terminated();
            }
            return res;
        } catch (ConsolidatorException ex) {
            stats.terminated();
            throw ex;
        } finally {
            LOGGER.info(stats.toString());
        }
    }

    public List<OldPowerSource> getComputedPowerDispatch() {
        return usage;
    }

    public Statistics getLastStatistics() {
        return stats;
    }

    private List<OldPowerSource> extractPowerSources(List<ErdsForecast> sources) throws ConsolidatorException {
        ErdsForecastToPowerSource conv = new ErdsForecastToPowerSource();
        List<OldPowerSource> l = new ArrayList<>(sources.size());
        DateTime origin = null;
        for (ErdsForecast fc : sources) {

            //Check origin consistency
            if (origin == null) {
                origin = fc.getDateFrom();
            } else if (!origin.equals(fc.getDateFrom())) {
                throw new ConsolidatorException("Origin between the sources mismatch. Expected =" + origin + " but Erds '" + fc.getErdsName() + "' proposed " + fc.getDateFrom());
            }
            //Check nb. of slots consistency
            if (nbSlots == -1) {
                nbSlots = fc.getTimeSlotForecasts().size();
            } else if (nbSlots != fc.getTimeSlotForecasts().size()) {
                throw new ConsolidatorException("Nb of slots between the sources mismatch. Expected=" + nbSlots + " but Erds '" + fc.getErdsName() + "' proposed " + fc.getTimeSlotForecasts().size());
            }

            //Check slot duration consistency
            if (slotDuration == null) {
                slotDuration = fc.getTimeSlotDuration();
            } else if (!fc.getTimeSlotDuration().equals(slotDuration)) {
                throw new ConsolidatorException("TimeSlot duration for between the sources mismatch. Expected=" + slotDuration + " but Erds '" + fc.getErdsName() + "' proposed " + fc.getTimeSlotDuration());
            }
            l.add(conv.convert(fc, slotDuration));
        }

        return l;
    }

    private List<SimpleActivity> extractSimpleActivities(List<EascOptionPlan> possiblePlans) {
        List<SimpleActivity> res = new ArrayList<>();
        for (EascOptionPlan aop : possiblePlans) {
            for (ActivityOption o : aop.getActivityOptions()) {
                ActivityOptionToSimpleActivity adapter = new ActivityOptionToSimpleActivity(aop.getEascName(), o);
                res.add(adapter.getSimpleActivity());
            }
        }
        return res;
    }

    private EascActivityPlan newActivityPlan(String eascName, TimeSlotBasedEntity timeRange) {
    	EascActivityPlan plan = new EascActivityPlan(eascName);
    	plan.setDateFrom(timeRange.getDateFrom());
    	plan.setDateTo(timeRange.getDateTo());
    	plan.setTimeSlotDuration(timeRange.getTimeSlotDuration());
    	return plan;
    }
    
    @Override
    public void setTimeout(int t) {
        timeOut = t;
    }

    @Override
    public int getTimeout() {
        return timeOut;
    }

    public void setHeuristic(String heuristic) {
    	if (heuristic.equals(HEURISTIC_IPP)) {
    		ippHeuristic = true;
    	} else if (heuristic.equals(HEURISTIC_INTERNAL)) {
    		ippHeuristic = false;
    	} else {
    		throw new IllegalArgumentException("Unsupported heuristic: " + heuristic);
    	}
    }
    
    public void setPowerConfig(List<Objective> objectives) {
        if (objectives != null) {
            this.goals = objectives;
        }
    }

    public List<Objective> getPowerConfig() {
        return goals;
    }

    public void setVerbosity(int lvl) {
        this.verbosity = lvl;
    }

    public OptionConsolidatorImpl reducer(Reducer w) {
        oWnd = w;
        return this;
    }

    public Reducer reducer() {
        return oWnd;
    }

    public OptionConsolidatorImpl nbSlaves(int nb) {
        nbSlaves = nb;
        return this;
    }

    public OptionConsolidatorImpl split(boolean b) {
        split = b;
        return this;
    }

    public int nbSlaves() {
        return nbSlaves;
    }

    public boolean split() {
        return split;
    }

    @Override
    public void doOptimize(boolean b) {
        optimize = b;
    }

    @Override
    public boolean doOptimize() {
        return optimize;
    }
}
