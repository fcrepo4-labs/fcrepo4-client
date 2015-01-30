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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;

import org.fcrepo.client.NotFoundException;

import org.apache.http.client.HttpClient;

import org.fcrepo.client.FedoraContent;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.ReadOnlyException;
import org.fcrepo.client.utils.HttpHelper;
import org.slf4j.Logger;

/**
 * Read-only FedoraRepository implementation that throws a ReadOnlyException any time a write operation is attempted.
 *
 * @author escowles
 * @since 2014-08-25
 */
public class ReadOnlyFedoraRepositoryImpl extends FedoraRepositoryImpl implements FedoraRepository {
    private static final Logger LOGGER = getLogger(ReadOnlyFedoraRepositoryImpl.class);
    private static final String msg = "Write operation attempted using read-only repository";

    /**
     * Constructor that takes the repository url
     *
     * @param repositoryURL Fedora base URL.
     */
    public ReadOnlyFedoraRepositoryImpl(final String repositoryURL) {
        this.repositoryURL = repositoryURL;
        this.httpHelper = new HttpHelper(repositoryURL, null, null, true);
    }

    /**
     * Constructor that takes the repoistory url and username/password for connecting
     *
     * @param repositoryURL Repository base URL
     * @param username Repository username
     * @param password Repository password
     */
    public ReadOnlyFedoraRepositoryImpl(final String repositoryURL, final String username, final String password) {
        this.repositoryURL = repositoryURL;
        this.httpHelper = new HttpHelper(repositoryURL, username, password, true);
    }

    /**
     * Constructor that takes the pre-configured HttpClient
     *
     * @param repositoryURL Repository baseURL
     * @param httpClient Pre-configured httpClient
     */
    public ReadOnlyFedoraRepositoryImpl(final String repositoryURL, final HttpClient httpClient) {
        this.repositoryURL = repositoryURL;
        this.httpHelper = new HttpHelper(repositoryURL, httpClient, true);
    }

    @Override
    public FedoraDatastream createDatastream(final String path, final FedoraContent content) throws ReadOnlyException {
        LOGGER.warn(msg);
        throw new ReadOnlyException();
    }

    @Override
    public FedoraObject createObject(final String path) throws FedoraException {
        LOGGER.warn(msg);
        throw new ReadOnlyException();
    }

    @Override
    public FedoraDatastream findOrCreateDatastream(final String path) throws FedoraException {
        try {
            return getDatastream(path);
        } catch ( NotFoundException ex ) {
            LOGGER.warn(msg);
            throw new ReadOnlyException();
        }
    }

    @Override
    public FedoraObject findOrCreateObject(final String path) throws FedoraException {
        try {
            return getObject(path);
        } catch ( NotFoundException ex ) {
            LOGGER.warn(msg);
            throw new ReadOnlyException();
        }
    }

    @Override
    public void registerNodeTypes(final InputStream cndStream) throws ReadOnlyException {
        LOGGER.warn(msg);
        throw new ReadOnlyException();
    }

    @Override
    public void addNamespace(final String prefix, final String uri) throws ReadOnlyException {
        LOGGER.warn(msg);
        throw new ReadOnlyException();
    }

    @Override
    public void removeNamespace(final String prefix) throws ReadOnlyException {
        LOGGER.warn(msg);
        throw new ReadOnlyException();
    }

    @Override
    public boolean isWritable() {
        return false;
    }
}
