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

package eu.dc4cities.configuration.goal;

import java.util.Date;

/**
 * Time frame in which an objective is active (either ABSOLUTE or RECURRENT)
 *
 *
 * 
 */
public class TimeFrame {

	/**
	 * Start date of ABSOLUTE time frame
	 */
	private Date startDate;

	/**
	 * Recurrent time frame expression
	 * 
	 * @see org.quartz.CronExpression
	 * @see <a href="http://en.wikipedia.org/wiki/Cron#CRON_expression">Short
	 *      format description</>
	 */
	private String recurrentExpression;

	/**
	 * Duration according to ISO 8601
	 * 
	 * @see javax.xml.datatype.Duration
	 * @see <a href="http://en.wikipedia.org/wiki/ISO_8601#Duration">Short
	 *      format description</>
	 */
	private String duration;

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 *            the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the recurrentExpression
	 */
	public String getRecurrentExpression() {
		return recurrentExpression;
	}

	/**
	 * @param recurrentExpression
	 *            the recurrentExpression to set
	 */
	public void setRecurrentExpression(String recurrentExpression) {
		this.recurrentExpression = recurrentExpression;
	}

	/**
	 * @return the duration
	 */
	public String getDuration() {
		return duration;
	}

	/**
	 * @param duration
	 *            the duration to set
	 */
	public void setDuration(String duration) {
		this.duration = duration;
	}
}
