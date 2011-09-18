package org.sakaiproject.nakamura.lite.storage.jdbc.migrate;

import org.sakaiproject.nakamura.api.lite.PropertyMigrator;

/**
 * An internal API so that the MigrateContentComponent can exist disabled, but
 * when it starts bind to the PropertyMigratorTrackerService which is enabled
 * and will hold all the PropertyMigrators in the system.
 * 
 * @author ieb
 * 
 */
public interface PropertyMigratorTracker {

    PropertyMigrator[] getPropertyMigrators();

}
