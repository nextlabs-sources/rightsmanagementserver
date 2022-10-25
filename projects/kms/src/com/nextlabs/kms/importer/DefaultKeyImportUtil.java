package com.nextlabs.kms.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.representation.Representation;
import com.nextlabs.kms.IKey;
import com.nextlabs.kms.IKeyId;
import com.nextlabs.kms.ImportKeyRingRequest;
import com.nextlabs.kms.ImportKeyRingResponse;
import com.nextlabs.kms.controller.interceptor.SecuredInterceptor;
import com.nextlabs.kms.dao.impl.FileSystemCertificateDAOImpl;
import com.nextlabs.kms.entity.enums.KeyAlgorithm;
import com.nextlabs.kms.impl.DTOConverter;
import com.nextlabs.kms.impl.Key;
import com.nextlabs.kms.impl.KeyRingUtil;
import com.nextlabs.kms.service.secure.CommArtifactsCache;
import com.nextlabs.kms.types.KeyRingWithKeysDTO;
import com.nextlabs.rms.sharedutil.KMSWebSvcResponseHandler;
import com.nextlabs.rms.sharedutil.OperationResult;
import com.nextlabs.rms.sharedutil.RestletUtil;

public class DefaultKeyImportUtil {	
	
	private static final String INTERNAL_PROPERTY_PREFIX = "X";
	private static final String NAME_PROPERTY = INTERNAL_PROPERTY_PREFIX + "name";
	private static final String SERVICE_NAME = "importKeyRing";
	private static final String KEY_KMSDATADIR = "KMSDATADIR";
	private static final String DEFAULT_TENANT_ID = "-1";
	private static final String DEFAULT_KMS_URL = "https://localhost:8443";
	private static final String CERT_FOLDER_NAME = "cert";

	public static void main(String[] args) throws Exception {
		DefaultKeyImportUtil importer = new DefaultKeyImportUtil();
		if (!importer.validate(args)) {
			return;
		}
		
		int i = 0;
		String tenantCode = "";
		String keyStorePath = "";
		String keyStorePass = "";
		String keyRingName = "";
		String kmsUrl = "";
		while(i<args.length){
			switch(args[i]) {
				case "-path":	
					keyStorePath=args[++i];
					break;
				case "-p":
					keyStorePass=args[++i];
					break;
				case "-name":
					keyRingName=args[++i];
					break;
				case "-tid":
					tenantCode=args[++i];
					break;
				case "-kmsUrl":
					kmsUrl=args[++i];
					break;
				default:
					System.out.println("Invalid arguments");
					printUsage();
					System.exit(-1);
			}
			i++;
		}

		if (keyStorePath == null || keyStorePath.length()==0) {
			System.out.println("KeyStore Path cannot be empty.");
			printUsage();
			System.exit(-1);
		}
		if (keyStorePass == null || keyStorePass.length()==0) {
			System.out.println("KeyStore Pass cannot be empty.");
			printUsage();
			System.exit(-1);
		}
		try{
			if (importKeys(tenantCode, keyStorePath, keyStorePass, keyRingName, kmsUrl, null)) {
				System.out.println("KeyRing successfully imported.");
			} else {
				System.out.println("Failed to import keyring.");
			}
		} catch (Exception e){
				System.out.println("Error occured while importing keyring.");
		}
	}

