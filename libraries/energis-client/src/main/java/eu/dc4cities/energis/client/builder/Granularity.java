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

/**
 *
 */
public class Granularity {

    public static enum Unit {

        YEARS,
        MONTHS,
        WEEKS,
        DAYS,
        HOURS,
        MINUTES;
    }

    @JsonProperty("unit")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Unit unit;

    @JsonProperty("value")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int value;

    public Granularity(Unit unit, int value) {
        super();
        this.unit = unit;
        this.value = value;
    }

    /**
     * The granularity unit.
     */
    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit someUnit) {
        unit = someUnit;
    }

    /**
     * The granularity value.
     */
    public int getValue() {
        return value;
    }

    public void setValue(int someValue) {
        value = someValue;
    }

}
