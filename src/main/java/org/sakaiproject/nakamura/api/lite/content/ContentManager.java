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
import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalTokenResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
     * Perform a search for content matching the given properties
     * 
     * @param searchProperties a Map of property names and values. All the properties must match to give a result
     * @return an Iterable of Content items in no guaranteed order
     * @throws StorageClientException
     * @throws AccessDeniedException
     */
    // TODO needs better documentation - not clear how to OR or AND
    Iterable<Content> find(Map<String, Object> searchProperties) throws StorageClientException, AccessDeniedException;

    /**
     * Counts the maximum number of results a find operation could return, ignoring access control. This method may cause problems
     * if used inappropriately on sets of results that are mostly not readable by the current user (eg how many documents are there with "ieb" and "your fired" in ?)
     * @param searchProperties Map the same as the finder
     * @return maximum number of results a find could return.
     */
    int count(Map<String, Object> countSearch) throws StorageClientException;

    /**
     * Save the current version of the content object including metadata and
     * file bodies as a read only snapshot
     * 
     * @param path
     *            the path to the item
     * @return 
     * @throws StorageClientException
     *             if there was a problem with the operation.
     * @throws AccessDeniedException
     *             if the user is unable to save a version the object at the
     *             path. This is not an indication that the objected at the path
     *             exists, just that the user can't save a version of anything
     *             at that location and possibly at parent locations.
     */
    String saveVersion(String path) throws StorageClientException, AccessDeniedException;

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

    /**
     * @param path
     * @return true if the path exists
     */
    boolean exists(String path);

    /**
     * Copy a content item from to
     * 
     * @param from
     *            the path to copy from, must exist
     * @param to
     *            the path to copy to, must not exist
     * @param withStreams
     *            if true, a copy is made of all the streams, if false the
     *            streams are shared but copies are made of the properties.
     * @throws IOException
     * @throws AccessDeniedException
     *             if the user cant read the source or write the desination.
     * @throws IOException
     */
    void copy(String from, String to, boolean withStreams) throws StorageClientException,
            AccessDeniedException, IOException;

    /**
     * Move a content item from to.
     * 
     * @param from
     *            the source, must exist
     * @param to
     *            the destination must not exist.
     * @throws StorageClientException
     * @throws AccessDeniedException
     */
    void move(String from, String to) throws AccessDeniedException, StorageClientException;

  /**
   * Move a content item, and all child items, from to. Acts recursively.
   * 
   * @param from
   *          the source, must exist
   * @param to
   *          the destination must not exist.
   * @return a List of the moves performed (from and to paths). Listed bottom-up,
   *         path-wise.
   * @throws StorageClientException
   * @throws AccessDeniedException
   */
  List<ActionRecord> moveWithChildren(String from, String to)
      throws AccessDeniedException,
      StorageClientException;

  /**
   * Create a Link. Links place a pointer to real content located at the to path, in the
   * from path. Modifications to the underlying content are reflected in both locations.
   * Permissions are controlled by the location and not the underlying content.
   * 
   * @param from
   *          the source of the link (the soft part), must not exist.
   * @param to
   *          the destination, must exist
   * @throws AccessDeniedException
   *           if the user cant read the to and write the from
   * @throws StorageClientException
   */
    void link(String from, String to) throws AccessDeniedException, StorageClientException;

    /**
     * Get a specific version
     * 
     * @param path
     * @param versionId
     * @return the Content object, which will be read only, or null if the
     *         version does not exist for the path.
     * @throws StorageClientException
     * @throws AccessDeniedException
     */
    Content getVersion(String path, String versionId) throws StorageClientException,
            AccessDeniedException;

    /**
     * Get an InputStream for the body of the version specified.
     * 
     * @param path
     * @param versionId
     * @param streamId
     * @return the input stream or null if the streamId or versionId does not
     *         exist for the specified path.
     * @throws AccessDeniedException
     * @throws StorageClientException
     * @throws IOException
     */
    InputStream getVersionInputStream(String path, String versionId, String streamId)
            throws AccessDeniedException, StorageClientException, IOException;

    /**
     * Get the primary stream for the version
     * 
     * @param path
     * @param versionId
     * @return the input stream or null if the version or main stream doesnt
     *         exist for the path.
     * @throws AccessDeniedException
     * @throws StorageClientException
     * @throws IOException
     */
    InputStream getVersionInputStream(String path, String versionId) throws AccessDeniedException,
            StorageClientException, IOException;

    /**
     * List the versionIds of all version starting wih the most recent.
     * 
     * @param path
     * @return an Iterator of all versions, the newest first or an empty
     *         iterator if there were no versions.
     * @throws AccessDeniedException
     * @throws StorageClientException
     */
    List<String> getVersionHistory(String path) throws AccessDeniedException,
            StorageClientException;

    /**
     * Gets a lazy iterator of child paths.
     * @param path the parent path.
     * @return
     * @throws StorageClientException
     */
    Iterator<String> listChildPaths(String path) throws StorageClientException;

    /**
     * Get a lazy iterator of child content objects.
     * @param path
     * @return
     * @throws StorageClientException
     */
    Iterator<Content> listChildren(String path) throws StorageClientException;

    /**
     * @param path the path of the content node
     * @param streamId the stream id, null for the default stream
     * @return true if the stream id is present.
     * @throws AccessDeniedException
     * @throws StorageClientException
     */
    boolean hasBody(String path, String streamId) throws StorageClientException, AccessDeniedException;

    /**
     * Sets the principal Token Resolver for all subsequent requests using this
     * session. When the ContentManager is invoked it will consult the supplied
     * principal Token Resolver to locate any extra tokens that have been
     * granted.
     *
     * @param principalTokenResolver
     */
    void setPrincipalTokenResolver(PrincipalTokenResolver principalTokenResolver);

    /**
     * Clear the principal Token Resolver
     */
    void cleanPrincipalTokenResolver();


    /**
     * Put the content manager into maintanence mode to allow an admin session to gain control over protected content properties.
     * Only use this in migration. Never use it in runtime production as you will risk breaking the referential integrety
     * of the internal content model. Also, please ensure that your code matches the same content model being used by 
     * the target ContentManagerImpl, before you enable maintanence mode. Failure to do so may destroy your content.
     */
    void setMaintanenceMode(boolean maintanenceMode);

    
    /**
     * @param path cause an event to be emitted for the path that will cause a refresh.
     * @throws AccessDeniedException 
     * @throws StorageClientException 
     */
    void triggerRefresh(String path) throws StorageClientException, AccessDeniedException;
    
    
    /**
     * Cause an event to be emitted for all items.
     * @throws StorageClientException 
     */
    void triggerRefreshAll() throws StorageClientException;



}
