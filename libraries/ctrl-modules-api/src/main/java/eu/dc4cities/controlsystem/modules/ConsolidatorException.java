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

package eu.dc4cities.controlsystem.modules;

/**
 * An exception related to the {@link eu.dc4cities.controlsystem.modules.OptionConsolidator}.
 *
 *
 */
public class ConsolidatorException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
     * New Exception.
     *
     * @param msg the error message
     */
    public ConsolidatorException(String msg) {
        super(msg);
    }

    /**
     * Rethrow an existing error
     *
     * @param msg the error message
     * @param t   the throwable to re-throw
     */
    public ConsolidatorException(String msg, Throwable t) {
        super(msg, t);
    }
    
}
