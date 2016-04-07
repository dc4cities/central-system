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

import com.fasterxml.jackson.core.type.TypeReference;
import eu.dc4cities.configuration.goal.Goal;
import eu.dc4cities.configuration.goal.GoalConfiguration;
import eu.dc4cities.configuration.goal.Objective;
import eu.dc4cities.configuration.technical.TechnicalConfiguration;
import eu.dc4cities.controlsystem.model.PowerPlan;
import eu.dc4cities.controlsystem.model.TimeParameters;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterExecutionPlan;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterPower;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterStatus;
import eu.dc4cities.controlsystem.model.easc.*;
import eu.dc4cities.controlsystem.model.erds.DataCenterForecast;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.model.json.JsonUtils;
import eu.dc4cities.controlsystem.modules.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.util.*;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;

public class ControlLoopTest {

	private String jsonDir;
	private TechnicalConfiguration technicalConfig;
	private GoalConfiguration goalConfig;
	private PowerPlanner powerPlanner1, powerPlanner2;
	private PowerSplitter powerSplitter1, powerSplitter2;
	private EascHandler eascHandler;
	private OptionConsolidator optionConsolidator;
	private EscalationManager escalationManager1, escalationManager2;
	private ControlLoop controlLoopWithoutEscalation;
	private ControlLoop controlLoopWithEscalation;
	
	private <T> T loadJson(String name, Class<T> clazz) {
		return loadJson(jsonDir, name, clazz);
	}
	
	private <T> T loadJson(String directory, String name, Class<T> clazz) {
		return JsonUtils.loadResource(directory + "/" + name + ".json", clazz);
	}
	
	private <T> T loadJson(String name, TypeReference<T> typeReference) {
		return loadJson(jsonDir, name, typeReference);
	}
	
	private <T> T loadJson(String directory, String name, TypeReference<T> typeReference) {
		return JsonUtils.loadResource(directory + "/" + name + ".json", typeReference);
	}
	
	@Before
	public void setUp() {
		technicalConfig = loadJson("control-loop", "technical-configuration", TechnicalConfiguration.class);
		goalConfig = loadJson("control-loop", "goal-configuration", GoalConfiguration.class);
		powerPlanner1 = mock(PowerPlanner.class);
		powerPlanner2 = mock(PowerPlanner.class);
		Map<String, PowerPlanner> powerPlanners = new HashMap<>();
		powerPlanners.put("dc1", powerPlanner1);
		powerPlanners.put("dc2", powerPlanner2);
		powerSplitter1 = mock(PowerSplitter.class);
		powerSplitter2 = mock(PowerSplitter.class);
		Map<String, PowerSplitter> powerSplitters = new HashMap<>();
		powerSplitters.put("dc1", powerSplitter1);
		powerSplitters.put("dc2", powerSplitter2);
		eascHandler = mock(EascHandler.class);
		optionConsolidator = mock(OptionConsolidator.class);
		escalationManager1 = mock(EscalationManager.class);
		escalationManager2 = mock(EscalationManager.class);
		Map<String, EscalationManager> escalationManagers = new HashMap<>();
		escalationManagers.put("dc1", escalationManager1);
		escalationManagers.put("dc2", escalationManager2);
		controlLoopWithoutEscalation = new ControlLoop(technicalConfig, goalConfig, powerPlanners, powerSplitters,
				eascHandler, optionConsolidator, null);
		controlLoopWithEscalation = new ControlLoop(technicalConfig, goalConfig, powerPlanners, powerSplitters,
				eascHandler, optionConsolidator, escalationManagers);
	}
	
	private List<Objective> getObjectives() {
    	return getObjectives(null);
    }
    
