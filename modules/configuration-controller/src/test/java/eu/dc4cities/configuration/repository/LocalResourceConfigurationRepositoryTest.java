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

package eu.dc4cities.configuration.repository;

import eu.dc4cities.configuration.goal.GoalConfiguration;
import eu.dc4cities.configuration.loader.JsonLoader;
import eu.dc4cities.configuration.technical.TechnicalConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * LocalResourceConfigurationRepository Test
 *
 *
 * 
 */
public class LocalResourceConfigurationRepositoryTest {

	private LocalResourceConfigurationRepository configRepo;

	@Before
	public void before() {
		configRepo = new LocalResourceConfigurationRepository(new JsonLoader(),
				new ClassPathResource("configs/goalconfiguration_test.json"),
				new ClassPathResource("configs/technicalconfiguration_test.json"));
	}
	
	/**
	 * Note: Location for GoalConfiguration resource loaded from test.properties
	 */
	@Test
	public void testGetGoalConfiguration() {
		assertNotNull(configRepo);

		// get
		GoalConfiguration goalConfig = configRepo.getGoalConfiguration();

		assertNotNull(goalConfig);

		assertEquals(1, goalConfig.getGoals().size());
		assertEquals(2, goalConfig.getGoals().get(0).getObjectives().size());

		assertEquals(1, configRepo.getGoals().size());
		assertEquals(2, configRepo.getObjectives().size());
		assertEquals(
				2,
				configRepo.getObjectives(
						goalConfig.getGoals().get(0).getId()).size());
	}
	
	
	
	@Test
	public void testGetTechnicalConfiguration() {
		assertNotNull(configRepo);
		TechnicalConfiguration technicalConfig = configRepo.getTechnicalConfiguration();
		assertNotNull(technicalConfig);
		// Just some basic tests since the full configuration is tested in JsonLoaderTest
		assertEquals(2, technicalConfig.getDataCenters().size());
		assertEquals(3, technicalConfig.getEascList().size());
	}

}
