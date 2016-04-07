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
import eu.dc4cities.configuration.goal.interval.TimeIntervalExpressionException;
import eu.dc4cities.configuration.goal.utils.ObjectiveUtils;
import eu.dc4cities.controlsystem.model.PowerPlan;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.TimeSlotPower;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.modules.PowerPlanner;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import java.util.*;

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
 * Remark by Torben Möller
 * NoOPCPPowerPlannerImpl is NOT using the constraint solver engine.
 */
@Component
public class NoOPCPPowerPlannerImpl implements PowerPlanner, PowerConfigAware {

	private final static int MAX_ITERATIONS = 200;

	private static final Logger logger = LoggerFactory.getLogger(NoOPCPPowerPlannerImpl.class);
	
	private List<Objective> energyConfiguration = new ArrayList<>();

	public NoOPCPPowerPlannerImpl() {

	}

	@Override
	public PowerPlan calculateIdealPowerPlan(TimeSlotBasedEntity timeRange, List<ErdsForecast> erdsForecasts, PowerPlan currentPowerPlan) {
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
				throw new ConfigurationNotSetException("No time configuration set!");
			}
			if (erdsForecasts == null) {
				throw new ConfigurationNotSetException("No forecasts available!");
			}
		} catch (ConfigurationNotSetException e) {
			e.printStackTrace();
		}

		int numberOfSlots = (int) ((timeRange.getDateTo().getMillis() - timeRange.getDateFrom().getMillis()) / timeRange.getTimeSlotDuration().longValue(
				MILLI(SECOND)));

		// If no current power plan available, assume maximum power usage on all
		// slots
		if (currentPowerPlan == null || currentPowerPlan.getPowerQuotas() == null) {
			currentPowerPlan = new PowerPlan();
			currentPowerPlan.setDateFrom(timeRange.getDateFrom());
			currentPowerPlan.setDateTo(timeRange.getDateTo());
			currentPowerPlan.setTimeSlotDuration(timeRange.getTimeSlotDuration());
			List<TimeSlotPower> quotas = new ArrayList<>();
			for (int i = 1; i <= numberOfSlots; i++) {
				TimeSlotPower timeSlotPower = new TimeSlotPower(i);
				Amount<Power> maxSlotPower = Amount.valueOf(0, WATT);
				for (ErdsForecast currentErdsForecast : erdsForecasts) {
					maxSlotPower = maxSlotPower.plus(currentErdsForecast.getTimeSlotForecasts().get(i - 1).getPower());
				}
				timeSlotPower.setPower(maxSlotPower);
				quotas.add(timeSlotPower);
			}
			currentPowerPlan.setPowerQuotas(quotas);
		} else {
			PowerPlan newPowerPlan = new PowerPlan();
			newPowerPlan.setDateFrom(timeRange.getDateFrom());
			newPowerPlan.setDateTo(timeRange.getDateTo());
			newPowerPlan.setTimeSlotDuration(timeRange.getTimeSlotDuration());
			DateTime currentTo = currentPowerPlan.getDateTo();
			if (currentTo.isAfter(timeRange.getDateFrom())) {
				int i = 0;
				while (currentPowerPlan.getDateFrom().isBefore(timeRange.getDateFrom())) {
					i++;
					if (!currentPowerPlan.getPowerQuotas().isEmpty()) {
						currentPowerPlan.getPowerQuotas().remove(0);
					}
					DateTime newCurrentFrom = currentPowerPlan.getDateFrom().plusMillis((int) currentPowerPlan.getTimeSlotDuration().longValue(MILLI(SECOND)));
					currentPowerPlan.setDateFrom(newCurrentFrom);
				}
				List<TimeSlotPower> newPowerQuotas = new ArrayList<>();
				while (i <= currentPowerPlan.getPowerQuotas().size()) {
					TimeSlotPower timeSlotPower = new TimeSlotPower(i);
					timeSlotPower.setPower(currentPowerPlan.getPowerQuotas().get(i - 1).getPower());
					newPowerQuotas.add(timeSlotPower);
					i++;
				}
				while (i <= numberOfSlots) {
					TimeSlotPower timeSlotPower = new TimeSlotPower(i);
					Amount<Power> maxSlotPower = Amount.valueOf(0, WATT);
					for (ErdsForecast currentErdsForecast : erdsForecasts) {
						maxSlotPower = maxSlotPower.plus(currentErdsForecast.getTimeSlotForecasts().get(i - 1).getPower());
					}
					timeSlotPower.setPower(maxSlotPower);
					newPowerQuotas.add(timeSlotPower);
					i++;
				}
				i = 1;
				while (i <= numberOfSlots) {
					Amount<Power> slotPower = newPowerQuotas.get(i - 1).getPower();
					Amount<Power> maxSlotPower = Amount.valueOf(0, WATT);
					for (ErdsForecast currentErdsForecast : erdsForecasts) {
						maxSlotPower = maxSlotPower.plus(currentErdsForecast.getTimeSlotForecasts().get(i - 1).getPower());
					}
					if (slotPower.isGreaterThan(maxSlotPower)) {
						newPowerQuotas.get(i - 1).setPower(maxSlotPower);
					}
					i++;
				}
				newPowerPlan.setPowerQuotas(newPowerQuotas);
			}
			currentPowerPlan = newPowerPlan;
		}

		PowerPlan powerCappedPowerPlan = new PowerPlan();
		PowerPlan energyCappedPowerPlan = new PowerPlan();
		PowerPlan energyPropertyCappedPowerPlan = new PowerPlan();

		powerCappedPowerPlan = applyPowerCaps(currentPowerPlan, powerObjectivesList, erdsForecasts);
		energyCappedPowerPlan = applyEnergyCaps(powerCappedPowerPlan, energyObjectivesList);
		energyCappedPowerPlan = applyPowerCaps(energyCappedPowerPlan, powerObjectivesList, erdsForecasts);

		energyPropertyCappedPowerPlan = applyEnergyPropertyCaps(energyCappedPowerPlan, energyPropertyObjectivesList, erdsForecasts);

		return energyPropertyCappedPowerPlan;
	}

	private PowerPlan applyPowerCaps(PowerPlan powerPlan, List<Objective> powerObjectivesList, List<ErdsForecast> erdsForecasts) {
		if (powerObjectivesList.isEmpty()) {
			return powerPlan;
		}
		List<TimeSlotPower> powerQuotas = powerPlan.getPowerQuotas();
		SortedMap<Integer, Objective> powerObjectiveMap = new TreeMap<>();
		for (Objective currentObjective : powerObjectivesList) {
			powerObjectiveMap.put(currentObjective.getPriority(), currentObjective);
		}
		Iterator<Objective> powerObjectiveIterator = powerObjectiveMap.values().iterator();
		while (powerObjectiveIterator.hasNext()) {
			Objective currentPowerObjective = powerObjectiveIterator.next();
			for (TimeSlotPower currentTimeSlotPower : powerQuotas) {
				Amount<Power> maxSlotPower = Amount.valueOf(0, WATT);
				int timeslot = currentTimeSlotPower.getTimeSlot();
				for (ErdsForecast currentErdsForecast : erdsForecasts) {
					maxSlotPower = maxSlotPower.plus(currentErdsForecast.getTimeSlotForecasts().get(timeslot - 1).getPower());
				}
				if (currentTimeSlotPower.getPower().isGreaterThan(maxSlotPower)) {
					currentTimeSlotPower.setPower(maxSlotPower);
				}
				Amount<Duration> slotOffset = powerPlan.getTimeSlotDuration().times((currentTimeSlotPower.getTimeSlot() - 1));
				DateTime slotStart = powerPlan.getDateFrom().plus(slotOffset.longValue(MILLI(SECOND)));
				List<Objective> activeObjectives = null;
				try {
					activeObjectives = ObjectiveUtils.filterActiveObjectives(Collections.singletonList(currentPowerObjective), slotStart.toDate());
				} catch (TimeIntervalExpressionException e) {
					e.printStackTrace();
				}
				if (!activeObjectives.isEmpty()) {
					if (currentTimeSlotPower.getPower().longValue(WATT) > (long) activeObjectives.get(0).getTarget().getValue()) {
						currentTimeSlotPower.setPower(Amount.valueOf((long) activeObjectives.get(0).getTarget().getValue(), WATT));
					}
				}
			}
		}
		powerPlan.setPowerQuotas(powerQuotas);
		return powerPlan;
	}

	private PowerPlan applyEnergyCaps(PowerPlan powerPlan, List<Objective> energyObjectivesList) {
		if (energyObjectivesList.isEmpty()) {
			return powerPlan;
		}
		List<TimeSlotPower> powerQuotas = powerPlan.getPowerQuotas();
		SortedMap<Integer, Objective> energyObjectiveMap = new TreeMap<>();
		for (Objective currentObjective : energyObjectivesList) {
			energyObjectiveMap.put(currentObjective.getPriority(), currentObjective);
		}
		Iterator<Objective> energyObjectiveIterator = energyObjectiveMap.values().iterator();
		while (energyObjectiveIterator.hasNext()) {
			Objective currentEnergyObjective = energyObjectiveIterator.next();
			int firstSlot = -1;
			int lastSlot;
			Amount<Energy> energyCap = null;
			for (TimeSlotPower currentTimeSlotPower : powerQuotas) {
				Amount<Duration> slotOffset = powerPlan.getTimeSlotDuration().times((currentTimeSlotPower.getTimeSlot() - 1));
				DateTime slotStart = powerPlan.getDateFrom().plus(slotOffset.longValue(MILLI(SECOND)));
				List<Objective> activeObjectives = null;
				try {
					activeObjectives = ObjectiveUtils.filterActiveObjectives(Collections.singletonList(currentEnergyObjective), slotStart.toDate());
				} catch (TimeIntervalExpressionException e) {
					e.printStackTrace();
				}
				if (!activeObjectives.isEmpty()) {
					if (firstSlot == -1) {
						firstSlot = currentTimeSlotPower.getTimeSlot();
						energyCap = (Amount<Energy>) Amount.valueOf((long) activeObjectives.get(0).getTarget().getValue() * 3600, JOULE);
					}
				} else {
					if (firstSlot != -1) {
						lastSlot = currentTimeSlotPower.getTimeSlot() - 1;
						Amount<Energy> rangeEnergyDemand = Amount.valueOf(0, JOULE);
						for (int timeSlot = firstSlot; timeSlot <= lastSlot; timeSlot++) {
							Amount<?> timeSlotEnergyDemand = powerQuotas.get(timeSlot - 1).getPower().times(powerPlan.getTimeSlotDuration());
							rangeEnergyDemand = rangeEnergyDemand.plus(timeSlotEnergyDemand);
						}
						double factor = energyCap.divide(rangeEnergyDemand).getEstimatedValue();
						for (int timeSlot = firstSlot; timeSlot <= lastSlot; timeSlot++) {
							powerQuotas.get(timeSlot - 1).setPower(powerQuotas.get(timeSlot - 1).getPower().times(factor));
						}
						break;
					}
				}
			}
		}
		powerPlan.setPowerQuotas(powerQuotas);
		return powerPlan;
	}

	private PowerPlan applyEnergyPropertyCaps(PowerPlan powerPlan, List<Objective> energyPropertyObjectivesList, List<ErdsForecast> erdsForecasts) {
		
		if (energyPropertyObjectivesList.isEmpty()) {
			return powerPlan;
		}
		
		List<TimeSlotPower> powerQuotas = powerPlan.getPowerQuotas();
		List<TreeMap<Integer, Amount<Power>>> timeSlotErdsList = new ArrayList<TreeMap<Integer, Amount<Power>>>();

		int numberOfSlots = powerQuotas.size();

		int i = 0;

		while (i < numberOfSlots) {
			TreeMap<Integer, Amount<Power>> currentErdsTreeMap = new TreeMap<Integer, Amount<Power>>();
			for (ErdsForecast currentErdsForecast : erdsForecasts) {
				int currentRenewablePercentage = (int) currentErdsForecast.getTimeSlotForecasts().get(i).getRenewablePercentage().longValue(PERCENT);
				Amount<Power> currentPower = currentErdsForecast.getTimeSlotForecasts().get(i).getPower();
				currentErdsTreeMap.put(currentRenewablePercentage, currentPower);
			}
			timeSlotErdsList.add(currentErdsTreeMap);
			i++;
		}

		Amount<Power> totalPower = Amount.valueOf(0, WATT);

		for (TimeSlotPower currentTimeSlotPower : powerQuotas) {
			totalPower = totalPower.plus(currentTimeSlotPower.getPower());
		}

		Amount<Power> powerStep = totalPower.divide((powerQuotas.size() * 10));

		SortedMap<Integer, Integer> timeSlotRenewable = calculateTimeSlotRenewablePercent(powerQuotas, timeSlotErdsList);
		i = 1;

		long renTotal = 0;
		long powerTotal = 0;
		while (i <= powerQuotas.size()) {
			renTotal += ((double) timeSlotRenewable.get(i)) / 100 * powerQuotas.get(i - 1).getPower().longValue(WATT);
			powerTotal += powerQuotas.get(i - 1).getPower().longValue(WATT);
			i++;
		}
		double avgPct = (double) ((renTotal*100) / powerTotal);

		double requestedPct = energyPropertyObjectivesList.get(0).getTarget().getValue();

		if (avgPct >= requestedPct) {
			powerPlan.setPowerQuotas(powerQuotas);
			return powerPlan;
		} else {
			int iterations = 1;
			while (avgPct < requestedPct && iterations <= MAX_ITERATIONS) {
				logger.debug("Iteration #" + iterations);
				logger.debug("PRE: " + avgPct);
				i = 1;
				while (i <= powerQuotas.size()) {
					Amount<Power> maxSlotPower = Amount.valueOf(0, WATT);
					powerStep = Amount.valueOf(powerQuotas.get(i - 1).getPower().divide(10).getMinimumValue(), WATT);
					for (ErdsForecast currentErdsForecast : erdsForecasts) {
						maxSlotPower = maxSlotPower.plus(currentErdsForecast.getTimeSlotForecasts().get(i - 1).getPower());
					}
					if (timeSlotRenewable.get(i) > requestedPct) {
						Amount<Power> newPower = powerQuotas.get(i - 1).getPower().plus(powerStep);
						if (newPower.isGreaterThan(maxSlotPower)) {
							newPower = maxSlotPower;
						}
						powerQuotas.get(i - 1).setPower(newPower);
					} else if (timeSlotRenewable.get(i) <= requestedPct) {
						powerQuotas.get(i - 1).setPower(powerQuotas.get(i - 1).getPower().minus(powerStep));
					}
					i++;
				}
				timeSlotRenewable = calculateTimeSlotRenewablePercent(powerQuotas, timeSlotErdsList);
				i = 1;
				renTotal = 0;
				powerTotal = 0;
				while (i <= powerQuotas.size()) {
					renTotal += ((double) timeSlotRenewable.get(i) / 100) * powerQuotas.get(i - 1).getPower().longValue(WATT);
					powerTotal += powerQuotas.get(i - 1).getPower().longValue(WATT);
					i++;
				}
				avgPct = ((double) renTotal / powerTotal) * 100;
				iterations++;
			}
			logger.debug("POST: " + avgPct);
			powerPlan.setPowerQuotas(powerQuotas);
			return powerPlan;
		}
	}

	private SortedMap<Integer, Integer> calculateTimeSlotRenewablePercent(List<TimeSlotPower> powerQuotas,
			List<TreeMap<Integer, Amount<Power>>> timeSlotErdsList) {
		int i = 1;
		SortedMap<Integer, Integer> timeSlotRenewable = new TreeMap<Integer, Integer>();
		for (TimeSlotPower currentTimeSlotPower : powerQuotas) {
			TreeMap<Integer, Amount<Power>> currentErdsTreeMap = timeSlotErdsList.get(i - 1);
			Amount<Power> residualPower = currentTimeSlotPower.getPower();
			Amount<Power> renewablePower = Amount.valueOf(0, WATT);
			Iterator<Integer> erdsPowerIterator = currentErdsTreeMap.descendingMap().keySet().iterator();
			while (erdsPowerIterator.hasNext()) {
				int currentRenewablePercentage = erdsPowerIterator.next();
				Amount<Power> currentPower = currentErdsTreeMap.get(currentRenewablePercentage);
				if (residualPower.isGreaterThan(currentPower)) {
					residualPower = residualPower.minus(currentPower);
					renewablePower = renewablePower.plus(currentPower.times((double) currentRenewablePercentage / 100));
				} else {
					renewablePower = renewablePower.plus(residualPower.times((double) currentRenewablePercentage / 100));
					double renewablePercent = renewablePower.divide(currentTimeSlotPower.getPower()).getEstimatedValue();
					timeSlotRenewable.put(i, (int) (Math.round((renewablePercent * 100))));
					i++;
					break;
				}
			}
		}
		return timeSlotRenewable;
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
}
