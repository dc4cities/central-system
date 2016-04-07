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
 * Created by fhermeni on 09/06/2015.
 */
public class RevenueTest {

    @Test
    public void testFlatPricing() {
        Revenue r = new Revenue(7, 5);
        r.add(new Modifier(12, 2).flat());
        r.add(new Modifier(10, 1).flat());
        r.add(new Modifier(5, -1).flat());
        r.add(new Modifier(3, -2).flat());
        Assert.assertEquals(7, r.revenue(13)); //over the last threshold to capped to the last modifier
        Assert.assertEquals(7, r.revenue(12));
        Assert.assertEquals(6, r.revenue(11));
        Assert.assertEquals(4, r.revenue(8));
        Assert.assertEquals(4, r.revenue(7));
        Assert.assertEquals(4, r.revenue(5));
        Assert.assertEquals(3, r.revenue(3));
        //Assert.assertEquals(3, r.revenue(1));
    }

    @Test
    public void testLinear() {
        /*
        * while the modifier associated to a threshold applies to all
        * performance levels between that threshold and the next (lower) threshold. The modifier for the last threshold in the
        * list applies to all lower performance levels.
         */
        /*Revenue r = new Revenue(7, 5);
        r.add(new Modifier(12, 2).linear());
        r.add(new Modifier(10, 1).linear());
        r.add(new Modifier(5, -1).linear());
        r.add(new Modifier(3, -2).linear());
        Assert.assertEquals(15, r.revenue(13)); //over the last threshold to capped to the last modifier
        Assert.assertEquals(15, r.revenue(12));
        Assert.assertEquals(9, r.revenue(11));
        Assert.assertEquals(5, r.revenue(8));
        Assert.assertEquals(5, r.revenue(7));
        Assert.assertEquals(3, r.revenue(5));
        Assert.assertEquals(-1, r.revenue(4));
        Assert.assertEquals(-3, r.revenue(3));*/
        Revenue r = new Revenue(830, 1000);
        r.add(new Modifier(850, 1).linear());
        r.add(new Modifier(820, 0));
        r.add(new Modifier(720, -100));
        r.add(new Modifier(0, -2).linear());
        Assert.assertEquals(1170, r.revenue(1000));
        Assert.assertEquals(1000, r.revenue(830));
        Assert.assertEquals(1000, r.revenue(849));
        Assert.assertEquals(900, r.revenue(720));
        Assert.assertEquals(340, r.revenue(500));
    }

    @Test
    public void testNoConcessions() {
        Revenue r = new Revenue(7, 5);
        r.add(new Modifier(10, 1).flat());
        Assert.assertEquals(6, r.revenue(12));
        Assert.assertEquals(5, r.revenue(7));
        Assert.assertEquals(5, r.revenue(5));
    }

    @Test
    public void testForObjective() {
        Revenue r = new Revenue(80, 0);
        r.add(new Modifier(80, 0));
        r.add(new Modifier(75, -1).linear());
        r.add(new Modifier(0, -1).linear());
        Assert.assertEquals(0, r.revenue(81));//over the max. No cost
        Assert.assertEquals(-3, r.revenue(77));
        Assert.assertEquals(-5, r.revenue(75));
        Assert.assertEquals(-6, r.revenue(74));
        Assert.assertEquals(-10, r.revenue(70));
    }

    @Test
    public void testTrento() {
        //40 630 -> perfs=[225, 1350] lb=420000; ub= 420000
        InstantRevenue r = new InstantRevenue(43, 630, 420000);
        r.add(new Modifier(630, 0).linear());
        r.add(new Modifier(0, -2000).linear());
        Assert.assertEquals(420000, r.revenue(1350));//over the max. No cost
        Assert.assertEquals(420000 - 810000, r.revenue(225));//over the max. No cost
    }
    @Test
    public void testHP() {
        /*
                instantBusinessObjective: !amount '1696 Req/s'
        basePrice: !amount '50 EUR'
        priceModifiers:
          - threshold: !amount '0 Req/s'
            modifier: !amount '-0.002 EUR/Req'
         */
        InstantRevenue r = new InstantRevenue(0, 1696, 50);
        r.add(new Modifier(0, -1).linear());
        System.out.println(r.revenue(1696));
        System.out.println(r.revenue(1697));
    }
}