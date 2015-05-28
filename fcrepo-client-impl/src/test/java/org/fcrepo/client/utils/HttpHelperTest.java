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
package org.fcrepo.client.utils;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;

import org.fcrepo.client.BadRequestException;
import org.fcrepo.client.FedoraContent;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.ForbiddenException;
import org.fcrepo.client.NotFoundException;
import org.fcrepo.client.ReadOnlyException;
import org.fcrepo.client.impl.FedoraResourceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * HttpHelper test
 * @author escowles
 * @since 2014-09-04
 */
public class HttpHelperTest {

    @Mock
    private HttpClient mockClient;

    private HttpHelper helper;
    private HttpHelper readOnlyHelper;
    private Map<String, List<String>> params;

    private String repoURL = "http://localhost:8080/rest";
    private String etag = "dummyEtag";

    @Before
    public void setUp() throws IOException {
        initMocks(this);
        helper = new HttpHelper(repoURL, mockClient, false);
        readOnlyHelper = new HttpHelper(repoURL, mockClient, true);

        final List<String> values = new ArrayList<>();
        values.add("bar");
        values.add("baz");
        params = new HashMap<>();
        params.put("foo", values);
    }

    @Test
    public void testExecuteGet() throws Exception {
        final HttpGet get = new HttpGet(repoURL);
        helper.execute(get);
        verify(mockClient).execute(eq(get));
    }

    @Test
    public void testExecutePut() throws Exception {
        final HttpPut put = new HttpPut(repoURL);
        helper.execute(put);
        verify(mockClient).execute(eq(put));
    }

    @Test
    public void testExecuteReadOnlyGet() throws Exception {
        final HttpGet get = new HttpGet(repoURL);
        readOnlyHelper.execute(get);
        verify(mockClient).execute(eq(get));
    }

    @Test (expected = ReadOnlyException.class)
    public void testExecuteReadOnlyPut() throws Exception {
        final HttpPut put = new HttpPut(repoURL);
        readOnlyHelper.execute(put);
    }

    @Test
    public void testCreateHeadMethod() {
        final HttpHead head = helper.createHeadMethod("/foo");
        assertEquals( repoURL + "/foo", head.getURI().toString() );
    }

    @Test
    public void testCreateGetMethod() {
        final HttpGet get = helper.createGetMethod("/foo", params);
        assertEquals( repoURL + "/foo?foo=bar&foo=baz", get.getURI().toString() );
    }

    @Test
    public void testCreatePatchMethod() throws Exception {
        final String sparql = "test sparql command";
        final HttpPatch patch = helper.createPatchMethod("/foo", sparql);
        assertEquals( repoURL + "/foo", patch.getURI().toString() );
        assertEquals( "application/sparql-update", patch.getFirstHeader("Content-Type").getValue());
        assertEquals( sparql, IOUtils.toString(patch.getEntity().getContent()) );
    }

    @Test (expected = FedoraException.class)
    public void testCreatePatchMethodEmpty() throws Exception  {
        helper.createPatchMethod("/foo", "");
    }

    @Test
    public void testCreatePostMethod() {
        final HttpPost post = helper.createPostMethod("/foo", params);
        assertEquals( repoURL + "/foo?foo=bar&foo=baz", post.getURI().toString() );
    }

    @Test
    public void testCreatePutMethod() {
        final HttpPut put = helper.createPutMethod("/foo", params);
        assertEquals( repoURL + "/foo?foo=bar&foo=baz", put.getURI().toString() );
    }

