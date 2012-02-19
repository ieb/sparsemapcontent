package org.sakaiproject.nakamura.lite.storage.jdbc.migrate;

import java.util.Map;

import org.sakaiproject.nakamura.api.lite.PropertyMigrator;

public class TPropertyMigrator implements PropertyMigrator {

    private String name;
    private String[] dependencies;
    private Map<String, String> options;

    public TPropertyMigrator(String name, String[] dependencies, Map<String,String> options) {
        this.name = name;
        this.dependencies = dependencies;
        this.options = options;
    }

    public boolean migrate(String rid, Map<String, Object> properties) {
        return false;
    }

    public String[] getDependencies() {
        return dependencies;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name;
    }

    public Map<String, String> getOptions() {
        return options;
    }

}
