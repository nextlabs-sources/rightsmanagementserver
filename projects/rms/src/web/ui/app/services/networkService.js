/*
  This is a shared service is a gateway for network calls (ajax)
*/
mainApp.factory('networkService', ['$http', '$state', '$window', function($http, $state, $window) {  
  var errorHandler = function(response) {
    // called asynchronously if an error occurs
    // or server returns response with an error status.
    // hadle generic network errors 
    switch (response.status) {
      case 400:
        // not found;
        break;
      case 401:
        /*
        Accessing the global variable CONTEXT_PATH from index.jsp instead of injecting 
        initSettingsService to prevent circular dependencies
        */
        $window.location.href = CONTEXT_PATH + "/login.jsp?action=session_timed_out";
        break;
      case 500:
        // internal server error
        break;
      default:
        // unknown    
    }
  }
  var get = function(url, headers, callback) {
    $http({
      method: 'GET',
      url: url,
      headers: headers
    }).then(function successCallback(response) {
      // this callback will be called asynchronously
      // when the response is available
      if (callback) callback(response.data);
    }, errorHandler);
  }
 
  var post = function(url, data, headers, callback) {
    $http({
      method: 'POST',
      url: url,
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      data: data
    }).then(function successCallback(response) {
      // this callback will be called asynchronously
      // when the response is available
      if (callback) callback(response.data);
    }, errorHandler);
    
  }

  var postAsFormData = function(url, data, headers, callback, failureCallback) {
    $http({
      method: 'POST',
      url: url,
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      transformRequest: function(obj) {
          var str = [];
          for(var p in obj)
          str.push($window.encodeURIComponent(p) + "=" + $window.encodeURIComponent(obj[p]));
          return str.join("&");
      },
      data: data
    }).then(function successCallback(response) {
      // this callback will be called asynchronously
      // when the response is available
      if (callback) callback(response.data);
    }, function error(){
      if (failureCallback){
        failureCallback(response);
      }else{
        errorHandler(response);
      }
    });
  }

  return {
    get: get,
    post: post,
    postAsFormData: postAsFormData
  }
}]);
