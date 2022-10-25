if node['installation_mode'] == "install"
	if !Utility::TCP.is_valid_port(node['rms_ssl_port'])
		raise "Invalid SSL port for RMS: #{node['rms_ssl_port']}"
	elsif !Utility::TCP.is_port_available(node['rms_ssl_port'])
		raise "SSL port for RMS is not available: #{node['rms_ssl_port']}"
	elsif !Utility::TCP.is_valid_port(node['rms_shutdown_port'])
		raise "Invalid shutdown port for RMS: #{node['rms_shutdown_port']}"
	elsif !Utility::TCP.is_port_available(node['rms_shutdown_port'])
		raise "Shutdown port for RMS is not available: #{node['rms_shutdown_port']}"
	elsif !Utility::TCP.is_valid_port(node['rmi_km_port'])
		raise "Invalid shutdown port for RMS: #{node['rmi_km_port']}"
	elsif !Utility::TCP.is_port_available(node['rmi_km_port'])
		raise "Shutdown port for RMS is not available: #{node['rmi_km_port']}"
	elsif node['rms_ssl_port'] == node['rms_shutdown_port'] || node['rms_ssl_port'] == node['rmi_km_port'] || node['rmi_km_port'] == node['rms_shutdown_port']
		raise "Port numbers cannot be the same."
	end
	
	if node['install']['rms']
		if node["icenet_server"] == ""
			raise "ICENet server URL is not specified."
		else
			valid = Utility::URLValidator.is_uri_valid? node["icenet_server"]
			if !valid
				raise "Invalid ICENet server URL format: #{node["icenet_server"]}"
			end
		end
	end
end

mysql_db_type = node["mysql"]["db_type"]
mysql_db_name = node["mysql"]["db_name"]
mysql_user_name = node["mysql"]["user_name"]
mysql_hostname = "localhost"

if node['install']['rms'] && !Utility::Config.is_RMS_DB_configured?
	if node['rms_db_type'] == ""
		raise "RMS Database Type not configured"
	elsif node['rms_db_type'].casecmp("IN_BUILT").zero?
		node.set['rms_db_port'] += node["mysql"]["port"] if node['rms_db_port'] == ""
		node.set['rms_db_password'] += node["mysql"]["password"] if node['rms_db_password'] == ""
		node.set['rms_db_type'] = mysql_db_type
		node.set['rms_db_host_name'] = mysql_hostname.downcase
		node.set['rms_db_name'] = mysql_db_name
		node.set['rms_db_username'] = mysql_user_name
	else
		raise "RMS Database Type not configured" if node['rms_db_type'] == ""
		if node['rms_db_conn_url'] == ""
			raise "RMS Database Server Hostname not configured" if node['rms_db_host_name'] == ""
			raise "RMS Database Server Port not configured" if node['rms_db_port'] == ""
			raise "RMS Database Name not configured" if node['rms_db_name'] == ""
		end
		raise "RMS Database Username not configured" if node['rms_db_username'] == ""			
		database_type = node['rms_db_type'].gsub("\r","").gsub("\n","").strip.split.join	
		if database_type.casecmp("MSSQL").zero?
			node.set['rms_db_type'] = "MSSQL"
		elsif database_type.casecmp("ORACLE").zero?
			node.set['rms_db_type'] = "ORACLE"
		elsif database_type.casecmp("MYSQL").zero?
			node.set['rms_db_type'] = "MYSQL"
		else
			raise "RMS Database Type not supported"
		end
	end
end

if node['install']['kms'] && !Utility::Config.is_KMS_DB_configured?
	if node['kms_db_type'] == ""
		raise "KMS Database Type not configured"
	elsif node['kms_db_type'].casecmp("IN_BUILT").zero?
		node.set['kms_db_port'] += node["mysql"]["port"] if node['kms_db_port'] == ""
		node.set['kms_db_password'] += node["mysql"]["password"] if node['kms_db_password'] == ""
		node.set['kms_db_type'] = mysql_db_type
		node.set['kms_db_host_name'] = mysql_hostname.downcase
		node.set['kms_db_name'] = mysql_db_name
		node.set['kms_db_username'] = mysql_user_name
	else
		raise "KMS Database Type not configured" if node['kms_db_type'] == ""
		if node['kms_db_conn_url'] == ""
			raise "KMS Database Server Hostname not configured" if node['kms_db_host_name'] == ""
			raise "KMS Database Server Port not configured" if node['kms_db_port'] == ""
			raise "KMS Database Name not configured" if node['kms_db_name'] == ""
		end
		raise "KMS Database Username not configured" if node['kms_db_username'] == ""			
		database_type = node['kms_db_type'].gsub("\r","").gsub("\n","").strip.split.join	
		if database_type.casecmp("MSSQL").zero?
			node.set['kms_db_type'] = "MSSQL"
		elsif database_type.casecmp("ORACLE").zero?
			node.set['kms_db_type'] = "ORACLE"
		elsif database_type.casecmp("MYSQL").zero?
			node.set['kms_db_type'] = "MYSQL"
		else
			raise "KMS Database Type not supported"
		end
	end
