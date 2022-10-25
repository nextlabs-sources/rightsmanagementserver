package com.test.nextlabs.kms.testAutomation;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXB;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.nextlabs.kms.CreateKeyRingResponse;
import com.nextlabs.kms.GenerateKeyResponse;
import com.nextlabs.kms.GetAllKeyRingsWithKeysResponse;
import com.nextlabs.kms.GetKeyResponse;
import com.nextlabs.kms.GetKeyRingNamesResponse;
import com.nextlabs.kms.GetKeyRingResponse;
import com.nextlabs.kms.GetKeyRingWithKeysResponse;
import com.nextlabs.kms.GetLatestKeyResponse;
import com.nextlabs.kms.importer.DefaultKeyImportUtil;
import com.nextlabs.kms.types.KeyDTO;
import com.nextlabs.kms.types.KeyIdDTO;
import com.nextlabs.kms.types.KeyRingDTO;
import com.nextlabs.kms.types.KeyRingNamesDTO;
import com.nextlabs.kms.types.KeyRingWithKeysDTO;
import com.nextlabs.kms.types.KeyRingsWithKeysDTO;

public class DefaultImplTester {
	
	private static String inputFolder = IntegrationTester.inputDir+File.separator+TestingConfigManager.DEFAULT_IMPL_FOLDER_NAME;
	private static final String KEY_RING_NAME = "NL_SHARE_TEST";
	private static final String TENANT_NAME = "KMS_TEST_AUTOMATION_TENANT";
	private static final String IMPORT_KEYSTORE = "NL_SHARE.keyring";
	private static final String IMPORT_KEYSTOREPASS = "123next!";
	private String report = "";
	private static int totTestCases = 0;
	private static int numOfFailedCases = 0;
	private static List<String> failedCases = new ArrayList<String>();
	private static final boolean IGNORE_REGISTER_TENANT_TEST = true;

	public void sendDefaultImplRequests() {
		
		try{			
			sendRegistrationRequests();
			sendKeyRingRequests();
		} catch (Exception e){
			e.printStackTrace();			
			System.out.println("DefaultImplTest failed with an Exception");	
		} finally {
			prepareReport();
		}
	}

	private void sendRegistrationRequests() throws Exception{
		String [] registrationRequests = {
				"registerTenant_newTenant.xml",
				"registerTenant_existingTenant.xml",
				"getTenantDetail_existingTenant.xml",
				"getTenantDetail_non-existingTenant.xml",
			};
		sendServiceRequests(registrationRequests);
	}
	
	private void sendKeyRingRequests() throws Exception{
		sendGetKeyRingNamesRequest();	//no KeyRings
		sendCreateNewKeyRingRequest();
		sendServiceRequests(new String []{"createKeyRing_existingKeyRing.xml",
																			"createKeyRing_non-existingTenant.xml",
																			"disableKeyRing_non-existingKeyRing.xml",
																			"disableKeyRing_existingKeyRing.xml",
																			"createKeyRing_disabledKeyRing.xml",
																			"getKeyRing_disabledKeyRing.xml",
																			"enableKeyRing_existingKeyRing.xml",
																			"getKeyRingWithKeys_non-existingKeyRing.xml",
																			"getLatestKey_emptyKeyRing.xml",
																			"generateKey_unsupportedAlgorithm.xml",
																			"generateKey_unsupportedKeyLength.xml"
																			});
		
		sendGetKeyRingWithKeys();	// empty KeyRing
		KeyIdDTO keyIdDTO = sendGenerateKeyRequest();
		KeyDTO key1 = sendGetLatestKeyRequest();
		KeyDTO key2 = sendGetKeyRequest(keyIdDTO);
		compareKeyResponses(key1, key2);
		sendGetAllKeyRingsWithKeysRequest();
		sendServiceRequests(new String []{"disableKeyRing_existingKeyRing.xml",
																			"deleteKeyRing_disabledKeyRing.xml",
																			"deleteKeyRing_non-existingKeyRing.xml",
																			"getKeyRing_deletedKeyRing.xml"
																			});
		sendImportKeysRequest();
		sendServiceRequests(new String []{"deleteKeyRing_importedKeyRing.xml"});
	}
	
