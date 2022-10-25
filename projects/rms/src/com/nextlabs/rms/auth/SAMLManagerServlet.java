package com.nextlabs.rms.auth;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;
import com.nextlabs.rms.util.StringUtils;
import com.onelogin.saml2.Auth;
import com.onelogin.saml2.exception.SettingsException;
import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.filters.AuthFilter;

import com.nextlabs.rms.locale.RMSMessageHandler;

public class SAMLManagerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String SAML_SERVLET = "/SAML";
    
    public static final String SAML_START_URL = SAML_SERVLET + "/AuthStart";

    // This is the assertion_consumer_service URL
    public static final String SAML_FINISH_URL = SAML_SERVLET + "/AuthFinish";
    
    // This is the entity_id 
    public static final String SAML_METADATA_URL = SAML_SERVLET + "/Metadata";

    private static Logger logger = Logger.getLogger(SAMLManagerServlet.class);
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            String uri = request.getRequestURI();
            if (uri.equals(GlobalConfigManager.RMS_CONTEXT_NAME + SAMLManagerServlet.SAML_START_URL)) {
                SAMLManagerServlet.startAuth(request, response);
            } else if (uri.equals(GlobalConfigManager.RMS_CONTEXT_NAME + SAMLManagerServlet.SAML_METADATA_URL)) {
                SAMLManagerServlet.getMetadata(request, response);
            }
        } catch (IOException | ServletException e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            String uri = request.getRequestURI();
            if (uri.equals(GlobalConfigManager.RMS_CONTEXT_NAME + SAMLManagerServlet.SAML_FINISH_URL)) {
                SAMLManagerServlet.finishAuth(request, response);
            }
        } catch (IOException | ServletException e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    public static void getMetadata(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Properties samlProps = GlobalConfigManager.getInstance().getSAMLConfig();
        if(samlProps == null) {
            logger.error("SAML authentication is not configured");
            HttpSession session = request.getSession(true);
            String cacheKey = (String) session.getAttribute("cache_key");
            String errMsg = RMSMessageHandler.getClientString("samlAuthErr");
            AuthFilter.redirectToLoginPage(response, request, cacheKey, errMsg);
            return;
        }
        Saml2Settings settings = new SettingsBuilder().fromProperties(samlProps).build();
        settings.setSPValidationOnly(true);
        try {
            String metadata = settings.getSPMetadata();
            List<String> errors = Saml2Settings.validateMetadata(metadata);
            if (errors.isEmpty()) {
                response.getWriter().write(metadata);
            } else {
                StringBuilder sb = new StringBuilder();
                for (String error : errors) {
                    sb.append(error).append(System.lineSeparator());
                }
                logger.error("Error occurred while validating metadata: " + sb.toString());
                response.getWriter().write("Error occurred while validating SP metadata.");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            response.getWriter().write("Error occurred while getting SP metadata.");
        }        
    }


    public static void startAuth(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            Properties samlProps = GlobalConfigManager.getInstance().getSAMLConfig();
            if(samlProps == null) {
                logger.error("SAML authentication is not configured");
                HttpSession session = request.getSession(true);
                String cacheKey = (String) session.getAttribute("cache_key");
                String errMsg = RMSMessageHandler.getClientString("samlAuthErr");
                AuthFilter.redirectToLoginPage(response, request, cacheKey, errMsg);
                return;
            }
            Saml2Settings settings = new SettingsBuilder().fromProperties(samlProps).build();
            Auth auth = new Auth(settings, request, response);
            auth.login();
        } catch (SettingsException e) {
            logger.error(e.getMessage(), e);
            HttpSession session = request.getSession(true);
            String cacheKey = (String) session.getAttribute("cache_key");
            String errMsg = RMSMessageHandler.getClientString("samlAuthErr");
            AuthFilter.redirectToLoginPage(response, request, cacheKey, errMsg);
            return;
        }
    }

    public static void finishAuth(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(true);
        String cacheKey = (String) session.getAttribute("cache_key");
        try {
            Properties samlProps = GlobalConfigManager.getInstance().getSAMLConfig();
            if(samlProps == null) {
                logger.error("SAML authentication is not configured");
                String errMsg = RMSMessageHandler.getClientString("samlAuthErr");
                AuthFilter.redirectToLoginPage(response, request, cacheKey, errMsg);
                return;
            }
            Saml2Settings settings = new SettingsBuilder().fromProperties(samlProps).build();
            Auth auth = new Auth(settings, request, response);
            try {
                auth.processResponse();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                String errMsg = RMSMessageHandler.getClientString("samlAuthErr");
                AuthFilter.redirectToLoginPage(response, request, cacheKey, errMsg);
                return;
            }

            if (!auth.isAuthenticated()) {
                logger.error("Not authenticated.");
                String errMsg = RMSMessageHandler.getClientString("samlAuthErr");
                AuthFilter.redirectToLoginPage(response, request, cacheKey, errMsg);
                return;
            }

            List<String> errors = auth.getErrors();
            StringBuilder sb = new StringBuilder();
            if (!errors.isEmpty()) {
                for (String error : errors) {
                    sb.append(error).append("\n");
                }
                String errMsg = sb.toString();
                logger.error(errMsg);
                AuthFilter.redirectToLoginPage(response, request, cacheKey, RMSMessageHandler.getClientString("samlAuthErr"));
                return;
            }

            Map<String, List<String>> userAttributes = auth.getAttributes();
            RMSUserPrincipal principal = createUserPrincipal(auth.getNameId(), userAttributes);
            logger.debug("Created a new user principal \n UseName: " + principal.getUserName() + "/n SAML Name ID: "
                    + principal.getUid() + "/n LoginContext: " + principal.getLoginContext());
            AuthFilter.redirectAfterLogin(session, cacheKey, response, principal);
        } catch (SettingsException e) {
            logger.error(e.getMessage(), e);
            String errMsg = RMSMessageHandler.getClientString("samlAuthErr");
            AuthFilter.redirectToLoginPage(response, request, cacheKey, errMsg);
            return;
        }
    }

    private static RMSUserPrincipal createUserPrincipal(String uid, Map<String, List<String>> userAttributes) {
        RMSUserPrincipal user = new RMSUserPrincipal();
        user.setUid(uid);
        if (userAttributes.get("email") == null || !StringUtils.hasText(userAttributes.get("email").get(0))) {
            user.setUserName(uid);
        } else {
            user.setUserName(userAttributes.get("email").get(0));
        }
        if(user.getUserName().equalsIgnoreCase(GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.SAML_RMS_ADMIN))){
            user.setRole(RMSUserPrincipal.ADMIN_USER);
        }
        user.setAuthProvider(RMSUserPrincipal.AUTH_SAML);
        user.setDomain(getDomainName(GlobalConfigManager.getInstance().getStringProperty(GlobalConfigManager.SAML_IDP_ENTITY_ID)));
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
}
