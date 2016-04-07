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

import java.util.*;

/**
 * Depicts the part of an {@link MyActivity} that runs on a datacenter.
 *
 */
public class DatacenterPart {

    private String name;

    private List<WM> workingModes;

    private WM def;

    private Map<WM, Map<WM, Integer>> transitions;
    private WM cur;

    /**
     * New part.
     *
     * @param n the datacenter name
     */
    public DatacenterPart(String n) {
        name = n;
        workingModes = new ArrayList<>();
        transitions = new HashMap<>();
    }

    /**
     * Add a working mode.
     * If no working mode is registered, the one to add is considered as the default mode.
     * @param w the working mode to add
     * @return {@code this}
     */
    public DatacenterPart add(WM w) {
        return add(w, workingModes.isEmpty());
    }

    /**
     * Add a working mode.
     * @param w the working mode to add.
     * @param d {@code true} to consider this working mode as the default one.
     * @return {@code this}
     */
    public DatacenterPart add(WM w, boolean d) {
        w.setDc(name);
        if (workingModes.isEmpty()) {
            cur = w;
        }
        workingModes.add(w);
        if (d) {
            def = w;
        }
        return this;
    }

    public String getName() {
        return name;
    }

    /**
     * Get the default working mode.
     * @return the working mode. {@code null} if no working mode has been registered
     */
    public WM getDefaultWM() {
        return def;
    }

    /**
     * Get the registered working modes.
     * @return a list that may be empty.
     */
    public List<WM> getWorkingModes() {
        return Collections.unmodifiableList(workingModes);
    }

    /**
     * Set the current working mode.
     * The mode is expected to be already registered
     * @param w the working mode to declare as the current one
     * @return {@code this}
     */
    public DatacenterPart currentWM(WM w) {
        cur = w;
        return this;
    }

    public WM currentWM() {
        return cur;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("DC('" + name + "'): [");
        for (Iterator<WM> ite = workingModes.iterator(); ite.hasNext();) {
            WM wm = ite.next();
            b.append(wm.name());
            if (ite.hasNext()) {
                b.append(",");
            }
        }
        b.append(", default=").append(def.name()).append("]");
        return b.toString();
    }

    /**
     * Set the performance cost due to a working mode switch.
     * @param from the initial working mode
     * @param to the new working mode
     * @param cost the impact over performance. Must be >= 0
     */
    public void setTransitionCost(WM from, WM to, int cost) {
        Map<WM, Integer> m = transitions.get(from);
        if (m == null) {
            m = new HashMap<>();
            transitions.put(from, m);
        }
        m.put(to, cost);
    }

    /**
     * Get the performance cost due to a working mode switch.
     * @param from the initial working mode
     * @param to the new working mode
     * @return the impact over performance. {@code 0} when there is no impact. > 0 otherwise
     */
    public int getTransitionCost(WM from, WM to) {
        Map<WM, Integer> m = transitions.get(from);
        if (m == null) {
            return 0;
        }
        Integer i = m.get(to);
        return i == null ? 0 : i;
    }
}
