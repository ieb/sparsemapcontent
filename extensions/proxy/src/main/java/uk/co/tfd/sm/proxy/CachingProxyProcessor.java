package uk.co.tfd.sm.proxy;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

public interface CachingProxyProcessor {

	boolean sendCached(Map<String, Object> config,
			Map<String, Object> templateParams, HttpServletResponse response) throws IOException;

}
