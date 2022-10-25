require_relative "../bootstrap"
require_relative "../utility"

class Installation < Shoes

  url '/finish',             :finish

  def finish 
    stack @@installer_page_size_style do
           
      # The header area
      stack @@installer_header_size_style do
        background @@header_color_style
        banner
      end
      # The body
	  case $installMode
	  when "install"
		setupText = "installation"
		logDescription = "installation"
	  when "upgrade"
		setupText = "upgrade"
		logDescription = "installation"
	  when "uninstall"
		setupText = "uninstallation"
		logDescription = "uninstallation"
	  end
	  log_location = File.join(Dir.tmpdir,ENV['LOG_DIR'])
	  log_location = log_location.gsub("/","\\") if RUBY_PLATFORM.include? "mingw"

      stack @@installer_content_size_style do
        background @@content_color_style
        stack :width => 1.0, :height => 80 do
          if $installation_canceled
            para_text = ReadableNames["finish_page"]["aborted_body"] % [setupText, $summary_hash["rms_ver"]]
			para_text += "\n" + ReadableNames["finish_page"]["log_msg"] % [logDescription, log_location]
          elsif $installation_finished
            if $installMode != "uninstall"
			  stack :width => 1.0, :height => 0 do
			    para " "
		      end
		      stack :width => 1.0, :height => 20 do
				para "  " + strong(fg($congrats_msg, @@progress_success_msg_color_style))
		      end
			  stack :width => 1.0, :height => 20 do
				para "  "
		      end
			  stack :width => 1.0, :height => 20 do
				para "  " + strong(fg($access_msg, @@progress_success_msg_color_style))
		      end
			  stack :width => 1.0, :height => 20 do
				para "  "
		      end
			  stack :width => 1.0, :height => 20 do
				para "  " + strong(fg($rms_url, @@progress_success_msg_color_style))
		      end
		    end
			para_text = "\n" + ReadableNames["finish_page"]["log_msg"] % [logDescription, log_location]
          else
			if $installMode == "uninstall" && !Utility::Config.is_RMS_installed?
				para_text = ReadableNames["finish_page"]["failed_body_uninstall_no_rms"]
			else
				para_text = ReadableNames["finish_page"]["failed_body"] % [setupText, $summary_hash["rms_ver"]]
				para_text += "\n" + ReadableNames["finish_page"]["log_msg"] % [logDescription, log_location]
			end
          end
          para para_text, @@help_note_style
        end
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

          stack :width => 50, :height => 1.0 do
            # Finish page don't have back btn (there's no turning back)
            para " "
          end

          # This is a horizontal spacer
          stack :width => 790, :height => 1.0 do
            para " "
          end

          stack :width => 50, :height => 1.0 do
            @finish_btn = nl_button :finish
            @finish_btn.click do
              exit
            end
          end
		  keypress { |k| 
			exit if k == "\n"
		  }
        end
      end
    end
  end
end

if __FILE__ == $0
  Shoes.app :title => ReadableNames["title"] , :width => 950, :height => 700 do
    win.set_window_position(Gtk::Window::POS_CENTER_ALWAYS)
    visit(wizard('/finish'))
  end
end