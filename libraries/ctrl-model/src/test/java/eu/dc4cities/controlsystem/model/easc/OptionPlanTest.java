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
 * Unit tests for {@link eu.dc4cities.controlsystem.model.easc.OptionPlan}.
 *
 *
 */
public class OptionPlanTest {

    @Test
    public void test() {
        OptionPlan op = new OptionPlan();
        Assert.assertFalse(op.toString().contains("null"));

        Assert.assertTrue(op.getWorkOptions().isEmpty());

        WorkOption w = new WorkOption();
        op.addWorkOption(w);

        WorkOption w2 = new WorkOption();
        op.addWorkOption(w2);
        Assert.assertEquals(op.getWorkOptions().size(), 2);

        Assert.assertTrue(op.getWorkOptions().contains(w));
        Assert.assertTrue(op.getWorkOptions().contains(w2));

        Assert.assertFalse(op.toString().contains("null"));

    }
}
