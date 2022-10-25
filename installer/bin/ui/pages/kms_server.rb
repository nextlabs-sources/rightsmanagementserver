require_relative "../bootstrap"
require_relative "../utility"
require_relative "../../../cookbooks/RMS/libraries/template_generator"
include Utility

class Installation < Shoes

  url '/km_server',             :km_server

  def km_server

    stack @@installer_page_size_style do
      
      style(Link, :underline => false, :stroke => "#FFF", :weight => "bold")
      style(LinkHover, :underline => false, :stroke => "#FFF", :weight => "bold")
      
      # The header area
      stack  @@installer_header_size_style do
        background @@header_color_style
        banner
      end

      # The body
      stack  @@installer_content_size_style do
        background @@content_color_style
         stack :width => 1.0, :height => 40 do
           para ReadableNames["km_server_page"]["heading"], @@heading_1_style 
         end
         stack :width => 1.0, :height => 60, :margin_right => 10 do
          para ReadableNames["km_server_page"]["help_note"],  @@help_note_style
         end
        flow :width => 1.0, :height => 40 do 
           stack :width => 200, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["inputs"]["km_server"],  @@text_label_style
           end
           stack :width => 300, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 40 do
            @km_server_edit = nl_text_edit( :long, text=$item.km_server, { :width => 300 } )
			@km_server_edit.focus
           end
		   stack :width => 300, :margin_left => 20, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["km_server_page"]["km_server_hint"],  @@text_hint_style
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
          icenet_back_proc = Proc.new{
            visit(wizard(:back))
          }
          stack :width => 50, :height => 1.0 do
            @km_server_back_btn = nl_button :back
            @km_server_back_btn.click { icenet_back_proc.call }
          end
          stack :width => 50, :height => 1.0 do
            @km_server_back_text_btn = nl_button :back_text, :click => icenet_back_proc
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
          km_next_proc = Proc.new{
			control_fields = {
              "km_server"      => @km_server_edit,
            }
            $item.km_server = @km_server_edit.text
			validated, field, error_msg = $item.validate_fields "km_server"
	
            if !validated
              alert_ontop_parent(app.win, error_msg, :title => app.instance_variable_get('@title'))
              control_fields[field].focus
			elsif !Utility::URLValidator.is_uri_valid? $item.km_server
		   	  alert_ontop_parent(app.win, "#{$item.km_server} is not a valid URL.", :title => app.instance_variable_get('@title'))
			  control_fields["km_server"].focus
			else
			  visit(wizard(:next))
			end
          }

          stack :width => 50, :height => 1.0 do
            @km_server_next_text_btn = nl_button :next_text, :click => km_next_proc
          end

          stack :width => 50, :height => 1.0 do
            @km_server_next_btn = nl_button :next
            @km_server_next_btn.click { km_next_proc.call }
          end
		  keypress { |k| 
			km_next_proc.call if k == "\n"
		  }
        end
      end
    end
  end
end

if __FILE__ == $0
  Shoes.app :title => ReadableNames["title"] , :width => 950, :height => 700 do
    win.set_window_position(Gtk::Window::POS_CENTER_ALWAYS)
    visit(wizard('/km_server'))
  end
end