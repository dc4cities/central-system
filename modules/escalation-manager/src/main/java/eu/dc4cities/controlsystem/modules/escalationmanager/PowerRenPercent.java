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

import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Power;

public class PowerRenPercent implements Comparable
{
	
	private Amount<Power> power;
	private Amount<Dimensionless> renPercent;
	
	/**
	 * 
	 */
	public PowerRenPercent() {
	}
	/**
	 * @param power
	 * @param renPercent
	 */
	public PowerRenPercent(Amount<Power> power, Amount<Dimensionless> renPercent) {
		this.power = power;
		this.renPercent = renPercent;
	}
	
	/**
	 * @return the power
	 */
	public Amount<Power> getPower() {
		return power;
	}
	/**
	 * @param power the power to set
	 */
	public void setPower(Amount<Power> power) {
		this.power = power;
	}
	/**
	 * @return the renPercent
	 */
	public Amount<Dimensionless> getRenPercent() {
		return renPercent;
	}
	/**
	 * @param renPercent the renPercent to set
	 */
	public void setRenPercent(Amount<Dimensionless> renPercent) {
		this.renPercent = renPercent;
	}
	
	@Override
	public boolean equals(Object o) 
	{
		if(o instanceof PowerRenPercent)
		{
			PowerRenPercent prp = (PowerRenPercent)o;
			return this.renPercent.equals(prp.renPercent);
		}
		return false;
	}
	@Override
	public int compareTo(Object o) 
	{
		if(o instanceof PowerRenPercent)
		{
			PowerRenPercent prp = (PowerRenPercent)o;
			return this.renPercent.compareTo(prp.renPercent);
		}
		return 0;
	}
	
	
	

}
