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
import eu.dc4cities.controlsystem.model.json.JsonUtils;

import java.util.List;

/**
 * Holds monitoring metrics for a given EASC.
 */
public class EascMetrics {

	private String eascName;
	private List<ActivityMetrics> activities;
	
	@JsonCreator
	public EascMetrics(@JsonProperty("eascName") String eascName) {
		this.eascName = eascName;
	}
	
	/**
	 * Returns the name of the EASC the metrics refer to.
	 * 
	 * @return the name of the EASC
	 */
	public String getEascName() {
		return eascName;
	}

	/**
	 * Returns the list of activities the EASC is running, with related metrics.
	 * 
	 * @return the list of activities in the EASC
	 */
	public List<ActivityMetrics> getActivities() {
		return activities;
	}

	public void setActivities(List<ActivityMetrics> activities) {
		this.activities = activities;
	}

	@Override
	public String toString() {
		return JsonUtils.toString(this);
	}
	
}
