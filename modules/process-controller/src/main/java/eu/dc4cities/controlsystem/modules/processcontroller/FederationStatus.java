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

import eu.dc4cities.configuration.technical.TechnicalConfiguration;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.TimeSlotPower;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterExecutionPlan;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterPower;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterStatus;
import eu.dc4cities.controlsystem.model.easc.*;
import eu.dc4cities.controlsystem.model.erds.DataCenterForecast;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.model.erds.TimeSlotErdsForecast;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Power;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import java.util.*;

/**
 * Holds the status of all entities in the federation managed by the controller, including current forecasts, plans and
 * actual metrics to be used in feedback loops.
 */
public class FederationStatus {
	
	private TechnicalConfiguration technicalConfiguration;
	// The time slot width in minutes
	private int timeSlotWidth;
	// The start date of the current time slot (for which items are accumulated in eascMetricsBuffer)
	private DateTime timeSlotStart;
	// The end date of the current time slot
	private DateTime timeSlotEnd;
	
	// The latest forecast from the power loop
	private List<DataCenterForecast> dataCenterForecasts;
	// The latest execution plans from the control loop
	private List<DataCenterExecutionPlan> dataCenterExecutionPlans;
	// The latest statuses from the control loop
	private List<DataCenterStatus> dataCenterStatuses;
	// The most recent EASC metrics measurement
	private DateTime latestEascMetricsDate;
	private List<EascMetrics> latestEascMetrics;
	// The EASC metrics measurements collected during the current time slot; at the end of the time slot they are
	// aggregated in order to update dataCenterPowerActuals and eascServiceLevels
	private List<EascMetricsItem> eascMetricsBuffer = new LinkedList<>();
	// The actual power usage of each data center in the federation, from the start of the current day up to the
	// current time slot
	private List<DataCenterPower> dataCenterPowerActuals = new LinkedList<>();
	// The actual performance levels of each EASC in the federation, from the start of the current day up to the
	// current time slot
	private List<EascServiceLevels> eascServiceLevels = new LinkedList<>();
	
	/**
	 * Creates a new FederationStatus.
	 * 
	 * @param firstTimeSlotStart the start date of the first time slot to be tracked in the actual metrics
	 * @param timeSlotWidth the time slot width in minutes
	 */
	public FederationStatus(DateTime firstTimeSlotStart, TechnicalConfiguration technicalConfiguration) {
		this.technicalConfiguration = technicalConfiguration;
		this.timeSlotWidth = technicalConfiguration.getTimeSlotWidth();
		this.timeSlotStart = firstTimeSlotStart;
		this.timeSlotEnd = timeSlotStart.plusMinutes(timeSlotWidth);
	}
	
	/**
	 * Returns the current data center forecasts stored in the federation status.
	 * 
	 * @return the current forecasts
	 */
	public List<DataCenterForecast> getDataCenterForecasts() {
		return dataCenterForecasts;
	}

	/**
	 * Returns a copy of the data center forecasts trimmed to the given time range. Throws an exception if the available
	 * forecasts do not cover the requested range.
	 * 
	 * @param timeRange the requested time range
	 * @return the forecast trimmed to the requested range
	 */
	public List<DataCenterForecast> getDataCenterForecasts(TimeSlotBasedEntity timeRange) {
		return DataCenterForecast.copyOfRange(dataCenterForecasts, timeRange.getDateFrom(), timeRange.getDateTo());
	}
	
	public void setDataCenterForecasts(List<DataCenterForecast> dataCenterForecasts) {
		this.dataCenterForecasts = dataCenterForecasts;
	}

	public List<DataCenterExecutionPlan> getDataCenterExecutionPlans() {
		return dataCenterExecutionPlans;
	}

	public void setDataCenterExecutionPlans(List<DataCenterExecutionPlan> dataCenterExecutionPlans) {
		this.dataCenterExecutionPlans = dataCenterExecutionPlans;
	}

