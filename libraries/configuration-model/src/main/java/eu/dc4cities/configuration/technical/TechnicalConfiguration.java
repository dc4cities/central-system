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

package eu.dc4cities.configuration.technical;

import org.joda.time.DateTime;

import java.util.List;

/**
 * The Control System configuration. Includes optimization parameters and external service endpoints.
 */
public class TechnicalConfiguration {

	private static final int DEFAULT_ESCALATION_WARNING_THRESHOLD = 75;
	
	private DateTime startAt;
	private int powerLoopInterval;
	private int controlLoopInterval;
	private int monitoringLoopInterval;
	private int timeSlotWidth;
    private int timeWindowWidth;
    private Integer consolidationTimeout;
    private String consolidationHeuristic;
    private boolean escalationManagerEnabled;
    private Integer escalationWarningThreshold = DEFAULT_ESCALATION_WARNING_THRESHOLD;
    private String hdbUrl;
    private boolean hdbEnabled;
	private String companyCode;
	private String energisUrl;
	private String energisApiKey;
	private List<DataCenterConfiguration> dataCenters;
	private List<ServiceConfiguration> eascList;
	private List<ProcessConfig> processConfigList;
	
	/**
	 * Returns the date and time when the system should start executing the control loops. It must be aligned to a time
	 * slot. If startAt is null or in the past, the system starts at the first time slot after it is launched. This
	 * parameter is ignored if automatic execution of control loops is disabled (loop intervals = 0).
	 * 
	 * @return the date and time to start the control loops at
	 */
	public DateTime getStartAt() {
		return startAt;
	}

	public void setStartAt(DateTime startAt) {
		this.startAt = startAt;
	}

	/**
	 * Returns the frequency of the power planning loop.
	 * 
	 * @return the frequency in minutes
	 */
    public int getPowerLoopInterval() {
		return powerLoopInterval;
	}

	public void setPowerLoopInterval(int powerLoopInterval) {
		this.powerLoopInterval = powerLoopInterval;
	}

	/**
	 * Returns the frequency of the EASC control loop.
	 * 
	 * @return the frequency in minutes
	 */
	public int getControlLoopInterval() {
		return controlLoopInterval;
	}

	public void setControlLoopInterval(int controlLoopInterval) {
		this.controlLoopInterval = controlLoopInterval;
	}

	/**
	 * Returns the frequency of the EASC monitoring loop.
	 * 
	 * @return the frequency in minutes
	 */
	public int getMonitoringLoopInterval() {
		return monitoringLoopInterval;
	}

	public void setMonitoringLoopInterval(int monitoringLoopInterval) {
		this.monitoringLoopInterval = monitoringLoopInterval;
	}

	/**
	 * Returns the width of a time slot.
	 * 
	 * @return the time slot width in minutes
	 */
	public int getTimeSlotWidth() {
		return timeSlotWidth;
	}

	public void setTimeSlotWidth(int timeSlotWidth) {
		this.timeSlotWidth = timeSlotWidth;
	}

	/**
	 * Returns the width of the optimization window.
	 * 
	 * @return the optimization window in hours
	 */
	public int getTimeWindowWidth() {
		return timeWindowWidth;
	}

	public void setTimeWindowWidth(int timeWindowWidth) {
		this.timeWindowWidth = timeWindowWidth;
	}

	/**
	 * Returns the timeout for producing an optimized activity plan in the option consolidator. The default value in the
	 * option consolidator is used if this is not specified in the configuration.
	 * 
	 * @return the consolidation timeout in seconds or {@code null} if not set
	 */
	public Integer getConsolidationTimeout() {
		return consolidationTimeout;
	}

	public void setConsolidationTimeout(Integer consolidationTimeout) {
		this.consolidationTimeout = consolidationTimeout;
	}
	
	/**
	 * Returns the name of the heuristic the option consolidator will use for preparing the activity plan. The heuristic
	 * based on the ideal power plan is used if this is not specified in the configuration.
	 * 
	 * @return the heuristic name
	 */
	public String getConsolidationHeuristic() {
		return consolidationHeuristic;
	}

	public void setConsolidationHeuristic(String consolidationHeuristic) {
		this.consolidationHeuristic = consolidationHeuristic;
	}

