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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Power;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains the option plans that can be used to execute an activity.
 * 
 * @deprecated use inversion of control instead of option plans
 */
@Deprecated
public class ActivityOption {
	
    private String activity;
    private Amount<Power> idlePower;
    private List<OptionPlan> optionPlans;

    /**
     * New ActivityOption.
     *
     * @param activity the activity name. Supposed to be unique among all activity options.
     */
    @JsonCreator
    public ActivityOption(@JsonProperty("activity") String activity) {
        this.activity = activity;
        optionPlans = new ArrayList<OptionPlan>();
    }

    /**
     * Returns the name of the activity this object refers to.
     *
     * @return the activity name
     */
    public String getActivity() {
        return activity;
    }

    /**
     * Returns the amount of power consumed by the activity when idling (i.e. working mode 0)
     * 
     * @return the amount of power consumed by the activity when idling
     */
    public Amount<Power> getIdlePower() {
		return idlePower;
	}

	public void setIdlePower(Amount<Power> idlePower) {
		this.idlePower = idlePower;
	}

	/**
     * Add a possible option plan for the activity.
     *
     * @param optionPlan the option plan to add
     */
    public void addOptionPlan(OptionPlan optionPlan) {
        optionPlans.add(optionPlan);
    }

    /**
     * Returns the list of possible option plans.
     *
     * @return an immutable list of option plans (read-only). Should not be empty.
     */
    public List<OptionPlan> getOptionPlans() {
        return Collections.unmodifiableList(optionPlans);
    }
    
    /**
     * Sets the list of possible option plans for the activity.
     * 
     * @param optionPlans the list of option plans to set
     */
    @JsonDeserialize(contentAs=OptionPlan.class)
    public void setOptionPlans(List<OptionPlan> optionPlans) {
		this.optionPlans = optionPlans;
	}

	@Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("ActivityOption{activity='").append(activity).append("', plans=" + optionPlans.size());
        return b.append("'}").toString();
    }
	
}
