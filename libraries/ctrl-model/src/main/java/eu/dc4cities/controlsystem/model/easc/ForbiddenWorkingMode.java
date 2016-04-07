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

package eu.dc4cities.controlsystem.model.easc;

/**
 * An item in a combination of working modes that constitutes a forbidden state.
 */
public class ForbiddenWorkingMode {

	private String dataCenterName;
	private String workingModeName;

	/**
	 * A forbidden working mode.
	 *
	 * @param dc the data centre
	 * @param wm the denied working mode for that data centre
	 */
	public ForbiddenWorkingMode(String dc, String wm) {
		dataCenterName = dc;
		workingModeName = wm;
	}

	/**
	 * A new empty forbidden working mode.
	 */
	public ForbiddenWorkingMode() {
		this(null, null);
	}
	/**
	 * Returns the name of the data center in which the working mode is defined.
	 * 
	 * @return the data center name
	 */
	public String getDataCenterName() {
		return dataCenterName;
	}

	public void setDataCenterName(String dataCenterName) {
		this.dataCenterName = dataCenterName;
	}
	
	/**
	 * Returns the name of the working mode that is part of a forbidden combination.
	 * 
	 * @return the working mode name
	 */
	public String getWorkingModeName() {
		return workingModeName;
	}

	public void setWorkingModeName(String workingModeName) {
		this.workingModeName = workingModeName;
	}
	
}
