require "green_shoes"
require "gtk2"

require_relative "../../cookbooks/RMS/libraries/utility"

ENV['RMS_GUI']="gui"

#Check installation mode
$installMode = ARGV[0]
if (($installMode == "install") && (Utility::Config.is_RMS_installed? || Utility::Config.is_KMS_installed?))
	$installMode = "upgrade"
end

require_relative "./bootstrap"
require_relative "./utility"
include Utility
require_relative "./wizard.rb"

Dir[File.expand_path(File.dirname(__FILE__)) + '/pages/*.rb'].each {|file|
  require file
}

Shoes.app :title => ReadableNames["title"] , :width => 950, :height => 700 do
  # disable the window from resized by the user
  # refer to http://goo.gl/H6m1rZ
  # win.set_size_request(950, 700)
  # win.set_resizable(false)
  win.set_window_position(Gtk::Window::POS_CENTER_ALWAYS)
  visit('/welcome')
end
