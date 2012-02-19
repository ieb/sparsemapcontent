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

import org.sakaiproject.nakamura.lite.content.InternalContent;

/**
 * <p>
 * The Content objects represents a Sparse Content Object. Sparse Content
 * Objects live in a structured tree. The may have 1..n children where n may be
 * very large (millions). They may or may not have a parent. If they dont have a
 * parent they are a root Content object. There may be many root Content objects
 * within a content store allowing many disconnected content subtrees trees.
 * </p>
 * <p>
 * To create a Content object, first check that the content object does not
 * already exist with a cotentManager.get(path), and if that responds with a
 * null object, then create a new Content object with new Content(path, map);
 * where path is the path of the content object and map is null or the initial
 * properties of the content object. At that point the content object is created
 * but not saved. To save perform contentManager.udpate(contentObject) which
 * will create any intermediate path and save the content object. At that point
 * it will be persisted in the content store and have structure objects.
 * </p>
 * <p>
 * Please note, if you create a Content object using the public constructor,
 * that object will have no children until it is saved and re-loaded by the
 * ContentManager. Any attempt to list children of the newly created Content
 * instance will result in an empty iterator.
 * </p>
 * <p>
 * If you need to make changes to a Content object, get it out of the store,
 * with contentManager.get(path); then change some properties before performing
 * a contentManager.update(contentObject); Transactions are managed by the
 * underlying store implementation and are not actively managed in the
 * contentManager. If your underlying store is not transactional, the update
 * operation will persist directly to the underlying store. Concurrent threads
 * in the same JVM may retrieve the same underlying data from the content store
 * but each contentManager will operate on its own set of contentObjects
 * isolated from other contentManagers until the update operation is completed.
 * </p>
 */
public class Content extends InternalContent {

    /**
     * Create a brand new content object not connected to the underlying store.
     * To save use contentManager.update(contentObject); Since the object is not
     * connected to the underlying store, it not have any children. Only Content
     * objects loaded from the underlying store with ContentManager.get(path)
     * are connected to the underlying store and have children. This is the case
     * even if the path of the Content instance created via the public
     * constructor exists within the underlying content store.
     * 
     * @param path
     *            the path in the store that should not already exist. If it
     *            does exist, this new object will overwrite.
     * @param content
     *            a map of initial content metadata.
     * @param
     */
    public Content(String path, java.util.Map<String, Object> content) {
        super(path, content);
    }


}
