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

import java.io.InputStream;
import java.net.URI;

/**
 * A Fedora Datastream, a Resource that can contain content.
 * @author escowles
 * @since 2014-08-01
**/
public interface FedoraDatastream extends FedoraResource {

    /**
     * Check the size and checksum of the datastream content.
    **/
    public void checkFixity() throws FixityException;

    /**
     * Get the datastream content as an InputStream.
    **/
    public InputStream getContent();

    /**
     * Get the checksum of the datastream content.
    **/
    public URI getContentDigest();

    /**
     * Get the size of the datastream content in bytes.
    **/
    public Long getContentSize();

    /**
     * Get the datastream filename.
    **/
    public String getFilename();

    /**
     * Get the datastream content type (MIME type).
    **/
    public String getContentType();

    /**
     * Get the Object that contains this Datastream.
    **/
    public FedoraObject getObject();

    /**
     * Check whether this Datastream has content.
    **/
    public boolean hasContent();

    /**
     * Replace the content of this Datastream.
     * @param content Updated content of the datastream.
    **/
    public void updateContent( FedoraContent content ) throws ReadOnlyException;
}
