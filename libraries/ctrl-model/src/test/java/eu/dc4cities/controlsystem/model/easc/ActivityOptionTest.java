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
 * Unit tests for {@link eu.dc4cities.controlsystem.model.easc.ActivityOption}.
 *
 *
 */
public class ActivityOptionTest {

    @Test
    public void testGetAndSet() {
        ActivityOption a = new ActivityOption("foo");
        Assert.assertEquals("foo", a.getActivity());
        Assert.assertTrue(a.getOptionPlans().isEmpty());
        Assert.assertFalse(a.toString().contains("null"));

        OptionPlan p1 = new OptionPlan();
        OptionPlan p2 = new OptionPlan();

        a.addOptionPlan(p1);
        a.addOptionPlan(p2);

        Assert.assertTrue(a.getOptionPlans().contains(p1));
        Assert.assertTrue(a.getOptionPlans().contains(p2));
        Assert.assertEquals(a.getOptionPlans().size(), 2);
        Assert.assertFalse(a.toString().contains("null"));
    }
}
