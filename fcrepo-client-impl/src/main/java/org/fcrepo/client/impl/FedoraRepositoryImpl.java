/**
 * Copyright 2014 DuraSpace, Inc.
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
package org.fcrepo.client.impl;

import static java.lang.Integer.MAX_VALUE;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotReader;
import org.apache.jena.riot.lang.CollectorStreamTriples;
import org.fcrepo.client.FedoraContent;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.ReadOnlyException;
import org.slf4j.Logger;

import com.hp.hpl.jena.graph.Triple;

/**
 * FedoraRepositoryImpl manage httpclient instance to run requests
 *
 * @author lsitu
 * @since 2014-08-11
 */
public class FedoraRepositoryImpl implements FedoraRepository {
    private static final Logger LOGGER = getLogger(FedoraRepositoryImpl.class);

    private HttpClient httpClient;
    private String repositoryURL;

    /**
     * Constructor that takes the repository url
     *
     * @param repoUrl
     */
    public FedoraRepositoryImpl(final String repoUrl) {
        this(repoUrl, null, null);
    }

    /**
     * Constructor
     *
     * @param repositoryURL Repository baseURL
     * @param username Repository username
     * @param password Repository password
     */
    public FedoraRepositoryImpl(final String repositoryURL,
                                final String username,
                                final String password) {
        this.repositoryURL = repositoryURL;
        this.httpClient = createHttpClient(repositoryURL, username, password);
    }

    /**
     * Constructor that takes the pre-configured HttpClient
     *
     * @param repositoryURL Repository baseURL
     * @param httpClient Pre-configured httpClient
     */
    public FedoraRepositoryImpl(final String repositoryURL, final HttpClient httpClient) {
        this.repositoryURL = repositoryURL;
        this.httpClient = httpClient;
    }

    protected static DefaultHttpClient createHttpClient(final String repositoryURL,
                                                        final String fedoraUsername,
                                                        final String fedoraPassword) {
        final PoolingClientConnectionManager connMann = new PoolingClientConnectionManager();
        connMann.setMaxTotal(MAX_VALUE);
        connMann.setDefaultMaxPerRoute(MAX_VALUE);

        final DefaultHttpClient httpClient = new DefaultHttpClient(connMann);
        httpClient.setRedirectStrategy(new DefaultRedirectStrategy());
        httpClient.setHttpRequestRetryHandler(new StandardHttpRequestRetryHandler(0, false));

        // If the Fedora instance requires authentication, set it up here
        if (!StringUtils.isBlank(fedoraUsername) && !StringUtils.isBlank(fedoraPassword)) {
            LOGGER.debug("Adding BASIC credentials to client for repo requests.");

            final URI fedoraUri = URI.create(repositoryURL);
            final CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(fedoraUri.getHost(), fedoraUri.getPort()),
                                         new UsernamePasswordCredentials(fedoraUsername, fedoraPassword));

            httpClient.setCredentialsProvider(credsProvider);
        }

