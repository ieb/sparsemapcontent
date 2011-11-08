package uk.co.tfd.sm.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import uk.co.tfd.sm.api.template.TemplateService;


@Component(immediate=true, metatype=true)
@Service(value=TemplateService.class)
public class TemplateServiceImpl implements TemplateService {

	/**
	 * The shared velocity engine, which should cache all the templates. (need
	 * to sort out how to invalidate).
	 */
	private VelocityEngine velocityEngine;

	@SuppressWarnings("unused")
	@Property(value = "templates")
	private static final String PROP_RESOURCE_LOADER_PATH = "file.resource.loader.path";
	
	@Activate
	public void activate(Map<String, Object> properties) throws IOException {
		
		Properties p = new Properties();
		InputStream in = this.getClass().getResourceAsStream("templateService.config");
		if ( in != null ) {
			p.load(in);
			in.close();
		}
		// override with any supplied properties.
		if ( properties != null ) {
			for ( Entry<String, Object> e : properties.entrySet()) {
				Object o = e.getValue();
				String k = e.getKey();
				p.put(k, o);
			}
		}
		velocityEngine = new VelocityEngine(p);
		VelocityLogger vl = new VelocityLogger(this.getClass());
		if ( properties != null ) {
			vl.setDebugMode(Boolean.parseBoolean(String.valueOf(properties.get("debug"))));
		}
		velocityEngine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM,
				vl);
		
		

		ExtendedProperties configuration = new ExtendedProperties();
		velocityEngine.setExtendedProperties(configuration);
		velocityEngine.init();
	}

	public boolean evaluate(Map<String, Object> context, Writer writer,
			String logTag, String templateAsString) {
		return velocityEngine.evaluate(new VelocityContext(context), writer, logTag, templateAsString);
	}

	public boolean evaluate(Map<String, Object> context, Writer writer,
			String logTag, Reader templateReader) {
		return velocityEngine.evaluate(new VelocityContext(context), writer, logTag, templateReader);
	}

	public boolean process(Map<String, Object> context, String encoding, Writer writer, String templateName) {
		return velocityEngine.mergeTemplate(templateName, encoding, new VelocityContext(context), writer);
		
	}

	public boolean checkTemplateExists(String templateName) {
		return velocityEngine.resourceExists(templateName);
	}

}
