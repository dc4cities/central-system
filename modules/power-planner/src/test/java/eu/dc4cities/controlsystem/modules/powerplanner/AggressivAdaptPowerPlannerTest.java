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

import eu.dc4cities.configuration.goal.*;
import eu.dc4cities.controlsystem.model.PowerPlan;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.TimeSlotPower;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.model.erds.TimeSlotErdsForecast;
import eu.dc4cities.controlsystem.model.unit.Units;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;
import org.junit.Test;

import javax.measure.quantity.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static javax.measure.unit.NonSI.MINUTE;
import static javax.measure.unit.NonSI.PERCENT;
import static javax.measure.unit.SI.*;

/**
 *
 */

public class AggressivAdaptPowerPlannerTest {

	@Test
	public void largeInputPowerPlannerNoCurrentPlan() {
		AggressivAdaptPowerPlannerImpl idealPowerPlanner = new AggressivAdaptPowerPlannerImpl();
		idealPowerPlanner.setDCMinPower(3000);
		idealPowerPlanner.setDCMaxPower(10000);
		Amount<Duration> timeSlotDuration = Amount.valueOf(15, MINUTE);
		DateTime dateFrom1 = new DateTime(2014, 7, 24, 12, 0, 0, 0);
		DateTime dateTo = new DateTime(2014, 7, 24, 14, 0, 0, 0);
		long timeDiff = dateTo.getMillis() - dateFrom1.getMillis();
		int timeSlotNumber = (int) (timeDiff / timeSlotDuration.longValue(MILLI(SECOND)));
		TimeSlotBasedEntity timeRange = new TimeSlotBasedEntity();
		timeRange.setDateFrom(dateFrom1);
		timeRange.setDateTo(dateTo);
		timeRange.setTimeSlotDuration(timeSlotDuration);
		ErdsForecast renForecast = prepareConstantForecast(4000, 0.5, 0, 0, 100, 0, timeSlotNumber, dateFrom1, dateTo, timeSlotDuration, "ren");
		ErdsForecast gridForecast = prepareConstantForecast(37548800L, 0, 100, 0.2, 20, 0.5, timeSlotNumber, dateFrom1, dateTo, timeSlotDuration, "grid");
		List<ErdsForecast> erdsForecastList = new ArrayList<>();
		erdsForecastList.add(renForecast);
		erdsForecastList.add(gridForecast);
		List<Objective> objectivesList = new ArrayList<>();
		Objective energyPropertyObjective1 = prepareEnergyPropertyObjective(dateFrom1, 80, "gt", "PT24H");
		objectivesList.add(energyPropertyObjective1);
		idealPowerPlanner.setEnergyConfiguration(objectivesList);
		PowerPlan newPowerPlan = idealPowerPlanner.calculateIdealPowerPlan(timeRange, erdsForecastList, null);

		System.out.println("1st run: " + newPowerPlan);
		renForecast = prepareConstantForecast(5000, 0.2, 0, 0, 100, 0, timeSlotNumber, dateFrom1.plusMinutes(15), dateTo.plusMinutes(15), timeSlotDuration,
				"ren");
		erdsForecastList = new ArrayList<>();
		erdsForecastList.add(renForecast);
		gridForecast.setDateFrom(dateFrom1.plusMinutes(15));
		gridForecast.setDateTo(dateTo.plusMinutes(15));
		erdsForecastList.add(gridForecast);
		timeRange.setDateFrom(dateFrom1.plusMinutes(15));
		timeRange.setDateTo(dateTo.plusMinutes(15));

		idealPowerPlanner.setAlpha(6);
		PowerPlan newerPowerPlan = idealPowerPlanner.calculateIdealPowerPlan(timeRange, erdsForecastList, newPowerPlan);

		System.out.println("2nd run: " + newerPowerPlan);
	}

