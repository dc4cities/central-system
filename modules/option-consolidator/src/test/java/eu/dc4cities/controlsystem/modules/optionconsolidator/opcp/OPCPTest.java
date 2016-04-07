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

package eu.dc4cities.controlsystem.modules.optionconsolidator.opcp;

import eu.dc4cities.controlsystem.modules.optionconsolidator.Utils;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.junit.Assert;
import org.junit.Ignore;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * Unit tests and sample code for OPCP.
 *
 *
 */
public class OPCPTest {

    /**
     * Simple activity with 3 working mode.
     * simple power source
     */
    @Ignore
    public void testSimpleActivity() {
        int nbSlots = 10;
        int[] profile = Utils.makeLinearProfile(nbSlots, 0, 2);
        OldPowerSource p1 = new OldPowerSource("s1", Utils.makeSlots(profile, new int[nbSlots]));

        SimpleActivity a = new SimpleActivity("a", "a1");
        WorkingModeSlot[] o1 = Utils.makeOptionSlots(Utils.makeProfile(nbSlots, 5), Utils.makeLinearProfile(nbSlots, 0, 0));
        WorkingModeSlot[] o2 = Utils.makeOptionSlots(Utils.makeLinearProfile(nbSlots, 0, 1), Utils.makeLinearProfile(nbSlots, 0, 1));
        WorkingModeSlot[] o3 = Utils.makeOptionSlots(Utils.makeLinearProfile(nbSlots, nbSlots, -1), Utils.makeLinearProfile(nbSlots, 0, 2));
        a.add(new WorkingMode(1, o1, null));
        a.add(new WorkingMode(2, o2, null));
        a.add(new WorkingMode(3, o3, null));

        OPCP opcp = new OPCP(Utils.slots(nbSlots, 1), Collections.singletonList(p1), Collections.singletonList(a));
        System.out.println(opcp);
        Assert.assertEquals(opcp.solve(10), ESat.TRUE);
        Map<SimpleActivity, WorkingMode> res = opcp.getSelectedPlans();
        Assert.assertEquals(res.size(), 1);

        IntVar[] e = opcp.getPowerUsage();
//        IntVar[] c = opcp.getPenalties();
        for (int t = 0; t < nbSlots; t++) {
            Assert.assertEquals(e[t].getLB(), res.get(a).getSlots()[t].powerDemand());
            //Assert.assertEquals(c[t].getLB(), res.get(a).getSlots()[t].price());
        }

        Assert.assertEquals(res.get(a).getName(), 2);
    }

    /**
     * Multiple activities, simple power source
     */
    /*@Ignore
    public void testMultipleActivities() {
        int nbSlots = 10;
        int nbApps = 2;
        int nbPlans = 3;
        int[] profile = Utils.makeProfile(nbSlots, 10);

        System.out.println("Energy Profile: " + Arrays.toString(profile));
        int[][][] demand = new int[nbSlots][2][nbPlans];
        int[][][] cost = new int[nbSlots][2][nbPlans];
        //Create a tight schedule with plan 0 + 1
        for (int a = 0; a < nbApps; a++) {
            for (int i = 0; i < nbPlans; i++) {
                for (int t = 0; t < nbSlots; t++) {
                    if (i == 0) {
                        demand[t][a][i] = t;
                    } else if (i == 1) {
                        demand[t][a][i] = 10 - t;
                    } else if (i == 2) {
                        demand[t][a][i] = t + 1;
                    }
                    cost[t][a][i] = i;
                }
            }
        }
        System.out.println("SimpleActivity 0:\n" + Utils.prettyProfiles(demand, 0));
        System.out.println("SimpleActivity 1:\n" + Utils.prettyProfiles(demand, 1));
        OPCP opcp = new OPCP(new int[][]{profile}, demand, cost);
        Assert.assertEquals(opcp.solve(10), ESat.TRUE);
        Map<String, String> opcp.getSelectedPlans();
        Assert.assertEquals(res.length, 2);
        Assert.assertTrue((res[0] == 1 && res[1] == 0) || (res[0] == 0 && res[1] == 1));

        IntVar[] e = opcp.getPowerUsage();
        IntVar[] c = opcp.getPenalties();
        for (int t = 0; t < nbSlots; t++) {
            Assert.assertEquals(e[t].getLB(), 10);
            Assert.assertEquals(c[t].getLB(), 1); //plan 0+1 or 1+0
        }
    }          */

    /**
     * Simple activity, multiple sources
     */
    /*@Ignore
    public void testEnergyAggregation() {
        int nbSlots = 10;

        //Two energy sources, one constant, one decreasing
        int[] s1 = Utils.makeProfile(nbSlots, 20);
        int[] s2 = Utils.makeLinearProfile(nbSlots, 11, -1);

        PowerSource ps1 = new PowerSource("s1", Utils.makeSlots(s1, new int[nbSlots], new int[nbSlots]));
        PowerSource ps2 = new PowerSource("s2", Utils.makeSlots(s2, new int[nbSlots], new int[nbSlots]));

        //The activity consumes everything
        SimpleActivity a = new SimpleActivity("a", "a1");
        a.add(new WorkingMode(1, Utils.makeOptionSlots(Utils.makeLinearProfile(nbSlots, 15, -1), new int[nbSlots]), null));

        OPCP pb = new OPCP(Utils.slots(nbSlots, 1), Arrays.asList(ps1, ps2), Collections.singletonList(a));

        new MinEnergySourceUsage(0).decorate(pb);
        System.out.println(pb);
        ESat res = pb.solve(-1);
        Assert.assertEquals(res, ESat.TRUE);

        int[] ideal = new int[nbSlots];
        int[] usageS1 = new int[nbSlots];
        int[] usageS2 = new int[nbSlots];

        for (int t = 0; t < nbSlots; t++) {
            System.out.println(pb.getPowerUsage()[t]);
            //Assert.assertEquals(pb.getPowerUsage()[t].getValue(),
            //        pb.getPowerUsage("s1")[t].getValue() + pb.getPowerUsage("s2")[t].getValue());
            ideal[t] = pb.getPowerUsage()[t].getValue();
            System.out.println(Arrays.toString(pb.getPowerUsage("s1")));
            System.out.println(Arrays.toString(pb.getPowerUsage("s2")));
            usageS1[t] = pb.getPowerUsage("s1")[t].getValue();
            usageS2[t] = pb.getPowerUsage("s2")[t].getValue();
        }
        System.out.println("Ideal Power Plan:" + Arrays.toString(ideal));
        System.out.println("Decomposition:");
        System.out.println("\tOn source 1:" + Arrays.toString(usageS1));
        System.out.println("\tOn source 2:" + Arrays.toString(usageS2));
    } */

