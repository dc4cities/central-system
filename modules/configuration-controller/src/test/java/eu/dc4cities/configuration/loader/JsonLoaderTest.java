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

package eu.dc4cities.configuration.loader;

import eu.dc4cities.configuration.goal.*;
import eu.dc4cities.configuration.technical.*;
import eu.dc4cities.controlsystem.model.easc.PriceModifier;
import org.apache.commons.io.FileUtils;
import org.jscience.physics.amount.Amount;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * JsonLoader Test
 *
 *
 * 
 */
public class JsonLoaderTest {

	private JsonLoader classUnderTest;

	private File tmpGoalConfigFile;

	@Before
	public void before() {
		classUnderTest = new JsonLoader();
		// save to tmp
		tmpGoalConfigFile = new File(FileUtils.getTempDirectory(),
				"goalconfiguration_" + System.currentTimeMillis() + ".json");
	}

	@After
	public void after() {
		// remove tmp file
		FileUtils.deleteQuietly(tmpGoalConfigFile);
	}

	private TimeFrame createTimeFrame(String duration, String recurrentExpression, Date startDate) {
		TimeFrame timeFrame = new TimeFrame();
		timeFrame.setDuration(duration);
		timeFrame.setRecurrentExpression(recurrentExpression);
		timeFrame.setStartDate(startDate);
		return timeFrame;
	}
	
	@Test
	public void testLoadJson_goalconfiguration() throws FileNotFoundException,
			IOException {
		assertNotNull(classUnderTest);

		GoalConfiguration goalConfig = new GoalConfiguration();
		goalConfig.setCreationDate(new Date());
		goalConfig.setLastModified(goalConfig.getCreationDate());
		goalConfig.setId(UUID.randomUUID().toString());

		List<Goal> goals = new ArrayList<Goal>();
		goalConfig.setGoals(goals);

		Goal goal = new Goal();
		goal.setCreationDate(new Date());
		goal.setLastModified(goal.getCreationDate());
		goal.setDescription("some goal description");
		goal.setId(UUID.randomUUID().toString());
		goal.setName("some goal");

		goals.add(goal);

		List<Objective> objectives = new ArrayList<Objective>();
		goal.setObjectives(objectives);

		Objective objective1 = new Objective();
		objective1.setCreationDate(new Date());
		objective1.setDataCenterId("datacenter_0");
		objective1.setDescription("infinite baseline objective");
		objective1.setEnabled(true);
		objective1.setId(UUID.randomUUID().toString());
		objective1.setImplementationType(ImplementationType.MUST);
		objective1.setLastModified(objective1.getCreationDate());
		objective1.setName("baseline");
		objective1.setPriority(0);

		Target target1 = new Target();
		target1.setMetric("SOME.AWESOME.METRIC");
		target1.setOperator(Target.GREATER_THAN);
		target1.setValue(0.8);
		objective1.setTarget(target1);

		TimeFrame timeFrame1 = createTimeFrame(null, null, new Date(0));
		objective1.setTimeFrame(timeFrame1);

		objective1.setType(ObjectiveType.POWER);
		
		List<PriceModifier> priceModifiers = new ArrayList<PriceModifier>(2);
		addModifier(priceModifiers, "100 %", "-1 EUR/pp");
		addModifier(priceModifiers, "80 %", "0 EUR/pp");
		addModifier(priceModifiers, "75 %", "1 EUR/pp");
		objective1.setPriceModifiers(priceModifiers);

		objectives.add(objective1);

		Objective objective2 = new Objective();
		objective2.setCreationDate(new Date());
		objective2.setDataCenterId("datacenter_0");
		objective2.setDescription("recurring objective");
		objective2.setEnabled(true);
		objective2.setId(UUID.randomUUID().toString());
		objective2.setImplementationType(ImplementationType.MUST);
		objective2.setLastModified(objective2.getCreationDate());
		objective2.setName("recurring sunday 3 hours");
		objective2.setPriority(0);

		Target target2 = new Target();
		target2.setMetric("SOME.OTHER.AWESOME.METRIC");
		target2.setOperator(Target.LESS_EQUALS);
		target2.setValue(0.3);
		objective2.setTarget(target2);

		TimeFrame timeFrame2 = createTimeFrame("PT3H", "0 0 0 ? * SUN", null);
		objective2.setTimeFrame(timeFrame2);

		objective2.setType(ObjectiveType.POWER);

		objectives.add(objective2);

		// save to tmp
		classUnderTest
				.save(goalConfig, new FileOutputStream(tmpGoalConfigFile));

		// load
		GoalConfiguration loadedGoalConfig = classUnderTest.load(
				FileUtils.openInputStream(tmpGoalConfigFile),
				GoalConfiguration.class);

		// asserts
		assertNotNull(loadedGoalConfig);

		assertEquals(goals.size(), loadedGoalConfig.getGoals().size());
		assertEquals(objectives.size(), loadedGoalConfig.getGoals().get(0)
				.getObjectives().size());
		
	}
	
