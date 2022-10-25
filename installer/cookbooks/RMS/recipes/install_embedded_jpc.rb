if node['install']['embedded_jpc'] 
	comm_profile_filename="commprofile.xml"
	puts "Installing Embedded JPC ..."
	Chef::Log.info("Installing Embedded JPC ...")

	if !node['rms_installed?']
		options={
			remove_destination:true
		}
		FileUtils.cp_r(File.join(node['installer_home'],node["rms"]["embedded_jpc"], "."), node['embedded_jpc_dir'], options)
		
		puts "Creating commprofile.xml ..."
		Chef::Log.info("Creating #{comm_profile_filename} ...")
		commprofile_path = File.join(node['embedded_jpc_dir'], "config", "#{comm_profile_filename}")
		contents = File.read(commprofile_path)

		location = node['icenet_server'].chomp("/") 
		output = contents.gsub('https://localhost:8443/dabs', "#{location}/dabs")
		File.write(commprofile_path, output)
		
		puts "Configuring Key Management Service ..."
		Chef::Log.info("Configuring Key Management Service ...")
		
		keymanagement_jar_path = File.join(node['embedded_jservice_jar_path'], node["jpc"]["plugins"]["key_management"]["name"])
		keymanagement_config_path = node['embedded_jservice_config_path']
		if !Dir.exist? (keymanagement_jar_path)
			FileUtils.mkdir_p keymanagement_jar_path
		end
		
		if !Dir.exist? (keymanagement_config_path)
			FileUtils.mkdir_p keymanagement_config_path
		end
		keymanagement_jar_name = node["jpc"]["plugins"]["key_management"]["jar"]
		keymanagement_prop_name = node["jpc"]["plugins"]["key_management"]["properties"]
		key_store_source = File.join(node['installer_home'],node["rms"]["conf"],"cert", "rmskmc-keystore.jks")
		trust_store_source = File.join(node['installer_home'],node["rms"]["conf"],"cert", "rmskmc-truststore.jks")
		
		FileUtils.mv File.join(node['embedded_jpc_dir'], keymanagement_jar_name), keymanagement_jar_path
		
		puts "Copying Key Management Service Truststore ..."
		Chef::Log.info ("Copying Key Management Service Truststore ...")
		FileUtils.cp trust_store_source, keymanagement_jar_path
		puts "Copying Key Management Service Keystore ..."
		Chef::Log.info ("Copying Key Management Service Keystore ...")
		FileUtils.cp key_store_source, keymanagement_jar_path
		
		FileUtils.cp File.join(node['installer_home'], node["rms"]["conf"], "javapc", keymanagement_prop_name), keymanagement_config_path
		
		contents = File.read(File.join(keymanagement_config_path, keymanagement_prop_name))
		output = contents.gsub("<RMS_KM_JAR_PATH>", keymanagement_jar_path.gsub('\\', '/')).gsub("<RMI_PORT_NUMBER>", node['rmi_km_port'])
		File.write(File.join(keymanagement_config_path, keymanagement_prop_name), output)
		
		puts "Key Management Service configured successfully."
		Chef::Log.info("Key Management Service configured successfully.")
	else
		embedded_pdp_jar_source_path = File.join(node['installer_home'], node["rms"]["embedded_jpc"], "embeddedpdp.jar")
		target_path = node['embedded_jpc_dir']
		if File.exist? (node['embedded_pdp_jar'])
			FileUtils.rm_f node['embedded_pdp_jar']
		end
		puts "Copying Embedded JPC jar ..."
		Chef::Log.info ("Copying Embedded JPC jar ...")
		FileUtils.cp embedded_pdp_jar_source_path, target_path
	end
	puts "Embedded JPC installed successfully."
	Chef::Log.info("Embedded JPC installed successfully.")
end