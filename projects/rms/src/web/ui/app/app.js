var STATE_HOME = "Home";
var STATE_REPOSITORIES = "Home.Repositories";
var STATE_ALL_REPOSITORIES = "Home.AllRepositories";
var STATE_MANAGE_REPOSITORIES = "Home.ManageRepositories";
var STATE_DOWNLOAD_RMC = "Home.DownloadRMC";
var STATE_SETTINGS = "Home.Settings";
var STATE_SERVICE_PROVIDERS = "Home.ServiceProviders";
var STATE_KEY_MANAGEMENT_SETTINGS = "Home.KeyManagementSettings";

var mainApp = angular.module('mainApp',['ui.router','ui.bootstrap','uiSwitch','ngSanitize','pascalprecht.translate','templates-main', 'ngJsTree','ngFileUpload','angularResizable','ngAnimate']);

mainApp.config(['$stateProvider','$urlRouterProvider','$translateProvider','$httpProvider', '$uibTooltipProvider', function($stateProvider, $urlRouterProvider,$translateProvider, $httpProvider, $uibTooltipProvider) {
    
   
    $urlRouterProvider.otherwise('/home/repositories');
    $stateProvider
        .state('Home', {
            url: '/home',
            views: {
            	
            	'': { 
                	templateUrl: 'ui/app/Home/home.html'
                },
            	
            	'menuView@Home': { 
                	templateUrl: 'ui/app/Home/menu.html'
                },
            	
                'contentView@Home': { 
                	templateUrl: 'ui/app/Home/content.html'
                }
            }
        })
        .state(STATE_REPOSITORIES, {        	
        	url: '/repositories/:repoId',
            views: {
            	
                'mainView@Home': { 
                	templateUrl: 'ui/app/Home/Repositories/fileList.html'
                }
            },
            params:{
            	repoName: null,
            	folder: null,
            	repoType: null
            }
        })
        .state(STATE_ALL_REPOSITORIES, {        	
        	url: '/repositories',
            views: {
            	
                'mainView@Home': { 
                	templateUrl: 'ui/app/Home/Repositories/fileList.html'                	
                }        
            },
	        params: {
	    		all: true
	    	}
        })
        .state(STATE_MANAGE_REPOSITORIES, {            
            url: '/manageRepositories',
            views: {
                
                'mainView@Home': { 
                    templateUrl: 'ui/app/Home/Repositories/manageRepositories.html'                  
                }        
            }
        })
        .state(STATE_DOWNLOAD_RMC, {
        	url: '/downloadRMC',
        	views: {
        		'mainView@Home': { 
                	templateUrl: 'ui/app/Home/downloadRMC.html'
                }
        	}
        })
        .state(STATE_SETTINGS, {        	
        	url: '/settings',
            views: {
            	
                'mainView@Home': { 
                	templateUrl: 'ui/app/Home/settings.html'
                }
            }
		})
		.state(STATE_SERVICE_PROVIDERS, {        	
        	url: '/serviceProviders',
            views: {
            	
                'mainView@Home': { 
                	templateUrl: 'ui/app/Home/settings/serviceProviders.html'
                }
            }
        }).state(STATE_KEY_MANAGEMENT_SETTINGS, {        	
        	url: '/kmSettings',
            views: {
            	
                'mainView@Home': { 
                	templateUrl: 'ui/app/Home/settings/kmSettings.html'
                }
            }
        })
        
    $translateProvider.useStaticFilesLoader({
		          prefix: 'ui/app/i18n/',
		          suffix: '.json'
        	});
	$translateProvider.preferredLanguage('en');
	// Enable escaping of HTML
	$translateProvider.useSanitizeValueStrategy('sanitize'); 
	$httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';

    $uibTooltipProvider.options({
      appendToBody: true      
    });        

}]);   

mainApp.run(['initSettingsService', '$rootScope', function(initSettingsService, $rootScope){
    //initSettingsData and CONTEXT_PATH are initialized in index.jsp
	initSettingsService.loadSettings(initSettingsData);
	initSettingsService.setRMSContextName(CONTEXT_PATH);
	$rootScope.$on('$viewContentLoaded', function(event, toState, toParams, fromState, fromParams){		
		initSettingsService.setRightPanelMinHeight();
	});
}]);

