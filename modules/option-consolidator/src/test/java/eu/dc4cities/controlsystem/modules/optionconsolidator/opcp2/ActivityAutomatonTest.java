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

import eu.dc4cities.controlsystem.model.easc.ForbiddenState;
import eu.dc4cities.controlsystem.model.easc.ForbiddenWorkingMode;
import eu.dc4cities.controlsystem.model.easc.Relocability;
import org.chocosolver.solver.constraints.nary.automata.FA.ICostAutomaton;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static eu.dc4cities.controlsystem.modules.optionconsolidator.Utils.WM;

/**
 * Created by fhermeni on 02/06/2015.
 */
public class ActivityAutomatonTest {

    @Test
    public void testMakeAutomationFixType() {
        MyActivity a = new MyActivity("a");
        DatacenterPart p1 = new DatacenterPart("dc1").add(WM("S", 1)).add(WM("M", 3)).add(WM("F",5));
        DatacenterPart p2 = new DatacenterPart("dc2").add(WM("S", 1)).add(WM("M",3)).add(WM("F",5));
        a.add(p1);
        ActivityAutomaton aa = new ActivityAutomaton(a);
        Assert.assertEquals(3, aa.automaton().getNbStates());
        //System.out.println(aa.toDot());
    }

    @Test
    public void testMakeAutomationMigrate() {
        MyActivity a = new MyActivity("a");
        a.relocatibility(Relocability.MIGRATABLE);
        DatacenterPart p1 = new DatacenterPart("dc1").add(WM("0", 1)).add(WM("S", 1)).add(WM("M", 3));
        DatacenterPart p2 = new DatacenterPart("dc2").add(WM("0", 1)).add(WM("S", 1)).add(WM("M", 3)).add(WM("L", 3));
        a.add(p1);
        ActivityAutomaton aa = new ActivityAutomaton(a);
        Assert.assertEquals(3, aa.automaton().getNbStates());

        a.add(p2);
        aa = new ActivityAutomaton(a);
        Assert.assertEquals(7, aa.automaton().getNbStates());
        //System.out.println(aa.toDot());
    }

    @Test
    public void testMakeAutomationSpreadable()  {
        MyActivity a = new MyActivity("a");
        a.relocatibility(Relocability.SPREADABLE);
        DatacenterPart p1 = new DatacenterPart("dc1").add(WM("S", 3, 3)).add(WM("M", 3, 3)).add(WM("L", 3, 3));
        DatacenterPart p2 = new DatacenterPart("dc2").add(WM("S", 3, 3)).add(WM("M", 3, 3));
        a.add(p1).add(p2);
        ActivityAutomaton aa = new ActivityAutomaton(a);
        Assert.assertEquals(6, aa.automaton().getNbStates());
        //   System.out.println(aa.toDot());
    }

    @Test
    public void testTrento() {
        MyActivity a = new MyActivity("Trento");
        DatacenterPart p1 = new DatacenterPart("dc1").add(WM("WM1", 3)).add(WM("WM2", 3));
        a.add(p1);
        ActivityAutomaton aa = new ActivityAutomaton(a);
        Assert.assertEquals(2, aa.automaton().getNbStates());
//        System.out.println(aa.toDot());
    }

    @Test
    public void testHP() {
        MyActivity a = new MyActivity("HP");
        a.relocatibility(Relocability.SPREADABLE);
        DatacenterPart usw = new DatacenterPart("us-w");
        usw.add(WM("WM0").addPerfLevel(0, 200));
        usw.add(WM("WM1").addPerfLevel(1190, 259).addPerfLevel(1360, 296).addPerfLevel(1530, 333).addPerfLevel(1700, 370));
        usw.add(WM("WM2").addPerfLevel(1500, 264).addPerfLevel(1750, 307).addPerfLevel(2000, 352).addPerfLevel(2250, 396).addPerfLevel(2500, 440));

        DatacenterPart use = new DatacenterPart("us-e");
        use.add(WM("WM0").addPerfLevel(0, 200));
        use.add(WM("WM1").addPerfLevel(1190, 259).addPerfLevel(1360, 296).addPerfLevel(1530, 333).addPerfLevel(1700, 370));
        use.add(WM("WM2").addPerfLevel(1500, 264).addPerfLevel(1750, 307).addPerfLevel(2000, 352).addPerfLevel(2250, 396).addPerfLevel(2500, 440));

        DatacenterPart eu = new DatacenterPart("eu");
        eu.add(WM("WM0").addPerfLevel(0, 200));
        eu.add(WM("WM1").addPerfLevel(1190, 259).addPerfLevel(1360, 296).addPerfLevel(1530, 333).addPerfLevel(1700, 370));
        eu.add(WM("WM2").addPerfLevel(1500, 264).addPerfLevel(1750, 307).addPerfLevel(2000, 352).addPerfLevel(2250, 396).addPerfLevel(2500, 440));

        a.add(usw).add(use).add(eu);
        ActivityAutomaton aa = new ActivityAutomaton(a);
        Assert.assertEquals(1000, aa.automaton().getNbStates());
    }

