package com.test.nextlabs.rms.testAutomation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class PolicyEvalTester {
	
	public String inputFolder = IntegrationTester.inputDir+File.separator+TestingConfigManager.POLICY_EVAL_FOLDER_NAME;
	private String report = "";	

	public void sendPolicyEvalRequest() {
		int totTestCases = 0;
		int numOfFailedCases = 0;
		List<String> failedCases = new ArrayList<String>();
		try{			
			File dir = new File(inputFolder);		
			for (File file : dir.listFiles())
			{
				String inputFileName = file.getName();
				if(!inputFileName.endsWith("_response.xml")){
				   byte[] encoded = Files.readAllBytes(Paths.get(inputFolder+File.separator+inputFileName));
				   Map<String,String> headerMap = new HashMap<>();
				   headerMap.put(TestingConfigManager.WEBSVC_CERTIFICATE_NAME,TestingConfigManager.TRUSTED_CERTIFICATE_VALUE );
				   String responseString = HttpRequestUtil.sendPostRequest(encoded,"EvaluatePolicies",headerMap);
			       responseString = responseString.replaceAll("\\s+", "");
			       totTestCases++;
			       File outputFile = new File(inputFolder+File.separator+FilenameUtils.removeExtension(inputFileName)+"_RESPONSE.xml");
				   if(isResponseEqual(responseString, outputFile)){
					   System.out.println(inputFileName+ ": Success");
				   }else{
					   numOfFailedCases++;					   
					   failedCases.add(inputFileName);
					   System.out.println(inputFileName+": Failed");						   
				   }
				}
			}			
			System.out.println("Test cases executed: "+ totTestCases);
		    System.out.println("Passed: " + (totTestCases-numOfFailedCases));
		    System.out.println("Failed: " + numOfFailedCases);
		    
		    setReport("PolicyEval test results:" + "\n");
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
		} catch (Exception e){
			e.printStackTrace();			
			System.out.println("PolicyEval test failed with an Exception");
			System.out.println("Test cases executed: "+ totTestCases);
		    System.out.println("Passed: " + (totTestCases-numOfFailedCases));
		    System.out.println("Failed: " + numOfFailedCases);
		    
		    setReport("PolicyEval test failed with an Exception:" + "\n");
		    setReport(getReport() + "    Test cases executed: "+ totTestCases +"\n");
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
	}

	public boolean isResponseEqual(String responseString, File file) throws IOException {		
		String outputStr = FileUtils.readFileToString(file, "UTF-8");
		outputStr = outputStr.replaceAll("\\s+", "");
		if(responseString.equals(outputStr)){
			return true;
		}else{
			return false;
		}
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public static void main(String[] args) {
		PolicyEvalTester tester = new PolicyEvalTester();
		tester.sendPolicyEvalRequest();
	}
}
