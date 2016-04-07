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

package eu.dc4cities.controlsystem.modules.optionconsolidator;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;

/**
 * Generate all the possible permutations for some elements.
 *
 *
 */
public class AllTuplesGenerator<T> implements Generator<T[]> {

    private T[][] doms;

    private int[] indexes;

    private int nbStates;

    private int k;

    private Class<T> cl;

    /**
     * New generator.
     *
     * @param cl      the objects class
     * @param domains the domain to iterate on
     */
    public AllTuplesGenerator(Class<T> cl, List<List<T>> domains) {
        doms = (T[][]) new Object[domains.size()][];
        indexes = new int[domains.size()];
        int i = 0;
        nbStates = 1;
        this.cl = cl;
        for (List<T> v : domains) {
            indexes[i] = 0;

            doms[i] = v.toArray((T[]) new Object[v.size()]);
            nbStates *= doms[i].length;
            i++;
        }
    }

    public void reset() {
        k = 0;
    }

    public int count() {
        return nbStates;
    }

    public int done() {
        return k;
    }

    @Override
    public boolean hasNext() {
        return k < nbStates;
    }

    @Override
    public T[] next() {
        T[] tuple = (T[]) Array.newInstance(cl, doms.length);
        for (int x = 0; x < doms.length; x++) {
            tuple[x] = doms[x][indexes[x]];
        }
        for (int x = 0; x < doms.length; x++) {
            indexes[x]++;
            if (indexes[x] < doms[x].length) {
                break;
            }
            indexes[x] = 0;
        }
        k++;
        return tuple;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T[]> iterator() {
        return this;
    }
}
