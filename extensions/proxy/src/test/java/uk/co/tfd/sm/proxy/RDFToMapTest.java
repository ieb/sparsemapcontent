package uk.co.tfd.sm.proxy;

import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class RDFToMapTest {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RDFToMap.class);

	@Test
	public void testRDFToMap() throws XMLStreamException {
		InputStream in = this.getClass().getResourceAsStream("test.rdf");
		InputStreamReader reader = new InputStreamReader(in);
		Builder<String, String> b = ImmutableMap.builder();
		b.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
		b.put("http://vivoweb.org/ontology/core#", "vivocore");
		b.put("http://vivo.tfd.co.uk/individual/","");
		b.put("http://www.w3.org/2000/01/rdf-schema#", "rdfs");
		b.put("http://vitro.mannlib.cornell.edu/ns/vitro/0.7#","vitro");
		b.put("http://xmlns.com/foaf/0.1/","foaf");
		b.put("http://www.w3.org/2002/07/owl#", "owl");

		RDFToMap rdfToMap = new RDFToMap(b.build());
		LOGGER.info("JSON = {} ",rdfToMap.readMap(reader).resolveToFullJson().toJson(true));
	}


}
