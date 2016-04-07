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

package eu.dc4cities.controlsystem.modules.optionconsolidator.opcp;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Arithmetic;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.SMF;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.util.ESat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static javax.measure.unit.SI.WATT;

/**
 * Implementation of the satisfaction part of the WorkingMode Plan Consolidator Problem.
 * <p/>
 * Energy-oriented variables are expressed in Watt-hour.
 * Power-oriented variables are expressed in _reduced_ watts using a reduction factor provided at instantiation.
 * <p/>
 * <p/>
 * It is smart to investigate for efficient reduction factor. Typically to provide fast conversion of power to energy.
 * TODO: support for carbon emission.
 *
 *
 */
public class OPCP {

    private final Logger LOGGER = LoggerFactory.getLogger("OPCP");

    private boolean homogeneousSlots;

    private int wattStepping;

    /**
     * Nb. of milliseconds in a second.
     */
    public static final int SECONDS = 1000;

    /**
     * The practical amount of power used at a given time-slot for each source.
     */
    private Map<OldPowerSource, IntVar[]> powerUsed;

    /**
     * Power used by each activity for each time-slot
     */
    private Map<SimpleActivity, IntVar[]> aPowerUsed;

    /**
     * The practical price for each activity by time-slot.
     */
    private Map<SimpleActivity, IntVar[]> penaltyUsed;

    private IntVar[] totalPowerUsed;

    /**
     * The practical amount of green-point per time-slot.
     */
    private IntVar[] totalPenaltyUsed;

    private Solver solver;

    private int verbosity = 0;

    private IntVar objective;

    private ResolutionPolicy resPolicy = ResolutionPolicy.SATISFACTION;

    /**
     * The number of time-slots.
     */
    private int nbSlots;

    /**
     * Maximum value for an integer.
     */
    public static final int MAX_INT = Integer.MAX_VALUE / 100;

    /**
     * 500 kilo-watts maximum before the reduction.
     */
    public static final int MAX_POWER = 500000;

    private List<OldPowerSource> sources;

    private List<SimpleActivity> activities;

    /**
     * Variable denoting the selected plan per activity.
     */
    private IntVar[] selectedPlan;


    private int[] slotsDuration;

    private boolean noSolution = false;
    /**
     * New problem
     *
     * @param durations    the duration of each time-slot in seconds. Ordered chronologically
     * @param powerSources the available power sources
     * @param activities   all the activities to consider
     */
    public OPCP(int[] durations, List<OldPowerSource> powerSources, List<SimpleActivity> activities) {
        this(durations, powerSources, activities, 1);
    }

    /**
     * New problem
     *
     * @param durations    the duration of each time-slot in seconds. Ordered chronologically
     * @param powerSources the available power sources
     * @param activities   all the activities to consider
     * @param wrf          the reduction factor applied to every power value.
     */
    public OPCP(int[] durations, List<OldPowerSource> powerSources, List<SimpleActivity> activities, int wrf) {
        nbEq = 0;
        nbElements = 0;
        this.wattStepping = wrf;
        this.sources = powerSources;
        this.activities = activities;
        this.nbSlots = durations.length;
        slotsDuration = Arrays.copyOf(durations, durations.length);

        homogeneousSlots = true;
        int d = slotsDuration[0];
        for (int i = 1; i < slotsDuration.length; i++) {
            if (d != slotsDuration[i]) {
                homogeneousSlots = false;
                break;
            }
        }

        solver = new Solver();

        try {
            makePowerVariables();
        } catch (ContradictionException ex) {
            noSolution = true;
        }
        makeActivityVariables();
    }

    /**
     * Get the factor that is used to reduce every power value.
     *
     * @return a factor >= 1
     */
    public int getWattReducingFactor() {
        return wattStepping;
    }

    /**
     * Get the amount of watt represented
     * by a given watt-oriented variable.
     * <p/>
     * This simply multiply the variable value by {@link #getWattReducingFactor()}
     *
     * @param w the watt-oriented variable
     * @return the real amount of watt
     */
    public int toWatt(IntVar w) {
        return w.getValue() * wattStepping;
    }

    /**
     * Reduce an amount of watts according to the watt reduction factor.
     * In practice, it is just a division by {@link #getWattReducingFactor()}.
     *
     * @param w the amount of watt to reduce.
     * @return the reduced amount.
     */
    public int reduceWatts(int w) {
        return w / wattStepping;
    }

