require 'erb'
require 'ostruct'

class TemplateGenerator < OpenStruct
  def self.render(t, h)
    TemplateGenerator.new(h).render(t)
  end

  def render(template)
    ERB.new(template, nil, '-').result(binding)
  end
end
