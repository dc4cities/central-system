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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the list of alternative option plans that can be selected to execute activities on a certain EASC.
 * 
 * @deprecated use inversion of control instead of option plans
 */
@Deprecated
public class EascOptionPlan extends TimeSlotBasedEntity {

    private String eascName;
    private List<ActivityOption> activityOptions;

    /**
     * New instance.
     *
     * @param eascName the name of the EASC
     */
    @JsonCreator
    public EascOptionPlan(@JsonProperty("eascName") String eascName) {
        this.eascName = eascName;
        activityOptions = new ArrayList<ActivityOption>();
    }

    /**
     * Returns the EASC name. The name should be known by the central-system.
     *
     * @return the EASC name
     */
    public String getEascName() {
        return eascName;
    }

    /**
     * Adds an activity to be run by the EASC.
     * It is expected that the name of the activity is not already used in the already registered activities.
     * <p/>
     * Todo: ensure there is no options with the same name ?
     *
     * @param activityOption the activity to add
     */
    public void addActivityOption(ActivityOption activityOption) {
        activityOptions.add(activityOption);
    }

    /**
     * Returns all the activities the EASC wants to run with their potential alternatives. All option plans for a given
     * activity must be contained in the same item of this list, i.e. there cannot be multiple {@code ActivityOption}s
     * with the same activity name.
     *
     * @return the list of options for activities; an empty list if the EASC has no activities to run
     */
    public List<ActivityOption> getActivityOptions() {
        return activityOptions;
    }

    /**
     * Sets the list of activities to be run by the EASC. Note that this will overwrite any activity added via
     * {@code addActivityOption}.
     * 
     * @param activityOptions the list of activity options
     */
    @JsonDeserialize(contentAs=ActivityOption.class)
    public void setActivityOptions(List<ActivityOption> activityOptions) {
		this.activityOptions = activityOptions;
	}
	
}
