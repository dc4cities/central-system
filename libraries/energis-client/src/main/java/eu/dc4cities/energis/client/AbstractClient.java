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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.dc4cities.energis.client.builder.ExecuteBuilder;
import eu.dc4cities.energis.client.builder.QueryBuilder;
import eu.dc4cities.energis.client.response.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;

/**
 * Base code used to query Energis.
 */
public abstract class AbstractClient implements Client {

    private String host;
    private int port;
    private String apiKey;
    private ObjectMapper mapper;

    /**
     * Creates a client
     *
     * @param host name of the Energis server
     * @param port Energis port
     * @param apiKey The authorisation key used to access the api
     */
    protected AbstractClient(String host, int port, String apiKey) {
        this.host = host;
        this.port = port;
        this.apiKey = apiKey;
        mapper = new EnergisObjectMapper();
    }

    @Override
    public QueryResponse query(QueryBuilder builder) throws URISyntaxException, IOException {
        ClientResponse clientResponse = postData(builder.build(), getURLBase() + "/energiscloud-gateway/restful/api/v1/data/query?apiKey=" + apiKey);
        int responseCode = clientResponse.getStatusCode();

        InputStream stream = clientResponse.getContentStream();
        try {
            if (stream != null) {
                StringWriter resultWriter = new StringWriter();
                IOUtils.copy(stream, resultWriter);

                JsonParser jsonParser = mapper.getFactory().createParser(resultWriter.toString());
                if (responseCode >= 400) {
                    QueryResponse response = new QueryResponse();
                    response.setStatusCode(responseCode);
                    ErrorResponse errorResponse = mapper.readValue(jsonParser, ErrorResponse.class);
                    response.addErrors(errorResponse.getErrors());
                    return response;
                } else {
                    QueryResponse response = mapper.readValue(jsonParser, QueryResponse.class);
                    response.setStatusCode(responseCode);
                    return response;
                }
            }
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        QueryResponse response = new QueryResponse();
        response.setStatusCode(responseCode);
        return response;
    }

    @Override
    public ExecuteResponse execute(ExecuteBuilder builder) throws URISyntaxException, IOException {
        ClientResponse clientResponse = postData(builder.build(), getURLBase() + "/energiscloud-gateway/restful/api/v1/data/execute?apiKey=" + apiKey);
        int responseCode = clientResponse.getStatusCode();

        InputStream stream = clientResponse.getContentStream();
        try {
            if (stream != null) {
                StringWriter resultWriter = new StringWriter();
                IOUtils.copy(stream, resultWriter);

                JsonParser jsonParser = mapper.getFactory().createParser(resultWriter.toString());
                if (responseCode >= 400) {
                    ExecuteResponse response = new ExecuteResponse();
                    response.setStatusCode(responseCode);
                    ErrorResponse errorResponse = mapper.readValue(jsonParser, ErrorResponse.class);
                    response.addErrors(errorResponse.getErrors());
                    return response;
                } else {
                    ExecuteResponse response = mapper.readValue(jsonParser, ExecuteResponse.class);
                    response.setStatusCode(responseCode);
                    return response;
                }
            }
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        ExecuteResponse response = new ExecuteResponse();
        response.setStatusCode(responseCode);
        return response;
    }

    @Override
    public SitesResponse querySites(String companyCode) throws URISyntaxException, IOException {
        ClientResponse clientResponse = queryData(getURLBase() + "/energiscloud-gateway/restful/api/v1/sites?apiKey=" + apiKey + "&companyCode=" + companyCode);
        int responseCode = clientResponse.getStatusCode();

        InputStream stream = clientResponse.getContentStream();
        try {
            if (stream != null) {
                StringWriter resultWriter = new StringWriter();
                IOUtils.copy(stream, resultWriter);

                JsonParser jsonParser = mapper.getFactory().createParser(resultWriter.toString());
                if (responseCode >= 400) {
                    SitesResponse response = new SitesResponse();
                    response.setStatusCode(responseCode);
                    ErrorResponse errorResponse = mapper.readValue(jsonParser, ErrorResponse.class);
                    response.addErrors(errorResponse.getErrors());
                    return response;
                } else {
                    SitesResponse response = mapper.readValue(jsonParser, SitesResponse.class);
                    response.setStatusCode(responseCode);
                    return response;
                }
            }
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        SitesResponse response = new SitesResponse();
        response.setStatusCode(responseCode);
        return response;
    }

    @Override
    public SiteDetailResponse querySite(Long id) throws URISyntaxException, IOException {
        ClientResponse clientResponse = queryData(getURLBase() + "/energiscloud-gateway/restful/api/v1/sites/ + " + id + "?apiKey=" + apiKey);
        int responseCode = clientResponse.getStatusCode();

        InputStream stream = clientResponse.getContentStream();
        try {
            if (stream != null) {
                StringWriter resultWriter = new StringWriter();
                IOUtils.copy(stream, resultWriter);

                JsonParser jsonParser = mapper.getFactory().createParser(resultWriter.toString());
                if (responseCode >= 400) {
                    SiteDetailResponse response = new SiteDetailResponse();
                    response.setStatusCode(responseCode);
                    ErrorResponse errorResponse = mapper.readValue(jsonParser, ErrorResponse.class);
                    response.addErrors(errorResponse.getErrors());
                    return response;
                } else {
                    SiteDetailResponse response = mapper.readValue(jsonParser, SiteDetailResponse.class);
                    response.setStatusCode(responseCode);
                    return response;
                }
            }
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        SiteDetailResponse response = new SiteDetailResponse();
        response.setStatusCode(responseCode);
        return response;
    }

    @Override
    public MetricsResponse queryMetrics(String companyCode, String assetCode, String reference) throws URISyntaxException, IOException {
        ClientResponse clientResponse;
        if (reference != null) {
            clientResponse = queryData(getURLBase() + "/energiscloud-gateway/restful/api/v1/metrics?apiKey=" + apiKey + "&companyCode=" + companyCode + "&reference=" + reference);
        } else {
            clientResponse = queryData(getURLBase() + "/energiscloud-gateway/restful/api/v1/metrics?apiKey=" + apiKey + "&companyCode=" + companyCode);
        }
        int responseCode = clientResponse.getStatusCode();

        InputStream stream = clientResponse.getContentStream();
        try {
            if (stream != null) {
                StringWriter resultWriter = new StringWriter();
                IOUtils.copy(stream, resultWriter);

                JsonParser jsonParser = mapper.getFactory().createParser(resultWriter.toString());
                if (responseCode >= 400) {
                    MetricsResponse response = new MetricsResponse();
                    response.setStatusCode(responseCode);
                    ErrorResponse errorResponse = mapper.readValue(jsonParser, ErrorResponse.class);
                    response.addErrors(errorResponse.getErrors());
                    return response;
                } else {
                    MetricsResponse response = mapper.readValue(jsonParser, MetricsResponse.class);
                    response.setStatusCode(responseCode);
                    return response;
                }
            }
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        MetricsResponse response = new MetricsResponse();
        response.setStatusCode(responseCode);
        return response;
    }

    @Override
    public MetricDetailResponse queryMetric(Long id) throws URISyntaxException, IOException {
        ClientResponse clientResponse = queryData(getURLBase() + "/energiscloud-gateway/restful/api/v1/metrics/ + " + id + "?apiKey=" + apiKey);
        int responseCode = clientResponse.getStatusCode();

        InputStream stream = clientResponse.getContentStream();
        try {
            if (stream != null) {
                StringWriter resultWriter = new StringWriter();
                IOUtils.copy(stream, resultWriter);

                JsonParser jsonParser = mapper.getFactory().createParser(resultWriter.toString());
                if (responseCode >= 400) {
                    MetricDetailResponse response = new MetricDetailResponse();
                    response.setStatusCode(responseCode);
                    ErrorResponse errorResponse = mapper.readValue(jsonParser, ErrorResponse.class);
                    response.addErrors(errorResponse.getErrors());
                    return response;
                } else {
                    MetricDetailResponse response = mapper.readValue(jsonParser, MetricDetailResponse.class);
                    response.setStatusCode(responseCode);
                    return response;
                }
            }
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        MetricDetailResponse response = new MetricDetailResponse();
        response.setStatusCode(responseCode);
        return response;
    }

    protected abstract ClientResponse postData(String json, String url) throws IOException;

    protected abstract ClientResponse queryData(String url) throws IOException;

    private String getURLBase() {
        if (isSSLConnection()) {
            return "https://" + host + ":" + port;
        } else {
            return "http://" + host + ":" + port;
        }
    }

}
