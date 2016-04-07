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

import eu.dc4cities.configuration.technical.TechnicalConfiguration;
import eu.dc4cities.controlsystem.model.TimeParameters;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterExecutionPlan;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterStatus;
import eu.dc4cities.controlsystem.model.easc.EascMetrics;
import eu.dc4cities.controlsystem.model.erds.DataCenterForecast;
import eu.dc4cities.controlsystem.model.util.TimeRangeUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.util.List;

/**
 * Handles the lifecycle of optimization loops. This bean is intended as a singleton, which lives for the life of
 * the application, while each time a loop is run, new instances of PowerLoop, ControlLoop, MonitoringLoop and related
 * delegates (ErdsHandler, PowerPlanner and so on) are spawn and discarded at the end of the optimization run. In this
 * way optimization components don't have to worry about clearing their state between different optimizations. This also
 * means any state that must persist between optimization loops must be kept in the {@code OptimizationManager}.
 * <p>
 * The class is abstract in order to get {@code PowerLoop}, {@code ControlLoop} and {@code MonitoringLoop} beans via
 * lookup method injection from {@code newPowerLoop()}, {@code newControlLoop()} and {@code newMonitoringLoop()}, since
 * {@code OptimizationManager} is a singleton bean while the loops are prototype beans.
 */
public abstract class OptimizationManager {
	
	private static final Logger logger = LoggerFactory.getLogger(OptimizationManager.class);
	
	private TechnicalConfiguration technicalConfiguration;
	private TaskScheduler taskScheduler;
	private HistoricalDbDao hdbDao;
	private Object optimizationLock = new Object();
	private Object statusLock = new Object();
	private FederationStatus federationStatus;
	
	public OptimizationManager(TechnicalConfiguration technicalConfiguration, TaskScheduler taskScheduler, 
			HistoricalDbDao hdbDao) {
		this.technicalConfiguration = technicalConfiguration;
		this.taskScheduler = taskScheduler;
		this.hdbDao = hdbDao;
	}
	
	/**
	 * Returns the current execution plan for the given data center.
	 * 
	 * @param dataCenter the name of the data center
	 * @return the execution plan for the data center or {@code null} if not found
	 */
	public DataCenterExecutionPlan getDataCenterExecutionPlan(String dataCenter) {
		synchronized (statusLock) {
			if (federationStatus != null && federationStatus.getDataCenterExecutionPlans() != null) {
				for (DataCenterExecutionPlan plan : federationStatus.getDataCenterExecutionPlans()) {
					if (plan.getDataCenterName().equals(dataCenter)) {
						return plan;
					}
				}
			}
			return null;
		}
	}
	
	/**
	 * Starts scheduled optimization loops based on the technical configuration. Scheduling is based on the
	 * {@code powerLoopInterval}, {@code controlLoopInterval} and {@code monitoringLoopInterval} settings. If an
	 * interval is 0, the corresponding loop is not scheduled. All loops must be enabled when the control loop is
	 * enabled.
	 * <p>
	 * Scheduled execution is always aligned to time slots, so the first scheduled loop triggers at the beginning of the
	 * first time slot following this method invocation.
	 */
	public void startScheduledLoops() {
		int powerInterval = technicalConfiguration.getPowerLoopInterval();
		int controlInterval = technicalConfiguration.getControlLoopInterval();
		int monitoringInterval = technicalConfiguration.getMonitoringLoopInterval();
		if (powerInterval == 0 && controlInterval == 0 && monitoringInterval == 0) {
			return;
		}
		validateSchedulingConfig();
		DateTime startAt = technicalConfiguration.getStartAt();
		if (startAt == null || startAt.isBeforeNow()) {
			startAt = TimeRangeUtils.calcNextTimeSlot(technicalConfiguration.getTimeSlotWidth());
		}
		LoopExecutor loopExecutor = new LoopExecutor(startAt);
		taskScheduler.scheduleAtFixedRate(loopExecutor, startAt.toDate(), monitoringInterval * 60000);
	}
	
	private void validateSchedulingConfig() {
		DateTime startAt = technicalConfiguration.getStartAt();
		int powerInterval = technicalConfiguration.getPowerLoopInterval();
		int controlInterval = technicalConfiguration.getControlLoopInterval();
		int monitoringInterval = technicalConfiguration.getMonitoringLoopInterval();
		int timeSlotWidth = technicalConfiguration.getTimeSlotWidth();
		int timeWindowWidth = technicalConfiguration.getTimeWindowWidth();
		if (powerInterval < 0 || controlInterval < 0 || monitoringInterval < 0) {
			throw new IllegalArgumentException("Loop intervals must be >= 0");
		} else if (timeSlotWidth <= 0 || timeWindowWidth <= 0) {
			throw new IllegalArgumentException("timeSlotWidth and timeWindowWidth must be > 0");
		} else if (controlInterval != 0 && (powerInterval == 0 || monitoringInterval == 0)) {
			throw new IllegalArgumentException("All loops must be enabled when the control loop is on");
		} else if ((timeWindowWidth * 60) % timeSlotWidth != 0) {
			throw new IllegalArgumentException("timeWindowWidth must be a multiple of timeSlotWidth");
		} else if (powerInterval != 0 && powerInterval % timeSlotWidth != 0) {
			throw new IllegalArgumentException("powerLoopInterval must be a multiple of timeSlotWidth");
		} else if (controlInterval != 0 && controlInterval % timeSlotWidth != 0) {
			throw new IllegalArgumentException("controlLoopInterval must be a multiple of timeSlotWidth");
		} else if (monitoringInterval != 0 && timeSlotWidth % monitoringInterval != 0) {
			throw new IllegalArgumentException("monitoringLoopInterval must be a divisor of timeSlotWidth");
		} else if (startAt != null && !TimeRangeUtils.isTimeSlotStart(startAt, timeSlotWidth)) {
			throw new IllegalArgumentException("startAt must be aligned to a time slot");
		}
	}
	
