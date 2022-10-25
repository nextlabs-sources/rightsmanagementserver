#DEPLOY RMS WAR IN WEBAPPS
puts "Installing Rights Management Server ..."
Chef::Log.info("Installing Rights Management Server ...")

#COPY TO INSTALLATION DIR
source = File.join(node['installer_home'],node['rms']['war'])
target = node['installation_dir']
FileUtils.cp_r(source, target)

#DELETE EXISTING RMS FOLDER
rms_folder = File.join(node['rms_catalina_home'],'webapps','RMS')
FileUtils.rm_rf rms_folder if Dir.exists? rms_folder

#COPY TO WEBAPPS
target = File.join(node['rms_catalina_home'],'webapps')
FileUtils.cp_r(source, target)

# SET RMS_HOME
node.set['rms_home'] = File.join(target, "RMS")

#EDIT LOG/CONFIG PROPERITES
conf_dist_path=File.join(node['installation_dir'],"conf")
FileUtils.mkdir_p conf_dist_path unless Dir.exist? conf_dist_path

rms_log_filename="RMSLog.properties"
log_path = File.join(node['data_dir'], "logs", "RMS.log")
#property files cannot read windows path separtor (either use unix or use "\\\\")
log_path = log_path.gsub("\\","/") if platform_family?('windows')
template "create_rms_log" do 
	path  File.join(conf_dist_path, rms_log_filename)
	source "rms_log.erb"
	mode "0664"
	action :nothing
	sensitive true
	variables(
		:log_path => log_path 
	)
end.run_action(:create)	

rms_config_filename="RMSConfig.properties"
puts "Configuring RMI Key Management port " + node['rmi_km_port']
template "create_rms_config" do 
	path  File.join(conf_dist_path, rms_config_filename)
	source "rms_config.erb"
	mode "0664"
	action :nothing
	sensitive true
	variables(
		:ldap_type =>node['ldap_type'],
		:host_name => node['ldap_host_name'],
		:domain => node['ldap_domain'],
		:search_base => node['ldap_search_base'],
		:user_group => node['ldap_user_group'],
		:admin => node['ldap_admin'],
		:rmi_km_port => node['rmi_km_port'],
		:kms_url => node['km_server']
	)
end.run_action(:create)	

lib_dir = File.join(node['installer_home'], node["rms"]["lib"])
java_bin_dir = File.join(node['rms_jre_home'], "bin")

puts "Configuring RMS Database Settings ..."
Chef::Log.info("Configuring RMS Database Settings ...")

db_config_filename="DBConfig.properties"
# create new DBConfig if DB_connection string is it has not been configured.
if !Utility::Config.is_RMS_DB_configured?
	#CREATE DB_CONFIG in InstallDir Conf
	conf_dist_path=File.join(node['installation_dir'],"conf")
	FileUtils.mkdir_p conf_dist_path unless Dir.exist? conf_dist_path
	
	db_settings = Utility::Config.buildDBProperties(node['rms_db_type'], node['rms_db_host_name'], node['rms_db_port'], node['rms_db_name'], node['rms_db_username'])
	db_settings['db_conn_url'] = (node['rms_db_conn_url'] == "") ? db_settings['db_conn_url'] : node['rms_db_conn_url']
	db_config_creator 'create_rms_db_config' do
		conf_dist_path 		conf_dist_path
		db_config_filename 	db_config_filename
		db_settings			db_settings
		encrypted_password	Utility::Config.encryption_util(java_bin_dir, lib_dir, node['rms_db_password'])
	end
	#Copy to datafiles
	FileUtils.cp_r(File.join(conf_dist_path, db_config_filename), node['data_dir'])
end

#DEPLOY THEM TO DATAFILES IF THEY DONT EXIST
target = File.join(node['data_dir'], rms_log_filename)
unless File.exist? target
	puts "Copying #{rms_log_filename} to Data Directory ..."
	Chef::Log.info("Copying #{rms_log_filename} to Data Directory ...")
	FileUtils.cp_r(File.join(conf_dist_path,rms_log_filename), target)
end

target = File.join(node['data_dir'], rms_config_filename)
unless File.exist? target
	puts "Copying #{rms_config_filename} to Data Directory ..."
	Chef::Log.info("Copying #{rms_config_filename} to Data Directory ...")
	FileUtils.cp_r(File.join(conf_dist_path,rms_config_filename), target)