    /**
     * Make a variable that will store a power value.
     * The value is reduced according to {@link #getWattReducingFactor()}.
     * Furthermore, if the reduced value exceeds {@link #MAX_INT}, it is truncated to this value
     *
     * @param n the variable name
     * @param w the upper-bound
     * @return the resulting variable
     */
    public IntVar makeWattVariable(String n, int w) {
        int tw = w;
        /*if (tw > MAX_POWER) {
            tw = MAX_POWER;
            LOGGER.debug("For variable '" + n + "', ub truncated from " + w + " to " + tw);
        }*/
        int sw = reduceWatts(tw);
        return VF.bounded(n, 0, sw, solver);
    }

    /**
     * Check if the slots have the same duration.
     *
     * @return {@code true} iff all the slots have the same duration
     */
    public boolean areSlotsHomogeneous() {
        return homogeneousSlots;
    }

    private void makeActivityVariables() {

        this.selectedPlan = new IntVar[activities.size()];
        aPowerUsed = new HashMap<>(activities.size());
        penaltyUsed = new HashMap<>(activities.size());
        totalPenaltyUsed = new IntVar[nbSlots];
        //The variable to denote the selected plan for each activity

        int i = 0;
        for (SimpleActivity a : activities) {
            String n = a.getEasc() + ":" + a.getName();
            IntVar selection = VF.enumerated("selectedPlan(" + n + ")", 0, a.getWorkingModes().size() - 1, solver);
            IntVar[] pwrUsage = new IntVar[nbSlots];
            IntVar[] penUsage = new IntVar[nbSlots];
            selectedPlan[i++] = selection;
            aPowerUsed.put(a, pwrUsage);
            penaltyUsed.put(a, penUsage);
            //set the resource usage for each activity depending on the selected plan.
            for (int t = 0; t < nbSlots; t++) {
                pwrUsage[t] = makeWattVariable("powerUsed(" + n + "," + t + ")", maxDemandAt(a, t));
                WorkingModeSlot[] possiblesOptions = possibleOptionSlots(a, t);
                among(pwrUsage[t], possiblePowerDemand(possiblesOptions), selection);
            }
        }

        IntVar[] buffer = new IntVar[activities.size()];
        for (int t = 0; t < nbSlots; t++) {
            //totalPenaltyUsed[t] = VF.bounded("penalty_used(" + t + ")", 0, MAX_INT, solver);
            varAt(buffer, aPowerUsed, t);
            solver.post(ICF.sum(buffer, totalPowerUsed[t]));
            //solver.post(ICF.sum(varAt(buffer, penaltyUsed, t), totalPenaltyUsed[t]));
        }
    }

    private void makePowerVariables() throws ContradictionException {
        powerUsed = new HashMap<>(sources.size());
        IntVar[] buffer = new IntVar[sources.size()];

        for (OldPowerSource src : sources) {
            IntVar[] used = new IntVar[nbSlots];
            int t = 0;
            for (OldPowerSourceSlot s : src.getSlots()) {
                if (t == nbSlots) {
                    break;
                }
                //Practical usage of the power for the source
                int ub = Math.min((int) s.getPeak().longValue(WATT), maxDemandAt(t));
                used[t] = makeWattVariable("power_used(" + src.getName() + "," + t + ")", ub);
                t++;
            }
            powerUsed.put(src, used);
        }

        //Sum
        totalPowerUsed = new IntVar[nbSlots];
        int[] bounds = new int[2];
        for (int t = 0; t < nbSlots; t++) {
            int max = maxPowerAt(t);
            boundedConsumptionAt(bounds, t);
            totalPowerUsed[t] = makeWattVariable("total_power_used(" + t + ")", max);

            varAt(buffer, powerUsed, t);
            IntVar[] usefull = extractDecisionVariables(buffer);

            totalPowerUsed[t].updateLowerBound(bounds[0], Cause.Null);
            totalPowerUsed[t].updateUpperBound(bounds[1], Cause.Null);
            if (usefull.length == 0) {
                int val = 0;
                for (IntVar v : buffer) {
                    if (v != null) {
                        val += v.getValue();
                        totalPowerUsed[t].instantiateTo(val, Cause.Null);
                    }
                }
            } else if (usefull.length == 1) {
                solver.post(new Arithmetic(usefull[0], Operator.EQ, totalPowerUsed[t]));
            } else {
                solver.post(ICF.sum(usefull, totalPowerUsed[t]));
            }

        }
    }

    private IntVar[] extractDecisionVariables(IntVar[] buffer) {
        List<IntVar> l = new ArrayList<>();
        for (IntVar v : buffer) {
            if (v != null && !v.isInstantiatedTo(0)) {
                l.add(v);
            }
        }
        return l.toArray(new IntVar[l.size()]);
    }

