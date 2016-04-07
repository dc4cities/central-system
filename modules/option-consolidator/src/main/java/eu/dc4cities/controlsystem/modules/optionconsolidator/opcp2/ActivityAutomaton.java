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

import eu.dc4cities.controlsystem.model.easc.ForbiddenState;
import eu.dc4cities.controlsystem.model.easc.ForbiddenWorkingMode;
import eu.dc4cities.controlsystem.model.easc.Relocability;
import eu.dc4cities.controlsystem.modules.optionconsolidator.AllTuplesGenerator;
import org.chocosolver.solver.constraints.nary.automata.FA.CostAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.FiniteAutomaton;
import org.chocosolver.solver.constraints.nary.automata.FA.ICostAutomaton;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generate the automata associated to a given activity.
 *
 * For each working mode and performance level, a {@link State} is created.
 * Depending on the relocability capabilities of the activities, the states are then linked each to the other.
 *
 * An activity with a relocability equals to NO. It is expected to have the activity hosted on a single datacenter.
 * The resulting automata is a clique.
 *
 * An activity with a relocability equals to MIGRATABLE can be executed over multiple datacenters.
 * In practice, a state is composed of a running working mode on one datacenter and default working modes for the other datacenters.
 * This reflects the fact a MIGRATABLE activity runs on a single datacenter. When a transition denotes a migration, a cost is applied from {@link MyActivity#migrationCost()}.
 *
 * An activity with the relocability equals to SPREADABLE can be executed over multiple datacenters.
 * For each datacenter, the working mode is however not necessarily the default working mode.
 *
 *
 *
 */
public class ActivityAutomaton {

    MyActivity job;

    private FiniteAutomaton fa;

    private State[] states;

    private int[][] transitionCosts;

    private int nbCumulatives;

    private List<InstantRevenue> instant;

    private List<CumulativeRevenue> cumulatives;

    private int RC_INSTANT = -1;
    /**
     * Make a new generator
     *
     * @param j the activity to manipulate
     */
    public ActivityAutomaton(MyActivity j) {
        job = j;
        fa = makeAutomaton();
        makeTransitionCosts();

        //Energy consumption + 1 Rc per cumulative Pricing (+ 1 for instant pricing)
        nbCumulatives = j.cumulativeRevenues().size();
        if (!j.instantRevenues().isEmpty()) {
            RC_INSTANT = nbCumulatives;
        }
        instant = j.instantRevenues();
        cumulatives = j.cumulativeRevenues();
    }

    private void makeTransitionCosts() {
        transitionCosts = new int[fa.getNbStates()][fa.getNbStates()];
        int x = 0;
        for (State from : states) {
            int y = 0;
            for (State to : states) {
                for (int i = 0; i < from.perfLevels().size(); i++) {
                    WM fromWM = from.perfLevels().get(i).WM();
                    WM toWM = to.perfLevels().get(i).WM();
                    int c = job.parts().get(i).getTransitionCost(fromWM, toWM);
                    if (job.relocatibility() == Relocability.MIGRATABLE && !fromWM.getDc().equals(toWM.getDc())) {
                        //If there is a DC migration, addInstantPricing the migration cost
                        c += job.migrationCost();
                    }
                    transitionCosts[x][y] += c;
                }
                y++;
            }
            x++;
        }
    }

    public int state(String id) {
        int i = 0;
        for (State st : states) {
            if (st.name().equals(id)) {
                return i;
            }
            i++;
        }
        return -1;
    }
    public FiniteAutomaton automaton() {
        return fa;
    }

    /**
     * Generate the automata.
     * @return the resulting automata.
     */
    private FiniteAutomaton makeAutomaton() {
        switch (job.relocatibility()) {
            case SPREADABLE:
                return spreadable();
            case NO:
                return fix();
            case MIGRATABLE:
                return migratable();
        }
        throw new UnsupportedOperationException("Unsupported relocatibility: " + job.relocatibility());
    }

    /**
     * Just run on a single DC. So a common initial state moving to default state for each DC.
     * @return the fa
     */
    private FiniteAutomaton fix() {
        if (job.parts().size() > 1) {
            throw new UnsupportedOperationException();
        }
        FiniteAutomaton fa = new FiniteAutomaton();
        DatacenterPart p = job.parts().get(0);
        WM cur = p.currentWM();
        states = new State[nbStates(p)];
        transitionCosts = new int[states.length][states.length];
        for (WM w : p.getWorkingModes()) {
            if (w.perfs().isEmpty()) {
                throw new IllegalArgumentException("no performance level for working mode " + w);
            }
            for (PerfLevel pl : w.perfs()) {
                int st = fa.addState();
                states[st] = new State(pl);
                fa.setFinal(st);
                if (cur == w) {
                    //We don't care that much about with PL it will be
                    //as there is no cost switching for 1 PL to another one in the same WM
                    fa.setInitialState(st);
                }
            }
        }
        //Transitions inside the DC
        clickify(fa);

        return fa;
    }

    private FiniteAutomaton migratable() {
        if (job.parts().size() == 1) {
            return fix();
        }
        FiniteAutomaton fa = new FiniteAutomaton();
        List<PerfLevel> defaults = new ArrayList<>();
        int maxStates = 0;
        for (DatacenterPart p : job.parts()) {
            defaults.add(p.getDefaultWM().perfs().get(0));
            maxStates += nbStates(p);
        }
        states = new State[maxStates];
        int j = 0;
        List<PerfLevel> current = currentState();
        for (DatacenterPart p : job.parts()) {
            for (WM wm : p.getWorkingModes()) {
                if (wm.perfs().isEmpty()) {
                    throw new IllegalArgumentException("no performance level for working mode " + wm);
                }
                for (PerfLevel pl : wm.perfs()) {
                    List<PerfLevel> states = new ArrayList<>();
                    for (int i = 0; i < job.parts().size(); i++) {
                        states.add(i == j ? pl : defaults.get(i));
                    }
                    int st = fa.addState();
                    fa.setFinal(st);
                    if (current.containsAll(states)) {
                        fa.setInitialState(st);
                    }
                    this.states[st] = new State(states);
                }
            }
            j++;
        }
        clickify(fa);
        return fa;
    }

    private List<PerfLevel> currentState() {
        return job.parts().stream().map(p -> p.currentWM().perfs().get(0)).collect(Collectors.toList());
    }


    /**
     * Check if a collection of working modes are prohibited.
     * Currently, we consider that a collection is prohibited if the total perf == 0
     *
     * @param modes the modes to consider
     * @return {@code true} if this state is not allowed
     */
    private boolean isForbidden(PerfLevel[] modes) {
        for (ForbiddenState st : job.forbiddenStates()) {
            if (match(st.getWorkingModes(), modes)) {
                return true;
            }
        }
        return false;
    }

    private boolean match(List<ForbiddenWorkingMode> workingModes, PerfLevel[] modes) {
        boolean allIn = true;
        for (ForbiddenWorkingMode wm : workingModes) {
            boolean got = false;
            for (PerfLevel pl : modes) {
                WM w = pl.WM();
                if (w.name().equals(wm.getWorkingModeName()) && w.getDc().equals(wm.getDataCenterName())) {
                    got = true;
                    break;
                }
            }
            if (!got) {
                allIn = false;
                break;
            }
        }
        return allIn;
    }

    private FiniteAutomaton spreadable() {
        FiniteAutomaton fa = new FiniteAutomaton();
        List<List<PerfLevel>> doms = new ArrayList<>();
        for (DatacenterPart p : job.parts()) {
            List<PerfLevel> l = new ArrayList<>();
            for(WM w : p.getWorkingModes()) {
                if (w.perfs().isEmpty()) {
                    throw new IllegalArgumentException("no performance level for working mode " + w);
                }
                l.addAll(w.perfs().stream().collect(Collectors.toList()));
            }
            doms.add(l);
        }

        AllTuplesGenerator<PerfLevel> gen = new AllTuplesGenerator<>(PerfLevel.class, doms);
        List<State> myStates = new ArrayList<>();
        while (gen.hasNext()) {
            PerfLevel[] tuple = gen.next();
            if (!isForbidden(tuple)) {
                int st = fa.addState();
                fa.setFinal(st);
                List<PerfLevel> l = Arrays.asList(tuple);
                if (currentState().containsAll(l)) {
                    fa.setInitialState(st);
                }
                myStates.add(new State(l));
            }
        }
        states = myStates.toArray(new State[myStates.size()]);
        clickify(fa);
        return fa;
    }

    /**
     * Generate the finite state fa.
     * The automate is in the DOT format. To use with the `circo` layout for a correct rendering
     * @return the string containing the DOT.
     */
    public String toDot() {
        StringBuilder b = new StringBuilder();
        b.append("digraph ").append(job.easc()).append(" {\n");
        for (int i = 0; i < states.length; i++) {
            b.append(" ").append(i).append(" [label=\"").append(states[i].name()).append("\"");
            if (fa.isFinal(i)) {
                b.append(",shape=doublecircle");
            }
            b.append("];\n");
        }
        //the transitions
        for (int i = 0; i < fa.getNbStates(); i++) {
            for (int [] tr :  fa.getTransitions(i)) {
                b.append(" ").append(tr[0]).append(" -> ").append(tr[1]).append("\n");
            }
        }
        //Initial state
        b.append("init [shape=plaintext,label=\"\"];\n");
        b.append("init -> ").append(fa.getInitialState());
        b.append("}");
        return b.toString();
    }

    private static void clickify(FiniteAutomaton a) {
        //Now the transitions
        for (int i = 0; i < a.getNbStates(); i++) {
            for (int j = 0; j < a.getNbStates(); j++) {
                a.addTransition(i, j, j);
            }
        }
    }

    /*
    * Generate the cost automaton from the costs stated between the different working modes
    * and the possible migration cost for MIGRATABLE activities.
    */
    public ICostAutomaton costAutomaton(int nbSlots) {
        int[][][][] c = new int[nbSlots][nbStates()][nbResources()][nbStates()];
        //[timeslot][nextState][dimension][currentState] = Count
        for (int t = 0; t < nbSlots; t++) {
            for (int to = 0; to < nbStates(); to++) {
                for (int rc = 0; rc < nbResources(); rc++) {
                    for (int from = 0; from < nbStates(); from++) {
                        State dest = state(to);
                        int realPerf = dest.perf() - transitionCost(from, to);
                        if (rc == instant()) {
                            //We store here the revenue if there is an instant at t
                            InstantRevenue r = job.instantRevenues(t);
                            if (r != null) {
                                /*if (realPerf > r.basePerf()) {
                                    System.err.println("Useless mode at " + t + " " + dest);
                                }*/
                                //System.out.println(state(from) + "->" +dest + " at " + t + ": " + realPerf + "->" + r.revenue(realPerf));
                                c[t][to][rc][from] = r.revenue(realPerf);
                            }
                        } else {
                            //cumulative objective, we store the performance
                            //the revenue will be derived from the resulting variable
                            CumulativeRevenue r = cumulatives.get(rc);
                            if (r.from() <= t && t < r.to()) {
                                c[t][to][rc][from] = realPerf;
                            }
                        }
                    }
                }
            }
        }

        int[] lbs = new int[nbResources()];
        int[] ubs = new int[nbResources()];
        int[] bounds = perfBounds();
        for (int i = 0; i < nbResources(); i++) {
            if (i != RC_INSTANT) {
                lbs[i] = /*bounds[0] * nbSlots;*/0;
                ubs[i] = bounds[1] * nbSlots;
            } else {
                int[] bs = instantRevenueBound();
                lbs[i] = bs[0];
                ubs[i] = bs[1];
            }
        }

        //Add instant costs
        return CostAutomaton.makeMultiResources(fa, c, lbs, ubs);
    }

    /**
     * Get the power usage depending on the state.
     * @return an array of power value in watts
     */
    public int [] power() {
        int [] power = new int[states.length];
        for (int i = 0; i < states.length; i++) {
            power[i] = states[i].power();
        }
        return power;
    }

    /**
     * Get the power usage depending on the state.
     * @param dcs the datacenter identifiers
     * @return an array of power value in watts. [dcId][state]
     */
    public Map<String, int[]> powerByDcs(String[] dcs) {
        Map<String, int[]> m = new HashMap<>();
        for (String dc : dcs) {
            m.put(dc, new int[states.length]);
        }
        for (int i = 0; i < states.length; i++) {
            State s = states[i];
            for (DatacenterPart p : job.parts()) {
                if (!m.containsKey(p.getName())) {
                    throw new IllegalArgumentException("Unknown datacentre '" + p.getName() + "'");
                }
                m.get(p.getName())[i] = s.perfLevel(p.getName()).power();
            }
        }
        return m;
    }

    public MyActivity activity() {
        return job;
    }

    public State state(int i) {
        return states[i];
    }

    public int nbStates() {
        return states.length;
    }
    @Override
    public String toString() {
        return Arrays.toString(states);
    }

    public int transitionCost(int i, int j) {
        return transitionCosts[i][j];
    }

    public int[][] transitionsCosts() {
        return transitionCosts;
    }

    private int nbStates(DatacenterPart p) {
        int nb = 0;
        for (WM wm : p.getWorkingModes()) {
            nb += wm.perfs().size();
        }
        return nb;
    }

    /**
     * Get the instantaneous performance boundaries.
     *
     * @return an array[minPerf, maxPerf]
     */
    public int[] perfBounds() {
        int[] bounds = new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE};
        for (int i = 0; i < nbStates(); i++) {
            State st = state(i);
            if (st.perf() > bounds[1]) {
                bounds[1] = st.perf();
            }
            if (st.perf() < bounds[0]) {
                bounds[0] = st.perf();
            }
        }
        return bounds;
    }

    public int[] wattBounds() {
        int[] bounds = new int[]{Integer.MAX_VALUE, Integer.MIN_VALUE};
        for (int i = 0; i < nbStates(); i++) {
            State st = state(i);
            if (st.power() > bounds[1]) {
                bounds[1] = st.power();
            }
            if (st.perf() < bounds[0]) {
                bounds[0] = st.power();
            }
        }
        return bounds;
    }
    /**
     * Get the resource identifier for the instant revenue.
     *
     * @return the identifier. {@code -1} if there is no instant revenue.
     */
    public int instant() {
        return RC_INSTANT;
    }

    /**
     * Get the identifier of a given cumulative revenue
     *
     * @param p the revenue
     * @return the identifier. {@code -1} if the revenue source is unknown
     */
    public int cumulative(CumulativeRevenue p) {
        int r = 0;
        for (CumulativeRevenue i : cumulatives) {
            if (i == p) {
                return r;
            }
            r++;
        }
        return -1;
    }

    public int nbResources() {
        int nb = nbCumulatives;
        if (RC_INSTANT >= 0) {
            nb++;
        }
        return nb;
    }

    public int[] instantRevenueBound() {
        int[] perfs = perfBounds();
        int[] bounds = new int[]{0, 0};
        for (InstantRevenue r : instant) {
            int lb = r.revenue(perfs[0]);
            int ub = r.revenue(perfs[1]);
            /*if (lb > ub) {
                r.pretty(perfs[1]); //Should throw the issue about a non-monotonic function
                throw new IllegalArgumentException("Error while computing the bounds for revenue " + r + ": lb=" + lb + "; ub=" + ub + "\nrevenues:" + r.pretty(perfs[1]));
            }*/

            //No negative revenues
            bounds[0] += Math.min(lb, ub);
            bounds[1] += Math.max(lb, ub);
            //bounds[0] = Math.max(0, bounds[0]);
            bounds[0] = 0;//Integer.MIN_VALUE / 2;
            bounds[1] = Math.max(0, bounds[1]);
            if (bounds[0] > bounds[1]) {
                throw new IllegalArgumentException("Overflow while summing the instantaneous revenues");
            }
        }
        return bounds;
    }

    public int[] revenueBounds() {
        int maxPerf = 0; //the maximum perf per timeslot
        for (State st : states) {
            maxPerf = Math.max(maxPerf, st.perf());
        }
        int maxCost = 0;
        //compute the maximum cumulative perf and the associated revenues to compute the UB
        for (CumulativeRevenue c : cumulatives) {
            int d = c.to() - c.from();
            maxCost += c.revenue(maxPerf * d);
        }
        return new int[]{0, maxCost};
    }

    public void prettyCosts(int[][][][] costs) {
        int nbResources = nbCumulatives;
        if (RC_INSTANT >= 0) {
            nbResources++;
        }
        for (int t = 0; t < costs.length; t++) {
            for (int j = 0; j < nbStates(); j++) {
                for (int i = 0; i < nbStates(); i++) {
                    for (int r = 0; r < nbResources; r++) {
                        System.out.println(t + " " + j + " " + r + " " + i + "=" + costs[t][j][r][i]);
                    }
                }
            }
        }

    }
}
