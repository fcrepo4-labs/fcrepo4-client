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
 * Exception thrown when a fixity check fails.
 * @author escowles
 * @since 2014-08-01
**/
public class FixityException extends Exception {

    /**
     * Default constructor.
    **/
    public FixityException() {
        super();
    }

    /**
     * Constructor with reason.
     * @param message Details of the fixity exception.
    **/
    public FixityException( final String message ) {
        super( message );
    }
}
