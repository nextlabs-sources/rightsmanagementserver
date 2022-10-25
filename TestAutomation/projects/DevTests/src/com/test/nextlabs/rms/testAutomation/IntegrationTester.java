package com.test.nextlabs.rms.testAutomation;

import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.IOException;

public class IntegrationTester implements TestInterface {
	public static String inputDir = TestingConfigManager.getInstance().getDataDir() + File.separator + TestingConfigManager.TEST_AUTOMATION_DATA_FOLDER_NAME;
    public static final int numReposToAdd = 2;
    public static final String tempPath = "C:\\ProgramData\\nextlabs\\RMS\\datafiles\\TestData";
	
	TestResult result = TestResult.NOT_EXECUTED;
	String report = "";

	public void runTests() throws XmlException, IOException, InterruptedException {
		System.out.println("Registering agent");
		RegistrationTester regTester = new RegistrationTester();
		int registerResult = regTester.sendRegisterRequest("RegisterAgent.xml");
		report += regTester.getReport() + "\n";
		if(registerResult != 0){
			return;
		}	
		System.out.println("Register agent completed");

		System.out.println("Sending heartbeat");
		HeartBeatTester hbTester = new HeartBeatTester(regTester.getAgentId());
		int heartBeatResult = hbTester.sendHeartBeatRequest("HeartBeat.xml");
		report += hbTester.getReport() + "\n";
		if(heartBeatResult != 0){
			return;
		}
		System.out.println("Heartbeat completed");

		LoggingTester logTester = new LoggingTester(hbTester.getPolicyIds(),hbTester.getUserIds());
		logTester.sendLoggingRequest("logging.xml");
		report += logTester.getReport() + "\n";
		System.out.println("Logging completed");

		System.out.println("Running ConvertFile Service Tests");
		ConvertFileTester ConvFileTester = new ConvertFileTester();		
		ConvFileTester.sendFileConvertRequest();
		report += ConvFileTester.getReport() + "\n";
		System.out.println("ConvertFile test completed");
		
		System.out.println("Running PolicyEval Service Tests");
		PolicyEvalTester policyEvalTester = new PolicyEvalTester();
		policyEvalTester.sendPolicyEvalRequest();
		report += policyEvalTester.getReport() + "\n";
		System.out.println("PolicyEval test completed");
				
        /*System.out.println("Running Sync Services Tests");
        SyncTesterHelper syncTesterHelper = new SyncTesterHelper();
        String syncReport = syncTesterHelper.testAllSyncServices();
        report += syncReport + "\n";
        System.out.println("Sync Services tests completed");*/
    }

	@Override
	public TestResult getResult() {
		return result;
	}
	@Override
	public String getReport() {
		return report;
	}
	@Override
	public void setResult(TestResult result) {
		this.result = result;
	}
	@Override
	public void setReport(String report) {

	}
}