package com.nextlabs.rms.auth;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.nextlabs.rms.util.RestClient;
import com.nextlabs.rms.util.RestClient.RestClientException;
import com.nextlabs.rms.util.StringUtils;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.config.OktaServerInfo;
import com.nextlabs.rms.filters.AuthFilter;

import com.nextlabs.rms.locale.RMSMessageHandler;

public class OktaAuthServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;
	
	public static final String OKTA_SERVLET = "/OktaAuth";
	
	public static final String OKTA_AUTH_START_URL = OKTA_SERVLET +"/AuthStart";
	
	public static final String OKTA_AUTH_FINISH_URL = OKTA_SERVLET + "/AuthFinish";

	private static Logger logger = Logger.getLogger(OktaAuthServlet.class);
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
	    try {
	        String uri = request.getRequestURI();
	        if (uri.equals(GlobalConfigManager.RMS_CONTEXT_NAME + OktaAuthServlet.OKTA_AUTH_START_URL)) {
	            OktaAuthServlet.startAuth(request, response);
	        } else if(uri.equals(GlobalConfigManager.RMS_CONTEXT_NAME + OktaAuthServlet.OKTA_AUTH_FINISH_URL)){
	            OktaAuthServlet.finishAuth(request, response);
	        }
	    } catch (IOException | ServletException e) {
	        logger.error(e.getMessage(), e);
	    }
	}
	    
    public static void startAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {

        OktaServerInfo config = GlobalConfigManager.getInstance().getOktaServerInfo();
        String oktaServer = config.getServerUrl();
        String authorizationServerId = config.getAuthServerId();
        String appId = config.getClientId();
        String port = request.getServerPort() == 443 || request.getServerPort() == 80 ? "" : ":" + request.getServerPort();
        String redirectUri = new StringBuilder(request.getScheme()).append("://").append(request.getServerName()).append(port).append(request.getContextPath()).append(OKTA_AUTH_FINISH_URL).toString();
        StringBuilder sb = new StringBuilder(oktaServer);
        sb.append("/oauth2/").append(authorizationServerId);
        sb.append("/v1/authorize?client_id=");
        sb.append(appId).append("&redirect_uri=");
        sb.append(URLEncoder.encode(redirectUri, "UTF-8"));
        sb.append("&response_type=code&display=popup&scope=openid email profile");

        JsonObject state = new JsonObject();
        sb.append("&state=").append(URLEncoder.encode(state.toString(), "UTF-8"));
        
        String url = sb.toString();
        if (logger.isTraceEnabled()) {
            logger.trace("Initializing Okta OAuth Request: " + url);
        }
        response.sendRedirect(url);
    }
	
    @SuppressWarnings("unchecked")
    public static void finishAuth(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        HttpSession session = request.getSession(true);
        String cacheKey = (String)session.getAttribute("cache_key");        
        
        String error = request.getParameter("error");
        if (StringUtils.hasText(error)) {
            logger.error(error);
            String errMsg = RMSMessageHandler.getClientString("oktaAuthErr");
            AuthFilter.redirectToLoginPage(response, request, cacheKey, errMsg);
            return;
        }

        Gson gson = new Gson();
        Properties prop = new Properties();
        prop.setProperty("Content-Type", "application/x-www-form-urlencoded");

        OktaServerInfo config = GlobalConfigManager.getInstance().getOktaServerInfo();
        String oktaServer = config.getServerUrl();
        String authorizationServerId = config.getAuthServerId();
        String authServerUrl = new StringBuilder(oktaServer).append("/oauth2/").append(authorizationServerId).toString();
        String appId = config.getClientId();
        String appSecret = config.getClientSecret();
        String port = request.getServerPort() == 443 || request.getServerPort() == 80 ? "" : ":" + request.getServerPort();
        String redirectUri = new StringBuilder(request.getScheme()).append("://").append(request.getServerName()).append(port).append(request.getContextPath()).append(OKTA_AUTH_FINISH_URL).toString();
        
        StringBuilder params = new StringBuilder(125);
        params.append("client_id=").append(appId);
        params.append("&client_secret=").append(appSecret);
        params.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, "UTF-8"));
        params.append("&code=").append(request.getParameter("code"));
        params.append("&grant_type=authorization_code");
        
        String ret = null;
        try {
            ret = RestClient.post(authServerUrl + "/v1/token?", prop, params.toString());
        } catch(RestClientException e) {
            logger.error(e.getStatusCode() + ": " + e.getMessage(), e);
            String errMsg = RMSMessageHandler.getClientString(e.getStatusCode() == 429 ? "login.server.busy" : "login.session_expired");
            AuthFilter.redirectToLoginPage(response, request, cacheKey, errMsg);
            return;
        }
        OktaToken oktaToken = gson.fromJson(ret, OktaToken.class);
        String accessToken = oktaToken.getAccessToken();
        
        prop = new Properties();
        prop.setProperty("Content-Type", "application/x-www-form-urlencoded");
        params = new StringBuilder(125);
        params.append("client_id=").append(appId);
        params.append("&client_secret=").append(appSecret);
        params.append("&token_type_hint=access_token&token=").append(accessToken);
   
        try {
            ret = RestClient.post(authServerUrl + "/v1/introspect?", prop, params.toString());
        } catch(RestClientException e) {
            logger.error(e.getStatusCode() + ": " + e.getMessage(), e);
            String errMsg = RMSMessageHandler.getClientString(e.getStatusCode() == 429 ? "login.server.busy" : "oktaAuthErr");
            AuthFilter.redirectToLoginPage(response, request, cacheKey, errMsg);
            return;
        }

        TokenData data = gson.fromJson(ret, TokenData.class);
        if (!data.isActive()) {
            String errMsg = RMSMessageHandler.getClientString("login.session_expired");
            AuthFilter.redirectToLoginPage(response, request, cacheKey, errMsg);
            return;
        }

        prop = new Properties();
        prop.setProperty("Authorization", "Bearer " + accessToken);
        try {
            ret = RestClient.get(authServerUrl + "/v1/userinfo", prop, false);
        } catch(RestClientException e) {
            logger.error(e.getStatusCode() + ": " + e.getMessage(), e);
            String errMsg = RMSMessageHandler.getClientString(e.getStatusCode() == 429 ? "login.server.busy" : "oktaAuthErr");
            AuthFilter.redirectToLoginPage(response, request, cacheKey, errMsg);
            return;
        }

        Map<String, Object> profile = gson.fromJson(ret, Map.class);
        Map<String, List<String>> userAttributes = new HashMap<>();
        for(Map.Entry<String, Object> entry : profile.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if(!(value instanceof List<?>)) {
                userAttributes.put(key, Arrays.asList(String.valueOf(value)));    
            } else {
                List<String> list = new ArrayList<>();
                List<Object> objList = (List<Object>)value;
                for (Object o: objList) {
                    list.add(String.valueOf(o));
                }
                userAttributes.put(key, list);
            }
        }
        
        RMSUserPrincipal principal = createUserPrincipal(config, data.getUid(), userAttributes);
        logger.debug("Created a new user principal \n UseName: "+principal.getUserName()+"/n Okta UID: "+principal.getUid()+"/n LoginContext: "+principal.getLoginContext());
        AuthFilter.redirectAfterLogin(session, cacheKey, response, principal);
    }
    
    private static RMSUserPrincipal createUserPrincipal(OktaServerInfo serverInfo, String uid, Map<String, List<String>> userAttributes){
        RMSUserPrincipal user = new RMSUserPrincipal();
        user.setUid(uid);
        if(userAttributes.get("email") == null || !StringUtils.hasText(userAttributes.get("email").get(0))) {
            user.setUserName(uid);
        } else {
            user.setUserName(userAttributes.get("email").get(0));
        }
        if(user.getUserName().equalsIgnoreCase(GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.OKTA_RMS_ADMIN))){
            user.setRole(RMSUserPrincipal.ADMIN_USER);
        }
        user.setAuthProvider(RMSUserPrincipal.AUTH_OKTA);
        user.setDomain(getDomainName(serverInfo.getServerUrl()));
        user.setTenantId(ConfigManager.KMS_DEFAULT_TENANT_ID);
        user.setRMSUser(true);
        user.setUserAttributes(userAttributes);
        return user;
    }
    
    private static String getDomainName(String url) {
        try {
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (URISyntaxException e) {
            return url;
        }
    }
    
    public static final class TokenData {
        private boolean active;
        private long exp;
        private long iat;
        @SerializedName("auth_time")
        private long authTime;
        private String uid;

        public TokenData() {
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public long getExp() {
            return exp;
        }

        public void setExp(long exp) {
            this.exp = exp;
        }

        public long getIat() {
            return iat;
        }

        public void setIat(long iat) {
            this.iat = iat;
        }

        public long getAuthTime() {
            return authTime;
        }

        public void setAuthTime(long authTime) {
            this.authTime = authTime;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }
    }
    
    public class OktaToken {

        @SerializedName("access_token")
        private String accessToken;
        @SerializedName("id_token")
        private String idToken;
        @SerializedName("token_type")
        private String tokenType;
        @SerializedName("expires_in")
        private long expiresIn;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public long getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(long expiresIn) {
            this.expiresIn = expiresIn;
        }

        public String getIdToken() {
            return idToken;
        }

        public void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }
 
}
