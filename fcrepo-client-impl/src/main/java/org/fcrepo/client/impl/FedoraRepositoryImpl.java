/*
 * Copyright 2015 DuraSpace, Inc.
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

import com.hp.hpl.jena.graph.Triple;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.fcrepo.client.AlreadyExistsException;
import org.fcrepo.client.FedoraContent;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.ForbiddenException;
import org.fcrepo.client.NotFoundException;
import org.fcrepo.client.ReadOnlyException;
import org.fcrepo.client.utils.HttpHelper;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * FedoraRepositoryImpl manage httpclient instance to run requests
 *
 * @author lsitu
 * @author escowles
 * @author gushakov
 * @since 2014-08-11
 */
public class FedoraRepositoryImpl implements FedoraRepository {
    private static final Logger LOGGER = getLogger(FedoraRepositoryImpl.class);

    private static final ThreadLocal<String> TX_ID = new ThreadLocal<>();

    private static final String TX = "tx:";
    private static final String FCR_TX = "/fcr:tx";
    private static final String FCR_COMMIT = "/fcr:commit";
    private static final String FCR_ROLLBACK = "/fcr:rollback";

    protected HttpHelper httpHelper;
    protected String repositoryURL;

    protected FedoraRepositoryImpl() {
        // for subclasses
    }

    /**
     * Constructor that takes the repository url
     *
     * @param repositoryURL Fedora base URL.
     */
    public FedoraRepositoryImpl(final String repositoryURL) {
        this(repositoryURL, null, null);
    }

    /**
     * Constructor
     *
     * @param repositoryURL Repository base URL
     * @param username Repository username
     * @param password Repository password
     */
    public FedoraRepositoryImpl(final String repositoryURL, final String username, final String password) {
        this.repositoryURL = repositoryURL;
        this.httpHelper = new HttpHelper(repositoryURL, username, password, false);
    }

    /**
     * Constructor that takes the pre-configured HttpClient
     *
     * @param repositoryURL Repository baseURL
     * @param httpClient Pre-configured httpClient
     */
    public FedoraRepositoryImpl(final String repositoryURL, final HttpClient httpClient) {
        this.repositoryURL = repositoryURL;
        this.httpHelper = new HttpHelper(repositoryURL, httpClient, false);
    }

    @Override
    public boolean exists(final String path) throws FedoraException, ForbiddenException {
        final HttpHead head = httpHelper.createHeadMethod(prependTransactionId(path));
        try {
            final HttpResponse response = httpHelper.execute(head);
            final StatusLine status = response.getStatusLine();
            final int statusCode = status.getStatusCode();
            final String uri = head.getURI().toString();
            if (statusCode == SC_OK) {
                return true;
            } else if (statusCode == SC_NOT_FOUND) {
                return false;
            } else if (statusCode == SC_FORBIDDEN) {
                LOGGER.error("request for resource {} is not authorized.", uri);
                throw new ForbiddenException("request for resource " + uri + " is not authorized.");
            } else {
                LOGGER.error("error checking resource {}: {} {}", uri, statusCode, status.getReasonPhrase());
                throw new FedoraException("error checking resource " + uri + ": " + statusCode + " " +
                                          status.getReasonPhrase());
            }
        } catch (final Exception e) {
            LOGGER.error("Could not encode URI parameter: {}", e.getMessage());
            throw new FedoraException(e);
        } finally {
            head.releaseConnection();
        }
    }

    @Override
    public FedoraDatastream getDatastream(final String path) throws FedoraException {
        return (FedoraDatastream) httpHelper
                .loadProperties(new FedoraDatastreamImpl(this, httpHelper, prependTransactionId(path)));
    }

    @Override
    public FedoraObject getObject(final String path) throws FedoraException {
        return (FedoraObject) httpHelper
                .loadProperties(new FedoraObjectImpl(this, httpHelper, prependTransactionId(path)));
    }

