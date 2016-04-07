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

import eu.dc4cities.controlsystem.model.easc.Activity;
import eu.dc4cities.controlsystem.model.easc.ActivityDataCenter;
import eu.dc4cities.controlsystem.model.easc.EascActivityPlan;
import eu.dc4cities.controlsystem.model.easc.Work;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.Score;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.Statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by fhermeni on 08/10/2015.
 */
public class Merger {

    public static Statistics mergeStatistics(List<Statistics> stats) {
        //The first global solution is at the moment each sub got a solution

        if (stats.size() == 1) {
            return stats.get(0);
        }
        long start = Long.MAX_VALUE;
        long end = Long.MIN_VALUE;
        int val = 0;
        long at = Long.MIN_VALUE;
        String status = "ok";
        for (Statistics st : stats) {
            Score sc = st.scores().get(0);
            at = Math.max(at, sc.timestamp);
            val += sc.value.intValue();
            start = Math.min(start, st.startTime());
            end = Math.max(end, st.endTime());
            if (st.status().equals("to")) {
                status = "to";
            }
        }

        //relative based scoring, ordered by the timestamp
        TreeSet<Score> allScores = new TreeSet<>((s1, s2) -> {
            return (int) (s1.timestamp - s2.timestamp);
        });
        for (Statistics st : stats) {
            Score base = st.scores().get(0);
            for (int i = 1; i < st.scores().size(); i++) {
                Score sc = st.scores().get(i);
                int diff = Math.abs(sc.value.intValue() - base.value.intValue());
                Score n = new Score(diff, sc.timestamp);
                allScores.add(n);
            }
        }
        //then each time a sub computes a solution, this is an improvement that must be stored
        Statistics st = new Statistics();
        st.setStartTime(start);
        st.setEndTime(end);
        st.setStatus(status);
        //Back to absolute values
        st.scores().add(new Score(val, at));
        for (Score sc : allScores) {
            Score s = new Score(st.scores().get(st.scores().size() - 1).value.intValue() + sc.value.intValue(), sc.timestamp);
            st.scores().add(s);
        }
        st.retainedPlan(merge(stats.stream().map(Statistics::retainedPlan).collect(Collectors.toList())));
        return st;
    }

    public static List<EascActivityPlan> merge(List<List<EascActivityPlan>> subs) {
        if (subs.size() == 1) {
            return subs.get(0);
        }
        //Sort by dates. Earliest first
        List<List<EascActivityPlan>> sorted = new ArrayList<>(subs);
        Collections.sort(sorted, (l1, l2) -> {
            return l1.get(0).getDateFrom().compareTo(l2.get(0).getDateFrom());
        });

        List<EascActivityPlan> res = new ArrayList<>();
        //The first sub contains the template (all the EASCs)
        List<EascActivityPlan> first = sorted.get(0);
        for (EascActivityPlan p : first) {
            EascActivityPlan cpy = new EascActivityPlan(p.getEascName());
            cpy.setDateFrom(p.getDateFrom());
            cpy.setTimeSlotDuration(p.getTimeSlotDuration());
            cpy.setDateTo(p.getDateTo());
            cpy.setActivities(new ArrayList<>());
            for (Activity a : p.getActivities()) {
                Activity cpA = new Activity(a.getName());
                cpy.getActivities().add(cpA);
                cpA.setDataCenters(new ArrayList<>());
                for (ActivityDataCenter aDc : a.getDataCenters()) {
                    ActivityDataCenter x = new ActivityDataCenter(aDc.getDataCenterName());
                    x.setWorks(new ArrayList<>());
                    cpA.getDataCenters().add(new ActivityDataCenter(aDc.getDataCenterName()));
                }
            }
            res.add(cpy);
        }

        //let's go
        for (List<EascActivityPlan> plans : sorted) {
            for (EascActivityPlan plan : plans) {
                EascActivityPlan globalPlan = res.stream().filter(p -> p.getEascName().equals(plan.getEascName())).findFirst().get();

                if (globalPlan.getDateTo().isBefore(plan.getDateTo())) {
                    globalPlan.setDateTo(plan.getDateTo());
                }

                for (Activity a : plan.getActivities()) {
                    Activity globalActivity = globalPlan.getActivities().stream().filter(p -> p.getName().equals(a.getName())).findFirst().get();
                    //merge the service levels, the activityDatacenter
                    globalActivity.getServiceLevels().addAll(a.getServiceLevels());
                    for (ActivityDataCenter aDc : a.getDataCenters()) {
                        ActivityDataCenter globalDc = globalActivity.getDataCenters().stream().filter(d -> d.getDataCenterName().equals(aDc.getDataCenterName())).findFirst().get();
                        for (Work w : aDc.getWorks()) {
                            //Erase the id
                            int from = globalDc.getWorks().size();
                            Work w2 = new Work(from, from + 1, w.getWorkingModeName(), w.getWorkingModeValue(), w.getPower(), w.getBusinessPerformance());
                            globalDc.getWorks().add(w2);
                        }

                    }
                }
            }
        }
        return res;
    }
}
