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

package eu.dc4cities.controlsystem.model.datacenter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains information about the status of a given data center goals with alerts in case problems have been detected.
 */
public class DataCenterStatus extends TimeSlotBasedEntity {

	private String dataCenterName;
	private StatusCode shortTermStatus;
	private StatusCode longTermStatus;
	private List<TimeSlotStatus> timeSlotStatuses = new LinkedList<>();
	private List<StatusAlert> alerts = new LinkedList<>();
	private List<EascStatus> eascs = new LinkedList<>();
	
	@JsonCreator
	public DataCenterStatus(@JsonProperty("dataCenterName") String dataCenterName) {
		this.dataCenterName = dataCenterName;
	}

	/**
	 * Returns the name of the data center the status refers to.
	 * 
	 * @return the name of the data center
	 */
	public String getDataCenterName() {
		return dataCenterName;
	}

	/**
	 * Returns the short term status for the data center. The time interval considered as "short term" is the first day
	 * included in the time range, from dateFrom to the end of the day.
	 * 
	 * @return the short term status for the data center
	 */
	@Deprecated
	public StatusCode getShortTermStatus() {
		return shortTermStatus;
	}

	@Deprecated
	public void setShortTermStatus(StatusCode shortTermStatus) {
		this.shortTermStatus = shortTermStatus;
	}

	/**
	 * Returns the long term status for the data center. The time interval considered as "long term" is the second day
	 * included in the time range, from the beginning to the end of the day.
	 * 
	 * @return the long term status for the data center
	 */
	@Deprecated
	public StatusCode getLongTermStatus() {
		return longTermStatus;
	}

	@Deprecated
	public void setLongTermStatus(StatusCode longTermStatus) {
		this.longTermStatus = longTermStatus;
	}

	/**
	 * Returns the detailed status information for each time slot in the optimization interval.
	 * 
	 * @return the detailed status information for each time slot
	 */
	@Deprecated
	@JsonInclude(Include.NON_EMPTY)
	public List<TimeSlotStatus> getTimeSlotStatuses() {
		return timeSlotStatuses;
	}

	@Deprecated
	public void setTimeSlotStatuses(List<TimeSlotStatus> timeSlotStatuses) {
		this.timeSlotStatuses = timeSlotStatuses;
	}
	
	@Deprecated
	public void addTimeSlotStatus(TimeSlotStatus timeSlotStatus) {
		timeSlotStatuses.add(timeSlotStatus);
	}

	/**
	 * Returns the list of alerts for this data center. The list is empty when there is no alert.
	 * 
	 * @return the data center alerts
	 */
	public List<StatusAlert> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<StatusAlert> alerts) {
		this.alerts = alerts;
	}
	
	public void addAlert(StatusAlert statusAlert) {
		alerts.add(statusAlert);
	}

	/**
	 * Returns the status for EASCs that can execute activities in this data center. The list is empty if EASC status
	 * is not provided.
	 * 
	 * @return the EASC statuses
	 */
	@JsonInclude(Include.NON_EMPTY)
	public List<EascStatus> getEascs() {
		return eascs;
	}

	public void setEascs(List<EascStatus> eascs) {
		this.eascs = eascs;
	}
	
	public void addEasc(EascStatus easc) {
		eascs.add(easc);
	}
	
}
