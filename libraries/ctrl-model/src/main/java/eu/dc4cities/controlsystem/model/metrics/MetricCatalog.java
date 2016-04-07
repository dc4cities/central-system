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

/**
 * Enum containing all the metrics of dc4cities at the date of it's creation   9/10/2014.
 * <p/>
 * The metric  power is represented by the constant MetricCatalog.POWER and is retrievable by
 * MetricCatalog.POWER.getName().
 * <p/>
 * <p/>
 * Created by Adrian on 9/10/2014.
 */
public enum MetricCatalog {

    AMBIENT_TEMPERATURE("ambient_temperature"),
    APC("apc"),
    AVG_VCPU_USAGE("avg_vcpu_usage"),
    BIZPERF_ITEMS("bizperf_items"),
    BIZPERF_ITEMS_RATE("bizperf_items_rate"),
    CARBON_EMISSION_VALUE("carbon_emission_value"),
    CO2EMISSIONS("co2emissions"),
    CPU_USAGE("cpu_usage"),
    DATACENTER_NUMBER_OF_VIRTUAL_CORES("datacenter_number_of_virtual_cores"),
    DATACENTER_NUMBER_OF_VMS("datacenter_number_of_vms"),
    DCA("dca"),
    DCEP("dcep"),
    DISK_READ("disk_read"),
    DISK_WRITE("disk_write"),
    ENERGY("energy"),
    IRRADIANCE("irradiance"),
    IRRADIATION("irradiation"),
    MEMORY_USAGE("memory_usage"),
    MODULE_TEMPERATURE("module_temperature"),
    NET_READ("net_read"),
    NET_WRITE("net_write"),
    NR_OF_ACTIVE_SERVERS("nr_of_active_servers"),
    POWER("power"),
    PUE("pue"),
    REN_PERCENT("ren_percent"),
    RENEWABLE_ENERGY("renewable_energy"),
    RENEWABLE_ENERGY_PERCENTAGE("renewable_energy_percentage"),
    RENEWABLE_POWER("renewable_power"),
    RENEWABLE_UTILISATION("renewable_utilisation"),
    SERVER_MAX_CORE_RATIO("server_max_core_ratio"),
    SERVER_NUMBER_OF_VIRTUAL_CORES("server_number_of_virtual_cores"),
    SERVER_NUMBER_OF_VMS("server_number_of_vms"),
    SERVER_RUNTIME_CORE_RATIO("server_runtime_core_ratio"),
    SUM_VCPU_USAGE("sum_vcpu_usage");


    private String name;


    MetricCatalog(String name) {
        this.name = name;
    }


    /**
     * @return metric name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Metric{" +
                "name='" + name + '\'' +
                '}';
    }


    public String getNameWithActual() {
        return name + "." + Reference.ACTUAL.name;
    }

    public String getNameWithExpectedMax() {
        return name + "." + Reference.EXPECTED_MAX.name;
    }

    public String getNameWithExpected() {
        return name + "." + Reference.EXPECTED.name;
    }

    public String getNameWithExpectedMin() {
        return name + "." + Reference.EXPECTED_MIN.name;
    }

    public String getNameWithPerformance() {
        return name + "." + Reference.PERFORMANCE.name;
    }

    public String getNameWithInvoice() {
        return name + "." + Reference.INVOICE.name;
    }

    public String getNameWithBudget() {
        return name + "." + Reference.BUDGET.name;
    }

    public String getNameWithAdjusted() {
        return name + "." + Reference.ADJUSTED.name;
    }

    public String getNameWithForecasted() {
        return name + "." + Reference.FORECASTED.name;
    }
    public String getNameWithEstimated() {
        return name + "." + Reference.ESTIMATED.name;
    }
    public String getNameWithPlanned() {
        return name + "." + Reference.PLANNED.name;
    }
    public String getNameWithBaseline() {
        return name + "." + Reference.BASELINE.name;
    }



    private enum Reference {
        ACTUAL("actual"),
        EXPECTED_MAX("expected_max"),
        EXPECTED("expected"),
        EXPECTED_MIN("expected_min"),
        PERFORMANCE("performance"),
        INVOICE("invoice"),
        BUDGET("budget"),
        ADJUSTED("adjusted"),
        FORECASTED("forecasted"),
        ESTIMATED("estimated"),
        PLANNED("planned"),
        BASELINE("baseline");

        Reference(String name) {
            this.name = name;
        }

        private String name;


    }


}
