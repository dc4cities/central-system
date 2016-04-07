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
 * The configuration for an external service, such as an ERDS or EASC.
 */
public class ServiceConfiguration {

	private String name;
	private String endpoint;
	
	@JsonCreator
	public ServiceConfiguration(@JsonProperty("name") String name) {
		this.name = name;
	}

	public ServiceConfiguration(String name, String endpoint) {
		this.name = name;
		this.endpoint = endpoint;
	}

	/**
	 * Returns the name used to identify the external service.
	 * 
	 * @return the name of the external service
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the API endpoint of the external service. This will be the base URL of the Northbound (for ERDS) or
	 * Southbound (for EASC) API.
	 * 
	 * @return the API endpoint of the external service
	 */
	public String getEndpoint() {
		return endpoint;
	}
	
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
}
