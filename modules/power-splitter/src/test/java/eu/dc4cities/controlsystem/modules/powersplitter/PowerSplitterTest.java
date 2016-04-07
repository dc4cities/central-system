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

package eu.dc4cities.controlsystem.modules.powersplitter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dc4cities.configuration.technical.EascGroup;
import eu.dc4cities.controlsystem.model.PowerPlan;
import eu.dc4cities.controlsystem.model.easc.EascPowerPlan;
import eu.dc4cities.controlsystem.model.json.JsonUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class PowerSplitterTest {
    
	private PowerSplitterImpl splitter;
	private ObjectMapper mapper;
	
	@Before
	public void setUp() {
		mapper = JsonUtils.getDc4CitiesObjectMapper();
	}
	
	private void initSplitter(String groupConfig) {
		List<EascGroup> eascGroups = 
				JsonUtils.loadResource("power-splitter/" + groupConfig, new TypeReference<List<EascGroup>>() {});
		splitter = new PowerSplitterImpl("dc1", eascGroups);
	}
	
	@Test
	public void groupSplitTest() {
		// Tests the 2-level splitting algorithm. The group configuration includes at least two groups, one with a
		// single EASC and one with multiple EASCs with different weights. Power values in the ideal plan should be such
		// that dividing by weight yields some integer and some non-integers values in order to test rounding handling.
		initSplitter("easc-groups-multi.json");
		PowerPlan idealPower = JsonUtils.loadResource("power-splitter/ideal-power-groups.json", PowerPlan.class);
		List<EascPowerPlan> actualPlans = splitter.splitPowerForEasc(idealPower);
		List<EascPowerPlan> expectedPlans = JsonUtils.loadResource("power-splitter/easc-power-plans-groups.json", 
				new TypeReference<List<EascPowerPlan>>() {});
		JsonNode actualTree = mapper.valueToTree(actualPlans);
		JsonNode expectedTree = mapper.valueToTree(expectedPlans);
		Assert.assertEquals(expectedTree, actualTree);
	}

	@Test
	public void fractionalHoursSplitTest() {
		// Tests when the splitting period is a non-integer number of hours (check correct handling of double values)
		initSplitter("easc-groups-single.json");
		PowerPlan idealPower = JsonUtils.loadResource("power-splitter/ideal-power-hours.json", PowerPlan.class);
		List<EascPowerPlan> actualPlans = splitter.splitPowerForEasc(idealPower);
		List<EascPowerPlan> expectedPlans = JsonUtils.loadResource("power-splitter/easc-power-plans-hours.json", 
				new TypeReference<List<EascPowerPlan>>() {});
		JsonNode actualTree = mapper.valueToTree(actualPlans);
		JsonNode expectedTree = mapper.valueToTree(expectedPlans);
		Assert.assertEquals(expectedTree, actualTree);
	}
	
	@Test
	public void daysSplitTest() {
		// Tests when the splitting period stretches over multiple days, with multiple groups and EASCs.
		initSplitter("easc-groups-multi.json");
		PowerPlan idealPower = JsonUtils.loadResource("power-splitter/ideal-power-days.json", PowerPlan.class);
		List<EascPowerPlan> actualPlans = splitter.splitPowerForEasc(idealPower);
		List<EascPowerPlan> expectedPlans = JsonUtils.loadResource("power-splitter/easc-power-plans-days.json", 
				new TypeReference<List<EascPowerPlan>>() {});
		JsonNode actualTree = mapper.valueToTree(actualPlans);
		JsonNode expectedTree = mapper.valueToTree(expectedPlans);
		Assert.assertEquals(expectedTree, actualTree);
	}
	
}
