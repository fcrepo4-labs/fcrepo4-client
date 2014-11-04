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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.fcrepo.kernel.RdfLexicon.CREATED_DATE;
import static org.fcrepo.kernel.RdfLexicon.LAST_MODIFIED_DATE;
import static org.fcrepo.kernel.RdfLexicon.HAS_MIXIN_TYPE;
import static org.fcrepo.kernel.RdfLexicon.WRITABLE;
import static org.fcrepo.kernel.RdfLexicon.DESCRIBES;
import static org.fcrepo.kernel.RdfLexicon.HAS_SIZE;
import static org.fcrepo.kernel.RdfLexicon.HAS_MIME_TYPE;
import static org.fcrepo.kernel.RdfLexicon.HAS_ORIGINAL_NAME;
import static org.fcrepo.client.impl.FedoraDatastreamImpl.REST_API_DIGEST;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import org.apache.commons.io.IOUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;

import org.fcrepo.client.FedoraContent;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.utils.HttpHelper;
import org.fcrepo.kernel.RdfLexicon;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Datastream Impl test.
 * @author escowles
 * @since 2014-08-25
 */
public class FedoraDatastreamImplTest {

    @Mock
    FedoraRepositoryImpl mockRepository;

    @Mock
    HttpHelper mockHelper;

    @Mock
    private FedoraObject mockObject;

    @Mock
    private Iterator<Triple> mockTriples;

    private FedoraDatastreamImpl datastream;

    private String path = "/test/image/fcr:metadata";

    private boolean isWritable = true;

    private static SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private String testDateValue = "2014-08-14T15:11:30.118Z";
    private String testMixinType = RdfLexicon.REPOSITORY_NAMESPACE + "test";
    private String checksum = "urn:sha1:187ff331acaea139c8dc1eb77da8be32bd81ac7d";
    private String filename = "test.jpg";
    private String mimeType = "image/jpeg";
    private String contentSize = "545376";
    private final String repositoryURL = "http://localhost:8080/rest";
    private Node dsSubj = createURI(repositoryURL + path);
    private Node contentSubj = createURI(repositoryURL + path.substring(0, path.lastIndexOf("/")));


    @Before
    public void setUp() throws IOException, FedoraException {
        initMocks(this);
        mockRepository.httpHelper = mockHelper;

        when(mockRepository.getRepositoryUrl()).thenReturn(repositoryURL);
        when(mockRepository.getObject(eq("/test"))).thenReturn(mockObject);
        datastream = new FedoraDatastreamImpl(mockRepository, mockHelper, path);
        assertTrue(datastream != null);

        final Graph graph = createDefaultGraph();
        graph.add( create(dsSubj, CREATED_DATE.asNode(), ResourceFactory.createPlainLiteral(testDateValue).asNode()) );
        graph.add( create(dsSubj, LAST_MODIFIED_DATE.asNode(),
            ResourceFactory.createPlainLiteral(testDateValue).asNode()) );
        graph.add( create(dsSubj, HAS_MIXIN_TYPE.asNode(), createURI(testMixinType)) );
        graph.add( create(dsSubj, WRITABLE.asNode(),
            ResourceFactory.createTypedLiteral(new Boolean(isWritable)).asNode()) );
        graph.add( create(dsSubj, DESCRIBES.asNode(), contentSubj) );
        graph.add( create(contentSubj, HAS_SIZE.asNode(), ResourceFactory.createPlainLiteral(contentSize).asNode()) );
        graph.add( create(contentSubj, HAS_MIME_TYPE.asNode(), ResourceFactory.createPlainLiteral(mimeType).asNode()) );
        graph.add( create(contentSubj, HAS_ORIGINAL_NAME.asNode(),
            ResourceFactory.createPlainLiteral(filename).asNode()) );
        graph.add( create(contentSubj, REST_API_DIGEST.asNode(), createURI(checksum)) );
        datastream.setGraph( graph );
    }

    // functionality inherited from FedoraResourceImpl

    @Test
    public void testGetCreatedDate() throws FedoraException {
        assertEquals("Created date is not the same",
                testDateValue, dateFormat.format(datastream.getCreatedDate()));
    }

    @Test
    public void testGetAndSetEtagValue() throws FedoraException {
        ((FedoraDatastreamImpl)datastream).setEtagValue("2a0e84efa8a39de57ebbc5ed3bc7e454a1a768de");
        assertEquals("ETag is not the same",
                "2a0e84efa8a39de57ebbc5ed3bc7e454a1a768de", datastream.getEtagValue());
     }

