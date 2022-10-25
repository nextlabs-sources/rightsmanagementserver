package com.nextlabs.rms.auth;



import org.apache.log4j.Logger;

import com.nextlabs.rms.services.application.RMSApplicationErrorCodes;
import com.nextlabs.rms.services.resource.LoginException;

import noNamespace.Error;
import noNamespace.LoginRequestType;
import noNamespace.LoginResponseType;
import noNamespace.LoginServiceDocument;
import noNamespace.LoginServiceType;

public class LoginServiceManager {

	private RMSLoginContext ctxt;
	private RMSUserPrincipal rmsUserPrincipal;
	private static Logger logger = Logger.getLogger(LoginServiceManager.class);
	public LoginServiceDocument authenticate(LoginServiceDocument loginServiceDocument) throws LoginException{

		LoginRequestType request = loginServiceDocument.getLoginService().getLoginRequest();
		LoginServiceType loginService=loginServiceDocument.addNewLoginService();
		LoginServiceDocument response=LoginServiceDocument.Factory.newInstance();
		LoginResponseType responseType=LoginResponseType.Factory.newInstance();
		
		if(request.getUserName()==null||request.getUserName().equals("")||request.getPassword()==null ||request.getPassword().equals("")|| request.getDomain()==null||request.getDomain().equals("")){
			logger.error("LoginService: UserName ,Password or Domain is  Missing");
			throw new LoginException(getErrorResponse(loginService, response, responseType,RMSApplicationErrorCodes.LOGIN_MALFORMED_INPUT_ERROR, "UserName ,Password or Domain is  Missing"));
		}
		ctxt = LoginContextFactory.getInstance().getContext(request.getDomain());
		if(ctxt==null){
			ctxt=LoginContextFactory.getInstance().getContextWithStartName(request.getDomain());
			if(ctxt==null){
				logger.error("LoginService: "+request.getDomain()+" Domain is not configured in RMS");
				throw new LoginException(getErrorResponse(loginService, response, responseType,RMSApplicationErrorCodes.LOGIN_DOMAIN_NOT_CONFIGURED_ERROR, "Domain is not configured in RMS"));
			}
		}
		try{
			boolean res = ctxt.authenticate(request.getUserName(), request.getPassword());
			logger.info("Authentication result for user " + request.getUserName()+ " : "+res);
			if(res){
				rmsUserPrincipal= ctxt.getUserPrincipal();
			}
			if(rmsUserPrincipal!=null){
				responseType.setUId(rmsUserPrincipal.getUid());
				responseType.setLoginResult(true);
				responseType.setPrincipalName(rmsUserPrincipal.getPrincipalName());
			}
			loginService.setLoginResponse(responseType);
			response.setLoginService(loginService);
		}catch(RMSLoginException e){
			logger.error("LoginService: Error occured during Authentication :"+e.getMessage());
			throw new LoginException(getErrorResponse(loginService, response, responseType,RMSApplicationErrorCodes.LOGIN_AUTHENTICATION_ERROR, e.getMessage()));
		} catch (Exception e) {
			logger.error("LoginService: Error occured during Authentication :"+e.getMessage());
			throw new LoginException(getErrorResponse(loginService, response, responseType,RMSApplicationErrorCodes.LOGIN_GENERAL_ERROR, e.getMessage()));
		}
		return response;
	}
	
	private LoginServiceDocument getErrorResponse(LoginServiceType loginService,
			LoginServiceDocument response, LoginResponseType responseType, int errCode, String errMsg) {
		Error errorResponse = responseType.addNewError();
		errorResponse.setErrorCode(errCode);
		errorResponse.setErrorMessage(errMsg);
		responseType.setError(errorResponse);
		responseType.setLoginResult(false);
		loginService.setLoginResponse(responseType);
		response.setLoginService(loginService);                            					
		return response;
	}
}
