package org.sakaiproject.nakamura.lite.accesscontrol;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public class AccessControlledMap<K, V> extends HashMap<K, V> {


    private PropertyAcl propertyAcl;

    public AccessControlledMap(PropertyAcl propertyAcl) {
        this.propertyAcl = propertyAcl;
    }
    /**
     * 
     */
    private static final long serialVersionUID = -6550830558631198709L;

    @Override
    public V put(K key, V value) {
        if ( propertyAcl.canWrite(key)) {
            return super.put(key, value);
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for ( Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }


}
