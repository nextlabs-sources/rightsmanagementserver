#! /usr/bin/env ruby
# encoding: utf-8
#
require_relative "../bootstrap"
require_relative "../utility"
include Utility

class Installation < Shoes

  url '/summary',            :summary

  def summary 
    
    style(Link, :underline => false, :stroke => "#FFF", :weight => "bold")
    style(LinkHover, :underline => false, :stroke => "#FFF", :weight => "bold")

	$summary_hash["installation_dir"] = $item.installation_dir
	$summary_hash["data_dir"] = $item.data_dir
	$summary_hash["rms_ssl_port"] = $item.rms_ssl_port
	$summary_hash["rms_shutdown_port"] = $item.rms_shutdown_port
	$summary_hash["rmi_km_port"] = $item.rmi_km_port
	$summary_hash["icenet_server"] = $item.icenet_server
	$summary_hash["install_rms"] = $item.component_rms == "no" ? false : true
	$summary_hash["install_kms"] = $item.component_kms == "no" ? false : true
	$summary_hash["rms_installed"] =  Utility::Config.is_RMS_installed?
	$summary_hash["km_server"] = $item.km_server
	summary_template = File.open(File.join(ENV['START_DIR'],"cookbooks/RMS/templates/default/summary.erb")).read
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
          para ReadableNames["ready_to_install_page"]["heading"], @@heading_1_style
        end

        stack :width => 1.0, :height => 25 do
          para ReadableNames["ready_to_install_page"]["help_note"], @@help_note_style
        end
        
        stack :width => 1.0, :height => 340 do
          para summary, @@help_note_style
        end
        
        stack :width => 1.0, :height => 50 do
          para ReadableNames["ready_to_install_page"]["help_note_2"], @@help_note_style
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

          summary_back_proc = Proc.new{
            visit(wizard(:back))
          }

          stack :width => 50, :height => 1.0 do
            @ready_to_install_back_btn = nl_button :back
            @ready_to_install_back_btn.click { summary_back_proc.call }
          end

          stack :width => 50, :height => 1.0 do
            @ready_to_install_back_text_btn = nl_button :back_text, :click => summary_back_proc
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

          summary_next_proc = Proc.new{
            visit(wizard(:next))
          }

          stack :width => 50, :height => 1.0 do
            @ready_to_install_next_text_btn = nl_button :next_text, :click => summary_next_proc
          end

          stack :width => 50, :height => 1.0 do
            @ready_to_install_next_btn = nl_button :next
            @ready_to_install_next_btn.click { summary_next_proc.call }
          end
		  keypress { |k| 
			summary_next_proc.call if k == "\n"
			summary_back_proc.call if k == "BackSpace"
		  }
        end
      end 
    end
  end
end

if __FILE__ == $0
  Shoes.app :title => ReadableNames["title"] , :width => 950, :height => 700 do
    win.set_window_position(Gtk::Window::POS_CENTER_ALWAYS)
    visit(wizard('/summary'))
  end
end