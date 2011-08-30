package org.sakaiproject.nakamura.lite.accesscontrol;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class PropertyAcl  implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3998584870894631478L;
    private Set<String> readDenied;
    private Set<String> writeDenied;

    public PropertyAcl(Map<String, Integer> denies) {
        Set<String> r = Sets.newHashSet();
        Set<String> w = Sets.newHashSet();
        for (Entry<String, Integer> ace : denies.entrySet()) {
            if ((Permissions.CAN_READ_PROPERTY.getPermission() & ace.getValue()) == Permissions.CAN_READ_PROPERTY
                    .getPermission()) {
                r.add(ace.getKey());
            }
            if ((Permissions.CAN_WRITE_PROPERTY.getPermission() & ace.getValue()) == Permissions.CAN_WRITE_PROPERTY
                    .getPermission()) {
                w.add(ace.getKey());
            }
        }
        readDenied = ImmutableSet.of(r.toArray(new String[r.size()]));
        writeDenied = ImmutableSet.of(w.toArray(new String[w.size()]));
    }

    public PropertyAcl() {
        readDenied = ImmutableSet.of();
        writeDenied = ImmutableSet.of();
    }

    public Set<String> readDeniedSet() {
        return readDenied;
    }

    public boolean canWrite(Object key) {
        return !writeDenied.contains(key);
    }

}
