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

package eu.dc4cities.controlsystem.model.easc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Describes a work with its possible working modes.
 * 
 * @deprecated use inversion of control instead of option plans
 */
@Deprecated
public class WorkOption implements Comparable<WorkOption>{

    private int startTimeSlot;
    private int endTimeSlot;

    private List<ModeOption> modeOptions = new ArrayList<ModeOption>();

    /**
     * The number of the time slot when this work must start.
     *
     * @return the start time slot number
     */
    public int getStartTimeSlot() {
        return startTimeSlot;
    }

    public void setStartTimeSlot(int startTimeSlot) {
        this.startTimeSlot = startTimeSlot;
    }

    /**
     * The number of the time slot when this work must end. This is inclusive, i.e. the work will run until the end of
     * the time slot.
     *
     * @return the end time slot number
     */
    public int getEndTimeSlot() {
        return endTimeSlot;
    }

    public void setEndTimeSlot(int endTimeSlot) {
        this.endTimeSlot = endTimeSlot;
    }

    /**
     * Returns the possible working modes.
     *
     * @return an immutable list of working modes. Should not be empty.
     */
    public List<ModeOption> getModeOptions() {
        return Collections.unmodifiableList(modeOptions);
    }

    @JsonDeserialize(contentAs = ModeOption.class)
    public void setModeOptions(List<ModeOption> modeOptions) {
        this.modeOptions = modeOptions;
    }

    /**
     * Add a possible working mode. It is expected that the name of the working mode is not already used in the already
     * registered options.
     * <p/>
     * Todo: ensure there is no other options with the same name?
     *
     * @param modeOption the working mode option to add
     */
    public void addModeOption(ModeOption modeOption) {
        modeOptions.add(modeOption);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("WorkOption{");
        b.append("durations=[" + startTimeSlot + ":" + endTimeSlot + "], ");
        b.append("modeOptions='");
        for (Iterator<ModeOption> ite = modeOptions.iterator(); ite.hasNext(); ) {
            b.append(ite.next().getWorkingMode());
            if (ite.hasNext()) {
                b.append(", ");
            }
        }
        return b.append("'}").toString();
    }

    public int compareTo(WorkOption o) {
        return Integer.compare(this.startTimeSlot, o.startTimeSlot);
    }
}
