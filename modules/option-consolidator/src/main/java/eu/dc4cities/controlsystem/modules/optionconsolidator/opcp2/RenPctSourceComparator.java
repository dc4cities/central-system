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

import java.util.Comparator;
import java.util.List;

/**
 * Created by fhermeni on 27/09/2015.
 */
public class RenPctSourceComparator implements Comparator<Integer> {

    private List<PowerSource> sources;

    private int slot;

    public RenPctSourceComparator(int t, List<PowerSource> sources) {
        this.sources = sources;
        slot = t;
    }

    @Override
    public int compare(Integer s1, Integer s2) {
        return sources.get(s1).slots()[slot].renPct() - sources.get(s1).slots()[slot].renPct();
    }
}
