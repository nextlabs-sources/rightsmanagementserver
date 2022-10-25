package com.test.nextlabs.kms.testAutomation;

import java.io.File;

public class IntegrationTester implements TestInterface {
	public static String inputDir = TestingConfigManager.getInstance().getDataDir() + File.separator + TestingConfigManager.TEST_AUTOMATION_DATA_FOLDER_NAME;
	
	TestResult result = TestResult.NOT_EXECUTED;
	String report = "";
	
	public void runTests() {
		System.out.println("Running Default Impl Tests ...");
		DefaultImplTester dfTester = new DefaultImplTester();
		dfTester.sendDefaultImplRequests();
		report += dfTester.getReport()+"\n";
		System.out.println("Running Default Impl Tests completed.");
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
