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

package eu.dc4cities.controlsystem.modules.optionconsolidator;

import eu.dc4cities.configuration.goal.Goal;
import eu.dc4cities.configuration.goal.GoalConfiguration;
import eu.dc4cities.configuration.goal.Objective;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.easc.*;
import eu.dc4cities.controlsystem.model.erds.DataCenterForecast;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.model.erds.TimeSlotErdsForecast;
import eu.dc4cities.controlsystem.model.json.JsonUtils;
import eu.dc4cities.controlsystem.model.unit.Units;
import eu.dc4cities.controlsystem.modules.ConsolidatorException;
import eu.dc4cities.controlsystem.modules.OptionConsolidator;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.measure.quantity.DataAmount;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Unit tests for {@link OptionConsolidatorImpl} related to the Phase-2 approach
 *
 *
 */
public class OptionConsolidatorImpl2Test {

    private TimeSlotBasedEntity makeTimeRange() {
        TimeSlotBasedEntity timeRange = new TimeSlotBasedEntity();
        DateTime from = new DateTime();
        timeRange.setDateFrom(from);
        timeRange.setDateTo(from.plusDays(1));
        timeRange.setTimeSlotDuration(Amount.valueOf(15 * 60, SI.SECOND));
        return timeRange;
    }

    @Test(expected = ConsolidatorException.class)
    public void testInconsistentDurations() throws ConsolidatorException {
        OptionConsolidator oc = new OptionConsolidatorImpl();
        ErdsForecast f1 = new ErdsForecast("grid");
        f1.setTimeSlotForecasts(new ArrayList<TimeSlotErdsForecast>());
        f1.setTimeSlotDuration(Amount.valueOf(10, SI.SECOND));

        ErdsForecast f2 = new ErdsForecast("pv");
        f2.setTimeSlotForecasts(new ArrayList<TimeSlotErdsForecast>());
        f2.setTimeSlotDuration(Amount.valueOf(30, SI.SECOND));

        oc.selectOptionPlans(makeTimeRange(), Arrays.asList(f1, f2), Collections.<EascOptionPlan>emptyList());
    }

    @Ignore
    public void testSolve() throws ConsolidatorException {
        OptionConsolidator oc = new OptionConsolidatorImpl();
        TimeSlotBasedEntity timeRange = makeTimeRange();
        int nbSlots = 96;
        int eascs = 50;
        Amount<Duration> slotDuration = Amount.valueOf(60 * 15, SI.SECOND);

        ErdsForecast grid = makeGrid(nbSlots, 500000, slotDuration); //500 kWatt
        ErdsForecast pv = makePv(nbSlots, 5000, slotDuration);
        List<ErdsForecast> forecasts = Arrays.asList(grid, pv);

        List<EascOptionPlan> options = new ArrayList<>();

        for (int i = 0; i < eascs; i++) {
            EascOptionPlan op = makeOptionPlan("easc" + (i + 1), timeRange.getDateFrom(), nbSlots, slotDuration);
            options.add(op);
        }
        oc.setTimeout(10);
        List<EascActivityPlan> res = oc.selectOptionPlans(timeRange, forecasts, options);
        Assert.assertFalse(res.isEmpty());
        System.out.println(res);
        /*EascActivityPlan resP1 = res.get(0);
        System.out.println(resP1);
        Assert.assertEquals(resP1.getActivities().get(0).getWorks().get(0).getWorkingMode(), "safe 0");
        Assert.assertEquals(resP1.getActivities().get(0).getWorks().get(1).getWorkingMode(), "safe 2");*/
    }

    private static Random rnd = new Random();

