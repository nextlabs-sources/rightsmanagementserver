require_relative "../bootstrap"
require_relative "../utility"
include Utility

class Installation < Shoes

  url '/welcome',             :welcome
  
  def welcome
    stack @@installer_page_size_style do
      background @@header_color_style
	  
      # Then welcome paragrah
      stack :width => 1.0, :height => 200 do
		msg = if $installMode == "uninstall" then "Uninstallation" else "Installation" end
        para ReadableNames["welcome_page"]["welcome_para"] % msg, @@heading_1_style.merge({:align => "center", 
            :margin_top => 150, :size => 15})
      end
	  
	  flow :width => 1.0, :height => 50 , :margin_top => 10 do
	   #Calcualting offset to center the logo (150x22). Shoes doesnt support :align for images yet
       offset = (app.width - 150)/2   
		#this is a horizontal spacer
        stack :width => offset, :height => 1.0  do
          para " "
        end
        stack :width => 150, :height => 22 do
          image(File.join(File.dirname(__FILE__), "../images/Logo-NextLabs-White.png"))
        end
      end
	  
      # Then product name
      stack :width => 1.0, :height => 80 do
        para ReadableNames["common"]["product_name"], @@heading_1_style.merge({:align => "center", 
            :size => 17})
      end
      # Then the next button
      flow :width => 1.0, :height => 60 do
        # This is a horizontal spacer  
        stack :width => 450, :height => 60 do
          para " "
        end
        # This is the container for next button
        stack :width => 50, :height => 50 do
		  welcome_next_proc = Proc.new {
			if ($installMode == "uninstall" && !Utility::Config.is_RMS_installed? && !Utility::Config.is_KMS_installed?)
				visit(wizard('/finish'))
			else
				visit(wizard(:next))
			end
		  }
          @welcome_next_btn = nl_button :next
          @welcome_next_btn.click { welcome_next_proc.call }
		  keypress { |k| welcome_next_proc.call if k == "\n"}
        end
      end
      # Then the copyright info
	  flow @@installer_footer_size_style.merge({:margin_top => 20})  do
		stack :width=>0.2 do
		end
		stack :width=>0.6 do
			para ReadableNames["welcome_page"]["copyright_message"],  @@copyright_label_style
		end
		stack :width=>0.2 do
		end
      end
    end	
  end
end

if __FILE__ == $0
  Shoes.app :title => ReadableNames["title"] , :width => 950, :height => 700 do
    win.set_window_position(Gtk::Window::POS_CENTER_ALWAYS)
	
    visit('/welcome')
  end
end