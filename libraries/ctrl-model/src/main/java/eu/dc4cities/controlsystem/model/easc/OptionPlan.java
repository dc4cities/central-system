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

package eu.dc4cities.controlsystem.model.easc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defines an option plan for the execution of an activity.
 * 
 * @deprecated use inversion of control instead of option plans
 */
@Deprecated
public class OptionPlan {
	private String name;
    private List<WorkOption> workOptions = new ArrayList<WorkOption>();
    
    /**
     * Returns the name of the option plan. Setting a name is optional and may be used for debugging purposes.
     * 
     * @return the name of the option plan or {@code null} if not set
     */
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
     * Adds a work option as part of this plan.
     *
     * @param workOption the work option to add
     */
    public void addWorkOption(WorkOption workOption) {
        workOptions.add(workOption);
    }

    /**
     * Returns the work options in this option plan.
     *
     * @return an immutable list that should not be empty.
     */
    public List<WorkOption> getWorkOptions() {
        return Collections.unmodifiableList(workOptions);
    }
    
    /**
     * Sets the list of work options in this option plan.
     * 
     * @param workOptions the list of work options
     */
    @JsonDeserialize(contentAs=WorkOption.class)
    public void setWorkOptions(List<WorkOption> workOptions) {
		this.workOptions = workOptions;
	}

	@Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("OptionPlan{");
        if (name != null) {
        	b.append("name=" + name + ", ");
        }
        b.append("workOptions='" + workOptions.size() + "'}");
        return b.toString();
    }
    
}