    @Test
    public void testCreateContentPutMethod() throws Exception {
        final String dummyContent = "dummy content";
        final InputStream in = new ByteArrayInputStream( dummyContent.getBytes() );
        final String mimeType = "text/plain";
        final String filename = "dummy.txt";
        final FedoraContent content = new FedoraContent().setContent(in).setContentType(mimeType)
                .setFilename(filename);

        final HttpPut put = helper.createContentPutMethod("/foo", params, content);
        assertEquals( repoURL + "/foo?foo=bar&foo=baz", put.getURI().toString() );
        assertEquals( mimeType, put.getFirstHeader("Content-Type").getValue());
        assertEquals( dummyContent, IOUtils.toString(put.getEntity().getContent()) );
        assertEquals( "attachment; filename=\"" + filename + "\"",
                put.getFirstHeader("Content-Disposition").getValue().toString());
    }

    @Test
    public void testCreateTriplesPutMethod() throws Exception {
        final String dummyContent = "dummy content";
        final InputStream in = new ByteArrayInputStream( dummyContent.getBytes() );
        final String mimeType = "text/rdf+n3";

        final HttpPut put = helper.createTriplesPutMethod("/foo", in, mimeType);
        assertEquals( repoURL + "/foo", put.getURI().toString() );
        assertEquals( mimeType, put.getFirstHeader("Content-Type").getValue());
        assertEquals( dummyContent, IOUtils.toString(put.getEntity().getContent()) );
    }

    @Test (expected = FedoraException.class)
    public void testCreateTriplesPutMethodWithoutContent() throws Exception {
        final String mimeType = "text/rdf+n3";
        helper.createTriplesPutMethod("/foo", null, mimeType);
    }

    @Test (expected = FedoraException.class)
    public void testCreateTriplesPutMethodWithoutType() throws Exception {
        final String dummyContent = "dummy content";
        final InputStream in = new ByteArrayInputStream( dummyContent.getBytes() );
        helper.createTriplesPutMethod("/foo", in, "");
    }

    @Test
    public void testLoadProperties() throws Exception {
        final FedoraResourceImpl testResource = testLoadPropertiesWithStatus(200);
        assertEquals(etag, testResource.getEtagValue() );
        assertTrue(testResource.getMixins().contains("fedora:resource") );
    }

    @Test (expected = ForbiddenException.class)
    public void testLoadPropertiesForbidden() throws Exception {
        testLoadPropertiesWithStatus(403);
    }

    @Test (expected = BadRequestException.class)
    public void testLoadPropertiesBadRequest() throws Exception {
        testLoadPropertiesWithStatus(400);
    }

    @Test (expected = NotFoundException.class)
    public void testLoadPropertiesNotFound() throws Exception {
        testLoadPropertiesWithStatus(404);
    }

    private FedoraResourceImpl testLoadPropertiesWithStatus( final int statusCode ) throws Exception {
        final String triples = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" +
              "<rdf:Description rdf:about=\"http://localhost:8080/rest/foo\">" +
                "<mixinTypes xmlns=\"http://fedora.info/definitions/v4/repository#\" " +
                    "rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">fedora:resource</mixinTypes>" +
              "</rdf:Description>" +
            "</rdf:RDF>";
        final FedoraRepository mockRepo = mock(FedoraRepository.class);
        when(mockRepo.getRepositoryUrl()).thenReturn(repoURL);
        final FedoraResourceImpl origResource = new FedoraResourceImpl(mockRepo, helper, "/foo");
        final HttpResponse mockResponse = mock(HttpResponse.class);
        final ByteArrayEntity entity = new ByteArrayEntity(triples.getBytes());
        entity.setContentType("application/rdf+xml");
        final Header etagHeader = new BasicHeader("Etag", etag);
        final Header[] etagHeaders = new Header[]{ etagHeader };
        final StatusLine mockStatus = mock(StatusLine.class);

        when(mockClient.execute(any(HttpGet.class))).thenReturn(mockResponse);
        when(mockResponse.getEntity()).thenReturn(entity);
        when(mockResponse.getHeaders("ETag")).thenReturn(etagHeaders);
        when(mockResponse.getStatusLine()).thenReturn(mockStatus);
        when(mockStatus.getStatusCode()).thenReturn(statusCode);

        return helper.loadProperties( origResource );
    }

}
