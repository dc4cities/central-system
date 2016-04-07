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
 * Contains information about the status of a given activity w.r.t. SLAs.
 * 
 * @see EascStatus
 */
public class ActivityStatus {

	private String activityName;
	private List<StatusAlert> alerts = new LinkedList<>();
	
	@JsonCreator
	public ActivityStatus(@JsonProperty("activityName") String activityName) {
		this.activityName = activityName;
	}

	/**
	 * Returns the name of the activity the status refers to.
	 * 
	 * @return the name of the activity
	 */
	public String getActivityName() {
		return activityName;
	}
	
	/**
	 * Returns the list of alerts for this activity. The list is empty when there is no alert.
	 * 
	 * @return the activity alerts
	 */
	public List<StatusAlert> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<StatusAlert> alerts) {
		this.alerts = alerts;
	}

	public void addAlert(StatusAlert statusAlert) {
		alerts.add(statusAlert);
	}
	
}
