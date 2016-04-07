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

import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		if (args.length == 0) {
			printUsageAndExit();
		} else if (args[0].equals("extract-planned-power")) {
			if (args.length < 3) {
				printUsageAndExit();
			}
			String logPath = args[1];
			String outputPath = args[2];
			String pueStr = args[3];
			if (logPath.length() == 0 || outputPath.length() == 0 || pueStr.length() == 0) {
				printUsageAndExit();
			}
			double pue = parseDoubleOrExit(pueStr, "pue");
			try {
				PlannedPowerExtractor extractor = new PlannedPowerExtractor();
				extractor.extract(logPath, outputPath, pue);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		} else {
			printUsageAndExit();
		}
	}
	
	private static void printUsageAndExit() {
		System.err.println("USAGE: java -jar log-parser.jar extract-planned-power <log-path> <output-path> <pue>");
		System.exit(1);
	}
	
	private static double parseDoubleOrExit(String value, String name) {
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			System.err.println("Invalid value for " + name + ": not a number (" + value + ")");
			System.exit(1);
			// Make the compiler happy
			return 0;
		}
	}
	
}