	public List<DataCenterStatus> getDataCenterStatuses() {
		return dataCenterStatuses;
	}

	public void setDataCenterStatuses(List<DataCenterStatus> dataCenterStatuses) {
		this.dataCenterStatuses = dataCenterStatuses;
	}

	public List<EascMetrics> getLatestEascMetrics() {
		return latestEascMetrics;
	}
	
	public List<DataCenterPower> getDataCenterPowerActuals() {
		return dataCenterPowerActuals;
	}

	public List<EascServiceLevels> getEascServiceLevels() {
		return eascServiceLevels;
	}

	/**
	 * Updates the metrics for the day with the given measurements from the EASCs. If the monitoring interval is shorter
	 * than the time slot width, new measurements are stored in a buffer until dateNow reaches the end of the time slot.
	 * At that point, dataCenterPowerActuals and eascServiceLevels are updated with metrics for the time slot that has
	 * completed. The measurement collected at the end of the time slot is considered the last one in that time slot
	 * (not the first one in the next time slot).
	 * <p>
	 * dataCenterPowerActuals contains power consumption values for each time slot since the start of the current day;
	 * for each time slot, the value is calculated as the average among all measurements collected for the time slot.
	 * <p>
	 * eascServiceLevels contains performance values for each activity, for each time slot since the start of the
	 * current day; if an activity spans over multiple data centers, the performance of the activity is obtained as the
	 * sum of performances in single data centers. When multiple measurements per time slot are collected, the instant
	 * performance of the time slot is calculated as the average among all measurements, while the cumulative
	 * performance is the same as the value of the last measurement (the EASC is assumed to return a monotone increasing
	 * value that is reset only at the start of a new SLO). The EASC is required to measure instant performance for
	 * service-oriented activities and cumulative performance for task-oriented activities, but it may provide both
	 * values if it wants to. So whether eascServiceLevels contains either instant or cumulative performance or both of
	 * them depends on the data returned by the EASC.
	 * <p>
	 * This class assumes day-based SLAs, so all metrics are reset at midnight.
	 * 
	 * @param dateNow the date when updated metrics have been collected
	 * @param eascMetrics the updated metrics
	 */
	public void updateEascMetrics(DateTime dateNow, List<EascMetrics> eascMetrics) {
		// Always save the latest metrics even if before timeSlotStart as they represent the last known state of the
		// system
		latestEascMetricsDate = dateNow;
		latestEascMetrics = eascMetrics;
		if (dateNow.compareTo(timeSlotStart) <= 0) {
			// Don't consider this measurement as it belongs to the previous time slot
			return;
		}
		eascMetricsBuffer.add(new EascMetricsItem(dateNow, eascMetrics));
		// Do the check in a loop in case errors in monitoring have caused updates to be skipped
		while (dateNow.compareTo(timeSlotEnd) >= 0) {
			List<EascMetricsItem> timeSlotMetrics = new LinkedList<>();
			Iterator<EascMetricsItem> iterator = eascMetricsBuffer.iterator();
			while (iterator.hasNext()) {
				EascMetricsItem item = iterator.next();
				if (item.date.compareTo(timeSlotEnd) <= 0) {
					timeSlotMetrics.add(item);
					iterator.remove();
				} else {
					break;
				}
			}
			if (timeSlotEnd.getMillisOfDay() == 0) {
				// Reset metrics at midnight to start a new SLA day
				dataCenterPowerActuals.clear();
				eascServiceLevels.clear();
			} else {
				addTimeSlotActuals(timeSlotMetrics);
			}
			timeSlotStart = timeSlotEnd;
			timeSlotEnd = timeSlotStart.plusMinutes(timeSlotWidth);
		}
	}
	
