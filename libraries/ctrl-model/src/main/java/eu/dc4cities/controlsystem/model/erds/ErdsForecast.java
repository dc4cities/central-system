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

package eu.dc4cities.controlsystem.model.erds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.util.TimeRangeUtils;
import org.joda.time.DateTime;

import javax.measure.unit.NonSI;
import java.util.ArrayList;
import java.util.List;

/**
 * The energy availability forecast for an ERDS in the given time frame. The time frame is divided into time slots of
 * the given duration and a separate forecast is provided for each time slot.
 */
public class ErdsForecast extends TimeSlotBasedEntity {

    private String erdsName;
    private List<TimeSlotErdsForecast> timeSlotForecasts;

    @JsonCreator
    public ErdsForecast(@JsonProperty("erdsName") String erdsName) {
        this.erdsName = erdsName;
    }
    
    /**
     * Returns the name of the ERDS the forecast refers to.
     *
     * @return the name of the ERDS
     */
    public String getErdsName() {
        return erdsName;
    }

    /**
     * Returns the energy forecasts for every time slot in the specified time range.
     *
     * @return the time slot energy forecasts
     */
    public List<TimeSlotErdsForecast> getTimeSlotForecasts() {
        return timeSlotForecasts;
    }

    @JsonDeserialize(contentAs=TimeSlotErdsForecast.class)
    public void setTimeSlotForecasts(List<TimeSlotErdsForecast> timeSlotForecasts) {
        this.timeSlotForecasts = timeSlotForecasts;
    }

    /**
	 * Returns a copy of a range of this forecast, from the given start date to the given (exclusive) end date.
	 * 
	 * @param rangeFrom the start date of the range (must be aligned to a time slot)
	 * @param rangeTo the end date of the range (exclusive; must be aligned to a time slot)
	 * @return the copy of this forecast for the given range
	 */
	public ErdsForecast copyOfRange(DateTime rangeFrom, DateTime rangeTo) {
		if (rangeTo.isBefore(rangeFrom)) {
			throw new IllegalArgumentException("rangeTo is before rangeFrom");
		} else if (rangeFrom.isBefore(dateFrom)) {
			throw new IllegalArgumentException("rangeFrom is before the start of this forecast");
		} else if (rangeTo.isAfter(dateTo)) {
			throw new IllegalArgumentException("rangeTo is after the end of this forecast");
		}
		int durationMinutes = (int) timeSlotDuration.longValue(NonSI.MINUTE);
		int startTimeSlot = TimeRangeUtils.getTimeSlotNumber(dateFrom, rangeFrom, durationMinutes);
		int endTimeSlot = TimeRangeUtils.getTimeSlotNumber(dateFrom, rangeTo, durationMinutes);
		ErdsForecast copy = new ErdsForecast(erdsName);
		copy.copyIntervalFrom(this);
		copy.dateFrom = rangeFrom;
		copy.dateTo = rangeTo;
		copy.timeSlotForecasts = new ArrayList<>(endTimeSlot - startTimeSlot);
		for (int i = startTimeSlot; i < endTimeSlot; i++) {
			TimeSlotErdsForecast timeSlotCopy = new TimeSlotErdsForecast(timeSlotForecasts.get(i));
			timeSlotCopy.setTimeSlot(i - startTimeSlot);
			copy.timeSlotForecasts.add(timeSlotCopy);
		}
		return copy;
	}
    
}
