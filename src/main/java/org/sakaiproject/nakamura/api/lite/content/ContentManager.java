package org.sakaiproject.nakamura.api.lite.content;

import java.io.IOException;
import java.io.InputStream;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;

public interface ContentManager {

    Content get(String path) throws StorageClientException, AccessDeniedException;

    void saveVersion(String path) throws StorageClientException, AccessDeniedException;

    void update(Content content) throws AccessDeniedException, StorageClientException;

    void delete(String path) throws AccessDeniedException, StorageClientException;

    long writeBody(String path, InputStream in) throws StorageClientException,
            AccessDeniedException, IOException;

    InputStream getInputStream(String path) throws StorageClientException, AccessDeniedException,
            IOException;

}
