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
 * Defines status codes for a data center.
 */
@Deprecated
public enum StatusCode {

	OK("ok"),
	WARNING("warning"),
	ALARM("alarm");

	private static Map<String, StatusCode> codes = new HashMap<>();
	static {
		codes.put(OK.value, OK);
		codes.put(WARNING.value, WARNING);
		codes.put(ALARM.value, ALARM);
	}
	
	private String value;
	
	private StatusCode(String value) {
		this.value = value;
	}
	
	@JsonCreator
	public static StatusCode from(String value) {
		return codes.get(value);
	}
	
	@JsonValue
	public String value() {
		return value;
	}
	
	@Override
	public String toString() {
		return value;
	}

}
