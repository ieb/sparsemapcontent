package org.sakaiproject.nakamura.lite.storage.spi;

import java.util.Map;


public class SparseMapRow implements SparseRow {

    private String rid;
    private Map<String, Object> values;

    public SparseMapRow(String rid, Map<String, Object> values) {
        this.rid = rid;
        this.values = values;
    }


    public String getRowId() {
        return rid;
    }

    public Map<String, Object> getProperties() {
        return values;
    }

}
