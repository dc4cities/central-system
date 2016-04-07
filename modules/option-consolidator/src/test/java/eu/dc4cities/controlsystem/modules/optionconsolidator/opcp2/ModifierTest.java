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

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by fhermeni on 19/06/2015.
 */
public class ModifierTest {

    @Test
    public void testFlatDiff() {
        Modifier m = new Modifier(30, 1000).flat();
        Assert.assertEquals(1, m.diff(100, 20));
    }

    @Test
    public void testLinearDiff() {
        Modifier m = new Modifier(30, 1000).linear();
        Assert.assertEquals(46, m.diff(70, 24));

        m.linear(10);
        Assert.assertEquals(5, m.diff(70, 20));
    }
}