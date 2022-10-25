begin
	if platform_family?('windows')
		if Utility::Config.is_RMS_installed? || Utility::Config.is_KMS_installed?
			install_dir, data_dir = Utility::Config.get_installation_dir_and_data_dir
			node.set['installation_dir'] += install_dir if node['installation_dir']==""
			node.set['data_dir'] += data_dir if node['data_dir']==""
		else
			node.set['installation_dir'] = ""	
			node.set['data_dir'] = ""	
		end
	else
		node.set['installation_dir'] = node["rms"]["installation_dir"]
		node.set['data_dir'] = node["rms"]["data_dir"]
	end
	node.set['kms_data_dir'] = File.join(node['data_dir'],'KMS')
	
	unless (node['installation_dir'] != "") and Dir.exist? node['installation_dir']
		puts "Unable to locate Right Management Server. The uninstallation cannot continue."
		Chef::Log.fatal("Unable to locate Right Management Server. The uninstallation cannot continue.");
		return
	end
	
	node.set['uninstall']['rms'] = true
	node.set['uninstall']['kms'] = false
	
	# Read RMS/KMS Version
	existing_ver_info = File.join(node['installation_dir'],node['rms_version_info_json'])
	new_ver_info = File.join(ENV['START_DIR'],'bin',node['rms_version_info_json'])
	version = Utility::Config.get_version_comparison(existing_ver_info, new_ver_info)
	node.set['rms_ver'] = version['old']['rms']
	node.set['kms_ver'] = version['old']['kms']

	include_recipe 'RMS::uninstall_rms'	if node['uninstall']['rms']
	include_recipe 'RMS::uninstall_kms'	if node['uninstall']['kms']	
	
	node.set['uninstall']['all'] = !(Utility::Config.is_RMS_installed? || Utility::Config.is_KMS_installed?)

	if node['uninstall']['all'] 
		if platform_family?('windows')
			puts "Removing Windows Registry values ..."
			Chef::Log.info("Removing Windows Registry values ...")
			registry_key "HKEY_LOCAL_MACHINE\\#{Utility::Config::REGISTRY_KEY_NAME}" do
				recursive true
				action :delete_key
			end
			tomcat_bin =  File.join(node['installation_dir'],'external',node["tomcat"]["name"], 'bin')
			if Dir.exist? tomcat_bin
				#DELETE WINDOWS SERVICE
				rms_service_remover 'remove_rms_service' do
					tomcat_bin 		tomcat_bin
				end		
			end
			if Utility::Config.is_mysql_installed?
				mysql_bin =  File.join(node['installation_dir'],'external','mysql', 'bin')
				#STOP WINDOWS SERVICE
				begin
					mysql_service_stopper 'stop_mysql_service' do
					end		
				rescue Mixlib::ShellOut::ShellCommandFailed => error
					Chef::Log.error(error.message)
					raise "Error occurred while stopping Windows Service for MySQL."
				end
				#DELETE WINDOWS SERVICE
				begin
					mysql_service_remover 'remove_mysql_service' do
						mysql_bin	mysql_bin
					end		
				rescue Mixlib::ShellOut::ShellCommandFailed => error
					Chef::Log.error(error.message)
					raise "Error occurred while deleting Windows Service for MySQL."
				end
			end
		else
			puts "Deleting Linux service for Tomcat ..."
			Chef::Log.info("Deleting Linux service for Tomcat ...")
			FileUtils.rm_rf("/etc/init.d/#{node['rms_service_name']}")
			FileUtils.rm_rf("/etc/systemd/system/multi-user.target.wants/#{node['rms_service_name']}.service");
			if Utility::Config.is_mysql_installed?
				execute "stop_service_mysql" do
					command "service #{node["rms__mysql_service_name_unix"]} stop"
					action :nothing
				end.run_action(:run)
				FileUtils.rm_rf("/etc/init.d/#{["rms__mysql_service_name_unix"]}")
			end
		end

		# Removing Directories
		puts "Deleting Installation Directory ..."
		Chef::Log.info("Deleting Installation Directory ...")
		FileUtils.rm_rf(node['installation_dir'])
		
		jpc_path = File.join(node['data_dir'], node["embedded_jpc"]["name"])
		if Dir.exist?(jpc_path)
			Dir.entries("#{jpc_path}").each do |f|
				FileUtils.rm_rf (File.join(jpc_path, f)) unless %w{. .. jservice}.any? {|val| val == f}
			end
			jservice_path = File.join(jpc_path, "jservice")
			jservice_jar_path = File.join(jservice_path, "jar")
			jservice_config_path = File.join(jservice_path, "config")
			keymanagement_prop_file = File.join(jservice_config_path, node["jpc"]["plugins"]["key_management"]["properties"])
			keymanagement_jar_path = File.join(jservice_jar_path, node["jpc"]["plugins"]["key_management"]["name"])
			if File.exist? keymanagement_prop_file
				FileUtils.rm_rf keymanagement_prop_file
			end
			if Dir.exist? keymanagement_jar_path
				FileUtils.rm_rf keymanagement_jar_path
			end
		end
		puts "Installation Directory deleted successfully."
		Chef::Log.info("Installation Directory deleted successfully.")
	end
	
	include_recipe 'RMS::start_rms' unless node['uninstall']['all']
	
rescue => error
	puts "Error: #{error.message}"
	Chef::Log.fatal(error.message)
	puts "Uninstallation failed."
	return
end

puts "Uninstallation completed successfully."
Chef::Log.info("Uninstallation completed successfully.")