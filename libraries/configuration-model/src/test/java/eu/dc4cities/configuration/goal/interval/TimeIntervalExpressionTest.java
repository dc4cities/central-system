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

package eu.dc4cities.configuration.goal.interval;

import eu.dc4cities.configuration.goal.Base;
import eu.dc4cities.configuration.goal.TimeFrame;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.LocalDate;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Unit tests for TimeExpression implementations
 *
 *
 * 
 */
public class TimeIntervalExpressionTest extends Base {

	/**
	 * Absolute finite TimeFrame
	 * 
	 * @throws TimeIntervalExpressionException
	 */
	@Test
	public void testTimeFrame_absolute() throws TimeIntervalExpressionException {
		// time zero .. 1970 ..
		Date startDate = new Date(0);

		// 3 hours
		String duration = "PT3H";

		TimeFrame timeFrame = createTimeFrame(duration, null, startDate);

		// CUT
		TimeIntervalExpression classUnderTest = TimeIntervalExpressionFactory.create(timeFrame);

		// asserts
		assertTrue(classUnderTest instanceof AbsoluteTimeIntervalExpression);

		assertFalse(classUnderTest.isInfinite());
		assertFalse(classUnderTest.isActive(new Date()));
		assertTrue(classUnderTest.isActive(new Date(1000 * 60 * 60)));

		assertEquals(startDate, classUnderTest.getStartDate(null));
		assertEquals(DateUtils.addHours(startDate, 3),
				classUnderTest.getEndDate(null));
	}

	/**
	 * Absolute infinite TimeFrame (e.g. static time frame)
	 * 
	 * @throws TimeIntervalExpressionException
	 */
	@Test
	public void testTimeFrame_absolute_infinite()
			throws TimeIntervalExpressionException {
		// time zero .. 1970 ..
		Date startDate = new Date(0);

		TimeFrame timeFrame = createTimeFrame(null, null, startDate);

		// CUT
		TimeIntervalExpression classUnderTest = TimeIntervalExpressionFactory.create(timeFrame);

		assertTrue(classUnderTest instanceof AbsoluteTimeIntervalExpression);

		assertTrue(classUnderTest.isInfinite());
		assertTrue(classUnderTest.isActive(new Date()));

		assertEquals(startDate, classUnderTest.getStartDate(null));
		assertNull(classUnderTest.getEndDate(null));
	}

	/**
	 * Recurrent TimeFrame
	 * 
	 * @throws TimeIntervalExpressionException
	 */
	@Test
	public void testTimeFrame_recurrent() throws TimeIntervalExpressionException {
		// 3 hours
		String duration = "PT3H";

		String recurrentExpression = "0 0 0 ? * SUN";

		TimeFrame timeFrame = createTimeFrame(duration, recurrentExpression,
				null);

		// CUT
		TimeIntervalExpression classUnderTest = TimeIntervalExpressionFactory.create(timeFrame);

		assertTrue(classUnderTest instanceof RecurrentTimeIntervalExpression);

		assertFalse(classUnderTest.isInfinite());
		assertFalse(classUnderTest.isActive(new Date()));

		// next sunday (cf. recurrentExpression)
		Date nextSunday = getNextDay(new LocalDate(), 7 /* sunday */).toDate();

		Date currentDate = new Date();
		assertEquals(nextSunday, classUnderTest.getStartDate(currentDate));
		assertEquals(DateUtils.addHours(nextSunday, 3),
				classUnderTest.getEndDate(currentDate));
	}

	private static LocalDate getNextDay(LocalDate currentDate, int day) {
		return currentDate.isBefore(currentDate.dayOfWeek().setCopy(day)) ? currentDate
				.dayOfWeek().setCopy(day) : currentDate.plusWeeks(1)
				.dayOfWeek().setCopy(day);
	}
}
