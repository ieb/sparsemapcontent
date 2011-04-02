package org.sakaiproject.nakamura.lite.content;

import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalTokenResolver;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.api.lite.content.ContentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PathPrincipalTokenResolver implements PrincipalTokenResolver,
        ChainingPrincipalTokenResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathPrincipalTokenResolver.class);
    private ContentManager contentManager;
    private PrincipalTokenResolver nextTokenResolver;
    private String tokenPath;

    public PathPrincipalTokenResolver(String tokenPath, ContentManager contentManager) {
        this.contentManager = contentManager;
        this.tokenPath = tokenPath;
    }

    public void resolveTokens(String principal, List<Content> tokens) {
        try {
            Content token = contentManager.get(StorageClientUtils.newPath(tokenPath, principal));
            if (token != null) {
                tokens.add(token);
            }
        } catch (AccessDeniedException e) {
            LOGGER.warn("Unable to get token for user " + e.getMessage());
        } catch (StorageClientException e) {
            LOGGER.warn("Unable to get token for user " + e.getMessage(), e);
        }
        if (nextTokenResolver != null) {
            nextTokenResolver.resolveTokens(principal, tokens);
        }
    }

    public void setNextTokenResovler(PrincipalTokenResolver nextTokenResolver) {
        this.nextTokenResolver = nextTokenResolver;
    }

    public void clearNextTokenResolver() {
        this.nextTokenResolver = null;
    }

}
