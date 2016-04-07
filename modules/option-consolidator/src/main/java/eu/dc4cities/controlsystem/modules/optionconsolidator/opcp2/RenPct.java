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

import eu.dc4cities.controlsystem.modules.optionconsolidator.Range;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;

import java.util.ArrayList;
import java.util.List;

/**
 * Cap the usage of renewable source to a given minimum.
 *
 *
 */
public class RenPct implements DynCost, Range {

    private IntVar pct;
    private IntVar penaltyVar;
    private IntVar sumAll, sumGreen;
    private int from, to;
    private Penalty cost;

    private String dcId;

    private List<PowerSource> pastPower;

    private OPCP2 pb;
    /**
     * New constraint.
     *
     * @param dcId the datacenter identifier
     * @param from the time frame lower bound (inclusive)
     * @param to   the time frame upper bound (exclusive)
     * @param p    the associated penaltyVariable function
     */
    public RenPct(String dcId, int from, int to, Penalty p) {
        this(dcId, new ArrayList<>(), from, to, p);
    }

    /**
     * New constraint.
     *
     * @param dcId the datacenter identifier
     * @param pastPowerUsage past usage
     * @param from    the time frame lower bound (inclusive)
     * @param to      the time frame upper bound (exclusive)
     * @param p the associated penaltyVariable function
     */
    public RenPct(String dcId, List<PowerSource> pastPowerUsage, int from, int to, Penalty p) {
        this.dcId = dcId;
        this.from = from;
        this.to = to;
        cost = p;
        pastPower = pastPowerUsage;
    }

    @Override
    public int from() {
        return from;
    }

    @Override
    public int to() {
        return to;
    }

    @Override
    public IntVar inject(OPCP2 pb) {
        this.pb = pb;
        //Sum the ren usage
        if (from > pb.getNbSlots() || to > pb.getNbSlots()) {
            //Out of the scheduling window. Assumed no penalty then
            pb.getLogger().info(this + " is outside the scheduling range. Cost of 0 assumed");
            penaltyVar = VF.fixed(0, pb.getSolver());
            return penaltyVar;
        }
        Solver s = pb.getSolver();

        List<PowerSource> srcs = pb.getSources(this.dcId);
        if (srcs.isEmpty()) {
            throw new IllegalArgumentException("RenPCT objective: Unknown datacentre '" + dcId + "'");
        }
        List<IntVar> green = new ArrayList<>();
        List<IntVar> all = new ArrayList<>();

        for (int p : pb.powerSources(srcs)) {
            List<IntVar> totalRen = makeGreenUsage(pb, p);
            List<IntVar> total = makeTotalUsage(pb, p);
            green.addAll(totalRen);
            all.addAll(total);
        }

        pct = VF.bounded("ren%", 0, 10000, s); // /!\ 777 -> 77.7%
        sumAll = pb.sum("allPower", all);
        sumGreen = pb.sum("allGreenPower", green);
        s.post(ICF.eucl_div(sumGreen, sumAll, pct));
        //apply the SLO
        int[] x = cost.flatten(10000); //O to 100.0%
        penaltyVar = VF.bounded("penaltyVariable(renPct)", x[0], x[x.length - 1], s);
        OPCP2.among(s, penaltyVar, x, pct);
        return penaltyVar;
    }

    private List<IntVar> makeTotalUsage(OPCP2 pb, int pId) {
        int ub = 0;
        int past = 0;
        for (PowerSource src : pastPower) {
            if (src.dcId().equals(datacenterId())) {
                for (PowerSourceSlot slot : src.slots()) {
                    past += slot.peak();
                }
            }
        }

        List<IntVar> all = new ArrayList<>();
        for (int t = from; t < to; t++) {
            IntVar usage = pb.getSourcePowerUsage()[pId][t];
            all.add(usage);
            ub += usage.getUB();
        }

        ub += past;
        all.add(VF.fixed("pastPower(dc=" + dcId + ")", past, pb.getSolver()));
        if (ub < 0 || ub > Integer.MAX_VALUE - 1) {
            pb.getLogger().error("Sum of total usage for source '" + pb.getSources().get(pId) + "' can overflow !");
        }
        return all;
    }

    private List<IntVar> makeGreenUsage(OPCP2 pb, int pId) {
        int ub = 0;
        int pastRen = 0;
        for (PowerSource src : pastPower) {
            if (src.dcId().equals(datacenterId())) {
                for (PowerSourceSlot slot : src.slots()) {
                    pastRen += slot.peak() * slot.renPct() * 10;
                }
            }
        }

        PowerSource src = pb.getSources().get(pId);
        List<IntVar> all = new ArrayList<>();
        for (int t = from; t < to; t++) {
            IntVar cons = pb.getSourcePowerUsage()[pId][t];
            IntVar usage = VF.scale(cons, src.slots()[t].renPct() * 10);
            all.add(usage);
            ub += usage.getUB();
        }

        all.add(VF.fixed("pastRenPower(dc=" + dcId + ")", pastRen, pb.getSolver()));
        ub += pastRen;

        if (ub < 0 || ub > Integer.MAX_VALUE - 1) {
            pb.getLogger().error("Sum of total usage for source '" + src.name() + "' can overflow !");
        }
        return all;
    }

    /**
     * Get the percentage.
     *
     * @return a variable between 0and 1000 (/!\denoting 100.0%)
     */
    public IntVar percentage() {
        return pct;
    }

    public IntVar penaltyVariable() {
        return penaltyVar;
    }

    public Penalty penalty() {
        return cost;
    }

    public String datacenterId() {
        return dcId;
    }

    @Override
    public String pretty() {

        return "RenPct{" + (!pastPower.isEmpty() ? pastPower.get(0).slots().length : 0) + "+[" + from + ";" + to + "[; dc=" + dcId + "; " + (1.0 * sumGreen.getValue() / 10.0) + "/" + sumAll.getValue() + "=" + ((1.0 * pct.getValue()) / 10.0) + "% (>=" + (1.0 * cost.basePerf() / 10.0) + "% expected); penalty=" + Converter.toEuros(penaltyVar.getValue()) + "}";
    }

    @Override
    public String toString() {
        return "RenPct{" + (!pastPower.isEmpty() ? pastPower.get(0).slots().length : 0) + "+[" + from + ";" + to + "[; dc=" + dcId + "; " + pct + "%; penalty=" + penaltyVar + '}';
    }

    @Override
    public void store(Statistics st) {
        st.setObjective("renPct", 1.0 * pb.val(pct) / 10);
    }

    public int basePerf() {
        return penalty().basePerf() / 10;
    }

    public int basePrice() {
        return penalty().basePenalty() * 10;
    }

    public boolean entailed() {
        return percentage().getLB() / 10 >= basePerf();
    }
}
