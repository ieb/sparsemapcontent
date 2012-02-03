package uk.co.tfd.sm.http;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.http.ServerProtectionService;
import uk.co.tfd.sm.api.http.ServerProtectionService.Action;

/**
 * Performs server protection for user content based on the URL alone. Filtering
 * of user content is performed in the place where user content is served from.
 * 
 * @author ieb
 * 
 */
@Component(immediate = true, metatype = true)
@Service(value = Filter.class)
@Properties(value={
		@Property(name="pattern", value="/.*")
})
public class ServerProtectionFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerProtectionFilter.class);
	@Reference
	private ServerProtectionService serverProtectionService;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		Action ac = serverProtectionService
				.checkAction((HttpServletRequest) request);
		switch (ac) {
		case OK:
			LOGGER.debug("OK");
			chain.doFilter(request, response);
			break;
		case FORBID:
			LOGGER.debug("Forbid");
			((HttpServletResponse) response)
					.sendError(HttpServletResponse.SC_FORBIDDEN);
			break;
		}
	}

	@Override
	public void destroy() {
	}

}
