package org.sakaiproject.nakamura.jetty;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.http.api.ExtHttpService;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.resource.Resource;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate=true, metatype=true )
public class StaticContentServiceFactory {
	
	@Property(value={ "/static = static"}, cardinality=9999 )
	private static final String MAPPINGS2 = "mappings";

	private static final Logger LOGGER = LoggerFactory.getLogger(StaticContentServiceFactory.class);

	@Reference
	protected ExtHttpService extHttpService;

	private String[] mappings;

	@Activate
	public void activate(Map<String, Object> properties) throws NamespaceException {
		final HttpContext defaultContext = extHttpService.createDefaultHttpContext();
		mappings = (String[]) toStringArray(properties.get(MAPPINGS2), new String[]{"/static = static"});
		if ( mappings != null && mappings.length > 0 ) {
			for ( String location : mappings ) {
				String[] mapping = StringUtils.split(location,"=",2);
				if ( mapping != null && mapping.length == 2) {
					final File base = new File(mapping[1].trim());
					LOGGER.info("Registering [{}] [{}] ", mapping[0].trim(), base.getAbsolutePath());
					HttpContext c = new HttpContext() {
						
						public boolean handleSecurity(HttpServletRequest arg0,
								HttpServletResponse arg1) throws IOException {
							return true;
						}
						
						public URL getResource(String path) {
							LOGGER.info("Getting Path  {} ",path);
							File f = new File(path);
							if ( f.getAbsolutePath().startsWith(base.getAbsolutePath()) ) {
								if ( f.exists() ) {
									LOGGER.info("Found {} ",f.getAbsolutePath());
									try {
										return f.toURI().toURL();
									} catch (MalformedURLException e) {
										LOGGER.warn(e.getMessage(),e);
									}
								} else {
									LOGGER.info("Doesnt exist [{}] ",base.getAbsolutePath(),f.getAbsolutePath());								
								}
							} else {
								LOGGER.info("Path Wrong [{}] [{}] ",base.getAbsolutePath(),f.getAbsolutePath());								
							}
							LOGGER.info("Not Found {} ",f.getAbsolutePath());
							return null;
						}
						
						public String getMimeType(String fileName) {
							return defaultContext.getMimeType(fileName);
						}
					};
					extHttpService.registerResources(mapping[0].trim(), base.getAbsolutePath(), c);
				}
			}
		}
	}

	private String[] toStringArray(Object object, String[] defaultValue) {
		if ( object == null ) {
			return defaultValue;
		}
		if ( object instanceof String[] ) {
			return (String[]) object;
		}
		return StringUtils.split(String.valueOf(object),",");
	}

	@Deactivate
	public void deactivate(Map<String, Object> properties) {
		if ( mappings != null && mappings.length > 0 ) {
			for ( String location : mappings ) {
				String[] mapping = StringUtils.split(location,"=",2);
				if ( mapping != null && mapping.length == 2) {					
					extHttpService.unregister(mapping[0].trim());
				}
			}
		}
		mappings = null;
	}


}
