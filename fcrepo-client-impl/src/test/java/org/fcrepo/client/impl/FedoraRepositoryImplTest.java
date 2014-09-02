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

import static com.hp.hpl.jena.graph.NodeFactory.createLiteral;
import static com.hp.hpl.jena.graph.NodeFactory.createURI;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.fcrepo.kernel.RdfLexicon.HAS_PRIMARY_IDENTIFIER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.fcrepo.client.NotFoundException;

import com.hp.hpl.jena.graph.Graph;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.HttpPut;
import org.fcrepo.client.FedoraContent;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.utils.HttpHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Writable repository impl -- read and write operations should both work.
 *
 * @author lsitu
 * @author escowles
 *
 */
public class FedoraRepositoryImplTest {

    FedoraRepositoryImpl fedoraRepository;
    HttpHelper httpHelper;

    @Mock
    HttpClient mockClient;

    @Mock
    private HttpResponse mockResponse;

    @Mock
    private HttpEntity mockEntity;

    @Mock
    private StatusLine mockStatusLine;

    @Mock
    private FedoraObjectImpl mockObject;

    String testRepositoryUrl = "http://localhost:8080/rest";

    private final String testContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" +
                " xmlns:fcrepo=\"http://fedora.info/definitions/v4/repository#\">" +
                "<rdf:Description rdf:about=\"http://localhost:8080/rest/testObject\">" +
                "<rdf:type rdf:resource=\"http://fedora.info/definitions/v4/rest-api#resource\"/>" +
                "<rdf:type rdf:resource=\"http://fedora.info/definitions/v4/rest-api#object\"/>" +
                "<fcrepo:uuid>2fb9c440-db63-434f-929b-0ff29253205c</fcrepo:uuid>" +
                "</rdf:Description>" +
                "</rdf:RDF>";


    @Before
    public void setUp() throws IOException {
        initMocks(this);
        fedoraRepository = new FedoraRepositoryImpl (testRepositoryUrl, mockClient);
        httpHelper = new HttpHelper( testRepositoryUrl, mockClient, false );
    }

    @Test
    public void testGetObject() throws IOException, FedoraException {
        final String testId = "testGetObject";
        when(mockClient.execute(any(HttpUriRequest.class))).thenReturn(mockResponse);
        when(mockResponse.getEntity()).thenReturn(mockEntity);
        final Header mockContentType = mock(Header.class);
        when(mockEntity.getContentType()).thenReturn(mockContentType);
        when(mockContentType.getValue()).thenReturn("application/rdf+xml");
        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(SC_OK);
        try (
            InputStream rdf =
                new ByteArrayInputStream(testContent.getBytes())) {
            when(mockEntity.getContent()).thenReturn(rdf);
        }
        final FedoraObject testObject = fedoraRepository.getObject(testId);
        assertNotNull(testObject);
        assertTrue(testObject.getProperties().hasNext());
    }

    @Test
    public void testGetRepositoryUrl() {
        assertEquals ("Resitory URL is not the same", testRepositoryUrl, fedoraRepository.getRepositoryUrl());
    }

