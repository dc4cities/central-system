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

import eu.dc4cities.configuration.goal.Objective;
import eu.dc4cities.configuration.goal.ObjectiveType;
import eu.dc4cities.configuration.goal.Target;
import eu.dc4cities.configuration.goal.TimeFrame;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.TimeSlotPower;
import eu.dc4cities.controlsystem.model.easc.*;
import eu.dc4cities.controlsystem.model.erds.DataCenterForecast;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.model.erds.TimeSlotErdsForecast;
import eu.dc4cities.controlsystem.model.json.JsonUtils;
import eu.dc4cities.controlsystem.model.unit.Units;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.reducer.Pass;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;
import org.junit.Assert;
import org.junit.Test;

import javax.measure.quantity.DataAmount;
import javax.measure.quantity.Duration;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by fhermeni on 29/09/2015.
 */
public class SplitterTest {

    private void addModifier(List<PriceModifier> modifiers, String threshold, String modifier) {
        PriceModifier pm = new PriceModifier(Amount.valueOf(threshold), Amount.valueOf(modifier));
        modifiers.add(pm);
    }

    private Objective renPctObjective(TimeFrame tf) {
        Objective o = new Objective();
        o.setDataCenterId("dc1");
        o.setId("greenObjective");
        o.setType("ENERGY_PROPERTY");
        o.setImplementationType("MUST");
        o.setEnabled(true);
        o.setType(ObjectiveType.ENERGY);
        o.setPriority(0);
        Target t = new Target();
        t.setMetric("renewablePercentage");
        t.setValue(80);
        t.setOperator("gt");
        o.setTarget(t);
        o.setTimeFrame(tf);
        Units.init();
        List<PriceModifier> priceModifiers = new ArrayList<>(2);
        addModifier(priceModifiers, "75 %", "0 EUR");
        addModifier(priceModifiers, "0 %", "-1 EUR/pp");
        o.setPriceModifiers(priceModifiers);
        return o;
    }

    private TimeFrame createTimeFrame(String duration, String recurrentExpression, Date startDate) {
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setDuration(duration);
        timeFrame.setRecurrentExpression(recurrentExpression);
        timeFrame.setStartDate(startDate);
        return timeFrame;
    }

    @Test
    public void testSplittableBudget() {
        EascPowerPlan p = new EascPowerPlan();
        DateTime now = new DateTime();
        DateTime start = now.withTimeAtStartOfDay();
        DateTime mid = start.plusDays(1);
        DateTime end = start.plusDays(2);
        p.setTimeSlotDuration(Amount.valueOf(15, NonSI.MINUTE));
        p.setDateFrom(start);
        p.setDateTo(end);
        //2 days quota inside a DC
        DataCenterPowerPlan dc = new DataCenterPowerPlan("foo");
        p.setDataCenterQuotas(Arrays.asList(dc));
        dc.setPowerQuotas(new ArrayList<>());
        for (int i = 0; i < 192; i++) {
            dc.getPowerQuotas().add(new TimeSlotPower(i, Amount.valueOf(i < 96 ? 1 : 2, SI.WATT)));
        }
        dc.setEnergyQuotas(new ArrayList<>());
        dc.getEnergyQuotas().add(new EnergyQuota(0, 96, Amount.valueOf(1, Units.WATT_HOUR)));
        dc.getEnergyQuotas().add(new EnergyQuota(96, 192, Amount.valueOf(2, Units.WATT_HOUR)));
        Assert.assertTrue(Splitter.splittableBudgets(Arrays.asList(p), mid));
        Assert.assertFalse(Splitter.splittableBudgets(Arrays.asList(p), mid.minusMinutes(15)));
    }

