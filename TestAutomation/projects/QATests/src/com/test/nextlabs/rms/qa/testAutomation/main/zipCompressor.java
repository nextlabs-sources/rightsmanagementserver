package com.test.nextlabs.rms.qa.testAutomation.main;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class zipCompressor {
	
	static final int BUFFER = 8192;
	
	private File zipFile;
	
	public zipCompressor(String pathName)
	{
		zipFile = new File(pathName);
	}
	
    public void compress(String srcPathName) {  
        File file = new File(srcPathName);  
        if (!file.exists())  
            throw new RuntimeException(srcPathName + "not find!");  
        try {  
            FileOutputStream fileOutputStream = new FileOutputStream(zipFile);  
            CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream,  
                    new CRC32());  
            ZipOutputStream out = new ZipOutputStream(cos);  
            String basedir = "";  
            compress(file, out, basedir);  
            out.close();  
        } catch (Exception e) {  
            throw new RuntimeException(e);  
        }  
    }  

	//compress a folder
	public static void compressDir(File dir, ZipOutputStream out, String basedir)
	{
		
		if(!dir.exists())
		{
			return;
		}
		
		File[] files = dir.listFiles();
		for(int i=0;i<files.length;i++)
		{
			compress(files[i], out, basedir + dir.getName() + "/");  
		}
	}
	
	//compress a file
	public static void compressFile(File file, ZipOutputStream out, String basedir)
	{
		if (!file.exists()) {  
			            return;  
			        }  
			        try {  
			        	BufferedInputStream bis = new BufferedInputStream(  
			                    new FileInputStream(file));  
			            ZipEntry entry = new ZipEntry(basedir + file.getName());  
			            out.putNextEntry(entry);  
			            int count;  
			            byte data[] = new byte[BUFFER];  
			            while ((count = bis.read(data, 0, BUFFER)) != -1) {  
			                out.write(data, 0, count);  
		            }  
			            bis.close();  
			        } catch (Exception e) {  
			           throw new RuntimeException(e);  
			        }  

	}
	
	// judge file is dir or file
	public static void compress(File file, ZipOutputStream out, String basedir)
	{
		if(file.isDirectory())
		{
			compressDir(file, out, basedir);
		}else
		{
			compressFile(file, out, basedir);
		}
	}
	
}
