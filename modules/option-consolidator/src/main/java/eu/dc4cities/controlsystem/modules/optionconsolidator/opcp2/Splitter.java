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
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.reducer.Reducer;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.measure.quantity.Duration;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fhermeni on 29/09/2015.
 */
public class Splitter {

    public static Logger LOGGER = LoggerFactory.getLogger("OptionConsolidator");

    public static List<Scheduler> split(TimeSlotBasedEntity range, List<Objective> objectives,
                                          List<DataCenterForecast> forecasts,
                                          List<DataCenterPowerPlan> ipp,
                                          List<EascPowerPlan> budgets,
                                          List<EascActivitySpecifications> specs,
                                          List<DataCenterPower> pastPowerUsage, List<EascServiceLevels> pastSLOs,
                                          List<EascMetrics> eascMetrics, Reducer r) throws TimeIntervalExpressionException {


        List<ServiceLevelObjective> slos = new ArrayList<>();
        for (EascActivitySpecifications spec : specs) {
            for (ActivitySpecification as : spec.getActivitySpecifications()) {
                slos.addAll(as.getServiceLevelObjectives());
            }
        }

        List<Scheduler> subs = new ArrayList<>();
        DateTime to = new DateTime(range.getDateFrom());
        DateTime from = new DateTime(range.getDateFrom());
        while (to.isBefore(range.getDateTo())) {
            to = to.plusSeconds((int) range.getTimeSlotDuration().longValue(SI.SECOND));
            if (splittableSLOs(slos, to) && splittableObjectives(objectives, to) && splittableBudgets(budgets, to)) {
                if (!to.equals(range.getDateTo())) {
                    LOGGER.debug("Split at " + to);
                }
                Scheduler s = new Scheduler(subRange(range, from, to),
                        objectives,
                        forecasts,
                        //subForeCasts(forecasts, from, to),
                        subPlans(ipp, from, to),
                        subBudget(budgets, from, to),
                        specs,
                        subPastPower(pastPowerUsage, subs.isEmpty()),
                        subPastSLOs(pastSLOs, subs.isEmpty()),
                        subMetrics(eascMetrics, subs.isEmpty()),
                        r.copy()
                        );
                subs.add(s);
                from = new DateTime(to);
            }
        }
        return subs;
    }

