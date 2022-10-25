javaExec = File.join(node['rms_jre_home'],"bin","java")
installer_dis_lib = File.join(node['installer_home'],"dist","rms","lib")
installer_dis_tools = File.join(node['installer_home'],"dist","rms","tools")
overwrite_option = {remove_destination: true}

if node['install']['rms']
	rms_tools_dir = File.join(node['installation_dir'], "RMS", "tools")
	
	puts "Creating RMS Utilities ..."
	Chef::Log.info("Creating RMS Utilities ...") 
	
	rms_crypt_dir = File.join(rms_tools_dir, "crypt")
	FileUtils.mkdir_p rms_crypt_dir unless Dir.exists? rms_crypt_dir
	
	jars = ["commons-codec-1.10.jar",
			"RMEncryptionUtil.jar"
	]
	
	if platform_family?('windows')
		crypt_script = File.join(rms_crypt_dir, "crypt.bat")
		crypt_file = File.join(installer_dis_tools, "crypt.bat")
	else
		crypt_script = File.join(rms_crypt_dir, "crypt.sh")
		crypt_file = File.join(installer_dis_tools, "crypt.sh")
	end
	
	Dir.chdir(installer_dis_lib) do
		jars.each { |jar|
			FileUtils.cp_r(jar, rms_crypt_dir, overwrite_option)
		}
	end
	FileUtils.cp_r(crypt_file, crypt_script, overwrite_option)
	
	puts "Created RMS Utilities."
	Chef::Log.info("Created RMS Utilities.") 
end

#CREATE KEY MANAGEMENT UTIL
if node['install']['kms']
	kms_tools_dir = File.join(node['installation_dir'], "KMS", "tools")
	
	puts "Creating KMS Utilities ..."
	Chef::Log.info("Creating KMS Utilities ...") 
	
	kms_crypt_dir = File.join(kms_tools_dir, "crypt")
	FileUtils.mkdir_p kms_crypt_dir unless Dir.exists? kms_crypt_dir
	
	jars = ["commons-codec-1.10.jar",
			"RMEncryptionUtil.jar"
	]

	if platform_family?('windows')
		crypt_script = File.join(kms_crypt_dir, "crypt.bat")
		crypt_file = File.join(installer_dis_tools, "crypt.bat")
	else
		crypt_script = File.join(kms_crypt_dir, "crypt.sh")
		crypt_file = File.join(installer_dis_tools, "crypt.sh")
	end
	
	Dir.chdir(installer_dis_lib) do
		jars.each { |jar|
			FileUtils.cp_r(jar, kms_crypt_dir, overwrite_option)
		}
	end
	FileUtils.cp_r(crypt_file, crypt_script, overwrite_option)
	
	keymanagement_dir = File.join(kms_tools_dir, "keymanagement")
	FileUtils.mkdir_p keymanagement_dir unless Dir.exists? keymanagement_dir
	
	jars = ["KMS.jar",	
			"RMSUtil.jar",
			"KMS_xmlbeans.jar",
			"commons-io-2.4.jar",
			"commons-codec-1.10.jar",
			"slf4j-api-1.7.13.jar",
			"slf4j-log4j12-1.7.13.jar",
			"log4j-1.2.17.jar",
			"org.restlet-2.3.5.jar",
			"org.restlet.ext.jaxb-2.3.5.jar"
	]

	if platform_family?('windows')
		keymanagement_script = File.join(keymanagement_dir, "keymanagement.bat")
		javaExec = javaExec.gsub("/","\\")
		classPath = jars.join(";")
	else
		keymanagement_script = File.join(keymanagement_dir, "keymanagement.sh")
		classPath = jars.join(":")
	end
	
	if platform_family?('windows')
		kmgmt_script = File.join(keymanagement_dir, "keymanagement.bat")
		kmgmt_file = File.join(installer_dis_tools, "keymanagement.bat")
	else
		kmgmt_script = File.join(keymanagement_dir, "keymanagement.sh")
		kmgmt_file = File.join(installer_dis_tools, "keymanagement.sh")
	end
	
	Dir.chdir(installer_dis_lib) do
		jars.each { |jar|
			FileUtils.cp_r(jar, keymanagement_dir, overwrite_option)
		}
	end
	FileUtils.cp_r(kmgmt_file, kmgmt_script, overwrite_option)
	
	puts "Created KMS Utilities."
	Chef::Log.info("Created KMS Utilities.")
	
end