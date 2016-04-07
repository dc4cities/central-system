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
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import java.util.LinkedList;
import java.util.List;

/**
 * Contains information about the status of data center goals for a given time slot.
 */
@Deprecated
public class TimeSlotStatus {

	private DateTime timestamp;
	private StatusCode status;
	private List<StatusAlert> alerts;
	
	@JsonCreator
	public TimeSlotStatus(@JsonProperty("timestamp") DateTime timestamp, @JsonProperty("status") StatusCode status) {
		this.timestamp = timestamp;
		this.status = status;
	}

	/**
	 * Returns the timestamp of the time slot this status refers to.
	 * 
	 * @return the timestamp of the time slot
	 */
	public DateTime getTimestamp() {
		return timestamp;
	}

	/**
	 * Returns the status code of the time slot.
	 * 
	 * @return the status code of the time slot
	 */
	public StatusCode getStatus() {
		return status;
	}
	
	/**
	 * Returns the alerts associated to this time slot. The list can be {@code null} or empty if no alerts are provided
	 * in addition to the status.
	 * 
	 * @return the list of alerts for this time slot or {@code null}
	 */
	public List<StatusAlert> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<StatusAlert> alerts) {
		this.alerts = alerts;
	}
	
	public void addAlert(StatusAlert alert) {
		if (alerts == null) {
			alerts = new LinkedList<>();
		}
		alerts.add(alert);
	}
	
}
