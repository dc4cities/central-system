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

public interface TimeSlot {
	
	void setPower(Amount<Power> power);
	
	Amount<Power> getPower();

    Amount<Duration> getLength();

    void setLength(Amount<Duration> length);

    DateTime getStartDate();

    void setStartDate(DateTime startDate);

    DateTime getEndDate();

    void setEndDate(DateTime endDate);

    Set<Objective> getObjectives();

    void setObjectives(Set<Objective> objectives);

    Amount<Power> getPowerCap();
    
    void setPowerCap(Amount<Power> powerCap);
}
