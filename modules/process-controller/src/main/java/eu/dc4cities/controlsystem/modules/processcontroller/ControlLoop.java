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

import eu.dc4cities.configuration.goal.Goal;
import eu.dc4cities.configuration.goal.GoalConfiguration;
import eu.dc4cities.configuration.goal.Objective;
import eu.dc4cities.configuration.technical.DataCenterConfiguration;
import eu.dc4cities.configuration.technical.TechnicalConfiguration;
import eu.dc4cities.controlsystem.model.PowerPlan;
import eu.dc4cities.controlsystem.model.TimeParameters;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.TimeSlotPower;
import eu.dc4cities.controlsystem.model.datacenter.*;
import eu.dc4cities.controlsystem.model.easc.*;
import eu.dc4cities.controlsystem.model.erds.DataCenterForecast;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.model.util.TimeRangeUtils;
import eu.dc4cities.controlsystem.modules.*;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.measure.quantity.Power;
import javax.measure.unit.SI;
import java.util.*;

/**
 * Executes the EASC control loop.
 */
public class ControlLoop {

	private static final Logger logger = LoggerFactory.getLogger(ControlLoop.class);
	
	private TechnicalConfiguration technicalConfig;
	private GoalConfiguration goalConfig;
    private Map<String, PowerPlanner> powerPlanners;
    private Map<String, PowerSplitter> powerSplitters;
    private EascHandler eascHandler;
    private OptionConsolidator optionConsolidator;
	private Map<String, EscalationManager> escalationManagers;

    /**
     * Creates a new instance using the given components. Components that work on a single data center per instance are
     * provided in a map, with one item for each data center in the federation and the data center names as the keys.
     * 
     * @param technicalConfig the system technical configuration
     * @param powerPlanners the map of power planners for the federation
     * @param powerSplitters the map of power splitters for the federation
     * @param eascHandler the EASC handler (a single instance for the whole federation)
     * @param optionConsolidator the option consolidator (a single instance for the whole federation)
     * @param escalationManagers the map of escalation managers for the federation or {@code null} to skip escalation
     */
    public ControlLoop(TechnicalConfiguration technicalConfig, GoalConfiguration goalConfig, 
    		Map<String, PowerPlanner> powerPlanners, Map<String, PowerSplitter> powerSplitters, EascHandler eascHandler,
    		OptionConsolidator optionConsolidator, Map<String, EscalationManager> escalationManagers) {
    	this.technicalConfig = technicalConfig;
    	this.goalConfig = goalConfig;
        this.powerPlanners = powerPlanners;
        this.powerSplitters = powerSplitters;
        this.eascHandler = eascHandler;
        this.optionConsolidator = optionConsolidator;
        this.escalationManagers = escalationManagers;
    }

