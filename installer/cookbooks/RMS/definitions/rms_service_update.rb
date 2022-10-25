define :rms_service_update, :tomcat_bin=>nil do
	dir = params[:tomcat_bin]
	tomcat_exe = ""
	Dir.chdir(dir) do
		tomcat_exe = Dir.glob("tomcat*[^w].exe")[0]
	end

	#Updating parameters in tomcat
	execute 'customize_service_parameters' do
		sensitive	true
		action		:nothing
		cwd			dir
		command		"#{tomcat_exe} //US//#{node["rms_service_name"]} --JvmMx=512 ++JvmOptions \"-Djava.net.preferIPv4Stack=true;-Djava.net.preferIPv4Addresses=true\"" + ";-Done-jar.jar.path=\"#{node['embedded_pdp_jar'].gsub('/', '\\')}\" ++Environment \"RMSDATADIR=#{node['data_dir']};RMSINSTALLDIR=#{node['installation_dir']};KMSDATADIR=#{node['kms_data_dir']};KMSINSTALLDIR=#{node['installation_dir']}"
	end.run_action(:run)
end