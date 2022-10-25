module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
    uglify: {
      options: {
        banner: ''
      },
      build: {
        src: 'build/app.min.js',
        dest: 'build/deploy-app.js'
      }
    },
    html2js: {
      options: {
        // custom options
        base:'../'
      },
      main: {
        src: ['app/**/*.html'],
        dest: 'build/templates.js'
      },
    },
    concat: {
      options: {
        // define a string to put between each file in the concatenated output
        separator: ';'
      },
      dist: {
        // the files to concatenate
        src: ['app/app.js',

              'app/Login/*.js',
               
              'app/**/*.js',
			  
			  '!app/viewers/*'
             ],
        // the location of the resulting JS file
        dest: 'build/app.min.js'
      }
    },
    copy: {
      main: {
        files: [

          // includes files within path and its sub-directories
          {expand: true, src: ['lib/**'], dest: 'deploy/ui/'},
          {expand: true, src: ['css/**'], dest: 'deploy/ui/'},
          {expand: true, src: ['config/**'], dest: 'deploy/ui/'},
          {flatten: true, src: ['app/i18n/*.json'], dest: 'deploy/ui/'},
		  {expand: true, src: ['app/viewers/**'], dest: 'deploy/ui/'},
		  {expand: true, src: ['app/**'], dest: 'deploy/ui/'},
          {flatten: true, src: ['build/deploy-app.js'], dest: 'deploy/ui/app.js',filter:'isFile'},
          {flatten: true, src: ['build/templates.js'], dest: 'deploy/ui/app/templates.js',filter:'isFile'},
          {flatten: true, src: ['../index-dev.jsp'], dest: 'deploy/index.jsp', filter:'isFile'},
		  {flatten: true, src: ['../CADViewer.jsp'], dest: 'deploy/CADViewer.jsp', filter:'isFile'},
		  {flatten: true, src: ['../login.jsp'], dest: 'deploy/login.jsp', filter:'isFile'},
		  {flatten: true, src: ['../ExternalViewer.jsp'], dest: 'deploy/ExternalViewer.jsp', filter:'isFile'},
		  {flatten: true, src: ['../OpenLink.jsp'], dest: 'deploy/OpenLink.jsp', filter:'isFile'},
		  {flatten: true, src: ['../register.jsp'], dest: 'deploy/register.jsp', filter:'isFile'},
		  {flatten: true, src: ['../SharepointApp.jsp'], dest: 'deploy/SharepointApp.jsp', filter:'isFile'},
		  {flatten: true, src: ['../ShowError.jsp'], dest: 'deploy/ShowError.jsp', filter:'isFile'},
		  {flatten: true, src: ['../DocViewer.jsp'], dest: 'deploy/DocViewer.jsp', filter:'isFile'},
		  {flatten: true, src: ['../RHViewer.jsp'], dest: 'deploy/RHViewer.jsp', filter:'isFile'},
		  {flatten: true, src: ['../VDSViewer.jsp'], dest: 'deploy/VDSViewer.jsp', filter:'isFile'},
		  {flatten: true, src: ['../error_404.jsp'], dest: 'deploy/error_404.jsp', filter:'isFile'},
		  {flatten: true, src: ['../error_403.jsp'], dest: 'deploy/error_403.jsp', filter:'isFile'},
		  {flatten: true, src: ['../error_500.jsp'], dest: 'deploy/error_500.jsp', filter:'isFile'}
        ]
      }
    }
  });

  // Load the plugin that provides the "uglify" task.
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-html2js');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-copy');

  // Default task(s).
  grunt.registerTask('default', ['html2js','copy']);

};