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

import org.junit.Test;

public class MetricCatalogTest {

    @Test
    public void testGetName() throws Exception {
        assert MetricCatalog.CO2EMISSIONS.getName().equals("co2emissions");
    }

    @Test
    public void testNameWithActual() throws Exception {
        assert MetricCatalog.AVG_VCPU_USAGE.getNameWithActual().equals("avg_vcpu_usage.actual");
    }

    @Test
    public void testNameWithExpectedMax() throws Exception {
        assert MetricCatalog.POWER.getNameWithExpectedMax().equals("power.expected_max");
    }

    @Test
    public void testNameWithExpected() throws Exception {
        assert MetricCatalog.REN_PERCENT.getNameWithExpected().equals("ren_percent.expected");
    }

    @Test
    public void testNameWithExpectedMin() throws Exception {
        assert MetricCatalog.AMBIENT_TEMPERATURE.getNameWithExpectedMin().equals("ambient_temperature.expected_min");
    }

    @Test
    public void testNameWithPerformance() throws Exception {
        assert MetricCatalog.APC.getNameWithPerformance().equals("apc.performance");
    }

    @Test
    public void testNameWithInvoice() throws Exception {
        assert MetricCatalog.BIZPERF_ITEMS.getNameWithInvoice().equals("bizperf_items.invoice");
    }

    @Test
    public void testNameWithBudget() throws Exception {
        assert MetricCatalog.CARBON_EMISSION_VALUE.getNameWithBudget().equals("carbon_emission_value.budget");
    }

    @Test
    public void testNameWithAdjusted() throws Exception {
        assert MetricCatalog.DATACENTER_NUMBER_OF_VIRTUAL_CORES.getNameWithAdjusted().equals("datacenter_number_of_virtual_cores.adjusted");
    }

    @Test
    public void testNameWithForecasted() throws Exception {
        assert MetricCatalog.MODULE_TEMPERATURE.getNameWithForecasted().equals("module_temperature.forecasted");
    }

    @Test
    public void testNameWithEstimated() throws Exception {
        assert MetricCatalog.RENEWABLE_POWER.getNameWithEstimated().equals("renewable_power.estimated");
    }

    @Test
    public void testNameWithPlanned() throws Exception {
        assert MetricCatalog.IRRADIANCE.getNameWithPlanned().equals("irradiance.planned");
    }

    @Test
    public void testNameWithBaseline() throws Exception {
        assert MetricCatalog.IRRADIATION.getNameWithBaseline().equals("irradiation.baseline");
    }
}