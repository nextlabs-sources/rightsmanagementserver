package com.nextlabs.rms.filters;

import com.nextlabs.rms.auth.*;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.pojo.ServiceProviderSetting;
import com.nextlabs.rms.repository.IRepository;
import com.nextlabs.rms.repository.RepositoryManager;
import com.nextlabs.rms.repository.exception.RepositoryException;

import org.apache.log4j.Logger;
import org.springframework.security.web.PortResolver;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

public class AuthFilter implements Filter {

	public static final String LOGGEDIN_USER = "LOGGEDIN_USER";
	
	public static final String USER_TEMP_DIR = "USER_TEMP_DIR";
	
	public static final String CACHED_FILE_UPLD = "CACHED_FILE_UPLD";
	
	private static final Logger logger = Logger.getLogger(AuthFilter.class);
	
	private static final String RMS_HOMEPAGE_URL = GlobalConfigManager.RMS_CONTEXT_NAME  +"/index.jsp";
	
	private static final String RMS_MANAGE_REPO_URL = RMS_HOMEPAGE_URL + "#/home/manageRepositories";
	
	private static final String RMS_SERVICE_PROVIDERS_URL = RMS_HOMEPAGE_URL + "#/home/serviceProviders";
	
	public static final String RMS_FILEUPLD_URI = GlobalConfigManager.RMS_CONTEXT_NAME + "/RMSViewer/UploadFile";
	
	public static final String GET_DOMAINS_URI =  GlobalConfigManager.RMS_CONTEXT_NAME + "/RMSViewer/GetDomains";
	
	public static final String LOGOUT_URI =  GlobalConfigManager.RMS_CONTEXT_NAME + "/RMSViewer/Logout";
	
	private static final String ACTION_LOGOUT = "logout";
	
