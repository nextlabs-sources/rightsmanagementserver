file = File.read("#{ENV['START_DIR']}/cookbooks/RMS/default_setup.json", :encoding => "utf-8")
data_hash = JSON.parse(file)

default["rms_required_space_gb"]["install_dir"] = 2
default["rms_required_space_gb"]["data_dir"] = 5

# NextLabs Distributables
default["rms"]["war"] = "dist/rms/RMS.war"
default["kms"]["war"] = "dist/rms/KMS.war"

# JRE and Tomcat Distributables
default["rms"]["tomcat"] = "dist/external/apache-tomcat-8.5.35-windows-x64.zip"
default["rms"]["jre"] = value_for_platform(
	"windows" => { "default" => data_hash["jre"]["windows"]},
	"default" => data_hash["jre"]["linux"]
)
default["rms"]["jce"] = "dist/external/UnlimitedJCEPolicyJDK8.zip"

#MySQL Distributables
default["rms"]["mysql"] = value_for_platform(
	"windows" => { "default" => data_hash["mysql"]["windows"]},
	"default" => data_hash["mysql"]["linux"]
)

# SharePoint Distributables
default["rms"]["sp_online"] = "dist/sharepoint/SecureCollaboration_SPOnLineApp-1.1.0.0-59-release-20160419.zip"
default["rms"]["sp_onpremise"] = "dist/sharepoint/SecureCollaboration_SPOnPremiseApp-1.1.0.0-59-release-20160419.zip"
default["rms"]["sp_online_repo"] = "dist/sharepoint/SecureCollaboration_SPOnlineRepositoryApp.app"
default["rms"]["conf"] = "dist/conf/"
default["rms"]["docs"] = "docs/"

# CAD, Perceptive and Location DB
default["rms"]["ipToCountry"] = "dist/external/IpToCountry.csv"
default["rms"]["cad"]["ms2012"] = "dist/external/vcredist_x64_30679.exe"
default["rms"]["cad"]["ms2013"] = "dist/external/vcredist_x64_40784.exe"
default["rms"]["cad"]["ms2015"] = "dist/external/vcredist_x64_48145.exe"
default["rms"]["cad"]["ms2010"] = "dist/external/vcredist_x64_14632.exe"

#Embedded Java PC
default["rms"]["embedded_jpc"] = "dist/javapc"
default["jpc"]["plugins"]["key_management"]["name"] = "KeyManagement"
default["jpc"]["plugins"]["key_management"]["properties"] = "KeyManagementService.properties"
default["jpc"]["plugins"]["key_management"]["jar"] = "KeyManagementService.jar"

default["rms"]["license_dir"] = "dist/rms/license/"
default["rms"]["license_file"] = "license.dat"

default["rms"]["policy_model"] = "dist/rms/Policy Model/"

default["rms"]["lib"] = "dist/rms/lib/"

default["rms"]["7z"] =	"engine/7za.exe"
default["rms"]["installation_dir"] = value_for_platform(
	"windows" => { "default" => data_hash["install_dir"]["windows"]},
	"default" => data_hash["install_dir"]["linux"]
)
default["rms"]["data_dir"] = value_for_platform(
	"windows" => { "default" => data_hash["data_dir"]["windows"]},
	"default" => data_hash["data_dir"]["linux"]
)
default["embedded_jpc"]["name"] = "javapc"
default["uninst_name"] = ".uninst"

default["rms_service_name"] = "rms"
default["rms__mysql_service_name_unix"] = "rms_mysql"
default["rms_version_info_json"] = ".version_info.json"

default["tomcat"]["name"] = "tomcat"
default["tomcat"]["shutdown_port"] = data_hash["tomcat"]["shutdown_port"]
default["tomcat"]["ssl_port"] = data_hash["tomcat"]["ssl_port"]
default["tomcat"]["rmi_km_port"] = data_hash["tomcat"]["km_port"]
default["tomcat"]["keystore_password"] = "changeit"
default["tomcat"]["keystore_type"] = "JKS"
default["tomcat"]["keystore_file"] = "rms_tomcat_keystore.jks"
default["tomcat"]["max_post_size"] = "524288000"
default["tomcat"]["ssl_max_threads"] = "300"

default["mysql"]["db_name"] = data_hash["mysql"]["db_name"]
default["mysql"]["db_type"] = data_hash["mysql"]["db_type"]
default["mysql"]["port"] = data_hash["mysql"]["port"]
default["mysql"]["user_name"] = data_hash["mysql"]["user_name"]
default["mysql"]["password"] = data_hash["mysql"]["password"]
default["mysql"]["service_name"] = "NextLabs Rights Management Database"
