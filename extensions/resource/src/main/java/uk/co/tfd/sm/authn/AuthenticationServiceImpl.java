package uk.co.tfd.sm.authn;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;

import uk.co.tfd.sm.api.authn.AuthenticationService;
import uk.co.tfd.sm.api.authn.AuthenticationServiceCredentials;
import uk.co.tfd.sm.api.authn.AuthenticationServiceHandler;
import uk.co.tfd.sm.authn.token.TokenAuthenticationService;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

@Component(immediate = true, metatype = true)
@Service(value = AuthenticationService.class)
@Reference(name = "authenticationHandler", referenceInterface = AuthenticationServiceHandler.class, bind = "bind", unbind = "unbind", cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC, strategy = ReferenceStrategy.EVENT)
public class AuthenticationServiceImpl implements AuthenticationService {

	private static final Set<String> TRUSTED = ImmutableSet.of(
			TrustedCredentials.class.getName(),
			TransferCredentials.class.getName());
	private static final Set<String> SSO = ImmutableSet
			.of(TrustedCredentials.class.getName());
	@Reference
	protected Repository repository;

	@Reference
	private TokenAuthenticationService tokenAuthenticationService;

	private AuthenticationServiceHandler[] authenticationServiceHandlers = new AuthenticationServiceHandler[0];
	private Set<AuthenticationServiceHandler> handlers = Sets.newHashSet();

	public AuthenticationServiceImpl() {
	}

	public AuthenticationServiceImpl(Repository repository) {
		if (repository == null) {
			throw new IllegalArgumentException("Repository cant be null");
		}
		this.repository = repository;
	}

	@Override
	public Session authenticate(HttpServletRequest request,
			HttpServletResponse response) throws StorageClientException {
		try {
			AuthenticationServiceHandler[] hs = authenticationServiceHandlers;
			for (AuthenticationServiceHandler h : hs) {
				AuthenticationServiceCredentials c = h.getCredentials(request);
				if (c != null) {
					Session s = null;
					if (isTrusted(c)) {
						s = repository.loginAdministrative(c.getUserName());
					} else {
						s = repository.login(c.getUserName(), c.getPassword());
					}
					if (isSso(c)) {
						tokenAuthenticationService.refreshCredentials(c,
								request, response);
					}
					return s;
				}
			}
			return repository.login();
		} catch (AccessDeniedException e) {
			try {
				return repository.login();
			} catch (AccessDeniedException e2) {
				throw new StorageClientException("Unable to login as anon ", e2);
			}
		}
	}

	private boolean isSso(AuthenticationServiceCredentials c) {
		return SSO.contains(c.getClass().getName());
	}

	private boolean isTrusted(AuthenticationServiceCredentials c) {
		return TRUSTED.contains(c.getClass().getName());
	}

	protected synchronized void bind(AuthenticationServiceHandler handler) {
		handlers.add(handler);
		authenticationServiceHandlers = handlers
				.toArray(new AuthenticationServiceHandler[handlers.size()]);

	}

	protected synchronized void unbind(AuthenticationServiceHandler handler) {
		handlers.add(handler);
		authenticationServiceHandlers = handlers
				.toArray(new AuthenticationServiceHandler[handlers.size()]);
	}

}
