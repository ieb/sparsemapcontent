package org.sakaiproject.nakamura.lite.storage.jdbc.migrate;

import org.sakaiproject.nakamura.api.lite.PropertyMigrator;

public interface PropertyMigratorTracker {

    PropertyMigrator[] getPropertyMigrators();

}
