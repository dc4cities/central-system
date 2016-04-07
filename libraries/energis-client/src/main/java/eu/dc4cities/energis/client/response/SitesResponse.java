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
package eu.dc4cities.energis.client.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.dc4cities.energis.client.builder.Site;

import java.util.ArrayList;
import java.util.List;

/**
 *
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SitesResponse extends Response {

    private List<Site> sites = new ArrayList<>();

    public SitesResponse() {
    }

    @JsonCreator
    public SitesResponse(@JsonProperty("sites") List<Site> sites) {
        this.sites = sites;
    }

    public List<Site> getSites() {
        return sites;
    }

}
