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

import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.dc4cities.controlsystem.model.unit.Units;
import org.jscience.physics.amount.Amount;

public final class JScienceModule extends SimpleModule {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public JScienceModule() {
    	Units.init();
        addDeserializer(Amount.class, new AmountDeserializer());
        addSerializer(Amount.class, new AmountSerializer());
    }


}
