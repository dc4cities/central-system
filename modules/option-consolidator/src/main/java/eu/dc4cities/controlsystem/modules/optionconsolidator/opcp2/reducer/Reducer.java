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

package eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.reducer;

import eu.dc4cities.controlsystem.modules.optionconsolidator.opcp2.OPCP2;

/**
 * Reduce a problem.
 *
 *
 */
public interface Reducer {

    /**
     * This method is called each time a solution is computed.
     * It allows then to post new constraints to reduce the CSP
     * and make it optimize some specific parts.
     *
     * @param pb the problem to reduce
     */
    void reduce(OPCP2 pb);

    void reset();

    Reducer copy();
}
