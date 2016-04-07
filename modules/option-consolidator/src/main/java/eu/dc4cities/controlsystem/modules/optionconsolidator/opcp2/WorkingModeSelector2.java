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

import eu.dc4cities.controlsystem.model.easc.Relocability;
import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.variables.IntVar;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Select the working mode to try depending on
 * the renPct goal.
 * If the slot is over the renPCT baseline, go for max performance
 * otherwise, go for the least performance.
 */
public class WorkingModeSelector2 implements IntValueSelector {

    private int expect;

    private IntVar[] where;

    private int timeSlot;

    private OPCP2 pb;

    private List<RenPct> renPcts;

    private boolean pureAvailable = false;

    private Map<IntVar, IntVar> revenues;
    public WorkingModeSelector2(OPCP2 pb, IntVar[] where, int expect, int timeSlot) {
        this.expect = expect;
        this.timeSlot = timeSlot;
        this.pb = pb;
        this.where = where;
        renPcts = new ArrayList<>();
        for (DynCost d : pb.getDynamicCosts()) {
            if (d instanceof RenPct) {
                renPcts.add((RenPct)d);
            }
        }
        for (PowerSource s : pb.getSources()) {
            if (s.isPure()) {
                pureAvailable = true;
            }
        }
        revenues = new HashMap<>();
        for (int aId = 0; aId < pb.getAutomaton().size(); aId++) {
            for (IntVar r : pb.revenues(aId)) {
                for (int t = 0; t < pb.getNbSlots(); t++) {
                    IntVar at = pb.getAllStates()[aId][t];
                    revenues.put(at, r);
                }
                if (pb.revenues(aId).length > 1) {
                    throw new IllegalArgumentException("buggy");
                }
            }
        }
    }

    /**
     * Try to find the most performant state to go, that will not waste power.
     * Basically, that will go over the awaited base performance
     *
     * @param v
     * @return
     */
    private int lastWorthy(IntVar v) {
        int aId = toAid(v);
        ActivityAutomaton auto = pb.getAutomaton().get(aId);


        if (!auto.activity().instantRevenues().isEmpty()) {
            //if instantaneous or pick the last state that ensure the SLO
            InstantRevenue ir = auto.activity().instantRevenues(timeSlot);
            int max = ir.basePerf();
            int ub = v.getUB();
            boolean reached = false;
            for (int i = v.getLB(); i <= ub; i = v.nextValue(i)) {
                if (auto.state(i).perf() > ir.basePerf()) {
                    if (reached) {
                        //Cannot return outside the loop in case i is outside v
                        return i;
                    } else {
                        reached = true;
                    }
                }
            }
            //System.out.println("UB was " + auto.state(ub) + " downgraded to " + auto.state(i) + " with slo=" + max);
        }
        return v.getUB();
    }

    public int range(IntVar v) {
        return Math.abs(v.getUB() - v.getLB());
    }

    private List<String> byPureProd(ActivityAutomaton aa) {
        int minW = aa.wattBounds()[0];
        List<PowerSource> pures = pb.getSources().stream().filter((s) -> s.isPure()).collect(Collectors.toList());
        Collections.sort(pures, (a, b) -> b.slots()[timeSlot].peak() - a.slots()[timeSlot].peak());
        List<String> names = new ArrayList<>();
        for (PowerSource src : pures) {
            names.add(src.dcId());
//            System.out.println("at " + timeSlot + ": " + src.slots()[timeSlot].peak());
        }
/*        if (pures.get(0).slots()[timeSlot].peak() < minW) {
            System.out.println("Don't care about pure here. Ask f");
        }
        System.out.println();*/
        return names;
    }

    private RenPct pctForDc(String dc) {
        for (RenPct p : renPcts) {
            if (p.datacenterId().equals(dc)) {
                return p;
            }
        }
        return null;
    }

    private int mid(IntVar v) {
        return (v.getUB() + v.getLB()) / 2;
    }

