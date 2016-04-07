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
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Power;
import java.util.Objects;

import static javax.measure.unit.SI.WATT;

/**
 * Describes a working mode that can be chosen in an option plan.
 * 
 * @deprecated use inversion of control instead of option plans
 */
@Deprecated
public class ModeOption {
	
	private String workingMode;
    private Amount<Power> power;
    private int greenPoints;

    /**
     * New ModeOption
     *
     * @param workingMode the associated working mode
     * @param power       the instantaneous power consumption for this option in watts
     * @param greenPoints the amount of green points associated to this option
     */
    @JsonCreator
    public ModeOption(@JsonProperty("workingMode") String workingMode, @JsonProperty("power") Amount<Power> power,
    		@JsonProperty("greenPoints") int greenPoints) {
    	this.workingMode = workingMode;
    	this.power = power;
    	this.greenPoints = greenPoints;
    }

    /**
     * Get the instantaneous power consumption for this option in watts.
     *
     * @return the power consumption
     */
    public Amount<Power> getPower() {
        return power;
    }

    /**
     * Get the working mode associated to this option.
     * 
     * @return the working mode name
     */
    public String getWorkingMode() {
        return workingMode;
    }

    /**
     * Get the amount of green points associated to this mode.
     * 
     * @return the number of green points
     */
    public int getGreenPoints() {
        return greenPoints;
    }

    /**
     * Set the power consumption in Watts for this option.
     *
     * @param power a positive amount of Watts.
     * @return {@code true} if the amount has been updated
     */
    public boolean setPower(Amount<Power> power) {
        if (power.isGreaterThan(Amount.valueOf(0, WATT))) {
            this.power = power;
            return true;
        }
        return false;
    }

    /**
     * Set the number of green points to associate to this mode.
     *
     * @param p an integer number
     * @return {@code true} if the amount has been updated
     */
    public void setGreenPoints(int p) {
    	greenPoints = p;
    }

    @Override
    public String toString() {
        return "ModeOption{" +
                "workingMode='" + workingMode + '\'' +
                ", power=" + power +
                ", greenPoints=" + greenPoints +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ModeOption that = (ModeOption) o;
        return (greenPoints == that.greenPoints && power == that.power && workingMode.equals(that.workingMode));
    }

    @Override
    public int hashCode() {
        return Objects.hash(power, greenPoints, workingMode);
    }
}
