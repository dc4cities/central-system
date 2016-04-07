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

import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.OPCP;

import java.util.Arrays;

/**
 * Model a power source.
 * The source is characterised by its identifier and its offering per time-slot.
 * <p/>
 * Each slot denotes a particular moment in the power-source lifetime.
 * Slots must be ordered in the chronological order.
 * The slot duration will be indicated directly inside the {@link OPCP}.
 *
 *
 */
public class PowerSource {

    /**
     * The time-slots.
     */
    private PowerSourceSlot[] slots;

    /**
     * The source identifier.
     */
    private String id;

    private String dcId;


    /**
     * New power source.
     *
     * @param name     the source identifier
     * @param pwrSlots the time-slots that characterise the source profile
     */
    public PowerSource(String name, PowerSourceSlot[] pwrSlots) {
        slots = pwrSlots;
        id = name;
        dcId = "-";
    }

    /**
     * Get the source profile
     *
     * @return a non-empty array.
     */
    public PowerSourceSlot[] slots() {
        return slots;
    }

    public String pretty() {
        if (dcId.equals("-")) {
            return id;
        }
        return dcId + ":" + id;
    }
    /**
     * Get the source identifier.
     *
     * @return the identifier provided during instantiation
     */
    public String name() {
        return id;
    }

    @Override
    public String toString() {
        int[] p = new int[slots.length];
        int[] r = new int[slots.length];
        int i = 0;
        for (PowerSourceSlot slot : slots) {
            p[i] = slot.peak();
            r[i] = slot.renPct();
            i++;
        }
        return "Source " + name() + "\n"
                + "\tPower : " + Arrays.toString(p) + "\n"
                + "\tRenPct: " + Arrays.toString(r);
    }

    /**
     * Get the datacenter identfier;
     *
     * @return the datacenter identifier
     */
    public String dcId() {
        return dcId;
    }

    public PowerSource dcId(String i) {
        dcId = i;
        return this;
    }

    /**
     * Test if the source is always pure.
     *
     * @return {@code true} iff all the slots have a renPCT equals to 100%
     */
    public boolean isPure() {
        for (PowerSourceSlot s : slots) {
            if (s.renPct() != 100) {
                return false;
            }
        }
        return true;
    }
}
