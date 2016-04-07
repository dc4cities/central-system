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

package eu.dc4cities.controlsystem.model.util;

import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Duration;
import javax.measure.unit.Dimension;
import javax.measure.unit.ProductUnit;
import javax.measure.unit.Unit;

/**
 * Provides utility methods for working with JScience amounts.
 */
public class AmountUtils {

	/**
	 * Returns whether the given amount is a quantity per time, i.e. something like "items/s".
	 * The method checks that the unit is a product unit with 2 elements, where the numerator
	 * can be anything and the denominator must be in the dimension of time (e.g. seconds, minutes, etc.).
	 * 
	 * @param amount the amount to check
	 * @return whether the amount is a quantity per time
	 */
	public static boolean isQuantityPerTime(Amount<?> amount) {
    	Unit<?> unit = amount.getUnit();
    	if (unit instanceof ProductUnit) {
    		ProductUnit<?> productUnit = (ProductUnit<?>) unit;
    		if (productUnit.getUnitCount() == 2 && productUnit.getUnitPow(1) == -1) {
    			return ((ProductUnit<?>) unit).getUnit(1).getDimension().equals(Dimension.TIME);
    		}
    	}
    	return false;
    }
	
	/**
	 * Returns whether the given amount has a fractional unit, with any numerator and denominatorUnit as the
	 * denominator. The numerator unit should be a simple unit (not a product) while the denominatorUnit can be a
	 * product.
	 * 
	 * @param amount the amount to check
	 * @param denominatorUnit the expected denominator unit
	 * @return whether the amount is a quantity per the denominator unit
	 */
	public static boolean isQuantityPerUnit(Amount<?> amount, Unit<?> denominatorUnit) {
    	Unit<?> amountUnit = amount.getUnit();
    	if (!(amountUnit instanceof ProductUnit)) {
    		return false;
    	}
    	ProductUnit<?> productUnit = (ProductUnit<?>) amountUnit;
    	if (productUnit.getUnitCount() <= 1 || productUnit.getUnitPow(0) != 1) {
    		return false;
    	}
    	Unit<?> numeratorUnit = productUnit.getUnit(0);
    	return productUnit.divide(numeratorUnit).inverse().equals(denominatorUnit);
    }
	
	/**
	 * Calculates the cumulative performance when the given instant performance is maintained for the given
	 * duration. This can be used to calculate the total performance over a time slot.
	 * 
	 * @param instantPerformance the instant performance (in items/s, e.g. Gbit/s)
	 * @param duration the duration to compute the total performance on
	 * @return the cumulative performance
	 * @throws IllegalArgumentException if instantPerformance is not a quantity per time (items/s)
	 */
	@SuppressWarnings("unchecked")
	public static Amount<?> calcCumulativePerformance(Amount<?> instantPerformance, Amount<Duration> duration) {
		if (!isQuantityPerTime(instantPerformance)) {
			throw new IllegalArgumentException(
					"instantPerformance is not a quantity per time (" + instantPerformance + ")");
		}
		ProductUnit<?> amountUnit = (ProductUnit<?>) instantPerformance.getUnit();
		Unit<Duration> timeUnit = (Unit<Duration>) amountUnit.getUnit(1);
		// Convert to the same time unit as the amount since JScience doesn't simplify units when they are different
		// (e.g. "10 bit/s" * "1 min" becomes "10 bit * min/s" instead of "60 bit")
		Amount<Duration> convertedDuration = duration.to(timeUnit);
		return instantPerformance.times(convertedDuration);
	}
	
}
