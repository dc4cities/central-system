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
import org.joda.time.DateTime;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Power;
import java.util.Set;

/**
 *
 */

public class TimeSlotImpl implements TimeSlot {
	private Amount<Power> power;
    private Set<Objective> objectives;
    private Amount<Duration> length;
    private Amount<Power> powerCap;
    private DateTime startDate;
    private DateTime endDate;

    public TimeSlotImpl(Amount<Duration> length) {
        this.length = length;
    }

    public Set<Objective> getObjectives() {
        return objectives;
    }

    public void setObjectives(Set<Objective> objectives) {
        this.objectives = objectives;
    }

    public Amount<Duration> getLength() {
        return length;
    }

    public void setLength(Amount<Duration> length) {
        this.length = length;
    }

    public Amount<Power> getPowerCap() {
        return powerCap;
    }

    public void setPowerCap(Amount<Power> powerCap) {
        this.powerCap = powerCap;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

	@Override
	public void setPower(Amount<Power> power) {
		this.power = power;
	}

	@Override
	public Amount<Power> getPower() {
		return power;
	}


}
