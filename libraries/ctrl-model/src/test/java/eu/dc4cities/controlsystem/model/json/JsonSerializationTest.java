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

package eu.dc4cities.controlsystem.model.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dc4cities.controlsystem.model.TimeParameters;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterExecutionPlan;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterPower;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterStatus;
import eu.dc4cities.controlsystem.model.easc.*;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import org.junit.Assert;
import org.junit.Test;

import javax.measure.quantity.DataAmount;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;
import java.net.URL;

/**
 * Tests JSON serialization/deserialization of Java classes in the model.
 */
public class JsonSerializationTest {

	private static final ObjectMapper mapper = JsonUtils.getDc4CitiesObjectMapper();
	
	private static final Unit<DataAmount> GBIT = SI.GIGA(SI.BIT);
	static {
		UnitFormat.getInstance().label(GBIT, "Gbit");
	}
	
	private void testJson(String fileName, Class<?> clazz) {
		String resourcePath = "dc4cities-object-mapper/" + fileName;
		URL url = this.getClass().getClassLoader().getResource(resourcePath);
		Object deserialized;
		try {
			deserialized = mapper.readValue(url, clazz);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		String reserialized;
		try {
			reserialized = mapper.writeValueAsString(deserialized);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		assertJsonEquals(url, reserialized);
	}
	
	private void assertJsonEquals(URL expected, String actual) {
		JsonNode expectedTree;
		try {
			expectedTree = mapper.readTree(expected);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		JsonNode actualTree;
		try {
			actualTree = mapper.readTree(actual);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Assert.assertEquals(expectedTree, actualTree);
	}
	
	@Test
	public void testTimeParameters() {
		testJson("time-parameters.json", TimeParameters.class);
	}
	
	@Test
	public void testTimeSlotBasedEntity() {
		testJson("time-slot-based-entity.json", TimeSlotBasedEntity.class);
	}
	
	@Test
	public void testErdsForecast() {
		testJson("erds-forecast.json", ErdsForecast.class);
	}
	
	@Test
	public void testDataCenterExecutionPlan() {
		testJson("datacenter-execution-plan.json", DataCenterExecutionPlan.class);
	}
	
	@Test
	public void testDataCenterStatus() {
		testJson("datacenter-status.json", DataCenterStatus.class);
	}
	
	@Test
	public void testEascPowerPlan() {
		testJson("easc-power-plan.json", EascPowerPlan.class);
	}
	
	@Test
	public void testEascOptionPlan() {
		testJson("easc-option-plan.json", EascOptionPlan.class);
	}
	
	@Test
	public void testEascActivitySpecifications() {
		testJson("easc-activity-specifications.json", EascActivitySpecifications.class);
	}
	
	@Test
	public void testTaskOrientedSlo() {
		testJson("task-oriented-slo.json", ServiceLevelObjective.class);
	}
	
	@Test
	public void testServiceOrientedSlo() {
		testJson("service-oriented-slo.json", ServiceLevelObjective.class);
	}
	
	@Test
	public void testEascActivityPlan() {
		testJson("easc-activity-plan.json", EascActivityPlan.class);
	}
	
	@Test
	public void testEascMetrics() {
		testJson("easc-metrics.json", EascMetrics.class);
	}
	
	@Test
	public void testDataCenterPower() {
		testJson("datacenter-power.json", DataCenterPower.class);
	}
	
	@Test
	public void testEascServiceLevels() {
		testJson("easc-service-levels.json", EascServiceLevels.class);
	}
	
}
