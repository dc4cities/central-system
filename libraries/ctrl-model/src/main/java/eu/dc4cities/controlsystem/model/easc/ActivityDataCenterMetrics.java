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

package eu.dc4cities.controlsystem.model.easc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Power;

/**
 * Holds monitoring metrics related to the execution of a given activity in a given data center.
 * 
 * @see ActivityMetrics
 */
public class ActivityDataCenterMetrics {

	private String dataCenterName;
	private String workingModeName;
	private int workingModeValue;
	private Amount<?> instantBusinessPerformance;
	private Amount<?> cumulativeBusinessPerformance;
	private Amount<Power> power;
	
	@JsonCreator
	public ActivityDataCenterMetrics(@JsonProperty("dataCenterName") String dataCenterName) {
		this.dataCenterName = dataCenterName;
	}

	/**
	 * Returns the name of the data center the metrics refer to.
	 * 
	 * @return the name of the data center
	 */
	public String getDataCenterName() {
		return dataCenterName;
	}
	
	/**
	 * Returns the name of the working mode in which the activity is currently running.
	 * 
	 * @return the name of the current working mode
	 */
	public String getWorkingModeName() {
		return workingModeName;
	}

	public void setWorkingModeName(String workingModeName) {
		this.workingModeName = workingModeName;
	}

	/**
	 * Returns the value of the working mode in which the activity is currently running.
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
	 * Returns the current instant business performance for the activity.
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
	 * Returns the cumulative business performance for the activity in the current service level objective time
	 * interval. This value is returned only for task-oriented activities and is {@code null} otherwise.
	 * 
	 * @return the cumulative business performance for the activity or {@code null}
	 */
	public Amount<?> getCumulativeBusinessPerformance() {
		return cumulativeBusinessPerformance;
	}

	public void setCumulativeBusinessPerformance(Amount<?> cumulativeBusinessPerformance) {
		this.cumulativeBusinessPerformance = cumulativeBusinessPerformance;
	}

	/**
	 * Returns the current power consumption for the activity.
	 * 
	 * @return the current power consumption for the activity
	 */
	public Amount<Power> getPower() {
		return power;
	}

	public void setPower(Amount<Power> power) {
		this.power = power;
	}
	
}
