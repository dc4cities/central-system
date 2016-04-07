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

package eu.dc4cities.controlsystem.modules;

import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.erds.DataCenterForecast;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;

import java.util.List;

/**
 * Handles communication with ERDS services via the Northbound API. When working in a federation, the component requests
 * the forecast for all data centers, each having its own set of ERDS services.
 */
public interface ErdsHandler {
	
	/**
	 * Requests energy forecasts for the given time range to all configured ERDS services.
	 * 
	 * @param timeRange the time range for which the forecast is requested
	 * @return the list of energy forecasts for each configured ERDS
	 * @deprecated use {@link #getEnergyForecasts(TimeSlotBasedEntity)} to support federation
	 */
	@Deprecated
    List<ErdsForecast> collectEnergyForecasts(TimeSlotBasedEntity timeRange);
	
	/**
	 * Requests energy forecasts for the given time range for all data centers.
	 * 
	 * @param timeRange the time range for which the forecast is requested
	 * @return the list of energy forecasts
	 */
    List<DataCenterForecast> getEnergyForecasts(TimeSlotBasedEntity timeRange);
    
}