    private static EascOptionPlan makeOptionPlan(String eascName, DateTime from, int nbSlots, Amount<Duration> slotDuration) {
        EascOptionPlan p = new EascOptionPlan(eascName);
        p.setDateFrom(from);
        p.setTimeSlotDuration(slotDuration);
        p.setActivityOptions(new ArrayList<ActivityOption>());
        ActivityOption ao = new ActivityOption("o1");
        ao.setOptionPlans(new ArrayList<OptionPlan>());

        p.addActivityOption(ao);

        OptionPlan p1 = new OptionPlan();
        ao.addOptionPlan(p1);

        OptionPlan p2 = new OptionPlan();
        ao.addOptionPlan(p2);

        OptionPlan p3 = new OptionPlan();
        ao.addOptionPlan(p3);

        int offset = 1;
        while (offset < nbSlots - 1) {
            int d = rnd.nextInt(5);
            if (offset + d >= nbSlots) {
                d = nbSlots - offset - 1;
            }
            WorkOption wo = new WorkOption();
            wo.setStartTimeSlot(offset);
            wo.setEndTimeSlot(offset + d);
            //3 working modes
            //2 mode, a safe, and a greedy one for each period of 2 time-slot
            ModeOption safe = new ModeOption("safe " + offset, Amount.valueOf(rnd.nextInt(500), SI.WATT), 33);
            ModeOption greedy = new ModeOption("greedy " + offset, Amount.valueOf(rnd.nextInt(1000), SI.WATT), 33);
            ModeOption omega = new ModeOption("omega " + offset, Amount.valueOf(rnd.nextInt(2000), SI.WATT), 33);

            p1.addWorkOption(makeWorkOption(offset, offset + d, safe));
            p2.addWorkOption(makeWorkOption(offset, offset + d, greedy));
            p3.addWorkOption(makeWorkOption(offset, offset + d, omega));
            offset += d;
        }
        return p;
    }

    private static WorkOption makeWorkOption(int from, int to, ModeOption... opts) {
        WorkOption o = new WorkOption();
        o.setStartTimeSlot(from);
        o.setEndTimeSlot(to);
        for (ModeOption opt : opts) {
            o.addModeOption(opt);
        }
        return o;
    }
    private static ErdsForecast makeGrid(int nbSlots, int capa, Amount<Duration> sd) {
        ErdsForecast grid = new ErdsForecast("grid");
        grid.setTimeSlotDuration(sd);
        grid.setTimeSlotForecasts(new ArrayList<TimeSlotErdsForecast>());

        for (int i = 0; i < nbSlots; i++) {
            TimeSlotErdsForecast s = new TimeSlotErdsForecast(1);
            s.setPower(Amount.valueOf(capa, SI.WATT));
            s.setRenewablePercentage(Amount.valueOf(0, Unit.ONE));
            s.setCo2Factor(Amount.valueOf(0, Units.KG_PER_KWH));
            grid.getTimeSlotForecasts().add(s);
        }
        return grid;
    }

    private static ErdsForecast makePv(int nbSlots, int to, Amount<Duration> sd) {
        ErdsForecast pv = new ErdsForecast("pv");
        pv.setTimeSlotForecasts(new ArrayList<TimeSlotErdsForecast>());
        pv.setTimeSlotDuration(sd);

        int step = to / (nbSlots / 2);
        int p = 0;
        for (int i = 0; i < nbSlots; i++) {
            TimeSlotErdsForecast s = new TimeSlotErdsForecast(i);
            s.setPower(Amount.valueOf(p, SI.WATT));
            s.setRenewablePercentage(Amount.valueOf(100, Unit.ONE));
            s.setCo2Factor(Amount.valueOf(0, Units.KG_PER_KWH));
            pv.getTimeSlotForecasts().add(s);

            if (i < nbSlots / 2) {
                p += step;
            } else {
                p -= step;
            }
        }
        return pv;
    }

    /**
     * Fix bugs due to activities that are shorter than the time-range
     *
     * @throws Exception
     */
    @Ignore
    public void testWithShorterActivities() throws Exception {
        String root = "src/test/resources/";

        ErdsForecast erds1 = JsonUtils.load(new FileInputStream(root + "forecast-erds1.json"), ErdsForecast.class);
        ErdsForecast erds2 = JsonUtils.load(new FileInputStream(root + "forecast-erds2.json"), ErdsForecast.class);
        EascOptionPlan plan = JsonUtils.load(new FileInputStream(root + "optionplan.json"), EascOptionPlan.class);
        TimeSlotBasedEntity tr = JsonUtils.load(new FileInputStream(root + "timerange.json"), TimeSlotBasedEntity.class);
        OptionConsolidator oc = new OptionConsolidatorImpl();
        List<EascActivityPlan> res = oc.selectOptionPlans(tr, Arrays.asList(erds1, erds2), Arrays.asList(plan));
        Assert.assertEquals(1, res.size());
    }

