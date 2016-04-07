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

package eu.dc4cities.controlsystem.model.datacenter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains information about the status of a given EASC within the data center.
 * 
 * @see DataCenterStatus
 */
public class EascStatus {

	private String eascName;
	private List<ActivityStatus> activities = new LinkedList<>();
	
	@JsonCreator
	public EascStatus(@JsonProperty("eascName") String eascName) {
		this.eascName = eascName;
	}

	/**
	 * Returns the name of the EASC the status refers to.
	 * 
	 * @return the EASC name
	 */
	public String getEascName() {
		return eascName;
	}
	
	/**
	 * Returns the status for activities that can run in this EASC and parent data center.
	 * 
	 * @return the activity statuses
	 */
	public List<ActivityStatus> getActivities() {
		return activities;
	}

	public void setActivities(List<ActivityStatus> activities) {
		this.activities = activities;
	}
	
	public void addActivity(ActivityStatus activity) {
		activities.add(activity);
	}
	
}
