require_relative "../bootstrap"
require_relative "../utility"
include Utility
require 'fileutils'

class Installation < Shoes

  url '/install_dir',             :install_dir

  def install_dir 

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
          para ReadableNames["install_dir_page"]["heading"], @@heading_1_style 
        end

        stack :width => 1.0, :height => 60 do
          para ReadableNames["install_dir_page"]["help_note"], @@help_note_style
        end

        stack :width => 1.0, :margin_top => 10,  :margin_left => 10, :height => 40 do
          para ReadableNames["install_dir_page"]["install_dir_label"], @@text_label_style
        end
        
        flow :width => 750, :height => 40 do
          stack :width => 520, :margin_left => 10, :margin_top => 10, :height => 1.0 do
            @install_dir_edit = nl_text_edit( :long, text=$item.installation_dir, { :width => 500 } )
            @install_dir_edit.change {
              # use "/" to be consistent
              $item.installation_dir = @install_dir_edit.text.gsub("\\", "/")
            }
          end
          stack :width => 180, :margin_left => 10, :margin_right => 10,
              :margin_top => 10, :height => 1.0 do
			nl_button :browse, :click => Proc.new{
				browse_result = ask_open_folder
               (@install_dir_edit.text = browse_result) if browse_result != nil
			}
          end
        end
		
		stack :width => 1.0, :margin_top => 10,  :margin_left => 10, :height => 40 do
          para ReadableNames["install_dir_page"]["data_dir_label"], @@text_label_style
        end
        flow :width => 750, :height => 40 do
          stack :width => 520, :margin_left => 10, :margin_top => 10, :height => 1.0 do
            @data_dir_edit = nl_text_edit( :long, text=$item.data_dir, { :width => 500 } )
            @data_dir_edit.change {
              # use "/" to be consistent
              $item.data_dir = @data_dir_edit.text.gsub("\\", "/")
            }
          end      
          stack :width => 180, :margin_left => 10, :margin_right => 10,
              :margin_top => 10, :height => 1.0 do
            nl_button :browse, :click => Proc.new{
				browse_result = ask_open_folder
               (@data_dir_edit.text = browse_result) if browse_result != nil
			}
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

          install_dir_back_proc = Proc.new{
            visit(wizard(:back))
          }

          stack :width => 50, :height => 1.0 do
            @installl_dir_back_btn = nl_button :back
            @installl_dir_back_btn.click { install_dir_back_proc.call }
          end

          stack :width => 50, :height => 1.0 do
            @installl_dir_back_text_btn = nl_button :back_text, :click => install_dir_back_proc
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

          install_dir_next_proc = Proc.new{
			
            control_fields = {
              "installation_dir"    => @install_dir_edit,
			  "data_dir"			=> @data_dir_edit
            }
            validated, field, error_msg = $item.validate_fields "installation_dir", "data_dir"
            if not validated then
              alert_ontop_parent(app.win, error_msg, :title => app.instance_variable_get('@title'))
              control_fields[field].focus
            else
			  if RUBY_PLATFORM.include? "mingw"
				$item.installation_dir = $item.installation_dir.gsub("/","\\") 
				$item.data_dir = $item.data_dir.gsub("/","\\") 
			  end
              visit(self.wizard(:next))
            end
          }

          stack :width => 50, :height => 1.0 do
            @installl_dir_next_text_btn = nl_button :next_text, :click => install_dir_next_proc
          end

          stack :width => 50, :height => 1.0 do
            @installl_dir_next_btn = nl_button :next
            @installl_dir_next_btn.click { install_dir_next_proc.call }
          end
          keypress { |k| 
			install_dir_next_proc.call if k == "\n"
		  }
        end

      end

    end

  end

end

if __FILE__ == $0
  Shoes.app :title => ReadableNames["title"] , :width => 950, :height => 700 do
    win.set_window_position(Gtk::Window::POS_CENTER_ALWAYS)
    visit(wizard('/install_dir'))
  end
end