	private void addModifier(List<PriceModifier> modifiers, String threshold, String modifier) {
		PriceModifier pm = new PriceModifier(Amount.valueOf(threshold), Amount.valueOf(modifier));
		modifiers.add(pm);
	}
	
	@Test
	public void testLoadTechnicalConfiguration() {
		String configPath = "configs/technicalconfiguration_test.json";
		TechnicalConfiguration config;
		try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(configPath)) {
			config = classUnderTest.load(is, TechnicalConfiguration.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		assertNotNull(config);
		assertEquals(60, config.getPowerLoopInterval());
		assertEquals(15, config.getControlLoopInterval());
		assertEquals(5, config.getMonitoringLoopInterval());
		assertEquals(15, config.getTimeSlotWidth());
		assertEquals(24, config.getTimeWindowWidth());
		assertEquals("http://myhdb.example.com:8484", config.getHdbUrl());
		assertEquals(true, config.isHdbEnabled());
		assertEquals("mycompany", config.getCompanyCode());
		List<DataCenterConfiguration> dataCenters = config.getDataCenters();
		assertEquals(2, dataCenters.size());
		DataCenterConfiguration dc1 = dataCenters.get(0);
		assertEquals("dc1", dc1.getName());
		assertEquals(1000, dc1.getMinPower());
		assertEquals(2000, dc1.getMaxPower());
		assertEquals(1.5, dc1.getPue(), 0);
		List<ServiceConfiguration> erdsList1 = dc1.getErdsList();
		assertServiceEquals("grid1", "http://grid.myenergyprovider1.com", erdsList1.get(0));
		assertServiceEquals("pv1", "http://pv.myenergyprovider1.com", erdsList1.get(1));
		List<EascGroup> eascGroups1 = dc1.getEascGroups();
		EascWeight easc1Weight = new EascWeight("easc1");
		easc1Weight.setWeight(1);
		assertEascGroupEquals("group1", 1, Arrays.asList(easc1Weight), eascGroups1.get(0));
		EascWeight easc2Weight = new EascWeight("easc2");
		easc2Weight.setWeight(3);
		EascWeight easc3Weight = new EascWeight("easc3");
		easc3Weight.setWeight(2);
		assertEascGroupEquals("group2", 2, Arrays.asList(easc2Weight, easc3Weight), eascGroups1.get(1));
		DataCenterConfiguration dc2 = dataCenters.get(1);
		assertEquals("dc2", dc2.getName());
		assertEquals(500, dc2.getMinPower());
		assertEquals(1000, dc2.getMaxPower());
		assertEquals(1.2, dc2.getPue(), 0);
		List<ServiceConfiguration> erdsList2 = dc2.getErdsList();
		assertServiceEquals("grid2", "http://grid.myenergyprovider2.com", erdsList2.get(0));
		List<EascGroup> eascGroups2 = dc2.getEascGroups();
		easc1Weight = new EascWeight("easc1");
		easc1Weight.setWeight(1);
		assertEascGroupEquals("group1", 1, Arrays.asList(easc1Weight), eascGroups2.get(0));
		List<ServiceConfiguration> eascList = config.getEascList();
		assertEquals(3, eascList.size());
		assertServiceEquals("easc1", "http://easc1.mycompany.com", eascList.get(0));
		assertServiceEquals("easc2", "http://easc2.mycompany.com", eascList.get(1));
		assertServiceEquals("easc3", "http://easc3.mycompany.com", eascList.get(2));
	}
	
	private void assertServiceEquals(String expectedName, String expectedEndpoint, ServiceConfiguration actualService) {
		assertEquals(expectedName, actualService.getName());
		assertEquals(expectedEndpoint, actualService.getEndpoint());
	}
	
	private void assertEascGroupEquals(String expectedName, int expectedWeight, List<EascWeight> expectedEascWeights,
			EascGroup actualGroup) {
		assertEquals(expectedName, actualGroup.getName());
		assertEquals(expectedWeight, actualGroup.getGroupWeight());
		List<EascWeight> actualEascWeights = actualGroup.getEascWeights();
		assertEquals(expectedEascWeights.size(), actualEascWeights.size());
		for (int i = 0; i < expectedEascWeights.size(); i++) {
			EascWeight expected = expectedEascWeights.get(i);
			EascWeight actual = actualEascWeights.get(i);
			assertEquals(expected.getEascName(), actual.getEascName());
			assertEquals(expected.getWeight(), actual.getWeight());
		}
	}
	
}
