puts "Saving uninstall information ..."
Chef::Log.info("Saving uninstall information ...")

uninstall_dir = File.join(node['installation_dir'],node['uninst_name'])
FileUtils.rm_rf uninstall_dir
FileUtils.mkdir_p uninstall_dir

Dir.chdir(node['installer_home']) do
	FileUtils.cp_r("bin", uninstall_dir)
	FileUtils.cp_r("cookbooks", uninstall_dir)
	FileUtils.cp_r("engine", uninstall_dir)
	FileUtils.cp_r("setup.json", uninstall_dir)
	
	if platform_family?('windows') 
		FileUtils.cp_r("uninstall.bat", uninstall_dir)
		FileUtils.cp_r("bin/uninst/uninstall.bat", node['installation_dir'])
	else
		FileUtils.cp_r("uninstall.sh", uninstall_dir)
		FileUtils.cp_r("bin/uninst/uninstall.sh", node['installation_dir'])
	end
end


