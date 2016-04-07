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

import eu.dc4cities.controlsystem.model.datacenter.AlertSeverity;

public class HdbAlertState {

	public static final int NONE = 0;
	public static final int WARNING = 1;
	public static final int ALARM = 2;
	
	public static int from(AlertSeverity severity) {
		if (severity.equals(AlertSeverity.WARNING)) {
			return WARNING;
		} else if (severity.equals(AlertSeverity.ALARM)) {
			return ALARM;
		} else {
			throw new IllegalArgumentException("Unsupported severity: " + severity);
		}
	}
	
}
