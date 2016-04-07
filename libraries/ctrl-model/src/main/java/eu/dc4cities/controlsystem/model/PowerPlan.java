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

package eu.dc4cities.controlsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.dc4cities.controlsystem.model.util.TimeRangeUtils;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Power;
import javax.measure.unit.NonSI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The power plan for a data center in the specified time range. Defines how much power can be consumed for each time
 * slot.
 */
public class PowerPlan extends TimeSlotBasedEntity {

	private List<TimeSlotPower> powerQuotas;
	
	/**
	 * Creates a new empty power plan.
	 */
	public PowerPlan() {}
	
	/**
	 * Creates a new power plan with the given time range and the given power for all time slots.
	 * 
	 * @param timeRange the time range to initialize the plan with
	 * @param power the power value to set on all time slots
	 */
	public PowerPlan(TimeSlotBasedEntity timeRange, Amount<Power> power) {
		super(timeRange);
		int numOfTimeSlots = getNumOfTimeSlots();
		powerQuotas = new ArrayList<>(numOfTimeSlots);
		for (int i = 0; i < numOfTimeSlots; i++) {
			powerQuotas.add(new TimeSlotPower(i, power));
		}
	}
	
	/**
	 * Creates a new power plan as a deep copy of the given one.
	 * 
	 * @param source the power plan to copy
	 */
	public PowerPlan(PowerPlan source) {
		super(source);
		List<TimeSlotPower> sourceQuotas = source.powerQuotas;
		powerQuotas = new ArrayList<TimeSlotPower>(sourceQuotas.size());
		for (TimeSlotPower sourceQuota : sourceQuotas) {
			powerQuotas.add(new TimeSlotPower(sourceQuota));
		}
	}
	
	/**
     * Returns a list with the maximum power that can be consumed in each time slot.
     * 
     * @return the list of power quotas
     */
	public List<TimeSlotPower> getPowerQuotas() {
		return powerQuotas;
	}

	/**
	 * Returns a map with time slot numbers as keys and corresponding power quotas as values. Any changes made to the power quotas
	 * in the map affect the power plan also.
	 * 
	 * @return a map with the power quotas in this plan
	 */
	@JsonIgnore
	public Map<Integer, TimeSlotPower> getPowerQuotasMap() {
		Map<Integer, TimeSlotPower> map = new HashMap<Integer, TimeSlotPower>();
		for (TimeSlotPower quota : powerQuotas) {
			map.put(quota.getTimeSlot(), quota);
		}
		return map;
	}
	
	@JsonDeserialize(contentAs=TimeSlotPower.class)
	public void setPowerQuotas(List<TimeSlotPower> powerQuotas) {
		this.powerQuotas = powerQuotas;
	}
	
	/**
	 * Sets the given amount of power on all power quotas in the plan.
	 * 
	 * @param power the amount of power to set
	 */
	public void fillPowerAmounts(Amount<Power> power) {
		for (TimeSlotPower quota : powerQuotas) {
			quota.setPower(power);
		}
	}
	
	/**
	 * Multiplies the power amounts in the plan by the given factor. Values are rounded to the nearest integer value.
	 * 
	 * @param factor the scale factor
	 */
	public void scalePowerAmounts(double factor) {
    	for (TimeSlotPower quota : powerQuotas) {
    		quota.scalePower(factor);
    	}
    }
	
	/**
	 * Appends the given plan at the end of this plan. Fails if the start of the plan to append doesn't match the end of
	 * this plan.
	 * 
	 * @param otherPlan the plan to append
	 */
	public void append(PowerPlan otherPlan) {
		if (!otherPlan.getTimeSlotDuration().equals(getTimeSlotDuration())) {
			throw new IllegalArgumentException(
					"The time slot duration of the plan to append doesn't match the one of this plan");
		} else if (!otherPlan.getDateFrom().equals(getDateTo())) {
			throw new IllegalArgumentException("The plan to append doesn't start at the end of this plan");
		}
		setDateTo(otherPlan.getDateTo());
		int nextTimeSlotNumber = powerQuotas.get(powerQuotas.size() - 1).getTimeSlot() + 1;
		for (TimeSlotPower timeSlot : otherPlan.powerQuotas) {
			TimeSlotPower newTimeSlot = new TimeSlotPower(timeSlot);
			newTimeSlot.setTimeSlot(nextTimeSlotNumber);
			nextTimeSlotNumber++;
			powerQuotas.add(newTimeSlot);
		}
	}
	
	/**
	 * Returns a copy of a range of this power plan starting from the time slot at the given date.
	 * 
	 * @param rangeFrom the start of the time slot
	 * @return the range copy of this power plan
	 */
	public PowerPlan copyOfRange(DateTime rangeFrom) {
		if (rangeFrom.isAfter(dateTo)) {
			throw new IllegalArgumentException("rangeFrom is after the end of this power plan");
		}
		int durationMinutes = (int) timeSlotDuration.longValue(NonSI.MINUTE);
		int startTimeSlot = TimeRangeUtils.getTimeSlotNumber(dateFrom, rangeFrom, durationMinutes);
		PowerPlan copy = new PowerPlan();
		copy.copyIntervalFrom(this);
		copy.setDateFrom(rangeFrom);
		copy.powerQuotas = new ArrayList<>(powerQuotas.size() - startTimeSlot);
		for (int i = startTimeSlot; i < powerQuotas.size(); i++) {
			TimeSlotPower sourceQuota = powerQuotas.get(i);
			copy.powerQuotas.add(new TimeSlotPower(i - startTimeSlot, sourceQuota.getPower()));
		}
		return copy;
	}
	
}
