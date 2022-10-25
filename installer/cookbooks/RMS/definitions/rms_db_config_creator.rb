define :db_config_creator, :conf_dist_path=>nil, :db_config_filename=>nil, :db_settings=>nil, :encrypted_password=>nil do	
	path = File.join(params[:conf_dist_path], params[:db_config_filename])
	Chef::Log.info("Creating DB Configurations at #{path}...")
	template "create_db_config" do 		
		path path
		source "db_config.erb"
		mode "0664"
		action :nothing
		sensitive true
		variables(				
			:driver => params[:db_settings]["db_jdbc_driver"],
			:jdbc_url => params[:db_settings]["db_conn_url"],
			:username => params[:db_settings]["db_username"],
			:password => params[:encrypted_password]
		)
	end.run_action(:create)
end