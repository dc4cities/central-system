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

import eu.dc4cities.configuration.goal.Objective;
import eu.dc4cities.configuration.goal.interval.TimeIntervalExpressionException;
import eu.dc4cities.configuration.goal.interval.TimeIntervalExpressionFactory;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Simple utility methods for processing objectives
 *
 *
 * 
 */
public class ObjectiveUtils {

	/**
	 * Get a list of objectives that are active for given date
	 * 
	 * @param objectives
	 *            List of objectives to be filtered
	 * @param date
	 *            Date to be evaluated
	 * @return filtered list of "active" objectives
	 * @throws TimeIntervalExpressionException
	 *             TimeFrame errors discovered
	 */
	public static List<Objective> filterActiveObjectives(
			List<Objective> objectives, Date date)
			throws TimeIntervalExpressionException {
		if (CollectionUtils.isEmpty(objectives)) {
			return null;
		}

		List<Objective> activeObjectives = new ArrayList<Objective>();
		for (Objective objective : objectives) {
			if (TimeIntervalExpressionFactory.create(objective.getTimeFrame())
					.isActive(date)) {
				activeObjectives.add(objective);
			}
		}

		return activeObjectives;
	}
}
