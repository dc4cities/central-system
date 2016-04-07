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

package eu.dc4cities.connectors.ForecastENEAConnector.test;

import com.google.gson.Gson;
import eu.dc4cities.connectors.ForecastENEAConnector.data.ENEAConnectorParams;
import eu.dc4cities.connectors.ForecastENEAConnector.process.ActualDataUploader;

import java.io.BufferedReader;
import java.io.FileReader;


/**
 *
 */
public class TestActual {
    public static void main(String[] args) {
        Gson gson = new Gson();
        try {
            System.out.println("----------------------------");
            System.out.print("Reading config from JSON file... ");
            BufferedReader br = new BufferedReader(new FileReader("config/eneaConnectorParams.json"));
            //convert the json string back to object
            ENEAConnectorParams params = gson.fromJson(br, ENEAConnectorParams.class);
            System.out.print("DONE!\n");
            System.out.println("----------------------------");

            System.out.println("Trying to upload actual data...");
            ActualDataUploader adu = new ActualDataUploader(params);
            adu.execute();
            System.out.print("...DONE!\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
