require_relative "../bootstrap"
require_relative "../utility"
require_relative "../../../cookbooks/RMS/libraries/utility"

include Utility

class Installation < Shoes

  url '/ad_config',             :ad_config

  def ad_config

	# Prepopulate AD settings and KMS_URL
	$item.readRMSConfig if $item.ldap_type==""
	$item.ldap_type  = Item::LDAP_FRIENDLY_NAMES_REVERSE_MAP.keys[0] if $item.ldap_type==""
  
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
           para ReadableNames["ad_config_page"]["heading"], @@heading_1_style 
         end

         stack :width => 1.0, :height => 60, :margin_right => 10 do
          para ReadableNames["ad_config_page"]["help_note"],  @@help_note_style
         end
		
        flow :width => 1.0, :height => 40 do
                stack :width => 200, :margin_left => 10, :margin_right => 10, 
                    :margin_top => 20, :height => 30 do
                    para ReadableNames["inputs"]["ldap_type"],  @@text_label_style
                end 	
                stack :width => 300, :margin_left => 10, :margin_right => 10, 
                    :margin_top => 20, :height => 40 do
                  my_list_box = list_box(items: Item::LDAP_FRIENDLY_NAMES_MAP.keys,
                        width: 190, choose: Item::LDAP_FRIENDLY_NAMES_REVERSE_MAP[$item.ldap_type])
                  my_list_box.change do |list_box|
                    choice = list_box.text                
                    @ldap_type_select = Item::LDAP_FRIENDLY_NAMES_MAP[choice]                           
                  end                                
                end 
				@ldap_type_select = $item.ldap_type			   
         end            
              
		
        flow :width => 1.0, :height => 40 do      
           stack :width => 200, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["inputs"]["ldap_host_name"],  @@text_label_style
           end
           stack :width => 300, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 40 do
            @ldap_host_name_edit = nl_text_edit( :short, text=$item.ldap_host_name, { :width => 300 } )
			@ldap_host_name_edit.focus if @ldap_host_name_edit.text == ''
           end
		   stack :width => 300, :margin_left => 20, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["ad_config_page"]["host_name_hint"],  @@text_hint_style
           end			   
        end 

        flow :width => 1.0, :height => 40 do 
           stack :width => 200, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["inputs"]["ldap_domain"],  @@text_label_style
           end
           stack :width => 300, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 40 do
            @ldap_domain_edit = nl_text_edit( :short, text=$item.ldap_domain, { :width => 300 } )
           end
		   stack :width => 300, :margin_left => 20, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["ad_config_page"]["domain_hint"],  @@text_hint_style
           end	
        end
		flow :width => 1.0, :height => 40 do 
           stack :width => 200, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["inputs"]["ldap_search_base"],  @@text_label_style
           end
           stack :width => 300, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 40 do
            @ldap_search_base_edit = nl_text_edit( :short, text=$item.ldap_search_base, { :width => 300 } )
           end
		   stack :width => 300, :margin_left => 20, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["ad_config_page"]["search_base_hint"],  @@text_hint_style
           end	
        end 
		flow :width => 1.0, :height => 40 do 
           stack :width => 200, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["inputs"]["ldap_user_group"],  @@text_label_style
           end
           stack :width => 300, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 40 do
            @ldap_user_group_edit = nl_text_edit( :short, text=$item.ldap_user_group, { :width => 300 } )
           end
		   stack :width => 300, :margin_left => 20, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["ad_config_page"]["user_group_hint"],  @@text_hint_style
           end	
        end 
		flow :width => 1.0, :height => 40 do 
           stack :width => 200, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["inputs"]["ldap_admin"],  @@text_label_style
           end
           stack :width => 300, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 40 do
            @ldap_admin_edit = nl_text_edit( :short, text=$item.ldap_admin, { :width => 300 } )
		   end
		   stack :width => 300, :margin_left => 20, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["ad_config_page"]["admin_hint"],  @@text_hint_style
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

          ad_config_back_proc = Proc.new{
            visit(wizard(:back))
          }

          stack :width => 50, :height => 1.0 do
            @ad_config_back_btn = nl_button :back
            @ad_config_back_btn.click { ad_config_back_proc.call }
          end

          stack :width => 50, :height => 1.0 do
            @ad_config_back_text_btn = nl_button :back_text, :click => ad_config_back_proc
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

          ad_config_next_proc = Proc.new{

            control_fields = {
              "ldap_host_name" => @ldap_host_name_edit,
			  "ldap_domain" => @ldap_domain_edit,
			  "ldap_search_base" => @ldap_search_base_edit,
			  "ldap_user_group" => @ldap_user_group_edit,
			  "ldap_admin" => @ldap_admin_edit
            }
            $item.ldap_type=@ldap_type_select
            $item.ldap_host_name = @ldap_host_name_edit.text
            $item.ldap_domain = @ldap_domain_edit.text
			$item.ldap_search_base = @ldap_search_base_edit.text
            $item.ldap_user_group = @ldap_user_group_edit.text
            $item.ldap_admin = @ldap_admin_edit.text

            validated, field, error_msg = $item.validate_fields "ldap_host_name", "ldap_domain", "ldap_search_base", "ldap_admin"
	
            if ! validated
              alert_ontop_parent(app.win, error_msg, :title => app.instance_variable_get('@title'))
              control_fields[field].focus
            else
			  visit(wizard(:next))
            end

          }

          stack :width => 50, :height => 1.0 do
            @service_port_next_text_btn = nl_button :next_text, :click => ad_config_next_proc
          end

          stack :width => 50, :height => 1.0 do
            @service_port_next_btn = nl_button :next
            @service_port_next_btn.click { ad_config_next_proc.call }
          end
		  
		  keypress { |k| 
			ad_config_next_proc.call if k == "\n"
		  }
        end
      end
    end
  end
end

if __FILE__ == $0
  Shoes.app :title => ReadableNames["title"] , :width => 950, :height => 700 do
    win.set_window_position(Gtk::Window::POS_CENTER_ALWAYS)
    visit(wizard('/ad_config'))
  end
end