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

import java.util.Collection;

/**
 * A Fedora Object, a Resource which can contain Datastreams and/or other Objects.
 * @author escowles
 * @since 2014-08-01
**/
public interface FedoraObject extends FedoraResource {

    /**
     * Get the Object and Datastream nodes that are children of the current Object.
     * @param mixin If not null, limit to results that have this mixin.
    **/
    public Collection<FedoraResource> getChildren( String mixin ) throws FedoraException;

    /**
     * Create a new resource with a repository-supplied path contained within
     * the resource exposed by this FedoraObject instance.
     * @return a FedoraObject representing the created resource.
     * @throws FedoraException if an error occurs while making the requests against the repository.
     **/
    public FedoraObject createObject() throws FedoraException;
}
