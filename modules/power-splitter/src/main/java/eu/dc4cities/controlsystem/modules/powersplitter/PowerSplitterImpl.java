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

package eu.dc4cities.controlsystem.modules.powersplitter;

import eu.dc4cities.configuration.technical.EascGroup;
import eu.dc4cities.configuration.technical.EascWeight;
import eu.dc4cities.controlsystem.model.PowerPlan;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.TimeSlotPower;
import eu.dc4cities.controlsystem.model.easc.DataCenterPowerPlan;
import eu.dc4cities.controlsystem.model.easc.EascPowerPlan;
import eu.dc4cities.controlsystem.model.easc.EnergyQuota;
import eu.dc4cities.controlsystem.model.unit.Units;
import eu.dc4cities.controlsystem.model.util.TimeRangeUtils;
import eu.dc4cities.controlsystem.modules.PowerSplitter;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import java.util.*;

/**
 * Splits power assigned to the data center among EASCs, using a 2-level algorithm. First, power available in each time
 * slot is split among EASC groups based on group weights. Then, within each group, each EASC is assigned a quota of the
 * total energy (for each day in the time interval) using its weight in the group. Thus every EASC can use up to the
 * peak power for the group in each time slot, while respecting the upper bound on total energy usage for the day.
 * This enables better distribution of work in the power curve among EASCs.
 */
public class PowerSplitterImpl implements PowerSplitter {
	
	private String dataCenterName;
    private List<EascGroup> eascGroups;
    
    /**
     * Creates a new power splitter.
     * 
     * @param dataCenterName the name of the data center for which the splitting is done
     * @param eascGroups the EASC group configuration with weights used for splitting
     */
    public PowerSplitterImpl(String dataCenterName, List<EascGroup> eascGroups) {
    	this.dataCenterName = dataCenterName;
    	this.eascGroups = eascGroups;
    }
    
