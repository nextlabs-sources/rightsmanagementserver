/*
	This is a shared service for app level config settings
*/
mainApp.factory('configService', ['networkService', function(networkService) {
  this.configObject = {};
  var configCallback = function(data) {
    this.configObject = (data) ? data : {};
  }
  var setConfig = function(obj) {
    this.configObject = obj;
    
  }
  var getUrl = function(key) {
    return this.configObject.baseUrl ? this.configObject.baseUrl : '';
    
  }
  return {
    configObject: this.configObject,
    setConfig: setConfig,
    getUrl: getUrl
  }
}]);