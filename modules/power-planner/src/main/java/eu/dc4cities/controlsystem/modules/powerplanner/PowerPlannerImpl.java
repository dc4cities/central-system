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
import eu.dc4cities.controlsystem.model.erds.TimeSlotErdsForecast;
import eu.dc4cities.controlsystem.modules.PowerPlanner;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.*;
import eu.dc4cities.controlsystem.modules.powerplanner.converter.MetricTimeSeriesToActivity;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;
import org.springframework.stereotype.Component;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Power;
import javax.measure.unit.SI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static javax.measure.unit.NonSI.MINUTE;
import static javax.measure.unit.NonSI.PERCENT;
import static javax.measure.unit.SI.*;

/**
 * Implementation of a power planner for a data centre. Given the forecasts of
 * renewable energy availability of all power sources of the data centre and the
 * current power usage plan of the data centre, calculates a power plan for the
 * configured time intervals that satisfies all power and energy objectives, if
 * possible.
 *
 *
 */
/**
 * Remark by Torben Möller
 * PowerPlannerImpl uses the constraint solver engine. It will be used in Phase 2.
 */

@Component
public class PowerPlannerImpl implements PowerPlanner, PowerConfigAware {

	private List<Objective> energyConfiguration = new ArrayList<>();
	private List<TimeSlot> objectivesPlan;
	private OPCP constraintSolver;
	private List<OPCPDecorator> decorators;

	public static final int WATT_REDUCER = 4;

	public PowerPlannerImpl() {
		objectivesPlan = new ArrayList<>();
		decorators = new ArrayList<>();
	}