    @Test
    public void testMigrationCost() {
        MyActivity a = new MyActivity("foo");
        DatacenterPart p1 = new DatacenterPart("dc1");
        WM p1w0 = WM("WM0").addPerfLevel(1, 1).addPerfLevel(2, 2);
        WM p1w1 = WM("WM1").addPerfLevel(3, 3).addPerfLevel(4, 4);
        p1.add(p1w0).add(p1w1);

        DatacenterPart p2 = new DatacenterPart("dc2");
        WM p2w0 = WM("WM0").addPerfLevel(1, 1).addPerfLevel(2, 2);
        WM p2w1 = WM("WM1").addPerfLevel(3, 3).addPerfLevel(4, 4);
        p2.add(p2w0).add(p2w1);

        a.relocatibility(Relocability.MIGRATABLE).add(p1).add(p2).migrationCost(3);
        ActivityAutomaton aa = new ActivityAutomaton(a);
        //Check for the transition costs
        for (int i = 0; i < aa.nbStates(); i++) {
            for (int j = 0; j < aa.nbStates(); j++) {
                State sI = aa.state(i);
                State sJ = aa.state(j);
                List<PerfLevel> plI = sI.perfLevels();
                List<PerfLevel> plJ = sJ.perfLevels();
                for (int w = 0; w < plI.size(); w++) {
                    WM w1 = plI.get(w).WM();
                    WM w2 = plJ.get(w).WM();
                    int c = 0;
                    if (!w1.getDc().equals(w2.getDc())) {
                        c = 3;
                    }
                    Assert.assertEquals(c, aa.transitionCost(i, j));
                }
            }
        }
    }
    @Test
    public void testMultiplePerfLevel() {
        MyActivity a = new MyActivity("foo");
        DatacenterPart p1 = new DatacenterPart("dc1");
        WM p1w0 = WM("WM0").addPerfLevel(1, 1).addPerfLevel(2, 2);
        WM p1w1 = WM("WM1").addPerfLevel(3, 3).addPerfLevel(4, 4);
        p1.add(p1w0);
        p1.add(p1w1);
        p1.setTransitionCost(p1w0, p1w1, 3);
        p1.setTransitionCost(p1w1, p1w0, 2);

        DatacenterPart p2 = new DatacenterPart("dc2");
        WM p2w0 = WM("WM0").addPerfLevel(1, 1).addPerfLevel(2, 2);
        WM p2w1 = WM("WM1").addPerfLevel(3, 3).addPerfLevel(4, 4);
        p2.add(p2w0);
        p2.add(p2w1);
        p1.setTransitionCost(p2w0, p2w1, 5);
        p1.setTransitionCost(p2w1, p2w0, 4);

        a.add(p1);//.add(p2);
        ActivityAutomaton aa = new ActivityAutomaton(a);
        Assert.assertEquals(4, aa.automaton().getNbStates());

        //Check for the transition costs
        for (int i = 0; i < aa.nbStates(); i++) {
            for (int j = 0; j < aa.nbStates(); j++) {
                State sI = aa.state(i);
                State sJ = aa.state(j);
                List<PerfLevel> plI = sI.perfLevels();
                List<PerfLevel> plJ = sJ.perfLevels();
                for (int w = 0; w < plI.size(); w++) {
                    WM w1 = plI.get(w).WM();
                    WM w2 = plJ.get(w).WM();
                    if (w1 == w2) {
                        Assert.assertEquals(0, aa.transitionCost(i, j));
                    } else {
                        if (p1w0 == w1 && w2 == p1w1) {
                            Assert.assertEquals(3, aa.transitionCost(i, j));
                        } else if (p1w1 == w1 && w2 == p1w0) {
                            Assert.assertEquals(2, aa.transitionCost(i, j));
                        }
                    }
                }
            }
        }
        a.add(p2);

        a.relocatibility(Relocability.MIGRATABLE);
        aa = new ActivityAutomaton(a);
        Assert.assertEquals(8, aa.automaton().getNbStates());

        a.relocatibility(Relocability.SPREADABLE);
        aa = new ActivityAutomaton(a);
        Assert.assertEquals(16, aa.automaton().getNbStates());
    }

