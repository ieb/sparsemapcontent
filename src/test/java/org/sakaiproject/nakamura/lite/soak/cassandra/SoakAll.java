package org.sakaiproject.nakamura.lite.soak.cassandra;

import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.lite.storage.ConnectionPoolException;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;

public class SoakAll {

    public static void main(String[] argv) throws ConnectionPoolException, StorageClientException, AccessDeniedException, ClassNotFoundException {
        CreateUsersAndGroupsSoak.main(argv);
        CreateUsersAndGroupsWithMembersSoak.main(argv);
    }
}
