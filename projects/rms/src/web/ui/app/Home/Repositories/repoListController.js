mainApp.controller('repoListController',['$scope', '$rootScope','$state', 'networkService','configService','loggerService','dialogService','$location','repositoryService', 'initSettingsService', 
function($scope,$rootScope,$state,networkService,configService,loggerService,dialogService,$location,repositoryService, initSettingsService){
	
	var DISPLAY_NAME_MAX_LENGTH = 20;				
	$scope.tabs = [];	
	$rootScope.isAdmin = false;
	$scope.toggleFilesTab = false;

	var init = function() {
		var initData = initSettingsService.getSettings();
		if (initData != null) {
			$rootScope.isAdmin = initData.isAdmin;
		}
		switch($state.current.name){
			case STATE_MANAGE_REPOSITORIES:
				repositoryService.setCurrentTab('manage_repositories');
				repositoryService.setIsInAllFilesPage(false);
				break;
			case STATE_SERVICE_PROVIDERS:
				repositoryService.setCurrentTab('service_providers');
				repositoryService.setIsInAllFilesPage(false);
				break;
			case STATE_SETTINGS:
				repositoryService.setCurrentTab('configuration');
				repositoryService.setIsInAllFilesPage(false);
				break;
			case STATE_KEY_MANAGEMENT_SETTINGS:
				repositoryService.setCurrentTab('key_mgmt');
				repositoryService.setIsInAllFilesPage(false);
				break;
			default:
				repositoryService.setCurrentTab(0);
		}
	}
	
	init();
	
	var repositoryList= function(){
		repositoryService.getRepositoryList(function(data) {
			var repoList = data;	        	
			for (var i = 0; i < repoList.length; i++) {
				repoList[i].shortenedDisplayName = repositoryService.getShortRepoName(repoList[i].repoName, DISPLAY_NAME_MAX_LENGTH);
				$scope.tabs.push({        	    	        	    	
					repoObj: repoList[i]
				});
			}			        	
		});
	}
	
	$rootScope.$on("refreshRepoList", function (event, args) {
		$scope.tabs = [];
		repositoryList();
	});
	
	$scope.collapseStatus = repositoryService.getCollapseStatus();
	$scope.toggleActiveTab = function(){
		$scope.toggleFilesTab =true;
	}
	$scope.onClickTab = function (tab) {
		if($scope.toggleFilesTab){
			$scope.toggleFilesTab = false;
			return;
		}		
		repositoryService.setCurrentTab(tab);
		$rootScope.$emit("toggleSideBar");
		if (tab== 'service_providers') {
			repositoryService.setIsInAllFilesPage(false);	
			$state.go(STATE_SERVICE_PROVIDERS);
			return;
		} else if (tab == 'manage_repositories') {
			repositoryService.setIsInAllFilesPage(false);
			$state.go(STATE_MANAGE_REPOSITORIES);
			return;
		} else if (tab == 'key_mgmt') {
			repositoryService.setIsInAllFilesPage(false);
			$state.go(STATE_KEY_MANAGEMENT_SETTINGS);
			return;
		}else if (tab == 'configuration' ||  tab == 'user_mgmt') {
			repositoryService.setIsInAllFilesPage(false);
			$state.go(STATE_SETTINGS);
			return;
		} else {
			repositoryService.setCurrentTab(tab==0 ? tab : tab.repoObj.repoId);
			var nextState = tab==0 ? STATE_ALL_REPOSITORIES : STATE_REPOSITORIES;
			var params = tab==0 ? {} : {repoId: tab.repoObj.repoId, repoName: tab.repoObj.repoName, repoType: tab.repoObj.repoType};
			
			if((nextState==STATE_ALL_REPOSITORIES)){
				repositoryService.setCollapseStatus(false);	
				$scope.collapseStatus = repositoryService.getCollapseStatus();
				repositoryService.setIsInAllFilesPage(true);
				$state.go(nextState, params, { reload: STATE_HOME });
			}
			else{
				repositoryService.setIsInAllFilesPage(false);				
				$state.go(nextState, params, { reload: nextState });
			}
		}
	}

	$scope.upload=function(){
		dialogService.upload({
		});
	}
	           
	$scope.isActiveTab = function(repoId) {
	    return repositoryService.getCurrentTab() == repoId;
	}

	function shortenRepoName(data){
		if(data.repoName.length>DISPLAY_NAME_MAX_LENGTH){
			var str = data.repoName.slice(0,DISPLAY_NAME_MAX_LENGTH-1);
			data.shortenedDisplayName = str + "..."
		} else {
			data.shortenedDisplayName = data.repoName;
		}
		return data;
	}

	repositoryList();
}]);