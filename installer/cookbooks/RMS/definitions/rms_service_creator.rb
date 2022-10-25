define :rms_service_creator, :tomcat_bin=>nil do
	dir = params[:tomcat_bin]
	tomcat_exe = ""
	Dir.chdir(dir) do
		tomcat_exe = Dir.glob("tomcat*[^w].exe")[0]
	end

	#CREATE SERVICE
	execute 'create_windows_service' do
		sensitive  	true
		action		:nothing
		cwd			dir
		command 	"service install " + node["rms_service_name"]
		environment ({	'JRE_HOME' => node['rms_jre_home'].gsub('/', '\\'),
						'JAVA_HOME' => '',
						'CATALINA_HOME' => node['rms_catalina_home'].gsub('/', '\\'),
						'CATALINA_BASE' => node['rms_catalina_home'].gsub('/', '\\')
					})
	end.run_action(:run)

	#CUSTOMIZE SERVICE
	execute 'customize_service' do
		sensitive	true
		action		:nothing
		cwd			dir
		command		tomcat_exe + " //US//" + node["rms_service_name"] +" --Description \"NextLabs Rights Management Server " + "\" --DisplayName \"NextLabs Rights Management Server\" --Startup auto"
	end.run_action(:run)
end