    /*@Ignore
    public void testWithUnpureEnergies() {
        int nbSlots = 4;
        int[] s1 = Utils.makeProfile(nbSlots, 1000); //1kW per slot
        int[] pctGreen = new int[]{0, 20, 50, 100};
        WorkingMode m1 = new WorkingMode(1, Utils.makeOptionSlots(new int[]{10, 70, 500, 747}, new int[nbSlots]), null);
        PowerSource ps1 = new PowerSource("s1", Utils.makeSlots(s1, pctGreen, new int[nbSlots]));
        SimpleActivity a1 = new SimpleActivity("a", "a1");
        a1.add(m1);
        OPCP pb = new OPCP(Utils.slots(nbSlots, 1), Collections.singletonList(ps1), Collections.singletonList(a1));
        Assert.assertEquals(pb.solve(-1), ESat.TRUE);

        for (int t = 0; t < nbSlots; t++) {
            int gReal = pb.getRenewablePowerUsage("s1")[t].getLB();
            int gTheory = m1.getSlots()[t].powerDemand() * ps1.getSlots()[t].getRenewablePct() / 100;
            System.out.println(gReal + " " + gTheory);
            Assert.assertEquals(gReal, gTheory);
        }
    }   */

    /*@Ignore
    public void testEnergyVariables() {
        int[] s = new int[]{4000, 9000};
        PowerSource p = new PowerSource("s1", Utils.makeSlots(s, new int[]{0, 0}, new int[]{0, 0}));
        int[] c = new int[]{3600, 7200}; //3600 Watts during 1 second -> 3600 Joules = 1Wh, 7200 * 2 sec = 4Wh
        SimpleActivity a = new SimpleActivity("a", "a1");
        a.add(new WorkingMode(1, Utils.makeOptionSlots(c, new int[]{0, 0}), null));
        OPCP pb = new OPCP(new int[]{1, 2}, Collections.singletonList(p), Collections.singletonList(a));
        IntVar watts = pb.getEnergyUsage();
        Assert.assertEquals(pb.solve(-1), ESat.TRUE);
        System.out.println(pb.getSolver());

        Assert.assertEquals(5, watts.getValue());
    }  */
    @Ignore
    public void testWithNoSolutions() {
        int[] s = new int[]{2};
        OldPowerSource p = new OldPowerSource("s1", Utils.makeSlots(new int[]{3}, new int[]{0}));

        SimpleActivity a = new SimpleActivity("a", "a1");
        a.add(new WorkingMode(1, Utils.makeOptionSlots(new int[]{5}, new int[]{7}), null));

        OPCP pb = new OPCP(s, Collections.singletonList(p), Collections.singletonList(a));
        Assert.assertTrue(pb.getSelectedPlans().isEmpty());
        Assert.assertEquals(pb.solve(-1), ESat.FALSE);
        Assert.assertTrue(pb.getSelectedPlans().isEmpty());
        Assert.assertNull(pb.getSelectedPlan(a));
    }

    @Ignore
    public void testReductionFactor() {
        OldPowerSource s = new OldPowerSource("s1", Utils.makeSlots(new int[]{10000, 14000, 37001}, new int[]{0, 0, 0}));
        SimpleActivity a1 = new SimpleActivity("easc", "a1");
        WorkingMode w1 = new WorkingMode(0, Utils.makeOptionSlots(new int[]{7000, 12000, 40000}, new int[]{0, 0, 0}), null);
        WorkingMode w2 = new WorkingMode(1, Utils.makeOptionSlots(new int[]{7000, 14000, 37001}, new int[]{0, 0, 0}), null);
        a1.add(w1);
        a1.add(w2);
        OPCP pb = new OPCP(new int[]{900, 900, 900}, Arrays.asList(s), Arrays.asList(a1), 4);
        Assert.assertEquals(4, pb.getWattReducingFactor());
        Assert.assertEquals(ESat.TRUE, pb.solve(0));
        Assert.assertEquals(w2, pb.getSelectedPlans().get(a1));

        //Check the variable value
        //w2 will be reduced to 1750, 3500, 9250
        IntVar[] pwr = pb.getPowerUsage();
        Assert.assertEquals(1750, pwr[0].getValue());
        Assert.assertEquals(3500, pwr[1].getValue());
        Assert.assertEquals(9250, pwr[2].getValue());
        //Invert the reduction
        Assert.assertEquals(7000, pb.toWatt(pwr[0]));
        Assert.assertEquals(14000, pb.toWatt(pwr[1]));
        Assert.assertEquals(37000, pb.toWatt(pwr[2]));
    }
}
