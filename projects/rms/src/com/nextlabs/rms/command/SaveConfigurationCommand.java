package com.nextlabs.rms.command;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.config.ConfigManager;
import com.nextlabs.rms.config.SettingManager;
import com.nextlabs.rms.eval.EvaluationAdapterFactory;
import com.nextlabs.rms.eval.IEvalAdapter;
import com.nextlabs.rms.eval.RMSPermissionMgr;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.nxl.decrypt.CryptManager;
import com.nextlabs.rms.sharedutil.OperationResult;
import com.nextlabs.rms.util.StringUtils;
import com.nextlabs.rms.util.UserLocationProvider;

public class SaveConfigurationCommand extends AbstractCommand {
	
	
	private static Logger logger = Logger.getLogger(SaveConfigurationCommand.class);
	
	private ConfigManager configManager = null;
	
	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		RMSUserPrincipal user=(RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		boolean isPermitted=RMSPermissionMgr.isActionAllowed(RMSPermissionMgr.ACTION_SAVE_SETTINGS, user);
		if(!isPermitted){
			response.sendError(403, RMSMessageHandler.getClientString("userNotAdmin"));
			JsonUtil.writeJsonToResponse(null, response);
			logger.warn("User (username: " + user.getUserName() + ", tenant: " + user.getTenantId()
					+ ") doesn't have permission to access configuration saving module.");
			return;
		}
		HashMap<String,String> updateSettingMap=new HashMap<String, String>();
		
		configManager = ConfigManager.getInstance(user.getTenantId());
		
		String oldEnableRemotePc = configManager.getStringProperty(ConfigManager.ENABLE_REMOTE_PC);
		String oldKmPCHostName = configManager.getStringProperty(ConfigManager.KM_POLICY_CONTROLLER_HOSTNAME);
		String oldKmRMIPortNum = configManager.getStringProperty(ConfigManager.KM_RMI_PORT_NUMBER);
		String oldKmRMIKeyStoreFile = configManager.getStringProperty(ConfigManager.KM_RMI_KEYSTOREFILE);
		String oldKmRMIKeyStorePass = configManager.getStringProperty(ConfigManager.KM_RMI_KEYSTOREPASSWORD);
		String oldKmRMITrustStoreFile = configManager.getStringProperty(ConfigManager.KM_RMI_TRUSTSTOREFILE);
		String oldKmRMITrustStorePass = configManager.getStringProperty(ConfigManager.KM_RMI_TRUSTSTOREPASSWORD);
		String oldeEvalPCHostName = configManager.getStringProperty(ConfigManager.EVAL_POLICY_CONTROLLER_HOSTNAME);
		String oldEvalRMIPortNum = configManager.getStringProperty(ConfigManager.EVAL_RMI_PORT_NUMBER);
		String oldEnableUserLocation = configManager.getStringProperty(ConfigManager.ENABLE_USER_LOCATION);
		String oldLocationUpdateFreq = configManager.getStringProperty(ConfigManager.LOCATION_UPDATE_FREQUENCY);

		updateSettingMap.put(ConfigManager.KM_POLICY_CONTROLLER_HOSTNAME, request.getParameter("KM_POLICY_CONTROLLER_HOSTNAME"));
		updateSettingMap.put(ConfigManager.KM_RMI_PORT_NUMBER, request.getParameter("KM_RMI_PORT_NUMBER"));
		updateSettingMap.put(ConfigManager.KM_RMI_KEYSTOREFILE, request.getParameter("KM_RMI_KEYSTOREFILE"));
		updateSettingMap.put(ConfigManager.KM_RMI_KEYSTOREPASSWORD, request.getParameter("KM_RMI_KEYSTORE_PASSWORD"));
		updateSettingMap.put(ConfigManager.KM_RMI_TRUSTSTOREFILE, request.getParameter("KM_RMI_TRUSTSTOREFILE"));
		updateSettingMap.put(ConfigManager.KM_RMI_TRUSTSTOREPASSWORD, request.getParameter("KM_RMI_TRUSTSTORE_PASSWORD"));
		updateSettingMap.put(ConfigManager.EVAL_POLICY_CONTROLLER_HOSTNAME, request.getParameter("KM_POLICY_CONTROLLER_HOSTNAME"));
		updateSettingMap.put(ConfigManager.EVAL_RMI_PORT_NUMBER, request.getParameter("EVAL_RMI_PORT_NUMBER"));
		updateSettingMap.put(ConfigManager.SMTP_HOST, request.getParameter("SMTP_HOST"));
		updateSettingMap.put(ConfigManager.SMTP_USER_NAME, request.getParameter("SMTP_USER_NAME"));
		updateSettingMap.put(ConfigManager.SMTP_PASSWORD, request.getParameter("SMTP_PASSWORD"));
		updateSettingMap.put(ConfigManager.SMTP_ENABLE_TTLS, request.getParameter("SMTP_ENABLE_TTLS"));
		updateSettingMap.put(ConfigManager.SMTP_PORT, request.getParameter("SMTP_PORT"));
		updateSettingMap.put(ConfigManager.SMTP_AUTH, request.getParameter("SMTP_AUTH"));
		updateSettingMap.put(ConfigManager.REGN_EMAIL_SUBJECT, request.getParameter("REGN_EMAIL_SUBJECT"));
		updateSettingMap.put(ConfigManager.RMS_ADMIN_EMAILID,request.getParameter("RMS_ADMIN_EMAILID"));
		updateSettingMap.put(ConfigManager.SESSION_TIMEOUT_MINS, request.getParameter("SESSION_TIMEOUT_MINS"));
		updateSettingMap.put(ConfigManager.POLICY_USER_LOCATION_IDENTIFIER, request.getParameter("POLICY_USER_LOCATION_IDENTIFIER"));
		updateSettingMap.put(ConfigManager.LOCATION_UPDATE_FREQUENCY, request.getParameter("LOCATION_UPDATE_FREQUENCY"));
		//updateSettingMap.put(ConfigManager.ENABLE_PERSONAL_REPO, request.getParameter("enable_personal_repo"));	
		updateSettingMap.put(ConfigManager.ALLOW_REGN_REQUEST, request.getParameter("ALLOW_REGN_REQUEST"));	
		updateSettingMap.put(ConfigManager.ENABLE_USER_LOCATION, request.getParameter("ENABLE_USER_LOCATION"));	
		updateSettingMap.put(ConfigManager.ICENET_URL, request.getParameter("ICENET_URL"));
		updateSettingMap.put(ConfigManager.RMC_CURRENT_VERSION, request.getParameter("RMC_CURRENT_VERSION"));
		updateSettingMap.put(ConfigManager.RMC_UPDATE_URL_32BITS, request.getParameter("RMC_UPDATE_URL_32BITS"));
		updateSettingMap.put(ConfigManager.RMC_CHECKSUM_32BITS, request.getParameter("RMC_CHECKSUM_32BITS"));
		updateSettingMap.put(ConfigManager.RMC_UPDATE_URL_64BITS, request.getParameter("RMC_UPDATE_URL_64BITS"));
		updateSettingMap.put(ConfigManager.RMC_CHECKSUM_64BITS, request.getParameter("RMC_CHECKSUM_64BITS"));
		updateSettingMap.put(ConfigManager.ENABLE_REMOTE_PC, request.getParameter("ENABLE_REMOTE_PC"));
		logger.debug("updateSettingMap:"+updateSettingMap);
		try {
			JsonUtil.writeJsonToResponse(SettingManager.saveConfiguration(user.getTenantId(), updateSettingMap), response);
		} catch (Exception e) {
			logger.error("Error occurred when saving configuration: " + e.getMessage(), e);
			OperationResult result = new OperationResult();
			result.setResult(false);
			result.setMessage(RMSMessageHandler.getClientString("error_while_saving_config"));
			JsonUtil.writeJsonToResponse(result, response);
			return;
		}
		configManager.loadConfigFromDB();
		if(isPCChanged(oldKmPCHostName, oldKmRMIPortNum, oldEvalRMIPortNum, oldeEvalPCHostName,
				oldKmRMIKeyStoreFile, oldKmRMIKeyStorePass, oldKmRMITrustStoreFile, oldKmRMITrustStorePass)&&(!isRemotePolicyControllerConfigChanged(oldEnableRemotePc))){
			logger.debug("About to re-initialize Policy Controller info");
			reInitializePC();
		}
				
		if(isUserLocationConfigChanged(oldEnableUserLocation,oldLocationUpdateFreq)){
			if(configManager.getStringProperty(ConfigManager.ENABLE_USER_LOCATION).equalsIgnoreCase("Yes")){
				UserLocationProvider.getInstance().initUserLocationDb();
			}
		}
		
		if(isRemotePolicyControllerConfigChanged(oldEnableRemotePc)){
			logger.debug("About to re-initialize Policy Controller as EnableRemotePC Config changed");
			reInitializePC();
		}
	}

