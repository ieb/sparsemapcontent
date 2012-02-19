package org.sakaiproject.nakamura.lite.content;

import org.sakaiproject.nakamura.api.lite.accesscontrol.PrincipalTokenResolver;

public interface ChainingPrincipalTokenResolver {

    void setNextTokenResovler(PrincipalTokenResolver nextTokenResolver);

    void clearNextTokenResolver();

}
