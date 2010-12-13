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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.UnmodifiableIterator;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.util.PreemptiveIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

public class InternalContent {

    private static final Logger LOGGER = LoggerFactory.getLogger(Content.class);
    /**
     * The ID of a content item
     */
    public static final String UUID_FIELD = "id";
    /**
     * The path of the content item (used in structure row)
     */
    public static final String PATH_FIELD = "path";
    /**
     * content item ID referenced by a Structure item
     */
    public static final String STRUCTURE_UUID_FIELD = ":cid";
    /**
     * BlockID where the body of this content item is stored, if there is a body
     * (content row)
     */
    public static final String BLOCKID_FIELD = "blockId";
    /**
     * ID of the previous version (content row)
     */
    public static final String PREVIOUS_VERSION_UUID_FIELD = "previousVersion";
    /**
     * Previous Block ID. (content row)
     */
    public static final String PREVIOUS_BLOCKID_FIELD = "previousBlockId";
    /**
     * The ID of the next version (content row)
     */
    public static final String NEXT_VERSION_FIELD = "nextVersion";
    /**
     * Set to "Y" if the content item is read only. (content row)
     */
    public static final String READONLY_FIELD = "readOnly";
    /**
     * set to "Y" if deleted. (content row)
     */
    public static final String DELETED_FIELD = "deleted";

    /**
     * The block size in bytes in each block in a block set, if body store uses
     * blocking (content row)
     */
    public static final String BLOCKSIZE_FIELD = "blocksize";
    /**
     * Total length of the content body (content row)
     */
    public static final String LENGTH_FIELD = "length";
    /**
     * The number of block sets in a body (content row)
     */
    public static final String NBLOCKS_FIELD = "nblocks";
    /**
     * Yes, True, etc
     */
    public static final String TRUE = "Y";

    /**
     * The date (stored as GMT epoch long) the body was last modified. (content
     * row)
     */
    public static final String BODY_LAST_MODIFIED = "bodyLastModified";

    /**
     * The user ID that last modified the body. (content row)
     */
    public static final String BODY_LAST_MODIFIED_BY = "bodyLastModifiedBy";

    /**
     * The date the body was created (GMT epoch long) (content row)
     */
    public static final String BODY_CREATED = "bodyCreated";

    /**
     * The user that created the body. (content row)
     */
    public static final String BODY_CREATED_BY = "bodyCreatedBy";

    /**
     * The time the item was created. (content row)
     */
    public static final String CREATED = "created";

    /**
     * The user that created the item. (content row)
     */
    public static final String CREATED_BY = "createdBy";

    /**
     * The time the item was last modified. (content row)
     */
    public static final String LASTMODIFIED = "lastModified";

    /**
     * The user that lastModified the item. (content row)
     */
    public static final String LASTMODIFIED_BY = "lastModifiedBy";

    private Map<String, Object> structure;
    private Map<String, Object> content;
    private String path;
    private ContentManagerImpl contentManager;
    private Map<String, Object> updatedContent;
    private boolean updated;
    private boolean newcontent;

    InternalContent(String path, Map<String, Object> structure, Map<String, Object> content,
            ContentManagerImpl contentManager) {
    }

    public InternalContent(String path, Map<String, Object> content) {
        this.content = content;
        this.updatedContent = Maps.newHashMap(content);
        this.path = path;
        updated = true;
        newcontent = true;
    }

    void internalize(Map<String, Object> structure, ContentManagerImpl contentManager) {
        this.structure = structure;
        this.contentManager = contentManager;
        updated = false;
        newcontent = false;
    }

    Map<String, Object> getContent() {
        return content;
    }

    Map<String, Object> getUpdated() {
        return updatedContent;
    }

    public void reset() {
        updatedContent.clear();
        updated = false;
        newcontent = false;
    }

    public boolean isNew() {
        return newcontent;
    }

    public boolean isUpdated() {
        return updated;
    }

    public Map<String, Object> getProperties() {
        LOGGER.debug("getting properties map {}", content);
        return ImmutableMap.copyOf(content);
    }

    public void setProperty(String key, Object value) {
        Object o = content.get(key);
        if (o == null || !o.equals(value)) {
            content.put(key, value);
            updatedContent.put(key, value);
            updated = true;
        }

    }

    public String getPath() {
        return path;
    }

    public Iterable<Content> listChildren() {
        return new Iterable<Content>() {

            public Iterator<Content> iterator() {
                final Iterator<String> childIterator = listChildPaths().iterator();
                return new PreemptiveIterator<Content>() {
                    Content childContent;

                    protected boolean internalHasNext() {
                        childContent = null;
                        try {
                            while (childContent == null && childIterator.hasNext()) {
                                String child = childIterator.next();
                                try {
                                    childContent = contentManager.get(StorageClientUtils.newPath(
                                            path, child));
                                } catch (AccessDeniedException e) {
                                    LOGGER.debug("Unable to load {} cause {}", new Object[] {
                                            child, e.getMessage() }, e);
                                }

                            }
                        } catch (StorageClientException e) {
                            LOGGER.debug("Unable to load Children cause {}", e.getMessage(), e);

                        }
                        return (childContent != null);
                    }

                    protected Content internalNext() {
                        return childContent;
                    }
                };
            }
        };
    }

    public Iterable<String> listChildPaths() {
        return new Iterable<String>() {
            public Iterator<String> iterator() {
                return Iterators.filter(structure.keySet().iterator(), new Predicate<String>() {
                    public boolean apply(String input) {
                        return input.charAt(0) != ':';
                    }
                });
            }
        };
    }

}
