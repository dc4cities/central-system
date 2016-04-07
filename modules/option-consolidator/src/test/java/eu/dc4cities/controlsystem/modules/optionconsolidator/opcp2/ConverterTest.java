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
import eu.dc4cities.configuration.goal.interval.TimeIntervalExpression;
import eu.dc4cities.configuration.goal.interval.TimeIntervalExpressionFactory;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.TimeSlotPower;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterPower;
import eu.dc4cities.controlsystem.model.easc.*;
import eu.dc4cities.controlsystem.model.erds.DataCenterForecast;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.model.erds.TimeSlotErdsForecast;
import eu.dc4cities.controlsystem.model.json.JsonUtils;
import eu.dc4cities.controlsystem.model.quantity.EnergyPrice;
import eu.dc4cities.controlsystem.model.unit.Units;
import org.joda.time.DateTime;
import org.jscience.economics.money.Currency;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.measure.quantity.DataAmount;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;
import javax.measure.unit.*;
import java.util.*;

/**
 * Unit tests for {@link Converter}.
 *
 */
public class ConverterTest {

    @Before
    public void setUp() throws Exception {
        Units.init();

    }

    @Test
    public void testBefore() {
        //DataCenterForecast dcFcs, DataCenterPower dcPower, DateTime max
        DateTime now = DateTime.now();

        //Past renewable percentage
        DataCenterForecast dcFcs = new DataCenterForecast("dc1");
        ErdsForecast forecast = new ErdsForecast("grid");
        ErdsForecast pv = new ErdsForecast("pv");
        forecast.setDateFrom(now);
        forecast.setDateTo(now.plusDays(2));
        pv.setDateTo(forecast.getDateTo());
        pv.setDateFrom(forecast.getDateFrom());
        forecast.setTimeSlotDuration(Amount.valueOf(15 * 60, SI.SECOND));
        pv.setTimeSlotDuration(forecast.getTimeSlotDuration());
        List<TimeSlotErdsForecast> fcs = new ArrayList<>();
        List<TimeSlotErdsForecast> pvs = new ArrayList<>();
        for (int i = 0; i < 96 * 2; i++) {
            TimeSlotErdsForecast fc = new TimeSlotErdsForecast(i);
            fc.setPower(Amount.valueOf(3 * i * 4, SI.WATT));
            fc.setRenewablePercentage(Amount.valueOf(i, Units.PERCENTAGE_POINT));
            fc.setConsumptionPrice(null);
            fcs.add(fc);

            TimeSlotErdsForecast p = new TimeSlotErdsForecast(i);
            p.setPower(Amount.valueOf(i * 4, SI.WATT));
            p.setRenewablePercentage(Amount.valueOf(100, Units.PERCENTAGE_POINT));
            p.setConsumptionPrice(null);
            pvs.add(p);

        }
        forecast.setTimeSlotForecasts(fcs);
        pv.setTimeSlotForecasts(pvs);
        dcFcs.setErdsForecasts(Arrays.asList(forecast, pv));

        //past power consumption for the datacenter. Considering 2 power sources
        DataCenterPower dcPower = new DataCenterPower("dc1");
        dcPower.setDateFrom(now);
        dcPower.setDateTo(now.plusDays(1));
        dcPower.setTimeSlotDuration(forecast.getTimeSlotDuration());

        List<TimeSlotPower> pastPower = new ArrayList<>();
        for (int i = 0; i < 96; i++) {
            pastPower.add(new TimeSlotPower(i, Amount.valueOf(3 * i * 4, SI.WATT)));
        }
        dcPower.setPowerValues(pastPower);
        DateTime max = now.plusDays(1);

        List<PowerSource> srcs = Converter.before(dcFcs, dcPower, max);
        Assert.assertEquals(2, srcs.size());
        PowerSource myGrid = srcs.get(0);
        PowerSource myPv = srcs.get(1);
        Assert.assertEquals(dcFcs.getDataCenterName(), myGrid.dcId());
        Assert.assertEquals(dcFcs.getDataCenterName(), myPv.dcId());
        Assert.assertEquals(dcFcs.getErdsForecasts().get(0).getErdsName(), myGrid.name());
        Assert.assertEquals(dcFcs.getErdsForecasts().get(1).getErdsName(), myPv.name());
        Assert.assertEquals(96, myPv.slots().length);
        Assert.assertEquals(96, myGrid.slots().length);
        for (int i = 0; i < myPv.slots().length; i++) {
            Assert.assertEquals(i, myPv.slots()[i].peak());
            Assert.assertEquals(100, myPv.slots()[i].renPct());
            Assert.assertEquals(2 * i, myGrid.slots()[i].peak());
            Assert.assertEquals(i, myGrid.slots()[i].renPct());
        }
    }

