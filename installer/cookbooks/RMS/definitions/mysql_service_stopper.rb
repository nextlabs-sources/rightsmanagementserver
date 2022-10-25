define :mysql_service_stopper do
	#STOP SERVICE
	puts "Stopping MySQL Windows Service ..."
	Chef::Log.info("Stopping MySQL Windows Service ...")	
	execute 'stop_windows_service' do
		action		:nothing
		command		"net stop \"#{node["mysql"]["service_name"]}\""
		returns [0,2]		#returns 2 if service does not exist
		action	:nothing
		sensitive true
	end.run_action(:run)
	puts "Stopped MySQL Windows Service."
	Chef::Log.info("Stopped MySQL Windows Service.")	
end