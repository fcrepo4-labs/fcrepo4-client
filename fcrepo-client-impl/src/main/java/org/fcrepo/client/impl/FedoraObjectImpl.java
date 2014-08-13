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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.jena.atlas.lib.NotImplemented;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.FedoraResource;
import com.hp.hpl.jena.graph.Triple;

/**
 * A Fedora Object Impl.
 *
 * @author lsitu
 * @since 2014-08-11
 */
public class FedoraObjectImpl extends FedoraResourceImpl implements FedoraObject {

    /**
     * Constructor for FedoraObjectImpl
     *
     * @param repository
     * @param path
     * @param triples
     * @throws IllegalStateException
     * @throws IOException
     */
    public FedoraObjectImpl(final FedoraRepository repository,
                            final String path,
                            final Iterator<Triple> triples) throws IllegalStateException, IOException {
        super(repository, path, triples);
    }

    /**
     * Get the Object and Datastream nodes that are children of the current Object.
     *
     * @param mixin If not null, limit to results that have this mixin.
     */
    public Collection<FedoraResource> getChildren(final String mixin) {
        throw new NotImplemented("Method getChildren( final String mixin ) is not implemented for FedoraObject.");
    }
}
