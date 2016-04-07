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

import eu.dc4cities.controlsystem.model.PowerPlan;
import eu.dc4cities.controlsystem.model.easc.EascPowerPlan;

import java.util.List;

/**
 * Splits the available power between EASCs. When working in a federation, each instance of this component works on a
 * single data center.
 */
public interface PowerSplitter {

	/**
	 * Splits the total power available in each time slot among the EASCs associated to the data center.
	 * The returned list has an item for each EASC; every item contains a single power plan (referring to the data
	 * center of the splitter) with maximum total energy and the maximum power for all time slots defined in the input
	 * plan.
	 * 
	 * @param idealPowerPlan the data center ideal power plan
	 * @return the power plan for each EASC
	 */
	List<EascPowerPlan> splitPowerForEasc(PowerPlan idealPowerPlan);

}
