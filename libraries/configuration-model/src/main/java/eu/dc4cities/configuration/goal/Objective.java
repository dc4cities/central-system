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

import eu.dc4cities.controlsystem.model.easc.PriceModifier;

import java.util.Date;
import java.util.List;

/**
 * Objective description
 *
 *
 */
public class Objective {

    /**
     * Objective ID
     */
    private String id;

    /**
     * Name
     */
    private String name;

    /**
     * Human-readable description
     */
    private String description;

    /**
     * Creation date
     */
    private Date creationDate;

    /**
     * Last modified
     */
    private Date lastModified;

    /**
     * Objective type (ENERGY, POWER or other)
     *
     * @see ObjectiveType
     */
    private String type;

    /**
     * Type of implementation (currently only MUST)
     *
     * @see ImplementationType
     */
    private String implementationType;

    /**
     * Objective enabled?
     */
    private boolean enabled;

    /**
     * Assigned to DC with id
     */
    private String dataCenterId;

    /**
     * Priority of this objective (phase I: 0,1)
     */
    private int priority;

    /**
     * Target of this objective (ERDS + "constraint")
     */
    private Target target;

    /**
     * Time frame of this objective (either ABSOLUTE or RECURRENT)
     */
    private TimeFrame timeFrame;

    private List<PriceModifier> priceModifiers;
    
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate the creationDate to set
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
     * @param lastModified the lastModified to set
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * @return the type
     * @see ObjectiveType
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     * @see ObjectiveType
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the implementationType
     * @see ImplementationType
     */
    public String getImplementationType() {
        return implementationType;
    }

    /**
     * @param implementationType the implementationType to set
     * @see ImplementationType
     */
    public void setImplementationType(String implementationType) {
        this.implementationType = implementationType;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled
     *            the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the dataCenterId
     */
    public String getDataCenterId() {
        return dataCenterId;
    }

    /**
     * @param dataCenterId
     *            the dataCenterId to set
     */
    public void setDataCenterId(String dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priority
     *            the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * @return the target
     */
    public Target getTarget() {
        return target;
    }

    /**
     * @param target
     *            the target to set
     */
    public void setTarget(Target target) {
        this.target = target;
    }

    /**
     * @return the timeFrame
     */
    public TimeFrame getTimeFrame() {
        return timeFrame;
    }

    /**
     * @param timeFrame
     *            the timeFrame to set
     */
    public void setTimeFrame(TimeFrame timeFrame) {
        this.timeFrame = timeFrame;
    }

    /**
     * Returns the price modifiers for this objective.
     * 
     * @return the list of price modifiers or {@code null} if none
     */
	public List<PriceModifier> getPriceModifiers() {
		return priceModifiers;
	}

	public void setPriceModifiers(List<PriceModifier> priceModifiers) {
		this.priceModifiers = priceModifiers;
	}

    @Override
    public String toString() {
        return "Objective(name='" + name + "')";
    }
}