	private void sendGetKeyRingNamesRequest() throws Exception{
		String inputFileName = "getKeyRingNames_noKeyRing.xml";
		String serviceName = FilenameUtils.getBaseName(inputFileName);
		serviceName = serviceName.substring(0, serviceName.indexOf('_'));
		byte[] encoded = Files.readAllBytes(Paths.get(inputFolder+File.separator+inputFileName));
		Map<String,String> headerMap = new HashMap<>();
		headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME,TestingConfigManager.TRUSTED_CERTIFICATE_VALUE);
		totTestCases++;
		String responseString = HttpRequestUtil.sendPostRequest(encoded, serviceName, headerMap);
		GetKeyRingNamesResponse response = JAXB.unmarshal(new StringReader(responseString), GetKeyRingNamesResponse.class);
		KeyRingNamesDTO krNamesDto = response.getKeyRingNames();
		if(krNamesDto != null && krNamesDto.getName().isEmpty()){
			System.out.println(FilenameUtils.removeExtension(inputFileName)+ ": Success");
			return;
		}
		System.out.println(FilenameUtils.removeExtension(inputFileName)+ ": Failed");
		numOfFailedCases++;
		failedCases.add(FilenameUtils.removeExtension(inputFileName));
	}
	
	private void sendCreateNewKeyRingRequest() throws Exception{
		String inputFileName = "createKeyRing_newKeyRing.xml";
		String serviceName = FilenameUtils.getBaseName(inputFileName);
		serviceName = serviceName.substring(0, serviceName.indexOf('_'));
		byte[] encoded = Files.readAllBytes(Paths.get(inputFolder+File.separator+inputFileName));
		Map<String,String> headerMap = new HashMap<>();
		headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME,TestingConfigManager.TRUSTED_CERTIFICATE_VALUE);
		totTestCases++;
		String responseString = HttpRequestUtil.sendPostRequest(encoded, serviceName, headerMap);
		CreateKeyRingResponse response = JAXB.unmarshal(new StringReader(responseString), CreateKeyRingResponse.class);
		KeyRingDTO krDto = response.getKeyRing();
		if(krDto != null) {
			String name = krDto.getName();
			long cDate = krDto.getCreatedDate();
			long mDate = krDto.getLastModifiedDate();
			if (name!=null && cDate!=0L && mDate!=0L && name.equals(KEY_RING_NAME)){
				System.out.println(inputFileName+ ": Success");
				return;
			}
		}
		System.out.println(inputFileName+ ": Failed");
		numOfFailedCases++;
		failedCases.add(inputFileName);
	}
	
	private void sendGetKeyRingWithKeys() throws Exception{
		String inputFileName = "getKeyRingWithKeys_emptyKeyRing.xml";
		String serviceName = FilenameUtils.getBaseName(inputFileName);
		serviceName = serviceName.substring(0, inputFileName.indexOf('_'));;
		byte[] encoded = Files.readAllBytes(Paths.get(inputFolder+File.separator+inputFileName));
		Map<String,String> headerMap = new HashMap<>();
		headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME,TestingConfigManager.TRUSTED_CERTIFICATE_VALUE);
		totTestCases++;
		String responseString = HttpRequestUtil.sendPostRequest(encoded, serviceName, headerMap);
		GetKeyRingWithKeysResponse response = JAXB.unmarshal(new StringReader(responseString), GetKeyRingWithKeysResponse.class);
		KeyRingWithKeysDTO keyDTO = response.getKeyRingWithKeys();
		if(keyDTO != null && keyDTO.getKeys().isEmpty()){
			System.out.println(FilenameUtils.removeExtension(inputFileName)+ ": Success");
			return;
		}
		System.out.println(FilenameUtils.removeExtension(inputFileName)+ ": Failed");
		numOfFailedCases++;
		failedCases.add(FilenameUtils.removeExtension(inputFileName));
	}
	
	private KeyIdDTO sendGenerateKeyRequest() throws Exception{
		String inputFileName = "generateKey.xml";
		String serviceName = FilenameUtils.getBaseName(inputFileName);
		byte[] encoded = Files.readAllBytes(Paths.get(inputFolder+File.separator+inputFileName));
		Map<String,String> headerMap = new HashMap<>();
		headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME,TestingConfigManager.TRUSTED_CERTIFICATE_VALUE);
		totTestCases++;
		String responseString = HttpRequestUtil.sendPostRequest(encoded, serviceName, headerMap);
		GenerateKeyResponse response = JAXB.unmarshal(new StringReader(responseString), GenerateKeyResponse.class);
		KeyIdDTO keyIdDTO = response.getKeyId();
		if(keyIdDTO != null && keyIdDTO.getHash()!= null && keyIdDTO.getTimestamp()!=0L){
			System.out.println(FilenameUtils.removeExtension(inputFileName)+ ": Success");
			return keyIdDTO;
		}
		System.out.println(FilenameUtils.removeExtension(inputFileName)+ ": Failed");
		numOfFailedCases++;
		failedCases.add(FilenameUtils.removeExtension(inputFileName));
		return null;
	}
	
	private KeyDTO sendGetKeyRequest(KeyIdDTO keyIdDTO) throws Exception{
		String hashInHex = Hex.encodeHexString(keyIdDTO.getHash());
		long timestamp = keyIdDTO.getTimestamp();
		String inputFileName = "getKey.xml";
		String serviceName = FilenameUtils.getBaseName(inputFileName);
		byte[] encoded = Files.readAllBytes(Paths.get(inputFolder+File.separator+inputFileName));
		String originalXml = new String(encoded, "UTF-8");
		String updatedXml = originalXml.replace("<hash></hash>", "<hash>"+ hashInHex + "</hash>");
		updatedXml = updatedXml.replace("<timestamp>0</timestamp>", "<timestamp>"+ timestamp +"</timestamp>");
		Map<String,String> headerMap = new HashMap<>();
		headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME,TestingConfigManager.TRUSTED_CERTIFICATE_VALUE);
		totTestCases++;
		String responseString = HttpRequestUtil.sendPostRequest(updatedXml.getBytes(StandardCharsets.UTF_8), serviceName, headerMap);
		GetKeyResponse response = JAXB.unmarshal(new StringReader(responseString), GetKeyResponse.class);
		KeyDTO keyDTO = response.getKey();
		if(keyDTO != null){
			System.out.println(FilenameUtils.removeExtension(inputFileName)+ ": Success");
			return keyDTO;
		}
		System.out.println(FilenameUtils.removeExtension(inputFileName)+ ": Failed");
		numOfFailedCases++;
		failedCases.add(FilenameUtils.removeExtension(inputFileName));
		return null;
	}
	
	private KeyDTO sendGetLatestKeyRequest() throws Exception{
		String inputFileName = "getLatestKey.xml";
		String serviceName = FilenameUtils.getBaseName(inputFileName);
		byte[] encoded = Files.readAllBytes(Paths.get(inputFolder+File.separator+inputFileName));
		Map<String,String> headerMap = new HashMap<>();
		headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME,TestingConfigManager.TRUSTED_CERTIFICATE_VALUE);
		totTestCases++;
		String responseString = HttpRequestUtil.sendPostRequest(encoded, serviceName, headerMap);
		GetLatestKeyResponse response = JAXB.unmarshal(new StringReader(responseString), GetLatestKeyResponse.class);
		KeyDTO keyDTO = response.getKey();
		if(keyDTO != null){
			System.out.println(FilenameUtils.removeExtension(inputFileName)+ ": Success");
			return keyDTO;
		}
		System.out.println(FilenameUtils.removeExtension(inputFileName)+ ": Failed");
		numOfFailedCases++;
		failedCases.add(FilenameUtils.removeExtension(inputFileName));
		return null;
	}
	
	private void compareKeyResponses(KeyDTO key1, KeyDTO key2){
		String testName = "compare_GetLatestKey_And_GetKey";
		long k1TimeStamp = key1.getKeyId().getTimestamp();
		long k2TimeStamp = key2.getKeyId().getTimestamp();
		byte[] k1hash = key1.getKeyValue();
		byte[] k2hash = key2.getKeyValue();
		
		if(k1hash != null && k2hash != null && k1TimeStamp != 0L && k2TimeStamp != 0L && Arrays.equals(k1hash,k2hash) && k1TimeStamp == k2TimeStamp) {
			System.out.println(testName + ": Success");
		} else{
			System.out.println(testName + ": Failed");
			numOfFailedCases++;
			failedCases.add(testName);
		}
	}
	
	/**
	 * Test fails if there is no keyRing or keyRing is empty
	 * @throws Exception
	 */
	private void sendGetAllKeyRingsWithKeysRequest() throws Exception{
		String inputFileName = "getAllKeyRingsWithKeys.xml";
		String serviceName = FilenameUtils.getBaseName(inputFileName);
		byte[] encoded = Files.readAllBytes(Paths.get(inputFolder+File.separator+inputFileName));
		Map<String,String> headerMap = new HashMap<>();
		headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME,TestingConfigManager.TRUSTED_CERTIFICATE_VALUE);
		totTestCases++;
		String responseString = HttpRequestUtil.sendPostRequest(encoded, serviceName, headerMap);
		GetAllKeyRingsWithKeysResponse response = JAXB.unmarshal(new StringReader(responseString), GetAllKeyRingsWithKeysResponse.class);
		KeyRingsWithKeysDTO keyRingsWithKeysDTO = response.getKeyRingWithKeys();
		if(keyRingsWithKeysDTO != null){
			List<KeyRingWithKeysDTO> keyRingsDTO = keyRingsWithKeysDTO.getKeyRings();
			if(keyRingsDTO == null || keyRingsDTO.isEmpty()) {
				System.out.println(FilenameUtils.removeExtension(inputFileName)+ ": Failed");
				numOfFailedCases++;
				failedCases.add(FilenameUtils.removeExtension(inputFileName));
				return;
			}
			else {
				for(KeyRingWithKeysDTO keyRingDTO: keyRingsDTO){
					if(keyRingDTO.getName().equals(KEY_RING_NAME)){
						List<KeyDTO> keysDTO = keyRingDTO.getKeys();
						if(keysDTO == null || keysDTO.isEmpty()) {
							System.out.println(FilenameUtils.removeExtension(inputFileName)+ ": Failed");
							numOfFailedCases++;
							failedCases.add(FilenameUtils.removeExtension(inputFileName));
							return;
						}
					}
				}
			}
		}
		System.out.println(FilenameUtils.removeExtension(inputFileName)+ ": Success");
	}
	
	private void sendImportKeysRequest() throws Exception{
		totTestCases++;
		String hostUrl = TestingConfigManager.getInstance().getStringProperty(TestingConfigManager.KMS_URL);
		if(hostUrl == null || hostUrl.length()==0){
			hostUrl = TestingConfigManager.LOCALHOST_URL;
		}

		if (DefaultKeyImportUtil.importKeys(TENANT_NAME, inputFolder+File.separator+IMPORT_KEYSTORE, IMPORT_KEYSTOREPASS, null, hostUrl, null)) {
			System.out.println("import_Keyring: Success");
		} else {
			System.out.println("import_Keyring: Failed");
			numOfFailedCases++;
			failedCases.add("import_Keyring");
		}
		
		String inputFileName = "getKeyRing_importedKeyRing.xml";
		String serviceName = FilenameUtils.getBaseName(inputFileName);
		serviceName = serviceName.substring(0, inputFileName.indexOf('_'));;
		byte[] encoded = Files.readAllBytes(Paths.get(inputFolder+File.separator+inputFileName));
		Map<String,String> headerMap = new HashMap<>();
		headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME,TestingConfigManager.TRUSTED_CERTIFICATE_VALUE);
		totTestCases++;
		String responseString = HttpRequestUtil.sendPostRequest(encoded, serviceName, headerMap);
		GetKeyRingResponse response = JAXB.unmarshal(new StringReader(responseString), GetKeyRingResponse.class);
		KeyRingDTO keyRingDTO = response.getKeyRing();
		if(keyRingDTO != null){
			byte [] sampleKeyHash = keyRingDTO.getKeyIds().get(0).getHash();
			long sampleTimestamp = keyRingDTO.getKeyIds().get(0).getTimestamp();
			if(sampleTimestamp == 1438677684000L && keyRingDTO.getName().equals("NL_SHARE") && Hex.encodeHexString(sampleKeyHash).equalsIgnoreCase("ADC2DB2110032BE2EA1EB57CA3D1D5A6CE882F4B000000000000000000000000")){
				System.out.println("validate_ImportKeyRing: Success");
				return;
			}
		}
		System.out.println("validate_ImportKeyRing: Failed");
		numOfFailedCases++;
		failedCases.add("validate_ImportKeyRing");
		
	}
	
	private void sendServiceRequests(String [] fileNames) throws Exception{
		for (String inputFileName : fileNames){
			String serviceName = inputFileName.substring(0, inputFileName.indexOf('_'));
			byte[] encoded = Files.readAllBytes(Paths.get(inputFolder+File.separator+inputFileName));
			Map<String,String> headerMap = new HashMap<>();
			headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME,TestingConfigManager.TRUSTED_CERTIFICATE_VALUE);
			totTestCases++;
			String responseString = HttpRequestUtil.sendPostRequest(encoded,serviceName,headerMap);
			responseString = responseString.replaceAll("\\s+", "");
		 
			File outputFile = new File(inputFolder+File.separator+FilenameUtils.removeExtension(inputFileName)+"_response.xml");
			if(IGNORE_REGISTER_TENANT_TEST && inputFileName.equals("registerTenant_newTenant.xml")) {
				totTestCases--;
			} else {
				if(!isResponseEqual(responseString, outputFile)){
					numOfFailedCases++;					   
					failedCases.add(inputFileName);
				}
			}
		}
	}
	
	private boolean isResponseEqual(String responseString, File file) throws IOException {		
		String serviceName = file.getName().substring(0,file.getName().indexOf("_response.xml"));
		String expectedResponse = FileUtils.readFileToString(file, "UTF-8");
		expectedResponse = expectedResponse.replaceAll("\\s+", "");
		if(responseString.equals(expectedResponse)){
			System.out.println(serviceName+ ": Success");
			return true;
		}else{
			System.out.println(serviceName+ ": Failed");
			return false;
		}
	}

	private void prepareReport(){
		System.out.println("\nTest cases executed: "+ totTestCases);
		System.out.println("Passed: " + (totTestCases-numOfFailedCases));
    System.out.println("Failed: " + numOfFailedCases);
    
    setReport("DefaultImplTest results:" + "\n");
    setReport(getReport() +"    Test cases executed: "+ totTestCases +"\n");
    setReport(getReport() + "    Passed: " + (totTestCases-numOfFailedCases)+"\n");
    setReport(getReport() + "    Failed: " + numOfFailedCases +"\n");
    if(failedCases.size() > 0){
    	int i = 1;
    	for(String eachCase: failedCases){
    		setReport(getReport() + "        " + i + ". " + eachCase+"\n");
    		i++;
    	}		    	
    }		    
	}
		
	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public static void main(String[] args) throws IOException {
		DefaultImplTester tester = new DefaultImplTester();
		tester.sendDefaultImplRequests();
	}
}
