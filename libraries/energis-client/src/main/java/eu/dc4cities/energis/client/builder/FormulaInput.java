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
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"variableName", "values"})
public class FormulaInput {

    @JsonProperty("variableName")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String variableName;

    @JsonProperty("values")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Double> values = new ArrayList<>();

    private FormulaInput() {
        
    }
    
    public FormulaInput(String variableName, List<Double> values) {
        this.variableName = variableName;
        this.values.addAll(values);
    }
    
    /**
     * The name of the variable as it appears in the formula.
     */
    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String someVariableName) {
        variableName = someVariableName;
    }

    /**
     * The list of points. The points can be based on time or not. If based on
     * time all the points must contain a time, the points that are not
     * containing a time will cause the computation to be skipped for that
     * instant.
     */
    public List<Double> getValues() {
        return values;
    }

    public void setValues(List<Double> someValues) {
        values = someValues;
    }

}
