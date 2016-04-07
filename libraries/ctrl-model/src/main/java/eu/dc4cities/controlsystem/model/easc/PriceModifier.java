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
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jscience.physics.amount.Amount;

/**
 * Defines a price penalty or reward that applies below a given performance threshold. Modifiers are typically given in
 * an ordered list, so the first element defines the highest threshold and the last element the lowest. No modifier
 * applies for performances above the highest threshold, while the modifier associated to a threshold applies to all
 * performance levels between that threshold and the next (lower) threshold. The modifier for the last threshold in the
 * list applies to all lower performance levels.
 * <p>
 * Thresholds with a positive modifier (reward) will be higher than the performance objective in the SLO, while
 * thresholds with a negative modifier (penalty) will be lower than that performance level. An interval with modifier 0
 * (i.e. no changes w.r.t. the base price) can be defined around the SLO performance objective.
 * 
 * @see ServiceLevelObjective
 */
public class PriceModifier {
	private Amount<?> threshold;
	private Amount<?> modifier;
	
	public PriceModifier() {
	}

	@JsonCreator
	public PriceModifier(@JsonProperty("threshold") Amount<?> threshold, @JsonProperty("modifier") Amount<?> modifier) {
		this.threshold = threshold;
		this.modifier = modifier;
	}

	/**
	 * Returns the performance level threshold below which this modifier applies. The threshold may be expressed as an
	 * instant or cumulative performance depending on the SLO. 
	 * 
	 * @return the performance threshold
	 */
	public Amount<?> getThreshold() {
		return threshold;
	}

	/**
	 * Returns the price modifier that applies below the threshold. The modifier is incremental, e.g. if the business
	 * objective is processing a certain number of pages, the modifier may be -1 EUR /page, meaning the price paid by
	 * the customer decreases by 1 EUR for each page less than the threshold that the system can process.
	 * 
	 * @return the price modifier
	 */
	public Amount<?> getModifier() {
		return modifier;
	}

	public void setThreshold(Amount<?> threshold) {
		this.threshold = threshold;
	}

	public void setModifier(Amount<?> modifier) {
		this.modifier = modifier;
	}
	
}
