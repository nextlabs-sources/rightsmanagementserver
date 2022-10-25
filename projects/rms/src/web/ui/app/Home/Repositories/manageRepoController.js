mainApp.controller('manageRepoController',['$scope','$rootScope', '$state', 'networkService','configService','loggerService','dialogService','$location', '$stateParams','repositoryService','$filter', 'serviceProviderService',function($scope,$rootScope,$state,networkService,configService,loggerService,dialogService,$location, $stateParams,repositoryService,$filter,serviceProviderService){									
	 $scope.isLoading = false;     
     $scope.message="";
     var urlParameters = $location.search();
     if(urlParameters.success || urlParameters.error){
        $scope.isSuccess=urlParameters.success?true:false;
        $scope.message=urlParameters.success?urlParameters.success:urlParameters.error;
        repositoryService.setMessageParams(true, $scope.isSuccess, $scope.message);
     }
     else{
        repositoryService.setMessageParams(false, false, null);
     }

     var getAllowedServiceProviders=function(){
         $scope.isLoading = true;
    	 serviceProviderService.getAllowedServiceProviders(function(data){
    		  $scope.serviceProviderPresent=false;
              $scope.allowedServiceProviders=data;   
              for(var value in data){
                    $scope.serviceProviderPresent=true;
			  }
              $scope.isLoading = false; 
    	  });
     }
     var getRepositories = function() {
        $scope.isLoading = true;
        $scope.repositories={};
        $scope.hasPersonalRepository=false;
        $scope.hasSharedRepository=false;
        repositoryService.getManagedRepositories(function(data){
        	$scope.repositories=data;
            for(repository in $scope.repositories ){
              if($scope.repositories[repository].isShared){
                  $scope.hasSharedRepository=true;
              }else{
                $scope.hasPersonalRepository=true;
              }
            }
            $scope.isLoading = false;
        });
    }

    var refreshRepoListIfNecessary = function(data) {
      if (data.result) {
        getRepositories();
        $rootScope.$broadcast("refreshRepoList", {});
      }
    }

    $scope.editRepository=function(repository){
        dialogService.editRepository({
          "repository":repository
        },function(data){
          refreshRepoListIfNecessary(data);
        });
    }
    var edit=function(data){
      getRepositories();
    }
       
   $scope.addRepository=function(){
      dialogService.addRepository({
              "allowedRepository":$scope.allowedServiceProviders
        },function(data){
          refreshRepoListIfNecessary(data);
        });
   }
   
   $scope.deleteRepository=function(repository){
    var confirm = window.confirm($filter('translate')('managerepo.delete.confirm'));
    if(!confirm){
        return;
    }
    else{
        repositoryService.removeRepository(repository.repoId,function(data){
        repositoryService.setMessageParams(true, data.result, data.message);
        refreshRepoListIfNecessary(data);
        });
    }
  } 

   $scope.getMessage = function() {
    return repositoryService.getMessage();
  }

  $scope.shouldDisplay = function() {
    return repositoryService.shouldDisplay();
  }

  $scope.isSuccess = function() {
    return repositoryService.isSuccess();
  }

  $scope.dismissMessage = function() {
    repositoryService.setMessageParams(false, false, null);
  }
   getAllowedServiceProviders();
   getRepositories();
}]);