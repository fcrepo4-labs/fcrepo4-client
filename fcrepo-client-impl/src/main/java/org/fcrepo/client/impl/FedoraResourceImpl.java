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

import org.apache.jena.atlas.lib.NotImplemented;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.FedoraResource;
import org.fcrepo.client.ReadOnlyException;
import org.fcrepo.client.utils.RDFSinkFilter;
import org.fcrepo.kernel.RdfLexicon;
import org.slf4j.Logger;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * A Fedora Object Impl.
 *
 * @author lsitu
 * @since 2014-08-11
 */
public class FedoraResourceImpl implements FedoraResource {
    private static final Logger LOGGER = getLogger(FedoraRepositoryImpl.class);

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    protected FedoraRepository repository = null;

    protected String path = null;

    protected Graph graph;

    private String etagValue = null;

    /**
     * FedoraResourceImpl constructor
     *
     * @param repository FedoraRepository that created this resource
     * @param path Repository path of this resource
     * @param triples Properties of this resource
     * @throws IOException
     * @throws IllegalStateException
     */
    public FedoraResourceImpl(final FedoraRepository repository,
                              final String path,
                              final Iterator<Triple> triples)
            throws IllegalStateException, IOException {
        this.repository = repository;
        this.path = path;
        graph = RDFSinkFilter.filterTriples(triples, Node.ANY);
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
    public void updateProperties(final String sparqlUpdate) throws ReadOnlyException {
        // TODO Auto-generated method stub
        throw new NotImplemented("Method updateProperties(final String sparqlUpdate) is not implemented.");
    }

    @Override
    public void updateProperties(final InputStream updatedProperties) throws ReadOnlyException {
        // TODO Auto-generated method stub
        throw new NotImplemented("Method updateProperties(final InputStream updatedProperties) is not implemented.");
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

    /**
     * Get the properties graph
     *
     * @return Graph containing properties for this resource
     */
    public Graph getGraph() {
        return graph;
    }

    private Date getDate(final Property property) {
        final ExtendedIterator<Triple> iterator = graph.find(Node.ANY,
                                                             property.asNode(),
                                                             Node.ANY);
        Date date = null;
        if (iterator.hasNext()) {
            final String dateValue = iterator.next()
                    .getObject().getLiteralValue().toString();
            try {
                date = dateFormat.parse(dateValue);
            } catch (final ParseException e) {
                LOGGER.debug("Invalid date format errr: " + dateValue);
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
        if (iterator.hasNext()) {
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
}
