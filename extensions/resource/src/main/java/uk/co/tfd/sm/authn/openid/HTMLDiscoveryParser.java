package uk.co.tfd.sm.authn.openid;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openid4java.OpenIDException;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.html.HtmlParser;
import org.openid4java.discovery.html.HtmlResult;

import com.google.common.collect.ImmutableSet;

public class HTMLDiscoveryParser implements HtmlParser {

    @Override
    public void parseHtml(String htmlData, HtmlResult result) throws DiscoveryException {
        Pattern head = Pattern.compile("\\<head.*?\\</head\\>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Pattern link = Pattern.compile("\\<link.*?\\>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Pattern linkRel = Pattern.compile("rel=\"(.*?)\"", Pattern.CASE_INSENSITIVE);
        Pattern linkHref = Pattern.compile("href=\"(.*?)\"", Pattern.CASE_INSENSITIVE);

        Matcher headMatch = head.matcher(htmlData);

        if (headMatch.find()) {
            Matcher linkMatcher = link.matcher(headMatch.group());
            while (linkMatcher.find()) {
                String linkTag = linkMatcher.group();
                Matcher linkRelMatch = linkRel.matcher(linkTag);
                if (linkRelMatch.find()) {
                    Matcher linkHrefMatcher = linkHref.matcher(linkTag);
                    if (linkHrefMatcher.find()) {
                        String href = linkHrefMatcher.group(1);
                        Set<String> terms = ImmutableSet.copyOf(StringUtils.split(linkRelMatch.group(1), " "));
                        if (terms.contains("openid.server")) {
                            if (result.getOP1Endpoint() != null) {
                                throw new DiscoveryException("More than one openid.server entries found",
                                        OpenIDException.DISCOVERY_HTML_PARSE_ERROR);
                            }
                            result.setEndpoint1(href);
                        }
                        if (terms.contains("openid.delegate")) {
                            if (result.getDelegate1() != null) {
                                throw new DiscoveryException("More than one openid.delegate entries found",
                                        OpenIDException.DISCOVERY_HTML_PARSE_ERROR);
                            }
                            result.setDelegate1(href);
                        }
                        if (terms.contains("openid2.provider")) {
                            if (result.getOP2Endpoint() != null) {
                                throw new DiscoveryException("More than one openid.server entries found",
                                        OpenIDException.DISCOVERY_HTML_PARSE_ERROR);
                            }
                            result.setEndpoint2(href);
                        }
                        if (terms.contains("openid2.local_id")) {
                            if (result.getDelegate2() != null) {
                                throw new DiscoveryException("More than one openid2.local_id entries found",
                                        OpenIDException.DISCOVERY_HTML_PARSE_ERROR);
                            }
                            result.setDelegate2(href);
                        }
                    }
                }
            }
        }
    }

}
