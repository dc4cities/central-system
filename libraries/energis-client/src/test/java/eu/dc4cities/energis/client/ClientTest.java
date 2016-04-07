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

package eu.dc4cities.energis.client;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import eu.dc4cities.energis.client.builder.*;
import eu.dc4cities.energis.client.response.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ClientTest {

    @Test
    public void shouldQueryWithValidResponse() throws IOException, URISyntaxException {
        QueryBuilder builder = QueryBuilder.getInstance();

        String json = Resources.toString(Resources.getResource("query_response_valid.json"), Charsets.UTF_8);

        FakeClient client = new FakeClient(200, json);

        QueryResponse response = client.query(builder);

        List<TimeValue> results = response.getTimeValues();

        assertThat(results.size(), equalTo(3));
        TimeValue dataPoint = results.get(0);
        assertThat(dataPoint.getTimestamp(), equalTo(new DateTime(1401580800000L, DateTimeZone.UTC)));
        assertThat(dataPoint.getValue(), equalTo(1.0));
        dataPoint = results.get(1);
        assertThat(dataPoint.getTimestamp(), equalTo(new DateTime(1401667200000L, DateTimeZone.UTC)));
        assertThat(dataPoint.getValue(), equalTo(2.0));
        dataPoint = results.get(2);
        assertThat(dataPoint.getTimestamp(), equalTo(new DateTime(1401753600000L, DateTimeZone.UTC)));
        assertThat(dataPoint.getValue(), equalTo(3.0));

    }

    @Test
    public void shouldExecuteWithValidResponse() throws IOException, URISyntaxException {
        ExecuteBuilder builder = ExecuteBuilder.getInstance();

        String json = Resources.toString(Resources.getResource("execute_response_valid.json"), Charsets.UTF_8);

        FakeClient client = new FakeClient(200, json);

        ExecuteResponse response = client.execute(builder);

        List<Double> results = response.getValues();

        assertThat(results.size(), equalTo(3));
        assertThat(results.get(0), equalTo(1.0));
        assertThat(results.get(1), equalTo(2.0));
        assertThat(results.get(2), equalTo(3.0));

    }

    @Test
    public void shouldQuerySitesWithValidResponse() throws IOException, URISyntaxException {

        String json = Resources.toString(Resources.getResource("query_sites_response_valid.json"), Charsets.UTF_8);

        FakeClient client = new FakeClient(200, json);

        SitesResponse response = client.querySites("test_code");

        List<Site> results = response.getSites();

        assertThat(results.size(), equalTo(3));
        assertThat(results.get(0).getCode(), equalTo("S1"));
        assertThat(results.get(1).getCode(), equalTo("S2"));
        assertThat(results.get(2).getCode(), equalTo("S3"));

    }

    @Test
    public void shouldQuerySiteDetailWithValidResponse() throws IOException, URISyntaxException {

        String json = Resources.toString(Resources.getResource("query_site_detail_response_valid.json"), Charsets.UTF_8);

        FakeClient client = new FakeClient(200, json);

        SiteDetailResponse response = client.querySite(1L);

        SiteDetail result = response.getSiteDetail();

        assertThat(result, notNullValue());
        assertThat(result.getCode(), equalTo("1"));

    }

    @Test
    public void shouldQueryMetricsWithValidResponse() throws IOException, URISyntaxException {

        String json = Resources.toString(Resources.getResource("query_metrics_response_valid.json"), Charsets.UTF_8);

        FakeClient client = new FakeClient(200, json);

        MetricsResponse response = client.queryMetrics("test_code", "test site", null);

        List<Metric> results = response.getMetrics();

        assertThat(results.size(), equalTo(3));
        assertThat(results.get(0).getName(), equalTo("MT1"));
        assertThat(results.get(0).getReference(), equalTo(Reference.ACTUAL));
        assertThat(results.get(1).getName(), equalTo("MT2"));
        assertThat(results.get(1).getReference(), equalTo(Reference.FORECASTED));
        assertThat(results.get(2).getName(), equalTo("MT3"));
        assertThat(results.get(2).getReference(), equalTo(Reference.ACTUAL));

    }

    @Test
    public void shouldQueryMetricDetailWithValidResponse() throws IOException, URISyntaxException {

        String json = Resources.toString(Resources.getResource("query_metric_detail_response_valid.json"), Charsets.UTF_8);

        FakeClient client = new FakeClient(200, json);

        MetricDetailResponse response = client.queryMetric(1L);

        MetricDetail result = response.getMetricDetail();

        assertThat(result, notNullValue());
        assertThat(result.getName(), equalTo("MT1"));
        assertThat(result.getReference(), equalTo(Reference.ACTUAL));
        assertThat(result.getAssetType(), equalTo(AssetType.SERVER));
        assertThat(result.getMedium(), equalTo(MetricDetail.Medium.UNKNOWN));
        assertThat(result.getEquations(), notNullValue());
        assertThat(result.getEquations().size(), equalTo(1));
        assertThat(result.getEquations().get(0).getInputs(), notNullValue());
        assertThat(result.getEquations().get(0).getInputs().size(), equalTo(2));
        assertThat(result.getEquations().get(0).getInputs().get(0).getVariableName(), equalTo("X1"));
        assertThat(result.getEquations().get(0).getInputs().get(0).getMetricName(), equalTo("MT1.1"));
        assertThat(result.getEquations().get(0).getInputs().get(1).getVariableName(), equalTo("X2"));
        assertThat(result.getEquations().get(0).getInputs().get(1).getMetricName(), equalTo("MT1.2"));

    }
}
