package com.test.nextlabs.rms;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

//import com.nextlabs.rms.PerceptiveDocumentConverter;

public class PerceptivePerformanceThread implements Runnable {

	private int id;
	String inputFile;
	public PerceptivePerformanceThread(int id,String inputFile)
	{
		this.id=id;
		this.inputFile=inputFile;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		System.out.println("Starting thread with id =" + id);
		long startTime = System.currentTimeMillis();
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		File outputFolder = new File(tmpDir + File.separator + "upload" + id );
		if(outputFolder.exists()){
			try {
				FileUtils.deleteDirectory(outputFolder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("");
//		PerceptiveDocumentConverter converter = new PerceptiveDocumentConverter(id);
		File file = new File(inputFile);
//		converter.convert(file, null);
		String str=UUID.randomUUID().toString()+id;
		int numFiles = outputFolder.listFiles().length;
		System.out.println("Time taken for conversion of thread with id = "+id+" is "+(System.currentTimeMillis()-startTime) + " ms " + "NumFiles in folder:"+numFiles);
	}
}
