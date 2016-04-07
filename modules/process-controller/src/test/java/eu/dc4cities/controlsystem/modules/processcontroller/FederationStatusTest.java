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
import eu.dc4cities.configuration.technical.TechnicalConfiguration;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterPower;
import eu.dc4cities.controlsystem.model.easc.EascMetrics;
import eu.dc4cities.controlsystem.model.easc.EascServiceLevels;
import eu.dc4cities.controlsystem.model.json.JsonUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.measure.unit.SI;
import javax.measure.unit.UnitFormat;
import java.util.List;

public class FederationStatusTest {
	
	private DateTime startDate;
	private FederationStatus federationStatus;
	private String jsonDir;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		UnitFormat.getInstance().label(SI.GIGA(SI.BIT), "Gbit");
	}
	
	@Before
	public void setUp() {
		initFederationStatus(new DateTime(2015, 9, 1, 0, 0));
	}
	
	private void initFederationStatus(DateTime startDate) {
		this.startDate = startDate;
		TechnicalConfiguration technicalConfig = new TechnicalConfiguration();
		technicalConfig.setTimeSlotWidth(15);
		federationStatus = new FederationStatus(startDate, technicalConfig);
	}
	
	private <T> T loadJson(String name, TypeReference<T> typeReference) {
		return JsonUtils.loadResource(jsonDir + "/" + name + ".json", typeReference);
	}
	
	@Test
	public void testSingleMeasurement() {
		// Tests when there is exactly 1 measurement per time slot, with a single EASC, activity and data center.
		jsonDir = "federation-status-single";
		List<EascMetrics> eascMetrics = loadJson("easc-metrics", new TypeReference<List<EascMetrics>>() {});
		List<DataCenterPower> expectedPowerActuals = 
				loadJson("power-actuals", new TypeReference<List<DataCenterPower>>() {});
		List<EascServiceLevels> expectedServiceLevels = 
				loadJson("service-levels", new TypeReference<List<EascServiceLevels>>() {});
		federationStatus.updateEascMetrics(new DateTime(2015, 9, 1, 0, 15), eascMetrics);
		Assert.assertEquals(eascMetrics, federationStatus.getLatestEascMetrics());
		JsonTestUtils.assertJsonEquals(expectedPowerActuals, federationStatus.getDataCenterPowerActuals());
		JsonTestUtils.assertJsonEquals(expectedServiceLevels, federationStatus.getEascServiceLevels());
	}
	
	@Test
	public void testMultipleMeasurements() {
		// Tests when there are multiple measurements per time slot, with multiple EASCs, activities and data centers.
		// The test covers two time slots in order to verify correct aggregation of measurements and appending of new
		// data for the new time slot to existing data structures.
		jsonDir = "federation-status-multi";
		int monitoringInterval = 5;
		// Test first time slot
		String[] metricsFiles = new String[] {"easc-metrics-1a", "easc-metrics-1b", "easc-metrics-1c"};
		List<EascMetrics> eascMetrics;
		DateTime metricsDate = startDate;
		for (int i = 0; i < metricsFiles.length; i++) {
			eascMetrics = loadJson(metricsFiles[i], new TypeReference<List<EascMetrics>>() {});
			metricsDate = metricsDate.plusMinutes(monitoringInterval);
			federationStatus.updateEascMetrics(metricsDate, eascMetrics);
			if (i < metricsFiles.length - 1) {
				Assert.assertTrue(federationStatus.getDataCenterPowerActuals().size() == 0);
				Assert.assertTrue(federationStatus.getEascServiceLevels().size() == 0);
				Assert.assertEquals(eascMetrics, federationStatus.getLatestEascMetrics());
			}
		}
		List<DataCenterPower> expectedPowerActuals = 
				loadJson("power-actuals-1", new TypeReference<List<DataCenterPower>>() {});
		List<EascServiceLevels> expectedServiceLevels = 
				loadJson("service-levels-1", new TypeReference<List<EascServiceLevels>>() {});
		JsonTestUtils.assertJsonEquals(expectedPowerActuals, federationStatus.getDataCenterPowerActuals());
		JsonTestUtils.assertJsonEquals(expectedServiceLevels, federationStatus.getEascServiceLevels());
		// Test second time slot
		metricsFiles = new String[] {"easc-metrics-2a", "easc-metrics-2b", "easc-metrics-2c"};
		for (int i = 0; i < metricsFiles.length; i++) {
			eascMetrics = loadJson(metricsFiles[i], new TypeReference<List<EascMetrics>>() {});
			metricsDate = metricsDate.plusMinutes(monitoringInterval);
			federationStatus.updateEascMetrics(metricsDate, eascMetrics);
		}
		expectedPowerActuals = loadJson("power-actuals-2", new TypeReference<List<DataCenterPower>>() {});
		expectedServiceLevels = loadJson("service-levels-2", new TypeReference<List<EascServiceLevels>>() {});
		JsonTestUtils.assertJsonEquals(expectedPowerActuals, federationStatus.getDataCenterPowerActuals());
		JsonTestUtils.assertJsonEquals(expectedServiceLevels, federationStatus.getEascServiceLevels());
	}
	
	@Test
	public void testSkippedMeasurements() {
		// Tests when after a first measurement, a series of measurements are skipped causing a full time slot to be
		// without data; output data are produced (for the first and third time slots) only after the second measurement
		// comes in, at the end of the third time slot.
		jsonDir = "federation-status-skipped";
		List<EascMetrics> metrics1 = loadJson("easc-metrics-1", new TypeReference<List<EascMetrics>>() {});
		List<EascMetrics> metrics2 = loadJson("easc-metrics-2", new TypeReference<List<EascMetrics>>() {});
		List<DataCenterPower> expectedPowerActuals = 
				loadJson("power-actuals", new TypeReference<List<DataCenterPower>>() {});
		List<EascServiceLevels> expectedServiceLevels = 
				loadJson("service-levels", new TypeReference<List<EascServiceLevels>>() {});
		federationStatus.updateEascMetrics(new DateTime(2015, 9, 1, 0, 5), metrics1);
		federationStatus.updateEascMetrics(new DateTime(2015, 9, 1, 0, 45), metrics2);
		JsonTestUtils.assertJsonEquals(expectedPowerActuals, federationStatus.getDataCenterPowerActuals());
		JsonTestUtils.assertJsonEquals(expectedServiceLevels, federationStatus.getEascServiceLevels());
	}
	
	@Test
	public void testMidnightReset() {
		// Tests that metrics are reset at midnight.
		// Override default federation status with start date close to midnight.
		initFederationStatus(new DateTime(2015, 9, 1, 23, 30));
		jsonDir = "federation-status-midnight";
		List<EascMetrics> metrics1 = loadJson("easc-metrics-1", new TypeReference<List<EascMetrics>>() {});
		List<EascMetrics> metrics2 = loadJson("easc-metrics-2", new TypeReference<List<EascMetrics>>() {});
		List<EascMetrics> metrics3 = loadJson("easc-metrics-3", new TypeReference<List<EascMetrics>>() {});
		List<DataCenterPower> expectedPowerActuals = 
				loadJson("power-actuals", new TypeReference<List<DataCenterPower>>() {});
		List<EascServiceLevels> expectedServiceLevels = 
				loadJson("service-levels", new TypeReference<List<EascServiceLevels>>() {});
		federationStatus.updateEascMetrics(new DateTime(2015, 9, 1, 23, 45), metrics1);
		Assert.assertTrue(federationStatus.getDataCenterPowerActuals().size() > 0);
		Assert.assertTrue(federationStatus.getEascServiceLevels().size() > 0);
		federationStatus.updateEascMetrics(new DateTime(2015, 9, 2, 0, 0), metrics2);
		Assert.assertTrue(federationStatus.getDataCenterPowerActuals().size() == 0);
		Assert.assertTrue(federationStatus.getEascServiceLevels().size() == 0);
		federationStatus.updateEascMetrics(new DateTime(2015, 9, 2, 0, 15), metrics3);
		JsonTestUtils.assertJsonEquals(expectedPowerActuals, federationStatus.getDataCenterPowerActuals());
		JsonTestUtils.assertJsonEquals(expectedServiceLevels, federationStatus.getEascServiceLevels());
	}
	
}
