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
 * Superclass of all Fedora repository exceptions.
 * @author escowles
 * @since 2014-08-12
**/
public class FedoraException extends Exception {

    /**
     * Default constructor.
    **/
    public FedoraException() {
        super();
    }

    /**
     * Constructor with reason.
     * @param message Exception message.
    **/
    public FedoraException( final String message ) {
        super( message );
    }

    /**
     * Constructor with reason and cause.
     * @param message Exception message.
     * @param cause Exception cause.
    **/
    public FedoraException( final String message, final Throwable cause ) {
        super( message, cause );
    }

    /**
     * Constructor with cause.
     * @param cause Exception cause.
    **/
    public FedoraException( final Throwable cause ) {
        super( cause );
    }
}
