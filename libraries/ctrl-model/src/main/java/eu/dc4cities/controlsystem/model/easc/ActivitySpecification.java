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
import org.jscience.physics.amount.Amount;

import java.util.ArrayList;
import java.util.List;

/**
 * Specifies how a given activity can be executed (data center and working modes) and what it should do (service level
 * objectives).
 */
public class ActivitySpecification {

	private String activityName;
	private List<String> precedences;
	private Relocability relocability;
	private Amount<?> migrationPerformanceCost;
	private List<DataCenterSpecification> dataCenters;
	private List<ForbiddenState> forbiddenStates;
	private List<ServiceLevelObjective> serviceLevelObjectives;
	
	@JsonCreator
	public ActivitySpecification(@JsonProperty("activityName") String activityName) {
		this.activityName = activityName;
		this.precedences = new ArrayList<>();
		this.forbiddenStates = new ArrayList<>();
	}

	/**
	 * Returns the name of the activity.
	 * 
	 * @return the name of the activity
	 */
	public String getActivityName() {
		return activityName;
	}
	
	/**
	 * Returns a list of activities that must be executed before this one.
	 * 
	 * @return a list of pre-requisite activities
	 */
	@JsonInclude(Include.NON_EMPTY)
	public List<String> getPrecedences() {
		return precedences;
	}

	public void setPrecedences(List<String> precedences) {
		this.precedences = precedences;
	}

	/**
	 * Returns whether the activity can be migrated or load-balanced across data centers.
	 * 
	 * @return the relocability of the activity
	 */
	public Relocability getRelocability() {
		return relocability;
	}

	public void setRelocability(Relocability relocability) {
		this.relocability = relocability;
	}

	/**
	 * Returns the performance cost for migrating the activity to another data center. This is expressed as an instant
	 * or cumulative performance based on whether the activity is service-oriented or task-oriented.
	 * 
	 * @return the migration performance cost
	 */
	public Amount<?> getMigrationPerformanceCost() {
		return migrationPerformanceCost;
	}

	public void setMigrationPerformanceCost(Amount<?> migrationPerformanceCost) {
		this.migrationPerformanceCost = migrationPerformanceCost;
	}

	/**
	 * Returns one or more data centers in which the activity can run.
	 * 
	 * @return the list of data centers for the activity
	 */
	public List<DataCenterSpecification> getDataCenters() {
		return dataCenters;
	}

	public void setDataCenters(List<DataCenterSpecification> dataCenters) {
		this.dataCenters = dataCenters;
	}

	/**
	 * Returns the list of states that are forbidden for this activity. Useful for activities in a federation.
	 * 
	 * @return the list of forbidden states
	 */
	@JsonInclude(Include.NON_EMPTY)
	public List<ForbiddenState> getForbiddenStates() {
		return forbiddenStates;
	}

	public void setForbiddenStates(List<ForbiddenState> forbiddenStates) {
		this.forbiddenStates = forbiddenStates;
	}

	/**
	 * Returns one or more service level objectives the activity should achieve.
	 * 
	 * @return the service level objectives for the activity
	 */
	public List<ServiceLevelObjective> getServiceLevelObjectives() {
		return serviceLevelObjectives;
	}

	public void setServiceLevelObjectives(List<ServiceLevelObjective> serviceLevelObjectives) {
		this.serviceLevelObjectives = serviceLevelObjectives;
	}
	
}
