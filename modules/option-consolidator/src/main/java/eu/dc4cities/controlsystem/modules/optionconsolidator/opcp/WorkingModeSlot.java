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

import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Power;

import static javax.measure.unit.SI.WATT;

/**
 * Metrics related to a given time-slot for a working mode.
 * During this period, the metrics are considered to be constant.
 *
 *
 */
public class WorkingModeSlot {

    private int powerDemand;

    private int penalty;

    /**
     * Reusable slot that consumes nothing.
     */
    public static final WorkingModeSlot ZERO = new WorkingModeSlot(0, 0);

    /**
     * New slot
     *
     * @param pwrDemand the power consumed every second for this slot in Watt.
     * @param p         the price >= 0
     */
    public WorkingModeSlot(int pwrDemand, int p) {
        powerDemand = pwrDemand;
        penalty = p;

    }

    public WorkingModeSlot(Amount<Power> pwrDemand, int p) {
        powerDemand = (int) pwrDemand.doubleValue(WATT);
        penalty = p;

    }

    /**
     * Get the power demand.
     *
     * @return a positive amount of Watt
     */
    public int powerDemand() {
        return powerDemand;
    }

    /**
     * Get the price.
     *
     * @return a positive integer
     */
    public int penalty() {
        return penalty;
    }

    @Override
    public String toString() {
        return "{pwrDemand: " + powerDemand() + ", price: " + penalty() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkingModeSlot that = (WorkingModeSlot) o;

        if (penalty != that.penalty) return false;
        if (powerDemand != that.powerDemand) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = powerDemand;
        result = 31 * result + penalty;
        return result;
    }
}
