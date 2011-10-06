package uk.co.tfd.sm.proxy;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.proxy.ProxyPostProcessor;
import uk.co.tfd.sm.api.proxy.ProxyResponse;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

@SuppressWarnings("restriction")
@Component(immediate = true, metatype = true)
@Service(value = ProxyPostProcessor.class)
public class RDFToResolvedJsonProxyPostProcessor implements ProxyPostProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RDFToResolvedJsonProxyPostProcessor.class);

	public void process(Map<String, Object> templateParams,
			HttpServletResponse response, ProxyResponse proxyResponse)
			throws IOException {
		String namespaceMapConfig = (String) templateParams.get("namespacemap");
		String[] pairs = StringUtils.split(namespaceMapConfig, ";");
		Builder<String, String> b = ImmutableMap.builder();
		for (String pair : pairs) {
			String[] kv = StringUtils.split(pair, "=", 2);
			if (kv == null || kv.length != 2) {
				throw new RuntimeException(
						"Names space key value pairs must be of the form ns=nsuri;ns=nsuri failed to parse "
								+ namespaceMapConfig);
			}
			b.put(kv[0], kv[1]);
		}
		try {
			RDFToMap rdfToMap = new RDFToMap(b.build());
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
