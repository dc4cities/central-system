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

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.dc4cities.controlsystem.model.json.JsonUtils;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Duration;
import javax.measure.unit.SI;

/**
 * Base object for representing entities that are based on splitting a given time interval into fixed-length time slots.<br>
 * Different properties may be associated to each time slot depending on the entity.<br>
 * This class is instantiated directly to represent a time range divided into slots, when no additional information is required.
 */
public class TimeSlotBasedEntity {

	protected DateTime dateFrom;
	protected DateTime dateTo;
	protected Amount<Duration> timeSlotDuration;
	
	/**
	 * Creates a new empty TimeSlotBasedEntity.
	 */
	public TimeSlotBasedEntity() {}
	
	/**
	 * Creates a new TimeSlotBasedEntity as a copy of the given one.
	 */
	public TimeSlotBasedEntity(TimeSlotBasedEntity source) {
		this.dateFrom = source.dateFrom;
		this.dateTo = source.dateTo;
		this.timeSlotDuration = source.timeSlotDuration;
	}
	
	/**
	 * Creates a new TimeSlotBasedEntity based on the given time parameters.
	 * 
	 * @param timeParameters the time parameters to use
	 */
	public TimeSlotBasedEntity(TimeParameters timeParameters) {
		this.dateFrom = timeParameters.getDateFrom();
		this.dateTo = timeParameters.getDateTo();
		this.timeSlotDuration = timeParameters.getTimeSlotDuration();
	}
	
	/**
	 * Returns the start date and time of the time interval to which the entity is associated. Minute precision is used
	 * (i.e. seconds are always zero).
	 * 
	 * @return the start date and time, with minute precision
	 */
	public DateTime getDateFrom() {
		return dateFrom;
	}
	
	public void setDateFrom(DateTime dateFrom) {
		this.dateFrom = dateFrom;
	}
	
	/**
	 * Returns the end date and time of the time interval to which the entity is associated. Minute precision is used
	 * (i.e. seconds are always zero).
	 * 
	 * @return the end date and time, with minute precision
	 */
	public DateTime getDateTo() {
		return dateTo;
	}
	
	public void setDateTo(DateTime dateTo) {
		this.dateTo = dateTo;
	}
	
	/**
	 * Returns the duration of the time slots in which the time interval must divided.
	 * 
	 * @return the time slot duration in minutes
	 */
	public Amount<Duration> getTimeSlotDuration() {
		return timeSlotDuration;
	}
	
	public void setTimeSlotDuration(Amount<Duration> timeSlotDuration) {
		this.timeSlotDuration = timeSlotDuration;
	}

	/**
	 * Copies the time interval and time slot duration from the source object to this object.
	 * 
	 * @param source the source of interval definition
	 */
	public void copyIntervalFrom(TimeSlotBasedEntity source) {
		this.dateFrom = source.dateFrom;
		this.dateTo = source.dateTo;
		this.timeSlotDuration = source.timeSlotDuration;
	}
	
	/**
	 * Returns the number of time slots in the interval.
	 * 
	 * @return the number of time slots
	 */
	@JsonIgnore
	public int getNumOfTimeSlots() {
		long durationMillis = timeSlotDuration.longValue(SI.MILLI(SI.SECOND));
		long intervalMillis = dateTo.getMillis() - dateFrom.getMillis();
		long numOfTimeSlots = intervalMillis / durationMillis;
		if (numOfTimeSlots * durationMillis != intervalMillis) {
			throw new RuntimeException("Time range cannot be split into an integer number of time slots");
		} else if (numOfTimeSlots > Integer.MAX_VALUE) {
			throw new RuntimeException("Number of time slots too large: " + numOfTimeSlots);
		}
		return (int) numOfTimeSlots;
	}
	
	@Override
	public String toString() {
		return JsonUtils.toString(this);
	}
	
}
