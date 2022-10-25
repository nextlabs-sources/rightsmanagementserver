# All errors in this recipe have to be rescued.

require 'fileutils'
puts

installDir = File.basename(node['installation_dir'])
dataDir = File.basename(node['data_dir'])

if (!Dir.exist?(File.join(node['rms_backup_dir'],installDir)) && !Dir.exist?(File.join(node['rms_backup_dir'],dataDir)))
	Chef::Log.fatal("No files to rollback.")
	return
end

puts "Rolling back Rights Management Server ..."
Chef::Log.info("Rolling back Rights Management Server ...")

if Dir.exist?(File.join(node['rms_backup_dir'],dataDir)) && Dir.entries("#{File.join(node['rms_backup_dir'],dataDir)}") != %w{. ..} 
	puts "Rolling back Data Directory ..."
	Chef::Log.info("Rolling back Data Directory  ...")
	FileUtils.rm_rf node['data_dir'] if Dir.exists? node['data_dir']
	FileUtils.cp_r(File.join(node['rms_backup_dir'],dataDir),File.expand_path("..",node['data_dir']))
end

if Dir.exist?(File.join(node['rms_backup_dir'],installDir)) && Dir.entries("#{File.join(node['rms_backup_dir'],installDir)}") != %w{. ..} 
	puts "Rolling back Installation Directory ..."
	Chef::Log.info("Rolling back Installation Directory ...")
	FileUtils.rm_rf node['installation_dir'] if Dir.exists? node['installation_dir']
	FileUtils.cp_r(File.join(node['rms_backup_dir'],installDir),File.expand_path("..",node['installation_dir']))
end

if node['installation_mode'] == "upgrade"
	if node['platform'] == 'windows'
		tomcat_bin_dir = File.join(node['installation_dir'],"external", node["tomcat"]["name"],"bin")
		begin
			#DELETE WINDOWS SERVICE
			rms_service_remover 'remove_rms_service' do
				tomcat_bin 		tomcat_bin_dir
			end		

			#CREATE NEW SERVICE
			rms_service_creator 'create_rms_service' do
				tomcat_bin 		tomcat_bin_dir
			end
			
			begin
				puts "Updating Windows Service parameters ..."
				Chef::Log.info("Updating Windows Service parameters ...")
				rms_service_update 'rms_service_update' do
					tomcat_bin	tomcat_bin_dir
				end		
			rescue Mixlib::ShellOut::ShellCommandFailed => error
				Chef::Log.error(error.message)
				raise "Error occurred while updating Windows Service parameters."
			end
			
			puts "Starting Rights Management Server ..."
			Chef::Log.info("Starting Rights Management Server ...")
			service 'start_rms' do
				service_name node['rms_service_name']
				action :nothing
			end.run_action(:start)
			puts "Rights Management Server successfully started."
			Chef::Log.info("Rights Management Server successfully started.")
		rescue Mixlib::ShellOut::ShellCommandFailed => error
			Chef::Log.error(error.message)
			raise "Error occurred while rolling back Windows Service for Tomcat."
		end
	else
		serviceFile = File.join(node['rms_backup_dir'], node["rms_service_name"])
		if File.exists?(serviceFile)
			begin
				FileUtils.cp_r(serviceFile, '/etc/init.d/')
		
				#START SERVICE
				execute "run_service_tomcat" do
					command "/sbin/service " + node["rms_service_name"] + " start"
					action :nothing
				end.run_action(:run)

				execute "chkconfig_#{node["rms_service_name"]}_on" do
					command "chkconfig " + node["rms_service_name"] + " on"
					action :nothing
				end.run_action(:run)
			rescue Mixlib::ShellOut::ShellCommandFailed => error
				Chef::Log.error(error.message)
				raise "Error occurred while rolling back Linux Service for Tomcat."
			end
		end
	end
end
if Dir.exist? node['rms_backup_dir']
	puts "Deleting Backup Directory ..."
	Chef::Log.info("Deleting Backup Directory ...")
	FileUtils.rm_rf node['rms_backup_dir']
end

puts "Finished rolling back Rights Management Server."