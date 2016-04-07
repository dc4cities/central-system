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

package eu.dc4cities.controlsystem.modules.easchandler;

import eu.dc4cities.configuration.technical.ServiceConfiguration;
import eu.dc4cities.controlsystem.model.TimeParameters;
import eu.dc4cities.controlsystem.model.easc.EascActivityPlan;
import eu.dc4cities.controlsystem.model.easc.EascActivitySpecifications;
import eu.dc4cities.controlsystem.model.easc.EascMetrics;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class EascHandlerTest {
	
	private List<ServiceConfiguration> eascList;
	private RestTemplate restTemplate;
	private EascHandlerImpl eascHandler;
	
	@Before
	public void setUp() {
		eascList = new ArrayList<ServiceConfiguration>(2);
		eascList.add(new ServiceConfiguration("easc1", "http://easc1.example.com"));
		eascList.add(new ServiceConfiguration("easc2", "http://easc2.example.com"));
		restTemplate = mock(RestTemplate.class);
		eascHandler = new EascHandlerImpl(eascList, restTemplate);
	}
	
	@Test
	public void testGetActivitySpecifications() {
		TimeParameters timeParameters = new TimeParameters();
		DateTime dateNow = DateTime.now();
		timeParameters.setDateNow(dateNow);
		timeParameters.setDateFrom(dateNow);
		timeParameters.setDateTo(dateNow.plusDays(1));
		List<EascActivitySpecifications> expectedSpecs = new ArrayList<EascActivitySpecifications>(2);
		expectedSpecs.add(new EascActivitySpecifications(eascList.get(0).getName()));
		expectedSpecs.add(new EascActivitySpecifications(eascList.get(1).getName()));
		when(restTemplate.postForObject(eascList.get(0).getEndpoint() + "/v1/easc/{eascName}/activityspecifications",
				timeParameters, EascActivitySpecifications.class, eascList.get(0).getName()))
		    .thenReturn(expectedSpecs.get(0));
		when(restTemplate.postForObject(eascList.get(1).getEndpoint() + "/v1/easc/{eascName}/activityspecifications",
				timeParameters, EascActivitySpecifications.class, eascList.get(1).getName()))
		    .thenReturn(expectedSpecs.get(1));
		List<EascActivitySpecifications> actualSpecs = eascHandler.getActivitySpecifications(timeParameters);
		Assert.assertEquals(2, actualSpecs.size());
		Assert.assertEquals(expectedSpecs.get(0).getEascName(), actualSpecs.get(0).getEascName());
		Assert.assertEquals(expectedSpecs.get(1).getEascName(), actualSpecs.get(1).getEascName());
	}
	
    @Test
    public void testSendActivityPlans() {
    	List<EascActivityPlan> activityPlans = new ArrayList<>(2);
    	String eascName1 = eascList.get(0).getName();
    	EascActivityPlan ap1 = new EascActivityPlan(eascName1);
    	activityPlans.add(ap1);
    	String eascName2 = eascList.get(1).getName();
    	EascActivityPlan ap2 = new EascActivityPlan(eascName2);
    	activityPlans.add(ap2);
        eascHandler.sendActivityPlans(activityPlans);
        verify(restTemplate).put(eascList.get(0).getEndpoint() + "/v1/easc/{eascName}/activityplan", ap1, eascName1);
        verify(restTemplate).put(eascList.get(1).getEndpoint() + "/v1/easc/{eascName}/activityplan", ap2, eascName2);
    }

    @Test
	public void testGetMetrics() {
		TimeParameters timeParameters = new TimeParameters();
		timeParameters.setDateNow(DateTime.now());
		List<EascMetrics> expectedMetrics = new ArrayList<EascMetrics>(2);
		expectedMetrics.add(new EascMetrics(eascList.get(0).getName()));
		expectedMetrics.add(new EascMetrics(eascList.get(1).getName()));
		when(restTemplate.postForObject(eascList.get(0).getEndpoint() + "/v1/easc/{eascName}/metrics", timeParameters,
				EascMetrics.class, eascList.get(0).getName()))
		    .thenReturn(expectedMetrics.get(0));
		when(restTemplate.postForObject(eascList.get(1).getEndpoint() + "/v1/easc/{eascName}/metrics", timeParameters,
				EascMetrics.class, eascList.get(1).getName()))
		    .thenReturn(expectedMetrics.get(1));
		List<EascMetrics> actualMetrics = eascHandler.getMetrics(timeParameters);
		Assert.assertEquals(2, actualMetrics.size());
		Assert.assertEquals(expectedMetrics.get(0).getEascName(), actualMetrics.get(0).getEascName());
		Assert.assertEquals(expectedMetrics.get(1).getEascName(), actualMetrics.get(1).getEascName());
	}

}