    @Test
    public void testMoney() {
        Amount eur = Amount.valueOf(2.5748, Currency.EUR);
        int m = Converter.money(eur);
        Assert.assertEquals(25748, m, 1);
    }
    /*@Test
    public void testBefore() {
        Units.init();
        ErdsForecast forecast = new ErdsForecast("grid");
        DateTime now = DateTime.now();
        forecast.setDateFrom(now);
        forecast.setDateTo(now.plusDays(2));
        forecast.setTimeSlotDuration(Amount.valueOf(15 * 60, SI.SECOND));
        List<TimeSlotErdsForecast> fcs = new ArrayList<>();
        for (int i = 0; i < 96*2; i++) {
            TimeSlotErdsForecast fc = new TimeSlotErdsForecast(i);
            fc.setPower(Amount.valueOf(i * 4, SI.WATT));
            fc.setRenewablePercentage(Amount.valueOf(i, Units.PERCENTAGE_POINT));
            fc.setConsumptionPrice(Amount.valueOf(i * 8, Units.EUR_PER_KWH));
            fcs.add(fc);
        }
        forecast.setTimeSlotForecasts(fcs);
        DateTime max = now.plusDays(1);
        PowerSource src = Converter.before(forecast, max);
        Assert.assertEquals(96, src.slots().length);
        for (int i = 0; i < src.slots().length; i++) {
            Assert.assertEquals(i, src.slots()[i].renPct());
            Assert.assertEquals(i, src.slots()[i].peak()); //not i * 4 due to watt reduction factor
            Assert.assertEquals(i * 800, src.slots()[i].price()); //800 due to conversion to euro-cents
        }
    }*/

    @Test
    public void testErdsForecastConverter() {
        Currency.setReferenceCurrency(Currency.EUR);
        Amount<Dimensionless> a = Amount.valueOf(3, Unit.ONE);
        ErdsForecast forecast = new ErdsForecast("grid");
        forecast.setTimeSlotDuration(Amount.valueOf(15 * 60, SI.SECOND));
        DateTime now = DateTime.now();
        forecast.setDateFrom(now);
        forecast.setDateTo(now.plusDays(2));
        TimeSlotBasedEntity range = new TimeSlotBasedEntity();

        range.setDateFrom(now.plusDays(1));
        range.setDateTo(now.plusDays(2));
        range.setTimeSlotDuration(forecast.getTimeSlotDuration());

        List<TimeSlotErdsForecast> forecasts = new ArrayList<>();
        for (int i = 0; i < 96 * 2; i++) {
            TimeSlotErdsForecast s = new TimeSlotErdsForecast(i);
            s.setPower(Amount.valueOf(i * 1000, SI.WATT));
            s.setRenewablePercentage(Amount.valueOf(i, NonSI.PERCENT));
            s.setTimeSlot(i);
            s.setConsumptionPrice(Amount.valueOf((double) i / 10.0, EnergyPrice.BASE_UNIT));
            forecasts.add(s);
        }
        forecast.setTimeSlotForecasts(forecasts);

        PowerSource src = Converter.powerSource(range, forecast, "dc1");
        Assert.assertEquals("dc1", src.dcId());
        Assert.assertEquals(96, src.slots().length);
        for (int i = 0; i < 96; i++) {
            PowerSourceSlot slot = src.slots()[i];
            Assert.assertEquals((96 + i) * 1000 / 4, slot.peak()); //watt reduction factor
            Assert.assertEquals((96 + i), slot.renPct());
            Assert.assertEquals((96 + i), slot.price());
        }
    }

