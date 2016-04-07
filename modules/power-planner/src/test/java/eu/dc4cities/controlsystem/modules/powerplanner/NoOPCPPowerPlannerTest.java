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
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.model.erds.TimeSlotErdsForecast;
import eu.dc4cities.controlsystem.model.unit.Units;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static javax.measure.unit.NonSI.PERCENT;
import static javax.measure.unit.SI.WATT;

/**
 *
 */

public class NoOPCPPowerPlannerTest {

	/**
	@Test
	public void testPowerPlannerNoCurrentPlan() {
		NoOPCPPowerPlannerImpl idealPowerPlanner = new NoOPCPPowerPlannerImpl();
		Amount<Duration> timeSlotDuration = Amount.valueOf(15, MINUTE);
		DateTime dateFrom1 = new DateTime(2014, 6, 1, 12, 0, 0, 0);
		DateTime dateTo = new DateTime(2014, 6, 2, 12, 0, 0, 0);
		long timeDiff = dateTo.getMillis() - dateFrom1.getMillis();
		int timeSlotNumber = (int) (timeDiff / timeSlotDuration.longValue(MILLI(SECOND)));
		TimeSlotBasedEntity timeRange = new TimeSlotBasedEntity();
		timeRange.setDateFrom(dateFrom1);
		timeRange.setDateTo(dateTo);
		timeRange.setTimeSlotDuration(timeSlotDuration);
		ErdsForecast renForecast = prepareConstantForecast(10000, 0.2, 0, 0, 100, 0, timeSlotNumber, dateFrom1, dateTo, timeSlotDuration, "ren");
		ErdsForecast gridForecast = prepareConstantForecast(Integer.MAX_VALUE, 0, 100, 0.2, 20, 0.5, timeSlotNumber, dateFrom1, dateTo, timeSlotDuration,
				"grid");
		List<ErdsForecast> erdsForecastList = new ArrayList<>();
		erdsForecastList.add(renForecast);
		erdsForecastList.add(gridForecast);
		List<Objective> objectivesList = new ArrayList<>();
		Objective powerObjective1 = preparePowerObjective(dateFrom1, 20000, 0, "lt", "P1M");
		Objective powerObjective2 = preparePowerObjective(dateFrom1, 5000, 1, "lt", "PT2H");
		Objective powerObjective3 = preparePowerObjective(dateFrom1, 1000, 2, "lt", "PT30M");
		Objective energyObjective1 = prepareEnergyObjective(dateFrom1, 30000, "lt", "PT5H");
		Objective energyPropertyObjective1 = prepareEnergyPropertyObjective(dateFrom1, 80, "gt", "PT24H");
		objectivesList.add(powerObjective1);
		objectivesList.add(powerObjective2);
		objectivesList.add(powerObjective3);
		objectivesList.add(energyObjective1);
		objectivesList.add(energyPropertyObjective1);
		idealPowerPlanner.setEnergyConfiguration(objectivesList);
		PowerPlan newPowerPlan = idealPowerPlanner.calculateIdealPowerPlan(timeRange, erdsForecastList, null);

		System.out.println("1st run: " + newPowerPlan);

		renForecast = prepareConstantForecast(9000, 0.2, 0, 0, 100, 0, timeSlotNumber, dateFrom1, dateTo, timeSlotDuration, "ren");
		erdsForecastList = new ArrayList<>();
		erdsForecastList.add(renForecast);
		erdsForecastList.add(gridForecast);
		timeRange.setDateFrom(dateFrom1.plusMinutes(15));
		timeRange.setDateTo(dateTo.plusMinutes(15));

		PowerPlan newerPowerPlan = idealPowerPlanner.calculateIdealPowerPlan(timeRange, erdsForecastList, newPowerPlan);

		System.out.println("2nd run: " + newerPowerPlan);

		renForecast = prepareConstantForecast(8000, 0.2, 0, 0, 100, 0, timeSlotNumber, dateFrom1, dateTo, timeSlotDuration, "ren");
		erdsForecastList = new ArrayList<>();
		erdsForecastList.add(renForecast);
		erdsForecastList.add(gridForecast);
		timeRange.setDateFrom(dateFrom1.plusMinutes(30));
		timeRange.setDateTo(dateTo.plusMinutes(30));

		newerPowerPlan = idealPowerPlanner.calculateIdealPowerPlan(timeRange, erdsForecastList, newPowerPlan);

		System.out.println("3rd run: " + newerPowerPlan);

		renForecast = prepareConstantForecast(7000, 0.2, 0, 0, 100, 0, timeSlotNumber, dateFrom1, dateTo, timeSlotDuration, "ren");
		erdsForecastList = new ArrayList<>();
		erdsForecastList.add(renForecast);
		erdsForecastList.add(gridForecast);
		timeRange.setDateFrom(dateFrom1.plusMinutes(45));
		timeRange.setDateTo(dateTo.plusMinutes(45));

		newerPowerPlan = idealPowerPlanner.calculateIdealPowerPlan(timeRange, erdsForecastList, newPowerPlan);

		System.out.println("4th run: " + newerPowerPlan);

		renForecast = prepareConstantForecast(6000, 0.2, 0, 0, 100, 0, timeSlotNumber, dateFrom1, dateTo, timeSlotDuration, "ren");
		erdsForecastList = new ArrayList<>();
		erdsForecastList.add(renForecast);
		erdsForecastList.add(gridForecast);
		timeRange.setDateFrom(dateFrom1.plusMinutes(60));
		timeRange.setDateTo(dateTo.plusMinutes(60));

		newerPowerPlan = idealPowerPlanner.calculateIdealPowerPlan(timeRange, erdsForecastList, newPowerPlan);

		System.out.println("5th run: " + newerPowerPlan);

		renForecast = prepareConstantForecast(5000, 0.2, 0, 0, 100, 0, timeSlotNumber, dateFrom1, dateTo, timeSlotDuration, "ren");
		erdsForecastList = new ArrayList<>();
		erdsForecastList.add(renForecast);
		erdsForecastList.add(gridForecast);
		timeRange.setDateFrom(dateFrom1.plusMinutes(75));
		timeRange.setDateTo(dateTo.plusMinutes(75));

		newerPowerPlan = idealPowerPlanner.calculateIdealPowerPlan(timeRange, erdsForecastList, newPowerPlan);

		System.out.println("6th run: " + newerPowerPlan);

	}
	
	
	@Test
	public void largeInputPowerPlannerNoCurrentPlan() {
		NoOPCPPowerPlannerImpl idealPowerPlanner = new NoOPCPPowerPlannerImpl();
		Amount<Duration> timeSlotDuration = Amount.valueOf(15, MINUTE);
		DateTime dateFrom1 = new DateTime(2014, 7, 24, 12, 0, 0, 0);
		DateTime dateTo = new DateTime(2014, 7, 24, 14, 0, 0, 0);
		long timeDiff = dateTo.getMillis() - dateFrom1.getMillis();
		int timeSlotNumber = (int) (timeDiff / timeSlotDuration.longValue(MILLI(SECOND)));
		TimeSlotBasedEntity timeRange = new TimeSlotBasedEntity();
		timeRange.setDateFrom(dateFrom1);
		timeRange.setDateTo(dateTo);
		timeRange.setTimeSlotDuration(timeSlotDuration);
		PowerPlan currentPowerPlan = new PowerPlan();
		currentPowerPlan.setDateFrom(dateFrom1.minusMinutes(15));
		currentPowerPlan.setDateTo(currentPowerPlan.getDateFrom().plusHours(1));
		currentPowerPlan.setTimeSlotDuration(timeSlotDuration);
		List<TimeSlotPower> powerQuotas = new ArrayList<>();
		TimeSlotPower slot1 = new TimeSlotPower(1);
		slot1.setPower(Amount.valueOf(37548800000L,WATT));
		powerQuotas.add(slot1);
		TimeSlotPower slot2 = new TimeSlotPower(2);
		slot2.setPower(Amount.valueOf(37548800000L,WATT));
		powerQuotas.add(slot2);
		TimeSlotPower slot3 = new TimeSlotPower(3);
		slot3.setPower(Amount.valueOf(37548800000L,WATT));
		powerQuotas.add(slot3);
		TimeSlotPower slot4 = new TimeSlotPower(4);
		slot4.setPower(Amount.valueOf(37548800000L,WATT));
		powerQuotas.add(slot4);
		currentPowerPlan.setPowerQuotas(powerQuotas);
		ErdsForecast renForecast = prepareConstantForecast(20000, 0.5, 0, 0, 100, 0, timeSlotNumber, dateFrom1, dateTo, timeSlotDuration, "ren");
		ErdsForecast gridForecast = prepareConstantForecast(37548800000L, 0, 100, 0.2, 20, 0.5, timeSlotNumber, dateFrom1, dateTo, timeSlotDuration,
				"grid");
		List<ErdsForecast> erdsForecastList = new ArrayList<>();
		erdsForecastList.add(renForecast);
		erdsForecastList.add(gridForecast);
		List<Objective> objectivesList = new ArrayList<>();
//		Objective powerObjective1 = preparePowerObjective(dateFrom1, 20000, 0, "lt", "P1M");
//		Objective powerObjective2 = preparePowerObjective(dateFrom1, 5000, 1, "lt", "PT2H");
//		Objective powerObjective3 = preparePowerObjective(dateFrom1, 1000, 2, "lt", "PT30M");
//		Objective energyObjective1 = prepareEnergyObjective(dateFrom1, 30000, "lt", "PT5H");
		Objective energyPropertyObjective1 = prepareEnergyPropertyObjective(dateFrom1, 80, "gt", "PT24H");
//		objectivesList.add(powerObjective1);
//		objectivesList.add(powerObjective2);
//		objectivesList.add(powerObjective3);
//		objectivesList.add(energyObjective1);
		objectivesList.add(energyPropertyObjective1);
		idealPowerPlanner.setEnergyConfiguration(objectivesList);
		PowerPlan newPowerPlan = idealPowerPlanner.calculateIdealPowerPlan(timeRange, erdsForecastList, null);

		System.out.println("1st run: " + newPowerPlan);
		
		renForecast = prepareConstantForecast(15000, 0.2, 0, 0, 100, 0, timeSlotNumber, dateFrom1.plusMinutes(15), dateTo.plusMinutes(15), timeSlotDuration, "ren");
		erdsForecastList = new ArrayList<>();
		erdsForecastList.add(renForecast);
		gridForecast.setDateFrom(dateFrom1.plusMinutes(15));
		gridForecast.setDateTo(dateTo.plusMinutes(15));
		erdsForecastList.add(gridForecast);
		timeRange.setDateFrom(dateFrom1.plusMinutes(15));
		timeRange.setDateTo(dateTo.plusMinutes(15));

		PowerPlan newerPowerPlan = idealPowerPlanner.calculateIdealPowerPlan(timeRange, erdsForecastList, newPowerPlan);

		System.out.println("2nd run: " + newerPowerPlan);
		
		renForecast = prepareConstantForecast(50000, 0.2, 0, 0, 100, 0, timeSlotNumber, dateFrom1.plusMinutes(15), dateTo.plusMinutes(15), timeSlotDuration, "ren");
		erdsForecastList = new ArrayList<>();
		erdsForecastList.add(renForecast);
		gridForecast.setDateFrom(dateFrom1.plusMinutes(30));
		gridForecast.setDateTo(dateTo.plusMinutes(30));
		erdsForecastList.add(gridForecast);
		timeRange.setDateFrom(dateFrom1.plusMinutes(30));
		timeRange.setDateTo(dateTo.plusMinutes(30));

		newerPowerPlan = idealPowerPlanner.calculateIdealPowerPlan(timeRange, erdsForecastList, newPowerPlan);

		System.out.println("3rd run: " + newerPowerPlan);
	}
	**/
	

//	TimeSlotErdsForecast [renewablePercentage=30 %, carbonEmissions=374 g/(kW·h), getPower()=35929400000 W],
//	TimeSlotErdsForecast [renewablePercentage=30 %, carbonEmissions=371 g/(kW·h), getPower()=36334250000 W], 
//	TimeSlotErdsForecast [renewablePercentage=31 %, carbonEmissions=368 g/(kW·h), getPower()=36739100000 W], 
//	TimeSlotErdsForecast [renewablePercentage=32 %, carbonEmissions=365 g/(kW·h), getPower()=37143950000 W], 
//	TimeSlotErdsForecast [renewablePercentage=33 %, carbonEmissions=362 g/(kW·h), getPower()=37548800000 W]],dateFrom=2014-07-24T10:00:00.000+02:00,dateTo=2014-07-24T11:00:00.000+02:00,timeSlotDuration=15 min]";

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
