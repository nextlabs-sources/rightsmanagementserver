dest_dir =  File.join(node['installation_dir'],'external')
mysql_dir =  File.join(dest_dir,'mysql')

if node['install']['mysql']
	puts "Installing MySQL ..."
	Chef::Log.info("Installing MySQL ...")
	
	#Delete existing installation
	if Dir.exist? mysql_dir
		puts "Deleting existing MySQL Installation Directory ..."
		Chef::Log.info("Deleting existing MySQL Installation Directory ...")
		
		if platform_family?('windows')
			#STOP WINDOWS SERVICE
			begin
				mysql_service_stopper 'stop_mysql_service' do
				end		
			rescue Mixlib::ShellOut::ShellCommandFailed => error
				Chef::Log.error(error.message)
				raise "Error occurred while stopping Windows Service for MySQL."
			end
			#DELETE WINDOWS SERVICE
			begin
				mysql_service_remover 'remove_mysql_service' do
					mysql_bin	File.join(mysql_dir,"bin")
				end		
			rescue Mixlib::ShellOut::ShellCommandFailed => error
				Chef::Log.error(error.message)
				raise "Error occurred while deleting Windows Service for MySQL."
			end
		else
			begin
				if File.exists?("/etc/init.d/#{node["rms__mysql_service_name_unix"]}")
					execute "stop_service_mysql" do
						command "service #{node["rms__mysql_service_name_unix"]} stop"
						action :nothing
					end.run_action(:run)
					file "/etc/init.d/#{node["rms__mysql_service_name_unix"]}" do
  						action :nothing
					end.run_action(:delete)
				end		
			rescue Mixlib::ShellOut::ShellCommandFailed => error
				Chef::Log.error(error.message)
				raise "Error occurred while deleting Linux service for Tomcat."
			end		
		end
		
		FileUtils.rm_r	mysql_dir
		puts "Existing MySQL Installation Directory deleted successfully."
		Chef::Log.info("Existing MySQL Installation Directory deleted successfully.")
	end

	puts "Extracting MySQL ..."
	Chef::Log.info("Extracting MySQL ...")
	
	if node['platform'] == 'windows'	
		#EXTRACT ZIP
		rms_extractor 'extract_jre_tar_gz' do
			extractor_name	"extract_mysql_zip"
			zip_file 		File.join(node['installer_home'],node['rms']['mysql'])
			dest_folder 	dest_dir
		end
	else	#unix tar does both unzip and untar
		rms_extractor 'extract_jre_targz' do
			extractor_name	"extract_mysql_targz"
			zip_file 		File.join(node['installer_home'],node['rms']['mysql'])
			dest_folder 	dest_dir
		end
	end
	
	#Rename mysql-x.x.x to mysql
	Dir.chdir(dest_dir) do
		FileUtils.mv(File.join(dest_dir, Dir.glob("mysql*")),mysql_dir)
	end
	
	mysql_install_dir = mysql_dir
	mysql_data_dir = File.join(node['data_dir'],"mysql")
	mysql_default_file = File.join(node['data_dir'],"RMSInBuiltDBOptions.ini")
	mysql_init_file = File.join(node['data_dir'],"RMSInBuiltDBInit.ini")
	mysql_port = node['install']['rms'] ? node['rms_db_port'] : (node['install']['kms'] ? node['kms_db_port'] : node["mysql"]["port"])
	mysql_password = node['install']['rms'] ? node['rms_db_password'] : (node['install']['kms'] ? node['kms_db_password'] : node["mysql"]["password"])
	mysql_db_name = node["mysql"]["db_name"]
	mysql_user_name = node["mysql"]["user_name"]
	mysql_db_type = node["mysql"]["db_type"]
	mysql_hostname = "localhost"
	
	if platform_family?('windows')
		mysql_install_dir = mysql_install_dir.gsub('\\', '/')
		mysql_data_dir = mysql_data_dir.gsub('\\', '/')
	end
	
	template "create_mysql_default_file" do 
	path  mysql_default_file
	source "mysql_default_file.erb"
	mode "0664"
	action :nothing
	sensitive true
	variables(
		:mysql_port => mysql_port,
		:mysql_install_dir => mysql_install_dir,
		:mysql_data_dir => mysql_data_dir
	)
	end.run_action(:create)	
	
	template "create_mysql_init_file" do 
	path  mysql_init_file
	source "mysql_init_file.erb"
	mode "0664"
	action :nothing
	sensitive true
	variables(
		:mysql_user_name => mysql_user_name,
		:mysql_db_name => mysql_db_name,
		:mysql_pwd => mysql_password
	)
	end.run_action(:create)	
	
	mysqld = File.join(mysql_install_dir, "bin", "mysqld")
	if platform_family?('windows')
		mysqld = mysqld.gsub("/","\\")
		mysql_default_file = mysql_default_file.gsub("/","\\")
		mysql_init_file = mysql_init_file.gsub("/","\\")
	end	
	
	if(!Dir.exists?(mysql_data_dir) || Dir.entries("#{mysql_data_dir}") == %w{. ..})
		FileUtils.mkdir_p mysql_data_dir unless Dir.exists? mysql_data_dir
		puts "Bootstrapping MySQL ..."
		Chef::Log.info("Bootstrapping MySQL ...")
		begin
			execute "bootstrap_mysql" do 
				command "\"#{mysqld}\" --defaults-file=\"#{mysql_default_file}\" --initialize --init-file=\"#{mysql_init_file}\""
				action :nothing
				sensitive true
			end.run_action(:run)
		rescue Mixlib::ShellOut::ShellCommandFailed => error
			Chef::Log.error(error.message)
			raise "Error occurred while bootstrapping MySQL."
		end
	end

	if platform_family?('windows')
		puts "Creating MySQL Windows Service..."
		Chef::Log.info("Creating MySQL Windows Service...")
		begin
			execute "create_mysql_windows_service" do 
				command "\"#{mysqld}\" --install \"#{node["mysql"]["service_name"]}\" --defaults-file=\"#{mysql_default_file}\""
				action :nothing
				sensitive true
			end.run_action(:run)
		rescue Mixlib::ShellOut::ShellCommandFailed => error
			Chef::Log.error(error.message)
			raise "Error occurred while creating MySQL Windows Service."
		end
	else
		puts "Creating MySQL Unix Service..."
		Chef::Log.info("Creating MySQL Unix Service...")
		begin
			rms_service_path = File.join("/etc/init.d", node["rms__mysql_service_name_unix"])
			template "create_mysql_service" do 
				path  rms_service_path
				source "mysql_init_d.erb"
				mode "0774"
				sensitive true
				action :nothing
				variables(
					:mysql_install_dir => mysql_install_dir,
					:mysql_data_dir => mysql_data_dir,
					:mysql_default_file => mysql_default_file
				)
			end.run_action(:create)
			
			# Create soft link in /usr/bin/
			rms_soft_link = "/usr/bin/#{node["rms__mysql_service_name_unix"]}"
			FileUtils.rm_rf rms_soft_link if File.exist? rms_soft_link
			
			execute "ln -s #{rms_service_path} #{rms_soft_link}" do
				action :nothing
			end.run_action(:run)
			
		rescue Mixlib::ShellOut::ShellCommandFailed => error
			Chef::Log.error(error.message)
			raise "Error occurred while creating Unix Service for MySQL."
		end
	end
	
	FileUtils.rm_rf mysql_init_file
	
	puts "MySQL installed successfully."
	Chef::Log.info("MySQL installed successfully.")
	puts
end

node.set['rms_mysql_home'] = mysql_dir