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

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.util.ESat;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

import static eu.dc4cities.controlsystem.modules.optionconsolidator.Utils.WM;

/**
 * Created by fhermeni on 11/06/2015.
 */
public class RenPctTest {

    @Ignore
    public void testFlat() {
        PowerSource grid = new PowerSource("grid", Utils.makeSlots(new int[]{50, 50, 50}, new int[]{50, 50, 50})).dcId("dc1");
        RenPct pct = new RenPct("dc1", 0, 3, new Penalty(80, -100));
        WM f = WM("F", 30, 30);
        WM f2 = WM("F", 80, 80);
        DatacenterPart p = new DatacenterPart("dc1").add(f).add(f2);
        MyActivity a = new MyActivity("a").add(p).add(new CumulativeRevenue(0, 3, 90, 1000));
        OPCP2 pb = new OPCP2(3, Arrays.asList(grid), Arrays.asList(a));
        pb.add(pct);
        Assert.assertEquals(ESat.TRUE, pb.solve());
        Assert.assertEquals(500, pb.val(pct.percentage()));
        //Assert.assertEquals(-100, pb.val(pct.penaltyVariable()));
        Assert.assertEquals(900, pb.val(pb.getProfit()));

        pct = new RenPct("dc1", 0, 3, new Penalty(40, -100).add(new Modifier(40, 1).linear()));
        pb = new OPCP2(3, Arrays.asList(grid), Arrays.asList(a));
        pb.add(pct);
        Assert.assertEquals(ESat.TRUE, pb.solve());
        Assert.assertEquals(50, pb.val(pct.percentage()));
        Assert.assertEquals(10, pb.val(pct.penaltyVariable()));
        Assert.assertEquals(1010, pb.val(pb.getProfit()));

    }

    @Test
    public void test() throws ContradictionException {
        int slots = 3;

        PowerSource grid = new PowerSource("grid", Utils.makeSlots(new int[]{2, 4, 6}, new int[]{0, 20, 50})).dcId("dc1");
        WM s = WM("S", 2, 2);
        WM m = WM("M", 4, 4);
        WM f = WM("F", 6, 6);
        DatacenterPart p = new DatacenterPart("dc1").add(s).add(m).add(f);
        MyActivity a = new MyActivity("a").add(p).add(new CumulativeRevenue(0, 3, 12, 10000));

        RenPct pct = new RenPct("dc1", 0, 1, new Penalty(80, -100));

        OPCP2 pb= new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        pb.add(pct);
        Assert.assertEquals(ESat.TRUE, pb.solve());

        //pb.add(pct);
        /*Assert.assertEquals(ESat.TRUE, pb.solve());
        System.out.println(pct.percentage());
        Assert.assertEquals(-100, pct.penaltyVariable().getValue());*/

        pct = new RenPct("dc1", 0, 3, new Penalty(30, -100));
        pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        //pb.add(pct);
        /*Assert.assertEquals(ESat.TRUE, pb.solve());
        Assert.assertEquals(31, pct.percentage().getValue());
        Assert.assertEquals(0, pct.penaltyVariable().getValue());*/

        Penalty pen = new Penalty(80, -100);
        pen.add(new Modifier(70, 0).linear()).add(new Modifier(0, -1).linear());
        pct = new RenPct("dc1", 0, 3, pen);
        pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        pb.add(pct);
        Assert.assertEquals(ESat.TRUE, pb.solve());
        Assert.assertEquals(316, pb.val(pct.percentage()));
//        Assert.assertEquals(-49, pb.val(pct.penaltyVariable()));
    }

    @Test
    public void testInThePast() throws ContradictionException {
        PowerSource past = new PowerSource("grid", Utils.makeSlots(new int[]{2, 4}, new int[]{0, 20})).dcId("dc1");
        PowerSource next = new PowerSource("grid", Utils.makeSlots(new int[]{6}, new int[]{50})).dcId("dc1");

        //System.out.println(past);
        WM s = WM("S", 2, 2);
        WM m = WM("M", 4, 4);
        WM f = WM("F", 6, 6);
        DatacenterPart p = new DatacenterPart("dc1").add(s).add(m).add(f);
        CumulativeRevenue cr = new CumulativeRevenue(0, 1, 6, 10000);
        MyActivity a = new MyActivity("a").add(p).add(cr); //no enough power

        RenPct pct = new RenPct("dc1", Arrays.asList(past), 0, 1, new Penalty(80, -100));

        OPCP2 pb = new OPCP2(1, Arrays.asList(next), Arrays.asList(a));
        pb.add(pct);
        Assert.assertEquals(ESat.TRUE, pb.solve());
        //System.err.println(pb.getSolver().toString());
        Assert.assertEquals(6, pb.val(pb.perf(0, cr)));
//        Assert.assertEquals(100, pb.val(pct.penaltyVariable()));
        Assert.assertEquals(316, pb.val(pct.percentage()));
    }

}