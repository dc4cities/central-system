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

import eu.dc4cities.controlsystem.model.easc.Relocability;
import eu.dc4cities.controlsystem.modules.ConsolidatorException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.Utils.WM;
import static eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.Utils.*;

/**
 * Unit tests for {@link OPCP2}.
 *
 */
public class OPCP2Test {

    @Test
    public void testTransitionCosts() {
        int slots = 3;
        //energy: 1 to 3
        PowerSource grid = new PowerSource("grid", makeSlots(makeLinearProfile(slots, 1, 1), makeProfile(slots, 1))).dcId("dc1");
        WM s = WM("S", 2, 1);
        WM m = WM("M", 4, 2);
        WM f = WM("F", 6, 3);
        MyActivity a = new MyActivity("a").add(new CumulativeRevenue(0, 3, 12, 1000)); //so the workflow will be S,M, F (from the power availability)
        DatacenterPart p = new DatacenterPart("dc1").add(s).add(m).add(f);
        a.add(p);
        OPCP2 pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve()); //Single solution
        Assert.assertEquals(1000, pb.val(pb.getProfit()));

        p.setTransitionCost(s, m, 1);
        pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve());//cannot meet the SLO
        Assert.assertEquals(0, pb.val(pb.getProfit()));
        a.add(new CumulativeRevenue(0, 3, 11, 1000));
        pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve());
        Assert.assertEquals(1000, pb.val(pb.getProfit()));
    }

    @Test
    public void testCumulativeSLO() {
        int slots = 10;
        CumulativeRevenue trento = new CumulativeRevenue(0, slots, 150, 500);
        trento.add(new Modifier(100, 0)).add(new Modifier(50, -1)).add(new Modifier(0, -2));

        PowerSource grid = new PowerSource("grid", makeSlots(makeLinearProfile(slots, 400, 25), makeProfile(slots, 1))).dcId("dc1");
        WM s = WM("WM1", 17, 370);
        WM m = WM("WM2", 25, 440);
        MyActivity a = new MyActivity("a");
        DatacenterPart p = new DatacenterPart("dc1").add(s).add(m);
        a.add(p);
        a.add(trento);
        OPCP2 pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve());
        Assert.assertEquals(500, pb.val(pb.getProfit()));
    }

    @Test
    public void testPartialCumulativeSLO() {
        int slots = 10;

        PowerSource grid = new PowerSource("grid", makeSlots(makeLinearProfile(slots, 6, 2), makeProfile(slots, 1))).dcId("dc1");
        //System.out.println(grid);
        WM s = WM("WM1", 4, 6);
        WM m = WM("WM2", 10, 10);
        MyActivity a = new MyActivity("a");
        DatacenterPart p = new DatacenterPart("dc1").add(s).add(m);
        a.add(p);

        CumulativeRevenue trento = new CumulativeRevenue(3, 6, 20, 500);
        a.add(trento);
        OPCP2 pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve());
        Assert.assertEquals(500, pb.val(pb.getProfit()));
    }

    /**
     * A cumulative on a sub window
     */
    @Test
    public void testCumulativeExceed() {
        int slots = 3;
        PowerSource grid = new PowerSource("grid", makeSlots(makeLinearProfile(slots, 1, 1), makeProfile(slots, 1))).dcId("dc1");
        //System.out.println(grid);
        WM s = WM("WM1", 1, 1);
        WM m = WM("WM2", 3, 3);
        MyActivity a = new MyActivity("a");
        DatacenterPart p = new DatacenterPart("dc1").add(s).add(m);
        a.add(p);

        CumulativeRevenue trento = new CumulativeRevenue(1, 2, 1, 500);
        a.add(trento);
        OPCP2 pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve());
        Assert.assertEquals(500, pb.val(pb.getProfit()));
    }

    @Test
    public void testOverlappingCumulativeSLO() {
        int slots = 6;

        PowerSource grid = new PowerSource("grid", makeSlots(makeLinearProfile(slots, 400, 25), makeProfile(slots, 1))).dcId("dc1");
        WM s = WM("WM1", 17, 370);
        WM m = WM("WM2", 25, 440);
        MyActivity a = new MyActivity("a");
        DatacenterPart p = new DatacenterPart("dc1").add(s).add(m);
        a.add(p);

        CumulativeRevenue trento = new CumulativeRevenue(0, 3, 100, 500);
        a.add(trento);
        OPCP2 pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve());
        Assert.assertEquals(0, pb.val(pb.getProfit()));

        trento.base(100); //little help from the past
        pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve());
        Assert.assertEquals(500, pb.val(pb.getProfit()));
    }

    @Test
    public void testInstantSLO() {
        int slots = 4;
        MyActivity a = new MyActivity("a");
        PowerSource grid = new PowerSource("grid", makeSlots(new int[]{5, 10, 5, 10}, new int[]{1, 1, 1, 1})).dcId("dc1");
        WM s = WM("S", 5, 5);
        WM m = WM("M", 10, 10);
        DatacenterPart p = new DatacenterPart("dc1").add(s).add(m);
        p.setTransitionCost(s, m, 1);
        a.add(p);

        InstantRevenue p1 = new InstantRevenue(0, 10, 7); //no way (power)
        InstantRevenue p2 = new InstantRevenue(1, 5, 2);  //ok for S or M
        InstantRevenue p3 = new InstantRevenue(2, 10, 8); //no way (power)
        InstantRevenue p4 = new InstantRevenue(3, 10, 14); //no way (transition cost prevents M)
        a.add(p1).add(p2).add(p4).add(p3);

        OPCP2 pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve()); //Single solution
        System.out.println(Arrays.toString(pb.instantPerf(0)));
        Assert.assertArrayEquals(new int[]{5, 5, 5, 5}, pb.instantPerf(0));

        Assert.assertEquals(2, pb.val(pb.instantRevenue(0)));
        /*Assert.assertEquals(0, pb.instantRevenues(0, p1).getValue());
        Assert.assertEquals(10, pb.revenue(0, p2).getValue());
        Assert.assertEquals(10, pb.revenue(0, p3).getValue());
        Assert.assertEquals(10, pb.revenue(0, p4).getValue());*/
    }

    @Test
    public void testSuspicious1() {
        int slots = 2;
        //energy: 1 to 2
        PowerSource grid = new PowerSource("grid", makeSlots(makeLinearProfile(slots, 1, 1), makeProfile(slots, 1))).dcId("dc1");
        WM s = WM("S", 2, 1);
        WM m = WM("M", 4, 2);
        WM f = WM("F", 6, 3);
        MyActivity a = new MyActivity("a");
        DatacenterPart p = new DatacenterPart("dc1").add(s).add(m).add(f);
        a.add(p);

        p.setTransitionCost(s, m, 1);
        p.setTransitionCost(m, f, 3);
        a.add(new CumulativeRevenue(0, 2, 5, 100000)); //s,m,m
        OPCP2 pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        pb.solve();
        Assert.assertEquals(ESat.TRUE, pb.solve());
    }

    @Test
    public void testPowerUsage() {
        int slots = 3;
        PowerSource grid = new PowerSource("grid", makeSlots(makeLinearProfile(slots, 2, 2), makeProfile(slots, 1))).dcId("dc1");
        WM s = WM("S", 2, 2);
        WM m = WM("M", 4, 4);
        WM f = WM("F", 6, 6);
        MyActivity a = new MyActivity("a").add(new CumulativeRevenue(0, 3, 12, 100000));
        DatacenterPart p = new DatacenterPart("dc1").add(s).add(m).add(f);
        a.add(p);
        OPCP2 pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve());
        for (int i = 0; i< slots; i++) {
            Assert.assertEquals(pb.val(pb.getActivitiesPowerUsage()[0][i][0]), pb.val(pb.getSourcePowerUsage()[0][i]));
            Assert.assertEquals("at time " + i, (i + 1) * 2, pb.val(pb.getActivitiesPowerUsage()[0][i][0]));
        }
    }

    @Test
    public void testSuspicious2() {
        int slots = 3;
        //energy: 1 to 3
        PowerSource grid = new PowerSource("grid", makeSlots(makeLinearProfile(slots, 1, 1), makeProfile(slots, 1))).dcId("dc1");
        WM s = WM("S", 2, 1);
        WM m = WM("M", 4, 2);
        WM f = WM("F", 6, 3);
        MyActivity a = new MyActivity("a").add(new CumulativeRevenue(0, slots, 12, 100)); //so the workflow will be S,M, F (from the power availability)
        DatacenterPart p = new DatacenterPart("dc1").add(s).add(m).add(f);
        a.add(p);
        p.setTransitionCost(s, m, 1);
        OPCP2 pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve());
        Assert.assertEquals(0, pb.val(pb.getProfit())); //the SLO cannot be satisfied. No profit
    }

    @Test
    public void testPrice() {
        int slots = 3;
        //energy: 1 to 3
        PowerSource grid = new PowerSource("grid", makeSlots(makeProfile(slots, 10), makeProfile(slots, 1), makeLinearProfile(slots, 5, 5))).dcId("dc1");
        ;
        WM s = WM("S", 2, 1);
        WM m = WM("M", 4, 2);
        WM f = WM("F", 6, 3);
        MyActivity a = new MyActivity("a").add(new CumulativeRevenue(0, 3, 12, 10000)); //so the workflow will be S,M, F (from the power availability)
        DatacenterPart p = new DatacenterPart("dc1").add(s).add(m).add(f);
        a.add(p);
        OPCP2 pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve()); //no solution due to the transition cost (max perf of 11)
        for (int i = 0; i < slots; i++) {
            int power = pb.val(pb.getSourcePowerUsage()[0][i]);
            int pricePerWatt = grid.slots()[i].price();
            Assert.assertEquals(power * pricePerWatt, pb.val(pb.getAllEnergyPrice()[0][i]));
            //System.err.println(power + "x" + pricePerWatt + "=" + pb.getAllEnergyPrice()[0][i].getValue());
        }
    }

    @Test(expected = ConsolidatorException.class)
    public void testUnfeasibleEnergyQuota() {
        int slots = 5;
        //energy: 1 to 3
        PowerSource grid = new PowerSource("grid", makeSlots(makeProfile(slots, 10), makeProfile(slots, 1), makeProfile(slots, 1))).dcId("dc1");
        WM s = WM("S", 1, 1);
        MyActivity a = new MyActivity("a").add(new CumulativeRevenue(0, slots, 14, 10000));
        DatacenterPart p = new DatacenterPart("dc1").add(s);
        a.add(p);
        OPCP2 pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        pb.setEnergyQuota(0, slots, new int[]{0}, "dc1", 3);
    }

    @Test
    public void testEnergyQuota() {
        int slots = 5;
        //energy: 1 to 3
        PowerSource grid = new PowerSource("grid", makeSlots(makeProfile(slots, 10), makeProfile(slots, 1), makeProfile(slots, 1))).dcId("dc1");
        WM s = WM("S", 1, 1);
        WM m = WM("M", 2, 4);
        WM f = WM("F", 4, 8);
        MyActivity a = new MyActivity("a").add(new CumulativeRevenue(0, slots, 14, 10000));
        DatacenterPart p = new DatacenterPart("dc1").add(s).add(m).add(f);
        a.add(p);
        OPCP2 pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        pb.setEnergyQuota(0, slots, new int[]{0}, "dc1", 12);
        Assert.assertEquals(ESat.TRUE, pb.solve());
        Assert.assertEquals(-5, pb.val(pb.getProfit())); //no enough energy to satisfy the SLO. 0 revenues, cost of 5 (power)

        pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        pb.setEnergyQuota(0, slots, new int[]{0}, "dc1", 28); //mode FFFSS
        Assert.assertEquals(ESat.TRUE, pb.solve()); //no solution due to the transition cost (max perf of 11)
        Assert.assertEquals(10000 - 26, pb.val(pb.getProfit())); //no enough energy to satisfy the SLO
        //Assert.assertEquals(26, pb.val(usage));
    }

    @Test
    public void testMultipleSources() {
        int slots = 3;
        //energy: 1 to 3 per source, so 2 to 6
        PowerSource g1 = new PowerSource("g1", makeSlots(makeLinearProfile(slots, 1, 1), makeProfile(slots, 1))).dcId("dc1");
        PowerSource g2 = new PowerSource("g2", makeSlots(makeLinearProfile(slots, 1, 1), makeProfile(slots, 1))).dcId("dc1");
        WM s = WM("S", 2, 2);
        WM m = WM("M", 4, 4);
        WM f = WM("F", 6, 6);
        MyActivity a = new MyActivity("a").add(new CumulativeRevenue(0, 3, 12, 100000));
        DatacenterPart p = new DatacenterPart("dc1").add(s).add(m).add(f);
        a.add(p);
        OPCP2 pb = new OPCP2(slots, Arrays.asList(g1, g2), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve()); //Single solution
        for (int t = 0; t < slots; t++) {
            int w = (t+1) * 2;
            Assert.assertEquals(w, pb.val(pb.getSourcePowerUsage()[0][t]) + pb.val(pb.getSourcePowerUsage()[1][t]));
            Assert.assertEquals(w, pb.val(pb.getActivitiesPowerUsage()[0][t][0]));
//            Assert.assertEquals(w, pb.val(pb.getGlobalPowerUsage()[t]));
        }
    }

    @Test(expected = ConsolidatorException.class)
    public void testUnfeasiblePowerQuota() {
        int slots = 3;
        PowerSource g1 = new PowerSource("g1", makeSlots(makeProfile(slots, 1), makeProfile(slots, 1))).dcId("dc1");
        PowerSource g2 = new PowerSource("g2", makeSlots(makeProfile(slots, 1), makeProfile(slots, 1))).dcId("dc1");
        //energy: 2 to 6
        WM s = WM("S", 2, 3);
        WM m = WM("M", 4, 4);
        WM f = WM("F", 6, 6);
        MyActivity a = new MyActivity("a").add(new CumulativeRevenue(0, 3, 10, 1000));
        DatacenterPart p = new DatacenterPart("dc1").add(s).add(m).add(f);
        a.add(p);
        OPCP2 pb = new OPCP2(slots, Arrays.asList(g1, g2), Arrays.asList(a));
        pb.setPowerQuota(2, new int[]{0}, "dc1", 4);
    }

    @Test
    public void testPowerQuota() {
        int slots = 3;
        PowerSource g1 = new PowerSource("g1", makeSlots(makeLinearProfile(slots, 1, 1), makeProfile(slots, 1))).dcId("dc1");
        PowerSource g2 = new PowerSource("g2", makeSlots(makeLinearProfile(slots, 1, 1), makeProfile(slots, 1))).dcId("dc1");
        //energy: 2 to 6
        WM s = WM("S", 2, 2);
        WM m = WM("M", 4, 4);
        WM f = WM("F", 6, 6);
        MyActivity a = new MyActivity("a").add(new CumulativeRevenue(0, 3, 10, 1000)); //S,M,F against price
        DatacenterPart p = new DatacenterPart("dc1").add(s).add(m).add(f);
        a.add(p);
        OPCP2 pb = new OPCP2(slots, Arrays.asList(g1, g2), Arrays.asList(a));
        pb.setPowerQuota(2, new int[]{0}, "dc1", 4); //up to 4 watts for t=2. Mode F not longer possible. Cannot met the SLO

        Logger.getRootLogger().removeAllAppenders();
        ConsoleAppender x = new ConsoleAppender();
        x.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
        x.setThreshold(Level.DEBUG);
        x.activateOptions();
        Logger.getRootLogger().addAppender(x);
        Logger.getLogger(OPCP2.class).setLevel(Level.TRACE);
        Assert.assertEquals(ESat.TRUE, pb.solve());
        System.out.println(pb.getSolver());
        Assert.assertEquals(1000, pb.val(pb.getProfit())); //SLO is met

        Assert.assertTrue(pb.val(pb.getActivitiesPowerUsage()[0][2][0]) <= 4); //dispatch
        Assert.assertTrue(pb.val(pb.getSourcePowerUsage()[0][2]) + pb.val(pb.getSourcePowerUsage()[1][2]) <= 4); //dispatch
        for (int t = 0; t < slots; t++) {
            int w = pb.val(pb.getActivitiesPowerUsage()[0][t][0]);
            //Assert.assertEquals("at " + t, w, pb.getGlobalPowerUsage()[t].getValue());
            Assert.assertEquals("at " + t, w, pb.val(pb.getSourcePowerUsage()[0][t]) + pb.val(pb.getSourcePowerUsage()[1][t])); //dispatch
        }
        //Assert.assertEquals(4, pb.val(pwr));
    }

    @Test
    public void testRevenueNoEMA() {
        int slots = 3;
        //energy: 1 to 3
        PowerSource g1 = new PowerSource("g1", makeSlots(new int[]{2, 4, 6}, new int[]{50, 25, 50}, new int[]{1, 2, 3})).dcId("dc1");
        WM s = WM("S", 2, 2);
        WM m = WM("M", 4, 4);
        WM f = WM("F", 6, 6);
        CumulativeRevenue pricing = new CumulativeRevenue(0, 3, 12, 30);
        MyActivity a = new MyActivity("a").add(pricing);
        DatacenterPart p = new DatacenterPart("dc1").add(s).add(m).add(f);
        a.add(p);
        OPCP2 pb = new OPCP2(slots, Arrays.asList(g1), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve());
        int eCost = 2 + 8 + 18;
        int aRevenue = pricing.basePrice();
        Assert.assertEquals(aRevenue - eCost, pb.val(pb.getProfit()));
    }

    @Test
    public void testRevenueWithEMA() {
        int slots = 3;
        PowerSource g1 = new PowerSource("g1",
                makeSlots(new int[]{2, 4, 6},  //Peak power
                          new int[]{50, 25, 50}, //ren pct
                        new int[]{0, 0, 0}) //price
        ).dcId("dc1");
        CumulativeRevenue pricing = new CumulativeRevenue(0, 3, 12, 30); //Cumulative performance of 12 at 30€
        MyActivity a = new MyActivity("a").add(pricing);

        // 3 working modes, name; performance; power
        WM s = WM("S", 2, 2);
        WM m = WM("M", 4, 4);
        WM f = WM("F", 6, 6);

        //1 Datacenter
        a.add(new DatacenterPart("dc1").add(s).add(m).add(f));

        OPCP2 pb = new OPCP2(slots, Arrays.asList(g1), Arrays.asList(a));
        RenPct r = new RenPct("dc1", 0, 3, new Penalty(50, -1));
        pb.add(r);
        Assert.assertEquals(ESat.TRUE, pb.solve());
        int aRevenue = pricing.basePrice();
        System.out.println(r.percentage());
        Assert.assertEquals(416, pb.val(r.percentage()));
        Assert.assertEquals(0, pb.val(r.penaltyVariable()));
//        Assert.assertEquals(29 /*easc income */ - 1 /* renPct penalty */, pb.val(pb.getProfit()));

    }

    @Test(expected = ConsolidatorException.class)
    public void testUnschedulability() throws ConsolidatorException {
        int slots = 2;
        PowerSource g1 = new PowerSource("g1", makeSlots(makeLinearProfile(slots, 9, 2), makeProfile(slots, 1))).dcId("dc1");
        PowerSource g2 = new PowerSource("g2", makeSlots(makeLinearProfile(slots, 9, 2), makeProfile(slots, 1))).dcId("dc2");
        PowerSource g3 = new PowerSource("g3", makeSlots(makeLinearProfile(slots, 9, 2), makeProfile(slots, 1))).dcId("dc2");

        CumulativeRevenue pricing = new CumulativeRevenue(0, slots, 1, 20000); //Cumulative performance of 12 at 30€
        MyActivity a = new MyActivity("a").add(pricing).relocatibility(Relocability.SPREADABLE);

        //At least 9 watts. Schedulable on dc1
        WM s1 = WM("S", 3, 9);
        WM m1 = WM("M", 20, 11);

        //At least 20 watts. Not schedulable on dc2 (min=18)
        WM s2 = WM("S", 3, 20);

        a.add(new DatacenterPart("dc1").add(s1).add(m1))
                .add(new DatacenterPart("dc2").add(s2));

        OPCP2 pb = new OPCP2(slots, Arrays.asList(g1, g2, g3), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve());
    }

    @Test
    public void testSimpleDay() {
        int slots = 96;
        PowerSource g1 = new PowerSource("g1", makeSlots(makeLinearProfile(slots, 20, 1), makeProfile(slots, 1))).dcId("dc1");

        CumulativeRevenue pricing = new CumulativeRevenue(0, slots, 2000, 20000); //Cumulative performance of 12 at 30€
        MyActivity a = new MyActivity("a").add(pricing);

        // 3 working modes, name; performance; power
        WM s = WM("S", 10, 20);
        WM m = WM("M", 20, 30);
        WM f = WM("F", 30, 50);

        //1 Datacenter
        a.add(new DatacenterPart("dc1").add(s).add(m).add(f));

        OPCP2 pb = new OPCP2(slots, Arrays.asList(g1), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve());
    }

    @Test
    public void testMultipleDC() {
        int slots = 2;
        PowerSource g1 = new PowerSource("g1", makeSlots(makeLinearProfile(slots, 1, 1), makeProfile(slots, 1))).dcId("dc1");
        PowerSource g2 = new PowerSource("g2", makeSlots(makeLinearProfile(slots, 1, 1), makeProfile(slots, 1))).dcId("dc2");
        PowerSource g3 = new PowerSource("g3", makeSlots(makeLinearProfile(slots, 1, 1), makeProfile(slots, 1))).dcId("dc2");

        CumulativeRevenue pricing = new CumulativeRevenue(0, slots, 6, 3000); //Cumulative performance of 12 at 30€
        MyActivity a = new MyActivity("a").add(pricing).relocatibility(Relocability.SPREADABLE);

        // 3 working modes, name; performance; power
        WM s1 = WM("S", 1, 1);
        WM m1 = WM("M", 2, 2);

        WM s2 = WM("S", 1, 2);
        WM m2 = WM("M", 2, 3);

        //2 Datacenters
        a.add(new DatacenterPart("dc1").add(s1).add(m1));
        a.add(new DatacenterPart("dc2").add(s2).add(m2));

        OPCP2 pb = new OPCP2(slots, Arrays.asList(g1, g2, g3), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve());

        Assert.assertEquals(3000, pb.val(pb.getProfit()));
        Assert.assertEquals(1, pb.val(pb.getActivitiesPowerUsage()[0][0][pb.dc("dc1")])); //a, t, d
        Assert.assertEquals(2, pb.val(pb.getActivitiesPowerUsage()[0][0][pb.dc("dc2")]));
        Assert.assertEquals(2, pb.val(pb.getActivitiesPowerUsage()[0][1][pb.dc("dc1")]));
        Assert.assertEquals(3, pb.val(pb.getActivitiesPowerUsage()[0][1][pb.dc("dc2")]));

        Assert.assertEquals(1, pb.val(pb.getSourcePowerUsage()[0][0])); //g1@0
        Assert.assertEquals(2, pb.val(pb.getSourcePowerUsage()[0][1])); //g1@1

        Assert.assertEquals(2, pb.val(pb.getSourcePowerUsage()[1][0]) + pb.val(pb.getSourcePowerUsage()[2][0])); //g2@0 + g3@0
        Assert.assertEquals(3, pb.val(pb.getSourcePowerUsage()[1][1]) + pb.val(pb.getSourcePowerUsage()[2][1])); //g2@1 + g2@2
    }

    @Test
    public void testFixActivity2Dcs() {
        int slots = 3;
        PowerSource g1 = new PowerSource("g1", makeSlots(makeLinearProfile(slots, 1, 1), makeProfile(slots, 1))).dcId("dc1");
        PowerSource g2 = new PowerSource("g2", makeSlots(makeLinearProfile(slots, 1, 1), makeProfile(slots, 1))).dcId("dc2");

        CumulativeRevenue pricing = new CumulativeRevenue(0, slots, 6, 3000); //Cumulative performance of 12 at 30€
        MyActivity a = new MyActivity("a").add(pricing).relocatibility(Relocability.NO);

        // 3 working modes, name; performance; power
        WM s1 = WM("S", 1, 1);
        WM m1 = WM("M", 2, 2);

        //2 Datacenters
        a.add(new DatacenterPart("dc1").add(s1).add(m1));
        OPCP2 pb = new OPCP2(slots, Arrays.asList(g1, g2), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve());
    }

    @Test
    public void testInstantRevenues() {
        int slots = 3;
        //1 € per watt/hour
        PowerSource g1 = new PowerSource("g1", makeSlots(makeLinearProfile(slots, 1, 1), makeProfile(slots, 1), makeProfile(slots, 1))).dcId("dc1");
        MyActivity a = new MyActivity("a").relocatibility(Relocability.NO);
        for (int i = 0; i < slots; i++) {
            a.add(new InstantRevenue(i, 2, 100));
        }

        // 3 working modes, name; performance; power
        WM s1 = WM("S", 1, 1);
        WM m1 = WM("M", 4, 2);

        //2 Datacenters
        a.add(new DatacenterPart("dc1").add(s1).add(m1));
        OPCP2 pb = new OPCP2(slots, Arrays.asList(g1), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve());
        IntVar profit = pb.getProfit();
        //5Watts per timeslot, 2 slots where instant revenue is satisfied
        Assert.assertEquals(-5 + 100 + 100, pb.val(profit));

    }

    @Test
    public void testSwitch() {
        int slots = 3;
        PowerSource grid = new PowerSource("grid",
                makeSlots(new int[]{1000, 1000, 10000},
                        makeProfile(slots, 0),
                        makeProfile(slots, 1600))).dcId("dc1"); //16 c€ kWh
        PowerSource pv = new PowerSource("pv",
                makeSlots(new int[]{0, 150, 0},
                        makeProfile(slots, 100),
                        makeProfile(slots, 0))).dcId("dc1"); //euro-cents

        MyActivity a = new MyActivity("a").relocatibility(Relocability.NO);
        // 3 working modes, name; performance; power
        WM s = WM("S", 100, 100);
        WM m = WM("M", 150, 150);
        a.add(new DatacenterPart("dc1").add(s).add(m));
        for (int i = 0; i < slots; i++) {
            InstantRevenue r = new InstantRevenue(i, 100, 5 * 1600 * 100); //5 times the price of the energy used to run the app in mode S
            r.add(new Modifier(150, 103)); //With 150 Watts, a bonus of 1.03 c€. So a bonus < the energy price when running using the grid
            a.add(r);
        }
        OPCP2 pb = new OPCP2(slots, Arrays.asList(grid, pv), Arrays.asList(a));
        Assert.assertEquals(ESat.TRUE, pb.solve());
        //IntVar profit = pb.getProfit();
        //Assert.assertEquals(-32000 /*2 slots- grid*/ + 2 * a.instantRevenues(0).revenue(100) /*revenue when running in s mode*/ + a.instantRevenues(0).revenue(150), pb.val(profit));
    }

    @Test
    public void testSmallCumulativeWork() {
        int slots = 96;
        PowerSource grid = new PowerSource("grid",
                makeSlots(makeProfile(slots, 10000),
                        makeProfile(slots, 0),
                        makeProfile(slots, 16))).dcId("dc1"); //16 c€ kWh
        MyActivity a = new MyActivity("a").relocatibility(Relocability.NO);
        // 3 working modes, name; performance; power
        WM s = WM("S", 100, 100);
        WM m = WM("M", 150, 150);
        WM f = WM("F", 200, 200);
        a.add(new DatacenterPart("dc1").add(s).add(m).add(f));
        a.add(new CumulativeRevenue(0, slots, 100, 1500));
        //Logger.getRootLogger().addAppender(new ConsoleAppender());
        //Logger.getLogger(OPCP2.class).setLevel(Level.TRACE);
        OPCP2 pb = new OPCP2(slots, Arrays.asList(grid), Arrays.asList(a));
        pb.timeLimit(2);
        Assert.assertEquals(ESat.TRUE, pb.solve());
    }
}