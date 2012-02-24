package uk.co.tfd.sm.authn.openid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openid4java.discovery.yadis.YadisException;
import org.openid4java.discovery.yadis.YadisHtmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTMLYadisDiscoveryParser implements YadisHtmlParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTMLYadisDiscoveryParser.class);

    @Override
    public String getHtmlMeta(String input) throws YadisException {
        Pattern head = Pattern.compile("\\<head.*?\\</head\\>",Pattern.CASE_INSENSITIVE | Pattern.DOTALL );
        Pattern meta = Pattern.compile("\\<meta.*?http-equiv=\"X-XRDS-Location\".*?\\>", Pattern.CASE_INSENSITIVE| Pattern.DOTALL);
        Pattern url = Pattern.compile("content=\"(.*?)\"", Pattern.CASE_INSENSITIVE);

        Matcher headMatch = head.matcher(input);
        
        if ( headMatch.find() ) {
            Matcher metaMatcher = meta.matcher(headMatch.group());
            while( metaMatcher.find()) {
                
                Matcher urlMatcher = url.matcher(metaMatcher.group());
                if ( urlMatcher.find() ) {
                    return urlMatcher.group(1);
                }
            } 
        } else {
            LOGGER.info("No head found in {} ", input);
        }
        return null;
    }

}
