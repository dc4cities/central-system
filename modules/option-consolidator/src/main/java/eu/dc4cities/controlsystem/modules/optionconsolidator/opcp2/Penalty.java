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

import java.util.Set;
import java.util.TreeSet;

/**
 * A flexible revenue with a reward that depends on a performance level.
 *
 *
 */
public class Penalty {

    private int basePerf, basePenalty;

    private Set<Modifier> all;

    /**
     * New revenue
     *
     * @param perf  the basic performance
     * @param price the default penalty if not met. < 0
     */
    public Penalty(int perf, int price) {
        if (price >= 0) {
            throw new IllegalArgumentException("Default penalty should be negative. Got " + price);
        }
        basePerf = perf;
        basePenalty = price;
        all = new TreeSet<>((o1, o2) -> o2.threshold() - o1.threshold());
    }

    /**
     * Add a modifier.
     *
     * @param m the new revenue threshold
     * @return {@code this}
     */
    public Penalty add(Modifier m) {
        all.add(m);
        return this;
    }

    /**
     * Returns the revenue for a given range of performance.
     *
     * @param max the maximum performance (excluded)
     * @return an array of revenues for a performance varying in [0;max[
     */
    public int[] flatten(int max) {
        int[] values = new int[max];
        for (int i = 0; i < max; i++) {
            values[i] = revenue(i);// Math.max(0, revenue(i));
        }
        return values;
    }

    public int revenue(int perf) {
        //Catch the bounds
        if (all.isEmpty()) {
            //it is an objective, basePenalty should be < 0
            return perf >= basePerf ? 0 : basePenalty;
        }

        int ub = Integer.MAX_VALUE;
        boolean got = false;
        Modifier picked = null;
        for (Modifier m : all) {
            picked = m;
            int lb = m.threshold();
            if (perf < ub && lb <= perf) {
                got = true;
                break;
            }
            ub = lb;
        }
        if (!got) {
            //We didn't catch any interval, return the basePrice
            return basePenalty;
        }
        return price(picked, perf);
    }

    private int price(Modifier m, int p) {
        return m.penalty() * Math.abs(m.diff(p, basePerf));
    }

    public int basePerf() {
        return basePerf;
    }

    public int basePenalty() {
        return basePenalty;
    }

    @Override
    public String toString() {
        return "Penalty{basePerf=" + basePerf + ", basePenalty" + basePenalty + ", modifiers=" + all + '}';
    }

    public String pretty(int ub) {
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < ub; i++) {
            b.append(i).append(":").append(revenue(i));
            if (i != ub - 1) {
                b.append(", ");
            }
        }
        return b.append("]").toString();
    }
}
