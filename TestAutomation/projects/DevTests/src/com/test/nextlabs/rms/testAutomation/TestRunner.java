package com.test.nextlabs.rms.testAutomation;

import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class TestRunner {
	public static void main(String[] args) throws XmlException, IOException, InterruptedException {
		//Run logging integration tests
		IntegrationTester integrationTester = new IntegrationTester();
		integrationTester.runTests();
		//Run other tests
		writeResultsToFile(integrationTester.getReport());
		//Email report for Dev Tests
		sendEmail(integrationTester.getReport());
	}

	private static void writeResultsToFile(String report) throws IOException {
		File resultDir = new File(TestingConfigManager.getInstance().getDataDir()+File.separator+"results");
		resultDir.mkdir();
		Path file = Paths.get(resultDir.getAbsolutePath()+File.separator+"devTestResults.txt");
		Files.deleteIfExists(file);
		Files.write(file, report.getBytes());
	}
	
	private static void sendEmail(String report) {
		SMTPClient mailClient = SMTPClient.getInstance();
		String mailSubject=TestingConfigManager.getInstance().getStringProperty(TestingConfigManager.REPORT_EMAIL_SUBJECT);
		String[] toMailArr = TestingConfigManager.getInstance().getStringPropertyArray(TestingConfigManager.RMS_TEST_TO_EMAIL);
		String[] ccMailArr = TestingConfigManager.getInstance().getStringPropertyArray(TestingConfigManager.RMS_TEST_CC_EMAIL);
		System.out.println("Emailing test results");
		Properties props = TestingConfigManager.getInstance().getSmtpServerProps();
		boolean res = mailClient.sendEmail(toMailArr, ccMailArr, mailSubject, report,props);
		if(res){
			System.out.println("Test results emailed successfully");
		}else{
			System.out.println("Couldn't email test result");
		}
	}
}
