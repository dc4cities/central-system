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
import java.util.stream.Collectors;

/**
 * Created by fhermeni on 27/09/2015.
 */
public class RenCapaComparator implements Comparator<Integer> {

    private List<PowerSource> sources;

    private int asc = 1;

    public RenCapaComparator(List<PowerSource> sources, boolean asc) {
        this.sources = sources.stream().filter(s -> s.slots()[0].renPct() == 100).collect(Collectors.toList());
        if (!asc) {
            this.asc = -1;
        }
    }

    @Override
    public int compare(Integer t1, Integer t2) {
        int capa1 = 0, capa2 = 0;
        for (PowerSource src : sources) {
            capa1 += src.slots()[t1].renPct() * src.slots()[t1].peak();
            capa2 += src.slots()[t2].renPct() * src.slots()[t2].peak();
        }
        return asc * (capa1 - capa2);
    }
}
