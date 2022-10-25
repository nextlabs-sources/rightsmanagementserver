#Delete Existing cert folder in InstallDir
source = File.join(node['installer_home'],node["rms"]["conf"],"cert")
target = File.join(node['installation_dir'],"conf","cert")
if Dir.exist? target
	puts "Deleting existing certificates ..."
	Chef::Log.info("Deleting existing certificates ...")
	FileUtils.rm_rf(target)
	puts "Existing certificates deleted successfully."
	Chef::Log.info("Existing certificates deleted successfully.")
end

#Copy cert folder to InstallDir
puts "Copying certificates ..."
Chef::Log.info("Copying certificates ...")
FileUtils.mkdir_p(target)
Dir.chdir(source) do
	FileUtils.cp_r(Dir.glob('kms*'),target)		if node['install']['kms']
	FileUtils.cp_r(Dir.glob('[^kms]*'),target)	if node['install']['rms']
end
puts "Finished copying certificates folder."
Chef::Log.info("Finished copying certificates folder.")

if node['install']['rms']
	#DEPLOY CERT FOLDER TO DATAFILES IF THEY DONT EXIST
	source = File.join(node['installation_dir'],"conf","cert")
	target = File.join(node['data_dir'], "cert")
	unless File.exist? target
		puts "Copying certificates folder to Data Directory ..."
		Chef::Log.info("Copying certificates folder to Data Directory ...")
		FileUtils.cp_r(source, target)
	else
		puts "Copying certificates to Data Directory ..."
		Chef::Log.info("Copying certificates to Data Directory ...")
		contents = File.join(source,".")
		FileUtils.cp_r(contents, target)
	end
end

if node['install']['kms']
	target =  File.join(node['data_dir'], "KMS/cert")
	FileUtils.mkdir_p(target)	unless Dir.exist? target
	Dir.chdir(source) do
		FileUtils.cp_r(Dir.glob('kms*'),target)
	end
end

puts