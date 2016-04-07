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
import eu.dc4cities.connectors.ForecastENEAConnector.parser.ForecastDataParser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;

/**
 *
 */

public class ForecastDataUploader {
    private ENEAConnectorParams ecp;

    public ForecastDataUploader(ENEAConnectorParams ecp) {
        this.ecp = ecp;
    }

    public void execute() throws URISyntaxException, IOException {
        ForecastComparator fc = new ForecastComparator();
        FileManager fm = new FileManager(ecp.getForecastDataPath(), ecp.getLoadedForecastFilePath(), ecp.isForecastReset(), ecp.isAddRecursive());
        fm.sort(fc);

        ForecastDataParser fdp = new ForecastDataParser();
        int counter = 0;
        boolean newData = true;
        while (newData && fm.hasNext()) {
            File f = fm.nextFile();
            if (f != null) 
            {
            	List<Data> forecastData = fdp.readData(f);
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

    class ForecastComparator implements Comparator<String> {
        public int compare(String f1, String f2) {
            String tmp1 = null;
            String tmp2 = null;

            if (f1.contains("3gg")) {
                tmp1 = "1_";
                int length = f1.length();
                String y1 = f1.substring(length - 8, length - 4);
                String m1 = f1.substring(length - 11, length - 9);
                String d1 = f1.substring(length - 14, length - 12);
                tmp1 += y1 + "-" + m1 + "-" + d1;
            } else {
                tmp1 = "2_";
                int length = f1.length();
                String y1 = f1.substring(length - 17, length - 13);
                String m1 = f1.substring(length - 20, length - 18);
                String d1 = f1.substring(length - 23, length - 21);
                String mm1 = f1.substring(length - 9, length - 7);
                String hh1 = f1.substring(length - 12, length - 10);
                tmp1 += y1 + "-" + m1 + "-" + d1 + "-" + hh1 + ":" + mm1;
            }

            if (f2.contains("3gg")) {
                tmp2 = "1_";
                int length = f2.length();
                String y2 = f2.substring(length - 8, length - 4);
                String m2 = f2.substring(length - 11, length - 9);
                String d2 = f2.substring(length - 14, length - 12);
                tmp2 += y2 + "-" + m2 + "-" + d2;
            } else {
                tmp2 = "2_";
                int length = f2.length();
                String y2 = f2.substring(length - 17, length - 13);
                String m2 = f2.substring(length - 20, length - 18);
                String d2 = f2.substring(length - 23, length - 21);
                String mm2 = f2.substring(length - 9, length - 7);
                String hh2 = f2.substring(length - 12, length - 10);
                tmp2 += y2 + "-" + m2 + "-" + d2 + "-" + hh2 + ":" + mm2;
            }

            return tmp1.compareTo(tmp2);
        }
    }
}
