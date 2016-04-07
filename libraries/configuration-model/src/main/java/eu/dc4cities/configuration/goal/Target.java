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

/**
 * Target of objective
 *
 *
 * 
 */
public class Target {

	/**
	 * Operator: LESS THAN, <
	 */
	public static final String LESS_THAN = "lt";
	/**
	 * Operator: GREATER THAN, >
	 */
	public static final String GREATER_THAN = "gt";
	/**
	 * Operator: EQUALS, =
	 */
	public static final String EQUALS = "eq";
	/**
	 * Operator: GREATER EQUALS, >=
	 */
	public static final String GREATER_EQUALS = "ge";
	/**
	 * Operator: LESS EQUALS, <=
	 */
	public static final String LESS_EQUALS = "le";

	/**
	 * Target metric that should be evaluated (format according to agreed
	 * standard in DC4Cities)
	 */
	private String metric;

	/**
	 * Operator which should be applied to evaluate metric (lt, gt, eq, ge, le)
	 */
	private String operator;

	/**
	 * Value
	 */
	private double value;

	/**
	 * @return the metric
	 */
	public String getMetric() {
		return metric;
	}

	/**
	 * @param metric
	 *            the metric to set
	 */
	public void setMetric(String metric) {
		this.metric = metric;
	}

	/**
	 * @return the operator
	 */
	public String getOperator() {
		return operator;
	}

	/**
	 * @param operator
	 *            the operator to set
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(double value) {
		this.value = value;
	}


}
