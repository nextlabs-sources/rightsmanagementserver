package com.test.nextlabs.rms;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class PerceptivePerformanceTester {
	public static void main(String args[]){
		System.out.println("java.library.path:"+System.getProperty("java.library.path"));
		System.out.println("java.io.tmpdir:"+System.getProperty("java.io.tmpdir"));
		long startTime = System.currentTimeMillis();
		int count=Integer.parseInt(args[0].trim()); 
		if(count<0)
		count=10;
		for(int i=0;i<count;i++)
		{
			try {
				FileUtils.copyFile(new File("C:\\temp\\perceptive\\Performance_Out.pptx"), new File("C:\\temp\\perceptive\\Performance_Out"+i+".pptx"));
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(int i=0;i<count;i++)
		{
			Thread t=new Thread(new PerceptivePerformanceThread(i,"C:\\temp\\perceptive\\Performance_Out"+i+".pptx"));
			t.start();
		
		}
	}
}