    /**
     * Executes the EASC control loop, optimizing activity execution over the given time range.
     * {@code currentExecutionPlans} refers to the execution plans currently in use for each data center
     * in the federation, i.e. the plans calculated during the previous optimization run.<br/>
     * {@code dataCenterPowerActuals} and {@code eascServiceLevels} state the actual power usage and performance
     * achieved from the start of the current SLA period up to the last time slot before the beginning of
     * {@code timeParameters}.
     * 
     * @param timeParameters the time range to optimize
     * @param dataCenterForecasts the energy forecasts for all data centers in the federation
     * @param dataCenterPowerActuals the actual power consumption for all data centers in the federation
     * @param eascServiceLevels the actual service levels achieved for all EASCs in the federation
     * @param eascMetrics the current metrics values for all EASCs in the federation
     * @param currentExecutionPlans the current execution plans for all data centers in the federation or
     *        {@code null} if this is the first run
     * @return the list of execution plans prepared for each data center
     */
    public List<DataCenterOptimization> execute(TimeParameters timeParameters, 
    		List<DataCenterForecast> dataCenterForecasts, List<DataCenterPower> dataCenterPowerActuals,
    		List<EascServiceLevels> eascServiceLevels, List<EascMetrics> eascMetrics,
    		List<DataCenterExecutionPlan> currentExecutionPlans) {
    	logger.debug("Starting EASC control loop with parameters: "
    			+ "timeParameters = " + timeParameters + ", "
    			+ "dataCenterForecasts = " + dataCenterForecasts + ", "
    			+ "dataCenterPowerActuals = " + dataCenterPowerActuals + ", "
    			+ "eascServiceLevels = " + eascServiceLevels + ", "
    			+ "eascMetrics = " + eascMetrics + ", "
    			+ "currentExecutionPlans = " + currentExecutionPlans);
    	if (currentExecutionPlans == null) {
    		// Execute a dry run to initialize the consolidated power plan
    		logger.debug("Optimization started without a consolidated power plan, executing dry run");
    		List<DataCenterResult> results = doExecute(timeParameters, dataCenterForecasts,
    				dataCenterPowerActuals,	eascServiceLevels, eascMetrics, null);
    		currentExecutionPlans = DataCenterResult.getExecutionPlans(results);
    		logger.debug("Re-running with consolidated power plan after dry run.");
    	} else {
    		// The power planner needs a consolidated plan spanning the whole optimization interval, so initialize any
    		// missing part with a dry run if necessary
    		TimeParameters missingRange = getMissingExecutionPlanTimeRange(timeParameters, currentExecutionPlans);
    		if (missingRange != null) {
        		logger.debug("Optimization started with a partial consolidated power plan, executing dry run");
        		List<DataCenterForecast> trimmedForecasts = DataCenterForecast.copyOfRange(dataCenterForecasts, 
        				missingRange.getDateFrom(), missingRange.getDateTo());
        		List<DataCenterResult> results = doExecute(missingRange, trimmedForecasts, dataCenterPowerActuals,
        				eascServiceLevels, eascMetrics, null);
        		List<DataCenterExecutionPlan> missingExecutionPlans = DataCenterResult.getExecutionPlans(results);
        		currentExecutionPlans = mergeConsolidatedPlans(currentExecutionPlans, missingExecutionPlans);
        		logger.debug("Extended execution plans after dry run: " + currentExecutionPlans);
        		logger.debug("Re-running with extended consolidated power plan after dry run.");
    		}
    	}
    	List<DataCenterResult> results = doExecute(timeParameters, dataCenterForecasts, 
    			dataCenterPowerActuals, eascServiceLevels, eascMetrics,	currentExecutionPlans);
    	return DataCenterResult.getOptimizations(results);
    }
    
    private TimeParameters getMissingExecutionPlanTimeRange(TimeParameters timeParameters, 
    		List<DataCenterExecutionPlan> executionPlans) {
    	DateTime availableEnd = executionPlans.get(0).getConsolidatedPowerPlan().getDateTo();
    	DateTime requestedEnd = timeParameters.getDateTo();
    	if (availableEnd.compareTo(requestedEnd) < 0) {
    		TimeParameters missingRange = new TimeParameters(timeParameters);
    		missingRange.setDateFrom(availableEnd);
    		missingRange.setDateTo(requestedEnd);
    		return missingRange;
    	} else {
    		return null;
    	}
    }
    
    private List<DataCenterExecutionPlan> mergeConsolidatedPlans(List<DataCenterExecutionPlan> plans1, 
    		List<DataCenterExecutionPlan> plans2) {
    	if (plans1.size() != plans2.size()) {
    		throw new IllegalArgumentException("The two lists of plans to merge have different sizes");
    	}
    	List<DataCenterExecutionPlan> merged = new ArrayList<>(plans1.size());
    	for (int i = 0; i < plans1.size(); i++) {
    		DataCenterExecutionPlan plan1 = plans1.get(i);
    		DataCenterExecutionPlan plan2 = plans2.get(i);
    		if (!plan1.getDataCenterName().equals(plan2.getDataCenterName())) {
    			throw new IllegalArgumentException("Data center names don't match");
    		}
    		DataCenterExecutionPlan mergedItem = new DataCenterExecutionPlan(plan1.getDataCenterName());
    		PowerPlan mergedPlan = new PowerPlan(plan1.getConsolidatedPowerPlan());
    		mergedPlan.append(plan2.getConsolidatedPowerPlan());
    		mergedItem.setConsolidatedPowerPlan(mergedPlan);
    		merged.add(mergedItem);
    	}
    	return merged;
    }
    
