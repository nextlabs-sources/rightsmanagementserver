define :rms_extractor, :zip_file=>nil, :dest_folder=>nil do

	if node['platform']=='windows'
		bin_loc = '"' + File.join(node['installer_home'],node["rms"]["7z"]).gsub('/', '\\') + '"'
		zip_file = '"' + params[:zip_file].gsub('/', '\\') + '"'
		dest_folder = '"' + params[:dest_folder].gsub('/', '\\') + '"'
		extract_command = "#{bin_loc} x #{zip_file} -o#{dest_folder} -y > nul"
	else
		zip_file = '"' + params[:zip_file] + '"'
		dest_folder = '"' + params[:dest_folder] + '"'
		
		if (File.extname(params[:zip_file])==".zip")
			extract_command = "unzip -q -o #{zip_file} -d #{dest_folder}"
		else
			extract_command = "tar xf #{zip_file} -C #{dest_folder}"
		end
	end

	FileUtils.mkdir_p params[:dest_folder] unless Dir.exist? params[:dest_folder] 
	
	begin
		execute params[:extractor_name] do 
			command extract_command 
			action :nothing
			sensitive true
		end.run_action(:run)
	rescue Mixlib::ShellOut::ShellCommandFailed => error
		Chef::Log.error(error.message)
		raise "Error occurred while extracting #{zip_file} to #{dest_folder}."
	end

end