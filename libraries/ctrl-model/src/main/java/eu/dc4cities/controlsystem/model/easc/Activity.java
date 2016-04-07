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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import eu.dc4cities.controlsystem.model.json.JsonUtils;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Power;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines the execution plan for a given activity.
 */
public class Activity {
	
    private String name;
    private Amount<Power> idlePower;
    private List<Work> works;
    private List<ActivityDataCenter> dataCenters;
    private List<ServiceLevel> serviceLevels;
    
    /**
     * Creates a new activity.
     *
     * @param name the activity name
     */
    @JsonCreator
    public Activity(@JsonProperty("name") String name) {
        this.name = name;
        this.serviceLevels = new ArrayList<>();
        this.dataCenters = new ArrayList<>();
        this.works = new ArrayList<>();
    }

    /**
     * Returns the activity name. The name must be unique within the EASC.
     *
     * @return the activity name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the data centers (and works) in which the activity will be executed
     * 
     * @return the data centers in which the activity will be executed
     */
    @JsonInclude(Include.NON_EMPTY)
    public List<ActivityDataCenter> getDataCenters() {
		return dataCenters;
	}

	public void setDataCenters(List<ActivityDataCenter> dataCenters) {
		this.dataCenters = dataCenters;
	}

	public void addDataCenter(ActivityDataCenter dataCenter) {
		dataCenters.add(dataCenter);
	}
	
	/**
	 * Returns the service levels that are expected to be achieved for the activity. Time intervals must be aligned to
	 * service level objectives defined for the activity in the corresponding ActivitySpecification. Time intervals that
	 * are listed in the SLOs but outside the time range of the activity plan can be omitted.
	 * 
	 * @return the expected service levels for the activity
	 */
    @JsonInclude(Include.NON_EMPTY)
    public List<ServiceLevel> getServiceLevels() {
        return serviceLevels;
	}

	public void setServiceLevels(List<ServiceLevel> serviceLevels) {
		this.serviceLevels = serviceLevels;
	}

	/**
     * Returns the amount of power consumed by the activity when idling (i.e. working mode 0)
     * 
     * @return the amount of power consumed by the activity when idling
     * @deprecated idle power is unnecessary as the activity plan now explicitly includes the default working mode
     */
    @Deprecated
    public Amount<Power> getIdlePower() {
		return idlePower;
	}

    @Deprecated
	public void setIdlePower(Amount<Power> idlePower) {
		this.idlePower = idlePower;
	}

	/**
     * Get all the works that makes this plan.
     *
     * @return a list of works. Should not be empty.
     * @deprecated works must be specified per each data center in which the activity runs, not globally per activity
     */
    @Deprecated
    @JsonInclude(Include.NON_EMPTY)
    public List<Work> getWorks() {
        return works;
    }

    /**
     * Set all the works that makes this plan.
     *
     */
    @Deprecated
    @JsonDeserialize(contentAs=Work.class)
    public void setWorks(List<Work> works) {
 		this.works = works;
 	}

    /**
     * Add a work.
     * It is expected the working mode of {@code w} is not already used in the already registered work.
     * <p/>
     * Todo: ensure there is no work in with the same name ?
     *
     * @param w the work to add.
     */
    @Deprecated
    public void addWork(Work w) {
        works.add(w);
    }

    @Override
	public String toString() {
		return JsonUtils.toString(this);
	}
    
}
