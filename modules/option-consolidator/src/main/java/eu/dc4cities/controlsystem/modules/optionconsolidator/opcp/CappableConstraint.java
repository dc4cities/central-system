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

import eu.dc4cities.controlsystem.modules.optionconsolidator.SatConstraint;

/**
 * Abstract class to represent a constraint that cap the value of ... something.
 * Allowed operators: {@code <, <=, >, >=, =, !=}
 *
 *
 */
public abstract class CappableConstraint implements SatConstraint {

    private String op;

    /**
     * Make a new constraint.
     *
     * @param operator the default operator
     */
    public CappableConstraint(String operator) {
        this.op = operator;
    }

    /**
     * Set the operator to use to express the restriction.
     *
     * @param operator the operator to use to
     */
    public void setOperator(String operator) {
        op = operator;
    }

    /**
     * Get the operator used to state the restriction.
     *
     * @return a non-null operator
     */
    public String getOperator() {
        return op;
    }
}
