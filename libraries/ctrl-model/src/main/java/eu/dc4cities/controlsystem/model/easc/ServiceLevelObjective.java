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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.dc4cities.controlsystem.model.json.JsonUtils;
import org.joda.time.DateTime;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a service level objective for an activity. The objective is valid for a given time interval. In case of
 * service-oriented activities, the objective is expressed as an instant performance that must be respected on average
 * in each time slot, while for task-based activities the objective is a cumulative performance to be achieved within
 * the objective time interval. The objective has a base price that the customer will pay if the objective is met and
 * can be modified (with penalties or rewards) if the performance differs from the objective based on a list of
 * thresholds.
 * 
 * @see ActivitySpecification
 */
public class ServiceLevelObjective {

	private DateTime dateFrom;
	private DateTime dateTo;
	private Amount<?> instantBusinessObjective;
	private Amount<?> cumulativeBusinessObjective;
	private Amount<Money> basePrice;
	private List<PriceModifier> priceModifiers;
	
	public ServiceLevelObjective() {
		super();
		priceModifiers = new ArrayList<>();
	}
	
	public ServiceLevelObjective(DateTime dateFrom, DateTime dateTo,
			Amount<?> instantBusinessObjective,
			Amount<?> cumulativeBusinessObjective, Amount<Money> basePrice,
			List<PriceModifier> priceModifiers) {
		this();
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		this.instantBusinessObjective = instantBusinessObjective;
		this.cumulativeBusinessObjective = cumulativeBusinessObjective;
		this.basePrice = basePrice;
		this.priceModifiers = priceModifiers;
	}

	@JsonCreator
	public ServiceLevelObjective(@JsonProperty("dateFrom") DateTime dateFrom, @JsonProperty("dateTo") DateTime dateTo) {
		this();
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
	}
	
	/**
	 * The start of the time interval
	 * 
	 * @return the start of the time interval
	 */
	public DateTime getDateFrom() {
		return dateFrom;
	}

	/**
	 * The end of the time interval
	 * 
	 * @return the end of the time interval
	 */
	public DateTime getDateTo() {
		return dateTo;
	}

	public void setDateFrom(DateTime dateFrom) {
		this.dateFrom = dateFrom;
	}

	public void setDateTo(DateTime dateTo) {
		this.dateTo = dateTo;
	}

	/**
	 * Returns the instant business performance objective for the time interval. This is set for service-oriented
	 * activities, {@code null} otherwise.
	 * 
	 * @return the instant business performance objective or {@code null}
	 */
	public Amount<?> getInstantBusinessObjective() {
		return instantBusinessObjective;
	}

	public void setInstantBusinessObjective(Amount<?> instantBusinessObjective) {
		this.instantBusinessObjective = instantBusinessObjective;
	}

	/**
	 * Returns the cumulative business performance objective for the time interval. This is set for task-oriented
	 * activities, {@code null} otherwise.
	 * 
	 * @return the cumulative business performance objective or {@code null}
	 */
	public Amount<?> getCumulativeBusinessObjective() {
		return cumulativeBusinessObjective;
	}

	public void setCumulativeBusinessObjective(Amount<?> cumulativeBusinessObjective) {
		this.cumulativeBusinessObjective = cumulativeBusinessObjective;
	}

	/**
	 * Returns the price paid by the customer when the business objective is met.
	 * 
	 * @return the base price
	 */
	public Amount<Money> getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(Amount<Money> basePrice) {
		this.basePrice = basePrice;
	}

	/**
	 * Returns a list of modifiers for the base price that apply if the actual performance during the time interval
	 * differs from the business objective.
	 * 
	 * @return a list of modifiers for the base price
	 */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	public List<PriceModifier> getPriceModifiers() {
		return priceModifiers;
	}

	public void setPriceModifiers(List<PriceModifier> priceModifiers) {
		this.priceModifiers = priceModifiers;
	}
	
	@Override
	public String toString() {
		return JsonUtils.toString(this);
	}
	
}
