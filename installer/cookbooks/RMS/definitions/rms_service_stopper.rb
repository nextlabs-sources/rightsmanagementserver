define :rms_service_stopper, :tomcat_bin=>nil do
	dir = params[:tomcat_bin]
	tomcat_exe = ""
	Dir.chdir(dir) do
		tomcat_exe = Dir.glob("tomcat*[^w].exe")[0]
	end
	
	#STOP SERVICE
	puts "Stopping Windows Service ..."
	Chef::Log.info("Stopping Windows Service ...")	
	execute 'stop_windows_service' do
		action		:nothing
		cwd			dir
		command		tomcat_exe + " //SS//" + node["rms_service_name"]
		returns [0,6,9]		#returns 9 if service does not exist
		action	:nothing
		sensitive false
	end.run_action(:run)
	puts "Stopped Windows Service."
	Chef::Log.info("Stopped Windows Service.")	
end