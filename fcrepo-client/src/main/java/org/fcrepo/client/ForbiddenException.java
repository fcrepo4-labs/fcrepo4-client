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

/**
 * Fedora exception indicating the requested resource cannot be accessed because
 * access controls do not allow it (HTTP Status Code 403).
 * @author escowles
 * @since 2014-09-02
**/
public class ForbiddenException extends FedoraException {

    /**
     * Default constructor.
    **/
    public ForbiddenException() {
        super();
    }

    /**
     * Constructor with reason.
     * @param message Exception message.
    **/
    public ForbiddenException( final String message ) {
        super( message );
    }

    /**
     * Constructor with reason and cause.
     * @param message Exception message.
     * @param cause Exception cause.
    **/
    public ForbiddenException( final String message, final Throwable cause ) {
        super( message, cause );
    }

    /**
     * Constructor with cause.
     * @param cause Exception cause.
    **/
    public ForbiddenException( final Throwable cause ) {
        super( cause );
    }
}
