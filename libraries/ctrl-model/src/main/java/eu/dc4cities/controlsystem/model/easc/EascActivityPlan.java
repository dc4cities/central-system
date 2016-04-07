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
 * Indicates all the activities running on an EASC.
 */
public class EascActivityPlan extends TimeSlotBasedEntity {

	private String eascName;
	private List<Activity> activities;
    
	/**
     * New plan.
     *
     * @param eascName the easc identifier
     */
	@JsonCreator
    public EascActivityPlan(@JsonProperty("eascName") String eascName) {
    	this.eascName = eascName;
        activities = new ArrayList<>();
    }

    /**
     * Get the identifier of the easc running the activities
     *
     * @return an easc identifier
     */
    public String getEascName() {
        return eascName;
    }
    
    /**
     * Add an activity.
     * It is expected the name of {@code a} is not already used in the already registered activities
     * <p/>
     * Todo: ensure there is no naming conflicts
     *
     * @param a the activity to add
     * @return {@code true} iff the activity has been added
     */
    public void addActivity(Activity a) {
        activities.add(a);
    }

    /**
     * Get the activities running inside the easc.
     *
     * @return the list of activities. Should not be empty.
     */
    public List<Activity> getActivities() {
        return activities;
    }
    
    /**
     * Set the activities running inside the easc.
     *
     */
    @JsonDeserialize(contentAs=Activity.class)
    public void setActivities(List<Activity> activities) {
		this.activities = activities;
	}
    
}
