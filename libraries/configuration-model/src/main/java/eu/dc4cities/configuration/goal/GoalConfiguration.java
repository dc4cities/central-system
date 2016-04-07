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
import java.util.List;

/**
 * Goal configuration description
 *
 *
 * 
 */
public class GoalConfiguration {

	/**
	 * GoalConfiguration ID
	 */
	private String id;

	/**
	 * Creation date
	 */
	private Date creationDate;

	/**
	 * Last modified
	 */
	private Date lastModified;

	/**
	 * Collection of goals owned by this configuration
	 */
	private List<Goal> goals;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate
	 *            the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the lastModified
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * @param lastModified
	 *            the lastModified to set
	 */
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * @return the goals
	 */
	public List<Goal> getGoals() {
		return goals;
	}

	/**
	 * @param goals
	 *            the goals to set
	 */
	public void setGoals(List<Goal> goals) {
		this.goals = goals;
	}
}