    private List<DataCenterResult> doExecute(TimeParameters timeParameters, 
    		List<DataCenterForecast> dataCenterForecasts, List<DataCenterPower> dataCenterPowerActuals,
    		List<EascServiceLevels> eascServiceLevels, List<EascMetrics> eascMetrics,
    		List<DataCenterExecutionPlan> currentExecutionPlans) {
    	boolean dryRun = (currentExecutionPlans == null);
    	TimeSlotBasedEntity timeRange = new TimeSlotBasedEntity(timeParameters);
    	List<DataCenterResult> dataCenterResults = calcPowerPlans(timeRange, dataCenterForecasts, 
    			currentExecutionPlans);
        logger.debug("Requesting EASC activity specifications...");
        List<EascActivitySpecifications> eascActivitySpecifications = 
        		eascHandler.getActivitySpecifications(timeParameters);
        logger.debug("Got activity specifications: " + eascActivitySpecifications);
        List<Objective> powerObjectives = getPowerObjectives();
        List<EascPowerPlan> mergedEascPlans = mergeEascPowerPlans(dataCenterResults);
        logger.debug("Merged EASC power plans: " + mergedEascPlans);
        logger.debug("Building activity plans...");
        List<DataCenterPowerPlan> dataCenterIdealPowerPlans = dryRun ? null : getPueIdealPowerPlans(dataCenterResults);
        List<EascActivityPlan> eascActivityPlans = optionConsolidator.buildActivityPlans(timeRange, powerObjectives, 
        		dataCenterForecasts, dataCenterIdealPowerPlans, mergedEascPlans, 
        		eascActivitySpecifications, dataCenterPowerActuals,	eascServiceLevels, eascMetrics);
        logger.debug("Optimized activity plans: " + eascActivityPlans);
        if (dryRun) {
        	logger.debug("Skipping sending of activity plans (this is a dry run).");
        } else {
        	logger.debug("Sending new activity plans...");
        	eascHandler.sendActivityPlans(eascActivityPlans);
            logger.debug("New activites scheduled");
        }
        calcConsolidatedPowerPlans(timeRange, eascActivityPlans, dataCenterResults);
        if (dryRun || escalationManagers == null) {
        	logger.debug("Skipping escalation manager (dry run or escalation manager not enabled).");
        } else {
        	determineStatus(timeParameters, dataCenterForecasts, eascActivitySpecifications, dataCenterPowerActuals,
        			eascServiceLevels, eascActivityPlans, dataCenterResults);
        }
    	return dataCenterResults;
    }

