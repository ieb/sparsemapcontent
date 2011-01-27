package org.sakaiproject.nakamura.lite.authorizable;

import org.sakaiproject.nakamura.api.lite.authorizable.Group;

import java.util.Map;

public class GroupInternal extends Group {

    public GroupInternal(Map<String, Object> groupMap, boolean objectIsNew) {
        super(groupMap);
        setObjectNew(objectIsNew);
    }

}
