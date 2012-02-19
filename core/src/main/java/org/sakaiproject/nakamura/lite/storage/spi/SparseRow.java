package org.sakaiproject.nakamura.lite.storage.spi;

import java.util.Map;

public interface SparseRow {

    String getRowId();

    Map<String, Object> getProperties();

}
