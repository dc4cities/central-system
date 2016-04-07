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

package eu.dc4cities.energis.client.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Response returned by Energis.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecuteResponse extends Response
{
	private List<Double> values = new ArrayList<Double>();

	public ExecuteResponse()
	{
	}

	@JsonCreator
	public ExecuteResponse(@JsonProperty("values") List<Double> values)
	{
		this.values = values;
	}

	public List<Double> getValues()
	{
		return values;
	}
}