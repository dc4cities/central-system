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
 * Unit tests for {@link eu.dc4cities.controlsystem.model.easc.WorkOption}.
 *
 *
 */
public class WorkOptionTest {

    @Test
    public void test() {
        WorkOption wo = new WorkOption();
        wo.setStartTimeSlot(0);
        wo.setEndTimeSlot(5);
        Assert.assertFalse(wo.toString().contains("null"));
        Assert.assertTrue(wo.getModeOptions().isEmpty());

        ModeOption mo1 = new ModeOption("wm", Amount.valueOf(3, WATT), 10);
        ModeOption mo2 = new ModeOption("wm2", Amount.valueOf(5, WATT), 7);
        wo.addModeOption(mo1);
        wo.addModeOption(mo2);
        Assert.assertEquals(wo.getModeOptions().size(), 2);
        Assert.assertTrue(wo.getModeOptions().contains(mo1));
        Assert.assertTrue(wo.getModeOptions().contains(mo2));
        Assert.assertEquals(wo.getStartTimeSlot(), 0);
        Assert.assertEquals(wo.getEndTimeSlot(), 5);

        Assert.assertFalse(wo.toString().contains("null"));
    }
}
