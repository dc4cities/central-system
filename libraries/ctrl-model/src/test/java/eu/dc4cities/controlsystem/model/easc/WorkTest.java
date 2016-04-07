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
 * Unit tests for {@link Work}.
 *
 *
 */
public class WorkTest {

    @Test
    public void testGetAndSet() {
        Work w = new Work(1, 2, "wm", Amount.valueOf(12, WATT), 7);
        Assert.assertFalse(w.toString().contains("null"));

        Assert.assertEquals(w.getWorkingMode(), "wm");

        Assert.assertEquals(w.getStartTimeSlot(), 1);
        Assert.assertEquals(w.getEndTimeSlot(), 2);

        Assert.assertEquals(w.getGreenPoints(), 7);
        w.setGreenPoints(17);
        Assert.assertEquals(w.getGreenPoints(), 17);

        //Commented because classes have been converted to beans
//        Assert.assertEquals(w.getPower().getExactValue(), 12);
//        w.setPower(Amount.valueOf(3, WATT));
//        Assert.assertEquals(w.getPower().getExactValue(), 3);
//
//        //Setters for the time
//        Assert.assertFalse(w.setStartAt(ed));
//        Assert.assertEquals(w.getStartAt(), st);
//
//        Assert.assertFalse(w.setEndAt(st));
//        Assert.assertEquals(w.getEndAt(), ed);
//
//        Date d3 = new Date(ed.getTime() + 1);
//        Assert.assertTrue(w.setEndAt(d3));
//        Assert.assertEquals(w.getEndAt(), d3);
//
//        Assert.assertTrue(w.setStartAt(ed));
//        Assert.assertEquals(w.getStartAt(), ed);

    }
}
