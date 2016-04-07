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

/**
 * A modifier to apply over a {@link Revenue} or a {@link Cost}.
 *
 */
public class Modifier {
    private int threshold;
    private int penalty;

    private boolean flat = true;
    private int stepping;

    /**
     * A new modifier.
     *
     * @param th the performance threshold
     * @param p  the associated price.
     */
    public Modifier(int th, int p) {
        threshold = th;
        penalty = p;
    }

    /**
     * shortcut.
     * @return {@code linear(1)}
     */
    public Modifier linear() {
        return linear(1);
    }

    /**
     * State the price varies linearly with the performance threshold
     * @param st the stepping function.
     * @return {@code this}
     */
    public Modifier linear(int st) {
        flat = false;
        stepping = st;
        return this;
    }

    /**
     * Get the threshold.
     * @return a performance indicator
     */
    public int threshold() {
        return threshold;
    }

    /**
     * Get the penalty.
     * @return the penalty
     */
    public int penalty() {
        return penalty;
    }

    @Override
    public String toString() {
        return "{threshold=" + threshold + ", penaltyVariable=" + penalty + (flat ? "; flat" : "; linear; stepping=" + stepping) + "}";
    }

    /**
     * State the price is a constant whatever the performance is.
     * @return {@code this}
     */
    public Modifier flat() {
        flat = true;
        return this;
    }

    /**
     * Check if the modifier is flat or linear.
     * @return {@code true} for a flat modifier
     */
    public boolean isFlat() {
        return flat;
    }

    /**
     * Get the stepping used for a linear modifier.
     * @return a positive integer
     */
    public int step() {
        return stepping;
    }

    public int diff(int base, int now) {
        if (flat) {
            return 1;
        }
        return (base - now) / stepping;
    }
}
