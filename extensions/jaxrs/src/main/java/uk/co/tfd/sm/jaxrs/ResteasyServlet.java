package uk.co.tfd.sm.jaxrs;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.plugins.server.servlet.HttpRequestFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpResponseFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpServletInputMessage;
import org.jboss.resteasy.plugins.server.servlet.HttpServletResponseWrapper;
import org.jboss.resteasy.plugins.server.servlet.ServletBootstrap;
import org.jboss.resteasy.plugins.server.servlet.ServletContainerDispatcher;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.jaxrs.JaxRestService;

import com.google.common.collect.Sets;

@Component(immediate = true, metatype = true)
@Service(value = Servlet.class)
@Properties(value = { @Property(name = "alias", value = "/") })
@References(value = { @Reference(name = "services", referenceInterface=JaxRestService.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC, strategy = ReferenceStrategy.EVENT, bind = "bindService", unbind = "unbindService") })
public class ResteasyServlet extends HttpServlet implements HttpRequestFactory,
		HttpResponseFactory {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3623498533852144726L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ResteasyServlet.class);
	protected ServletContainerDispatcher servletContainerDispatcher;
	private Set<JaxRestService> pendingServices = Sets.newHashSet();
	private Object registrationSync = new Object();

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher#init(javax.servlet.ServletConfig)
	 */

	public Dispatcher getDispatcher() {
		return servletContainerDispatcher.getDispatcher();
	}

	public Registry getRegistry() {
		return servletContainerDispatcher.getDispatcher().getRegistry();
	}
	
	@Activate
	public void activate(Map<String, Object> properties) {
		
	}
	
	@Deactivate
	public void deactivate(Map<String, Object> properties ) {
		
	}

	public void init(ServletConfig servletConfig) throws ServletException {
		synchronized (registrationSync) {
			ClassLoader bundleClassloader = this.getClass().getClassLoader();
			ClassLoader contextClassloader = Thread.currentThread()
					.getContextClassLoader();
			try {
				Thread.currentThread().setContextClassLoader(bundleClassloader);
				super.init(servletConfig);
				ServletBootstrap bootstrap = new ServletBootstrap(servletConfig);
				servletContainerDispatcher = new ServletContainerDispatcher();
				servletContainerDispatcher.init(
						servletConfig.getServletContext(), bootstrap, this,
						this);
				servletContainerDispatcher.getDispatcher()
						.getDefaultContextObjects()
						.put(ServletConfig.class, servletConfig);

			} finally {
				Thread.currentThread()
						.setContextClassLoader(contextClassloader);
			}
			Registry registry = getRegistry();
			for (JaxRestService service : pendingServices) {
				LOGGER.info("Registering JaxRestService {} ",service);
				registry.addSingletonResource(service);
			}
			pendingServices.clear();
		}

	}

	@Override
	public void destroy() {
		synchronized (registrationSync) {
			super.destroy();
			LOGGER.info("Removing all JaxRestServices ");
			servletContainerDispatcher.destroy();
			servletContainerDispatcher = null;
		}
	}

	protected void bindService(JaxRestService service) {
		synchronized (registrationSync) {
			if (servletContainerDispatcher == null) {
				pendingServices.add(service);
			} else {
				LOGGER.info("Registering JaxRestService {} ",service);
				getRegistry().addSingletonResource(service);
			}
		}
	}

	protected void unbindService(JaxRestService service) {
		synchronized (registrationSync) {
			if (servletContainerDispatcher == null) {
				pendingServices.remove(service);
			} else {
				LOGGER.info("Removing JaxRestService {} ",service);
				getRegistry().removeRegistrations(service.getClass());
			}
		}
	}

	protected void service(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws ServletException,
			IOException {
		service(httpServletRequest.getMethod(), httpServletRequest,
				httpServletResponse);
	}

	public void service(String httpMethod, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		servletContainerDispatcher.service(httpMethod, request, response, true);
	}

	public HttpRequest createResteasyHttpRequest(String httpMethod,
			HttpServletRequest request, HttpHeaders headers,
			UriInfoImpl uriInfo, HttpResponse theResponse,
			HttpServletResponse response) {
		return createHttpRequest(httpMethod, request, headers, uriInfo,
				theResponse, response);
	}

	public HttpResponse createResteasyHttpResponse(HttpServletResponse response) {
		return createServletResponse(response);
	}

	protected HttpRequest createHttpRequest(String httpMethod,
			HttpServletRequest request, HttpHeaders headers,
			UriInfoImpl uriInfo, HttpResponse theResponse,
			HttpServletResponse response) {
		return new HttpServletInputMessage(request, theResponse, headers,
				uriInfo, httpMethod.toUpperCase(),
				(SynchronousDispatcher) getDispatcher());
	}

	protected HttpResponse createServletResponse(HttpServletResponse response) {
		return new HttpServletResponseWrapper(response, getDispatcher()
				.getProviderFactory());
	}

}