	/**
	 * Starts an immediate optimization job for the federation, using dateNow as the current time. The
	 * optimization range is determined based on dateNow. Execution is asynchronous.
	 * 
	 * @param dateNow the date to use as the current time; must be aligned to a time slot
	 */
	public void startImmediateOptimization(DateTime dateNow) {
		if (!TimeRangeUtils.isTimeSlotStart(dateNow, technicalConfiguration.getTimeSlotWidth())) {
			throw new IllegalArgumentException("dateNow must be aligned to a time slot");
		}
		taskScheduler.schedule(new LoopExecutor(dateNow), DateTime.now().toDate());
	}
	
	/**
	 * Gets a new {@code PowerLoop} bean via lookup injection.
	 * 
	 * @return the bean to handle a single run of the power planning loop
	 */
	protected abstract PowerLoop newPowerLoop();
	
	/**
	 * Gets a new {@code ControlLoop} bean via lookup injection.
	 * 
	 * @return the bean to handle a single run of the EASC control loop
	 */
	protected abstract ControlLoop newControlLoop();
	
	/**
	 * Gets a new {@code MonitoringLoop} bean via lookup injection.
	 * 
	 * @return the bean to handle a single run of the EASC monitoring loop
	 */
	protected abstract MonitoringLoop newMonitoringLoop();
	
	private class LoopExecutor implements Runnable {
		
		private DateTime nextExecutionDate;
		private DateTime nextPowerLoopDate;
		private DateTime nextControlLoopDate;
		
		private int powerLoopInterval = technicalConfiguration.getPowerLoopInterval();
		private int controlLoopInterval = technicalConfiguration.getControlLoopInterval();
		private int monitoringLoopInterval = technicalConfiguration.getMonitoringLoopInterval();
		private int timeSlotWidth = technicalConfiguration.getTimeSlotWidth();
		
		/**
		 * Creates a new LoopExecutor. The executor must be initialized with the date of the first scheduled execution,
		 * which is used to initialize the dateNow of the loops precisely (relying on the system date would introduce
		 * deviations due to triggering delays in the task scheduler). The executor MUST be scheduled to run at every
		 * monitoring interval.
		 * 
		 * @param firstExecutionDate the first scheduled execution date of the executor; must be aligned to a time slot
		 */
		public LoopExecutor(DateTime firstExecutionDate) {
			this.nextExecutionDate = firstExecutionDate;
			this.nextPowerLoopDate = firstExecutionDate;
			this.nextControlLoopDate = firstExecutionDate;
		}
		
		@Override
		public void run() {
			try {
				// Only one optimization job can run at a time
				synchronized (optimizationLock) {
					DateTime dateNow = nextExecutionDate;
					nextExecutionDate = nextExecutionDate.plusMinutes(monitoringLoopInterval);
					// Calculate the next execution dates in advance so they are updated for next time even if an error
					// stops this execution.
					boolean doPowerLoop = false, doControlLoop = false;
					if (dateNow.equals(nextPowerLoopDate)) {
						doPowerLoop = true;
						nextPowerLoopDate = nextPowerLoopDate.plusMinutes(powerLoopInterval);
					}
					if (dateNow.equals(nextControlLoopDate)) {
						doControlLoop = true;
						nextControlLoopDate = nextControlLoopDate.plusMinutes(controlLoopInterval);
					}
					synchronized (statusLock) {
						if (federationStatus == null) {
							federationStatus = new FederationStatus(dateNow, technicalConfiguration);
						}
					}
					try {
						executeMonitoringLoop(dateNow);
					} catch (Exception ex) {
						logger.error("Could not complete monitoring loop", ex);
					}
					if (doPowerLoop || doControlLoop) {
						int windowWidth = technicalConfiguration.getTimeWindowWidth();
						TimeSlotBasedEntity optimizationRange = TimeRangeUtils.calcNearestTimeRange(dateNow, 
								windowWidth, timeSlotWidth);
						if (doPowerLoop) {
							try {
								executePowerLoop(optimizationRange);
							} catch (Exception ex) {
								logger.error("Could not complete power loop", ex);
							}
						}
						if (doControlLoop) {
							try {
								executeControlLoop(dateNow, optimizationRange);
							} catch (Exception ex) {
								logger.error("Could not complete control loop", ex);
							}
						}
					}
					if (technicalConfiguration.isHdbEnabled()) {
						try {
							List<HdbDataCenterMetrics> hdbMetrics;
							List<DataCenterExecutionPlan> executionPlans = null;
							List<DataCenterStatus> statuses = null;
							synchronized (statusLock) {
								hdbMetrics = federationStatus.getHdbMetrics();
								if (doControlLoop) {
									executionPlans = federationStatus.getDataCenterExecutionPlans();
									statuses = federationStatus.getDataCenterStatuses();
								}
							}
							logger.debug("Uploading data center metrics to historical database...");
							hdbDao.writeDataCenterMetrics(dateNow, hdbMetrics);
							logger.debug("Data center metrics uploaded");
							if (doControlLoop) {
								logger.debug("Uploading power plan metrics to historical database...");
								hdbDao.writeExecutionPlanMetrics(executionPlans);
								logger.debug("Power plan metrics uploaded");
								// The list of statuses is empty if the escalation manager is disabled
								if (statuses.size() > 0) {
									logger.debug("Uploading status alarms to historical database...");
									hdbDao.writeStatusMetrics(statuses);
									logger.debug("Status alarms uploaded");
								}
							}
						} catch (Exception ex) {
							logger.error("Could not upload metrics to historical database", ex);
						}
					}
				}
			} catch (Exception e) {
				logger.error("Error during scheduled loop execution", e);
			}
		}
		
