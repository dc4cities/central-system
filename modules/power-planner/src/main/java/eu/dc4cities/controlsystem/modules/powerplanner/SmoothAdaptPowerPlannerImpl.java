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

package eu.dc4cities.controlsystem.modules.powerplanner;

import eu.dc4cities.configuration.goal.Objective;
import eu.dc4cities.configuration.technical.ProcessConfig;
import eu.dc4cities.controlsystem.model.PowerPlan;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.TimeSlotPower;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.modules.PowerPlanner;
import eu.dc4cities.controlsystem.modules.ProcessConfigAware;
import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static javax.measure.unit.NonSI.PERCENT;
import static javax.measure.unit.SI.*;

/**
 * Implementation of a power planner for a data centre. Given the forecasts of
 * renewable energy availability of all power sources of the data centre and the
 * current power usage plan of the data centre, calculates a power plan for the
 * configured time intervals that satisfies all power, energy and energy
 * property objectives, if possible.
 *
 *
 */
/**
 * Remark by Torben Möller NoOPCPRenPctOptimizingPowerPlannerImpl is NOT using
 * any contraint. It maximizes the share of renewable energies.
 */

@Component
public class SmoothAdaptPowerPlannerImpl implements PowerPlanner,
		PowerConfigAware, ProcessConfigAware {

	private int DCMaxPower;

	private int DCMinPower;

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory
			.getLogger(SmoothAdaptPowerPlannerImpl.class);

	private List<Objective> energyConfiguration = new ArrayList<>();

	public SmoothAdaptPowerPlannerImpl() {

	}

	@Override
	public PowerPlan calculateIdealPowerPlan(TimeSlotBasedEntity timeRange,
			List<ErdsForecast> erdsForecasts, PowerPlan currentPowerPlan) {
		List<Objective> powerObjectivesList = new ArrayList<>();
		List<Objective> energyObjectivesList = new ArrayList<>();
		List<Objective> energyPropertyObjectivesList = new ArrayList<>();

		for (Objective currentObjective : energyConfiguration) {
			if (currentObjective.getType().equals("POWER")) {
				powerObjectivesList.add(currentObjective);
			} else if (currentObjective.getType().equals("ENERGY")) {
				energyObjectivesList.add(currentObjective);
			} else if (currentObjective.getType().equals("ENERGY_PROPERTY")) {
				energyPropertyObjectivesList.add(currentObjective);
			}
		}

		// Check if objectives have been set by process controller
		try {
			if (timeRange == null) {
				throw new ConfigurationNotSetException(
						"No time configuration set!");
			}
			if (erdsForecasts == null) {
				throw new ConfigurationNotSetException(
						"No forecasts available!");
			}
		} catch (ConfigurationNotSetException e) {
			e.printStackTrace();
		}

		int numberOfSlots = (int) ((timeRange.getDateTo().getMillis() - timeRange
				.getDateFrom().getMillis()) / timeRange.getTimeSlotDuration()
				.longValue(MILLI(SECOND)));

		// If no current power plan available, assume maximum power usage on all
		// slots
		if (currentPowerPlan == null
				|| currentPowerPlan.getPowerQuotas() == null) {
			currentPowerPlan = new PowerPlan();
			currentPowerPlan.setDateFrom(timeRange.getDateFrom());
			currentPowerPlan.setDateTo(timeRange.getDateTo());
			currentPowerPlan.setTimeSlotDuration(timeRange
					.getTimeSlotDuration());
			List<TimeSlotPower> quotas = new ArrayList<>();
			for (int i = 1; i <= numberOfSlots; i++) {
				TimeSlotPower timeSlotPower = new TimeSlotPower(i);
				Amount<Power> maxSlotPower = Amount.valueOf(0, WATT);
				for (ErdsForecast currentErdsForecast : erdsForecasts) {
					maxSlotPower = maxSlotPower.plus(currentErdsForecast
							.getTimeSlotForecasts().get(i - 1).getPower());
				}
				timeSlotPower.setPower(maxSlotPower);
				quotas.add(timeSlotPower);
			}
			currentPowerPlan.setPowerQuotas(quotas);
			return currentPowerPlan;
		}
		Amount<Energy> currentPowerPlanTotalEnergy = Amount.valueOf(0, JOULE);
		for (int i = 0; i < currentPowerPlan.getPowerQuotas().size(); i++) {
			TimeSlotPower currentTimeSlotPower = currentPowerPlan
					.getPowerQuotas().get(i);
			currentPowerPlanTotalEnergy = currentPowerPlanTotalEnergy
					.plus(currentTimeSlotPower.getPower().times(
							currentPowerPlan.getTimeSlotDuration()));

		}
		PowerPlan newPowerPlan = new PowerPlan();
		newPowerPlan.setDateFrom(timeRange.getDateFrom());
		newPowerPlan.setDateTo(timeRange.getDateTo());
		newPowerPlan.setTimeSlotDuration(timeRange.getTimeSlotDuration());
		List<TimeSlotPower> quotas = new ArrayList<>();
		for (int i = 1; i <= numberOfSlots; i++) {
			TimeSlotPower timeSlotPower = new TimeSlotPower(i);
			Amount<Power> slotPower = Amount.valueOf(DCMinPower, WATT);
			timeSlotPower.setPower(slotPower);
			quotas.add(timeSlotPower);
		}
		newPowerPlan.setPowerQuotas(quotas);

		PowerPlan renPctOptimizedPowerPlan = new PowerPlan();

		renPctOptimizedPowerPlan = smoothAdapt(newPowerPlan, erdsForecasts,
				currentPowerPlanTotalEnergy);

		return renPctOptimizedPowerPlan;
	}

	private PowerPlan smoothAdapt(PowerPlan powerPlan,
			List<ErdsForecast> erdsForecasts,
			Amount<Energy> currentPowerPlanTotalEnergy) {

		List<TimeSlotPower> powerQuotas = powerPlan.getPowerQuotas();
		TreeMap<Integer, Amount<Power>> currentErdsTreeMap = new TreeMap<Integer, Amount<Power>>();

		int numberOfSlots = powerQuotas.size();
		Amount<Power> maxPower = Amount.valueOf(0, WATT);
		
		int i = 1;

		while (i <= numberOfSlots) {
			Amount<Power> timeSlotPower = Amount.valueOf(0, WATT);
			Amount<Power> timeSlotRenPower = Amount.valueOf(0, WATT);
			for (ErdsForecast currentErdsForecast : erdsForecasts) {
				long currentRenewablePercentage = currentErdsForecast
						.getTimeSlotForecasts().get(i - 1)
						.getRenewablePercentage().longValue(PERCENT);
				Amount<Power> currentPower = currentErdsForecast
						.getTimeSlotForecasts().get(i - 1).getPower();
				Amount<Power> currentRenPower = currentPower
						.times(currentRenewablePercentage * 0.01);
				timeSlotPower = timeSlotPower.plus(currentPower);
				timeSlotRenPower = timeSlotRenPower.plus(currentRenPower);
			}
			if (timeSlotPower.isGreaterThan(maxPower)) {
				maxPower = timeSlotPower;
			}
			currentErdsTreeMap.put(i, timeSlotRenPower);
			i++;
		}

		//System.out.println(currentErdsTreeMap);

		i = 1;

		Amount<Power> minRenPower = Amount.valueOf(Long.MAX_VALUE, WATT);
		Amount<Power> maxRenPower = Amount.valueOf(0, WATT);
		while (i <= numberOfSlots) {
			Amount<Power> timeSlotRenPower = currentErdsTreeMap.get(i);
			if (timeSlotRenPower.isGreaterThan(maxRenPower)) {
				maxRenPower = Amount.valueOf(timeSlotRenPower.longValue(WATT),
						WATT);
			}
			if (timeSlotRenPower.isLessThan(minRenPower)) {
				minRenPower = Amount.valueOf(timeSlotRenPower.longValue(WATT),
						WATT);
			}
			i++;
		}

		long powerInputDiff = maxRenPower.longValue(WATT)
				- minRenPower.longValue(WATT);
		if (DCMaxPower > maxPower.longValue(WATT)) {
			DCMaxPower = (int) maxPower.longValue(WATT);
		}
		long dcPowerDiff = DCMaxPower - DCMinPower;
		double fittingFactor = ((double) powerInputDiff)
				/ ((double) dcPowerDiff);

		//System.out.println(fittingFactor);
		
		TreeMap<Integer, Amount<Power>> powerDiff = new TreeMap<Integer, Amount<Power>>();
		
		i = 1;

		while (i <= numberOfSlots) {
			Amount<Power> currentRenPower = currentErdsTreeMap.get(i);
			Amount<Power> timeSlotPowerDiff = currentRenPower.minus(minRenPower);		
			powerDiff.put(i, timeSlotPowerDiff);
			i++;
		}

		//System.out.println(powerDiff);
		
		Amount<Power> dCMinPowerValue = Amount.valueOf(DCMinPower, WATT);
		
		i = 0;
		List<TimeSlotPower> newPowerQuotas = new ArrayList<>();
		
		while (i < numberOfSlots) {
			Amount<Power> powerValue = powerDiff.get(i+1);
			powerValue = powerValue.divide(fittingFactor).plus(dCMinPowerValue);
			TimeSlotPower currentTimeSlotPower = new TimeSlotPower(i+1);
			currentTimeSlotPower.setPower(Amount.valueOf((long) powerValue.getMaximumValue(), WATT));
			newPowerQuotas.add(currentTimeSlotPower);
			i++;
		}
		
		powerPlan.setPowerQuotas(newPowerQuotas);
		
		return powerPlan;
	}

	@Override
	public void setPowerConfig(List<Objective> energyConfiguration) {
		this.energyConfiguration = energyConfiguration;
	}

	@Override
	public List<Objective> getPowerConfig() {
		return energyConfiguration;
	}

	public List<Objective> getEnergyConfiguration() {
		return energyConfiguration;
	}

	public void setEnergyConfiguration(List<Objective> energyConfiguration) {
		this.energyConfiguration = energyConfiguration;
	}

	@Override
	public void setProcessConfig(ProcessConfig processConfig) {
		DCMinPower = processConfig.getMinPower();
		DCMaxPower = processConfig.getMaxSuboptimalPower();
	}

	public int getDCMaxPower() {
		return DCMaxPower;
	}

	public void setDCMaxPower(int dCMaxPower) {
		DCMaxPower = dCMaxPower;
	}

	public int getDCMinPower() {
		return DCMinPower;
	}

	public void setDCMinPower(int dCMinPower) {
		DCMinPower = dCMinPower;
	}

}
