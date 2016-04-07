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

package eu.dc4cities.controlsystem.model.json;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jscience.physics.amount.Amount;

import java.io.IOException;

@SuppressWarnings("rawtypes")
public final class AmountSerializer extends StdSerializer<Amount> {

	private static final long serialVersionUID = 1L;

	public AmountSerializer() {
        super(Amount.class);
    }

	@Override
    public void serialize(Amount amount, JsonGenerator jgen, SerializerProvider provider) 
    		throws IOException, JsonGenerationException {
    	String value;
    	if (amount.isExact()) {
    		value = String.valueOf(amount.getExactValue());
    	} else {
    		// JScience stores double values as a range between a minimum and a maximum, which results in values with
    		// rounding errors due to floating point arithmetic when calculating an estimated value (e.g. the estimated
    		// value for 0.3 becomes 0.29999999999999993).
    		// So round to the 4th decimal to avoid this problem in output JSON.
    		// This must be kept synchronized with the precision of the optimizer (i.e. now the optimizer can handle up
    		// to four decimal figures, if this increases the precision of the serializer must also increase because it
    		// impacts the activity specifications sent by the EASC to the Control System).
    		double rounded = (double) Math.round(amount.getEstimatedValue() * 10000) / 10000;
    		value = String.valueOf(rounded);
    	}
        jgen.writeString(value + " " + amount.getUnit());
    }
    
}