		private void executeMonitoringLoop(DateTime dateNow) {
			MonitoringLoop monitoringLoop = newMonitoringLoop();
			List<EascMetrics> eascMetrics = monitoringLoop.execute(dateNow);
			synchronized (statusLock) {
				federationStatus.updateEascMetrics(dateNow, eascMetrics);
			}
		}
		
		private List<DataCenterForecast> executePowerLoop(TimeSlotBasedEntity optimizationRange) {
			TimeSlotBasedEntity actualRange = new TimeSlotBasedEntity(optimizationRange);
			DateTime dateTo = actualRange.getDateTo();
			if (powerLoopInterval > controlLoopInterval) {
				// If the power loop is less frequent than the control loop, the forecast needs to cover up to
				// the next power loop execution
				dateTo = dateTo.plusMinutes(powerLoopInterval - controlLoopInterval);
				actualRange.setDateTo(dateTo);
			}
			// Make sure the range covers full days, from the beginning to the end, since the optimizer needs to plan
			// SLAs over full days
			extendToFullDays(actualRange);
			PowerLoop powerLoop = newPowerLoop();
			List<DataCenterForecast> forecasts = powerLoop.execute(actualRange);
			synchronized (statusLock) {
				federationStatus.setDataCenterForecasts(forecasts);
			}
			return forecasts;
		}
		
		private void extendFromStartOfDay(TimeSlotBasedEntity range) {
			DateTime dateFrom = range.getDateFrom();
			if (dateFrom.getMillisOfDay() != 0) {
				dateFrom = dateFrom.withTimeAtStartOfDay();
				range.setDateFrom(dateFrom);
			}
		}
		
		private void extendToEndOfDay(TimeSlotBasedEntity range) {
			DateTime dateTo = range.getDateTo();
			if (dateTo.getMillisOfDay() != 0) {
				dateTo = dateTo.withTimeAtStartOfDay().plusDays(1);
				range.setDateTo(dateTo);
			}
		}
		
		private void extendToFullDays(TimeSlotBasedEntity range) {
			extendFromStartOfDay(range);
			extendToEndOfDay(range);
		}
		
		private void executeControlLoop(DateTime dateNow, TimeSlotBasedEntity optimizationRange) {
			TimeSlotBasedEntity actualRange = new TimeSlotBasedEntity(optimizationRange);
			// Make sure the range ends at the end of the day since the optimizer needs to plan SLAs over full days
			extendToEndOfDay(actualRange);
			// Forecasts must start from the beginning of the day to allow the optimizer to get the rec pct for the past
			TimeSlotBasedEntity forecastRange = new TimeSlotBasedEntity(actualRange);
			extendFromStartOfDay(forecastRange);
			ControlLoop controlLoop = newControlLoop();
			TimeParameters timeParameters = new TimeParameters(dateNow, actualRange);
			List<DataCenterOptimization> optimizations = controlLoop.execute(timeParameters,
					federationStatus.getDataCenterForecasts(forecastRange),
					federationStatus.getDataCenterPowerActuals(), federationStatus.getEascServiceLevels(),
					federationStatus.getLatestEascMetrics(), federationStatus.getDataCenterExecutionPlans());
			List<DataCenterExecutionPlan> executionPlans = DataCenterOptimization.getExecutionPlans(optimizations);
			List<DataCenterStatus> statuses = DataCenterOptimization.getStatuses(optimizations);
			synchronized (statusLock) {
				federationStatus.setDataCenterExecutionPlans(executionPlans);
				federationStatus.setDataCenterStatuses(statuses);
			}
		}
		
	}
	
}
