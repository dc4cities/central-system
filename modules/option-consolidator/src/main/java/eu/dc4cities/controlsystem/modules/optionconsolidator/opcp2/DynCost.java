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

import org.chocosolver.solver.variables.IntVar;

/**
 * A dynamic cost function that is a part of a problem.
 *
 *
 */
public interface DynCost {

    /**
     * Inject the cost variable.
     *
     * @param pb the associated problem
     * @return a variable
     */
    IntVar inject(OPCP2 pb);

    String pretty();

    public void store(Statistics st);
}


