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

import java.util.ArrayList;
import java.util.List;

/**
 * A Working mode is a deployment mode of an activity on a given datacenter.
 * For a same working mode, the application might have different level of performance.
 *
 */
public class WM {

    /**
     * The mode identifier.
     */
    private String id;

    /**
     * The running datacenter.
     */
    private String dc;

    /**
     * The possible performance levels.
     */
    private List<PerfLevel> perfs;

    /**
     * The working mode identifier.
     */
    private int value;

    /**
     * a new working mode
     * @param dc the running datacenter
     * @param id the working mode identifier
     */
    public WM(String dc, String id) {
        this.id = id;
        this.dc = dc;
        this.perfs = new ArrayList<>();
    }

    public String getDc() {
        return dc;
    }

    public WM setDc(String dc) {
        this.dc = dc;
        return this;
    }

    public List<PerfLevel> perfs() {
        return perfs;
    }

    public WM addPerfLevel(int perf, int pow) {
        return addPerfLevel(0, perf, pow);
    }

    public WM addPerfLevel(int id, int perf, int pow) {
        PerfLevel pl = new PerfLevel(this, id, perf, pow);
        perfs.add(pl);
        return this;
    }

    public WM addPerfLevel(PerformanceLevel p, int id, int perf, int pow) {
        PerfLevel pl = new PerfLevel(this, id, perf, pow);
        pl.backend(p);
        perfs.add(pl);
        return this;
    }

    public String name() {
        return id;
    }

    @Override
    public String toString() {
        return "WM{id='" + id + '\'' + ", dc='" + dc + '\'' + ", perfs=" + perfs + '}';
    }

    public void setValue(int v) {
        this.value = v;
    }

    public int getValue() {
        return value;
    }
}
