require_relative "../bootstrap"
require_relative "../utility"
require_relative "../../../cookbooks/RMS/libraries/utility"

include Utility

class Installation < Shoes

  url '/rmi_eval_port',             :rmi_eval_port

  def rmi_eval_port

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
           para ReadableNames["rmi_eval_port_page"]["heading"], @@heading_1_style 
         end

         stack :width => 1.0, :height => 60, :margin_right => 10 do
          para ReadableNames["rmi_eval_port_page"]["help_note"],  @@help_note_style

         end
		
		flow :width => 1.0, :height => 40 do 
           stack :width => 300, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["inputs"]["rmi_km_port"],  @@text_label_style
           end

           stack :width => 250, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 40 do
            @rmi_km_port_edit = nl_text_edit( :short, text=$item.rmi_km_port, { :width => 100 } )
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

          rmi_eval_port_back_proc = Proc.new{
            visit(wizard(:back))
          }

          stack :width => 50, :height => 1.0 do
            @rmi_eval_port_back_btn = nl_button :back
            @rmi_eval_port_back_btn.click { rmi_eval_port_back_proc.call }
          end

          stack :width => 50, :height => 1.0 do
            @rmi_eval_port_back_text_btn = nl_button :back_text, :click => rmi_eval_port_back_proc
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

          rmi_eval_port_next_proc = Proc.new{

            control_fields = {
			  "rmi_km_port"		=> @rmi_km_port_edit
            }

			$item.rmi_km_port = @rmi_km_port_edit.text
            validated, field, error_msg = $item.validate_fields "rmi_km_port"
	
            if !validated
              alert_ontop_parent(app.win, error_msg, :title => app.instance_variable_get('@title'))
              control_fields[field].focus
            else 
			  if !Utility::TCP.is_port_available($item.rmi_km_port) 
			    alert_ontop_parent(app.win, "Port #{$item.rmi_km_port} is not available. Please choose another port.", :title => app.instance_variable_get('@title'))
			    control_fields["rmi_km_port"].focus
			  else
			    visit(wizard(:next))
			  end
            end
          }

          stack :width => 50, :height => 1.0 do
            @rmi_eval_port_next_text_btn = nl_button :next_text, :click => rmi_eval_port_next_proc
          end

          stack :width => 50, :height => 1.0 do
            @rmi_eval_port_next_btn = nl_button :next
            @rmi_eval_port_next_btn.click { rmi_eval_port_next_proc.call }
          end
		  keypress { |k| 
			rmi_eval_port_next_proc.call if k == "\n"
		  }
        end
      end
    end
  end
end

if __FILE__ == $0
  Shoes.app :title => ReadableNames["title"] , :width => 950, :height => 700 do
    win.set_window_position(Gtk::Window::POS_CENTER_ALWAYS)
    visit(wizard('/rmi_eval_port'))
  end
end