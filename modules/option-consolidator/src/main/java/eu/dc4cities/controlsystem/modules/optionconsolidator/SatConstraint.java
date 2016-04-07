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

import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.OPCPDecorator;
import org.chocosolver.solver.variables.IntVar;

/**
 * An interface to denote a satisfaction-oriented constraints.
 * It is possible to define a constraint that can be satisfied thanks to the usage of the
 * boolean variable {@link #isSatisfied()}. One can also state a priority for the constraint.
 *
 *
 */
public interface SatConstraint extends OPCPDecorator {

    /**
     * Indicates whether or not the constraint must be satisfied.
     *
     * @return a variable instantiated to {@code 1} iff the constraint is satisfied.
     */
    IntVar isSatisfied();

    /**
     * Indicates the priority of the constraint
     *
     * @return a number > 0
     */
    int getPriority();
}