	/**
	 * Returns whether the escalation manager is enabled. Default is false. When the escalation manager is enabled, any
	 * resulting alerts are uploaded to the historical DB and Energis, provided the HDB is enabled in this 
	 * configuration. Setting {@code hdbEnabled} to {@code false} disables uploads to both the HDB and Energis.
	 * 
	 * @return whether the escalation manager is enabled
	 */
	public boolean isEscalationManagerEnabled() {
		return escalationManagerEnabled;
	}

	public void setEscalationManagerEnabled(boolean escalationManagerEnabled) {
		this.escalationManagerEnabled = escalationManagerEnabled;
	}
	
	/**
	 * Returns the warning threshold used by the escalation manager, in percentage w.r.t. the objective. Default is
	 * {@value #DEFAULT_ESCALATION_WARNING_THRESHOLD}.
	 *  
	 * @return the warning threshold percentage
	 */
	public int getEscalationWarningThreshold() {
		return escalationWarningThreshold;
	}

	public void setEscalationWarningThreshold(Integer escalationWarningThreshold) {
		this.escalationWarningThreshold = escalationWarningThreshold;
	}

	/**
     * Returns the URL of the historical database server.
     * 
     * @return the URL of the historical database server
     */
    public String getHdbUrl() {
		return hdbUrl;
	}

	public void setHdbUrl(String hdbUrl) {
		this.hdbUrl = hdbUrl;
	}
	
	/**
	 * Returns whether the controller should upload metrics to the historical database
	 * 
	 * @return true if the HDB is enabled, false otherwise
	 */
	public boolean isHdbEnabled() {
		return hdbEnabled;
	}

	public void setHdbEnabled(boolean hdbEnabled) {
		this.hdbEnabled = hdbEnabled;
	}
	
	/**
	 * Returns the company code used to store data into the historical database.
	 * 
	 * @return the company code
	 */
	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	/**
	 * Returns the URL of the Energis API used to upload alerts produced by the Escalation Manager. This is required
	 * when both the Escalation Manager and the HDB are enabled, otherwise it can be {@code null}.
	 * 
	 * @return the Energis API URL or {@code null}
	 */
	public String getEnergisUrl() {
		return energisUrl;
	}

	public void setEnergisUrl(String energisUrl) {
		this.energisUrl = energisUrl;
	}

	/**
	 * Returns the key used to authenticate with the Energis API. Can be {@code null} when the Energis API is not
	 * used.
	 * 
	 * @return the Energis API key or {@code null}
	 */
	public String getEnergisApiKey() {
		return energisApiKey;
	}

	public void setEnergisApiKey(String energisApiKey) {
		this.energisApiKey = energisApiKey;
	}

	/**
	 * Returns the list of data centers managed by the control system. Optimization settings are provided for each data
	 * center.
	 * 
	 * @return the list of managed data centers
	 */
	public List<DataCenterConfiguration> getDataCenters() {
		return dataCenters;
	}

	public void setDataCenters(List<DataCenterConfiguration> dataCenters) {
		this.dataCenters = dataCenters;
	}

	/**
	 * Returns the configuration for the given data center.
	 * 
	 * @param name the name of the data center
	 * @return the data center configuration or {@code null} if not found
	 */
	public DataCenterConfiguration getDataCenter(String name) {
		if (dataCenters != null) {
			for (DataCenterConfiguration dataCenter : dataCenters) {
				if (dataCenter.getName().equals(name)) {
					return dataCenter;
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the list of EASCs working in the data centers managed by the control system.
	 * 
	 * @return the list of EASCs
	 */
	public List<ServiceConfiguration> getEascList() {
		return eascList;
	}

	public void setEascList(List<ServiceConfiguration> eascList) {
		this.eascList = eascList;
	}

	@Deprecated
	public ProcessConfig getProcessConfig(String dataCenter) {
    	if (processConfigList != null) {
    		for (ProcessConfig config : processConfigList) {
    			if (config.getDataCenterId().equals(dataCenter)) {
    				return config;
    			}
    		}
    	}
    	return null;
    }
    
	@Deprecated
    public List<ProcessConfig> getProcessConfigList() {
        return processConfigList;
    }

	@Deprecated
    public void setProcessConfigList(List<ProcessConfig> processConfigList) {
        this.processConfigList = processConfigList;
    }
	
}
