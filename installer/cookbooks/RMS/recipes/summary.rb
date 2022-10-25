params = {
	"rms_ver" => node['rms_ver'],
	"kms_ver" => node['kms_ver'],
	"install_rms" => node['install']['rms'],
	"rms_installed" => node['rms_installed?'],
	"install_kms" => node['install']['kms'],
	"install_tomcat" => node['install']['tomcat'],
	"installation_dir" => node['installation_dir'],
	"data_dir" => node['data_dir'],
	"rms_ssl_port" => node['rms_ssl_port'],
	"rms_shutdown_port" => node['rms_shutdown_port'],
	"rmi_km_port" => node['rmi_km_port'],
	"icenet_server" => node['icenet_server'],
	"km_server" => node['km_server'],
	"license_path" => node['license_file_location']
}
summary_template = File.read(File.join(File.dirname(__FILE__), "../templates/default/summary.erb"), :encoding => "utf-8")
puts TemplateGenerator::render(summary_template, params) 
puts