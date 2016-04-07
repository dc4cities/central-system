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

package eu.dc4cities.controlsystem.model.unit;

import eu.dc4cities.controlsystem.model.quantity.EnergyPrice;
import eu.dc4cities.controlsystem.model.quantity.GasEmission;
import org.jscience.economics.money.Currency;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Energy;
import javax.measure.unit.*;

import static javax.measure.unit.NonSI.BYTE;
import static javax.measure.unit.SI.*;
import static javax.measure.unit.Unit.ONE;

/**
 * Defines custom JScience units for DC4Cities
 */
public class Units {

	public static final Unit<Energy> WATT_HOUR = SI.JOULE.times(3600);
	public static final Unit<Energy> KILOWATT_HOUR = KILO(WATT_HOUR);
	public static final Unit<EnergyPrice> EUR_PER_KWH = 
			new ProductUnit<EnergyPrice>(Currency.EUR.divide(Units.KILOWATT_HOUR));
	public static final Unit<GasEmission> KG_PER_KWH = 
			new ProductUnit<GasEmission>(SI.KILOGRAM.divide(Units.KILOWATT_HOUR));
	public static final Unit<Dimensionless> PERCENTAGE_POINT = NonSI.PERCENT.divide(100);

	public static final Unit<Dimensionless> PAGE = ONE.alternate("Page");
	public static final Unit<Dimensionless> REQUEST = ONE.alternate("Req");
	public static final Unit<Dimensionless> EXAM = ONE.alternate("Exam");
	public static final Unit<Dimensionless> WEBS = ONE.alternate("Webs");
	
	private static boolean initialized;
	
	/**
	 * Registers custom unit definitions in JScience.
	 */
	public static void init() {
		if (!initialized) {
			UnitFormat unitFormat = UnitFormat.getInstance();
			unitFormat.label(WATT_HOUR, "Wh");
			unitFormat.label(KILOWATT_HOUR, "kWh");
			unitFormat.label(PERCENTAGE_POINT, "pp");
			// Force initialization of the Currency class so labels are registered in the parser
			unitFormat.label(Currency.EUR, "EUR");
			// Data amount unit labels
			unitFormat.label(GIGA(SI.BIT), "Gbit");
			unitFormat.label(KILO(BYTE), "KB");
			unitFormat.label(MEGA(BYTE), "MB");
			unitFormat.label(GIGA(BYTE), "GB");
			unitFormat.label(KILO(PAGE), "kPage");
			initialized = true;
		}
	}
}
