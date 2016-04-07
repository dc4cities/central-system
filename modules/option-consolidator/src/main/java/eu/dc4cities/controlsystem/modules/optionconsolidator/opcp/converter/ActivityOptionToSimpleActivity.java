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
import eu.dc4cities.controlsystem.modules.optionconsolidator.AllTuplesGenerator;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.SimpleActivity;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.WorkingMode;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.WorkingModeSlot;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Power;
import java.util.*;

import static javax.measure.unit.SI.WATT;

/**
 * An adapter to convert an {@link ActivityOption} to a simple list of {@link eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.SimpleActivity}
 * that can be used inside an {@link eu.dc4cities.controlsystem.modules.optionconsolidator.opcp.OPCP}.
 *
 *
 */
public class ActivityOptionToSimpleActivity {

    private String aName, easc;
    
    private Amount<Power> idlePower;

    private List<OptionPlan> plans;

    private List<WorkingMode> wms;

    private Map<Integer, WorkingModeSlot> cache;

    /**
     * New converter.
     *
     * @param opts the activity options
     */
    public ActivityOptionToSimpleActivity(String easc, ActivityOption opts) {
    	this.easc = easc;
        this.aName = opts.getActivity();
        this.idlePower = opts.getIdlePower();
        this.plans = opts.getOptionPlans();
        wms = new LinkedList<>();

        cache = new WeakHashMap<>();
        convert();
    }

    private void convert() {
        for (OptionPlan p : plans) {
            List<List<Work>> x = flatten(p);
            AllTuplesGenerator<Work> tg = new AllTuplesGenerator<>(Work.class, x);
            for (Work[] o : tg) {
                toWorkingMode(o);
            }
        }
    }

    /**
     * Get the resulting activity.
     *
     * @return an activity composed by a flatten view of all the option plans.
     */
    public SimpleActivity getSimpleActivity() {
        return new SimpleActivity(easc, aName, wms);
    }

    private WorkingMode toWorkingMode(Work[] opts) {
        List<WorkingModeSlot> slots = new ArrayList<>();
        Activity a = new Activity(aName);
        a.setIdlePower(idlePower);
        int slot = 0;
        for (Work o : opts) {
            //Padding
            while (slot < o.getStartTimeSlot()) {
                slots.add(WorkingModeSlot.ZERO);
                slot++;
            }
            for (int j = 0; j < (o.getEndTimeSlot() - o.getStartTimeSlot() + 1); j++) {
                slots.add(makeSlot(o));
                slot++;
            }
            a.addWork(o);
        }
        int id = wms.size();
        WorkingMode w = new WorkingMode(id, slots.toArray(new WorkingModeSlot[slots.size()]), a);
        wms.add(w);
        return w;
    }

    private WorkingModeSlot makeSlot(Work w) {
        int watts = (int) w.getPower().longValue(WATT);
        int hash = watts + w.getGreenPoints() * 31;
        WorkingModeSlot s = cache.get(hash);
        if (s == null) {
            s = new WorkingModeSlot(watts, w.getGreenPoints());
            cache.put(hash, s);
        }
        return s;
    }
    private List<List<Work>> flatten(OptionPlan p) {
        List<List<Work>> alt = new ArrayList<>(p.getWorkOptions().size());
        for (WorkOption o : p.getWorkOptions()) {
            alt.add(flatten(o));
        }
        return alt;
    }

    /**
     * Get all the possible work for a given work option.
     *
     * @param o the option to flatten
     * @return a list of work
     */
    private List<Work> flatten(WorkOption o) {
        List<Work> res = new ArrayList<>();

        for (ModeOption mo : o.getModeOptions()) {
            Work tom = new Work(o.getStartTimeSlot(), o.getEndTimeSlot(), mo.getWorkingMode(), mo.getPower(), mo.getGreenPoints());
            res.add(tom);
        }
        return res;
    }

}
