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

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.dc4cities.controlsystem.model.json.JsonUtils;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Power;
import java.util.Objects;


/**
 * Denotes a work to be done over a given period.
 */
public class Work {

	private int startTimeSlot;
	private int endTimeSlot;
	private String workingMode;
	private String workingModeName;
	private int workingModeValue;
    private Amount<Power> power;
    private int greenPoints;
    private Amount<?> perf;
    /**
     * @deprecated this has been added for JSON-parsing backward compatibility with the old 
     *             {@link #Work(int, int, String, Amount, int)} constructor and will be removed when the old constructor
     *             is removed. Use the new {@link #Work(int, int, String, int, Amount, Amount)} constructor.
     */
    public Work() {}

    public Work(int startTimeSlot, int endTimeSlot, String workingModeName, int workingModeValue, Amount<Power> power, Amount<?> perf) {
        this.startTimeSlot = startTimeSlot;
        this.endTimeSlot = endTimeSlot;
        this.workingModeName = workingModeName;
    	this.workingModeValue = workingModeValue;
        this.power = power;
        this.perf = perf;
    }

    public Work(int startTimeSlot, int endTimeSlot, String workingModeName, int workingModeValue, Amount<Power> power) {
        this.startTimeSlot = startTimeSlot;
        this.endTimeSlot = endTimeSlot;
        this.workingModeName = workingModeName;
        this.workingModeValue = workingModeValue;
        this.power = power;
    }
    
    /**
     * New Work.
     *
     * @param startTimeSlot       the moment to start the work
     * @param endTimeSlot         the moment the work ends
     * @param workingMode the label to use to identify the work
     * @param power         the power consumption at every second for this work in Watt.
     * @param greenPoints          the amount of green points associated to the work
     *
     * @deprecated use the new {@link #Work(int, int, String, int, Amount, Amount)} constructor.
     */
    @Deprecated
    public Work(int startTimeSlot, int endTimeSlot, String workingMode, Amount<Power> power, int greenPoints) {
    	this.startTimeSlot = startTimeSlot;
    	this.endTimeSlot = endTimeSlot;
        this.workingMode = workingMode;
        this.power = power;
        this.greenPoints = greenPoints;
    }

    /**
     * Returns the number of the time slot when this work must start (0-based).
     * 
     * @return the start time slot number
     */
    public int getStartTimeSlot() {
		return startTimeSlot;
	}

	public void setStartTimeSlot(int startTimeSlot) {
		this.startTimeSlot = startTimeSlot;
	}

	/**
	 * Returns the number of the time slot when this work must end (exclusive).
	 * 
	 * @return the end time slot number
	 */
	public int getEndTimeSlot() {
		return endTimeSlot;
	}

	public void setEndTimeSlot(int endTimeSlot) {
		this.endTimeSlot = endTimeSlot;
	}
	
	/**
	 * Returns the name of the working mode to use during this work.
	 * 
	 * @return the name of the working mode for this work
	 */
	public String getWorkingModeName() {
		return workingModeName;
	}

	public void setWorkingModeName(String workingModeName) {
		this.workingModeName = workingModeName;
	}

	/**
	 * Returns the value of the working mode to use during this work.
	 * 
	 * @return the value of the working mode for this work
	 */
	public int getWorkingModeValue() {
		return workingModeValue;
	}

	public void setWorkingModeValue(int workingModeValue) {
		this.workingModeValue = workingModeValue;
	}

    /**
     * Returns the estimated average power consumption for this work.
     *
     * @return the power consumption for this work, in Watt
     */
    public Amount<Power> getPower() {
        return power;
    }
	
    public void setPower(Amount<Power> power) {
        this.power = power;
    }

    public Amount<?> getBusinessPerformance() {
        return perf;
    }

    public void setBusinessPerformance(Amount<?> p) {
        perf = p;
    }
    /**
     * Get the working mode that identify this work.
     *
     * @return a String
     * @deprecated use {@link #getWorkingModeName()} and {@link #getWorkingModeValue()} instead
     */
    @Deprecated
    public String getWorkingMode() {
        return workingMode;
    }

    /**
     * Set the working mode that identify this work.
     *
     * @return a String
     */
    @Deprecated
    public void setWorkingMode(String mode) {
        this.workingMode = mode;
    }


    /**
     * Get the amount of green points associated to this work.
     *
     * @return a positive number
     * @deprecated green points have been replaced with price penalties and rewards
     */
    @Deprecated
    @JsonIgnore
    public int getGreenPoints() {
        return greenPoints;
    }

    /**
     * Set the amount of green points associated to this work.
     */
    @Deprecated
    public void setGreenPoints(int greenPoints) {
        this.greenPoints = greenPoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Work w = (Work) o;
        if (workingMode != null) {
        	// Legacy mode, remove when deprecated elements are removed
        	return (greenPoints == w.greenPoints &&
                    power == w.power &&
                    startTimeSlot == w.startTimeSlot &&
                    endTimeSlot == w.endTimeSlot &&
                    workingMode.equals(w.workingMode));
        } else {
        	// To-be mode, keep this when deprecated elements are removed
        	return startTimeSlot == w.startTimeSlot &&
                   endTimeSlot == w.endTimeSlot &&
                   workingModeName.equals(w.workingModeName) &&
                   workingModeValue == w.workingModeValue &&
                   power.equals(w.power);
        }
    }

    @Override
    public int hashCode() {
    	if (workingMode != null) {
    		// Legacy mode, remove when deprecated elements are removed
    		return Objects.hash(workingMode, startTimeSlot, endTimeSlot, greenPoints, power.toString());
    	} else {
    		// To-be mode, keep this when deprecated elements are removed
    		return Objects.hash(startTimeSlot, endTimeSlot, workingModeName, workingModeValue, power.toString());
    	}
    }

    @Override
	public String toString() {
		return JsonUtils.toString(this);
	}
    
}