	// Appends a new time slot to actual metrics, with values calculated as the average among arguments
	private void addTimeSlotActuals(List<EascMetricsItem> eascMetricsItems) {
		// The list of power measurements for each data center, collected within the time slot.
		// Use a linked hash map to achieve predictable order in output objects (required for test verifications, also
		// useful when inspecting logs for debugging)
		Map<String, List<Amount<Power>>> dataCenterPowerMeasurements = new LinkedHashMap<>();
		Map<String, Map<String, ActivityPerformance>> activityPerfsMap = new LinkedHashMap<>();
		for (EascMetricsItem metricsItem : eascMetricsItems) {
			// The total power consumptions of each data center at the time of the monitoring call
			Map<String, Amount<Power>> dataCenterPowerTotals = new LinkedHashMap<>();
			for (EascMetrics eascMetrics : metricsItem.eascMetrics) {
				String eascName = eascMetrics.getEascName();
				for (ActivityMetrics activity : eascMetrics.getActivities()) {
					String activityName = activity.getName();
					Amount<?> totalInstantPerf = null;
					Amount<?> totalCumulativePerf = null;
					for (ActivityDataCenterMetrics dataCenter : activity.getDataCenters()) {
						String dataCenterName = dataCenter.getDataCenterName();
						Amount<Power> dataCenterPower = dataCenter.getPower();
						sumPower(dataCenterName, dataCenterPower, dataCenterPowerTotals);
						Amount<?> instantPerf = dataCenter.getInstantBusinessPerformance();
						if (instantPerf != null) {
							totalInstantPerf = sumAmount(instantPerf, totalInstantPerf);
						}
						Amount<?> cumulativePerf = dataCenter.getCumulativeBusinessPerformance();
						if (cumulativePerf != null) {
							totalCumulativePerf = sumAmount(cumulativePerf, totalCumulativePerf);
						}
					}
					// At least one between totalInstantPerf and totalCumulativePerf is non-null at this point since the
					// EASC is required to provide at least one of those
					updatePerfMeasurements(eascName, activityName, totalInstantPerf, totalCumulativePerf, 
							activityPerfsMap);
				}
			}
			for (Map.Entry<String, Amount<Power>> total : dataCenterPowerTotals.entrySet()) {
				addPowerMeasurement(total.getKey(), total.getValue(), dataCenterPowerMeasurements);
			}
		}
		addPowerActualsForCurrentTimeSlot(dataCenterPowerMeasurements);
		addServiceLevelsForCurrentTimeSlot(activityPerfsMap);
	}
	
	private void sumPower(String dataCenter, Amount<Power> power, 
			Map<String, Amount<Power>> dataCenterTotals) {
		Amount<Power> powerTotal = dataCenterTotals.get(dataCenter);
		if (powerTotal == null) {
			powerTotal = power;
		} else {
			powerTotal = powerTotal.plus(power);
		}
		dataCenterTotals.put(dataCenter, powerTotal);
	}
	
	private Amount<?> sumAmount(Amount<?> amount, Amount<?> total) {
		if (total == null) {
			return amount;
		} else {
			return total.plus(amount);
		}
	}
	
	private void addPowerMeasurement(String dataCenter, Amount<Power> power, 
			Map<String, List<Amount<Power>>> dataCenterMeasurements) {
		List<Amount<Power>> measurements = dataCenterMeasurements.get(dataCenter);
		if (measurements == null) {
			measurements = new LinkedList<>();
			dataCenterMeasurements.put(dataCenter, measurements);
		}
		measurements.add(power);
	}
	
	private void updatePerfMeasurements(String easc, String activity, Amount<?> instantPerf, Amount<?> cumulativePerf,
			Map<String, Map<String, ActivityPerformance>> measurementsMap) {
		Map<String, ActivityPerformance> eascMeasurements = measurementsMap.get(easc);
		if (eascMeasurements == null) {
			eascMeasurements = new LinkedHashMap<>();
			measurementsMap.put(easc, eascMeasurements);
		}
		ActivityPerformance activityPerf = eascMeasurements.get(activity);
		if (activityPerf == null) {
			activityPerf = new ActivityPerformance();
			eascMeasurements.put(activity, activityPerf);
		}
		if (instantPerf != null) {
			activityPerf.instantPerformances.add(instantPerf);
		}
		if (cumulativePerf != null) {
			activityPerf.cumulativePerformance = cumulativePerf;
		}
	}
	
