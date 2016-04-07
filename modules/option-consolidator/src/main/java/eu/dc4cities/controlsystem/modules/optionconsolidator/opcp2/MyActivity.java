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

import eu.dc4cities.controlsystem.model.easc.ForbiddenState;
import eu.dc4cities.controlsystem.model.easc.Relocability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An activity is a task to run on a datacenter.
 * This object describes the activity specification and management capabilities.
 *
 * It is created from {@link Converter}
 *
 */
public class MyActivity {

    private int migrationCost;

    private String eascName;

    private String name;

    private List<DatacenterPart> parts;

    private Relocability type = Relocability.NO;

    private List<InstantRevenue> iSlos;
    private List<CumulativeRevenue> cSlos;

    private List<ForbiddenState> forbiddenStates;

    /**
     * New activity.
     *
     * @param easc the easc it belongs to
     * @param n    the activity identifier
     */
    public MyActivity(String easc, String n) {
        this(n);
        eascName = easc;
        forbiddenStates = new ArrayList<>();

    }

    public MyActivity forbiddenStates(List<ForbiddenState> states) {
        this.forbiddenStates = states;
        return this;
    }

    public List<ForbiddenState> forbiddenStates() {
        return this.forbiddenStates;
    }

    /**
     * Simpler constructor for testing purpose.
     * @param n activity name
     */
    public MyActivity(String n) {
        name = n;
        eascName = "-";
        parts = new ArrayList<>();
        iSlos = new ArrayList<>();
        migrationCost = 0;
        cSlos = new ArrayList<>();
        forbiddenStates = new ArrayList<>();
    }

    public MyActivity add(DatacenterPart p) {
        parts.add(p);
        return this;
    }

    public Relocability relocatibility() {
        return type;
    }

    public MyActivity relocatibility(Relocability t) {
        this.type = t;
        return this;
    }

    public String easc() {
        return eascName;
    }

    public String name() {
        return name;
    }

    /**
     * Get the instant revenues.
     *
     * @return a lit that may be empty
     */
    public List<InstantRevenue> instantRevenues() {
        return iSlos;
    }

    /**
     * Add an instant revenue.
     *
     * @param r the revenue to add
     * @return this
     */
    public MyActivity add(InstantRevenue r) {
        iSlos.add(r);
        return this;
    }

    /**
     * Add a cumulative revenue.
     *
     * @param r the revenue to add
     * @return this
     */
    public MyActivity add(CumulativeRevenue r) {
        cSlos.add(r);
        return this;
    }

    /**
     * Get the cumulative revenues.
     *
     * @return a list that may be empty
     */
    public List<CumulativeRevenue> cumulativeRevenues() {
        return cSlos;
    }

    /**
     * Get the instant revenue associated to a given time-slot.
     *
     * @param t the time-slot
     * @return the associated revenue or {@code null} if no revenue is associated
     */
    public InstantRevenue instantRevenues(int t) {
        for (InstantRevenue s : iSlos) {
            if (s.at() == t) {
                return s;
            }
        }
        //Outside
        return null;
    }

    public List<DatacenterPart> parts() {
        return Collections.synchronizedList(parts);
    }

    public int migrationCost() {
        return migrationCost;
    }

    /**
     * Set the migration cost to pay in terms
     * of performance when a working mode is migrated from a datacenter to another one.
     * @param c the performance cost
     */
    public MyActivity migrationCost(int c) {
        migrationCost = c;
        return this;
    }

    public String pretty() {
        if (eascName.equals("-")) {
            return name;
        }
        return eascName + ":" + name;
    }

    @Override
    public String toString() {
        return "activity{" +
                "name='" + eascName + ":" + name + '\'' +
                ", parts=" + parts +
                ", " + type +
                '}';
    }

    public DatacenterPart datacenterPart(String n) {
        for (DatacenterPart p : parts) {
            if (p.getName().equals(n)) {
                return p;
            }
        }
        return null;
    }

    public int maxPowerConsumption() {
        int m = 0;
        for (DatacenterPart p : parts) {
            int dcMax = 0;
            for (WM w : p.getWorkingModes()) {
                for (PerfLevel l : w.perfs()) {
                    dcMax = Math.max(dcMax, l.power());
                }
            }
            m += dcMax;
        }
        return m;
    }
}
