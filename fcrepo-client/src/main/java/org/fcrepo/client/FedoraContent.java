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
     *
     * @return gets the contents as an input stream
    **/
    public InputStream getContent() {
        return content;
    }

    /**
     * Get the content type (MIME type) of the content.
     *
     * @return string containing the MIME type
    **/
    public String getContentType() {
        return contentType;
    }

    /**
     * Get the original filename of the content source file.
     *
     * @return string containing the filename
    **/
    public String getFilename() {
        return filename;
    }

    /**
     * Get the SHA-1 checksum of the content as a URI (e.g.,
     * "{@code urn:sha1:290fa4c6a6161c0941fcaa915e2f96aecc85cd9f}").
     *
     * @return uri containing a SHA-1 checksum of the contents
    **/
    public URI getChecksum() {
        return checksum;
    }

    /**
     * Set the content stream.
     *
     * @param content set the content to the given input stream
     * @return The updated object for chaining.
    **/
    public FedoraContent setContent( final InputStream content ) {
        this.content = content;
        return this;
    }

    /**
     * Set the content type (MIME type) of the content.
     *
     * @param contentType string containing the content type
     * @return The updated object for chaining.
    **/
    public FedoraContent setContentType( final String contentType ) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Set the filename of the content source file.
     *
     * @param filename string containing the filename
     * @return The updated object for chaining.
    **/
    public FedoraContent setFilename( final String filename ) {
        this.filename = filename;
        return this;
    }

    /**
     * Set the checksum of the content as a URI (e.g.,
     * "{@code urn:sha1:290fa4c6a6161c0941fcaa915e2f96aecc85cd9f}").
     *
     * @param checksum uri containing SHA-1 checksum of contents
     * @return The updated object for chaining.
    **/
    public FedoraContent setChecksum( final URI checksum ) {
        this.checksum = checksum;
        return this;
    }

}
