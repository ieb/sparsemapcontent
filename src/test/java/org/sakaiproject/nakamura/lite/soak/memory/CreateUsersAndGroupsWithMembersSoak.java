package org.sakaiproject.nakamura.lite.soak.memory;

import org.sakaiproject.nakamura.api.lite.ConnectionPoolException;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.lite.content.BlockContentHelper;
import org.sakaiproject.nakamura.lite.soak.AbstractSoakController;
import org.sakaiproject.nakamura.lite.soak.authorizable.CreateUsersAndGroupsWithMembersClient;
import org.sakaiproject.nakamura.lite.storage.ConnectionPool;
import org.sakaiproject.nakamura.lite.storage.mem.MemoryStorageClientConnectionPool;

import com.google.common.collect.ImmutableMap;

public class CreateUsersAndGroupsWithMembersSoak extends AbstractSoakController {

    private int totalUsers;
    private ConnectionPool connectionPool;
    private int totalGroups;

    public CreateUsersAndGroupsWithMembersSoak(int totalUsers, int totalGroups, ConnectionPool connectionPool) {
        super(totalUsers+(totalGroups*5));
        this.connectionPool = connectionPool;
        this.totalUsers = totalUsers;
        this.totalGroups = totalGroups;
    }


    protected Runnable getRunnable(int nthreads) throws ConnectionPoolException,
            StorageClientException, AccessDeniedException {
        int usersPerThread = totalUsers / nthreads;
        int groupsPerThread = totalGroups / nthreads;
        return new CreateUsersAndGroupsWithMembersClient(usersPerThread, groupsPerThread, connectionPool);
    }

    public static void main(String[] argv) throws ConnectionPoolException, StorageClientException,
            AccessDeniedException, ClassNotFoundException {

        int totalUsers = 1000;
        int totalGroups = 100;
        int nthreads = 10;

        if (argv.length > 0) {
            nthreads = StorageClientUtils.getSetting(Integer.valueOf(argv[0]), nthreads);
        }
        if (argv.length > 1) {
            totalUsers = StorageClientUtils.getSetting(Integer.valueOf(argv[1]), totalUsers);
        }
        if (argv.length > 2) {
            totalGroups = StorageClientUtils.getSetting(Integer.valueOf(argv[2]), totalUsers);
        }

        CreateUsersAndGroupsWithMembersSoak createUsersAndGroupsSoak = new CreateUsersAndGroupsWithMembersSoak(
                totalUsers, totalGroups, getConnectionPool());
        createUsersAndGroupsSoak.launchSoak(nthreads);
    }
    
    protected static ConnectionPool getConnectionPool() throws ClassNotFoundException {
        MemoryStorageClientConnectionPool cp = new MemoryStorageClientConnectionPool();
        cp.activate(ImmutableMap.of("test", (Object) "test",
                BlockContentHelper.CONFIG_MAX_CHUNKS_PER_BLOCK, 9));
        return cp;
    }

}
