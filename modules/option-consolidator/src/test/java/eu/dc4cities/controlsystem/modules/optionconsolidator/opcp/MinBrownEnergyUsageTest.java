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

package eu.dc4cities.controlsystem.modules.optionconsolidator.opcp;

import eu.dc4cities.controlsystem.modules.optionconsolidator.Utils;
import org.chocosolver.util.ESat;
import org.junit.Assert;
import org.junit.Ignore;

import java.util.Arrays;

/**
 * Test for {@link MinBrownEnergyUsage}.
 *
 *
 */
public class MinBrownEnergyUsageTest {

    @Ignore
    public void test() {
        int nbSlots = 10;
        OldPowerSource grid = new OldPowerSource("grid", Utils.makeSlots(Utils.makeProfile(nbSlots, 5000), new int[nbSlots]));
        int[] pvPurity = Utils.makeProfile(nbSlots, 100); //all green
        int[] pvProduction = new int[]{0, 100, 200, 300, 400, 500, 400, 300, 200, 100};
        OldPowerSource pv = new OldPowerSource("pv", Utils.makeSlots(pvProduction, pvPurity));

        SimpleActivity a1 = new SimpleActivity("a", "a1");
        SimpleActivity a2 = new SimpleActivity("a", "a2");

        a1.add(new WorkingMode(1, Utils.makeOptionSlots(Utils.makeLinearProfile(nbSlots, 0, 100), new int[nbSlots]), null));
        a1.add(new WorkingMode(2, Utils.makeOptionSlots(Utils.makeLinearProfile(nbSlots, 0, 200), new int[nbSlots]), null));
        a2.add(new WorkingMode(3, Utils.makeOptionSlots(Utils.makeLinearProfile(nbSlots, 0, 50), new int[nbSlots]), null));
        a2.add(new WorkingMode(4, Utils.makeOptionSlots(Utils.makeLinearProfile(nbSlots, 0, 100), new int[nbSlots]), null));

        System.out.println(a1);
        System.out.println(a2);
        System.out.println(pv);
        System.out.println(grid);
        //Each slot is 1 h
        // (1h) long so a 1Watt consumption = 1watt-hour per slot
        OPCP opcp = new OPCP(Utils.slots(nbSlots, 60 * 15), Arrays.asList(grid, pv), Arrays.asList(a1, a2));
        MinBrownEnergyUsage o = new MinBrownEnergyUsage();
        o.decorate(opcp);
        //opcp.setVerbosity(2);
        Assert.assertEquals(opcp.solve(-1), ESat.TRUE);
        System.out.println(Arrays.toString(opcp.getPowerUsage(grid)));
        System.out.println(Arrays.toString(opcp.getPowerUsage(pv)));

        //Check we burn all the green energies
        for (int i = 0; i < nbSlots; i++) {
            Assert.assertEquals(opcp.getPowerUsage(pv)[i].getValue(), pvProduction[i]);
        }
    }
}
