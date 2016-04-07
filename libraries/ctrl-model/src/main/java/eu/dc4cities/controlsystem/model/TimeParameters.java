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

import eu.dc4cities.controlsystem.model.json.JsonUtils;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Duration;

/**
 * Holds time-related parameters that can be used in REST requests. Single members may or may not be specified depending
 * on the request.
 */
public class TimeParameters {

	private DateTime dateNow;
	private DateTime dateFrom;
	private DateTime dateTo;
	private Amount<Duration> timeSlotDuration;
	
	public TimeParameters() {}
	
	/**
	 * Creates a new instance initialized with the given current date and time range.
	 * 
	 * @param dateNow the current date
	 * @param timeRange the time range
	 */
	public TimeParameters(DateTime dateNow, TimeSlotBasedEntity timeRange) {
		this.dateNow = dateNow;
		this.dateFrom = timeRange.getDateFrom();
		this.dateTo = timeRange.getDateTo();
		this.timeSlotDuration = timeRange.getTimeSlotDuration();
	}
	
	/**
	 * Creates a new instance as a copy of the given source.
	 * 
	 * @param source the time parameters to copy
	 */
	public TimeParameters(TimeParameters source) {
		dateNow = source.dateNow;
		dateFrom = source.dateFrom;
		dateTo = source.dateTo;
		timeSlotDuration = source.timeSlotDuration;
	}
	
	/**
	 * Returns the current time seen at the Control System. Useful for propagating the current time to external systems
	 * when using the virtual clock.
	 * 
	 * @return the current time at the Control System
	 */
	public DateTime getDateNow() {
		return dateNow;
	}
	
	public void setDateNow(DateTime dateNow) {
		this.dateNow = dateNow;
	}
	
	/**
	 * Returns the start of a time range.
	 * 
	 * @return the start of a time range
	 */
	public DateTime getDateFrom() {
		return dateFrom;
	}
	
	public void setDateFrom(DateTime dateFrom) {
		this.dateFrom = dateFrom;
	}
	
	/**
	 * Returns the end of a time range.
	 * 
	 * @return the end of a time range
	 */
	public DateTime getDateTo() {
		return dateTo;
	}
	
	public void setDateTo(DateTime dateTo) {
		this.dateTo = dateTo;
	}
	
	/**
	 * Returns the duration of a time slot in which the time range is split.
	 * 
	 * @return the duration of a time slot
	 */
	public Amount<Duration> getTimeSlotDuration() {
		return timeSlotDuration;
	}

	public void setTimeSlotDuration(Amount<Duration> timeSlotDuration) {
		this.timeSlotDuration = timeSlotDuration;
	}

	@Override
	public String toString() {
		return JsonUtils.toString(this);
	}
	
}
