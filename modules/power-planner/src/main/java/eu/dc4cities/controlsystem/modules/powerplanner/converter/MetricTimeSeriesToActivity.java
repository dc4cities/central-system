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

package eu.dc4cities.controlsystem.modules.powerplanner.converter;

import eu.dc4cities.controlsystem.model.PowerPlan;
import eu.dc4cities.controlsystem.model.TimeSlotPower;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.SimpleActivity;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.WorkingMode;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.WorkingModeSlot;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 *
 */
public class MetricTimeSeriesToActivity {

	/**
	 * 
	 * @param currentPowerPlan
	 * @return
	 */
	public static List<SimpleActivity> convert(PowerPlan currentPowerPlan, PowerPlan cappedPowerPlan) {
		List<SimpleActivity> simpleActivityList = new ArrayList<>();
		List<WorkingModeSlot> timeSlots = new ArrayList<WorkingModeSlot>();

		List<TimeSlotPower> timeSlotPowerList = currentPowerPlan.getPowerQuotas();
		List<TimeSlotPower> cappedTimeSlotPowerList = cappedPowerPlan.getPowerQuotas();
		int i = 0;
		int j = 0;
		for (TimeSlotPower currentTimeSlotPower : timeSlotPowerList) {
			SimpleActivity simpleActivity = new SimpleActivity(null, "SimpleActivity" + i);
			
			timeSlots.clear();
			while (j < i) {
				timeSlots.add(WorkingModeSlot.ZERO);
				j++;
			}
			WorkingModeSlot workingModeSlot = new WorkingModeSlot(currentTimeSlotPower.getPower(), 0);
			timeSlots.add(workingModeSlot);
			j++;
			
			while (j < timeSlotPowerList.size()) {
				timeSlots.add(WorkingModeSlot.ZERO);
				j++;
			}
			WorkingModeSlot[] wms = timeSlots.toArray(new WorkingModeSlot[timeSlots.size()]);
			WorkingMode workingMode = new WorkingMode(0, wms, null);
			simpleActivity.add(workingMode);
			j = 0;
			
			timeSlots.clear();
			while (j < i) {
				timeSlots.add(WorkingModeSlot.ZERO);
				j++;
			}
			workingModeSlot = new WorkingModeSlot(cappedTimeSlotPowerList.get(i).getPower(), 0);
			timeSlots.add(workingModeSlot);
			j++;
			
			while (j < timeSlotPowerList.size()) {
				timeSlots.add(WorkingModeSlot.ZERO);
				j++;
			}
			wms = timeSlots.toArray(new WorkingModeSlot[timeSlots.size()]);
			workingMode = new WorkingMode(1, wms, null);
			simpleActivity.add(workingMode);
			j = 0;
			
			timeSlots.clear();
			while (j < i) {
				timeSlots.add(WorkingModeSlot.ZERO);
				j++;
			}
			workingModeSlot = new WorkingModeSlot(cappedTimeSlotPowerList.get(i).getPower().minus(cappedTimeSlotPowerList.get(i).getPower().times(0.1)), 0);
			timeSlots.add(workingModeSlot);
			j++;
			
			while (j < timeSlotPowerList.size()) {
				timeSlots.add(WorkingModeSlot.ZERO);
				j++;
			}
			wms = timeSlots.toArray(new WorkingModeSlot[timeSlots.size()]);
			workingMode = new WorkingMode(2, wms, null);
			simpleActivity.add(workingMode);
			j = 0;
			
			timeSlots.clear();
			while (j < i) {
				timeSlots.add(WorkingModeSlot.ZERO);
				j++;
			}
			workingModeSlot = new WorkingModeSlot(cappedTimeSlotPowerList.get(i).getPower().minus(cappedTimeSlotPowerList.get(i).getPower().times(0.2)), 0);
			timeSlots.add(workingModeSlot);
			j++;
			
			while (j < timeSlotPowerList.size()) {
				timeSlots.add(WorkingModeSlot.ZERO);
				j++;
			}
			wms = timeSlots.toArray(new WorkingModeSlot[timeSlots.size()]);
			workingMode = new WorkingMode(3, wms, null);
			simpleActivity.add(workingMode);
			j = 0;
			
			timeSlots.clear();
			while (j < i) {
				timeSlots.add(WorkingModeSlot.ZERO);
				j++;
			}
			workingModeSlot = new WorkingModeSlot(cappedTimeSlotPowerList.get(i).getPower().minus(cappedTimeSlotPowerList.get(i).getPower().times(0.4)), 0);
			timeSlots.add(workingModeSlot);
			j++;
			
			while (j < timeSlotPowerList.size()) {
				timeSlots.add(WorkingModeSlot.ZERO);
				j++;
			}
			wms = timeSlots.toArray(new WorkingModeSlot[timeSlots.size()]);
			workingMode = new WorkingMode(4, wms, null);
			simpleActivity.add(workingMode);
			j = 0;
			
			timeSlots.clear();
			while (j < i) {
				timeSlots.add(WorkingModeSlot.ZERO);
				j++;
			}
			workingModeSlot = new WorkingModeSlot(cappedTimeSlotPowerList.get(i).getPower().minus(cappedTimeSlotPowerList.get(i).getPower().times(0.8)), 0);
			timeSlots.add(workingModeSlot);
			j++;
			
			while (j < timeSlotPowerList.size()) {
				timeSlots.add(WorkingModeSlot.ZERO);
				j++;
			}
			wms = timeSlots.toArray(new WorkingModeSlot[timeSlots.size()]);
			workingMode = new WorkingMode(5, wms, null);
			simpleActivity.add(workingMode);
			j = 0;
			
//			timeSlots.clear();
//			while (j < timeSlotPowerList.size()) {
//				timeSlots.add(WorkingModeSlot.ZERO);
//				j++;
//			}
//			wms = timeSlots.toArray(new WorkingModeSlot[timeSlots.size()]);
//			workingMode = new WorkingMode(6, wms, null);
//			simpleActivity.add(workingMode);
//			j = 0;
			
			simpleActivityList.add(simpleActivity);
			i++;
		}
		return simpleActivityList;
	}
}
