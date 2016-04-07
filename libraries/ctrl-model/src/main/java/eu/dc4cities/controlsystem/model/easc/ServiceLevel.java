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
import eu.dc4cities.controlsystem.model.json.JsonUtils;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;

/**
 * The service level for an activity in a given time interval. As for service level objectives, the performance
 * can be expressed as instant or cumulative depending on the type of activity (service-oriented or task-oriented).
 * This class may be used to express the actual or planned service level, depending on the context.
 * 
 * @see ServiceLevelObjective
 */
public class ServiceLevel {

	private DateTime dateFrom;
	private DateTime dateTo;
	private Amount<?> instantBusinessPerformance;
	private Amount<?> cumulativeBusinessPerformance;
	
	public ServiceLevel(DateTime dateFrom, DateTime dateTo,
			Amount<?> instantBusinessPerformance, Amount<?> cumulativeBusinessPerformance) {
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		this.instantBusinessPerformance = instantBusinessPerformance;
		this.cumulativeBusinessPerformance = cumulativeBusinessPerformance;
	}

	@JsonCreator
	public ServiceLevel(@JsonProperty("dateFrom") DateTime dateFrom, @JsonProperty("dateTo") DateTime dateTo) {
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
	}
	
	/**
	 * The start of the time interval
	 * 
	 * @return the start of the time interval
	 */
	public DateTime getDateFrom() {
		return dateFrom;
	}

	/**
	 * The end of the time interval
	 * 
	 * @return the end of the time interval
	 */
	public DateTime getDateTo() {
		return dateTo;
	}
	
	/**
	 * Returns the instant business performance for the time interval. This is set for service-oriented
	 * activities, {@code null} otherwise.
	 * 
	 * @return the instant business performance or {@code null}
	 */
	public Amount<?> getInstantBusinessPerformance() {
		return instantBusinessPerformance;
	}

	public void setInstantBusinessPerformance(Amount<?> instantBusinessPerformance) {
		this.instantBusinessPerformance = instantBusinessPerformance;
	}

	/**
	 * Returns the cumulative business performance for the time interval. This is set for task-oriented
	 * activities, {@code null} otherwise.
	 * 
	 * @return the cumulative business performance or {@code null}
	 */
	public Amount<?> getCumulativeBusinessPerformance() {
		return cumulativeBusinessPerformance;
	}

	public void setCumulativeBusinessPerformance(Amount<?> cumulativeBusinessPerformance) {
		this.cumulativeBusinessPerformance = cumulativeBusinessPerformance;
	}
	
	@Override
	public String toString() {
		return JsonUtils.toString(this);
	}
	
}
