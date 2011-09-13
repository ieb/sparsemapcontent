package uk.co.tfd.sm.wedav;

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
import org.sakaiproject.nakamura.lite.RepositoryImpl;

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

@Component(immediate=true, metatype=true)
@Service(value=Servlet.class)
@Properties(value={
		@Property(name="alias", value="/dav")
})
public class MiltonServlet extends HttpServlet {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7451452381693049651L;
	private HttpManager httpManager;
	
	
	@Reference
	private RepositoryImpl repository;
	
	
	@Activate
	public void activate(Map<String, Object> properties) {
		ResourceFactory resourceFactory = new MiltonContentResourceFactory();
		SecurityManager securityManager = new MiltonSecurityManager(repository);
		httpManager = new HttpManager(resourceFactory);
		Http11ResponseHandler  responseHandler = httpManager.getResponseHandler();
		Filter authenticationFilter = new PreAuthenticationFilter(responseHandler, securityManager);
		httpManager.addFilter(0, authenticationFilter);		
	}
	
	@Deactivate
	public void deactivate(Map<String, Object> properties) {
	}
	
	
	
	@Override
	protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
			throws ServletException, IOException {
		 try {
	            Request request = new ServletRequest( servletRequest );
	            Response response = new ServletResponse( servletResponse );
	            httpManager.process( request, response );
	        } finally {
	            servletResponse.getOutputStream().flush();
	            servletResponse.flushBuffer();
	        }	
	    }

}
