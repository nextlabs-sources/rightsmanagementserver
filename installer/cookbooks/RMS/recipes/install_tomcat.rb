dest_dir =  File.join(node['installation_dir'],'external')
tomcat_dir = File.join(dest_dir,node["tomcat"]["name"])

if node['install']['tomcat']
	puts "Installing Tomcat ..."
	Chef::Log.info("Installing Tomcat ...")
	if Dir.exist? tomcat_dir
		if node['platform'] == 'windows' && node['installation_mode'] == 'upgrade'
			#DELETE WINDOWS SERVICE
			begin
				rms_service_remover 'remove_rms_service' do
					tomcat_bin 		 File.join(tomcat_dir,"bin")
				end		
			rescue Mixlib::ShellOut::ShellCommandFailed => error
				Chef::Log.error(error.message)
				raise "Error occurred while deleting Windows Service for Tomcat."
			end
		else
			begin
				if File.exists?("/etc/init.d/#{node["rms_service_name"]}")
					file "/etc/init.d/#{node["rms_service_name"]}" do
  						action :nothing
					end.run_action(:delete)
				end		
			rescue Mixlib::ShellOut::ShellCommandFailed => error
				Chef::Log.error(error.message)
				raise "Error occurred while deleting Linux service for Tomcat."
			end
		end
		
		#DELETE PREVIOUS INSTALLATION
		puts "Deleting previous Tomcat ..."
		Chef::Log.info("Deleting previous Tomcat ...")
		FileUtils.rm_rf	tomcat_dir
		puts "Previous Tomcat deleted successfully."
		Chef::Log.info("Previous Tomcat deleted successfully.")
	end

	#EXTRACT TOMCAT
	rms_extractor 'extract_tomcat_zip' do
		extractor_name	"extract_tomcat_zip"
		zip_file 		File.join(node['installer_home'],node["rms"]["tomcat"])
		dest_folder 	dest_dir
	end
	
	#RENAME TOMCAT
	Dir.chdir(dest_dir) do
		FileUtils.mv(File.join(dest_dir, Dir.glob("apache*")),tomcat_dir)
	end
	node.set['rms_catalina_home'] = tomcat_dir
	
	Dir.chdir(File.join(tomcat_dir, "webapps")) do
		FileUtils.rm_rf %w(docs examples host-manager manager)
	end
	
	root_path = File.join(tomcat_dir, "webapps", "ROOT")
	if Dir.exist? (root_path)
		options={
			remove_destination:true
		}
		FileUtils.cp_r(File.join(node['installer_home'],"dist", "rms", "root", "."), root_path, options)
	end
  
	# Creating SSL Cert
	puts "Creating SSL certificate for Tomcat ..."
	Chef::Log.info("Creating SSL certificate for Tomcat ...")

	keyStorePath = File.join(node['data_dir'],"cert")
	keyStoreFile = File.join(keyStorePath, node["tomcat"]["keystore_file"])
	keytool_bin = File.join(node['rms_jre_home'],"bin","keytool")

	FileUtils.mkdir_p File.join(keyStorePath) unless Dir.exist? File.join(keyStorePath)
	if platform_family?('windows')
		keyStoreFile = keyStoreFile.gsub('/', '\\')
		keytool_bin = keytool_bin.gsub('/', '\\')
	end
		
	if File.file? keyStoreFile
		puts "Not creating a new certificate. Using the existing certificate at " + keyStoreFile
		Chef::Log.warn("Not creating a new certificate. Using the existing certificate at " + keyStoreFile)
	else
		hostname = if node['fqdn'].nil? or node['fqdn'].empty? then node['hostname'].downcase else node['fqdn'].downcase end
		keytool_command = "\"" + keytool_bin + "\" -genkey -noprompt -alias rms -keyalg RSA -dname \"CN=#{hostname}, OU=RMS, O=NextLabs, C=US\" -keystore \"" + keyStoreFile + "\" -storepass \"changeit\" -keypass \"" + node["tomcat"]["keystore_password"] + "\" -validity 365"
		begin
			execute "create_ssl_cert" do 
				command keytool_command
				action :nothing
				sensitive true
			end.run_action(:run)
		rescue Mixlib::ShellOut::ShellCommandFailed => error
			Chef::Log.error(error.message)
			raise "Error occurred while generating self-signed certificate for Tomcat."
		end
	end

	#Configuring SERVER.XML
	copy_server_xml_success = true
	if node['installation_mode'] == "upgrade"
		server_xml_path = File.join(node['rms_backup_dir'], node["tomcat"]["name"], "conf", "server.xml")
		target_path = File.join(node['rms_catalina_home'], "conf")
		if File.exist?(server_xml_path)
			puts "Copying server.xml from backup folder"
			FileUtils.cp server_xml_path, target_path
			copy_server_xml_success = true
		else
			puts "Unable to find Tomcat server.xml"
			Chef::Log.warn("Unable to find Tomcat server.xml.")
			copy_server_xml_success = false
		end
	end
	
	puts "Configuring Tomcat ..."
	Chef::Log.info("Configuring Tomcat ...")
	if node['installation_mode'] == "install" || copy_server_xml_success == false
		puts "Configuring SSL Enabled connector port " + node['rms_ssl_port']
		template "create_server_xml" do 
			path  File.join(node['rms_catalina_home'],"conf","server.xml")
			source "server_xml.erb"
			mode "0664"
			action :nothing
			sensitive true
			variables(
				:shutdown_port => node["rms_shutdown_port"], 
				:ssl_port => node["rms_ssl_port"], 
				:config_dir => keyStorePath,
				:keystore_file => keyStoreFile,
				:keystore_type => node["tomcat"]["keystore_type"],
				:ssl_max_threads => node["tomcat"]["ssl_max_threads"],
				:max_post_size => node["tomcat"]["max_post_size"]
			)
		end.run_action(:create)	
	end

	#CREATE SERVICE FOR WINDOWS
	if platform_family?('windows')
		puts "Creating Windows Service for Tomcat ..."
		Chef::Log.info("Creating Windows Service for Tomcat ...")
		dir = File.join(node['rms_catalina_home'],"bin")
		Dir.chdir(dir) do
			$tomcat_wexe = Dir.glob("tomcat*[w].exe")[0]
			#RENAME tomcat*w.exe to RMS.exe (GUI for changing service parameters)
			FileUtils.mv(File.join(dir,$tomcat_wexe), File.join(dir,node["rms_service_name"]+".exe"))
		end
		
		#Create NEW service
		begin
			rms_service_creator 'create_rms_service' do
				tomcat_bin 		dir
			end
		rescue Mixlib::ShellOut::ShellCommandFailed => error
			Chef::Log.error(error.message)
			raise "Error occurred while creating Windows Service for Tomcat."
		end
	else
		puts "Creating Unix Service for Tomcat ..."
		begin
			shFiles = File.join(tomcat_dir, "bin")
			Dir.entries(shFiles).each do |f|
				if !File.directory? f
  					File.chmod(0774, File.join(tomcat_dir, "bin", f))  					
  				end
			end
			
			rms_service_path = File.join("/etc/init.d", node["rms_service_name"])
			template "create_tomcat_service" do 
				path  rms_service_path
				source "tomcat_init.erb"
				mode "0774"
				sensitive true
				action :nothing
				variables(
					:name => 'RMS',
					:jre_home => node['rms_jre_home'], 
					:tomcat_path => tomcat_dir, 
					:service_name => node["rms_service_name"]
				)
			end.run_action(:create)
			
			# Create soft link in /usr/bin/
			rms_soft_link = "/usr/bin/#{node['rms_service_name']}"
			FileUtils.rm_rf rms_soft_link if File.exist? rms_soft_link
			
			execute "ln -s #{rms_service_path} #{rms_soft_link}" do
				action :nothing
			end.run_action(:run)
			
		rescue Mixlib::ShellOut::ShellCommandFailed => error
			Chef::Log.error(error.message)
			raise "Error occurred while creating Unix Service for Tomcat."
		end
	end
	puts "Finished configuring Tomcat."
	Chef::Log.info("Finished configuring Tomcat.")
	puts "Tomcat installed successfully."
	Chef::Log.info("Tomcat installed successfully.")
	puts
end

node.set['rms_catalina_home'] = tomcat_dir