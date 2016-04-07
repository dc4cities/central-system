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

/**
 * Provide the metrics associated to a time-slot for a power source.
 * During this period, the metrics are considered to be constant.
 *
 *
 */
public class PowerSourceSlot {

    /**
     * Peak Capacity in Watt.
     */
    private int peak;

    /**
     * Percentage of renewable energy
     */
    private int renewablePct;

    /**
     * Energy price in euro-cents per kW.h
     */
    private int price;

    /**
     * New slot.
     *
     * @param pp    the peak consumption in Watt for every second of this slot.
     * @param rPct  the percentage of renewable energy for this period. Between 0 and 100
     * @param price the energy price in euros per kilo watt.hours
     */
    public PowerSourceSlot(int pp, int rPct, int price) {
        peak = pp;
        renewablePct = rPct;
        this.price = price;
    }

    /**
     * Get the amount of Watt the source can provide every second
     *
     * @return a positive value in Watt
     */
    public int peak() {
        return peak;
    }

    /**
     * Get the percentage of the power that comes from renewable energies
     *
     * @return a positive number < 100
     */
    public int renPct() {
        return renewablePct;
    }

    /**
     * Get the energy price in euros per kilo-watt hours.
     *
     * @return a positive number
     */
    public int price() {
        return price;
    }
}
