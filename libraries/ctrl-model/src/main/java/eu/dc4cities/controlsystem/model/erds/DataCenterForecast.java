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
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * The energy forecast for all ERDSes of a data center.
 */
public class DataCenterForecast extends TimeSlotBasedEntity {

    private String dataCenterName;
    private List<ErdsForecast> erdsForecasts;

    @JsonCreator
    public DataCenterForecast(@JsonProperty("dataCenterName") String dataCenterName) {
        this.dataCenterName = dataCenterName;
    }
    
    /**
     * Returns the name of the data center the forecast refers to.
     *
     * @return the name of the data center
     */
    public String getDataCenterName() {
        return dataCenterName;
    }

    /**
     * Returns the energy forecast for every ERDS connected to the data center.
     *
     * @return the ERDS energy forecasts
     */
    public List<ErdsForecast> getErdsForecasts() {
        return erdsForecasts;
    }

    public void setErdsForecasts(List<ErdsForecast> erdsForecasts) {
        this.erdsForecasts = erdsForecasts;
    }

    /**
	 * Returns a copy of a range of all forecasts, from the given start date to the given (exclusive) end date.
	 * 
	 * @param rangeFrom the start date of the range (must be aligned to a time slot)
	 * @param rangeTo the end date of the range (exclusive; must be aligned to a time slot)
	 * @return the copy of forecasts for the given range
	 */
	public DataCenterForecast copyOfRange(DateTime rangeFrom, DateTime rangeTo) {
		// Delegate range consistency checks to ErdsForecast.copyOfRange()
		DataCenterForecast copy = new DataCenterForecast(dataCenterName);
		copy.copyIntervalFrom(this);
		if (copy.dateFrom != null) {
			copy.dateFrom = rangeFrom;
		}
		if (copy.dateTo != null) {
			copy.dateTo = rangeTo;
		}
		copy.erdsForecasts = new ArrayList<>(erdsForecasts.size());
		for (ErdsForecast erdsForecast : erdsForecasts) {
			copy.erdsForecasts.add(erdsForecast.copyOfRange(rangeFrom, rangeTo));
		}
		return copy;
	}
    
	/**
	 * Returns a copy of a forecast range from the given list of data centers.
	 * 
	 * @param rangeFrom the start date of the range (must be aligned to a time slot)
	 * @param rangeTo the end date of the range (exclusive; must be aligned to a time slot)
	 * @return a copy of the list with forecasts trimmed to the given range
	 */
	public static List<DataCenterForecast> copyOfRange(List<DataCenterForecast> dataCenterForecasts, DateTime rangeFrom,
			DateTime rangeTo) {
		List<DataCenterForecast> copy = new ArrayList<>(dataCenterForecasts.size());
		for (DataCenterForecast forecast : dataCenterForecasts) {
			copy.add(forecast.copyOfRange(rangeFrom, rangeTo));
		}
		return copy;
	}
	
}
