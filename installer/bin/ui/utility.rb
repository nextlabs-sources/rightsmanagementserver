require "json"
require "resolv"
require_relative "../../cookbooks/RMS/libraries/utility"

module Utility

  ReadableNames = JSON.parse(File.read(File.join(File.dirname(__FILE__), "message_properties.json"), :encoding => "utf-8"))
  case $installMode
  when "install"
    setupText = "Installation"
  when "upgrade"
	setupText = "Upgrade"
  when "uninstall"
	setupText = "Uninstallation"
  end
  ReadableNames["title"] = ReadableNames["title"].gsub("Setup", setupText)
  ReadableNames["cancel_confirm"] = ReadableNames["cancel_confirm"].gsub("Setup", setupText)
  ReadableNames["install_page"]["progress_start_chef"] = ReadableNames["install_page"]["progress_start_chef"].gsub("Setup", setupText)
  ReadableNames["install_page"]["rollback_finish"] = ReadableNames["install_page"]["rollback_finish"].gsub("Setup", setupText)
  ReadableNames["install_page"]["progress_finish"] = ReadableNames["install_page"]["progress_finish"].gsub("Setup", setupText)
  ReadableNames["install_page"]["progress_failed"] = ReadableNames["install_page"]["progress_failed"].gsub("Setup", setupText)
  
  module Validator
    # validator modules includes some valiators that are 
    # methods that accept an input
    # and returns (valid, error_msg) 
    @@validator_errors_msgs = ReadableNames["validator_errors"]

    def self.validate_ip ip
      # ip address or localhost
      # for validate ip adreess, refer to http://goo.gl/RsnfkC
      valid = ((ip.eql? "localhost") || ip =~ Resolv::IPv4::Regex) ? true : false
      error_msg = valid ? "" : (@@validator_errors_msgs["ip"])
      return valid, error_msg
    end

    def self.validate_hostname hostname
      # valid hostname, refer to http://goo.gl/x2x0dY'
      validate_hostname_lambda = lambda { |hostname|
        if hostname.length > 255 then
          return false
        end
        hostname = hostname[0..-2] if ( hostname[-1].eql? "." )
        if hostname.split(".").select { |chunk| 
          chunk =~ /^(?!-)[A-Z\d\-_]{1,63}(?<!-)$/i
          }.length == hostname.split(".").length then
          return true
        else
          return false
        end
      }

      valid = validate_hostname_lambda.call hostname
      error_msg = valid ? "" :(@@validator_errors_msgs["hostname"])
      return valid, error_msg
    end

    def self.validate_port port
	
      valid = ((port =~ /^[1-9]\d{0,4}$/) and port.to_i <= 65535) ? true : false
      error_msg = valid ? "" :(@@validator_errors_msgs and @@validator_errors_msgs["port"])
      return valid, error_msg
    end

    def self.validate_dir dir
      # only for windows 
	  if RUBY_PLATFORM.include? "mingw"
		drive = dir[0,2].upcase
		valid = (Dir.exist? drive) ? true : false 
	  else
		valid = dir.index("/") == 0 ? true : false
	  end
      error_msg =  valid ? "" :(@@validator_errors_msgs and @@validator_errors_msgs["dir"])
      return valid, error_msg
    end
	
	def self.validate_dat_file_exist file
	  valid = (file == "" || (File.file?(file) && File.extname(file)==".dat")) ? true : false
	  error_msg = valid ? "" :( @@validator_errors_msgs and @@validator_errors_msgs["dat_file"])
	  return valid, error_msg
	end
    
    def self.validate_non_empty input
      valid = ((input != nil) and (input != ""))
      error_msg =  valid ? "" :( @@validator_errors_msgs and @@validator_errors_msgs["non_empty"])
      return valid, error_msg
    end
  end

  class Item
    
    attr_reader :installation_dir, :data_dir, \
                :license_file_location, :installation_type, \
                :rms_ssl_port, :rms_shutdown_port, :rmi_km_port,\
				:icenet_server, :delete_data_dir, \
				:component_rms, :component_kms, :km_server, :km_keystore_password, \
				:ldap_type,:ldap_host_name, :ldap_domain, :ldap_search_base, :ldap_user_group, :ldap_admin, \
		        :rms_db_type, :rms_db_host_name, :rms_db_port, :rms_db_name, :rms_db_username, :rms_db_password, :rms_db_conn_url, \
				:kms_db_type, :kms_db_host_name, :kms_db_port, :kms_db_name, :kms_db_username, :kms_db_password, :kms_db_conn_url

    attr_writer :installation_dir, :data_dir, \
                :license_file_location, :installation_type, \
                :rms_ssl_port, :rms_shutdown_port, :rmi_km_port,\
				:icenet_server, :delete_data_dir, \
				:component_rms, :component_kms, :km_server, :km_keystore_password, \
				:ldap_type,:ldap_host_name, :ldap_domain, :ldap_search_base, :ldap_user_group, :ldap_admin, \
		        :rms_db_type, :rms_db_host_name, :rms_db_port, :rms_db_name, :rms_db_username, :rms_db_password, :rms_db_conn_url, \
				:kms_db_type, :kms_db_host_name, :kms_db_port, :kms_db_name, :kms_db_username, :kms_db_password, :kms_db_conn_url

    DATABASE_TYPES = ["MSSQL", "ORACLE", "MYSQL", "IN_BUILT"]  
    DB_PORT = {
    	"MSSQL" => "1433",
    	"ORACLE" => "1521",
    	"MYSQL" => "3306",
    	"IN_BUILT" => "3306"
    }
    DB_FRIENDLY_NAMES_MAP = {
    	"Microsoft SQL Server" => "MSSQL", 
    	"Oracle" => "ORACLE", 
    	"MySQL" => "MYSQL", 
    	"In-Built" => "IN_BUILT"
	}
	
	DB_FRIENDLY_NAMES_REVERSE_MAP = DB_FRIENDLY_NAMES_MAP.invert
	
	LDAP_FRIENDLY_NAMES_MAP ={
	    "Active Directory" =>"AD",
		"OpenLDAP" =>"OPENLDAP"
	}
	LDAP_FRIENDLY_NAMES_REVERSE_MAP = LDAP_FRIENDLY_NAMES_MAP.invert
    
	def initialize()

      config_file_path = File.join(File.dirname(__FILE__), "../../cookbooks/RMS/default_setup.json")
      external_config_defaults = nil
	  external_config_defaults = JSON.parse(File.read(config_file_path))
	  
	  return if $installMode == "uninstall" && !Utility::Config.is_RMS_installed? && !Utility::Config.is_KMS_installed?

	  if $installMode == "install"
		case RUBY_PLATFORM
	    when /mingw/ then # we are on windows
	      @installation_dir = external_config_defaults["install_dir"]["windows"]
		  @data_dir = external_config_defaults["data_dir"]["windows"]
	    when /linux/ then# we are on linux
		  @installation_dir = external_config_defaults["install_dir"]["linux"]
		  @data_dir = external_config_defaults["data_dir"]["linux"]
	    end
		@component_rms = "yes"
		@component_kms = "no"
	  else
		@installation_dir, @data_dir = Utility::Config.get_installation_dir_and_data_dir
		@component_rms = Utility::Config.is_RMS_installed? ? "yes" : "no"
		@component_kms = "no"
	  end
	  @license_file_location = ""	  
	  @icenet_server = ""
	  @rms_ssl_port = external_config_defaults["tomcat"]["ssl_port"]
	  @rms_shutdown_port = external_config_defaults["tomcat"]["shutdown_port"]
	  @rmi_km_port = external_config_defaults["tomcat"]["km_port"]
	  @ldap_type = ""
	  @ldap_host_name = ""
	  @ldap_domain = ""
	  @ldap_search_base = ""
	  @ldap_user_group = ""
	  @ldap_admin = ""
	  @km_server = ""
	  @rms_db_type = ""
      @rms_db_host_name =  ""
      @rms_db_port = ""
      @rms_db_name = ""
      @rms_db_username = ""
      @rms_db_password = ""
	  @rms_db_conn_url = ""
	  @kms_db_type = ""
      @kms_db_host_name = ""
      @kms_db_port = ""
      @kms_db_name = ""
      @kms_db_username = ""
      @kms_db_password = ""
	  @kms_db_conn_url = ""	  
	  @km_keystore_password = ""

	  #Preparing Summary
	  existing_ver_info = File.join(@installation_dir, ".version_info.json")
	  new_ver_info = File.join(ENV['START_DIR'],"bin/.version_info.json")
      version = Utility::Config.get_version_comparison(existing_ver_info, new_ver_info)
	  $summary_hash = {
		"rms_ver" => version['new']['rms'],
		"kms_ver" => version['new']['kms'],
		"install_tomcat" => version['new']['tomcat'] > version['old']['tomcat']
	  }
	  
	  #Pretty UI print for WINDOWS 
	  if RUBY_PLATFORM.include? "mingw"
		@license_file_location = @license_file_location.gsub("/","\\") 
		@installation_dir = @installation_dir.gsub("/","\\") 
		@data_dir = @data_dir.gsub("/","\\") 
	  end
    end

    def to_json
      hash = {}
	  self.remove_line_endings
      self.instance_variables.each { |var|
        hash[var.to_s.delete("@")] = self.instance_variable_get(var)                
      }      
      return JSON.pretty_generate(hash)
    end

	def readRMSConfig
		rms_config = Utility::Config.read_RMS_Config($item.data_dir)
		if !rms_config.empty?
			@ldap_type = rms_config.fetch('ldap_type', '')
			@ldap_host_name = rms_config.fetch('ldap_host_name', '')
			@ldap_domain = rms_config.fetch('ldap_domain', '')
			@ldap_search_base = rms_config.fetch('ldap_search_base', '')
			@ldap_user_group = rms_config.fetch('ldap_user_group', '')
			@ldap_admin = rms_config.fetch('ldap_admin', '')
			@km_server = rms_config.fetch('kms_url', '')
		end
	end
	
	def remove_line_endings
		$item.data_dir = $item.data_dir.gsub("\r","").gsub("\n","").strip
		$item.installation_dir = $item.installation_dir.gsub("\r","").gsub("\n","").strip
		$item.license_file_location = $item.license_file_location.gsub("\r","").gsub("\n","").strip 
		$item.icenet_server = $item.icenet_server.gsub("\r","").gsub("\n","").strip
		$item.rms_ssl_port = $item.rms_ssl_port.gsub("\r","").gsub("\n","").strip
		$item.rms_shutdown_port = $item.rms_shutdown_port.gsub("\r","").gsub("\n","").strip
		$item.rmi_km_port = $item.rmi_km_port.gsub("\r","").gsub("\n","").strip
		$item.ldap_host_name = $item.ldap_host_name.gsub("\r","").gsub("\n","").strip
		$item.ldap_domain = $item.ldap_domain.gsub("\r","").gsub("\n","").strip
		$item.ldap_search_base = $item.ldap_search_base.gsub("\r","").gsub("\n","").strip
		$item.ldap_user_group = $item.ldap_user_group.gsub("\r","").gsub("\n","").strip
		$item.ldap_admin = $item.ldap_admin.gsub("\r","").gsub("\n","").strip
		$item.km_server = $item.km_server.gsub("\r","").gsub("\n","").strip
	    $item.rms_db_host_name = $item.rms_db_host_name.gsub("\r","").gsub("\n","").strip
        $item.rms_db_port = $item.rms_db_port.gsub("\r","").gsub("\n","").strip
	    $item.rms_db_name = $item.rms_db_name.gsub("\r","").gsub("\n","").strip
    	$item.rms_db_username = $item.rms_db_username.gsub("\r","").gsub("\n","").strip
	    $item.rms_db_password = $item.rms_db_password.gsub("\r","").gsub("\n","").strip
		$item.rms_db_conn_url = $item.rms_db_conn_url.gsub("\r","").gsub("\n","").strip
		$item.kms_db_host_name = $item.kms_db_host_name.gsub("\r","").gsub("\n","").strip
        $item.kms_db_port = $item.kms_db_port.gsub("\r","").gsub("\n","").strip
	    $item.kms_db_name = $item.kms_db_name.gsub("\r","").gsub("\n","").strip
    	$item.kms_db_username = $item.kms_db_username.gsub("\r","").gsub("\n","").strip
	    $item.kms_db_password = $item.kms_db_password.gsub("\r","").gsub("\n","").strip
		$item.kms_db_conn_url = $item.kms_db_conn_url.gsub("\r","").gsub("\n","").strip
		$item.km_keystore_password = $item.km_keystore_password.gsub("\r","").gsub("\n","").strip
	end
	
    def validate_fields *fields
      validators = {
        "installation_dir"          => [:validate_non_empty, :validate_dir],
		"data_dir"					=> [:validate_non_empty, :validate_dir],
		"license_file_location"     => [:validate_dat_file_exist],
		"rms_ssl_port"				=> [:validate_non_empty, :validate_port],
        "rms_shutdown_port"			=> [:validate_non_empty, :validate_port],
		"rmi_km_port"				=> [:validate_non_empty, :validate_port],
		"icenet_server"				=> [:validate_non_empty],
		"ldap_host_name"			=> [:validate_non_empty],
		"ldap_domain"				=> [:validate_non_empty],
		"ldap_search_base"			=> [:validate_non_empty],
		"ldap_admin"				=> [:validate_non_empty],
		"km_server"					=> [:validate_non_empty],
	    "rms_db_host_name"       	=> [:validate_non_empty],
		"rms_db_port"            	=> [:validate_non_empty, :validate_port],
	    "rms_db_name"        		=> [:validate_non_empty],
	    "rms_db_username"        	=> [:validate_non_empty],
		"kms_db_host_name"       	=> [:validate_non_empty],
		"kms_db_port"            	=> [:validate_non_empty, :validate_port],
	    "kms_db_name"        		=> [:validate_non_empty],
	    "kms_db_username"        	=> [:validate_non_empty]
      }
      error_field = nil
      error_msg = ""

      fields.each do |field|

        validators[field].each do |validator|
          valid, err_msg = Validator.method(validator).call(
              self.instance_variable_get(("@"+field).to_sym) )
          if not valid then
            error_field = field
            error_msg = ReadableNames["ErrorMsgTemplate"] % 
                [ReadableNames["inputs"][field], err_msg]
            break
          end
        end
        
        break if (error_field != nil)
      end

      if error_field != nil then 
        return [false, error_field, error_msg]
      else
        return [true, nil, nil]
      end
    end
  end
end

Dir[File.expand_path(File.dirname(__FILE__)) + '/libraries/*.rb'].each {|file|
  require file
}
