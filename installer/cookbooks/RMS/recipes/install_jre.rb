dest_dir =  File.join(node['installation_dir'],'external')
jre_dir =  File.join(dest_dir,'jre')

if node['install']['jre']
	
	#Delete existing installation
	if Dir.exist? jre_dir
		puts "Deleting existing JRE ..."
		Chef::Log.info("Deleting existing JRE ...")
		FileUtils.rm_r	jre_dir
		puts "Existing JRE deleted successfully."
		Chef::Log.info("Existing JRE deleted successfully.")
	end
	
	puts "Installing JRE ..."
	Chef::Log.info("Installing JRE ...")
	if node['platform'] == 'windows'	#windows need to unzip and untar
		#EXTRACT TAR.GZ
		puts "Extracting JRE ..."
		Chef::Log.info("Extracting JRE ...")
		rms_extractor 'extract_jre_tar_gz' do
			extractor_name	"extract_jre_tar_gz"
			zip_file 		File.join(node['installer_home'],node['rms_jre_zip'])
			dest_folder 	node['rms_tmp_dir']
		end

		tmp_tar_dir = ""
		Dir.chdir(node['rms_tmp_dir']) do
			tmp_tar_dir = File.join(node['rms_tmp_dir'], Dir.glob("jre*"))
		end

		#UNTAR
		rms_extractor 'extract_jre_tar' do
			extractor_name	"extract_jre_tar"
			zip_file 		tmp_tar_dir
			dest_folder 	dest_dir
		end
	else	#unix tar does both unzip and untar
		puts "Extracting JRE ..."
		Chef::Log.info("Extracting JRE ...")
		rms_extractor 'extract_jre_targz' do
			extractor_name	"extract_jre_targz"
			zip_file 		File.join(node['installer_home'],node['rms_jre_zip'])
			dest_folder 	dest_dir
		end
	end
	
	#Rename JRE_x.x.x to jre
	Dir.chdir(dest_dir) do
		FileUtils.mv(File.join(dest_dir, Dir.glob("jre*")),jre_dir)
	end
	puts "JRE installed successfully."
	Chef::Log.info("JRE installed successfully.")

	#Copy JCE
	puts "Installing Unlimited Java Cryptography Extension ..."
	Chef::Log.info("Installing Unlimited Java Cryptography Extension ...")
	jceDestDir = File.join(jre_dir, "lib","security");
	File.rename(File.join(jceDestDir,"local_policy.jar"),File.join(jceDestDir,"local_policy.jar.original"));
	File.rename(File.join(jceDestDir,"US_export_policy.jar"),File.join(jceDestDir,"US_export_policy.jar.original"));
	rms_extractor 'extract_jce' do
	extractor_name	"extract_jce"
		zip_file 		File.join(node['installer_home'],node["rms"]["jce"])
		dest_folder 	jceDestDir
	end
	puts "Unlimited Java Cryptography Extension installed successfully."
	Chef::Log.info("Unlimited Java Cryptography Extension installed successfully.")
	puts
end

node.set['rms_jre_home'] = jre_dir