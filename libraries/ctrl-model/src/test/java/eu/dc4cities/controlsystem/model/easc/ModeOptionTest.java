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

import org.jscience.physics.amount.Amount;
import org.junit.Assert;
import org.junit.Test;

import static javax.measure.unit.SI.WATT;

/**
 * Unit tests for {@link eu.dc4cities.controlsystem.model.easc.ModeOption}.
 *
 *
 */
public class ModeOptionTest {

    @Test
    public void testGetAndSet() {
        ModeOption o = new ModeOption("wm", Amount.valueOf(3, WATT), 12);
        Assert.assertFalse(o.toString().contains("null"));

        Assert.assertEquals(o.getPower(), Amount.valueOf(3, WATT));
        Assert.assertTrue(o.setPower(Amount.valueOf(7, WATT)));
        Assert.assertEquals(o.getPower(), Amount.valueOf(7, WATT));

        Assert.assertEquals(o.getGreenPoints(), 12);
        o.setGreenPoints(18);
        Assert.assertEquals(o.getGreenPoints(), 18);

        Assert.assertEquals(o.getWorkingMode(), "wm");
    }
    
}
