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

package eu.dc4cities.controlsystem.modules.processcontroller;

import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.erds.DataCenterForecast;
import eu.dc4cities.controlsystem.modules.ErdsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Executes the power planning loop.
 */
public class PowerLoop {

	private static final Logger logger = LoggerFactory.getLogger(PowerLoop.class);
	
	private ErdsHandler erdsHandler;

    public PowerLoop(ErdsHandler erdsHandler) {
        this.erdsHandler = erdsHandler;
    }
    
    /**
     * Executes the power planning loop retrieving energy forecast data for all data centers.
     * 
     * @param timeRange the time range to get the forecast for
     * @return the energy forecast for all data centers
     */
    public List<DataCenterForecast> execute(TimeSlotBasedEntity timeRange) {
    	logger.debug("Starting power planning loop for time range: " + timeRange);
    	logger.debug("Requesting energy forecasts...");
		List<DataCenterForecast> forecasts = erdsHandler.getEnergyForecasts(timeRange);
        logger.debug("Got energy forecasts: " + forecasts);
        return forecasts;
    }
	
}
