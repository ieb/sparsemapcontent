package uk.co.tfd.sm.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.proxy.ProxyResponse;

import com.google.common.collect.ImmutableMap;

public class RDFToResolvedJsonProxyPostProcessorTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RDFToResolvedJsonProxyPostProcessorTest.class);

	@Mock
	private HttpServletResponse response;
	
	@Mock
	private ProxyResponse proxyResponse;
	
	public RDFToResolvedJsonProxyPostProcessorTest() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test() throws IOException {
		InputStream in = this.getClass().getResourceAsStream("test.rdf");
		String rdfContent = IOUtils.toString(in);
		Mockito.when(proxyResponse.getResponseBodyAsString()).thenReturn(rdfContent);
		StringWriter outputWriter = new StringWriter();
		Mockito.when(response.getWriter()).thenReturn(new PrintWriter(outputWriter));
		RDFToResolvedJsonProxyPostProcessor rp = new RDFToResolvedJsonProxyPostProcessor();
		StringBuilder ns = new StringBuilder();
		ns.append("rdf=http://www.w3.org/1999/02/22-rdf-syntax-ns#;");
		ns.append("vivocore=http://vivoweb.org/ontology/core#;");
		ns.append("http://vivo.tfd.co.uk/individual/;");
		ns.append("rdfs=http://www.w3.org/2000/01/rdf-schema#;");
		ns.append("vitro=http://vitro.mannlib.cornell.edu/ns/vitro/0.7#;");
		ns.append("foaf=http://xmlns.com/foaf/0.1/;");
		ns.append("owl=http://www.w3.org/2002/07/owl#;");

		Map<String, Object> config = ImmutableMap.of(RDFToResolvedJsonProxyPostProcessor.CONFIG_NAMESPACEMAP, (Object)ns.toString());
		Map<String, Object> templateParams = ImmutableMap.of();
		rp.process(config, templateParams, response, proxyResponse);
		String op = outputWriter.toString();
		LOGGER.info("Output {} ", op);
		
	}

}
