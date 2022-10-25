if node['installation_mode'] == "upgrade" 

	if platform_family?('windows')
		begin
			tomcat_bin =  File.join(node['installation_dir'],'external',node["tomcat"]["name"], 'bin')
			if Dir.exist? tomcat_bin
				puts "Stopping Rights Management Server ..."
				Chef::Log.info("Stopping Rights Management Server ...")	
				#STOP WINDOWS SERVICE
				rms_service_stopper 'stop_rms_service' do
					tomcat_bin 		tomcat_bin
				end
				puts "Rights Management Server is stopped successfully."
				Chef::Log.info("Rights Management Server is stopped successfully.")	
			end	
		rescue Mixlib::ShellOut::ShellCommandFailed => error
			Chef::Log.error(error.message)
			raise "Error occurred while stopping Rights Management Server."
		end
		
	else
		if platform_family?('debian')
			if(node['platform'] == "ubuntu" && node['platform_version'] >= "15")
				execute "systemctl stop #{node["rms_service_name"]}" do
					action :nothing
				end.run_action(:run)
				execute "systemctl disable #{node["rms_service_name"]}" do
					action :nothing
				end.run_action(:run)
				puts "Rights Management Server is stopped successfully."
				Chef::Log.info("Rights Management Server is stopped successfully.")	
			end
		else
			begin
				if File.exists?("/etc/init.d/#{node["rms_service_name"]}")
					puts "Stopping Rights Management Server ..."
					Chef::Log.info("Stopping Rights Management Server ...")	
					execute 'stop_linux_service' do
						command	"/etc/init.d/#{node["rms_service_name"]} stop"
						sensitive true
						action	:nothing
					end.run_action(:run)
					puts "Rights Management Server is stopped successfully."
					Chef::Log.info("Rights Management Server is stopped successfully.")	
				end
			rescue Mixlib::ShellOut::ShellCommandFailed => error
				Chef::Log.error(error.message)
				raise "Error occurred while stopping Rights Management Server."
			end
		end
	end
end

