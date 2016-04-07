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

import eu.dc4cities.controlsystem.model.PowerPlan;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.TimeSlotPower;
import eu.dc4cities.controlsystem.model.datacenter.*;
import eu.dc4cities.controlsystem.model.metrics.MetricCatalog;
import eu.dc4cities.controlsystem.model.unit.Units;
import eu.dc4cities.controlsystem.modules.processcontroller.EnergisAlertFeed.EnergisAlertFeedDelete;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;
import org.kairosdb.client.HttpClient;
import org.kairosdb.client.builder.Metric;
import org.kairosdb.client.builder.MetricBuilder;
import org.kairosdb.client.response.Response;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestOperations;

import javax.measure.quantity.Power;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static javax.measure.unit.NonSI.PERCENT;
import static javax.measure.unit.SI.*;

/**
 * Implements access to the historical database.
 */
public class HistoricalDbDao {
	
	private static final String METRIC_ALERT_STATE = "alert_state";
	private static final String REFERENCE_ACTUAL = "ACTUAL";
	private static final String GRANULARITY_5_MIN = "FIVE_MINUTE";
	private static final String GRANULARITY_15_MIN = "FIFTEEN_MINUTE";
	
	private String hdbUrl;
	private String energisUrl;
	private String energisApiKey;
	private String companyCode;
	private RestOperations restOps;
	
	/**
	 * Creates a DAO that can upload metrics to KairosDB.
	 * 
	 * @param hdbUrl the KairosDB URL
	 * @param companyCode the company code to use as a tag when uploading metrics 
	 */
	public HistoricalDbDao(String hdbUrl, String companyCode) {
		this.hdbUrl = trimLastSlash(hdbUrl);
		this.companyCode = companyCode;
	}
	
	/**
	 * Creates a DAO that supports uploading alerts to Energis in addition to metrics to KairosDB.
	 * 
	 * @param hdbUrl the KairosDB URL
	 * @param energisUrl the Energis API URL
	 * @param energisApiKey the key for Energis API authentication
	 * @param companyCode the company code to use as a tag when uploading metrics
	 * @param restOps the REST client to use for uploading alerts to Energis
	 */
	public HistoricalDbDao(String hdbUrl, String energisUrl, String energisApiKey, String companyCode, 
			RestOperations restOps) {
		this(hdbUrl, companyCode);
		this.energisUrl = trimLastSlash(energisUrl);
		this.energisApiKey = energisApiKey;
		this.restOps = restOps;
	}
	
	private String trimLastSlash(String url) {
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		return url;
	}
	
	public void writeDataCenterMetrics(DateTime date, List<HdbDataCenterMetrics> dataCenterMetrics) {
		MetricBuilder builder = MetricBuilder.getInstance();
		for (HdbDataCenterMetrics dataCenter : dataCenterMetrics) {
			String dataCenterName = dataCenter.getDataCenterName();
			setActualDataCenterMetric(builder, dataCenterName, MetricCatalog.POWER.getName(), date.getMillis(), 
					dataCenter.getPowerConsumption().doubleValue(WATT));
			setActualDataCenterMetric(builder, dataCenterName, MetricCatalog.RENEWABLE_ENERGY_PERCENTAGE.getName(),
					date.getMillis(), dataCenter.getGridRenewablePercentage().doubleValue(PERCENT));
			Amount<Power> renPowerAmount = dataCenter.getRenewablePower();
			if (renPowerAmount == null) {
				// Energis requires renewable power to be set to 0 if not present
				renPowerAmount = Amount.valueOf(0, SI.WATT);
			}
			setActualDataCenterMetric(builder, dataCenterName, MetricCatalog.RENEWABLE_POWER.getName(),
					date.getMillis(), renPowerAmount.doubleValue(WATT));
			setActualDataCenterMetric(builder, dataCenterName, "electricity_co2_factor",
					date.getMillis(), dataCenter.getCo2Factor().doubleValue(Units.KG_PER_KWH));
			setActualDataCenterMetric(builder, dataCenterName, "electricity_consumption_price",
					date.getMillis(), dataCenter.getConsumptionPrice().doubleValue(Units.EUR_PER_KWH));
			setActualDataCenterMetric(builder, dataCenterName, "electricity_primary_energy_factor",
					date.getMillis(), dataCenter.getPrimaryEnergyFactor());
			for (HdbEascMetrics easc : dataCenter.getEascs()) {
				String eascName = easc.getEascName();
				for (HdbActivityMetrics activity : easc.getActivities()) {
					String activityName = activity.getActivityName();
					setActualActivityMetric(builder, dataCenterName, eascName, activityName, 
							MetricCatalog.BIZPERF_ITEMS_RATE.getName(), date.getMillis(), 
							activity.getInstantBusinessPerformance().getEstimatedValue());
					Amount<?> cumulativePerf = activity.getCumulativeBusinessPerformance();
					if (cumulativePerf != null) {
						setActualActivityMetric(builder, dataCenterName, eascName, activityName, 
								MetricCatalog.BIZPERF_ITEMS.getName(), date.getMillis(),
								cumulativePerf.getEstimatedValue());
					}
					setActualActivityMetric(builder, dataCenterName, eascName, activityName, "working_mode",
							date.getMillis(), activity.getWorkingModeValue());
					setActualActivityMetric(builder, dataCenterName, eascName, activityName, 
							MetricCatalog.POWER.getName(), date.getMillis(), activity.getPower().doubleValue(WATT));
				}
			}
		}
		uploadMetrics(builder);
	}
	
