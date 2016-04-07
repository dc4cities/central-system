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

package eu.dc4cities.configuration.goal.utils;

import eu.dc4cities.configuration.goal.Base;
import eu.dc4cities.configuration.goal.Objective;
import eu.dc4cities.configuration.goal.TimeFrame;
import eu.dc4cities.configuration.goal.interval.TimeIntervalExpressionException;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for ObjectiveUtils
 *
 *
 * 
 */
public class ObjectiveUtilsTest extends Base {

	@Test
	public void testFilterActiveObjectives()
			throws TimeIntervalExpressionException {
		List<Objective> objectives = new ArrayList<Objective>();

		// infinite from now on
		objectives
				.add(createObjective(createTimeFrame(null, null, new Date())));
		// active for 6hous starting from tomorrow
		objectives.add(createObjective(createTimeFrame("PT6H", null,
				DateUtils.addDays(new Date(), 1))));
		// starts now and is active for one hour
		objectives
				.add(createObjective(createTimeFrame("PT1H", null, new Date())));

		// add a minute to now to be in the time interval
		Date currentDate = DateUtils.addMinutes(new Date(), 1);

		List<Objective> activeObjectives = ObjectiveUtils
				.filterActiveObjectives(objectives, currentDate);

		// asserts
		assertEquals(2, activeObjectives.size());
	}

	private static Objective createObjective(TimeFrame timeFrame) {
		Objective obj = new Objective();

		obj.setTimeFrame(timeFrame);

		return obj;
	}
}
