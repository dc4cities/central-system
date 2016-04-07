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

package eu.dc4cities.connectors.ForecastENEAConnector.runner;

import com.google.gson.Gson;
import eu.dc4cities.connectors.ForecastENEAConnector.data.ENEAConnectorParams;
import eu.dc4cities.connectors.ForecastENEAConnector.process.ActualDataUploader;
import eu.dc4cities.connectors.ForecastENEAConnector.process.ForecastDataUploader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;

/**
*
 *
*/

public class ENEAForecastRunner 
{
	private String configFilePath;
	
	public ENEAForecastRunner(String configFilePath)
	{
		this.configFilePath = configFilePath;
	}

    public void execute() throws IOException, URISyntaxException {
        System.out.println("+-----------------------------------+");
        System.out.println("|   ENEA forecast connector START   |");
			System.out.println("+-----------------------------------+");
			System.out.println();
        System.out.print("Reading config from JSON file '" + configFilePath + "'");
        BufferedReader br = new BufferedReader(new FileReader(this.configFilePath));
        Gson gson = new Gson();
			ENEAConnectorParams params = gson.fromJson(br, ENEAConnectorParams.class);
        System.out.print(" DONE!\n");
        System.out.println();

        System.out.println("Trying to upload forecast data...");
			ForecastDataUploader fdu = new ForecastDataUploader(params);
			fdu.execute();
        System.out.print(" ...DONE!\n");
        System.out.println();
        System.out.println("Trying to upload actual data...");
			ActualDataUploader adu = new ActualDataUploader(params);
			adu.execute();
			System.out.print("...DONE!\n");
			System.out.println();
			System.out.println("+----------------------------------+");
			System.out.println("|   ENEA forecast connector END    |");
			System.out.println("+----------------------------------+");
	}

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Missing configuration file");
            System.exit(1);
        }
        ENEAForecastRunner r = new ENEAForecastRunner(args[0]);
        try {
            r.execute();
        } catch (IOException | URISyntaxException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

}
