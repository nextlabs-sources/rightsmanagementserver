require_relative "./bootstrap"
require_relative "./utility"
include Utility

class Installation < Shoes

  COMPLETE_WIZARD_ARRAY = [
    '/welcome',
	'/agreement',
#	'/select_components',
	'/license',
    '/install_dir',
    '/rms_ports',
	'/rms_db_config',
#	'/kms_db_config',
	'/ad_config',
	'/rmi_eval_port',
	'/icenet_server',
#	'/km_server',
#	'/kms_jceks_config',
    '/summary',
    '/setup',
    '/finish'
  ]
  
  UNINSTALL_WIZARD_ARRAY = [
	'/welcome',
#	'/select_components',
	'/summary_uninstall',
	'/setup',
    '/finish'
  ]
  
  RMS_SCREENS = [
	'/rms_db_config',
	'/ad_config',
	'/rmi_eval_port',
	'/icenet_server',
	'/km_server'
  ]
  
  KMS_SCREENS = [
	'/kms_db_config',
	'/kms_jceks_config'
  ]
  
  case $installMode
  when "install"
    case RUBY_PLATFORM
    when /mingw/ then # we are on windows
	  $wizard_array = COMPLETE_WIZARD_ARRAY
    when /linux/ then# we are on linux
	  $wizard_array = COMPLETE_WIZARD_ARRAY - ['/install_dir']
    end
  when "upgrade"
    case RUBY_PLATFORM
    when /mingw/ then # we are on windows
	  $wizard_array = COMPLETE_WIZARD_ARRAY - ['/license', '/install_dir', '/rms_ports']
    when /linux/ then# we are on linux
	  $wizard_array = COMPLETE_WIZARD_ARRAY - ['/license', '/install_dir', '/rms_ports']
    end
	$wizard_array = $wizard_array - ['/rms_db_config']	if Utility::Config.is_RMS_DB_configured?
	$wizard_array = $wizard_array - ['/kms_db_config']	if Utility::Config.is_KMS_DB_configured?

  when "uninstall"
    $wizard_array = UNINSTALL_WIZARD_ARRAY
  end

  # define history array "stack" here, with the initial position being '/welcome'
  $history = [];
  $history[0] = $wizard_array.find_index('/welcome');

  def wizard direction
    wizard_array = getwizardarray()
    current_idx = wizard_array.find_index(location())

    case direction
    when :next
      $history.push(current_idx)
      return wizard_array.fetch(current_idx + 1)
    when :back
      prev_idx = $history.pop
      return wizard_array.fetch(prev_idx)
    else
      $history.push(current_idx)
      return direction
    end
  end

  def jump numsteps
      wizard_array = getwizardarray()
      current_idx = wizard_array.find_index(location())

      $history.push(current_idx)
      return wizard_array.fetch(current_idx + numsteps)
  end

  def getwizardarray
  	wizard_array = $wizard_array;
	
	if(Utility::Config.is_RMS_installed?)
		wizard_array = wizard_array - ['/ad_config', '/rmi_eval_port', '/icenet_server']
	end

    return wizard_array
  end
end
