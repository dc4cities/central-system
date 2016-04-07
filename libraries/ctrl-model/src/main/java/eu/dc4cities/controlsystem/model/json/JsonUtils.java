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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.joda.cfg.JacksonJodaDateFormat;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.TimeZone;

/**
 * JSON serialization and deserialization utilities.
 * {@code getDc4CitiesObjectMapper} returns a custom Jackson mapper that can handle all DC4Cities beans used in
 * REST API calls.
 * Other methods allow to read and save beans from/to JSON files.
 */
public class JsonUtils {

    private static final Logger LOG = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper defaultMapper = getDc4CitiesObjectMapper();

    public static ObjectMapper getDc4CitiesObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(getJodaModule());
        mapper.registerModule(new JScienceModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.setTimeZone(TimeZone.getDefault());
        return mapper;
    }
    
    private static JodaModule getJodaModule() {
    	// Override default Joda serializer since it doesn't consider Jackson dateFormat setting
    	DateTimeFormatter formatter = 
    			new DateTimeFormatterBuilder()
    					.append(ISODateTimeFormat.date())
    					.appendLiteral('T')
    					.appendHourOfDay(2)
    					.appendLiteral(':')
    					.appendMinuteOfHour(2)
    					.appendLiteral(':')
    					.appendSecondOfMinute(2)
    					.toFormatter()
    					.withZone(DateTimeZone.getDefault());
    	DateTimeSerializer dtSerializer = new DateTimeSerializer(new JacksonJodaDateFormat(formatter));
    	JodaModule jodaModule = new JodaModule();
        jodaModule.addSerializer(DateTime.class, dtSerializer);
        return jodaModule;
    }

    /**
     * Returns a string with the JSON representation of the given object.
     * 
     * @param object the object to write as a string
     * @return the string representation of the object
     */
    public static String toString(Object object) {
    	try {
			return defaultMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
    }
    
    /**
     * Reads an object from a JSON file in the classpath.
     * 
     * @param resource the resource path in the classpath
     * @param clazz the class of the object to load
     * @return the instance of clazz created from the resource
     */
    public static <T> T loadResource(String resource, Class<T> clazz) {
    	try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
    		return load(is, clazz);
    	} catch (IOException e) {
    		throw new RuntimeException("Couldn't read resource " + resource, e);
		}
    }
    
    /**
     * Reads an object from a JSON file in the classpath, using the given type reference. Useful for loading JSON lists
     * using the correct item type.
     * 
     * @param resource the resource path in the classpath
     * @param typeReference the type reference for loading the object represented in the JSON
     * @return the instance of the object created from the resource
     */
    public static <T> T loadResource(String resource, TypeReference<T> typeReference) {
    	try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
    		return load(is, typeReference);
    	} catch (IOException e) {
    		throw new RuntimeException("Couldn't read resource " + resource, e);
		}
    }
    
    /**
     * Reads an object of the given type from a JSON input stream.
     * 
     * @param input the input stream with JSON content
     * @param clazz the class of the object represented in the JSON
     * @return the instance of typeClass created from input
     */
    public static <T> T load(InputStream input, Class<T> clazz) {
    	try {
			return getDc4CitiesObjectMapper().readValue(input, clazz);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't load JSON for " + clazz, e);
		}
    }

    public static <T> T load(URL url, Class<T> typeClass) throws IOException {
        try {

            T t = getDc4CitiesObjectMapper().readValue(url, typeClass);

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
     * Reads an object from a JSON input stream using the given type reference. Useful for loading JSON lists using the
     * correct item type.
     * 
     * @param input the input stream with JSON content
     * @param typeReference the type reference for loading the object represented in the JSON
     * @return the instance of the object created from input
     */
    public static <T> T load(InputStream input, TypeReference<T> typeReference) {
    	try {
			return getDc4CitiesObjectMapper().readValue(input, typeReference);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't load JSON for " + typeReference, e);
		}
    }
    
    /**
     * Save entity in JSON
     * Apply custom serialization for JScience objects
     */
    public static <T> void save(Object entity, OutputStream out) throws IOException {
        try {

            getDc4CitiesObjectMapper().writerWithDefaultPrettyPrinter().writeValue(out, entity);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Saved entity");
            }
        } catch (Throwable e) {
            LOG.warn("Couldn't write JSON for "
                            + (entity != null ? entity.getClass().getName() : "null"),
                    e
            );

            throw new IOException("Couldn't write JSON for "
                    + (entity != null ? entity.getClass().getName() : "null"),
                    e
            );
        }
    }

}
