package org.sakaiproject.nakamura.lite;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.ConnectionPoolException;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Authenticator;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.accesscontrol.AuthenticatorImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.CacheHolder;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableActivator;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Component(immediate=true, metatype=true)
@Service(value=Repository.class)
public class RepositoryImpl implements Repository {
  private static final Logger logger = LoggerFactory.getLogger(RepositoryImpl.class);

    @Reference
    protected Configuration configuration;

    @Reference
    protected ConnectionPool connectionPool;

    private Map<String, CacheHolder> sharedCache;

    ThreadLocal<Map<String, Session>> boundSessions = new ThreadLocal<Map<String, Session>>() {
      @Override
      protected Map<String,Session> initialValue() {
        return Maps.newHashMap();
      }
    };

    ThreadLocal<Map<String, Session>> boundAdminSessions = new ThreadLocal<Map<String, Session>>() {
      @Override
      protected Map<String,Session> initialValue() {
        return Maps.newHashMap();
      }
    };

    public RepositoryImpl() {
    }

    @Activate
    public void activate(Map<String, Object> properties) throws ConnectionPoolException, StorageClientException, AccessDeniedException {
        StorageClient client = connectionPool.openConnection();
        AuthorizableActivator authorizableActivator = new AuthorizableActivator(
                client, configuration);
        authorizableActivator.setup();
        connectionPool.closeConnection();
        sharedCache = connectionPool.getSharedCache();
    }

    @Override
    public void logout() throws ConnectionPoolException {
      // clean up any admin sessions
      Map<String, Session> adminSessions = boundAdminSessions.get();
      for (Entry<String, Session> openSession : adminSessions.entrySet()) {
        try {
          openSession.getValue().logout();
        } catch (ConnectionPoolException e) {
          logger.error(e.getMessage(), e);
        }
        adminSessions.remove(openSession.getKey());
      }
      boundAdminSessions.remove();
      boundAdminSessions = null;

      // clean up any user sessions
      Map<String, Session> userSessions = boundSessions.get();
      for (Entry<String, Session> openSession : userSessions.entrySet()) {
        try {
          openSession.getValue().logout();
        } catch (ConnectionPoolException e) {
          logger.error(e.getMessage(), e);
        }
        userSessions.remove(openSession.getKey());
      }
      boundSessions.remove();
      boundSessions = null;
    }

    @Override
    public Session login(String username, String password) throws ConnectionPoolException, StorageClientException, AccessDeniedException {
      Session s = boundSessions.get().get(username);
      if (s == null) {
        StorageClient client = connectionPool.openConnection();
        AuthenticatorImpl authenticatorImpl = new AuthenticatorImpl(client, configuration);
        User currentUser = authenticatorImpl.systemAuthenticate(username);
        if ( currentUser == null ) {
            throw new StorageClientException("User "+username+" does not exist, cant login as this user");
        }
        s = new SessionImpl(this, currentUser, connectionPool, configuration, sharedCache);
        boundSessions.get().put(username, s);
      }
      return s;
    }

    @Override
    public Session login() throws ConnectionPoolException, StorageClientException, AccessDeniedException {
      Session s = boundSessions.get().get(User.ANON_USER);
      if (s == null) {
        StorageClient client = connectionPool.openConnection();
        AuthenticatorImpl authenticatorImpl = new AuthenticatorImpl(client, configuration);
        User currentUser = authenticatorImpl.systemAuthenticate(User.ANON_USER);
        if ( currentUser == null ) {
            throw new StorageClientException("User "+User.ANON_USER+" does not exist, cant login as this user");
        }
        s = new SessionImpl(this, currentUser, connectionPool, configuration, sharedCache);
        boundSessions.get().put(User.ANON_USER, s);
      }
      return s;
    }

    @Override
    public Session loginAdministrative() throws ConnectionPoolException, StorageClientException, AccessDeniedException {
      Session s = boundAdminSessions.get().get(User.ADMIN_USER);
      if (s == null) {
        StorageClient client = connectionPool.openConnection();
        AuthenticatorImpl authenticatorImpl = new AuthenticatorImpl(client, configuration);
        User currentUser = authenticatorImpl.systemAuthenticate(User.ADMIN_USER);
        if ( currentUser == null ) {
            throw new StorageClientException("User "+User.ADMIN_USER+" does not exist, cant login administratively as this user");
        }
        s = new SessionImpl(this, currentUser, connectionPool, configuration, sharedCache);
        boundAdminSessions.get().put(User.ADMIN_USER, s);
      }
      return s;
    }

    @Override
    public Session loginAdministrative(String username) throws ConnectionPoolException, StorageClientException, AccessDeniedException {
      Session s = boundAdminSessions.get().get(username);
      if (s == null) {
        StorageClient client = connectionPool.openConnection();
        AuthenticatorImpl authenticatorImpl = new AuthenticatorImpl(client, configuration);
        User currentUser = authenticatorImpl.systemAuthenticate(username);
        if ( currentUser == null ) {
            throw new StorageClientException("User "+username+" does not exist, cant login administratively as this user");
        }
        s = new SessionImpl(this, currentUser, connectionPool, configuration, sharedCache);
        boundAdminSessions.get().put(username, s);
      }
      return s;
    }


    @Override
    public Authenticator getAuthenticator() throws ConnectionPoolException {
        StorageClient client = connectionPool.openConnection();
        return new AuthenticatorImpl(client, configuration);
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public void unbindSession(Session session) {
        // this only deals with thread locals so is thread safe.
        List<String> toRemove = Lists.newArrayList();
        Map<String, Session> n = boundAdminSessions.get();
        Map<String, Session> a = boundAdminSessions.get();
        for ( Entry<String, Session> e:  n.entrySet() ) {
            if ( session.equals(e.getValue())) {
                toRemove.add(e.getKey());
            }
        }
        for ( Entry<String, Session> e: a.entrySet()) {
            if ( session.equals(e.getValue())) {
                toRemove.add(e.getKey());
            }
        }
        for ( String r : toRemove) {
            n.remove(r);
            a.remove(r);
        }
    }


}
