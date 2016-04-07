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

package eu.dc4cities.controlsystem.backend;

import eu.dc4cities.configuration.controller.DefaultConfigurationController;
import eu.dc4cities.configuration.goal.GoalConfiguration;
import eu.dc4cities.configuration.loader.JsonLoader;
import eu.dc4cities.configuration.repository.ConfigurationRepository;
import eu.dc4cities.configuration.repository.LocalResourceConfigurationRepository;
import eu.dc4cities.configuration.technical.DataCenterConfiguration;
import eu.dc4cities.configuration.technical.TechnicalConfiguration;
import eu.dc4cities.controlsystem.modules.*;
import eu.dc4cities.controlsystem.modules.easchandler.EascHandlerImpl;
import eu.dc4cities.controlsystem.modules.erdshandler.ErdsHandlerImpl;
import eu.dc4cities.controlsystem.modules.escalationmanager.EscalationManagerImpl;
import eu.dc4cities.controlsystem.modules.optionconsolidator.OptionConsolidatorImpl;
import eu.dc4cities.controlsystem.modules.powerplanner.AggressivAdaptPowerPlannerImpl;
import eu.dc4cities.controlsystem.modules.powersplitter.PowerSplitterImpl;
import eu.dc4cities.controlsystem.modules.processcontroller.*;
import eu.dc4cities.controlsystem.modules.processcontroller.rest.FederationController;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Declares all beans to be created in the spring context for the controller back end.
 */
@Configuration
@Import(WebMvcConfig.class)
public class BackEndConfig {
	
	@Autowired
	private WebMvcConfig webMvcConfig;
	
	@Bean
	public JsonLoader configurationJsonLoader() {
		return new JsonLoader();
	}
	
	@Bean
	public ConfigurationRepository configurationRepository() {
		ClassPathResource goalResource = new ClassPathResource("goal-configuration.json");
		ClassPathResource technicalResource = new ClassPathResource("technical-configuration.json");
		return new LocalResourceConfigurationRepository(configurationJsonLoader(), goalResource, technicalResource);
	}
	
	@Bean
	public ConfigurationController configurationController() {
		return new DefaultConfigurationController(configurationRepository());
	}
	
	@Bean
	public ThreadPoolTaskScheduler taskScheduler() {
		return new ThreadPoolTaskScheduler();
	}
	
	@Bean
	public OptimizationManager optimizationManager() {
		TechnicalConfiguration technicalConfiguration = configurationController().getTechnicalConfiguration();
		OptimizationManager optimizationManager = new OptimizationManager(technicalConfiguration, taskScheduler(), 
				hdbDao()) {
			@Override
			protected MonitoringLoop newMonitoringLoop() {
				return monitoringLoop();
			}
			@Override
			protected PowerLoop newPowerLoop() {
				return powerLoop();
			}
			@Override
			protected ControlLoop newControlLoop() {
				return controlLoop();
			}
		};
		optimizationManager.startScheduledLoops();
		return optimizationManager;
	}

	@Bean
	public HistoricalDbDao hdbDao() {
		TechnicalConfiguration config = configurationController().getTechnicalConfiguration();
		if (!config.isHdbEnabled()) {
			return null;
		}
		String hdbUrl = config.getHdbUrl();
		String companyCode = config.getCompanyCode();
		if (config.isEscalationManagerEnabled()) {
			String energisUrl = config.getEnergisUrl();
			String energisApiKey = config.getEnergisApiKey();
			if (energisUrl == null || energisApiKey == null) {
				throw new BeanCreationException("You must set both energisUrl and energisApiKey when the escalation "
						+ "manager and HDB upload are enabled");
			}
			return new HistoricalDbDao(hdbUrl, energisUrl, energisApiKey, companyCode, restTemplate());
		} else {
			return new HistoricalDbDao(hdbUrl, companyCode);
		}
	}
	
	@Bean
	@Scope("prototype")
	public MonitoringLoop monitoringLoop() {
		return new MonitoringLoop(eascHandler());
	}
	
	@Bean
	@Scope("prototype")
	public PowerLoop powerLoop() {
		return new PowerLoop(erdsHandler());
	}
	
