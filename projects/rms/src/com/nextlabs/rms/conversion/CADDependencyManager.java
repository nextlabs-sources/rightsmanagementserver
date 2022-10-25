package com.nextlabs.rms.conversion;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import com.nextlabs.rms.eval.RMSException;
import com.nextlabs.rms.locale.RMSMessageHandler;
import com.nextlabs.rms.util.StringUtils;

public class CADDependencyManager {

	private static Logger logger = Logger.getLogger(CADDependencyManager.class);
	
	private static final String HOOPS_DEPENDENCIES_ASSEMBLIES = "#ASSEMBLIES";
	private static final String HOOPS_DEPENDENCIES_PARTS = "#PARTS";
	private static final String HOOPS_DEPENDENCIES_MISSING = "#MISSING";
	
	public List<String> getDependencies(String assemblyFile) throws RMSException{
	    String outputFile = assemblyFile + ".txt";
		if(generateDependenciesFile(assemblyFile, outputFile)) {
			return readDependenciesFile(outputFile);
		}
		else {
			logger.error("Error occurred while processing the file:"+ FilenameUtils.getBaseName(assemblyFile));
			throw new RMSException(RMSMessageHandler.getClientString("fileProcessErr"));
		}	
	}
	
	private List<String> readDependenciesFile(String dependenciesFile) throws RMSException{
		List<String> assemblies = new ArrayList<>();
		List<String> parts = new ArrayList<>();
		List<String> missing = new ArrayList<>();
		int assemblyTagCount = 0; 
		int partsTagCount = 0;
		int missingTagCount = 0;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(dependenciesFile));
		    String line;
		    while ((line = br.readLine()) != null) {
		    	
		    	 if(StringUtils.equals(HOOPS_DEPENDENCIES_ASSEMBLIES, line)){
		    		 assemblyTagCount++;
		    		 while((line=br.readLine())!=null && !StringUtils.equals(HOOPS_DEPENDENCIES_PARTS, line)){
		    	  	 if(StringUtils.hasText(line)) {
		    	  		 assemblies.add(line);
		    	  	 }
		    	   }
		    	 }
		    	 
		    	 if(StringUtils.equals(HOOPS_DEPENDENCIES_PARTS, line)){
		    		 partsTagCount++;
		    		 while((line=br.readLine())!=null && !StringUtils.equals(HOOPS_DEPENDENCIES_MISSING, line)){
		    	  	 if(StringUtils.hasText(line)) {
		    	  		 parts.add(line);
		    	  	 }
		    	   }
		    	 }
		    	 
		    	 if(StringUtils.equals(HOOPS_DEPENDENCIES_MISSING, line)){
		    		 missingTagCount ++;
	    		   while((line=br.readLine())!=null && !line.equals("")){
	    		  	 int winIdx = line.lastIndexOf("\\");
	    		  	 int idx = line.lastIndexOf("/");
	    		  	 int index = Math.max(winIdx, idx);
	    			   String missing_part = line.substring(index+1);
	    			   missing.add(missing_part);
	    		   }
		       }
		    }
		    if(assemblyTagCount == 0 || partsTagCount == 0 || missingTagCount == 0){
		    	String msg = "Dependencies file is not in the correct format. ";
		    	msg += assemblyTagCount == 0 ? "No Assemblies Tag is found. " : "";
		    	msg += partsTagCount == 0 ? "No Parts Tag is found. " : "";
		    	msg += missingTagCount == 0 ? "No Missing Tag is found. " : "";
		    	logger.error(msg);
		    	throw new RMSException(RMSMessageHandler.getClientString("fileProcessErr"));
		    }    
		} catch (RMSException e) {
			 throw e;
		} catch (Exception e) {
			 logger.error(e);
		} finally {
			try {
				if(br!=null)
					br.close();
			} catch (IOException e) {
				 logger.error(e);
			}
		}
		return missing;
	}
		
	private boolean generateDependenciesFile (String inputPath, String destinationPath) throws RMSException{
		return new CADConverter().executeHOOPSConverter(inputPath, destinationPath, "--output_dependencies");
	}
}