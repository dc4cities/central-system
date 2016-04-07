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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import eu.dc4cities.controlsystem.model.PowerPlan;
import eu.dc4cities.controlsystem.model.TimeSlotPower;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Power;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Defines the power quotas allocated for each time slot on a given EASC. Quotas are split between data centers in which
 * the EASC can execute activities in order to support federation.
 */
@JsonPropertyOrder({"eascName"})
public class EascPowerPlan extends PowerPlan {
	
	private String eascName;
	private List<DataCenterPowerPlan> dataCenterQuotas = new LinkedList<>();

    /**
     * Creates a new instance without specifying the EASC name. This is allowed only when the power quotas refer to a
     * known EASC.
     */
    public EascPowerPlan() {}
    
    /**
     * Creates a new instance referring to the given EASC.
     * 
     * @param eascName the EASC name
     */
    public EascPowerPlan(String eascName) {
    	this.eascName = eascName;
    }
    
    /**
     * Returns the name of the EASC the quotas refer to.
     * 
     * @return the EASC name
     */
    public String getEascName() {
		return eascName;
	}

	public void setEascName(String eascName) {
		this.eascName = eascName;
	}

	/**
	 * Returns the power quotas assigned to every data center in which the EASC can execute activities.
	 * 
	 * @return the per data center power quotas
	 */
	@JsonInclude(Include.NON_EMPTY)
	public List<DataCenterPowerPlan> getDataCenterQuotas() {
		return dataCenterQuotas;
	}

	public void setDataCenterQuotas(List<DataCenterPowerPlan> dataCenterQuotas) {
		this.dataCenterQuotas = dataCenterQuotas;
	}

	public void addDataCenterQuotas(List<DataCenterPowerPlan> dataCenterQuotas) {
		this.dataCenterQuotas.addAll(dataCenterQuotas);
	}
	
	/**
	 * @deprecated use {@link #getDataCenterQuotas()} to get data center specific quotas that support federation
	 */
	@Deprecated
	@Override
	public List<TimeSlotPower> getPowerQuotas() {
		return super.getPowerQuotas();
	}

	@Deprecated
	@Override
	public Map<Integer, TimeSlotPower> getPowerQuotasMap() {
		return super.getPowerQuotasMap();
	}

	/**
	 * @deprecated use {@link #setDataCenterQuotas(List)} to assign data center specific quotas and support federation
	 */
	@Deprecated
	@Override
	public void setPowerQuotas(List<TimeSlotPower> powerQuotas) {
		super.setPowerQuotas(powerQuotas);
	}

	@Deprecated
	@Override
	public void fillPowerAmounts(Amount<Power> power) {
		super.fillPowerAmounts(power);
	}
    
}