    @Override
    public FedoraDatastream createDatastream(final String path, final FedoraContent content) throws FedoraException {
        final HttpPut put = httpHelper.createContentPutMethod(prependTransactionId(path), null, content);
        try {
            final HttpResponse response = httpHelper.execute(put);
            final String uri = put.getURI().toString();
            final StatusLine status = response.getStatusLine();
            final int statusCode = status.getStatusCode();

            if (statusCode == SC_CREATED) {
                return getDatastream(path);
            } else if (statusCode == SC_FORBIDDEN) {
                LOGGER.error("request to create resource {} is not authorized.", uri);
                throw new ForbiddenException("request to create resource " + uri + " is not authorized.");
            } else if (statusCode == SC_CONFLICT) {
                LOGGER.error("resource {} already exists", uri);
                throw new AlreadyExistsException("resource " + uri + " already exists");
            } else {
                LOGGER.error("error creating resource {}: {} {}", uri, statusCode, status.getReasonPhrase());
                throw new FedoraException("error retrieving resource " + uri + ": " + statusCode + " " +
                                                  status.getReasonPhrase());
            }
        } catch (final Exception e) {
            LOGGER.error("Could not encode URI parameter: {}", e.getMessage());
            throw new FedoraException(e);
        } finally {
            put.releaseConnection();
        }
    }

    @Override
    public FedoraDatastream createOrUpdateRedirectDatastream(final String path, final String url)
            throws FedoraException {
        final HttpPut put = httpHelper.createContentPutMethod(prependTransactionId(path), null, null);
        try {
            put.setHeader("Content-Type", "message/external-body; access-type=URL; URL=\"" + url + "\"");
            final HttpResponse response = httpHelper.execute(put);
            final String uri = put.getURI().toString();
            final StatusLine status = response.getStatusLine();
            final int statusCode = status.getStatusCode();

            if (statusCode == SC_CREATED) {
                return getDatastream(path);
            } else if (statusCode == SC_FORBIDDEN) {
                LOGGER.error("request to create resource {} is not authorized.", uri);
                throw new ForbiddenException("request to create resource " + uri + " is not authorized.");
            } else {
                LOGGER.error("error creating resource {}: {} {}", uri, statusCode, status.getReasonPhrase());
                throw new FedoraException("error creating resource " + uri + ": " + statusCode + " " +
                        status.getReasonPhrase());
            }
        } catch (final Exception e) {
            LOGGER.error("Error making or building PUT request: {}", e.getMessage());
            throw new FedoraException(e);
        } finally {
            put.releaseConnection();
        }
    }

    @Override
    public FedoraObject createObject(final String path) throws FedoraException {
        final HttpPut put = httpHelper.createPutMethod(prependTransactionId(path), null);
        try {
            final HttpResponse response = httpHelper.execute(put);
            final String uri = put.getURI().toString();
            final StatusLine status = response.getStatusLine();
            final int statusCode = status.getStatusCode();

            if (statusCode == SC_CREATED) {
                return getObject(path);
            } else if (statusCode == SC_FORBIDDEN) {
                LOGGER.error("request to create resource {} is not authorized.", uri);
                throw new ForbiddenException("request to create resource " + uri + " is not authorized.");
            } else if (statusCode == SC_CONFLICT) {
                LOGGER.error("resource {} already exists", uri);
                throw new AlreadyExistsException("resource " + uri + " already exists");
            } else {
                LOGGER.error("error creating resource {}: {} {}", uri, statusCode, status.getReasonPhrase());
                throw new FedoraException("error retrieving resource " + uri + ": " + statusCode + " " +
                                                  status.getReasonPhrase());
            }
        } catch (final Exception e) {
            LOGGER.error("Could not encode URI parameter: {}", e.getMessage());
            throw new FedoraException(e);
        } finally {
            put.releaseConnection();
        }
    }

    @Override
    public FedoraObject createResource(final String containerPath) throws FedoraException {
        final HttpPost post = httpHelper
                .createPostMethod(containerPath == null
                        ? prependTransactionId("") : prependTransactionId(containerPath), null);
        try {
            final HttpResponse response = httpHelper.execute(post);
            final String uri = post.getURI().toString();
            final StatusLine status = response.getStatusLine();
            final int statusCode = status.getStatusCode();

            if (statusCode == SC_CREATED) {
                return getObject(response.getFirstHeader("Location").getValue().substring(repositoryURL.length()));
            } else if (statusCode == SC_FORBIDDEN) {
                LOGGER.error("request to create resource {} is not authorized.", uri);
                throw new ForbiddenException("request to create resource " + uri + " is not authorized.");
            } else {
                LOGGER.error("error creating resource {}: {} {}", uri, statusCode, status.getReasonPhrase());
                throw new FedoraException("error creating resource " + uri + ": " + statusCode + " " +
                        status.getReasonPhrase());
            }
        } catch (final Exception e) {
            LOGGER.error("Could not encode URI parameter: {}", e.getMessage());
            throw new FedoraException(e);
        } finally {
            post.releaseConnection();
        }
    }

