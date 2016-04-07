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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dc4cities.energis.client.EnergisObjectMapper;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.StringWriter;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static eu.dc4cities.energis.client.util.Preconditions.checkNotNullOrEmpty;

/**
 * Builder used to create the JSON to query Energis.
 *
 * The query returns the data points for the given metrics for the specified
 * time range. The time range can be specified as absolute or relative.
 *
 */
public class QueryBuilder {

    @JsonProperty("startAbsolute")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DateTime startAbsolute;

    @JsonProperty("endAbsolute")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DateTime endAbsolute;

    @JsonProperty("relativeTimePeriod")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private RelativeTime relativeTimePeriod;

    @JsonProperty("metricName")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String metricName;

    @JsonProperty("companyCode")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String companyCode;

    @JsonProperty("assetCode")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String assetCode;

    @JsonProperty("granularity")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Granularity granularity;

    private ObjectMapper mapper;

    private QueryBuilder() {
        mapper = new EnergisObjectMapper();
    }

    /**
     * The beginning time of the time range.
     *
     * @param start start time
     * @return the builder
     */
    public QueryBuilder setStart(DateTime start) {
        checkNotNull(start);
        checkArgument(relativeTimePeriod == null, "Both relative and absolute start times cannot be set.");

        this.startAbsolute = start;
        return this;
    }

    /**
     * The ending value of the time range. Must be later in time than the start
     * time. An end time is not required and default to now.
     *
     * @param end end time
     * @return the builder
     */
    public QueryBuilder setEnd(DateTime end) {
        checkNotNull(end);
        checkArgument(relativeTimePeriod == null, "Both relative and absolute end times cannot be set.");
        this.endAbsolute = end;
        return this;
    }

    /**
     * Returns a new query builder.
     *
     * @return new query builder
     */
    public static QueryBuilder getInstance() {
        return new QueryBuilder();
    }

    /**
     * The metric to query for.
     *
     * @param name metric name
     * @return the builder
     */
    public QueryBuilder setMetricName(String name) {
        checkNotNullOrEmpty(name, "Name cannot be null or empty.");
        metricName = name;
        return this;
    }

    public QueryBuilder setCompanyCode(String code) {
        checkNotNullOrEmpty(code, "Company code cannot be null or empty.");
        companyCode = code;
        return this;
    }

    public QueryBuilder setAssetCode(String code) {
        checkNotNullOrEmpty(code, "Asset code cannot be null or empty.");
        assetCode = code;
        return this;
    }

    public QueryBuilder setGranularity(Granularity granularity) {
        checkNotNull(granularity, "Granularity cannot be null.");
        this.granularity = granularity;
        return this;
    }

    /**
     * Returns the absolute range start time.
     *
     * @return absolute range start time
     */
    public DateTime getStartAbsolute() {
        return startAbsolute;
    }

    /**
     * Returns the absolute range end time.
     *
     * @return absolute range end time
     */
    public DateTime getEndAbsolute() {
        return endAbsolute;
    }

    /**
     * Returns the relative range start time.
     *
     * @return relative range start time
     */
    public RelativeTime getRelativeTimePeriod() {
        return relativeTimePeriod;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public Granularity getGranularity() {
        return granularity;
    }

    /**
     * Returns the JSON string built by the builder. This is the JSON that can
     * be used by the client to query Energis.
     *
     * @return JSON
     * @throws IOException if the query is invalid and cannot be converted to
     * JSON
     */
    public String build() throws IOException {
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, this);

        return writer.toString();
    }

}
