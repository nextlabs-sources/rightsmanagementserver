require_relative "../bootstrap"
require_relative "../utility"
include Utility

class Installation < Shoes

  url '/kms_jceks_config',             :kms_jceks_config

  def kms_jceks_config

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
           para ReadableNames["kms_jceks_page"]["heading"], @@heading_1_style 
         end
         stack :width => 1.0, :height => 60, :margin_right => 10 do
          para ReadableNames["kms_jceks_page"]["help_note"],  @@help_note_style
         end
        flow :width => 1.0, :height => 40 do 
           stack :width => 200, :margin_left => 10, :margin_right => 10, 
             :margin_top => 20, :height => 30 do
              para ReadableNames["inputs"]["jceks_password"],  @@text_label_style
           end
           stack :width => 300, :margin_left => 10, :margin_right => 10, :margin_top => 20, :height => 40 do
			@jceks_password_edit = nl_text_edit( :short, text=$item.km_keystore_password, { :width => 300, :secret => true } )
			@jceks_password_edit.focus
           end
        end
		flow :width => 1.0, :height => 40 do 
           stack :width => 200, :margin_left => 10, :margin_right => 10, 
             :margin_top => 20, :height => 30 do
              para ReadableNames["inputs"]["jceks_password_retype"],  @@text_label_style
           end
           stack :width => 300, :margin_left => 10, :margin_right => 10, :margin_top => 20, :height => 40 do
			@jceks_password_retype_edit = nl_text_edit( :short, text="", { :width => 300, :secret => true } )
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
          kms_jceks_back_proc = Proc.new{
              visit(wizard(:back))
          }
          stack :width => 50, :height => 1.0 do
            @kms_jceks_back_btn = nl_button :back
            @kms_jceks_back_btn.click { kms_jceks_back_proc.call }
          end
          stack :width => 50, :height => 1.0 do
            @kms_jceks_back_text_btn = nl_button :back_text, :click => kms_jceks_back_proc
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
          kms_jceks_next_proc = Proc.new{
		  
			control_fields = {                                  
              "jceks_password" => @jceks_password_edit,
              "jceks_password_retype" => @jceks_password_retype_edit
			}
            $item.km_keystore_password = @jceks_password_edit.text
			
            if $item.km_keystore_password==""
			  error_msg = ReadableNames["ErrorMsgTemplate"] % [ReadableNames["inputs"]["km_keystore_password"],ReadableNames["validator_errors"]["non_empty"]]
              alert_ontop_parent(app.win, error_msg, :title => app.instance_variable_get('@title'))
			  @jceks_password_edit.focus
			elsif @jceks_password_retype_edit.text==""
			  error_msg = ReadableNames["ErrorMsgTemplate"] % [ReadableNames["inputs"]["km_keystore_password"],ReadableNames["validator_errors"]["non_empty"]]
              alert_ontop_parent(app.win, error_msg, :title => app.instance_variable_get('@title'))
			  @jceks_password_retype_edit.focus
			elsif($item.km_keystore_password != @jceks_password_retype_edit.text)
			  alert_ontop_parent(app.win, ReadableNames["kms_jceks_page"]["error_not_match"], :title => app.instance_variable_get('@title'))
			  @jceks_password_edit.focus
			elsif($item.km_keystore_password.length < 6)
			  alert_ontop_parent(app.win, ReadableNames["kms_jceks_page"]["error_short_pwd"], :title => app.instance_variable_get('@title'))
			  @jceks_password_edit.focus
			else
			  visit(wizard(:next))
			end
          }

          stack :width => 50, :height => 1.0 do
            @kms_jceks_next_text_btn = nl_button :next_text, :click => kms_jceks_next_proc
          end

          stack :width => 50, :height => 1.0 do
            @kms_jceks_next_btn = nl_button :next
            @kms_jceks_next_btn.click { kms_jceks_next_proc.call }
          end
		  keypress { |k| 
			kms_jceks_next_proc.call if k == "\n"
		  }
        end
      end
    end
  end
end

if __FILE__ == $0
  Shoes.app :title => ReadableNames["title"] , :width => 950, :height => 700 do
    win.set_window_position(Gtk::Window::POS_CENTER_ALWAYS)
    visit(wizard('/kms_jceks_config'))
  end
end