	private void reInitializePC() throws Exception{
		CryptManager.refresh();
		IEvalAdapter adapter = EvaluationAdapterFactory.getInstance().getAdapter(true);		
		adapter.initializeSDK();
	}
	private boolean isRemotePolicyControllerConfigChanged(String oldEnableRemotePc) {
		String enableRemotePC=configManager.getStringProperty(ConfigManager.ENABLE_REMOTE_PC);
		if(!oldEnableRemotePc.equalsIgnoreCase(enableRemotePC))
			return true;
		return false;
	}

	private boolean isUserLocationConfigChanged(String oldenableUserLocation,String oldLocationUpdateFreq) {
		String enableUserLocation = configManager.getStringProperty(ConfigManager.ENABLE_USER_LOCATION);
		String locationUpdateFreq=configManager.getStringProperty(ConfigManager.LOCATION_UPDATE_FREQUENCY);
		if (!oldenableUserLocation.equalsIgnoreCase(enableUserLocation)
				|| !oldLocationUpdateFreq.equalsIgnoreCase(locationUpdateFreq)) {
			return true;
		}
		return false;
	}
	
	private boolean isPCChanged(String oldKmPCHostName, String oldKmRMIPortNum, String oldEvalRMIPortNum, String oldEvalPCHostName, 
			String oldKmRMIKeyStoreFile, String oldKmRMIKeyStorePass, String oldKmRMITrustStoreFile, String oldKmRMITrustStorePass) {
		String kmPCHostName = configManager.getStringProperty(ConfigManager.KM_POLICY_CONTROLLER_HOSTNAME);
		String kmRMIPortNum = configManager.getStringProperty(ConfigManager.KM_RMI_PORT_NUMBER);
		String evalRMIPortNum = configManager.getStringProperty(ConfigManager.EVAL_RMI_PORT_NUMBER);
		String evalPCHostName = configManager.getStringProperty(ConfigManager.EVAL_POLICY_CONTROLLER_HOSTNAME);
		String kmRMIKeyStoreFile = configManager.getStringProperty(ConfigManager.KM_RMI_KEYSTOREFILE);
		String kmRMIKeyStorePass = configManager.getStringProperty(ConfigManager.KM_RMI_KEYSTOREPASSWORD);
		String kmRMITrustStoreFile = configManager.getStringProperty(ConfigManager.KM_RMI_TRUSTSTOREFILE);
		String kmRMITrustStorePass = configManager.getStringProperty(ConfigManager.KM_RMI_TRUSTSTOREPASSWORD);
		if(!oldKmPCHostName.equalsIgnoreCase(kmPCHostName)
				|| !oldKmRMIPortNum.equalsIgnoreCase(kmRMIPortNum)
				|| !oldEvalRMIPortNum.equalsIgnoreCase(evalRMIPortNum)
				|| !oldEvalPCHostName.equalsIgnoreCase(evalPCHostName)
				|| !oldKmRMIKeyStoreFile.equalsIgnoreCase(kmRMIKeyStoreFile)
				|| !StringUtils.equals(oldKmRMIKeyStorePass, kmRMIKeyStorePass)
				|| !oldKmRMITrustStoreFile.equalsIgnoreCase(kmRMITrustStoreFile)
				|| !StringUtils.equals(oldKmRMITrustStorePass, kmRMITrustStorePass)){
			return true;
		}
		return false;
	}
	
}
