package uk.co.tfd.sm.jetty;

import org.apache.felix.http.api.ExtHttpService;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * This class that operates as a managed service.
 */
@Component(enabled=false, immediate=true, metatype=true)
@Service(value=Filter.class)
@Properties(value={
		@Property(name="bufferSize", intValue=8192),
		@Property(name="minGzipSize", intValue=8192),
		@Property(name="mimeTypes", value="text/html,text/plain,text/css,text/javascript,text/xml,application/xml,application/xhtml+xml,application/rss+xml,application/javascript,application/x-javascript,application/json"), 
		@Property(name="excludedAgents", value="") 
		})
public class OSGiGZipFilter extends GzipFilter {

  private static final String DEFAULT_USER_AGENT = "(?:Mozilla[^\\(]*\\(compatible;\\s*+([^;]*);.*)|(?:.*?([^\\s]+/[^\\s]+).*)";
	
  @SuppressWarnings("unused")
@Property(value=DEFAULT_USER_AGENT)
  private static final String PROP_USER_AGENT = "userAgent";
	
  @Reference
  protected ExtHttpService extHttpService;

  @SuppressWarnings("rawtypes")
  @Activate
  public void activate(Map<String, Object> properties) throws ServletException {
    Hashtable<String, Object> props = new Hashtable<String, Object>();
    props.putAll(properties);
    extHttpService.registerFilter(this, ".*", (Dictionary) properties, 100, null);

  }

  @Override
  public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
      throws IOException, ServletException {
    super.doFilter(arg0, arg1, arg2);
  }

  @Deactivate
  public void deactivate(Map<String, Object> properties) {
    extHttpService.unregisterFilter(this);
  }

}
