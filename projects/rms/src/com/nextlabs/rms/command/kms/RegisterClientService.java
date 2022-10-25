package com.nextlabs.rms.command.kms;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;

import com.nextlabs.kms.RegisterClientRequest;
import com.nextlabs.kms.RegisterClientResponse;
import com.nextlabs.nxl.sharedutil.EncryptionUtil;
import com.nextlabs.rms.command.kms.KMSCommManager.KMSWebSvcUrl;
import com.nextlabs.rms.services.manager.ssl.SSLSocketFactoryGenerator;
import com.nextlabs.rms.sharedutil.KMSWebSvcResponseHandler;
import com.nextlabs.rms.sharedutil.OperationResult;
import com.nextlabs.rms.sharedutil.RestletUtil;
import com.nextlabs.rms.util.StringUtils;

public class RegisterClientService{
	private static Logger logger = Logger.getLogger(RegisterClientService.class);
	
	public static void registerClient() throws Exception {
		
		RegisterClientRequest tenantRequest = new RegisterClientRequest();
		String localCert = SSLSocketFactoryGenerator.getKMSKeyStoreUnsecureCertificate();
		Map<String,String> headerMap = new HashMap<>();
		headerMap.put(KMSWebSvcUrl.WEBSVC_KMS_CERTIFICATE_NAME, localCert);
		
		OperationResult errorResult = new OperationResult();
		Representation representation = new JaxbRepresentation<RegisterClientRequest>(MediaType.APPLICATION_XML, tenantRequest);
		RegisterClientResponse clientResponse = RestletUtil.sendRequest(KMSWebSvcUrl.REGISTER_CLIENT.getServiceUrl(), Method.POST, representation, headerMap, 
																											new KMSWebSvcResponseHandler<>(RegisterClientResponse.class, errorResult));
		if(clientResponse == null){
			throw new Exception("Could not retrieve certificate response from KMS.");
		}
		
		if(errorResult.getMessage() == null) {
			String base64Cert = clientResponse.getCertificate();
			String certStorePass = clientResponse.getCertStorePass();
			if(!StringUtils.hasText(base64Cert) || !StringUtils.hasText(certStorePass)) {
				throw new Exception("Could not retrieve certificate response from KMS.");
			}
			
			KeyStore ks = KeyStore.getInstance("JKS");
			String decryptedPass = new EncryptionUtil().decrypt(certStorePass);
			char [] password = decryptedPass.toCharArray();
			ks.load(null, password);
			
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			InputStream in = new ByteArrayInputStream(Base64.decodeBase64(base64Cert.getBytes()));
			X509Certificate cert = (X509Certificate)certFactory.generateCertificate(in);
			ks.setCertificateEntry(SSLSocketFactoryGenerator.KMS_KEYSTORE_SECURE_ALIAS, cert);
			in.close();
			
			String certFile = SSLSocketFactoryGenerator.CERT_PATH + SSLSocketFactoryGenerator.KMS_KEYSTORE_FILE_SECURE;
			FileOutputStream fos = new FileOutputStream(certFile);
			ks.store(fos, password);
			fos.close();
			
			FileUtils.writeStringToFile(new File(SSLSocketFactoryGenerator.CERT_PATH, SSLSocketFactoryGenerator.KMS_KEYSTORE_DAT_SECURE), certStorePass);
			
			logger.info("Registered RMS client in KMS.");
		}
	}
}