end

if node['install']['rms'] && !Utility::Config.is_RMS_DB_configured? && node['install']['kms'] && !Utility::Config.is_KMS_DB_configured?
	if node['kms_db_type'].casecmp("IN_BUILT").zero? && node['rms_db_type'].casecmp("IN_BUILT").zero?
		raise "The port number for in-built database cannot be different. (#{node['rms_db_port']} and #{node['kms_db_port']}" if node['rms_db_port'] != node['kms_db_port']
		if !Utility::TCP.is_valid_port(node['rms_db_port'])
			raise "Invalid Database Port Number: #{node['rms_db_port']}"
		elsif !Utility::TCP.is_port_available(node['rms_db_port'])
			raise "Database Port Number is not available: #{node['rms_db_port']}"
		end
		raise "The user password for in-built database cannot be different." if node['rms_db_password'] != node['kms_db_password']
	end	
end

if node['install']['kms'] && !node['kms_installed?'] 
	if node['km_keystore_password'] == ""
		raise "KeyStore Password for Key Management Server is not specified."
	elsif node['km_keystore_password'].length < 6
		raise "KeyStore Password for Key Management Server must be at least 6 characters long."
	end
end

puts "Checking system requirements ..."
Chef::Log.warn("Checking system requirements ...")

if platform_family?('windows')
	installation_drive = node['installation_dir'][0,2].upcase 
	data_drive =  node['data_dir'][0,2].upcase 
	
	if(!Dir.exist?(installation_drive) || !Dir.exist?(data_drive))
		raise "Invalid installation directory provided. The installation cannot continue."
	end	
else
	$dir_output = nil
	rms_unix_drive_locator 'locate_dir' do
	end	
	
	installation_drive = $dir_output
	data_drive = $dir_output
end

install_dir_free_space = Integer(node['filesystem'][installation_drive]['kb_available'])/1024/1024.0
data_dir_free_space = Integer(node['filesystem'][data_drive]['kb_available'])/1024/1024.0

if installation_drive != data_drive
	puts "Available space in Installation Directory #{installation_drive} is #{(install_dir_free_space).round(2)} GB."
	puts "Available space in Data Directory #{data_drive} is #{(data_dir_free_space).round(2)} GB."
	puts "Total required space in Installation Directory is #{node['rms_required_space_gb']['install_dir'] } GB."
	puts "Total required space in Data Directory is #{node['rms_required_space_gb']['data_dir'] } GB.\n\n"

	if install_dir_free_space < node['rms_required_space_gb']['install_dir'] 
		raise "Not enough space in drive " + installation_drive + ". The installation cannot continue.\n\n"
	elsif data_dir_free_space < node['rms_required_space_gb']['data_dir']
		raise "Not enough space in drive " + installation_drive + ". The installation cannot continue.\n\n"
	end
else
	required_space = node['rms_required_space_gb']['install_dir'] + node['rms_required_space_gb']['data_dir']
	puts "Available space in #{data_drive} is #{(data_dir_free_space).round(2)} GB."
	puts "Total required space is #{required_space} GB."
	if install_dir_free_space < required_space
		raise "Not enough space in drive " + installation_drive + ". The installation cannot continue.\n\n"
	end
end

if node["license_file_location"] != ""
	if !File.exist? node["license_file_location"] or !File.file? node["license_file_location"] or File.basename(node["license_file_location"]) != node["rms"]["license_file"]
		puts "License file is not found: #{node["license_file_location"]}."
		Chef::Log.warn("License file is not found: #{node["license_file_location"]}.")
	end
else
	puts "No license file is provided."
	Chef::Log.warn("No license file is provided.")
end
puts