    @Test
    public void testSplit() {
        TimeSlotPower p = new TimeSlotPower(1);
        p.setPower(Amount.valueOf(80, SI.WATT));
        List<PowerSourceSlot> slots = new ArrayList<>();
        slots.add(new PowerSourceSlot(10, 50, 3));
        slots.add(new PowerSourceSlot(5, 30, 3));
        slots.add(new PowerSourceSlot(10, 20, 3));//spare watts


        List<PowerSourceSlot> res = Converter.split(p, slots);
        Assert.assertEquals(slots.size(), res.size());

        Assert.assertEquals(10, res.get(0).peak());
        Assert.assertEquals(5, res.get(1).peak());
        Assert.assertEquals(5, res.get(2).peak());
        Assert.assertEquals(50, res.get(0).renPct());
        Assert.assertEquals(30, res.get(1).renPct());
        Assert.assertEquals(20, res.get(2).renPct());
    }

    @Test
    public void testConvertDatacenterSpecification() {
        DataCenterSpecification spec = new DataCenterSpecification("dc1");
        ActivityDataCenterMetrics m = new ActivityDataCenterMetrics("dc1");
        m.setWorkingModeName("bar");
        WorkingMode bar = new WorkingMode("bar", 1);
        spec.getWorkingModes().add(bar);
        WorkingMode foo = new WorkingMode("foo", 0);
        spec.getWorkingModes().add(foo);
        foo.getTransitions().add(new Transition("bar", Amount.valueOf(7, Unit.ONE)));
        for (int i = 0; i < 2; i++) {
            PerformanceLevel pl = new PerformanceLevel(Amount.valueOf(i + " Gbit/min"), Amount.valueOf(100 * i, SI.WATT));
            foo.getPerformanceLevels().add(pl);
        }
        spec.setDefaultWorkingMode("foo");

        DatacenterPart p = Converter.datacenterPart(spec, m, false, Amount.valueOf(1 * 60, SI.SECOND));
        Assert.assertEquals(2, p.getWorkingModes().size());
        for (WM w : p.getWorkingModes()) {
            Assert.assertEquals("dc1", w.getDc());
            if (w.name().equals("foo")) {
                Assert.assertEquals(w, p.getDefaultWM());
            }
        }
        Assert.assertEquals("dc1", p.getName());
        Assert.assertEquals("bar", p.currentWM().name());
    }

    @Test
    public void testWorkingModeConverter() {
        UnitFormat.getInstance().label(SI.GIGA(SI.BIT), "Gbit");
        WorkingMode foo = new WorkingMode("foo", 0);
        foo.getTransitions().add(new Transition("bar", Amount.valueOf(7, Unit.ONE)));
        for (int i = 0; i < 2; i++) {
            PerformanceLevel pl = new PerformanceLevel(Amount.valueOf(i + " Gbit/min"), Amount.valueOf(100 * i, SI.WATT));
            foo.getPerformanceLevels().add(pl);
        }
        WM wm = Converter.WM(foo);
        WM wm2 = Converter.WM(foo, Amount.valueOf(15 * 60, SI.SECOND));
        Assert.assertEquals("foo", wm.name());
        Assert.assertEquals(2, wm.perfs().size());
        for (int i = 0; i < wm.perfs().size(); i++) {
            PerfLevel pf = wm.perfs().get(i);
            Assert.assertEquals(i, pf.perf());
            Assert.assertEquals(i * 25, pf.power());
            PerfLevel pf2 = wm2.perfs().get(i);
            Assert.assertEquals(i * 15, pf2.perf());
            Assert.assertEquals(i * 25, pf2.power());

        }
    }

    @Test
    public void testEnergyPrice() throws Exception {
        Currency.setReferenceCurrency(Currency.EUR);
        ProductUnit<EnergyPrice> u = new ProductUnit<>(Units.EUR_PER_KWH);
        Amount<EnergyPrice> p = Amount.valueOf(0.18, u); //18 c€ / Kwh -> 0,018 c€/Wh -> 18 10-2 c€/Wh
        Assert.assertEquals(2, Converter.energyPrice(p)); //FIXME: rounding ? but LB
    }

