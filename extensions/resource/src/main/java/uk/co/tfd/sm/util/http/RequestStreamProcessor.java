package uk.co.tfd.sm.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;

/**
 * A stream processor knows how to process a sections of a stream of data.
 * 
 * @author ieb
 * 
 * @param <T>
 *            The type of parent object the stream processor works on.
 */
public interface RequestStreamProcessor<T> {

	Collection<? extends String> processStream(String streamName,
			String fileName, String contentType, InputStream stream,
			ModificationRequest modificationRequest)
			throws StorageClientException, AccessDeniedException, IOException;

}
