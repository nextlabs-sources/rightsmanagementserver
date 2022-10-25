mainApp.controller('fileListController',['$scope', '$state', 'networkService','configService','loggerService','dialogService','$location', '$stateParams','repositoryService','$filter','$rootScope', 'initSettingsService', 'serviceProviderService' ,function($scope,$state,networkService,configService,loggerService,dialogService,$location, $stateParams,repositoryService,$filter, $rootScope, initSettingsService, serviceProviderService){									
        
		$rootScope.$on('$stateChangeSuccess', function (ev, to, toParams, from, fromParams) {
			repositoryService.setPreviousState(from.name);
			repositoryService.setCurrentState(to.name);
		});
		initSettingsService.setRightPanelMinHeight();
	
        $scope.isDisplay = true;
        var PAGE_TITLE_MAX_LENGTH_DESKTOP = 40;
        var PAGE_TITLE_MAX_LENGTH_MOBILE = 20;

		$scope.parentFolder = null;
		$scope.currentFolderName = null;
		
        var getFiles = function(folder){
        	
        	$scope.searchString="";
        	$scope.showTree = true;
        	$scope.isLoading =true;
        	if(!$scope.searchActivated){
        		var pageTitle = folder != undefined && folder != null ? folder.repoName : $stateParams.repoName;
                $scope.pageTitle =  $stateParams.all ? $filter('translate')('all.files') : pageTitle;
                if ($scope.pageTitle != null) {
                    $scope.pageTitleDesktopShortened = repositoryService.getShortRepoName($scope.pageTitle, PAGE_TITLE_MAX_LENGTH_DESKTOP);
                    $scope.pageTitleMobileShortened = repositoryService.getShortRepoName($scope.pageTitle, PAGE_TITLE_MAX_LENGTH_MOBILE);
                }
        	}
        	if($stateParams.all){           		
        		repositoryService.getAllFiles(null, setData);
        	} else {
        		$scope.showAllServices = false;
        		repoDetails = $.param({
   
        			repoId: folder.repoId ? folder.repoId : $stateParams.repoId,
        			repoType: folder.repoType? folder.repoType : $stateParams.repoType,
        			path: folder.pathId
        		});        		
        		$scope.breadCrumbsContent = [];
        		if(folder.pathId != "/"){        			
        			var folderPathArray = folder.path.split("/");
        			var folderPathIdArray = folder.pathId.split("/");
        			var buildPathId = "";
        			var buildPath = "";
        			for (var i = 0; i < folderPathArray.length; i++) {
        				var breadCrumbElement = {};
						breadCrumbElement.repoId = folder.repoId ? folder.repoId : $stateParams.repoId;
						breadCrumbElement.pathId = buildPathId + "/" +folderPathIdArray[i];
						breadCrumbElement.path =   buildPath + "/" + folderPathArray[i];
						breadCrumbElement.isFolder = true;
						breadCrumbElement.folderName =  folderPathArray[i];
        				buildPathId = breadCrumbElement.pathId;
        				buildPath = breadCrumbElement.path;
        				breadCrumbElement.pathId = breadCrumbElement.pathId.replace("/", "");         			
        				breadCrumbElement.path = breadCrumbElement.path.replace("/", "");        				
        				$scope.breadCrumbsContent.push(breadCrumbElement);
        			}
        			$scope.breadCrumbsContent.shift();
        		}
				var indexOfPathId = folder.pathId.lastIndexOf("/");
				$scope.parentFolder = jQuery.extend(true, {}, folder);
				
				if(folder.pathId == "/") {
					$scope.parentFolder.hasParent = false;
					$scope.currentFolderName = "";
				}
				else {
					var indexOfPath = folder.path.lastIndexOf("/");
					$scope.parentFolder.hasParent = true;
					if(indexOfPathId == 0) {
						$scope.parentFolder.pathId = "/"
						$scope.parentFolder.path = "/";
					} else {
						$scope.parentFolder.pathId =  folder.pathId.substring(0,indexOfPathId);
						$scope.parentFolder.path =  folder.path.substring(0,indexOfPath);
					}
					$scope.currentFolderName = folder.path.substring(++indexOfPath);
				}

       			$scope.onClickFolder = false;
        		repositoryService.setCurrentFolderPath(folder.pathId);
        		repositoryService.setIsInAllFilesPage(false);
        		$scope.repoContents = [];
        		repositoryService.getFilesWithPath(repoDetails, setData);
        		$scope.searchActivated=false;
        	}
        }
        $scope.pageTitle = null;
        $scope.pageTitleDesktopShortened = null;
        $scope.pageTitleMobileShortened = null;
        $scope.showAllServices = $stateParams.all;
        $scope.repoContents = null;
        $scope.testValue = 0;
        $scope.breadCrumbsContent = [];
        $scope.isTreeOpen = false;
        $scope.toggleTreeView = function(){
        	setMinHeightToTree();
        	var len = $scope.breadCrumbsContent.length;
        	if(len > 0){
        		$scope.onClickFolder = true;
        		var current_selection = angular.element("#rmstree").jstree("get_selected");
        		angular.element("#rmstree").jstree("deselect_node", "#"+current_selection);    		
        		angular.element("#rmstree").jstree("select_node", "#"+$scope.breadCrumbsContent[len-1].pathId);     
        		$scope.onClickFolder = false;
        	}
        	$scope.isTreeOpen = !$scope.isTreeOpen;        	
        	return $scope.isTreeOpen;
        }
        
        $scope.repos = [];
        var allKeyword = "all";
        $scope.filterData = [];
        $scope.filterData[allKeyword]=true;
        $scope.filteredRepos = [allKeyword];
        var repositoryList= function(){
    		repositoryService.getRepositoryList(function(data) {
    			var repoList = data;	        	
    			for (var i = 0; i < repoList.length; i++) {
    				$scope.repos.push({        	    	        	    	
    					repoObj: repoList[i]
    				});
    				$scope.filterData[repoList[i].repoId] = true;
    			}			        	    			   	
    		});
    	}                
        repositoryList(); 
        
        var DISPLAY_NAME_MAX_LENGTH = 25;
        var repoNamesToString = function(){
        	var selectedRepos = $scope.selectedRepos;
        	var str = "";
        	for(var i=0;i<selectedRepos.length;i++){        		
        		str += selectedRepos[i];
        		if(i!=selectedRepos.length-1){
        			str += ", "
        		}
        	}        	
        	str = repositoryService.getShortRepoName(str, DISPLAY_NAME_MAX_LENGTH);
        	return str;
        }

        $scope.searchActivated = false;
        $scope.search = function() {
        	$scope.isLoading = true;
            $scope.pageTitle = $filter('translate')('search');
            $scope.pageTitleDesktopShortened = $scope.pageTitle;
            $scope.pageTitleMobileShortened = $scope.pageTitle;
        	searchDetails = $.param({
        			searchString: $scope.searchString
        		});
        	repositoryService.getSearchResults(searchDetails, setData);
        	$scope.searchActivated=true;
        	repositoryService.setIsInAllFilesPage(false);
        }
        $scope.filterRepo = function(){        	
        	if(!$scope.filterData[allKeyword]){
				appendingRepoNames();
				$scope.displaySelectedRepos = repoNamesToString();
        	} else{
        		$scope.selectedRepos=[];
        		$scope.displaySelectedRepos = "";
        	}    	        	
        	$scope.isOpen = false;        	
        	$scope.filteredRepos = [];
        	var filterKeys = Object.keys($scope.filterData);
        	if($scope.filterData[allKeyword]){
        		$scope.filteredRepos.push(allKeyword);
        		return $scope.filteredRepos;
        	}
        	for(var i=0; i< filterKeys.length; i++){
        		if($scope.filterData[filterKeys[i]]){
        			$scope.filteredRepos.push(filterKeys[i]);
        		}        		
        	}        	
        	return $scope.filteredRepos;
        }
        
        $scope.isFiltered = function(data){
        	if ($scope.filteredRepos.indexOf(allKeyword) > -1){
        		return true;
        	}
        	if($scope.filteredRepos.indexOf(String(data.repoId)) > -1){
        		return true;
        	}
        	return false;
        }
                
        $scope.selectedRepos = [];        
		$scope.selectAllRepos = function(){						
			var filterKeys = Object.keys($scope.filterData);
			for(var i=0; i < filterKeys.length; i++){
				if($scope.filterData[allKeyword]){								
					$scope.filterData[filterKeys[i]] = true;					
				} else{
					$scope.filterData[filterKeys[i]] = false;
				}				
			}			
			validateSelectedRepos();			
		}

		$scope.clickCheckbox = function(){			
			$scope.filterData[allKeyword] = false;								
			validateSelectedRepos();					
		}

		$scope.selectedReposValidationStatus = true;
		function validateSelectedRepos(){
			var filterKeys = Object.keys($scope.filterData);
			var count = 0;
			for(var i=0; i < filterKeys.length; i++){
				if(!$scope.filterData[filterKeys[i]]){
					count++;
				}
			}
			if(count == filterKeys.length){
				$scope.selectedReposValidationStatus = false;	
			} else {
				$scope.selectedReposValidationStatus = true;	
			}
		}

		function appendingRepoNames(){
			$scope.selectedRepos=[];
			for(var i=0; i<$scope.repos.length; i++){
				var repoObj = $scope.repos[i].repoObj;
				if($scope.filterData[repoObj.repoId]){
					$scope.selectedRepos.push(repoObj.repoName);
				}
			}
		}        
        
        $scope.dropdownClicked = function($event) {
	        // loggerService.log($event.target, 1);
	        if ($($event.target).attr('data-propagation') != 'true') $event.stopPropagation();
	    };
        
	    function buildTree(data, contents){
	    	if (!$stateParams.all) {
	    		if (data.name === 'Root' && data.children.path === undefined) {
	    			result = convertToJSON(contents);
	    			for (var idx in result) {
	    				pushNode(result[idx]);
	    			}
	    		} else {		    		
	    			if (Array.isArray(data)) {
	    				result = convertToJSON(contents);
	    				for (var idx in result) {
	    					pushNode(result[idx]);
	    				}
	    			}
	    		}
	    	}
	    }
	    
	    function buildTreeDynamically(){
	    	var currentPath = repositoryService.getCurrentFolderPath();
	    	var folderIdArr = currentPath.split("/");
	    	var pathBuilt = ""
	    	for(var i=0; i<folderIdArr.length; i++){
	    		if(i==0){
	    			pathBuilt = "/";
	    		} else if(i==1){
	    			pathBuilt = pathBuilt + folderIdArr[i];
	    		} else {
	    			pathBuilt = pathBuilt +"/"+ folderIdArr[i];
	    		}
	    		
	    		repoDetails = $.param({
        			repoId: $stateParams.repoId,
        			path: pathBuilt
        		});
	    		repositoryService.getFilesWithPath(repoDetails, setDataDynamically);
	    	}
	    	return;
	    }
	    
	    var setDataDynamically = function(data){
	    	var contents;	    		    	
	    	if(data.content.children){
	    		contents = data.content.children;
	    	}
	    	else{
	    		contents = data.content
	    	}
	    	buildTree(data.content, contents);
//	    	return;
	    }
	    
	    $scope.messages = [];

	    var setData = function(data) {
	    	var contents;
	    	if (!data.result && data.redirectUrl) {
	    		var confirmed = window.confirm($filter('translate')('err.repo.not.authorized'));
          		if (confirmed) {
          			window.location.replace(data.redirectUrl);
          		} else {
          			repositoryService.setCurrentTab(0);
          			repositoryService.setIsInAllFilesPage(true);
          			$state.go(STATE_ALL_REPOSITORIES);
          		}
	    	} 

	    	var previousState = repositoryService.getPreviousState();
	    	var currentState = repositoryService.getCurrentState();
	    	var isTreeBuiltDynamically = false;
	    	if(currentState != STATE_ALL_REPOSITORIES && currentState !=null){
	    		buildTreeDynamically();
	    		repositoryService.setPreviousState(null);
	    		isTreeBuiltDynamically = true;
	    	}
	    	else if(repositoryService.getSearchActivatedAlias() && !$scope.searchActivated){	    		
	    		buildTreeDynamically();
	    		isTreeBuiltDynamically = true;	 
	    	}

	    	if(data.content != undefined && data.content.length != 0){	    		    			    	
				var hasChildren = data.content && data.content.name == "Root";
		    	if (hasChildren) {
		    		contents = data.content.children;
		    	}
		    	else {
		    		contents = data.content
		    	}
		    	
		    	if(!isTreeBuiltDynamically){
		    		buildTree(data.content, contents);
		    		isTreeBuiltDynamically = false;
		    	}

		    	$scope.repoContents = contents;

		    	if(!$scope.searchActivated){
		    		$scope.pageTitle = contents[0].repoName != null ? contents[0].repoName : '';
		    		$scope.pageTitleDesktopShortened = repositoryService.getShortRepoName($scope.pageTitle, PAGE_TITLE_MAX_LENGTH_DESKTOP);
		    		$scope.pageTitleMobileShortened = repositoryService.getShortRepoName($scope.pageTitle, PAGE_TITLE_MAX_LENGTH_MOBILE);
		    	}
	    	} else{
	    		$scope.repoContents = [];
	    	}


            $scope.messages = data.messages;
            if($stateParams.all && !$scope.searchActivated){
                $scope.pageTitle = $filter('translate')('all.files');
                $scope.pageTitleDesktopShortened = $scope.pageTitle;
                $scope.pageTitleMobileShortened = $scope.pageTitle;
	    	}
	    	$scope.isLoading =false;
            $scope.testValue = 1;

			if ($scope.messages != null && $scope.messages.length > 0) {
				$scope.isDisplay = true;
			}
            if($scope.searchActivated){
            	repositoryService.setSearchActivatedAlias(true);
            	repositoryService.setPreviousState(null);
            } else{
            	repositoryService.setSearchActivatedAlias(false);	    			
            }
	    }
	    
	    
	    $scope.onClickFolder = false;
	    $scope.onClickFile = function (folder) {
	    	if(!folder.isFolder){
	    		$scope.isLoading =true;	        	
	        	repositoryService.showFile(folder, null, openViewer);
	        }
	        else{
	        	$scope.onClickFolder = true;
	        	if(!repositoryService.getIsInAllFilesPage() && !repositoryService.getSearchActivatedAlias()){
	        		var current_selection = angular.element("#rmstree").jstree("get_selected");
	        		angular.element("#rmstree").jstree("deselect_node", "#"+current_selection);
	        		angular.element("#rmstree").jstree("select_node", "#"+folder.pathId);
	        	}
	        	if($stateParams.all || $scope.searchActivated){
	        		var params = {repoId: folder.repoId, folder: folder};
	        		$state.go(STATE_REPOSITORIES, params, {reload:STATE_HOME});
	        	} else{
	        		getFiles(folder);
	        	}
	        }
	    }
	    
	    $scope.serviceProviderPresent=false;
	    var getAllowedServiceProviders=function(){
	    	serviceProviderService.getAllowedServiceProviders(function(data){
	    		  for(var value in data){
	                  $scope.serviceProviderPresent=true;
	                  break;
	                }  
	        	  var initData = initSettingsService.getSettings();	        
	        	  if(!$scope.serviceProviderPresent && initData.isAdmin){ 
	        		  $scope.message =  $filter('translate')('err.add.service.provider.admin');
	        	  }
	        	  else if(!$scope.serviceProviderPresent && !initData.isAdmin){ 
	        		  $scope.message = $filter('translate')('err.add.service.provider.user');
	        	  }
	    	  });
	     }
	    getAllowedServiceProviders();
	    
	    // Executes with this controller on load
	    if($stateParams.folder){
	    	repositoryService.setCollapseStatus(true);
	    	repositoryService.setCurrentTab($stateParams.folder.repoId)
	    	getFiles($stateParams.folder);
	    	$stateParams.folder = null;
	    } else{
	    	var folder={pathId:'/'}	    	
		    getFiles(folder);
	    }
	    
	    $scope.sortOptions = [
	    {'lookupCode': ['-isFolder','-lastModifiedTime'],'description':'last.modified'},
	    {'lookupCode': ['-isFolder','lastModifiedTime'],'description':'first.modified'},
	    {'lookupCode': ['-isFolder','name'],'description':'filename.ascending'},
	    {'lookupCode': ['-isFolder','-name'],'description':'filename.descending'}
	    ];
    	$scope.selectedSort = $scope.sortOptions[0].lookupCode;
	    $scope.onClickMenu = function (file) {
	    	$scope.selectedFileId=file.pathId;
	    	$scope.toggleMenuMode();
	    }

	    $scope.MenuClickedMode=false;
	    
	    $scope.toggleMenuMode=function(){
	    	$scope.MenuClickedMode=!$scope.MenuClickedMode;
	    }
	    $scope.onClickDownload=function(file){
	    	downloadRepoFile(file.repoType,file.repoId, file.pathId);
	    }
	    $scope.onClickInfo=function(file){
	    	$scope.isLoading =true;
	    	$scope.selectedFile=file;
	    	repositoryService.getFileDetails(file, null, openInfoWindow);
	    }

		var openInfoWindow =function(data){
			$scope.isLoading = false;
			if ((Object.keys(data.tagsMap)) && ((Object.keys(data.tagsMap).length % 2) == 1)) {
 				data.tagsMap[" "] = " ";
			}
			dialogService.info({
				tags:data.tagsMap,
				rights:data.rightsMap.rights,
				fileName:$scope.selectedFile.name,
				fileSize:$scope.selectedFile.fileSize,
				fileType:$scope.selectedFile.fileType,
				repoName:$scope.selectedFile.repoName,
				lastModifiedTime:$scope.selectedFile.lastModifiedTime,
				path:$scope.selectedFile.path
			});
			$scope.toggleMenuMode();
	    }
	    
	    var openViewer = function(data){     	
	    	$scope.isLoading =false;
	    	var redirectUrl=data.viewerUrl;
	    	openSecurePopup(redirectUrl); 		    	        	        
	    }
	    
	    function setMinHeightToTree(){
	  	  var containerHeight = $("#rms-main-container").height();
	  	  var fileListHeaderHeight = $("#fileListHeader").height();
	  	  var footerHeight = initSettingsService.getFooterHeight();	  	  
	  	  var minHeight = containerHeight - fileListHeaderHeight - footerHeight - 40;
		  $("#resizable-tree").css("min-height", minHeight);	  
	    }
	    
	    
	    $scope.showSearch = false;
	    $scope.showRepoFilter = false;
	    $scope.showSort = false;
	    $scope.toggleSearch = function(){
	    	angular.element("#rms-repoTitle-id").hide();
	    	$scope.showSearch = true;
	    }
	    
	    $scope.closeFileListOptions = function(){
	    	$scope.showSearch = false;
	    	$scope.showRepoFilter = false;
	    	$scope.showSort = false;
	    	angular.element("#rms-repoTitle-id").show();
	    }
	    
	    $scope.toggleRepoFilter = function(){
	    	angular.element("#rms-repoTitle-id").hide();
	    	$scope.showRepoFilter = true;
	    }
	    
	    $scope.toggleSort = function(){
	    	angular.element("#rms-repoTitle-id").hide();
	    	$scope.showSort = true;
	    }
	    

	    $scope.treeConfig = {
            core : {
                multiple : false,
                animation: true,
                error : function(error) {
                },
                check_callback : true,
                worker : true
            },
            version : 1,
            types : {
            	"default" : {
            		"icon" : "glyphicon glyphicon-folder-close"
            	}
            },            
            plugins : [ "types" ]
	    };

	    $scope.applyModelChanges = function () {
	    	return true;
	    };
	    
	    $scope.treeEventsObj = {
	      'select_node': selectNode
	    }
	    $scope.treeData = [{id: '/', parent: '#', text: '/', state: {opened: true}}];

	    $scope.dismissMessage = function() {
	        $scope.isDisplay = false;
	    }

	    $scope.shouldDisplay = function() {
			return $scope.isDisplay;
		}

	    function selectNode(event, data) {
	    	if(!$scope.onClickFolder){
	    		var folder = {};
	    		folder.isFolder = true;
	    		folder.pathId = data.selected[0];
	    		folder.path = data.node.original.path;
	    		folder.repoName = data.node.original.repoName;
	    		getFiles(folder);	    	
	    	}
	    }
	    function convertToJSON (data) {
	    	var result = [];
	    	var idx = 0;
	    	data = Array.isArray(data) ? data : [data];
    		for (var i = 0; i < data.length; i++) {
    			var record = data[i];
    			if (record.isFolder === true) {
    				var folder = {};
    				folder.id = record.pathId;
    				folder.parent = getParentNode(record.pathId);
    				folder.text = record.name;
    				folder.state = { opened: true};
    				folder.path = record.path;
    				folder.repoName = record.repoName;
    				if (!!folder.id && !!folder.parent) {
    					result[idx++] = folder;
    				}
    			}
    		}
	    	return result;
	    }
	    
	    function getParentNode(pathId) {
	    	var elements = $scope.treeData;
	    	var parentPathId = pathId.substr(0, pathId.lastIndexOf('/'));
	    	parentPathId = $.trim(parentPathId).length == 0 ? '/' : parentPathId;
	    	for (var idx in elements) {
	    		if (elements[idx].id === parentPathId) {
	    			return elements[idx];
	    		}
	    	}
	    	return null;
	    }
	    
	    function pushNode(newElement) {
	    	var elements = $scope.treeData;
	    	var exists = false;
	    	for (var idx in elements) {
    			var el = elements[idx];
    			if (el.id === newElement.id) {
    				exists = true;
    			}
    		}
	    	if (!exists) {
	    		elements.push(newElement);
	    	}
	    }
	    
}]);