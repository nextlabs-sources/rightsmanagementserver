puts "Installing CAD Viewer pre-requisites ..."
Chef::Log.info("Installing CAD Viewer pre-requisites ...")

#for 8.3, delete existing cad bin folder
existing_cad_bin_dir = File.join(node['installation_dir'],"external","RMSCADCONVERTER")
FileUtils.rm_rf existing_cad_bin_dir

if !node['rms_installed?'] && platform_family?("windows")
	ms2012_exe = '"' + File.join(node['installer_home'],node['rms']['cad']['ms2012']).gsub("/","\\") + '"'
	ms2013_exe = '"' + File.join(node['installer_home'],node['rms']['cad']['ms2013']).gsub("/","\\") + '"'
	ms2015_exe = '"' + File.join(node['installer_home'],node['rms']['cad']['ms2015']).gsub("/","\\") + '"'
	ms2010_exe = '"' + File.join(node['installer_home'],node['rms']['cad']['ms2010']).gsub("/","\\") + '"'
	begin
		execute 'install_msvcr_30679' do 
			command "#{ms2012_exe} /quiet"
			action :nothing
		end.run_action(:run)
		sleep(5)
		execute 'install_msvcr_40784' do 
			command "#{ms2013_exe} /quiet"
			action :nothing
		end.run_action(:run)
		sleep(5)
		execute 'install_msvcr_48145' do 
			command "#{ms2015_exe} /quiet"
			action :nothing
		end.run_action(:run)
		sleep(5)
		execute 'install_msvcr_14632' do 
			command "#{ms2010_exe} /quiet"
			returns [0, 5100]	#5100 is returned when a higher vcredist is installed 
			action :nothing
		end.run_action(:run)
	end
	
end

if(platform_family?('rhel') || platform_family?('debian'))
	cad_path = File.join(existing_cad_bin_dir, "bin","linux64");
	cad_env_path = File.join(node['installation_dir'], "external", "cad_viewer_env.sh")
	template "create_cad_env_variable" do
		path  cad_env_path
		source "cad_env.erb"
		mode "0774"
		sensitive true
		action :nothing
		variables(
			:cad_path => cad_path,
		)
	end.run_action(:create)
	node.set['user_env']['hoops'] = cad_env_path
end

puts "CAD Viewer pre-requisites installed successfully."
Chef::Log.info("CAD Viewer pre-requisites installed successfully.")
puts

