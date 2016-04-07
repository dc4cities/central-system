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

import eu.dc4cities.controlsystem.model.easc.EascActivityPlan;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.Score;
import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.Statistics;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by fhermeni2 on 12/10/15.
 */
public class MergerTest {

    @Test
    public void testMerge() {
        EascActivityPlan a1 = new EascActivityPlan("a1");
        DateTime d0 = new DateTime().withTimeAtStartOfDay();
        DateTime d1 = d0.plusDays(1);

        List<List<EascActivityPlan>> subs;
    }

    @Test
    public void testMergeStatistics() throws Exception {
        Statistics s1 = new Statistics();
        s1.setStatus("ok");
        s1.setStartTime(1);
        s1.setEndTime(10);
        Score sc1 = new Score(3, 2);
        Score sc2 = new Score(4, 4);
        s1.scores().add(sc1);
        s1.scores().add(sc2);

        Statistics s2 = new Statistics();
        s2.setStatus("to");
        s2.setStartTime(2);
        s2.setEndTime(15);

        Score sc3 = new Score(3, 3);
        Score sc4 = new Score(5, 7);
        s2.scores().add(sc3);
        s2.scores().add(sc4);

        EascActivityPlan a1 = new EascActivityPlan("a1");
        a1.setDateFrom(new DateTime().withTimeAtStartOfDay());
        a1.setDateTo(a1.getDateFrom().plusDays(1));

        EascActivityPlan a2 = new EascActivityPlan("a1");
        a2.setDateFrom(a1.getDateTo());
        a2.setDateTo(a1.getDateTo().plusDays(1));
        s1.retainedPlan(new ArrayList<>());
        s1.retainedPlan().add(a1);
        s2.retainedPlan(new ArrayList<>());
        s2.retainedPlan().add(a2);
        Statistics global = Merger.mergeStatistics(Arrays.asList(s1, s2));
        System.out.println(global);
        Assert.assertEquals(1, global.startTime());
        Assert.assertEquals(15, global.endTime());

        Assert.assertEquals("to", global.status());

        //Score: 3-> 6, 4 -> 7, 7 -> 9
        Assert.assertEquals(3, global.scores().size());
        Score s = global.scores().get(0);
        Assert.assertEquals(3, s.timestamp);
        Assert.assertEquals(6, s.value.intValue());

        s = global.scores().get(1);
        Assert.assertEquals(4, s.timestamp);
        Assert.assertEquals(7, s.value.intValue());

        s = global.scores().get(2);
        Assert.assertEquals(7, s.timestamp);
        Assert.assertEquals(9, s.value.intValue());
    }
}