    @Test
    public void testExists() throws IOException, FedoraException {
        final String testId = "testGetObject";
        when(mockClient.execute(any(HttpUriRequest.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(SC_OK);
        assertTrue(fedoraRepository.exists(testId));
    }

    @Test
    public void testExistsNonExistent() throws IOException, FedoraException {
        final String testId = "testNonExistent";
        when(mockClient.execute(any(HttpUriRequest.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(SC_NOT_FOUND);
        assertFalse(fedoraRepository.exists(testId));
    }

    @Test
    public void testCreateObject() throws IOException, FedoraException {
        final String testId = "testNewObject";
        final HttpResponse mockResponse2 = mock(HttpResponse.class);
        final StatusLine mockStatusLine2 = mock(StatusLine.class);

        when(mockClient.execute(any(HttpUriRequest.class))).thenReturn(mockResponse2,mockResponse);

        // put
        when(mockResponse2.getStatusLine()).thenReturn(mockStatusLine2);
        when(mockStatusLine2.getStatusCode()).thenReturn(SC_CREATED);
        when(mockStatusLine2.getReasonPhrase()).thenReturn("Created");

        // get
        when(mockResponse.getEntity()).thenReturn(mockEntity);
        final Header mockContentType = mock(Header.class);
        when(mockEntity.getContentType()).thenReturn(mockContentType);
        when(mockContentType.getValue()).thenReturn("application/rdf+xml");
        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(SC_OK);
        when(mockStatusLine.getReasonPhrase()).thenReturn("OK");
        try (
            InputStream rdf =
                new ByteArrayInputStream(testContent.getBytes())) {
            when(mockEntity.getContent()).thenReturn(rdf);
        }

        final FedoraObject testObject = fedoraRepository.createObject(testId);
        assertNotNull(testObject);
        assertTrue(testObject.getProperties().hasNext());
    }

    @Test
    public void testFindOrCreateObject() throws FedoraException {
        final FedoraRepositoryImpl spy = spy( new FedoraRepositoryImpl(testRepositoryUrl, mockClient) );
        final FedoraObject mockObject = mock(FedoraObject.class);
        doReturn(mockObject).when(spy).getObject(anyString());

        final FedoraObject object = spy.findOrCreateObject("/foo");
        assertEquals(mockObject, object);
    }

    @Test
    public void testFindOrCreateObjectNonExistent() throws FedoraException {
        final FedoraRepositoryImpl spy = spy( new FedoraRepositoryImpl(testRepositoryUrl, mockClient) );
        final NotFoundException mockException = mock(NotFoundException.class);
        doThrow(mockException).when(spy).getObject(anyString());
        final FedoraObject mockObject = mock(FedoraObject.class);
        doReturn(mockObject).when(spy).createObject(anyString());

        final FedoraObject object = spy.findOrCreateObject("/foo");
        assertEquals(mockObject, object);
    }

    @Test
    public void testWritable() {
        assertTrue( fedoraRepository.isWritable() );
    }

    @Test
    public void testUpdateProperties() throws Exception {
        final String path = "/testObject";
        final String etag = "urn:sha1:e242ed3bffccdf271b7fbaf34ed72d089537b42f";
        final URI uri = new URI( testRepositoryUrl + path );
        final HttpResponse response = mock(HttpResponse.class);
        final StatusLine status = mock(StatusLine.class);
        final FedoraObjectImpl object = new FedoraObjectImpl( fedoraRepository, httpHelper, path );
        final Header etagHeader = mock(Header.class);
        final Header[] etagArray = new Header[]{ etagHeader };
        final Header typeHeader = mock(Header.class);
        final HttpEntity entity = mock(HttpEntity.class);

        when(mockClient.execute(any(HttpUriRequest.class))).thenReturn(response);
        when(response.getStatusLine()).thenReturn(status);
        when(status.getStatusCode()).thenReturn(200);
        when(response.getHeaders(eq("ETag"))).thenReturn(etagArray);
        when(response.getEntity()).thenReturn(entity);
        when(entity.getContentType()).thenReturn(typeHeader);
        when(etagHeader.getValue()).thenReturn(etag);
        when(typeHeader.getValue()).thenReturn("application/rdf+xml");
        when(entity.getContent()).thenReturn(new ByteArrayInputStream(testContent.getBytes()));

        httpHelper.loadProperties( object );
        final Graph g = object.getGraph();
        assertNotNull(g);
        assertTrue( g.contains( createURI(uri.toString()), HAS_PRIMARY_IDENTIFIER.asNode(),
            createLiteral("2fb9c440-db63-434f-929b-0ff29253205c")) );
    }

    @Test
    public void testCreateContentPutMethod() throws Exception {
        final String path = "/test";
        final InputStream in = new ByteArrayInputStream( "foo".getBytes() );
        final String mime = "image/jpeg";
        final String fn = "image.jpg";
        final String chk = "urn:sha1:c6bbf022f8f19d09106622aa912218417723f543";
        final URI checksumURI = new URI(chk);
        final FedoraContent cont = new FedoraContent().setContent(in).setContentType(mime)
                                                      .setFilename(fn).setChecksum(checksumURI);

        final HttpPut put = httpHelper.createContentPutMethod(path, null, cont);

        assertEquals( testRepositoryUrl + path + "/fcr:content?checksum=" + chk, put.getURI().toString() );
        assertEquals( in, put.getEntity().getContent() );
        assertEquals( mime, put.getFirstHeader("Content-Type").getValue() );
        assertEquals( "attachment; filename=\"" + fn + "\"", put.getFirstHeader("Content-Disposition").getValue() );
    }
}
