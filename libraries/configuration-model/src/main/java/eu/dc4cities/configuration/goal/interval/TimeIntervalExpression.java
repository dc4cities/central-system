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

import java.util.Date;

/**
 * Interface for TimeFrame validation of objectives
 *
 *
 * 
 */
public interface TimeIntervalExpression {

	/**
	 * @param date
	 *            "Current" date (mainly required for RECURRENT time expressions
	 *            to determine the closest date for given date). Can be null for
	 *            ABSOLUTE time expressions
	 * @return start date (active starting from)
	 */
	public Date getStartDate(Date date);

	/**
	 * @param date
	 *            "Current" date (mainly required for RECURRENT time expressions
	 *            to determine the closest date for given date). Can be null for
	 *            ABSOLUTE time expressions
	 * @return end date (active until), returns null if time expression has
	 *         infinite duration
	 */
	public Date getEndDate(Date date);

	/**
	 * @return true if time expression is infinite starting from start date,
	 *         otherwise false
	 */
	public boolean isInfinite();

	/**
	 * @param date
	 *            Date
	 * @return true if time expression matches given date
	 */
	public boolean isActive(Date date);
}
