package org.sakaiproject.nakamura.jetty;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

@Component(immediate=true, metatype=true )
public class StaticContentServiceFactory {
	

	private static final Logger LOGGER = LoggerFactory.getLogger(StaticContentServiceFactory.class);

	private static final String[] DEFAULT_MAPPINGS = new String[]{
		"/devwidgets = static/ui/devwidgets",
		"/dev = static/ui/dev",
		"/403 = static/ui/dev/403.html",
		"/404 = static/ui/dev/404.html",
		"/500 = static/ui/dev/500.html",
		"/acknowledgements = static/ui/dev/acknowledgements.html",
		"/categories = static/ui/dev/allcategories.html",
		"/category = static/ui/dev/category.html",
		"/content = static/ui/dev/content_profile.html",
		"/register = static/ui/dev/create_new_account.html",
		"/create.html = static/ui/dev/createnew.html",
		"/create = static/ui/dev/createnew.html",
		"/favicon.ico = static/ui/dev/favicon.ico",
		"/index.html = static/ui/dev/index.html",
		"/index = static/ui/dev/index.html",
		"/logout = static/ui/dev/logout.html",
		"/me.html = static/ui/dev/me.html",
		"/me = static/ui/dev/me.html",
		"/search = static/ui/dev/search.html",
		"/search/sakai2 = static/ui/dev/search_sakai2.html",
		"/var = static/var",
		"/system/me = static/me.json"
		};

	@Property(value={ 
			"/devwidgets = static/ui/devwidgets",
			"/dev = static/ui/dev",
			"/403 = static/ui/dev/403.html",
			"/404 = static/ui/dev/404.html",
			"/500 = static/ui/dev/500.html",
			"/acknowledgements = static/ui/dev/acknowledgements.html",
			"/categories = static/ui/dev/allcategories.html",
			"/category = static/ui/dev/category.html",
			"/content = static/ui/dev/content_profile.html",
			"/register = static/ui/dev/create_new_account.html",
			"/create.html = static/ui/dev/createnew.html",
			"/create = static/ui/dev/createnew.html",
			"/favicon.ico = static/ui/dev/favicon.ico",
			"/index.html = static/ui/dev/index.html",
			"/index = static/ui/dev/index.html",
			"/logout = static/ui/dev/logout.html",
			"/me.html = static/ui/dev/me.html",
			"/me = static/ui/dev/me.html",
			"/search = static/ui/dev/search.html",
			"/search/sakai2 = static/ui/dev/search_sakai2.html",
			"/var = static/var",
			"/system/me = static/me.json"
			
	}, cardinality=9999 )
	private static final String MAPPINGS2 = "mappings";

	@Reference
	protected ExtHttpService extHttpService;

	private String[] mappings;

	@Activate
	public void activate(Map<String, Object> properties) throws NamespaceException, IOException {
		final HttpContext defaultContext = extHttpService.createDefaultHttpContext();
		mappings = (String[]) toStringArray(properties.get(MAPPINGS2), DEFAULT_MAPPINGS);
		Map<String, String> mt = Maps.newHashMap();
		loadMimeTypes(mt, "mime.types");
		loadMimeTypes(mt, "core_mime.types");
		
		final Map<String, String> mimeTypes = ImmutableMap.copyOf(mt);
		if ( mappings != null && mappings.length > 0 ) {
			for ( String location : mappings ) {
				String[] mapping = StringUtils.split(location,"=",3);
				if ( mapping != null && mapping.length > 1) {
					String fileMimeType = null;
					if ( mapping.length == 3) {
						fileMimeType = mapping[2].trim();
					}
					final String mimeType = fileMimeType;
					final File base = new File(mapping[1].trim());
					LOGGER.info("Registering [{}] [{}] ", mapping[0].trim(), base.getAbsolutePath());
					HttpContext c = new HttpContext() {
						
						public boolean handleSecurity(HttpServletRequest arg0,
								HttpServletResponse arg1) throws IOException {
							return true;
						}
						
						public URL getResource(String path) {
							File f = new File(path);
							if ( f.getAbsolutePath().startsWith(base.getAbsolutePath()) ) {
								if ( f.exists() ) {
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
							if ( mimeType != null ) {
								return mimeType;
							} else {
								int i = fileName.lastIndexOf('.');
								String m = null;
								if ( i > 0 ) {
									String ext = fileName.substring(i+1);
									if ( ext.endsWith("/")) {
										ext = ext.substring(0, ext.length()-1);
									}
									m = mimeTypes.get(ext);
								} else {
									m = defaultContext.getMimeType(fileName);
								}
								return m;
							}
						}
					};
					extHttpService.registerResources(mapping[0].trim(), mapping[1].trim(), c);
				}
			}
		}
	}

	private void loadMimeTypes(Map<String, String> mt, String mimeTypes) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(mimeTypes)));
		for( String s = in.readLine(); s != null; s = in.readLine()) {
			String[] l = new String[] { s };
			int c = s.indexOf("#");
			if ( c == 0 ) {
				continue;
			} else if ( c > 0 ) {
				l = StringUtils.split(s,"#");
			}
			
			String[] p = StringUtils.split(l[0]," ");
			if ( p != null && p.length > 1 ) {
				for ( int i = 1; i < p.length; i++ ) {
					mt.put(p[i], p[0]);
				}
			}
		}
		in.close();
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
