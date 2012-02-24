package uk.co.tfd.sm.authn.openid;

import org.junit.Assert;
import org.junit.Test;
import org.openid4java.discovery.yadis.YadisException;

import uk.co.tfd.sm.authn.openid.HTMLYadisDiscoveryParser;

public class HTMLYadisDiscoveryParserTest {
    
    
    @Test
    public void test() throws YadisException {
        HTMLYadisDiscoveryParser parser = new HTMLYadisDiscoveryParser();
        Assert.assertEquals("test url",parser.getHtmlMeta("   <head> <meta http-equiv=\"X-XRDS-Location\" content=\"test url\" /> </head>"));
        Assert.assertEquals("test url",parser.getHtmlMeta("   <head>\n <meta http-equiv=\"X-XRDS-Location\" content=\"test url\" /> </head>"));
        Assert.assertEquals("test url",parser.getHtmlMeta("   <head http-equiv=\"X-XRDS-Location\">\n <meta\nhttp-equiv=\"X-XRDS-Location\"\ncontent=\"test url\"\n/>\n</head>"));
        Assert.assertEquals("test url",parser.getHtmlMeta("<HeAd http-equiv=\"X-XRDS-Location\">\n <META\nhttp-equiv=\"x-xrds-location\"\nconteNt=\"test url\"\n>\n</hEAd>"));
        Assert.assertEquals("test url",parser.getHtmlMeta("<HeAd http-equiv=\"X-XRDS-Location\">\n <META\n conteNt=\"test url\" http-equiv=\"x-xrds-location\"\n ></MeTA>\n</hEAd>"));
    }

}
