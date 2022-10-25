require_relative "../bootstrap"
require_relative "../utility"
require_relative "../../../cookbooks/RMS/libraries/utility"

include Utility

class Installation < Shoes

  url '/rms_ports',             :rms_ports

  def rms_ports 

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
           para ReadableNames["rms_ports_page"]["heading"], @@heading_1_style 
         end

         stack :width => 1.0, :height => 60, :margin_right => 10 do
          para ReadableNames["rms_ports_page"]["help_note"],  @@help_note_style

         end

        flow :width => 1.0, :height => 40 do 
           stack :width => 300, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["inputs"]["rms_ssl_port"],  @@text_label_style
           end

           stack :width => 200, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 40 do
            @rms_ssl_port_edit = nl_text_edit( :short, text=$item.rms_ssl_port, { :width => 100 } )
			@rms_ssl_port_edit.focus if @rms_ssl_port_edit.text == ''
           end
        end 

        flow :width => 1.0, :height => 40 do 
           stack :width => 300, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["inputs"]["rms_shutdown_port"],  @@text_label_style
           end

           stack :width => 250, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 40 do
            @rms_shutdown_port_edit = nl_text_edit( :short, text=$item.rms_shutdown_port, { :width => 100 } )
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

          rms_ports_back_proc = Proc.new{
            visit(wizard(:back))
          }

          stack :width => 50, :height => 1.0 do
            @rms_ports_back_btn = nl_button :back
            @rms_ports_back_btn.click { rms_ports_back_proc.call }
          end

          stack :width => 50, :height => 1.0 do
            @rms_ports_back_text_btn = nl_button :back_text, :click => rms_ports_back_proc
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

          rms_ports_next_proc = Proc.new{

            control_fields = {
              "rms_ssl_port"      => @rms_ssl_port_edit,
              "rms_shutdown_port"  => @rms_shutdown_port_edit,
            }
            $item.rms_ssl_port = @rms_ssl_port_edit.text
            $item.rms_shutdown_port = @rms_shutdown_port_edit.text
			
            validated, field, error_msg = $item.validate_fields "rms_ssl_port", "rms_shutdown_port", "rmi_km_port"
	
            if !validated
              alert_ontop_parent(app.win, error_msg, :title => app.instance_variable_get('@title'))
              control_fields[field].focus
            else
			  if $item.rms_ssl_port == $item.rms_shutdown_port
				alert_ontop_parent(app.win, "Port numbers cannot be the same.", :title => app.instance_variable_get('@title'))
				control_fields["rms_shutdown_port"].focus
			  else 
			    if !Utility::TCP.is_port_available($item.rms_ssl_port) 
				  alert_ontop_parent(app.win, "Port #{$item.rms_ssl_port} is not available. Please choose another port.", :title => app.instance_variable_get('@title'))
				  control_fields["rms_ssl_port"].focus
			    elsif !Utility::TCP.is_port_available($item.rms_shutdown_port) 
		   		  alert_ontop_parent(app.win, "Port #{$item.rms_shutdown_port} is not available. Please choose another port.", :title => app.instance_variable_get('@title'))
				  control_fields["rms_shutdown_port"].focus
			    else
				  visit(wizard(:next))
			    end
			  end
            end
          }

          stack :width => 50, :height => 1.0 do
            @rms_ports_next_text_btn = nl_button :next_text, :click => rms_ports_next_proc
          end

          stack :width => 50, :height => 1.0 do
            @rms_ports_next_btn = nl_button :next
            @rms_ports_next_btn.click { rms_ports_next_proc.call }
          end
		  keypress { |k| 
			rms_ports_next_proc.call if k == "\n"
		  }
        end
      end
    end
  end
end

if __FILE__ == $0
  Shoes.app :title => ReadableNames["title"] , :width => 950, :height => 700 do
    win.set_window_position(Gtk::Window::POS_CENTER_ALWAYS)
    visit(wizard('/rms_ports'))
  end
end