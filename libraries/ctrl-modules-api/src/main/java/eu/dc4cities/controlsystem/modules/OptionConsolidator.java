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

import eu.dc4cities.configuration.goal.Objective;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterPower;
import eu.dc4cities.controlsystem.model.easc.*;
import eu.dc4cities.controlsystem.model.erds.DataCenterForecast;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;

import java.util.List;


/**
 * Builds activity plans to be sent to EASCs based on power and SLA requirements. When working in a federation, a single
 * instance of this component handles the whole federation.
 */
public interface OptionConsolidator {

	
	/**
     * Selects for each {@link eu.dc4cities.controlsystem.model.easc.EascOptionPlan}, one {@link EascActivityPlan}
     * that must be executed by the associated EASC.
     *
     * @param timeRange     the time range to optimize
     * @param erdsForecasts the available energy forecasts
     * @param optionPlans   the possible option plans for each EASC
     * @return a final activity plan for each EASC
     * @deprecated use {@link #buildActivityPlans(TimeSlotBasedEntity, List, List, List, List, List, List, List)} to
     *             support inversion of control
     */
	@Deprecated
	List<EascActivityPlan> selectOptionPlans(TimeSlotBasedEntity timeRange, List<ErdsForecast> erdsForecasts, 
			List<EascOptionPlan> optionPlans) throws ConsolidatorException;
	
    /**
     * Builds the activity plan for all EASCs in the federation, based on power objectives, power availability and
     * activity SLAs. In order to evaluate SLAs against actual performance up to the current time, the 
     * {@code dataCenterPowerActuals} and {@code eascServiceLevels} parameters are provided. Both state the actual power
     * usage and performance achieved from the start of the current SLA period up to the last time slot before the
     * beginning of {@code timeRange}. The method is also provided the latest metrics retrieved by the monitoring loop
     * for each EASC in the {@code eascMetrics} parameter.
     * <br>
     * The consolidator should use {@code dataCenterIdealPowerPlans} as an heuristic for the allocation of
     * activities, but it can exceed the ideal quotas if necessary. If ideal power plans are not available (i.e. during
     * dry runs), {@code dataCenterIdealPowerPlans} is {@code null} and the consolidator should use a different
     * heuristic for power allocation.
     * <br>
     * On the other hand, {@code eascPowerPlans} is always provided and states constraints on power and energy quotas
     * that the consolidator must always respect.
     *
     * @param timeRange the time range to optimize
     * @param powerObjectives the power objectives assigned by the smart city to all data centers in the federation
     * @param dataCenterForecasts the energy forecasts for all data centers in the federation
     * @param dataCenterIdealPowerPlans the ideal power plans calculated for each data center in the federation or 
     *        {@code null}
     * @param eascPowerPlans the power budgets for all EASCs in the federation
     * @param eascActivitySpecifications the activity specifications for all EASCs in the federation
     * @param dataCenterPowerActuals the actual power consumption for all data centers in the federation
     * @param eascServiceLevels the actual service levels achieved for all EASCs in the federation
     * @param eascMetrics the current metrics for all EASCs in the federation
     * @return the list of activity plans for all EASCs in the federation
     * @throws ConsolidatorException if optimization fails
     */
    List<EascActivityPlan> buildActivityPlans(TimeSlotBasedEntity timeRange, List<Objective> powerObjectives,
    		List<DataCenterForecast> dataCenterForecasts, List<DataCenterPowerPlan> dataCenterIdealPowerPlans,
    		List<EascPowerPlan> eascPowerPlans, List<EascActivitySpecifications> eascActivitySpecifications,
    		List<DataCenterPower> dataCenterPowerActuals, List<EascServiceLevels> eascServiceLevels,
    		List<EascMetrics> eascMetrics) throws ConsolidatorException;

    /**
     * Set the maximum solving duration.
     *
     * @param t a timeOut in seconds. Set to 0 or less for an infinite duration.
     */
    void setTimeout(int t);

    /**
     * Get the maximum solving duration
     *
     * @return a duration in seconds. <=0 for an infinite duration
     */
    int getTimeout();

	/**
	 * replay the states of a given activity
	 * @param activity the fully qualified name
	 * @param states the states, ordered by timeslot.
     */
	void replay(String activity, List<String> states);

	/**
	 * Makes the consolidator tries to improve the solution it computed until
	 * it hits the timeout or proved the optimality of the last computed one
	 *
	 * @param b {{@code true}} to optimise
	 */
	void doOptimize(boolean b);

	/**
	 * Does the solver optimise ?
	 *
	 * @return
	 */
	boolean doOptimize();
}
