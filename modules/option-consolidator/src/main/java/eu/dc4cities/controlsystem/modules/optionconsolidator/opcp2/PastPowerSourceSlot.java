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

package eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2;

/**
 * Created by fhermeni on 15/07/2015.
 */
public class PastPowerSourceSlot extends PowerSourceSlot {

    public PastPowerSourceSlot(int ren) {
        super(-1, ren, -1);
    }


    @Override
    public int peak() {
        throw new UnsupportedOperationException("No peek power available for past data");
    }

    @Override
    public int price() {
        throw new UnsupportedOperationException("No price available for past data");
    }
}
