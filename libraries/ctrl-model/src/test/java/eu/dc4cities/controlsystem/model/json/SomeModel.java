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

import org.jscience.physics.amount.Amount;

/**
 * Some test model
 * 
 */
public class SomeModel {

	/**
	 * Annotations only needed if ObjectMapper is not aware of
	 * serializer/deserializer classes.
	 */
	// @JsonSerialize(using = AmountSerializer.class)
	// @JsonDeserialize(using = AmountDeserializer.class)
	private Amount cef;

	/**
	 * @return the cef
	 */
	public Amount getCef() {
		return cef;
	}

	/**
	 * @param cef
	 *            the cef to set
	 */
	public void setCef(Amount cef) {
		this.cef = cef;
	}
}