	public static boolean importKeys(String tenantCode, String keyStorePath, String keyStorePass, String keyRingName, String kmsUrl, String certFile)
			throws Exception {
		try {
			KeyStore ks = null;
			File file = new File(keyStorePath);
			if (!file.exists() || !file.isFile()) {
				throw new FileNotFoundException("Invalid path: " + file.getAbsolutePath());
			}

			InputStream readStream = null;
			try {
				readStream = new FileInputStream(keyStorePath);
				ks = KeyStore.getInstance("JCEKS");
				ks.load(readStream, keyStorePass.toCharArray());
			} catch (IOException e) {
				throw e;
			} finally {
				if (readStream != null) {
					try {
						readStream.close();
					} catch (Exception e) {
					}
				}
			}

			Enumeration<String> aliases = ks.aliases();
			List<IKey> keysToImport = new ArrayList<>();

			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();
				java.security.Key jceksKey = ks.getKey(alias, keyStorePass.toCharArray());
				
				if (alias.equalsIgnoreCase(NAME_PROPERTY)) {
					if (keyRingName == null || keyRingName.length()<=0) {
						keyRingName = new String(jceksKey.getEncoded());
					}
				} else {
					IKeyId keyId = KeyRingUtil.convertToKeyId(alias);
					KeyAlgorithm keyAlgorithm = KeyAlgorithm.convertToKeyAlgorithm(jceksKey);
					IKey key = new Key(keyId, jceksKey.getEncoded(), keyAlgorithm);
					keysToImport.add(key);
				}
			}
			if (keyRingName == null || keyRingName.length()<=0) {
				throw new IllegalArgumentException("Key ring name is required");
			}
			
			tenantCode = (tenantCode.length()<=0) ? DEFAULT_TENANT_ID : tenantCode ;
			
			Map<String,String> headerMap = new HashMap<>();
			headerMap.put(SecuredInterceptor.HTTP_CERT_HEADER, getWebSvcCert());
			
			kmsUrl = (kmsUrl.length()<=0) ? DEFAULT_KMS_URL : kmsUrl;
			kmsUrl = kmsUrl.endsWith("/") ? kmsUrl.substring(0, kmsUrl.length()-1) : kmsUrl;
			String serviceUrl = kmsUrl + "/KMS/service/" + SERVICE_NAME;
			
			ImportKeyRingRequest keyRingRequest = new ImportKeyRingRequest();
			keyRingRequest.setTenantId(tenantCode);
			keyRingRequest.setVersion(1);
			KeyRingWithKeysDTO keyRingDTO = new KeyRingWithKeysDTO();
			keyRingDTO.setName(keyRingName);
			keyRingDTO.getKeys().addAll(Arrays.asList(DTOConverter.toKeyDTOs(keysToImport)));
			keyRingRequest.setKeyRingWithKeys(keyRingDTO);
			
			OperationResult errorResult = new OperationResult();
			Representation representation = new JaxbRepresentation<ImportKeyRingRequest>(MediaType.APPLICATION_XML, keyRingRequest);
			
			ImportKeyRingResponse response = RestletUtil.sendRequest(serviceUrl, Method.POST, representation, headerMap, new KMSWebSvcResponseHandler<>(ImportKeyRingResponse.class, errorResult));
			
			if(response == null) {
				System.out.println("Could not connect to Key Management Server.");
				return false;
			}
			if(errorResult.getMessage() != null) {
				System.out.println(errorResult.getMessage());
				return false;
			}
			else {
				System.out.println("Retrieved tenant details.");
				return true;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}	
	}
	
	static {
    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
      new javax.net.ssl.HostnameVerifier(){
      	public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
      		return true;
      	}
      });
    }
	
	private static String getWebSvcCert() {
		String dataDir = System.getenv(KEY_KMSDATADIR);
		if (dataDir == null || dataDir.length()==0) {
			if (isWindows()) {
				dataDir = "C:\\ProgramData\\nextlabs\\RMS\\datafiles\\KMS";
			} else {
				dataDir = "/var/opt/nextlabs/RMS/datafiles/KMS";
			}
		}
		try {
			String certFolder = dataDir + File.separator + CERT_FOLDER_NAME;
			File f = new File(certFolder, FileSystemCertificateDAOImpl.KMS_KEYSTORE_FILE_SECURE);
			if (!f.exists() || !f.isFile()) {
				throw new FileNotFoundException("Unable to find certificate: " + f.getAbsolutePath());
			}
			String encodedCertificate = CommArtifactsCache.getInstance().getEncodedCertificate(f, FileSystemCertificateDAOImpl.KMS_KEYSTORE_SECURE_ALIAS,
					"123next!");
			return encodedCertificate;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private boolean validate(String[] args) {
		if (args.length == 0) {
			printUsage();
			return false;
		}
		return true;
	}
	
	private static boolean isWindows() {
		String os = System.getProperty("os.name", "").toLowerCase();
		return (os.indexOf("win") >= 0);
	}

	private static void printUsage() {
		System.out.println("\nDefault JCEKS Importer");
		System.out.println("\nDescription");
		System.out.println("    This utility imports keys from a JKS/JCEKS keystore to KMS.");
		System.out.println("\nUSAGE");
		if (isWindows()) {
			System.out.println("    keymanagement.bat -path [storePath] -p [storePass] -name [keyRingName] -tid [tenantId] -kmsUrl [kmsUrl]");
		} else {
			System.out.println("    ./keymanagement.sh -path [storePath] -p [storePass] -name [keyRingName] -tid [tenantId] -kmsUrl [kmsUrl]");
		}
		System.out.println("\nOPTIONS");
		System.out.println("    storePath");
		System.out.println("        Absolute Path of the keystore");
		System.out.println("    storePass");
		System.out.println("        Password for keystore");
		System.out.println("    keyRingName");
		System.out.println("        KeyRing Name (Optional)");
		System.out.println("    tenantId");
		System.out.println("        Id of the tenant (Optional)");
		System.out.println("    kmsUrl");
		System.out.println("        URL of Key Management Server (Optional)");
		System.out.println("\nExample");
		if (isWindows()) {
			System.out.println("    keymanagement.bat -path \"absolute\\path\\to\\keystore\" -p \"keystorePassword\" -name \"NL_SHARE\" -tid \"-1\" -kmsUrl https://localhost:8443");
		} else {
			System.out.println("    ./keymanagement.sh -path \"/absolute/path/to/keystore\" -p \"keystorePassword\" -name \"NL_SHARE\" -tid \"-1\" -kmsUrl https://localhost:8443");
		}
	}

}