    @Test
    public void testPriceModifier() {
        Currency.setReferenceCurrency(Currency.EUR);
        Amount<Dimensionless> pct = Amount.valueOf(75, Units.PERCENTAGE_POINT);
        Amount<Money> eur = Amount.valueOf(-1.56, Currency.EUR);


        PriceModifier pm = new PriceModifier(pct, eur);
        Modifier m = Converter.modifier(pm, null);
        Assert.assertEquals(75, m.threshold());
        Assert.assertEquals(-15600, m.penalty());
        Assert.assertTrue(m.isFlat());

        Unit<DataAmount> GBIT = SI.GIGA(SI.BIT);
        UnitFormat.getInstance().label(GBIT, "GBit");
        Unit EUR_PER_GBIT_SEC = Currency.EUR.divide(GBIT.divide(SI.SECOND));
        Amount<?> unSteppedPenalty = Amount.valueOf(-3.12, EUR_PER_GBIT_SEC);
        pm = new PriceModifier(Amount.valueOf("75 GBit / s"), unSteppedPenalty);
        m = Converter.modifier(pm, Amount.valueOf(15, NonSI.MINUTE));
        Assert.assertEquals(75 * 60 * 15, m.threshold());
        Assert.assertFalse(m.isFlat());
        Assert.assertEquals(1, m.step());
        Assert.assertEquals(-31200, m.penalty());
    }

    @Test
    public void testPriceModifier2() {
        /*
          instantBusinessObjective: !amount '1679 Req/s'
          basePrice: !amount '16.79 EUR'
          priceModifiers:
          - threshold: !amount '1679 Req/s'
            modifier: !amount '0 EUR/(Req/s)'
          - threshold: !amount '0 Req/s'
            modifier: !amount '-0.02 EUR/(Req/s)'
         */
        Currency.setReferenceCurrency(Currency.EUR);

        //Linear modifier, expressed as a penalty per request
        PriceModifier pm1 = new PriceModifier(Amount.valueOf("0 Req"), Amount.valueOf("-0.02 EUR/Req"));
        Modifier m = Converter.modifier(pm1, null);
        Assert.assertFalse(m.isFlat());
        Assert.assertEquals(1, m.step());
        Assert.assertEquals(-200, m.penalty());

        //Linear modifier, expressed as a penalty per request rate
        PriceModifier pm2 = new PriceModifier(Amount.valueOf("0 Req/s"), Amount.valueOf("-0.02 EUR/(Req/s)"));
        m = Converter.modifier(pm2, Amount.valueOf(15, NonSI.MINUTE));
        Assert.assertFalse(m.isFlat());
        Assert.assertEquals(1, m.step());
        Assert.assertEquals(-200, m.penalty());
    }

    @Test
    public void testEnergy() {
        Amount<Energy> e = Amount.valueOf(3600, SI.JOULE);
        Assert.assertEquals(1, Converter.energy(e));
    }

