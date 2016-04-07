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

package eu.dc4cities.connectors.ForecastENEAConnector.parser;

import eu.dc4cities.connectors.ForecastENEAConnector.data.Data;
import eu.dc4cities.connectors.ForecastENEAConnector.data.ForecastData;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 *
 */
public class ForecastDataParser {
    String forecastTag;

    public ForecastDataParser() {
    }

    public LinkedList<Data> readData(File f) throws IOException {

        try (BufferedReader br = new BufferedReader(new FileReader(f))) 
        {
            if (f.getName().contains("3gg")) {
                this.forecastTag = "3gg";
            } else {
                this.forecastTag = "1g";
            }

            LinkedList<Data> ll = new LinkedList<Data>();

            String line;
            do {
                line = br.readLine();
            }
            while (line.startsWith("ORE") || line.startsWith(",,") || line.equals(""));

            while (line != null) {
                if (!line.isEmpty()) {
                    StringTokenizer st = new StringTokenizer(line, ",");

                    String date = st.nextToken();
                    long timestamp = this.getDateFromString(date);

                    int ghi = 0;
                    if (st.hasMoreTokens()) {
                        ghi = Integer.parseInt(st.nextToken());
                    }

                    ForecastData fd = new ForecastData(timestamp, ghi, this.forecastTag);
                    ll.add(fd);
                }
                line = br.readLine();
            }
            br.close();
            return ll;
        }
        catch(IOException e)
        {
        	return null;
        }
    }

    private long getDateFromString(String date) {
        int year;
        int month;
        int day;
        int hours;
        int minutes;

        StringTokenizer st = new StringTokenizer(date, "/ .");

        day = Integer.parseInt(st.nextToken());
        month = Integer.parseInt(st.nextToken());
        year = 2000 + Integer.parseInt(st.nextToken());
        hours = Integer.parseInt(st.nextToken());
        minutes = Integer.parseInt(st.nextToken());
        
        /*
        double qTmp = 60 / (double) minutes;
        int intPart = (int) qTmp;
        double realPart = qTmp - intPart;
        int round = (int) Math.round(realPart);
        */
        double qTmp = 4 * (double) minutes / 60;
        int intPart = (int) qTmp;
        double realPart = qTmp - intPart;
        int round = (int) Math.round(realPart);

        int minRounded = (intPart + round) * 15;

        boolean nextHour = false;
        if (minRounded == 60) {
            minRounded = 0;
            nextHour = true;
        }

        DateTime timeDate = new DateTime(year, month, day, hours, minRounded, DateTimeZone.UTC);

        if (nextHour) {
            timeDate = timeDate.plusHours(1);
        }

        return timeDate.getMillis();
    }

	/*
    private long getDateFromString(String date)
	{		
		int year;
		int month;
		int day;
		int hours;
		int minutes;
		
		StringTokenizer st = new StringTokenizer(date, "/ .");
		
		day = Integer.parseInt(st.nextToken());
		month = Integer.parseInt(st.nextToken()) - 1;
		year = 2000 + Integer.parseInt(st.nextToken());
		hours = Integer.parseInt(st.nextToken());
		minutes = Integer.parseInt(st.nextToken());
		
		double qTmp = 60/(double)minutes;
		int intPart = (int)qTmp;
		double realPart = qTmp - intPart;
		int round = (int)Math.round(realPart);
		
		int minRounded = intPart*15 + round;
		
		boolean nextHour = false;
		if (minRounded == 60)
		{
			minRounded = 0;
			nextHour = true;
		}
		
		GregorianCalendar gc = new GregorianCalendar(year, month, day, hours, minRounded);
		if(nextHour)
		{
			gc.add(Calendar.HOUR_OF_DAY, 1);
		}
		
		return gc.getTimeInMillis();
	}
	*/


}
