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

package eu.dc4cities.controlsystem.modules.erdshandler;

import eu.dc4cities.configuration.technical.DataCenterConfiguration;
import eu.dc4cities.configuration.technical.ServiceConfiguration;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.erds.DataCenterForecast;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import javax.measure.unit.NonSI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ErdsHandlerImplTest {

	private RestTemplate restTemplate;
	private ErdsHandlerImpl erdsHandler;
	
	@Before
	public void setUp() {
		List<DataCenterConfiguration> configurations = new ArrayList<>(2);
		List<ServiceConfiguration> erdsList1 = new ArrayList<ServiceConfiguration>(2);
		erdsList1.add(new ServiceConfiguration("erds1", "http://erds1.example.com"));
		erdsList1.add(new ServiceConfiguration("erds2", "http://erds2.example.com"));
		DataCenterConfiguration dc1 = new DataCenterConfiguration("dc1");
		dc1.setErdsList(erdsList1);
		configurations.add(dc1);
		List<ServiceConfiguration> erdsList2 = new ArrayList<ServiceConfiguration>(1);
		erdsList2.add(new ServiceConfiguration("erds3", "http://erds3.example.com"));
		DataCenterConfiguration dc2 = new DataCenterConfiguration("dc2");
		dc2.setErdsList(erdsList2);
		configurations.add(dc2);
		restTemplate = mock(RestTemplate.class);
		erdsHandler = new ErdsHandlerImpl(configurations, restTemplate);
	}
	
	private DataCenterForecast buildDataCenterForecast(String dataCenter, TimeSlotBasedEntity timeRange, ErdsForecast... erdsForecasts) {
		DataCenterForecast forecast = new DataCenterForecast(dataCenter);
		forecast.copyIntervalFrom(timeRange);
		forecast.setErdsForecasts(Arrays.asList(erdsForecasts));
		return forecast;
	}
	
	@Test
	public void testForecast() {
		TimeSlotBasedEntity timeRange = new TimeSlotBasedEntity();
		DateTime dateFrom = DateTime.now();
		timeRange.setDateFrom(dateFrom);
		timeRange.setDateTo(dateFrom.plusDays(1));
		timeRange.setTimeSlotDuration(Amount.valueOf(15, NonSI.MINUTE));
		ErdsForecast forecast1 = new ErdsForecast("erds1");
		ErdsForecast forecast2 = new ErdsForecast("erds2");
		ErdsForecast forecast3 = new ErdsForecast("erds3");
		List<DataCenterForecast> expected = new ArrayList<DataCenterForecast>(2);
		expected.add(buildDataCenterForecast("dc1", timeRange, forecast1, forecast2));
		expected.add(buildDataCenterForecast("dc2", timeRange, forecast3));
		when(restTemplate.postForObject(
				"http://erds1.example.com/v1/erds/{erdsName}/forecast", timeRange, ErdsForecast.class, "erds1"))
				.thenReturn(forecast1);
		when(restTemplate.postForObject(
				"http://erds2.example.com/v1/erds/{erdsName}/forecast", timeRange, ErdsForecast.class, "erds2"))
				.thenReturn(forecast2);
		when(restTemplate.postForObject(
				"http://erds3.example.com/v1/erds/{erdsName}/forecast", timeRange, ErdsForecast.class, "erds3"))
				.thenReturn(forecast3);
		List<DataCenterForecast> actual = erdsHandler.getEnergyForecasts(timeRange);
		Assert.assertEquals(expected.toString(), actual.toString());
	}
	
}