else
	puts "Updating #{rms_config_filename} in Data Directory ..."
	Chef::Log.info("Updating #{rms_config_filename} in Data Directory ...")
	rms_config_km_port_key = "EMBEDDEDJPC_RMI_PORT_NUMBER"
	rms_config_kms_url = "KMS_URL"
	rms_config_ad_ldap_type="LDAP.1.LDAP_TYPE"
	rms_config_ad_hostname_key = "LDAP.1.HOST_NAME"
	rms_config_ad_domain_key = "LDAP.1.DOMAIN"
	rms_config_ad_search_base_key = "LDAP.1.SEARCH_BASE"
	rms_config_ad_user_group_key = "LDAP.1.RMS_USERGROUP"
	rms_config_ad_admin_key = "LDAP.1.RMS_ADMIN"
	
	new_configs = []
#	new_configs.push("#{rms_config_kms_url}=#{node['km_server']}")
	
	# For upgrade scenario, read from Config file
	File.open(target, "r").each_line do |line|	
		if line.include? rms_config_km_port_key
			node.set['rmi_km_port'] = line[line.index("=")+1, line.length].strip
			node.set['rmi_km_port'] = node["tomcat"]["rmi_km_port"] if node['rmi_km_port'] == ""
		elsif /LDAP\.\d+\.HOST_NAME/i.match line
			new_configs.push(line.gsub(/HOST_NAME=.*/i, "LDAP_TYPE=#{node['ldap_type']}"))
		else 
			indices = new_configs.size.times.select { |i| new_configs[i].split("=").first.casecmp(line.split("=").first) == 0 }
			indices.each do |index|
				new_configs.delete_at index
			end
		end	
	end
	
	lines = IO.readlines(target).map do |line|
		if line.strip[0] != "#" && line.strip[0] != "!"
			if line.include? rms_config_km_port_key
				"#{rms_config_km_port_key}=#{node['rmi_km_port']}"
			elsif line.include? rms_config_ad_ldap_type
				"#{rms_config_ad_ldap_type}=#{node['ldap_type']}"
			elsif line.include? rms_config_ad_hostname_key
				"#{rms_config_ad_hostname_key}=#{node['ldap_host_name']}"
			elsif line.include? rms_config_ad_domain_key
				"#{rms_config_ad_domain_key}=#{node['ldap_domain']}"
			elsif line.include? rms_config_ad_search_base_key
				"#{rms_config_ad_search_base_key}=#{node['ldap_search_base']}"
			elsif line.include? rms_config_ad_user_group_key
				"#{rms_config_ad_user_group_key}=#{node['ldap_user_group']}"
			elsif line.include? rms_config_ad_admin_key
				"#{rms_config_ad_admin_key}=#{node['ldap_admin']}"
			else
				line
			end
		else
			line
		end
	end
	
	ruby_block 'add_new_rms_configs' do
		block do
			fe = Chef::Util::FileEdit.new(target)
			new_configs.each { |line|
				if(/\ALDAP\.\d+\.LDAP_TYPE/i.match(line))
					seq = line[/\d+/]
					fe.insert_line_after_match(/\ALDAP\.#{seq}\.HOST_NAME/i, line)
				elsif(/.+LDAP\.\d+\.LDAP_TYPE/i.match(line))
					seq = line[/\d+/]
					fe.insert_line_after_match(/.+LDAP\.#{seq}\.HOST_NAME/i, line)
				else
					fe.insert_line_if_no_match(/#{line}/i, line)
				end
			}
			fe.write_file
		end
		action :nothing
	end.run_action(:run)
	
end

#CREATE VIEWER FOLDER
viewers_dir = File.join(node['installation_dir'],"viewers")
FileUtils.mkdir_p viewers_dir unless Dir.exists? viewers_dir

#COPY INFINISPAN XML FILE
puts "Copying infinispan.xml file ..."
Chef::Log.info("Copying infinispan.xml  file ...")
source = File.join(node['installer_home'],node['rms']['conf'], "infinispan.xml")
target = File.join(node['installation_dir'], "conf")
FileUtils.cp_r(source, target)
FileUtils.cp_r(source, node['data_dir']) if !File.exists?(File.join(node['data_dir'],"infinispan.xml"))

#COPY INFINISPAN CLUSTERED XML FILE
puts "Copying infinispan_clustered.xml file ..."
Chef::Log.info("Copying infinispan_clustered.xml  file ...")
source = File.join(node['installer_home'],node['rms']['conf'], "infinispan_clustered.xml")
target = File.join(node['installation_dir'], "conf")
FileUtils.cp_r(source, target)
#user can change infinispan_clustered.xml, so we do not overwrite the one in datafiles
FileUtils.cp_r(source, node['data_dir']) if !File.exists?(File.join(node['data_dir'],"infinispan_clustered.xml"))

#COPY JGROUPS XML FILE
puts "Copying jgroups.xml file ..."
Chef::Log.info("Copying jgroups.xml  file ...")
source = File.join(node['installer_home'],node['rms']['conf'], "jgroups.xml")
target = File.join(node['installation_dir'], "conf")
FileUtils.cp_r(source, target)
#user can change jgroups.xml, so we do not overwrite the one in datafiles
FileUtils.cp_r(source, node['data_dir']) if !File.exists?(File.join(node['data_dir'],"jgroups.xml"))

#Copying RMC_Classification.xml
puts "Copying RMC classification file ..."
Chef::Log.info("Copying RMC classification file ...")
source = File.join(node['installer_home'],node['rms']['conf'], "RMC_Classification.xml")
target = File.join(node['installation_dir'], "conf")
FileUtils.cp_r(source, target)
FileUtils.cp_r(source, node['data_dir'])

#COPY SHAREPOINT APPS TO INSTALL DIR
puts "Copying SharePoint Apps ..."
Chef::Log.info("Copying SharePoint Apps ...")
dest = File.join(node['installation_dir'], "sharepoint")
FileUtils.rm_rf dest if File.exists? dest	#fix for beta, can be removed in future releases
FileUtils.rm_rf dest if Dir.exists? dest
FileUtils.mkdir_p dest
FileUtils.cp_r(File.join(node['installer_home'],node['rms']['sp_online']),dest)
FileUtils.cp_r(File.join(node['installer_home'],node['rms']['sp_onpremise']),dest)
FileUtils.cp_r(File.join(node['installer_home'],node['rms']['sp_online_repo']),dest)

#EXTRACT APPS TO DATAFILES
dest = File.join(node['data_dir'],"sharepoint")
FileUtils.rm_rf dest if File.exists? dest	#fix for beta, can be removed in future releases
FileUtils.rm_rf dest if Dir.exists? dest
FileUtils.mkdir_p dest

rms_extractor 'extract_sp_online' do
	extractor_name	'extract_sp_online'
	zip_file 		File.join(node['installer_home'],node['rms']['sp_online'])
	dest_folder 	dest
end
rms_extractor 'extract_sp_onpremise' do
	extractor_name	'extract_sp_onpremise'
	zip_file 		File.join(node['installer_home'],node['rms']['sp_onpremise'])
	dest_folder 	dest
end
FileUtils.cp_r(File.join(node['installer_home'],node['rms']['sp_online_repo']),dest)
Dir.chdir dest do 
	FileUtils.rm_rf "AppWeb.deploy" if Dir.exists? "AppWeb.deploy"
	FileUtils.rm_rf "scripts" if Dir.exists? "scripts"
	#rename
	FileUtils.mv("SecureCollaboration_SPOnlineApp.app", "SecureCollaboration_SPOnlineApp.zip")
	FileUtils.mv("SecureCollaboration_SPOnPremiseApp.app", "SecureCollaboration_SPOnPremiseApp.zip")
	FileUtils.mv("SecureCollaboration_SPOnlineRepositoryApp.app", "SecureCollaboration_SPOnlineRepositoryApp.zip")
end

#COPY LOCATION DATABASE
puts "Copying Location Database to Data Directory ..."
Chef::Log.info("[Install]: Copying Location Database to Data Directory ...")
FileUtils.cp_r(File.join(node['installer_home'],node["rms"]["ipToCountry"]),File.join(node['installation_dir'],"external"))
FileUtils.cp_r(File.join(node['installation_dir'],"external","IpToCountry.csv"),node['data_dir'])

puts "Rights Management Server installed successfully."
Chef::Log.info("Rights Management Server installed successfully.")
puts