package org.sakaiproject.nakamura.lite.storage.jdbc.migrate;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.nakamura.api.lite.Feedback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class FileRedoLoggerTest {

    protected static final Logger LOGGER = LoggerFactory.getLogger(FileRedoLoggerTest.class);
    protected int files;

    @Test
    public void testFileRedoLogger() throws IOException {
        FileRedoLogger logger = new FileRedoLogger("target/redoloaTest", 1024, new Feedback() {
            
            private File lastFile;

            public void log(String format, Object... params) {
                LOGGER.info(MessageFormat.format(format, params));
            }
            
            public void exception(Throwable e) {
                LOGGER.warn(e.getMessage(),e);
            }

            public void newLogFile(File currentFile) {
                if ( lastFile != null ) {
                    LOGGER.info("Last File size {} ",lastFile.length());
                    if ( lastFile.length() < 1024) {
                        Assert.fail("File was not big enough");
                    }
                } 
                LOGGER.info("New Log File {}  ",currentFile.getAbsoluteFile());
                lastFile = currentFile;
                files++;
            }

            public void progress(boolean dryRun, long done, long toDo) {
                LOGGER.info("DryRun:{} {}% remaining {} ", new Object[] { dryRun,
                        ((done * 100) / toDo), toDo - done });
                
            }
        });
        
        for ( int i = 0; i < 1000; i++ ) {
            logger.begin();
            logger.before("n", "cf", "key", ImmutableMap.of("before", (Object)"before"));
            logger.after("n", "cf", "key", ImmutableMap.of("after", (Object)"after"));
            logger.delete("n", "cf", "k2");
            if ( i % 10 == 0) {
                logger.rollback();
            } else if ( i % 11 != 0 ) {
                logger.commit();
            }
        }
        Assert.assertTrue(files > 0);
        logger.close();
        
        FileRedoReader reader = new FileRedoReader(logger.getLocation());
        reader.analyse();

        
    }
}
