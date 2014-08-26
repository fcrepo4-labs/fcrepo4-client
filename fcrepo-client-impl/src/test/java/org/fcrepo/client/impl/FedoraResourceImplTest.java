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

import static com.hp.hpl.jena.graph.Factory.createDefaultGraph;
import static com.hp.hpl.jena.graph.NodeFactory.createURI;
import static com.hp.hpl.jena.graph.Triple.create;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.utils.HttpHelper;
import org.fcrepo.kernel.RdfLexicon;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author lsitu
 *
 */
public class FedoraResourceImplTest {

    @Mock
    FedoraRepository mockRepository;

    @Mock
    HttpHelper mockHelper;

    private FedoraResourceImpl resource;

    private String path = "/test";

    private boolean isWritable = true;

    private static SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private String testDateValue = "2014-08-14T15:11:30.118Z";
    private String testMixinType = RdfLexicon.REPOSITORY_NAMESPACE + "test";
    private final String repositoryURL = "http://localhost:8080/rest";

    @Before
    public void setUp() throws IOException {
        initMocks(this);
        when(mockRepository.getRepositoryUrl()).thenReturn(repositoryURL);
        resource = new FedoraResourceImpl(mockRepository, mockHelper, path);
        assertTrue(resource != null);

        final Graph graph = createDefaultGraph();
        graph.add( create(createURI(repositoryURL + "/test"), RdfLexicon.CREATED_DATE.asNode(),
                          ResourceFactory.createPlainLiteral(testDateValue).asNode()) );
        graph.add( create(createURI(repositoryURL + "/test"), RdfLexicon.LAST_MODIFIED_DATE.asNode(),
                          ResourceFactory.createPlainLiteral(testDateValue).asNode()) );
        graph.add( create(createURI(repositoryURL + "/test"), RdfLexicon.HAS_MIXIN_TYPE.asNode(),
                          createURI(testMixinType)) );
        graph.add( create(createURI(repositoryURL + "/test"), RdfLexicon.WRITABLE.asNode(),
                          ResourceFactory.createTypedLiteral(new Boolean(isWritable)).asNode()) );
        resource.setGraph( graph );
    }

    @Test
    public void testGetCreatedDate() throws FedoraException {
        assertEquals("Created date is not the same",
                testDateValue, dateFormat.format(resource.getCreatedDate()));
    }

    @Test
    public void testGetAndSetEtagValue() throws FedoraException {
        ((FedoraResourceImpl)resource).setEtagValue("2a0e84efa8a39de57ebbc5ed3bc7e454a1a768de");
        assertEquals ("ETag is not the same",
                "2a0e84efa8a39de57ebbc5ed3bc7e454a1a768de", resource.getEtagValue());
     }

    @Test
    public void testGetLastModifiedDate() throws FedoraException {
        assertEquals("LastModifiedDate is not the same",
                testDateValue, dateFormat.format(resource.getLastModifiedDate()));
    }

    @Test
    public void testGetMixins() throws FedoraException {
        assertTrue (resource.getMixins().contains(testMixinType));
    }

    @Test
    public void testGetName() throws IOException, FedoraException {
        assertEquals ("Name is not the same", "test", resource.getName());
    }

    @Test
    public void testGetPath() throws FedoraException {
        assertEquals ("Path is not the same", path, resource.getPath());
    }

    @Test
    public void testGetProperties() throws FedoraException {
        assertEquals ("Can't retrieve Properties",
                resource.getProperties().hasNext(), true);
    }

    @Test
    public void testGetSize() throws FedoraException {
        assertTrue (resource.getSize() == 4);
    }

    @Test
    public void testIsWritable() {
        assertEquals ("IsWriatable value is not the same",
                isWritable, resource.isWritable());
    }

    @Test
    public void TestGetGraph () {
        assertNotNull(((FedoraResourceImpl)resource).getGraph());
    }
}