    @Test
    public void testSplitBudget() {
        EascPowerPlan p = new EascPowerPlan();
        p.setEascName("ee");
        DateTime now = new DateTime();
        DateTime start = now.withTimeAtStartOfDay();
        DateTime mid = start.plusDays(1);
        DateTime end = start.plusDays(2);
        p.setTimeSlotDuration(Amount.valueOf(15, NonSI.MINUTE));
        p.setDateFrom(start);
        p.setDateTo(end);
        //2 days quota inside a DC
        DataCenterPowerPlan dc = new DataCenterPowerPlan("foo");
        p.setDataCenterQuotas(Arrays.asList(dc));
        dc.setPowerQuotas(new ArrayList<>());
        dc.setTimeSlotDuration(p.getTimeSlotDuration());
        for (int i = 0; i < 192; i++) {
            dc.getPowerQuotas().add(new TimeSlotPower(i, Amount.valueOf(i < 96 ? 1 : 2, SI.WATT)));
        }
        dc.setEnergyQuotas(new ArrayList<>());
        dc.getEnergyQuotas().add(new EnergyQuota(0, 96, Amount.valueOf(1, Units.WATT_HOUR)));
        dc.getEnergyQuotas().add(new EnergyQuota(96, 192, Amount.valueOf(2, Units.WATT_HOUR)));

        //1st day
        List<EascPowerPlan> sub = Splitter.subBudget(Arrays.asList(p), start, mid);
        Assert.assertEquals(1, sub.size());

        EascPowerPlan pp = sub.get(0);
        Assert.assertEquals("ee", pp.getEascName());
        Assert.assertEquals(start, pp.getDateFrom());
        Assert.assertEquals(mid, pp.getDateTo());
        Assert.assertEquals(p.getTimeSlotDuration(), pp.getTimeSlotDuration());
        DataCenterPowerPlan dd = pp.getDataCenterQuotas().get(0);
        Assert.assertEquals("foo", dd.getDataCenterName());
        Assert.assertEquals(96, dd.getPowerQuotas().size());
        for (int i = 0; i < 96; i++) {
            Assert.assertEquals(i, dd.getPowerQuotas().get(i).getTimeSlot());
            Assert.assertEquals(Amount.valueOf(1, SI.WATT), dd.getPowerQuotas().get(i).getPower());
        }
        EnergyQuota q = dd.getEnergyQuotas().get(0);
        Assert.assertEquals(1, dd.getEnergyQuotas().size());
        Assert.assertEquals(0, q.getStartTimeSlot());
        Assert.assertEquals(96, q.getEndTimeSlot());
        Assert.assertEquals(Amount.valueOf(1, Units.WATT_HOUR), q.getEnergy());

        //2nd day
        sub = Splitter.subBudget(Arrays.asList(p), mid, end);
        Assert.assertEquals(1, sub.size());

        pp = sub.get(0);
        Assert.assertEquals("ee", pp.getEascName());
        Assert.assertEquals(mid, pp.getDateFrom());
        Assert.assertEquals(end, pp.getDateTo());
        Assert.assertEquals(p.getTimeSlotDuration(), pp.getTimeSlotDuration());
        dd = pp.getDataCenterQuotas().get(0);
        Assert.assertEquals("foo", dd.getDataCenterName());
        Assert.assertEquals(96, dd.getPowerQuotas().size());
        for (int i = 0; i < 96; i++) {
            Assert.assertEquals(i, dd.getPowerQuotas().get(i).getTimeSlot());
            Assert.assertEquals(Amount.valueOf(2, SI.WATT), dd.getPowerQuotas().get(i).getPower());
        }
        q = dd.getEnergyQuotas().get(0);
        Assert.assertEquals(1, dd.getEnergyQuotas().size());
        Assert.assertEquals(0, q.getStartTimeSlot());
        Assert.assertEquals(96, q.getEndTimeSlot());
        Assert.assertEquals(Amount.valueOf(2, Units.WATT_HOUR), q.getEnergy());
    }

    @Test
    public void testSplitableSLOs() throws Exception {
        List<ServiceLevelObjective> slos = new ArrayList<>();

        DateTime now = new DateTime();
        DateTime start = now.withTimeAtStartOfDay();
        DateTime mid = start.plusDays(1);
        DateTime end = start.plusDays(2);

        ServiceLevelObjective s1 = new ServiceLevelObjective();
        s1.setDateFrom(start);
        s1.setDateTo(mid);

        ServiceLevelObjective s2 = new ServiceLevelObjective();
        s2.setDateFrom(mid);
        s2.setDateTo(end);
        slos.add(s1);
        slos.add(s2);
        Assert.assertTrue(Splitter.splittableSLOs(slos, mid));
//        Assert.assertFalse(Splitter.splittableSLOs(slos, mid.minusMinutes(15)));
    }

