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

package eu.dc4cities.configuration.controller;

import eu.dc4cities.configuration.goal.Goal;
import eu.dc4cities.configuration.goal.GoalConfiguration;
import eu.dc4cities.configuration.goal.Objective;
import eu.dc4cities.configuration.repository.ConfigurationRepository;
import eu.dc4cities.configuration.technical.TechnicalConfiguration;
import eu.dc4cities.controlsystem.modules.ConfigurationController;

import java.util.List;

/**
 * Default Configuration Controller impl. for configs (e.g. GoalConfiguration etc.).
 * 
 * Note: For handling of time intervals for Objectives, see TimeInterval API in
 * configuration-model lib.
 *
 *
 * 
 * @see eu.dc4cities.configuration.goal.GoalConfiguration
 * @see eu.dc4cities.configuration.repository.LocalResourceConfigurationRepository
 * 
 */
public class DefaultConfigurationController implements ConfigurationController {

	private ConfigurationRepository configurationRepository;

	/**
	 * Creates a new instance using the given repository as the configuration provider.
	 * 
	 * @param configurationRepository the configuration repository
	 */
	public DefaultConfigurationController(ConfigurationRepository configurationRepository) {
		this.configurationRepository = configurationRepository;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GoalConfiguration getGoalConfiguration() {
		return configurationRepository.getGoalConfiguration();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Goal> getGoals() {
		return configurationRepository.getGoals();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Objective> getObjectives() {
		return configurationRepository.getObjectives();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Objective> getObjectives(String goalId) {
		return configurationRepository.getObjectives(goalId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TechnicalConfiguration getTechnicalConfiguration() {
		return configurationRepository.getTechnicalConfiguration();
	}
	
}
