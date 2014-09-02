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

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.IOException;

import org.fcrepo.client.NotFoundException;

import org.fcrepo.client.FedoraException;
import org.fcrepo.client.ReadOnlyException;

import org.junit.Before;
import org.junit.Test;


/**
 * Read-only repository impl -- read operations should work, but write operations
 * should throw a ReadOnlyException.
 *
 * @author escowles
**/
public class ReadOnlyFedoraRepositoryImplTest extends FedoraRepositoryImplTest {

    ReadOnlyFedoraRepositoryImpl fedoraRepository;

    @Before
    public void setUp() throws IOException {
        super.setUp();
        super.fedoraRepository = new ReadOnlyFedoraRepositoryImpl(testRepositoryUrl, mockClient);
    }

    @Test
    public void testGetObject() throws IOException, FedoraException {
        super.testGetObject();
    }

    @Test
    public void testGetRepositoryUrl() {
        super.testGetRepositoryUrl();
    }

    @Test
    public void testExists() throws IOException, FedoraException {
        super.testExists();
    }

    @Test
    public void testExistsNonExistent() throws IOException, FedoraException {
        super.testExistsNonExistent();
    }

    @Test (expected = ReadOnlyException.class)
    public void testCreateObject() throws IOException, FedoraException {
        super.testCreateObject();
    }

    @Test
    public void testFindOrCreateObject() throws FedoraException {
        super.testFindOrCreateObject();
    }

    @Test (expected = ReadOnlyException.class)
    public void testFindOrCreateObjectNonExistent() throws FedoraException {
        final ReadOnlyFedoraRepositoryImpl spy = spy( new ReadOnlyFedoraRepositoryImpl(testRepositoryUrl, mockClient) );
        final NotFoundException mockException = mock(NotFoundException.class);
        doThrow(mockException).when(spy).getObject(anyString());
        spy.findOrCreateObject("/foo");
    }

    @Test
    public void testWritable() {
        System.out.println("ReadOnlyFedoraRepositoryImpl.isWritable()" + fedoraRepository);
        assertFalse( super.fedoraRepository.isWritable() );
    }
}