	private void addPowerActualsForCurrentTimeSlot(Map<String, List<Amount<Power>>> powerMeasurementsMap) {
		for (Map.Entry<String, List<Amount<Power>>> dataCenterEntry : powerMeasurementsMap.entrySet()) {
			String dataCenter = dataCenterEntry.getKey();
			Amount<Power> timeSlotAvgPower = calcAveragePower(dataCenterEntry.getValue());
			addPowerActualForCurrentTimeSlot(dataCenter, timeSlotAvgPower);
		}
	}
	
	private Amount<Power> calcAveragePower(List<Amount<Power>> powerValues) {
		double total = 0;
		for (Amount<Power> value : powerValues) {
			total += value.getEstimatedValue();
		}
		long average = Math.round(total / powerValues.size());
		return Amount.valueOf(average, powerValues.get(0).getUnit());
	}
	
	private void addPowerActualForCurrentTimeSlot(String dataCenter, Amount<Power> powerActual) {
		DataCenterPower dataCenterPower = getDataCenterPowerActuals(dataCenter);
		List<TimeSlotPower> powerValues = dataCenterPower.getPowerValues();
		int lastTimeSlot;
		if (powerValues.size() > 0) {
			lastTimeSlot = powerValues.get(powerValues.size() - 1).getTimeSlot();
		} else {
			lastTimeSlot = -1;
		}
		// Check for gaps in measurements (may be caused by repeated errors in REST monitoring calls)
		long gapMillis = timeSlotStart.getMillis() - dataCenterPower.getDateTo().getMillis();
		int gapTimeSlots = (int) (gapMillis / (timeSlotWidth * 60000));
		TimeSlotPower newPowerValue = new TimeSlotPower(lastTimeSlot + gapTimeSlots + 1, powerActual);
		powerValues.add(newPowerValue);
		dataCenterPower.setDateTo(timeSlotEnd);
	}
	
	private DataCenterPower getDataCenterPowerActuals(String dataCenter) {
		for (DataCenterPower powerActuals : dataCenterPowerActuals) {
			if (powerActuals.getDataCenterName().equals(dataCenter)) {
				return powerActuals;
			}
		}
		DataCenterPower powerActuals = new DataCenterPower(dataCenter);
		powerActuals.setDateFrom(timeSlotStart);
		powerActuals.setDateTo(timeSlotStart);
		powerActuals.setTimeSlotDuration(Amount.valueOf(timeSlotWidth, NonSI.MINUTE));
		dataCenterPowerActuals.add(powerActuals);
		return powerActuals;
	}
	
	private void addServiceLevelsForCurrentTimeSlot(Map<String, Map<String, ActivityPerformance>> activityPerfsMap) {
		for (Map.Entry<String, Map<String, ActivityPerformance>> eascEntry : activityPerfsMap.entrySet()) {
			String easc = eascEntry.getKey();
			for (Map.Entry<String, ActivityPerformance> activityEntry : eascEntry.getValue().entrySet()) {
				String activity = activityEntry.getKey();
				ActivityPerformance activityPerf = activityEntry.getValue();
				List<Amount<?>> perfValues = activityPerf.instantPerformances;
				Amount<?> perfAvg = null;
				if (perfValues.size() > 0) {
					Amount<?> perfTotal = perfValues.get(0);
					for (int i = 1; i < perfValues.size(); i++) {
						perfTotal = perfTotal.plus(perfValues.get(i));
					}
					perfAvg = perfTotal.divide(perfValues.size());
				}
				addServiceLevelForCurrentTimeSlot(easc, activity, perfAvg, activityPerf.cumulativePerformance);
			}
		}
	}
	
