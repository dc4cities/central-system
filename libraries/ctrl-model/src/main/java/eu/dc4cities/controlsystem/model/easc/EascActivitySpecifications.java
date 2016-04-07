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
 * Contains the specifications of the activities to be executed on a given EASC.
 */
public class EascActivitySpecifications {

	private String eascName;
	private List<ActivitySpecification> activitySpecifications;
	
	@JsonCreator
	public EascActivitySpecifications(@JsonProperty("eascName") String eascName) {
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
	 * Returns the list of activities to be executed on the EASC. May be empty if there are no activities to run.
	 * 
	 * @return the list of activities for the EASC
	 */
	public List<ActivitySpecification> getActivitySpecifications() {
		return activitySpecifications;
	}

	public void setActivitySpecifications(List<ActivitySpecification> activitySpecifications) {
		this.activitySpecifications = activitySpecifications;
	}
	
	@Override
	public String toString() {
		return JsonUtils.toString(this);
	}
	
}
