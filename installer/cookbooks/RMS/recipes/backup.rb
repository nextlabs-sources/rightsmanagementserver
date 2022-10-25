require 'fileutils'

FileUtils.rm_rf node['rms_backup_dir'] if Dir.exist? node['rms_backup_dir']
FileUtils.mkdir_p node['rms_backup_dir']
if Dir.exists?(node['installation_dir']) && Dir.entries("#{node['installation_dir']}") != %w{. ..}
	puts "Backing up Installation Directory #{node['installation_dir']} ..."
	Chef::Log.info("Backing up Installation Directory #{node['installation_dir']} ...")
	FileUtils.cp_r(node['installation_dir'], node['rms_backup_dir'])
end
if Dir.exists?(node['data_dir']) && Dir.entries("#{node['data_dir']}") != %w{. ..}
	if Dir.exists? File.join(node['data_dir'], "temp")
		puts "Deleting temp folder in Data Directory #{node['data_dir']} ..."
		Chef::Log.info("Backing up Data Directory #{node['data_dir']} ...")
		FileUtils.rm_rf(File.join(node['data_dir'], "temp"))
	end
	puts "Backing up Data Directory #{node['data_dir']} ..."
	Chef::Log.info("Backing up Data Directory #{node['data_dir']} ...")
	FileUtils.cp_r(node['data_dir'], node['rms_backup_dir'])
end
if platform_family?('rhel')
	service_file = File.join('/etc/init.d', node["rms_service_name"])	
	if File.exists?(service_file)
		puts "Backing up service #{node["rms_service_name"]}"
		Chef::Log.info("Backing up service #{node["rms_service_name"]}")
		FileUtils.cp_r(service_file, node['rms_backup_dir'])
	end
end
puts