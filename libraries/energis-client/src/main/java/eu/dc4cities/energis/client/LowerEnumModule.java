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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.dc4cities.energis.client;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 *
 *
 */
public class LowerEnumModule extends SimpleModule {

    public LowerEnumModule() {
        super("lower-enum", new Version(1, 0, 0, "", "com.fmc.energiscloud", "energiscloud-json"));
        addSerializer(Enum.class, new LowerEnumSerializer());
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        Deserializers.Base deser = new Deserializers.Base() {
            @SuppressWarnings("unchecked")
            @Override
            public JsonDeserializer<?> findEnumDeserializer(Class<?> type,
                    DeserializationConfig config, BeanDescription beanDesc)
                    throws JsonMappingException {
                return new LowerEnumDeserializer((Class<Enum<?>>) type);
            }
        };
        context.addDeserializers(deser);
    }
;
}