    @Override
    public FedoraDatastream findOrCreateDatastream(final String path) throws FedoraException {
        try {
            return getDatastream(path);
        } catch ( NotFoundException ex ) {
            return createDatastream(path, null);
        }
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
        return true;
    }

    @Override
    public String startTransaction() throws FedoraException {
        final HttpPost post = httpHelper.createPostMethod(FCR_TX, null);
        try {

            final HttpResponse response = httpHelper.execute(post);
            final String uri = post.getURI().toString();
            final StatusLine status = response.getStatusLine();
            final int statusCode = status.getStatusCode();

            if (statusCode == SC_CREATED) {
                final String txId = response.getFirstHeader("Location").getValue().substring(repositoryURL.length());
                TX_ID.set(txId);
                LOGGER.debug("Started transaction {}", txId);
                return txId;
            } else if (statusCode == SC_FORBIDDEN) {
                LOGGER.error("request to start new transaction {} is not authorized.", uri);
                throw new ForbiddenException("request to start new transaction " + uri + " is not authorized.");
            } else {
                LOGGER.error("error start new transaction {}: {} {}", uri, statusCode, status.getReasonPhrase());
                throw new FedoraException("error start new transaction " + uri + ": " + statusCode + " " +
                        status.getReasonPhrase());
            }

        } catch (final Exception e) {
            LOGGER.error("Could not encode URI parameter: ", e.getMessage());
            throw new FedoraException(e);
        } finally {
            post.releaseConnection();
        }

    }

    @Override
    public void commitTransaction() throws FedoraException {
        final HttpPost post = httpHelper.createPostMethod(TX_ID.get()
                + FCR_TX + FCR_COMMIT, null);
        try {
            final HttpResponse response = httpHelper.execute(post);
            final String uri = post.getURI().toString();
            final StatusLine status = response.getStatusLine();
            final int statusCode = status.getStatusCode();

            if (statusCode == SC_FORBIDDEN) {
                LOGGER.error("request to commit transaction {} is not authorized.", uri);
                throw new ForbiddenException("request to commit transaction " + uri + " is not authorized.");
            } else {
                if (statusCode != SC_NO_CONTENT) {
                    LOGGER.error("error committing back transaction {}: {} {}", uri,
                            statusCode, status.getReasonPhrase());
                    throw new FedoraException("error committing transaction " + uri + ": " + statusCode + " " +
                            status.getReasonPhrase());
                }
            }
            LOGGER.debug("Committed transaction {}", TX_ID.get());
        } catch (final Exception e) {
            LOGGER.error("Could not encode URI parameter: {}", e.getMessage());
            throw new FedoraException(e);
        } finally {
            post.releaseConnection();
            TX_ID.remove();
        }
    }

    @Override
    public void rollbackTransaction() throws FedoraException {
        final HttpPost post = httpHelper.createPostMethod(TX_ID.get()
                + FCR_TX + FCR_ROLLBACK, null);
        try {
            final HttpResponse response = httpHelper.execute(post);
            final String uri = post.getURI().toString();
            final StatusLine status = response.getStatusLine();
            final int statusCode = status.getStatusCode();

            if (statusCode == SC_FORBIDDEN) {
                LOGGER.error("request to rollback transaction {} is not authorized.", uri);
                throw new ForbiddenException("request to rollback transaction " + uri + " is not authorized.");
            } else {
                if (statusCode != SC_NO_CONTENT) {
                    LOGGER.error("error rolling back transaction {}: {} {}", uri, statusCode, status.getReasonPhrase());
                    throw new FedoraException("error rolling back transaction " + uri + ": " + statusCode + " " +
                            status.getReasonPhrase());
                }
            }

            LOGGER.debug("Rolled back transaction {}", TX_ID.get());
        } catch (final Exception e) {
            LOGGER.error("Could not encode URI parameter: {}", e.getMessage());
            throw new FedoraException(e);
        } finally {
            post.releaseConnection();
            TX_ID.remove();
        }
    }

    @Override
    public String getRepositoryUrl() {
        return repositoryURL;
    }

    private String prependTransactionId(final String path) {
        final String txId = TX_ID.get();
        // append (if needed) tx id to the path if a transaction has been started for this thread
        if (txId == null || path.contains(TX)) {
            return path;
        } else {
            // respect the slash (if present) in the original path
            if (path.startsWith("/")) {
                return "/" + txId + path;
            } else {
                return txId + "/" + path;
            }
        }
    }
}
