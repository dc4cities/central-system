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

package eu.dc4cities.controlsystem.modules.escalationmanager;

import eu.dc4cities.controlsystem.model.easc.Activity;

import java.util.LinkedList;
import java.util.List;

public class EascAct 
{
	private String eascName;
	private List<Activity> involvedActivities;
	
	public EascAct()
	{		
		this.involvedActivities = new LinkedList<Activity>();
	}
	
	public EascAct(String eascName)
	{
		this.eascName = eascName;
		this.involvedActivities = new LinkedList<Activity>();
	}
	
	public void addActivity(Activity a)
	{
		this.involvedActivities.add(a);
	}

	/**
	 * @return the eascName
	 */
	public String getEascName() {
		return eascName;
	}

	/**
	 * @param eascName the eascName to set
	 */
	public void setEascName(String eascName) {
		this.eascName = eascName;
	}

	/**
	 * @return the involvedActivities
	 */
	public List<Activity> getInvolvedActivities() {
		return involvedActivities;
	}

	/**
	 * @param involvedActivities the involvedActivities to set
	 */
	public void setInvolvedActivities(List<Activity> involvedActivities) {
		this.involvedActivities = involvedActivities;
	}

}
