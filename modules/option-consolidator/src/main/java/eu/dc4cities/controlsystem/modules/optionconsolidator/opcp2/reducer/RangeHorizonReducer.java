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
 * Reduce a problem horizon.
 */
public class RangeHorizonReducer extends WindowReducer {

    private int from, to;

    public RangeHorizonReducer(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public RangeHorizonReducer(int to) {
        this(0, to);
    }

    @Override
    public void reduce(OPCP2 pb) {
        this.startsAt(pb, from);
        this.endsAt(pb, to);
    }

    @Override
    public void reset() {

    }

    @Override
    public Reducer copy() {
        return new RangeHorizonReducer(from, to);
    }

}
