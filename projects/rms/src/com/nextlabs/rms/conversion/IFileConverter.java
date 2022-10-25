package com.nextlabs.rms.conversion;

import com.nextlabs.rms.eval.RMSException;

public interface IFileConverter {
	
	public boolean convertFile(String inputPath,String destinationPath) throws RMSException;

}
