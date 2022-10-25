begin
	preInstallSuccess = false
	include_recipe 'RMS::pre_init'
	include_recipe 'RMS::validation'
	include_recipe 'RMS::summary' if (ENV['RMS_GUI'].nil?) || (ENV['RMS_GUI']!="gui")
	include_recipe 'RMS::pre_install'
	include_recipe 'RMS::backup'
	preInstallSuccess = true
	
	include_recipe 'RMS::install_jre'
	include_recipe 'RMS::install_tomcat'
	include_recipe 'RMS::install_in_built_db'

	if node['install']['rms']
		include_recipe 'RMS::install_rms'
		include_recipe 'RMS::install_doc_viewer_prerequisites'
		include_recipe 'RMS::install_cad_viewer_prerequisites'
		include_recipe 'RMS::deploy_license'
		include_recipe 'RMS::install_embedded_jpc'
	end
	if node['install']['kms']
		include_recipe 'RMS::install_kms'
	end
	include_recipe 'RMS::deploy_certs'
	include_recipe 'RMS::post_install'
	include_recipe 'RMS::install_tools'
	include_recipe 'RMS::package_uninstaller'
	include_recipe 'RMS::start_rms'
rescue => error
	puts "Error: #{error.message}"
	Chef::Log.fatal(error.message)
	begin
		include_recipe 'RMS::rollback' if preInstallSuccess == true
	rescue => error
		puts error.message
	end
	puts "Installation failed."
	return
end
	
puts "Installation completed successfully."
Chef::Log.info("Installation completed successfully.")
rms_installed = Utility::Config.is_RMS_installed?
kms_installed = Utility::Config.is_KMS_installed?
hostname = (node['fqdn'].nil? or node['fqdn'].empty?) ? node['hostname'] : node['fqdn']
if rms_installed && kms_installed
	puts "Rights Management Server and Key Management Server have been successfully installed."
	puts "You can now access Rights Management Server at the following URL."
	puts "https://"+"#{hostname.downcase}:#{node['rms_ssl_port']}/RMS"
elsif rms_installed
	puts "Rights Management Server has been successfully installed."
	puts "You can now access Rights Management Server at the following URL."
	puts "https://"+"#{hostname.downcase}:#{node['rms_ssl_port']}/RMS"
elsif kms_installed
	puts "Key Management Server has been successfully installed."
end
	