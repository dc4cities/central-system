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

package eu.dc4cities.controlsystem.modules.optionconsolidator.opcp;

import eu.dc4cities.configuration.goal.Objective;
import eu.dc4cities.configuration.goal.ObjectiveType;
import eu.dc4cities.configuration.goal.Target;
import eu.dc4cities.configuration.goal.interval.TimeIntervalExpressionException;
import eu.dc4cities.configuration.goal.utils.ObjectiveUtils;
import org.apache.commons.lang3.Validate;

import java.util.*;

import static javax.measure.unit.NonSI.MINUTE;

/**
 *
 */
public class OPCPDecoratorFactory {

	/**
	 * Factory method to create OPCP decorators from Objectives
	 *
	 * @param objective
	 *            The objective to be set
	 * @param timeSlotList
	 * @param startDate
	 * @return A list of OPCPDecorators representing the objective
	 */
	public static List<OPCPDecorator> create(Objective objective, List<TimeSlot> timeSlotList, Date startDate) {
		Validate.notNull(objective, "objective cannot be null");
		Validate.notNull(timeSlotList, "time slots cannot be null");
		Validate.notNull(startDate, "start date cannot be null");

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		List<OPCPDecorator> decorators = new ArrayList<OPCPDecorator>();

		Target target = objective.getTarget();
		int value = (int) Math.round(target.getValue());
		String operator = target.getOperator();
		int[] slots = findSlot(objective, timeSlotList, startDate);

		// power relocatibility objective
		if (objective.getType().equals(ObjectiveType.POWER)) {
			for (int i = slots[0]; i < slots[1]; i++) {
				CapTotalPowerUsage decorator = new CapTotalPowerUsage(i, value, objective.getPriority());
				setOperator(operator, decorator);
				decorators.add(decorator);
			}
		}

		// energy relocatibility objective
		if (objective.getType().equals(ObjectiveType.ENERGY)) {
			if (slots[0] != -1 && slots[1] != -1) {
				int startSlot = slots[0];
				int endSlot = slots[1];
				CapTotalEnergyUsage decorator = new CapTotalEnergyUsage(startSlot, endSlot, value);
				setOperator(operator, decorator);
				decorators.add(decorator);
			}
		}

		// energy property relocatibility objective
		if (objective.getType().equals(ObjectiveType.ENERGY_PROPERTY)) {
			if (slots[0] != -1 && slots[1] != -1) {
				int startSlot = slots[0];
				int endSlot = slots[1];
				CapGreenEnergyUsage decorator = new CapGreenEnergyUsage(startSlot, endSlot, value);
				setOperator(operator, decorator);
				decorators.add(decorator);
			}
		}
		return decorators;
	}

	/**
	 * Checks the operator present in the objective and sets it in the
	 * OPCPDecorator
	 *
	 * @param operator
	 *            The operator of the objective
	 * @param decorator
	 *            The OPCPDecorator to set the operator in
	 */
	private static void setOperator(String operator, CappableConstraint decorator) {
		String opcpOperator = null;
		switch (operator) {
		case "lt":
			opcpOperator = "<";
			break;
		case "leq":
			opcpOperator = "<=";
			break;
		case "eq":
			opcpOperator = "==";
			break;
		case "geq":
			opcpOperator = ">=";
			break;
		case "gt":
			opcpOperator = ">";
			break;
		}
		Validate.notNull(opcpOperator, "Error while setting operator!");
		decorator.setOperator(opcpOperator);
	}

	/**
	 * @param objective
	 * @param timeSlotList
	 * @param startDate
	 * @return
	 */
	private static int[] findSlot(Objective objective, List<TimeSlot> timeSlotList, Date startDate) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		int slot = 0;
		int[] slots = new int[] { -1, -1 };

		for (TimeSlot timeSlot : timeSlotList) {
			try {
				if (!ObjectiveUtils.filterActiveObjectives(Collections.singletonList(objective), calendar.getTime()).isEmpty()) {
					if (slots[0] == -1) {
						slots[0] = slot;
					} else {
						slots[1] = slot + 1;
					}
				}
			} catch (TimeIntervalExpressionException e) {
				e.printStackTrace();
			}
			slot++;
			int timeSlotLength = (int) (timeSlot.getLength().longValue(MINUTE));
			calendar.add(Calendar.MINUTE, timeSlotLength);
		}
		return slots;
	}

}