    @Test
    public void testPerfs() {
        int nbSlots = 6;
        MyActivity a = new MyActivity("foo");
        DatacenterPart d1 = new DatacenterPart("dc1");
        WM w1 = WM("w1", 10, 10);
        WM w2 = WM("w2", 20, 20);
        WM w3 = WM("w3", 30, 30);
        d1.add(w1).add(w2).add(w3);
        a.add(d1);
        CumulativeRevenue c1 = new CumulativeRevenue(0, 3, 20, 20);
        CumulativeRevenue c2 = new CumulativeRevenue(3, nbSlots, 10, 20);
        for (int i = 0; i < 3; i++) {
            a.add(new InstantRevenue(i, i + 1, 5));
        }
        a.add(c1).add(c2);
        ActivityAutomaton aa = new ActivityAutomaton(a);
        ICostAutomaton ic = aa.costAutomaton(nbSlots);

        //[timeslot][nextState][currentState][dimension] = Count
        //int layer, int value, int counter, int state
        //c1
        //TODO: test instant pricing
        int idC1 = aa.cumulative(c1);
        int idC2 = aa.cumulative(c2);
        for (int t = 0; t < nbSlots; t++) {
            for (int i = 0; i < aa.nbStates(); i++) {
                for (int j = 0; j < aa.nbStates(); j++) {
                    int p1 = (int) ic.getCostByResourceAndState(t, j, idC1, i);
                    int p2 = (int) ic.getCostByResourceAndState(t, j, idC2, i);
                    int p3 = (int) ic.getCostByResourceAndState(t, j, aa.instant(), i);
                    if (t < 3) {
                        Assert.assertNotEquals("was " + p1 + " want not 0", 0, p1);
                        Assert.assertEquals(0, p2);
                    } else {
                        Assert.assertEquals(0, p1);
                        Assert.assertNotEquals("was " + p2 + " want not 0", 0, p2);
                    }
                    //Assert.assertEquals(t + 1, p3);
                }
            }
        }
    }

    @Test
    public void testForbiddenStates() {
        MyActivity a = new MyActivity("foo");
        DatacenterPart d1 = new DatacenterPart("d1");
        WM w11 = WM("w1", 10, 10);
        WM w12 = WM("w2", 20, 20);
        d1.add(w11).add(w12);

        DatacenterPart d2 = new DatacenterPart("d2");
        WM w21 = WM("w1", 10, 10);
        WM w22 = WM("w2", 20, 20);
        d2.add(w21).add(w22);

        a.add(d1).add(d2);
        ForbiddenState f = new ForbiddenState();
        List<ForbiddenWorkingMode> wms = new ArrayList<>();
        wms.add(new ForbiddenWorkingMode("d1", "w1"));
        wms.add(new ForbiddenWorkingMode("d2", "w2"));
        f.setWorkingModes(wms);
        a.relocatibility(Relocability.SPREADABLE);
        ActivityAutomaton auto = new ActivityAutomaton(a);
        Assert.assertEquals(4, auto.nbStates());
        a.forbiddenStates(Arrays.asList(f));

        auto = new ActivityAutomaton(a);
        Assert.assertEquals(3, auto.nbStates());
        System.out.println(auto.toString());

        wms.clear();
        wms.add(new ForbiddenWorkingMode("d1", "w1"));
        auto = new ActivityAutomaton(a);
        Assert.assertEquals(2, auto.nbStates());
    }

}