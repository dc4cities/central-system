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

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class PlannedPowerExtractorTest {
	
	private static final double PUE = 1.5;
	
	private PlannedPowerExtractor extractor = new PlannedPowerExtractor();
	
	private void testExtractor(String source, String expected) throws IOException {
		testExtractor(toFile(source), toFile(expected));
	}
	
	private File toFile(String resource) {
		if (resource == null) {
			return null;
		}
		return new File(getClass().getClassLoader().getResource("planned-power-extractor/" + resource).getPath());
	}
	
	private void testExtractor(File source, File expected) throws IOException {
		File output = File.createTempFile("test-extractor-dest", null);
		try {
			// We only need the file name; the file should be created by the extractor
			if (!output.delete()) {
				throw new IOException("Couldn't delete " + output);
			}
			extractor.extract(source.getCanonicalPath(), output.getCanonicalPath(), PUE);
			if (expected == null) {
				if (output.exists()) {
					Assert.fail("Extractor created an output file while none was expected");
				}
			} else {
				if (!FileUtils.contentEquals(output, expected)) {
					Assert.fail(getUnexpectedOutputMessage(expected, output));
				}
			}
		} finally {
			output.delete();
		}
	}
	
	private String getUnexpectedOutputMessage(File expected, File actual) {
		String message = "Unexpected output content:\n";
		message += "expected:\n" + readFileToString(expected);
		message += "actual:\n" + readFileToString(actual);
		return message;
	}
	
	private String readFileToString(File file) {
		try {
			return FileUtils.readFileToString(file);
		} catch (Exception e) {
			return "Couldn't read " + file + ": " + e;
		}
	}
	
	@Test
	public void testEmptyLog() throws IOException {
		// Verifies that the method returns without creating the output when the log is empty
		File source = File.createTempFile("test-extractor-empty", null);
		try {
			testExtractor(source, null);
		} finally {
			source.delete();
		}
	}
	
	@Test
	public void testWithoutPlan() throws IOException {
		// Verifies that the method returns without creating the output when the log doesn't contain any activity plan
		testExtractor("without-plan.log", null);
	}
	
	@Test
	public void testMultiplePlans() throws IOException {
		// Verifies that the output is correct when the log contains two activity plans, with the second one partly changing the
		// first one: there must be at least a time slot in which power is removed, one in which power is changed and one time slot
		// that had null power in which power is allocated.
		testExtractor("multiple-plans.log", "multiple-plans.csv");
	}

}
