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

package org.fcrepo.client.utils;

import java.util.Iterator;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.riot.lang.SinkTriplesToGraph;
import org.apache.jena.riot.system.StreamRDFBase;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.RandomOrderGraph;

/**
 * class RDFSinkFilter filtering StreamRDF to Sink.
 * @author lsitu
 * @since 2014-08-13
**/
public class RDFSinkFilter extends StreamRDFBase {
    // properties to filter
    private final Node[] properties ;
    // destination to send the triples filtered.
    private final Sink<Triple> dest ;

    private RDFSinkFilter(final Sink<Triple> dest, final Node... properties) {
        this.dest = dest ;
        this.properties = new Node[properties.length];
        for ( int i = 0; i < properties.length; i++) {
            this.properties[i] = properties[i];
        }
    }

    @Override
    public void triple(final Triple triple) {
        for ( final Node p : properties ) {
            if ( Node.ANY == p || triple.getPredicate().equals(p) ) {
                dest.send(triple);
            }
        }
    }

    @Override
    public void finish() {
        // flush the buffered.
        dest.flush() ;
    }

    /**
     * Filter the triples
     * @param triples
     * @param properties
     * @return
     */
    public static Graph filterTriples (
            final Iterator<Triple> triples,
            final Node... properties) {
        final Graph filteredGraph = new RandomOrderGraph(RandomOrderGraph.createDefaultGraph());
        final Sink<Triple> graphOutput = new SinkTriplesToGraph(true, filteredGraph);
        final RDFSinkFilter rdfFilter = new RDFSinkFilter(graphOutput, properties);
        rdfFilter.start();
        while (triples.hasNext()) {
            final Triple triple = triples.next();
            rdfFilter.triple(triple);
        }
        rdfFilter.finish();
        return filteredGraph;
    }
}
