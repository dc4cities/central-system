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

import eu.dc4cities.configuration.technical.ProcessConfig;

/**
 * This interface allows a component to declare to the process controller that it needs to be passed a
 * ProcessConfig configuration in order to work. Only the process controller, which manages the process
 * execution workflow, knows which process configuration (from the set of process configurations defined
 * in the technical configuration) must be passed to every module.
 *
 *
 */
public interface ProcessConfigAware {

    public void setProcessConfig(ProcessConfig processConfig);

}