	private List<DataCenterResult> calcPowerPlans(TimeSlotBasedEntity timeRange,
			List<DataCenterForecast> dataCenterForecasts, List<DataCenterExecutionPlan> currentExecutionPlans) {
		List<DataCenterResult> results = new ArrayList<>(technicalConfig.getDataCenters().size());
    	for (DataCenterConfiguration dataCenterConfig : technicalConfig.getDataCenters()) {
    		String dataCenterName = dataCenterConfig.getName();
    		DataCenterForecast forecast = getDataCenterForecast(dataCenterName, dataCenterForecasts);
    		if (forecast == null) {
    			throw new IllegalArgumentException("Missing forecast for data center " + dataCenterName);
    		}
    		DataCenterResult result = new DataCenterResult();
            result.dataCenterName = dataCenterName;
            results.add(result);
    		double pue = dataCenterConfig.getPue();
    		// Skip calculating the ideal power plan in dry runs
    		if (currentExecutionPlans != null) {
    			DataCenterExecutionPlan executionPlan = getDataCenterExecutionPlan(dataCenterName, 
    					currentExecutionPlans);
    			if (executionPlan == null) {
        			throw new IllegalArgumentException("Missing execution plan for data center " + dataCenterName);
        		}
    			PowerPlan previousPlan = executionPlan.getConsolidatedPowerPlan();
    			if (previousPlan == null) {
        			throw new IllegalArgumentException("Found null consolidated power plan for data center " + 
        					dataCenterName);
        		} else if (previousPlan.getDateTo().compareTo(timeRange.getDateTo()) != 0) {
        			throw new IllegalArgumentException("Consolidated power plan for data center "
        					+ dataCenterName + " ends at " + previousPlan.getDateTo() + " but requested "
        					+ "time range ends at " + timeRange.getDateTo());
        		}
    			DateTime dateFrom = timeRange.getDateFrom();
    			if (previousPlan.getDateFrom().compareTo(dateFrom) != 0) {
    				// Trim to the requested time range as required by the power planner
    				previousPlan = previousPlan.copyOfRange(timeRange.getDateFrom());
    			}
    			PowerPlanner powerPlanner = powerPlanners.get(dataCenterName);
        		if (powerPlanner == null) {
        			throw new IllegalArgumentException("Power planner not configured for data center " + dataCenterName);
        		}
        		logger.debug(dataCenterName + " - Calculating ideal power plan...");
        		PowerPlan idealPlan = powerPlanner.calculateIdealPowerPlan(timeRange, forecast.getErdsForecasts(), 
        				previousPlan);
                logger.debug(dataCenterName + " - Ideal power plan: " + idealPlan);
                result.idealPowerPlan = idealPlan;
                PowerPlan pueIdealPlan = new PowerPlan(idealPlan);
                pueIdealPlan.scalePowerAmounts(1 / pue);
                logger.debug(dataCenterName + " - Ideal power plan reduced by PUE: " + pueIdealPlan);
                result.pueIdealPowerPlan = pueIdealPlan;
    		}
            PowerSplitter powerSplitter = powerSplitters.get(dataCenterName);
    		if (powerSplitter == null) {
    			throw new IllegalArgumentException("Power splitter not configured for data center " + dataCenterName);
    		}
            logger.debug(dataCenterName + " - Calculating EASC power quotas...");
            PowerPlan maxPowerPlan = calcMaxPowerPlan(timeRange, forecast.getErdsForecasts(), 
            		dataCenterConfig.getMaxPower(), pue);
            List<EascPowerPlan> eascPowerPlans = powerSplitter.splitPowerForEasc(maxPowerPlan);
            logger.debug(dataCenterName + " - EASC power quotas: " + eascPowerPlans);
            result.eascPowerPlans = eascPowerPlans;
    	}
    	return results;
    }
    
    private DataCenterForecast getDataCenterForecast(String dataCenterName, List<DataCenterForecast> forecasts) {
    	for (DataCenterForecast forecast : forecasts) {
    		if (forecast.getDataCenterName().equals(dataCenterName)) {
    			return forecast;
    		}
    	}
    	return null;
    }
    
    private PowerPlan calcMaxPowerPlan(TimeSlotBasedEntity timeRange, List<ErdsForecast> forecasts, 
    		int dataCenterMaxPower,	double pue) {
    	PowerPlan maxPowerPlan = new PowerPlan(timeRange, Amount.valueOf(0, SI.WATT));
    	// Assume all forecasts use the same time interval
    	DateTime rangeFrom = timeRange.getDateFrom();
    	DateTime forecastFrom = forecasts.get(0).getDateFrom();
    	if (rangeFrom.isBefore(forecastFrom)) {
    		throw new IllegalArgumentException("timeRange.dateFrom is before forecasts.dateFrom");
    	} else if (timeRange.getDateTo().isAfter(forecasts.get(0).getDateTo())) {
    		throw new IllegalArgumentException("timeRange.dateTo is after forecasts.dateTo");
    	}
    	int offset = TimeRangeUtils.getTimeSlotNumber(forecastFrom, rangeFrom, timeRange.getTimeSlotDuration());
    	List<TimeSlotPower> maxPowerQuotas = maxPowerPlan.getPowerQuotas();
    	for (int i = 0; i < maxPowerQuotas.size(); i++) {
    		TimeSlotPower powerQuota = maxPowerQuotas.get(i);
    		Amount<Power> maxPower = powerQuota.getPower();
    		for (ErdsForecast forecast : forecasts) {
    			maxPower = maxPower.plus(forecast.getTimeSlotForecasts().get(i + offset).getPower());
    		}
    		if (maxPower.getExactValue() > dataCenterMaxPower) {
    			maxPower = Amount.valueOf(dataCenterMaxPower, SI.WATT);
    		}
    		powerQuota.setPower(maxPower);
    		powerQuota.scalePower(1 / pue);
    	}
		return maxPowerPlan;
    }
    
