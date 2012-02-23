package uk.co.tfd.sm.authn;

import java.util.Arrays;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.tfd.sm.api.authn.AuthenticationService;
import uk.co.tfd.sm.api.authn.AuthenticationServiceCredentials;
import uk.co.tfd.sm.api.authn.AuthenticationServiceHandler;
import uk.co.tfd.sm.authn.openid.OpenIdCredentials;
import uk.co.tfd.sm.authn.token.TokenAuthenticationService;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

@Component(immediate = true, metatype = true)
@Service(value = AuthenticationService.class)
@Reference(name = "authenticationHandler", referenceInterface = AuthenticationServiceHandler.class, bind = "bind", unbind = "unbind", cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, policy = ReferencePolicy.DYNAMIC, strategy = ReferenceStrategy.EVENT)
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Set<String> TRUSTED = ImmutableSet.of(TrustedCredentials.class.getName(),
            TransferCredentials.class.getName(), OpenIdCredentials.class.getName());
    private static final Set<String> REFRESH = ImmutableSet.of(TrustedCredentials.class.getName(),
            OpenIdCredentials.class.getName());
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

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
    public Session authenticate(HttpServletRequest request, HttpServletResponse response) throws StorageClientException {
        try {
            AuthenticationServiceHandler[] hs = authenticationServiceHandlers;

            for (AuthenticationServiceHandler h : hs) {
                LOGGER.debug("Trying {} ", h);
                AuthenticationServiceCredentials c = h.getCredentials(request);
                if (c != null) {
                    Session s = null;
                    if (isTrusted(c)) {
                        if (c.allowCreate()) {
                            Session adminSession = repository.loginAdministrative();
                            adminSession.getAuthorizableManager().createUser(c.getUserName(), c.getUserName(), null,
                                    c.getProperties());
                            adminSession.logout();
                        }
                        s = repository.loginAdministrative(c.getUserName());
                    } else {
                        s = repository.login(c.getUserName(), c.getPassword());
                    }
                    if (isRefresh(c)) {
                        tokenAuthenticationService.refreshCredentials(c, request, response);
                    }
                    LOGGER.info("Logged in as {} ", s.getUserId());
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

    private boolean isRefresh(AuthenticationServiceCredentials c) {
        return REFRESH.contains(c.getClass().getName());
    }

    private boolean isTrusted(AuthenticationServiceCredentials c) {
        return TRUSTED.contains(c.getClass().getName());
    }

    protected synchronized void bind(AuthenticationServiceHandler handler) {
        handlers.add(handler);
        AuthenticationServiceHandler[] newHandlers = handlers.toArray(new AuthenticationServiceHandler[handlers.size()]);
        Arrays.sort(newHandlers);
        authenticationServiceHandlers = newHandlers;

    }

    protected synchronized void unbind(AuthenticationServiceHandler handler) {
        handlers.add(handler);
        AuthenticationServiceHandler[] newHandlers = handlers.toArray(new AuthenticationServiceHandler[handlers.size()]);
        Arrays.sort(newHandlers);
        authenticationServiceHandlers = newHandlers;
    }

}
