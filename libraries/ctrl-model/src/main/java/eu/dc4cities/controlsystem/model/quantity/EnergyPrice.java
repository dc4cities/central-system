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

package eu.dc4cities.controlsystem.model.quantity;

import eu.dc4cities.controlsystem.model.unit.Units;
import org.jscience.economics.money.Money;

import javax.measure.quantity.Quantity;
import javax.measure.unit.ProductUnit;
import javax.measure.unit.Unit;

/**
 * Represents the price of energy, expressed in money per energy unit (e.g. EUR/kWh).
 */
public interface EnergyPrice extends Quantity {

	public final static Unit<EnergyPrice> BASE_UNIT = 
			new ProductUnit<EnergyPrice>(Money.BASE_UNIT.divide(Units.KILOWATT_HOUR));
	
}
