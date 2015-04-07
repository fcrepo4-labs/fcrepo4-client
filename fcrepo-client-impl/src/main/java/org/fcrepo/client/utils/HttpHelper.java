/**
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
package org.fcrepo.client.utils;

import static java.lang.Integer.MAX_VALUE;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;

import static org.apache.jena.riot.WebContent.contentTypeSPARQLUpdate;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.fcrepo.client.BadRequestException;
import org.fcrepo.client.ForbiddenException;
import org.fcrepo.client.NotFoundException;

import com.hp.hpl.jena.graph.Node;

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
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
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
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.ReadOnlyException;
import org.fcrepo.client.impl.FedoraResourceImpl;

import org.slf4j.Logger;


/**
 * HTTP utilities
 * @author escowles
 * @since 2014-08-26
**/
public class HttpHelper {
    private static final Logger LOGGER = getLogger(HttpHelper.class);

    private final String repositoryURL;
    private final HttpClient httpClient;
    private final boolean readOnly;

    /**
     * Create an HTTP helper with a pre-configured HttpClient instance.
     * @param repositoryURL Fedora base URL.
     * @param httpClient Pre-configured HttpClient instance.
     * @param readOnly If true, throw an exception when an update is attempted.
    **/
    public HttpHelper(final String repositoryURL, final HttpClient httpClient, final boolean readOnly) {
        this.repositoryURL = repositoryURL;
        this.httpClient = httpClient;
        this.readOnly = readOnly;
    }

    /**
     * Create an HTTP helper for the specified repository.  If fedoraUsername and fedoraPassword are not null, then
     * they will be used to connect to the repository.
     * @param repositoryURL Fedora base URL.
     * @param fedoraUsername Fedora username
     * @param fedoraPassword Fedora password
     * @param readOnly If true, throw an exception when an update is attempted.
    **/
    public HttpHelper(final String repositoryURL, final String fedoraUsername, final String fedoraPassword,
                      final boolean readOnly) {
        this.repositoryURL = repositoryURL;
        this.readOnly = readOnly;

        final PoolingClientConnectionManager connMann = new PoolingClientConnectionManager();
        connMann.setMaxTotal(MAX_VALUE);
        connMann.setDefaultMaxPerRoute(MAX_VALUE);

        final DefaultHttpClient httpClient = new DefaultHttpClient(connMann);
        httpClient.setRedirectStrategy(new DefaultRedirectStrategy());
        httpClient.setHttpRequestRetryHandler(new StandardHttpRequestRetryHandler(0, false));

        // If the Fedora instance requires authentication, set it up here
        if (!isBlank(fedoraUsername) && !isBlank(fedoraPassword)) {
            LOGGER.debug("Adding BASIC credentials to client for repo requests.");

            final URI fedoraUri = URI.create(repositoryURL);
            final CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(fedoraUri.getHost(), fedoraUri.getPort()),
                                         new UsernamePasswordCredentials(fedoraUsername, fedoraPassword));

            httpClient.setCredentialsProvider(credsProvider);
        }

