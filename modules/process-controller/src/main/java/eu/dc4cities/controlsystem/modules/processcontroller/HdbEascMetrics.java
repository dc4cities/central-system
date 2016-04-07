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

package eu.dc4cities.controlsystem.modules.processcontroller;

import java.util.LinkedList;
import java.util.List;

/**
 * Holds historical database metrics related to a given EASC.
 */
public class HdbEascMetrics {
	
	private String eascName;
	private List<HdbActivityMetrics> activities = new LinkedList<>();
	
	public HdbEascMetrics(String eascName) {
		this.eascName = eascName;
	}

	/**
	 * Returns the name of the EASC.
	 * 
	 * @return the name of the EASC
	 */
	public String getEascName() {
		return eascName;
	}
	
	/**
	 * Returns the metrics for every activity running in the EASC.
	 * 
	 * @return the activity metrics
	 */
	public List<HdbActivityMetrics> getActivities() {
		return activities;
	}

	/**
	 * Returns the metrics for the given activity, if any.
	 * 
	 * @param activityName the name of the activity
	 * @return the activity metrics or {@code null} if not found
	 */
	public HdbActivityMetrics getActivity(String activityName) {
		for (HdbActivityMetrics activity : activities) {
			if (activity.getActivityName().equals(activityName)) {
				return activity;
			}
		}
		return null;
	}
	
	/**
	 * Adds the given activity metrics to this data center.
	 * 
	 * @param easc the activity metrics to add
	 */
	public void addActivity(HdbActivityMetrics activity) {
		activities.add(activity);
	}
	
	public void setActivities(List<HdbActivityMetrics> activities) {
		this.activities = activities;
	}
	
}
