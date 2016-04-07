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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DataCenterConfiguration {

	private String name;
	private int minPower;
	private int maxPower;
	private Double aggressiveness;
	private double pue;
	private List<ServiceConfiguration> erdsList;
	private List<EascGroup> eascGroups;
	
	@JsonCreator
	public DataCenterConfiguration(@JsonProperty("name") String name) {
		this.name = name;
	}

	/**
	 * Returns the name used to identify the data center
	 * 
	 * @return the data center name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the minimum power that should be allocated to the data center in the ideal power plan.
	 * 
	 * @return the minimum power for the data center, in watts
	 */
	public int getMinPower() {
		return minPower;
	}

	public void setMinPower(int minPower) {
		this.minPower = minPower;
	}

	/**
	 * Returns the maximum power that should be allocated to the data center in the ideal power plan.
	 * 
	 * @return the maximum power for the data center, in watts
	 */
	public int getMaxPower() {
		return maxPower;
	}

	public void setMaxPower(int maxPower) {
		this.maxPower = maxPower;
	}

	/**
	 * Returns the aggressiveness factor to use for generating the ideal power plan. The default value in the
	 * power planner is used if this is not specified in the configuration.
	 * 
	 * @return the ideal power plan aggressiveness factor or {@code null} if not set
	 */
	public Double getAggressiveness() {
		return aggressiveness;
	}

	public void setAggressiveness(Double aggressiveness) {
		this.aggressiveness = aggressiveness;
	}

	/**
	 * Returns the power usage effectiveness factor of the data center.
	 * 
	 * @return the PUE factor of the data center
	 */
	public double getPue() {
		return pue;
	}

	public void setPue(double pue) {
		this.pue = pue;
	}

	/**
	 * Returns the list of ERDS services connected to the data center.
	 * 
	 * @return the list of ERDS services
	 */
	public List<ServiceConfiguration> getErdsList() {
		return erdsList;
	}

	public void setErdsList(List<ServiceConfiguration> erdsList) {
		this.erdsList = erdsList;
	}

	/**
	 * Returns the list of EASC groups in the data center. Each group as a whole, and each single EASC in the group is
	 * assigned a weight. Those weights are then used to split power and energy among all EASCs.
	 * 
	 * @return the list of power-splitting EASC groups
	 */
	public List<EascGroup> getEascGroups() {
		return eascGroups;
	}

	public void setEascGroups(List<EascGroup> eascGroups) {
		this.eascGroups = eascGroups;
	}
	
}
