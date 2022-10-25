define :rms_unix_drive_locator do
	begin
	tmp_file = "unix_drive"
		execute 'calculate_disk_space' do 
			command "df -k / | head -2 | tail -1 | awk '{print $1}' > #{tmp_file}"
			action :nothing
			sensitive true
		end.run_action(:run)
	rescue Mixlib::ShellOut::ShellCommandFailed => error
		Chef::Log.error(error.message)
		raise "Error occurred while calculating required disk space!"
	end
	
	file = File.open(tmp_file, "rb")
	$dir_output = file.read.strip
	file.close
	File.delete(tmp_file)
	if $dir_output.include? "by-uuid"
		$dir_output = File.basename($dir_output)
		begin
		tmp_file = "unix_findfs"
			execute 'find fs' do 
				command "findfs UUID=#{$dir_output} > #{tmp_file}"
				action :nothing
				sensitive true
			end.run_action(:run)
		rescue Mixlib::ShellOut::ShellCommandFailed => error
			Chef::Log.error(error.message)
			raise "Error occurred while calculating required disk space!"
		end
		file = File.open(tmp_file, "rb")
		$dir_output = file.read.strip
		file.close
		File.delete(tmp_file)
	end
end