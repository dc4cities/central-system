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
import eu.dc4cities.controlsystem.model.easc.*;
import eu.dc4cities.controlsystem.modules.EascHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestOperations;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EascHandlerImpl implements EascHandler {
	
    private static final Logger log = LoggerFactory.getLogger(EascHandlerImpl.class);

    private Map<String, ServiceConfiguration> eascMap = new LinkedHashMap<>();
	private RestOperations restOps;
	
	public EascHandlerImpl(List<ServiceConfiguration> eascList, RestOperations restOps) {
		for (ServiceConfiguration easc : eascList) {
			eascMap.put(easc.getName(), easc);
		}
		this.restOps = restOps;
	}

	@Deprecated
	@Override
	public List<EascOptionPlan> collectOptionPlans(List<EascPowerPlan> eascPowerPlans) {
		throw new UnsupportedOperationException("collectOptionPlans is not supported any more, use getActivitySpecifications");
	}

	@Override
	public List<EascActivitySpecifications> getActivitySpecifications(TimeParameters timeRange) {
		List<EascActivitySpecifications> specsList = new ArrayList<>(eascMap.size());
		for (ServiceConfiguration easc : eascMap.values()) {
			String eascName = easc.getName();
			String endpoint = easc.getEndpoint();
			log.debug("Requesting activity specifications for EASC: " + eascName + " at " + endpoint);
			String url = endpoint + "/v1/easc/{eascName}/activityspecifications";
			EascActivitySpecifications specs = restOps.postForObject(url, timeRange, EascActivitySpecifications.class, eascName);
			log.debug("Activity specifications received");
			specsList.add(specs);
		}
		return specsList;
	}

    @Override
    public void sendActivityPlans(List<EascActivityPlan> eascActivityPlans) {
    	for (EascActivityPlan activityPlan : eascActivityPlans) {
    		String eascName = activityPlan.getEascName();
    		ServiceConfiguration service = eascMap.get(eascName);
    		if (service == null) {
    			throw new IllegalArgumentException("Could not find service configuration for EASC " + eascName);
    		}
    		String endpoint = service.getEndpoint();
	        log.debug("Sending activity plan to " + eascName + " at " + endpoint);
	        String url = endpoint + "/v1/easc/{eascName}/activityplan";
            restOps.put(url, activityPlan, eascName);
			log.debug("Activity plan sent");
    	}
    }

	@Override
	public List<EascMetrics> getMetrics(TimeParameters currentTime) {
		List<EascMetrics> metricsList = new ArrayList<>(eascMap.size());
		for (ServiceConfiguration easc : eascMap.values()) {
			String eascName = easc.getName();
			String endpoint = easc.getEndpoint();
			log.debug("Requesting metrics for EASC: " + eascName + " at " + endpoint);
			String url = endpoint + "/v1/easc/{eascName}/metrics";
			EascMetrics metrics = restOps.postForObject(url, currentTime, EascMetrics.class, eascName);
			log.debug("Metrics received");
			metricsList.add(metrics);
		}
		return metricsList;
	}

}
