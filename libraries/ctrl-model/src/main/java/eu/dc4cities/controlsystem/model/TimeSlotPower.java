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

package eu.dc4cities.controlsystem.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Power;

/**
 * Represents an amount of power related to a single time slot. Depending on the situation in which this class is used the
 * power may refer to consumed power, available power and so on.
 */
public class TimeSlotPower implements Comparable<TimeSlotPower> {
	private int timeSlot;
	private Amount<Power> power;
	
	@JsonCreator
	public TimeSlotPower(@JsonProperty("timeSlot") int timeSlot) {
		this.timeSlot = timeSlot;
	}

	public TimeSlotPower(int timeSlot, Amount<Power> power) {
		this.timeSlot = timeSlot;
		this.power = power;
	}
	
	/**
	 * Creates a new TimeSlotPower as a copy of the given one.
	 * 
	 * @param source the object to copy
	 */
	public TimeSlotPower(TimeSlotPower source) {
		this.timeSlot = source.timeSlot;
		this.power = source.power;
	}
	
	/**
	 * Returns the number of the time slot the power refers to.
	 * 
	 * @return the time slot number
	 */
	public int getTimeSlot() {
		return timeSlot;
	}
	
	public void setTimeSlot(int timeSlot) {
		this.timeSlot = timeSlot;
	}
	
	/**
	 * Returns the amount of power related to the time slot.
	 * 
	 * @return the power in watts
	 */
	public Amount<Power> getPower() {
		return power;
	}
	
	public void setPower(Amount<Power> power) {
		this.power = power;
	}

	/**
	 * Adds the specified power amount to this time slot's power. If the current power is not set, it is considered zero.
	 * 
	 * @param power the power to add
	 */
	public void addPower(Amount<Power> power) {
		if (this.power == null) {
			this.power = power;
		} else {
			this.power = this.power.plus(power);
		}
	}
	
	/**
	 * Multiplies the time slot power by the given factor. Values are rounded to the nearest integer value.
	 * 
	 * @param factor the scale factor
	 */
	public void scalePower(double factor) {
		long scaledValue = Math.round(power.getEstimatedValue() * factor);
		power = Amount.valueOf(scaledValue, power.getUnit());
    }
	
	public int compareTo(TimeSlotPower o) {
		if(this.power.compareTo(o.power) > 0)
			return 1;
		
		if(this.power.compareTo(o.power) < 0)
			return -1;
		
		//in case there are equal power values, we take those that are in advance
		if(this.timeSlot > o.timeSlot)
			return -1;
		
		if(this.timeSlot < o.timeSlot)
			return 1;
			
		return 0;
	}

}
