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

import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.variables.IntVar;

/**
 * Select the working mode to try depending on
 * the renPct goal.
 * If the slot is over the renPCT baseline, go for max performance
 * otherwise, go for the least performance.
 */
public class WorkingModeSelector implements IntValueSelector {

    private int expect;

    private IntVar[] where;

    private int timeSlot;

    private OPCP2 pb;

    private RenPct renPct;

    private boolean pureAvailable = false;
    public WorkingModeSelector(OPCP2 pb, IntVar[] where, int expect, int timeSlot) {
        this.expect = expect;
        this.timeSlot = timeSlot;
        this.pb = pb;
        this.where = where;
        if (!pb.getDynamicCosts().isEmpty() && pb.getDynamicCosts().get(0) instanceof RenPct) {
            this.renPct = (RenPct) pb.getDynamicCosts().get(0);
        }
        for (PowerSource s : pb.getSources()) {
            if (s.isPure()) {
                pureAvailable = true;
            }
        }
    }


    public int range(IntVar v) {
        return v.getUB() - v.getLB();
    }


    @Override
    public int selectValue(IntVar v) {
        int lb = pb.getRevenues().getLB() - pb.maxRevenues();
        int ub = pb.getRevenues().getUB() - pb.maxRevenues();
        //System.out.println(pb.getGlobalEnergy() + " slo penalties: [" + lb + ";" + ub + "]; " + pb.getGlobalCost());
        //System.out.println(range(pb.getGlobalEnergy()) + " " + range(pb.getRevenues()) + " " + range(pb.getGlobalCost()));
        if (pureAvailable) {
            if (!leftToDo(toAid(v))) {
                return mostProfitable(v);
            }
            if (maxRenPct(v)) {
                //System.out.println("UP!");
                return v.getUB();
            }
            //System.out.println("down");
            return mostProfitable(v);
        } else {
            if ((maxRenPct(v) || worthy(v)) && leftToDo(toAid(v))) {
                //System.out.println("UP!");
                return v.getUB();
            }
            return mostProfitable(v);
        }
    }

    private boolean leftToDo(int aId) {
        for (CumulativeRevenue cr : pb.getAutomaton().get(aId).job.cumulativeRevenues()) {
            IntVar left = pb.revenue(aId, cr);
            if (left.isInstantiated()) {
                //System.out.println("nothing left to do");
                return false;
            }
        }
        return true;
    }

    private int mostProfitable(IntVar v) {
        int aId = toAid(v);
        InstantRevenue ir = pb.getAutomaton().get(aId).job.instantRevenues(timeSlot);
        if (ir != null) {
            if (((renPct.percentage().getLB() / 10) + (renPct.percentage().getUB() / 10)) / 2 >= expect) {
                //System.out.println("renPct done");
                //We reached the goal, we can go up
                //System.out.println("UP!");
                return v.getUB();
            }
        }
        //System.out.println("still have to work: " + renPct.percentage());
        //System.out.println("down!");
        return v.getLB();
    }

    private int toAid(IntVar v) {
        int aId = 0;
        for (IntVar v2 : where) {
            if (v2 == v) {
                return aId;
            }
            aId++;
        }
        return -1;
    }

    private double average(PowerSource src) {
        int sum = 0;
        for (int t = 0; t < pb.getNbSlots(); t++) {
            sum += src.slots()[t].renPct();
        }
        return sum / pb.getNbSlots();
    }

    private boolean maxRenPct(IntVar v) {
        /*if (renPct.entailed()) {
            return true;
        }*/
        for (PowerSource s : pb.getSources()) {
            PowerSourceSlot slot = s.slots()[timeSlot];
            //possible good ren sources
            int sId = pb.powerSource(s);
            int dcId = pb.dc(s.dcId());
            //System.out.println("available %: " + slot.renPct() + " ; expect=" +expect + " pct= " + renPct.percentage());
            //System.out.println("available power: " + pb.getSourcePowerUsage()[sId][timeSlot]);
            if (slot.renPct() >= expect && slot.peak() > 0) {
                //System.out.println("Possible capacity: " + pb.getSourcePowerUsage()[sId][timeSlot]+ " usage=" + pb.getActivitiesPowerUsage()[toAid(v)][timeSlot][dcId]);

                if (pb.getActivitiesPowerUsage()[toAid(v)][timeSlot][dcId].getLB() > pb.getSourcePowerUsage()[sId][timeSlot].getUB()) {
                    //System.out.println("no enough power"); //because we will use it a bit
                    return false;
                }
                return true;
            }
            //System.out.println("Not green enough");
        }
        return false;
    }

    private boolean worthy(IntVar v) {
        for (PowerSource s : pb.getSources()) {
            int available = s.slots()[timeSlot].renPct();
            double avg = average(s);
            //System.out.println("available: " + available + " ; avg=" + avg + " " + " pct= " + renPct.percentage());
            if (avg > available) {
                //below the average. Lets try the next source
                continue;
            }

            //possible good ren sources
            int sId = pb.powerSource(s);
            int dcId = pb.dc(s.dcId());
            //System.out.println("Possible capacity: " + pb.getSourcePowerUsage()[sId][timeSlot]+ " with ren = " + slot.renPct());
            if (pb.getActivitiesPowerUsage()[toAid(v)][timeSlot][dcId].getLB() > pb.getSourcePowerUsage()[sId][timeSlot].getUB()) {
                //System.out.println("no enough power");
                continue;
            }
            //System.out.println("worthy");
            return true;
        }
        //System.out.println("not worthy");
        return false;
    }
}