    /**
     * Get the unreduced max watt consumption at a given moment.
     *
     * @param t the moment
     * @return the watt consumption (unreduced)
     */
    private int maxDemandAt(int t) {
        int max = 0;
        for (SimpleActivity a : this.activities) {
            max += maxDemandAt(a, t);
        }
        return max;
    }


    /**
     * Get the unreduced max watt consumption at a given moment.
     *
     * @param a the activity to consider
     * @param t the moment
     * @return the watt consumption (unreduced)
     */
    private int maxDemandAt(SimpleActivity a, int t) {
        int max = 0;
        for (WorkingMode m : a.getWorkingModes()) {
            if (t < m.getSlots().length) {
                max = Math.max(max, m.getSlots()[t].powerDemand());
            }
        }
        return max;
    }

    private WorkingModeSlot[] possibleOptionSlots(SimpleActivity a, int t) {
        List<WorkingModeSlot> slots = new ArrayList<>();
        for (WorkingMode o : a.getWorkingModes()) {
            if (t < o.getSlots().length) {
                slots.add(o.getSlots()[t]);
            } else {
                slots.add(WorkingModeSlot.ZERO);
            }
        }
        return slots.toArray(new WorkingModeSlot[slots.size()]);
    }

    /**
     * Get the possible power demand for all the slots.
     * The demand is reduced according to the reduction factor.
     *
     * @param slots the slots
     * @return the reduction power demand for each slot
     */
    private int[] possiblePowerDemand(WorkingModeSlot[] slots) {
        int[] v = new int[slots.length];
        int i = 0;
        for (WorkingModeSlot o : slots) {
            v[i++] = reduceWatts(o.powerDemand());
        }
        return v;
    }

    private int[] possiblePenalties(WorkingModeSlot[] slots) {
        int[] v = new int[slots.length];
        int i = 0;
        for (WorkingModeSlot o : slots) {
            v[i++] = o.penalty();
        }
        return v;
    }

    private void varAt(IntVar[] vars, Map<?, IntVar[]> usages, int t) {
        int i = 0;
        for (Map.Entry<?, IntVar[]> e : usages.entrySet()) {
            vars[i++] = e.getValue()[t];
        }
    }

    /**
     * Get the unreduced max watt production at a given moment.
     *
     * @param t the moment
     * @return the watt consumption (unreduced)
     */
    private int maxPowerAt(int t) {
        int max = 0;
        for (OldPowerSource s : sources) {
            max = Math.max(max, (int) s.getSlots()[t].getPeak().longValue(WATT));
        }
        return max;
    }

    private void boundedConsumptionAt(int[] bounds, int t, SimpleActivity sa) {
        int max = 0;
        int min = Integer.MAX_VALUE;
        for (WorkingMode wm : sa.getWorkingModes()) {
            if (t < wm.getSlots().length) {
                max = Math.max(reduceWatts(wm.getSlots()[t].powerDemand()), max);
                min = Math.min(reduceWatts(wm.getSlots()[t].powerDemand()), min);
            } else {
                min = 0;
            }
        }
        bounds[0] = min;
        bounds[1] = max;
    }

    private void boundedConsumptionAt(int[] b, int t) {
        int max = 0;
        int min = 0;
        int[] buffer = new int[2];
        for (SimpleActivity a : activities) {
            boundedConsumptionAt(buffer, t, a);
            min += buffer[0];
            max += buffer[1];
        }
        b[0] = min;
        b[1] = max;
    }

    private static int nbEq = 0, nbElements = 0;
    /**
     * Link the value variable to the possible one.
     * If all the values in the table are identical, then the variable is instantiated.
     * Otherwise, an element constraint is posted.
     *
     * @param value the variable
     * @param table the possible values for the variable
     * @param index the index in the table that indicate the value.
     */
    private void among(IntVar value, int[] table, IntVar index) {
        if (!constant(table)) {
            solver.post(ICF.element(value, table, index));
            nbElements++;
        } else {
            solver.post(new Arithmetic(value, Operator.EQ, table[0]));
            nbEq++;
        }
    }

