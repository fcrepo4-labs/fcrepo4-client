/*
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
package org.fcrepo.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test of NotFoundException.
 * @author escowles
 * @since 2014-09-02
**/
public class NotFoundExceptionTest extends FedoraExceptionTest {

    @Test
    public void testNotFoundException() {
        final String message = "Test exception message";
        final NullPointerException npe = new NullPointerException();

        final NotFoundException ex = new NotFoundException(message, npe);

        assertEquals( message, ex.getMessage() );
        assertEquals( npe, ex.getCause() );
    }

    @Test
    public void testNotFoundExceptionMessage() {
        final String message = "Test exception message";
        final NotFoundException ex = new NotFoundException(message);
        assertEquals( message, ex.getMessage() );
    }

    @Test
    public void testNotFoundExceptionCause() {
        final NullPointerException npe = new NullPointerException("Embedded");
        final NotFoundException ex = new NotFoundException(npe);
        assertEquals( npe, ex.getCause() );
        assertEquals( npe.toString(), ex.getMessage() );
    }
}