	public static final String LOGIN_PAGE = "/login.jsp";
	
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {			

		req.setCharacterEncoding("UTF-8");
		String key;
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;
		logger.debug("Request URL: "+request.getRequestURI());

		String uri = request.getRequestURI();
		if(uri.startsWith(GlobalConfigManager.RMS_CONTEXT_NAME+'/'+GlobalConfigManager.TEMPDIR_NAME)){
			if(!uri.split("/")[3].equalsIgnoreCase(request.getSession().getId())){
				response.sendRedirect(GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp?errMsg="+URLEncoder.encode(RMSMessageHandler.getClientString("fileNotFound"),"UTF-8"));
				return;
			}
		}
		StringTokenizer pathTokenizer=new StringTokenizer(uri,"?");
		String path=pathTokenizer.nextToken();
		boolean httpEnabled=GlobalConfigManager.getInstance().getBooleanProperty(GlobalConfigManager.ENABLE_HTTP);
		if(!httpEnabled && !request.isSecure() && (path.equals(GlobalConfigManager.RMS_CONTEXT_NAME+LOGIN_PAGE) || 
				path.equals(GlobalConfigManager.RMS_CONTEXT_NAME+"/register.jsp")|| path.equals(GlobalConfigManager.RMS_CONTEXT_NAME+"/SharepointApp.jsp"))){
			response.sendRedirect(GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp?errMsg="+URLEncoder.encode(RMSMessageHandler.getClientString("httpAccessErr"),"UTF-8"));
			return;
		}
		String action = request.getParameter("action");
		if(request.getSession().getAttribute(LOGGEDIN_USER)!=null ||
				request.getSession().getAttribute(ConfigManager.SP_OAUTH_TOKEN)!=null ||
				uri.matches(GlobalConfigManager.RMS_CONTEXT_NAME+"/(css|fonts|img|js|resources|help)/.*") ||
				uri.equals(GlobalConfigManager.RMS_CONTEXT_NAME+LOGIN_PAGE) ||
				uri.equals(LOGOUT_URI) ||
				uri.matches(GlobalConfigManager.RMS_CONTEXT_NAME +"/ui/(app|config|css|lib)/.*") ||
				uri.equals(GET_DOMAINS_URI) ||
				uri.equals(GlobalConfigManager.RMS_CONTEXT_NAME+"/register.jsp") ||
				uri.equals(GlobalConfigManager.RMS_CONTEXT_NAME+"/SharepointApp.jsp") ||
				uri.equals(GlobalConfigManager.RMS_CONTEXT_NAME+"/ShowError.jsp") ||
				uri.equals(GlobalConfigManager.RMS_CONTEXT_NAME+"/ExternalViewer.jsp") ||
				uri.equals(GlobalConfigManager.RMS_CONTEXT_NAME+"/RMSViewer/SendMail") ||
                uri.startsWith(GlobalConfigManager.RMS_CONTEXT_NAME + OktaAuthServlet.OKTA_SERVLET) ||
                uri.startsWith(GlobalConfigManager.RMS_CONTEXT_NAME + SAMLManagerServlet.SAML_SERVLET) ||
				uri.endsWith("/SharePointAuth/AuthStart") || 
				uri.endsWith("/SharePointAuth/AuthFinish") ||
				uri.endsWith("/SharePointAuth/OnPremiseAuth") ||
				uri.endsWith("/RMSViewer/DisplayTCFile") ||
				uri.endsWith("/RMSViewer/DisplaySPOnPremFile") ||
                uri.startsWith("/RMS/service")) {
//				||uri.equals(ConfigManager.RMS_CONTEXT_NAME+"/WrapperSvc/GetHTMLWithFile")){
			//no authentication needed..proceed ahead
			if(uri.equals(GlobalConfigManager.RMS_CONTEXT_NAME+"/RMSViewer/Login")
					|| (request.getSession().getAttribute(LOGGEDIN_USER)!=null 
					&& uri.equals(GlobalConfigManager.RMS_CONTEXT_NAME+LOGIN_PAGE)
							&& !ACTION_LOGOUT.equalsIgnoreCase(action))){
				response.sendRedirect(RMS_HOMEPAGE_URL);
				return;				
			}
			if (uri.startsWith("/RMS/service") && (!WebServiceAuthManager.authenticateWebSvc(request))) {
				response.sendError(401);
				return;
			}
			chain.doFilter(req, res);
			return;
		}
		if(uri.equals(GlobalConfigManager.RMS_CONTEXT_NAME+"/RMSViewer/Login")){
			handleLoginRequest(response, chain, request);
			return;
		}else{
			if(uri.equalsIgnoreCase(GlobalConfigManager.RMS_CONTEXT_NAME)||
					uri.equalsIgnoreCase(GlobalConfigManager.RMS_CONTEXT_NAME+"/")){
                response.sendRedirect(GlobalConfigManager.RMS_CONTEXT_NAME + LOGIN_PAGE);
				return;
			}else{
				//cache the request and redirect to login page.
				ServletContext context = request.getSession().getServletContext();
				key=System.currentTimeMillis()+UUID.randomUUID().toString();
				PortResolver portResolver = new PortResolverImpl();
				DefaultSavedRequest cachedReq = new DefaultSavedRequest(request, portResolver);
				CachedRequestWrapper reqWrapper = new CachedRequestWrapper(cachedReq, request);
				context.setAttribute(key, reqWrapper);
				logger.debug("Key for putting request in cache is "+key);
				if (isAjax(request) || request.getRequestURI().endsWith("UploadAndView")) {
				    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				    response.setHeader("Location", response.encodeRedirectURL(GlobalConfigManager.RMS_CONTEXT_NAME+ LOGIN_PAGE + "?action=session_timed_out"));
				    response.flushBuffer();
				    return;
				}
				request.getSession().setAttribute("cache_key", key);
                response.sendRedirect(GlobalConfigManager.RMS_CONTEXT_NAME + LOGIN_PAGE);
				return;
			}
		}
	}

	private static boolean isAjax(HttpServletRequest request) {
	    return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
	}
	
	private void handleLoginRequest(HttpServletResponse res, FilterChain chain,
			HttpServletRequest request) throws ServletException, IOException {
		logger.info("Attempting to login");
		HttpSession session = request.getSession(true);
		String cacheKey=(String)session.getAttribute("cache_key");
		RMSUserPrincipal principal = null;
		try {
			logger.info("Authenticating user");
			principal = authenticateUser(request, res);				
		} catch (RMSLoginException le) {
			logger.error("Error occurred while authenticating user:"+le.getMessage(),le);
			redirectToLoginPage(res, request, cacheKey, le.getMessage());
			return;				
		} catch(Exception e){
			logger.error("Error occurred while authenticating user:"+e.getMessage(),e);
			redirectToLoginPage(res, request, cacheKey, RMSMessageHandler.getClientString("authError"));
			return;
		}
		//Login successful. Redirect to cached request or home page
		redirectAfterLogin(session, cacheKey, res, principal);
//      chain.doFilter(cachedReq, res);
	}
	
	public static void redirectAfterLogin(HttpSession session, String cacheKey, HttpServletResponse response, RMSUserPrincipal principal) throws IOException{
	    addSessionParameters(session, principal);
	    ServletContext context = session.getServletContext();
        CachedRequestWrapper cachedReq = null;
        if(cacheKey!=null){
            cachedReq = (CachedRequestWrapper)context.getAttribute(cacheKey);
        }
        
        if(cachedReq == null){
            try {
                if(RMSUserPrincipal.ADMIN_USER.equalsIgnoreCase(principal.getRole())){
                    Map<String, ServiceProviderSetting> availableServiceProviders = ConfigManager.getInstance(principal.getTenantId()).getServiceProviderMap();
                    if(availableServiceProviders.isEmpty()) {
                        response.sendRedirect(RMS_SERVICE_PROVIDERS_URL);
                        return;
                    }
                } 
                List<IRepository> repoList = RepositoryManager.getInstance().getRepositoryList(principal, false);
                if(repoList.isEmpty()) {
                    response.sendRedirect(RMS_MANAGE_REPO_URL);
                    return;
                } 
            } catch (RepositoryException e) {
                logger.error(e);
                response.sendRedirect(RMS_HOMEPAGE_URL);
                return;
            }
            response.sendRedirect(RMS_HOMEPAGE_URL);
            return;
        }else{
            cachedReq.setSession(session);
            context.removeAttribute(cacheKey);
            if(!cachedReq.getRequestURI().endsWith(RMS_FILEUPLD_URI)){
                String redirectURI =cachedReq.getRequestURI();
                String queryString = cachedReq.getQueryString();
                if(queryString!=null && queryString.length()>0){
                    redirectURI = redirectURI + "?" + queryString;
                }
                response.sendRedirect(redirectURI);
                return;
            }else{
                @SuppressWarnings("unchecked")
                Map<String, String[]> paramMap = cachedReq.getParameterMap();
                session.setAttribute(CACHED_FILE_UPLD, paramMap);
                response.sendRedirect(RMS_FILEUPLD_URI+"?cached=true");
                return;
            }
        }
	}

	public static void addSessionParameters(HttpSession session,
			RMSUserPrincipal principal) {
		session.setAttribute(LOGGEDIN_USER, principal);
		int timeout=ConfigManager.getInstance(principal.getTenantId()).getIntProperty(ConfigManager.SESSION_TIMEOUT_MINS)*60;
		if(timeout<=0){
			logger.info("No value set or invalid value set for Session timeout interval. Using default value instead");
			timeout=60*60;
		}
		logger.info("Session timeout interval set to "+timeout+"seconds");
		session.setMaxInactiveInterval(timeout);
		String userTempDir = GlobalConfigManager.getInstance().getTempDir()+File.separator+session.getId();
		File tempDir = new File(userTempDir);
		if(!tempDir.exists()){
			tempDir.mkdirs();
		}
		session.setAttribute(USER_TEMP_DIR, userTempDir);
	}

	public static void redirectToLoginPage(HttpServletResponse response,
			HttpServletRequest request, String cacheKey, String errorMsg)
			throws ServletException, IOException {
		if (isAjax(request)) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			Map<String, String> result = new HashMap<>(2);
			result.put("status", String.valueOf(HttpServletResponse.SC_FORBIDDEN));
			result.put("error", errorMsg);
			JsonUtil.writeJsonToResponse(result, response);
			return;
		} else {			
			StringBuilder redirectURL = new StringBuilder(GlobalConfigManager.RMS_CONTEXT_NAME).append(LOGIN_PAGE).append("?cache_key=").append(cacheKey).append("&msg=").append(URLEncoder.encode(errorMsg, "UTF-8"));
			response.sendRedirect(redirectURL.toString());
			return;
		}
	}
	
	
	private RMSUserPrincipal authenticateUser(HttpServletRequest request, ServletResponse response) throws Exception{
			String userName=request.getParameter("userName");
			String password=request.getParameter("password");
			String domainName=request.getParameter("domainName");
			logger.debug("Getting LoginContext");
			RMSLoginContext ctxt = LoginContextFactory.getInstance().getContext(domainName);
			if (ctxt == null) {
				logger.error("Unknown domain name: " + domainName);
				throw new RMSLoginException(RMSMessageHandler.getClientString("authError"));
			}
			boolean res = ctxt.authenticate(userName, password);
			logger.info("Authentication result for user " + userName+ " : "+res);
			if(res){
				return ctxt.getUserPrincipal();
			}
			return null;
	}


	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}
}
