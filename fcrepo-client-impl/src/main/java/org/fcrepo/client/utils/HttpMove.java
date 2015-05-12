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

import java.net.URI;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * HTTP move
 * 
 * @author sleroux
 * @since 2015-06-03
 **/
public class HttpMove extends HttpEntityEnclosingRequestBase {

    /**
     * Create an HTTP MOVE request.
     * 
     * @param source
     *            Source String URL.
     * @param destination
     *            Destination String URL.
     **/
    public HttpMove(final String source, final String destination) {
        this(URI.create(source), URI.create(destination));
    }

    /**
     * Create an HTTP MOVE request.
     * 
     * @param source
     *            Source URL.
     * @param destination
     *            Destination URL.
     **/
    public HttpMove(final URI source, final URI destination) {
        this.setHeader(HttpHeaders.DESTINATION, destination.toASCIIString());
        this.setURI(source);
    }

    /**
     * Returns the request method.
     **/
    @Override
    public String getMethod() {
        return "MOVE";
    }
}
