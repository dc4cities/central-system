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

package eu.dc4cities.controlsystem.tools.logparser;

import com.fasterxml.jackson.core.type.TypeReference;
import eu.dc4cities.controlsystem.model.easc.Activity;
import eu.dc4cities.controlsystem.model.easc.EascActivityPlan;
import eu.dc4cities.controlsystem.model.easc.Work;
import eu.dc4cities.controlsystem.model.json.JsonUtils;
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Duration;
import javax.measure.unit.SI;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class PlannedPowerExtractor {

	public void extract(String logPath, String outputPath, double pue) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(Paths.get(logPath), Charset.forName("UTF-8"))) {
			String line;
			StringBuilder json = new StringBuilder();
			SortedMap<DateTime, PowerAtTime> plannedPower = new TreeMap<>();
			while ((line = reader.readLine()) != null) {
				if (line.endsWith("[OptimizationProcess] Selected activity plans: [{")) {
					json.append("[{");
					while ((line = reader.readLine()) != null) {
						json.append(line);
						if (line.equals("}]")) {
							List<EascActivityPlan> plans = JsonUtils.getDc4CitiesObjectMapper().readValue(
									json.toString(), new TypeReference<List<EascActivityPlan>>(){});
							json.setLength(0);
							updatePlannedPower(plans, plannedPower);
							break;
						}
					}
					if (json.length() > 0) {
						throw new EOFException("EOF reached while reading activity JSON");
					}
				} else {
					continue;
				}
			}
			if (plannedPower.size() == 0) {
				System.err.println("No activity plans found in log file. Destination file not created.");
				return;
			}
			try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputPath), Charset.forName("UTF-8"))) {
				for (PowerAtTime powerAtTime : plannedPower.values()) {
					String timeStamp = powerAtTime.timeStamp.toString("yyyy-MM-dd'T'HH:mm:ss");
					long power = Math.round(powerAtTime.power * pue);
					writer.write(timeStamp + ";" + power);
					writer.newLine();
				}
			}
		}
	}
	
	private class PowerAtTime {
		
		public DateTime timeStamp;
		public long power;
		
		public PowerAtTime(DateTime timeStamp, long power) {
			this.timeStamp = timeStamp;
			this.power = power;
		}
		
	}
	
	private void updatePlannedPower(List<EascActivityPlan> plans, Map<DateTime, PowerAtTime> plannedPower) throws IOException {
		DateTime dateFrom = null, dateTo = null;
		Amount<Duration> slotDuration = null;
		long slotDurationMillis = 0;
		boolean firstPlan = true;
		for (EascActivityPlan plan : plans) {
			if (firstPlan) {
				dateFrom = plan.getDateFrom();
				dateTo = plan.getDateTo();
				slotDuration = plan.getTimeSlotDuration();
				slotDurationMillis = slotDuration.to(SI.MILLI(SI.SECOND)).getExactValue();
				// We assume every new planning completely replaces the previous one
				clearPowerRange(dateFrom, dateTo, slotDurationMillis, plannedPower);
			} else if (!plan.getDateFrom().equals(dateFrom) || !plan.getDateTo().equals(dateTo) 
			           || !plan.getTimeSlotDuration().equals(slotDuration)) {
				throw new IllegalArgumentException("All activity plans must refer to the same interval");
			}
    		for (Activity activity : plan.getActivities()) {
    			for (Work work : activity.getWorks()) {
    				long power = work.getPower().getExactValue();
    				for (int ts = work.getStartTimeSlot(); ts <= work.getEndTimeSlot(); ts++) {
    					DateTime timeStamp = dateFrom.plus(slotDurationMillis * (ts - 1));
    					addPower(timeStamp, power, plannedPower);
    				}
    			}
    		}
    	}
	}
	
	private void clearPowerRange(DateTime dateFrom, DateTime dateTo, long slotDurationMillis, 
			Map<DateTime, PowerAtTime> plannedPower) {
		DateTime timeStamp = dateFrom;
		while (timeStamp.isBefore(dateTo)) {
			PowerAtTime powerAtTime = new PowerAtTime(timeStamp, 0);
			plannedPower.put(timeStamp, powerAtTime);
			timeStamp = timeStamp.plus(slotDurationMillis);
		}
	}
	
	private void addPower(DateTime timeStamp, long power, Map<DateTime, PowerAtTime> plannedPower) {
		PowerAtTime powerAtTime = plannedPower.get(timeStamp);
		if (powerAtTime == null) {
			// All time slots must be in the plan because we initialized them at 0 W
			throw new IllegalArgumentException("Time slot not found for " + timeStamp);
		}
		powerAtTime.power += power;
	}
	
}
