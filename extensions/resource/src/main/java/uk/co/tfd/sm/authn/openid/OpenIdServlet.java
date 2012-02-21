package uk.co.tfd.sm.authn.openid;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

/**
 * This servlet redirects a GET call to an OpenID end authentication end point.
 * @author ieb
 *
 */
@Component(immediate = true, metatype = true)
@Service(value = Servlet.class)
@Properties(value = { @Property(name = "alias", value = "/login/openid") })
public class OpenIdServlet extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6418213387275354179L;
	@Reference
	private OpenIdService openIdService;
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String returnToUrl = request.getParameter("url");
		String userSuppliedString = request.getParameter("t");
		
		String authUrl = openIdService.getAuthRedirectUrl(userSuppliedString, returnToUrl);
		
		if ( authUrl != null ) {
			response.sendRedirect(authUrl);
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
		
	}

}