    @Test
    public void testSplit() throws Exception {
        /*
            public static List<Scheduler> split(TimeSlotBasedEntity range, List<Objective> objectives,
                                          List<DataCenterForecast> forecasts, List<EascPowerPlan> budgets,
                                          List<EascActivitySpecifications> specs,
                                          List<DataCenterPower> pastPowerUsage, List<EascServiceLevels> pastSLOs,
                                          List<EascMetrics> eascMetrics, Reducer r) throws TimeIntervalExpressionException {
         */

        //spec
        Unit<DataAmount> GBIT = SI.GIGA(SI.BIT);
        UnitFormat.getInstance().label(GBIT, "Gbit");
        EascActivitySpecifications cEasc = JsonUtils.loadResource("easc-activity-specifications-2d.json", EascActivitySpecifications.class);
        EascActivitySpecifications iEasc = JsonUtils.loadResource("easc-activity-specifications-2d-instant.json", EascActivitySpecifications.class);


        //DateTime now = new DateTime();
        DateTime start = cEasc.getActivitySpecifications().get(0).getServiceLevelObjectives().get(0).getDateFrom();
        DateTime end = start.plusDays(2);
        Amount<Duration> d = Amount.valueOf(15, NonSI.MINUTE);
        System.out.println(start);
        TimeSlotBasedEntity range = new TimeSlotBasedEntity();
        range.setDateFrom(start);
        range.setDateTo(end);
        range.setTimeSlotDuration(d);

        //1 objective, per day
        List<Objective> objectives = new ArrayList<>();
        Objective o = renPctObjective(createTimeFrame("P1D", "0 0 0 ? * *", start.toDate()));
        Objective o2d = renPctObjective(createTimeFrame("P12H", "0 0 0 ? * *", start.toDate()));
        //2 days forecasts
        ErdsForecast fc = new ErdsForecast("dc");
        fc.setDateFrom(start);
        fc.setDateTo(end);
        fc.setTimeSlotDuration(d);
        fc.setTimeSlotForecasts(new ArrayList<>());
        for (int i = 0; i < 96 * 2; i++) {
            TimeSlotErdsForecast ts = new TimeSlotErdsForecast(i);
            ts.setRenewablePercentage(Amount.valueOf(10, Units.PERCENTAGE_POINT));
            ts.setConsumptionPrice(Amount.valueOf(0.16, Units.EUR_PER_KWH));
            ts.setPower(Amount.valueOf(5000, SI.WATT));
            ts.setTimeSlot(i);
            fc.getTimeSlotForecasts().add(ts);
        }
        DataCenterForecast dFc = new DataCenterForecast("dc");
        dFc.setDateFrom(start);
        dFc.setDateTo(end);
        dFc.setTimeSlotDuration(d);
        dFc.setErdsForecasts(Arrays.asList(fc));
        //daily budgets
        List<EascPowerPlan> budgets = new ArrayList<>();

        List<Scheduler> subs = Splitter.split(range, Arrays.asList(), Arrays.asList(dFc), new ArrayList<>(), budgets, Arrays.asList(cEasc), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new Pass());
        Assert.assertEquals(2, subs.size());
        for (Scheduler s : subs) {
            System.out.println(s.getRange());
        }

        //No objectives, instant SLO, 96 * 2 splits
        subs = Splitter.split(range, Arrays.asList(), Arrays.asList(dFc), new ArrayList<>(), budgets, Arrays.asList(iEasc), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new Pass());
        Assert.assertEquals(96 * 2, subs.size());

        //objective, instant SLO, 2 splits
        subs = Splitter.split(range, Arrays.asList(o), Arrays.asList(dFc), new ArrayList<>(), budgets, Arrays.asList(iEasc), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new Pass());
        Assert.assertEquals(2, subs.size());

        //No objective, cumulative SLO, 2 splits
        subs = Splitter.split(range, Arrays.asList(), Arrays.asList(dFc), new ArrayList<>(), budgets, Arrays.asList(cEasc), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new Pass());
        Assert.assertEquals(2, subs.size());

        // objective, cumulative SLO, 2 splits
        subs = Splitter.split(range, Arrays.asList(o), Arrays.asList(dFc), new ArrayList<>(), budgets, Arrays.asList(cEasc), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new Pass());
        Assert.assertEquals(2, subs.size());

        //2d objective, cumulative SLOs, 1 split
        /*System.out.println("--");
        subs = Splitter.split(range, Arrays.asList(o2d), Arrays.asList(dFc), budgets, Arrays.asList(iEasc), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new Pass());
        Assert.assertEquals(4,subs.size());*/
        //1d objective, cumulative SLOs, 2 splits
    }
}