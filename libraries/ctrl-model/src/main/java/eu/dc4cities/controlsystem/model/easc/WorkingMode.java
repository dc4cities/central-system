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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a working mode that can be used to execute a given activity.
 */
public class WorkingMode {

	private String name;
	private int value;
	private List<PerformanceLevel> performanceLevels;
	private List<Transition> transitions;
	
	public WorkingMode() {
		transitions = new ArrayList<>();
		performanceLevels = new ArrayList<>();
	}
	
	@JsonCreator
	public WorkingMode(@JsonProperty("name") String name, @JsonProperty("value") int value) {
		this();
		this.name = name;
		this.value = value;
	}

	/**
	 * Returns the name of the working mode.
	 * 
	 * @return the name of the working mode
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a unique value associated to the working mode for storing historical usage data as a metric.
	 * 
	 * @return the value associated to the working mode
	 */
	public int getValue() {
		return value;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(int value) {
		this.value = value;
	}

	/**
	 * Returns a list of performance levels the working mode can achieve.
	 * 
	 * @return a list of performance levels for the working mode
	 */
	public List<PerformanceLevel> getPerformanceLevels() {
		return performanceLevels;
	}

	public void setPerformanceLevels(List<PerformanceLevel> performanceLevels) {
		this.performanceLevels = performanceLevels;
	}

	/**
	 * Returns info about transitioning from this working mode to a list of target modes. If the transition cost to a
	 * certain target mode is not listed, a cost of zero is assumed. So the list of transitions can be empty if either
	 * all costs are zero or no transitions to other working modes are possible.
	 * 
	 * @return the list of transitions
	 */
	@JsonInclude(Include.NON_EMPTY)
	public List<Transition> getTransitions() {
		return transitions;
	}

	public void setTransitions(List<Transition> transitions) {
		this.transitions = transitions;
	}
	
}
