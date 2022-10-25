package com.nextlabs.rms.eval;

import java.util.List;

import com.nextlabs.rms.locale.RMSMessageHandler;

public class MissingDependenciesException extends Exception{

	private static final long serialVersionUID = -4286159944344240820L;
	
	private List<String> missingParts;
	
	public MissingDependenciesException(List<String> parts){
		super();
		missingParts = parts;
	}
	
	public List<String> getMissingDependencies(){
		return missingParts;
	}
	
	@Override
	public String getMessage(){
		String errorMessage = RMSMessageHandler.getClientString("missingDependenciesErr");
		for(String part: missingParts)
			errorMessage = errorMessage.concat(part + System.lineSeparator());
		return errorMessage;
	}
	
}
