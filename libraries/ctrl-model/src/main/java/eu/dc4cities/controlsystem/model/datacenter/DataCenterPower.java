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
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.TimeSlotPower;

import java.util.LinkedList;
import java.util.List;

/**
 * Holds power usage values for a data center over a given time range, split by time slot.
 */
public class DataCenterPower extends TimeSlotBasedEntity {

	private String dataCenterName;
	private List<TimeSlotPower> powerValues = new LinkedList<>();
	
	@JsonCreator
	public DataCenterPower(@JsonProperty("dataCenterName") String dataCenterName) {
		this.dataCenterName = dataCenterName;
	}

	public String getDataCenterName() {
		return dataCenterName;
	}
	
	public List<TimeSlotPower> getPowerValues() {
		return powerValues;
	}

	public void setPowerValues(List<TimeSlotPower> powerValues) {
		this.powerValues = powerValues;
	}
	
}
