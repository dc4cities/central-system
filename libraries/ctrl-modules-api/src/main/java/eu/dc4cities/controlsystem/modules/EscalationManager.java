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

import eu.dc4cities.configuration.goal.Objective;
import eu.dc4cities.controlsystem.model.TimeParameters;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterPower;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterStatus;
import eu.dc4cities.controlsystem.model.easc.EascActivityPlan;
import eu.dc4cities.controlsystem.model.easc.EascActivitySpecifications;
import eu.dc4cities.controlsystem.model.easc.EascServiceLevels;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;

import java.util.List;

/**
 * Analyzes activity plans to produce a data center status and send notifications (e.g. via e-mail) in case of alarms.
 * When working in a federation, each instance of this component works on a single data center.
 */
public interface EscalationManager {

	/**
	 * Determines the status for the given data center based on the given objectives and activity plan.
	 * The EASC activity specifications and EASC activity plans may contain more data centers than the one for which
	 * status must be evaluated, so the escalation manager must consider only the activities in which the data center
	 * is involved.<br/>
	 * The time interval to analyze (defined by timeParameters) always spans full days, from midnight to midnight. The
	 * interval may cover 24 hours (when dateNow is midnight) or 48 hours (when dateNow is after midnight).
	 * 
	 * @param dataCenterName the name of the data center
	 * @param timeParameters the time interval to consider
	 * @param powerObjectives the power objectives assigned by the smart city to the data center
	 * @param erdsForecasts the power availability forecasts for each ERDS connected to the data center
	 * @param eascActivitySpecifications the activity specifications
	 * @param dataCenterPowerActual the actual power consumption for the data center from the beginning of the day up
	 *        to the current time
	 * @param eascServiceLevels the actual service levels achieved for all EASCs in the federation, from the beginning
	 *        of the day up to the current time
	 * @param eascActivityPlans the activity plans prepared by the optimizer, including the expected service levels
	 * @return the data center status
	 */
	DataCenterStatus determineDcStatus(String dataCenterName, TimeParameters timeParameters, 
			List<Objective> powerObjectives, List<ErdsForecast> erdsForecasts,
			List<EascActivitySpecifications> eascActivitySpecifications,
			DataCenterPower dataCenterPowerActual, List<EascServiceLevels> eascServiceLevels,
			List<EascActivityPlan> eascActivityPlans);
	
}
