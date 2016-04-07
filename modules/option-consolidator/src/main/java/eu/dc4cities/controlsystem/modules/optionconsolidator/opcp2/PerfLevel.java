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

import eu.dc4cities.controlsystem.model.easc.PerformanceLevel;

/**
 * State a performance level for a working mode.
 *
 */
public class PerfLevel {

    private int perf, power;

    private WM wm;

    private int id;

    private PerformanceLevel backend;

    /**
     * A new performance level.
     *
     * @param wm   the associated working mode.
     * @param id   the working mode identifier.
     * @param perf the performance level
     * @param pow  the associated power consumption.
     */
    public PerfLevel(WM wm, int id, int perf, int pow) {
        this.id = id;
        this.perf = perf;
        this.power = pow;
        this.wm = wm;
    }

    public int perf() {
        return perf;
    }

    public int power() {
        return power;
    }

    public int id() {
        return id;
    }

    public WM WM() {
        return wm;
    }

    /**
     * Store the original performance level.
     * Convenient to avoid any rounding issue
     *
     * @param p the original, un-compressed, performance level
     * @return {@code this}
     */
    public PerfLevel backend(PerformanceLevel p) {
        backend = p;
        return this;
    }

    /**
     * Get the un-compressed performance level
     *
     * @return the original level. Can be null
     */
    public PerformanceLevel backend() {
        return backend;
    }

    @Override
    public String toString() {
        return wm.getDc() + ":" + wm.name() + ":" + this.id;
    }
}
