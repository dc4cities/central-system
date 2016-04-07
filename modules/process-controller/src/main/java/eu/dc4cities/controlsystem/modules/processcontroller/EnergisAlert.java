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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.datacenter.*;
import eu.dc4cities.controlsystem.model.util.TimeRangeUtils;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Duration;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a single alert to be stored in Energis. Fields names must conform to the Energis API for correct
 * JSON serialization. Fields without a setter are not used by DC4Cities but they are required by Energis, so they are
 * filled with either fixed values of values derived from the other fields.
 */
public class EnergisAlert {

	/**
	 * The date format expected by the Energis Alerts API in JSON payloads. Requires milliseconds which are not included
	 * in the default format used by DC4Cities.
	 */
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	
	private String name;
	private String status = "OPENED";
	private DateTime openingTime;
	private DateTime closingTime;
	private int occurrences = 1;
	private DateTime occurringSince;
	private DateTime lastOccurrence;
	private String assetCode;
	private AlertSeverity severity;
	private String classification;
	private AlertType type;
	private String description;
	private String procedure = "d4c";
	
	/**
	 * Builds a list of Energis alerts from the given data center status.
	 * 
	 * @param dataCenterStatus the source of alerts
	 * @return the energis alerts
	 */
	public static List<EnergisAlert> list(DataCenterStatus dataCenterStatus) {
		String dataCenterName = dataCenterStatus.getDataCenterName();
		List<EnergisAlert> energisAlerts = new LinkedList<>();
		addEnergisAlerts(dataCenterStatus, dataCenterName, null, null, dataCenterStatus.getAlerts(), energisAlerts);
		for (EascStatus easc : dataCenterStatus.getEascs()) {
			String eascName = easc.getEascName();
			for (ActivityStatus activity : easc.getActivities()) {
				String activityName = activity.getActivityName();
				addEnergisAlerts(dataCenterStatus, dataCenterName, eascName, activityName, activity.getAlerts(),
						energisAlerts);
			}
		}
		return energisAlerts;
	}
	
	private static void addEnergisAlerts(TimeSlotBasedEntity timeRange, String dataCenterName, String eascName, 
			String activityName, List<StatusAlert> statusAlerts, List<EnergisAlert> energisAlerts) {
		DateTime dateFrom = timeRange.getDateFrom();
		Amount<Duration> timeSlotDuration = timeRange.getTimeSlotDuration();
		for (StatusAlert statusAlert : statusAlerts) {
			EnergisAlert energisAlert = new EnergisAlert();
			DateTime openingTime = 
					TimeRangeUtils.getTimeSlotStart(dateFrom, statusAlert.getStartTimeSlot(), timeSlotDuration);
			DateTime lastOccurrence = 
					TimeRangeUtils.getTimeSlotStart(dateFrom, statusAlert.getEndTimeSlot(), timeSlotDuration);
			energisAlert.setOpeningTime(openingTime);
			energisAlert.setLastOccurrence(lastOccurrence);
			String assetCode = dataCenterName;
			if (activityName != null) {
				assetCode += "." + eascName + "." + activityName;
			}
			energisAlert.setAssetCode(assetCode);
			energisAlert.setSeverity(statusAlert.getSeverity());
			energisAlert.setType(statusAlert.getType());
			energisAlert.setDescription(statusAlert.getMessage());
			energisAlerts.add(energisAlert);
		}
	}
	
	public String getName() {
		return name;
	}

	private void updateName() {
		if (type != null && openingTime != null) {
			name = type.value() + "-" + openingTime.getMillis();
		} else {
			name = null;
		}
	}
	
	public String getStatus() {
		return status;
	}

	/**
	 * Returns the start of the interval the alert applies to.
	 * 
	 * @return the alert start time
	 */
	@JsonFormat(shape = Shape.STRING, pattern = DATE_FORMAT)
	public DateTime getOpeningTime() {
		return openingTime;
	}

	public void setOpeningTime(DateTime openingTime) {
		this.openingTime = openingTime;
		this.occurringSince = openingTime;
		updateName();
	}
	
	/**
	 * Returns the time when the alert status was changed to CLOSED. Null if the status is still OPENED.
	 * 
	 * @return the alert closing time
	 */
	@JsonFormat(shape = Shape.STRING, pattern = DATE_FORMAT)
	public DateTime getClosingTime() {
		return closingTime;
	}

	public void setClosingTime(DateTime closingTime) {
		this.closingTime = closingTime;
	}
	
	public int getOccurrences() {
		return occurrences;
	}
	
	@JsonFormat(shape = Shape.STRING, pattern = DATE_FORMAT)
	public DateTime getOccurringSince() {
		return occurringSince;
	}

	@JsonFormat(shape = Shape.STRING, pattern = DATE_FORMAT)
	public DateTime getLastOccurrence() {
		return lastOccurrence;
	}
	
	/**
	 * Sets the time when the alert was last seen.
	 * 
	 * @param lastOccurrence the time of the last occurrence
	 */
	public void setLastOccurrence(DateTime lastOccurrence) {
		this.lastOccurrence = lastOccurrence;
	}

	/**
	 * Returns the code of the asset the alert refers to.
	 * 
	 * @return the asset code the alert refers to
	 */
	public String getAssetCode() {
		return assetCode;
	}

	public void setAssetCode(String assetCode) {
		this.assetCode = assetCode;
	}
	
	/**
	 * Returns the severity level of the alert.
	 * 
	 * @return the alert severity
	 */
	public AlertSeverity getSeverity() {
		return severity;
	}

	public void setSeverity(AlertSeverity severity) {
		this.severity = severity;
	}
	
	public String getClassification() {
		return classification;
	}
	
	/**
	 * Returns the type of the alert.
	 * 
	 * @return the alert type
	 */
	public AlertType getType() {
		return type;
	}

	public void setType(AlertType type) {
		this.type = type;
		this.classification = type.classification();
		updateName();
	}
	
	/**
	 * Returns the alert message.
	 * 
	 * @return the alert message
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getProcedure() {
		return procedure;
	}
	
}
