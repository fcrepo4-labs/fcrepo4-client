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
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Triple;

/**
 * Superclass of Fedora Objects and Datastreams containing common functionality.
 * @author escowles
 * @since 2014-08-01
**/
public interface FedoraResource {

    /**
     * Copy this Resource to a new path.
     * @param destination The path of the new copy.
    **/
    public void copy( String destination ) throws ReadOnlyException;

    /**
     * Remove this Resource.
    **/
    public void delete() throws ReadOnlyException;

    /**
     * Get the creation date of this Resource.
    **/
    public Date getCreatedDate();

    /**
     * Get the ETag of this Resource.
    **/
    public String getEtagValue();

    /**
     * Get the modification date of this Resource.
    **/
    public Date getLastModifiedDate();

    /**
     * Get the mixins assigned to this Resource.
    **/
    public Collection<String> getMixins();

    /**
     * Get the name of this Resource.
    **/
    public String getName();

    /**
     * Get the full path of the Resource, relative to the repository root.
    **/
    public String getPath();

    /**
     * Get the RDF properties of this Resource.
    **/
    public Iterator<Triple> getProperties();

    /**
     * Get the size of this Resource in bytes.
    **/
    public Long getSize();

    /**
     * Move this Resource to a new path.
     * @param destination The path of the new copy.
    **/
    public void move( String destination ) throws ReadOnlyException;

    /**
     * Update the properties of this Resource using SPARQL Update.
     * @param sparqlUpdate SPARQL Update command.
    **/
    public void updateProperties( String sparqlUpdate ) throws ReadOnlyException;

    /**
     * Update the properties of this Resource with the provided RDF.
     * @param updatedProperties RDF properties as an InputStream.
    **/
    public void updateProperties( InputStream updatedProperties ) throws ReadOnlyException;

    /**
     * Check whether this Resource is writable.
    **/
    public boolean isWritable();
}
