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

import eu.dc4cities.energis.client.builder.ExecuteBuilder;
import eu.dc4cities.energis.client.builder.QueryBuilder;
import eu.dc4cities.energis.client.response.*;

import java.io.IOException;
import java.net.URISyntaxException;

public interface Client {

    /**
     * Queries the list of site belonging the given company.
     * 
     * @param companyCode The code of the company the sites belong.
     * @return The list of sites
     * @throws URISyntaxException
     * @throws IOException 
     */
    SitesResponse querySites(String companyCode) throws URISyntaxException, IOException;
 
    /**
     * Queries the details of a site.
     * 
     * @param id The id of the site to be queried.
     * @return The details of the site.
     * @throws URISyntaxException
     * @throws IOException 
     */
    SiteDetailResponse querySite(Long id) throws URISyntaxException, IOException;

    MetricsResponse queryMetrics(String companyCode, String assetCode, String reference) throws URISyntaxException, IOException;

    MetricDetailResponse queryMetric(Long id) throws URISyntaxException, IOException;

    /**
     * Queries Energis using the query built by the builder.
     *
     * @param builder query builder
     * @return response from the server
     * @throws URISyntaxException if the host or post is invalid
     * @throws IOException problem occurred querying the server
     */
    QueryResponse query(QueryBuilder builder) throws URISyntaxException, IOException;
    
    /**
     * Execute a formula on Energis using the query built by the builder.
     *
     * @param builder execute formula request builder
     * @return response from the server
     * @throws URISyntaxException if the host or post is invalid
     * @throws IOException problem occurred querying the server
     */
    ExecuteResponse execute(ExecuteBuilder builder) throws URISyntaxException, IOException;
    
    /**
     * Returns true of the connection is using SSL.
     *
     * @return true if using SSL
     */
    public boolean isSSLConnection();

    /**
     * Returns the number of retries.
     *
     * @return number of retries
     */
    @SuppressWarnings("UnusedDeclaration")
    public int getRetryCount();

    /**
     * Shuts down the client. Should be called when done using the client.
     */
    void shutdown();
}
