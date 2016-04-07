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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Lists the works that have to be completed in the given data center for a certain activity.
 * 
 * @see Activity
 */
public class ActivityDataCenter {

	private String dataCenterName;
	private List<Work> works;
	
	@JsonCreator
	public ActivityDataCenter(@JsonProperty("dataCenterName") String dataCenterName) {
		this.dataCenterName = dataCenterName;
		works = new ArrayList<>();
	}

	/**
	 * Returns the name of the data center in which works are to be executed.
	 * 
	 * @return the data center name
	 */
	public String getDataCenterName() {
		return dataCenterName;
	}

	/**
	 * Returns the list of works to execute in this data center.
	 * 
	 * @return the list of works to execute
	 */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	public List<Work> getWorks() {
		return works;
	}

	public void setWorks(List<Work> works) {
		this.works = works;
	}
	
}
