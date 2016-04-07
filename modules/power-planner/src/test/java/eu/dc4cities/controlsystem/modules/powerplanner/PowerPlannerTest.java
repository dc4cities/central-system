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

import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.model.erds.TimeSlotErdsForecast;
import eu.dc4cities.controlsystem.model.unit.Units;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.measure.quantity.Duration;
import java.util.ArrayList;
import java.util.List;

import static javax.measure.unit.NonSI.PERCENT;
import static javax.measure.unit.SI.WATT;

/**
 *
 */

public class PowerPlannerTest {

	private static Logger LOGGER = LoggerFactory.getLogger("powerplanner");

	// @SuppressWarnings("rawtypes")
	// @Test
	// public void testPowerPlannerSimplePowerObjective() {
	// PowerPlannerImpl idealPowerPlanner = new PowerPlannerImpl();
	// Amount<Duration> timeSlotDuration = Amount.valueOf(15, MINUTE);
	// DateTime dateFrom = new DateTime(2014, 6, 1, 12, 0, 0, 0);
	// DateTime dateTo = new DateTime(2014, 6, 2, 12, 0, 0, 0);
	// long timeDiff = dateTo.getMillis() - dateFrom.getMillis();
	// int timeSlotNumber = (int) (timeDiff /
	// timeSlotDuration.longValue(MILLI(SECOND)));
	// PowerPlan currentPowerPlan = prepareCurrentPowerPlan(20000, 0.5,
	// timeSlotNumber, dateFrom, dateTo, timeSlotDuration);
	// TimeSlotBasedEntity timeRange = new TimeSlotBasedEntity();
	// timeRange.setDateFrom(dateFrom);
	// timeRange.setDateTo(dateTo);
	// timeRange.setTimeSlotDuration(timeSlotDuration);
	// ErdsForecast erdsForecast = prepareConstantForecast(90000, 0,
	// timeSlotNumber, dateFrom, dateTo, timeSlotDuration);
	// Objective objective = preparePowerObjective(dateFrom, 22000, "lt");
	// idealPowerPlanner.setEnergyConfiguration(Collections.singletonList(objective));
	// List<ErdsForecast> erdsForecasts =
	// Collections.singletonList(erdsForecast);
	//
	// idealPowerPlanner.calculateIdealPowerPlan(timeRange, erdsForecasts,
	// currentPowerPlan);
	//
	// OPCP constraintSolver = idealPowerPlanner.getConstraintSolver();
	// int nbSlots = constraintSolver.getNbSlots();
	// int[] ideal = new int[nbSlots];
	// int[] usage = new int[nbSlots];
	// List<Constraint[]> constraintsList = new ArrayList<Constraint[]>();
	//
	// for (int t = 0; t < nbSlots; t++) {
	// System.out.println(constraintSolver.getPowerUsage()[t]);
	// ideal[t] = constraintSolver.getPowerUsage()[t].getValue();
	// System.out.println(Arrays.toString(constraintSolver.getPowerUsage("test")));
	// usage[t] = constraintSolver.getPowerUsage("test")[t].getValue();
	// constraintsList.add(constraintSolver.getSolver().getCstrs());
	// }
	//
	// System.out.println("Ideal Power Plan:" + Arrays.toString(ideal));
	// System.out.println("Decomposition:");
	// System.out.println("\tOn source 1:" + Arrays.toString(usage));
	// for (OPCPDecorator currentOPCPDecorator :
	// idealPowerPlanner.getDecorators()) {
	// System.out.println(currentOPCPDecorator.toString() + ": " +
	// ((CappableConstraint) currentOPCPDecorator).isSatisfied().getValue());
	// }
	//
	// LOGGER.debug(idealPowerPlanner.toString());
	// }

