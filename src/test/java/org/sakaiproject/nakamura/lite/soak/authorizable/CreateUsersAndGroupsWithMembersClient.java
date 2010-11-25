package org.sakaiproject.nakamura.lite.soak.authorizable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sakaiproject.nakamura.api.lite.ClientPoolException;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Group;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.lite.accesscontrol.AccessControlManagerImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.AuthenticatorImpl;
import org.sakaiproject.nakamura.lite.accesscontrol.CacheHolder;
import org.sakaiproject.nakamura.lite.authorizable.AuthorizableManagerImpl;
import org.sakaiproject.nakamura.lite.soak.AbstractScalingClient;
import org.sakaiproject.nakamura.lite.storage.StorageClientPool;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class CreateUsersAndGroupsWithMembersClient extends AbstractScalingClient {

    private int nusers;
    private int ngroups;
    private Map<String, CacheHolder> sharedCache = new ConcurrentHashMap<String, CacheHolder>(1000);

    public CreateUsersAndGroupsWithMembersClient(int totalUsers,  int totalGroups, StorageClientPool connectionPool) throws ClientPoolException,
            StorageClientException, AccessDeniedException {
        super(connectionPool);
        nusers = totalUsers;
        ngroups = totalGroups;
    }

    @Override
    public void run() {
        try {
            super.setup();
            String tname = String.valueOf(Thread.currentThread().getId())
                    + String.valueOf(System.currentTimeMillis());
            AuthenticatorImpl AuthenticatorImpl = new AuthenticatorImpl(client, configuration);
            User currentUser = AuthenticatorImpl.authenticate("admin", "admin");

            AccessControlManagerImpl accessControlManagerImpl = new AccessControlManagerImpl(
                    client, currentUser, configuration, sharedCache);

            AuthorizableManagerImpl authorizableManager = new AuthorizableManagerImpl(currentUser,
                    client, configuration, accessControlManagerImpl);
            
            List<String> userNames = Lists.newArrayList();
            List<String> groupNames = Lists.newArrayList();
            
            for (int i = 0; i < nusers; i++) {
                userNames.add("u"+tname + "_" + i);
            }
            for (int i = 0; i < ngroups; i++) {
                userNames.add("g"+tname + "_" + i);
            }
            for ( String userId : userNames) {
                authorizableManager.createUser(userId, userId, "test", ImmutableMap.of(userId,
                        (Object) "testvalue"));
            }
            int mu = 0;
            for ( String groupId : groupNames) {
                authorizableManager.createGroup(groupId, groupId, ImmutableMap.of(groupId,
                        (Object) "testvalue"));
                Group group = (Group) authorizableManager.findAuthorizable(groupId);
                for ( int i = 0; i < 4; i++) {
                    group.addMember(userNames.get(mu));
                    mu++;
                    if ( mu >= userNames.size()) {
                        mu = 0;
                    }
                }                
            }

        } catch (StorageClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (AccessDeniedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
