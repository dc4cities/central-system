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

/**
 * Defines a transition between working modes. A transition is always contained as a child in a parent
 * {@code WorkingMode}, which is the initial state of the transition. The target working mode is defined in the
 * transition itself. The characteristics of the transition, such as the performance cost, depend on the target working
 * mode.
 */
public class Transition {

	private String target;
	private Amount<?> performanceCost;
	
	@JsonCreator
	public Transition(@JsonProperty("target") String target,
			@JsonProperty("performanceCost") Amount<?> performanceCost) {
		this.target = target;
		this.performanceCost = performanceCost;
	}

	/**
	 * Returns the name of the target working mode for this transition.
	 * 
	 * @return the transition target working mode
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * Returns the performance cost of the transition. The cost is expressed as the amount of performance lost because
	 * of the time taken to perform the transition w.r.t. the ideal performance of the target working mode in case the
	 * transition was instant. The performance can be expressed as an instant performance or a cumulative performance,
	 * depending on the kind of activity.
	 * 
	 * @return the performance cost of the transition
	 */
	public Amount<?> getPerformanceCost() {
		return performanceCost;
	}

	public Transition() {
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void setPerformanceCost(Amount<?> performanceCost) {
		this.performanceCost = performanceCost;
	}
	
}