	@Override
	public List<EascPowerPlan> splitPowerForEasc(PowerPlan idealPowerPlan) {
		if (eascGroups == null || eascGroups.size() == 0) {
        	throw new RuntimeException("Missing EASC group configuration");
        }
        int numOfGroups = eascGroups.size();
        int groupWeights[] = new int[numOfGroups];
        int totalGroupWeight = 0;
        PowerPlan[] groupPlans = new PowerPlan[numOfGroups];
        for (int g = 0; g < numOfGroups; g++) {
        	groupWeights[g] = eascGroups.get(g).getGroupWeight();
        	totalGroupWeight += groupWeights[g];
        	groupPlans[g] = new PowerPlan(idealPowerPlan);
        }
        List<DayBoundaries> days = getDayBoundaries(idealPowerPlan);
        long groupPowerSums[][] = new long[numOfGroups][days.size()];
        List<TimeSlotPower> idealQuotas = idealPowerPlan.getPowerQuotas();
        int numOfTimeSlots = idealQuotas.size();
        int day = 0;
        int dayEndTimeSlot = days.get(0).endTimeSlot;
        for (int t = 0; t < numOfTimeSlots; t++) {
        	if (t >= dayEndTimeSlot) {
        		day++;
        		dayEndTimeSlot = days.get(day).endTimeSlot;
        	}
        	long idealPower = idealQuotas.get(t).getPower().longValue(SI.WATT);
        	long allocatedPower = 0;
        	for (int g = 0; g < numOfGroups; g++) {
        		long groupPower;
        		if (g < numOfGroups - 1) {
        			groupPower = Math.round(idealPower * (double) groupWeights[g] / totalGroupWeight);
        			allocatedPower =+ groupPower;
        		} else {
        			// Assign the remaining power to the last group to accommodate for rounding in previous groups
        			groupPower = idealPower - allocatedPower;
        		}
        		Amount<Power> groupPowerAmount = Amount.valueOf(groupPower, SI.WATT);
        		groupPlans[g].getPowerQuotas().get(t).setPower(groupPowerAmount);
        		groupPowerSums[g][day] += groupPower;
        	}
        }
        List<EascPowerPlan> eascPlans = new LinkedList<>();
        int timeSlotWidth = (int) idealPowerPlan.getTimeSlotDuration().longValue(NonSI.MINUTE);
        for (int g = 0; g < numOfGroups; g++) {
        	List<EascWeight> eascList = eascGroups.get(g).getEascWeights();
        	int numOfEascs = eascList.size();
        	int eascWeights[] = new int[numOfEascs];
        	int totalEascWeight = 0;
        	for (int e = 0; e < numOfEascs; e++) {
        		eascWeights[e] = eascList.get(e).getWeight();
            	totalEascWeight += eascWeights[e];
        	}
        	Map<String, List<EnergyQuota>> eascQuotasMap = new HashMap<>();
        	for (int d = 0; d < days.size(); d++) {
        		DayBoundaries dayBoundaries = days.get(d);
        		int dayStartSlot = dayBoundaries.startTimeSlot;
        		int dayEndSlot = dayBoundaries.endTimeSlot;
        		int slotsInDay = dayEndSlot - dayStartSlot;
        		double hoursInDay = (double) slotsInDay * timeSlotWidth / 60;
        		double averageGroupPower = (double) groupPowerSums[g][d] / slotsInDay;
        		double groupEnergy = averageGroupPower * hoursInDay;
        		long allocatedEnergy = 0;
            	for (int e = 0; e < numOfEascs; e++) {
            		long eascEnergy;
            		if (e < numOfEascs - 1) {
            			eascEnergy = Math.round(groupEnergy * eascWeights[e] / totalEascWeight);
            			allocatedEnergy =+ eascEnergy;
            		} else {
            			// Assign the remaining energy to the last EASC to accommodate for rounding in previous EASCs.
            			// Round to the floor because groupEnergy might not be an integer and we should not allocate
            			// more energy than available by rounding up.
            			eascEnergy = (long) Math.floor(groupEnergy - allocatedEnergy);
            		}
            		Amount<Energy> eascEnergyAmount = Amount.valueOf(eascEnergy, Units.WATT_HOUR);
            		EnergyQuota energyQuota = new EnergyQuota(dayStartSlot, dayEndSlot, eascEnergyAmount);
            		String eascName = eascList.get(e).getEascName();
            		addEnergyQuota(eascQuotasMap, eascName, energyQuota);
            	}
        	}
        	for (EascWeight easc : eascList) {
        		PowerPlan groupPlan = groupPlans[g];
        		String eascName = easc.getEascName();
        		EascPowerPlan eascPlan = new EascPowerPlan(eascName);
        		eascPlan.copyIntervalFrom(groupPlan);
        		DataCenterPowerPlan dcPlan = new DataCenterPowerPlan(dataCenterName);
        		dcPlan.setEnergyQuotas(eascQuotasMap.get(eascName));
        		dcPlan.setPowerQuotas(groupPlan.getPowerQuotas());
        		eascPlan.setDataCenterQuotas(Arrays.asList(dcPlan));
        		eascPlans.add(eascPlan);
        	}
        }
		return eascPlans;
	}

	private List<DayBoundaries> getDayBoundaries(TimeSlotBasedEntity timeRange) {
		DateTime dateFrom = timeRange.getDateFrom();
		DateTime dateTo = timeRange.getDateTo();
		int timeSlotWidth = (int) timeRange.getTimeSlotDuration().longValue(NonSI.MINUTE);
		DateTime startDate = dateFrom;
		int startTimeSlot = 0;
		List<DayBoundaries> days = new LinkedList<>();
		while (startDate.isBefore(dateTo)) {
			DateTime nextDay = startDate.withTimeAtStartOfDay().plusDays(1);
			DateTime endDate = nextDay.isAfter(dateTo) ? dateTo : nextDay;
			int endTimeSlot = TimeRangeUtils.getTimeSlotNumber(dateFrom, endDate, timeSlotWidth);
			days.add(new DayBoundaries(startTimeSlot, endTimeSlot));
			startDate = endDate;
			startTimeSlot = endTimeSlot;
		}
		return days;
	}
	
	private void addEnergyQuota(Map<String, List<EnergyQuota>> quotasMap, String eascName, EnergyQuota quota) {
		List<EnergyQuota> quotas = quotasMap.get(eascName);
		if (quotas == null) {
			quotas = new LinkedList<>();
			quotasMap.put(eascName, quotas);
		}
		quotas.add(quota);
	}
	
	private static class DayBoundaries {
		
		public int startTimeSlot;
		public int endTimeSlot;
		
		public DayBoundaries(int startTimeSlot, int endTimeSlot) {
			this.startTimeSlot = startTimeSlot;
			this.endTimeSlot = endTimeSlot;
		}
		
	}
	
}