    public static boolean splittableBudgets(List<EascPowerPlan> plans, DateTime to) {

        for (EascPowerPlan plan : plans) {
            int at = Converter.timeSlot(plan, to);
            DateTime from = plan.getDateFrom();
            DateTime end = plan.getDateTo();
            for (DataCenterPowerPlan dcPlan : plan.getDataCenterQuotas()) {
                if (from == null) {
                    from = dcPlan.getDateFrom();
                }
                if (end == null) {
                    end = dcPlan.getDateTo();
                }

                for (EnergyQuota eq : dcPlan.getEnergyQuotas()) {
                    int st = eq.getStartTimeSlot();
                    int ed = eq.getEndTimeSlot();
                    if (st > at && at < ed) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static List<EascMetrics> subMetrics(List<EascMetrics> metrics, boolean first) {
        if (first) {
            return metrics;
        }
            List<EascMetrics> sub = new ArrayList<>();
            for (EascMetrics m : metrics) {
                EascMetrics e = new EascMetrics(m.getEascName());
                e.setActivities(new ArrayList<>());
                sub.add(e);
                for (ActivityMetrics am : m.getActivities()) {
                    ActivityMetrics a = new ActivityMetrics(am.getName());
                    a.setDataCenters(new ArrayList<>());
                    for (ActivityDataCenterMetrics dm : am.getDataCenters()) {
                        ActivityDataCenterMetrics d = new ActivityDataCenterMetrics(dm.getDataCenterName());
                        d.setWorkingModeName(dm.getWorkingModeName()); //we don't know what WM it will be
                        //We reset the metrics
                        d.setInstantBusinessPerformance(Amount.valueOf(0, Unit.ONE));
                        d.setCumulativeBusinessPerformance(Amount.valueOf(0, Unit.ONE));
                        d.setPower(Amount.valueOf(0, SI.WATT));
                        //d.setRenewablePercentage(Amount.valueOf(0, Unit.ONE));
                        d.setWorkingModeValue(0);
                        a.getDataCenters().add(d);
                        e.getActivities().add(a);
                    }
                }
            }
            return sub;
    }

    public static List<EascServiceLevels> subPastSLOs(List<EascServiceLevels> slos, boolean first/*DateTime from, DateTime to*/) {
        if (first) {
            return slos;
        }
        List<EascServiceLevels> sub = new ArrayList<>();
        for (EascServiceLevels sl : slos) {
            EascServiceLevels e = new EascServiceLevels(sl.getEascName());
            e.setActivityServiceLevels(new ArrayList<>());
            for (ActivityServiceLevels as : sl.getActivityServiceLevels()) {
                ActivityServiceLevels a = new ActivityServiceLevels(as.getActivityName());
                a.setServiceLevels(new ArrayList<>());
/*
                for (ServiceLevel s : as.getServiceLevels()) {
                    if ((s.getDateFrom().equals(from) || s.getDateFrom().isAfter(from))
                        && (s.getDateTo().equals(to) || s.getDateTo().isBefore(to))) {
                        a.getServiceLevels().add(s);
                    }
                }*/
                e.getActivityServiceLevels().add(a);
            }
            sub.add(e);
        }
        return sub;
    }
  /*  public static List<DataCenterPower> subPastPower(List<DataCenterPower> power, DateTime from, DateTime to) {
        List<DataCenterPower> sub = new ArrayList<>();
        for (DataCenterPower dcP : power) {
            DataCenterPower p = new DataCenterPower(dcP.getDataCenterName());
            p.setPowerValues(new ArrayList<>());
            int i = 0;
            for (TimeSlotPower ts :p.getPowerValues()) {
                DateTime at = at(p.getDateFrom(), p.getTimeSlotDuration(), i);
                if (isIn(from, to, at)) {
                    p.getPowerValues().add(ts);
                }
            }
        }
        return sub;
    }*/

    public static List<DataCenterPower> subPastPower(List<DataCenterPower> power, boolean first) {
        if (first) {
            return power;
        }
        //No values
        List<DataCenterPower> sub = new ArrayList<>();
        for (DataCenterPower dcP : power) {
            DataCenterPower p = new DataCenterPower(dcP.getDataCenterName());
            p.setPowerValues(new ArrayList<>());
        }
        return sub;
    }

    public static DateTime at(DateTime from, Amount<Duration> duration, int nb) {
        return from.plus(nb * duration.longValue(SI.MILLI(SI.SECOND)));
    }

    public static TimeSlotBasedEntity subRange(TimeSlotBasedEntity range, DateTime from, DateTime to) {
        TimeSlotBasedEntity et = new TimeSlotBasedEntity();
        et.setTimeSlotDuration(range.getTimeSlotDuration());
        et.setDateFrom(from);
        et.setDateTo(to);
        return et;
    }

    public static List<EascPowerPlan> subBudget(List<EascPowerPlan> budgets, DateTime from, DateTime to) {
        List<EascPowerPlan> subs = new ArrayList<>();
        for (EascPowerPlan budget : budgets) {
            int start = -1;
            int end = -1;
            if (budget.getDateFrom() != null) {
                start = Converter.timeSlot(budget, from);
            }
            if (budget.getDateTo() != null) {
                end = Converter.timeSlot(budget, to);
            }
            EascPowerPlan p = new EascPowerPlan();
            p.setEascName(budget.getEascName());
            p.setTimeSlotDuration(budget.getTimeSlotDuration());
            p.setDateFrom(from);
            p.setDateTo(to);
            p.setDataCenterQuotas(new ArrayList<>());

            for (DataCenterPowerPlan pp : budget.getDataCenterQuotas()) {
                DataCenterPowerPlan sub = new DataCenterPowerPlan(pp.getDataCenterName());
                sub.setPowerQuotas(new ArrayList<>());

                if (pp.getDateFrom() == null) {
                    pp.setDateFrom(from);
                }
                if (pp.getDateTo() == null) {
                    pp.setDateTo(to);
                }
                if (pp.getTimeSlotDuration() == null) {
                    pp.setTimeSlotDuration(p.getTimeSlotDuration());
                }
                //Copy only viable slots
                for (TimeSlotPower tsp : pp.getPowerQuotas()) {
                    //We have to change the timeslot offset against the new sub
                    DateTime d = Converter.timeSlot(pp, tsp.getTimeSlot());
                    if (isIn(from, to, d)) {
                        int offset = Converter.offset(from, pp.getTimeSlotDuration(), d);
                        TimeSlotPower newP = new TimeSlotPower(offset, tsp.getPower());
                        sub.getPowerQuotas().add(newP);
                    }
                }
                sub.setEnergyQuotas(new ArrayList<>());
                for (EnergyQuota quota : pp.getEnergyQuotas()) {
                    DateTime a = Converter.timeSlot(pp, quota.getStartTimeSlot());
                    DateTime b = Converter.timeSlot(pp, quota.getEndTimeSlot());
                    if (isIn(from, to, a)) {
                        EnergyQuota newQ = new EnergyQuota(Converter.offset(from, pp.getTimeSlotDuration(), a),
                                Converter.offset(from, pp.getTimeSlotDuration(), b),
                                quota.getEnergy());
                        sub.getEnergyQuotas().add(newQ);
                    }
                }
                p.getDataCenterQuotas().add(sub);

            }
            subs.add(p);
        }
        return subs;
    }

    public static List<DataCenterPowerPlan> subPlans(List<DataCenterPowerPlan> plans, DateTime from, DateTime to) {
        if (plans == null) {
            return null;
        }
        List<DataCenterPowerPlan> dcs = new ArrayList<>();
        for (DataCenterPowerPlan plan : plans) {
            DataCenterPowerPlan copy = new DataCenterPowerPlan(plan.getDataCenterName());
            copy.setDateFrom(from);
            copy.setDateTo(to);
            copy.setTimeSlotDuration(plan.getTimeSlotDuration());
            copy.setPowerQuotas(new ArrayList<>());
            //Copy only viable slots
            for (TimeSlotPower tsp : plan.getPowerQuotas()) {
                //We have to change the timeslot offset against the new sub
                DateTime d = Converter.timeSlot(plan, tsp.getTimeSlot());
                if (isIn(from, to, d)) {
                    int offset = Converter.offset(from, plan.getTimeSlotDuration(), d);
                    TimeSlotPower newP = new TimeSlotPower(offset, tsp.getPower());
                    copy.getPowerQuotas().add(newP);
                }
            }
            dcs.add(copy);
        }
        return dcs;
    }

    public static List<DataCenterForecast> subForeCasts(List<DataCenterForecast> forecasts, DateTime from, DateTime to) {
        List<DataCenterForecast> sub = new ArrayList<>();
        for (DataCenterForecast fc : forecasts) {
            DataCenterForecast c = new DataCenterForecast(fc.getDataCenterName());
            c.setErdsForecasts(new ArrayList<>());
            for (ErdsForecast eFc : fc.getErdsForecasts()) {
                ErdsForecast e = new ErdsForecast(eFc.getErdsName());
                e.setTimeSlotForecasts(new ArrayList<>());
                for (TimeSlotErdsForecast ts : eFc.getTimeSlotForecasts()) {
                    DateTime at = at(eFc.getDateFrom(), eFc.getTimeSlotDuration(), ts.getTimeSlot());
                    if (isIn(from, to, at)) {
                        e.getTimeSlotForecasts().add(ts);
                    }
                }
                e.setDateFrom(from);
                e.setDateTo(to);
                e.setTimeSlotDuration(eFc.getTimeSlotDuration());
                c.getErdsForecasts().add(e);
            }
            sub.add(c);
        }
        return sub;
    }

    public static boolean isIn(DateTime from, DateTime to, DateTime t) {
        return (t.isBefore(to) && (t.isAfter(from) || t.equals(from)));
    }

    public static boolean splittableObjectives(List<Objective> objectives, DateTime to) throws TimeIntervalExpressionException {
        for (Objective o : objectives) {
            TimeIntervalExpression interval = TimeIntervalExpressionFactory.create(o.getTimeFrame());
            if (interval.isActive(to.toDate())) {
                return false;
            }
            //System.out.println(o.getId()+ " splittable at " + to);
        }
        return true;
    }

    public static boolean splittableSLOs(List<ServiceLevelObjective> slos, DateTime t) {
        for (ServiceLevelObjective slo: slos) {
            DateTime f = slo.getDateFrom();
            DateTime e = slo.getDateTo();
            if (slo.getCumulativeBusinessObjective() != null) {
                if (!(t.equals(f) || t.isBefore(f) || t.isAfter(e) || t.equals(e))) {
                    return false;
                }
            } else if (slo.getInstantBusinessObjective() != null) {
                return true;
            }
        }
        return true;
    }
}
