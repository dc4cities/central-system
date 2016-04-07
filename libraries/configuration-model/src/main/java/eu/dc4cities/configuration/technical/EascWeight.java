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

/**
 * The weight of a single EASC in a power-splitting group.
 */
public class EascWeight {

	private String eascName;
	private int weight;
	
	@JsonCreator
	public EascWeight(@JsonProperty("name") String eascName) {
		this.eascName = eascName;
	}

	/**
	 * Returns the name of the EASC the weight refers to.
	 * 
	 * @return the name of the EASC
	 */
	public String getEascName() {
		return eascName;
	}

	/**
	 * Returns the weight of the EASC.
	 * 
	 * @return the EASC weight
	 */
	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	
}
