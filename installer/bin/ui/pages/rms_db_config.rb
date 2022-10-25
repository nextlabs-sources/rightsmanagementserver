require_relative "../bootstrap"
require_relative "../utility"
require_relative "../../../cookbooks/RMS/libraries/utility"

include Utility

$skipped_kms = true

class Installation < Shoes

  url '/rms_db_config',             :rms_db_config

  def display_fields(update_port)
	if @db_type_select == "IN_BUILT" 
	  @db_host_name_flow.hide()
	  @db_port_flow.hide()
	  @db_name_flow.hide()
	  @db_username_flow.hide()
	  @db_password_flow.hide()
	  @db_conn_url_flow.hide()
	  @db_test_connection_flow.hide()
	  @db_test_connection_loading_stack.hide         
	  @in_built_port_flow.show() 
	  @in_built_password_flow.show()
	  @in_built_db_port_edit.text = Item::DB_PORT[@db_type_select]  if update_port
	else
	  @db_host_name_flow.show()
	  @db_port_flow.show()
	  @db_name_flow.show()
	  @db_username_flow.show()
	  @db_password_flow.show()
	  @db_conn_url_flow.show()
	  @db_test_connection_flow.show()
	  @db_test_connection_loading_stack.hide     
	  @in_built_port_flow.hide()
	  @in_built_password_flow.hide()
	  @db_port_edit.text = Item::DB_PORT[@db_type_select]  if update_port
	end   
  end
  
  def rms_db_config
	$item.rms_db_type  = Item::DB_FRIENDLY_NAMES_REVERSE_MAP.keys[0] if $item.rms_db_type==""
	$item.rms_db_port = Item::DB_PORT[$item.rms_db_type] if $item.rms_db_port==""
	
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
           para ReadableNames["db_config_page"]["heading_rms"], @@heading_1_style 
         end

         stack :width => 1.0, :height => 60, :margin_right => 10 do
          para ReadableNames["db_config_page"]["help_note_rms"],  @@help_note_style
         end
		 
          flow :width => 1.0, :height => 40 do
            stack :width => 200, :margin_left => 10, :margin_right => 10, 
                 :margin_top => 20, :height => 30 do
                para ReadableNames["inputs"]["rms_db_type"],  @@text_label_style
            end 	
			
            stack :width => 300, :margin_left => 10, :margin_right => 10, 
                 :margin_top => 20, :height => 40 do
              my_list_box = list_box(items: Item::DB_FRIENDLY_NAMES_MAP.keys,
                    width: 200, choose: Item::DB_FRIENDLY_NAMES_REVERSE_MAP[$item.rms_db_type])     			
              my_list_box.change do |list_box|
                choice = list_box.text
				list_box.move(210,220)		#bug in shoes messes with the layout
                @db_type_select = Item::DB_FRIENDLY_NAMES_MAP[choice]
				display_fields(true)                
              end
              #default values on page load             
              @db_type_select = $item.rms_db_type			
            end  

			@in_built_port_flow = flow :width => 1.0, :height => 40 do      
				stack :width => 200, :margin_left => 10, :margin_right => 10, 
				   :margin_top => 20, :height => 30 do
				  para ReadableNames["inputs"]["rms_db_port"],  @@text_label_style
				end
				stack :width => 300, :margin_left => 10, :margin_right => 10, 
				   :margin_top => 20, :height => 40 do
				  @in_built_db_port_edit = nl_text_edit( :short, text = $item.rms_db_port, { :width => 300 } )  
				end	
			end
			@in_built_port_flow.hide()
			@in_built_password_flow = flow :width => 1.0, :height => 40 do      
				stack :width => 200, :margin_left => 10, :margin_right => 10, 
					:margin_top => 20, :height => 30 do
					para ReadableNames["inputs"]["rms_db_password"],  @@text_label_style
				end
				stack :width => 300, :margin_left => 10, :margin_right => 10, 
					:margin_top => 20, :height => 40 do
					@in_built_db_password_edit = nl_text_edit( :short, text=$item.rms_db_password, { :width => 300, :secret => true } )
				end      
				stack :width => 300, :margin_left => 20, :margin_right => 10, 
					:margin_top => 20, :height => 30 do
					para ReadableNames["db_config_page"]["in_built_password_hint"],  @@text_hint_style
				end                  
			end
			@in_built_password_flow.hide()
			
          end                

        @db_host_name_flow = flow :width => 1.0, :height => 40 do      
           stack :width => 200, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["inputs"]["rms_db_host_name"],  @@text_label_style
           end
            stack :width => 300, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 40 do
              @db_host_name_edit = nl_text_edit( :short, text=$item.rms_db_host_name, { :width => 300 } )
              @db_host_name_edit.focus if @db_host_name_edit.text == ''
			  @db_host_name_edit.change do |text_box|
				@db_conn_url_edit.text = Utility::Config.buildDBProperties( @db_type_select,  @db_host_name_edit.text, @db_port_edit.text, @db_name_edit.text, @db_username_edit.text)["db_conn_url"]
			  end
            end
            stack :width => 300, :margin_left => 20, :margin_right => 10, 
             :margin_top => 20, :height => 30 do
              para ReadableNames["db_config_page"]["db_host_name_hint"],  @@text_hint_style
            end                  
        end

        @db_port_flow = flow :width => 1.0, :height => 40 do      
           stack :width => 200, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["inputs"]["rms_db_port"],  @@text_label_style
           end
            stack :width => 300, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 40 do
              @db_port_edit = nl_text_edit( :short, text = $item.rms_db_port, { :width => 300 } )  
            end
			@db_port_edit.change do |text_box|
				@db_conn_url_edit.text = Utility::Config.buildDBProperties( @db_type_select,  @db_host_name_edit.text, @db_port_edit.text, @db_name_edit.text, @db_username_edit.text)["db_conn_url"]
			end			
        end
		
        @db_name_flow = flow :width => 1.0, :height => 40 do 
           stack :width => 200, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["inputs"]["rms_db_name"],  @@text_label_style
           end
           stack :width => 300, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 40 do
            @db_name_edit = nl_text_edit( :short, text=$item.rms_db_name, { :width => 300 } )
			@db_name_edit.change do |text_box|
				@db_conn_url_edit.text = Utility::Config.buildDBProperties( @db_type_select,  @db_host_name_edit.text, @db_port_edit.text, @db_name_edit.text, @db_username_edit.text)["db_conn_url"]
			  end
           end
             stack :width => 300, :margin_left => 20, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
                para ReadableNames["db_config_page"]["db_name_hint"],  @@text_hint_style
             end      
        end

        @db_username_flow = flow :width => 1.0, :height => 40 do 
           stack :width => 200, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["inputs"]["rms_db_username"],  @@text_label_style
           end
           stack :width => 300, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 40 do
            @db_username_edit = nl_text_edit( :short, text=$item.rms_db_username, { :width => 300 } )
           end      
        end  

        @db_password_flow = flow :width => 1.0, :height => 40 do 
           stack :width => 200, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["inputs"]["rms_db_password"],  @@text_label_style
           end
           stack :width => 300, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 40 do
            @db_password_edit = nl_text_edit( :short, text=$item.rms_db_password, { :width => 300, :secret => true } )
           end      
        end

		@db_conn_url_flow = flow :width => 1.0, :height => 40 do	
           stack :width => 200, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 30 do
              para ReadableNames["inputs"]["rms_db_conn_url"],  @@text_label_style
           end
           stack :width => 300, :margin_left => 10, :margin_right => 10, 
               :margin_top => 20, :height => 40 do
            @db_conn_url_edit = nl_text_edit( :short, text=$item.rms_db_conn_url, { :width => 300 })
           end      
        end

        # Test connection process with validation
        db_config_testcon_proc = Proc.new{ 
            control_fields = {
              "rms_db_host_name" => @db_host_name_edit,
              "rms_db_port" => @db_port_edit,
              "rms_db_name" => @db_name_edit,
              "rms_db_username" => @db_username_edit,
              "rms_db_password" => @db_password_edit        
            }
			
            validated, field, error_msg = $item.validate_fields "rms_db_host_name", "rms_db_port", "rms_db_name"
            if !validated && @db_conn_url_edit.text == ""
              alert_ontop_parent(app.win, error_msg, :title => app.instance_variable_get('@title'))
              control_fields[field].focus
			elsif @db_username_edit.text == ""
				error_msg = ReadableNames["ErrorMsgTemplate"] % [ReadableNames["inputs"]["rms_db_username"], ReadableNames["validator_errors"]["non_empty"]]
				alert_ontop_parent(app.win, error_msg, :title => app.instance_variable_get('@title'))
				@db_username_edit.focus			
            else
              begin                                                                                                  
                db_prop = Utility::Config.buildDBProperties(@db_type_select, @db_host_name_edit.text, @db_port_edit.text, @db_name_edit.text, @db_username_edit.text)
				jdbc_url = @db_conn_url_edit.text=="" ?  db_prop['db_conn_url'] : @db_conn_url_edit.text
                db_connection_result = Utility::DB.test_db_connection(
                    jdbc_url, @db_username_edit.text, @db_password_edit.text, 30
                  )                  
              rescue Exception => e
                db_connection_result = false
                msg = ReadableNames["db_config_page"]["test_connnection_failed_template"] % e.message
              end

              if db_connection_result
                msg = ReadableNames["db_config_page"]["test_connnection_success"]
              else
                msg = (ReadableNames["db_config_page"]["test_connnection_failed_template"] % '') if msg == nil
              end

              alert_ontop_parent(app.win, msg, :title => app.instance_variable_get('@title'))
            end  
        }

		@db_test_connection_flow = flow :width => 1.0, :height => 40 do
			stack :width => 100, :margin_left => 300, :margin_right => 10, :margin_top => 20, :height => 1.0 do
				nl_button :test, :click => Proc.new {
					@testing_connection=true
					@db_test_connection_loading_stack.show			
				}	
			end
			@db_test_connection_loading_stack = stack :width => 10, :margin_left => 330, :margin_right => 10, :margin_top => 20, :height => 1.0 do
				image File.join __FILE__,"../../images/loading.gif"
			end
			@db_test_connection_loading_stack.hide
		end

		unless($item.component_kms == "no")
		   flow :width => 1.0, :height => 40 do
              stack :width => 15, :margin_left => 10, :margin_right => 10,
                  :margin_top => 15, :height => 20 do
                @db_reuse_settings_check = check
                @db_reuse_settings_check.checked = $skipped_kms
              end
                @db_reuse_settings_text_btn = nl_button :reuse
                @db_reuse_settings_text_btn.click {
                    @db_reuse_settings_check.checked = @db_reuse_settings_check.checked?() ? false : true;
				}
           end
        end
		
		display_fields(false)

		#polling because Shoes refresh the UI at the end of the process execution and loading gif cannot be seen
		@testing_connection = false
		every 1 do 
			if @testing_connection
				@testing_connection = false
				db_config_testcon_proc.call
				@db_test_connection_loading_stack.hide
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

          db_config_back_proc = Proc.new{
            visit(wizard(:back))
          }

          stack :width => 50, :height => 1.0 do
            @db_config_back_btn = nl_button :back
            @db_config_back_btn.click { db_config_back_proc.call }
          end

          stack :width => 50, :height => 1.0 do
            @db_config_back_text_btn = nl_button :back_text, :click => db_config_back_proc
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

          # Validaton process before going to next page
          db_config_next_proc = Proc.new{            

            control_fields = {
              "rms_db_host_name" => @db_host_name_edit,
              "rms_db_port" => @db_port_edit,
              "rms_db_name" => @db_name_edit,
              "rms_db_username" => @db_username_edit,
              "rms_db_password" => @db_password_edit        
            }
			$skipped_kms = (defined?(@db_reuse_settings_check) && @db_reuse_settings_check.checked?()) ? true : false;			
			
			$item.rms_db_type = @db_type_select
            $item.rms_db_host_name = @db_host_name_edit.text
            $item.rms_db_port = @db_port_edit.text
            $item.rms_db_name = @db_name_edit.text
            $item.rms_db_username = @db_username_edit.text
            $item.rms_db_password = @db_password_edit.text
			$item.rms_db_conn_url = @db_conn_url_edit.text

            if @db_type_select != "IN_BUILT"
			  validated, field, error_msg = $item.validate_fields "rms_db_host_name", "rms_db_port", "rms_db_name"
              if !validated && @db_conn_url_edit.text == ""
                alert_ontop_parent(app.win, error_msg, :title => app.instance_variable_get('@title'))
                control_fields[field].focus
              else
				validated, field, error_msg = $item.validate_fields "rms_db_username"
				if !validated
					alert_ontop_parent(app.win, error_msg, :title => app.instance_variable_get('@title'))
					control_fields[field].focus
				else
					if $skipped_kms
					  $item.kms_db_type = @db_type_select
					  $item.kms_db_host_name = @db_host_name_edit.text
					  $item.kms_db_port = @db_port_edit.text
					  $item.kms_db_name = @db_name_edit.text
					  $item.kms_db_username = @db_username_edit.text
					  $item.kms_db_password = @db_password_edit.text
					  $item.kms_db_conn_url = @db_conn_url_edit.text
					  visit(jump(2))
					else
					  visit(wizard(:next))
					end
				end
              end
            else
				$item.rms_db_type = @db_type_select
				$item.rms_db_port = @in_built_db_port_edit.text
 				$item.rms_db_password = @in_built_db_password_edit.text
				$item.rms_db_host_name = ""
				$item.rms_db_name = ""
				$item.rms_db_username = ""
				$item.rms_db_conn_url = ""
				
				if !Utility::TCP.is_valid_port($item.rms_db_port)
				  error_msg = ReadableNames["ErrorMsgTemplate"] % [ReadableNames["inputs"]["rms_db_port"], ReadableNames["validator_errors"]["port"]]
				  alert_ontop_parent(app.win, error_msg, :title => app.instance_variable_get('@title'))
				  @in_built_db_port_edit.focus
				elsif !Utility::TCP.is_port_available($item.rms_db_port)
				  alert_ontop_parent(app.win, "Port #{$item.rms_db_port} is not available. Please choose another port.", :title => app.instance_variable_get('@title'))
				  @in_built_db_port_edit.focus
				elsif @in_built_db_password_edit.text == ""
				  error_msg = ReadableNames["ErrorMsgTemplate"] % [ReadableNames["inputs"]["rms_db_password"], ReadableNames["validator_errors"]["non_empty"]]
				  alert_ontop_parent(app.win, error_msg, :title => app.instance_variable_get('@title'))
				  @in_built_db_password_edit.focus
				else
				  if $skipped_kms
				  	$item.kms_db_type = @db_type_select
					$item.kms_db_port = @in_built_db_port_edit.text
					$item.kms_db_password = @in_built_db_password_edit.text
					$item.kms_db_host_name = ""
					$item.kms_db_name = ""
					$item.kms_db_username = ""
					$item.kms_db_conn_url = ""
					visit(jump(2))
				  else
					visit(wizard(:next))
				  end
				end
			end
          }

          stack :width => 50, :height => 1.0 do
            @service_port_next_text_btn = nl_button :next_text, :click => db_config_next_proc
          end

          stack :width => 50, :height => 1.0 do
            @service_port_next_btn = nl_button :next
            @service_port_next_btn.click { db_config_next_proc.call }
          end
      
      keypress { |k| 
      db_config_next_proc.call if k == "\n"
      }
        end
      end
    end
  end
end

if __FILE__ == $0
  Shoes.app :title => ReadableNames["title"] , :width => 950, :height => 700 do
    win.set_window_position(Gtk::Window::POS_CENTER_ALWAYS)
    visit(wizard('/rms_db_config'))
  end
end