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

package eu.dc4cities.connectors.ForecastENEAConnector.dbconnection;

import eu.dc4cities.connectors.ForecastENEAConnector.data.ActualData;
import eu.dc4cities.connectors.ForecastENEAConnector.data.Data;
import org.kairosdb.client.HttpClient;
import org.kairosdb.client.builder.Metric;
import org.kairosdb.client.builder.MetricBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 *
 */
public class HistoricalDBConnector {
    private String host;
    private int port;

    private String assetType;
    private String assetCode;
    private String companyCode;

    public HistoricalDBConnector(String host, int port, String assetType, String assetCode, String companyCode) {
        this.host = host;
        this.port = port;
        this.assetType = assetType;
        this.assetCode = assetCode;
        this.companyCode = companyCode;
    }

    public void uploadData(List<Data> l) throws URISyntaxException, IOException {
        MetricBuilder ghiBuilder = MetricBuilder.getInstance();
        MetricBuilder temperatureBuilder = null;

        if (l.get(0).getDataType().equalsIgnoreCase("forecasted")) {
            Metric ghi = ghiBuilder.addMetric("irradiance")
                    .addTag("assetType", this.assetType)
                    .addTag("assetCode", this.assetCode)
                    .addTag("companyCode", this.companyCode)
                    .addTag("reference", "forecasted");

            for (int i = 0; i < l.size(); i++) {
                Data d = l.get(i);
                ghi.addDataPoint(d.getTimestamp(), d.getGHI());
            }
        } else {
            Metric ghi = ghiBuilder.addMetric("irradiance")
                    .addTag("assetType", this.assetType)
                    .addTag("assetCode", this.assetCode)
                    .addTag("companyCode", this.companyCode)
                    .addTag("reference", "actual");

            temperatureBuilder = MetricBuilder.getInstance();
            Metric temperature = temperatureBuilder.addMetric("module_temperature")
                    .addTag("assetType", this.assetType)
                    .addTag("assetCode", this.assetCode)
                    .addTag("companyCode", this.companyCode)
                    .addTag("reference", "actual");

            for (int i = 0; i < l.size(); i++) {
                Data d = l.get(i);
                ghi.addDataPoint(d.getTimestamp(), d.getGHI());
                temperature.addDataPoint(d.getTimestamp(), ((ActualData) d).getTemperature());
            }
        }

    	/*
    	Metric ghi = ghiBuilder.addMetric("irradiation")
    		.addTag("assetType", this.assetType)
    		.addTag("assetCode", this.assetCode)
    		.addTag("companyCode", this.companyCode);
    	    		
    	MetricBuilder tempBuilder = null;
    	Metric temp = null; 
    	if(l.get(0).getDataType().equalsIgnoreCase("ACTUAL"))
    	{
    		tempBuilder = MetricBuilder.getInstance();
    		temp = tempBuilder.addMetric("module_temperature")
        			.addTag("assetType", this.assetType)
        			.addTag("assetCode", this.assetCode)
        			.addTag("companyCode", this.companyCode)
    				.addTag("reference", "ACTUAL");
    		
    		ghi.addTag("reference", "ACTUAL");
    	}
    	else
    	{
    		ghi.addTag("reference", "FORECASTED");
    	}
    	
    	for(int i=0 ; i<l.size() ; i++)
    	{
    		Data d = l.get(i);
    		ghi.addDataPoint(d.getTimestamp(), d.getGHI());
    		if(temp!=null)
    		{    			
    			temp.addDataPoint(d.getTimestamp(), ((ActualData)d).getTemperature());
    		}
    	}
    	*/

        HttpClient client = new HttpClient(this.host+":"+this.port);
        client.pushMetrics(ghiBuilder);
        if (temperatureBuilder != null) {
            client.pushMetrics(temperatureBuilder);
        }
        client.shutdown();
    }
}
