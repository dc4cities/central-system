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
 * Unit tests for {@link eu.dc4cities.controlsystem.model.easc.Activity}.
 *
 *
 */
public class ActivityTest {

    @Test
    public void tesGetAndSet() {
        Activity a = new Activity("e1");
        Assert.assertEquals(a.getName(), "e1");
        Assert.assertFalse(a.toString().contains("null"));

        Assert.assertTrue(a.getWorks().isEmpty());
        Work w = new Work(0, 1, "wm", Amount.valueOf(2, WATT), 3);

        a.addWork(w);
        Assert.assertTrue(a.getWorks().contains(w));

        w = new Work(0, 1, "wm2", Amount.valueOf(2, WATT), 3);
        a.addWork(w);
        Assert.assertTrue(a.getWorks().contains(w));

        Assert.assertEquals(a.getWorks().size(), 2);
    }
}
