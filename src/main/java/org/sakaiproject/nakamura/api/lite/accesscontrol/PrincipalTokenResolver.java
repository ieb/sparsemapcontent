package org.sakaiproject.nakamura.api.lite.accesscontrol;

import org.sakaiproject.nakamura.api.lite.content.Content;

import java.util.List;

/**
 * Resolves proxyPrincipals to tokens. An implementation of this will be
 * provided by the caller if principal tokens are to be resolved. This
 * implementation should bind to the user in question.
 */
public interface PrincipalTokenResolver {

    /**
     * Resolve the principal.
     *
     * @param principal
     * @return the tokens associated with the proxyPrincipal, could be more than
     *         one.
     */
    void resolveTokens(String principal, List<Content> tokens);

}
