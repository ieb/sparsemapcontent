package uk.co.tfd.sm.proxy;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.proxy.ProxyPostProcessor;
import uk.co.tfd.sm.api.proxy.ProxyResponse;
import uk.co.tfd.sm.api.template.TemplateService;

import com.google.common.collect.ImmutableMap;

@SuppressWarnings("restriction")
@Component(immediate = true, metatype = true)
@Service(value = ProxyPostProcessor.class)
public class RDFToHTMLProxyPostProcessor implements ProxyPostProcessor {

	private static final String RESULT = "result";

	public static final String CONFIG_RESULT_KEY = "result-key";

	public static final String CONFIG_FINALTEMPLATE = "finaltemplate";

	public static final String CONFIG_NAMESPACEMAP = "namespacemap";

	private static final Logger LOGGER = LoggerFactory.getLogger(RDFToHTMLProxyPostProcessor.class);

	@Reference
	protected TemplateService templateService;

	@SuppressWarnings("unchecked")
	public void process(Map<String, Object> templateParams,
			HttpServletResponse response, ProxyResponse proxyResponse)
			throws IOException {
		String namespaceMapConfig = (String) templateParams.get(CONFIG_NAMESPACEMAP);
		String templateName = (String)templateParams.get(CONFIG_FINALTEMPLATE);
		if  (!templateService.checkTemplateExists(templateName) ) {
			throw new IOException("Cant find template "+templateName+" specified by "+CONFIG_FINALTEMPLATE);
		}
		
		try {
			RDFToMap rdfToMap = new RDFToMap(namespaceMapConfig);
			Map<String, Object> fullMap = rdfToMap.readMap(
					new StringReader(proxyResponse
							.getResponseBodyAsString()))
					.resolveToFullJson().toMap();
			String key = (String) templateParams.get(templateParams.get(CONFIG_RESULT_KEY));
			if ( key != null && fullMap.containsKey(key)) {
				fullMap.put(RESULT, ImmutableMap.copyOf((Map<String, Object>) fullMap.get(key)));
			}
			LOGGER.info("Rendering with {} {}  ",key,fullMap);
			templateService.process(fullMap, "UTF-8", response.getWriter(), templateName);
		} catch (XMLStreamException e) {
			LOGGER.error(e.getMessage(),e);
			response.sendError(500,"Failed to parse response from remote server");
		}

	}

	public String getName() {
		return "RDFToHTMLResolvedJsonProxyPostProcessor";
	}

}
