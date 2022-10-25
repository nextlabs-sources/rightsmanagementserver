define :mysql_service_remover, :mysql_bin=>nil do
	dir = params[:mysql_bin]
	mysqld = ""
	Dir.chdir(dir) do
		mysqld = Dir.glob("mysqld.exe")[0]
	end
	
	#DELETE SERVICE
	puts "Deleting Existing MySQL Windows Service ..."
	Chef::Log.info("Deleting Existing MySQL Windows Service ...")	
	execute 'delete_mysql_windows_service' do
		action		:nothing
		cwd			dir
		command		mysqld + " --remove \"#{node["mysql"]["service_name"]}\""
		returns [0]	
		action	:nothing
		sensitive true
	end.run_action(:run)
	puts "Deleted Existing MySQL Windows Service."
	Chef::Log.info("Deleted Existing MySQL Windows Service ...")
end