	private void addServiceLevelForCurrentTimeSlot(String easc, String activity, Amount<?> instantPerf, 
			Amount<?> cumulativePerf) {
		ActivityServiceLevels activityLevels = getActivityServiceLevels(easc, activity);
		ServiceLevel serviceLevel = new ServiceLevel(timeSlotStart, timeSlotEnd);
		serviceLevel.setInstantBusinessPerformance(instantPerf);
		serviceLevel.setCumulativeBusinessPerformance(cumulativePerf);
		activityLevels.getServiceLevels().add(serviceLevel);
	}
	
	private ActivityServiceLevels getActivityServiceLevels(String easc, String activity) {
		for (EascServiceLevels eascLevels : eascServiceLevels) {
			if (eascLevels.getEascName().equals(easc)) {
				List<ActivityServiceLevels> activityLevelsList = eascLevels.getActivityServiceLevels();
				for (ActivityServiceLevels activityLevels : activityLevelsList) {
					if (activityLevels.getActivityName().equals(activity)) {
						return activityLevels;
					}
				}
				ActivityServiceLevels activityLevels = new ActivityServiceLevels(activity);
				activityLevelsList.add(activityLevels);
				return activityLevels;
			}
		}
		EascServiceLevels eascLevels = new EascServiceLevels(easc);
		ActivityServiceLevels activityLevels = new ActivityServiceLevels(activity);
		eascLevels.getActivityServiceLevels().add(activityLevels);
		eascServiceLevels.add(eascLevels);
		return activityLevels;
	}
	
	private static class EascMetricsItem {
		
		public DateTime date;
		public List<EascMetrics> eascMetrics;
		
		public EascMetricsItem(DateTime date, List<EascMetrics> eascMetrics) {
			this.date = date;
			this.eascMetrics = eascMetrics;
		}
		
	}
	
	private static class ActivityPerformance {
		public List<Amount<?>> instantPerformances = new LinkedList<>();
		public Amount<?> cumulativePerformance;		
	}
	
	/**
	 * Returns the historical database metrics for all data centers in the federation, based on the latest EASC metrics
	 * and the data center forecasts for the corresponding time slot. 
	 * 
	 * @return the list of data center HDB metrics
	 */
	@SuppressWarnings("unchecked")
	public List<HdbDataCenterMetrics> getHdbMetrics() {
		// HDB metrics are aggregated by data center instead of by EASC
		Map<String, HdbDataCenterMetrics> aggregateMetrics = new HashMap<>();
		for (EascMetrics easc : latestEascMetrics) {
			String eascName = easc.getEascName();
			for (ActivityMetrics activity : easc.getActivities()) {
				String activityName = activity.getName();
				for (ActivityDataCenterMetrics dataCenter : activity.getDataCenters()) {
					String dataCenterName = dataCenter.getDataCenterName();
					HdbDataCenterMetrics aggregateItem = aggregateMetrics.get(dataCenterName);
					if (aggregateItem == null) {
						aggregateItem = new HdbDataCenterMetrics(dataCenterName);
						aggregateMetrics.put(dataCenterName, aggregateItem);
					}
					double pue = technicalConfiguration.getDataCenter(dataCenterName).getPue();
					Amount<Power> power = dataCenter.getPower();
					long puePowerValue = Math.round(power.getEstimatedValue() * pue);
					Amount<Power> puePower = Amount.valueOf(puePowerValue, power.getUnit());
					aggregateItem.setPowerConsumption((Amount<Power>) 
							sumAmount(puePower, aggregateItem.getPowerConsumption()));
					HdbEascMetrics hdbEasc = getHdbEasc(aggregateItem, eascName);
					HdbActivityMetrics hdbActivity = new HdbActivityMetrics(activityName);
					hdbEasc.addActivity(hdbActivity);
					hdbActivity.setWorkingModeValue(dataCenter.getWorkingModeValue());
					hdbActivity.setInstantBusinessPerformance(dataCenter.getInstantBusinessPerformance());
					hdbActivity.setCumulativeBusinessPerformance(dataCenter.getCumulativeBusinessPerformance());
					hdbActivity.setPower(puePower);
				}
			}
		}
		List<HdbDataCenterMetrics> hdbMetrics = new ArrayList<>(aggregateMetrics.values());
		for (HdbDataCenterMetrics dataCenter : hdbMetrics) {
			setPowerSupplyMetrics(latestEascMetricsDate, dataCenter);
		}
    	return hdbMetrics;
	}
	
