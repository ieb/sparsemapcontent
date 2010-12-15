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
package org.sakaiproject.nakamura.lite.content;

import static org.sakaiproject.nakamura.lite.content.InternalContent.BLOCKID_FIELD;
import static org.sakaiproject.nakamura.lite.content.InternalContent.BODY_CREATED;
import static org.sakaiproject.nakamura.lite.content.InternalContent.BODY_CREATED_BY;
import static org.sakaiproject.nakamura.lite.content.InternalContent.BODY_LAST_MODIFIED;
import static org.sakaiproject.nakamura.lite.content.InternalContent.BODY_LAST_MODIFIED_BY;
import static org.sakaiproject.nakamura.lite.content.InternalContent.CREATED;
import static org.sakaiproject.nakamura.lite.content.InternalContent.CREATED_BY;
import static org.sakaiproject.nakamura.lite.content.InternalContent.DELETED_FIELD;
import static org.sakaiproject.nakamura.lite.content.InternalContent.LASTMODIFIED;
import static org.sakaiproject.nakamura.lite.content.InternalContent.LASTMODIFIED_BY;
import static org.sakaiproject.nakamura.lite.content.InternalContent.LENGTH_FIELD;
import static org.sakaiproject.nakamura.lite.content.InternalContent.NEXT_VERSION_FIELD;
import static org.sakaiproject.nakamura.lite.content.InternalContent.PATH_FIELD;
import static org.sakaiproject.nakamura.lite.content.InternalContent.PREVIOUS_BLOCKID_FIELD;
import static org.sakaiproject.nakamura.lite.content.InternalContent.PREVIOUS_VERSION_UUID_FIELD;
import static org.sakaiproject.nakamura.lite.content.InternalContent.READONLY_FIELD;
import static org.sakaiproject.nakamura.lite.content.InternalContent.STRUCTURE_UUID_FIELD;
import static org.sakaiproject.nakamura.lite.content.InternalContent.TRUE;
import static org.sakaiproject.nakamura.lite.content.InternalContent.UUID_FIELD;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.sakaiproject.nakamura.api.lite.Configuration;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessControlManager;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Permissions;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Security;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.sakaiproject.nakamura.lite.storage.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * Content Manager.
 * Manages two types of content,
 * Bundles of content properties and bodies.
 * Bodies are chunked into sizes to aide efficiency when retrieving the content.
 * 
 * CF content stores the structure of the content keyed by path.
 * Each item contains child names in columns + the guid of the item
 * eg
 *   path : {
 *       ':id' : thisitemUUID,
 *       subitemA : subitemAUUID,
 *       subitemB : subitemBUUID
 *   }
 * the guid of the item points to the CF content version where items are keyed by the version.
 * These items also contain child nodes under children as an array
 * 
 * eg
 *    itemUUID : {
 *         'id' : thisitemUUID
 *         'children' : [ 
 *           subitemA : subitemAUUID,
 *           subitemB : subitemBUUID
 *         ],
 *         'nblocks' = numberOfBlocksSetsOfContent
 *         'length' = totalLenghtOftheContent
 *         'blocksize' = storageBlockSize
 *         'blockid' = blockID
 *         ... other properties ...
 *    }
 *    
 * The content blocks are stored in CF content body
 * eg
 *   blockID:blockSetNumber : {
 *         'id' : blockID,
 *         'numblocks' : numberOfBlocksInThisSet,
 *         'blocklength0' : lengthOfThisBlock,
 *         'body0' : byte[]
 *         'blocklength1' : lengthOfThisBlock,
 *         'body1' : byte[]
 *         ...
 *         'blocklengthn' : lengthOfThisBlock,
 *         'bodyn' : byte[]
 *    }
 * 
 * 
 * Versioning:
 * 
 * When a version is saved, the CF contentVersion item is cloned and the CF content :id and any subitems IDs are updated.
 * Block 0 is marked as readonly
 * 
 * When the body is written to its CF content row is checked to see if the block is read only. If so a new block is created with and linked in with 'previousversion'
 * 
 * </pre>
 * 
 * @author ieb
 * 
 */
