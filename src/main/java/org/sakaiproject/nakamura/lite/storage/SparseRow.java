package org.sakaiproject.nakamura.lite.storage;

import java.util.Map;

public interface SparseRow {

    String getRowId();

    Map<String, Object> getProperties();

}
