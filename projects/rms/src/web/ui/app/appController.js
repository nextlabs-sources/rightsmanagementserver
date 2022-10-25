mainApp.controller('appController',['$scope','networkService','configService','loggerService','$location', 'initSettingsService',
function($scope,networkService,configService,loggerService,$location, initSettingsService){
		configService.setConfig(MainAppConfig);		
		var settings = initSettingsService.getSettings();
		$scope.rmsVersion = settings.rmsVersion;
		$scope.isRMCConfigured = settings.isRMCConfigured;
		$scope.userName = settings.userName;
		$scope.copyrightYear = settings.copyrightYear;
		
		$scope.$watch(function(){
			$( window ).resize(function() {
			  initSettingsService.setRightPanelMinHeight();
			});
		}); 
}]);