public class ContentManagerImpl implements ContentManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentManagerImpl.class);

    /**
     * Key containing deleted items.
     */
    private static final String DELETEDITEMS_KEY = ":deleteditems";

    /**
     * Storage Client
     */
    private StorageClient client;
    /**
     * The access control manager in use.
     */
    private AccessControlManager accessControlManager;
    /**
     * Key space for this content.
     */
    private String keySpace;
    /**
     * Column Family for this content.
     */
    private String contentColumnFamily;

    private boolean closed;

    public ContentManagerImpl(StorageClient client, AccessControlManager accessControlManager,
            Configuration config) {
        this.client = client;
        this.accessControlManager = accessControlManager;
        keySpace = config.getKeySpace();
        contentColumnFamily = config.getContentColumnFamily();
        closed = false;
    }

    public Content get(String path) throws StorageClientException, AccessDeniedException {
        checkOpen();
        accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_READ);
        Map<String, Object> structure = client.get(keySpace, contentColumnFamily, path);
        if (structure != null && structure.size() > 0) {
            String contentId = StorageClientUtils.toString(structure.get(STRUCTURE_UUID_FIELD));
            Map<String, Object> content = client.get(keySpace, contentColumnFamily, contentId);
            if (content != null && content.size() > 0) {
                Content contentObject = new Content(path, content);
                ((InternalContent) contentObject).internalize(structure, this);
                return contentObject;
            }
        }
        return null;

    }

    public void saveVersion(String path) throws StorageClientException, AccessDeniedException {
        checkOpen();
        accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_WRITE);
        Map<String, Object> structure = client.get(keySpace, contentColumnFamily, path);
        String contentId = StorageClientUtils.toString(structure.get(STRUCTURE_UUID_FIELD));
        Map<String, Object> saveVersion = client.get(keySpace, contentColumnFamily, contentId);
        Map<String, Object> newVersion = Maps.newHashMap(saveVersion);
        String newVersionId = StorageClientUtils.getUuid();
        String saveVersionId = StorageClientUtils.toString(saveVersion.get(UUID_FIELD));
        String saveBlockId = StorageClientUtils.toString(saveVersion.get(BLOCKID_FIELD));

        Object newVersionIdS = StorageClientUtils.toStore(newVersionId);
        Object saveVersionIdS = StorageClientUtils.toStore(saveVersionId);
        Object saveBlockIdS = StorageClientUtils.toStore(saveBlockId);

        newVersion.put(UUID_FIELD, newVersionIdS);
        newVersion.put(PREVIOUS_VERSION_UUID_FIELD, saveVersionIdS);
        if (saveBlockId != null) {
            newVersion.put(PREVIOUS_BLOCKID_FIELD, saveBlockIdS);
        }

        saveVersion.put(NEXT_VERSION_FIELD, newVersionIdS);
        saveVersion.put(READONLY_FIELD, TRUE);

        client.insert(keySpace, contentColumnFamily, saveVersionId, saveVersion);
        client.insert(keySpace, contentColumnFamily, newVersionId, newVersion);
        client.insert(keySpace, contentColumnFamily, path,
                ImmutableMap.of(STRUCTURE_UUID_FIELD, newVersionIdS));
        if (!path.equals("/")) {
            client.insert(keySpace, contentColumnFamily,
                    StorageClientUtils.getParentObjectPath(path),
                    ImmutableMap.of(StorageClientUtils.getObjectName(path), newVersionIdS));
        }
        LOGGER.debug("Saved Version [{}] {}", saveVersionId, saveVersion);
        LOGGER.debug("New Version [{}] {}", newVersionId, newVersion);
        LOGGER.debug("Structure {} ", client.get(keySpace, contentColumnFamily, path));
        LOGGER.debug(
                "Parent Structure {} ",
                client.get(keySpace, contentColumnFamily,
                        StorageClientUtils.getParentObjectPath(path)));

    }

    public void update(Content excontent) throws AccessDeniedException, StorageClientException {
        checkOpen();
        InternalContent content = (InternalContent) excontent;
        String path = content.getPath();
        accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_WRITE);
        String id = null;
        Object idStore = null;
        Map<String, Object> toSave = null;
        Map<String, Object> contentPropertes = content.getContent();
        if (content.isNew()) {
            toSave = Maps.newHashMap(contentPropertes);
            id = StorageClientUtils.getUuid();
            idStore = StorageClientUtils.toStore(id);
            toSave.put(UUID_FIELD, idStore);
            toSave.put(PATH_FIELD, StorageClientUtils.toStore(path));
            toSave.put(CREATED, StorageClientUtils.toStore(System.currentTimeMillis()));
            toSave.put(CREATED_BY,
                    StorageClientUtils.toStore(accessControlManager.getCurrentUserId()));
            LOGGER.debug("New Content with {} {} ", id, toSave);
        } else if (content.isUpdated()) {
            toSave = Maps.newHashMap(content.getUpdated());
            id = StorageClientUtils.toString(contentPropertes.get(UUID_FIELD));
            idStore = StorageClientUtils.toStore(contentPropertes.get(UUID_FIELD));
            toSave.put(LASTMODIFIED, StorageClientUtils.toStore(System.currentTimeMillis()));
            toSave.put(LASTMODIFIED_BY,
                    StorageClientUtils.toStore(accessControlManager.getCurrentUserId()));
            LOGGER.debug("Updating Content with {} {} ", id, toSave);
        } else {
            // if not new or updated, dont update.
            return;
        }

        Map<String, Object> checkContent = client.get(keySpace, contentColumnFamily, id);
        if (TRUE.equals(StorageClientUtils.toString(checkContent.get(READONLY_FIELD)))) {
            throw new AccessDeniedException(Security.ZONE_CONTENT, path,
                    "update on read only Content Item (possibly a previous version of the item)",
                    accessControlManager.getCurrentUserId());
        }
        if (content.isNew()) {
            // only when new do we update the structure.
            if (!StorageClientUtils.isRoot(path)) {
                client.insert(keySpace, contentColumnFamily,
                        StorageClientUtils.getParentObjectPath(path),
                        ImmutableMap.of(StorageClientUtils.getObjectName(path), idStore));
            }
            client.insert(keySpace, contentColumnFamily, path,
                    ImmutableMap.of(STRUCTURE_UUID_FIELD, idStore));
        }
        // save the content id.
        client.insert(keySpace, contentColumnFamily, id, toSave);
        LOGGER.debug("Saved {} at {} as {} ", new Object[] { path, id, toSave });
        // reset state to unmodified to take further modifications.
        content.reset();
    }

    public void delete(String path) throws AccessDeniedException, StorageClientException {
        checkOpen();
        accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_DELETE);
        Map<String, Object> structure = client.get(keySpace, contentColumnFamily, path);
        String uuid = StorageClientUtils.toString(structure.get(STRUCTURE_UUID_FIELD));
        client.remove(keySpace, contentColumnFamily, path);
        Map<String, Object> m = new HashMap<String, Object>();
        m.put(StorageClientUtils.getObjectName(path), null);
        client.insert(keySpace, contentColumnFamily, StorageClientUtils.getParentObjectPath(path),
                m);
        client.insert(keySpace, contentColumnFamily, uuid,
                ImmutableMap.of(DELETED_FIELD, (Object) TRUE));
        client.insert(keySpace, contentColumnFamily, DELETEDITEMS_KEY,
                ImmutableMap.of(uuid, StorageClientUtils.toStore(path)));
    }

    public long writeBody(String path, InputStream in) throws StorageClientException,
    AccessDeniedException, IOException {
        return writeBody(path, in, null);
    }

    public long writeBody(String path, InputStream in, String streamId ) throws StorageClientException,
            AccessDeniedException, IOException {
        checkOpen();
        accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_WRITE);
        Map<String, Object> structure = client.get(keySpace, contentColumnFamily, path);
        String contentId = StorageClientUtils.toString(structure.get(STRUCTURE_UUID_FIELD));
        Map<String, Object> content = client.get(keySpace, contentColumnFamily, contentId);
        String contentBlockId = null;
        boolean isnew = false;
        String blockIdField = getAltField(BLOCKID_FIELD,streamId);
        if (content.containsKey(blockIdField)) {
            contentBlockId = StorageClientUtils.toString(content.get(blockIdField));
        } else {
            contentBlockId = StorageClientUtils.getUuid();
            isnew = true;
        }
        Map<String, Object> metadata = client.streamBodyIn(keySpace, contentColumnFamily,
                contentId, contentBlockId, content, in);
        metadata.put(getAltField(BODY_LAST_MODIFIED, streamId), StorageClientUtils.toStore(System.currentTimeMillis()));
        metadata.put(getAltField(BODY_LAST_MODIFIED_BY, streamId),
                StorageClientUtils.toStore(accessControlManager.getCurrentUserId()));
        if (isnew) {
            metadata.put(getAltField(BODY_CREATED, streamId), StorageClientUtils.toStore(System.currentTimeMillis()));
            metadata.put(getAltField(BODY_CREATED_BY, streamId),
                    StorageClientUtils.toStore(accessControlManager.getCurrentUserId()));
        }
        client.insert(keySpace, contentColumnFamily, contentId, metadata);
        long length = StorageClientUtils.toLong(metadata.get(LENGTH_FIELD));
        return length;

    }
    public String getAltField(String field, String streamId) {
        if ( streamId == null ) {
            return field;
        }
        return field+"/"+streamId;
    }

    public InputStream getInputStream(String path) throws StorageClientException,
    AccessDeniedException, IOException {
            return getInputStream(path,null);
    }

    public InputStream getInputStream(String path, String streamId) throws StorageClientException,
            AccessDeniedException, IOException {
        checkOpen();
        accessControlManager.check(Security.ZONE_CONTENT, path, Permissions.CAN_READ);
        Map<String, Object> structure = client.get(keySpace, contentColumnFamily, path);
        LOGGER.debug("Structure Loaded {} {} ", path, structure);
        String contentId = StorageClientUtils.toString(structure.get(STRUCTURE_UUID_FIELD));
        Map<String, Object> content = client.get(keySpace, contentColumnFamily, contentId);
        String contentBlockId = StorageClientUtils.toString(content.get(getAltField(BLOCKID_FIELD, streamId)));
        return client.streamBodyOut(keySpace, contentColumnFamily, contentId, contentBlockId,
                content);
    }

    public void close() {
        closed = true;
    }

    private void checkOpen() throws StorageClientException {
        if (closed) {
            throw new StorageClientException("Content Manager is closed");
        }
    }

}
