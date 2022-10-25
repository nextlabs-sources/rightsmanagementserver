if platform_family?('windows')
	puts "Removing RMS from Windows Registry values ..."
	Chef::Log.info("Removing RMS from Windows Registry values ...")
	registry_key "HKEY_LOCAL_MACHINE\\#{Utility::Config::REGISTRY_KEY_NAME}" do
		values [{:name => 'RMSVersion', :type => :string, :data => ""}
		]
		recursive true
		action :nothing
	end.run_action (:create)
	
	tomcat_bin =  File.join(node['installation_dir'],'external',node["tomcat"]["name"], 'bin')
	if Dir.exist? tomcat_bin
		#STOP WINDOWS SERVICE
		rms_service_stopper 'stop_rms_service' do
			tomcat_bin 		tomcat_bin
		end
	end
	sys_path = ""
	reg_val_arr = registry_get_values("HKEY_LOCAL_MACHINE\\System\\CurrentControlSet\\Control\\Session Manager\\Environment")
	reg_val_arr.each { |val|
		if val[:name] == "Path"
			sys_path = val[:data]
			break
		end
	}
	
	# Deleting Path Entry
	new_path = ""
	sys_path.split(";").each { |entry|
		new_path += entry + ";"		if (!entry.include?(File.join(node['installation_dir'],"external","perceptive").gsub("/","\\")) && entry!="")
	}
	puts "Removing variables from system path ..."
	Chef::Log.info("Removing variables from system path ...")
	registry_key "HKEY_LOCAL_MACHINE\\System\\CurrentControlSet\\Control\\Session Manager\\Environment" do
		values [{
			:name => "Path",
			:type => :expand_string,
			:data => new_path #sys_path
		}]
		action	:nothing
	end.run_action(:create)
	puts "Removed variables from system path."
	Chef::Log.info("Removed variables from system path.")
else	#LINUX
	if File.exists?("/etc/init.d/#{node["rms_service_name"]}")
		puts "Stopping Rights Management Server ..."
		Chef::Log.info("Stopping Rights Management Server ...")
		begin
			execute 'stop_linux_service' do
				command	"/etc/init.d/#{node["rms_service_name"]} stop"
				sensitive true
				action	:nothing
			end.run_action(:run)
		rescue Mixlib::ShellOut::ShellCommandFailed => error
			Chef::Log.error(error.message)
			puts "Unable to stop Linux service."
		end
	end
end

puts "Deleting RMS Tools and Webapp ..."
Chef::Log.info("Deleting RMS Tools and Webapp ...")
FileUtils.rm_rf(File.join(node['installation_dir'],"RMS"))
FileUtils.rm_rf(File.join(node['installation_dir'],"external","tomcat","webapps","RMS"))
FileUtils.rm_rf(File.join(node['installation_dir'],"external","tomcat","webapps","RMS.war"))
FileUtils.rm_rf(File.join(node['installation_dir'],"external","RMSCADCONVERTER"))
FileUtils.rm_rf(File.join(node['installation_dir'],"external","perceptive"))
FileUtils.rm_rf(File.join(node['installation_dir'],"external","IpToCountry.csv"))
FileUtils.rm_rf(File.join(node['installation_dir'],"sharepoint"))
FileUtils.rm_rf(File.join(node['installation_dir'],"RMS.war"))

#UPDATE VERSION INFO
existing_ver_info = File.join(node['installation_dir'],".version_info.json")
file = File.read(existing_ver_info)
version = JSON.parse(file)
version['rms']  = ""
File.open(existing_ver_info, 'w') do |file|
	file.write(JSON.pretty_generate(version))
end

if node['delete_data_dir'].downcase == "yes"
	puts "Deleting RMS Data Directory ..."
	Chef::Log.info("Deleting RMS Data Directory ...")
	if node['kms_data_dir'].include? node['data_dir']
		Dir.entries("#{node['data_dir']}").each do |f|
			FileUtils.rm_rf (File.join(node['data_dir'], f)) unless %w{. .. KMS}.any? {|val| val == f}
		end
	else
		FileUtils.rm_rf node['data_dit']
	end
	puts "RMS Data Directory deleted successfully."
	Chef::Log.info("RMS Data Directory deleted successfully.")
end