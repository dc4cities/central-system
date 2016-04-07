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

import eu.dc4cities.controlsystem.model.TimeParameters;
import eu.dc4cities.controlsystem.model.easc.EascMetrics;
import eu.dc4cities.controlsystem.modules.EascHandler;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Executes the EASC monitoring loop.
 */
public class MonitoringLoop {

	private static final Logger logger = LoggerFactory.getLogger(MonitoringLoop.class);
	
	private EascHandler eascHandler;

    public MonitoringLoop(EascHandler eascHandler) {
        this.eascHandler = eascHandler;
    }
    
    /**
     * Executes the EASC monitoring loop retrieving the latest metrics for all EASCs.
     * 
     * @param dateNow the current time seen by the control system (may be affected by the virtual clock)
     * @return the current metrics for all EASCs
     */
    public List<EascMetrics> execute(DateTime dateNow) {
    	logger.debug("Starting EASC monitoring loop");
    	TimeParameters timeParameters = new TimeParameters();
    	timeParameters.setDateNow(dateNow);
    	logger.debug("Requesting EASC metrics...");
		List<EascMetrics> metrics = eascHandler.getMetrics(timeParameters);
        logger.debug("Got metrics: " + metrics);
        return metrics;
    }
	
}
