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

/**
 * A group of EASCs for power splitting.
 */
public class EascGroup {

	private String name;
	private int groupWeight;
	private List<EascWeight> eascWeights;
	
	@JsonCreator
	public EascGroup(@JsonProperty("name") String name) {
		this.name = name;
	}

	/**
	 * Returns the name of the EASC group.
	 * 
	 * @return the name of the EASC group
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the weight of the EASC group. This is used for the first level of power splitting. Then the power
	 * assigned to the group is further split among single EASCs using their specific weights.
	 * 
	 * @return the weight of the EASC group
	 */
	public int getGroupWeight() {
		return groupWeight;
	}
	
	public void setGroupWeight(int groupWeight) {
		this.groupWeight = groupWeight;
	}

	/**
	 * Returns the list of EASCs in the group, with their weights.
	 * 
	 * @return the list of EASC weights
	 */
	public List<EascWeight> getEascWeights() {
		return eascWeights;
	}
	
	public void setEascWeights(List<EascWeight> eascWeights) {
		this.eascWeights = eascWeights;
	}
	
}
