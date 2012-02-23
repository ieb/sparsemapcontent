package uk.co.tfd.sm.authn.openid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.SparseSessionTracker;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.authn.AuthenticationService;

/**
 * This servlet redirects a GET call to an OpenID end authentication end point
 * and receives the response back before redirecting to the desired destination.
 * 
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
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdServlet.class);
    @Reference
    private OpenIdService openIdService;
    @Reference
    protected SparseSessionTracker sessionTracker;
    @Reference
    protected AuthenticationService authenticationService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doOpenIdRedirect(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doOpenIdRedirect(request, response);
    }

    public void doOpenIdRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestUrl = request.getRequestURL().toString();
        int i = requestUrl.indexOf("/login/openid/");
        if (i < 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String hostPart = requestUrl.substring(0, i);
        String openIdProvider = requestUrl.substring(i + "/login/openid/".length());
        if (openIdProvider.equals("verify")) {
            try {
                Session session = sessionTracker.register(authenticationService.authenticate(request, response), request);
                LOGGER.info("Authenticated as {} ", session.getUserId());

                // this is a verify request, extract the final destination and
                // redirect
                String finalUrl = request.getParameter("eurl");
                GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(Base64.decodeBase64(finalUrl)));
                String targetUrl = IOUtils.toString(gz, "UTF-8");
                LOGGER.info("Got verified as {} going to {}  ", session.getUserId(), targetUrl);
                response.sendRedirect(targetUrl);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } finally {
            }
        } else {
            String returnToUrl = request.getParameter("url");
            if (returnToUrl == null) {
                returnToUrl = "/";
            }
            if (!returnToUrl.startsWith("/")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                GZIPOutputStream gz = new GZIPOutputStream(baos);
                gz.write(returnToUrl.getBytes("UTF-8"));
                gz.finish();
                String encUrl = Base64.encodeBase64URLSafeString(baos.toByteArray());
                String openIdVerifyUrl = hostPart + "/login/openid/verify?eurl=" + encUrl;

                String authUrl = openIdService.getAuthRedirectUrl(openIdProvider, openIdVerifyUrl);

                if (authUrl != null) {
                    LOGGER.info("Open ID Auth Going to  {} {}  ", hostPart, authUrl);
                    response.sendRedirect(authUrl);
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        }

    }

}
