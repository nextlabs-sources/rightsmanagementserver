mainApp.controller('loginController',['$scope','$rootScope','$state','$http','$filter','dialogService','initSettingsService', 
	function($scope,$rootScope,$state,$http,$filter,dialogService, initSettingsService){
		
		var rmsContextName = initSettingsService.getRMSContextName();
		$scope.toggleSideBar = function(){
			$rootScope.$emit("toggleSideBar");
		}
		$scope.doLogout = function(){
			window.location=initSettingsService.getLogoutUrl();
		};
		
		$scope.getLoggedInUserName = function(){
			var initData = initSettingsService.getSettings();
			return (initData == null? "" : initData.userName); 
		};

		$scope.help = function(){
			var currentStateName = $state.current.name;
			var url = initSettingsService.getOnlineHelpPage("Repositories");
			if(currentStateName === STATE_REPOSITORIES){
				url = initSettingsService.getOnlineHelpPage("Repositories");
			} else if(currentStateName === STATE_MANAGE_REPOSITORIES){
				url = initSettingsService.getOnlineHelpPage("ManageRepositories");
			} else if(currentStateName === STATE_SETTINGS){
				url = initSettingsService.getOnlineHelpPage("Configuration");
			} else if(currentStateName === STATE_SERVICE_PROVIDERS){
				url = initSettingsService.getOnlineHelpPage("ServiceProviders");
			} else if(currentStateName === STATE_KEY_MANAGEMENT_SETTINGS){
				url = initSettingsService.getOnlineHelpPage("KeyManagement");
			}
			url=rmsContextName+'/help/'+url;
			openCenteredPopup(url,'myWindow','800','600','yes');
		}

		function openCenteredPopup(url,winName,w,h,scroll){
		    LeftPosition = (screen.width) ? (screen.width-w)/2 : 0;
		    TopPosition = (screen.height) ? (screen.height-h)/2 : 0;
		    settings = 'height='+h+',width='+w+',top='+TopPosition+',left='+LeftPosition+',scrollbars='+scroll+',resizable';
		    popupWindow = window.open(url,winName,settings);
		    popupWindow.focus();
		}
}
])