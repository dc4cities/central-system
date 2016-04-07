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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterExecutionPlan;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterStatus;
import eu.dc4cities.controlsystem.model.json.JsonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the results of a data center optimization from the control loop.
 */
public class DataCenterOptimization {

	private String dataCenterName;
	private DataCenterExecutionPlan executionPlan;
	private DataCenterStatus status;
	
	@JsonCreator
	public DataCenterOptimization(@JsonProperty("dataCenterName") String dataCenterName, 
			@JsonProperty("executionPlan") DataCenterExecutionPlan executionPlan,
			@JsonProperty("status") DataCenterStatus status) {
		this.dataCenterName = dataCenterName;
		this.executionPlan = executionPlan;
		this.status = status;
	}

	/**
	 * Returns the name of the data center.
	 * 
	 * @return the name of the data center
	 */
	public String getDataCenterName() {
		return dataCenterName;
	}

	/**
	 * Returns the execution plan calculated for the data center in the control loop.
	 * 
	 * @return the data center execution plan
	 */
	public DataCenterExecutionPlan getExecutionPlan() {
		return executionPlan;
	}

	/**
	 * Returns the status for the data center calculated by the escalation manager in the control loop. The status is
	 * {@code null} if the escalation manager is disabled.
	 * 
	 * @return the data center status or {@code null}
	 */
	public DataCenterStatus getStatus() {
		return status;
	}
	
	/**
	 * Extracts the list of execution plans from the given list of optimizations.
	 * 
	 * @param dataCenterOptimizations the list of optimizations to extract execution plans from
	 * @return the list of execution plans
	 */
	public static List<DataCenterExecutionPlan> getExecutionPlans(
			List<DataCenterOptimization> dataCenterOptimizations) {
		List<DataCenterExecutionPlan> executionPlans = new ArrayList<>(dataCenterOptimizations.size());
		for (DataCenterOptimization optimization : dataCenterOptimizations) {
			executionPlans.add(optimization.executionPlan);
		}
		return executionPlans;
	}
	
	/**
	 * Extracts the list of statuses from the given list of optimizations. Optimizations with status = null are skipped,
	 * so the returned list is empty if the escalation manager is disabled.
	 * 
	 * @param dataCenterOptimizations the list of optimizations to extract statuses from
	 * @return the list of statuses
	 */
	public static List<DataCenterStatus> getStatuses(List<DataCenterOptimization> dataCenterOptimizations) {
		List<DataCenterStatus> statuses = new ArrayList<>(dataCenterOptimizations.size());
		for (DataCenterOptimization optimization : dataCenterOptimizations) {
			if (optimization.status != null) {
				statuses.add(optimization.status);
			}
		}
		return statuses;
	}
	
	@Override
	public String toString() {
		return JsonUtils.toString(this);
	}
	
}
