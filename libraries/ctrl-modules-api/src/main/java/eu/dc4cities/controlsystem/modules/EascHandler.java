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

package eu.dc4cities.controlsystem.modules;

import eu.dc4cities.controlsystem.model.TimeParameters;
import eu.dc4cities.controlsystem.model.easc.*;

import java.util.List;

/**
 * Handles communications with EASC services via the Southbound API. When working in a federation, there must be a
 * single endpoint for each EASC in the federation. That endpoint takes care of distributing load to underlying data
 * centers in which the EASC can run. Thus {@code EascHandler} does not need to care about federation issues and there
 * is a single instance of the component for the whole federation.
 */
public interface EascHandler {

	/**
	 * Requests option plans to the given EASCs based on the specified power quotas. A corresponding option plan is
	 * returned for each requested EASC.
	 * 
	 * @param eascQuotas the power quotas for every EASC for which an option plan is requested
	 * @return the option plans for the EASCs
	 * @deprecated use {@link #getActivitySpecifications(TimeParameters)} to support inversion of control
	 */
	@Deprecated
    List<EascOptionPlan> collectOptionPlans(List<EascPowerPlan> eascPowerPlans);

    /**
     * Requests activity specifications to all EASCs for the given time range.
     * 
     * @param timeRange the requested time range with dateNow, dateFrom and dateTo
     * @return the list of activity specifications
     */
    List<EascActivitySpecifications> getActivitySpecifications(TimeParameters timeRange);
    
    /**
     * Sends activity execution plans to all EASCs. Each EASC will change its working mode to the one assigned to the
     * first time slot in the plan (which is always the current time slot).
     * 
     * @param eascActivityPlans the activity plans to send
     */
    void sendActivityPlans(List<EascActivityPlan> eascActivityPlans);
    
    /**
     * Requests current metrics values to all EASCs.
     * 
     * @param currentTime the current time seen at the control system (dateNow), used for virtual clock support
     * @return the list of metrics for each EASC
     */
    List<EascMetrics> getMetrics(TimeParameters currentTime);
    
}