	public void writeExecutionPlanMetrics(List<DataCenterExecutionPlan> dataCenterExecutionPlans) {
		for (DataCenterExecutionPlan executionPlan : dataCenterExecutionPlans) {
			String dataCenter = executionPlan.getDataCenterName();
			uploadPowerPlanMetrics(dataCenter, "IDEAL", executionPlan.getIdealPowerPlan());
			uploadPowerPlanMetrics(dataCenter, "PLANNED", executionPlan.getConsolidatedPowerPlan());
		}
	}
	
	private void uploadPowerPlanMetrics(String assetCode, String reference, PowerPlan powerPlan) {
		if (powerPlan.getTimeSlotDuration().longValue(NonSI.MINUTE) != 15) {
			throw new IllegalArgumentException("Historical DB requires 15-minute time slots");
		}
		MetricBuilder builder = MetricBuilder.getInstance();
		Metric metric = newDataCenterMetric(builder, assetCode, MetricCatalog.POWER.getName(), reference,
		                                    GRANULARITY_15_MIN);
		long slotDuration = powerPlan.getTimeSlotDuration().to(MILLI(SECOND)).getExactValue();
		long startMillis = powerPlan.getDateFrom().getMillis();
		for (TimeSlotPower quotas : powerPlan.getPowerQuotas()) {
			long timestamp = startMillis + slotDuration * quotas.getTimeSlot();
			double power = quotas.getPower().doubleValue(WATT);
			metric.addDataPoint(timestamp, power);
		}
		uploadMetrics(builder);
	}
	
	public void writeStatusMetrics(List<DataCenterStatus> dataCenterStatuses) {
		for (DataCenterStatus status : dataCenterStatuses) {
			if (status.getTimeSlotDuration().longValue(NonSI.MINUTE) != 15) {
				throw new IllegalArgumentException("Historical DB requires 15-minute time slots");
			}
			uploadAlertStates(status);
			uploadAlertMessages(status);
		}
	}
	
	private void uploadAlertStates(DataCenterStatus status) {
		MetricBuilder builder = MetricBuilder.getInstance();
		String dataCenterName = status.getDataCenterName();
		setAlertStateMetrics(builder, dataCenterName, null, null, status, status.getAlerts(), AlertType.RENPCT);
		for (EascStatus easc : status.getEascs()) {
			String eascName = easc.getEascName();
			for (ActivityStatus activity : easc.getActivities()) {
				String activityName = activity.getActivityName();
				setAlertStateMetrics(builder, dataCenterName, eascName, activityName, status, activity.getAlerts(),
						AlertType.BIZPERF);
			}
		}
		uploadMetrics(builder);
	}
	
	private void setAlertStateMetrics(MetricBuilder builder, String dataCenterName, String eascName,
			String activityName, TimeSlotBasedEntity timeRange, List<StatusAlert> alerts, AlertType alertType) {
		// Any previous alert states for the same interval must be overwritten, so initialize to none and then
		// set the alert state for time slots that have a relevant alarm.
		int numOfTimeSlots = timeRange.getNumOfTimeSlots();
		int[] alertStates = new int[numOfTimeSlots];
		Arrays.fill(alertStates, HdbAlertState.NONE);
		for (StatusAlert alert : alerts) {
			if (alert.getType().equals(alertType)) {
				int alertState = HdbAlertState.from(alert.getSeverity());
				Arrays.fill(alertStates, alert.getStartTimeSlot(), alert.getEndTimeSlot(), alertState);
			}
		}
		Metric metric;
		if (activityName == null) {
			metric = newDataCenterMetric(builder, dataCenterName, METRIC_ALERT_STATE, REFERENCE_ACTUAL, 
					GRANULARITY_15_MIN);
		} else {
			metric = newActivityMetric(builder, dataCenterName, eascName, activityName, METRIC_ALERT_STATE,
					REFERENCE_ACTUAL, GRANULARITY_15_MIN);
		}
		long slotDuration = timeRange.getTimeSlotDuration().to(MILLI(SECOND)).getExactValue();
		long startMillis = timeRange.getDateFrom().getMillis();
		for (int i = 0; i < alertStates.length; i++) {
			long timestamp = startMillis + slotDuration * i;
			metric.addDataPoint(timestamp, alertStates[i]);
		}
	}
	
