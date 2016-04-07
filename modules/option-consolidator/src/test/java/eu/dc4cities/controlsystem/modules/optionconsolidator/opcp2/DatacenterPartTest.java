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

package eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2;

import eu.dc4cities.controlsystem.modules.optionconsolidator.Utils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by fhermeni on 09/06/2015.
 */
public class DatacenterPartTest {

    @Test
    public void testTransitionCosts() {
        DatacenterPart p = new DatacenterPart("dc1");
        WM s = Utils.WM("S", 3);
        WM m = Utils.WM("M", 6);
        WM f = Utils.WM("F", 10);
        p.add(s).add(m).add(f);
        //0 costs by defaults
        for (WM from : p.getWorkingModes()) {
            for (WM to : p.getWorkingModes()) {
                Assert.assertEquals(0, p.getTransitionCost(from, to));
            }
        }
        p.setTransitionCost(s, m, 5);
        p.setTransitionCost(m, s, 3);
        for (WM from : p.getWorkingModes()) {
            for (WM to : p.getWorkingModes()) {
                if (from == s && to == m) {
                    Assert.assertEquals(5, p.getTransitionCost(from, to));
                } else if (from == m && to == s) {
                    Assert.assertEquals(3, p.getTransitionCost(from, to));
                } else {
                    Assert.assertEquals(0, p.getTransitionCost(from, to));
                }
            }
        }
    }
}