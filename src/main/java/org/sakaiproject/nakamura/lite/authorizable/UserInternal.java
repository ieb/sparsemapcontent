package org.sakaiproject.nakamura.lite.authorizable;

import org.sakaiproject.nakamura.api.lite.authorizable.User;

import java.util.Map;

public class UserInternal extends User {

    public UserInternal(Map<String, Object> groupMap, boolean objectIsNew) {
        super(groupMap);
        setObjectNew(objectIsNew);
    }

}
