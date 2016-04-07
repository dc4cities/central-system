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
import org.joda.time.Interval;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import java.util.Date;

/**
 * Absolute time expression capable of handling ABSOLUTE time frames
 *
 *
 * 
 */
public class AbsoluteTimeIntervalExpression implements TimeIntervalExpression {

	private TimeFrame timeFrame;
	protected final Duration duration;

	/**
	 * @param timeFrame
	 *            Time frame
	 * @throws TimeIntervalExpressionException
	 *             Processing of TimeFrame failed
	 */
	AbsoluteTimeIntervalExpression(TimeFrame timeFrame) throws TimeIntervalExpressionException {
		Validate.notNull(timeFrame, "TimeFrame cannot be null");

		this.timeFrame = timeFrame;

		try {
			// assuming empty duration or P0D denotes "forever" (e.g. static
			// objective) ..
			if (StringUtils.equals(timeFrame.getDuration(), "POD")
					|| StringUtils.isEmpty(timeFrame.getDuration())) {
				this.duration = null;
			} else {
				this.duration = DatatypeFactory.newInstance().newDuration(
						timeFrame.getDuration());
			}
		} catch (Throwable e) {
			throw new TimeIntervalExpressionException("Couldn't read duration "
					+ timeFrame, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActive(Date date) {
		Date startDate = getStartDate(date);

		if (isInfinite()) {
			return date.after(startDate);
		} else {
			Date endDate = getEndDate(date);

			Interval interval = new Interval(startDate.getTime(),
					endDate.getTime());
			return interval.contains(date.getTime());
		}
	}

	/**
	 * Returns null if no end date (infinite duration)
	 */
	@Override
	public Date getEndDate(Date date) {
		if (isInfinite()) {
			return null;
		} else {
			Date startDate = getStartDate(date);
			duration.addTo(startDate);

			return startDate;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getStartDate(Date date) {
		Date startDate = getTimeFrame().getStartDate();

		Validate.notNull(timeFrame.getStartDate(), "Start date cannot be null");

		return (Date) startDate.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isInfinite() {
		return duration == null;
	}

	/**
	 * @return the timeFrame
	 */
	protected TimeFrame getTimeFrame() {
		return timeFrame;
	}
}
