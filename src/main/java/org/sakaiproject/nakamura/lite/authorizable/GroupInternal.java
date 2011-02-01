package org.sakaiproject.nakamura.lite.authorizable;

import com.google.common.collect.ImmutableMap;

import org.sakaiproject.nakamura.api.lite.authorizable.Group;

import java.util.Map;

public class GroupInternal extends Group {

    public GroupInternal(Map<String, Object> groupMap, boolean objectIsNew) {
        super(groupMap);
        setObjectNew(objectIsNew);
    }

    public GroupInternal(ImmutableMap<String, Object> groupMap, boolean objectIsNew, boolean readOnly) {
        super(groupMap);
        setObjectNew(objectIsNew);
        setReadOnly(readOnly);
    }


}
