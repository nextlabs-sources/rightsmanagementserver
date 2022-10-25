require_relative "../bootstrap"
require_relative "../utility"
include Utility

$install_cmd_exited = false
$rolling_back = false

# to stop the installation half way, we need to send SIGINT to the subprocess,
# but the chef-client spawns other processes, so we must send SIGINT to the process group
# which means the father process will also receive the SIGINT
# so we need to ignore here
trap("INT") {}

class Installation < Shoes

  url '/setup',             :setup

  def setup

    style(Link, :underline => false, :stroke => "#FFF", :weight => "bold")
    style(LinkHover, :underline => false, :stroke => "#FFF", :weight => "bold")

    stack @@installer_page_size_style do
      # The header area
      stack @@installer_header_size_style do
        background @@header_color_style
        banner
      end

      # The progress area
      stack :width => 1.0, :heigth => 75 do
        background @@content_color_style
        # a vertical spacer
        stack :width => 1.0, :height => 50 do
          para " "
        end

        flow :width => 1.0, :height => 25 do     
          stack :width => 85, :height => 1.0 do
            para " "
          end
          stack :width => 700, :height => 1.0 do
            @progress_msg = inscription ""
          end
		end
		flow :width => 1.0, :height => 25 do    
		  stack :width => 85, :height => 1.0 do
            para " "
          end
		  stack :width => 700, :height => 1.0 do
            @rollback_msg = inscription ""
          end
        end

        @progress = progress :width => 750, :left => 85, :top => 125      
      end

      # The log area
      flow :width => 1.0, :height => 375 do
        stack :width => 50, :height => 1.0 do
          background @@content_color_style
          para " "
        end

        stack :width => 850, :height => 1.0 do
          @log_area = edit_box :width => 1.0, :height => 1.0, :state => "disabled"
        end

        stack :width => 50, :height => 1.0 do
          background @@content_color_style
          para " "
        end
      end

      # A vertical spacer
      stack :width => 1.0, :height => 25 do
        background @@content_color_style
        para " "
      end
      
      # The footer area
      stack @@installer_footer_size_style do

        background @@footer_color_style

        # This is a vertical spacer
        stack :width => 1.0, :height => 35 do
          para " "
        end

        flow :width => 1.0, :height => 50 do
          # This is a horizontal spacer
          stack :width => 30, :height => 1.0 do
            para " "
          end

          # The installation page has no back btn
          stack :width => 50, :height => 1.0 do
           para " "
          end

          @install_cancel_btn_slot = stack :width => 100, :height => 1.0 do
            
            # a helper lambda for getting process group id
            sysint_gpid = lambda do
              case RUBY_PLATFORM
              when /mingw/ then
                return 0
              when /linux/ then
                return -Process.getpgrp
              end
            end
            install_cancel_btn_click_proc = Proc.new{
              # The cancel btn should only work during installation
              if !$install_cmd_exited
                if confirm_ontop_parent(app.win, ReadableNames["cancel_confirm"],
                    :title=> app.instance_variable_get('@title')) then
                  if @install_util["pipe"] != nil then
                    # set the canceled flag
                    $installation_canceled = true
                    Process.kill 'INT', sysint_gpid.call
                    # then disable the cancel btn for now
                    @install_cancel_btn.state = "disabled"
                    # Sometimes the SIGINT got ignored by subprocesses, then ,
                    # let's send one more, but still there's occasions 
                    # the subprocess just can't stop itself
                    sleep 1
                    Process.kill 'INT', sysint_gpid.call
              
                  else
                    puts "child not live"
                  end
                end
              end
            }

            nl_button :cancel, :click => install_cancel_btn_click_proc

          end

          # This is a horizontal spacer
          stack :width => 640, :height => 1.0 do
            para " "
          end

          setup_next_proc = Proc.new{

            # we need to check the installation process
            if $install_cmd_exited 
              # First we need move the progress bar
              @progress.move(0, -200)
              visit(wizard(:next))
            end

          }

          @next_text_stack = stack :width => 50, :height => 1.0 do
            @install_next_text_btn = nl_button :next_text, :click => setup_next_proc
          end

          stack :width => 50, :height => 1.0 do
            @install_next_btn = nl_button :next, :state => "disabled" 
            @install_next_btn.click { setup_next_proc.call }
          end

		  keypress { |k| 
			setup_next_proc.call if k == "\n" && ($installation_failed || $installation_finished)
		  }
        end
      end
    end

    # Hide the cancel/next functionality
    @install_cancel_btn_slot.hide
	@install_next_btn.hide
	@next_text_stack.hide

    # Then the installation call
    # First generate the json property file and save the properties
    config_path = "#{ENV['START_DIR']}/setup_ui.json"
    begin
      File.open(config_path, 'w') do |file|
        file.write($item.to_json)
      end
    rescue Exception => ex
      puts "Failed to create setup.json file."
	  raise
    end

    @progress_msg.text = strong(fg(ReadableNames["install_page"]["progress_save_config"], @@progress_msg_color_style))
    @progress.fraction = 0.05

    @progress_msg.text = strong(fg(ReadableNames["install_page"]["progress_start_chef"],@@progress_msg_color_style))
	@log_area.text = @progress_msg.text + "\n"

    @install_util = {}

    update_progress = lambda do |log_msg|
	
	  if match = log_msg.match(/(.*)/i)
        progress_msg = match.captures[0].strip
		if $rolling_back && $installMode != "uninstall"
			@rollback_msg.text = strong(fg(progress_msg, @@progress_msg_color_style))
		else
			@progress_msg.text = strong(fg(progress_msg, @@progress_msg_color_style))
		end
		
		if $installMode == "uninstall"
			case progress_msg
			when /Deleting Installation Directory/i
			  @progress.fraction = 0.33
			when /Deleting Data Directory/i
			  @progress.fraction = 0.67
			when /Uninstallation completed successfully/i
			  @progress.fraction = 1.0
			  $installation_finished = true
			@progress_msg.text = strong(fg(ReadableNames["install_page"]["progress_finish"], @@progress_success_msg_color_style))
			when /Uninstallation failed/i
			  @progress.fraction = 0.0
			  $installation_failed = true
			  @progress_msg.text = strong(fg(ReadableNames["install_page"]["progress_failed"], @@progress_error_msg_color_style))
			end
		else 
			case progress_msg
			when /\AChecking System Requirements/i
			  @progress.fraction = 0.1
			when /\ABacking up Rights Management Server/i
			  @progress.fraction = 0.1
			when /\AInstalling JRE/i
			  @progress.fraction = 0.2
			when /\AInstalling Tomcat/i
			  @progress.fraction = 0.3
			when /\AInstalling MySQL/i
			  @progress.fraction = 0.35  
			when /\AInstalling Rights Management Server/i
			  @progress.fraction = 0.4
			when /\AInstalling Document Viewer pre-requisites/i
			  @progress.fraction = 0.45
			when /\AInstalling CAD Viewer pre-requisites/i
			  @progress.fraction = 0.5
			when /\AInstalling Embedded JPC/i
			  @progress.fraction = 0.55  
			when /\AInstalling Key Management Server/i
			  @progress.fraction = 0.65  
			when /\ASaving version info/i
			  @progress.fraction = 0.7  
			when /\ACreating RMS Utilities/i
			  @progress.fraction = 0.8   
			when /\ACreating KMS Utilities/i
			  @progress.fraction = 0.8
			when /\ASaving uninstall information/i
			  @progress.fraction = 0.85
			when /\AStarting Rights Management Server/i
			  @progress.fraction = 0.95  
			when /\AFinished rolling back Rights Management Server/i		#This one has higher priority
			  @progress.fraction = 0.0
			  $installation_failed = true
			  $rolling_back = false
			  @rollback_msg.text = strong(fg(ReadableNames["install_page"]["rollback_finish"], @@progress_msg_color_style))
			when /\ARolling back Rights Management Server/i
			  @progress.fraction = 0.2
			  $rolling_back = true
			  @progress_msg.text = strong(fg("#{ReadableNames["install_page"]["progress_failed"]} #{$errorMessage}", @@progress_error_msg_color_style))
			when /\AInstallation failed/i
			  @progress.fraction = 0.0
			  $installation_failed = true
			  @progress_msg.text = strong(fg(ReadableNames["install_page"]["progress_failed"], @@progress_error_msg_color_style))
			when /\AInstallation completed successfully/i
			  @progress.fraction = 1.0
			  $installation_finished = true
			  @progress_msg.text = strong(fg(ReadableNames["install_page"]["progress_finish"], @@progress_success_msg_color_style))
			when /been successfully installed.\z/i
			  $congrats_msg = progress_msg
			  @progress_msg.text = strong(fg(ReadableNames["install_page"]["progress_finish"], @@progress_success_msg_color_style))
			when /\AYou can now access/i
			  $access_msg = progress_msg
			  @progress_msg.text = strong(fg(ReadableNames["install_page"]["progress_finish"], @@progress_success_msg_color_style))
			when /\Ahttp.*RMS\z/i
			  $rms_url = progress_msg
			  @progress_msg.text = strong(fg(ReadableNames["install_page"]["progress_finish"], @@progress_success_msg_color_style))
			when /\AError/i
			  $errorMessage = progress_msg.gsub("Error", "")
			  $errorMessage = $errorMessage.gsub(": ", "")
			end
		end
      end
    end

    post_install_script_call = lambda do
	
		@install_next_btn.show
		@next_text_stack.show
		
      if $installation_failed
        @progress.fraction = 0.0
        @progress_msg.text = strong(fg("#{ReadableNames["install_page"]["progress_failed"]} #{$errorMessage}", @@progress_error_msg_color_style))	
      end

      if $installation_canceled then
        @progress.fraction = 0.0
        @progress_msg.text = strong(fg(ReadableNames["install_page"]["progress_canceled"],@@progress_msg_color_style))
      end

      # update the flag
      $install_cmd_exited = true
      # # The installation is finished, chagne btn states
      # @install_next_btn.state = nil
    end     
    
    @progress.fraction = 0.1
	platform = if RUBY_PLATFORM.include? "mingw" then 'windows' else 'linux' end

	if $installMode == "uninstall"
		@install_util["install_script"] = "/opt/chef/bin/chef-client --format null --config ${START_DIR}/bin/client_ui.rb -o RMS::uninstall"
		@install_util["install_script"] = "call \"%START_DIR%/engine/chef/bin/chef-client\" --format null --config \"%START_DIR%/bin/client_ui.rb\" -o RMS::uninstall" if platform == "windows"
	else
		@install_util["install_script"] = "/opt/chef/bin/chef-client --format null --config ${START_DIR}/bin/client_ui.rb -o RMS::install"
		@install_util["install_script"] = "call \"%START_DIR%/engine/chef/bin/chef-client\" --format null --config \"%START_DIR%/bin/client_ui.rb\" -o RMS::install" if platform == "windows"
    end
	trap("INT") {}
      Thread.new do
        trap("INT") {}
        @install_util["pipe"] = IO.popen(@install_util["install_script"])
        @install_util["pipe"].each do |line|
          update_progress.call line
          @log_area.text += line.to_s
        end
        post_install_script_call.call
      end
  end
end

if __FILE__ == $0
  Shoes.app :title => ReadableNames["title"] , :width => 950, :height => 700 do
    win.set_window_position(Gtk::Window::POS_CENTER_ALWAYS)
    visit(wizard('/setup'))
  end
end
