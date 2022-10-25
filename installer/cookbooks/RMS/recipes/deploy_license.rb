#Copy License Folder to Datafiles
puts "Copying license jar ..."
Chef::Log.info("Copying license jar ...")
FileUtils.cp_r(File.join(node['installer_home'], node['rms']['license_dir']), node['data_dir'])

dest_dir = File.join(node['data_dir'], "license")
if (node['license_file_location']!="" && File.exist?(node['license_file_location']) && File.basename(node['license_file_location']) == node['rms']['license_file'] && File.dirname(node["license_file_location"]) != dest_dir)
	puts "Copying license file ..."
	Chef::Log.info("Copying license file ...")	
	FileUtils.cp node["license_file_location"], dest_dir
end
puts