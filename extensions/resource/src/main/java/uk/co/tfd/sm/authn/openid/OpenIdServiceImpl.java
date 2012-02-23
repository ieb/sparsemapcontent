package uk.co.tfd.sm.authn.openid;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
import org.sakaiproject.nakamura.api.memory.Cache;
import org.sakaiproject.nakamura.api.memory.CacheManagerService;
import org.sakaiproject.nakamura.api.memory.CacheScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

@Component(immediate = true, metatype = true)
@Service(value = OpenIdService.class)
public class OpenIdServiceImpl implements OpenIdService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenIdServiceImpl.class);

    private ConsumerManager consumerManager;

    @Reference
    protected CacheManagerService cacheManagerService;

    private Cache<DiscoveryInformation> discoveryCache;

    private Map<String, String> discoveryEndPoints;

    @Property(value = { "google:https://www.google.com/accounts/o8/id", "yahoo:https://me.yahoo.com",
            "paypal:https://www.paypal.com/webapps/auth/server", "aol:https://openid.aol.com/", "hyves:http://www.hyves.nl/",
            "myid:https://myid.net/", "myopenid:https://myopenid.com/", "myspace:https://api.myspace.com/openid",
            "ntt:https://i.mydocomo.com/", "verisign:https://pip.verisignlabs.com/" })
    private static final String OPEN_ID_DISCOVERY = "openid-discovery";

    @Activate
    protected void activate(Map<String, Object> properties) throws ConsumerException, MessageException {
        modified(properties);
        Message.addExtensionFactory(OAuthMessageExtensionFactory.class);
    }

    @Modified
    protected void modified(Map<String, Object> properties) throws ConsumerException {
        consumerManager = new ConsumerManager();
        discoveryCache = cacheManagerService.getCache(this.getClass().getName(), CacheScope.INSTANCE);
        String[] discoveryUrls = (String[]) properties.get(OPEN_ID_DISCOVERY);
        Builder<String, String> b = ImmutableMap.builder();
        for (String discoveryUrl : discoveryUrls) {
            String[] kv = StringUtils.split(discoveryUrl, ":", 2);
            b.put(kv[0], kv[1]);
        }
        discoveryEndPoints = b.build();
    }

    @Override
    public Map<String, Object> getIdentity(HttpServletRequest request) {
        // only consider OpenId if the request is a get and there is a _oidk
        // parameter in the url.
        if ("_GET_POST_".indexOf(request.getMethod()) > 0) {
            String key = request.getParameter("_oidk");
            if (key != null && discoveryCache.containsKey(key)) {
                Map<?, ?> pm = request.getParameterMap();
                for (Entry<?, ?> e : pm.entrySet()) {
                    LOGGER.info("Param {} Val {} ", e.getKey(), Arrays.toString((Object[]) e.getValue()));
                }
                // NB OpenID redirects must come back to the same server.
                // When configuring a Load balancer make certain that happens.
                // The discovery information may only be used once.
                DiscoveryInformation discoveryInformation = discoveryCache.get(key);
                discoveryCache.remove(key);

                ParameterList response = new ParameterList(request.getParameterMap());
                StringBuffer receivingURL = request.getRequestURL();
                String queryString = request.getQueryString();
                if (queryString != null && queryString.length() > 0)
                    receivingURL.append("?").append(request.getQueryString());

                VerificationResult verification;
                try {
                    LOGGER.info("Performing verification on URL {} ", receivingURL.toString());
                    verification = consumerManager.verify(receivingURL.toString(), response, discoveryInformation);
                    Identifier verified = verification.getVerifiedId();
                    if (verified != null) {
                        AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();

                        // could extract more information at this point, like an
                        // OAuth token
                        // to be stored on the users authorizable, or somewhere
                        // else.
                        FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);

                        Builder<String, Object> b = ImmutableMap.builder();
                        @SuppressWarnings("unchecked")
                        Map<String, List<String>> attributes = fetchResp.getAttributes();
                        for (Entry<String, List<String>> e : attributes.entrySet()) {
                            List<String> v = e.getValue();
                            if (v != null) {
                                if (v.size() == 1) {
                                    b.put("attr." + e.getKey(), v.get(0));
                                } else if (v.size() > 1) {
                                    b.put("attr." + e.getKey(), v.toArray(new String[v.size()]));
                                }
                            }
                            if ("email".equals(e.getKey())) {
                                b.put("email", v.get(0));
                                b.put("username", v.get(0));
                            }
                        }

                        OAuthResponse oauthResponse = (OAuthResponse) authSuccess
                                .getExtension(OAuthMessageExtensionFactory.OAUTH_EXTENSION_NS);
                        if (oauthResponse == null) {
                            LOGGER.info("No OAuth Response ");
                        } else {
                            LOGGER.info("Got OAuth Response ");
                            String[] scope = oauthResponse.getScope();
                            if (scope != null) {
                                b.put("oauth.scope", scope);
                            }
                            String token = oauthResponse.getToken();
                            if (token != null) {
                                b.put("oauth.token", token);
                            }
                        }
                        b.put("eid", authSuccess.getIdentity());
                        return b.build();
                    }
                } catch (MessageException e) {
                    LOGGER.error(e.getMessage(), e);
                } catch (DiscoveryException e) {
                    LOGGER.error(e.getMessage(), e);
                } catch (AssociationException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        return null;
    }

    public String getAuthRedirectUrl(String userSuppliedString, String returnToUrl) {
        try {
            if (userSuppliedString == null) {
                return null;
            }

            String endpoint = discoveryEndPoints.get(userSuppliedString);
            if (endpoint == null) {
                return null;
            }

            List<?> discoveries = consumerManager.discover(endpoint);
            DiscoveryInformation discoveryInformation = consumerManager.associate(discoveries);

            String key = StorageClientUtils.getUuid();
            discoveryCache.put(key, discoveryInformation);

            if (returnToUrl == null) {
                returnToUrl = "/";
            }
            if (returnToUrl.contains("?")) {
                returnToUrl = returnToUrl + "&_oidk=" + key;
            } else {
                returnToUrl = returnToUrl + "?_oidk=" + key;
            }
            AuthRequest authReq = consumerManager.authenticate(discoveryInformation, returnToUrl);

            // Attribute Exchange example: fetching the 'email' attribute
            FetchRequest fetch = FetchRequest.createFetchRequest();
            fetch.addAttribute("email", "http://schema.openid.net/contact/email", // type
                                                                                  // URI
                    true); // required
            fetch.addAttribute("firstname", "http://schema.openid.net/contact/firstname", // type
                                                                                          // URI
                    true); // required

            fetch.addAttribute("lastname", "http://schema.openid.net/contact/lastname", // type
                                                                                        // URI
                    true); // required
            authReq.addExtension(fetch);

            // Perform OAuth at the same time. Wont work if not registered.
            authReq.addExtension(new OAuthRequest("localhost:8080", new String[] { "http://docs.google.com/feeds/",
                    "http://spreadsheets.google.com/feeds/" }));

            // Add extensions here, eg email or request for OAuth token.
            return authReq.getDestinationUrl(true);
        } catch (DiscoveryException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (MessageException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (ConsumerException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

}