    @Ignore
    public void testBuildActivityPlans() throws IOException {
        Units.init();
        Unit<DataAmount> GBIT = SI.GIGA(SI.BIT);
        UnitFormat.getInstance().label(GBIT, "Gbit");
        Unit EUR_PER_GBIT = org.jscience.economics.money.Currency.EUR.divide(GBIT);

        DataCenterForecast d1Forecasts = loadJson("dc1-forecasts", DataCenterForecast.class);
        DataCenterForecast d2Forecasts = loadJson("dc2-forecasts", DataCenterForecast.class);
        GoalConfiguration goals = loadJson("goal-configuration", GoalConfiguration.class);
        List<Objective> objectives = new ArrayList<>();
        for (Goal g : goals.getGoals()) {
            objectives.addAll(g.getObjectives());
        }
        EascActivitySpecifications specs = loadJson("easc-spec", EascActivitySpecifications.class);
        EascPowerPlan budget = loadJson("easc-power-plans", EascPowerPlan.class);
        EascMetrics metrics = loadJson("easc-metrics", EascMetrics.class);
        TimeSlotBasedEntity range = loadJson("time-parameters", TimeSlotBasedEntity.class);

        OptionConsolidatorImpl oc = new OptionConsolidatorImpl();
        List<EascActivityPlan> plans = oc.buildActivityPlans(range,
                objectives,
                Arrays.asList(d1Forecasts, d2Forecasts),
                null,
                Arrays.asList(budget),
                Arrays.asList(specs),
                Arrays.asList(),
                Arrays.asList(),
                Arrays.asList(metrics)
        );
        Assert.assertEquals("easc1", plans.get(0).getEascName());
        Activity p = plans.get(0).getActivities().get(0);
        Assert.assertEquals("Activity 1", plans.get(0).getActivities().get(0).getName());
        for (ActivityDataCenter aDc : p.getDataCenters()) {
            if (aDc.getDataCenterName().equals("dc1")) {
                Work w1 = aDc.getWorks().get(0);
                Assert.assertEquals(0, w1.getStartTimeSlot());
                Assert.assertEquals(1, w1.getEndTimeSlot());
                Assert.assertEquals(0, w1.getWorkingModeValue());
                Assert.assertEquals("WM0", w1.getWorkingModeName());
                Assert.assertEquals(100, w1.getPower().longValue(SI.WATT));

                Work w2 = aDc.getWorks().get(1);
                Assert.assertEquals(1, w2.getStartTimeSlot());
                Assert.assertEquals(2, w2.getEndTimeSlot());
                Assert.assertEquals(0, w2.getWorkingModeValue());
                Assert.assertEquals("WM0", w2.getWorkingModeName());
                Assert.assertEquals(100, w2.getPower().longValue(SI.WATT));

            } else if (aDc.getDataCenterName().equals("dc2")) {
                Work w1 = aDc.getWorks().get(0);
                Assert.assertEquals(0, w1.getStartTimeSlot());
                Assert.assertEquals(1, w1.getEndTimeSlot());
                Assert.assertEquals(1, w1.getWorkingModeValue());
                Assert.assertEquals("WM1", w1.getWorkingModeName());
                Assert.assertEquals(200, w1.getPower().longValue(SI.WATT));

                Work w2 = aDc.getWorks().get(1);
                Assert.assertEquals(1, w2.getStartTimeSlot());
                Assert.assertEquals(2, w2.getEndTimeSlot());
                Assert.assertEquals(1, w2.getWorkingModeValue());
                Assert.assertEquals("WM1", w2.getWorkingModeName());
                Assert.assertEquals(200, w2.getPower().longValue(SI.WATT));
            } else {
                Assert.fail();
            }
        }
        ServiceLevel sla = p.getServiceLevels().get(0);
        Assert.assertEquals(GBIT, sla.getCumulativeBusinessPerformance().getUnit());
        System.out.println(plans.get(0));
        System.out.println(sla);
        Assert.assertEquals(20, sla.getCumulativeBusinessPerformance().getExactValue());
        System.out.println(oc.getLastStatistics());
    }

    private <T> T loadJson(String name, Class<T> clazz) {
        return JsonUtils.loadResource(name + ".json", clazz);
    }

}

