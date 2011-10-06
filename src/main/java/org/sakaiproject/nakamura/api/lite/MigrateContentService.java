package org.sakaiproject.nakamura.api.lite;

import java.io.IOException;

import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;

public interface MigrateContentService {

    /**
     * @param dryRun
     *            dry run the migration
     * @param limit
     *            if dry running, limit the number
     * @param reindexAll
     *            if try reindex all
     * @param feedback
     *            a logger to provide feedback to. If you want to control the
     *            Migration, implement your own logger and throw a
     *            RuntimeException from the info or debug methods to stop the
     *            migrator in the case of an emergency.
     * @throws ClientPoolException
     * @throws StorageClientException
     * @throws AccessDeniedException
     * @throws IOException
     * @throws PropertyMigrationException thrown if there are unresolved dependencies.
     */
    void migrate(boolean dryRun, int limit, boolean reindexAll, Feedback feedback)
            throws ClientPoolException, StorageClientException, AccessDeniedException, IOException, PropertyMigrationException;

}
