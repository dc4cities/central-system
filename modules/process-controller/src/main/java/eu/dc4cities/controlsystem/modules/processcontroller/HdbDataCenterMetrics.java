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

import eu.dc4cities.controlsystem.model.quantity.EnergyPrice;
import eu.dc4cities.controlsystem.model.quantity.GasEmission;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Power;
import java.util.LinkedList;
import java.util.List;

/**
 * Holds metrics related to the total activity in a given data center.
 */
public class HdbDataCenterMetrics {
	
	private String dataCenterName;
	private Amount<Power> powerConsumption;
	private Amount<Dimensionless> gridRenewablePercentage;
	private Amount<Power> renewablePower;
	private Amount<GasEmission> co2Factor;
	private double primaryEnergyFactor;
	private Amount<EnergyPrice> consumptionPrice;
	private List<HdbEascMetrics> eascs = new LinkedList<>();
	
	public HdbDataCenterMetrics(String dataCenterName) {
		this.dataCenterName = dataCenterName;
	}

	/**
	 * Returns the name of the data center the metrics refer to.
	 * 
	 * @return the name of the data center
	 */
	public String getDataCenterName() {
		return dataCenterName;
	}
	
	/**
	 * Returns the power consumption for the data center.
	 * 
	 * @return the power consumption for the data center
	 */
	public Amount<Power> getPowerConsumption() {
		return powerConsumption;
	}

	public void setPowerConsumption(Amount<Power> powerConsumption) {
		this.powerConsumption = powerConsumption;
	}

	/**
	 * Returns the percentage of renewable power available from the grid.
	 * 
	 * @return the percentage of renewable power available from the grid
	 */
	public Amount<Dimensionless> getGridRenewablePercentage() {
		return gridRenewablePercentage;
	}

	public void setGridRenewablePercentage(Amount<Dimensionless> gridRenewablePercentage) {
		this.gridRenewablePercentage = gridRenewablePercentage;
	}

	/**
	 * Returns the total power available from pure renewable sources (e.g. photovoltaic panels).
	 * 
	 * @return the total power available from pure renewable sources (e.g. photovoltaic panels).
	 */
	public Amount<Power> getRenewablePower() {
		return renewablePower;
	}

	public void setRenewablePower(Amount<Power> renewablePower) {
		this.renewablePower = renewablePower;
	}

	/**
	 * Returns the amount of CO2 emitted for producing the energy consumed by the data center.
	 * 
	 * @return the CO2 emissions in kg/kWh
	 */
	public Amount<GasEmission> getCo2Factor() {
		return co2Factor;
	}

	public void setCo2Factor(Amount<GasEmission> co2Factor) {
		this.co2Factor = co2Factor;
	}

	/**
	 * Returns the primary energy factor for energy consumed by the data center.
	 * 
	 * @return the PE factor
	 */
	public double getPrimaryEnergyFactor() {
		return primaryEnergyFactor;
	}

	public void setPrimaryEnergyFactor(double primaryEnergyFactor) {
		this.primaryEnergyFactor = primaryEnergyFactor;
	}

	/**
	 * Returns the price paid for energy by the data center.
	 * 
	 * @return the consumption price in EUR/kWh
	 */
	public Amount<EnergyPrice> getConsumptionPrice() {
		return consumptionPrice;
	}

	public void setConsumptionPrice(Amount<EnergyPrice> consumptionPrice) {
		this.consumptionPrice = consumptionPrice;
	}

	/**
	 * Returns the metrics for all EASCs running in the data center.
	 * 
	 * @return the EASC metrics
	 */
	public List<HdbEascMetrics> getEascs() {
		return eascs;
	}

	/**
	 * Returns the metrics for the given EASC, if any.
	 * 
	 * @param eascName the name of the EASC
	 * @return the EASC metrics or {@code null} if not found
	 */
	public HdbEascMetrics getEasc(String eascName) {
		for (HdbEascMetrics easc : eascs) {
			if (easc.getEascName().equals(eascName)) {
				return easc;
			}
		}
		return null;
	}
	
	/**
	 * Adds the given EASC metrics to this data center.
	 * 
	 * @param easc the EASC metrics to add
	 */
	public void addEasc(HdbEascMetrics easc) {
		eascs.add(easc);
	}
	
	public void setEascs(List<HdbEascMetrics> eascs) {
		this.eascs = eascs;
	}
	
}
