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

package eu.dc4cities.controlsystem.modules.processcontroller;

import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Power;

/**
 * Holds historical database metrics related to a given activity.
 */
public class HdbActivityMetrics {
	
	private String activityName;
	private int workingModeValue;
	private Amount<?> instantBusinessPerformance;
	private Amount<?> cumulativeBusinessPerformance;
	private Amount<Power> power;
	
	public HdbActivityMetrics(String activityName) {
		this.activityName = activityName;
	}

	/**
	 * Returns the name of the activity
	 * 
	 * @return the name of the activity
	 */
	public String getActivityName() {
		return activityName;
	}

	/**
	 * Returns the value of the working mode in which the activity is running.
	 * 
	 * @return the value of the current working mode
	 */
	public int getWorkingModeValue() {
		return workingModeValue;
	}

	public void setWorkingModeValue(int workingModeValue) {
		this.workingModeValue = workingModeValue;
	}

	/**
	 * Returns the instant business performance for the activity.
	 * 
	 * @return the instant business performance for the activity
	 */
	public Amount<?> getInstantBusinessPerformance() {
		return instantBusinessPerformance;
	}

	public void setInstantBusinessPerformance(Amount<?> instantBusinessPerformance) {
		this.instantBusinessPerformance = instantBusinessPerformance;
	}

	/**
	 * Returns the cumulative business performance for the activity or {@code null} if this is a service-oriented
	 * activity with no cumulative performance concept.
	 * 
	 * @return the cumulative business performance or {@code null}
	 */
	public Amount<?> getCumulativeBusinessPerformance() {
		return cumulativeBusinessPerformance;
	}

	public void setCumulativeBusinessPerformance(Amount<?> cumulativeBusinessPerformance) {
		this.cumulativeBusinessPerformance = cumulativeBusinessPerformance;
	}

	/**
	 * Returns the power consumption for the activity.
	 * 
	 * @return the power consumption for the activity
	 */
	public Amount<Power> getPower() {
		return power;
	}

	public void setPower(Amount<Power> power) {
		this.power = power;
	}
	
}
