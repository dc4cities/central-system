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


import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;

/**
 * A constraint to enforce {@code a == b / divider} where {@code divider} is a real and {@code a} and {@code b} are
 * both integers.
 * The division is rounded up to the smallest integer.
 * <p/>
 * In practice, the constraint maintains:
 * <ul>
 * <li>{@code a = Math.ceil(b / divider)}</li>
 * <li>{@code b = ((a - 1 )* divider) % 1 == 0 ? [(a - 1)*divider + 1; Math.floor(a * divider)] : [Math.ceil((a -1)*divider); Math.floor(a * divider)]}</li>
 * </ul>
 *
 *
 */
public class RoundedUpDivision extends Constraint {

    private double qq;

    private IntVar aa, bb;

    /**
     * Make a new constraint.
     *
     * @param a the variable to divide
     * @param b the resulting ratio
     * @param d the divider
     */
    public RoundedUpDivision(IntVar a, IntVar b, double d) {
        /*super(new IntVar[]{a, b}, a.getSolver());
        qq = d;
        setPropagators(new RoundedUpDivisionPropagator(vars, d));*/
        super("RoundedUpDiv", new RoundedUpDivisionPropagator(new IntVar[]{a, b}, d));
        qq = d;
        aa = a;
        bb = b;
    }

    /*@Override
    public ESat isSatisfied(int[] values) {
        return ESat.eval(values[0] == (int) Math.ceil((double) values[1] / qq));
    }*/

    @Override
    public String toString() {
        return aa.toString() + " = " + bb.toString() + '/' + qq;
    }

    /**
     * The propagator for this constraint.
     */
    static class RoundedUpDivisionPropagator extends Propagator<IntVar> {

        private double divider;

        /**
         * New propagator
         *
         * @param vs the variables
         * @param d  the divider
         */
        public RoundedUpDivisionPropagator(IntVar[] vs, double d) {
            super(vs, PropagatorPriority.BINARY, true);
            this.divider = d;
        }

        @Override
        public int getPropagationConditions(int vIdx) {
            return IntEventType.DECUPP.getMask() + IntEventType.INCLOW.getMask() + IntEventType.INSTANTIATE.getMask();
        }

        @Override
        public ESat isEntailed() {
            if (vars[0].getDomainSize() == 1 && vars[1].getDomainSize() == 1) {
                return ESat.eval(vars[0].getValue() == (int) Math.ceil((double) vars[1].getValue() / divider));
            }
            return ESat.UNDEFINED;
        }

        private int div(int b) {
            return (int) Math.ceil((double) b / divider);
        }

        private int multLB(int a) {
            if ((a - 1 * divider) % 1 == 0) {
                return (int) ((a - 1) * divider + 1);
            }
            return (int) Math.ceil(divider * (a - 1));
        }

        @Override
        public void propagate(int evtMask) throws ContradictionException {
            filter();
            if (vars[0].getLB() != div(vars[1].getLB())
                    || vars[0].getUB() != div(vars[1].getUB())) {
                this.contradiction(null, "");
            }
        }

        private boolean filter() throws ContradictionException {
            boolean fix = awakeOnInf(0);
            fix |= awakeOnSup(0);
            fix |= awakeOnInf(1);
            fix |= awakeOnSup(1);
            return fix;
        }

        @Override
        public void propagate(int idx, int mask) throws ContradictionException {
            do {
            } while (filter());
        }


        private boolean awakeOnInf(int i) throws ContradictionException {
            if (i == 1) {
                return vars[0].updateLowerBound(div(vars[1].getLB()), this);
            } else {
                return vars[1].updateLowerBound(multLB(vars[0].getLB()), this);
            }
        }

        private boolean awakeOnSup(int i) throws ContradictionException {
            if (i == 1) {
                return vars[0].updateUpperBound(div(vars[1].getUB()), this);
            } else {
                return vars[1].updateUpperBound((int) Math.floor(divider * vars[0].getUB()), this);
            }
        }
    }
}
