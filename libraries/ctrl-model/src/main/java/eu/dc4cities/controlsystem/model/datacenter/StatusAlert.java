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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * An alert message related to the status of the data center. For example this may point to low levels of renewable
 * energy usage or business performance.
 */
@JsonPropertyOrder({"startTimeSlot", "endTimeSlot", "type", "severity", "eascName", "activityName", "message"})
public class StatusAlert {

	private int startTimeSlot;
	private int endTimeSlot;
	private AlertType type;
	private AlertSeverity severity;
	private String eascName;
	private String activityName;
	private String message;
	private String action;
	
	@Deprecated
	public StatusAlert(@JsonProperty("message") String message) {
		this.message = message;
	}
	
	@Deprecated
	public StatusAlert(String message, String action) {
		this.message = message;
		this.action = action;
	}
	
	public StatusAlert() {}

	/**
	 * Returns the number of the time slot when this alert begins (inclusive, 0-based).
	 * 
	 * @return the start time slot
	 */
	public int getStartTimeSlot() {
		return startTimeSlot;
	}

	public void setStartTimeSlot(int startTimeSlot) {
		this.startTimeSlot = startTimeSlot;
	}

	/**
	 * Returns the number of the time slot when this alert ends (exclusive).
	 * 
	 * @return the end time slot
	 */
	public int getEndTimeSlot() {
		return endTimeSlot;
	}

	public void setEndTimeSlot(int endTimeSlot) {
		this.endTimeSlot = endTimeSlot;
	}

	/**
	 * Returns the type of the alert
	 * 
	 * @return the alert type
	 */
	public AlertType getType() {
		return type;
	}

	public void setType(AlertType type) {
		this.type = type;
	}

	/**
	 * Returns the severity of the alert
	 * 
	 * @return the severity type
	 */
	public AlertSeverity getSeverity() {
		return severity;
	}

	public void setSeverity(AlertSeverity severity) {
		this.severity = severity;
	}

	/**
	 * Returns the name of the EASC the alert refers to. This can be omitted when the alert refers to the whole data
	 * center.
	 * 
	 * @return the name of the EASC or {@code null}
	 */
	public String getEascName() {
		return eascName;
	}

	public void setEascName(String eascName) {
		this.eascName = eascName;
	}

	/**
	 * Returns the name of the activity the alert refers to. This can be omitted when the alert refers to the whole data
	 * center.
	 * 
	 * @return the name of the activity or {@code null}
	 */
	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	/**
	 * Returns the alert message.
	 * 
	 * @return the alert message
	 */
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Returns a possible action to undertake for solving the problem.
	 * 
	 * @return a suggestion to solve the alert or {@code null} if not provided
	 */
	@Deprecated
	public String getAction() {
		return action;
	}

	@Deprecated
	public void setAction(String action) {
		this.action = action;
	}
	
}
