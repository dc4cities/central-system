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

import org.joda.time.DateTime;

/**
 *
 *
 */
public class SiteDetail {

    private Long id;
    private String code;
    private String name;
    private String companyCode;
    private String parentCode;
    private EnergyClass energyClass;
    private Double co2ElecFactor;
    private Double co2GasFactor;
    private Double co2GasFactor2;
    private DateTime constructionDate;
    private String ecoefficient;
    private Double gasFactor;
    private Integer occupants;
    private Double oilFactor;
    private Double surface;
    private String street;
    private String streetNumber;
    private String box;
    private String postalCode;
    private String city;
    private String country;
    private Integer numberOfMeter;
    private Integer numberOfSubMeter;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public EnergyClass getEnergyClass() {
        return energyClass;
    }

    public void setEnergyClass(EnergyClass energyClass) {
        this.energyClass = energyClass;
    }

    public Double getCo2ElecFactor() {
        return co2ElecFactor;
    }

    public void setCo2ElecFactor(Double co2ElecFactor) {
        this.co2ElecFactor = co2ElecFactor;
    }

    public Double getCo2GasFactor() {
        return co2GasFactor;
    }

    public void setCo2GasFactor(Double co2GasFactor) {
        this.co2GasFactor = co2GasFactor;
    }

    public Double getCo2GasFactor2() {
        return co2GasFactor2;
    }

    public void setCo2GasFactor2(Double co2GasFactor2) {
        this.co2GasFactor2 = co2GasFactor2;
    }

    public DateTime getConstructionDate() {
        return constructionDate;
    }

    public void setConstructionDate(DateTime constructionDate) {
        this.constructionDate = constructionDate;
    }

    public String getEcoefficient() {
        return ecoefficient;
    }

    public void setEcoefficient(String ecoefficient) {
        this.ecoefficient = ecoefficient;
    }

    public Double getGasFactor() {
        return gasFactor;
    }

    public void setGasFactor(Double gasFactor) {
        this.gasFactor = gasFactor;
    }

    public Integer getOccupants() {
        return occupants;
    }

    public void setOccupants(Integer occupants) {
        this.occupants = occupants;
    }

    public Double getOilFactor() {
        return oilFactor;
    }

    public void setOilFactor(Double oilFactor) {
        this.oilFactor = oilFactor;
    }

    public Double getSurface() {
        return surface;
    }

    public void setSurface(Double surface) {
        this.surface = surface;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getBox() {
        return box;
    }

    public void setBox(String box) {
        this.box = box;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getNumberOfMeter() {
        return numberOfMeter;
    }

    public void setNumberOfMeter(Integer numberOfMeter) {
        this.numberOfMeter = numberOfMeter;
    }

    public Integer getNumberOfSubMeter() {
        return numberOfSubMeter;
    }

    public void setNumberOfSubMeter(Integer numberOfSubMeter) {
        this.numberOfSubMeter = numberOfSubMeter;
    }

}
