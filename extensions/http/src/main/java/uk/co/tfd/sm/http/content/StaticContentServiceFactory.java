package uk.co.tfd.sm.http.content;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

@Component(immediate = true, metatype = true)
public class StaticContentServiceFactory {


	private static final String[] DEFAULT_MAPPINGS = new String[] {
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
			"/system/me = static/me.json",
			"/tags = static/tags"

	};

	@Property(value = { 
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
			"/system/me = static/me.json",
			"/tags = static/tags"
			})
	private static final String MAPPINGS = "mappings";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(StaticContentServlet.class);

	private Map<String, String> mimeTypes;

	private Map<Servlet, ServiceRegistration> servlets = Maps.newHashMap();

	@Activate
	public void activate(ComponentContext componentContext)
			throws NamespaceException, IOException, ServletException {
		BundleContext bundleContext = componentContext.getBundleContext();
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> properties = componentContext
				.getProperties();
		String[] mappings = (String[]) toStringArray(properties.get(MAPPINGS),
				DEFAULT_MAPPINGS);
		Map<String, String> mt = Maps.newHashMap();
		loadMimeTypes(mt, "mime.types");
		loadMimeTypes(mt, "core_mime.types");

		mimeTypes = ImmutableMap.copyOf(mt);
		servlets.clear();
		if (mappings != null && mappings.length > 0) {

			for (String location : mappings) {
				String[] mapping = StringUtils.split(location, "=", 3);
				String alias = mapping[0].trim();
				String path = mapping[1].trim();
				StaticContentServlet contentServlet = new StaticContentServlet(
						alias, path, mimeTypes);

				Dictionary<String, String> props = new Hashtable<String, String>();
				props.put("alias", alias);
				servlets.put(contentServlet, bundleContext.registerService(
						Servlet.class.getName(), contentServlet, props));
				LOGGER.debug("Registering {} as {} {} ", new Object[] {
						contentServlet, alias, path });
			}
		}
	}

	@Deactivate
	public void deactivate(ComponentContext componentContext) {
		for (Entry<Servlet, ServiceRegistration> e : servlets.entrySet()) {
			try {
				e.getValue().unregister();
			} catch (Exception ex) {
				LOGGER.debug(ex.getMessage(), ex);
			}
		}
	}

	private void loadMimeTypes(Map<String, String> mt, String mimeTypes)
			throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(getClass()
				.getResourceAsStream(mimeTypes)));
		for (String s = in.readLine(); s != null; s = in.readLine()) {
			String[] l = new String[] { s };
			int c = s.indexOf("#");
			if (c == 0) {
				continue;
			} else if (c > 0) {
				l = StringUtils.split(s, "#");
			}

			String[] p = StringUtils.split(l[0], " ");
			if (p != null && p.length > 1) {
				for (int i = 1; i < p.length; i++) {
					mt.put(p[i], p[0]);
				}
			}
		}
		in.close();
	}

	private String[] toStringArray(Object object, String[] defaultValue) {
		if (object == null) {
			return defaultValue;
		}
		if (object instanceof String[]) {
			return (String[]) object;
		}
		return StringUtils.split(String.valueOf(object), ",");
	}


}
