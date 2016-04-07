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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Specify an activity that can be scheduling according to multiple possible working mode.
 *
 *
 */
public class SimpleActivity {

    private String easc;

    /**
     * The supported working modes.
     */
    private List<WorkingMode> workingModes;

    /**
     * SimpleActivity identifier
     */
    private String id;

    /**
     * New activity.
     *
     * @param easc the easc identifier
     * @param name the activity identifier
     */
    public SimpleActivity(String easc, String name) {
        this(easc, name, new ArrayList<WorkingMode>());
    }

    /**
     * New activity.
     *
     * @param easc  the easc identifier
     * @param name  the activity identifier
     * @param modes the associated working modes
     */
    public SimpleActivity(String easc, String name, List<WorkingMode> modes) {
        this.id = name;
        this.easc = easc;
        workingModes = modes;
    }

    /**
     * Get the identifier of the easc running the task.
     *
     * @return the easc identifier.
     */
    public String getEasc() {
        return easc;
    }

    /**
     * Add a possible working mode.
     *
     * @param o the working mode to add
     * @return {@code true} if the working mode has been added successfully
     */
    public boolean add(WorkingMode o) {
        return workingModes.add(o);
    }

    /**
     * Get the possible working modes.
     *
     * @return a list that is not supposed to be empty.
     */
    public List<WorkingMode> getWorkingModes() {
        return Collections.unmodifiableList(workingModes);
    }

    /**
     * Get the activity identifier
     *
     * @return the identifier provided during the instantiation
     */
    public String getName() {
        return id;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("SimpleActivity ").append(easc).append(":").append(id).append(":\n");
        for (WorkingMode o : getWorkingModes()) {
            b.append(o).append("\n");
        }
        return b.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SimpleActivity that = (SimpleActivity) o;
        return easc.equals(that.easc) && id.equals(that.id) && workingModes.equals(that.workingModes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(easc, workingModes, id);
    }
}
