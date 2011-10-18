package uk.co.tfd.sm.http.batch;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Iterators;

public class RequestWrapper extends HttpServletRequestWrapper {

	private Map<String, String[]> parameters;
	private String method;
	private String url;
	private String path;
	private String query;

	public RequestWrapper(HttpServletRequest request, RequestInfo requestInfo) {
		super(request);
		this.parameters = requestInfo.getParameters();
		this.method = requestInfo.getMethod();
		if (method == null) {
			method = "GET";
		}
		this.url = requestInfo.getUrl();
		String[] parts = StringUtils.split(url,"?",2);
	
		if ( parts.length == 0 ) {
			path = null;
			query = null;
		} else if ( parts.length == 1) {
			path = parts[0];
			query = null;
		} else {
			path = parts[0];
			query = parts[1];
		}
	}

	//
	// Sling Request parameters
	//

	@Override
	public String getParameter(String name) {
		String[] param = parameters.get(name);
		if (param != null && param.length > 0) {
			return param[0];
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getParameterMap() {
		return parameters;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getParameterNames() {
		return Iterators.asEnumeration(parameters.keySet().iterator());
	}

	@Override
	public String[] getParameterValues(String name) {
		return parameters.get(name);
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public String getPathInfo() {
		return path;
	}

	@Override
	public String getPathTranslated() {
		return path;
	}

	@Override
	public String getQueryString() {
		return query;
	}

	@Override
	public String getServletPath() {
		return path;
	}

	@Override
	public String getRequestURI() {
		return path;
	}
	
}
