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

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link eu.dc4cities.controlsystem.model.easc.EascOptionPlan}.
 *
 *
 */
public class ActivityOptionPlanTest {

    @Test
    public void test() {
        EascOptionPlan a = new EascOptionPlan("e1");
        Assert.assertEquals(a.getEascName(), "e1");
        Assert.assertTrue(a.getActivityOptions().isEmpty());
        Assert.assertFalse(a.toString().contains("null"));

        ActivityOption o1 = new ActivityOption("foo");
        ActivityOption o2 = new ActivityOption("bar");

        a.addActivityOption(o1);
        a.addActivityOption(o2);

        Assert.assertEquals(a.getActivityOptions().size(), 2);
        Assert.assertTrue(a.getActivityOptions().contains(o1));
        Assert.assertTrue(a.getActivityOptions().contains(o2));
        Assert.assertFalse(a.toString().contains("null"));
    }
}
