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

package eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.converter;

import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.model.erds.TimeSlotErdsForecast;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.OldPowerSource;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.OldPowerSourceSlot;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Duration;
import javax.measure.unit.SI;
import java.util.ArrayList;
import java.util.List;

/**
 * Convert an ErdsForecast of renewable energy to power sources.
 * <p/>
 * The power source identifier equals the erds identifier.
 * Values are caster to integers. It is expected to have a percentage of renewable energies btw. 0 < x < 100.
 *
 *
 */
public class ErdsForecastToPowerSource {

    public static final OldPowerSourceSlot ZERO = new OldPowerSourceSlot(Amount.valueOf(0, SI.WATT), 0, 0);

    /**
     * Make the conversion.
     *
     * @param fc            the forecast to convert
     * @param slotsDuration the duration of a slot.
     * @return the resulting PowerSource
     */
    public OldPowerSource convert(ErdsForecast fc, Amount<Duration> slotsDuration) {
        List<OldPowerSourceSlot> slots = new ArrayList<>();
        //The slots start at 1, this is painful to due the shifting in the OPCP.
        //So a fake slot at 0 to be aligned with the SimpleActivityDesign
        slots.add(ZERO);
        for (TimeSlotErdsForecast t : fc.getTimeSlotForecasts()) {
            slots.add(new OldPowerSourceSlot(t.getPower(),
                    (int) t.getRenewablePercentage().getExactValue(),
                    t.getCo2Factor().getExactValue()));
        }
        return new OldPowerSource(fc.getErdsName(), slots.toArray(new OldPowerSourceSlot[slots.size()]));
    }
}

