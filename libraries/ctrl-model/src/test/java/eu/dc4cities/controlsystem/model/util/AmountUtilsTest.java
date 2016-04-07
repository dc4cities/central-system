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

import org.jscience.economics.money.Currency;
import org.jscience.physics.amount.Amount;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.measure.quantity.Duration;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

public class AmountUtilsTest {
	
	@BeforeClass
	public static void setUpBeforeClass() {
		UnitFormat.getInstance().label(SI.GIGA(SI.BIT), "Gbit");
	}
	
	@Test
	public void testIsQuantityPerTime() {
		Assert.assertFalse(AmountUtils.isQuantityPerTime(Amount.valueOf("10 Gbit")));
		Assert.assertFalse(AmountUtils.isQuantityPerTime(Amount.valueOf("10 N/m")));
		Assert.assertTrue(AmountUtils.isQuantityPerTime(Amount.valueOf("10 Gbit/s")));
	}
	
	@Test
	public void testIsQuantityPerUnit() {
		// We need to initialize JScience with currency labels by using Currency.EUR instead of parsing "1 EUR"
		Amount<?> eur = Amount.valueOf(1, Currency.EUR);
		Amount<?> eurPerSec = Amount.valueOf("1 EUR/s");
		Amount<?> eurPerGbSec = Amount.valueOf("1 EUR/(Gbit/s)");
		Unit<?> gbSec = Unit.valueOf("Gbit/s");
		Unit<?> meterSec = Unit.valueOf("m/s");
		Assert.assertFalse(AmountUtils.isQuantityPerUnit(eur, SI.SECOND));
		Assert.assertTrue(AmountUtils.isQuantityPerUnit(eurPerSec, SI.SECOND));
		Assert.assertFalse(AmountUtils.isQuantityPerUnit(eurPerSec, SI.METER));
		Assert.assertTrue(AmountUtils.isQuantityPerUnit(eurPerGbSec, gbSec));
		Assert.assertFalse(AmountUtils.isQuantityPerUnit(eurPerGbSec, meterSec));
	}
	
	@Test
	public void testCalcCumulativePerformance() {
		Amount<Duration> duration = Amount.valueOf(15, NonSI.MINUTE);
		Amount<?> cumulative;
		try {
			AmountUtils.calcCumulativePerformance(Amount.valueOf("10 Gbit"), duration);
		} catch (IllegalArgumentException ex) {
			// All good, exception expected
		}
		cumulative = AmountUtils.calcCumulativePerformance(Amount.valueOf("10 Gbit/s"), duration);
		Assert.assertEquals(Amount.valueOf("9000 Gbit"), cumulative);
		cumulative = AmountUtils.calcCumulativePerformance(Amount.valueOf("10 Gbit/min"), duration);
		Assert.assertEquals(Amount.valueOf("150 Gbit"), cumulative);
	}
	
}
