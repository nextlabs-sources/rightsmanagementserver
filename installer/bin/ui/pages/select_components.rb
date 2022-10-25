require_relative "../bootstrap"
require_relative "../utility"
include Utility
require 'fileutils'

class Installation < Shoes

  url '/select_components',             :select_components

  def select_components 

    style(Link, :underline => false, :stroke => "#FFF", :weight => "bold")
    style(LinkHover, :underline => false, :stroke => "#FFF", :weight => "bold")

    stack @@installer_page_size_style do
      
      
      # The header area
      stack @@installer_header_size_style do

        background @@header_color_style
        banner

      end

      # The body

      stack @@installer_content_size_style do

        background @@content_color_style

        stack :width => 1.0, :height => 40 do
          para ReadableNames["select_components_page"]["heading"], @@heading_1_style 
        end

        stack :width => 1.0, :height => 60 do
		  placeholderText = $installMode == "upgrade" ? "upgraded" : $installMode + "ed"
          para ReadableNames["select_components_page"]["help_note"] % placeholderText , @@help_note_style
        end

		unless($installMode == "uninstall" && !Utility::Config.is_RMS_installed?)
			stack :width => 1.0, :height => 20 do
			  flow :width => 1.0, :height => 200 do
				para "", @@text_label_style.merge({:width => 10})
				@install_rms_check = check
				@install_rms_check.checked = $item.component_rms == "no" ? false : true
				para ReadableNames["select_components_page"]["rms"] , @@text_label_style.merge({:width => 800})
			  end
			end
		end
		
		stack :width => 1.0, :height => 0 do
          para " "
        end
		
		unless($installMode == "uninstall" && !Utility::Config.is_KMS_installed?)
			stack :width => 1.0, :height => 20 do
			  flow :width => 1.0, :height => 200 do
				para "", @@text_label_style.merge({:width => 10})
				@install_kms_check = check
				@install_kms_check.checked = $item.component_kms  == "no" ? false : true
				para ReadableNames["select_components_page"]["kms"] , @@text_label_style.merge({:width => 800})
			  end
			end
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

          select_components_back_proc = Proc.new{
            visit(wizard(:back))
          }

          stack :width => 50, :height => 1.0 do
            @select_components_back_btn = nl_button :back
            @select_components_back_btn.click { select_components_back_proc.call }
          end

          stack :width => 50, :height => 1.0 do
            @select_components_back_text_btn = nl_button :back_text, :click => select_components_back_proc
          end

          stack :width => 100, :height => 1.0 do
            
            cancel_btn_click_proc = Proc.new{
              if confirm_ontop_parent(app.win, ReadableNames["cancel_confirm"],
                  :title=> app.instance_variable_get('@title')) then
                exit
              end
            }

            nl_button :cancel, :click => cancel_btn_click_proc

          end

          # This is a horizontal spacer
          stack :width => 590, :height => 1.0 do
            para " "
          end

          select_components_next_proc = Proc.new{
			$item.component_rms = (@install_rms_check.nil? || !@install_rms_check.checked?)? "no" : "yes"
			$item.component_kms = (@install_kms_check.nil? || !@install_kms_check.checked?)? "no" : "yes"
			if $item.component_rms == "no" && $item.component_kms == "no"
			  placeholderText = $installMode == "upgrade" ? "upgraded" : $installMode + "ed"
              error_msg = ReadableNames["select_components_page"]["error"] % placeholderText
              alert_ontop_parent(app.win, error_msg, :title => app.instance_variable_get('@title'))
            else
              visit(self.wizard(:next))
            end
          }

          stack :width => 50, :height => 1.0 do
            @select_components_text_btn = nl_button :next_text, :click => select_components_next_proc
          end

          stack :width => 50, :height => 1.0 do
            @select_components_next_btn = nl_button :next
            @select_components_next_btn.click { select_components_next_proc.call }
          end
          keypress { |k| 
			select_components_next_proc.call if k == "\n"
			select_components_back_proc.call if k == "BackSpace"
		  }
        end

      end

    end

  end

end

if __FILE__ == $0
  Shoes.app :title => ReadableNames["title"] , :width => 950, :height => 700 do
    win.set_window_position(Gtk::Window::POS_CENTER_ALWAYS)
    visit(wizard('/select_components'))
  end
end
