/*
  This is a shared service for app level config settings
*/
mainApp.factory('dialogService', ['$uibModal','$rootScope','serviceProviderService', 'repositoryService', 
  function($uibModal, $rootScope, serviceProviderService, repositoryService) {
  var confirm = function(parameter) {
    var title = parameter.title;
    var msg = parameter.msg;
    var ok = parameter.ok;
    var cancel = parameter.cancel;
    $uibModal.open({
      animation: true,
      // template: msg,
      templateUrl: 'ui/app/templates/dialog-confirm.html',
      controller: ['$uibModalInstance', '$scope', function($uibModalInstance, $scope) {
        $scope.title = title;
        $scope.msg = msg;
        $scope.ok = function() {
            $uibModalInstance.dismiss('cancel');
            ok && ok();
          },
          $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
            cancel && cancel();
          };
      }]
    });
  };
  var info = function(parameter) {
    var ok = parameter.ok;
    var cancel = parameter.cancel;
    var tags = parameter.tags;
    var tagsExist = Object.keys(tags).length > 0 ? true : false;
    var rights = parameter.rights;
    for (var i = 0; i < rights.length; i++) {
      if(rights[i]=="View")
        var ViewRights=true;
      if(rights[i]=="Print")
        var PrintRights=true;
      if(rights[i]=="PMI")
        var PMIRights=true;
    }
    var tagList=generateTagListFromTagMap(tags);
    $uibModal.open({
      animation: true,
      // template: msg,
      templateUrl: 'ui/app/Home/Repositories/partials/fileInfoTemplate.html',
      windowClass:'app-modal-window',
      controller: ['$uibModalInstance', '$scope', function($uibModalInstance, $scope) {
        $scope.tags=tagList;
        $scope.tagsExist=tagsExist;
        $scope.rights=rights;
        $scope.ViewRights=ViewRights;
        $scope.PrintRights=PrintRights;
        $scope.PMIRights=PMIRights;
        $scope.fileDetails=parameter;

        $scope.ok = function() {
            $uibModalInstance.dismiss('cancel');
            ok && ok();
          },
          $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
            cancel && cancel();
          };
      }]
    });
  };

  

  var repositoryConfig = function(parameters,add) {
    $uibModal.open({
      animation: true,
      // template: msg,
      templateUrl: 'ui/app/Home/settings/partials/appConfigurationTemplate.html',
      windowClass:'app-modal-window app-modal-window-mobile',
      controller: ['$uibModalInstance', '$scope', 'serviceProviderService', '$filter', 
      function($uibModalInstance, $scope, serviceProviderService, $filter) {
      setTooltip(parameters, $scope, $filter);
      $scope.provider = parameters.providerType;
      $scope.messageStatus = 0;
      $scope.downloadable = false;
      $scope.add = add;
      $scope.originalAllowRepos = false;
      if(add){
        //Update case
        $scope.settings = parameters.settings.attributes;

        if (!($scope.settings.APP_DISPLAY_MENU && $.trim($scope.settings.APP_DISPLAY_MENU))) {
          $scope.settings.APP_DISPLAY_MENU = $filter('translate')('sp.default.app_display_menu');
        }

        if (!($scope.settings.REDIRECT_URL && $.trim($scope.settings.REDIRECT_URL))) {
          $scope.settings.REDIRECT_URL = parameters.redirectUrl;
        }

        $scope.originalAllowRepos = $.parseJSON($scope.settings.ALLOW_PERSONAL_REPO);
        if($scope.originalAllowRepos){
          $scope.settings.ALLOW_PERSONAL_REPO = true;
        }else{
          $scope.settings.ALLOW_PERSONAL_REPO = false;
        }
        $scope.id = parameters.settings.id;
        $scope.providerType = parameters.settings.providerType;
        $scope.displayName =  parameters.settings.providerTypeDisplayName;
        if(parameters.settings.providerType=='SHAREPOINT_ONPREMISE'){
          $scope.settings.ENABLE_SHAREPOINT = true;
        }
        $scope.downloadable = parameters.settings.downloadable;
      }else{
        //Insert case
        $scope.providerType = parameters.settings.providerType;
        $scope.displayName =  parameters.settings.providerTypeDisplayName;
        $scope.settings={};
        $scope.settings.APP_ID="";
        $scope.settings.APP_SECRET="";
        $scope.settings.REDIRECT_URL="";
        $scope.settings.REDIRECT_URL="";
        $scope.settings.APP_DISPLAY_MENU=$filter('translate')('sp.default.app_display_menu');
        $scope.settings.REDIRECT_URL=parameters.redirectUrl;
        if($scope.providerType=='SHAREPOINT_ONPREMISE'){
          $scope.settings.ENABLE_SHAREPOINT = false;
        }
        if($scope.providerType=='DROPBOX' || $scope.providerType=='BOX' || $scope.providerType=='GOOGLE_DRIVE' || 
          $scope.providerType=='ONE_DRIVE' || $scope.providerType=='SHAREPOINT_ONPREMISE'
          || $scope.providerType=='SHAREPOINT_ONLINE'){
          $scope.settings.ALLOW_PERSONAL_REPO=false;
        }
        if($scope.providerType=='SHAREPOINT_CROSSLAUNCH'){
          $scope.settings.REMOTE_WEB_URL="";
          $scope.settings.APP_NAME="";
        }
        if($scope.providerType=='SHAREPOINT_ONLINE_CROSSLAUNCH'){
          $scope.settings.APP_NAME="";
        }
      }
      if($scope.settings.APP_SECRET && $scope.settings.APP_SECRET.length > 0){
        $scope.showAppSecret = false;
      }else{
        $scope.showAppSecret = true;
      }

      $scope.showAppSecretField = function(){
        $scope.showAppSecret = true;
      }

      $scope.delete = function(id){
        $scope.isLoading = true;
        var confirm = window.confirm($filter('translate')('service_provider.delete.confirm'));
        if(!confirm){
          $scope.isLoading = false;
          return;
        }

        $scope.messageStatus = 0;
        serviceProviderService.removeServiceProviderSettings(id,function(data){
          if(data.result){
            serviceProviderService.setMessageParams(true, true, data.message);
            $rootScope.$emit("refreshRepoList");
            $uibModalInstance.close(data);
          }else{
            $scope.messageStatus = 2;
            $scope.message = data.message;
          }
          $scope.isLoading = false; 
        });
      }

      $scope.setFormDirty = function(param){
        if(param){
          return;
        }
        $scope.repoConfigForm.$setDirty();
      }

      $scope.downloadCrossLaunchApp = function(providerType, id){
        serviceProviderService.downloadCrossLaunchApp(providerType, id); 
      }

      $scope.saveServiceProviderSettings = function(){
        if($scope.originalAllowRepos && !$scope.settings.ALLOW_PERSONAL_REPO 
          && $scope.providerType!='SHAREPOINT_CROSSLAUNCH' && $scope.providerType!='SHAREPOINT_ONLINE_CROSSLAUNCH'){
          var confirm = window.confirm($filter('translate')('service_provider.save.confirm'));
          if(!confirm){
            return;
          }
        }
        $scope.isLoading = true;
        $scope.messageStatus = 0;
        var params={};
        if($scope.providerType=='SHAREPOINT_ONPREMISE'){
            params.ALLOW_PERSONAL_REPO = $scope.settings.ALLOW_PERSONAL_REPO;
            params.ENABLE_SHAREPOINT = $scope.settings.ENABLE_SHAREPOINT;
            params.providerType = $scope.providerType;
            if($scope.id){
              params.id = $scope.id;
            } 
        }else{
          params ={
            "APP_ID" : $scope.settings.APP_ID,
            "APP_SECRET" : $scope.settings.APP_SECRET,
            "REDIRECT_URL" : $scope.settings.REDIRECT_URL,
            "providerType" : $scope.providerType,
          };
          if($scope.id){
            params.id = $scope.id;
          }
          if($scope.providerType=='SHAREPOINT_CROSSLAUNCH'){
            params.REMOTE_WEB_URL=$scope.settings.REMOTE_WEB_URL;
            params.APP_NAME=$scope.settings.APP_NAME;
            params.APP_DISPLAY_MENU=$scope.settings.APP_DISPLAY_MENU;
          }
          if($scope.providerType=='SHAREPOINT_ONLINE_CROSSLAUNCH'){
            params.APP_NAME=$scope.settings.APP_NAME;
            params.APP_DISPLAY_MENU=$scope.settings.APP_DISPLAY_MENU;
          }
          if($scope.providerType=='DROPBOX' || $scope.providerType=='BOX' || $scope.providerType=='GOOGLE_DRIVE' || 
            $scope.providerType=='ONE_DRIVE'|| $scope.providerType=='SHAREPOINT_ONLINE'){
            params.ALLOW_PERSONAL_REPO = $scope.settings.ALLOW_PERSONAL_REPO;
          }
        }
        serviceProviderService.saveServiceProviderSettings(params,function(data){
          if(data.result){
            serviceProviderService.setMessageParams(true, true, data.message);
            $rootScope.$emit("refreshRepoList");
            $uibModalInstance.close(true);
          }else{
            $scope.messageStatus = 2;
            $scope.message = data.message;
          }
          $scope.isLoading = false;
        });
      }

      $scope.cancel = function() {
        $scope.messageStatus = 1;
        $uibModalInstance.dismiss('cancel');
      };
      }]
    });
  };

  var editRepository = function(parameters,successCallback,errorCallback) {

    var REPO_NAME_MAX_LENGTH = 40;

    $uibModal.open({
      animation: true,
      // template: msg,
      templateUrl: 'ui/app/Home/Repositories/partials/editRepository.html',
      windowClass:'app-modal-window',
      controller: ['$uibModalInstance', '$scope','$filter', function($uibModalInstance, $scope,$filter) {
      $scope.repository = angular.copy(parameters.repository);       
      $scope.repoNameShortened = repositoryService.getShortRepoName($scope.repository.repoName, REPO_NAME_MAX_LENGTH);
      $scope.cancel = function() {
        $uibModalInstance.dismiss('cancel');
      };

      $scope.updateRepository=function(repository){
    	$scope.isLoading=true;
        repositoryService.updateRepository(repository,$scope.repository.repoName,function(data) {
          $scope.isLoading=false;
          if (successCallback && typeof(successCallback) == "function") {
              $uibModalInstance.dismiss('cancel');
              repositoryService.setMessageParams(true, data.result, data.message);
              successCallback(data);
          }
        },function(response){
        	$scope.isLoading=false;
            if (errorCallback && typeof(errorCallback) == "function") {
               repositoryService.setMessageParams(true, false, $filter('translate')('managerepo.update.error'));
               errorCallback(response);
            }
          });
      };

      $scope.deleteRepository=function(repository){
        var confirm = window.confirm($filter('translate')('managerepo.delete.confirm'));
        if(!confirm){
          return;
        }
        else{
          repositoryService.removeRepository(repository.repoId,function(data) {
            if (successCallback && typeof(successCallback) == "function") {
                $uibModalInstance.dismiss('cancel');
                repositoryService.setMessageParams(true, true, $filter('translate')('managerepo.delete.success'));
                successCallback(data);
            }
          },function(response){
              if (errorCallback && typeof(errorCallback) == "function") {
                 repositoryService.setMessageParams(true, true, $filter('translate')('managerepo.delete.error'));
                 errorCallback(response);
              }
            });
        }
      };
      }]
    });
  };

  var addRepository = function(parameters,callback) {
     var params={};
     var confirmationMsg="";
     var allowedServiceProvidersList=[];
     
      var allowedServiceProvidersMap=parameters.allowedRepository;
      for(var value in allowedServiceProvidersMap){
        var allowedServiceProvider={key:"",serviceProvider:""};
        allowedServiceProvider.key=value;
        allowedServiceProvider.serviceProvider=allowedServiceProvidersMap[value];
        allowedServiceProvidersList.push(allowedServiceProvider);
      }
      $uibModal.open({
      animation: true,
      // template: msg,
      templateUrl: 'ui/app/Home/Repositories/partials/addRepository.html',
      windowClass:'app-modal-window',
      controller: ['$uibModalInstance', '$scope','$filter', function($uibModalInstance, $scope,$filter) {
      $scope.change = function(serviceProviderId){
       $scope.displayName="";
       $scope.sitesUrl="";
       $scope.button = serviceProviderId;
       $scope.selectedServiceProvider=allowedServiceProvidersMap[serviceProviderId];
       if (typeof $scope.selectedServiceProvider.attributes.ALLOW_PERSONAL_REPO == "string") {
          $scope.selectedServiceProvider.attributes.ALLOW_PERSONAL_REPO = 
              $.parseJSON($scope.selectedServiceProvider.attributes.ALLOW_PERSONAL_REPO);
       }
       if($scope.selectedServiceProvider.attributes.ALLOW_PERSONAL_REPO){
            $scope.allow_all = false;
           }else{
             $scope.allow_all = true;
           }
      };
      $scope.displayName="";
      $scope.allow_all=false;
      $scope.selectedServiceProvider=allowedServiceProvidersList[0].serviceProvider;
      $scope.button=allowedServiceProvidersList[0].key;
      $scope.change(allowedServiceProvidersList[0].key);  
      $scope.sitesUrl="";
      $scope.allowedServiceProvidersList=allowedServiceProvidersList; 
      $scope.cancel = function() {
        $uibModalInstance.dismiss('cancel');
      };
      $scope.connectRepository=function(){
        params.repoName=$scope.displayName;
        params.allow_all=$scope.allow_all;
        if($scope.button=='GOOGLE_DRIVE'){
          params.repoType=$scope.button;
          confirmationMsg=$filter('translate')('managerepo.redirect.google');

        }
        else if($scope.button=='DROPBOX'){
          params.repoType=$scope.button;
          confirmationMsg=$filter('translate')('managerepo.redirect.dropbox');
        }
        else if($scope.button=='SHAREPOINT_ONLINE'){
          params.repoType=$scope.button;
          params.sitesUrl=$scope.sitesUrl;
          confirmationMsg=$filter('translate')('managerepo.redirect.sharepoint');
        }
        else if($scope.button=='SHAREPOINT_ONPREMISE'){
          params.repoType=$scope.button;
          params.sitesUrl=$scope.sitesUrl;
        }
        else if($scope.button=='ONE_DRIVE'){
          params.repoType=$scope.button;
          confirmationMsg=$filter('translate')('managerepo.redirect.onedrive');
        }
        else if($scope.button=='BOX'){
          params.repoType=$scope.button;
          confirmationMsg=$filter('translate')('managerepo.redirect.box');
        }
        if(confirmationMsg != ""){
        	var confirmed=window.confirm(confirmationMsg);
        }
       	
        if(params.repoType === "SHAREPOINT_ONPREMISE"){
  		  repositoryService.addSharePointOnPremiseRepo(params,function(data){
  			  callback(data);
  		  });
  		  $uibModalInstance.dismiss('cancel');
  	  	} else {
  	  		if(confirmed){
  	  			repositoryService.addRepository(params,function(data){
  	  				if(data==true){
  	  					$uibModalInstance.dismiss('cancel');
  	  				}
  	  			});
  	  		}
  	  	}
        
    };
      }]
    });
  };

  var upload=function(parameter) {
    
    $uibModal.open({
      animation: true,
      // template: msg,
      templateUrl: 'ui/app/Home/Repositories/partials/uploadfileTemplate.html',
      windowClass:'app-modal-window',
      controller: ['$uibModalInstance', '$scope','Upload','$timeout', function($uibModalInstance, $scope,Upload,$timeout) {
        $scope.ok = function() {
            $uibModalInstance.dismiss('cancel');   
          },
          $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
          };
          $scope.$watch('files', function () {
             $scope.uploadandview($scope.files);
          });
        
          $scope.uploadandview=function(file,errFiles){
            if(file){
              $scope.isLoading=true;
              var uploader = Upload.upload({
                url: '/RMS/RMSViewer/UploadAndView',
                data: {file: file}
              });
              uploader.then(function (response) {
                 $scope.isLoading=false;
                 var url=response.data[0].viewerUrl;
                 var error = response.data[0].error;
                 if(error) {
                   $scope.uploadErr = error;
                 } else if(url){
                   $scope.uploadErr = "";
                   $scope.cancel();
                   openSecurePopup(url);
                 }
             });
            }
          }
      }]
    });
  }

  function setTooltip(data, $scope, $filter){

      if(data.settings.providerType === "ONE_DRIVE"){
        $scope.appIdInfo = $filter('translate')('onedrive.app_id.info');
        $scope.appSecretInfo = $filter('translate')('onedrive.app_secret.info');
        $scope.appRedirectUrl = $filter('translate')('onedrive.redirect_url.info');
      } else if(data.settings.providerType === "GOOGLE_DRIVE"){
        $scope.appIdInfo = $filter('translate')('googledrive.app_id.info');
        $scope.appSecretInfo = $filter('translate')('googledrive.app_secret.info');
        $scope.appRedirectUrl = $filter('translate')('googledrive.redirect_url.info');
      } else if(data.settings.providerType === "DROPBOX"){
        $scope.appIdInfo = $filter('translate')('dropbox.app_id.info');
        $scope.appSecretInfo = $filter('translate')('dropbox.app_secret.info');
        $scope.appRedirectUrl = $filter('translate')('dropbox.redirect_url.info');
      } else if(data.settings.providerType === "BOX"){
        $scope.appIdInfo = $filter('translate')('box.app_id.info');
        $scope.appSecretInfo = $filter('translate')('box.app_secret.info');
        $scope.appRedirectUrl = $filter('translate')('box.redirect_url.info');
      } else if(data.settings.providerType === "SHAREPOINT_ONLINE"){
        $scope.appIdInfo = $filter('translate')('spol.app_id.info');
        $scope.appSecretInfo = $filter('translate')('spol.app_secret.info');
        $scope.appRedirectUrl = $filter('translate')('spol.redirect_url.info');
      } else if(data.settings.providerType === "SHAREPOINT_ONLINE_CROSSLAUNCH"){
        $scope.appIdInfo = $filter('translate')('spol_cl.app_id.info');
        $scope.appSecretInfo = $filter('translate')('spol_cl.app_secret.info');
        $scope.appRedirectUrl = $filter('translate')('spol_cl.redirect_url.info');
        $scope.appDisplayMenu = $filter('translate')('spol_cl.app_display_menu.info');
      } else if(data.settings.providerType === "SHAREPOINT_CROSSLAUNCH"){
        $scope.appIdInfo = $filter('translate')('sp_cl.app_id.info');
        $scope.appSecretInfo = $filter('translate')('sp_cl.app_secret.info');
        $scope.appRedirectUrl = $filter('translate')('sp_cl.redirect_url.info');
        $scope.appDisplayMenu = $filter('translate')('sp_cl.app_display_menu.info');
      }
  }


  return {
    confirm: confirm,
    info:info,
    upload:upload,
    repositoryConfig:repositoryConfig,
    editRepository:editRepository,
    addRepository:addRepository
  }
}]);