    private DataCenterPower getDataCenterPower(String dataCenterName, List<DataCenterPower> powerItems) {
    	for (DataCenterPower powerItem : powerItems) {
    		if (powerItem.getDataCenterName().equals(dataCenterName)) {
    			return powerItem;
    		}
    	}
    	return null;
    }
    
    private DataCenterExecutionPlan getDataCenterExecutionPlan(String dataCenterName, 
    		List<DataCenterExecutionPlan> executionPlans) {
    	for (DataCenterExecutionPlan executionPlan : executionPlans) {
    		if (executionPlan.getDataCenterName().equals(dataCenterName)) {
    			return executionPlan;
    		}
    	}
    	return null;
    }
    
    private List<Objective> getPowerObjectives() {
    	return getPowerObjectives(null);
    }
    
    private List<Objective> getPowerObjectives(String dataCenterName) {
    	List<Objective> objectives = new LinkedList<>();
    	for (Goal goal : goalConfig.getGoals()) {
    		for (Objective objective : goal.getObjectives()) {
    			if (dataCenterName == null || objective.getDataCenterId().equals(dataCenterName)) {
    				objectives.add(objective);
    			}
    		}
    	}
    	return objectives;
    }
    
    private List<EascPowerPlan> mergeEascPowerPlans(List<DataCenterResult> sourceItems) {
    	Map<String, EascPowerPlan> targetPlans = new LinkedHashMap<>();
    	for (DataCenterResult item : sourceItems) {
    		for (EascPowerPlan sourcePlan : item.eascPowerPlans) {
    			String eascName = sourcePlan.getEascName();
    			EascPowerPlan targetPlan = targetPlans.get(eascName);
    			if (targetPlan == null) {
    				targetPlan = new EascPowerPlan(eascName);
    				targetPlan.copyIntervalFrom(sourcePlan);
    				targetPlans.put(eascName, targetPlan);
    			}
    			targetPlan.addDataCenterQuotas(sourcePlan.getDataCenterQuotas());
    		}
    	}
    	return new ArrayList<>(targetPlans.values());
    }
    
    private List<DataCenterPowerPlan> getPueIdealPowerPlans(List<DataCenterResult> dataCenterResults) {
    	List<DataCenterPowerPlan> dataCenterPowerPlans = new ArrayList<>(dataCenterResults.size());
    	for (DataCenterResult dataCenterResult : dataCenterResults) {
    		DataCenterPowerPlan dataCenterPowerPlan = new DataCenterPowerPlan(dataCenterResult.dataCenterName);
    		PowerPlan pueIdealPowerPlan = dataCenterResult.pueIdealPowerPlan;
    		dataCenterPowerPlan.copyIntervalFrom(pueIdealPowerPlan);
    		dataCenterPowerPlan.setPowerQuotas(pueIdealPowerPlan.getPowerQuotas());
    		dataCenterPowerPlans.add(dataCenterPowerPlan);
    	}
    	return dataCenterPowerPlans;
    }
    
