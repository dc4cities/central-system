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

import java.util.ArrayList;
import java.util.List;

/**
 * Holds monitoring metrics for a given activity.
 * 
 * @see EascMetrics
 */
public class ActivityMetrics {

	private String name;
	private List<ActivityDataCenterMetrics> dataCenters;
	
	@JsonCreator
	public ActivityMetrics(@JsonProperty("name") String name) {
		this.name = name;
		dataCenters = new ArrayList<>();
	}
	
	/**
	 * Returns the name of the activity the metrics refer to.
	 * 
	 * @return the name of the activity
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the list of data centers the activity is running in, with the related metrics.
	 * 
	 * @return the list of metrics per data center
	 */
	public List<ActivityDataCenterMetrics> getDataCenters() {
		return dataCenters;
	}

	public void setDataCenters(List<ActivityDataCenterMetrics> dataCenters) {
		this.dataCenters = dataCenters;
	}
	
}
