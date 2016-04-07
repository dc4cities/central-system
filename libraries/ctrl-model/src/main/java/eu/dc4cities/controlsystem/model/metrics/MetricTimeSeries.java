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

package eu.dc4cities.controlsystem.model.metrics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Deprecated
public class MetricTimeSeries<T extends TimeBasedMetric> {

	private String assetID;
	private String assetName;
	private Date dateFrom; 
	private Date dateTo;
	private Date dateForecasting;
    private List<T> timeSeries = new ArrayList<T>();
    private List<MetricDefinition> metricsDefinition = new ArrayList<MetricDefinition>();
    private TimeSlotDefinition timeSlots = new TimeSlotDefinition();

	public String getAssetID() {
		return assetID;
	}
	public void setAssetID(String assetID) {
		this.assetID = assetID;
	}
	public String getAssetName() {
		return assetName;
	}
	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}
	public Date getDateFrom() {
		return dateFrom;
	}
	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}
	public Date getDateTo() {
		return dateTo;
	}
	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}
	public Date getDateForecasting() {
		return dateForecasting;
	}
	public void setDateForecasting(Date dateForecasting) {
		this.dateForecasting = dateForecasting;
	}
	
	public List<MetricDefinition> getMetricsDefinition() {
		return metricsDefinition;
	}
	public void setMetricsDefinition(List<MetricDefinition> metricsDefinition) {
		this.metricsDefinition = metricsDefinition;
	}
	public List<T> getTimeSeries() {
		return timeSeries;
	}
	public void setTimeSeries(List<T> timeSeries) {
		this.timeSeries = timeSeries;
	}
	public TimeSlotDefinition getTimeSlots() {
		return timeSlots;
	}
	public void setTimeSlots(TimeSlotDefinition timeSlots) {
		this.timeSlots = timeSlots;
	}
	
}
