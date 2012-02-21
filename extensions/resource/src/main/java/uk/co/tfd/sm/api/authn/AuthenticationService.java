package uk.co.tfd.sm.api.authn;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;

public interface AuthenticationService {

	Session authenticate(HttpServletRequest request,
			HttpServletResponse response) throws StorageClientException;

}
