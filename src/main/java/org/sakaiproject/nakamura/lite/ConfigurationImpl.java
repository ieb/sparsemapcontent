package org.sakaiproject.nakamura.lite;

import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.sakaiproject.nakamura.api.lite.Configuration;

@Component(immediate = true, metatype = true)
@Service(value = Configuration.class)
public class ConfigurationImpl implements Configuration {

    @Property(value = "Acl")
    private static final String ACL_COLUMN_FAMILY = "acl-column-family";
    @Property(value = "Nakamura")
    private static final String KEYSPACE = "keyspace";
    @Property(value = "Authorizable")
    private static final String AUTHORIZABLE_COLUMN_FAMILY = "authorizable-column-family";
    private static final String CONTENT_COLUMN_FAMILY = "content-column-family";
    private String aclColumnFamily;
    private String keySpace;
    private String authorizableColumnFamily;
    private String contentColumnFamily;

    @Activate
    public void activate(Map<String, Object> properties) {
        aclColumnFamily = (String) properties.get(ACL_COLUMN_FAMILY);
        keySpace = (String) properties.get(KEYSPACE);
        authorizableColumnFamily = (String) properties.get(AUTHORIZABLE_COLUMN_FAMILY);
        contentColumnFamily = (String) properties.get(CONTENT_COLUMN_FAMILY);
    }

    public String getAclColumnFamily() {
        return aclColumnFamily;
    }

    public String getKeySpace() {
        return keySpace;
    }

    public String getAuthorizableColumnFamily() {
        return authorizableColumnFamily;
    }

    public String getContentColumnFamily() {
        return contentColumnFamily;
    }
    
    

}
