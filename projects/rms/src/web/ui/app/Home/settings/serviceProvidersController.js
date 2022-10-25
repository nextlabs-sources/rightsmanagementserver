mainApp.controller('serviceProvidersController',['$scope','$rootScope','$state', 'networkService','configService','loggerService','dialogService','settingsService','serviceProviderService','$location', '$filter',
					function($scope,$rootScope,$state,networkService,configService,loggerService,dialogService,settingsService,serviceProviderService,$location, $filter){
	$scope.isLoading = false;
	$rootScope.$on("reloadSettings", function(){
       $scope.loadSettings();
    });
  	$scope.loadSettings = function() {
		$scope.isLoading = true;
		serviceProviderService.loadSettings(function(data){
	    	$scope.serviceProviderSettingsMap={};
			$scope.supportedProviders=[];
			$scope.remainingServiceProviders=[];
			$scope.crossLaunchProviders=[];
	    	$scope.isLoading = false;
	    	$scope.serviceProviderSettingsMap=data["serviceProviderSettingList"];	  
	    	$scope.supportedProviders=data["supportedProvidersMap"];
	    	$scope.redirectUrl=data["redirectUrl"];
	    	// var supportedProvidersKey=Object.keys($scope.supportedProviders);
	    	Object.keys($scope.supportedProviders).forEach(function(key,index) {
			    var found = false;
	    		for(var j=0; j<$scope.serviceProviderSettingsMap.length;j++){
	    			if($scope.serviceProviderSettingsMap[j].providerType == key){
	    				found = true;
	    				break;
	    			}
	    		}
	    		if(!found){
	    			$scope.remainingServiceProviders.push({"providerType":key,"providerTypeDisplayName":$scope.supportedProviders[key]});
	    		} 
			});
			Object.keys(data["crossLaunchProvidersMap"]).forEach(function(key,index) {
	    		$scope.crossLaunchProviders.push({"providerType":key,"providerTypeDisplayName":data["crossLaunchProvidersMap"][key]});
	    	});
	    	$scope.isLoading = false;
    	});
   	}
	
	$scope.openConfigModal = function(settings,add){
		$scope.dismissMessage();
    	dialogService.repositoryConfig({
		    "settings":angular.copy(settings),
		    "redirectUrl":$scope.redirectUrl
		},add);
	}

	$scope.unconfigure = function(settings){
		$scope.dismissMessage();
		var confirm = window.confirm($filter('translate')('service_provider.delete.confirm'));
		if(!confirm){
			return;
		}

        serviceProviderService.removeServiceProviderSettings(settings.id,function(data){
          if(data.result){
        	serviceProviderService.setMessageParams(true, true, data.message);
        	$rootScope.$emit("refreshRepoList"); 
          }else{
            serviceProviderService.setMessageParams(true, false, data.message);
          }
        });
	}

	$scope.downloadApp = function(setting){
		$scope.dismissMessage();
		providerType = setting.providerType;
        serviceProviderService.downloadCrossLaunchApp(providerType, setting.id); 
	}

	$scope.getMessage = function() {
		return serviceProviderService.getMessage();
	}

	$scope.shouldDisplay = function() {
		return serviceProviderService.shouldDisplay();
	}

	$scope.isSuccess = function() {
		return serviceProviderService.isSuccess();
	}

	$scope.dismissMessage = function() {
		serviceProviderService.setMessageParams(false, false, null);
	}

	$scope.dismissMessage();
	$scope.loadSettings();
}]);
