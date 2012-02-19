package org.sakaiproject.nakamura.lite.authorizable;

import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.User;

import java.util.Map;

public class UserInternal extends User {

    public UserInternal(Map<String, Object> groupMap, Session session, boolean objectIsNew)
            throws StorageClientException, AccessDeniedException {
        super(groupMap, session);
        setObjectNew(objectIsNew);
    }

}
