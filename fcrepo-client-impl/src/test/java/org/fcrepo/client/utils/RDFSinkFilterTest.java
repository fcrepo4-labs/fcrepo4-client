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
package org.fcrepo.client.utils;

import static com.hp.hpl.jena.graph.NodeFactory.createURI;
import static com.hp.hpl.jena.graph.Triple.create;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Iterator;

import org.fcrepo.client.FedoraException;
import org.fcrepo.kernel.RdfLexicon;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
/**
 *
 * @author lsitu
 *
 */
public class RDFSinkFilterTest {

    @Mock
    private Iterator<Triple> mockTriples;

    private String testDateValue = "2014-08-14T15:11:30.118Z";
    private String testMixinType = RdfLexicon.REPOSITORY_NAMESPACE + "test";
    private final Triple testCreatedDateTriple =
            create(createURI(RdfLexicon.RESTAPI_NAMESPACE + "test"), RdfLexicon.CREATED_DATE.asNode(),
                    ResourceFactory.createPlainLiteral(testDateValue).asNode());
    private final Triple testLastModifiedDateTriple =
            create(createURI(RdfLexicon.RESTAPI_NAMESPACE + "test"), RdfLexicon.LAST_MODIFIED_DATE.asNode(),
                    ResourceFactory.createPlainLiteral(testDateValue).asNode());
    private final Triple testMixinTriple =
            create(createURI(RdfLexicon.RESTAPI_NAMESPACE + "test"),
                    RdfLexicon.HAS_MIXIN_TYPE.asNode(), createURI(testMixinType));
    private final Triple testIsWritable =
            create(createURI(RdfLexicon.RESTAPI_NAMESPACE + "test"),
                    RdfLexicon.WRITABLE.asNode(), ResourceFactory.createPlainLiteral("true").asNode());

    @Before
    public void setUp() throws IOException {
        initMocks(this);
        when(mockTriples.hasNext()).thenReturn(true, true, true, true, false);
        when(mockTriples.next()).thenReturn(testCreatedDateTriple, testLastModifiedDateTriple,
                testMixinTriple, testIsWritable);
    }

    @Test
    public void testGetAllTriples() throws FedoraException {
        final Graph graph = RDFSinkFilter.filterTriples(mockTriples, Node.ANY);
        assertEquals("The number of triples doesn't matched.", 4, graph.size());
        assertTrue(graph.contains(testCreatedDateTriple));
        assertTrue(graph.contains(testLastModifiedDateTriple));
        assertTrue(graph.contains(testMixinTriple));
        assertTrue(graph.contains(testIsWritable));
    }

    @Test
    public void testFilterTriples() throws FedoraException {
        final Graph graph = RDFSinkFilter.filterTriples(mockTriples, RdfLexicon.CREATED_DATE.asNode());
        assertEquals("The number of triples doesn't matched.", graph.size(), 1);
        assertTrue(graph.contains(testCreatedDateTriple));
    }
}
