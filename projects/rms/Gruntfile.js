module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
	emberTemplates: {
	  compile: {
		options: {
		  amd: false,
		  templateBasePath: "web/js/rms/templates"
		},
		files: {
		  "web/js/rmstemplates.js": "web/js/rms/templates/**/*.hbs"
		}
	  }
	}
  });

  grunt.loadNpmTasks('grunt-ember-templates');

  // Default task(s).
  grunt.registerTask('default', 'emberTemplates');

};