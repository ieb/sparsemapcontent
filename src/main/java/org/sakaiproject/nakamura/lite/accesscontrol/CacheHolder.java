package org.sakaiproject.nakamura.lite.accesscontrol;

import java.util.Map;

public class CacheHolder {

    private Map<String, Object> o;

    public CacheHolder(Map<String, Object> o) {
        this.o = o;
    }

    public Map<String, Object> get() {
        return o;
    }

}
