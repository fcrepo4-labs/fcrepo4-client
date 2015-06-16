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
     * Get the base URL for the repository.
    **/
    public String getRepositoryUrl();

    /**
     * Check whether a path is an existing resource.
     * @param path The resource path.
    **/
    public boolean exists( String path ) throws FedoraException;

    /**
     * Get an existing Datastream.
     * @param path The Datastream path.
    **/
    public FedoraDatastream getDatastream( String path ) throws FedoraException;

    /**
     * Get an existing Object.
     * @param path The Object path.
    **/
    public FedoraObject getObject( String path ) throws FedoraException;

    /**
     * Create a new Datastream.
     * @param path The path of the new datastream.
     * @param content Content of the new datastream.
    **/
    public FedoraDatastream createDatastream( String path, FedoraContent content ) throws FedoraException;

    /**
     * Create or replace a new external datastream.
     * @param path The path of the datastream.
     * @param url the URL to which accessors of the datastream will be redirected.
     **/
    public FedoraDatastream createOrUpdateRedirectDatastream( String path, String url ) throws FedoraException;

    /**
     * Create a new Object.
     * @param path The Object path.
    **/
    public FedoraObject createObject( String path ) throws FedoraException;

    /**
     * Create a new Object with a repository-supplied path that is within the container
     * at the provided containerPath.
     * @param containerPath the path to a container in which this resource will be created.  An
     *                      empty String or null will create a new resource at the root level.
     * @return a FedoraObject representing the created resource.
     * @throws FedoraException if an error occurs while making the requests against the repository.
     **/
    public FedoraObject createResource(String containerPath) throws FedoraException;

    /**
     * Get an existing Datastream if it exists, otherwise create a new Datastream.
     * @param path The Datastream path.
    **/
    public FedoraDatastream findOrCreateDatastream( String path ) throws FedoraException;

    /**
     * Get an existing Object if it exists, otherwise create a new Object.
     * @param path The Object path.
    **/
    public FedoraObject findOrCreateObject( String path ) throws FedoraException;

    /**
     * Get an RDF description of the node types configured for this repository.
    **/
    public Iterator<Triple> getNodeTypes() throws FedoraException;
// see https://svn.apache.org/repos/asf/jena/trunk/jena-arq/src-examples/arq/examples/riot/ExRIOT_6.java

    /**
     * Update the node types for this repository.
     * @param cndStream The new node type definition as a CND content stream.
    **/
    public void registerNodeTypes( InputStream cndStream ) throws FedoraException;

    /**
     * Get a map of namespace prefixes to URIs.
    **/
    public Map<String,String> getRepositoryNamespaces() throws FedoraException;

    /**
     * Register a namespace.
     * @param prefix The namespace prefix.
     * @param uri The namespace URI.
    **/
    public void addNamespace( String prefix, String uri ) throws FedoraException;

    /**
     * Remove a namespace.
     * @param prefix The namespace prefix.
    **/
    public void removeNamespace( String prefix ) throws FedoraException;

    /**
     * Get the number of objects this repository contains.
    **/
    public Long getRepositoryObjectCount() throws FedoraException;

    /**
     * Get the size of the repository content in bytes.
    **/
    public Long getRepositorySize() throws FedoraException;

    /**
     * Check whether this repository is writable.
    **/
    public boolean isWritable();
}
