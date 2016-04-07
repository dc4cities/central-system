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

package eu.dc4cities.controlsystem.model.datacenter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.dc4cities.controlsystem.model.PowerPlan;
import eu.dc4cities.controlsystem.model.json.JsonUtils;

/**
 * The optimized execution plan for a given data center.
 */
public class DataCenterExecutionPlan {

	private String dataCenterName;
	private PowerPlan idealPowerPlan;
	private PowerPlan consolidatedPowerPlan;
	
	/**
	 * Creates a new empty data center plan.
	 */
	@JsonCreator
	public DataCenterExecutionPlan(@JsonProperty("dataCenterName") String dataCenterName) {
		this.dataCenterName = dataCenterName;
	}

	/**
	 * Creates a new execution plan as a copy of the given one.
	 * 
	 * @param source the plan to copy
	 */
	public DataCenterExecutionPlan(DataCenterExecutionPlan source) {
		dataCenterName = source.dataCenterName;
		idealPowerPlan = new PowerPlan(source.idealPowerPlan);
		consolidatedPowerPlan = new PowerPlan(source.consolidatedPowerPlan);
	}
	
	/**
	 * Returns the name of the data center the execution plan refers to.
	 * 
	 * @return the name of the data center
	 */
	public String getDataCenterName() {
		return dataCenterName;
	}

	/**
	 * Returns the ideal power plan calculated based on the energy forecast.
	 * 
	 * @return the ideal power plan for the data center, including PUE
	 */
	public PowerPlan getIdealPowerPlan() {
		return idealPowerPlan;
	}
	
	public void setIdealPowerPlan(PowerPlan idealPowerPlan) {
		this.idealPowerPlan = idealPowerPlan;
	}

	/**
	 * Returns the consolidated power plan obtained by summing up the power consumption for all works in the selected activity
	 * plans.
	 * 
	 * @return the consolidated power plan for the data center, including PUE
	 */
	public PowerPlan getConsolidatedPowerPlan() {
		return consolidatedPowerPlan;
	}
	
	public void setConsolidatedPowerPlan(PowerPlan consolidatedPowerPlan) {
		this.consolidatedPowerPlan = consolidatedPowerPlan;
	}
	
	/**
	 * Appends the given plan at the end of this plan.
	 * 
	 * @param otherPlan the plan to append
	 */
	public void append(DataCenterExecutionPlan otherPlan) {
		if (!dataCenterName.equals(otherPlan.getDataCenterName())) {
			throw new IllegalArgumentException("Data center names don't match");
		}
		idealPowerPlan.append(otherPlan.idealPowerPlan);
		consolidatedPowerPlan.append(otherPlan.consolidatedPowerPlan);
	}
	
	@Override
	public String toString() {
		return JsonUtils.toString(this);
	}
	
}
