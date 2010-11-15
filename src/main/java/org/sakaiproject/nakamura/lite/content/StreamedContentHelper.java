package org.sakaiproject.nakamura.lite.content;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface StreamedContentHelper {



    Map<String, Object> writeBody(String keySpace, String columnFamily, String contentId,
            String contentBlockId, Map<String, Object> content, InputStream in) throws IOException;

    InputStream readBody(String keySpace, String columnFamily, String contentBlockId,
            Map<String, Object> content) throws IOException;

}
