package uk.co.tfd.sm.proxy;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.proxy.ProxyPostProcessor;
import uk.co.tfd.sm.api.proxy.ProxyResponse;

@SuppressWarnings("restriction")
@Component(immediate = true, metatype = true)
@Service(value = ProxyPostProcessor.class)
public class RDFToResolvedJsonProxyPostProcessor implements ProxyPostProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RDFToResolvedJsonProxyPostProcessor.class);

	public static final String CONFIG_NAMESPACEMAP = "namespacemap";

	public void process(Map<String, Object> templateParams,
			HttpServletResponse response, ProxyResponse proxyResponse)
			throws IOException {
		String namespaceMapConfig = (String) templateParams.get(CONFIG_NAMESPACEMAP);
		try {
			RDFToMap rdfToMap = new RDFToMap(namespaceMapConfig);
			response.getWriter().append(
					rdfToMap.readMap(
							new StringReader(proxyResponse
									.getResponseBodyAsString()))
							.resolveToFullJson().toJson(false));
		} catch (XMLStreamException e) {
			LOGGER.error(e.getMessage(),e);
			response.sendError(500,"Failed to parse response from remote server");
		}

	}

	public String getName() {
		return "RDFToResolvedJsonProxyPostProcessor";
	}

}
