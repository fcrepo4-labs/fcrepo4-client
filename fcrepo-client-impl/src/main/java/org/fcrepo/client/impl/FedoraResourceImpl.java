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
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.http.client.methods.HttpPost;
import org.apache.jena.atlas.lib.NotImplemented;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPut;

import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.FedoraResource;
import org.fcrepo.client.ForbiddenException;
import org.fcrepo.client.NotFoundException;
import org.fcrepo.client.ReadOnlyException;
import org.fcrepo.client.utils.HttpHelper;
import org.fcrepo.kernel.RdfLexicon;

import org.slf4j.Logger;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * A Fedora Object Impl.
 *
 * @author lsitu
 * @author escowles
 * @since 2014-08-11
 */
public class FedoraResourceImpl implements FedoraResource {
    private static final Logger LOGGER = getLogger(FedoraResourceImpl.class);

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    protected FedoraRepository repository = null;

    protected HttpHelper httpHelper = null;

    protected String path = null;

    protected Node subject = null;

    protected Graph graph;

    private String etagValue = null;

    /**
     * FedoraResourceImpl constructor
     *
     * @param repository FedoraRepositoryImpl that created this resource
     * @param httpHelper HTTP helper for making repository requests
     * @param path Repository path of this resource
     */
    public FedoraResourceImpl(final FedoraRepository repository, final HttpHelper httpHelper, final String path) {
        this.repository = repository;
        this.httpHelper = httpHelper;
        this.path = path;
        subject = NodeFactory.createURI(repository.getRepositoryUrl() + path);
    }

    @Override
    public void copy(final String destination) throws ReadOnlyException {
        // TODO Auto-generated method stub
        throw new NotImplemented("Method copy(final String destination) is not implemented.");
    }

    @Override
    public void delete() throws ReadOnlyException {
        // TODO Auto-generated method stub
        throw new NotImplemented("Method delete() is not implemented.");
    }

    @Override
    public Date getCreatedDate() {
        return getDate(RdfLexicon.CREATED_DATE);
    }

    @Override
    public String getEtagValue() {
        return etagValue;
    }

    /**
     * set etagValue
     *
     * @param etagValue
     */
    public void setEtagValue(final String etagValue) {
        this.etagValue = etagValue;
    }

    @Override
    public Date getLastModifiedDate() {
        return getDate(RdfLexicon.LAST_MODIFIED_DATE);
    }

    @Override
    public Collection<String> getMixins() {
        return getPropertyValues(RdfLexicon.HAS_MIXIN_TYPE);
    }

