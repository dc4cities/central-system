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

import eu.dc4cities.connectors.ForecastENEAConnector.runner.ENEAForecastRunner;


/**
 *
 */
public class TestAll {
    public static void main(String[] args) throws Exception {
        ENEAForecastRunner efr = new ENEAForecastRunner(args[0]);
        //ENEAForecastRunner efr = new ENEAForecastRunner("config/eneaConnectorParams.json");
        efr.execute();

		/*
        try
		{
			System.out.println("+-----------------------------------+");
			System.out.println("|   ENEA forecast connector START   |");
			System.out.println("+-----------------------------------+");
			System.out.println();
			System.out.print("Reading config from JSON file... ");
			BufferedReader br = new BufferedReader(new FileReader("config/eneaConnectorParams.json"));
			Gson gson = new Gson();
			ENEAConnectorParams params = gson.fromJson(br, ENEAConnectorParams.class);
			System.out.print("DONE!\n");
			System.out.println();
			
			System.out.println("Trying to upload forecast data...");
			ForecastDataUploader fdu = new ForecastDataUploader(params);
			fdu.execute();
			System.out.print("...DONE!\n");
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
		catch(Exception e)
		{
			e.printStackTrace();
		}
		*/
    }

}
