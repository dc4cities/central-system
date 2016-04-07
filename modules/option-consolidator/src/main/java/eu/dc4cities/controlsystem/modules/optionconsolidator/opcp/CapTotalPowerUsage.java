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

import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;

/**
 * Soft capping of the total power that can be used for a given time-slot.
 * In practice the constraint enforces:
 * <p/>
 * power(s, t) <= K
 *
 *
 */
public class CapTotalPowerUsage extends CappableConstraint {

    private int t, k;

    private IntVar sat;

    private int priority;

    /**
     * New constraint with a default "<=" operator.
     *
     * @param t the time-slot
     * @param w the maximum amount of energy to use for the period
     * @param p the constraint priority
     */
    public CapTotalPowerUsage(int t, int w, int p) {
        super("<=");
        k = w;
        this.t = t;
        priority = p;
    }

    @Override
    public void decorate(OPCP pb) {
    	sat = VF.fixed("isSatisfied(" + toString() + ")", 1, pb.getSolver());
        IntVar e = pb.getPowerUsage()[t];
        pb.getSolver().post(ICF.arithm(e, getOperator(), pb.reduceWatts(k)));
    }

    @Override
    public String toString() {
        return "power@" + t + " " + getOperator() + " " + k;
    }

    @Override
    public IntVar isSatisfied() {
        return sat;
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
