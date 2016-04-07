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
import org.apache.commons.lang3.Validate;
import org.quartz.CronExpression;

import java.util.Date;

/**
 * Recurrent TimeExpression based on Cron expressions using Quartz's
 * CronExpression.
 *
 *
 */
public class RecurrentTimeIntervalExpression extends AbsoluteTimeIntervalExpression {

    private final CronExpression cronExpression;

    /**
     * @param timeFrame TimeFrame
     * @throws TimeIntervalExpressionException Processing of TimeFrame failed
     */
    RecurrentTimeIntervalExpression(TimeFrame timeFrame) throws TimeIntervalExpressionException {
        super(timeFrame);

        Validate.notBlank(timeFrame.getRecurrentExpression(),
                "Recurrent time expression is blank");

        // parse
        try {
            this.cronExpression = new CronExpression(
                    timeFrame.getRecurrentExpression());
        } catch (Throwable e) {
            throw new TimeIntervalExpressionException("Couldn't parse expression "
                    + timeFrame.getRecurrentExpression(), e);
        }
    }

    /**
     * Get closest start date to cron expression
     */
    @Override
    public Date getStartDate(Date date) {
    	if (cronExpression.isSatisfiedBy(date)) {
    		return date;
    	}
    	if (duration != null) {
    		// If we are inside the interval, return the start of it; else return the start of the next one
    		Date prevStart = new Date(date.getTime());
    		duration.negate().addTo(prevStart);
    		prevStart = cronExpression.getNextValidTimeAfter(prevStart);
    		if (prevStart.getTime() < date.getTime()) {
    			return prevStart;
    		}
    	}
    	return cronExpression.getNextValidTimeAfter(date);
    }

    /**
     * Recurrent dates cannot be infinite by definition, returns false by
     * default
     */
    @Override
    public boolean isInfinite() {
        return false;
    }

}
