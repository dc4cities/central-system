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
import eu.dc4cities.controlsystem.model.unit.Units;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.OldPowerSource;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.OldPowerSourceSlot;
import org.jscience.physics.amount.Amount;
import org.junit.Assert;
import org.junit.Test;

import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.util.ArrayList;

/**
 *
 */
public class ErdsForecastToOldPowerSourceTest {

    @Test
    public void testSimple() {
        ErdsForecast fc = new ErdsForecast("foo");
        fc.setTimeSlotForecasts(new ArrayList<TimeSlotErdsForecast>());
        for (int i = 1; i <= 4; i++) {
            TimeSlotErdsForecast t = new TimeSlotErdsForecast(i);
            t.setPower(Amount.valueOf(100, SI.WATT));
            t.setRenewablePercentage(Amount.valueOf(10 * i, Unit.ONE));
            t.setCo2Factor(Amount.valueOf(7 * i, Units.KG_PER_KWH));
            fc.getTimeSlotForecasts().add(t);
        }
        ErdsForecastToPowerSource c = new ErdsForecastToPowerSource();
        OldPowerSource src = c.convert(fc, Amount.valueOf(60, SI.SECOND));
        Assert.assertEquals(5, src.getSlots().length); //4 + 1 cause slots starts at 1
        System.out.println(src);
        Assert.assertEquals(ErdsForecastToPowerSource.ZERO, src.getSlots()[0]);
        for (int i = 1; i < src.getSlots().length; i++) {
            OldPowerSourceSlot s = src.getSlots()[i];
            Assert.assertEquals(s.getPeak().longValue(SI.WATT), 100);
            Assert.assertEquals(s.getRenewablePct(), 10 * i);

        }
    }
}
