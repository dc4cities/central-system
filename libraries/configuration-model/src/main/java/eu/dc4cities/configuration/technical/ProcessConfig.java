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

import org.slf4j.LoggerFactory;

import java.util.List;

@Deprecated
public class ProcessConfig {
	
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ProcessConfig.class);

    private String dataCenterId;
    private String dataCenterAssetCode;
    private String dataCenterCompanyCode;
    private long timeSlotWidth;
    private long timeWindowWidth;
    private long executionSchedule;
    private int timeCompressionFactor;
    private String processClass;
    private int minPower;
    private int maxSuboptimalPower;
    private double pue;

    private List<ERDSRegistryItem> erdsRegistryItems;
    private List<EASCRegistryItem> eascRegistryItems;


    public String getDataCenterId() {
        return dataCenterId;
    }

    public void setDataCenterId(String dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    /**
     * Returns the asset code identifying the data center in the historical database.
     * 
     * @return the asset code identifying the data center in the historical database
     */
    public String getDataCenterAssetCode() {
		return dataCenterAssetCode;
	}

	public void setDataCenterAssetCode(String dataCenterAssetCode) {
		this.dataCenterAssetCode = dataCenterAssetCode;
	}

	/**
     * Returns the code identifying the company the data center belongs to in the historical database.
     * 
     * @return the company code of the data center in the historical database
     */
	public String getDataCenterCompanyCode() {
		return dataCenterCompanyCode;
	}

	public void setDataCenterCompanyCode(String dataCenterCompanyCode) {
		this.dataCenterCompanyCode = dataCenterCompanyCode;
	}

	/**
     * Returns the width of a time slot.
     * 
     * @return the width of time slot in minutes
     */
    public long getTimeSlotWidth() {
        return timeSlotWidth;
    }

    public void setTimeSlotWidth(long timeSlotWidth) {
        this.timeSlotWidth = timeSlotWidth;
    }

    /**
     * Returns the width of the time range to consider in the optimization process.
     * 
     * @return the width of the optimization time range in hours
     */
    public long getTimeWindowWidth() {
        return timeWindowWidth;
    }

    public void setTimeWindowWidth(long timeWindowWidth) {
        this.timeWindowWidth = timeWindowWidth;
    }

    public long getExecutionSchedule() {
        return executionSchedule;
    }

    public void setExecutionSchedule(long executionSchedule) {
        this.executionSchedule = executionSchedule;
    }

    public int getTimeCompressionFactor() {
        return timeCompressionFactor;
    }

    public void setTimeCompressionFactor(int timeCompressionFactor) {
        this.timeCompressionFactor = timeCompressionFactor;
    }

    public String getProcessClass() {
        return processClass;
    }

    public void setProcessClass(String processClass) {
        this.processClass = processClass;
    }
    
    public int getMinPower() {
		return minPower;
	}

	public void setMinPower(int minPower) {
		this.minPower = minPower;
	}

	public int getMaxSuboptimalPower() {
		return maxSuboptimalPower;
	}

	public void setMaxSuboptimalPower(int maxSuboptimalPower) {
		this.maxSuboptimalPower = maxSuboptimalPower;
	}
	
	public double getPue() {
		return pue;
	}

	public void setPue(double pue) {
		this.pue = pue;
	}

	public List<ERDSRegistryItem> getErdsRegistryItems() {
        return erdsRegistryItems;
    }

    public void setErdsRegistryItems(List<ERDSRegistryItem> erdsRegistryItems) {
        this.erdsRegistryItems = erdsRegistryItems;
    }

    public List<EASCRegistryItem> getEascRegistryItems() {
        return eascRegistryItems;
    }

    public void setEascRegistryItems(List<EASCRegistryItem> eascRegistryItems) {
        this.eascRegistryItems = eascRegistryItems;
    }

    public EASCRegistryItem getEascRegistryItemById(String eascId) {
        for (EASCRegistryItem item : eascRegistryItems) {
            if (item.getId().equals(eascId)) {
                return item;
            }
        }
        log.debug("No easc found with id " + eascId);
        return null;
    }
    
    public EASCRegistryItem getEascRegistryItemByName(String eascName) {
        for (EASCRegistryItem item : eascRegistryItems) {
            if (item.getName().equals(eascName)) {
                return item;
            }
        }
        log.debug("No easc found with name " + eascName);
        return null;
    }

}