        return httpClient;
    }

    @Override
    public boolean exists(final String path) throws FedoraException, ForbiddenException {
        final HttpHead head = new HttpHead(repositoryURL + path);
        try {
            final HttpResponse response = httpClient.execute(head);
            final StatusLine status = response.getStatusLine();
            final int statusCode = status.getStatusCode();
            final String uri = head.getURI().toString();
            if (statusCode == OK.getStatusCode()) {
                return true;
            } else if (statusCode == NOT_FOUND.getStatusCode()) {
                return false;
            } else if (statusCode == FORBIDDEN.getStatusCode()) {
                LOGGER.error("request for resource {} is not authorized.", uri);
                throw new ForbiddenException("request for resource " + uri + " is not authorized.");
            } else {
                LOGGER.error("error checking resource {}: {} {}", uri, statusCode, status.getReasonPhrase());
                throw new FedoraException("error checking resource " + uri + ": " + statusCode + " " +
                                          status.getReasonPhrase());
            }
        } catch (final Exception e) {
            LOGGER.error("could not encode URI parameter", e);
            throw new FedoraException(e);
        } finally {
            head.releaseConnection();
        }
    }

    @Override
    public FedoraDatastream getDatastream(final String path) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FedoraObject getObject(final String path) throws FedoraException, ForbiddenException {
        final HttpGet get = createGetMethod(path, null);

        try {
            get.setHeader("accept", "application/rdf+xml");
            final HttpResponse response = httpClient.execute(get);

            final String uri = get.getURI().toString();
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == OK.getStatusCode()) {
                LOGGER.debug("Datastream for resource {} retrieved", uri);
                // header processing
                final Header[] etagHeader = response.getHeaders("ETag");
                final String etagValue =
                        (etagHeader != null && etagHeader.length > 0) ? etagHeader[0].getValue() : null;
                final HttpEntity entity = response.getEntity();

                // StreamRdf
                final Lang lang = RDFLanguages.contentTypeToLang(entity.getContentType().getValue().split(":")[0]);
                final CollectorStreamTriples streamTriples = new CollectorStreamTriples();
                RiotReader.parse(entity.getContent(), lang, getRepositoryUrl() + path, streamTriples);

                final FedoraObjectImpl fedoraObject = new FedoraObjectImpl(this, path,
                                                                           streamTriples.getCollected().iterator());

                // set etag value
                fedoraObject.setEtagValue(etagValue != null && etagValue.startsWith("\"")
                                                  ? etagValue.substring(1, etagValue.length() - 1) : etagValue);

                return fedoraObject;

            } else if (statusCode == FORBIDDEN.getStatusCode()) {
                LOGGER.error("request for resource {} is not authorized.", uri);
                throw new ForbiddenException("request for resource " + uri + " is not authorized.");
            } else if (statusCode == BAD_REQUEST.getStatusCode()) {
                LOGGER.error("server does not support metadata type application/rdf+xml for resource {} " +
                                     " cannot retrieve", uri);
                throw new BadRequestException("server does not support the request metadata type for resource " + uri);
            } else if (statusCode == NOT_FOUND.getStatusCode()) {
                LOGGER.error("resource {} does not exist, cannot retrieve", uri);
                throw new NotFoundException("resource " + uri + " does not exist, cannot retrieve");
            } else {
                LOGGER.error("error retrieving resource {}: {} {}",
                             uri,
                             statusCode,
                             response.getStatusLine().getReasonPhrase());
                throw new FedoraException("error retrieving resource " + uri + ": " + statusCode + " " +
                                                  response.getStatusLine().getReasonPhrase());
            }
        } catch (final Exception e) {
            LOGGER.error("could not encode URI parameter", e);
            throw new FedoraException(e);
        } finally {
            get.releaseConnection();
        }
    }

    @Override
    public FedoraDatastream createDatastream(final String path, final FedoraContent content) throws ReadOnlyException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FedoraObject createObject(final String path) throws FedoraException {
        final HttpPut put = createPutMethod(path, null);
        try {
            final HttpResponse response = httpClient.execute(put);
            final String uri = put.getURI().toString();
            final StatusLine status = response.getStatusLine();
            final int statusCode = status.getStatusCode();

            if (statusCode == CREATED.getStatusCode()) {
                return getObject(path);
            } else if (statusCode == FORBIDDEN.getStatusCode()) {
                LOGGER.error("request to create resource {} is not authorized.", uri);
                throw new ForbiddenException("request to create resource " + uri + " is not authorized.");
            } else if (statusCode == CONFLICT.getStatusCode()) {
                LOGGER.error("resource {} already exists", uri);
                throw new FedoraException("resource " + uri + " already exists");
            } else {
                LOGGER.error("error creating resource {}: {} {}", uri, statusCode, status.getReasonPhrase());
                throw new FedoraException("error retrieving resource " + uri + ": " + statusCode + " " +
                                                  status.getReasonPhrase());
            }
        } catch (final Exception e) {
            LOGGER.error("could not encode URI parameter", e);
            throw new FedoraException(e);
        } finally {
            put.releaseConnection();
        }
    }

    @Override
    public FedoraDatastream findOrCreateDatastream(final String path) throws ReadOnlyException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FedoraObject findOrCreateObject(final String path) throws FedoraException {
        try {
            return getObject(path);
        } catch ( NotFoundException ex ) {
            return createObject(path);
        }
    }

    @Override
    public Iterator<Triple> getNodeTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void registerNodeTypes(final InputStream cndStream) throws ReadOnlyException {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<String, String> getRepositoryNamespaces() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addNamespace(final String prefix, final String uri) throws ReadOnlyException {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeNamespace(final String prefix) throws ReadOnlyException {
        // TODO Auto-generated method stub

    }

    @Override
    public Long getRepositoryObjectCount() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getRepositorySize() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isWritable() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * Create GET method with list of parameters
     * @param path Resource path, relative to repository baseURL
     * @param params Query parameters
     * @return GET method
    **/
    protected HttpGet createGetMethod(final String path, final Map<String, List<String>> params) {
        return new HttpGet(repositoryURL + path + queryString(params));
    }

    /**
     * Create PUT method with list of parameters
     * @param path Resource path, relative to repository baseURL
     * @param params Query parameters
     * @return PUT method
    **/
    protected HttpPut createPutMethod(final String path, final Map<String, List<String>> params) {
        return new HttpPut(repositoryURL + path + queryString(params));
    }

    /**
     * Encode URL parameters as a query string.
     * @param params Query parameters
    **/
    protected String queryString( final Map<String, List<String>> params ) {
        final StringBuilder builder = new StringBuilder();
        if (params != null && params.size() > 0) {
            for (final Iterator<String> it = params.keySet().iterator(); it.hasNext(); ) {
                final String key = it.next();
                final List<String> values = params.get(key);
                for (final String value : values) {
                    try {
                        builder.append(key + "=" + URLEncoder.encode(value, "UTF-8") + "&");
                    } catch (final UnsupportedEncodingException e) {
                        builder.append(key + "=" + value + "&");
                    }
                }
            }
            return builder.length() > 0 ? "?" + builder.substring(0, builder.length() - 1) : "";
        }
        return "";
    }

    /**
     * Get the repository baseURL.
    **/
    public String getRepositoryUrl() {
        // TODO Auto-generated method stub
        return repositoryURL;
    }
}
