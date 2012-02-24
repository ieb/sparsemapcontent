package uk.co.tfd.sm.authn.openid;

import org.junit.Assert;
import org.junit.Test;
import org.openid4java.OpenIDException;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.html.HtmlResult;
import org.openid4java.discovery.yadis.YadisException;

import uk.co.tfd.sm.authn.openid.HTMLYadisDiscoveryParser;

public class HTMLDiscoveryParserTest {
    
    
    @Test
    public void test() throws DiscoveryException {
        HTMLDiscoveryParser parser = new HTMLDiscoveryParser();
        HtmlResult result = new HtmlResult();
        parser.parseHtml(" <head attr=\"bad\" \n " +
        		"   > <junk/> <title> \n" +
        		"  <link rel=\"yaya\" href=\"ignore\" /> " +
                        "  <link rel=\"openid.server\" href=\"http://host1/linkA\" /> " +
                        "  <LINK \n href=\"http://host1/delegate2\" rel=\"openid.delegate\" ></link> " +
                        "  <link rel=\"openid2.provider\" href=\"http://host1/endpoint1\" /> " +
                        "  <link HREF=\"http://host1/endpoint2\" rel=\"openid2.local_id\" /> " +
        		" </head>" +
        		"  <head>" +
                        "  <link rel=\"yaya\" href=\"ignore\" /> " +
                        "  <link rel=\"openid.server\" href=\"http://host1/linkA\" /> " +
                        "  <LINK \n href=\"http://host1/delegate2\" rel=\"openid.delegate\" ></link> " +
                        "  <link rel=\"openid2.provider\" href=\"http://host1/endpoint1\" /> " +
                        "  <link HREF=\"http://host1/endpoint2\" rel=\"openid2.local_id\" /> " +
        		"   </head>", result);
        
        Assert.assertEquals("http://host1/linkA", result.getOP1Endpoint().toString());
        Assert.assertEquals("http://host1/delegate2", result.getDelegate1().toString());
        Assert.assertEquals("http://host1/endpoint1", result.getOP2Endpoint().toString());
        Assert.assertEquals("http://host1/endpoint2", result.getDelegate2().toString());
    }

}
