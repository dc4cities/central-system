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

package eu.dc4cities.configuration.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dc4cities.controlsystem.model.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Load config from JSON input stream
 *
 *
 * 
 */
public class JsonLoader implements ConfigLoader {

	private static final Logger LOG = LoggerFactory.getLogger(JsonLoader.class);
	private ObjectMapper mapper = JsonUtils.getDc4CitiesObjectMapper();
	
	/**
	 * Load config from JSON input stream
	 */
	@Override
	public <T> T load(InputStream input, Class<T> typeClass) throws IOException {
		try {

			T t = mapper.readValue(input, typeClass);

			if (LOG.isDebugEnabled()) {
				LOG.debug(typeClass.getName() + " initialized successfully");
			}

			return t;
		} catch (Throwable e) {
			LOG.warn("Couldn't load JSON for " + typeClass.getName(), e);

			throw new IOException("Couldn't load JSON for "
					+ typeClass.getName(), e);
		}
	}

	/**
	 * Save entity in JSON
	 */
	@Override
	public <T> void save(Object entity, OutputStream out) throws IOException {
		try {

			mapper.writerWithDefaultPrettyPrinter().writeValue(out, entity);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Saved entity");
			}
		} catch (Throwable e) {
			LOG.warn("Couldn't write JSON for "
					+ (entity != null ? entity.getClass().getName() : "null"),
					e);

			throw new IOException("Couldn't write JSON for "
					+ (entity != null ? entity.getClass().getName() : "null"),
					e);
		}
	}

}
