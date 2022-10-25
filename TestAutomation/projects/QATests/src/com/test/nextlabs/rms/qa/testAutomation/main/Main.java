package com.test.nextlabs.rms.qa.testAutomation.main;

public class Main {
	public static void main(String[] args) {
		zipCompressor compress = new zipCompressor("test-output/report.zip");
		compress.compress("test-output/html");
		GetEmailData data = new GetEmailData();
		data.getData();
		data.sendEmail();
	}
}
