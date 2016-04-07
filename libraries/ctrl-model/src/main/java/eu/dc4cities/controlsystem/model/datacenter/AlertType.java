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

package eu.dc4cities.controlsystem.model.datacenter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines possible types for status alerts.
 * 
 * @see StatusAlert
 */
public enum AlertType {

	RENPCT("renpct", "smartcity"),
	BIZPERF("bizperf", "sla");

	private static Map<String, AlertType> types = new HashMap<>();
	static {
		types.put(RENPCT.value, RENPCT);
		types.put(BIZPERF.value, BIZPERF);
	}
	
	private String value;
	private String classification;
	
	private AlertType(String value, String classification) {
		this.value = value;
		this.classification = classification;
	}
	
	@JsonCreator
	public static AlertType from(String value) {
		return types.get(value);
	}
	
	@JsonValue
	public String value() {
		return value;
	}
	
	public String classification() {
		return classification;
	}
	
	@Override
	public String toString() {
		return value;
	}

}
