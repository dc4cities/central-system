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

/**
 * Reduce the scheduling window by a fixed amount.
 */
public class RangeOffsetReducer extends WindowReducer {

    private int to;
    private int offset = -1;

    /**
     * The reduction.
     *
     * @param offset > 0
     */
    public RangeOffsetReducer(int offset) {
        this.offset = offset;
        to = -1;
    }

    @Override
    public void reduce(OPCP2 pb) {
        if (to < 0) {
            to = pb.getNbSlots();
        }
        int old = to;
        to = to - offset;
        if (to <= 0) {
            to = 1;
        }
        //System.out.println("Reduce from [0-" + old + "[ to [0-" + to + "[");
        endsAt(pb, to);
    }

    public void reset() {
        to = -1;
    }

    @Override
    public Reducer copy() {
        return new RangeOffsetReducer(offset);
    }

}