	private void uploadAlertMessages(DataCenterStatus status) {
		String dataCenterName = status.getDataCenterName();
		DateTime dateFrom = status.getDateFrom();
		DateTime dateTo = status.getDateTo();
		EnergisAlertFeed feed = new EnergisAlertFeed(companyCode);
		EnergisAlertFeedDelete delete = new EnergisAlertFeedDelete();
		delete.setAssetCode(dataCenterName);
		delete.setOccurringSinceFrom(dateFrom);
		delete.setOccurringSinceTo(dateTo);
		feed.setDelete(delete);
		feed.setSave(EnergisAlert.list(status));
		URI feedUri = URI.create(energisUrl + "/v1/alerts/feed");
		RequestEntity<EnergisAlertFeed> request = 
				RequestEntity.post(feedUri)
				             .header("X-Api-Key", energisApiKey)
				             .body(feed);
		restOps.exchange(request, Void.class);
	}
	
	private void setActualDataCenterMetric(MetricBuilder builder, String dataCenterName, String metricName,
			long timestamp,	double value) {
		Metric metric = newDataCenterMetric(builder, dataCenterName, metricName, REFERENCE_ACTUAL, GRANULARITY_5_MIN);
		metric.addDataPoint(timestamp, value);
	}
	
	private Metric newDataCenterMetric(MetricBuilder builder, String dataCenterName, String metricName, 
			String reference, String granularity) {
		return builder.addMetric(metricName)
		              .addTag("reference", reference)
		              .addTag("assetType", "SITE")
		              .addTag("assetCode", dataCenterName)
		              .addTag("companyCode", companyCode)
		              .addTag("granularity", granularity);
	}
	
	private void setActualActivityMetric(MetricBuilder builder, String dataCenterName, String eascName, 
			String activityName, String metricName, long timestamp, double value) {
		Metric metric = newActivityMetric(builder, dataCenterName, eascName, activityName, metricName, REFERENCE_ACTUAL, 
				GRANULARITY_5_MIN);
		metric.addDataPoint(timestamp, value);
	}
	
	private Metric newActivityMetric(MetricBuilder builder, String dataCenterName, String eascName, String activityName, 
			String metricName, String reference, String granularity) {
		return builder.addMetric(metricName)
		              .addTag("reference", reference)
		              .addTag("assetType", "SITE_ENTITY")
		              .addTag("assetCode", dataCenterName + "." + eascName + "." + activityName)
		              .addTag("companyCode", companyCode)
		              .addTag("granularity", granularity);
	}
	
	private void uploadMetrics(MetricBuilder metricBuilder) {
		HttpClient client = null;
		try {
			client = new HttpClient(hdbUrl);
			// Don't do retries at the KairosDB client level since the underlying Apache HttpClient already does retries
			// for transient errors. Doing more retries in the KairosDB client may cause the upload operation to last for
			// minutes unsuccessfully and accumulate delays in control loops scheduling. So it's better to abort a metric
			// upload than losing planning cycles.
			client.setRetryCount(0);
			Response response = client.pushMetrics(metricBuilder);
			int status = response.getStatusCode();
			if (status < 200 || status >= 300) {
				throw newUploadException("HTTP " + status + ": " + response.getErrors(), null);
			}
		} catch (IOException e) {
			throw newUploadException(null, e);
		} catch (URISyntaxException e) {
			throw newUploadException(null, e);
		} finally {
			if (client != null) {
				try { client.shutdown(); } catch (Exception e) {}
			}
		}
	}
	
	private RuntimeException newUploadException(String description, Throwable e) {
		String message = "Error uploading metrics to the historical DB";
		if (description != null) {
			message += ": " + description;
		}
		return new RuntimeException(message, e);
	}
	
}
