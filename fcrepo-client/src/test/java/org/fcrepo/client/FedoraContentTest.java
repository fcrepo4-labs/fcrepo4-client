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

package org.fcrepo.client;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

/**
 * Test of FedoraContet.
 * @author escowles
 * @since 2014-08-08
**/
public class FedoraContentTest {

    @Test
    public void testFedoraContent() throws URISyntaxException {
        final InputStream in = new ByteArrayInputStream("foo".getBytes());
        final String contentType = "text/plain";
        final String filename = "foo.txt";
        final URI checksum = new URI("urn:sha1:0beec7b5ea3f0fdbc95d0dd47f3c5bc275da8a33");

        final FedoraContent content = new FedoraContent();
        content.setContent(in);
        content.setContentType(contentType);
        content.setFilename(filename);
        content.setChecksum(checksum);

        assertEquals( in, content.getContent() );
        assertEquals( contentType, content.getContentType() );
        assertEquals( filename, content.getFilename() );
        assertEquals( checksum, content.getChecksum() );
    }

    @Test
    public void testFedoraContentChain() throws URISyntaxException {
        final ByteArrayInputStream in = new ByteArrayInputStream("foo".getBytes());
        final String contentType = "text/plain";
        final String filename = "foo.txt";
        final URI checksum = new URI("urn:sha1:0beec7b5ea3f0fdbc95d0dd47f3c5bc275da8a33");

        final FedoraContent content = new FedoraContent().setContent(in).setContentType(contentType)
                .setFilename(filename).setChecksum(checksum);

        assertEquals( in, content.getContent() );
        assertEquals( contentType, content.getContentType() );
        assertEquals( filename, content.getFilename() );
        assertEquals( checksum, content.getChecksum() );
    }
}
