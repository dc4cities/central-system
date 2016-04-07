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

import eu.dc4cities.controlsystem.model.PowerPlan;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;

import java.util.List;

/**
 * Calculates the ideal power budget for the data center. When working in a federation, each instance of this
 * component works on a single data center.
 */
public interface PowerPlanner {

	/**
	 * Calculates the ideal power plan for the whole data center for each time slot in the given time range.
	 * Currently the time range and time slots for which the plan is requested are the same used in the ERDS
	 * forecasts; all forecasts use the same time range and time slots.
	 * The calculation is based on the ERDS forecasts and the current power plan (i.e. the plan calculated in the
	 * previous iteration).
	 * 
	 * @param timeRange the time range for which the plan is requested
	 * @param erdsForecasts the latest energy forecasts for the time range
	 * @param currentPowerPlan the current power plan; {@code null} if this is the first iteration 
	 * @return the updated ideal power plan
	 */
	PowerPlan calculateIdealPowerPlan(TimeSlotBasedEntity timeRange, List<ErdsForecast> erdsForecasts, 
			PowerPlan currentPowerPlan);
	
}
