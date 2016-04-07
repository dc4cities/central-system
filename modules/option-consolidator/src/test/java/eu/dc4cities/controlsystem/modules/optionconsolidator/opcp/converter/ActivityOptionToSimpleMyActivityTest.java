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

package eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.converter;

import eu.dc4cities.controlsystem.model.easc.*;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.SimpleActivity;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.WorkingMode;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.WorkingModeSlot;
import org.jscience.physics.amount.Amount;
import org.junit.Assert;
import org.junit.Test;

import javax.measure.unit.SI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static javax.measure.unit.SI.WATT;

/**
 *
 */
public class ActivityOptionToSimpleMyActivityTest {

    /**
     * Test the deliverable example
     */
    @Test
    public void test() throws Exception {
        /*
        MyApp:
        X1: X1.1(w1d1) || X1.2(w2d2 OR w2d1)
        ||
        X2: X2.1(w3d2)
         */

        OptionPlan x1 = new OptionPlan();
        WorkOption x1_1 = new WorkOption();
        x1_1.setStartTimeSlot(0);
        x1_1.setEndTimeSlot(5);
        ModeOption w1d1 = new ModeOption("W1D1", Amount.valueOf(20, WATT), 1);
        x1_1.addModeOption(w1d1);
        x1.addWorkOption(x1_1);

        WorkOption x1_2 = new WorkOption();
        x1_2.setStartTimeSlot(6);
        x1_2.setEndTimeSlot(9);
        ModeOption w2d2 = new ModeOption("W2D2", Amount.valueOf(40, WATT), 0);
        x1_2.addModeOption(w2d2);

        ModeOption w2d1 = new ModeOption("W2D1", Amount.valueOf(30, WATT), 2);
        x1_2.addModeOption(w2d1);
        x1.addWorkOption(x1_2);

        OptionPlan x2 = new OptionPlan();
        WorkOption x2_1 = new WorkOption();
        x2_1.setStartTimeSlot(0);
        x2_1.setEndTimeSlot(9);
        ModeOption w3d2 = new ModeOption("W3D2", Amount.valueOf(40, WATT), 0);
        x2_1.addModeOption(w3d2);

        x2.addWorkOption(x2_1);

        List<OptionPlan> plans = new ArrayList<>();
        plans.add(x1);
        plans.add(x2);

        ActivityOption ao = new ActivityOption("Activity1");
        for (OptionPlan p : plans) {
            ao.addOptionPlan(p);
        }
        ActivityOptionToSimpleActivity conv = new ActivityOptionToSimpleActivity("EASC1", ao);

        SimpleActivity a = conv.getSimpleActivity();
        System.out.println(ao);
        System.out.println(a);
        Assert.assertEquals(a.getWorkingModes().size(), 3);
        for (WorkingMode m : a.getWorkingModes()) {
            Assert.assertEquals(m.getSlots().length, 10);
            checkConsistency(m, m.getActivity());
        }
    }

    private void checkConsistency(WorkingMode m, Activity a) {
        int t = 0;
        WorkingModeSlot[] slots = m.getSlots();
        for (Work w : a.getWorks()) {
            while (t < w.getStartTimeSlot()) {
                Assert.assertEquals(slots[t].powerDemand(), 0);
                Assert.assertEquals(slots[t].penalty(), 0);
                t++;
            }
            for (int i = 0; i < (w.getEndTimeSlot() - w.getStartTimeSlot() + 1); i++) {
                Assert.assertEquals(slots[t].powerDemand(), w.getPower().longValue(SI.WATT));
                Assert.assertEquals(slots[t].penalty(), w.getGreenPoints());
                t++;
            }
        }
    }

    @Test
    public void testWithHoles() {
        OptionPlan x1 = new OptionPlan();
        WorkOption x1_1 = new WorkOption();
        x1_1.setStartTimeSlot(0);
        x1_1.setEndTimeSlot(5);
        ModeOption x1_1_1 = new ModeOption("W1D1", Amount.valueOf(20, WATT), 1);
        x1_1.addModeOption(x1_1_1);
        x1.addWorkOption(x1_1);

        WorkOption x1_2 = new WorkOption();
        x1_2.setStartTimeSlot(8);
        x1_2.setEndTimeSlot(9);
        ModeOption x1_2_1 = new ModeOption("W3D2", Amount.valueOf(40, WATT), 0);
        x1_2.addModeOption(x1_2_1);

        ModeOption x1_2_2 = new ModeOption("W2D1", Amount.valueOf(30, WATT), 2);
        x1_2.addModeOption(x1_2_2);
        x1.addWorkOption(x1_2);


        List<OptionPlan> plans = new ArrayList<>();
        plans.add(x1);

        ActivityOption ao = new ActivityOption("Activity1");
        for (OptionPlan p : plans) {
            ao.addOptionPlan(p);
        }
        ActivityOptionToSimpleActivity conv = new ActivityOptionToSimpleActivity("EASC1", ao);

        SimpleActivity a = conv.getSimpleActivity();
        System.out.println(ao);
        System.out.println(a);
        Assert.assertEquals(a.getWorkingModes().size(), 2);
        for (WorkingMode m : a.getWorkingModes()) {                        
            Assert.assertEquals(m.getSlots().length, 10);
            checkConsistency(m, m.getActivity());
            //WM1: 0:5 - W1D1; 6:10 - W3D2
            //WM2: 0:5 - W1D1; 6:10 - W2D1
            //WM3: 0:10 - W3D2
        }
    }

    @Test
    public void largeTest() {
        //96 slots, each time 3 alternatives. Damn '
        ActivityOption a = new ActivityOption("activity1");
        a.setOptionPlans(new ArrayList<OptionPlan>());
        OptionPlan p = new OptionPlan();
        p.setWorkOptions(new ArrayList<WorkOption>());

        int nbSlots = 10;
        Random rnd = new Random();
        for (int i = 0; i < nbSlots; i++) {
            WorkOption wo = new WorkOption();
            wo.setStartTimeSlot(i);
            wo.setEndTimeSlot(i + 1);
            wo.addModeOption(new ModeOption("W" + i + "A1", Amount.valueOf(rnd.nextInt(5), SI.WATT), 0));
            wo.addModeOption(new ModeOption("W" + i + "A2", Amount.valueOf(rnd.nextInt(200), SI.WATT), 0));
            p.addWorkOption(wo);
        }
        a.addOptionPlan(p);
        ActivityOptionToSimpleActivity conv = new ActivityOptionToSimpleActivity("easc1", a);
        SimpleActivity sa = conv.getSimpleActivity();
        Assert.assertEquals(sa.getWorkingModes().size(), (int) Math.pow(2, nbSlots));
        for (WorkingMode wm : sa.getWorkingModes()) {
            checkConsistency(wm, wm.getActivity());
        }
        //System.out.println(sa);
    }
}