	@Bean
	@Scope("prototype")
	public ControlLoop controlLoop() {
		TechnicalConfiguration technicalConfig = configurationController().getTechnicalConfiguration();
		GoalConfiguration goalConfig = configurationController().getGoalConfiguration();
		Map<String, PowerPlanner> powerPlanners = new HashMap<>();
		Map<String, PowerSplitter> powerSplitters = new HashMap<>();
		Map<String, EscalationManager> escalationManagers = 
				technicalConfig.isEscalationManagerEnabled() ? new HashMap<>() : null;
		for (DataCenterConfiguration dataCenter : technicalConfig.getDataCenters()) {
			String dataCenterName = dataCenter.getName();
			powerPlanners.put(dataCenterName, powerPlanner(dataCenterName));
			powerSplitters.put(dataCenterName, powerSplitter(dataCenterName));
			if (escalationManagers != null) {
				escalationManagers.put(dataCenterName, escalationManager());
			}
		}
		return new ControlLoop(technicalConfig, goalConfig, powerPlanners, powerSplitters, eascHandler(), 
				optionConsolidator(), escalationManagers);
	}
	
	@Bean
	@Scope("prototype")
	public ErdsHandler erdsHandler() {
		TechnicalConfiguration technicalConfig = configurationController().getTechnicalConfiguration();
		return new ErdsHandlerImpl(technicalConfig.getDataCenters(), restTemplate());
	}
	
	@Bean
	@Scope("prototype")
	public PowerPlanner powerPlanner(String dataCenterName) {
		TechnicalConfiguration technicalConfig = configurationController().getTechnicalConfiguration();
		DataCenterConfiguration dataCenterConfig = technicalConfig.getDataCenter(dataCenterName);
		if (dataCenterConfig == null) {
			throw new BeanCreationException("powerPlanner", 
					"Could not find configuration for data center " + dataCenterName);
		}
		AggressivAdaptPowerPlannerImpl powerPlanner = new AggressivAdaptPowerPlannerImpl();
		powerPlanner.setDCMinPower(dataCenterConfig.getMinPower());
        powerPlanner.setDCMaxPower(dataCenterConfig.getMaxPower());
        if (dataCenterConfig.getAggressiveness() != null) {
        	powerPlanner.setAlpha(dataCenterConfig.getAggressiveness());
        }
		return powerPlanner;
	}
	
	@Bean
	@Scope("prototype")
	public PowerSplitter powerSplitter(String dataCenterName) {
		TechnicalConfiguration technicalConfig = configurationController().getTechnicalConfiguration();
		DataCenterConfiguration dataCenterConfig = technicalConfig.getDataCenter(dataCenterName);
		if (dataCenterConfig == null) {
			throw new BeanCreationException("powerSplitter", "Could not find configuration for data center " + dataCenterName);
		}
		return new PowerSplitterImpl(dataCenterName, dataCenterConfig.getEascGroups());
	}
	
	@Bean
	@Scope("prototype")
	public EascHandler eascHandler() {
		TechnicalConfiguration technicalConfig = configurationController().getTechnicalConfiguration();
		return new EascHandlerImpl(technicalConfig.getEascList(), restTemplate());
	}
	
	@Bean
	@Scope("prototype")
	public OptionConsolidator optionConsolidator() {
		OptionConsolidatorImpl consolidator = new OptionConsolidatorImpl();
		Integer timeout = configurationController().getTechnicalConfiguration().getConsolidationTimeout();
		if (timeout != null) {
			consolidator.setTimeout(timeout);
		}
		String heuristic = configurationController().getTechnicalConfiguration().getConsolidationHeuristic();
		if (heuristic != null) {
			consolidator.setHeuristic(heuristic);
		}
		return consolidator;
	}
	
	@Bean
	@Scope("prototype")
	public EscalationManager escalationManager() {
		TechnicalConfiguration technicalConfig = configurationController().getTechnicalConfiguration();
		EscalationManagerImpl escalationManager = new EscalationManagerImpl();
        escalationManager.setWarnThreshold(technicalConfig.getEscalationWarningThreshold());
        return escalationManager;
	}
	
	@Bean
	public RestTemplate restTemplate() {
		RestTemplate template = new RestTemplate();
		List<HttpMessageConverter<?>> converters = new ArrayList<>(1);
		converters.add(webMvcConfig.jacksonMessageConverter());
		template.setMessageConverters(converters);
		return template;
	}
	
	@Bean
	public WelcomeController welcomeController() {
		return new WelcomeController();
	}
	
	@Bean
	public FederationController dataCenterController() {
		return new FederationController(optimizationManager());
	}
	
}
