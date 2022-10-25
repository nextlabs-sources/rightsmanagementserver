package com.test.nextlabs.rms.testAutomation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlException;

import noNamespace.LogServiceDocument;
import noNamespace.LogType;
import noNamespace.PolicyType;
import noNamespace.ResponseEnum;

public class LoggingTester implements TestInterface{

	private long[] policyIds;
	private long[] userIds;
	
	private TestResult testResult = TestResult.NOT_EXECUTED;
	private String report = "";

	public LoggingTester(long[] policyIds, long[] userIds) {
		this.policyIds = policyIds;
		this.userIds = userIds;
	}
	public int sendLoggingRequest(String inputFile)  {
		System.out.println("Sending log request for file "+ inputFile);
		int errorCode = -1;
		try {
			byte[] encoded = convertToValidInput(inputFile);
			Map<String,String> headerMap = new HashMap<>();
			headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME,TestingConfigManager.TRUSTED_CERTIFICATE_VALUE );
			String responseString = HttpRequestUtil.sendPostRequest(encoded, "SendLog",headerMap);
			LogServiceDocument response = LogServiceDocument.Factory.parse(responseString);
			errorCode = response.getLogService().getLogResponse().getFault().getErrorCode();
			ResponseEnum.Enum responseEnum = response.getLogService().getLogResponse().getResponse();
			if (errorCode == 0 && responseEnum.equals(ResponseEnum.SUCCESS)) {
				report = "Logging test was successful";
				testResult = TestResult.PASS;
			}
			else {
				report = "Logging test was unsuccessful, error code: " + errorCode;
				testResult = TestResult.FAIL;
			}
		} catch (Exception e) {
			e.printStackTrace();
			report = "LogSuccessTest failed with an exception \n"+e.getMessage();
			testResult = TestResult.FAIL;
			return errorCode;
		}
		return errorCode;
	}

	private byte[] convertToValidInput(String inputFile) throws XmlException {
		LogServiceDocument doc = null;
		try {
			doc = LogServiceDocument.Factory.parse(new File(IntegrationTester.inputDir + File.separator+TestingConfigManager.LOGGING_FOLDER_NAME+File.separator+inputFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		LogType[] logArray = doc.getLogService().getLogRequest().getLogs().getLogArray();
		for(int i = 0; i<logArray.length; i++){
			PolicyType[] policyArray = logArray[i].getHitPolicies().getPolicyArray();
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(Calendar.getInstance().getTime());
			long timestamp = gc.getTimeInMillis();

			logArray[i].setTimestamp(gc);
			logArray[i].setUid(Long.parseLong(timestamp + ""+ (int)(1000*Math.random())));
			System.out.println("UId for the log record is "+logArray[i].getUid());
			logArray[i].getUser().setContext(getValidUserId(i));
			for(int j = 0; j < policyArray.length;j++){
				policyArray[j].setId((int) getValidPolicyId(j));
			}
		}
		return doc.xmlText().getBytes(Charset.forName("UTF-8"));
	}

	private String getValidUserId(int i) {
		long[] validUserIds = userIds;
		if(i >= validUserIds.length){
			return validUserIds[validUserIds.length-1]+"";
		}
		return validUserIds[i]+"";
	}
	private long getValidPolicyId(int j) {
		long[] validPolicyIds = policyIds;
		if(j >= validPolicyIds.length){
			return validPolicyIds[validPolicyIds.length-1];
		}
		return validPolicyIds[j];
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
