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
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Power;
import javax.measure.unit.SI;
import java.util.LinkedList;
import java.util.List;

public class AggregateActivityValues 
{
	private List<EascAct> involvedEascActivities = new LinkedList<EascAct>();
	private Amount<Power> totalPowerConsumption;
	DateTime timestamp;
	
	public AggregateActivityValues()
	{
		this.totalPowerConsumption = Amount.valueOf(0.0, SI.WATT);
	}
	
	public AggregateActivityValues(DateTime timestamp)
	{
		this.timestamp = timestamp;
		this.totalPowerConsumption = Amount.valueOf(0.0, SI.WATT);
	}
		
	public void addWorkContribution(Amount<Power> pow, String eascName, Activity activity)
	{
		this.totalPowerConsumption = this.totalPowerConsumption.plus(pow);
		
		if(eascName!=null)
		{
			boolean isEascPresent = false;
			for(int i=0 ; !isEascPresent && i<this.involvedEascActivities.size() ; i++)
			{
				if(this.involvedEascActivities.get(i).getEascName().equalsIgnoreCase(eascName))
				{
					this.involvedEascActivities.get(i).addActivity(activity);
					isEascPresent = true;
				}
			}
			if (!isEascPresent)
			{
				EascAct eascAct = new EascAct(eascName);
				eascAct.addActivity(activity);
				this.involvedEascActivities.add(eascAct);
			}
		}
	}

	/**
	 * @return the involvedEascActivities
	 */
	public List<EascAct> getInvolvedEascActivities() {
		return involvedEascActivities;
	}

	/**
	 * @param involvedEascActivities the involvedEascActivities to set
	 */
	public void setInvolvedEascActivities(List<EascAct> involvedEascActivities) {
		this.involvedEascActivities = involvedEascActivities;
	}

	/**
	 * @return the totalPowerConsumption
	 */
	public Amount<Power> getTotalPowerConsumption() {
		return totalPowerConsumption;
	}

	/**
	 * @param totalPowerConsumption the totalPowerConsumption to set
	 */
	public void setTotalPowerConsumption(Amount<Power> totalPowerConsumption) {
		this.totalPowerConsumption = totalPowerConsumption;
	}

	/**
	 * @return the timestamp
	 */
	public DateTime getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(DateTime timestamp) {
		this.timestamp = timestamp;
	}	
	
	
	

}