    private void calcConsolidatedPowerPlans(TimeSlotBasedEntity timeRange, List<EascActivityPlan> eascActivityPlans, 
    		List<DataCenterResult> destItems) {
    	Map<String, Map<Integer, TimeSlotPower>> consolidatedDcQuotas = new HashMap<>();
    	for (DataCenterResult destItem : destItems) {
    		PowerPlan consolidatedPlan = new PowerPlan(timeRange, Amount.valueOf(0, SI.WATT));
    		destItem.consolidatedPowerPlan = consolidatedPlan;
    		consolidatedDcQuotas.put(destItem.dataCenterName, consolidatedPlan.getPowerQuotasMap());
    	}
    	for (EascActivityPlan eascActivityPlan : eascActivityPlans) {
    		for (Activity activity : eascActivityPlan.getActivities()) {
    			for (ActivityDataCenter dataCenter : activity.getDataCenters()) {
    				Map<Integer, TimeSlotPower> powerQuotas = consolidatedDcQuotas.get(dataCenter.getDataCenterName());
    				for (Work work : dataCenter.getWorks()) {
        				for (int ts = work.getStartTimeSlot(); ts < work.getEndTimeSlot(); ts++) {
        					powerQuotas.get(ts).addPower(work.getPower());
        				}
        			}
    			}
    		}
    	}
    	for (DataCenterResult destItem : destItems) {
    		String dataCenterName = destItem.dataCenterName;
    		DataCenterConfiguration dataCenterConfig = technicalConfig.getDataCenter(dataCenterName);
    		double pue = dataCenterConfig.getPue();
    		PowerPlan consolidatedPlan = destItem.consolidatedPowerPlan;
    		consolidatedPlan.scalePowerAmounts(pue);
    		logger.debug(dataCenterName + " - Consolidated power plan (including PUE): " + consolidatedPlan);
    	}
    }
    
    private void determineStatus(TimeParameters timeParameters, List<DataCenterForecast> dataCenterForecasts,
    		List<EascActivitySpecifications> eascActivitySpecifications, List<DataCenterPower> dataCenterPowerActuals,
    		List<EascServiceLevels> eascServiceLevels, List<EascActivityPlan> eascActivityPlans,
    		List<DataCenterResult> results) {
    	for (DataCenterResult result : results) {
    		String dataCenterName = result.dataCenterName;
    		EscalationManager escalationManager = escalationManagers.get(dataCenterName);
    		if (escalationManager == null) {
    			throw new IllegalArgumentException("Escalation Manager not configured for data center " 
    					+ dataCenterName);
    		}
    		List<Objective> powerObjectives = getPowerObjectives(dataCenterName);
    		DataCenterForecast forecast = getDataCenterForecast(dataCenterName, dataCenterForecasts);
    		if (forecast == null) {
    			throw new IllegalArgumentException("Missing forecast for data center " + dataCenterName);
    		}
    		// powerActual can be null when no previous metrics are available (at the beginning of the day)
    		DataCenterPower powerActual = getDataCenterPower(dataCenterName, dataCenterPowerActuals);
    		// Make sure dateFrom is the beginning of the day, the escalation manager needs to work on full days
    		TimeParameters extendedTimeParameters = new TimeParameters(timeParameters);
    		extendedTimeParameters.setDateFrom(timeParameters.getDateFrom().withTimeAtStartOfDay());
    		logger.debug(dataCenterName + " - Analyzing status...");
    		DataCenterStatus status = escalationManager.determineDcStatus(dataCenterName, extendedTimeParameters,
    				powerObjectives, forecast.getErdsForecasts(), eascActivitySpecifications, powerActual,
    				eascServiceLevels, eascActivityPlans);
    		logger.debug(dataCenterName + " - Status: " + status);
    		postProcessStatus(status, eascActivitySpecifications);
    		logger.debug(dataCenterName + " - Post-processed status: " + status);
    		result.status = status;
    	}
    }
    
