mainApp.factory('kmSettingsService', ['$http', 'networkService', 'configService', 'loggerService', 'initSettingsService', 
	function($http, networkService, configService, loggerService, initSettingsService){
  

  var message = "";
  var success = false;
  var display =false;
  var headers = {'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8'};
  var RMS_CONTEXT_NAME = initSettingsService.getRMSContextName();


   var getTenantDetails = function(successCallback, errorCallback){
	networkService.post(RMS_CONTEXT_NAME+"/RMSViewer/KMSSettings/GetTenantDetails", null, headers, function(data){  
      if (data.result && data.result == true) {
		successCallback(data);
      }
      else {
      	errorCallback(data);
      }
    }); 
   }

   var getAllKeyRings = function(successCallback, errorCallback){ 
   	networkService.post(RMS_CONTEXT_NAME+"/RMSViewer/KMSSettings/GetKeyRingsWithKeys", null, headers, function(data){  
       if (data.result && data.result == true) {
		successCallback(data.keyRings);
      }
      else {
      	errorCallback(data);
      }
    });
   }
   
   var generateKey = function(keyRingName,successCallback, errorCallback){ 
	keyRingDetails = $.param({
		keyring_name: keyRingName
	});  
	networkService.post(RMS_CONTEXT_NAME+"/RMSViewer/KMSSettings/GenerateKey", keyRingDetails, headers, function(data){  
	   if (data.result && data.result == true) {
		successCallback();
	  }
	  else {
	  	errorCallback(data);
	  }
	});
   }
   
   var exportKeyRing = function(keyRingName,successCallback, errorCallback){ 
	keyRingDetails = $.param({
		keyring_name: keyRingName
	});
	networkService.post(RMS_CONTEXT_NAME+"/RMSViewer/KMSSettings/ExportKeyRing", keyRingDetails, headers, function(data){  
	   if (data.result && data.result == true) {
		successCallback(data);
	  }
	  else {
	  	errorCallback(data);
	  }
	});
   }

  return{
  	getTenantDetails:getTenantDetails,
  	getAllKeyRings:getAllKeyRings,
  	generateKey:generateKey,
	exportKeyRing: exportKeyRing
  }
}]);
