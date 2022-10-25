package com.nextlabs.rms.command.kms;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;

import com.google.gson.JsonObject;
import com.nextlabs.kms.GetKeyRingWithKeysRequest;
import com.nextlabs.kms.GetKeyRingWithKeysResponse;
import com.nextlabs.kms.GetTenantDetailRequest;
import com.nextlabs.kms.GetTenantDetailResponse;
import com.nextlabs.kms.types.Attribute;
import com.nextlabs.kms.types.KeyDTO;
import com.nextlabs.kms.types.KeyRingWithKeysDTO;
import com.nextlabs.nxl.sharedutil.EncryptionUtil;
import com.nextlabs.rms.auth.RMSUserPrincipal;
import com.nextlabs.rms.command.AbstractCommand;
import com.nextlabs.rms.command.kms.KMSCommManager.KMSWebSvcUrl;
import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.filters.AuthFilter;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.sharedutil.KMSWebSvcResponseHandler;
import com.nextlabs.rms.sharedutil.OperationResult;
import com.nextlabs.rms.sharedutil.RestletUtil;

public class ExportKeyRingCommand extends AbstractCommand {
	private static Logger logger = Logger.getLogger(ExportKeyRingCommand.class);

	private static final String KEYRING_NAME_KEY = "Xname";
	@Override
	public void doAction(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		RMSUserPrincipal user = (RMSUserPrincipal)request.getSession().getAttribute(AuthFilter.LOGGEDIN_USER);
		String keyStorePassword = getKeyStorePassword(user.getTenantId());
		if(keyStorePassword == null) {
			OperationResult errorResult = new OperationResult();
			errorResult.setResult(false);
			errorResult.setMessage("Failed to retrieve key store password.");
			JsonUtil.writeJsonToResponse(errorResult, response);
			return;
		}
		String keyRingName = request.getParameter("keyring_name");
		GetKeyRingWithKeysRequest keyRingRequest = new GetKeyRingWithKeysRequest();
		
		keyRingRequest.setTenantId(user.getTenantId());
		keyRingRequest.setVersion(1);
		keyRingRequest.setName(keyRingName);
		
		Map<String,String> headerMap = KMSCommManager.constructHeader();
		OperationResult errorResult = new OperationResult();
		Representation representation = new JaxbRepresentation<GetKeyRingWithKeysRequest>(MediaType.APPLICATION_XML, keyRingRequest);
		GetKeyRingWithKeysResponse keyRingResponse = RestletUtil.sendRequest(KMSWebSvcUrl.GET_KEYRING_WITH_KEYS.getServiceUrl(), Method.POST, representation, headerMap, 
																											new KMSWebSvcResponseHandler<>(GetKeyRingWithKeysResponse.class, errorResult));		
		if(errorResult.getMessage() != null) {
			JsonUtil.writeJsonToResponse(errorResult, response); 
		}
		else {
			KeyRingWithKeysDTO keyRingWithKeys = keyRingResponse.getKeyRingWithKeys();
			List<KeyDTO> keys = keyRingWithKeys.getKeys();
			
			KeyStore ks = KeyStore.getInstance("JCEKS");
			ks.load(null, keyStorePassword.toCharArray());
			
			PasswordProtection password = new PasswordProtection(keyStorePassword.toCharArray()); 
			for(KeyDTO key: keys){
				String alias = key.getKeyId().getId();
				SecretKey secKey = new SecretKeySpec(key.getKeyValue(), 0, key.getKeyValue().length, key.getKeyAlgorithm()); 
        ks.setEntry(alias, new KeyStore.SecretKeyEntry(secKey), password);
			}
			SecretKey keyRingNameKey =  new SecretKeySpec(keyRingName.getBytes(), 0, keyRingName.getBytes().length, ""); 
			ks.setEntry(KEYRING_NAME_KEY, new KeyStore.SecretKeyEntry(keyRingNameKey), password);
			
			String sessionId = request.getSession().getId();
			String webDir = GlobalConfigManager.getInstance().getWebDir();
			File tempWebDir = new File(webDir, GlobalConfigManager.TEMPDIR_NAME+File.separator+sessionId+File.separator);
			if(!tempWebDir.exists()){
				tempWebDir.mkdirs();
			}
			File keyRingFile = new File(tempWebDir,keyRingName+".keyring");
			String redirectUrl = GlobalConfigManager.RMS_CONTEXT_NAME+ "/" + GlobalConfigManager.TEMPDIR_NAME + "/" + sessionId + "/" +keyRingFile.getName();
			
			ks.store(new FileOutputStream(keyRingFile), keyStorePassword.toCharArray());
			
			JsonObject json = new JsonObject();
			json.addProperty("result", true);
			json.addProperty("filePath", redirectUrl);
			JsonUtil.writeJsonToResponse(json, response);
			return;
		}
	}
	
	private String getKeyStorePassword(String tenantId) throws RMSException{
		GetTenantDetailRequest tenantDetailRequest = new GetTenantDetailRequest();
		tenantDetailRequest.setTenantId(tenantId);
		tenantDetailRequest.setVersion(1);
		
		Map<String,String> headerMap = KMSCommManager.constructHeader();
		OperationResult errorResult = new OperationResult();
		Representation representation = new JaxbRepresentation<GetTenantDetailRequest>(MediaType.APPLICATION_XML, tenantDetailRequest);
		GetTenantDetailResponse tenantDetailResponse = RestletUtil.sendRequest(KMSWebSvcUrl.GET_TENANT_DETAIL.getServiceUrl(), Method.POST, representation, headerMap, 
																											new KMSWebSvcResponseHandler<>(GetTenantDetailResponse.class, errorResult));
		
		if(errorResult.getMessage() != null) {
			logger.error(errorResult.getMessage());
			return null;
		}
		else {
			List<Attribute> attributes = tenantDetailResponse.getAttributes();
			for(Attribute attr: attributes) {
				if(attr.getName().equals("storepass")){
					return new EncryptionUtil().decrypt(attr.getValue());
				}
			}
		}
		return null;
	}
}
