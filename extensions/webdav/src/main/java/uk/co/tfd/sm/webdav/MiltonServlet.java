package uk.co.tfd.sm.webdav;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.Repository;

import com.bradmcevoy.http.Filter;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.Response;
import com.bradmcevoy.http.SecurityManager;
import com.bradmcevoy.http.ServletRequest;
import com.bradmcevoy.http.ServletResponse;
import com.bradmcevoy.http.http11.Http11ResponseHandler;
import com.bradmcevoy.http.http11.auth.PreAuthenticationFilter;
import com.bradmcevoy.http.http11.auth.SecurityManagerBasicAuthHandler;
import com.bradmcevoy.http.http11.auth.SecurityManagerDigestAuthenticationHandler;
import com.google.common.collect.Lists;

@Component(immediate = true, metatype = true)
@Service(value = Servlet.class)
@Properties(value = { @Property(name = "alias", value = "/dav") })
public class MiltonServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7451452381693049651L;
	private HttpManager httpManager;

	@Reference
	private Repository repository;

	@Activate
	public void activate(Map<String, Object> properties) {
		String basePath = (String) properties.get("alias");
		if ( basePath == null ) {
			basePath = "/dav";
		}
		ResourceFactory resourceFactory = new MiltonContentResourceFactory(basePath);
		SecurityManager securityManager = new MiltonSecurityManager(repository);
		httpManager = new HttpManager(resourceFactory);
		Http11ResponseHandler responseHandler = httpManager
				.getResponseHandler();

		Filter authenticationFilter = new PreAuthenticationFilter(
				responseHandler, Lists.immutableList(
						new SecurityManagerBasicAuthHandler(securityManager),
						new SecurityManagerDigestAuthenticationHandler(
								securityManager), new AnonAuthHandler(
								securityManager)));
		httpManager.addFilter(0, authenticationFilter);
	}

	@Deactivate
	public void deactivate(Map<String, Object> properties) {
	}

	@Override
	protected void service(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) throws ServletException,
			IOException {
		try {
			Request request = new ServletRequest(servletRequest);
			Response response = new ServletResponse(servletResponse);
			httpManager.process(request, response);
		} finally {
			servletResponse.getOutputStream().flush();
			servletResponse.flushBuffer();
		}
	}

}
