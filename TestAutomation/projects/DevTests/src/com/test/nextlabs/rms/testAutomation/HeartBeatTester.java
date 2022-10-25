package com.test.nextlabs.rms.testAutomation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlException;

import noNamespace.HeartBeatRequestDocument;
import noNamespace.HeartBeatResponseDocument;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.POLICYSET.POLICY;
import noNamespace.POLICYBUNDLETYPE.POLICYBUNDLE.USERGROUPMAP.USERMAP;

public class HeartBeatTester implements TestInterface {

	private TestResult testResult = TestResult.NOT_EXECUTED;
	private String report = "";

	private long agentId;
	private long[] policyIds;
	private long[] userIds;

	public HeartBeatTester(long agentId) {
		this.agentId = agentId;
	}

	public int sendHeartBeatRequest(String inputFile) {
		System.out.println("Sending log request for file "+ inputFile);
		int errorCode = -1;
		String inputFilePath = IntegrationTester.inputDir+File.separator+TestingConfigManager.HEARTBEAT_FOLDER_NAME+File.separator+inputFile;
		try {
			byte[] encoded = convertToValidInput(inputFilePath);
			Map<String,String> headerMap = new HashMap<>();
			headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME,TestingConfigManager.TRUSTED_CERTIFICATE_VALUE );
			String responseString = HttpRequestUtil.sendPostRequest(encoded,"HeartBeat",headerMap);
			HeartBeatResponseDocument response = HeartBeatResponseDocument.Factory.parse(responseString);
			errorCode = response.getHeartBeatResponse().getFault().getErrorCode();
			if (errorCode == 0) {
				System.out.println("HeartBeat was successful");
				POLICY[] policyArray = response.getHeartBeatResponse().getAgentUpdates().getPolicyDeploymentBundle().getPOLICYBUNDLE().getPOLICYSET().getPOLICYArray();
				policyIds = new long[policyArray.length];
				for(int i = 0 ; i<policyArray.length; i++){
					policyIds[i] = policyArray[i].getId();
				}
				USERMAP[] usergroupmap = response.getHeartBeatResponse().getAgentUpdates().getPolicyDeploymentBundle().getPOLICYBUNDLE().getUSERGROUPMAP().getUSERMAPArray();
				userIds = new long[usergroupmap.length];
				for(int i = 0; i<usergroupmap.length;i++){
					userIds[i] = Long.parseLong(usergroupmap[i].getContext());
				}
				testResult = TestResult.PASS;
                report = "HeartBeat test was successful";
			}
			else {
				testResult = TestResult.FAIL;
				report = "HeartBeatTest was unsuccessful, error code: " + errorCode;
			}
		} catch (Exception e) {
			report = "HeartBeat failed with an exception \n"+e.getMessage();
			e.printStackTrace();
			return errorCode;
		}
		return errorCode;
	}

	private byte[] convertToValidInput(String inputFile) throws XmlException {
		HeartBeatRequestDocument request = null;
		try {
			request = HeartBeatRequestDocument.Factory.parse(new File(inputFile));
			request.getHeartBeatRequest().setAgentId(agentId);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return request.toString().getBytes(Charset.forName("UTF-8"));
	}

	public long getAgentId() {
		return agentId;
	}

	public void setAgentId(long agentId) {
		this.agentId = agentId;
	}

	public long[] getPolicyIds() {
		return policyIds;
	}

	public void setPolicyIds(long[] policyIds) {
		this.policyIds = policyIds;
	}

	public long[] getUserIds() {
		return userIds;
	}

	public void setUserIds(long[] userIds) {
		this.userIds = userIds;
	}
	
	@Override
	public TestResult getResult() {
		return testResult;
	}

	@Override
	public String getReport() {
		return report;
	}

	@Override
	public void setResult(TestResult result) {
		this.testResult = result;
	}

	@Override
	public void setReport(String report) {
		this.report = report;
	}
}
