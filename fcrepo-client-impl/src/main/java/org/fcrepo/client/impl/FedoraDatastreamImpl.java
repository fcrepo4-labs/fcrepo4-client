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
package org.fcrepo.client.impl;

import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createProperty;

import static org.fcrepo.kernel.RdfLexicon.DESCRIBES;
import static org.fcrepo.kernel.RdfLexicon.HAS_ORIGINAL_NAME;
import static org.fcrepo.kernel.RdfLexicon.HAS_MIME_TYPE;
import static org.fcrepo.kernel.RdfLexicon.HAS_SIZE;
import static org.fcrepo.kernel.RdfLexicon.REPOSITORY_NAMESPACE;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.fcrepo.client.ForbiddenException;
import org.fcrepo.client.NotFoundException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;

import org.apache.jena.atlas.lib.NotImplemented;

import org.fcrepo.client.FedoraContent;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.utils.HttpHelper;

import org.fcrepo.kernel.FedoraJcrTypes;
import org.slf4j.Logger;

/**
 * A Fedora Datastream Impl.
 *
 * @author escowles
 * @since 2014-08-25
 */
public class FedoraDatastreamImpl extends FedoraResourceImpl implements FedoraDatastream {
    private static final Logger LOGGER = getLogger(FedoraDatastreamImpl.class);
    protected static final Property REST_API_DIGEST = createProperty(REPOSITORY_NAMESPACE + "digest");
    private boolean hasContent;
    private Node contentSubject;

    /**
     * Constructor for FedoraDatastreamImpl
     *
     * @param repository Repository that created this object.
     * @param httpHelper HTTP helper for making repository requests
     * @param path Path of the datastream in the repository
     */
    public FedoraDatastreamImpl(final FedoraRepository repository, final HttpHelper httpHelper, final String path) {
        super(repository, httpHelper, path);
        contentSubject = NodeFactory.createURI(
                repository.getRepositoryUrl() + path.substring(0, path.lastIndexOf("/")) );
    }

    @Override
    public void setGraph( final Graph graph ) {
        super.setGraph( graph );
        hasContent = getTriple( subject, DESCRIBES ) != null;
    }

    @Override
    public String getPropertiesPath() {
        return path + "/" + FedoraJcrTypes.FCR_METADATA;
    }

    @Override
    public boolean hasContent() throws FedoraException {
        return hasContent;
    }

    @Override
    public FedoraObject getObject() throws FedoraException {
        final String contentPath = path.substring(0, path.lastIndexOf("/"));
        return repository.getObject( contentPath.substring(0, contentPath.lastIndexOf("/")) );
    }

    @Override
    public String getName() {
        final String p = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        final String[] paths = p.split("/");
        return paths[paths.length - 2];
    }

    @Override
    public URI getContentDigest() throws FedoraException {
        final Node contentDigest = getObjectValue( REST_API_DIGEST );
        try {
            if ( contentDigest == null ) {
                return null;
            }

            return new URI( contentDigest.getURI() );
        } catch ( final URISyntaxException e ) {
            throw new FedoraException("Error parsing checksum URI: " + contentDigest.getURI(), e);
        }
    }

    @Override
    public Long getContentSize() throws FedoraException {
        final Node size = getObjectValue( HAS_SIZE );
        if ( size == null ) {
            return null;
        }

        return new Long( size.getLiteralValue().toString() );
    }

    @Override
    public String getFilename() throws FedoraException {
        final Node filename = getObjectValue( HAS_ORIGINAL_NAME );
        if ( filename == null ) {
            return null;
        }

        return filename.getLiteralValue().toString();
    }

    @Override
    public String getContentType() throws FedoraException {
        final Node contentType = getObjectValue( HAS_MIME_TYPE );
        if ( contentType == null ) {
            return null;
        }

        return contentType.getLiteralValue().toString();
    }

    @Override
    public void updateContent( final FedoraContent content ) throws FedoraException {
        final HttpPut put = httpHelper.createContentPutMethod( path, null, content );

        try {
            final HttpResponse response = httpHelper.execute( put );
            final StatusLine status = response.getStatusLine();
            final String uri = put.getURI().toString();

            if ( status.getStatusCode() == SC_CREATED
                    || status.getStatusCode() == SC_NO_CONTENT) {
                LOGGER.debug("content updated successfully for resource {}", uri);
            } else if ( status.getStatusCode() == SC_FORBIDDEN) {
                LOGGER.error("request for resource {} is not authorized.", uri);
                throw new ForbiddenException("request for resource " + uri + " is not authorized.");
            } else if ( status.getStatusCode() == SC_NOT_FOUND) {
                LOGGER.error("resource {} does not exist, cannot retrieve", uri);
                throw new NotFoundException("resource " + uri + " does not exist, cannot retrieve");
            } else if ( status.getStatusCode() == SC_CONFLICT) {
                LOGGER.error("checksum mismatch for {}", uri);
                throw new FedoraException("checksum mismatch for resource " + uri);
            } else {
                LOGGER.error("error retrieving resource {}: {} {}", uri, status.getStatusCode(),
                             status.getReasonPhrase());
                throw new FedoraException("error retrieving resource " + uri + ": " + status.getStatusCode() + " " +
                                          status.getReasonPhrase());
            }

            // update properties from server
            httpHelper.loadProperties(this);

        } catch (final FedoraException e) {
            throw e;
        } catch (final Exception e) {
            LOGGER.error("could not encode URI parameter", e);
            throw new FedoraException(e);
        } finally {
            put.releaseConnection();
        }
    }

    @Override
    public InputStream getContent() throws FedoraException {
        final HttpGet get = httpHelper.createGetMethod( path, null );
        final String uri = get.getURI().toString();

        try {
            final HttpResponse response = httpHelper.execute( get );
            final StatusLine status = response.getStatusLine();

            if ( status.getStatusCode() == SC_OK) {
                return response.getEntity().getContent();
            } else if ( status.getStatusCode() == SC_FORBIDDEN) {
                LOGGER.error("request for resource {} is not authorized.", uri);
                throw new ForbiddenException("request for resource " + uri + " is not authorized.");
            } else if ( status.getStatusCode() == SC_NOT_FOUND) {
                LOGGER.error("resource {} does not exist, cannot retrieve", uri);
                throw new NotFoundException("resource " + uri + " does not exist, cannot retrieve");
            } else {
                LOGGER.error("error retrieving resource {}: {} {}", uri, status.getStatusCode(),
                             status.getReasonPhrase());
                throw new FedoraException("error retrieving resource " + uri + ": " + status.getStatusCode() + " " +
                                          status.getReasonPhrase());
            }
        } catch (final Exception e) {
            LOGGER.error("could not encode URI parameter", e);
            throw new FedoraException(e);
        } finally {
            get.releaseConnection();
        }
    }

    @Override
    public void checkFixity() {
        throw new NotImplemented("Method checkFixity() is not implemented");
    }

    private Node getObjectValue( final Property property ) {
        if ( !hasContent ) {
            return null;
        }

        final Triple t = getTriple( contentSubject, property );
        if ( t == null ) {
            return null;
        }

        return t.getObject();
    }
}