    private List<String> byTotalPct() {
        List<String> dcs = Arrays.asList(pb.getDatacenters());
        //Collections.sort(dcs, (a, b) -> pctForDc(b).percentage().getUB() - pctForDc(a).percentage().getUB());
        Collections.sort(dcs, (a, b) -> mid(pctForDc(b).percentage()) - mid(pctForDc(a).percentage()));
        return dcs;
    }

    private List<State> maxAt(List<State> options, int cap, String dc) {
        Optional<State> max = options.stream()
                .max((a, b) -> a.perfLevel(dc).perf() - b.perfLevel(dc).perf());
        return options.stream()
                .filter(s -> s.perfLevel(dc).perf() == max.get().perfLevel(dc).perf())
                .collect(Collectors.toList());
    }

    private List<State> minAt(List<State> options, int cap, String dc) {
        Optional<State> min = options.stream()
                .min((a, b) -> a.perf() - b.perf());
        return options.stream()
                .filter(s -> s.perf() == min.get().perf())
                .collect(Collectors.toList());
    }

    private List<State> states(IntVar v) {
        int aId = toAid(v);
        int ub = v.getUB();
        List<State> states = new ArrayList<>();
        ActivityAutomaton auto = pb.getAutomaton().get(aId);
        for (int i = v.getLB(); i <= ub; i = v.nextValue(i)) {
            states.add(auto.state(i));
        }
        return states;
    }

    private State toGreener(IntVar v, int need) {
        List<State> options = states(v);
        for (String dc : byTotalPct()) {
            //System.out.println("opts:" + options);
            RenPct pct = pctForDc(dc);
            //mid > threshold, catch a high working mode
            double th = pct.percentage().getUB() / 10;
            if (th >= pct.basePerf()) {
                //upper mode that is close to need
                //  System.out.println("UP for " + pct);
                options = maxAt(options, need, dc);
            } else {
                //lower bound
                //System.out.println("Down for " + pct);
                options = minAt(options, need, dc);
            }
        }
        //System.out.println("need:" + need + " got:" + options);
        return options.get(0);
    }

    private State dispatch(List<String> names, List<State> viables, int need) {
        //System.out.println("At " + timeSlot + " dcs=" + names + " viables=" + viables);
        List<State> remainders = new ArrayList<>(viables);
        for (String dc : names) {
            Optional<State> max = remainders.stream()
                    .max((a, b) -> a.perfLevel(dc).perf() - b.perfLevel(dc).perf());
            remainders = remainders.stream()
                    .filter(s -> s.perfLevel(dc).perf() == max.get().perfLevel(dc).perf())
                    .collect(Collectors.toList());
            //Get only the minimum global perfs
            Optional<State> min = remainders.stream()
                    .min((a, b) -> a.perf() - b.perf());
            remainders = remainders.stream()
                    .filter(s -> s.perf() == min.get().perf())
                    .collect(Collectors.toList());
        }
        State st = remainders.get(0);
        String firstDc = names.get(0);
        int minWatts = st.perfLevel(firstDc).power();
        for (PowerSource s : pb.getSources(firstDc)) {
            if (s.isPure() && s.slots()[timeSlot].peak() < minWatts) {
                //System.err.println(timeSlot +" abuse pure at " + firstDc + " : ask=" + minWatts + " prod=" + s.slots()[timeSlot].peak());
                //Switch to a min value
            }
        }
        //System.out.println("need:" + need + " got:" + remainders);
        return remainders.get(0);
    }

    private State migrate(int aId, CumulativeRevenue cr, List<String> dcs, List<State> viables, int need) {
        IntVar r = pb.revenue(aId, cr);
        String dc = dcs.get(0);

        State got = viables.get(0);
        for (State st : viables) {
            PerfLevel p = st.perfLevel(dc);
            if (p != null) {
                if (r.getLB() >= need && (got == null || p.perf() < got.perf())) {
                    got = st;
                } else if (r.getLB() < need && (got == null || p.perf() > got.perf())) {
                    got = st;
                }
            }
        }
        return got;
    }


