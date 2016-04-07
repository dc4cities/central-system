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

import javax.measure.unit.Unit;

/**
 * A revenue that is computed from a performance at a given moment.
 *
 *
 */
public class InstantRevenue extends Revenue {

    private int at;

    private Unit unit;

    /**
     * A new revenue.
     *
     * @param at        the moment
     * @param basePerf  the default performance level
     * @param basePrice the default price
     */
    public InstantRevenue(int at, int basePerf, int basePrice) {
        super(basePerf, basePrice);
        this.at = at;
    }

    /**
     * The moment the revenue is considered
     *
     * @return a positive integer
     */
    public int at() {
        return at;
    }

    /**
     * Set the performance unit.
     *
     * @param u the unit
     * @return {@code this}
     */
    public InstantRevenue unit(Unit u) {
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

