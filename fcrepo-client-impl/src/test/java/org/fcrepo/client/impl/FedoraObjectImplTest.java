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
import static com.hp.hpl.jena.graph.NodeFactory.createLiteral;
import static com.hp.hpl.jena.graph.NodeFactory.createURI;
import static com.hp.hpl.jena.graph.Triple.create;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.fcrepo.kernel.RdfLexicon.HAS_CHILD;
import static org.fcrepo.kernel.RdfLexicon.HAS_MIXIN_TYPE;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Collection;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;

import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraResource;
import org.fcrepo.client.utils.HttpHelper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Object Impl test.
 * @author escowles
 * @since 2014-10-08
 */
public class FedoraObjectImplTest {

    @Mock
    FedoraRepositoryImpl mockRepository;

    @Mock
    HttpHelper mockHelper;

    private FedoraObject object;

    private String objectPath = "/test/object";
    private String objectWithoutChildrenPath = "/test/objectWithoutChildren";
    private String datastreamChildPath = "/test/object/ds1";
    private String objectChildPath = "/test/object/obj1";
    private String customChildPath = "/test/object/custom1";
    private String repositoryURL = "http://localhost:8080/rest";

    private Node objectSubj = createURI(repositoryURL + objectPath);
    private Node objectChildSubj = createURI(repositoryURL + objectChildPath);
    private Node customChildSubj = createURI(repositoryURL + customChildPath);
    private Node datastreamChildSubj = createURI(repositoryURL + datastreamChildPath);
    private FedoraObjectImpl objectWithChildren;
    private FedoraObjectImpl objectWithoutChildren;
    private FedoraObject objectChild;
    private FedoraObject customChild;
    private FedoraDatastream datastreamChild;

    @Before
    public void setUp() throws FedoraException {
        initMocks(this);

        objectWithChildren = new FedoraObjectImpl(mockRepository, mockHelper, objectPath);
        objectWithoutChildren = new FedoraObjectImpl(mockRepository, mockHelper, objectWithoutChildrenPath);
        objectChild = new FedoraObjectImpl(mockRepository, mockHelper, objectChildPath);
        customChild = new FedoraObjectImpl(mockRepository, mockHelper, customChildPath);
        datastreamChild = new FedoraDatastreamImpl(mockRepository, mockHelper, datastreamChildPath);

        final Graph graph = createDefaultGraph();
        graph.add( create(objectSubj, HAS_CHILD.asNode(), datastreamChildSubj) );
        graph.add( create(objectSubj, HAS_CHILD.asNode(), objectChildSubj) );
        graph.add( create(objectSubj, HAS_CHILD.asNode(), customChildSubj) );
        graph.add( create(datastreamChildSubj, HAS_MIXIN_TYPE.asNode(), createLiteral("fedora:datastream")) );
        graph.add( create(objectChildSubj, HAS_MIXIN_TYPE.asNode(), createLiteral("fedora:object")) );
        graph.add( create(customChildSubj, HAS_MIXIN_TYPE.asNode(), createLiteral("fedora:custom")) );
        objectWithChildren.setGraph( graph );
        objectWithoutChildren.setGraph( createDefaultGraph() );

        when(mockRepository.getRepositoryUrl()).thenReturn(repositoryURL);
        when(mockRepository.getObject(eq(objectPath))).thenReturn(objectWithChildren);
        when(mockRepository.getObject(eq(objectChildPath))).thenReturn(objectChild);
        when(mockRepository.getObject(eq(customChildPath))).thenReturn(customChild);
        when(mockRepository.getDatastream(eq(datastreamChildPath))).thenReturn(datastreamChild);
    }

    @Test
    public void testGetChildren() throws FedoraException {
        final Collection<FedoraResource> children = objectWithChildren.getChildren(null);
        verify(mockRepository).getObject(objectChildPath);
        verify(mockRepository).getObject(customChildPath);
        verify(mockRepository).getDatastream(datastreamChildPath);
        assertTrue( children.contains(objectChild) );
        assertTrue( children.contains(customChild) );
        assertTrue( children.contains(datastreamChild) );
    }

    @Test
    public void testGetChildrenObjects() throws FedoraException {
        final Collection<FedoraResource> children = objectWithChildren.getChildren("fedora:object");
        verify(mockRepository).getObject(objectChildPath);
        verify(mockRepository, never()).getObject(customChildPath);
        verify(mockRepository, never()).getDatastream(datastreamChildPath);
        assertTrue( children.contains(objectChild) );
        assertFalse( children.contains(customChild) );
        assertFalse( children.contains(datastreamChild) );
    }

    @Test
    public void testGetChildrenCustom() throws FedoraException {
        final Collection<FedoraResource> children = objectWithChildren.getChildren("fedora:custom");
        verify(mockRepository, never()).getObject(objectChildPath);
        verify(mockRepository).getObject(customChildPath);
        verify(mockRepository, never()).getDatastream(datastreamChildPath);
        assertFalse( children.contains(objectChild) );
        assertTrue( children.contains(customChild) );
        assertFalse( children.contains(datastreamChild) );
    }

    @Test
    public void testGetChildrenDatastreams() throws FedoraException {
        final Collection<FedoraResource> children = objectWithChildren.getChildren("fedora:datastream");
        verify(mockRepository, never()).getObject(objectChildPath);
        verify(mockRepository, never()).getObject(customChildPath);
        verify(mockRepository).getDatastream(datastreamChildPath);
        assertFalse( children.contains(objectChild) );
        assertFalse( children.contains(customChild) );
        assertTrue( children.contains(datastreamChild) );
    }

    @Test
    public void testGetChildrenNoChildren() throws FedoraException {
        final Collection<FedoraResource> children = objectWithoutChildren.getChildren(null);
        verify(mockRepository, never()).getObject(anyString());
        verify(mockRepository, never()).getDatastream(anyString());
        assertEquals( 0, children.size() );
    }

    @Test
    public void testGetChildrenNoMatch() throws FedoraException {
        final Collection<FedoraResource> children = objectWithChildren.getChildren("bogus:mixin");
        verify(mockRepository, never()).getObject(anyString());
        verify(mockRepository, never()).getDatastream(anyString());
        assertEquals( 0, children.size() );
    }
}