    private int greenDatacenter(IntVar v) {
        int aId = toAid(v);


        ActivityAutomaton auto = pb.getAutomaton().get(aId);
        int need;
        if (!auto.activity().instantRevenues().isEmpty()) {
            need = auto.activity().instantRevenues(timeSlot).basePerf();
        } else {
            need = auto.activity().cumulativeRevenues().get(0).basePerf();
        }
        List<State> viables = new ArrayList<>();
        int ub = v.getUB();
        for (int i = v.getLB(); i <= ub; i = v.nextValue(i)) {
            if (auto.state(i).perf() >= need) {
                viables.add(auto.state(i));
            }
        }
        if (viables.isEmpty()) {
            viables.add(auto.state(v.getUB()));
        }

        if (auto.activity().relocatibility() == Relocability.SPREADABLE) {
            return auto.state(dispatch(byPureProd(auto), viables, need).name());
        } else {
            CumulativeRevenue cr = auto.activity().cumulativeRevenues().get(0);
            return auto.state(migrate(aId, cr, byPureProd(auto), viables, need).name());
        }
        //return auto.state(toGreener(v, need).name());
    }

    /*
     //get the states max in the current DC
     //then max in the second dc
     //...
     */

    private int greenDatacenter2(IntVar v) {
        int aId = toAid(v);

        ActivityAutomaton auto = pb.getAutomaton().get(aId);
        int need = auto.activity().instantRevenues(timeSlot).basePerf();
        List<State> viables = new ArrayList<>();
        int ub = v.getUB();
        boolean reached = false;

        /*
        -> browse les état qui satisfont ir
        -> pick l'endroit ou production solaire
        -> qui l'état avec la plus grosse prod pour cet endroit
        */
        for (int i = v.getLB(); i <= ub; i = v.nextValue(i)) {
            if (auto.state(i).perf() >= need) {
                viables.add(auto.state(i));
            }
        }

        if (viables.isEmpty()) {
            viables.add(auto.state(v.getUB()));
            // System.err.println("No viable state at t=" + timeSlot + " need=" + need + " add " + viables.get(0));
        }
        int maxProd = 0;
        String bestLocation = null;
        for (int d = 0; d < pb.getNbDatacentres(); d++) {
            List<PowerSource> localSources = pb.getSources(d);
            for (PowerSource s : localSources) {
                int sId = pb.powerSource(s);
                if (s.isPure()) {
                    //int freeWatts = s.slots()[timeSlot].peak() - pb.getSourcePowerUsage()[sId][timeSlot].getLB();
                    int freeWatts = s.slots()[timeSlot].peak();
                    if (freeWatts > maxProd) {
                        maxProd = freeWatts;
                        bestLocation = s.dcId();
                    }
                }
            }
        }
        if (bestLocation == null) {
            //System.err.println("No sun at t=" + timeSlot + "; maxProd=" + maxProd + "; pick" +auto.state(lastWorthy(v)));
            //no sun, nowhere
            return lastWorthy(v);
        }
        //Catch the PerfLevel among where the perf is at the greatest for bestLocation
        State bestState = viables.get(0);
        for (State st : viables) {
            if (st.perfLevel(bestLocation).perf() > bestState.perfLevel(bestLocation).perf()) {
                bestState = st;
            }
        }

        //among equals values, go with the lowest general perf, so minimize weight of other sites
        for (State st : viables) {
            if (st.perfLevel(bestLocation).perf() == bestState.perfLevel(bestLocation).perf() && st.power() < bestState.power()) {
                bestState = st;
            }
            //System.out.println("Candidate: " + st);
        }
        //System.out.println("Best candidate: " + bestState + " slo=" + need);
        return auto.state(bestState.name());
    }

    @Override
    public int selectValue(IntVar v) {
        int aId = toAid(v);
        ActivityAutomaton auto = pb.getAutomaton().get(aId);

        IntVar r = revenues.get(v);
        //IntVar r = pb.getRevenues();
        //System.out.println("income range: " + r + " objective range:" + pb.getGlobalCost());
        if (range(r) > range(pb.getGlobalCost())) {
            //bigger progression by maximizing the revenue
            //System.out.println("inc cause revenue " + r);
            return v.getUB();
        } else {
            //bigger progression by maximising the objective
            if ((maxRenPct(v) || worthy(v)) && leftToDo(toAid(v))) {
                //System.out.println("inc cause objective" + renPct);
                if (pb.getNbDatacentres() > 1) {
                    return greenDatacenter(v);
                }
                return v.getUB();
            }
        }
        //System.out.println("dec cause objective or revenue capped");
        return v.getLB();
    }


