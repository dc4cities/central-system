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

package eu.dc4cities.controlsystem.modules.optionconsolidator.opcp;

import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Power;
import java.util.Arrays;

/**
 * Model a power source.
 * The source is characterised by its identifier and its offering per time-slot.
 *
 * Each slot denotes a particular moment in the power-source lifetime.
 * Slots are ordered in the chronological order.
 * The slot duration will be indicated directly inside the {@link OPCP}.

 *
 */
public class OldPowerSource {

    /**
     * The time-slots.
     */
    private OldPowerSourceSlot[] slots;

    /**
     * The source identifier.
     */
    private String id;

    /**
     * New power source.
     * @param name the source identifier
     * @param pwrSlots the time-slots that characterise the source profile
     */
    public OldPowerSource(String name, OldPowerSourceSlot[] pwrSlots) {
        slots = Arrays.copyOfRange(pwrSlots, 0, pwrSlots.length);
        id = name;
    }

    /**
     * Get the source profile
     * @return a non-empty array.
     */
    public OldPowerSourceSlot[] getSlots() {
        return slots;
    }

    /**
     * Get the source identifier.
     * @return the identifier provided during instantiation
     */
    public String getName() {
        return id;
    }

    public int[] getPurity(int from, int to) {
        int[] pcts = new int[to - from];
        for (int i = 0; i < to - from; i++) {
            pcts[i] = slots[i].getRenewablePct();
        }
        return pcts;
    }

    @Override
    public String toString() {
        Amount<Power> [] p = new Amount [slots.length];
        int [] r = new int[slots.length];
        float [] c = new float[slots.length];
        int i = 0;
        for (OldPowerSourceSlot slot : slots) {
            p[i] = slot.getPeak();
            r[i] = slot.getRenewablePct();
            i++;
        }
        return "Source " + getName() + "\n"
                + "\tPower : " + Arrays.toString(p) + "\n"
                + "\tRenPct: " + Arrays.toString(r);
    }
}
