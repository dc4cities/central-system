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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jscience.physics.amount.Amount;
import org.junit.Test;

import javax.measure.quantity.Duration;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * 
 * @see eu.dc4cities.controlsystem.model.json.AmountSerializer
 * @see eu.dc4cities.controlsystem.model.json.AmountDeserializer
 * 
 */
public class AmountSerializeDeserializeTest {

	/**
	 * Test carbon emission factor (serialize/deserialize should work for all
	 * other units as well ..).
	 * 
	 * Depending on the ObjectMapper configuration, serializers/deserializers
	 * are either configured globally or by using Jackson annotations.
	 * 
	 * @throws IOException
	 * 
	 * @see SomeModel
	 */
	@Test
	public void testSerializeDeserializeCefTest() throws IOException {
		SomeModel someModel = new SomeModel();
		// carbon emission factor
		Amount cef = Amount.valueOf(380, Unit.valueOf("g/(kW·h)"));

		someModel.setCef(cef);

		ObjectMapper mapper = JsonUtils.getDc4CitiesObjectMapper();

		// serialize
		String json = mapper.writeValueAsString(someModel);

		Map map = mapper.readValue(json, Map.class);

		// assert
		assertEquals("380 g/(kW·h)", map.get("cef"));

		// deserialize
		SomeModel deserializedModel = mapper.readValue(json, SomeModel.class);

		// assert
		assertEquals(someModel.getCef(), deserializedModel.getCef());
	}

	@Test
	public void testDuration() throws IOException {
		Amount<Duration> duration = (Amount<Duration>) Amount.valueOf(10,
				NonSI.MINUTE);

		ObjectMapper mapper = JsonUtils.getDc4CitiesObjectMapper();

		// serialize
		String json = mapper.writeValueAsString(duration);

		// deserialize
		Amount<Duration> deserializedModel = mapper.readValue(json,
				Amount.class);

		// assert
		assertEquals(duration, deserializedModel);

		System.out.println(deserializedModel.longValue(deserializedModel.getUnit()) + " "
				+ deserializedModel.getUnit().toString());
	}
}
