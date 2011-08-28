package org.sakaiproject.nakamura.lite.content;


import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessControlManager;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AclModification;
import org.sakaiproject.nakamura.api.lite.accesscontrol.Permission;
import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalTokenResolver;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.lite.accesscontrol.PropertyAcl;

import java.util.Map;

public class AccessControlManagerTokenWrapper implements AccessControlManager {

    private AccessControlManager delegate;
    private PrincipalTokenResolver principalTokenResovler;

    public AccessControlManagerTokenWrapper(AccessControlManager accessControlManager,
            PrincipalTokenResolver principalResolver) {
        this.delegate = accessControlManager;
        this.principalTokenResovler = principalResolver;
    }

    public Map<String, Object> getAcl(String objectType, String objectPath)
            throws StorageClientException, AccessDeniedException {
        try {
            delegate.setRequestPrincipalResolver(principalTokenResovler);
            return delegate.getAcl(objectType, objectPath);
        } finally {
            delegate.clearRequestPrincipalResolver();
        }
    }

    public Map<String, Object> getEffectiveAcl(String objectType, String objectPath)
            throws StorageClientException, AccessDeniedException {
        try {
            delegate.setRequestPrincipalResolver(principalTokenResovler);
            return delegate.getEffectiveAcl(objectType, objectPath);
        } finally {
            delegate.clearRequestPrincipalResolver();
        }
    }

    public void setAcl(String objectType, String objectPath, AclModification[] aclModifications)
            throws StorageClientException, AccessDeniedException {
        try {
            delegate.setRequestPrincipalResolver(principalTokenResovler);
            delegate.setAcl(objectType, objectPath, aclModifications);
        } finally {
            delegate.clearRequestPrincipalResolver();
        }
    }

    public void check(String objectType, String objectPath, Permission permission)
            throws AccessDeniedException, StorageClientException {
        try {
            delegate.setRequestPrincipalResolver(principalTokenResovler);
            delegate.check(objectType, objectPath, permission);
        } finally {
            delegate.clearRequestPrincipalResolver();
        }
    }

    public String getCurrentUserId() {
        return delegate.getCurrentUserId();
    }

    public boolean can(Authorizable authorizable, String objectType, String objectPath,
            Permission permission) {
        throw new UnsupportedOperationException();
    }

    public Permission[] getPermissions(String objectType, String objectPath)
            throws StorageClientException {
        try {
            delegate.setRequestPrincipalResolver(principalTokenResovler);
            return delegate.getPermissions(objectType, objectPath);
        } finally {
            delegate.clearRequestPrincipalResolver();
        }
    }

    public String[] findPrincipals(String objectType, String objectPath, int permission,
            boolean granted) throws StorageClientException {
        try {
            delegate.setRequestPrincipalResolver(principalTokenResovler);
            return delegate.findPrincipals(objectType, objectPath, permission, granted);
        } finally {
            delegate.clearRequestPrincipalResolver();
        }
    }

    public void setRequestPrincipalResolver(PrincipalTokenResolver tokenPrincipalResolver) {
        if ( principalTokenResovler instanceof ChainingPrincipalTokenResolver) {
            ((ChainingPrincipalTokenResolver)tokenPrincipalResolver).setNextTokenResovler(tokenPrincipalResolver);
        }
    }

    public void clearRequestPrincipalResolver() {
        if ( principalTokenResovler instanceof ChainingPrincipalTokenResolver) {
            ((ChainingPrincipalTokenResolver)principalTokenResovler).clearNextTokenResolver();
        }
    }

    public void signContentToken(Content token, String objectPath) throws StorageClientException,
            AccessDeniedException {
        try {
            delegate.setRequestPrincipalResolver(principalTokenResovler);
            delegate.signContentToken(token, objectPath);
        } finally {
            delegate.clearRequestPrincipalResolver();
        }
    }

    public PropertyAcl getPropertyAcl(String objectType, String objectPath)
            throws AccessDeniedException, StorageClientException {
        try {
            delegate.setRequestPrincipalResolver(principalTokenResovler);
            return delegate.getPropertyAcl(objectType, objectPath);
        } finally {
            delegate.clearRequestPrincipalResolver();
        }
    }

}
