package uk.co.tfd.sm.search.es.handlers;

import org.osgi.service.event.Event;

import uk.co.tfd.sm.api.search.IndexingHandler;
import uk.co.tfd.sm.api.search.InputDocument;
import uk.co.tfd.sm.api.search.RepositorySession;

import java.util.Collection;
import java.util.Collections;

public class DefaultSparseHandler implements IndexingHandler {

	public Collection<InputDocument> getDocuments(RepositorySession repositorySession, Event event) {
		return Collections.emptyList();
	}

	public Collection<String> getDeleteQueries(RepositorySession repositorySession, Event event) {
		return Collections.emptyList();
	}

}
