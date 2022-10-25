require_relative "../bootstrap"
require_relative "../utility"
include Utility

class Installation < Shoes

  url '/agreement',    :agreement
  
  def agreement
    # changes the link style and link hover style
    # this method changes the link style and link hover style for the whole application
    style(Link, :underline => false, :stroke => "#FFF", :weight => "bold")
    style(LinkHover, :underline => false, :stroke => "#FFF", :weight => "bold")

    # The header
    stack @@installer_header_size_style do
      background @@header_color_style
      banner
    end
    
    # The agreement area
    stack @@installer_content_size_style do
      @agreement_area = edit_box :width => 1.0, :height => 1.0, :state => "disabled"
      agreement_file = File.join(File.dirname(File.dirname(__FILE__)), "agreement.txt")
      @agreement_area.text = File.exist?(agreement_file) ? File.read(agreement_file) :
          "Error loading agreement"
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

        agreement_back_proc = Proc.new {
          visit(wizard(:back))
        }

        stack :width => 50, :height => 1.0 do
          @agreement_back_btn = nl_button :back
          @agreement_back_btn.click { agreement_back_proc.call }
        end

        stack :width => 200, :height => 1.0 do
          
          # This is a vertical spacer
          stack :width => 1.0, :height => 5 do
            para " "
          end

          para( link ReadableNames["agreement_page"]["reject_message"] {
            agreement_back_proc.call
            }
          )
        end

        # This is a horizontal spacer
        stack :width => 390, :height => 1.0 do
          para " "
        end

        agreement_next_proc = Proc.new {
          visit(wizard(:next))
        }
        stack :width => 200, :height => 1.0 do
          para (link ReadableNames["agreement_page"]["accept_message"] { agreement_next_proc.call }),
              @@btn_help_text_style.merge({:align => "right", :margin_top => 0})
        end
        stack :width => 50, :height => 1.0 do
          @agreement_next_btn = nl_button :next
          @agreement_next_btn.click { agreement_next_proc.call }
        end
		keypress { |k| 
		  agreement_next_proc.call if k == "\n"
		  agreement_back_proc.call if k == "BackSpace"
		}
      end
    end
  end
end

if __FILE__ == $0
  Shoes.app :title => ReadableNames["title"] , :width => 950, :height => 700 do
    win.set_window_position(Gtk::Window::POS_CENTER_ALWAYS)
    visit(wizard('/agreement'))
  end
end
