// Portions Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// Clark & Parsia, LLC parts of this source code are available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package org.mindswap.pellet.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.mindswap.pellet.jena.PelletInfGraph;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.mindswap.pellet.jena.graph.loader.GraphLoader;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.LocationMapper;
import org.apache.jena.util.LocatorFile;
import org.apache.jena.util.iterator.ExtendedIterator;

public class JenaWebOntTester implements WebOntTester {
	private OntModel		model;
	private LocationMapper	mapper;
	
	public JenaWebOntTester() {
		mapper = new LocationMapper();
		FileManager manager = OntDocumentManager.getInstance().getFileManager();
		manager.setLocationMapper( mapper );
		manager.addLocator( new LocatorFile(null) );
	}

	public void classify() {
		((PelletInfGraph) model.getGraph()).getKB().realize();
	}

	public boolean isConsistent() {
		return ((PelletInfGraph) model.getGraph()).getKB().isConsistent();
	}
	
	public void testEntailment(String entailmentFileURI, boolean positiveEntailment) {
		Model entailments = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
		entailments.read( entailmentFileURI, entailmentFileURI, fileType( entailmentFileURI ) );
		
		Graph entailmentsGraph = entailments.getGraph();
		PelletInfGraph pellet = (PelletInfGraph) model.getGraph();

		GraphLoader savedLoader = pellet.attachTemporaryGraph( entailmentsGraph );

		ExtendedIterator i = entailmentsGraph.find( Triple.ANY );
		
		while( i.hasNext() ) {
			Triple triple = (Triple) i.next();
			if( !pellet.entails( triple ) ) {
				assertFalse( "Entailment failed for " + triple, positiveEntailment );
				return;				
			}
		}

		pellet.detachTemporaryGraph( entailmentsGraph, savedLoader );
		
		assertTrue( "All axioms entailed in negative entailment test", positiveEntailment );
	}

	public void setInputOntology(String inputFileURI) {
		model = ModelFactory.createOntologyModel( PelletReasonerFactory.THE_SPEC );
		model.read( inputFileURI, inputFileURI, fileType( inputFileURI ) );
		model.prepare();
	}

	public void setTimeout(long timeout) {
		((PelletInfGraph) model.getGraph()).getKB().setTimeout( timeout );
	}

	public void registerURIMapping(String fromURI, String toURI) {
		mapper.addAltEntry( fromURI, toURI );
	}

	private String fileType(String fileURI) {
		if( fileURI.endsWith( ".n3" ) ) {
			return "N3";
		}
		else {
			return "RDF/XML";
		}
	}
}
