package org.sakaiproject.nakamura.lite.soak.content;

import java.util.Iterator;
import java.util.Map;

import org.sakaiproject.nakamura.api.lite.CacheHolder;
import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalValidatorResolver;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.lite.LoggingStorageListener;
import org.sakaiproject.nakamura.lite.accesscontrol.AccessControlManagerImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.AuthenticatorImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.PrincipalValidatorResolverImpl;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableManagerImpl;
import org.sakaiproject.nakamura.lite.content.ContentManagerImpl;
import org.sakaiproject.nakamura.lite.soak.AbstractScalingClient;
import org.sakaiproject.nakamura.lite.storage.spi.ConcurrentLRUMap;
import org.sakaiproject.nakamura.lite.storage.spi.StorageClientPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentCreateClient extends AbstractScalingClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractScalingClient.class);
    private Map<String, CacheHolder> sharedCache = new ConcurrentLRUMap<String, CacheHolder>(1000);
    private PrincipalValidatorResolver principalValidatorResolver = new PrincipalValidatorResolverImpl();
    private int totalContentItems;
    private Map<String, Object> propertyMap;

    public ContentCreateClient(int totalContentItems, StorageClientPool clientPool,
            Configuration configuration, Map<String, Object> propertyMap)
            throws ClientPoolException, StorageClientException, AccessDeniedException {
        super(clientPool, configuration);
        this.propertyMap = propertyMap;
        this.totalContentItems = totalContentItems;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="DLS_DEAD_LOCAL_STORE",justification="Its a test, so not used.")
    public void run() {
        try {
            super.setup();
            AuthenticatorImpl AuthenticatorImpl = new AuthenticatorImpl(client, configuration, null);
            User currentUser = AuthenticatorImpl.authenticate("admin", "admin");

            AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(
                    client, currentUser, configuration, sharedCache, new LoggingStorageListener(),
                    principalValidatorResolver);

            @SuppressWarnings("unused")
            AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser,
                    null, client, configuration, accessControlManagerImpl, sharedCache,
                    new LoggingStorageListener());

            ContentManagerImpl contentManagerImpl = new ContentManagerImpl(client,
                    accessControlManagerImpl, configuration, sharedCache,
                    new LoggingStorageListener(true));

            String basePath = String.valueOf(System.currentTimeMillis());
            long s = System.currentTimeMillis();
            long s100 = s;
            Content baseContent = new Content(basePath, propertyMap);
            contentManagerImpl.update(baseContent);
            for (int i = 0; i < totalContentItems; i++) {
                Content content = new Content(basePath + "/"  + i, propertyMap);
                contentManagerImpl.update(content);
                if (i > 0 && i % 1000 == 0) {
                    long tn = System.currentTimeMillis();
                    long t = tn - s;
                    long t100 = tn - s100;
                    s100 = tn;
                    LOGGER.info("Created {} items in {}, average {} ms last 1000 {} ms  ", new Object[] { i, t,
                            ((double) t / (double) i), ((double) t100/(double)1000) });
                }
            }
            long t = System.currentTimeMillis() - s;
            LOGGER.info("Created {} items in {}, each item {} ms ", new Object[] {
                    totalContentItems, t, ((double) t / (double) totalContentItems) });
            for (int i = 0; i < totalContentItems; i++) {
                Content content = contentManagerImpl.get(basePath + "/" + i);
                content.setProperty("sling:resourceType", "somethingelse");
                contentManagerImpl.update(content);
                if (i > 0 && i % 1000 == 0) {
                    long tn = System.currentTimeMillis();
                    t = tn - s;
                    long t100 = tn - s100;
                    s100 = tn;
                    LOGGER.info("Updated {} items in {}, average {} ms last 1000 {} ms  ", new Object[] { i, t,
                            ((double) t / (double) i), ((double) t100/(double)1000) });
                }
            }
            t = System.currentTimeMillis() - s;
            LOGGER.info("Updated {} items in {}, each item {} ms ", new Object[] {
                    totalContentItems, t, ((double) t / (double) totalContentItems) });
            
            Content parent = contentManagerImpl.get(basePath);
            s = System.currentTimeMillis();
            Iterable<String> i = parent.listChildPaths();
            t = System.currentTimeMillis();
            LOGGER.info("Getting Child iterable took {} ms ", (t-s));
            s = t;
            Iterator<String> iterator = i.iterator();
            t = System.currentTimeMillis();
            LOGGER.info("Getting Child iterator took {} ms ", (t-s));
            s = t;
            int n = 0;
            while(iterator.hasNext()) {
                @SuppressWarnings("unused")
                String p = iterator.next();
                if ( n == 0 ) {
                    t = System.currentTimeMillis();
                    LOGGER.info("Getting First Child took {} ms ", (t-s));
                    s = t;
                }
                n++;
            }
            t = System.currentTimeMillis();
            LOGGER.info("Getting All Children took {} ms ", (t-s));
        } catch (StorageClientException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (AccessDeniedException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
