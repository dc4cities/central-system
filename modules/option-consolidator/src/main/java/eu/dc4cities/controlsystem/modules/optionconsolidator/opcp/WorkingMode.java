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

import eu.dc4cities.controlsystem.model.easc.Activity;

import java.util.Arrays;
import java.util.Objects;

/**
 * Indicate a possible working mode for an activity.
 * <p/>
 * A working mode is composed by slots. Each slot denotes a particular
 * moment in the activity lifetime. Slots are ordered in the chronological order.
 * The slot duration will be indicated directly inside the {@link OPCP}.
 *
 *
 * @see SimpleActivity
 */
public class WorkingMode {

    /**
     * The associated activity.
     */
    private Activity activity;

    /**
     * The mode identifier.
     */
    private int id;

    /**
     * All the slots.
     */
    private WorkingModeSlot[] slots;

    /**
     * New working mode.
     *
     * @param id       the working mode identifier
     * @param optSlots the slots that compose the working mode.
     * @param a        the associated activity
     */
    public WorkingMode(int id, WorkingModeSlot[] optSlots, Activity a) {
        this.id = id;
        slots = Arrays.copyOf(optSlots, optSlots.length);
        activity = a;
    }

    /**
     * Get the activity associated to the working mode.
     *
     * @return an activity
     */
    public Activity getActivity() {
        return activity;
    }

    /**
     * Get the slots composing the working mode.
     *
     * @return an array of slots.
     */
    public WorkingModeSlot[] getSlots() {
        return slots;
    }

    /**
     * Get the working mode identifier.
     *
     * @return the identifier provided during the instantiation
     */
    public int getName() {
        return id;
    }

    @Override
    public String toString() {
        int[] pwr = new int[slots.length];
        int[] pe = new int[slots.length];
        int i = 0;
        for (WorkingModeSlot s : slots) {
            pwr[i] = s.powerDemand();
            pe[i] = s.penalty();
            i++;
        }
        return "WorkingMode " + id + "\n"
                + "\tPower  : " + Arrays.toString(pwr) + "\n"
                + "\tPenalty: " + Arrays.toString(pe);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WorkingMode that = (WorkingMode) o;

        return id == that.id && Arrays.equals(slots, that.slots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, slots);
    }
}
