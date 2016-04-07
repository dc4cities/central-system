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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Represents a relative time period based on a reference instant.
 *
 */
@JsonPropertyOrder({"referenceInstant", "shift", "unit"})
public class RelativeTime {

    private Date referenceInstant;

    private int shift;

    private TimeUnit unit;

    /**
     * The instant from which apply the shit to calculate the time period. Based
     * on the value of the shift can represent the start or the end of the
     * period.
     */
    public Date getReferenceInstant() {
        return referenceInstant;
    }

    public void setReferenceInstant(Date someReferenceInstant) {
        referenceInstant = someReferenceInstant;
    }

    /**
     * A value used to create one of the boundary of the period based on the
     * reference instant. Can be a positive or a negative value.
     */
    public int getShift() {
        return shift;
    }

    public void setShift(int someShift) {
        shift = someShift;
    }

    /**
     * The unit of time used to perform the shift.
     */
    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit someUnit) {
        unit = someUnit;
    }

}
