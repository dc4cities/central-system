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
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Energy;

/**
 * Defines the amount of energy assigned to an entity (e.g. a data center) for a given time range.
 */
public class EnergyQuota {

	private int startTimeSlot;
	private int endTimeSlot;
	private Amount<Energy> energy;
	
	@JsonCreator
	public EnergyQuota(@JsonProperty("startTimeSlot") int startTimeSlot, @JsonProperty("endTimeSlot") int endTimeSlot, 
			@JsonProperty("energy") Amount<Energy> energy) {
		this.startTimeSlot = startTimeSlot;
		this.endTimeSlot = endTimeSlot;
		this.energy = energy;
	}

	/**
	 * Returns the start time slot of the time range.
	 * 
	 * @return the start time slot of the time range, inclusive
	 */
	public int getStartTimeSlot() {
		return startTimeSlot;
	}

	/**
	 * Returns the end time slot of the time range.
	 * 
	 * @return the end time slot of the time range, exclusive
	 */
	public int getEndTimeSlot() {
		return endTimeSlot;
	}

	/**
	 * Returns the energy quota for the time range.
	 * 
	 * @return the energy quota for the time range, in Wh
	 */
	public Amount<Energy> getEnergy() {
		return energy;
	}
	
}
