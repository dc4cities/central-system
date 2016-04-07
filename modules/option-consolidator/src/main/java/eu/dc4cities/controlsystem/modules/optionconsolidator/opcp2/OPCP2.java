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

import eu.dc4cities.controlsystem.modules.ConsolidatorException;
import eu.dc4cities.controlsystem.modules.optionconsolidator.Range;
import org.chocosolver.memory.trailing.EnvironmentTrailing;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Arithmetic;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.nary.automata.FA.ICostAutomaton;
import org.chocosolver.solver.constraints.nary.automata.PropMultiCostRegular;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.chocosolver.solver.search.strategy.IntStrategyFactory.*;

/**
 * A solver to compute the most appropriate working mode for a set of activities
 * depending on their specification, the power sources and an objective that consists in
 * maximizing the profits.
 *
 *
 */
public class OPCP2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(OPCP2.class);

    private Solver s;
    private List<MyActivity> activities;
    private List<PowerSource> sources;
    private int nbSlots;

    private int nbDcs;

    private String[] dcs;
    private List<ActivityAutomaton> automaton;
    private List<ICostAutomaton> costAutomatons;
    private IntVar[][] allStates;
    /**
     * The power usage per activity.
     * [aId][t][dId]
     */
    private IntVar[][][] activitiesPowerUsage;
    private IntVar[][] globalActivitiesPowerUsage;

    /**
     * The power usage per power source [sId][t]
     */
    private IntVar[][] sourcePowerUsage;
    private IntVar[][] allEnergyPrice;

    private IntVar profit;
    private List<IntVar> costs;

    private IntVar[][] revenues;

    private IntVar[][] resources;

    //Default timelimit in seconds.
    private long timeLimit = 5;

    private List<DynCost> dynCosts;

    private String name;

    private boolean noIWaste;

    private List<PowerSource> ipp;

    private boolean pretty = true;
    private IntVar globalEnergy, globalCost, globalRevenues;

    public boolean profitBased = false;
    public OPCP2(int nbSlots, List<PowerSource> sources, List<MyActivity> activities) {
        this("OPCP2", nbSlots, sources, activities, true);
    }

    public OPCP2(String n, int nbSlots, List<PowerSource> sources, List<MyActivity> activities, boolean noInstantWaste) {
        this.name = n;
        this.noIWaste = noInstantWaste;
        this.nbSlots = nbSlots;
        this.sources = sources;
        this.activities = activities;
        this.resources = new IntVar[activities.size()][];
        this.revenues = new IntVar[activities.size()][];
        automaton = new ArrayList<>();
        costAutomatons = new ArrayList<>();

        s = new Solver(new EnvironmentTrailing(), "OPCP2");

        allStates = new IntVar[activities.size()][];
        activitiesPowerUsage = new IntVar[activities.size()][][];
        globalActivitiesPowerUsage = new IntVar[activities.size()][nbSlots];
        sourcePowerUsage = new IntVar[sources.size()][nbSlots];
        allEnergyPrice = new IntVar[sources.size()][nbSlots];
        costs = new ArrayList<>();


        Set<String> dcNames = new HashSet<>();

        for (PowerSource src : sources) {
            dcNames.add(src.dcId());
        }
        nbDcs = dcNames.size();
        dcs = new String[nbDcs];
        dcNames.toArray(dcs);
        int i = 0;

        int maxCons = 0;
        for (MyActivity a : activities) {
            maxCons += a.maxPowerConsumption();
        }
        for (PowerSource src : sources) {
            sourcePowerUsage[i] = makePowerUsage(src, maxCons);
            allEnergyPrice[i] = energyPrice(sourcePowerUsage[i], src);
            i++;
        }

        i = 0;
        for (MyActivity a : activities) {
            ActivityAutomaton aa = new ActivityAutomaton(a);
            automaton.add(aa);
            ICostAutomaton ca = aa.costAutomaton(nbSlots);
            costAutomatons.add(ca);

            allStates[i] = makeStateVariables(s, nbSlots, aa, ca.getNbStates());
            activitiesPowerUsage[i] = powerConsumption2(s, allStates[i], aa, i);
            IntVar[] costs = new IntVar[aa.nbResources()];
            int[] perfBounds = aa.perfBounds();
            for (int r = 0; r < aa.nbResources(); r++) {
                if (r == aa.instant()) {
                    int[] bounds = aa.instantRevenueBound();
                    costs[r] = VF.bounded("iRevenues(" + a.pretty() + ")", bounds[0], bounds[1], s);
                } else {
                    costs[r] = VF.bounded("cPerf(" + a.pretty() + ")", 0, perfBounds[1] * nbSlots, s);
                }
            }
            resources[i] = costs; //the last might be a instant revenue already
            revenues[i] = new IntVar[aa.nbResources()];

            //Hard constraints for the cumulative SLA
            for (CumulativeRevenue p : a.cumulativeRevenues()) {
                injectCumulative(i, p);
            }
            if (aa.instant() >= 0) {
                injectInstant(i);
            }
            s.post(new Constraint("MCR(" + a.easc() + ")", new PropMultiCostRegular(allStates[i], costs, ca)));
            i++;
            checkSchedulability(aa);
        }

        //Power related variables
        capPower3();
        dynCosts = new ArrayList<>();

        localSourceFirst();
        ipp = null;

        profit = VF.bounded("profit", Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, s);
//        s.setObjectives(profit);
    }

    private static String pretty(Range b) {
        return "[" + b.from() + "; " + b.to() + "[";
    }

    public IntVar[] revenues(int aId) {
        return revenues[aId];
    }

    private void injectCumulative(int aId, CumulativeRevenue p) {
        ActivityAutomaton aa = automaton.get(aId);
        int cId = aa.cumulative(p);
        if (p.to() < 0 || p.from() >= nbSlots) {
            LOGGER.info("Cumulative SLO for '" + activities.get(aId).pretty() + "' outside the scheduling window. 0 revenues assumed");
            revenues[aId][cId] = VF.fixed(0, getSolver());
            return;
        }
        IntVar perf = VF.offset(resources[aId][cId], p.base());
        //convert the performance to a revenue
        int[] rev = p.flatten(perf.getUB() + 1);
        //No negative revenues
        int max = 0;
        for (int r : rev) {
            max = Math.max(r, max);
        }
        IntVar revenue = VF.bounded("revenue(" + aa.job.pretty() + ", range=" + pretty(p) + ")", 0, max, s);
        among(s, revenue, rev, perf);
        //Cap the maximum perf to the moment the profit will not be higher
        int maxProfit = rev[rev.length - 1];
        int upperPerfThreshold;
        for (upperPerfThreshold = 0; upperPerfThreshold < rev.length; upperPerfThreshold++) {
            if (rev[upperPerfThreshold] == maxProfit) {
                break; //Got the threshold where doing more does not matter
            }
        }
        revenues[aId][cId] = revenue;
        //how much occurrence for the mostPowerful state to reach the perf threshold ?
        State mostPowerfull = null;
        State leastPowerfull = null;
        for (int i = 0; i < aa.nbStates(); i++) {
            State st = aa.state(i);
            if (mostPowerfull == null || st.perf() > mostPowerfull.perf()) {
                mostPowerfull = st;
            }
            if (leastPowerfull == null || st.perf() < leastPowerfull.perf()) {
                leastPowerfull = st;
            }

        }
    }

    public int getNbDatacentres() {
        return nbDcs;
    }
    private void injectInstant(int aId) {

        ActivityAutomaton aa = automaton.get(aId);
        int rId = aa.instant();
        //the resource is the revenue so nothing special to do
        IntVar revenue = resources[aId][rId];
        revenues[aId][rId] = revenue;

        //Prune the useless mode
        if (noIWaste) {
            for (InstantRevenue ir : aa.job.instantRevenues()) {
                IntVar state = allStates[aId][ir.at()];
                //System.err.println("at " + ir.at() + " toRemove=" + uselessModes(aId, ir));
                for (int m : uselessModes(aId, ir)) {
                    //System.err.println("at " + ir.at() + " remove " + aa.state(m));
                    s.post(ICF.arithm(state, "!=", m));
                }
            }
        }
    }

    private List<Integer> uselessModes(int aId, InstantRevenue r) {
        int t = r.at();
        ActivityAutomaton aa = automaton.get(aId);
        IntVar state = allStates[aId][t];
        int basePerf = r.basePerf();
        int ub = state.getUB();
        List<Integer> toRemove = new ArrayList<>();
        int[][] costs = aa.transitionsCosts();
        for (int i = state.getLB(); i <= ub; i = state.nextValue(i)) {
            int maxCost = 0;
            for (int x = 0; x < aa.nbStates(); x++) {
                maxCost = Math.max(maxCost, costs[x][i]);
            }

            if (aa.state(i).perf() - maxCost > basePerf) {
                //In the worst case scenario, the mode still provides too much perf
                toRemove.add(i);
            }/* else {
                System.err.println("Keep " + aa.state(i));
            }*/
        }
        if (toRemove.size() <= 1) {
            return new ArrayList<>();
        }
        return toRemove.subList(1, toRemove.size());
    }

    private State firstExceeding(int aId, int perf) {
        ActivityAutomaton aa = automaton.get(aId);
        Set<State> over = new TreeSet<>((s1, s2) -> {
            return s1.perf() - s2.perf();
        });
        for (int s = 0; s < aa.nbStates(); s++) {
            int[][] costs = aa.transitionsCosts();
            int maxCost = 0;
            for (int x = 0; x < aa.nbStates(); x++) {
                maxCost = Math.max(maxCost, costs[x][s]);
            }
            if (aa.state(s).perf() - maxCost >= perf) {
                //In the worst case scenario, the mode still provides too much perf
                over.add(aa.state(s));
            }
        }
        if (over.size() < 1) {
            return null;
        }
        Iterator<State> ite = over.iterator();
        return ite.next();
    }

    private double resultingPct(int w, int t) {
        List<PowerSource> srcs = new ArrayList<>(sources);
        Collections.sort(srcs, (a, b) -> {
            return b.slots()[t].renPct() - a.slots()[t].renPct();
        });
        double green = 0;
        int remainder = w;
        for (PowerSource s : srcs) {
            if (remainder > 0) {
                int used = Math.min(s.slots()[t].peak(), remainder);
                remainder -= used;
                green += used * s.slots()[t].renPct();
            }
        }
        return green / w;
    }

    public int activity(MyActivity a) {
        for (int i = 0; i < activities.size(); i++) {
            if (activities.get(i) == a) {
                return i;
            }
        }
        return -1;
    }

    public int[] activities(String easc) {
        int i = 0;
        List<Integer> res = new ArrayList<>();
        for (MyActivity a : activities) {
            if (a.easc().equals(easc)) {
                res.add(i);
            }
            i++;
        }
        int[] arr = new int[res.size()];
        for (i = 0; i < arr.length; i++) {
            arr[i] = res.get(i);
        }
        return arr;
    }

    private void localSourceFirst() {
        //for each slot
        //if a source is 100% renewable, its usage is the minimum between the activity usage and its capacity
        for (int d = 0; d < nbDcs; d++) {
            int[] sourceIds = powerSources(getSources(dcs[d]));
            for (int t = 0; t < nbSlots; t++) {
                for (int i = 0; i < sourceIds.length; i++) {
                    int sId = sourceIds[i];
                    if (sources.get(sId).slots()[t].renPct() == 100) {
                        int peak = sources.get(sId).slots()[t].peak();
                        List<IntVar> max = new ArrayList<>();
                        for (int a = 0; a < activitiesPowerUsage.length; a++) {
                            max.add(activitiesPowerUsage[a][t][d]);
                        }
                        IntVar sum = sum(dcs[d] + "@" + t, max);
                        s.post(ICF.minimum(sourcePowerUsage[sId][t], new IntVar[]{VF.fixed(peak, s), sum}));
                    }
                }
            }
        }
    }

    public ESat solve(boolean optimize) {
        //Reduce some bounds
        int[] powerBounds = new int[]{0, 0};
        for (ActivityAutomaton a : automaton) {
            int[] power = a.wattBounds();
            powerBounds[0] += power[0];
            powerBounds[1] += power[1];
        }

        try {
            //Cap the power consumption
            for (IntVar[] src : sourcePowerUsage) {
                for (int t = 0; t < src.length; t++) {
                    src[t].updateUpperBound(powerBounds[1], Cause.Null);
                }
            }
            int srcId = 0;
            for (IntVar[] src : allEnergyPrice) {
                for (int t = 0; t < src.length; t++) {
                    src[t].updateUpperBound(powerBounds[1] * sources.get(srcId).slots()[t].price(), Cause.Null);
                }
                srcId++;
            }
        } catch (ContradictionException ex) {
            LOGGER.error("There is no enough power available to supply the activities");
            return ESat.FALSE;
        }

        //objectives
        IntVar[] pens = costs.toArray(new IntVar[costs.size()]);

        //energy consumption
        IntVar[] en = ArrayUtils.flatten(allEnergyPrice);

        if (getLogger().isTraceEnabled()) {
            Chatterbox.showDecisions(s);
        }
        return maxProfit(s, en, ArrayUtils.flatten(new IntVar[][]{pens}), ArrayUtils.flatten(revenues), optimize);
    }

    public ESat solve() {
        return solve(true);
    }

    private ESat maxProfit(Solver s, IntVar[] en, IntVar[] costs, IntVar[] revenues, boolean opt) {

        globalEnergy = sum("sumEnergy", en);
        globalCost = sum("objectives", costs);
        globalRevenues = sum("revenues", revenues);

        //We cannot override profit because it is already in the model
        //so p will act as a pivot variable
        profit = sum("_profit_", new IntVar[]{VF.minus(globalEnergy), globalCost, globalRevenues});
        //s.post(ICF.arithm(profit, "=", p));

        if (getLogger().isDebugEnabled() && pretty) {
            s.plugMonitor((IMonitorSolution) () -> {
                int watts = 0;
                for (IntVar[] src : sourcePowerUsage) {
                    for (IntVar w : src) {
                        watts += w.getValue();
                    }
                }
                System.out.println("-- [" + name + "] Solution. Profit: " + Converter.toEuros(profit.getValue()) + " --");
                System.out.println("Nb. of slots: " + nbSlots);
                System.out.println(s.getMeasures().getNodeCount() + " nodes; " + s.getMeasures().getBackTrackCount() + " backtracks; " + s.getMeasures().getFailCount() + " fails; optimize=" + opt + "; ipp heuristic: " + (ipp != null));
                System.out.println("Energy: consumption=" + watts + " Watt/hour; price=" + Converter.toEuros(globalEnergy.getValue()));
                System.out.println("Objective penalties: " + Converter.toEuros(globalCost.getValue()));
                for (DynCost c : dynCosts) {
                    System.out.println("\t" + c.pretty());
                }
                System.out.println("EASC incomes: " + Converter.toEuros(globalRevenues.getValue()));
                for (int aId = 0; aId < allStates.length; aId++) {
                    MyActivity a = activities.get(aId);
                    for (CumulativeRevenue cr : a.cumulativeRevenues()) {
                        System.out.println("\t" + a.pretty() + "[" + cr.from() + ";" + cr.to() + "[: " + Converter.toEuros(revenue(aId, cr).getValue()));
                    }
                    if (!a.instantRevenues().isEmpty()) {
                        int id = automaton.get(aId).instant();
                        System.out.println("\t" + a.pretty() + ": " + Converter.toEuros(revenues[id].getValue()));
                    }
                }
                System.out.println("States:");
                for (int aId = 0; aId < allStates.length; aId++) {
                    List<List<WM>> states = new ArrayList<>();
                    ActivityAutomaton a = automaton.get(aId);
                    for (int t = 0; t < nbSlots; t++) {
                        State st = a.state(allStates[aId][t].getValue());
                        List<WM> levels = new ArrayList<>();
                        for (PerfLevel pf : st.perfLevels()) {
                            WM w = pf.WM();
                            levels.add(w);
                        }
                        states.add(levels);
                    }
                    System.out.println("\t" + a.job.pretty() + ": " + merge(states));
                }

                //change the heuristic if the solution was the first computed
            });
        }

        SMF.limitTime(s, timeLimit() * 1000);

        if (ipp == null) {
            //The search heuristic
            List<IntStrategy> strats = new ArrayList<>();
            //We assumes the perfs goes up, but that's a free assumption
            //we choose the cheapest sources first
            strats.addAll(greenerFirst());
            s.set(new StrategiesSequencer(strats.toArray(new IntStrategy[strats.size()])));
        } else {
            List<IntStrategy> strats = new ArrayList<>();
            strats.addAll(ippBased());
            s.set(new StrategiesSequencer(strats.toArray(new IntStrategy[strats.size()])));
        }

        if (opt) {
            s.findOptimalSolution(ResolutionPolicy.MAXIMIZE, profit);
        } else {
            s.findSolution();
        }
        return s.isFeasible();
    }

    private List<IntStrategy> ippBased() {
        List<IntStrategy> strats = new ArrayList<>();
        int[] usage = new int[nbSlots];
        Integer[] ts = new Integer[nbSlots];
        for (int i = 0; i < ts.length; i++) {
            ts[i] = i;
        }
        for (PowerSource s : ipp) {
            for (int t = 0; t < s.slots().length; t++) {
                usage[t] += s.slots()[t].peak();
            }
        }
        Arrays.sort(ts, new PeakUsageComparator(usage));
        for (int t : ts) {
            //State per activity at t
            IntVar[] where = new IntVar[allStates.length];
            for (int i = 0; i < allStates.length; i++) {
                where[i] = allStates[i][t];
            }

            if (!dynCosts.isEmpty() && dynCosts.get(0) instanceof RenPct) {
                RenPct r = (RenPct) dynCosts.get(0);
                int best = r.basePerf();
                strats.add(custom(IntStrategyFactory.minDomainSize_var_selector(),
                        profitBased ?
                                new WorkingModeSelector2(this, where, best, t) :
                                new WorkingModeSelector(this, where, best, t)
                        , where));
            } else {
                strats.add(custom(IntStrategyFactory.minDomainSize_var_selector(), mid_value_selector(true), where));
            }
            //now the power dispatch. Greener sources in first
            IntVar[] where2 = new IntVar[sourcePowerUsage.length];
            int x = 0;
            for (int s : sourceByRenPct(t)) {
                where2[x] = sourcePowerUsage[s][t];
                x++;
            }
            strats.add(minDom_UB(where2));
        }
        return strats;
    }

    private Integer[] slotsByRenCapa(boolean asc) {
        //if there is a pure ren source, use the capa
        //otherwise, use the %
        List<PowerSource> pures = new ArrayList<>();
        for (PowerSource s : sources) {
            boolean pure = true;
            for (int t = 0; t < nbSlots; t++) {
                if (s.slots()[t].renPct() != 100) {
                    pure = false;
                    break;
                }
            }
            if (pure) {
                pures.add(s);
            }
        }
        Integer[] ts = new Integer[nbSlots];
        for (int i = 0; i < ts.length; i++) {
            ts[i] = i;
        }

        Arrays.sort(ts, !pures.isEmpty() ? new RenCapaComparator(pures, asc) : new RenPctComparator(sources, asc));
        return ts;
    }


    private Integer[] sourceByRenPct(int t) {
        Integer[] ts = new Integer[this.sources.size()];
        for (int i = 0; i < ts.length; i++) {
            ts[i] = i;
        }
        Arrays.sort(ts, new RenPctSourceComparator(t, sources));
        return ts;
    }

    private List<IntStrategy> greenerFirst() {
        List<IntStrategy> strats = new ArrayList<>();
        for (int t : slotsByRenCapa(false)) {
            //State per activity at t
            IntVar[] where = new IntVar[allStates.length];
            for (int i = 0; i < allStates.length; i++) {
                where[i] = allStates[i][t];
            }

            if (!dynCosts.isEmpty() && dynCosts.get(0) instanceof RenPct) {
                RenPct r = (RenPct) dynCosts.get(0);
                int best = r.basePerf();
                strats.add(custom(IntStrategyFactory.minDomainSize_var_selector(),
                        profitBased ?
                                new WorkingModeSelector2(this, where, best, t) :
                                new WorkingModeSelector(this, where, best, t)
                        , where));
            } else {
                strats.add(custom(IntStrategyFactory.minDomainSize_var_selector(), mid_value_selector(true), where));
            }
            //now the power dispatch. Greener sources in first
            IntVar[] where2 = new IntVar[sourcePowerUsage.length];
            int x = 0;
            for (int s : sourceByRenPct(t)) {
                where2[x] = sourcePowerUsage[s][t];
                x++;
            }
            strats.add(minDom_UB(where2));

        }
        return strats;
    }

    private String merge(List<List<WM>> states) {
        StringBuilder b = new StringBuilder();
        int from = 0;
        for (int t = 1; t < nbSlots; t++) {
            if (!states.get(t).equals(states.get(from)) || t == nbSlots - 1) {
                if (t == nbSlots - 1) {
                    b.append("[").append(from).append("-").append(t).append("]");
                } else {
                    if (t != from + 1) {
                        b.append("[").append(from).append("-").append(t).append("[");
                    } else {
                        b.append(from);
                    }
                }
                b.append(":");
                for (WM wm : states.get(t - 1)) {
                    b.append(wm.getDc()).append("/").append(wm.name()).append(";");
                }
                b.append(" ");
                from = t;
            }
        }
        return b.toString();
    }

    public List<PowerSource> sortByPrice(final int t) {
        List<PowerSource> l = new ArrayList<>(sources);
        Collections.sort(l, (s1, s2) -> s1.slots()[t].price() - s2.slots()[t].price());
        return l;
    }

    private void capPower3() {
        for (String dc : dcs) {
            List<PowerSource> sources = getSources(dc);
            int dcId = dc(dc);
            for (int t = 0; t < nbSlots; t++) {
                List<IntVar> capacities = new ArrayList<>();
                for (PowerSource src : sources) {
                    int sId = powerSource(src);
                    IntVar[] vs = sourcePowerUsage[sId];
                    capacities.add(vs[t]);
                }

                List<IntVar> consumptions = new ArrayList<>();
                for (IntVar[][] vs : activitiesPowerUsage) {
                    consumptions.add(vs[t][dcId]);
                }

                IntVar cons = sum("power_cons(dc=" + dc + ",t=" + t + ")", consumptions);
                IntVar capa = sum("power_capa(dc=" + dc + ",t=" + t + ")", capacities);
                s.post(ICF.arithm(cons, "=", capa));
            }

        }
    }

    private IntVar[] at(IntVar[][] g, int i) {
        IntVar[] res = new IntVar[g.length];
        for (int j = 0; j < res.length; j++) {
            res[j] = g[j][i];
        }
        return res;
    }

    private IntVar [] makeStateVariables(Solver s, int nbSlots, ActivityAutomaton a, int nbStates) {
        IntVar [] vars = new IntVar[nbSlots];
        for (int i = 0; i < nbSlots; i++) {
            vars[i] = VF.enumerated("state('" + a.activity().pretty() + "', @" + i + ")", 0, nbStates - 1, s);
        }
        return vars;
    }

    /**
     * Get the power consumption of a given activity automaton.
     *
     * @param s      the solver
     * @param states the state at each time slot.
     * @param aa     the activity.
     * @return the power consumed by the activity at each state over each timeslot
     */
    private IntVar[][] powerConsumption2(Solver s, IntVar[] states, ActivityAutomaton aa, int id) {
        IntVar[][] usage = new IntVar[states.length][aa.job.parts().size()];
        int[][] powerByDCs = new int[nbDcs][states.length];
        int[][] bounds = new int[nbDcs][2];
        for (int d = 0; d < nbDcs; d++) {
            bounds[d] = new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE};
            powerByDCs[d] = aa.powerByDcs(dcs).get(dcs[d]);
            for (int st = 0; st < powerByDCs[d].length; st++) {
                bounds[d][0] = Math.min(powerByDCs[d][st], bounds[d][0]);
                bounds[d][1] = Math.max(powerByDCs[d][st], bounds[d][1]);
            }
        }
        for (int t = 0; t < states.length; t++) {
            IntVar[] cons = new IntVar[nbDcs];
            int min = 0;
            int max = 0;
            for (int d = 0; d < nbDcs; d++) {
                cons[d] = VF.bounded("pow('" + aa.activity().pretty() + "', '" + dcs[d] + "', @" + t + ")", bounds[d][0], bounds[d][1], s);
                among(s, cons[d], powerByDCs[d], states[t]);
                min += bounds[d][0];
                max += bounds[d][1];
            }
            usage[t] = cons;
            globalActivitiesPowerUsage[id][t] = VF.bounded("pow('" + aa.activity().pretty() + "',@" + t + ")", min, max, s);
            s.post(ICF.sum(cons, globalActivitiesPowerUsage[id][t]));
        }
        return usage;
    }

    private int peakCapacityAt(int t, String d) {
        int c = 0;
        for (PowerSource s : sources) {
            if (s.dcId().equals(d)) {
                c += s.slots()[t].peak();
            }
        }
        return c;
    }

    private void checkSchedulability(ActivityAutomaton aa) {
        for (int t = 0; t < nbSlots; t++) {
            //Get the peak usage per DC
            Set<State> feasible = new HashSet<>();
            for (int s = 0; s < aa.nbStates(); s++) {
                State st = aa.state(s);
                boolean ok = true;
                for (PerfLevel pl : st.perfLevels()) {
                    int peak = peakCapacityAt(t, pl.WM().getDc());
                    if (pl.power() > peak) {
                        ok = false;
                    }
                }
                if (ok) {
                    feasible.add(st);
                }
            }
            if (feasible.isEmpty()) {
                throw new ConsolidatorException("No working mode schedulable for activity '" + aa.job.pretty() + "' at time slot " + t);
            }
        }
    }

    /**
     * Get the energy price per time-slot depending on its usage.
     * @param usages the energy usage
     * @param src the power source
     * @return the energy price per time-slot
     */
    private IntVar[] energyPrice(IntVar[] usages, PowerSource src) {
        IntVar [] price = new IntVar[usages.length];
        for (int i = 0; i< usages.length; i++) {
            price[i] = VF.scale(usages[i], src.slots()[i].price());
        }
        return price;
    }

    private IntVar[] makePowerUsage(PowerSource source, int cap) {
        IntVar[] power = new IntVar[source.slots().length];
        int i = 0;
        for (PowerSourceSlot slot : source.slots()) {
            power[i] = VF.bounded("pow('" + source.pretty() + "', @" + i + ")", 0, Math.min(cap, slot.peak()), s);
            i++;
        }
        return power;
    }

    /**
     * Link the value variable to the possible one.
     * If all the values in the table are identical, then the variable is instantiated.
     * Otherwise, an element constraint is posted.
     *
     * @param value the variable
     * @param table the possible values for the variable
     * @param index the index in the table that indicate the value.
     */
    public static void among(Solver s, IntVar value, int[] table, IntVar index) {
        if (!constant(table)) {
            s.post(ICF.element(value, table, index));
        } else {
            s.post(new Arithmetic(value, Operator.EQ, table[0]));
        }
    }

    public void roundProfit(final int euro) {
        s.plugMonitor((IMonitorSolution) () -> {
            if (s.getObjectives() != null) {
                IntVar profit = (IntVar) s.getObjectives()[0];
                s.post(ICF.arithm(profit, ">=", profit.getValue() + euro * Converter.EUR_TO_INT));
            }
        });
    }

    public static boolean constant(int [] values) {
        int v = values[0];
        for (int v2 : values) {
            if (v2 != v) {
                return false;
            }
        }
        return true;
    }

    public List<ActivityAutomaton> getAutomaton() {
        return automaton;
    }

    /**
     * Get the states per activity.
     *
     * @return an array [idActivity][slotId];
     */
    public IntVar[][] getAllStates() {
        return allStates;
    }

    /**
     * Get the activity from his identifiers.
     *
     * @param easc the easc identifier
     * @param n    the activity name
     * @return the associated activity or {@code null} if there is no match
     */
    public MyActivity activity(String easc, String n) {
        for (MyActivity a : activities) {
            if (a.easc().equals(easc) && a.name().equals(n))
            return a;
        }
        return null;
    }

    /**
     * Get the power consumed per activity and per time-slot.
     * @return an array[activityId][timeSlotId][dcId]
     */
    public IntVar[][][] getActivitiesPowerUsage() {
        return activitiesPowerUsage;
    }

    /**
     * Get the power consumed per source and per time-slot.
     * @return an array[sourceId][timeSlotId]
     */
    public IntVar[][] getSourcePowerUsage() {
        return sourcePowerUsage;
    }

    public IntVar[][] getAllEnergyPrice() {
        return allEnergyPrice;
    }

    public IntVar getProfit() {
        return profit;
    }

    /**
     * Set the energy quota for a list of activities
     *
     * @param from beginning of the window (inclusive)
     * @param to   end of the window (exclusive)
     * @param aIds the activity identifiers
     * @param dcId the datacenter identifier
     * @param q    the energy quota
     * @throws ConsolidatorException if the constraint is not feasible
     */
    public void setEnergyQuota(int from, int to, int[] aIds, String dcId, int q) {
        List<IntVar> usage = new ArrayList<>();//[aIds.length * (to - from)];
        int min = 0;
        int max = 0;
        int x = 0;
        for (int t = from; t < to; t++) {
            for (int a : aIds) {
                usage.add(activitiesPowerUsage[a][t][dc(dcId)]);
                    x++;
                min += activitiesPowerUsage[a][t][dc(dcId)].getLB();
                max += activitiesPowerUsage[a][t][dc(dcId)].getUB();
            }
        }
        if (min > q) {
            throw new ConsolidatorException("The min energy consumption is '" + min + "'");
        }
        if (q < max) {
            IntVar en = sum("energy_quota(aIds=" + prettyActivities(aIds) + ", dc=" + dcId + ", range=[" + from + ";" + to + "[)", usage);
            s.post(ICF.arithm(en, "<=", VF.fixed(q, s)));
        }
    }

    /**
     * Set the cumulative power quota for a list of activities
     * @param at the time-slot to constrain
     * @param ids the activity identifiers
     * @param dcId the datacenter identifier
     * @param pow the peak power usage
     * @throws ConsolidatorException if the constraint is not feasible
     */
    public void setPowerQuota(int at, int[] ids, String dcId, int pow) {
        //The usage at time-slot 'at' for all the activities
        List<IntVar> usage = new ArrayList<>();
        int lb = 0;
        int ub = 0;
        for (int i = 0; i < ids.length; i++) {
            lb += activitiesPowerUsage[ids[i]][at][dc(dcId)].getLB();
            ub += activitiesPowerUsage[ids[i]][at][dc(dcId)].getUB();
            usage.add(activitiesPowerUsage[ids[i]][at][dc(dcId)]);
        }
        if (lb > pow) {
            throw new ConsolidatorException("The min power consumption is '" + lb + "'");
        }
        if (ub > pow) {
            IntVar pwr = VF.bounded("pow_quota(aIds=" + prettyActivities(ids) + ", dc=" + dcId + ", @" + at + ")", lb, pow, s);
            s.post(ICF.sum(usage.toArray(new IntVar[usage.size()]), pwr));
        }
    }

    public void add(DynCost p) {
        dynCosts.add(p);
        costs.add(p.inject(this));
    }

    public List<DynCost> getDynamicCosts() {
        return dynCosts;
    }

    public int getNbSlots() {
        return nbSlots;
    }

    public List<PowerSource> getSources() {
        return sources;
    }

    /**
     * Get the power sources of a given datacenter
     *
     * @param dcId the datacenter identifier
     * @return a list of sources
     */
    public List<PowerSource> getSources(String dcId) {
        return sources.stream().filter(p -> p.dcId().equals(dcId)).collect(Collectors.toList());
    }

    public List<PowerSource> getSources (int id) {
        String dcId = dcs[id];
        return sources.stream().filter(p -> p.dcId().equals(dcId)).collect(Collectors.toList());
    }
    /**
     * Get all the power source identifiers.
     *
     * @param sources the power sources
     * @return the associated identifiers
     */
    public int[] powerSources(List<PowerSource> sources) {
        int[] ids = new int[sources.size()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = powerSource(sources.get(i));
        }
        return ids;
    }

    /**
     * Get the power source identifier.
     *
     * @param src the power source
     * @return its identifier. {@code -1} if the source is unknown
     */
    public int powerSource(PowerSource src) {
        for (int i = 0; i < sources.size(); i++) {
            if (src == sources.get(i)) {
                return i;
            }
        }
        return -1;
    }
    public Logger getLogger() {
        return LOGGER;
    }

    public Solver getSolver() {
        return s;
    }

    public IntVar perf(int aId, CumulativeRevenue p) {
        int rId = automaton.get(aId).cumulative(p);
        return resources[aId][rId];
    }

    public IntVar revenue(int aId, CumulativeRevenue p) {
        int rId = automaton.get(aId).cumulative(p);

        return revenues[aId][rId];
    }


    public IntVar instantRevenue(int aId) {
        int rId = automaton.get(aId).instant();
        if (rId >= 0) {
            return revenues[aId][rId];
        }
        return null;
    }

    public int[] instantPerf(int aId) {
        int[] perfs = new int[nbSlots];
        ICostAutomaton ca = costAutomatons.get(aId);
        //costs[t][j][2][i] = pricing.price(perf);
        //layer_value_resource_state
        int from = ca.getInitialState(); //current state
        ActivityAutomaton aa = automaton.get(aId);
        for (int t = 0; t < nbSlots; t++) {
            int st = val(allStates[aId][t]);
            perfs[t] = aa.state(st).perf() - aa.transitionCost(from, st);
        }
        return perfs;
    }

    public int val(IntVar var) {
        return s.getSolutionRecorder().getLastSolution().getIntVal(var);
    }

    public String prettyActivities(int[] aIds) {
        StringBuilder b = new StringBuilder().append("[");
        for (int i = 0; i < aIds.length; i++) {
            b.append(activities.get(i).pretty());
            if (i < aIds.length - 1) {
                b.append(",");
            }
        }
        return b.append("]").toString();
    }

    public int dc(String n) {
        for (int d = 0; d < nbDcs; d++) {
            if (dcs[d].equals(n)) {
                return d;
            }
        }
        return -1;
    }

    public IntVar sum(String n, List<IntVar> ops) {
        return sum(n, ops.toArray(new IntVar[ops.size()]));
    }

    public IntVar sum(String n, IntVar[] ops) {
        if (ops.length == 0) {
            return VF.fixed(n, 0, s);
        }
        int lb = 0;
        int ub = 0;
        List<IntVar> l = new ArrayList<>();
        for (int i = 0; i < ops.length; i++) {
            IntVar op = ops[i];
            if (!op.isInstantiatedTo(0)) {
                l.add(op);
            }
            lb += op.getLB();
            ub += op.getUB();
        }
        if (ub < lb) {
            throw new IllegalArgumentException("Unable to state the sum of the variables equals to " + n + ". Bounds: [" + lb + ";" + ub + "]");
        }
        if (l.size() == 1) {
            return l.get(0);
        }
        IntVar v = VF.bounded(n, lb, ub, s);
        if (l.size() == 2) {
            VF.task(l.get(0), l.get(1), v);
        } else {
            s.post(ICF.sum(l.toArray(new IntVar[l.size()]), v));
        }
        return v;
    }

    /**
     * Get the timeLimit.
     *
     * @return a value in second
     */
    public long timeLimit() {
        return timeLimit;
    }

    /**
     * Set the time limit.
     *
     * @param t the limit in seconds
     * @return {@code this}
     */
    public OPCP2 timeLimit(long t) {
        timeLimit = t;
        return this;
    }

    public void ippHeuristic(List<PowerSource> ipp) {
        this.ipp = ipp;
    }

    public void pretty(boolean b) {
        pretty = b;
    }

    public int maxRevenues() {
        int nb = 0;
        for (ActivityAutomaton a : automaton) {
            for (InstantRevenue i : a.job.instantRevenues()) {
                nb += i.basePrice();
            }
            for (CumulativeRevenue c : a.job.cumulativeRevenues()) {
                nb += c.basePrice();
            }
        }
        return nb;
    }

    public IntVar getRevenues() {
        return globalRevenues;
    }

    public IntVar getGlobalCost() {
        return globalCost;
    }

    public IntVar getGlobalEnergy() {
        return globalEnergy;
    }

    public String[] getDatacenters() {
        return dcs;
    }
}