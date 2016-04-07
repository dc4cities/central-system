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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * ConfigLoader interface
 *
 *
 * 
 */
public interface ConfigLoader {

	/**
	 * Load config from somewhere
	 * 
	 * @param input
	 *            InputStream
	 * @param typeClass
	 *            Class type
	 * @return Cast to expected type
	 * @throws IOException
	 *             I/O error
	 */
	public <T> T load(InputStream input, Class<T> typeClass) throws IOException;

	/**
	 * Save entity
	 * 
	 * @param entity
	 *            Entity to be saved
	 * @param out
	 *            OutputStream
	 * @throws IOException
	 *             I/O error
	 */
	public <T> void save(Object entity, OutputStream out) throws IOException;
}