    @Test
    public void testGetLastModifiedDate() throws FedoraException {
        assertEquals("LastModifiedDate is not the same",
                testDateValue, dateFormat.format(datastream.getLastModifiedDate()));
    }

    @Test
    public void testGetMixins() throws FedoraException {
        assertTrue(datastream.getMixins().contains(testMixinType));
    }

    @Test
    public void testGetName() throws IOException, FedoraException {
        assertEquals("Name is not the same", "image", datastream.getName());
    }

    @Test
    public void testGetPath() throws FedoraException {
        assertEquals("Path is not the same", path, datastream.getPath());
    }

    @Test
    public void testGetProperties() throws FedoraException {
        assertEquals("Can't retrieve Properties",
                datastream.getProperties().hasNext(), true);
    }

    @Test
    public void testIsWritable() {
        assertEquals("IsWriatable value is not the same",
                isWritable, datastream.isWritable());
    }

    @Test
    public void TestGetGraph() {
        assertNotNull(((FedoraDatastreamImpl)datastream).getGraph());
    }

    // datastream-specific functionality

    @Test
    public void testHasContent() throws FedoraException {
        assertTrue("Should have content", datastream.hasContent());
    }

    @Test
    public void testGetContentDigest() throws FedoraException {
        assertEquals("Checksum is not the same", checksum, datastream.getContentDigest().toString());
    }

    @Test
    public void testGetContentSize() throws FedoraException {
        assertEquals("Content size is not the same", contentSize, String.valueOf(datastream.getContentSize()));
    }

    @Test
    public void testGetFilename() throws FedoraException {
        assertEquals("Content filename is not the same", filename, datastream.getFilename());
    }

    @Test
    public void testGetContentType() throws FedoraException {
        assertEquals("Content mime type is not the same", mimeType, datastream.getContentType());
    }

    @Test
    public void testGetObject() throws FedoraException {
        final FedoraObject obj = datastream.getObject();
        assertEquals("Parent object doesn't match", mockObject, obj);
    }

    @Test
    public void testGetContent() throws IOException, URISyntaxException, FedoraException {
        final URI getURI = new URI(repositoryURL + path);
        final HttpGet mockGet = mock(HttpGet.class);
        final HttpResponse mockResponse = mock(HttpResponse.class);
        final StatusLine mockStatus = mock(StatusLine.class);
        final HttpEntity mockEntity = mock(HttpEntity.class);
        final String mockContent = "test datastream content";

        when(mockHelper.createGetMethod(anyString(), any(Map.class))).thenReturn(mockGet);
        when(mockGet.getURI()).thenReturn(getURI);
        when(mockHelper.execute(any(HttpGet.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusLine()).thenReturn(mockStatus);
        when(mockStatus.getStatusCode()).thenReturn(200);
        when(mockResponse.getEntity()).thenReturn(mockEntity);
        when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream(mockContent.getBytes()));

        final InputStream contentStream = datastream.getContent();
        final String content = IOUtils.toString(contentStream);
        assertEquals("Content doesn't match", mockContent, content);
    }

    @Test
    public void testUpdateContent() throws IOException, URISyntaxException, FedoraException {
        final String newFilename = "test.png";
        final String newMimeType = "image/png";
        final String mockContent = "test datastream content";
        final FedoraContent content = new FedoraContent()
            .setContent(new ByteArrayInputStream(mockContent.getBytes()))
            .setFilename(newFilename).setContentType(newMimeType);

        final URI putURI = new URI(repositoryURL + path);
        final HttpPut mockPut = mock(HttpPut.class);
        final HttpResponse mockResponse = mock(HttpResponse.class);
        final StatusLine mockStatus = mock(StatusLine.class);
        final HttpEntity mockEntity = mock(HttpEntity.class);

        when(mockHelper.createContentPutMethod(anyString(), any(Map.class), eq(content))).thenReturn(mockPut);
        when(mockPut.getURI()).thenReturn(putURI);
        when(mockHelper.execute(any(HttpPut.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusLine()).thenReturn(mockStatus);
        when(mockStatus.getStatusCode()).thenReturn(204);
        when(mockResponse.getEntity()).thenReturn(mockEntity);
        when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream(mockContent.getBytes()));
        when(mockHelper.loadProperties(eq(datastream))).thenReturn(datastream);

        datastream.updateContent( content );

        verify(mockHelper).execute(any(HttpPut.class));
        verify(mockHelper).loadProperties(datastream);
    }
}
