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

package eu.dc4cities.controlsystem.modules.processcontroller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dc4cities.controlsystem.model.json.JsonUtils;
import org.junit.Assert;

public class JsonTestUtils {

	private static final ObjectMapper mapper = JsonUtils.getDc4CitiesObjectMapper();
	
	public static void assertJsonEquals(Object expected, Object actual) {
		JsonNode tree1 = mapper.valueToTree(expected);
		JsonNode tree2 = mapper.valueToTree(actual);
		Assert.assertEquals(tree1, tree2);
	}
	
	public static boolean jsonEquals(Object object1, Object object2) {
		JsonNode tree1 = mapper.valueToTree(object1);
		JsonNode tree2 = mapper.valueToTree(object2);
		return tree1.equals(tree2);
	}
	
}
