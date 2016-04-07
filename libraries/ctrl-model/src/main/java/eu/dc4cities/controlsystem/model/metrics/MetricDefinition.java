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

@Deprecated
public class MetricDefinition {

    private String field = "";
    private String metric = "";
    private String unit = "";

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

/*	
 * 	Example:
 * {	
		  "assetName": "erds.grid",
		  "dateFrom": "2014-03-05T10:00",
		  "dateTo": "2014-03-06T10:00",
		  "dateForecasting": "2014-03-05T09:00",
		  "timeSlots": {
		    "values": [0, 1, 2, 3, 4, 8, 12, 16, …],
		    "unit": "slot"
		  },
		  "metrics": {
		    "power":    "estimated.renewable_power.power", --> field
		    "renewablePercentage":   
		                "estimated.renewable_power.renewable_percentage", --> metric
		    "carbonEmissionFactor": 
		                "estimated.renewable_power.carbon_emission_factor" --> unit
		  }
		}
*/

}
