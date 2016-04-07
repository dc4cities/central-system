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

package eu.dc4cities.controlsystem.model.util;

import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Duration;
import javax.measure.unit.NonSI;

public class TimeRangeUtils {

	/**
	 * Returns a new time range defined by the given parameters.
	 * 
	 * @param dateFrom the start date of the time range
	 * @param rangeWidth the width of the time range, in hours
	 * @param timeSlotDuration the duration of a time slot in the time range, in minutes
	 * @return a new time range
	 */
	public static TimeSlotBasedEntity newTimeRange(DateTime dateFrom, int rangeWidth, int timeSlotDuration) {
		TimeSlotBasedEntity timeRange = new TimeSlotBasedEntity();
		timeRange.setDateFrom(dateFrom);
		timeRange.setDateTo(dateFrom.plusHours(rangeWidth));
		timeRange.setTimeSlotDuration(Amount.valueOf(timeSlotDuration, NonSI.MINUTE));
		return timeRange;
	}
	
	/**
	 * Calculates the optimization time range nearest to the given date. The time range begins at date if date is the
	 * beginning of a time slot, or at the first time slot after date otherwise.
	 * 
	 * @param date the reference date
	 * @param rangeWidth the width of the time range, in hours
	 * @param timeSlotDuration the duration of a time slot in the time range, in minutes
	 * @return the nearest optimization time range
	 */
	public static TimeSlotBasedEntity calcNearestTimeRange(DateTime date, int rangeWidth, int timeSlotDuration) {
		DateTime dateFrom;
		if (isTimeSlotStart(date, timeSlotDuration)) {
			dateFrom = date;
		} else {
			dateFrom = calcNextTimeSlot(date, timeSlotDuration);
		}
		return newTimeRange(dateFrom, rangeWidth, timeSlotDuration);
	}
	
	/**
	 * Calculates the time range for next optimization loop, based on the current time and the given range parameters.
	 * 
	 * @param rangeWidth the width of the time range, in hours
	 * @param timeSlotDuration the duration of a time slot in the time range, in minutes
	 * @return the time range for the next optimization loop
	 */
	public static TimeSlotBasedEntity calcNextTimeRange(int rangeWidth, int timeSlotDuration) {
		DateTime dateFrom = calcNextTimeSlot(timeSlotDuration);
		return newTimeRange(dateFrom, rangeWidth, timeSlotDuration);
	}
	
	/**
	 * Calculates the start of the next time slot based on the current time.
	 * 
	 * @param timeSlotWidth the time slot width
	 * @return the start of the next time slot
	 */
	public static DateTime calcNextTimeSlot(int timeSlotWidth) {
		return calcNextTimeSlot(new DateTime(), timeSlotWidth);
	}
	
	/**
	 * Calculates the start of the next time slot based on the given date.
	 * 
	 * @param date the reference date
	 * @param timeSlotWidth the time slot width
	 * @return the start of the next time slot
	 */
	public static DateTime calcNextTimeSlot(DateTime date, int timeSlotWidth) {
		if (timeSlotWidth > 60) {
			// The current algorithm assumes time slots are at most 1-hour long, so their boundaries can always be positioned
			// consistently starting from the beginning of the current hour. If time slots are longer than 1 hour we should
			// probably start counting from midnight instead of the current hour in order to get deterministic placement
			// independently of the time the process controller is started.
			throw new IllegalArgumentException("Time slots longer than 60 minutes are not supported");
		}
		int minutes = date.getMinuteOfHour();
		int minutesToNextSlot = timeSlotWidth - (minutes % timeSlotWidth);
		return date.withMillisOfSecond(0).withSecondOfMinute(0).plusMinutes(minutesToNextSlot);
	}
	
	/**
	 * Returns whether the given date is the start of a time slot.
	 * 
	 * @param date the reference date
	 * @param timeSlotWidth the time slot width
	 * @return whether the date is the start of a time slot.
	 */
	public static boolean isTimeSlotStart(DateTime date, int timeSlotWidth) {
		if (timeSlotWidth > 60) {
			// See comment in calcNextTimeSlot
			throw new IllegalArgumentException("Time slots longer than 60 minutes are not supported");
		}
		return date.getMinuteOfHour() % timeSlotWidth == 0;
	}
	
	/**
	 * Returns the number of the time slot starting at the given date, referring to an interval starting at dateFrom.
	 *  
	 * @param dateFrom the start of the interval
	 * @param timeSlotStart the start of the time slot
	 * @param timeSlotWidth the time slot width in minutes
	 * @return the number of the time slot
	 */
	public static int getTimeSlotNumber(DateTime dateFrom, DateTime timeSlotStart, int timeSlotWidth) {
		long dateFromMillis = dateFrom.getMillis();
		long timeSlotMillis = timeSlotStart.getMillis();
		long widthMillis = timeSlotWidth * 60000;
		long delta = timeSlotMillis - dateFromMillis;
		if (delta < 0) {
			throw new IllegalArgumentException("dateFrom is before timeSlotStart");
		} else if (delta % widthMillis != 0) {
			throw new IllegalArgumentException("dateFrom or timeSlotStart are not aligned to time slot boundaries");
		}
		return (int) (delta / widthMillis);
	}
	
	/**
	 * Same as getTimeSlotNumber(dateFrom, timeSlotStart, timeSlotWidth), where timeSlotWidth is the time slot duration
	 * converted to minutes.
	 */
	public static int getTimeSlotNumber(DateTime dateFrom, DateTime timeSlotStart, Amount<Duration> timeSlotDuration) {
		int durationMinutes = (int) timeSlotDuration.longValue(NonSI.MINUTE);
		return getTimeSlotNumber(dateFrom, timeSlotStart, durationMinutes);
	}
	
	/**
	 * Returns the start date (and time) of the given time slot relative to the given time range.
	 * 
	 * @param dateFrom the start of the time range time slots refer to
	 * @param timeSlotNumber the time slot number, 0-based
	 * @param timeSlotDuration the duration of a time slot
	 * @return the time slot start date
	 */
	public static DateTime getTimeSlotStart(DateTime dateFrom, int timeSlotNumber, Amount<Duration> timeSlotDuration) {
		int durationMinutes = (int) timeSlotDuration.longValue(NonSI.MINUTE);
		return dateFrom.plusMinutes(timeSlotNumber * durationMinutes);
	}
	
}
