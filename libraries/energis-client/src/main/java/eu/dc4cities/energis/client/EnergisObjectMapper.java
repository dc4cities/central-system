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

package eu.dc4cities.energis.client;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.IOException;


public class EnergisObjectMapper extends ObjectMapper {

    /**
     * 
     */
    private static final long serialVersionUID = -1978783094112764349L;

    
    public EnergisObjectMapper() {
        super();
        JodaModule jd = new JodaModule();
        jd.addSerializer(DateTime.class, new JsonSerializer<DateTime>() {

            @Override
            public void serialize(DateTime date, JsonGenerator generator, SerializerProvider provider) throws IOException, JsonProcessingException {

                String formattedDate = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss").print(date);
                generator.writeString(formattedDate);                
            }
            
            
        });
        
        LowerEnumModule lowerEnumModule = new LowerEnumModule();
        
        super.registerModules(jd, lowerEnumModule);
        super.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS , false);
    }
    
}
