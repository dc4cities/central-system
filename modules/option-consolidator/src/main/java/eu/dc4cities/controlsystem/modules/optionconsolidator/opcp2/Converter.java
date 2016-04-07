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
import eu.dc4cities.configuration.goal.interval.TimeIntervalExpressionException;
import eu.dc4cities.configuration.goal.interval.TimeIntervalExpressionFactory;
import eu.dc4cities.controlsystem.model.TimeSlotBasedEntity;
import eu.dc4cities.controlsystem.model.TimeSlotPower;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterPower;
import eu.dc4cities.controlsystem.model.easc.*;
import eu.dc4cities.controlsystem.model.erds.DataCenterForecast;
import eu.dc4cities.controlsystem.model.erds.ErdsForecast;
import eu.dc4cities.controlsystem.model.erds.TimeSlotErdsForecast;
import eu.dc4cities.controlsystem.model.quantity.EnergyPrice;
import eu.dc4cities.controlsystem.model.unit.Units;
import eu.dc4cities.controlsystem.model.util.AmountUtils;
import org.joda.time.DateTime;
import org.jscience.economics.money.Currency;
import org.jscience.economics.money.Money;
import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import javax.measure.unit.NonSI;
import javax.measure.unit.ProductUnit;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helpers to perform the necessary conversions needed
 * to use the {@link OPCP2}.
 *
 * Watt are sampled by the {@link #WATT_REDUCTION_FACTOR}. With time-slot of 15 minutes, this means
 * the power value associated to a time-slot denotes also the amount of Watt/hours
 *
 */
public class Converter {

	private static final Logger logger = LoggerFactory.getLogger(Converter.class);
	
    // price is expressed in euro/Kwh with values around 0.10 and 0.20 (so a few cents)
    // to have integer values, I must then convert at least to euro-cents
    // BUT, energy consumption is in terms of Watt/h so,
    // 16c€ / kWh --> 0,016 c€ / Wh which is not an integer sadly
    // this means, price should be computed in ... 10-2 c€
    // this limits the maximum price to 2B 10-2 cE, so 2M c€ so 20,000€
    // this seems to fit DC4Cities bounds.

    /**
     * Utility class.
     */
    private Converter() {
    }

    //Move to cents
    public static final int EUR_TO_INT = 100 /* to have cents */ * 100 /* 10-2 cents */;

    public static final int WATT_REDUCTION_FACTOR = 4;


    /**
     * Convert a forecast to a power source.
     * Slots before and after the scheduling window are ignored
     * @param range the scheduling window
     * @param fc the forecast to convert
     * @param dcId the datacenter name
     * @return the resulting PowerSource
     */
    public static PowerSource powerSource(TimeSlotBasedEntity range, ErdsForecast fc, String dcId) {
        List<PowerSourceSlot> slots = new ArrayList<>();
        DateTime start = range.getDateFrom();
        DateTime end = range.getDateTo();
        for (TimeSlotErdsForecast f : fc.getTimeSlotForecasts()) {
            DateTime from = Converter.timeSlot(fc, f.getTimeSlot());
            if ((from.equals(start) || from.isAfter(start)) && (from.isBefore(end))) {
                slots.add(powerSourceSlot(f));
            }
        }
        return new PowerSource(fc.getErdsName(), slots.toArray(new PowerSourceSlot[slots.size()])).dcId(dcId);
    }

    /**
     * Convert a forecasted slot.
     *
     * @param tc the slot to forecast
     * @return the resulting conversion
     */
    public static PowerSourceSlot powerSourceSlot(TimeSlotErdsForecast tc) {
        return new PowerSourceSlot(power(tc.getPower()), pct(tc.getRenewablePercentage()), energyPrice(tc.getConsumptionPrice()));
    }

    public static List<PowerSourceSlot> split(TimeSlotPower pow, List<PowerSourceSlot> slots) {
        int usage = power(pow.getPower());
        List<PowerSourceSlot> res = new ArrayList<>();
        int renPct = 120;
        for (PowerSourceSlot s : slots) {
            PowerSourceSlot ss;
            if (s.renPct() > renPct) {
                throw new IllegalArgumentException("Slots should be ordered asc. against ren%");
            }
            if (s.peak() < usage) {
                ss = new PowerSourceSlot(s.peak(), s.renPct(), 0);
                usage -= s.peak();
            } else {
                ss = new PowerSourceSlot(usage, s.renPct(), 0);
                usage = 0;
            }
            res.add(ss);
        }
        if (usage != 0) {
            throw new UnsupportedOperationException("Unable to split the past power correctly: " + usage + " Watts remaining");
        }
        return res;
    }

    private static PowerSource before(ErdsForecast fc, DateTime max) {
        List<PowerSourceSlot> slots = fc.getTimeSlotForecasts().stream()
                .filter(t -> {
                    return Converter.timeSlot(fc, t.getTimeSlot()).isBefore(max);
                })
                .map(t -> {
                    return new PowerSourceSlot(power(t.getPower()), pct(t.getRenewablePercentage()), 0);
                }).collect(Collectors.toList());
        return new PowerSource(fc.getErdsName(), slots.toArray(new PowerSourceSlot[slots.size()]));
    }

    /**
     * @param dcFcs
     * @param dcPower
     * @param max     the deadline (excluded)
     * @return
     */
    public static List<PowerSource> before(DataCenterForecast dcFcs, DataCenterPower dcPower, DateTime max) {
        List<PowerSource> res = new ArrayList<>();
        for (ErdsForecast fc : dcFcs.getErdsForecasts()) {
            res.add(before(fc, max).dcId(dcFcs.getDataCenterName()));
        }
        return fullfill(res, dcPower);
    }

    public static Amount<Money> toEuros(int e) {
        return Amount.valueOf(((double) e) / EUR_TO_INT, Currency.EUR);
    }

    private static List<PowerSource> fullfill(List<PowerSource> sources, DataCenterPower dcPower) {
        List<PowerSource> res = new ArrayList<>();
        PowerSourceSlot[][] slots = new PowerSourceSlot[sources.size()][sources.get(0).slots().length];
        for (int t = 0; t < slots[0].length; t++) {
            int used = power(dcPower.getPowerValues().get(t).getPower());
            for (int i = 0; i < sources.size(); i++) {
                int usedBy = usedBy(sources, sources.get(i), t, used);
                slots[i][t] = new PowerSourceSlot(usedBy, sources.get(i).slots()[t].renPct(), 0);
                used -= usedBy;
            }
        }
        for (int i = 0; i < sources.size(); i++) {
            PowerSource p = new PowerSource(sources.get(i).name(), slots[i]);
            p.dcId(sources.get(i).dcId());
            res.add(p);
        }
        return res;
    }

    private static int usedBy(List<PowerSource> sources, PowerSource my, int at, int total) {
        Set<PowerSource> s = new TreeSet<>((o1, o2) -> o2.slots()[at].renPct() - o1.slots()[at].renPct());
        s.addAll(sources);
        for (PowerSource source : s) {
            if (source == my) {
                if (total > source.slots()[at].peak()) {
                    return source.slots()[at].peak();
                } else {
                    return total;
                }
            } else {
                if (total > source.slots()[at].peak()) {
                    total -= source.slots()[at].peak();
                } else {
                    total = 0;
                }
            }
        }
        throw new IllegalArgumentException("Unable to split the power. Remainder is " + total);
    }

    public static MyActivity myActivity(TimeSlotBasedEntity range, String eascName, ActivitySpecification spec, List<ServiceLevel> currentSLOs, ActivityMetrics metrics) {
        MyActivity a = new MyActivity(eascName, spec.getActivityName());
        //TODO precedences
        if (spec.getPrecedences() != null && !spec.getPrecedences().isEmpty()) {
            throw new UnsupportedOperationException("Does not support precedences");
        }
        if (spec.getRelocability() == null) {
            throw new UnsupportedOperationException(a.pretty() + " did not specify any relocation method");
        }
        a.migrationCost((int) spec.getMigrationPerformanceCost().getEstimatedValue());
        a.relocatibility(spec.getRelocability());
        a.forbiddenStates(spec.getForbiddenStates());
        //SLOs
        /*if (spec.getServiceLevelObjectives().isEmpty()) {
            throw new UnsupportedOperationException(a.pretty() + " did not specify any SLO");
        }*/
        boolean cumulative = false;
        boolean instant = false;

        for (ServiceLevelObjective slo : spec.getServiceLevelObjectives()) {
            // Cumulative revenues
            if (slo.getDateFrom().isAfter(range.getDateTo()) || slo.getDateFrom().equals(range.getDateTo())) {
                //Bypass a future SLO
                continue;
            } else if (slo.getDateTo().isBefore(range.getDateFrom()) || slo.getDateTo().equals(range.getDateFrom())) {
                //bypass a past SLO
                continue;
            }
            if (slo.getCumulativeBusinessObjective() != null) {
                cumulative = true;
                ServiceLevel current = null;
            	// Select the most recent measurement of cumulative performance that fits in the current SLO
                for (ServiceLevel l : currentSLOs) {
                	if (l.getDateFrom().compareTo(slo.getDateFrom()) >= 0 
                			&& l.getDateTo().compareTo(slo.getDateTo()) <= 0) {
                		current = l;
                	} else if (l.getDateTo().compareTo(slo.getDateTo()) > 0) {
                		break;
                	}
                }
                a.add(cumulativeRevenue(range, slo, current));
            }
            if (slo.getInstantBusinessObjective() != null) {
                instant = true;
                instantRevenue(range, slo).forEach(a::add);
            }
        }
        /*if (a.cumulativeRevenues().isEmpty() && a.instantRevenues().isEmpty()) {
            throw new IllegalStateException(a.pretty() + " does not embed any SLO after conversion. Check the time boundaries");
        }*/

        if (cumulative && instant) {
            throw new IllegalStateException("There is both cumulative and instant SLO");
        }
        for (DataCenterSpecification d : spec.getDataCenters()) {
            ActivityDataCenterMetrics m = null;
            for (ActivityDataCenterMetrics dm : metrics.getDataCenters()) {
                if (dm.getDataCenterName().equals(d.getDataCenterName())) {
                    m = dm;
                    break;
                }
            }
            a.add(datacenterPart(d, m, cumulative, range.getTimeSlotDuration()));
        }
        return a;
    }

    /**
     * Convert a cumulative revenue.
     * if the slo specification is overlapping the current range, then the current slo can be used to specify the
     * current performance level.
     *
     * @param range   the scheduling window
     * @param slo     the slo to convert. Must have a non-null cumulative business performance
     * @param current the current slo if exists. Can be null
     * @return the resulting revenue.
     */
    public static CumulativeRevenue cumulativeRevenue(TimeSlotBasedEntity range, ServiceLevelObjective slo, ServiceLevel current) {
        int basePrice = money(slo.getBasePrice());

        int from = timeSlot(range, slo.getDateFrom());
        int to = timeSlot(range, slo.getDateTo());

        int basePerf = instantPerf(slo.getCumulativeBusinessObjective()/*, range.getTimeSlotDuration()*/);
        //TODO: scaling factor for the base-price
        CumulativeRevenue p = new CumulativeRevenue(from, to, basePerf, basePrice);
        p.unit(slo.getCumulativeBusinessObjective().getUnit());


        if (slo.getDateFrom().isBefore(range.getDateFrom())) {
        	Amount<?> workDone;
            if (current == null) {
            	logger.info("Previous work done not provided, assuming 0 (SLO:" + slo + ", range: " + range);
            	workDone = Amount.ZERO;
            } else {
            	workDone = current.getCumulativeBusinessPerformance();
            }
            p.base(instantPerf(workDone));
        }
        if (slo.getDateTo().isAfter(range.getDateTo()) && slo.getDateFrom().isBefore(range.getDateTo())) {
            throw new IllegalArgumentException("The SLO:\n" + slo + "\n ends after the time range: \n" + range);
        }
        for (PriceModifier m : slo.getPriceModifiers()) {
            p.add(modifier(m, null/*range.getTimeSlotDuration()*/));
        }
        return p;
    }

    /**
     * Convert a instant revenue.
     * if the slo specification is overlapping the current range, then the current slo can be used to specify the
     * current performance level.
     *
     * @param range the scheduling window
     * @param slo   the slo to convert. Must have a non-null instant business performance
     * @return the resulting revenues. One InstantRevenue per time-slot in the original SLO.
     */
    public static List<InstantRevenue> instantRevenue(TimeSlotBasedEntity range, ServiceLevelObjective slo) {
        int basePrice = money(slo.getBasePrice());

        //We ignore all the slots before and after the range
        //it's ok because on the instantaneous principle
        int from = Math.max(timeSlot(range, slo.getDateFrom()), timeSlot(range, range.getDateFrom()));
        int to = Math.min(timeSlot(range, slo.getDateTo()), timeSlot(range, range.getDateTo()));


        List<InstantRevenue> l = new ArrayList<>();
        int basePerf = instantPerf(slo.getInstantBusinessObjective());//cumulativePerf(slo.getInstantBusinessObjective(), range.getTimeSlotDuration());
        List<Modifier> modifiers = new ArrayList<>();
        for (PriceModifier m : slo.getPriceModifiers()) {
            modifiers.add(modifier(m, /*range.getTimeSlotDuration()*/null));
        }
        for (int t = from; t < to; t++) {
            InstantRevenue p = new InstantRevenue(t, basePerf, basePrice);
            p.unit(slo.getInstantBusinessObjective().getUnit());
            modifiers.forEach(p::add);
            l.add(p);
        }
        return l;
    }

    /**
     * Convert a datacenter specification.
     * The datacenter name is maintained.
     * All the underlying working mode and the transition costs are converted.
     *
     * @param spec the specification to convert
     * @param metrics the current metrics
     * @return the conversion result
     */
    public static DatacenterPart datacenterPart(DataCenterSpecification spec, ActivityDataCenterMetrics metrics, boolean cumulative, Amount<javax.measure.quantity.Duration> d) {
        Map<String, WM> trans = new HashMap<>();
        DatacenterPart p = new DatacenterPart(spec.getDataCenterName());
        for (WorkingMode wm : spec.getWorkingModes()) {
            String k = wm.getName();
            WM w = cumulative ? WM(wm, d) : WM(wm);
            p.add(w, spec.getDefaultWorkingMode().equals(k));
            trans.put(k, w);
        }
        //Performance costs
        for (WorkingMode wm : spec.getWorkingModes()) {
            String src = wm.getName();
            for (Transition t : wm.getTransitions()) {
                String dst = t.getTarget();
                p.setTransitionCost(trans.get(src), trans.get(dst), instantPerf(t.getPerformanceCost()));
            }
        }
        //current working mode
        p.currentWM(trans.get(metrics.getWorkingModeName()));
        return p;
    }

    public static Modifier modifier(PriceModifier pm, Amount<javax.measure.quantity.Duration> d) {
        Amount<?> a = pm.getModifier();
        int th = (int) Math.round(pm.getThreshold().getEstimatedValue());
        if (d != null) {
            th = (int) Math.round(AmountUtils.calcCumulativePerformance(pm.getThreshold(), d).getEstimatedValue());
        }
        boolean linear = isLinearModifier(pm);
        double tmp = a.getEstimatedValue();
        int v = (int) Math.round(1.0 * tmp * EUR_TO_INT);
        //int v = (int) (tmp * EUR_TO_INT);
        
        if (v==0 && tmp != 0) {
            throw new IllegalArgumentException("Rounding the price leads to null price, not expensive enough");
        }
        
        Modifier m = new Modifier(th, v);
        
        
        if (linear) {
            m.linear();
        } else {
            m.flat();
        }
        return m;
    }

    private static boolean isLinearModifier(PriceModifier priceModifier) {
    	Amount<?> modifier = priceModifier.getModifier();
        Unit<?> modifierUnit = modifier.getUnit();
        if (modifierUnit instanceof ProductUnit) {
        	ProductUnit<?> productUnit = (ProductUnit<?>) modifierUnit;
        	if (!(productUnit.getUnitPow(0) == 1 && productUnit.getUnit(0) instanceof Currency)) {
        		throw new IllegalArgumentException("Modifier unit is not a currency: " + productUnit);
        	} else {
        		Unit<?> thresholdUnit = priceModifier.getThreshold().getUnit();
        		Unit<?> requiredDenominatorUnit;
        		if (thresholdUnit.equals(NonSI.PERCENT)) {
        			requiredDenominatorUnit = Units.PERCENTAGE_POINT;
        		} else {
        			requiredDenominatorUnit = thresholdUnit;
        		}
        		if (!AmountUtils.isQuantityPerUnit(modifier, requiredDenominatorUnit)) {
        			throw new IllegalArgumentException("Modifier unit is not compatible with threshold unit (should be "
            				+ "money/thresholdUnit). Threshold unit: " +  thresholdUnit + ", modifier unit: " 
        					+ modifierUnit);
        		}
        	}
            return true;
        } else if (!(modifierUnit instanceof Currency)) {
        	throw new IllegalArgumentException("Modifier unit is not a currency: " + modifierUnit);
        }
        return false;
    }
    
    /**
     * Convert a Working Mode.
     * All the underlying performance level are converted as well.
     * The conversion does not copy the datacenter identifier as it is implied by the WM addition
     *
     * @param s the mode to convert
     * @return the converted mode.
     */
    public static WM WM(WorkingMode s) {
        WM wm = new WM("", s.getName());
        int i = 0;
        for (PerformanceLevel pl : s.getPerformanceLevels()) {
            wm.addPerfLevel(pl, i, instantPerf(pl.getBusinessPerformance()), power(pl.getPower()));
            i++;
        }
        wm.setValue(s.getValue());
        return wm;
    }

    public static WM WM(WorkingMode s, Amount<javax.measure.quantity.Duration> d) {
        WM wm = new WM("", s.getName());
        int i = 0;
        for (PerformanceLevel pl : s.getPerformanceLevels()) {
            wm.addPerfLevel(pl, i, cumulativePerf(pl.getBusinessPerformance(), d), power(pl.getPower()));
            i++;
        }
        wm.setValue(s.getValue());
        return wm;
    }


    /**
     * Convert an amount of power to an integer
     *
     * @param a the amount to convert
     * @return the power in watt
     */
    public static int power(Amount<Power> a) {
        double d = 1.0 * a.longValue(SI.WATT);
        return (int) Math.floor(d / WATT_REDUCTION_FACTOR);
    }

    /**
     * Returns the energy as an integer
     * @param a the amount of energy to convert
     * @return an amount in watt/hour
     */
    public static int energy(Amount<Energy> a) {
        return (int) a.longValue(SI.JOULE) / (60 * 60);
    }

    /**
     * Convert an amount of money.
     * @param a the amount to convert
     * @return the money as an integer
     */
    public static int money(Amount<Money> a) {
        double d = a.doubleValue(Currency.EUR);
        return (int) Math.round(1.0 * d * EUR_TO_INT);
    }

    public static int pct(Amount<Dimensionless> a) {
        return (int) a.getExactValue();
    }

    /**
     * Convert the timestamp to a time-slot. 0 aligned.
     * @param range the time range
     * @param now the timestamp to convert.
     * @return the time-slot.
     * @throws IllegalArgumentException if the timestamp is not aligned with a time-slot number wrt. the slots duration
     */
    public static int timeSlot(TimeSlotBasedEntity range, DateTime now) {
        long msec = range.getTimeSlotDuration().longValue(SI.MILLI(SI.SECOND));
        long duration = now.toDate().getTime() - range.getDateFrom().toDate().getTime();
        if (duration % msec != 0) {
            throw new IllegalArgumentException("The timestamp (" + now + ") is not aligned with a time-slot in:\n" + range);
        }
        return (int) (duration / msec);
    }

    public static int offset(DateTime root, Amount<javax.measure.quantity.Duration> d, DateTime now) {
        long msec = d.longValue(SI.MILLI(SI.SECOND));
        long duration = now.toDate().getTime() - root.toDate().getTime();
        if (duration % msec != 0) {
            throw new IllegalArgumentException("The timestamp (" + now + ") is not aligned with a time-slot in:\n" + root);
        }
        return (int) (duration / msec);
    }

    public static DateTime timeSlot(TimeSlotBasedEntity range, int offset) {
        DateTime from = range.getDateFrom();
        return from.plusSeconds((int) (offset * range.getTimeSlotDuration().longValue(SI.SECOND)));
    }

    /**
     * Convert the energy price to a value usuable in the optimizer.
     * @param a the amount to convert
     * @return the resulting value
     */
    public static int energyPrice(Amount<EnergyPrice> a) {
        double d = a.doubleValue(Units.EUR_PER_KWH) / 1000 * EUR_TO_INT; //in watt hours
        int v = (int) Math.round(d);
        if (d != 0 && v == 0) {
            throw new IllegalArgumentException("Rounding the energy price lead to a 0 value. Not enough expensive");
        }
        return v;
    }

    public static int instantPerf(Amount<?> a) {
        return (int)a.getEstimatedValue();
    }

    public static int cumulativePerf(Amount<?> a, Amount<javax.measure.quantity.Duration> ts) {
        return (int) Math.round(AmountUtils.calcCumulativePerformance(a, ts).getEstimatedValue());
    }
    /**
     * Convert an objective.
     * Currently only the RenPCT objective is supported.
     *
     * @param o     the objective to convert
     * @param range the scheduling period. Currently it must wrap the objective range
     * @param pastPowerUsage the past power usage on the datacenters
     * @return the resulting penalty objects, may be empty
     * @throws TimeIntervalExpressionException if the cron expression cannot be parse
     */
    public static List<DynCost> dynCost(Objective o, TimeSlotBasedEntity range, List<PowerSource> pastPowerUsage) throws TimeIntervalExpressionException, DatatypeConfigurationException {
        Date now = range.getDateFrom().toDate();
        TimeFrame tf = o.getTimeFrame();
        TimeIntervalExpression interval = TimeIntervalExpressionFactory.create(tf);
        List<DynCost> res = new ArrayList<>();

        //Decompose the interval
        DateTime from = new DateTime(interval.getStartDate(now));
        if (from.isAfter(range.getDateFrom())) {
            //Maybe in an ongoing objective ?
            Duration d = DatatypeFactory.newInstance().newDuration(tf.getDuration());
            long ms = d.getTimeInMillis(Calendar.getInstance());
            from = new DateTime(interval.getStartDate(range.getDateFrom().minus(ms).toDate()));
        }

        DateTime to = new DateTime(interval.getEndDate(from.toDate()));
        List<int[]> bounds = new ArrayList<>();
        while (from.isBefore(range.getDateTo())) {
            int ts = timeSlot(range, from);
            if (ts < 0) {
                for (PowerSource src : pastPowerUsage) {
                    if (src.slots().length != -ts) {
                        throw new IllegalArgumentException("We are in an ongoing objective [" + from + "; " + to + "[ with incompatible data (got " + (-src.slots().length) + " slots; expected " + (-ts) + ")");
                    }
                }
            }
            ts = Math.max(ts, 0);
            bounds.add(new int[]{ts, timeSlot(range, to)});
            if (to.equals(range.getDateTo())) {
                break;
            }
            from = to;
            to = new DateTime(interval.getEndDate(from.toDate()));
        }
        if (!to.equals(range.getDateTo())) {
            throw new IllegalArgumentException("The objective end (" + to + ") is not synced with the scheduling termination (" + range.getDateTo() + ")");
        }
        if (bounds.isEmpty()) {
            return res;
        }

        //not wrapped properly
        if (/*from.isBefore(range.getDateFrom()) ||*/ to.isAfter(range.getDateTo())) {
            throw new IllegalArgumentException("The timeframe of objective " + o + " [" + from + "-" + to + "[ is not wrapped by the time range: \n" + range);
        }

        String dcId = o.getDataCenterId();
        if ((o.getType().equals(ObjectiveType.ENERGY) || o.getType().equals(ObjectiveType.ENERGY_PROPERTY)) &&
                o.getImplementationType().equals("MUST") &&
                o.isEnabled()
                ) {
            Target t = o.getTarget();
            if (!t.getMetric().equals("renewablePercentage")) {
                throw new IllegalArgumentException("Unsupported target metric '" + t.getMetric() + "'");
            }
            double v = t.getValue();
            if (t.getOperator().equals(Target.GREATER_THAN)) {
                v += 1;
            } else if (!t.getOperator().equals(Target.GREATER_EQUALS)) {
                throw new IllegalArgumentException("Unsupported target operator" + t.getOperator());
            }

            //We multiply everything by 10 to have a better precision: 771 -> 77.1%
            Penalty pen = new Penalty((int) v * 10, Integer.MIN_VALUE / 100); //Default value that should not be used: there will be some modifiers

            for (PriceModifier pm : o.getPriceModifiers()) {
                try {
                    Modifier m = modifier(pm, null);
                    Modifier m2 = new Modifier(m.threshold() * 10, m.penalty() / 10);
                    if (m.isFlat()) {
                        m2.flat();
                    } else {
                        m2.linear(m.step());
                    }
                    pen.add(m2);
                } catch (IllegalArgumentException ex) {
                    throw new IllegalArgumentException("Unable to convert " + o + ": " + ex.getMessage());
                }
            }
            for (int[] bound : bounds) {
                res.add(new RenPct(dcId, bound[0] == 0 ? pastPowerUsage : new ArrayList<>(), bound[0], bound[1], pen));
            }
        } else {
            throw new IllegalArgumentException("Unsupported type '" + o.getType() + "'");
        }
        return res;
    }
}
