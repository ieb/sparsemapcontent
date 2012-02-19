package org.sakaiproject.nakamura.api.lite;

import java.io.File;


public interface Feedback {

    void log(String format, Object ... params);

    void exception(Throwable e);

    void newLogFile(File currentFile);

    void progress(boolean dryRun, long done, long toDo);

}
