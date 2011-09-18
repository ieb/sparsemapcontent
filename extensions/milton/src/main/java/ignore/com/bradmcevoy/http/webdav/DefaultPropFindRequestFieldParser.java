package ignore.com.bradmcevoy.http.webdav;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.bradmcevoy.http.webdav.PropFindRequestFieldParser;
import com.bradmcevoy.http.webdav.PropFindSaxHandler;
import com.bradmcevoy.io.StreamUtils;
// This was modified to correct bad xml processing.
/**
 * Simple implmentation which just parses the request body. If no xml is present
 * it will return an empty set.
 *
 * Note this generally shouldnt be used directly, but should be wrapped by
 * MSPropFindRequestFieldParser to support windows clients.
 *
 * @author brad
 */
public class DefaultPropFindRequestFieldParser implements PropFindRequestFieldParser {

    private static final Logger log = LoggerFactory.getLogger( DefaultPropFindRequestFieldParser.class );

    public DefaultPropFindRequestFieldParser() {
    }

    public ParseResult getRequestedFields( InputStream in ) {
        try {
            final Set<QName> set = new LinkedHashSet<QName>();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            StreamUtils.readTo( in, bout, false, true );
            byte[] arr = bout.toByteArray();
            if( arr.length > 1 ) {
                ByteArrayInputStream bin = new ByteArrayInputStream( arr );
                XMLReader reader = XMLReaderFactory.createXMLReader();
                PropFindSaxHandler handler = new PropFindSaxHandler();
                reader.setContentHandler( handler );
                try {
                    reader.parse( new InputSource( bin ) );
                    if( handler.isAllProp() ) {
                        return new ParseResult( true, set );
                    } else {
                        set.addAll( handler.getAttributes().keySet() );
                    }
                } catch( IOException e ) {
                    log.warn( "exception parsing request body", e );
                    // ignore
                } catch( SAXException e ) {
                    log.warn( "exception parsing request body", e );
// ieb modification start
                    throw new RuntimeBadRequestException(e.getMessage(), e);
// ieb modificatoin end
                }
            }
            return new ParseResult( false, set );
        } catch ( RuntimeException ex ) {
        	throw ex;
        } catch( Exception ex ) {
            throw new RuntimeException( ex );
        }
    }
}
