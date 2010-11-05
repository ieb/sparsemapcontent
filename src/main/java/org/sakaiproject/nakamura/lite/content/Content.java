package org.sakaiproject.nakamura.lite.content;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.lite.storage.StorageClientException;
import org.sakaiproject.nakamura.lite.storage.StorageClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

public class Content {

    private static final Logger LOGGER = LoggerFactory.getLogger(Content.class);
    private Map<String, Object> structure;
    private Map<String, Object> content;
    private String path;
    private ContentManager contentManager;

    Content(String path, Map<String, Object> structure, Map<String, Object> content,
            ContentManager contentManager) {
        this.structure = structure;
        this.content = content;
        this.path = path;
        this.contentManager = contentManager;
    }

    void setStructure(Map<String, Object> structure) {
        this.structure = structure;
    }

    void setContentManager(ContentManager contentManager) {
        this.contentManager = contentManager;
    }

    public Content(String path, Map<String, Object> content) {
        this.content = content;
        this.path = path;
    }

    Map<String, Object> getContent() {
        return content;
    }

    public Map<String, Object> getProperties() {
        return ImmutableMap.copyOf(content);
    }

    public String getPath() {
        return path;
    }

    public Iterable<Content> listChildren() {
        return new Iterable<Content>() {

            public Iterator<Content> iterator() {
                final Iterator<String> childIterator = listChildPaths().iterator();
                return new UnmodifiableIterator<Content>() {
                    Content childContent;

                    public boolean hasNext() {
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

                    public Content next() {
                        return childContent;
                    }
                };
            }
        };
    }

    public Iterable<String> listChildPaths() {
        return new Iterable<String>() {  
            public Iterator<String> iterator() {
                return Iterators.filter(structure.keySet().iterator(),
                        new Predicate<String>() {
                            public boolean apply(String input) {
                                return input.charAt(0) != ':';
                            }
                        });
            }
        };
    }

}
