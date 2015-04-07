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

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import junit.framework.Assert;
import org.fcrepo.client.FedoraContent;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.kernel.RdfLexicon;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Mike Durbin
 */
public class FedoraRepositoryImplIT {

    final static Logger LOGGER = getLogger(FedoraRepositoryImplTest.class);

    public static String FEDORA_CONTEXT = "fcrepo-webapp";

    private static String CARGO_PORT = System.getProperty("fcrepo.dynamic.test.port", "8080");

    private static String getFedoraBaseUrl() {
        return "http://localhost:" + CARGO_PORT + "/" + FEDORA_CONTEXT;
    }

    private FedoraRepository repo;

    @Before
    public void setUp() throws FedoraException {
        repo = new FedoraRepositoryImpl("http://localhost:" + CARGO_PORT + "/" + FEDORA_CONTEXT + "/rest/");
    }

    @Test
    public void testBasicResourceCreation() throws IOException, FedoraException {
        final String path = getRandomUniqueId();

        repo.createObject(path);

        final FedoraObject object = repo.getObject(path);

        final String content = "Test String";
        final String dsid = getRandomUniqueId();
        final String dsPath = path + "/" + dsid;

        repo.createDatastream(dsPath, getStringTextContent(content));

        final FedoraDatastream datastream = repo.getDatastream(dsPath);
    }

    @Test
    public void testBasicPropertiesCreation() throws IOException, FedoraException {
        final String objectPath = getRandomUniqueId();
        final FedoraObject object = repo.createObject(objectPath);
        final String sparqlUpdate = "INSERT DATA { <> <" + RdfLexicon.DC_NAMESPACE + "identifier> 'test' . } ";
        object.updateProperties(sparqlUpdate);
        final Iterator<Triple> tripleIt = object.getProperties();
        while (tripleIt.hasNext()) {
            final Triple t = tripleIt.next();
            if (t.objectMatches(NodeFactory.createLiteral("test"))
                    && t.predicateMatches(NodeFactory.createURI(RdfLexicon.DC_NAMESPACE + "identifier"))) {
                return;
            }
        }
        Assert.fail("Unable to verify added object property!");
    }

    @Test
    public void testBasicDatastreamPropertiesCreation() throws IOException, FedoraException {
        final String objectPath = getRandomUniqueId();
        final FedoraObject object = repo.createObject(objectPath);
        final String datastreamPath = objectPath + "/" + getRandomUniqueId();
        final FedoraDatastream datastream = repo.createDatastream(datastreamPath, getStringTextContent("test"));
        final String sparqlUpdate = "INSERT DATA { <> <" + RdfLexicon.DC_NAMESPACE + "identifier> 'test' . } ";
        datastream.updateProperties(sparqlUpdate);
        final Iterator<Triple> tripleIt = datastream.getProperties();
        while (tripleIt.hasNext()) {
            final Triple t = tripleIt.next();
            if (t.objectMatches(NodeFactory.createLiteral("test"))
                    && t.predicateMatches(NodeFactory.createURI(RdfLexicon.DC_NAMESPACE + "identifier"))) {
                return;
            }
        }
        Assert.fail("Unable to verify added datastream property!");
    }

    private FedoraContent getStringTextContent(final String value) throws UnsupportedEncodingException {
        return new FedoraContent().setContent(new ByteArrayInputStream(value.getBytes("UTF-8")))
                .setContentType("text/plain");
    }

    private String getRandomUniqueId() {
        return UUID.randomUUID().toString();
    }
}
