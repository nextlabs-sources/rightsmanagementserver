puts "Installing Document Viewer pre-requisites ..."
Chef::Log.info("Installing Document Viewer pre-requisites ...")

target = File.join(node['installation_dir'],"external","perceptive")
FileUtils.mkdir_p target unless Dir.exists? target

#ADD PERCEPTIVE TO PATH
if platform_family?('windows')
	system_path = ""
	reg_val_arr = registry_get_values("HKEY_LOCAL_MACHINE\\System\\CurrentControlSet\\Control\\Session Manager\\Environment")
	reg_val_arr.each { |val|
		if val[:name] == "Path"
			system_path = val[:data]
			break
		end
	}
	perceptive_path = target.gsub("/","\\") + ";"
	unless system_path.include? perceptive_path
		puts "Adding document converter variables to system path ..."
		Chef::Log.info("Adding document converter variables to system path ...")
		system_path = system_path + ";#{perceptive_path}"
		registry_key "HKEY_LOCAL_MACHINE\\System\\CurrentControlSet\\Control\\Session Manager\\Environment" do
			values [{
				:name => "Path",
				:type => :expand_string,
				:data =>  system_path
			}]
			action :nothing
		end.run_action(:create)
		puts "Document converter variables added to system path successfully."
		Chef::Log.info("Document converter variables added to system path successfully.")
	end
	
else
	
	perceptive_path = target
	isys_fonts_entry = File.join(target, "fonts")
	perceptive_env_path = File.join(target, "perceptive_env.sh")
	template "create_perceptive_env_variable" do 
		path  perceptive_env_path
		source "perceptive_env.erb"
		mode "0774"
		sensitive true
		action :nothing
		variables(
			:perceptive_path => perceptive_path, 
			:perceptive_font_path => isys_fonts_entry,
		)
	end.run_action(:create)
	node.set['user_env']['perceptive'] = perceptive_env_path
end
puts "Document viewer pre-requisites installed successfully."
Chef::Log.info("Document viewer pre-requisites installed successfully.")
puts