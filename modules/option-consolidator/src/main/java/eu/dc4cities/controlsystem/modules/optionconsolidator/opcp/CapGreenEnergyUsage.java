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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;

import java.util.Arrays;

/**
 * Cap the total energy usage for a given period.
 * In practice the constraint enforces:
 * <p/>
 * energy(s,from, to) <= K
 *
 *
 */
public class CapGreenEnergyUsage extends CappableConstraint {

    private int from, to, priority;

    private IntVar sat;

    private int greenPct;

    /**
     * New constraint with a default ">=" operator
     *
     * @param from the beginning of the period to consider
     * @param to   the end of the period (excluded)
     * @param k    the minimum amount of green energy to use for the period in percent
     */
    public CapGreenEnergyUsage(int from, int to, int k) {
        super(">=");
        greenPct = k;
        this.from = from;
        this.to = to;
    }

    @Override
    public void decorate(OPCP pb) {
        //hard constraint, necessarily satisfied
        sat = VF.fixed("isSatisfied(" + toString() + ")", 1, pb.getSolver());

        IntVar energy;
        if (!pb.areSlotsHomogeneous()) {
            throw new UnsupportedOperationException("Slots are supposed to be homogeneous");
        }
        Solver s = pb.getSolver();
        IntVar[] pwr = from == 0 && to == pb.getNbSlots() ? pb.getPowerUsage() : Arrays.copyOfRange(pb.getPowerUsage(), from, to);
        //get the conversion factor
        int sd = pb.getSlotDurations()[0];
        int ws = pb.getWattReducingFactor();
        int convFactor = sd * ws / 3600;
        if (convFactor <= 0) {
            throw new UnsupportedOperationException("The conversion factor should be positive and an integer. Currently: " + (sd * ws / 3600.0));
        }
        if (convFactor == 1) {
            energy = VF.bounded("WattHour[" + from + ";" + to + "[", 0, Integer.MAX_VALUE - 1, s);
            s.post(ICF.sum(pwr, energy));
        } else {
            IntVar e = VF.bounded("WattHour[" + from + ";" + to + "[ /" + convFactor, 0, Integer.MAX_VALUE - 1, s);
            energy = VF.scale(e, convFactor);
        }

        pb.getSolver().post(ICF.arithm(energy, getOperator(), greenPct));
    }

    @Override
    public String toString() {
        return "energy([" + from + ":" + to + "[) " + getOperator() + greenPct;
    }

    @Override
    public IntVar isSatisfied() {
        return sat;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

}
