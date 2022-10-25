mainApp.factory('repositoryService', ['$http', 'networkService', 'configService', 'loggerService', 'initSettingsService', function($http, networkService, configService, loggerService, initSettingsService){
  

  var message = "";
  var success = false;
  var display =false;
  var headers = {'Content-Type': 'application/x-www-form-urlencoded; charset=utf-8'};
  var RMS_CONTEXT_NAME = initSettingsService.getRMSContextName();


  var getRepositoryList = function(callback) {
    networkService.get(RMS_CONTEXT_NAME+"/RMSViewer/GetRepositories", null, function(data){
    	if (callback && typeof(callback) == "function") {
    		callback(data);
    	}
    });
  };
  
  var removeRepository= function(repositoryId,successCallback,errorCallback){
    repoDetails = $.param({
              repoId: repositoryId
            }); 
    networkService.post(RMS_CONTEXT_NAME+"/RMSViewer/RemoveRepository", repoDetails, headers, function(data){
      if (successCallback && typeof(successCallback) == "function") {
        successCallback(data);
      }
    },function(response){
      if (errorCallback && typeof(errorCallback) == "function") {
        errorCallback(response);
      }
    });
  };

  var updateRepository= function(repository,repoName,successCallback,errorCallback){
    repoDetails = $.param({
              repoId: repository.repoId,
              repoName:repoName
            }); 
    networkService.post(RMS_CONTEXT_NAME+"/RMSViewer/UpdateRepository", repoDetails, headers, function(data){
      if (successCallback && typeof(successCallback) == "function") {
        successCallback(data);
      }
    },function(response){
      if (errorCallback && typeof(errorCallback) == "function") {
        errorCallback(response);
      }
    });
  };


  var addRepository=function(params){
    var parameters=params;
    repoDetails = $.param({
              repoType: params.repoType,
              showAll:params.allow_all
            }); 
    networkService.post(RMS_CONTEXT_NAME+"/RMSViewer/GetRepoAuthLink", repoDetails, headers, function(data){
      if(data.url != null && data.url != "") {
        if(parameters.repoType=="SHAREPOINT_ONLINE"){
        window.location.replace(data.url+"?name="+parameters.repoName+"&isShared="+parameters.allow_all+"&siteName="+parameters.sitesUrl+"&repoType=SHAREPOINT_ONLINE");
        }else{
        window.location.replace(data.url+"?name="+parameters.repoName+"&repoType="+parameters.repoType);
      }
    }
    });
  };
  
  var addSharePointOnPremiseRepo = function(params,callback){
	repoDetails = $.param({
              repoType: params.repoType,
              repoName: params.repoName,
              repoId: params.sitesUrl,
              showAll:params.allow_all
            }); 
    networkService.post(RMS_CONTEXT_NAME+"/RMSViewer/AddRepository", repoDetails, headers, function(data){
    	setMessageParams(true, data.result, data.message);
    	callback(data);
    });
  };


  var addedRepository=function(data){
    if(data.url != null && data.url != "") {
        window.location.replace(data.url+"?name="+$("#addDropboxName")[0].value+"&repoType="+repoType);
      }
  };
  
  var getAllFiles = function(params, callback){
	  networkService.post(RMS_CONTEXT_NAME+"/RMSViewer/GetAllFiles", params, headers, callback);
  };
  
  var getFilesWithPath = function(params, callback){
	  networkService.post(RMS_CONTEXT_NAME+"/RMSViewer/GetFilesWithPath", params, headers, callback);
  };
  
  var getSearchResults = function(params, callback){
	  networkService.post(RMS_CONTEXT_NAME+"/RMSViewer/GetSearchResults", params, headers, callback);
  };
  
  var showFile = function(folder, params, callback){
    var lastModifiedTime = folder.lastModifiedTime;
    if(!lastModifiedTime){
      lastModifiedTime = 0;
    }
    networkService.post(RMS_CONTEXT_NAME+"/RMSViewer/ShowFile?filePath="+encodeURIComponent(folder.pathId)+"&repoId="+encodeURIComponent(folder.repoId)+"&displayPath="+encodeURIComponent(folder.path)+"&lastModifiedDate="+encodeURIComponent(lastModifiedTime), params, headers, callback);
  };
  
  var getFileDetails = function(file, params, callback){
	  networkService.post(RMS_CONTEXT_NAME+"/RMSViewer/GetFileDetails?filePath="+
			  encodeURIComponent(file.pathId)+
			  "&repoId="+encodeURIComponent(file.repoId), params, headers, callback);
  };
  
  var getManagedRepositories = function(callback){
	  networkService.get(RMS_CONTEXT_NAME+"/RMSViewer/GetManagedRepositories", null, callback);  
  };
  
  var getShortRepoName = function(data, DISPLAY_NAME_MAX_LENGTH){
    if(data.length>DISPLAY_NAME_MAX_LENGTH){
      var str = data.slice(0,DISPLAY_NAME_MAX_LENGTH-1);
      str = str + "..."
    } else {
      str = data;
    }
    return str;
  }
  
  


  var inAll = true;
  var getIsInAllFilesPage = function(){
	return inAll;  
  };
  var setIsInAllFilesPage = function(value){
	  inAll = value;
  };
  
  var currentTab = 0;
  var getCurrentTab = function(){
	return currentTab;  
  };
  var setCurrentTab = function(value){
	  currentTab = value;
  }
  
  var collapseStatus = false;
  var getCollapseStatus = function(){
	  return collapseStatus;
  }
  var setCollapseStatus = function(value){
	  collapseStatus = value;
  }
  
  var previousState = null;
  var getPreviousState = function(){
	  return previousState;
  }
  var setPreviousState = function(value){
	  previousState = value;
  }

  var currentState = null;
  var setCurrentState = function(value){
    currentState = value;
  }
  var getCurrentState = function(){
    return currentState;
  }
  
  var currentFolderPath = null;
  var getCurrentFolderPath = function(){
	  return currentFolderPath;
  }
  var setCurrentFolderPath = function(value){
	  currentFolderPath = value;
  }
  
  var searchActivatedAlias = false;
  var getSearchActivatedAlias = function(){
	  return searchActivatedAlias;
  }
  var setSearchActivatedAlias = function(value){
	  searchActivatedAlias = value;
  }

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
    getRepositoryList:getRepositoryList,
    getIsInAllFilesPage:getIsInAllFilesPage,
    setIsInAllFilesPage:setIsInAllFilesPage,
    getCurrentTab:getCurrentTab,
    setCurrentTab:setCurrentTab,
    getCollapseStatus:getCollapseStatus,
    setCollapseStatus:setCollapseStatus,
    addRepository:addRepository,
    getPreviousState:getPreviousState,
    setPreviousState:setPreviousState,
    getCurrentFolderPath:getCurrentFolderPath,
    setCurrentFolderPath:setCurrentFolderPath,
    getSearchActivatedAlias:getSearchActivatedAlias,
    setSearchActivatedAlias:setSearchActivatedAlias,
    removeRepository:removeRepository,
    addSharePointOnPremiseRepo:addSharePointOnPremiseRepo,
    setMessageParams:setMessageParams,
    getMessage:getMessage,
    isSuccess:isSuccess,
    shouldDisplay:shouldDisplay,
    updateRepository:updateRepository,
    getAllFiles:getAllFiles,
    getFilesWithPath:getFilesWithPath,
    getSearchResults:getSearchResults,
    showFile:showFile,
    getFileDetails:getFileDetails,
    getManagedRepositories:getManagedRepositories,
    getShortRepoName:getShortRepoName,
    setCurrentState:setCurrentState,
    getCurrentState:getCurrentState
  }
}]);