    /**
     * Check if there is at least 2 different values.
     *
     * @param values the values to check
     * @return {@code true} if all the values are identical, {@code false} otherwise
     */
    private boolean constant(int[] values) {
        int v = values[0];
        for (int i = 1; i < values.length; i++) {
            if (v != values[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Solve the problem
     *
     * @param to the timeout in second
     * @return {@code null} if there is no solution, the selected plan for each activity otherwise (ordered by activity id)
     */
    public ESat solve(int to) {
        //System.err.println(nbEq + " " + nbElements);
        if (to > 0) {
            SMF.limitTime(solver, to * SECONDS);
        }

        if (verbosity >= 1) {
            Chatterbox.showSolutions(solver);
        }
        if (verbosity >= 3) {
            Chatterbox.showDecisions(solver);
        }
        if (verbosity >= 4) {
            Chatterbox.showContradiction(solver);
        }

        if (resPolicy == ResolutionPolicy.SATISFACTION) {
            solver.findSolution();
            return solver.isFeasible();
        } else {
            solver.findOptimalSolution(resPolicy, objective);
        }
        return solver.isFeasible();
    }

    /**
     * Get the selected working mode for each activity.
     * The problem must have been solved before using {@link #solve(int)}
     *
     * @return the selected plan for each activity if a solution exists. an empty array otherwise
     */
    public Map<SimpleActivity, WorkingMode> getSelectedPlans() {
        if (solver.isFeasible() != ESat.TRUE) {
            return Collections.emptyMap();
        }
        Map<SimpleActivity, WorkingMode> res = new HashMap<>();
        int i = 0;
        for (SimpleActivity a : activities) {
            int v = selectedPlan[i++].getLB();
            res.put(a, a.getWorkingModes().get(v));
        }
        return res;
    }

    public IntVar[] getSelections() {
        return selectedPlan;
    }

    /**
     * Get the working mode that has been selected for a simple activity.
     *
     * @param a the activity to check
     * @return the selected working mode. {@code null} if no solution were computed
     */
    public WorkingMode getSelectedPlan(SimpleActivity a) {
        if (ESat.TRUE.equals(solver.isFeasible())) {
            for (int i = 0; i < activities.size(); i++) {
                if (activities.get(i) == a) {
                    if (selectedPlan[i].isInstantiated()) {
                        return a.getWorkingModes().get(selectedPlan[i].getLB());
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Power sources:\n");
        for (OldPowerSource src : sources) {
            b.append(src).append("\n");
        }

        for (SimpleActivity a : activities) {
            b.append(a).append("\n");
        }

        return b.toString();
    }

    //Only pure getters

    /**
     * Set the level of verbosity.
     * Vary from 0 to 2. 0 means quiet, 1 print the solution, 2 prints the choices
     *
     * @param lvl in [0,2]
     */
    public void setVerbosity(int lvl) {
        this.verbosity = lvl;
    }

    /**
     * Define an objective.
     *
     * @param v the optimisation variable
     * @param p {@link org.chocosolver.solver.ResolutionPolicy#MAXIMIZE} or {@link org.chocosolver.solver.ResolutionPolicy#MINIMIZE}
     */
    public void setObjective(IntVar v, ResolutionPolicy p) {
        resPolicy = p;
        objective = v;
    }

    /**
     * Get the verbosity level.
     *
     * @return an integer between [0,2]
     */
    public int getVerbosity() {
        return verbosity;
    }

    /**
     * Get the CP solver used to model the problem
     *
     * @return the cp solver.
     */
    public Solver getSolver() {
        return solver;
    }

    /**
     * Get the power used for each time-slot.
     *
     * @param s the power source identifier
     * @return the variables associated to each time-slot
     */
    public IntVar[] getPowerUsage(OldPowerSource s) {
        return powerUsed.get(s);
    }

    /**
     * Get the power usage for a given activity.
     *
     * @param s the activity identifier
     * @return the power usage for every time-slot.
     */
    public IntVar[] getActivityPowerUsage(SimpleActivity s) {
        return aPowerUsed.get(s);
    }

    /**
     * Get the price for a given activity
     *
     * @param s the activity identifier
     * @return the price for every time-slot.
     */
    public IntVar[] getPenaltyUsed(SimpleActivity s) {
        return penaltyUsed.get(s);
    }

    /**
     * Get the number of time-slots for the profiles
     *
     * @return a positive number
     */
    public int getNbSlots() {
        return nbSlots;
    }

    /**
     * Get the duration of each slot in seconds.
     *
     * @return an array of positive values.
     */
    public int[] getSlotDurations() {
        return slotsDuration;
    }

    /**
     * Get the total power usage.
     *
     * @return the total power usage in Watt per time-slot
     */
    public IntVar[] getPowerUsage() {
        return totalPowerUsed;
    }

    /**
     * Get the green-point used for each time-slot.
     *
     * @return the variables associated to each time-slot
     */
    public IntVar[] getPenalties() {
        return totalPenaltyUsed;
    }

    public List<OldPowerSource> getPowerSources() {
        return Collections.unmodifiableList(sources);
    }

    public List<SimpleActivity> getActivities() {
        return Collections.unmodifiableList(activities);
    }

    public Logger getLogger() {
        return LOGGER;
    }
}