    /*@Override
    public int selectValue(IntVar v) {


        IntVar r = revenues.get(v);
        //IntVar r = pb.getRevenues();
        //System.out.println("income range: " + r + " objective range:" + pb.getGlobalCost());
        if (range(r) > range(pb.getGlobalCost())) {
            //bigger progression by maximizing the revenue
            //System.out.println("inc cause revenue " + r);
            return v.getUB();
        } else {
            //bigger progression by maximising the objective
            if ((maxRenPct(v) || worthy(v)) && leftToDo(toAid(v))) {
                //System.out.println("inc cause objective" + renPct);
                if (pb.getNbDatacentres() > 1 && !auto.activity().instantRevenues().isEmpty()) {
                    return greenDatacenter(v);
                }
            }
            //System.out.println(timeSlot + " inc cause revenue " + r);
            return v.getUB();
        }
        //System.out.println("dec cause objective or revenue capped");
        return v.getLB();
    }*/

    private boolean leftToDo(int aId) {
        for (CumulativeRevenue cr : pb.getAutomaton().get(aId).job.cumulativeRevenues()) {
            IntVar left = pb.revenue(aId, cr);
            if (left.isInstantiated()) {
                //System.out.println("nothing left to do");
                return false;
            }
        }
        return true;
    }

    private int toAid(IntVar v) {
        int aId = 0;
        for (IntVar v2 : where) {
            if (v2 == v) {
                return aId;
            }
            aId++;
        }
        return -1;
    }

    private double average(PowerSource src) {
        int sum = 0;
        for (int t = 0; t < pb.getNbSlots(); t++) {
            sum += src.slots()[t].renPct();
        }
        return sum / pb.getNbSlots();
    }

    private boolean maxRenPct(IntVar v) {
        /*if (renPct.entailed()) {
            return true;
        }*/
        for (PowerSource s : pb.getSources()) {
            PowerSourceSlot slot = s.slots()[timeSlot];
            //possible good ren sources
            int sId = pb.powerSource(s);
            int dcId = pb.dc(s.dcId());
            //System.out.println("available %: " + slot.renPct() + " ; expect=" +expect + " pct= " + renPct.percentage());
            //System.out.println("available power: " + pb.getSourcePowerUsage()[sId][timeSlot]);
            if (slot.renPct() >= expect && slot.peak() > 0) {
                //System.out.println("Possible capacity: " + pb.getSourcePowerUsage()[sId][timeSlot]+ " usage=" + pb.getActivitiesPowerUsage()[toAid(v)][timeSlot][dcId]);

                if (pb.getActivitiesPowerUsage()[toAid(v)][timeSlot][dcId].getLB() > pb.getSourcePowerUsage()[sId][timeSlot].getUB()) {
                    //System.out.println("no enough power"); //because we will use it a bit
                    return false;
                }
                return true;
            }
            //System.out.println("Not green enough");
        }
        return false;
    }

    private boolean worthy(IntVar v) {
        for (PowerSource s : pb.getSources()) {
            int available = s.slots()[timeSlot].renPct();
            double avg = average(s);
            //System.out.println("available: " + available + " ; avg=" + avg + " " + " pct= " + renPct.percentage());
            if (avg > available) {
                //below the average. Lets try the next source
                continue;
            }

            //possible good ren sources
            int sId = pb.powerSource(s);
            int dcId = pb.dc(s.dcId());
            //System.out.println("Possible capacity: " + pb.getSourcePowerUsage()[sId][timeSlot]+ " with ren = " + slot.renPct());
            if (pb.getActivitiesPowerUsage()[toAid(v)][timeSlot][dcId].getLB() > pb.getSourcePowerUsage()[sId][timeSlot].getUB()) {
                //System.out.println("no enough power");
                continue;
            }
            //System.out.println("worthy");
            return true;
        }
        //System.out.println("not worthy");
        return false;
    }
}
