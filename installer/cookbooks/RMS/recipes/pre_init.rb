node.set['installer_home'] = ENV['START_DIR']
node.set['rms_tmp_dir'] = File.join(Dir.tmpdir,"RMS");
node.set['rms_backup_dir'] = File.join(node['rms_tmp_dir'],"backup")

node.set['rms_installed?'] = Utility::Config.is_RMS_installed?
node.set['kms_installed?'] = Utility::Config.is_KMS_installed?
node.set['installation_mode'] = (node['rms_installed?'] || node['kms_installed?']) ? "upgrade" : "install"

if platform_family?('windows')
	if node['installation_mode'] == "install"
		node.set['installation_dir'] += node["rms"]["installation_dir"] if node['installation_dir']==""
		node.set['data_dir'] += node["rms"]["data_dir"] if node['data_dir']==""
	else
		install_dir, data_dir = Utility::Config.get_installation_dir_and_data_dir
		node.set['installation_dir'] = install_dir
		node.set['data_dir'] = data_dir
	end
else
	# we should use default installation dir / data dir
	install_dir, data_dir = Utility::Config.get_installation_dir_and_data_dir
	node.set['installation_dir'] = install_dir
	node.set['data_dir'] = data_dir
end
node.set['kms_data_dir'] = File.join(node['data_dir'],'KMS')

node.set['rms_ssl_port'] += node["tomcat"]["ssl_port"] if node['rms_ssl_port']==""
node.set['rms_shutdown_port'] += node["tomcat"]["shutdown_port"] if node['rms_shutdown_port']==""
node.set['rmi_km_port'] += node["tomcat"]["rmi_km_port"]	if node['rmi_km_port']==""
node.set['rms_jre_zip'] = node["rms"]["jre"]

node.set['embedded_jpc_dir'] = File.join(node['data_dir'], node["embedded_jpc"]["name"])
node.set['embedded_jservice_dir'] = File.join(node['embedded_jpc_dir'], "jservice")
node.set['embedded_jservice_jar_path'] = File.join(node['embedded_jservice_dir'], "jar")
node.set['embedded_jservice_config_path'] = File.join(node['embedded_jservice_dir'], "config")
node.set['embedded_pdp_jar'] = File.join(node['embedded_jpc_dir'], "embeddedpdp.jar")

#READING VERSION INFORMATION AND DETERMINING COMPONENTS TO BE INSTALLED

existing_ver_info = File.join(node['installation_dir'],node['rms_version_info_json'])
new_ver_info = File.join(node['installer_home'],'bin',node['rms_version_info_json'])
version = Utility::Config.get_version_comparison(existing_ver_info, new_ver_info)

# Read/Store RMS version
node.set['rms_ver'] = version['new']['rms']
node.set['initial_rms_ver'] = version['old']['rms']
node.set['kms_ver'] = version['new']['kms']
node.set['initial_kms_ver'] = version['old']['kms']
node.set['jre_ver'] = version['new']['jre']
node.set['embedded_jpc_ver'] = version['new']['embedded_jpc']
node.set['tomcat_ver'] = version['new']['tomcat']
node.set['install']['jre'] = version['new']['jre'] > version['old']['jre']
node.set['install']['tomcat'] = version['new']['tomcat'] > version['old']['tomcat']
node.set['install']['embedded_jpc'] = version['new']['embedded_jpc'] > version['old']['embedded_jpc']
node.set['install']['mysql'] = (node['rms_db_type'].casecmp("IN_BUILT").zero? && version['new']['mysql'] > version['old']['mysql'])

#read from JSON and defaults to "YES"
node.set['install']['rms']	= true
node.set['install']['kms']	= false

if node['install']['rms']
	if (node['ldap_type'] == "" && node['ldap_host_name'] == "" && node['ldap_domain'] == "" && node['ldap_search_base'] == "" && node['ldap_admin'] == "" && node['ldap_user_group']  == '')
		rms_config = Utility::Config.read_RMS_Config(node['data_dir'])
		if !rms_config.empty?
			node.set['ldap_type'] = rms_config.fetch('ldap_type', 'AD')	#DEFAULT TO AD IF MISSING
			node.set['ldap_host_name'] = rms_config.fetch('ldap_host_name', '')
			node.set['ldap_domain'] = rms_config.fetch('ldap_domain', '')
			node.set['ldap_search_base'] = rms_config.fetch('ldap_search_base', '')
			node.set['ldap_user_group']  = rms_config.fetch('ldap_user_group', '')
			node.set['ldap_admin'] = rms_config.fetch('ldap_admin', '')
		end	
	end
	
	if node['ldap_type'] == ""
		raise "LDAP Type is not specified."
	end
	if node['ldap_host_name'] == ""
		raise "LDAP Server Hostname is not specified."
	end
	if node['ldap_domain'] == ""
		raise "LDAP Domain Name is not specified."
	end
	if node['ldap_search_base'] == ""
		raise "LDAP Search Base is not specified."
	end
	if node['ldap_admin'] == "" 
		raise "LDAP Admin for RMS is not specified." 
	end
end