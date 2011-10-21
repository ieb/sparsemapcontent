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

	private static final String CONFIG_CONTENT_TYPE = "content-type";

	private static final String CONFIG_CONTENT_ENCODING = "content-encoding";

	private static final String RESULT = "result";

	public static final String CONFIG_RESULT_KEY = "result-key";

	public static final String CONFIG_FINALTEMPLATE = "finaltemplate";

	public static final String CONFIG_NAMESPACEMAP = "namespacemap";

	private static final Logger LOGGER = LoggerFactory.getLogger(RDFToHTMLProxyPostProcessor.class);


	@Reference
	protected TemplateService templateService;

	@SuppressWarnings("unchecked")
	public void process(Map<String, Object> config, Map<String, Object> templateParams,
			HttpServletResponse response, ProxyResponse proxyResponse)
			throws IOException {
		String namespaceMapConfig = (String) config.get(CONFIG_NAMESPACEMAP);
		LOGGER.info("Namespace setup {} ",namespaceMapConfig);
		String templateName = (String)config.get(CONFIG_FINALTEMPLATE);
		LOGGER.info("Template setup {} ",templateName);
		if  (templateName != null && !templateService.checkTemplateExists(templateName) ) {
			throw new IOException("Cant find template "+templateName+" specified by "+CONFIG_FINALTEMPLATE);
		}
		String contentType = (String) config.get(CONFIG_CONTENT_TYPE);
		String contentEncoding = (String) config.get(CONFIG_CONTENT_ENCODING);
		
		try {
			RDFToMap rdfToMap = new RDFToMap(namespaceMapConfig);
			String key = (String) templateParams.get(config.get(CONFIG_RESULT_KEY));
			rdfToMap.readMap(
					new StringReader(proxyResponse
							.getResponseBodyAsString()))
					.resolveToFullJson();
			if ( templateName != null ) {
				Map<String, Object> fullMap = rdfToMap.toMap();
				if ( key != null && fullMap.containsKey(key)) {
					fullMap.put(RESULT, ImmutableMap.copyOf((Map<String, Object>) fullMap.get(key)));
				}
				LOGGER.info("Rendering with {} as the base of the result map.",key);
				if ( contentType != null ) {
					response.setContentType(contentType);
				} else {
					response.setContentType("application/octet");
				}
				if ( contentEncoding != null ) {
					response.setCharacterEncoding(contentEncoding);
				}
				
			   templateService.process(fullMap, "UTF-8", response.getWriter(), templateName);
			} else {
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(rdfToMap.toJson(true));
				
			}
		} catch (XMLStreamException e) {
			LOGGER.error(e.getMessage(),e);
			response.sendError(500,"Failed to parse response from remote server");
		}

	}

	public String getName() {
		return "RDFToHTMLResolvedJsonProxyPostProcessor";
	}

}
