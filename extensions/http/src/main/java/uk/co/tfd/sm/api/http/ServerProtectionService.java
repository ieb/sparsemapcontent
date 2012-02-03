package uk.co.tfd.sm.api.http;

import javax.servlet.http.HttpServletRequest;

/**
 * A service to protect the server from rogue requests. It should prevent
 * certain types of request from certain hosts, and may validate the tokens of
 * other parts of the request. It should only use information in the request and
 * not try and resolve the request into a resource. 
 * 
 * @author ieb
 * 
 */
public interface ServerProtectionService {

	public enum Action {
		OK(), FORBID();
	}

	public Action checkAction(HttpServletRequest request);

}
