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
 * Defines a business performance level for a working mode and the associated estimated power consumption.
 */
public class PerformanceLevel {

	private Amount<?> businessPerformance;
	private Amount<Power> power;
	
	@JsonCreator
	public PerformanceLevel(@JsonProperty("businessPerformance") Amount<?> businessPerformance,
			@JsonProperty("power") Amount<Power> power) {
		this.businessPerformance = businessPerformance;
		this.power = power;
	}

	/**
	 * Returns the business performance associated to this performance level. The unit depends on the kind of work to do.
	 * 
	 * @return the business performance level
	 */
	public Amount<?> getBusinessPerformance() {
		return businessPerformance;
	}

	/**
	 * Returns the estimated power consumption to achieve this performance level.
	 * 
	 * @return the estimated power consumption in Watts
	 */
	public Amount<Power> getPower() {
		return power;
	}

	public void setBusinessPerformance(Amount<?> businessPerformance) {
		this.businessPerformance = businessPerformance;
	}

	public void setPower(Amount<Power> power) {
		this.power = power;
	}

	public PerformanceLevel() {
	}
	
}
