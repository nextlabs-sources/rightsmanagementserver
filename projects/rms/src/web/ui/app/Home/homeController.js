mainApp.controller('homeController',['$scope','$rootScope', 'initSettingsService',
	function($scope,$rootScope,initSettingsService){
		$scope.checked = true;		
        $scope.toggleSideBar = function(){
        	$scope.checked = !$scope.checked
        	if(!$scope.checked){
        		$("#rms-home-footer-mobile").css("visibility", "hidden");        		
        	} else{
        		$("#rms-home-footer-mobile").css("visibility", "visible");
        	}        	
        }
        $rootScope.$on("toggleSideBar", function(){
       		$scope.toggleSideBar();       		
    	});
        
        $scope.doLogout = function(){        	
			window.location=initSettingsService.getLogoutUrl();
		};
	}
]);