require_relative "../bootstrap"
require_relative "../utility"
include Utility

class Installation < Shoes

  url '/summary_uninstall',            :summary_uninstall

  def summary_uninstall 
    
    style(Link, :underline => false, :stroke => "#FFF", :weight => "bold")
    style(LinkHover, :underline => false, :stroke => "#FFF", :weight => "bold")

	$summary_hash["installation_dir"] = $item.installation_dir
	$summary_hash["data_dir"] = $item.data_dir
	$summary_hash["install_rms"] = $item.component_rms == "no" ? false : true
	$summary_hash["install_kms"] = $item.component_kms == "no" ? false : true
	summary_template = File.open(File.join(ENV['START_DIR'],"cookbooks/RMS/templates/default/summary_uninstall.erb")).read
	summary =  TemplateGenerator::render(summary_template, $summary_hash)

    stack @@installer_page_size_style do
      
      
      # The header area
      stack @@installer_header_size_style do

        background @@header_color_style
        banner

      end

      # The body
      stack @@installer_content_size_style do

        background @@content_color_style

        stack :width => 1.0, :height => 25 do
          para ReadableNames["ready_to_uninstall_page"]["heading"], @@heading_1_style
		  para " "
        end
		
		stack :width => 1.0, :height => 0 do
          para " "
        end
			
        stack :width => 1.0, :height => 25 do
          para ReadableNames["ready_to_uninstall_page"]["help_note"], @@help_note_style
        end
        
        stack :width => 1.0, :height => 0 do
          para summary, @@help_note_style
        end
        
		stack :width => 1.0, :height => 0 do
          para " "
        end
		
		stack :width => 1.0, :height => 20 do
		  flow :width => 1.0, :height => 200 do
			  para "", @@text_label_style.merge({:width => 10})
			  @delete_data_dir_check = check
			  para ReadableNames["ready_to_uninstall_page"]["delete_data_dir"], @@text_label_style.merge({:width => 800})
			end
		end
		
		stack :width => 1.0, :height => 0 do
          para " "
        end
		
		stack :width => 1.0, :height => 20 do
          para ReadableNames["ready_to_uninstall_page"]["help_note_2"], @@help_note_style
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

          summary_uninstall_back_proc = Proc.new{
            visit(wizard(:back))
          }

          stack :width => 50, :height => 1.0 do
            @ready_to_install_back_btn = nl_button :back
            @ready_to_install_back_btn.click { summary_uninstall_back_proc.call }
          end

          stack :width => 50, :height => 1.0 do
            @ready_to_install_back_text_btn = nl_button :back_text, :click => summary_uninstall_back_proc
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

          summary_uninstall_next_proc = Proc.new{
			if @delete_data_dir_check.checked?
				$item.delete_data_dir = "yes"
			else
				$item.delete_data_dir = ""
            end
            visit(wizard(:next))
          }

          stack :width => 50, :height => 1.0 do
            @ready_to_install_next_text_btn = nl_button :next_text, :click => summary_uninstall_next_proc
          end

          stack :width => 50, :height => 1.0 do
            @ready_to_install_next_btn = nl_button :next
            @ready_to_install_next_btn.click { summary_uninstall_next_proc.call }
          end
		  
		  keypress { |k| 
			summary_uninstall_next_proc.call if k == "\n"
			summary_uninstall_back_proc.call if k == "BackSpace"
		  }
        end
      end 
    end
  end
end

if __FILE__ == $0
  Shoes.app :title => ReadableNames["title"] , :width => 950, :height => 700 do
    win.set_window_position(Gtk::Window::POS_CENTER_ALWAYS)
    visit(wizard('/summary_uninstall'))
  end
end