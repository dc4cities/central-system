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

import eu.dc4cities.controlsystem.modules.optionconsolidator.Range;

import javax.measure.unit.Unit;

/**
 * A revenue that is computed from a cumulative performance over a period.
 *
 *
 */
public class CumulativeRevenue extends Revenue implements Range {

    private int from, to;

    /**
     * What has been already consumed in the past.
     */
    private int base;

    private Unit unit;
    /**
     * A new revenue.
     *
     * @param from      the period beginning
     * @param to        the end of the period (exclusive)
     * @param basePerf  the default performance level
     * @param basePrice the default price
     */
    public CumulativeRevenue(int from, int to, int basePerf, int basePrice) {
        super(basePerf, basePrice);
        this.from = from;
        this.to = to;
    }

    /**
     * State an amount of energy that as already been consumed in the past.
     * Typically, when from < 0
     * @param v the amount already consumed
     * @return {@code this}
     */
    public CumulativeRevenue base(int v) {
        base = v;
        return this;
    }

    /**
     * The beginning of the period.
     * @return a positive integer
     */
    public int from() {
        return from;
    }

    /**
     * The end of the period (exclusive).
     * @return a positive integer
     */
    public int to() {
        return to;
    }

    /**
     * Get the amount of energy consumed in the past.
     *
     * @return a positive integer
     */
    public int base() {
        return base;
    }

    /**
     * Set the performance unit.
     *
     * @param u the unit
     * @return {@code this}
     */
    public CumulativeRevenue unit(Unit u) {
        unit = u;
        return this;
    }

    /**
     * Get the performance unit.
     *
     * @return the unit. Can be null
     */
    public Unit unit() {
        return unit;
    }

}

