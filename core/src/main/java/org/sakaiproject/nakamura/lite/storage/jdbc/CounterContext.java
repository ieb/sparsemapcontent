package org.sakaiproject.nakamura.lite.storage.jdbc;

import java.util.concurrent.atomic.AtomicInteger;

public interface CounterContext {

    AtomicInteger get(String key);

}
