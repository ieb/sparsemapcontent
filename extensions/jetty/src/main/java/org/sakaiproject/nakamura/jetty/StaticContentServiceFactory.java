package org.sakaiproject.nakamura.jetty;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.felix.http.api.ExtHttpService;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyOption;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate=true, configurationFactory=true, metatype = true)
@Properties(value = {
		@Property(name = "alias", value = "/test"),
		@Property(name = "acceptRanges", value = "T", options = {
				@PropertyOption(name = "T", value = "True"),
				@PropertyOption(name = "F", value = "False") }),
		@Property(name = "dirAllowed", value = "T", options = {
				@PropertyOption(name = "T", value = "True"),
				@PropertyOption(name = "F", value = "False") }),
		@Property(name = "gzip", value = "F", options = {
				@PropertyOption(name = "T", value = "True"),
				@PropertyOption(name = "F", value = "False") }),
		@Property(name = "resourceBase", value = "/Users/ieb/public_html"),
		@Property(name = "relativeResourceBase", value = ""),
		@Property(name = "aliases", value = "F", options = {
				@PropertyOption(name = "T", value = "True"),
				@PropertyOption(name = "F", value = "False") }),
		@Property(name = "cacheType", value = "both", options = {
				@PropertyOption(name = "nio", value = "Native IO"),
				@PropertyOption(name = "both", value = "Both") }),
		@Property(name = "maxCacheSize", intValue = 102400),
		@Property(name = "maxCachedFileSize", intValue = 102400),
		@Property(name = "maxCachedFiles", intValue = 10000),
		@Property(name = "useFileMappedBuffer", value = "T", options = {
				@PropertyOption(name = "T", value = "True"),
				@PropertyOption(name = "F", value = "False") }),
		@Property(name = "cacheControl", value = "")

})
public class StaticContentServiceFactory {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(StaticContentServiceFactory.class);

	private static final String FACTORY_PID = "org.sakaiproject.nakamura.jetty.StaticContentServiceFactory";

	@Reference
	protected ExtHttpService extHttpService;

	private Map<String, StaticContentServlet> runningServlets = new HashMap<String, StaticContentServlet>();
	

	@Activate
	public void activate(Map<String, Object> properties) {
	}

	@Deactivate
	public void deactivate(Map<String, Object> properties) {
	}

	public void deleted(String pid) {
		synchronized(runningServlets) {
			if ( runningServlets.containsKey(pid)) {
				StaticContentServlet servlet = runningServlets.remove(pid);
				extHttpService.unregisterServlet(servlet);
				servlet.destroy();
			}
		}
	}

	public String getName() {
		return FACTORY_PID;
	}


	@Modified
	public void updated(ComponentContext componentContext)
			throws ConfigurationException {
		Dictionary<String, Object> properties = componentContext.getProperties();
		LOGGER.debug("Properties {} ", properties);
		String pid = (String) properties.get(Constants.SERVICE_PID);
		String factoryPid = (String) properties.get("service.factoryPid");
		if ( factoryPid == null || FACTORY_PID.equals(pid)) {
			return;
		}
		LOGGER.debug("Calling Updated {} ",pid);
		synchronized (runningServlets) {
			if ( runningServlets.containsKey(pid)) {
				LOGGER.debug("Destroying {} ",pid);
				StaticContentServlet servlet = runningServlets.remove(pid);
				extHttpService.unregisterServlet(servlet);
				servlet.destroy();				
			}
			LOGGER.debug("Creating {} ",pid);
			StaticContentServlet servlet = new StaticContentServlet();
			String baseUrl = (String) properties.get("alias");
			HttpContext httpContext = extHttpService.createDefaultHttpContext();
			try {
				extHttpService.registerServlet(baseUrl, servlet, properties,
						httpContext);
				LOGGER.debug("Registered Servlet {} {} {} ",new Object[]{baseUrl, servlet, httpContext});
				runningServlets.put(pid, servlet);
			} catch (ServletException e) {
				LOGGER.error(e.getMessage(), e);
				throw new ConfigurationException(null, e.getMessage(), e);
			} catch (NamespaceException e) {
				LOGGER.error(e.getMessage(), e);
				throw new ConfigurationException(null, e.getMessage(), e);
			}
		}
	}

}
