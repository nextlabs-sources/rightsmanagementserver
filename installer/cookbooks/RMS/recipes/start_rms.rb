if platform_family?('windows')
	if Utility::Config.is_mysql_installed?
		puts "Starting MySQL Server ..."
		Chef::Log.info("Starting MySQL Server ...")
		service 'start_mysql' do
			service_name node["mysql"]["service_name"]
			action :nothing
		end.run_action(:start)
	end
	puts "Starting Rights Management Server ..."
	Chef::Log.info("Starting Rights Management Server ...")
	service 'start_rms' do
		service_name node['rms_service_name']
		action :nothing
	end.run_action(:start)

elsif platform_family?('rhel')
	if Utility::Config.is_mysql_installed?
		puts "Starting MySQL Server ..."
		Chef::Log.info("Starting MySQL Server ...")
		execute "run_service_mysql" do
			command "service #{node["rms__mysql_service_name_unix"]} start"
			action :nothing
		end.run_action(:run)
		execute "chkconfig_#{node["rms__mysql_service_name_unix"]}_on" do
			command "chkconfig #{node["rms__mysql_service_name_unix"]} on"
			action :nothing
		end.run_action(:run)
	end
	puts "Starting Rights Management Server ..."
	Chef::Log.info("Starting Rights Management Server ...")
	#Start RMS service
	execute "run_service_tomcat" do
		command "service " + node["rms_service_name"] + " start"
		action :nothing
	end.run_action(:run)
	
	execute "chkconfig_#{node["rms_service_name"]}_on" do
		command "chkconfig " + node["rms_service_name"] + " on"
		action :nothing
	end.run_action(:run)
	
elsif platform_family?('debian')
	if(node['platform'] == "ubuntu" && node['platform_version'] >= "15")
		puts "Registering service in systemd ..."
		Chef::Log.info("Registering service in systemd ...")
		template "create_systemd_service" do 
			path  "/etc/systemd/system/multi-user.target.wants/#{node["rms_service_name"]}.service"
			source "rms.service.erb"
			mode "0774"
			sensitive true
			action :nothing
		end.run_action(:create)
=begin
		execute "systemctl enable #{node["rms_service_name"]}" do
			action :nothing
		end.run_action(:run)
		execute "systemctl start #{node["rms_service_name"]}" do
			action :nothing
		end.run_action(:run)
=end
	else
		puts "Could not start Rights Management Server."
		Chef::Log.warn("Could not start Rights Management Server.")
		return
	end
end

puts "Rights Management Server successfully started."
Chef::Log.info("Rights Management Server successfully started.")
