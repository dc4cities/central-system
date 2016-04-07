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

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 */
public class MetricDetail {

    private Long id;
    private String name;
    private AggregationFunction aggregation;
    private Medium medium;
    private String defaultLabel;
    private Double scaleFactor;
    private String unit;
    private Reference reference;
    private AssetType assetType;

    private List<Equation> equations = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AggregationFunction getAggregation() {
        return aggregation;
    }

    public void setAggregation(AggregationFunction aggregation) {
        this.aggregation = aggregation;
    }

    public Medium getMedium() {
        return medium;
    }

    public void setMedium(Medium medium) {
        this.medium = medium;
    }

    public String getDefaultLabel() {
        return defaultLabel;
    }

    public void setDefaultLabel(String defaultLabel) {
        this.defaultLabel = defaultLabel;
    }

    public Double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(Double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public List<Equation> getEquations() {
        return equations;
    }

    public void setEquations(List<Equation> equations) {
        this.equations = equations;
    }

    public enum AggregationFunction {

        NONE,
        SUM,
        AVG,
        MIN,
        MAX;

    }

    public enum Medium {

        UNKNOWN,
        ELECTRICITY,
        GAS,
        WATER,
        OIL,
        HEAT,
        ENVIRONMENT,
        EQUIPMENT;

    }

}