    private List<Objective> getObjectives(String dataCenterName) {
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
	
	private List<ErdsForecast> getDataCenterForecasts(String dataCenterName, List<DataCenterForecast> forecasts) {
		for (DataCenterForecast forecast : forecasts) {
			if (forecast.getDataCenterName().equals(dataCenterName)) {
				return forecast.getErdsForecasts();
			}
		}
		return null;
	}
	
	private DataCenterPower getDataCenterPower(String dataCenterName, List<DataCenterPower> powerItems) {
		for (DataCenterPower powerItem : powerItems) {
			if (powerItem.getDataCenterName().equals(dataCenterName)) {
				return powerItem;
			}
		}
		return null;
	}
	
	private PowerPlan getConsolidatedPowerPlanFromOptimizations(String dataCenterName, 
			List<DataCenterOptimization> optimizations) {
		for (DataCenterOptimization optimization : optimizations) {
			if (optimization.getDataCenterName().equals(dataCenterName)) {
				return optimization.getExecutionPlan().getConsolidatedPowerPlan();
			}
		}
		return null;
	}
	
	private PowerPlan getConsolidatedPowerPlanFromExecutionPlans(String dataCenterName, 
			List<DataCenterExecutionPlan> executionPlans) {
		for (DataCenterExecutionPlan executionPlan : executionPlans) {
			if (executionPlan.getDataCenterName().equals(dataCenterName)) {
				return executionPlan.getConsolidatedPowerPlan();
			}
		}
		return null;
	}
	
	private DataCenterStatus getDataCenterStatus(String dataCenterName, List<DataCenterOptimization> optimizations) {
		for (DataCenterOptimization optimization : optimizations) {
			if (optimization.getDataCenterName().equals(dataCenterName)) {
				return optimization.getStatus();
			}
		}
		return null;
	}
	
	private class JsonMatcher<T> extends ArgumentMatcher<T> {
		
		private Object match;
		
		public JsonMatcher(Object match) {
			this.match = match;
		}
		
		@Override
		public boolean matches(Object argument) {
			return JsonTestUtils.jsonEquals(match, argument);
		}
		
	}
	
	@Test
	public void testDryRun() {
		// Test the control loop when a dry run is required before the real execution, without the escalation manager
		doTestDryRun(false);
	}
	
	@Test
	public void testDryRunWithEscalationManager() {
		// Test the control loop when a dry run is required before the real execution, using the escalation manager
		doTestDryRun(true);
	}
	
	@SuppressWarnings("unchecked")
	private void doTestDryRun(boolean useEscalationManager) {
		jsonDir = "control-loop-midnight";
		ControlLoop loopToTest = useEscalationManager ? controlLoopWithEscalation : controlLoopWithoutEscalation;
		TimeParameters timeParameters = loadJson("time-parameters", TimeParameters.class);
		TimeSlotBasedEntity timeRange = new TimeSlotBasedEntity(timeParameters);
		JsonMatcher<TimeSlotBasedEntity> timeRangeMatcher = new JsonMatcher<>(timeRange);
		List<DataCenterForecast> forecasts = loadJson("datacenter-forecasts", 
				new TypeReference<List<DataCenterForecast>>() {});
		List<ErdsForecast> erdsForecasts1 = getDataCenterForecasts("dc1", forecasts);
		List<ErdsForecast> erdsForecasts2 = getDataCenterForecasts("dc2", forecasts);
		List<DataCenterExecutionPlan> executionPlansIn = null;
		PowerPlan idealPlan1 = loadJson("ideal-plan-dc1", PowerPlan.class);
		PowerPlan idealPlan2 = loadJson("ideal-plan-dc2", PowerPlan.class);
		PowerPlan maxPowerPlan1 = loadJson("max-power-plan-dc1", PowerPlan.class);
		JsonMatcher<PowerPlan> maxPowerPlan1Matcher = new JsonMatcher<>(maxPowerPlan1);
		PowerPlan maxPowerPlan2 = loadJson("max-power-plan-dc2", PowerPlan.class);
		JsonMatcher<PowerPlan> maxPowerPlan2Matcher = new JsonMatcher<>(maxPowerPlan2);
		List<EascPowerPlan> eascPowerPlans1 = loadJson("easc-power-plans-dc1", 
				new TypeReference<List<EascPowerPlan>>() {});
		List<EascPowerPlan> eascPowerPlans2 = loadJson("easc-power-plans-dc2",
				new TypeReference<List<EascPowerPlan>>() {});
		List<EascPowerPlan> eascPowerPlans = loadJson("easc-power-plans",
				new TypeReference<List<EascPowerPlan>>() {});
		JsonMatcher<List<EascPowerPlan>> eascPowerPlansMatcher = new JsonMatcher<>(eascPowerPlans);
		List<DataCenterPowerPlan> pueIdealPlans = loadJson("datacenter-pue-ideal-plans",
				new TypeReference<List<DataCenterPowerPlan>>() {});
		JsonMatcher<List<DataCenterPowerPlan>> pueIdealPlansMatcher = new JsonMatcher<>(pueIdealPlans);
		List<EascActivitySpecifications> eascActivitySpecifications = loadJson("easc-activity-specifications", 
				new TypeReference<List<EascActivitySpecifications>>() {});
		List<DataCenterPower> dataCenterPowerActuals = new ArrayList<>(0);
		List<EascServiceLevels> eascServiceLevels = new ArrayList<>(0);
		List<EascMetrics> eascMetrics = new ArrayList<>(0);
		List<EascActivityPlan> eascActivityPlans = loadJson("easc-activity-plans",
				new TypeReference<List<EascActivityPlan>>() {});
		String optimizationsFile = 
				useEscalationManager ? "datacenter-optimizations-escalation" : "datacenter-optimizations";
		List<DataCenterOptimization> expectedOptimizations = loadJson(optimizationsFile,
				new TypeReference<List<DataCenterOptimization>>() {});
		when(powerPlanner1.calculateIdealPowerPlan(argThat(timeRangeMatcher), eq(erdsForecasts1), 
				isNull(PowerPlan.class))).thenReturn(idealPlan1);
		when(powerPlanner2.calculateIdealPowerPlan(argThat(timeRangeMatcher), eq(erdsForecasts2), 
				isNull(PowerPlan.class))).thenReturn(idealPlan2);
		PowerPlan prevPlan1 = getConsolidatedPowerPlanFromOptimizations("dc1", expectedOptimizations);
		JsonMatcher<PowerPlan> prevPlan1Matcher = new JsonMatcher<>(prevPlan1);
		PowerPlan prevPlan2 = getConsolidatedPowerPlanFromOptimizations("dc2", expectedOptimizations);
		JsonMatcher<PowerPlan> prevPlan2Matcher = new JsonMatcher<>(prevPlan2);
		when(powerPlanner1.calculateIdealPowerPlan(argThat(timeRangeMatcher), eq(erdsForecasts1), 
				argThat(prevPlan1Matcher))).thenReturn(idealPlan1);
		when(powerPlanner2.calculateIdealPowerPlan(argThat(timeRangeMatcher), eq(erdsForecasts2), 
				argThat(prevPlan2Matcher))).thenReturn(idealPlan2);
		when(powerSplitter1.splitPowerForEasc(argThat(maxPowerPlan1Matcher))).thenReturn(eascPowerPlans1);
		when(powerSplitter2.splitPowerForEasc(argThat(maxPowerPlan2Matcher))).thenReturn(eascPowerPlans2);
		when(eascHandler.getActivitySpecifications(timeParameters)).thenReturn(eascActivitySpecifications);
		when(optionConsolidator.buildActivityPlans(argThat(timeRangeMatcher), eq(getObjectives()), eq(forecasts), 
				isNull(List.class), argThat(eascPowerPlansMatcher), eq(eascActivitySpecifications), 
				eq(dataCenterPowerActuals),	eq(eascServiceLevels), eq(eascMetrics))).thenReturn(eascActivityPlans);
		when(optionConsolidator.buildActivityPlans(argThat(timeRangeMatcher), eq(getObjectives()), eq(forecasts), 
				argThat(pueIdealPlansMatcher), argThat(eascPowerPlansMatcher), eq(eascActivitySpecifications), 
				eq(dataCenterPowerActuals),	eq(eascServiceLevels), eq(eascMetrics))).thenReturn(eascActivityPlans);
		if (useEscalationManager) {
			JsonMatcher<TimeParameters> timeParametersMatcher = new JsonMatcher<>(timeParameters);
			DataCenterStatus status1 = loadJson("datacenter-status-dc1", DataCenterStatus.class);
			DataCenterStatus status2 = loadJson("datacenter-status-dc2", DataCenterStatus.class);
			when(escalationManager1.determineDcStatus(eq("dc1"), argThat(timeParametersMatcher),
					eq(getObjectives("dc1")), eq(erdsForecasts1), eq(eascActivitySpecifications),
					isNull(DataCenterPower.class), eq(eascServiceLevels), eq(eascActivityPlans))).thenReturn(status1);
			when(escalationManager2.determineDcStatus(eq("dc2"), argThat(timeParametersMatcher),
					eq(getObjectives("dc2")), eq(erdsForecasts2), eq(eascActivitySpecifications),
					isNull(DataCenterPower.class), eq(eascServiceLevels), eq(eascActivityPlans))).thenReturn(status2);
		}
		List<DataCenterOptimization> actualOptimizations = loopToTest.execute(timeParameters, forecasts,
				dataCenterPowerActuals, eascServiceLevels, eascMetrics,	executionPlansIn);
		verify(eascHandler).sendActivityPlans(eascActivityPlans);
		JsonTestUtils.assertJsonEquals(expectedOptimizations, actualOptimizations);
	}
	
	@Test
	public void testPreviousPlanAligned() {
		// Tests the control loop when no dry run is required and the previous plan is aligned to the requested
		// time range
		jsonDir = "control-loop-midnight";
		TimeParameters timeParameters = loadJson("time-parameters", TimeParameters.class);
		TimeSlotBasedEntity timeRange = new TimeSlotBasedEntity(timeParameters);
		JsonMatcher<TimeSlotBasedEntity> timeRangeMatcher = new JsonMatcher<>(timeRange);
		List<DataCenterForecast> forecasts = loadJson("datacenter-forecasts", 
				new TypeReference<List<DataCenterForecast>>() {});
		List<ErdsForecast> erdsForecasts1 = getDataCenterForecasts("dc1", forecasts);
		List<ErdsForecast> erdsForecasts2 = getDataCenterForecasts("dc2", forecasts);
		List<DataCenterExecutionPlan> executionPlansIn = null;
		PowerPlan idealPlan1 = loadJson("ideal-plan-dc1", PowerPlan.class);
		PowerPlan idealPlan2 = loadJson("ideal-plan-dc2", PowerPlan.class);
		PowerPlan maxPowerPlan1 = loadJson("max-power-plan-dc1", PowerPlan.class);
		JsonMatcher<PowerPlan> maxPowerPlan1Matcher = new JsonMatcher<>(maxPowerPlan1);
		PowerPlan maxPowerPlan2 = loadJson("max-power-plan-dc2", PowerPlan.class);
		JsonMatcher<PowerPlan> maxPowerPlan2Matcher = new JsonMatcher<>(maxPowerPlan2);
		List<EascPowerPlan> eascPowerPlans1 = loadJson("easc-power-plans-dc1", 
				new TypeReference<List<EascPowerPlan>>() {});
		List<EascPowerPlan> eascPowerPlans2 = loadJson("easc-power-plans-dc2",
				new TypeReference<List<EascPowerPlan>>() {});
		List<EascPowerPlan> eascPowerPlans = loadJson("easc-power-plans",
				new TypeReference<List<EascPowerPlan>>() {});
		JsonMatcher<List<EascPowerPlan>> eascPowerPlansMatcher = new JsonMatcher<>(eascPowerPlans);
		List<DataCenterPowerPlan> pueIdealPlans = loadJson("datacenter-pue-ideal-plans",
				new TypeReference<List<DataCenterPowerPlan>>() {});
		JsonMatcher<List<DataCenterPowerPlan>> pueIdealPlansMatcher = new JsonMatcher<>(pueIdealPlans);
		List<EascActivitySpecifications> eascActivitySpecifications = new ArrayList<>(0);
		List<DataCenterPower> dataCenterPowerActuals = new ArrayList<>(0);
		List<EascServiceLevels> eascServiceLevels = new ArrayList<>(0);
		List<EascMetrics> eascMetrics = new ArrayList<>(0);
		List<EascActivityPlan> eascActivityPlans = loadJson("easc-activity-plans",
				new TypeReference<List<EascActivityPlan>>() {});
		List<DataCenterOptimization> expectedOptimizations = loadJson("datacenter-optimizations",
				new TypeReference<List<DataCenterOptimization>>() {});
		executionPlansIn = loadJson("datacenter-execution-plans-in", 
				new TypeReference<List<DataCenterExecutionPlan>>() {});
		PowerPlan prevPlan1 = getConsolidatedPowerPlanFromExecutionPlans("dc1", executionPlansIn);
		PowerPlan prevPlan2 = getConsolidatedPowerPlanFromExecutionPlans("dc2", executionPlansIn);
		when(powerPlanner1.calculateIdealPowerPlan(argThat(timeRangeMatcher), eq(erdsForecasts1), eq(prevPlan1)))
			.thenReturn(idealPlan1);
		when(powerPlanner2.calculateIdealPowerPlan(argThat(timeRangeMatcher), eq(erdsForecasts2), eq(prevPlan2)))
			.thenReturn(idealPlan2);
		when(powerSplitter1.splitPowerForEasc(argThat(maxPowerPlan1Matcher))).thenReturn(eascPowerPlans1);
		when(powerSplitter2.splitPowerForEasc(argThat(maxPowerPlan2Matcher))).thenReturn(eascPowerPlans2);
		when(eascHandler.getActivitySpecifications(timeParameters)).thenReturn(eascActivitySpecifications);
		when(optionConsolidator.buildActivityPlans(argThat(timeRangeMatcher), eq(getObjectives()), eq(forecasts), 
				argThat(pueIdealPlansMatcher), argThat(eascPowerPlansMatcher),	eq(eascActivitySpecifications), 
				eq(dataCenterPowerActuals),	eq(eascServiceLevels), eq(eascMetrics))).thenReturn(eascActivityPlans);
		List<DataCenterOptimization> actualOptimizations = controlLoopWithoutEscalation.execute(timeParameters, 
				forecasts, dataCenterPowerActuals, eascServiceLevels, eascMetrics,	executionPlansIn);
		verify(eascHandler).sendActivityPlans(eascActivityPlans);
		JsonTestUtils.assertJsonEquals(expectedOptimizations, actualOptimizations);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testPreviousPlanNotAligned() {
		// Tests the control loop when a previous consolidated plan is provided but it doesn't cover the whole requested
		// time range and a partial dry run is required.
		jsonDir = "control-loop-after-midnight";
		TimeParameters timeParameters = loadJson("time-parameters", TimeParameters.class);
		TimeSlotBasedEntity timeRange = new TimeSlotBasedEntity(timeParameters);
		JsonMatcher<TimeSlotBasedEntity> timeRangeMatcher = new JsonMatcher<>(timeRange);
		List<DataCenterForecast> forecasts = loadJson("datacenter-forecasts", 
				new TypeReference<List<DataCenterForecast>>() {});
		List<ErdsForecast> erdsForecasts1 = getDataCenterForecasts("dc1", forecasts);
		List<ErdsForecast> erdsForecasts2 = getDataCenterForecasts("dc2", forecasts);
		List<DataCenterPower> dataCenterPowerActuals = loadJson("datacenter-power-actuals", 
				new TypeReference<List<DataCenterPower>>() {});
		DataCenterPower powerActual1 = getDataCenterPower("dc1", dataCenterPowerActuals);
		DataCenterPower powerActual2 = getDataCenterPower("dc2", dataCenterPowerActuals);
		List<DataCenterExecutionPlan> prevExecPlans = loadJson("previous-execution-plans",
				new TypeReference<List<DataCenterExecutionPlan>>() {});
		TimeParameters missingTimeParameters = loadJson("missing-time-parameters", TimeParameters.class);
		JsonMatcher<TimeParameters> missingTimeParamsMatcher = new JsonMatcher<>(missingTimeParameters);
		TimeSlotBasedEntity missingTimeRange = new TimeSlotBasedEntity(missingTimeParameters);
		JsonMatcher<TimeSlotBasedEntity> missingTimeRangeMatcher = new JsonMatcher<>(missingTimeRange);
		List<DataCenterForecast> trimmedForecasts = loadJson("trimmed-datacenter-forecasts", 
				new TypeReference<List<DataCenterForecast>>() {});
		JsonMatcher<List<DataCenterForecast>> trimmedForecastsMatcher = new JsonMatcher<>(trimmedForecasts);
		List<ErdsForecast> trimmedErdsForecasts1 = getDataCenterForecasts("dc1", trimmedForecasts);
		JsonMatcher<List<ErdsForecast>> trimmedErdsForecasts1Matcher = new JsonMatcher<>(trimmedErdsForecasts1);
		List<ErdsForecast> trimmedErdsForecasts2 = getDataCenterForecasts("dc2", trimmedForecasts);
		JsonMatcher<List<ErdsForecast>> trimmedErdsForecasts2Matcher = new JsonMatcher<>(trimmedErdsForecasts2);
		PowerPlan missingIdealPlan1 = loadJson("missing-ideal-plan-dc1", PowerPlan.class);
		PowerPlan missingIdealPlan2 = loadJson("missing-ideal-plan-dc2", PowerPlan.class);
		when(powerPlanner1.calculateIdealPowerPlan(argThat(missingTimeRangeMatcher),
				argThat(trimmedErdsForecasts1Matcher), isNull(PowerPlan.class))).thenReturn(missingIdealPlan1);
		when(powerPlanner2.calculateIdealPowerPlan(argThat(missingTimeRangeMatcher),
				argThat(trimmedErdsForecasts2Matcher), isNull(PowerPlan.class))).thenReturn(missingIdealPlan2);
		PowerPlan missingMaxPowerPlan1 = loadJson("missing-max-power-plan-dc1", PowerPlan.class);
		JsonMatcher<PowerPlan> missingMaxPowerPlan1Matcher = new JsonMatcher<>(missingMaxPowerPlan1);
		PowerPlan missingMaxPowerPlan2 = loadJson("missing-max-power-plan-dc2", PowerPlan.class);
		JsonMatcher<PowerPlan> missingMaxPowerPlan2Matcher = new JsonMatcher<>(missingMaxPowerPlan2);
		List<EascPowerPlan> missingEascPowerPlans1 = loadJson("missing-easc-power-plans-dc1", 
				new TypeReference<List<EascPowerPlan>>() {});
		List<EascPowerPlan> missingEascPowerPlans2 = loadJson("missing-easc-power-plans-dc2",
				new TypeReference<List<EascPowerPlan>>() {});
		when(powerSplitter1.splitPowerForEasc(argThat(missingMaxPowerPlan1Matcher))).thenReturn(missingEascPowerPlans1);
		when(powerSplitter2.splitPowerForEasc(argThat(missingMaxPowerPlan2Matcher))).thenReturn(missingEascPowerPlans2);
		List<EascActivitySpecifications> eascActivitySpecifications = new ArrayList<>(0);
		when(eascHandler.getActivitySpecifications(argThat(missingTimeParamsMatcher)))
				.thenReturn(eascActivitySpecifications);
		List<EascPowerPlan> missingEascPowerPlans = loadJson("missing-easc-power-plans",
				new TypeReference<List<EascPowerPlan>>() {});
		JsonMatcher<List<EascPowerPlan>> missingEascPowerPlansMatcher = new JsonMatcher<>(missingEascPowerPlans);
		List<EascServiceLevels> eascServiceLevels = new ArrayList<>(0);
		List<EascMetrics> eascMetrics = new ArrayList<>(0);
		List<EascActivityPlan> missingEascActivityPlans = loadJson("missing-easc-activity-plans",
				new TypeReference<List<EascActivityPlan>>() {});
		when(optionConsolidator.buildActivityPlans(argThat(missingTimeRangeMatcher), eq(getObjectives()), 
				argThat(trimmedForecastsMatcher), isNull(List.class), 
				argThat(missingEascPowerPlansMatcher), eq(eascActivitySpecifications),
				eq(dataCenterPowerActuals), eq(eascServiceLevels), eq(eascMetrics)))
				.thenReturn(missingEascActivityPlans);
		PowerPlan trimmedConsolidatedPlan1 = loadJson("trimmed-consolidated-plan-dc1", PowerPlan.class);
		JsonMatcher<PowerPlan> trimmedConsolidatedPlan1Matcher = new JsonMatcher<>(trimmedConsolidatedPlan1);
		PowerPlan trimmedConsolidatedPlan2 = loadJson("trimmed-consolidated-plan-dc2", PowerPlan.class);
		JsonMatcher<PowerPlan> trimmedConsolidatedPlan2Matcher = new JsonMatcher<>(trimmedConsolidatedPlan2);
		PowerPlan idealPlan1 = loadJson("ideal-plan-dc1", PowerPlan.class);
		PowerPlan idealPlan2 = loadJson("ideal-plan-dc2", PowerPlan.class);
		when(powerPlanner1.calculateIdealPowerPlan(argThat(timeRangeMatcher), eq(erdsForecasts1),
				argThat(trimmedConsolidatedPlan1Matcher))).thenReturn(idealPlan1);
		when(powerPlanner2.calculateIdealPowerPlan(argThat(timeRangeMatcher), eq(erdsForecasts2),
				argThat(trimmedConsolidatedPlan2Matcher))).thenReturn(idealPlan2);
		PowerPlan maxPowerPlan1 = loadJson("max-power-plan-dc1", PowerPlan.class);
		JsonMatcher<PowerPlan> maxPowerPlan1Matcher = new JsonMatcher<>(maxPowerPlan1);
		PowerPlan maxPowerPlan2 = loadJson("max-power-plan-dc2", PowerPlan.class);
		JsonMatcher<PowerPlan> maxPowerPlan2Matcher = new JsonMatcher<>(maxPowerPlan2);
		List<EascPowerPlan> eascPowerPlans1 = loadJson("easc-power-plans-dc1", 
				new TypeReference<List<EascPowerPlan>>() {});
		List<EascPowerPlan> eascPowerPlans2 = loadJson("easc-power-plans-dc2",
				new TypeReference<List<EascPowerPlan>>() {});
		when(powerSplitter1.splitPowerForEasc(argThat(maxPowerPlan1Matcher))).thenReturn(eascPowerPlans1);
		when(powerSplitter2.splitPowerForEasc(argThat(maxPowerPlan2Matcher))).thenReturn(eascPowerPlans2);
		when(eascHandler.getActivitySpecifications(timeParameters)).thenReturn(eascActivitySpecifications);
		List<EascPowerPlan> eascPowerPlans = loadJson("easc-power-plans",
				new TypeReference<List<EascPowerPlan>>() {});
		JsonMatcher<List<EascPowerPlan>> eascPowerPlansMatcher = new JsonMatcher<>(eascPowerPlans);
		List<DataCenterPowerPlan> pueIdealPlans = loadJson("datacenter-pue-ideal-plans",
				new TypeReference<List<DataCenterPowerPlan>>() {});
		JsonMatcher<List<DataCenterPowerPlan>> pueIdealPlansMatcher = new JsonMatcher<>(pueIdealPlans);
		List<EascActivityPlan> eascActivityPlans = loadJson("easc-activity-plans",
				new TypeReference<List<EascActivityPlan>>() {});
		when(optionConsolidator.buildActivityPlans(argThat(timeRangeMatcher), eq(getObjectives()), 
				eq(forecasts), argThat(pueIdealPlansMatcher), argThat(eascPowerPlansMatcher), 
				eq(eascActivitySpecifications),	eq(dataCenterPowerActuals), eq(eascServiceLevels), eq(eascMetrics)))
				.thenReturn(eascActivityPlans);
		List<DataCenterOptimization> expectedOptimizations = loadJson("datacenter-optimizations",
				new TypeReference<List<DataCenterOptimization>>() {});
		DataCenterStatus status1 = getDataCenterStatus("dc1", expectedOptimizations);
		DataCenterStatus status2 = getDataCenterStatus("dc2", expectedOptimizations);
		TimeParameters timeParametersFromMidnight = loadJson("time-parameters-from-midnight", TimeParameters.class);
		JsonMatcher<TimeParameters> timeParametersFromMidnightMatcher = new JsonMatcher<>(timeParametersFromMidnight);
		when(escalationManager1.determineDcStatus(eq("dc1"), argThat(timeParametersFromMidnightMatcher),
				eq(getObjectives("dc1")), eq(erdsForecasts1), eq(eascActivitySpecifications), eq(powerActual1),
				eq(eascServiceLevels), eq(eascActivityPlans))).thenReturn(status1);
		when(escalationManager2.determineDcStatus(eq("dc2"), argThat(timeParametersFromMidnightMatcher),
				eq(getObjectives("dc2")), eq(erdsForecasts2), eq(eascActivitySpecifications), eq(powerActual2),
				eq(eascServiceLevels), eq(eascActivityPlans))).thenReturn(status2);
		List<DataCenterOptimization> actualOptimizations = controlLoopWithEscalation.execute(timeParameters,
				forecasts, dataCenterPowerActuals, eascServiceLevels, eascMetrics, prevExecPlans);
		verify(eascHandler).sendActivityPlans(eascActivityPlans);
		JsonTestUtils.assertJsonEquals(expectedOptimizations, actualOptimizations);
	}
	
}
