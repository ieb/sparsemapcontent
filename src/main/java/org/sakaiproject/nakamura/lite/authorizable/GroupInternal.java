package org.sakaiproject.nakamura.lite.authorizable;

import com.google.common.collect.ImmutableMap;

import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.authorizable.Group;

import java.util.Map;

public class GroupInternal extends Group {

    public GroupInternal(Map<String, Object> groupMap, Session session, boolean objectIsNew)
            throws StorageClientException, AccessDeniedException {
        super(groupMap, session);
        setObjectNew(objectIsNew);
    }

    public GroupInternal(ImmutableMap<String, Object> groupMap, Session session,
            boolean objectIsNew, boolean readOnly) throws StorageClientException,
            AccessDeniedException {
        super(groupMap, session);
        setObjectNew(objectIsNew);
        setReadOnly(readOnly);
    }


}