    @Test
    public void testToTimeSlot() {
        TimeSlotBasedEntity range = new TimeSlotBasedEntity();
        DateTime from = new DateTime();
        DateTime to = from.plusDays(1);
        Amount<Duration> d = Amount.valueOf(60 * 15, SI.SECOND);
        range.setDateFrom(from);
        range.setDateTo(to);
        range.setTimeSlotDuration(d);
        Assert.assertEquals(0, Converter.timeSlot(range, from));
        Assert.assertEquals(96, Converter.timeSlot(range, to));
        Assert.assertEquals(1, Converter.timeSlot(range, from.plusMinutes(15)));

        //Test past date
        range.setDateFrom(to);
        range.setDateTo(to.plusDays(1));
        Assert.assertEquals(-96, Converter.timeSlot(range, from));
        Assert.assertEquals(-1, Converter.timeSlot(range, to.minusMinutes(15)));
        try {
            Assert.assertEquals(96, Converter.timeSlot(range, from.plusMinutes(10))); //not round
            Assert.fail("Not a real timestamp");
        } catch (IllegalArgumentException ex) {
        }

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

    @Test
    //Hard to test because the nextStartDate() is the next for sure. Even if it is equals to the start date
    public void testRenPctConversion() throws Exception {
        Objective o = renPctObjective(createTimeFrame("PT48H", "0 0 0 ? * SUN", null));
        TimeIntervalExpression interval = TimeIntervalExpressionFactory.create(o.getTimeFrame());
        Date now = new Date();
        DateTime from = new DateTime(interval.getStartDate(now));
        DateTime to = new DateTime(interval.getEndDate(now));
        TimeSlotBasedEntity range = new TimeSlotBasedEntity();
        range.setDateFrom(from);
        range.setDateTo(to);
        range.setTimeSlotDuration(Amount.valueOf(15 * 60, SI.SECOND));

        RenPct dp = (RenPct) Converter.dynCost(o, range, new ArrayList<>()).get(0);
        System.out.println(dp.penalty().pretty(100));
        Penalty pen = dp.penalty();
        Assert.assertEquals(810, pen.basePerf());
        Assert.assertEquals(0, pen.revenue(750));
        Assert.assertEquals(0, pen.revenue(1000));
        Assert.assertEquals(-11 * 10000, pen.revenue(700)); //10000 -> 10-2 euro-cents
        Assert.assertEquals("dc1", dp.datacenterId());
    }

    @Test
    //The scheduling window spread over 2 days, so there should be 2 RenPct objective
    public void testMultipleRenPctConversion() throws Exception {
        TimeFrame tf = createTimeFrame("P1D", "0 0 0 ? * *", new Date());
        TimeIntervalExpression interval = TimeIntervalExpressionFactory.create(tf);
        Objective o = renPctObjective(tf);

        DateTime from = new DateTime(interval.getStartDate(new Date()));
        DateTime to = from.plusDays(2);
        from = from.plusMinutes(30); //we are inside the first range

        TimeSlotBasedEntity range = new TimeSlotBasedEntity();
        range.setDateFrom(from);
        range.setDateTo(to);
        range.setTimeSlotDuration(Amount.valueOf(15 * 60, SI.SECOND));


        //We are at slot 2 of the ongoing objective so need 2 past slots
        PowerSource s = new PowerSource("foo", Utils.makeSlots(new int[]{1, 1}, new int[]{0, 0}));
        List<DynCost> costs = Converter.dynCost(o, range, Arrays.asList(s));
        Assert.assertEquals(2, costs.size());
        DynCost c = costs.get(0);
        Assert.assertEquals(0, ((RenPct) c).from());
        Assert.assertEquals(94, ((RenPct) c).to());

        c = costs.get(1);
        Assert.assertEquals(94, ((RenPct) c).from());
        Assert.assertEquals(190, ((RenPct) c).to());
    }

    private TimeFrame createTimeFrame(String duration, String recurrentExpression, Date startDate) {
        TimeFrame timeFrame = new TimeFrame();
        timeFrame.setDuration(duration);
        timeFrame.setRecurrentExpression(recurrentExpression);
        timeFrame.setStartDate(startDate);
        return timeFrame;
    }

    private void addModifier(List<PriceModifier> modifiers, String threshold, String modifier) {
        PriceModifier pm = new PriceModifier(Amount.valueOf(threshold), Amount.valueOf(modifier));
        modifiers.add(pm);
    }

    @Test
    public void testCumulativeRevenue() {
        TimeSlotBasedEntity range = new TimeSlotBasedEntity();
        DateTime from = new DateTime();
        DateTime to = from.plusDays(1);
        range.setDateFrom(from);
        range.setDateTo(to);
        range.setTimeSlotDuration(Amount.valueOf(15 * 60, SI.SECOND));
        ServiceLevelObjective slo = new ServiceLevelObjective(from, from.plusHours(1));
        slo.setBasePrice(Amount.valueOf(10, Currency.EUR));
        slo.setCumulativeBusinessObjective(Amount.valueOf(100, SI.METERS_PER_SECOND));
        CumulativeRevenue r = Converter.cumulativeRevenue(range, slo, null);
        Assert.assertEquals(SI.METERS_PER_SECOND, r.unit());
        Assert.assertEquals(100000, r.basePrice());
        Assert.assertEquals(100/* * 15 * 60*/, r.basePerf());
        Assert.assertEquals(0, r.from());
        Assert.assertEquals(0, r.base());
        Assert.assertEquals(4, r.to());
    }

    /**
     * Test with a window outside the range
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCumulativeRevenue() {
        TimeSlotBasedEntity range = new TimeSlotBasedEntity();
        DateTime from = new DateTime();
        DateTime to = from.plusHours(1);
        range.setDateFrom(from);
        range.setDateTo(to);
        range.setTimeSlotDuration(Amount.valueOf(15 * 60, SI.SECOND));

        ServiceLevelObjective slo = new ServiceLevelObjective(from, from.plusHours(1));
        slo.setBasePrice(Amount.valueOf(10, Currency.EUR));
        slo.setCumulativeBusinessObjective(Amount.valueOf(100, SI.JOULE));

        //In the past and the future
        slo.setDateFrom(slo.getDateFrom().minusMinutes(30));
        slo.setDateTo(slo.getDateTo().plusMinutes(30));
        ServiceLevel c = new ServiceLevel(slo.getDateFrom(), slo.getDateTo());
        c.setCumulativeBusinessPerformance(Amount.valueOf(10, Unit.ONE));
        Converter.cumulativeRevenue(range, slo, c);
    }

    @Test
    public void testPartialCumulativeRevenue() {
        TimeSlotBasedEntity range = new TimeSlotBasedEntity();
        DateTime from = new DateTime();
        DateTime to = from.plusHours(1);
        range.setDateFrom(from);
        range.setDateTo(to);
        range.setTimeSlotDuration(Amount.valueOf(15 * 60, SI.SECOND));

        ServiceLevelObjective slo = new ServiceLevelObjective(from, from.plusHours(1));
        slo.setBasePrice(Amount.valueOf(10, Currency.EUR));
        slo.setCumulativeBusinessObjective(Amount.valueOf(100, SI.METERS_PER_SECOND));

        //In the past with past data.
        slo.setDateFrom(slo.getDateFrom().minusMinutes(30));
        ServiceLevel c = new ServiceLevel(slo.getDateFrom(), slo.getDateTo());
        c.setCumulativeBusinessPerformance(Amount.valueOf(10, Unit.ONE));
        CumulativeRevenue r = Converter.cumulativeRevenue(range, slo, c);
        Assert.assertEquals(10, r.base());
    }

    @Test
    public void testInstantRevenue() {
        TimeSlotBasedEntity range = new TimeSlotBasedEntity();
        DateTime from = new DateTime();
        DateTime to = from.plusDays(1);
        range.setDateFrom(from);
        range.setDateTo(to);
        range.setTimeSlotDuration(Amount.valueOf(15 * 60, SI.SECOND));
        ServiceLevelObjective slo = new ServiceLevelObjective(from, from.plusHours(1));
        slo.setBasePrice(Amount.valueOf(10, Currency.EUR));
        slo.setInstantBusinessObjective(Amount.valueOf("100 J/s"));
        List<InstantRevenue> l = Converter.instantRevenue(range, slo);
        Assert.assertEquals(4, l.size());
        int i = 0;
        for (InstantRevenue r : l) {
            Assert.assertEquals("J/s", r.unit().toString());
            Assert.assertEquals(i, r.at());
            Assert.assertEquals(100000, r.basePrice());
            Assert.assertEquals(100, r.basePerf());
            i++;
        }
    }

    @Test
    public void testInvalidInstantRevenue() {
        TimeSlotBasedEntity range = new TimeSlotBasedEntity();
        DateTime from = new DateTime();
        DateTime to = from.plusHours(1);
        range.setDateFrom(from);
        range.setDateTo(to);
        range.setTimeSlotDuration(Amount.valueOf(15 * 60, SI.SECOND));

        ServiceLevelObjective slo = new ServiceLevelObjective(from, from.plusHours(1));
        slo.setBasePrice(Amount.valueOf(10, Currency.EUR));
        slo.setInstantBusinessObjective(Amount.valueOf("100 J/s"));

        //30 minutes before and after
        slo.setDateFrom(from.minusMinutes(30));
        slo.setDateTo(to.plusMinutes(30));
        List<InstantRevenue> l = Converter.instantRevenue(range, slo);
        Assert.assertEquals(4, l.size());
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(i, l.get(i).at());
        }

        //15 minutes after and 15 minutes before
        slo.setDateFrom(from.plusMinutes(15));
        slo.setDateTo(to.minusMinutes(15));
        l = Converter.instantRevenue(range, slo);
        Assert.assertEquals(2, l.size());
        for (int i = 0; i < 2; i++) {
            Assert.assertEquals(i + 1, l.get(i).at());
        }
    }

    @Test
    public void testMyActivity() throws Exception {
        Unit<DataAmount> GBIT = SI.GIGA(SI.BIT);
        UnitFormat.getInstance().label(GBIT, "Gbit");
        EascActivitySpecifications ass = JsonUtils.loadResource("easc-activity-specifications.json", EascActivitySpecifications.class);
        EascMetrics mss = JsonUtils.loadResource("easc-metrics.json", EascMetrics.class);
        TimeSlotBasedEntity range = new TimeSlotBasedEntity();
        ActivitySpecification as = ass.getActivitySpecifications().get(0);
        ActivityMetrics am = mss.getActivities().get(0);
        DateTime from = new DateTime("2015-05-10T00:00:00").minusHours(1); //4 Time-slots before the slo start
        DateTime to = from.plusDays(1);
        ServiceLevelObjective slo = as.getServiceLevelObjectives().get(0);
        range.setDateFrom(from);
        range.setDateTo(to);
        slo.setDateFrom(from);
        slo.setDateTo(to);
        range.setTimeSlotDuration(Amount.valueOf(15 * 60, SI.SECOND));

        MyActivity ma = Converter.myActivity(range, ass.getEascName(), as, Collections.<ServiceLevel>emptyList(), am);
        Assert.assertEquals("easc1", ma.easc());
        Assert.assertEquals("Activity 1", ma.name());
        Assert.assertEquals("WM2", ma.datacenterPart("dc1").currentWM().name());
        Assert.assertEquals("WM1", ma.datacenterPart("dc2").currentWM().name());
        Assert.assertEquals(Relocability.NO, ma.relocatibility());
        Assert.assertEquals(2, ma.parts().size());
        for (DatacenterPart p : ma.parts()) {
            if (p.getName().equals("dc1")) {
                Assert.assertEquals(3, p.getWorkingModes().size());
                for (WM wm : p.getWorkingModes()) {
                    if (wm.name().equals("WM0")) {
                        Assert.assertEquals(0, wm.perfs().get(0).perf());
                    } else if (wm.name().equals("WM1")) {
                        for (PerfLevel pl : wm.perfs()) {
                            if (pl.power() == 500 / 4) {
                                Assert.assertEquals(0.25 * 15, pl.perf(), 1); //rounding issue
                            } else if (pl.power() == 665 / 4) {
                                Assert.assertEquals(0.5 * 15, pl.perf(), 1); //rounding issue
                            } else {
                                Assert.fail("Unexpected perf-level " + pl);
                            }
                        }
                    }
                }
            } else if (p.getName().equals("dc2")) {
                Assert.assertEquals(1, p.getWorkingModes().size());
            } else {
                Assert.fail("Unexpected datacenter: " + p.getName());
            }
        }
        Assert.assertEquals(1, ma.cumulativeRevenues().size());
        CumulativeRevenue cr = ma.cumulativeRevenues().get(0);
        Assert.assertEquals(0, cr.from());
        Assert.assertEquals(96, cr.to());
        Assert.assertEquals(750, cr.basePerf());
        Modifier m = cr.modifiers().iterator().next();
        Assert.assertEquals(300, m.threshold());
        List<InstantRevenue> irs = ma.instantRevenues();
        Assert.assertEquals(0, irs.size());
        /*int i = 4;
        for (InstantRevenue r : irs) {
            Assert.assertEquals(i++, r.at());
        }
        System.out.println(ma);*/
    }
}