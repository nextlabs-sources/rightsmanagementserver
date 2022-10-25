mainApp.factory('serviceProviderService', ['$rootScope','$http', '$window', 'networkService', 'configService', 'loggerService', 'initSettingsService', 
  function($rootScope, $http, $window, networkService, configService, loggerService, initSettingsService){
  
  var displayMessage = false;
  var message = "";
  var success = false;
  var RMS_CONTEXT_NAME = initSettingsService.getRMSContextName();
  

  var removeServiceProviderSettings = function(serviceProviderId,successCallback,errorCallback){
      //URL for deleting settings
      var url = RMS_CONTEXT_NAME+"/RMSViewer/DeleteServiceProviderSetting";
      networkService.postAsFormData(url, {"serviceProviderId":serviceProviderId}, null, function(data){
      if (successCallback && typeof(successCallback) == "function") {
        successCallback(data);
        $rootScope.$emit("reloadSettings");
      }
    },function(response){
      if (errorCallback && typeof(errorCallback) == "function") {
        errorCallback(response);
      }
    });
  }

  var saveServiceProviderSettings = function(settings,successCallback,errorCallback){
      //URL for adding/updating settings
      var url =RMS_CONTEXT_NAME+"/RMSViewer/SaveServiceProviderSetting";
      networkService.postAsFormData(url, settings, null, function(data){
      if (successCallback && typeof(successCallback) == "function") {
        successCallback(data);
        $rootScope.$emit("reloadSettings");
      }
    },function(response){
      if (errorCallback && typeof(errorCallback) == "function") {
        errorCallback(response);
      }
    });
  }

  var loadSettings = function(callback){
    networkService.get(RMS_CONTEXT_NAME+"/RMSViewer/FetchServiceProviderSettings", null, callback);
  }


  var downloadCrossLaunchApp = function(providerType, id){
    var url = RMS_CONTEXT_NAME+"/RMSViewer/ConfigureSharePointApp?id="+$window.encodeURIComponent(id)+
    "&type="+$window.encodeURIComponent(providerType);
    $window.open(url); 
  }
  
  var getAllowedServiceProviders = function(callback){
	  networkService.get(RMS_CONTEXT_NAME+"/RMSViewer/GetAllowedServiceProviders", null, callback);
  };

  var getMessage =function() {
    return message;
  }

  var isSuccess =function() {
    return success;
  }

  var shouldDisplay =function() {
    return display;
  }

  var setMessageParams =function(isDisplay, isSuccess, msg) {
    display = isDisplay;
    success = isSuccess;
    message = msg;
  }

  return{
    downloadCrossLaunchApp:downloadCrossLaunchApp,
    removeServiceProviderSettings:removeServiceProviderSettings,
    saveServiceProviderSettings:saveServiceProviderSettings,
    loadSettings:loadSettings,
    getMessage:getMessage,
    isSuccess:isSuccess,
    shouldDisplay:shouldDisplay,
    setMessageParams:setMessageParams,
    getAllowedServiceProviders:getAllowedServiceProviders
  }
}]);