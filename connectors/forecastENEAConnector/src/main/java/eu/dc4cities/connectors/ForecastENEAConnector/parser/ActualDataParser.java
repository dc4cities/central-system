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

package eu.dc4cities.connectors.ForecastENEAConnector.parser;

import eu.dc4cities.connectors.ForecastENEAConnector.data.ActualData;
import eu.dc4cities.connectors.ForecastENEAConnector.data.Data;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;



/**
 *
 */
public class ActualDataParser {

    public ActualDataParser() {
    }

    public LinkedList<Data> readData(File f) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(f))) 
        {
            LinkedList<Data> ll = new LinkedList<Data>();

            String line = br.readLine();
            StringTokenizer st = new StringTokenizer(line, ",");

            float ghi = Float.parseFloat(st.nextToken());
            float temp = Float.parseFloat(st.nextToken());
            String offsetTime = st.nextToken();
            String acquisitionDate = st.nextToken();
            long timestamp = this.getDateFromString(offsetTime, acquisitionDate);

            ActualData ad = new ActualData(timestamp, ghi, temp);
            ll.add(ad);
            br.close();
            return ll;
        }
        catch (IOException | NoSuchElementException e)
        {
        	return null;
        }
    }


    private long getDateFromString(String offsetTime, String acquisitionDate) {
        int year;
        int month;
        int day;
        int hours;
        int minutes;
        int seconds;

        StringTokenizer st1 = new StringTokenizer(acquisitionDate, "/ ");
        month = Integer.parseInt(st1.nextToken());
        day = Integer.parseInt(st1.nextToken());
        year = Integer.parseInt(st1.nextToken());

        DateTime date = new DateTime(year, month, day, 0, 0, DateTimeZone.UTC);

        StringTokenizer st2 = new StringTokenizer(offsetTime, ".");
        int hTmp = Integer.parseInt(st2.nextToken());
        int mTmp = Integer.parseInt(st2.nextToken());

        if (Double.parseDouble(offsetTime) < 0) {
            hours = 24 - hTmp;
            if (mTmp == 0)
                minutes = 0;
            else {
                minutes = 60 * mTmp / 10;
                hours -= 1;
            }
            date = date.minusDays(1);
        } else {
            hours = hTmp;
            if (mTmp == 0)
                minutes = 0;
            else {
                minutes = 60 * mTmp / 10;
            }
        }

        date = date.withHourOfDay(hours).withMinuteOfHour(minutes);

        return date.getMillis();
    }
    
    /*
    private long getDateFromString(String offsetTime, String acquisitionDate ) 
	{
		int year;
		int month;
		int day;
		int hours;
		int minutes;
		int seconds;
		
		StringTokenizer st1 = new StringTokenizer(acquisitionDate, "/ ");
		month = Integer.parseInt(st1.nextToken()) - 1;
		day = Integer.parseInt(st1.nextToken());
		year = Integer.parseInt(st1.nextToken());
		
		
		
		GregorianCalendar gc = new GregorianCalendar(year, month, day);
		
		StringTokenizer st2 = new StringTokenizer(offsetTime, ".");
		int hTmp = Integer.parseInt(st2.nextToken());
		int mTmp = Integer.parseInt(st2.nextToken());
		
		if(Integer.parseInt(offsetTime)<0)
		{
			hours = 24-hTmp;			
			if(mTmp == 0)
				minutes = 0;
			else
			{
				minutes = 60*mTmp/10;
				hours -= 1;
			}		
			gc.add(Calendar.DAY_OF_MONTH, -1);
		}
		else
		{
			hours = hTmp;
			if(mTmp == 0)
				minutes = 0;
			else
			{
				minutes = 60*mTmp/10;
			}
		}
		
		gc.set(Calendar.HOUR_OF_DAY, hours);
		gc.set(Calendar.MINUTE, minutes);
		
		return gc.getTimeInMillis();
	}
    */
}
