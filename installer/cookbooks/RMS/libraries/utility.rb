require 'socket'
require 'timeout'
require 'win32/registry' if RUBY_PLATFORM =~ /mswin|mingw|windows/
require 'json'
require 'uri'

module Utility
	START_DIR = File.expand_path(".")
	module TCP 
		def self.is_port_available(port)
		  Timeout::timeout(2000) do
		    begin
				s = TCPServer::new('127.0.0.1', port)
				return true
			rescue SystemCallError
				return false
		    ensure
				s.close  unless s.nil?
		    end
		  end
		rescue Timeout::Error
		  return false
		end;
		
		def self.is_valid_port(port)
			return ((port =~ /^[1-9]\d{0,4}$/) and port.to_i <= 65535) ? true : false
		end;
	end
	
	module URLValidator
		def self.is_uri_valid?(uri)
			begin
				result = URI.parse(uri)
			rescue
				return false;
			else
				return true if result.kind_of?(URI::HTTP) or result.kind_of?(URI::HTTPS)
			end
			return false;
		end;
	end 
	
	module Config
		REGISTRY_KEY_NAME = %q[SOFTWARE\NextLabs,Inc.\RMS]
		def self.get_reg_entry(key)
			reg_type = Win32::Registry::KEY_READ | 0x100
			begin
			Win32::Registry::HKEY_LOCAL_MACHINE.open(REGISTRY_KEY_NAME, reg_type) do |reg|
				return reg[key]
			end
			rescue
				return nil
			end
		end
		
		def self.get_installation_dir_and_data_dir
			case RUBY_PLATFORM
			when /mingw/ then # we are on windows
				return self.get_reg_entry("InstallDir"), self.get_reg_entry("DataDir")
			when /linux/ then# we are on linux
				config_file_path = File.join(File.dirname(__FILE__), "../default_setup.json")
				external_config_defaults = nil
				external_config_defaults = JSON.parse(File.read(config_file_path))
				return external_config_defaults["install_dir"]["linux"], external_config_defaults["data_dir"]["linux"]
			end
		end
		
		def self.is_RMS_installed?
			self.is_component_installed? "RMS"
		end
		
		def self.is_KMS_installed?
			self.is_component_installed? "KMS"
		end
		
		def self.is_CAD_Viewer_installed?
			install_dir, data_dir = self.get_installation_dir_and_data_dir
			if((!install_dir.nil?) && (!data_dir.nil?) && (Dir.exists? install_dir) && Dir.entries("#{install_dir}") != %w{. ..})
				cad_viewer_dir = File.join(install_dir,"external","RMSCADCONVERTER")
				return Dir.exists? cad_viewer_dir
			end
			return false
		end
		
		def self.is_mysql_installed?
			install_dir, data_dir = self.get_installation_dir_and_data_dir
			if !install_dir.nil?
				mysql_install_dir = File.join(install_dir,"external","mysql")
				return Dir.exists? mysql_install_dir
			end
			return false
		end
		
		def self.is_component_installed?(component)
			install_dir, data_dir = self.get_installation_dir_and_data_dir
			if((!install_dir.nil?) && (!data_dir.nil?) && (Dir.exists? install_dir) && Dir.entries("#{install_dir}") != %w{. ..})
				component_dir = File.join(install_dir,"external","tomcat","webapps",component)
				component_war = File.join(install_dir,"external","tomcat","webapps",component+".war")
				component_installed = Dir.exists?(component_dir) || File.exists?(component_war)
				case RUBY_PLATFORM
				when /mingw/ then # we are on windows
					component_ver =  ""
					if component == "RMS" 
						component_ver = self.get_reg_entry("RMSVersion")
					elsif component == "KMS"
						component_ver = self.get_reg_entry("KMSVersion")
					end
					return (!component_ver.nil? && !component_ver.empty? && component_installed )
				when /linux/ then# we are on linux
					return component_installed
				end
			end
			return false
		end
			
		def self.get_version_comparison(existing_ver_info, new_ver_info)
			version = Hash.new { |hash, key| hash[key] = {} }
			#Reading from Existing Installation			
			if File.file? existing_ver_info
				begin
					file = File.read(existing_ver_info)
					version ['old'] = JSON.parse(file)
				rescue
					puts "Error: Error occurred while reading/parsing JSON file. Using default values."

				end
			end
			version['old']['jre'] = '' if version['old']['jre'].nil?
			version['old']['tomcat'] = '' if version['old']['tomcat'].nil?
			version['old']['rms'] = '' if version['old']['rms'].nil?
			version['old']['kms'] = '' if version['old']['kms'].nil?
			version['old']['embedded_jpc'] = ''	if version['old']['embedded_jpc'].nil?
			version['old']['mysql'] = '' if version['old']['mysql'].nil?

			#Reading from Installer 
			begin
				file = File.read(new_ver_info)
				version['new'] = JSON.parse(file)	
			rescue
				raise "Error: The installer is corrupted. The installation cannot continue."
			end
			return version
		end
		
		def self.read_RMS_Config data_dir
			rms_config = Hash.new{}
			config = File.join(data_dir, "RMSConfig.properties")
			if File.exists? config
				IO.foreach(config) do |line|
					line = line.strip.gsub("\r","").gsub("\n","")
					if(/\ALDAP.1.LDAP_TYPE/i.match(line))
						rms_config['ldap_type'] = line[line.index("=")+1, line.length]
					elsif(/\ALDAP.1.HOST_NAME/i.match(line))
						rms_config['ldap_host_name'] = line[line.index("=")+1, line.length]
					elsif(/\ALDAP.1.DOMAIN/i.match(line))
						rms_config['ldap_domain'] = line[line.index("=")+1, line.length]
					elsif(/\ALDAP.1.SEARCH_BASE/i.match(line))
						rms_config['ldap_search_base'] = line[line.index("=")+1, line.length]
					elsif(/\ALDAP.1.RMS_USERGROUP/i.match(line))
						rms_config['ldap_user_group'] = line[line.index("=")+1, line.length]
					elsif(/\ALDAP.1.RMS_ADMIN/i.match(line))
						rms_config['ldap_admin'] = line[line.index("=")+1, line.length]
					elsif(/\AKMS_URL/i.match(line))
						rms_config['kms_url'] = line[line.index("=")+1, line.length]
					end
				end
			end
			return rms_config
		end

		def self.read_DB_Config db_config_file
			db_config = Hash.new{}
			config = File.join(db_config_file)				
			if File.exists? config					
				IO.foreach(config) do |line|
					line = line.strip.gsub("\r","").gsub("\n","")
					if(/\Ajavax.persistence.jdbc.url/i.match(line))
						db_config['db_conn_url'] = line[line.index("=")+1, line.length]	
						db_config = db_config.merge(self.getDBPropertiesFromDBConnUrl(db_config['db_conn_url']))
					elsif(/\Ajavax.persistence.jdbc.driver/i.match(line))
						db_config['db_jdbc_driver'] = line[line.index("=")+1, line.length]
					elsif(/\Ajavax.persistence.jdbc.user/i.match(line))
						db_config['db_username'] = line[line.index("=")+1, line.length]
					elsif(/\Ajavax.persistence.jdbc.password/i.match(line))
						db_config['db_password'] = line[line.index("=")+1, line.length]
					end
				end
			end
			return db_config
		end
		
		def self.is_RMS_DB_configured?
			install_dir, data_dir = self.get_installation_dir_and_data_dir
			if self.is_RMS_installed?
				config = File.join(data_dir, "DBConfig.properties")
				return File.exists? config
			end
			return false
		end
		
		def self.is_KMS_DB_configured?
			install_dir, data_dir = self.get_installation_dir_and_data_dir
			if self.is_KMS_installed?
				config = File.join(data_dir, "KMS", "KMSDBConfig.properties")
				return File.exists? config
			end
			return false
		end
		
		def self.encryption_util(java_bin_dir, lib_dir, raw_text)							
			encrypted_output = ""
			begin				
				if raw_text != ""
					commons_codec_jar_path = File.join(lib_dir, "commons-codec-1.10.jar")
					encryption_util_jar_path = File.join(lib_dir, "RMEncryptionUtil.jar")
					java_exec = File.join(java_bin_dir, "java")								
					command =  "\"#{java_exec}\" -cp \""+commons_codec_jar_path+";"+encryption_util_jar_path+";.\" com.nextlabs.nxl.sharedutil.EncryptionUtil encrypt "+raw_text
				
					case RUBY_PLATFORM
					when /linux/ then # we are on linux
						command = command.gsub(";",":")
					end					
					encrypted_strings = ""					
					io = IO.popen(command, :err=>[:child, :out])
					encrypted_strings = io.readlines					
					io.close
					raise "Error: Failed to encrypt the password." if $?.exitstatus != 0
					encrypted_strings.each do |line|
						if /\AEncrypted content:/.match line
							encrypted_output = line.sub("Encrypted content:","").gsub("\r","").gsub("\n","").strip
						end
					end
				end
			rescue
				raise "Error: Failed to encrypt the password."
			end			
			return encrypted_output
		end		

		def self.buildDBProperties(db_type, db_host_name, db_port, db_name, db_username)
			if db_type == "MSSQL"
				db_conn_url = "jdbc:sqlserver://"+db_host_name+":"+db_port+";DatabaseName="+db_name+";"	
				db_jdbc_driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver"				
			elsif db_type == "ORACLE"
				db_conn_url = "jdbc:oracle:thin:@"+db_host_name+":"+db_port+":"+db_name
				db_jdbc_driver = "oracle.jdbc.OracleDriver"				
			elsif db_type == "MYSQL"
				db_conn_url = "jdbc:mysql://"+db_host_name+":"+db_port+"/"+db_name +"?useSSL=false"
				db_jdbc_driver = "com.mysql.jdbc.Driver"				
			end			
			db_conn_url = (db_host_name=="" || db_port=="" || db_name=="") ? "": db_conn_url
			db_settings = {
				"db_conn_url" => db_conn_url,
				"db_jdbc_driver" => db_jdbc_driver,
				"db_username" => db_username
			}								
			return db_settings
		end

		def self.extract_utility(zip_file, dest_folder)			
			case RUBY_PLATFORM
			when /mingw/ then # we are on windows
				bin_loc = '"' + File.join(Utility::START_DIR, 'engine', '7za.exe').gsub('/', '\\') + '"'
				zip_file = '"' + zip_file.gsub('/', '\\') + '"'
				dest_folder = '"' + dest_folder.gsub('/', '\\') + '"'
				extract_command = "#{bin_loc} x #{zip_file} -o#{dest_folder} -y > nul"
			when /linux/ then# we are on linux
				zip_file = '"' + zip_file + '"'
				dest_folder = '"' + dest_folder + '"'
				
				if (File.extname(zip_file)==".zip")
					extract_command = "unzip -q -o #{zip_file} -d #{dest_folder}"
				else
					extract_command = "tar xf #{zip_file} -C #{dest_folder}"
				end
			end
			pipe = IO.popen(extract_command, :err=>[:child, :out])	
			puts pipe.readlines						
			pipe.close
			raise "Error: Failed to extract the file" if $?.exitstatus != 0
		end		

		def self.unzip_jre			
			global_config_file = File.join(File.dirname(__FILE__), "../default_setup.json")
			global_config = JSON.parse(File.read(global_config_file))			
			dest_folder =  File.join(Utility::START_DIR, 'dist', 'external')
			tmp_jre_dir = File.join(Utility::START_DIR, 'dist', 'external', 'jre-temp')			
			Dir.mkdir tmp_jre_dir unless Dir.exist? tmp_jre_dir			
			case RUBY_PLATFORM
			  when /mswin|mingw|windows/			    			    	            		            
		        zip_file =  File.join(Utility::START_DIR, global_config["jre"]["windows"])
		        Config.extract_utility(zip_file, dest_folder)
		        tmp_tar_file = File.join(Utility::START_DIR, global_config["jre"]["windows"].sub!(".gz", ""))
		        #untar
		        Config.extract_utility(tmp_tar_file, tmp_jre_dir)		        		        
		        FileUtils.rm_rf(tmp_tar_file)			    		        
			  when /linux/			    			    
			    zip_file =  File.join(Utility::START_DIR, global_config["jre"]["linux"])
			    Config.extract_utility(zip_file, tmp_jre_dir)			    			    
			end			
			#Rename JRE_x.x.x to jre
			Dir.chdir(tmp_jre_dir) do
				FileUtils.mv(File.join(tmp_jre_dir, Dir.glob("jre*")),File.join(dest_folder,"jre"))
			end						
			FileUtils.rm_rf(tmp_jre_dir)
		end
	end

	module DB

	    # check the database connection
	    #
	    # - sqlserver://localhost:1433;DatabaseName=<db name>;
	    # - oracle:thin:@localhost:1521:orcl
	    # - postgresql://localhost:5432/cc76
	    def self.connect_to_DB(connection_string, username, password, jre_x_path, classpath, tries=1, seconds=15)	      
	      retries ||= tries

	      Timeout::timeout(seconds) do

	        command = "\"#{jre_x_path}\" -cp \"#{classpath}\" com.nextlabs.rms.sharedutil.DBConnectionUtil \"#{connection_string}\" \"#{username}\" \"#{password}\""

	        puts("[DB] execute connect DB command: " + command)

	        pipe = IO.popen(command, :err=>[:child, :out])

	        Process.wait(pipe.pid)

	        output = pipe.readline.strip
	        puts output
	        if $?.exitstatus != 0 || output != 'true'
	          raise "Failed to connect to DB: #{output}"
	        else
	          return true
	        end
	      end
	    rescue Exception => ex
	      if (retries -= 1) > 0
	        puts("Retry DB connect")
	        retry
	      else
	        raise ex
	      end
	    end

	    # method that would be invoked by the GUI
	    def self.test_db_connection(connectionString, username, password, seconds=15)	      
	      connectionString = connectionString.sub!(/^jdbc:/, "")	      	      	     
	      dest_folder =  File.join(Utility::START_DIR, 'dist', 'external')	      	      
	      jre_dir = File.join(dest_folder, 'jre')	      	      	      
	      case RUBY_PLATFORM
	          when /mswin|mingw|windows/
	            java_executable = 'java.exe'	            
	          when /linux/
	            java_executable = 'java'	            
	      end
			unless Dir.exist? jre_dir
				Config.unzip_jre		            
			end
	      jre_x_path = File.join(jre_dir, 'bin', java_executable)
	      classpath_separator = case RUBY_PLATFORM
	                              when /mswin|mingw|windows/
	                                ';'
	                              when /linux/
	                                ':'
	                            end
	      # the path contains required jars used for db validation
	      support_dir = File.join(Utility::START_DIR, 'dist', 'rms', 'lib')
	      classpath = Dir[support_dir + '/*.jar'].join(classpath_separator)		      	       	          	   
	      dbConnectionSuccess = connect_to_DB(connectionString, username, password, jre_x_path, classpath, tries=1, seconds=seconds)
	    end

	    private_class_method(:connect_to_DB)
  	end
end
