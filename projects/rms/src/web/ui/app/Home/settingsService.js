mainApp.factory('settingsService', ['$http', 'networkService', 'configService', 'loggerService', 'initSettingsService', function($http, networkService, configService, loggerService, initSettingsService){
 
  var RMS_CONTEXT_NAME = initSettingsService.getRMSContextName();
	
  var getSettings = function(callback) {
    networkService.get(RMS_CONTEXT_NAME+"/RMSViewer/FetchConfiguration", null, function(data){
    	if (callback && typeof(callback) == "function") {
    		callback(data);
    	}
    });
  };

  var saveSettings = function(callback,params,failure){
    networkService.postAsFormData(RMS_CONTEXT_NAME+"/RMSViewer/SaveConfiguration", params, null, function(data){
      if (callback && typeof(callback) == "function") {
        callback(data);
      }
    },function(response){
      if (failure && typeof(failure) == "function") {
        failure(response);
      }
    });
  }

  var checkPCConnection = function(callback,params,failure){
    networkService.postAsFormData(RMS_CONTEXT_NAME+"/RMSViewer/CheckPCConnection", params, null, function(data){
      if (callback && typeof(callback) == "function") {
        callback(data);
      }
    });
  }
  return{
    getSettings:getSettings,
    saveSettings:saveSettings,
    checkPCConnection:checkPCConnection
  }
}]);