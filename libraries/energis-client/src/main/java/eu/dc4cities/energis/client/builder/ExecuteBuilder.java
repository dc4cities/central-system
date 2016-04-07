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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.dc4cities.energis.client.builder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dc4cities.energis.client.EnergisObjectMapper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static eu.dc4cities.energis.client.util.Preconditions.checkNotNullOrEmpty;

/**
 *
 *
 */
public class ExecuteBuilder {
    
    @JsonProperty("companyCode")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String companyCode;

    @JsonProperty("assetCode")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String assetCode;

    @JsonProperty("metricName")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String metricName;

    @JsonProperty("inputs")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<FormulaInput> inputs = new ArrayList<>();
    
    private ObjectMapper mapper;

    private ExecuteBuilder() {
        mapper = new EnergisObjectMapper();
    }

    /**
     * Returns a new query builder.
     *
     * @return new query builder
     */
    public static ExecuteBuilder getInstance() {
        return new ExecuteBuilder();
    }

    /**
     * The metric to query for.
     *
     * @param name metric name
     * @return the builder
     */
    public ExecuteBuilder setMetricName(String name) {
        checkNotNullOrEmpty(name, "Name cannot be null or empty.");
        metricName = name;
        return this;
    }

    public ExecuteBuilder setCompanyCode(String code) {
        checkNotNullOrEmpty(code, "Company code cannot be null or empty.");
        companyCode = code;
        return this;
    }

    public ExecuteBuilder setAssetCode(String code) {
        checkNotNullOrEmpty(code, "Asset code cannot be null or empty.");
        assetCode = code;
        return this;
    }
    
    public ExecuteBuilder addInput(FormulaInput input) {
        checkNotNull(input, "The input cannot be null");
        this.inputs.add(input);
        return this;
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

    public List<FormulaInput> getInputs() {
        return inputs;
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
