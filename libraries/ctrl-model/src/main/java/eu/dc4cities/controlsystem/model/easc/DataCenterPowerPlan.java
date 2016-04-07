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

package eu.dc4cities.controlsystem.model.easc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.dc4cities.controlsystem.model.PowerPlan;

import java.util.List;

/**
 * Defines the power quotas allocated to a given data center. Properties that define the time range (dateFrom, dateTo,
 * timeSlotDuration) can be omitted when the {@code DataCenterPowerPlan} is part of a parent {@code EascPowerPlan} in
 * which the time range is defined (in this case all child data centers inherit the same settings).
 */
@JsonPropertyOrder({"dataCenterName", "energyQuotas", "powerQuotas"})
public class DataCenterPowerPlan extends PowerPlan {
	
	private String dataCenterName;
    private List<EnergyQuota> energyQuotas;
    
    /**
     * Creates a new instance referring to the given data center.
     * 
     * @param dataCenterName the data center name
     */
    @JsonCreator
    public DataCenterPowerPlan(@JsonProperty("dataCenterName") String dataCenterName) {
    	this.dataCenterName = dataCenterName;
    }
    
    /**
     * Returns the name of the data center the quotas refer to.
     * 
     * @return the data center name
     */
    public String getDataCenterName() {
		return dataCenterName;
	}

    /**
     * Returns the quotas of total energy the data center can use over the time frame of the power plan. Each quota
     * applies for a time-slot-based range. The list of quotas must cover the whole time range of the power plan.<br>
     * The energy quota is a constraint in addition to power quotas. The data center should not exceed the instant
     * power quota in any time slot and also not exceed the total energy quota consumed over all time slots in the
     * time range of each quota.
     * <p>
     * This method can return {@code null} if the data center has no special constraint on energy other than the power
     * quotas.
     * 
     * @return the energy quotas of the data center or {@code null}
     */
	public List<EnergyQuota> getEnergyQuotas() {
		return energyQuotas;
	}

	public void setEnergyQuotas(List<EnergyQuota> energyQuotas) {
		this.energyQuotas = energyQuotas;
	}
    
}
