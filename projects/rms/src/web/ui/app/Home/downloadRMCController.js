mainApp.controller('downloadRMCController',['$scope','$rootScope','$filter','$state','networkService','$location','initSettingsService',
function($scope,$rootScope,$filter,$state,networkService,$location,initSettingsService){
	
	$scope.keyrings = [];
	$scope.selected_keyring;
	$scope.kmProvider;
	$scope.message = "";
	$scope.isLoading = false;
	
	$scope.dismissStatus = function() {
		$scope.messageStatus=0;
	}
	
	var headers = {'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8'};
	var RMS_CONTEXT_NAME = initSettingsService.getRMSContextName();
	
	$scope.downloadRMC = function(){
	  networkService.post(RMS_CONTEXT_NAME+"/RMSViewer/DownloadRMCPackage", null, headers, function(data){  
	    if (data!=null && data.result == true) {
		  if(data.filePath != null) {
			window.location.href = data.filePath;
			$scope.messageStatus=0;
		  } else {
			handleError(data.message);
		  }
	    }
	    else {
	      handleError($filter('translate')('download.rmc.error'));
	    }
	  });
	}
	
	function handleError(message){
		$scope.isLoading = false;
		$scope.messageStatus = 1;
		if(message == null) {
			$scope.message = $filter('translate')('download.rmc.error');
		} else {
			$scope.message = message;
		}
	}
}]);