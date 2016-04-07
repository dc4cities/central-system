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
 * Unit tests for {@link eu.dc4cities.controlsystem.model.easc.EascActivityPlan}.
 *
 *
 */
public class ActivityPlanTest {

    @Test
    public void test() {
        EascActivityPlan a = new EascActivityPlan("e1");
        Assert.assertEquals(a.getEascName(), "e1");
        Assert.assertTrue(a.getActivities().isEmpty());
        Assert.assertFalse(a.toString().contains("null"));

        Activity o1 = new Activity("a1");
        Activity o2 = new Activity("a2");

        a.addActivity(o1);
        a.addActivity(o2);

        Assert.assertEquals(a.getActivities().size(), 2);
        Assert.assertTrue(a.getActivities().contains(o1));
        Assert.assertTrue(a.getActivities().contains(o2));
        Assert.assertFalse(a.toString().contains("null"));
    }
}
