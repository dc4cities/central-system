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
 */
public class Revenue {

    private int basePerf, basePrice;

    private Set<Modifier> all;
    /**
     * New revenue
     *
     * @param perf  the basic performance
     * @param price the default price
     */
    public Revenue(int perf, int price) {
        basePerf = perf;
        basePrice = price;
        all = new TreeSet<>((o1, o2) -> o2.threshold() - o1.threshold());
        //if (price != 0) {
        //all.add(new Modifier(perf, 0).linear());
        //}
    }

    /**
     * Add a modifier.
     *
     * @param m the new revenue threshold
     * @return {@code this}
     */
    public Revenue add(Modifier m) {
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
        int [] values = new int[max];
        for (int i = 0; i < max; i++) {
            values[i] = revenue(i);// Math.max(0, revenue(i));
            if (i > 0) {
                if (values[i - 1] > values[i]) {
                    throw new UnsupportedOperationException("The revenue is not a monotonic increasing function: revenue(" + (i - 1) + ")=" + revenue(i - 1) + " but revenue(" + (i) + ")=" + revenue(i));
                }
            }
        }
        return values;
    }

    /**
     * Compute the revenue depending on a performance.
     * Catch the interval ]x;y] where x > perf && perf >= y then considers y as the modifier to use.
     * If there is no modifier, being under the basePerf return a 0 revenue
     * @param perf the performance
     * @return the associated revenue
     */
    /*public int revenue(int perf) {
        if (perf == basePerf) {
            return basePrice;
        }
        //Too high performance ?
        int max = all.iterator().next().threshold();
        if (perf >= max) {
            return price(all.iterator().next(), max);
        }

        Modifier ub = all.iterator().next();
        Modifier lb = null;
        for (Modifier m : all) {
            lb = m;
            if (ub.threshold() > perf && perf >= lb.threshold()) {
                return price(lb, perf);
            }
            ub = lb;
        }
        //Hard constraint ?
        return 0;
        //System.out.println(ub + " " + perf + " " + lb + "\t" + lb.diff(perf, basePerf) + " pen=" + lb.penalty());
    }*/
    public int revenue(int perf) {
        //Catch the bounds
        if (all.isEmpty()) {
            if (basePrice > 0) {
                //it is an income
                return perf >= basePerf ? basePrice : 0;
            }
            //it is an objective, basePrice should be < 0
            return perf >= basePerf ? 0 : basePrice;
        }

        int ub = Integer.MAX_VALUE;
        boolean got = false;
        Modifier picked = null;

        if (all.iterator().next().threshold() < perf && all.iterator().next().penalty() < 0 && basePerf < perf) {
            //Basically, we are upper than the base perf and there is no rewards
            //so we return the baseprice
            return basePrice;//price(all.iterator().next(), perf);
        }
        for (Modifier m : all) {
            picked = m;
            int lb = m.threshold();
            if (perf < ub && lb <= perf) {
                //Got catch an interval and it is not above the highest value possible
                got = true;
                break;
            }
            ub = lb;
        }
        if (!got) {
            //We didn't catch any interval, return the basePrice
            return basePrice;
        }
        return price(picked, perf);
    }

    private int price(Modifier m, int p) {
        return basePrice + m.penalty() * Math.abs(m.diff(p, basePerf));
    }

    public int basePerf() {
        return basePerf;
    }

    public int basePrice() {
        return basePrice;
    }

    @Override
    public String toString() {
        return "Pricing{basePerf=" + basePerf + ", basePrice=" + basePrice + ", modifiers=" + all + '}';
    }

    public Set<Modifier> modifiers() {
        return all;
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
