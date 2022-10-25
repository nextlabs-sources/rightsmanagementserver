params = {
	"rms_ver" => node['rms_ver'],
	"jre_ver" => node['jre_ver'],
	"install_jre" => node['install']['jre'],
	"tomcat_ver" => node['tomcat_ver'],
	"install_tomcat" => node['install']['tomcat'],
	"installation_dir" => node['installation_dir'],
	"data_dir" => node['data_dir'],
	"rms_ssl_port" => node['rms_ssl_port'],
	"rms_shutdown_port" => node['rms_shutdown_port']
}
summary_template = File.read(File.join(File.dirname(__FILE__), "../templates/default/summary_uninstall.erb"), :encoding => "utf-8")
puts TemplateGenerator::render(summary_template, params) 