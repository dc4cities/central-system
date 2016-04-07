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

package eu.dc4cities.connectors.ForecastENEAConnector.data;

import com.google.gson.annotations.Expose;

public class ENEAConnectorParams {

    @Expose
    private String forecastDataPath;
    @Expose
    private String actualDataPath;
    @Expose
    private String loadedForecastFilePath;
    @Expose
    private String loadedActualFilePath;
    @Expose
    private boolean forecastReset;
    @Expose
    private boolean actualReset;
    @Expose
    private boolean addRecursive;
    @Expose
    private String hdbHost;
    @Expose
    private int hdbPort;
    @Expose
    private String assetType;
    @Expose
    private String assetCode;
    @Expose
    private String companyCode;

    public String getForecastDataPath() {
        return forecastDataPath;
    }

    public void setForecastDataPath(String forecastDataPath) {
        this.forecastDataPath = forecastDataPath;
    }

    public String getActualDataPath() {
        return actualDataPath;
    }

    public void setActualDataPath(String actualDataPath) {
        this.actualDataPath = actualDataPath;
    }

    public String getLoadedForecastFilePath() {
        return loadedForecastFilePath;
    }

    public void setLoadedForecastFilePath(String loadedForecastFilePath) {
        this.loadedForecastFilePath = loadedForecastFilePath;
    }

    public String getLoadedActualFilePath() {
        return loadedActualFilePath;
    }

    public void setLoadedActualFilePath(String loadedActualFilePath) {
        this.loadedActualFilePath = loadedActualFilePath;
    }

    public boolean isForecastReset() {
        return forecastReset;
    }

    public void setForecastReset(boolean forecastReset) {
        this.forecastReset = forecastReset;
    }

    public boolean isActualReset() {
        return actualReset;
    }

    public void setActualReset(boolean actualReset) {
        this.actualReset = actualReset;
    }

    public boolean isAddRecursive() {
        return addRecursive;
    }

    public void setAddRecursive(boolean addRecursive) {
        this.addRecursive = addRecursive;
    }

    public String getHdbHost() {
        return hdbHost;
    }

    public void setHdbHost(String hdbHost) {
        this.hdbHost = hdbHost;
    }

    public int getHdbPort() {
        return hdbPort;
    }

    public void setHdbPort(int hdbPort) {
        this.hdbPort = hdbPort;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

}