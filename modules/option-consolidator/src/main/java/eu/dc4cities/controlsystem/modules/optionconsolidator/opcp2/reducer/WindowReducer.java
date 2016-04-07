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

package eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.reducer;

import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.OPCP2;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.variables.IntVar;

/**
 * Created by fhermeni on 19/09/2015.
 */
public abstract class WindowReducer implements Reducer {

    public void endsAt(OPCP2 pb, int to) {
        for (int t = to; t < pb.getNbSlots(); t++) {
            for (IntVar[] states : pb.getAllStates()) {
                pb.getSolver().post(ICF.arithm(states[t], "=", states[t].getValue()));
            }
            for (IntVar[] src : pb.getSourcePowerUsage()) {
                pb.getSolver().post(ICF.arithm(src[t], "=", src[t].getValue()));
            }
        }
    }

    public void startsAt(OPCP2 pb, int from) {
        for (int t = 0; t < Math.min(from + 1, pb.getNbSlots()); t++) {
            for (IntVar[] states : pb.getAllStates()) {
                pb.getSolver().post(ICF.arithm(states[t], "=", states[t].getValue()));
            }
            for (IntVar[] src : pb.getSourcePowerUsage()) {
                pb.getSolver().post(ICF.arithm(src[t], "=", src[t].getValue()));
            }
        }

    }
}
