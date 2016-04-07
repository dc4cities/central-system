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
import eu.dc4cities.controlsystem.modules.ErdsHandler;
import org.springframework.web.client.RestOperations;

import java.util.ArrayList;
import java.util.List;

public class ErdsHandlerImpl implements ErdsHandler {
	
	private List<DataCenterConfiguration> dataCenterConfigurations;
	private RestOperations restOps;
	
	/**
	 * Creates a new ERDS handler.
	 * 
	 * @param dataCenterConfigurations the configurations for each data center in the federation
	 * @param restOps the object used to perform REST calls
	 */
	public ErdsHandlerImpl(List<DataCenterConfiguration> dataCenterConfigurations, RestOperations restOps) {
		validateConfig(dataCenterConfigurations);
		this.dataCenterConfigurations = dataCenterConfigurations;
		this.restOps = restOps;
	}
	
	private void validateConfig(List<DataCenterConfiguration> dataCenterConfigurations) {
		if (dataCenterConfigurations == null || dataCenterConfigurations.size() == 0) {
			throw new IllegalArgumentException("No configuration provided");
		}
		for (DataCenterConfiguration config : dataCenterConfigurations) {
			List<ServiceConfiguration> erdsList = config.getErdsList();
			if (erdsList == null || erdsList.size() == 0) {
				throw new IllegalArgumentException("ERDS list empty for data center " + config.getName());
			}
		}
	}

	@Override
	public List<DataCenterForecast> getEnergyForecasts(TimeSlotBasedEntity timeRange) {
		List<DataCenterForecast> forecasts = new ArrayList<>(dataCenterConfigurations.size());
		for (DataCenterConfiguration config : dataCenterConfigurations) {
			String dataCenter = config.getName();
			DataCenterForecast dataCenterForecast = new DataCenterForecast(dataCenter);
			dataCenterForecast.copyIntervalFrom(timeRange);
			List<ServiceConfiguration> erdsList = config.getErdsList();
			List<ErdsForecast> erdsForecasts = new ArrayList<>(erdsList.size());
			for (ServiceConfiguration erds : erdsList) {
				String url = erds.getEndpoint() + "/v1/erds/{erdsName}/forecast";
				ErdsForecast erdsForecast = restOps.postForObject(url, timeRange, ErdsForecast.class, erds.getName());
				erdsForecasts.add(erdsForecast);
			}
			dataCenterForecast.setErdsForecasts(erdsForecasts);
			forecasts.add(dataCenterForecast);
		}
		return forecasts;
	}

	@Deprecated
	@Override
	public List<ErdsForecast> collectEnergyForecasts(TimeSlotBasedEntity timeRange) {
		throw new UnsupportedOperationException("collectEnergyForecasts is not supported any more, upgrade to getEnergyForecats");
	}

}
