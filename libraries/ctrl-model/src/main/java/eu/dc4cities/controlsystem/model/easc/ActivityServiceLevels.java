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

import java.util.LinkedList;
import java.util.List;

/**
 * Holds service levels for a given activity.
 */
public class ActivityServiceLevels {
	
    private String activityName;
    private List<ServiceLevel> serviceLevels = new LinkedList<>();
    
    @JsonCreator
	public ActivityServiceLevels(@JsonProperty("activityName") String activityName) {
		this.activityName = activityName;
	}
	
	public String getActivityName() {
		return activityName;
	}

	public List<ServiceLevel> getServiceLevels() {
		return serviceLevels;
	}

	public void setServiceLevels(List<ServiceLevel> serviceLevels) {
		this.serviceLevels = serviceLevels;
	}
    
}
