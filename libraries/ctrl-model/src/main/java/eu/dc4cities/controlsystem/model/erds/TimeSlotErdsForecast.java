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
import eu.dc4cities.controlsystem.model.TimeSlotPower;
import eu.dc4cities.controlsystem.model.quantity.EnergyPrice;
import eu.dc4cities.controlsystem.model.quantity.GasEmission;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Dimensionless;

/**
 * The energy availability forecast for a single time slot. In this case the
 * power value refers to the maximum power that will be available during the
 * time slot.
 */
public class TimeSlotErdsForecast extends TimeSlotPower {

	private Amount<Dimensionless> renewablePercentage;
	private Amount<GasEmission> co2Factor;
	private double primaryEnergyFactor;
	private Amount<EnergyPrice> consumptionPrice;

	@JsonCreator
	public TimeSlotErdsForecast(@JsonProperty("timeSlot") int timeSlot) {
		super(timeSlot);
	}

	/**
	 * Creates a new TimeSlotErdsForecast as a copy of the given one.
	 * 
	 * @param source the object to copy
	 */
	public TimeSlotErdsForecast(TimeSlotErdsForecast source) {
		super(source);
		renewablePercentage = source.renewablePercentage;
		co2Factor = source.co2Factor;
		primaryEnergyFactor = source.primaryEnergyFactor;
		consumptionPrice = source.consumptionPrice;
	}
	
	/**
	 * Returns the percentage of energy produced from renewable sources w.r.t
	 * the total energy in the time slot.
	 * 
	 * @return the percentage of renewable energy
	 */
	public Amount<Dimensionless> getRenewablePercentage() {
		return renewablePercentage;
	}

	public void setRenewablePercentage(Amount<Dimensionless> renewablePercentage) {
		this.renewablePercentage = renewablePercentage;
	}

	/**
	 * Returns the amount of CO2 emitted to produce energy in the time slot.
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
	 * Returns the primary energy factor for energy produced in the time slot.
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
	 * Returns the price paid for consuming energy in the time slot.
	 * 
	 * @return the consumption price in EUR/kWh
	 */
	public Amount<EnergyPrice> getConsumptionPrice() {
		return consumptionPrice;
	}

	public void setConsumptionPrice(Amount<EnergyPrice> consumptionPrice) {
		this.consumptionPrice = consumptionPrice;
	}

}