	// @SuppressWarnings("rawtypes")
	// @Test
	// public void testPowerPlannerSimpleEnergyObjective() {
	// PowerPlannerImpl idealPowerPlanner = new PowerPlannerImpl();
	// Amount<Duration> timeSlotDuration = Amount.valueOf(15, MINUTE);
	// DateTime dateFrom = new DateTime(2014, 6, 1, 12, 0, 0, 0);
	// DateTime dateTo = new DateTime(2014, 6, 2, 12, 0, 0, 0);
	// long timeDiff = dateTo.getMillis() - dateFrom.getMillis();
	// int timeSlotNumber = (int) (timeDiff /
	// timeSlotDuration.longValue(MILLI(SECOND)));
	// PowerPlan currentPowerPlan = prepareCurrentPowerPlan(20000, 0,
	// timeSlotNumber, dateFrom, dateTo, timeSlotDuration);
	// TimeSlotBasedEntity timeRange = new TimeSlotBasedEntity();
	// timeRange.setDateFrom(dateFrom);
	// timeRange.setDateTo(dateTo);
	// timeRange.setTimeSlotDuration(timeSlotDuration);
	// ErdsForecast erdsForecast = prepareConstantForecast(90000, 0,
	// timeSlotNumber, dateFrom, dateTo, timeSlotDuration);
	// Objective objective = prepareEnergyObjective(dateFrom, 200000000, "lt");
	// idealPowerPlanner.setEnergyConfiguration(Collections.singletonList(objective));
	// List<ErdsForecast> erdsForecasts =
	// Collections.singletonList(erdsForecast);
	//
	// idealPowerPlanner.calculateIdealPowerPlan(timeRange, erdsForecasts,
	// currentPowerPlan);
	//
	// OPCP constraintSolver = idealPowerPlanner.getConstraintSolver();
	// int nbSlots = constraintSolver.getNbSlots();
	// int[] ideal = new int[nbSlots];
	// int[] usage = new int[nbSlots];
	// List<Constraint[]> constraintsList = new ArrayList<Constraint[]>();
	//
	// for (int t = 0; t < nbSlots; t++) {
	// System.out.println(constraintSolver.getPowerUsage()[t]);
	// ideal[t] = constraintSolver.getPowerUsage()[t].getValue();
	// System.out.println(Arrays.toString(constraintSolver.getPowerUsage("test")));
	// usage[t] = constraintSolver.getPowerUsage("test")[t].getValue();
	// constraintsList.add(constraintSolver.getSolver().getCstrs());
	// }
	//
	// System.out.println("Ideal Power Plan:" + Arrays.toString(ideal));
	// System.out.println("Decomposition:");
	// System.out.println("\tOn source 1:" + Arrays.toString(usage));
	// for (OPCPDecorator currentOPCPDecorator :
	// idealPowerPlanner.getDecorators()) {
	// System.out.println(currentOPCPDecorator.toString() + ": " +
	// ((CappableConstraint) currentOPCPDecorator).isSatisfied().getValue());
	// }
	//
	// LOGGER.debug(idealPowerPlanner.toString());
	// }
	//
//	@SuppressWarnings("rawtypes")
//	@Test
//	public void testPowerPlannerMultipleObjectives() {
//		PowerPlannerImpl idealPowerPlanner = new PowerPlannerImpl();
//		Amount<Duration> timeSlotDuration = Amount.valueOf(15, MINUTE);
//		DateTime dateFrom1 = new DateTime(2014, 6, 1, 12, 0, 0, 0);
//		DateTime dateFrom2 = new DateTime(2014, 6, 1, 16, 0, 0, 0);
//		DateTime dateFrom3 = new DateTime(2014, 6, 1, 20, 0, 0, 0);
//		DateTime dateTo = new DateTime(2014, 6, 2, 12, 0, 0, 0);
//		long timeDiff = dateTo.getMillis() - dateFrom1.getMillis();
//		int timeSlotNumber = (int) (timeDiff / timeSlotDuration.longValue(MILLI(SECOND)));
//		PowerPlan currentPowerPlan = prepareCurrentPowerPlan(20000, 0.5, timeSlotNumber, dateFrom1, dateTo, timeSlotDuration);
//		TimeSlotBasedEntity timeRange = new TimeSlotBasedEntity();
//		timeRange.setDateFrom(dateFrom1);
//		timeRange.setDateTo(dateTo);
//		timeRange.setTimeSlotDuration(timeSlotDuration);
//		ErdsForecast renForecast = prepareConstantForecast(9000, 0.2, 0, 0, 100, 0, timeSlotNumber, dateFrom1, dateTo, timeSlotDuration, "ren");
//		ErdsForecast gridForecast = prepareConstantForecast(OPCP.MAX_INT, 0, 100, 0.2, 50, 0.5, timeSlotNumber, dateFrom1, dateTo, timeSlotDuration, "grid");
//		List<ErdsForecast> erdsForecastList = new ArrayList<>();
//		erdsForecastList.add(renForecast);
//		erdsForecastList.add(gridForecast);
//		List<Objective> objectivesList = new ArrayList<>();
//        Objective energyObjective1 = prepareEnergyObjective(dateFrom1, OPCP.MAX_INT, "lt", "PT6H");
//        Objective energyObjective2 = prepareEnergyObjective(dateFrom2, 30000, "lt", "PT5H");
//        Objective powerObjective1 = preparePowerObjective(dateFrom1, 20000, "lt", "P1M");
//		Objective powerObjective2 = preparePowerObjective(dateFrom3, 5000, "lt", "PT4H");
//		objectivesList.add(energyObjective1);
//		objectivesList.add(energyObjective2);
//		objectivesList.add(powerObjective1);
//		objectivesList.add(powerObjective2);
//		idealPowerPlanner.setEnergyConfiguration(objectivesList);
//		PowerPlan newPowerPlan = idealPowerPlanner.calculateIdealPowerPlan(timeRange, erdsForecastList, currentPowerPlan);
//
//
//		OPCP constraintSolver = idealPowerPlanner.getConstraintSolver();
//		int nbSlots = constraintSolver.getNbSlots();
//
//        for (PowerSource src : constraintSolver.getPowerSources()) {
//            int[] values = new int[nbSlots];
//            for (int t = 0; t < nbSlots; t++) {
//                values[t] = constraintSolver.toWatt(constraintSolver.getPowerUsage(src)[t]);
//            }
//            System.out.println(src.name() + ": " + Arrays.toString(values));
//        }
//        int[] ideal = new int[nbSlots];
//        for (SimpleActivity sa : constraintSolver.getActivities()) {
//            for (int t = 0; t < nbSlots; t++) {
//                ideal[t] += constraintSolver.toWatt(constraintSolver.getActivityPowerUsage(sa)[t]);
//            }
//        }
//        for (int t = 0; t < nbSlots; t++) {
//            Assert.assertEquals(ideal[t], newPowerPlan.getPowerQuotas().get(t).getPower().getExactValue());
//        }
//        System.out.println("Ideal power usage: " + Arrays.toString(ideal));
//        System.out.println("new power plan: " + newPowerPlan);
//        LOGGER.debug(idealPowerPlanner.toString());
//	}
	/**
	@SuppressWarnings("rawtypes")
	@Test
	public void testPowerPlannerNoCurrentPlan() {
		PowerPlannerImpl idealPowerPlanner = new PowerPlannerImpl();
		Amount<Duration> timeSlotDuration = Amount.valueOf(15, MINUTE);
		DateTime dateFrom1 = new DateTime(2014, 6, 1, 12, 0, 0, 0);
		DateTime dateTo = new DateTime(2014, 6, 2, 12, 0, 0, 0);
		long timeDiff = dateTo.getMillis() - dateFrom1.getMillis();
		int timeSlotNumber = (int) (timeDiff / timeSlotDuration.longValue(MILLI(SECOND)));
		PowerPlan currentPowerPlan = prepareCurrentPowerPlan(20000, 0.5, timeSlotNumber, dateFrom1, dateTo, timeSlotDuration);
		TimeSlotBasedEntity timeRange = new TimeSlotBasedEntity();
		timeRange.setDateFrom(dateFrom1);
		timeRange.setDateTo(dateTo);
		timeRange.setTimeSlotDuration(timeSlotDuration);
		ErdsForecast renForecast = prepareConstantForecast(9000, 0.2, 0, 0, 100, 0, timeSlotNumber, dateFrom1, dateTo, timeSlotDuration, "ren");
		ErdsForecast gridForecast = prepareConstantForecast(OPCP.MAX_INT, 0, 100, 0.2, 50, 0.5, timeSlotNumber, dateFrom1, dateTo, timeSlotDuration, "grid");
		List<ErdsForecast> erdsForecastList = new ArrayList<>();
		erdsForecastList.add(renForecast);
		erdsForecastList.add(gridForecast);
		List<Objective> objectivesList = new ArrayList<>();
        Objective powerObjective1 = preparePowerObjective(dateFrom1, 200000, "lt", "P1M");
		//objectivesList.add(powerObjective1);
		idealPowerPlanner.setEnergyConfiguration(objectivesList);
		PowerPlan newPowerPlan = idealPowerPlanner.calculateIdealPowerPlan(timeRange, erdsForecastList, null);

		OPCP constraintSolver = idealPowerPlanner.getConstraintSolver();
		int nbSlots = constraintSolver.getNbSlots();

        for (PowerSource src : constraintSolver.getPowerSources()) {
            int[] values = new int[nbSlots];
            for (int t = 0; t < nbSlots; t++) {
                values[t] = constraintSolver.toWatt(constraintSolver.getPowerUsage(src)[t]);
            }
	System.out.println(src.name() + ": " + Arrays.toString(values));
	}
	int[] ideal = new int[nbSlots];
        for (SimpleActivity sa : constraintSolver.getActivities()) {
            for (int t = 0; t < nbSlots; t++) {
                ideal[t] += constraintSolver.toWatt(constraintSolver.getActivityPowerUsage(sa)[t]);
            }
        }
        for (int t = 0; t < nbSlots; t++) {
            Assert.assertEquals(ideal[t], newPowerPlan.getPowerQuotas().get(t).getPower().getExactValue());
        }
        System.out.println("Ideal power usage: " + Arrays.toString(ideal));
        System.out.println("new power plan: " + newPowerPlan);
        LOGGER.debug(idealPowerPlanner.toString());
	}

	private PowerPlan prepareCurrentPowerPlan(long averagePower, double variation, int timeSlotNumber, DateTime dateFrom, DateTime dateTo,
			Amount<Duration> timeSlotDuration) {
		PowerPlan powerPlan = new PowerPlan();

		List<TimeSlotPower> powerQuotas = new ArrayList<>();

		int i = 1;
		System.out.println("Current power plan:");
		while (i <= timeSlotNumber) {
			TimeSlotPower timeSlotPower = new TimeSlotPower(i);
			long power = averagePower + (long) (averagePower * variation * (Math.random() - 0.5));
			System.out.print(power + ", ");
			timeSlotPower.setPower(Amount.valueOf(power, WATT));
			powerQuotas.add(timeSlotPower);
			i++;
		}
		System.out.println();
		powerPlan.setDateFrom(dateFrom);
		powerPlan.setDateTo(dateTo);
		powerPlan.setPowerQuotas(powerQuotas);
		powerPlan.setTimeSlotDuration(timeSlotDuration);

		return powerPlan;
	}

	private Objective preparePowerObjective(DateTime dateFrom, int power, String operator, String duration) {
		Objective objective = new Objective();
		objective.setCreationDate(new Date());
		objective.setDataCenterId("0");
		objective.setDescription("TestObjective");
		objective.setEnabled(true);
		objective.setId("0");
		objective.setImplementationType(ImplementationType.MUST);
		objective.setLastModified(new Date());
		objective.setName("TestObjective");
		objective.setPriority(0);
		objective.setType(ObjectiveType.POWER);
		Target target = new Target();
		target.setMetric("W");
		target.setOperator(operator);
		target.setValue(power);
		objective.setTarget(target);

		TimeFrame timeFrame = new TimeFrame();
		timeFrame.setStartDate(dateFrom.toDate());
		timeFrame.setDuration(duration);
		objective.setTimeFrame(timeFrame);

		return objective;
	}

	private Objective prepareEnergyObjective(DateTime dateFrom, int energy, String operator, String duration) {
		Objective objective = new Objective();
		objective.setCreationDate(new Date());
		objective.setDataCenterId("0");
		objective.setDescription("TestObjective");
		objective.setEnabled(true);
		objective.setId("0");
		objective.setImplementationType(ImplementationType.MUST);
		objective.setLastModified(new Date());
		objective.setName("TestObjective");
		objective.setPriority(0);
		objective.setType(ObjectiveType.ENERGY);
		Target target = new Target();
		target.setMetric("Wh");
		target.setOperator("lt");
		target.setValue(energy);
		objective.setTarget(target);

		TimeFrame timeFrame = new TimeFrame();
		timeFrame.setStartDate(dateFrom.toDate());
		timeFrame.setDuration(duration);
		objective.setTimeFrame(timeFrame);

		return objective;
	}

	private ErdsForecast prepareConstantForecast(long averagePower, double powerVariation, long averageCEF, double cefVariation, long averageRenPct,
			double renPctVariation, int timeSlotNumber, DateTime dateFrom, DateTime dateTo, Amount<Duration> timeSlotDuration, String name) {
		ErdsForecast erdsForecast = new ErdsForecast(name);
		List<TimeSlotErdsForecast> timeSlotErdsForecastList = new ArrayList<>();
		int i = 1;
        System.out.println(name + " Power forecasted:");
        while (i <= timeSlotNumber) {
			TimeSlotErdsForecast timeSlotErdsForecast = new TimeSlotErdsForecast(i);
			long power = averagePower + (long) (averagePower * powerVariation * (Math.random() - 0.5));
			long renPct = averageRenPct + (long) (averageRenPct * renPctVariation * (Math.random() - 0.5));
			long cef = averageCEF + (long) (averageCEF * cefVariation * (Math.random() - 0.5));
			System.out.print(power + ", ");
			timeSlotErdsForecast.setPower(Amount.valueOf(power, WATT));
			timeSlotErdsForecast.setCarbonEmissions(Amount.valueOf(cef, AdvancedUnit.CEF));
			timeSlotErdsForecast.setRenewablePercentage(Amount.valueOf(renPct, PERCENT));
			timeSlotErdsForecastList.add(timeSlotErdsForecast);
			i++;
		}
		System.out.println();
		erdsForecast.setDateFrom(dateFrom);
		erdsForecast.setDateTo(dateTo);
		erdsForecast.setTimeSlotDuration(timeSlotDuration);
		erdsForecast.setTimeSlotForecasts(timeSlotErdsForecastList);

		return erdsForecast;
	}
**/
	private ErdsForecast preparePVForecast(long peakPower, int timeSlotNumber, DateTime dateFrom, DateTime dateTo, Amount<Duration> timeSlotDuration) {
		ErdsForecast erdsForecast = new ErdsForecast("test");
		List<TimeSlotErdsForecast> timeSlotErdsForecastList = new ArrayList<>();
		int i = 1;
		System.out.println("Power forecasted:");
		while (i <= timeSlotNumber) {
			TimeSlotErdsForecast timeSlotErdsForecast = new TimeSlotErdsForecast(i);
			long power = (long) (10000 * Math.pow(Math.E, -(Math.exp(Math.pow(((i - (timeSlotNumber / 2)) / (timeSlotNumber / 5)), 2)))));
			System.out.print(power + ", ");
			timeSlotErdsForecast.setPower(Amount.valueOf(power, WATT));
			timeSlotErdsForecast.setCo2Factor(Amount.valueOf(0, Units.KG_PER_KWH));
			timeSlotErdsForecast.setRenewablePercentage(Amount.valueOf(100, PERCENT));
			timeSlotErdsForecastList.add(timeSlotErdsForecast);
			i++;
		}
		System.out.println();
		erdsForecast.setDateFrom(dateFrom);
		erdsForecast.setDateTo(dateTo);
		erdsForecast.setTimeSlotDuration(timeSlotDuration);
		erdsForecast.setTimeSlotForecasts(timeSlotErdsForecastList);

		return erdsForecast;
	}
}