        this.httpClient = httpClient;
    }

    /**
     * Execute a request for a subclass.
    **/
    public HttpResponse execute( final HttpUriRequest request ) throws IOException, ReadOnlyException {
        if ( readOnly ) {
            switch ( request.getMethod().toLowerCase() ) {
                case "copy": case "delete": case "move": case "patch": case "post": case "put":
                    throw new ReadOnlyException();
                default:
                    break;
            }
        }

        return httpClient.execute(request);
    }

    /**
     * Encode URL parameters as a query string.
     * @param params Query parameters
    **/
    private static String queryString( final Map<String, List<String>> params ) {
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
     * Create HEAD method
     * @param path Resource path, relative to repository baseURL
     * @return HEAD method
    **/
    public HttpHead createHeadMethod(final String path) {
        return new HttpHead(repositoryURL + path);
    }

    /**
     * Create GET method with list of parameters
     * @param path Resource path, relative to repository baseURL
     * @param params Query parameters
     * @return GET method
    **/
    public HttpGet createGetMethod(final String path, final Map<String, List<String>> params) {
        return new HttpGet(repositoryURL + path + queryString(params));
    }

    /**
     * Create a request to update triples with SPARQL Update.
     * @param path The datastream path.
     * @param sparqlUpdate SPARQL Update command.
    **/
    public HttpPatch createPatchMethod(final String path, final String sparqlUpdate) throws FedoraException {
        if ( isBlank(sparqlUpdate) ) {
            throw new FedoraException("SPARQL Update command must not be blank");
        }

        final HttpPatch patch = new HttpPatch(repositoryURL + path);
        patch.setEntity( new ByteArrayEntity(sparqlUpdate.getBytes()) );
        patch.setHeader("Content-Type", contentTypeSPARQLUpdate);
        return patch;
    }

    /**
     * Create POST method with list of parameters
     * @param path Resource path, relative to repository baseURL
     * @param params Query parameters
     * @return PUT method
    **/
    public HttpPost createPostMethod(final String path, final Map<String, List<String>> params) {
        return new HttpPost(repositoryURL + path + queryString(params));
    }

    /**
     * Create PUT method with list of parameters
     * @param path Resource path, relative to repository baseURL
     * @param params Query parameters
     * @return PUT method
    **/
    public HttpPut createPutMethod(final String path, final Map<String, List<String>> params) {
        return new HttpPut(repositoryURL + path + queryString(params));
    }

    /**
     * Create a request to create/update content.
     * @param path The datastream path.
     * @param content Content parameters.
    **/
    public HttpPut createContentPutMethod(final String path, final Map<String, List<String>> params,
                                          final FedoraContent content ) {
        String contentPath = path;
        if ( content != null && content.getChecksum() != null ) {
            contentPath += "?checksum=" + content.getChecksum();
        }

        final HttpPut put = createPutMethod( contentPath, params );

        // content stream
        if ( content != null ) {
            put.setEntity( new InputStreamEntity(content.getContent()) );
        }

        // filename
        if ( content != null && content.getFilename() != null ) {
            put.setHeader("Content-Disposition", "attachment; filename=\"" + content.getFilename() + "\"" );
        }

        // content type
        if ( content != null && content.getContentType() != null ) {
            put.setHeader("Content-Type", content.getContentType() );
        }

        return put;
    }

    /**
     * Create a request to update triples.
     * @param path The datastream path.
     * @param updatedProperties InputStream containing RDF.
     * @param contentType Content type of the RDF in updatedProperties (e.g., "text/rdf+n3" or
     *        "application/rdf+xml").
    **/
    public HttpPut createTriplesPutMethod(final String path, final InputStream updatedProperties,
                                          final String contentType) throws FedoraException {
        if ( updatedProperties == null ) {
            throw new FedoraException("updatedProperties must not be null");
        } else if ( isBlank(contentType) ) {
            throw new FedoraException("contentType must not be blank");
        }

        final HttpPut put = new HttpPut(repositoryURL + path);
        put.setEntity( new InputStreamEntity(updatedProperties) );
        put.setHeader("Content-Type", contentType);
        return put;
    }

    /**
     * Retrieve RDF from the repository and update the properties of a resource
     * @param resource The resource to update
     * @return the updated resource
    **/
    public FedoraResourceImpl loadProperties( final FedoraResourceImpl resource ) throws FedoraException {
        final String path = resource.getPropertiesPath();
        final HttpGet get = createGetMethod(path, null);
        if (resource instanceof FedoraObject) {
            get.addHeader("Prefer", "return=representation; "
                + "include=\"http://fedora.info/definitions/v4/repository#EmbedResources\"");
        }

        try {
            get.setHeader("accept", "application/rdf+xml");
            final HttpResponse response = execute(get);

            final String uri = get.getURI().toString();
            final StatusLine status = response.getStatusLine();

            if (status.getStatusCode() == SC_OK) {
                LOGGER.debug("Updated properties for resource {}", uri);

                // header processing
                final Header[] etagHeader = response.getHeaders("ETag");
                if (etagHeader != null && etagHeader.length > 0) {
                    resource.setEtagValue( etagHeader[0].getValue() );
                }

                // StreamRdf
                final HttpEntity entity = response.getEntity();
                final Lang lang = RDFLanguages.contentTypeToLang(entity.getContentType().getValue().split(":")[0]);
                final CollectorStreamTriples streamTriples = new CollectorStreamTriples();
                RiotReader.parse(entity.getContent(), lang, uri, streamTriples);
                resource.setGraph( RDFSinkFilter.filterTriples(streamTriples.getCollected().iterator(), Node.ANY) );
                return resource;
            } else if (status.getStatusCode() == SC_FORBIDDEN) {
                LOGGER.info("request for resource {} is not authorized.", uri);
                throw new ForbiddenException("request for resource " + uri + " is not authorized.");
            } else if (status.getStatusCode() == SC_BAD_REQUEST) {
                LOGGER.info("server does not support metadata type application/rdf+xml for resource {} " +
                                     " cannot retrieve", uri);
                throw new BadRequestException("server does not support the request metadata type for resource " + uri);
            } else if (status.getStatusCode() == SC_NOT_FOUND) {
                LOGGER.info("resource {} does not exist, cannot retrieve", uri);
                throw new NotFoundException("resource " + uri + " does not exist, cannot retrieve");
            } else {
                LOGGER.info("unexpected status code ({}) when retrieving resource {}", status.getStatusCode(), uri);
                throw new FedoraException("error retrieving resource " + uri + ": " + status.getStatusCode() + " "
                                          + status.getReasonPhrase());
            }
        } catch (final FedoraException e) {
            throw e;
        } catch (final Exception e) {
            e.printStackTrace();
            LOGGER.info("could not encode URI parameter", e);
            throw new FedoraException(e);
        } finally {
            get.releaseConnection();
        }
    }

}
