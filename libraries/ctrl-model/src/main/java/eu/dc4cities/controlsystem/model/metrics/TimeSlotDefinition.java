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

package eu.dc4cities.controlsystem.model.metrics;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class TimeSlotDefinition {

    List<Integer> values = new ArrayList<Integer>();
    //Define the kind of unit: currently it will be "slotNumber"
    String unit;
    //Define the value related to the unit: currently will be the duration of the timeslot taken from the configuration
    String unitValue;

    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        this.values = values;
    }

    public String getUnit() {
        return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}

    @JsonIgnore
    public int getSlotsNumber() {
        return values != null ? values.size() : 0;
    }

    public String getUnitValue() {
        return unitValue;
    }

    public void setUnitValue(String unitValue) {
        this.unitValue = unitValue;
    }


}
