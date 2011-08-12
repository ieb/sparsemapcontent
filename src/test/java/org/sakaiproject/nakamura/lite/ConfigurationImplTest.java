package org.sakaiproject.nakamura.lite;

import java.io.IOException;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.content.Content;
import org.sakaiproject.nakamura.lite.content.InternalContentAccess;

import com.google.common.collect.ImmutableMap;

public class ConfigurationImplTest {

    
    @Test
    public void testProperties() throws IOException {
        InternalContentAccess.resetInternalContent();
        ConfigurationImpl configurationImpl = new ConfigurationImpl();
        Map<String,Object> properties = ImmutableMap.of();
        configurationImpl.activate(properties);
        Assert.assertEquals(ConfigurationImpl.DEFAULT_UUID_FIELD,Content.getUuidField());
    }
    @Test
    public void testPropertiesOSGiOverride() throws IOException {
        InternalContentAccess.resetInternalContent();
        ConfigurationImpl configurationImpl = new ConfigurationImpl();
        Map<String,Object> properties = ImmutableMap.of(ConfigurationImpl.UUID_FIELD_NAME,(Object)"_somethingElse");
        configurationImpl.activate(properties);
        Assert.assertEquals("_somethingElse",Content.getUuidField());
    }
    @Test
    public void testPropertiesSharedOverride() throws IOException {
        InternalContentAccess.resetInternalContent();
        ConfigurationImpl configurationImpl = new ConfigurationImpl();
        System.setProperty(ConfigurationImpl.SHAREDCONFIGPROPERTY, "src/test/resources/testsharedoverride.properties");
        Map<String,Object> properties = ImmutableMap.of();
        configurationImpl.activate(properties);
        System.clearProperty(ConfigurationImpl.SHAREDCONFIGPROPERTY);
        Assert.assertEquals("_somethingElseFromProperties",Content.getUuidField());
    }
    @Test
    public void testPropertiesSharedOverrideOSGi() throws IOException {
        InternalContentAccess.resetInternalContent();
        ConfigurationImpl configurationImpl = new ConfigurationImpl();
        System.setProperty(ConfigurationImpl.SHAREDCONFIGPROPERTY, "src/test/resources/testsharedoveride.properties");
        Map<String,Object> properties = ImmutableMap.of(ConfigurationImpl.UUID_FIELD_NAME,(Object)"_somethingElse");
        configurationImpl.activate(properties);
        System.clearProperty(ConfigurationImpl.SHAREDCONFIGPROPERTY);
        Assert.assertEquals("_somethingElse",Content.getUuidField());
    }
}
