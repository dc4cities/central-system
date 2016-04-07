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

package eu.dc4cities.connectors.ForecastENEAConnector.process;

import eu.dc4cities.connectors.ForecastENEAConnector.data.Data;
import eu.dc4cities.connectors.ForecastENEAConnector.data.ENEAConnectorParams;
import eu.dc4cities.connectors.ForecastENEAConnector.dbconnection.HistoricalDBConnector;
import eu.dc4cities.connectors.ForecastENEAConnector.file.FileManager;
import eu.dc4cities.connectors.ForecastENEAConnector.parser.ActualDataParser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 *
 */

public class ActualDataUploader {
    private ENEAConnectorParams ecp;

    public ActualDataUploader(ENEAConnectorParams ecp) {
        this.ecp = ecp;
    }

    public void execute() throws URISyntaxException, IOException {
        FileManager fm = new FileManager(ecp.getActualDataPath(), ecp.getLoadedActualFilePath(), ecp.isActualReset(), true);
        ActualDataParser adp = new ActualDataParser();

        int counter = 0;
        boolean newData = true;
        while (newData && fm.hasNext()) {
            File f = fm.nextFile();
            if (f != null) 
            {
                List<Data> forecastData = adp.readData(f);
                if(forecastData!=null)
                {
                	HistoricalDBConnector dbf = new HistoricalDBConnector(ecp.getHdbHost(), ecp.getHdbPort(), ecp.getAssetType(), ecp.getAssetCode(), ecp.getCompanyCode());
                    System.out.print("\tUploading data from file " + f.getName() + " ...");
                    dbf.uploadData(forecastData);
                    System.out.print(" DONE!\n");
                }
                else
                {
                	System.out.print("\tFile " + f.getName() + " damaged... SKIPPED!\n");
                }                	
                counter++;
            } else {
                newData = false;
                System.out.println("\tNo new data to be uploaded!");
            }
        }
        if (counter != 0) System.out.println("\tUploaded data from " + counter + " new files!");
        fm.closeFileWriter();
    }
}
