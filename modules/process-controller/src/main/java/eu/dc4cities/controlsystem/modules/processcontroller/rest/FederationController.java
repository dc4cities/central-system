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

package eu.dc4cities.controlsystem.modules.processcontroller.rest;

import eu.dc4cities.controlsystem.model.TimeParameters;
import eu.dc4cities.controlsystem.model.datacenter.DataCenterExecutionPlan;
import eu.dc4cities.controlsystem.modules.processcontroller.OptimizationManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class FederationController {

    private OptimizationManager optimizationManager;
    
    public FederationController(OptimizationManager optimizationManager) {
    	this.optimizationManager = optimizationManager;
    }
    
    /**
     * Returns the current execution plan for the given data center.
     * 
     * @param dataCenterName the name of the data center
     * @return the current plan or {@code null} if none is available
     */
    @RequestMapping(value = "/v1/datacenters/{dataCenterName}/executionplan", method = RequestMethod.GET)
    @ResponseBody
    public DataCenterExecutionPlan getDataCenterExecutionPlan(@PathVariable String dataCenterName) {
        return optimizationManager.getDataCenterExecutionPlan(dataCenterName);
    }

    /**
     * Calculates a new execution plan for the whole federation of data centers. The calculation uses the given dateNow
     * as the current time and to calculate the optimization range. When the new plan is ready it becomes the
     * current plan for the federation. This method is asynchronous and returns with HTTP 202 and no body if the
     * request is accepted.
     * 
     * @param timeParameters the time parameters containing dateNow (other parameters should be null)
     */
    @RequestMapping(value = "/v1/executionplan", method = RequestMethod.POST)
    public ResponseEntity<Void> calculateDataCenterPlan(@RequestBody TimeParameters timeParameters) {
    	optimizationManager.startImmediateOptimization(timeParameters.getDateNow());
    	return new ResponseEntity<Void>(HttpStatus.ACCEPTED);
    }
    
}