	private HdbEascMetrics getHdbEasc(HdbDataCenterMetrics dataCenter, String eascName) {
		HdbEascMetrics easc = dataCenter.getEasc(eascName);
		if (easc == null) {
			easc = new HdbEascMetrics(eascName);
			dataCenter.addEasc(easc);
		}
		return easc;
	}
	
    // Set fake power supply measurements based on the forecasts
    @SuppressWarnings("unchecked")
	private void setPowerSupplyMetrics(DateTime date, HdbDataCenterMetrics hdbDataCenter) {
    	String dataCenterName = hdbDataCenter.getDataCenterName();
    	DataCenterForecast dataCenterForecast  = getDataCenterForecast(dataCenterName);
    	if (dataCenterForecast == null) {
    		throw new IllegalArgumentException("Forecast not found for " + dataCenterName);
    	}
    	List<ErdsForecast> erdsList = dataCenterForecast.getErdsForecasts();
    	long durationMillis = erdsList.get(0).getTimeSlotDuration().longValue(SI.MILLI(SI.SECOND));
    	int timeSlotNumber = (int) ((date.getMillis() - erdsList.get(0).getDateFrom().getMillis()) / durationMillis);
    	for (ErdsForecast erds : erdsList) {
    		TimeSlotErdsForecast timeSlotForecast = erds.getTimeSlotForecasts().get(timeSlotNumber);
    		if (timeSlotForecast.getRenewablePercentage().longValue(NonSI.PERCENT) == 100) {
    			hdbDataCenter.setRenewablePower((Amount<Power>) 
    					sumAmount(timeSlotForecast.getPower(), hdbDataCenter.getRenewablePower()));
    			// Make assumptions to simplify metrics calculations
    			if (timeSlotForecast.getCo2Factor().getEstimatedValue() > 0
    					|| timeSlotForecast.getPrimaryEnergyFactor() != 1
    					|| timeSlotForecast.getConsumptionPrice().getEstimatedValue() > 0) {
    				throw new IllegalArgumentException("Totally renewable sources are assumed to have Co2Factor = 0, "
    						+ "primaryEnergyFactor = 1 and consumptionPrice = 0");
    			}
    		} else {
    			if (hdbDataCenter.getGridRenewablePercentage() != null) {
    				throw new IllegalArgumentException("Found multiple grid power sources but only one is supported");
    			}
    			hdbDataCenter.setGridRenewablePercentage(timeSlotForecast.getRenewablePercentage());
    			hdbDataCenter.setCo2Factor(timeSlotForecast.getCo2Factor());
        		hdbDataCenter.setConsumptionPrice(timeSlotForecast.getConsumptionPrice());
        		hdbDataCenter.setPrimaryEnergyFactor(timeSlotForecast.getPrimaryEnergyFactor());
    		}
    	}
    }
    
    private DataCenterForecast getDataCenterForecast(String dataCenter) {
    	for (DataCenterForecast forecast : dataCenterForecasts) {
    		if (forecast.getDataCenterName().equals(dataCenter)) {
    			return forecast;
    		}
    	}
    	return null;
    }
	
}