    @Override
    public String getName() {
        final String p = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        final String[] paths = p.split("/");
        return paths[paths.length - 1];
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Iterator<Triple> getProperties() {
        return graph.find(Node.ANY, Node.ANY, Node.ANY);
    }

    @Override
    public Long getSize() {
        return (long) graph.size();
    }

    @Override
    public void move(final String destination) throws ReadOnlyException {
        // TODO Auto-generated method stub
        throw new NotImplemented("Method move(final String destination) is not implemented.");
    }

    @Override
    public void updateProperties(final String sparqlUpdate) throws FedoraException {
        final HttpPatch patch = httpHelper.createPatchMethod(getPropertiesPath(), sparqlUpdate);

        try {
            final HttpResponse response = httpHelper.execute( patch );
            final StatusLine status = response.getStatusLine();
            final String uri = patch.getURI().toString();

            if ( status.getStatusCode() == SC_NO_CONTENT) {
                LOGGER.debug("triples updated successfully for resource {}", uri);
            } else if ( status.getStatusCode() == SC_FORBIDDEN) {
                LOGGER.error("updating resource {} is not authorized.", uri);
                throw new ForbiddenException("updating resource " + uri + " is not authorized.");
            } else if ( status.getStatusCode() == SC_NOT_FOUND) {
                LOGGER.error("resource {} does not exist, cannot update", uri);
                throw new NotFoundException("resource " + uri + " does not exist, cannot update");
            } else if ( status.getStatusCode() == SC_CONFLICT) {
                LOGGER.error("resource {} is locked", uri);
                throw new FedoraException("resource is locked: " + uri);
            } else {
                LOGGER.error("error updating resource {}: {} {}", uri, status.getStatusCode(),
                             status.getReasonPhrase());
                throw new FedoraException("error updating resource " + uri + ": " + status.getStatusCode() + " " +
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
            patch.releaseConnection();
        }
    }

    @Override
    public void updateProperties(final InputStream updatedProperties, final String contentType)
            throws FedoraException {

        final HttpPut put = httpHelper.createTriplesPutMethod(getPropertiesPath(), updatedProperties, contentType);

        try {
            final HttpResponse response = httpHelper.execute( put );
            final StatusLine status = response.getStatusLine();
            final String uri = put.getURI().toString();

            if ( status.getStatusCode() == SC_NO_CONTENT) {
                LOGGER.debug("triples updated successfully for resource {}", uri);
            } else if ( status.getStatusCode() == SC_FORBIDDEN) {
                LOGGER.error("updating resource {} is not authorized.", uri);
                throw new ForbiddenException("updating resource " + uri + " is not authorized.");
            } else if ( status.getStatusCode() == SC_NOT_FOUND) {
                LOGGER.error("resource {} does not exist, cannot update", uri);
                throw new NotFoundException("resource " + uri + " does not exist, cannot update");
            } else if ( status.getStatusCode() == SC_CONFLICT) {
                LOGGER.error("resource {} is locked", uri);
                throw new FedoraException("resource is locked: " + uri);
            } else {
                LOGGER.error("error updating resource {}: {} {}", uri, status.getStatusCode(),
                             status.getReasonPhrase());
                throw new FedoraException("error updating resource " + uri + ": " + status.getStatusCode() + " " +
                                          status.getReasonPhrase());
            }

            // update properties from server
            httpHelper.loadProperties(this);

        } catch (final FedoraException e) {
            throw e;
        } catch (final Exception e) {
            LOGGER.error("Error executing request", e);
            throw new FedoraException(e);
        } finally {
            put.releaseConnection();
        }
    }

    @Override
    public boolean isWritable() {
        final Collection<String> values = getPropertyValues(RdfLexicon.WRITABLE);
        if (values != null && values.size() > 0) {
            final Iterator<String> it = values.iterator();
            return Boolean.parseBoolean(it.next());
        }
        return false;
    }

    @Override
    public void createVersionSnapshot(final String label) throws FedoraException {
        final HttpPost postVersion = httpHelper.createPostMethod(path + "/fcr:versions", null);
        try {
            postVersion.setHeader("Slug", label);
            final HttpResponse response = httpHelper.execute(postVersion);
            final StatusLine status = response.getStatusLine();
            final String uri = postVersion.getURI().toString();

            if ( status.getStatusCode() == SC_NO_CONTENT) {
                LOGGER.debug("new version created for resource at {}", uri);
            } else if ( status.getStatusCode() == SC_CONFLICT) {
                LOGGER.debug("The label {} is in use by another version.", label);
                throw new FedoraException("The label \"" + label + "\" is in use by another version.");
            } else if ( status.getStatusCode() == SC_FORBIDDEN) {
                LOGGER.error("updating resource {} is not authorized.", uri);
                throw new ForbiddenException("updating resource " + uri + " is not authorized.");
            } else if ( status.getStatusCode() == SC_NOT_FOUND) {
                LOGGER.error("resource {} does not exist, cannot create version", uri);
                throw new NotFoundException("resource " + uri + " does not exist, cannot create version");
            } else {
                LOGGER.error("error updating resource {}: {} {}", uri, status.getStatusCode(),
                        status.getReasonPhrase());
                throw new FedoraException("error updating resource " + uri + ": " + status.getStatusCode() + " " +
                        status.getReasonPhrase());
            }
        } catch (IOException e) {
            LOGGER.error("Error executing request", e);
            throw new FedoraException(e);
        } finally {
            postVersion.releaseConnection();
        }
    }

    /**
     * Get the properties graph
     *
     * @return Graph containing properties for this resource
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Update the properties graph
    **/
    public void setGraph( final Graph graph ) {
        this.graph = graph;
    }

    private Date getDate(final Property property) {
        Date date = null;
        final Triple t = getTriple(subject, property);
        if ( t != null ) {
            final String dateValue = t.getObject().getLiteralValue().toString();
            try {
                date = dateFormat.parse(dateValue);
            } catch (final ParseException e) {
                LOGGER.debug("Invalid date format error: " + dateValue);
            }
        }
        return date;
    }

    /**
     * Return all the values of a property
     *
     * @param property The Property to get values for
     * @return Collection of values
     */
    protected Collection<String> getPropertyValues(final Property property) {
        final ExtendedIterator<Triple> iterator = graph.find(Node.ANY,
                                                             property.asNode(),
                                                             Node.ANY);
        final Set<String> set = new HashSet<>();
        while (iterator.hasNext()) {
            final Node object = iterator.next().getObject();
            if (object.isLiteral()) {
                set.add(object.getLiteralValue().toString());
            } else if (object.isURI()) {
                set.add(object.getURI().toString());
            } else {
                set.add(object.toString());
            }
        }
        return set;
    }

    protected Triple getTriple( final Node subject, final Property property ) {
        final ExtendedIterator<Triple> it = graph.find( subject, property.asNode(), null );
        try {
            if ( it.hasNext() ) {
                return it.next();
            } else {
                return null;
            }
        } finally {
            it.close();
        }
    }

    /**
     * Gets the path to which properties of this resource may be accessed.
     */
    public String getPropertiesPath() {
        return path;
    }

}
