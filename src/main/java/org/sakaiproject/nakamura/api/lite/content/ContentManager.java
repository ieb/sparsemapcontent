/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.nakamura.api.lite.content;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Defines a ContentManager service for operating on content.
 */
public interface ContentManager {

    /**
     * Get a content object from the store.
     * 
     * @param path
     *            the path to the content object.
     * @return the content object or null if it doesn't exist.
     * @throws StorageClientException
     *             if there was a problem with the operation.
     * @throws AccessDeniedException
     *             if the user is unable to read the object at the path. This is
     *             not an indication that the objected at the path exists, just
     *             that the user can't read anything at that location and
     *             possibly at parent locations.
     */
    Content get(String path) throws StorageClientException, AccessDeniedException;

    /**
     * Save the current version of the content object including metadata and
     * file bodies as a read only snapshot
     * 
     * @param path
     *            the path to the item
     * @throws StorageClientException
     *             if there was a problem with the operation.
     * @throws AccessDeniedException
     *             if the user is unable to save a version the object at the
     *             path. This is not an indication that the objected at the path
     *             exists, just that the user can't save a version of anything
     *             at that location and possibly at parent locations.
     */
    void saveVersion(String path) throws StorageClientException, AccessDeniedException;

    /**
     * Update or create the content object, and intermediate path if necessary,
     * stored at the location indicated by the path of the content object
     * supplied.
     * 
     * @param content
     *            the content object to update.
     * @throws StorageClientException
     *             if there was a problem with the operation.
     * @throws AccessDeniedException
     *             if the user is unable to write the object at the path. This
     *             is not an indication that the objected at the path exists,
     *             just that the user can't write anything at that location and
     *             possibly at parent locations.
     */
    void update(Content content) throws AccessDeniedException, StorageClientException;

    /**
     * Delete the content object at the path indicated.
     * 
     * @param path
     *            the path where the cotent object should be deleted.
     * @throws StorageClientException
     *             if there was a problem with the operation.
     * @throws AccessDeniedException
     *             if the user is unable to delete the object at the path. This
     *             is not an indication that the objected at the path exists,
     *             just that the user can't delete anything at that location and
     *             possibly at parent locations.
     */
    void delete(String path) throws AccessDeniedException, StorageClientException;

    /**
     * Write a body stream associated with the content item at the specified
     * path
     * 
     * @param path
     *            the path to the content object
     * @param in
     *            an input stream containing the bytes of teh body.
     * @return the size of the body after writing was completed.
     * @throws StorageClientException
     *             if there was a problem with the operation.
     * @throws AccessDeniedException
     *             if the user is unable to write the body of the object at the
     *             path. This is not an indication that the objected at the path
     *             exists, just that the user can't write anything at that
     *             location and possibly at parent locations.
     * @throws IOException
     *             if there was a problem reading from the stream or writing to
     *             the underlying store.
     */
    long writeBody(String path, InputStream in) throws StorageClientException,
            AccessDeniedException, IOException;

    /**
     * Get an input stream for the body of associated with the content object at
     * the path.
     * 
     * @param path
     *            the path to the content object.
     * @return an Input Stream ready to read the body from.
     * @throws StorageClientException
     *             if there was a problem with the operation.
     * @throws AccessDeniedException
     *             if the user is unable to read the body of the object at the
     *             path. This is not an indication that the objected at the path
     *             exists, just that the user can't read anything at that
     *             location and possibly at parent locations.
     * @throws IOException
     *             if there was a problem creating the stream.
     */
    InputStream getInputStream(String path) throws StorageClientException, AccessDeniedException,
            IOException;

    /**
     * Write a alternative body stream associated with the content item at the
     * specified path
     * 
     * @param path
     *            the path to the content object
     * @param in
     *            an input stream containing the bytes of the alternative body.
     * @param streamId
     *            the name of the alternative stream.
     * @return the size of the body after writing was completed.
     * @throws StorageClientException
     *             if there was a problem with the operation.
     * @throws AccessDeniedException
     *             if the user is unable to write the body of the object at the
     *             path. This is not an indication that the objected at the path
     *             exists, just that the user can't write anything at that
     *             location and possibly at parent locations.
     * @throws IOException
     *             if there was a problem reading from the stream or writing to
     *             the underlying store.
     */
    long writeBody(String path, InputStream in, String streamId) throws StorageClientException,
            AccessDeniedException, IOException;

    /**
     * Get an alternative input stream for the body of associated with the
     * content object at the path.
     * 
     * @param path
     *            the path to the content object.
     * @param streamId
     *            the name of the alternative stream.
     * @return an Input Stream ready to read the alternative body from.
     * @throws StorageClientException
     *             if there was a problem with the operation.
     * @throws AccessDeniedException
     *             if the user is unable to read the body of the object at the
     *             path. This is not an indication that the objected at the path
     *             exists, just that the user can't read anything at that
     *             location and possibly at parent locations.
     * @throws IOException
     *             if there was a problem creating the stream.
     */
    InputStream getInputStream(String path, String streamId) throws StorageClientException,
            AccessDeniedException, IOException;

}
