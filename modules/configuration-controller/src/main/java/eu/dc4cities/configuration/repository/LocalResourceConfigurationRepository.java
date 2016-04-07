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
import eu.dc4cities.configuration.loader.JsonLoader;
import eu.dc4cities.configuration.technical.TechnicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * ConfigurationRepository implementation using simple resource loading mechanisms (either filesystem or classpath 
 * resource).
 *
 *
 */
public class LocalResourceConfigurationRepository implements ConfigurationRepository {

	private static final Logger LOG = LoggerFactory
			.getLogger(LocalResourceConfigurationRepository.class);

	/**
	 * JSON loader
	 */
	private JsonLoader jsonLoader;

	/**
	 * Points to a location where the goal configuration file (assumed to be in
	 * JSON format) can be resolved (either classpath or filesystem).
	 */
	private Resource goalConfigurationResource;

	/**
	 * Points to a location where the technical configuration file (assumed to be in
	 * JSON format) can be resolved (either classpath or filesystem).
	 */
	private Resource technicalConfigurationResource;

	// loaded goal config
	private GoalConfiguration goalConfiguration;
	
	private TechnicalConfiguration technicalConfiguration;

	/**
	 * Creates a new local repository that uses the given loader and resources. The goal and technical configuration
	 * resources must point to a location where the goal configuration file (assumed to be in JSON format) can be
	 * resolved (either classpath or filesystem).
	 * 
	 * @param jsonLoader the JSON loader to use for reading the configuration
	 * @param goalConfigurationResource the goal configuration resource
	 * @param technicalConfigurationResource the technical configuration resource
	 */
	public LocalResourceConfigurationRepository(JsonLoader jsonLoader, Resource goalConfigurationResource,
			Resource technicalConfigurationResource) {
		this.jsonLoader = jsonLoader;
		this.goalConfigurationResource = goalConfigurationResource;
		this.technicalConfigurationResource = technicalConfigurationResource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public GoalConfiguration getGoalConfiguration() {
		try {
			if (goalConfiguration == null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("GoalConfiguration not initialized yet, initializing ...");
				}

				// load from resource
				goalConfiguration = jsonLoader.load(
						goalConfigurationResource.getInputStream(),
						GoalConfiguration.class);

				if (LOG.isDebugEnabled()) {
					LOG.debug("GoalConfiguration initialized: "
							+ ToStringBuilder.reflectionToString(
									goalConfiguration,
									ToStringStyle.MULTI_LINE_STYLE) + " from "
							+ goalConfigurationResource.getURI());
				}
			}

			return goalConfiguration;
		} catch (Throwable e) {
			LOG.warn("Initializing GoalConfiguration failed for "
					+ goalConfigurationResource.getFilename());

			throw new ConfigurationException(
					"Initializing GoalConfiguration failed for "
							+ goalConfigurationResource.getFilename(), e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Goal> getGoals() {
		return getGoalConfiguration().getGoals();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Objective> getObjectives() {
		List<Objective> objectives = new ArrayList<Objective>();

		for (Goal goal : getGoals()) {
			objectives.addAll(goal.getObjectives());
		}

		return objectives;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Objective> getObjectives(String goalId) {
		for (Goal goal : getGoals()) {
			if (StringUtils.equals(goalId, goal.getId())) {
				List<Objective> objectives = new ArrayList<Objective>();
				objectives.addAll(goal.getObjectives());

				return objectives;
			}
		}

		return null;
	}

	@Override
	public TechnicalConfiguration getTechnicalConfiguration() {
		try {
			if (technicalConfiguration == null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("TechnicalConfiguration not initialized yet, initializing ...");
				}

				// load from resource
				technicalConfiguration = jsonLoader.load(
						technicalConfigurationResource.getInputStream(),
						TechnicalConfiguration.class);

				if (LOG.isDebugEnabled()) {
					LOG.debug("TechnicalConfiguration initialized: "
							+ ToStringBuilder.reflectionToString(
									technicalConfiguration,
									ToStringStyle.MULTI_LINE_STYLE) + " from "
							+ technicalConfigurationResource.getURI());
				}
			}

			return technicalConfiguration;
		} catch (Throwable e) {
			LOG.warn("Initializing TechnicalConfiguration failed for "
					+ technicalConfigurationResource.getFilename());

			throw new ConfigurationException(
					"Initializing TechnicalConfiguration failed for "
							+ technicalConfigurationResource.getFilename(), e);
		}
	}

}
