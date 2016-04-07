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

package eu.dc4cities.configuration.repository;

import eu.dc4cities.configuration.goal.Goal;
import eu.dc4cities.configuration.goal.GoalConfiguration;
import eu.dc4cities.configuration.goal.Objective;
import eu.dc4cities.configuration.technical.TechnicalConfiguration;

import java.util.List;

/**
 * DAO for configurations
 *
 *
 * 
 */
public interface ConfigurationRepository {

	/**
	 * @return GoalConfiguration
	 */
	public GoalConfiguration getGoalConfiguration();

	/**
	 * @return GoalConfiguration
	 */
	public TechnicalConfiguration getTechnicalConfiguration();

	/**
	 * @return List of goals
	 */
	public List<Goal> getGoals();

	/**
	 * @return List of objectives
	 */
	public List<Objective> getObjectives();

	/**
	 * @param goalId
	 *            Objectives for given goal id
	 * @return List of objectives
	 */
	public List<Objective> getObjectives(String goalId);
}
