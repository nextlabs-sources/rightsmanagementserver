puts "Saving version info ..."
Chef::Log.info("Saving version info ...")

# Copy version_info.json
source = File.join(node['installer_home'],'bin',node['rms_version_info_json'])
target = File.join(node['installation_dir'],node['rms_version_info_json'])
FileUtils.cp_r(source, target)

# Copy policy model
puts "Copying sample policy model ..."
Chef::Log.info("Copying sample policy model ...")
source = File.join(node['installer_home'], node['rms']['policy_model'])
target = File.join(node['installation_dir'])
FileUtils.cp_r(source, target)

rms_reg = node['install']['rms']  ? node['rms_ver'] : (node['rms_installed?'] ? node['initial_rms_ver'] : "")

if !node['install']['rms'] || !node['install']['kms']
	#UPDATE VERSION INFO
	existing_ver_info = File.join(node['installation_dir'],".version_info.json")
	file = File.read(existing_ver_info)
	version = JSON.parse(file)
	version['rms'] = rms_reg
	File.open(existing_ver_info, 'w') do |file|
		file.write(JSON.pretty_generate(version))
	end
end

#Copy Docs
source = File.join(node['installer_home'],node['rms']['docs'])
target = node['installation_dir']
FileUtils.cp_r(source, target)
Dir.chdir node['installation_dir'] do
	FileUtils.rm_rf "Documentation" if Dir.exists? "Documentation"
	FileUtils.mv "docs", "Documentation"
end

puts "Creating environment variables ..."
Chef::Log.info("Creating environment variables ...")

template "create_env_variable" do 
	path  File.join(node['rms_catalina_home'], "bin", if platform_family? ('windows') then "setenv.bat" else "setenv.sh" end)
	source "setenv.erb"
	mode "0774"
	sensitive true
	action :nothing
	variables(
		:jre_home => (platform_family? ('windows')) ? node['rms_jre_home'].gsub('/', '\\') : node['rms_jre_home'],
		:tomcat_path => (platform_family? ('windows')) ? node['rms_catalina_home'].gsub('/', '\\') : node['rms_catalina_home'],
		:data_dir_path => (platform_family? ('windows')) ? node['data_dir'].gsub('/', '\\') : node['data_dir'],
		:install_dir_path => (platform_family? ('windows')) ? node['installation_dir'].gsub('/', '\\') : node['installation_dir'],
		:user_attribute => node['user_env'],
		:embedded_pdp_path => (platform_family? ('windows')) ? node['embedded_pdp_jar'].gsub('/', '\\') : node['embedded_pdp_jar'],
		:kms_data_dir_path => (platform_family? ('windows')) ? node['kms_data_dir'].gsub('/', '\\') : node['kms_data_dir'],
		:install_kms => node['install']['kms'],
		:kms_installed =>  node['kms_installed?']
	)
end.run_action(:create)

puts "Creating catalina.properties ..."
Chef::Log.info("Creating catalina.properties ...")
embedded_pdp_jar_path = node['embedded_pdp_jar']
if platform_family?('windows')
	shared_loader = "\"#{embedded_pdp_jar_path.gsub('\\', '/')}\""
else
	shared_loader = "\"#{embedded_pdp_jar_path}\""
end

template "create_catalina_properties" do 
	path  File.join(node['rms_catalina_home'], "conf", "catalina.properties")
	source "catalina.properties.erb"
	mode "0664"
	sensitive true
	action :nothing
	variables(
		:shared_loader => shared_loader
	)
end.run_action(:create)

puts "Creating logging.properties ..."
Chef::Log.info("Creating logging.properties ...")
template "create_tomcat_logging_properties" do 
	path  File.join(node['rms_catalina_home'], "conf", "logging.properties")
	source "tomcat_logging.properties.erb"
	mode "0664"
	sensitive true
	action :nothing
	variables(
		:embedded_jpc_log_pattern => File.join(node['embedded_jpc_dir'], "logs", "DCC.%g.log").gsub('\\', '/')
	)
end.run_action(:create)

# Creating DB Settings
db_settings = {}
if !node['icenet_server'].nil? && !node['icenet_server'].empty?
	db_settings['ICENET_URL'] = "#{node["icenet_server"].chomp("/")}"
end

install_db_settings_filename="install_db_settings.txt"
puts "Creating #{install_db_settings_filename} ..."
Chef::Log.info("Creating #{install_db_settings_filename} ...")
db_settings_content = ""
db_settings.each do |key, value|
	db_settings_content += "#{key}=#{value}" + (if platform_family?('windows') then "\r\n" else "\n" end)
end
db_settings_path = File.join(node['data_dir'], install_db_settings_filename)
File.write(db_settings_path, db_settings_content)

kms_data_dir = node['install']['kms']  ? node['kms_data_dir'] : (node['kms_installed?'] ? node['kms_data_dir'] : "")
if platform_family?('windows')
	puts "Updating Windows Registry ..."
	Chef::Log.info("Updating Windows Registry ...")
	registry_key "HKEY_LOCAL_MACHINE\\#{Utility::Config::REGISTRY_KEY_NAME}" do
		values [{:name => 'RMSVersion', :type => :string, :data => rms_reg},
			  {:name => 'InstallDir', :type => :string, :data => node['installation_dir']},
			  {:name => 'DataDir', :type => :string, :data => node['data_dir']}
		]
		recursive true
		action :nothing
	end.run_action (:create)

	begin
		puts "Updating Windows Service parameters ..."
		Chef::Log.info("Updating Windows Service parameters ...")
		rms_service_update 'rms_service_update' do
			tomcat_bin	File.join(node['rms_catalina_home'], "bin")
		end		
	rescue Mixlib::ShellOut::ShellCommandFailed => error
		Chef::Log.error(error.message)
		raise "Error occurred while updating Windows Service parameters."
	end
end

#Obfuscate password fields in setup_ui.json
setup_ui_json_file = File.join(node['installer_home'], "setup_ui.json")
setup_ui_json_hash = {}
if File.exists? setup_ui_json_file
	file = File.read(setup_ui_json_file)
	setup_ui_json_hash = JSON.parse(file)
	setup_ui_json_hash['rms_db_password'] = '●' * setup_ui_json_hash['rms_db_password'].length if setup_ui_json_hash['rms_db_password']!=""
	setup_ui_json_hash['kms_db_password'] = '●' * setup_ui_json_hash['kms_db_password'].length if setup_ui_json_hash['kms_db_password']!=""
	setup_ui_json_hash['km_keystore_password'] = '●' * setup_ui_json_hash['km_keystore_password'].length if setup_ui_json_hash['km_keystore_password']!=""
	File.open(setup_ui_json_file, 'w') do |file|
		file.write(JSON.pretty_generate(setup_ui_json_hash))
	end
end

if Dir.exists? node['rms_backup_dir']
	puts "Deleting Backup Directory ..."
	Chef::Log.info("Deleting Backup Directory ...")
	FileUtils.rm_rf node['rms_backup_dir']
end
if Dir.exists? node['rms_tmp_dir']
	puts "Deleting temporary files ..."
	Chef::Log.info("Deleting temporary files ...")
	FileUtils.rm_rf node['rms_tmp_dir']
end

puts "Finished configuring the Rights Management Server."
Chef::Log.info("Finished configuring the Rights Management Server.")
puts