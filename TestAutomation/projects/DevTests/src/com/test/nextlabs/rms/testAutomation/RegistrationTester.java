package com.test.nextlabs.rms.testAutomation;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import noNamespace.RegisterAgentResponseDocument;

public class RegistrationTester implements TestInterface{    
	private long agentId;
	private TestResult testResult = TestResult.NOT_EXECUTED;
	private String report;
	
	public int sendRegisterRequest(String inputFile)  {
        System.out.println("Sending log request for file "+ inputFile);
        int errorCode = -1;
    	try {
			byte[] encoded = Files.readAllBytes(Paths.get(IntegrationTester.inputDir+File.separator+TestingConfigManager.REGISTER_AGENT_FOLDER_NAME+File.separator+inputFile));
            Map<String,String> headerMap = new HashMap<>();
			headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME, TestingConfigManager.UNTRUSTED_CERTIFICATE_VALUE);
            String responseString = HttpRequestUtil.sendPostRequest(encoded,"RegisterAgent",headerMap);
            RegisterAgentResponseDocument response = RegisterAgentResponseDocument.Factory.parse(responseString);
            errorCode = response.getRegisterAgentResponse().getFault().getErrorCode();
            if (errorCode == 0) {
                agentId = response.getRegisterAgentResponse().getStartupConfiguration().getId();
                testResult = TestResult.PASS;
                report = "Registration test was successful";
            }
            else {
                report = "Registration test was unsuccessful, error code: " + errorCode;
                testResult = TestResult.FAIL;
            }
        } catch (Exception e) {
            System.out.println("RegisterAgent failed.");
            e.printStackTrace();
            report = "Registration test was unsuccessful with an exception \n"+e.getMessage();
            testResult = TestResult.FAIL;
            return errorCode;
        }
        return errorCode;
    }

	public long getAgentId() {
		return agentId;
	}

	public void setAgentId(long agentId) {
		this.agentId = agentId;
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
