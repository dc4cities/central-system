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

import eu.dc4cities.configuration.goal.TimeFrame;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for TimeExpressions
 *
 *
 * 
 */
public class TimeIntervalExpressionFactory {

	private static final Logger LOG = LoggerFactory
			.getLogger(TimeIntervalExpressionFactory.class);

	/**
	 * Factory method to create TimeExpressions from TimeFrames
	 * 
	 * @param timeFrame
	 *            TimeFrame
	 * @return TimeExpression instance
	 * @throws TimeIntervalExpressionException
	 *             Creation of TimeExpression failed
	 */
	public static TimeIntervalExpression create(TimeFrame timeFrame)
			throws TimeIntervalExpressionException {
		Validate.notNull(timeFrame, "TimeFrame cannot be null");

		// recurrent
		if (StringUtils.isNotBlank(timeFrame.getRecurrentExpression())) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("Discovered RECURRENT TimeFrame: "
						+ ToStringBuilder.reflectionToString(timeFrame));
			}

			return new RecurrentTimeIntervalExpression(timeFrame);
		} else {
			// absolute
			if (LOG.isTraceEnabled()) {
				LOG.trace("Discovered ABSOLUTE TimeFrame: "
						+ ToStringBuilder.reflectionToString(timeFrame));
			}

			return new AbsoluteTimeIntervalExpression(timeFrame);
		}
	}
}
