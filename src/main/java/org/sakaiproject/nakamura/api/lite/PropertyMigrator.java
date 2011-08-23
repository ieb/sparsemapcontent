package org.sakaiproject.nakamura.api.lite;

import java.util.Map;

/**
 * Implementations of PropertyMigrators registered with OSGi are called by the
 * MigrateContentComponent, when its activated (normally disabled). All
 * registered implementation will be called, once for each Map within the
 * system. If they determine that the map is of the appropriate type and needs
 * modification, they should modify it, and return true. If not they should
 * leave the map untouched. There is no guarantee in what order each migrator
 * might be called. If any PropertyMigrator modifies a set of properties, the
 * map will be resaved under the same key. If no properties are modified by any
 * PropertyMigrators, then the object will be re-indexed with the current index
 * operation. Un filtered access is given to all properties, so anyone
 * implementing this interface must take great care not to break referential
 * integrity of each object or invalidate the internals of the object.
 * 
 * The MigrateContentComponent is not active by default, and should only be made
 * active by an Administrator using the Web UI.
 * 
 * The migrate methods will be called once for every object within the system.
 * (could be billions of times).
 * 
 * @author ieb
 * 
 */
public interface PropertyMigrator {

    /**
     * @param rid
     *            the row id of the current object as loaded from the store. If
     *            the property representing the key for the type of object is
     *            changed, this object will be saved under a new rowid. The
     *            calculation of the rowid depends on the storage implementation
     *            and the value of the key.
     * @param properties
     *            a map of properties. Implementations are expected to modify
     *            this map, and return true if modifications are made.
     * @return true if any modifications were made to properties, false
     *         otherwise.
     */
    boolean migrate(String rid, Map<String, Object> properties);

}
