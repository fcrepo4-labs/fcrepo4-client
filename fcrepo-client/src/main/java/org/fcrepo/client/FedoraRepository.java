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

package org.fcrepo.client;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.graph.Triple;

/**
 * The primary client class for interacting with a Fedora repository.
 * @author escowles
 * @since 2014-08-01
**/
public interface FedoraRepository {

    /**
     * Check whether a path is an existing resource.
     * @param path The resource path.
    **/
    public boolean exists( String path );

    /**
     * Get an existing Datastream.
     * @param path The Datastream path.
    **/
    public FedoraDatastream getDatastream( String path );

    /**
     * Get an existing Object.
     * @param path The Object path.
    **/
    public FedoraObject getObject( String path );

    /**
     * Create a new Datastream.
     * @param path The path of the new datastream.
     * @param content Content of the new datastream.
    **/
    public FedoraDatastream createDatastream( String path, FedoraContent content ) throws ReadOnlyException;

    /**
     * Create a new Object.
     * @param path The Object path.
    **/
    public FedoraObject createObject( String path ) throws ReadOnlyException;

    /**
     * Get an existing Datastream if it exists, otherwise create a new Datastream.
     * @param path The Datastream path.
    **/
    public FedoraDatastream findOrCreateDatastream( String path ) throws ReadOnlyException;

    /**
     * Get an existing Object if it exists, otherwise create a new Object.
     * @param path The Object path.
    **/
    public FedoraObject findOrCreateObject( String path ) throws ReadOnlyException;

    /**
     * Get an RDF description of the node types configured for this repository.
    **/
    public Iterator<Triple> getNodeTypes();
// see https://svn.apache.org/repos/asf/jena/trunk/jena-arq/src-examples/arq/examples/riot/ExRIOT_6.java

    /**
     * Update the node types for this repository.
     * @param cndStream The new node type definition as a CND content stream.
    **/
    public void registerNodeTypes( InputStream cndStream ) throws ReadOnlyException;

    /**
     * Get a map of namespace prefixes to URIs.
    **/
    public Map<String,String> getRepositoryNamespaces();

    /**
     * Register a namespace.
     * @param prefix The namespace prefix.
     * @param uri The namespace URI.
    **/
    public void addNamespace( String prefix, String uri ) throws ReadOnlyException;

    /**
     * Remove a namespace.
     * @param prefix The namespace prefix.
    **/
    public void removeNamespace( String prefix ) throws ReadOnlyException;

    /**
     * Get the number of objects this repository contains.
    **/
    public Long getRepositoryObjectCount();

    /**
     * Get the size of the repository content in bytes.
    **/
    public Long getRepositorySize();

    /**
     * Check whether this repository is writable.
    **/
    public boolean isWritable();
}
