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

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.chocosolver.solver.search.strategy.IntStrategyFactory.*;

/**
 * An objective that minimizes the total usage of non-renewable power.
 *
 *
 */
public class MinBrownEnergyUsage implements OptConstraint {

    private IntVar brown;

    @Override
    public IntVar getObjectiveVariable() {
        return brown;
    }

    @Override
    public void decorate(OPCP pb) {
        Solver s = pb.getSolver();

        if (pb.areSlotsHomogeneous()) {
            List<IntVar> brownList = new ArrayList<>(pb.getNbSlots() * pb.getPowerSources().size());
            for (OldPowerSource src : pb.getPowerSources()) {
                if (!pure(src)) {
                    //We don't need to divide by 100 because it is a constant on each term
                    //so it can be set ot of the opt variable (we don't care about the variable value)
                    brownList.addAll(Arrays.asList(getBrownUsage(pb, src)));
                }
            }

            IntVar[] brownVars = brownList.toArray(new IntVar[brownList.size()]);
            brown = VF.bounded("total_brown_power", 0, Integer.MAX_VALUE - 1, s);
            s.post(ICF.sum(brownVars, brown));

            pb.setObjective(brown, ResolutionPolicy.MINIMIZE);

            IntVar [] c = pb.getSelections();


            s.set(sequencer(
                    impact(c, 5),
                    custom(minDomainSize_var_selector(), max_value_selector(), greenSources(pb, 100)),
                    custom(minDomainSize_var_selector(), min_value_selector(), greenSources(pb, 0))
            ));
            //SMF.restartAfterEachSolution(s);
        } else {
            throw new UnsupportedOperationException("Slots must have an homegeneous duration");
        }
    }

    private boolean pure(OldPowerSource src) {
        for (OldPowerSourceSlot s : src.getSlots()) {
            if (s.getRenewablePct() != 100) {
                return false;
            }
        }
        return true;
    }

    private IntVar[] greenSources(OPCP pb, int minPct) {
        int i = 0;
        List<IntVar> v = new ArrayList<>();
        for (OldPowerSource s : pb.getPowerSources()) {
            IntVar[] pwr = pb.getPowerUsage(s);
            for (int t = 0; t < pwr.length; t++) {
                if (s.getSlots()[t].getRenewablePct() >= minPct) {
                    v.add(pwr[t]);
                }
            }
            i++;
        }
        return v.toArray(new IntVar[v.size()]);
    }

    /**
     * Get the usage of brown power for the source.
     * The returned variable is the amount multiplied by the pct. So the real amount
     * should be divided by 100 to consider the ratio.
     *
     * @param pb
     * @param src
     * @return
     */
    private IntVar[] getBrownUsage(OPCP pb, OldPowerSource src) {
        IntVar[] usage = new IntVar[pb.getNbSlots()];
        for (int t = 0; t < usage.length; t++) {
            IntVar totalUsage = pb.getPowerUsage(src)[t];
            int brownPct = 100 - src.getSlots()[t].getRenewablePct();
            usage[t] = VF.scale(totalUsage, brownPct);
        }
        return usage;
    }

    @Override
    public String toString() {
        return "max(brownEnergies)";
    }
}
