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

package eu.dc4cities.energis.client.builder;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class QueryBuilderTest {

    @Test(expected = NullPointerException.class)
    public void shouldRejectMetricNameNull() {
        QueryBuilder.getInstance().setMetricName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectMetricNameEmpty() {
        QueryBuilder.getInstance().setMetricName("");
    }

    @Test(expected = NullPointerException.class)
    public void shouldRejectAbsoluteStartNull() {
        QueryBuilder.getInstance().setStart(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldRejectAbsoluteEndNull() {
        QueryBuilder.getInstance().setEnd(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldRejectCompanyCodeNull() {
        QueryBuilder.getInstance().setCompanyCode(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectCompanyCodeEmpty() {
        QueryBuilder.getInstance().setCompanyCode("");
    }

    @Test(expected = NullPointerException.class)
    public void shouldRejectAssetCodeNull() {
        QueryBuilder.getInstance().setAssetCode(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectAssetCodeEmpty() {
        QueryBuilder.getInstance().setAssetCode("");
    }

    @Test
    public void shouldBuildQueryWithAbsoluteTimes() throws IOException {
        String json = Resources.toString(Resources.getResource("query_metric_absolute_times.json"), Charsets.UTF_8);

        QueryBuilder builder = QueryBuilder.getInstance();
        builder.setStart(new DateTime(1398902400000L, DateTimeZone.UTC))
                .setEnd(new DateTime(1401580800000L, DateTimeZone.UTC))
                .setMetricName("metric1")
                .setGranularity(new Granularity(Granularity.Unit.DAYS, 1))
                .setCompanyCode("company1")
                .setAssetCode("asset1");
        assertThat(builder.build(), equalTo(json));
    }

}
