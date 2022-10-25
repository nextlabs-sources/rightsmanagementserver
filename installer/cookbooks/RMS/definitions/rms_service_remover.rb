define :rms_service_remover, :tomcat_bin=>nil do
	dir = params[:tomcat_bin]
	tomcat_exe = ""
	Dir.chdir(dir) do
		tomcat_exe = Dir.glob("tomcat*[^w].exe")[0]
	end
	
	#DELETE SERVICE
	puts "Deleting Existing Windows Service ..."
	Chef::Log.info("Deleting Existing Windows Service ...")	
	execute 'delete_windows_service' do
		action		:nothing
		cwd			dir
		command		tomcat_exe + " //DS//" + node["rms_service_name"]
		returns [0,9]	#returns 9 if service does not exist
		action	:nothing
		sensitive true
	end.run_action(:run)
	puts "Deleted Existing Windows Service."
	Chef::Log.info("Deleted Existing Windows Service ...")
end