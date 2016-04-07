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

import java.util.Collections;
import java.util.List;

/**
 * Define an activity state.
 *
 */
public class State {

    /**
     * The possible performance level.
     */
    private List<PerfLevel> wms;

    /**
     * A new state
     *
     * @param pl the default performance level.
     */
    public State(PerfLevel pl) {
        this(Collections.singletonList(pl));
    }

    /**
     * A new state.
     * Used when the state represent the activity running on multiple datacenter.
     *
     * @param pls all the wrapping performance levels.
     */
    public State(List<PerfLevel> pls) {
        this.wms = pls;
    }

    public int power() {
        int p = 0;
        for (PerfLevel w : wms) {
            p += w.power();
        }
        return p;
    }

    public int perf() {
        int p = 0;
        for (PerfLevel w : wms) {
            p += w.perf();
        }
        return p;

    }

    @Override
    public String toString() {
        return "{" + name() + ": power=" + power() +  ", perf=" + perf() + '}';
    }

    public String name() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < wms.size(); i++) {
            PerfLevel wm = wms.get(i);
            b.append(wm.toString());
            if (i != wms.size() - 1) {
                b.append("; ");
            }
        }
        return b.toString();
    }

    public List<PerfLevel> perfLevels() {
        return wms;
    }

    public PerfLevel perfLevel(String d) {
        for (PerfLevel p : perfLevels()) {
            if (p.WM().getDc().equals(d)) {
                return p;
            }
        }
        return null;
    }
}
