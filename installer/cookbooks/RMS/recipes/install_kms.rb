#DEPLOY KMS WAR IN WEBAPPS
puts "Installing Key Management Server ..."
Chef::Log.info("Installing Key Management Server ...")

#COPY TO INSTALLATION DIR
source = File.join(node['installer_home'],node['kms']['war'])
target = node['installation_dir']
FileUtils.cp_r(source, target)

#DELETE EXISTING KMS FOLDER
kms_folder = File.join(node['rms_catalina_home'],'webapps','KMS')
FileUtils.rm_rf kms_folder if Dir.exists? kms_folder

#COPY TO WEBAPPS
target = File.join(node['rms_catalina_home'],'webapps')
FileUtils.cp_r(source, target)

#CREATE KMS DATA DIR IF IT DOESN'T EXISTING
FileUtils.mkdir_p node['kms_data_dir'] unless Dir.exists? node['kms_data_dir']

lib_dir = File.join(node['installer_home'], node["rms"]["lib"])
java_bin_dir = File.join(node['rms_jre_home'], "bin")

db_config_filename="KMSDBConfig.properties"

# create new DBConfig if DB has not been configured
if !Utility::Config.is_KMS_DB_configured?
	#CREATE DB_CONFIG in InstallDir Conf
	conf_dist_path=File.join(node['installation_dir'],"conf")
	FileUtils.mkdir_p conf_dist_path unless Dir.exist? conf_dist_path
	
	db_settings = Utility::Config.buildDBProperties(node['kms_db_type'], node['kms_db_host_name'], node['kms_db_port'], node['kms_db_name'], node['kms_db_username'])
	db_settings['db_conn_url'] = (node['kms_db_conn_url'] == "") ? db_settings['db_conn_url'] : node['kms_db_conn_url']
	db_config_creator 'create_kms_db_config' do
		conf_dist_path 		conf_dist_path
		db_config_filename 	db_config_filename
		db_settings			db_settings
		encrypted_password Utility::Config.encryption_util(java_bin_dir, lib_dir, node['kms_db_password'])
	end
	#Copy to datafiles
	FileUtils.cp_r(File.join(conf_dist_path, db_config_filename), node['kms_data_dir'])
end

unless node['kms_installed?']
	encrypted_jceks_password = Utility::Config.encryption_util(java_bin_dir, lib_dir, node['km_keystore_password'])
	kms_tmp_file = File.join(node['kms_data_dir'],'.kms_tmp_file.properties')
	open(kms_tmp_file, 'w') { |f|
		f << "provider=DEFAULT\n"
		f << "storepass=#{encrypted_jceks_password}\n"
	}
end

puts "Finished installing Key Management Server ..."
Chef::Log.info("Finished installing Key Management Server ...")
puts