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
import org.apache.log4j.Logger;
import org.jscience.physics.amount.Amount;
import org.springframework.stereotype.Component;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import java.util.*;

import static javax.measure.unit.NonSI.PERCENT;
import static javax.measure.unit.SI.*;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

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
 * It uses aggressiveness factor to increase the power consumption during time slots, when %REN is high
 * and decrease when %REN is low
 */

@Component
public class AggressivAdaptPowerPlannerImpl implements PowerPlanner,
PowerConfigAware, ProcessConfigAware {

	private int DCMaxPower;

	private int DCMinPower;

	// Alpha : Aggressiveness factor
	private double Alpha=1;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AggressivAdaptPowerPlannerImpl.class);

	private List<Objective> energyConfiguration = new ArrayList<>();

	public AggressivAdaptPowerPlannerImpl() {
	}

	@Override
	public PowerPlan calculateIdealPowerPlan(TimeSlotBasedEntity timeRange,
			List<ErdsForecast> erdsForecasts, PowerPlan currentPowerPlan) {
		List<Objective> powerObjectivesList = new ArrayList<>();
		List<Objective> energyObjectivesList = new ArrayList<>();
		List<Objective> energyPropertyObjectivesList = new ArrayList<>();
		//logger.debug("timeRange"+ timeRange);
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

		// If no current power plan available, assume the smallest amount between (maximum power usage, DCMaxPower)
		//on all slots
		if (currentPowerPlan == null
				|| currentPowerPlan.getPowerQuotas() == null) {
			currentPowerPlan = new PowerPlan();
			currentPowerPlan.setDateFrom(timeRange.getDateFrom());
			currentPowerPlan.setDateTo(timeRange.getDateTo());
			currentPowerPlan.setTimeSlotDuration(timeRange
					.getTimeSlotDuration());
			List<TimeSlotPower> quotas = new ArrayList<>();
			for (int i = 0; i < numberOfSlots; i++) {
				TimeSlotPower timeSlotPower = new TimeSlotPower(i);
				Amount<Power> maxSlotPower = Amount.valueOf(0, WATT);
				for (ErdsForecast currentErdsForecast : erdsForecasts) {
					maxSlotPower = maxSlotPower.plus(currentErdsForecast
							.getTimeSlotForecasts().get(i).getPower());
				}
				if(maxSlotPower.isGreaterThan(Amount.valueOf(DCMaxPower, WATT))){
					maxSlotPower=Amount.valueOf(DCMaxPower, WATT);
				}

				timeSlotPower.setPower(maxSlotPower);
				quotas.add(timeSlotPower);
			}
			currentPowerPlan.setPowerQuotas(quotas);
			//logger.debug("Forcasts "+ erdsForecasts.toString());
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

		PowerPlan renPctOptimizedPowerPlan = new PowerPlan();
		renPctOptimizedPowerPlan = aggressiveAdapt(currentPowerPlan, erdsForecasts,
				currentPowerPlanTotalEnergy);

		return renPctOptimizedPowerPlan;
	}

	private PowerPlan aggressiveAdapt(PowerPlan powerPlan,
			List<ErdsForecast> erdsForecasts,
			Amount<Energy> currentPowerPlanTotalEnergy) {
		int numOfSlotsForecasts=erdsForecasts.get(0).getTimeSlotForecasts().size();		
		List<TimeSlotPower> powerQuotas = powerPlan.getPowerQuotas();
		TreeMap<Integer, Amount<Power>> localRenwablesTreeMap = new TreeMap<Integer, Amount<Power>>();
		int numberOfSlots = powerQuotas.size();	
		int shift=numOfSlotsForecasts-numberOfSlots;

		Integer [] renPercentages= new Integer[numberOfSlots];
		int i = 0;
		i = 0;

		Amount<Power> powerAVG1=Amount.valueOf(0, WATT);
		i = 0;
		while (i < numberOfSlots) {
			Amount<Power> currentSlotPower = powerQuotas.get(i).getPower();	
			powerAVG1=powerAVG1.plus(currentSlotPower);
			i++;
		}	

		//logger.debug("powerAVG1 "+ powerAVG1);

		//--- only scaling the amounts which are above of DCMinPower--//

		List<TimeSlotPower> newPowerQuotas1 = new ArrayList<>();
		i=0;
		while (i < numberOfSlots) {
			Amount<Power> currentSlotPower= powerQuotas.get(i).getPower();
			TimeSlotPower currentTimeSlotPower = new TimeSlotPower(i);
			currentTimeSlotPower.setPower(currentSlotPower.minus(Amount.valueOf(DCMinPower, WATT)));
			newPowerQuotas1.add(currentTimeSlotPower);
			i++;
		}
		powerPlan.setPowerQuotas(newPowerQuotas1);
		powerQuotas = powerPlan.getPowerQuotas();
		numberOfSlots = powerQuotas.size();
		//System.out.println(powerPlan);

		//---End--//

		float renPercAvg=0;
		/* find
		 * The RenAvg
		 * Array of RenPercentage in every time slot
		 * Amount of available local renewables in each time slot 
		 * 
		 */
		i=0;
		while (i < numberOfSlots) {
			Amount<Power> localRenwables = Amount.valueOf(0, WATT);
			long timeSlotRenewablePercentage=0;
			for (ErdsForecast currentErdsForecast : erdsForecasts) {
				Amount<Power> currentPower=Amount.valueOf(0, WATT);
				long currentRenewablePercentage = currentErdsForecast
						.getTimeSlotForecasts().get(i+shift)
						.getRenewablePercentage().longValue(PERCENT);
				if (currentRenewablePercentage== 100){
					currentPower = currentErdsForecast
							.getTimeSlotForecasts().get(i+shift).getPower();
					localRenwables=localRenwables.plus(currentPower);
				}
				else if(currentRenewablePercentage > timeSlotRenewablePercentage) {
					timeSlotRenewablePercentage=currentRenewablePercentage;
				}
			}
			if(localRenwables.isGreaterThan(Amount.valueOf(DCMaxPower, WATT)))
				localRenwablesTreeMap.put(i, Amount.valueOf(DCMaxPower, WATT));
			else 
				localRenwablesTreeMap.put(i, localRenwables);
			renPercentages[i]=(int)timeSlotRenewablePercentage;
			renPercAvg+=timeSlotRenewablePercentage;
			i++;
		}

		renPercAvg/=numberOfSlots;

		//--subtract local renewables from current power plan--//

		//logger.debug("localRenwablesTreeMap "+ localRenwablesTreeMap.toString());
		TreeMap<Integer, Amount<Power>> powerDiff = new TreeMap<Integer, Amount<Power>>();
		i = 0;
		while (i < numberOfSlots) {
			//Amount<Power> currentLocalRen = localRenwablesTreeMap.get(i);
			Amount<Power> currentSlotPower= powerQuotas.get(i).getPower();
			//Amount<Power> timeSlotPowerDiff = currentSlotPower.minus(currentLocalRen);
			//if (timeSlotPowerDiff.isLessThan(Amount.valueOf(0, WATT)))
			//timeSlotPowerDiff=Amount.valueOf(0, WATT);
			//powerDiff.put(i, Amount.valueOf((long)timeSlotPowerDiff.getMaximumValue(), WATT));
			powerDiff.put(i, currentSlotPower);
			i++;
		}		
		//---End--//


		//-- calculating  powerAVG --//

		Amount<Power> powerAVG=Amount.valueOf(0, WATT);
		i = 0;
		while (i < numberOfSlots) {
			Amount<Power> currentSlotPower = powerDiff.get(i);
			//Amount<Power> scaledcurrentSlotPower= currentSlotPower.times(renPercentages[i]/renPercAvg);			
			powerAVG=powerAVG.plus(currentSlotPower);
			//powerDiff.put(i, Amount.valueOf((long) scaledcurrentSlotPower.getMaximumValue(), WATT));
			i++;
		}

		Amount<Power> totalPowerDiff = Amount.valueOf((long)powerAVG.getMaximumValue(),WATT);			
		powerAVG=Amount.valueOf((long)powerAVG.divide(numberOfSlots).getMaximumValue(),WATT);

		//---End--//

		//logger.debug("Poweravg "+ powerAVG.toString());

		//-- scaling powerDiff using aggressiveness factor (alpha) --//

		i = 0;
		Amount<Power> totalPowerScaledDiff = Amount.valueOf(0, WATT);

		while (i < numberOfSlots) {
			/*double adaptingFactor=Math.pow(Math.abs(renPercentages[i]-renPercAvg)*0.01,1/Alpha);
			Amount<Power> aggscaledcurrentSlotPower= powerAVG.plus(powerAVG.times(Math.signum(renPercentages[i]-renPercAvg)*adaptingFactor));
			powerDiff.put(i, Amount.valueOf((long) aggscaledcurrentSlotPower.getMaximumValue(), WATT));*/
			double adaptingFactor=1+ Alpha*(renPercentages[i]-renPercAvg)*0.01;
			Amount<Power> aggscaledcurrentSlotPower=Amount.valueOf(Math.pow(powerDiff.get(i).getMaximumValue(),adaptingFactor),WATT);
			if (aggscaledcurrentSlotPower.isGreaterThan(Amount.valueOf(DCMaxPower, WATT))){
				powerDiff.put(i, Amount.valueOf(DCMaxPower, WATT));
			}else{
				powerDiff.put(i, aggscaledcurrentSlotPower);
			}

			totalPowerScaledDiff=totalPowerScaledDiff.plus(powerDiff.get(i));
			i++;
		}

		//logger.debug("Aggressive Scaled powerDiff"+ powerDiff.toString());

		//---End--//


		double scaleFactor=(double)totalPowerDiff.longValue(WATT)/(double)totalPowerScaledDiff.longValue(WATT);

		i = 0;
		Amount<Power> totalPowerScaledDiff1 = Amount.valueOf(0, WATT);
		while (i < numberOfSlots) {
			Amount<Power> currentSlotPower = powerDiff.get(i);	
			currentSlotPower= currentSlotPower.times(scaleFactor);
			powerDiff.put(i, Amount.valueOf((long) currentSlotPower.getMaximumValue(), WATT));
			totalPowerScaledDiff1=totalPowerScaledDiff1.plus(powerDiff.get(i));
			i++;
		}


		/*powerAVG=Amount.valueOf(0, WATT);
		i = 0;
		while (i < numberOfSlots) {
			Amount<Power> currentSlotPower = powerDiff.get(i);
			powerAVG=powerAVG.plus(currentSlotPower).plus(Amount.valueOf(DCMinPower, WATT));
			i++;
		}	
		 */

		i = 0;
		List<TimeSlotPower> newPowerQuotas = new ArrayList<>();

		while (i < numberOfSlots) {
			Amount<Power> powerValue = powerDiff.get(i);
			TimeSlotPower currentTimeSlotPower = new TimeSlotPower(i);
			currentTimeSlotPower.setPower(powerValue.plus(Amount.valueOf(DCMinPower, WATT)));
			newPowerQuotas.add(currentTimeSlotPower);
			i++;
		}

		powerPlan.setPowerQuotas(newPowerQuotas);


		powerAVG=Amount.valueOf(0, WATT);
		i = 0;
		while (i < numberOfSlots) {
			Amount<Power> currentSlotPower = newPowerQuotas.get(i).getPower();	
			powerAVG=powerAVG.plus(currentSlotPower);
			i++;
		}	

		//logger.debug("powerAVG new plan"+powerAVG);
		return optimizePowerPlan(powerPlan,renPercentages,localRenwablesTreeMap);
	}


	/* 
	 * check if we have some values bigger than DCMaxPower or smaller than DCMinPower
	 * and then optimize the power plan by distributing the surplus power on other time slots
	 */

	private PowerPlan optimizePowerPlan(PowerPlan powerPlan,Integer [] renPercentages, TreeMap<Integer, Amount<Power>> localRenwablesTreeMap) {

		List<TimeSlotPower> powerQuotas = powerPlan.getPowerQuotas();
		int numberOfSlots = powerQuotas.size();
		Amount<Power> surplusPower=Amount.valueOf(0, WATT);
		Amount<Power> neededPower=Amount.valueOf(0, WATT);
		Amount<Power> DCMax=Amount.valueOf(DCMaxPower, WATT);
		Amount<Power> DCMin=Amount.valueOf(DCMinPower, WATT);

		//-- Optimization phase 1  --//

		for (int i = 0; i < numberOfSlots; i++) {
			TimeSlotPower currentTimeSlotPower = powerQuotas.get(i);
			if (localRenwablesTreeMap.get(i).isGreaterThan(currentTimeSlotPower.getPower())) {
				Amount<Power> add=localRenwablesTreeMap.get(i).minus(currentTimeSlotPower.getPower());
				Amount<Power> newValue=currentTimeSlotPower.getPower().plus(add);
				if(newValue.isLessThan(DCMax)){
					surplusPower = surplusPower.plus(add);
					currentTimeSlotPower.setPower(newValue);
				}
				else{
					surplusPower = surplusPower.plus(DCMax.minus(currentTimeSlotPower.getPower()));
					currentTimeSlotPower.setPower(DCMax);
				}
			}
			powerQuotas.set(i, currentTimeSlotPower);
		}		
		//logger.debug("surplusPower"+ surplusPower);

		/* if (surplusPower.longValue(WATT)==0){
				powerPlan.setPowerQuotas(powerQuotas);
				return powerPlan;
				}*/

		ArrayIndexComparator comparator = new ArrayIndexComparator(renPercentages);
		Integer[] indexes = comparator.createIndexArray();
		Arrays.sort(indexes, comparator);

		/*logger.debug(Arrays.toString(indexes));
			//logger.debug("ammar");
				for (int i =0 ; i <= indexes.length-1; i++){
					TimeSlotPower currentTimeSlotPower = powerQuotas.get(indexes[i]);
					Amount<Power> diff=currentTimeSlotPower.getPower().minus(DCMin);
					if (diff.isGreaterThan(Amount.valueOf(0, WATT))) {
						if(diff.isLessThan(surplusPower)){
							currentTimeSlotPower.setPower(DCMin);
							surplusPower=surplusPower.minus(diff);
						}
						else {
							currentTimeSlotPower.setPower(currentTimeSlotPower.getPower().minus(surplusPower));
							surplusPower=Amount.valueOf(0, WATT);					
							break;
						}		
					}

					powerQuotas.set(indexes[i], currentTimeSlotPower);
				}*/

		/**** End  ****/

		/**** optimization phase 2  ****/
		surplusPower=Amount.valueOf(0, WATT);
		for (int i = 0; i < numberOfSlots; i++) {
			TimeSlotPower currentTimeSlotPower = powerQuotas.get(i);
			if (currentTimeSlotPower.getPower().isGreaterThan(DCMax)) {
				surplusPower = surplusPower.plus(currentTimeSlotPower.getPower().minus(DCMax));
				currentTimeSlotPower.setPower(DCMax);		
			}
			if (currentTimeSlotPower.getPower().isLessThan(DCMin)) {
				neededPower = neededPower.plus(DCMin.minus(currentTimeSlotPower.getPower()));
				currentTimeSlotPower.setPower(DCMin);		
			}
			powerQuotas.set(i, currentTimeSlotPower);
		}

		surplusPower=surplusPower.minus(neededPower);	
		if (surplusPower.longValue(WATT)==0){
			powerPlan.setPowerQuotas(powerQuotas);	
			return powerPlan;
		}

		if (surplusPower.isLessThan(Amount.valueOf(0, WATT))){	
			for (int i =0 ; i <indexes.length; i++){
				TimeSlotPower currentTimeSlotPower = powerQuotas.get(indexes[i]);
				Amount<Power> diff=currentTimeSlotPower.getPower().minus(DCMin);
				if (diff.isGreaterThan(Amount.valueOf(0, WATT))) {
					if(diff.isLessThan(surplusPower.abs())){
						currentTimeSlotPower.setPower(DCMin);
						surplusPower=surplusPower.plus(diff);
					}
					else {
						currentTimeSlotPower.setPower(currentTimeSlotPower.getPower().plus(surplusPower));
						surplusPower=Amount.valueOf(0, WATT);					
						break;
					}		
				}

				powerQuotas.set(indexes[i], currentTimeSlotPower);
			}
		}
		else 
		{

			for (int i =indexes.length-1 ; i >= 0; --i){
				TimeSlotPower currentTimeSlotPower = powerQuotas.get(indexes[i]);
				Amount<Power> diff=DCMax.minus(currentTimeSlotPower.getPower());
				if ((diff.isGreaterThan(Amount.valueOf(0, WATT))) && ((diff.isLessThan(DCMax.minus(DCMin))))) {
					if(diff.isLessThan(surplusPower)){
						currentTimeSlotPower.setPower(DCMax);
						surplusPower=surplusPower.minus(diff);
					}
					else {
						currentTimeSlotPower.setPower(currentTimeSlotPower.getPower().plus(surplusPower));
						surplusPower=Amount.valueOf(0, WATT);					
						break;
					}		
				}

				powerQuotas.set(indexes[i], currentTimeSlotPower);
			}

		}
		/**** End  ****/

		/**** calculate Energy in Power plan ***/

		/*		Amount<Power> powerAVG=Amount.valueOf(0, WATT);
		int i = 0;
		while (i < numberOfSlots) {
			Amount<Power> currentSlotPower = powerQuotas.get(i).getPower();	
			powerAVG=powerAVG.plus(currentSlotPower);
			i++;
		}	*/

		/*** End ***/

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

	public double getAlpha() {
		return Alpha;
	}

	public void setAlpha (double alpha) {
		Alpha = alpha;
	}

}

class ArrayIndexComparator implements Comparator<Integer>
{
	private final Integer[] array;

	public ArrayIndexComparator(Integer[] array)
	{
		this.array = array;
	}

	public Integer[] createIndexArray()
	{
		Integer[] indexes = new Integer[array.length];
		for (int i = 0; i < array.length; i++)
		{
			indexes[i] = i; // Autoboxing
		}
		return indexes;
	}

	@Override
	public int compare(Integer index1, Integer index2)
	{
		// Autounbox from Integer to int to use as array indexes
		return array[index1].compareTo(array[index2]);
	}
}