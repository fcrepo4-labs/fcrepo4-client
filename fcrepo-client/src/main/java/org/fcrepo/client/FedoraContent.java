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
 * Container for holding properties of datastream content.  All setters return the updated object so they can be
 * chained:
 * <pre>{@code FedoraContent ds = new FedoraContent().setContent(in).setFilename(filename);}</pre>
 *
 * @author escowles
 * @since 2014-08-08
**/
public class FedoraContent {

    private InputStream content;
    private String contentType;
    private String filename;
    private URI checksum;

    /**
     * Default constructor.
    **/
    public FedoraContent() {
    }

    /**
     * Get the content stream.
    **/
    public InputStream getContent() {
        return content;
    }

    /**
     * Get the content type (MIME type) of the content.
    **/
    public String getContentType() {
        return contentType;
    }

    /**
     * Get the original filename of the content source file.
    **/
    public String getFilename() {
        return filename;
    }

    /**
     * Get the SHA-1 checksum of the content as a URI (e.g.,
     * "{@code urn:sha1:290fa4c6a6161c0941fcaa915e2f96aecc85cd9f}").
    **/
    public URI getChecksum() {
        return checksum;
    }

    /**
     * Set the content stream.
     * @return The updated object for chaining.
    **/
    public FedoraContent setContent( final InputStream content ) {
        this.content = content;
        return this;
    }

    /**
     * Set the content type (MIME type) of the content.
     * @return The updated object for chaining.
    **/
    public FedoraContent setContentType( final String contentType ) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Set the filename of the content source file.
     * @return The updated object for chaining.
    **/
    public FedoraContent setFilename( final String filename ) {
        this.filename = filename;
        return this;
    }

    /**
     * Set the checksum of the content as a URI (e.g.,
     * "{@code urn:sha1:290fa4c6a6161c0941fcaa915e2f96aecc85cd9f}").
     * @return The updated object for chaining.
    **/
    public FedoraContent setChecksum( final URI checksum ) {
        this.checksum = checksum;
        return this;
    }

}
