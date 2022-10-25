package com.test.nextlabs.kms.testAutomation;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

public class TestRunner {
	public static void main(String[] args) {
		IntegrationTester integrationTester = new IntegrationTester();
		integrationTester.runTests();
		System.out.println(integrationTester.getReport());
		File outputFile = new File(TestingConfigManager.getInstance().getDataDir(), TestingConfigManager.TEST_RESULT_FILENAME);
		try {
			FileUtils.writeStringToFile(outputFile, integrationTester.getReport());
		} catch (IOException e) {
			System.out.println("Error occured while generating report to " + outputFile);
			e.printStackTrace();
		}
	}
}