	@Override
	public PowerPlan calculateIdealPowerPlan(TimeSlotBasedEntity timeRange, List<ErdsForecast> erdsForecasts, PowerPlan currentPowerPlan) {

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

		if (currentPowerPlan == null) {
			currentPowerPlan = new PowerPlan();
			currentPowerPlan.setDateFrom(timeRange.getDateFrom());
			currentPowerPlan.setDateTo(timeRange.getDateTo());
			currentPowerPlan.setTimeSlotDuration(timeRange.getTimeSlotDuration());
			List<TimeSlotPower> quotas = new ArrayList<>();
			for (int i = 1; i <= numberOfSlots; i++) {
				TimeSlotPower timeSlotPower = new TimeSlotPower(i);
				timeSlotPower.setPower(Amount.valueOf(OPCP.MAX_POWER, WATT));
				quotas.add(timeSlotPower);
			}
			currentPowerPlan.setPowerQuotas(quotas);
		}

		PowerPlan powerConstraintCappedPowerPlan = new PowerPlan();
		powerConstraintCappedPowerPlan.setDateFrom(timeRange.getDateFrom());
		powerConstraintCappedPowerPlan.setDateTo(timeRange.getDateTo());
		powerConstraintCappedPowerPlan.setTimeSlotDuration(timeRange.getTimeSlotDuration());
		List<TimeSlotPower> cappedPowerQuotas = new ArrayList<>();

		Amount<Duration> timeSlotDuration = timeRange.getTimeSlotDuration();
		List<TimeSlotPower> powerQuotas = new ArrayList<>();

		int i = 0;
		while (i < numberOfSlots) {
			TimeSlot timeSlot = new TimeSlotImpl(timeSlotDuration);
			int offset = (int) timeRange.getTimeSlotDuration().times(i).longValue(MINUTE);
			DateTime timeSlotStart = timeRange.getDateFrom().plusMinutes(offset);
			timeSlot.setStartDate(new DateTime(timeSlotStart));
			offset = offset + (int) timeRange.getTimeSlotDuration().longValue(MINUTE);
			DateTime timeSlotEnd = timeRange.getDateFrom().plusMinutes(offset);
			timeSlot.setEndDate(new DateTime(timeSlotEnd));
			HashSet<Objective> obj = new HashSet<Objective>();
			List<Objective> activeObjectives = null;
			try {
				activeObjectives = ObjectiveUtils.filterActiveObjectives(energyConfiguration, timeSlot.getStartDate().toDate());
			} catch (TimeIntervalExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (activeObjectives != null) {
				obj.addAll(activeObjectives);
			}
			timeSlot.setObjectives(obj);
			timeSlot.setPower(currentPowerPlan.getPowerQuotas().get(i).getPower());
			timeSlot.setPowerCap(timeSlot.getPower());
			objectivesPlan.add(timeSlot);
			i++;
		}

		applyPowerCaps(objectivesPlan);

		i = 1;
		for (TimeSlot currentTimeSlot : objectivesPlan) {
			TimeSlotPower currentTimeSlotPower = new TimeSlotPower(i);
			currentTimeSlotPower.setPower(currentTimeSlot.getPower());
			cappedPowerQuotas.add(currentTimeSlotPower);
		}
		powerConstraintCappedPowerPlan.setPowerQuotas(cappedPowerQuotas);

		List<OldPowerSource> powerSources = new ArrayList<>();

		for (ErdsForecast currentErdsForecast : erdsForecasts) {
			OldPowerSourceSlot[] slots = new OldPowerSourceSlot[currentErdsForecast.getTimeSlotForecasts().size()];
			i = 0;
			for (TimeSlotErdsForecast currentTimeSlotErdsForecast : currentErdsForecast.getTimeSlotForecasts()) {
				Amount<Power> power = currentTimeSlotErdsForecast.getPower();
				int renewablePercentage = (int) currentTimeSlotErdsForecast.getRenewablePercentage().longValue(PERCENT);
				float carbonEmissions = (float) currentTimeSlotErdsForecast.getCo2Factor().getExactValue();
				slots[i] = new OldPowerSourceSlot(power, renewablePercentage, carbonEmissions);
				i++;
			}
			String erdsName = currentErdsForecast.getErdsName();
			OldPowerSource powerSource = new OldPowerSource(erdsName, slots);
			powerSources.add(powerSource);
		}

		List<SimpleActivity> simpleActivityList = MetricTimeSeriesToActivity.convert(currentPowerPlan, powerConstraintCappedPowerPlan);
		int[] slots = new int[numberOfSlots];
		Arrays.fill(slots, (int) timeRange.getTimeSlotDuration().longValue(SI.SECOND));
		constraintSolver = new OPCP(slots, powerSources, simpleActivityList, WATT_REDUCER);

		for (Objective objective : energyConfiguration) {
			List<OPCPDecorator> opcpDecoratorList = OPCPDecoratorFactory.create(objective, objectivesPlan, timeRange.getDateFrom().toDate());
			for (OPCPDecorator currentOPCPDecorator : opcpDecoratorList) {
				currentOPCPDecorator.decorate(constraintSolver);
				decorators.add(currentOPCPDecorator);
			}
		}

		MaxGreenEnergyUsage maxGreenEnergyUsage = new MaxGreenEnergyUsage();
		maxGreenEnergyUsage.decorate(constraintSolver);

		// constraintSolver.setVerbosity(2);
		constraintSolver.solve(10);
		int constraintSolverSlots = constraintSolver.getNbSlots();

		int[] ideal = new int[constraintSolverSlots];

		for (int t = 0; t < constraintSolverSlots; t++) {
			ideal[t] = constraintSolver.toWatt(constraintSolver.getActivityPowerUsage(simpleActivityList.get(t))[t]);
		}

		i = 1;
		for (int power : ideal) {
			TimeSlotPower timeSlotPower = new TimeSlotPower(i);
			timeSlotPower.setPower(Amount.valueOf(power, WATT));
			powerQuotas.add(timeSlotPower);
			i++;
		}
		/*
		 * for (IntVar v : maxGreenEnergyUsage.getGreenList()) { if
		 * (v.getValue() != 0) { System.out.println(v); } }
		 */
		return buildPowerPlan(timeRange, powerQuotas);
	}

	private void applyPowerCaps(List<TimeSlot> objectivesPlan2) {
		for (TimeSlot currentTimeSlot : objectivesPlan) {
			for (Objective currentObjective : currentTimeSlot.getObjectives()) {
				int priority = 0;
				if (currentObjective.getType().equals("POWER")) {
					if (currentObjective.getTarget().getOperator().equals("lt") || currentObjective.getTarget().getOperator().equals("leq")
							|| currentObjective.getTarget().getOperator().equals("eq")) {
						if (priority <= currentObjective.getPriority()) {
							priority = currentObjective.getPriority();
							if (currentTimeSlot.getPower().getExactValue() > currentObjective.getTarget().getValue()) {
								currentTimeSlot.setPower(Amount.valueOf((long) currentObjective.getTarget().getValue() - 1, WATT));
							}
						}
					} else {
						if (priority <= currentObjective.getPriority()) {
							priority = currentObjective.getPriority();
							if (currentTimeSlot.getPower().getExactValue() < currentObjective.getTarget().getValue()) {
								currentTimeSlot.setPower(Amount.valueOf((long) currentObjective.getTarget().getValue() + 1, WATT));
							}
						}
					}
				}
			}
		}
	}

	// Builds a power plan to hand to the DC
	private PowerPlan buildPowerPlan(TimeSlotBasedEntity timeRange, List<TimeSlotPower> powerQuotas) {
		PowerPlan powerPlan = new PowerPlan();
		powerPlan.setDateFrom(timeRange.getDateFrom());
		powerPlan.setDateTo(timeRange.getDateTo());
		powerPlan.setTimeSlotDuration(timeRange.getTimeSlotDuration());
		powerPlan.setPowerQuotas(powerQuotas);
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

	public OPCP getConstraintSolver() {
		return constraintSolver;
	}

	public void setConstraintSolver(OPCP constraintSolver) {
		this.constraintSolver = constraintSolver;
	}

	public List<Objective> getEnergyConfiguration() {
		return energyConfiguration;
	}

	public void setEnergyConfiguration(List<Objective> energyConfiguration) {
		this.energyConfiguration = energyConfiguration;
	}

	public List<TimeSlot> getObjectivesPlan() {
		return objectivesPlan;
	}

	public void setObjectivesPlan(List<TimeSlot> objectivesPlan) {
		this.objectivesPlan = objectivesPlan;
	}

	public List<OPCPDecorator> getDecorators() {
		return decorators;
	}
}