    private void postProcessStatus(DataCenterStatus dataCenterStatus, 
    		List<EascActivitySpecifications> eascActivitySpecifications) {
    	// Extracts activity alerts produced by the escalation manager into dedicated EascStatus and ActivityStatus
    	// items, making sure there is an ActivityStatus for every activity in the data center, even the ones with no
    	// alerts (this is required for storing state metrics in the historical db).
    	String currentDataCenter = dataCenterStatus.getDataCenterName();
    	Map<ActivityStatusKey, ActivityStatus> activityStatuses = new HashMap<>();
    	for (EascActivitySpecifications eascSpecs : eascActivitySpecifications) {
    		String eascName = eascSpecs.getEascName();
    		EascStatus eascStatus = null;
    		for (ActivitySpecification activitySpec : eascSpecs.getActivitySpecifications()) {
    			String activityName = activitySpec.getActivityName();
    			for (DataCenterSpecification dataCenterSpec : activitySpec.getDataCenters()) {
    				if (dataCenterSpec.getDataCenterName().equals(currentDataCenter)) {
    					if (eascStatus == null) {
    						eascStatus = new EascStatus(eascName);
    						dataCenterStatus.addEasc(eascStatus);
    					}
    					ActivityStatus activityStatus = new ActivityStatus(activityName);
    					eascStatus.addActivity(activityStatus);
    					activityStatuses.put(new ActivityStatusKey(eascName, activityName), activityStatus);
    				}
    			}
    		}
    	}
    	Iterator<StatusAlert> alerts = dataCenterStatus.getAlerts().iterator();
    	while (alerts.hasNext()) {
    		StatusAlert alert = alerts.next();
    		if (alert.getActivityName() != null) {
    			ActivityStatusKey key = new ActivityStatusKey(alert.getEascName(), alert.getActivityName());
    			ActivityStatus activity = activityStatuses.get(key);
    			activity.addAlert(alert);
    			// EASC and activity name are now implied by the ActivityStatus object so unset them
    			alert.setEascName(null);
    			alert.setActivityName(null);
    			alerts.remove();
    		}
    	}
    }
    
    private static class DataCenterResult {
    	
    	public String dataCenterName;
    	public PowerPlan idealPowerPlan;
    	public PowerPlan pueIdealPowerPlan;
    	public List<EascPowerPlan> eascPowerPlans;
    	public PowerPlan consolidatedPowerPlan;
    	public DataCenterStatus status;
    	
    	public DataCenterExecutionPlan getExecutionPlan() {
    		DataCenterExecutionPlan executionPlan = new DataCenterExecutionPlan(dataCenterName);
        	executionPlan.setIdealPowerPlan(idealPowerPlan);
        	executionPlan.setConsolidatedPowerPlan(consolidatedPowerPlan);
        	return executionPlan;
    	}
    	
    	public DataCenterOptimization getOptimization() {
    		return new DataCenterOptimization(dataCenterName, getExecutionPlan(), status);
    	}
    	
    	public static List<DataCenterExecutionPlan> getExecutionPlans(List<DataCenterResult> dataCenterResults) {
    		List<DataCenterExecutionPlan> executionPlans = new ArrayList<>(dataCenterResults.size());
            for (DataCenterResult result : dataCenterResults) {
            	executionPlans.add(result.getExecutionPlan());
            }
        	return executionPlans;
    	}
    	
    	public static List<DataCenterOptimization> getOptimizations(List<DataCenterResult> dataCenterResults) {
    		List<DataCenterOptimization> optimizations = new ArrayList<>(dataCenterResults.size());
            for (DataCenterResult result : dataCenterResults) {
            	optimizations.add(result.getOptimization());
            }
        	return optimizations;
    	}
    	
    }
    
    private static class ActivityStatusKey {
		
    	private String easc;
		private String activity;
		
		public ActivityStatusKey(String easc, String activity) {
			this.easc = easc;
			this.activity = activity;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ActivityStatusKey)) {
				return false;
			}
			ActivityStatusKey other = (ActivityStatusKey) obj;
			return other.easc.equals(easc) && other.activity.equals(activity);
		}

		@Override
		public int hashCode() {
			return easc.hashCode() + activity.hashCode();
		}
		
	}
    
}
