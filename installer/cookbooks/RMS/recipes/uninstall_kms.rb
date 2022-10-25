if platform_family?('windows')
	puts "Removing KMS from Windows Registry values ..."
	Chef::Log.info("Removing KMS from Windows Registry values ...");
	registry_key "HKEY_LOCAL_MACHINE\\#{Utility::Config::REGISTRY_KEY_NAME}" do
		values [
			  {:name => 'KMSVersion', :type => :string, :data => ""},
			  {:name => 'KMSDataDir', :type => :string, :data => ""}
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
	
puts "Deleting KMS Tools and Webapp ..."
Chef::Log.info("Deleting KMS Tools and Webapp ...")
FileUtils.rm_rf(File.join(node['installation_dir'],"KMS"))
FileUtils.rm_rf(File.join(node['installation_dir'],"external","tomcat","webapps","KMS"))
FileUtils.rm_rf(File.join(node['installation_dir'],"external","tomcat","webapps","KMS.war"))

#UPDATE VERSION INFO
existing_ver_info = File.join(node['installation_dir'],".version_info.json")
file = File.read(existing_ver_info)
version = JSON.parse(file)
version['kms']  = ""
File.open(existing_ver_info, 'w') do |file|
	file.write(JSON.pretty_generate(version))
end

if node['delete_data_dir'].downcase == "yes"
	puts "Deleting KMS Data Directory ..."
	Chef::Log.info("Deleting KMS Data Directory ...")
	FileUtils.rm_rf(node['kms_data_dir'])
	puts "KMS Data Directory deleted successfully."
	Chef::Log.info("KMS Data Directory deleted successfully.")
end