	@Test
	public void testBug223() {
		NoOPCPRenPctOptimizingPowerPlannerImpl idealPowerPlanner = new NoOPCPRenPctOptimizingPowerPlannerImpl();
		idealPowerPlanner.setDCMinPower(1000);
		idealPowerPlanner.setDCMaxPower(2000);
		Amount<Duration> timeSlotDuration = Amount.valueOf(15, MINUTE);
		DateTime dateFrom = new DateTime(2014, 7, 2, 10, 0, 0, 0);
		DateTime dateTo = new DateTime(2014, 7, 2, 12, 0, 0, 0);
		long timeDiff = dateTo.getMillis() - dateFrom.getMillis();

		TimeSlotBasedEntity timeRange = new TimeSlotBasedEntity();
		timeRange.setDateFrom(dateFrom);
		timeRange.setDateTo(dateTo);
		timeRange.setTimeSlotDuration(timeSlotDuration);

		PowerPlan currentPowerPlan = new PowerPlan();
		currentPowerPlan.setDateFrom(dateFrom);
		currentPowerPlan.setDateTo(dateTo);
		currentPowerPlan.setTimeSlotDuration(timeSlotDuration);
		List<TimeSlotPower> powerQuotas = new ArrayList<>();
		TimeSlotPower timeSlotPower1 = new TimeSlotPower(1);
		timeSlotPower1.setPower(Amount.valueOf(300, WATT));
		powerQuotas.add(timeSlotPower1);
		TimeSlotPower timeSlotPower2 = new TimeSlotPower(1);
		timeSlotPower2.setPower(Amount.valueOf(420, WATT));
		powerQuotas.add(timeSlotPower2);
		TimeSlotPower timeSlotPower3 = new TimeSlotPower(1);
		timeSlotPower3.setPower(Amount.valueOf(495, WATT));
		powerQuotas.add(timeSlotPower3);
		TimeSlotPower timeSlotPower4 = new TimeSlotPower(1);
		timeSlotPower4.setPower(Amount.valueOf(345, WATT));
		powerQuotas.add(timeSlotPower4);
		TimeSlotPower timeSlotPower5 = new TimeSlotPower(1);
		timeSlotPower5.setPower(Amount.valueOf(345, WATT));
		powerQuotas.add(timeSlotPower5);
		TimeSlotPower timeSlotPower6 = new TimeSlotPower(1);
		timeSlotPower6.setPower(Amount.valueOf(345, WATT));
		powerQuotas.add(timeSlotPower6);
		TimeSlotPower timeSlotPower7 = new TimeSlotPower(1);
		timeSlotPower7.setPower(Amount.valueOf(225, WATT));
		powerQuotas.add(timeSlotPower7);
		TimeSlotPower timeSlotPower8 = new TimeSlotPower(1);
		timeSlotPower8.setPower(Amount.valueOf(225, WATT));
		powerQuotas.add(timeSlotPower8);
		currentPowerPlan.setPowerQuotas(powerQuotas);

		ErdsForecast erds1Forecast = new ErdsForecast("erds1");
		erds1Forecast.setDateFrom(dateFrom);
		erds1Forecast.setDateTo(dateTo);
		erds1Forecast.setTimeSlotDuration(timeSlotDuration);
		List<TimeSlotErdsForecast> timeSlotForecasts1 = new ArrayList<>();
		TimeSlotErdsForecast timeSlotErdsForecast11 = new TimeSlotErdsForecast(1);
		timeSlotErdsForecast11.setCo2Factor(Amount.valueOf(202, Units.KG_PER_KWH));
		timeSlotErdsForecast11.setPower(Amount.valueOf(1522, WATT));
		timeSlotErdsForecast11.setRenewablePercentage(Amount.valueOf(32, PERCENT));
		timeSlotForecasts1.add(timeSlotErdsForecast11);
		TimeSlotErdsForecast timeSlotErdsForecast12 = new TimeSlotErdsForecast(2);
		timeSlotErdsForecast12.setCo2Factor(Amount.valueOf(212, Units.KG_PER_KWH));
		timeSlotErdsForecast12.setPower(Amount.valueOf(1632, WATT));
		timeSlotErdsForecast12.setRenewablePercentage(Amount.valueOf(42, PERCENT));
		timeSlotForecasts1.add(timeSlotErdsForecast12);
		TimeSlotErdsForecast timeSlotErdsForecast13 = new TimeSlotErdsForecast(3);
		timeSlotErdsForecast13.setCo2Factor(Amount.valueOf(215, Units.KG_PER_KWH));
		timeSlotErdsForecast13.setPower(Amount.valueOf(1220, WATT));
		timeSlotErdsForecast13.setRenewablePercentage(Amount.valueOf(30, PERCENT));
		timeSlotForecasts1.add(timeSlotErdsForecast13);
		TimeSlotErdsForecast timeSlotErdsForecast14 = new TimeSlotErdsForecast(4);
		timeSlotErdsForecast14.setCo2Factor(Amount.valueOf(100, Units.KG_PER_KWH));
		timeSlotErdsForecast14.setPower(Amount.valueOf(1015, WATT));
		timeSlotErdsForecast14.setRenewablePercentage(Amount.valueOf(20, PERCENT));
		timeSlotForecasts1.add(timeSlotErdsForecast14);
		TimeSlotErdsForecast timeSlotErdsForecast15 = new TimeSlotErdsForecast(5);
		timeSlotErdsForecast15.setCo2Factor(Amount.valueOf(200, Units.KG_PER_KWH));
		timeSlotErdsForecast15.setPower(Amount.valueOf(1330, WATT));
		timeSlotErdsForecast15.setRenewablePercentage(Amount.valueOf(20, PERCENT));
		timeSlotForecasts1.add(timeSlotErdsForecast15);
		TimeSlotErdsForecast timeSlotErdsForecast16 = new TimeSlotErdsForecast(6);
		timeSlotErdsForecast16.setCo2Factor(Amount.valueOf(100, Units.KG_PER_KWH));
		timeSlotErdsForecast16.setPower(Amount.valueOf(1435, WATT));
		timeSlotErdsForecast16.setRenewablePercentage(Amount.valueOf(40, PERCENT));
		timeSlotForecasts1.add(timeSlotErdsForecast16);
		TimeSlotErdsForecast timeSlotErdsForecast17 = new TimeSlotErdsForecast(7);
		timeSlotErdsForecast17.setCo2Factor(Amount.valueOf(50, Units.KG_PER_KWH));
		timeSlotErdsForecast17.setPower(Amount.valueOf(1640, WATT));
		timeSlotErdsForecast17.setRenewablePercentage(Amount.valueOf(50, PERCENT));
		timeSlotForecasts1.add(timeSlotErdsForecast17);
		TimeSlotErdsForecast timeSlotErdsForecast18 = new TimeSlotErdsForecast(8);
		timeSlotErdsForecast18.setCo2Factor(Amount.valueOf(100, Units.KG_PER_KWH));
		timeSlotErdsForecast18.setPower(Amount.valueOf(1737, WATT));
		timeSlotErdsForecast18.setRenewablePercentage(Amount.valueOf(45, PERCENT));
		timeSlotForecasts1.add(timeSlotErdsForecast18);
		erds1Forecast.setTimeSlotForecasts(timeSlotForecasts1);

		ErdsForecast erds2Forecast = new ErdsForecast("erds2");
		erds2Forecast.setDateFrom(dateFrom);
		erds2Forecast.setDateTo(dateTo);
		erds2Forecast.setTimeSlotDuration(timeSlotDuration);
		List<TimeSlotErdsForecast> timeSlotForecasts2 = new ArrayList<>();
		TimeSlotErdsForecast timeSlotErdsForecast21 = new TimeSlotErdsForecast(1);
		timeSlotErdsForecast21.setCo2Factor(Amount.valueOf(200, Units.KG_PER_KWH));
		timeSlotErdsForecast21.setPower(Amount.valueOf(1520, WATT));
		timeSlotErdsForecast21.setRenewablePercentage(Amount.valueOf(35, PERCENT));
		timeSlotForecasts2.add(timeSlotErdsForecast21);
		TimeSlotErdsForecast timeSlotErdsForecast22 = new TimeSlotErdsForecast(2);
		timeSlotErdsForecast22.setCo2Factor(Amount.valueOf(210, Units.KG_PER_KWH));
		timeSlotErdsForecast22.setPower(Amount.valueOf(1430, WATT));
		timeSlotErdsForecast22.setRenewablePercentage(Amount.valueOf(40, PERCENT));
		timeSlotForecasts2.add(timeSlotErdsForecast22);
		TimeSlotErdsForecast timeSlotErdsForecast23 = new TimeSlotErdsForecast(3);
		timeSlotErdsForecast23.setCo2Factor(Amount.valueOf(200, Units.KG_PER_KWH));
		timeSlotErdsForecast23.setPower(Amount.valueOf(1535, WATT));
		timeSlotErdsForecast23.setRenewablePercentage(Amount.valueOf(45, PERCENT));
		timeSlotForecasts2.add(timeSlotErdsForecast23);
		TimeSlotErdsForecast timeSlotErdsForecast24 = new TimeSlotErdsForecast(4);
		timeSlotErdsForecast24.setCo2Factor(Amount.valueOf(190, Units.KG_PER_KWH));
		timeSlotErdsForecast24.setPower(Amount.valueOf(1640, WATT));
		timeSlotErdsForecast24.setRenewablePercentage(Amount.valueOf(50, PERCENT));
		timeSlotForecasts2.add(timeSlotErdsForecast24);
		TimeSlotErdsForecast timeSlotErdsForecast25 = new TimeSlotErdsForecast(5);
		timeSlotErdsForecast25.setCo2Factor(Amount.valueOf(200, Units.KG_PER_KWH));
		timeSlotErdsForecast25.setPower(Amount.valueOf(1425, WATT));
		timeSlotErdsForecast25.setRenewablePercentage(Amount.valueOf(35, PERCENT));
		timeSlotForecasts2.add(timeSlotErdsForecast25);
		TimeSlotErdsForecast timeSlotErdsForecast26 = new TimeSlotErdsForecast(6);
		timeSlotErdsForecast26.setCo2Factor(Amount.valueOf(210, Units.KG_PER_KWH));
		timeSlotErdsForecast26.setPower(Amount.valueOf(1232, WATT));
		timeSlotErdsForecast26.setRenewablePercentage(Amount.valueOf(25, PERCENT));
		timeSlotForecasts2.add(timeSlotErdsForecast26);
		TimeSlotErdsForecast timeSlotErdsForecast27 = new TimeSlotErdsForecast(7);
		timeSlotErdsForecast27.setCo2Factor(Amount.valueOf(200, Units.KG_PER_KWH));
		timeSlotErdsForecast27.setPower(Amount.valueOf(1037, WATT));
		timeSlotErdsForecast27.setRenewablePercentage(Amount.valueOf(30, PERCENT));
		timeSlotForecasts2.add(timeSlotErdsForecast27);
		TimeSlotErdsForecast timeSlotErdsForecast28 = new TimeSlotErdsForecast(8);
		timeSlotErdsForecast28.setCo2Factor(Amount.valueOf(50, Units.KG_PER_KWH));
		timeSlotErdsForecast28.setPower(Amount.valueOf(940, WATT));
		timeSlotErdsForecast28.setRenewablePercentage(Amount.valueOf(41, PERCENT));
		timeSlotForecasts2.add(timeSlotErdsForecast28);
		erds2Forecast.setTimeSlotForecasts(timeSlotForecasts2);

		List<ErdsForecast> erdsForecastList = new ArrayList<>();
		erdsForecastList.add(erds1Forecast);
		erdsForecastList.add(erds2Forecast);
		List<Objective> objectivesList = new ArrayList<>();
		Objective energyPropertyObjective1 = prepareEnergyPropertyObjective(dateFrom, 80, "gt", "PT24H");
		objectivesList.add(energyPropertyObjective1);
		idealPowerPlanner.setEnergyConfiguration(objectivesList);
		System.out.println("1st run");
		PowerPlan newPowerPlan = idealPowerPlanner.calculateIdealPowerPlan(timeRange, erdsForecastList, currentPowerPlan);
		System.out.println("1st run: " + newPowerPlan);

	}

	private Objective preparePowerObjective(DateTime dateFrom, int power, int priority, String operator, String duration) {
		Objective objective = new Objective();
		objective.setCreationDate(new Date());
		objective.setDataCenterId("0");
		objective.setDescription("TestObjective");
		objective.setEnabled(true);
		objective.setId("0");
		objective.setImplementationType(ImplementationType.MUST);
		objective.setLastModified(new Date());
		objective.setName("TestObjective");
		objective.setPriority(priority);
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

	private Objective prepareEnergyPropertyObjective(DateTime dateFrom, int percent, String operator, String duration) {
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
		objective.setType(ObjectiveType.ENERGY_PROPERTY);
		Target target = new Target();
		target.setMetric("%");
		target.setOperator("gt");
		target.setValue(percent);
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
			timeSlotErdsForecast.setCo2Factor(Amount.valueOf(cef, Units.KG_PER_KWH));
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
}
