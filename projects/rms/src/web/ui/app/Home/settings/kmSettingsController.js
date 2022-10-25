mainApp.controller('kmSettingsController',['$scope','$rootScope','$filter','$state','networkService','configService','loggerService','dialogService','$location', 'kmSettingsService',
function($scope,$rootScope,$filter,$state,networkService,configService,loggerService,dialogService,$location,kmSettingsService){
	
	$scope.keyrings = [];
	$scope.selected_keyring;
	$scope.kmProvider;
	$scope.message = "";
	$scope.isLoading = false;
	
	$scope.dismissStatus = function() {
		$scope.messageStatus=0;
	}
	
	
	var getKeyRings = function(){
		$scope.isLoading = true;
		kmSettingsService.getAllKeyRings(function(data) {			
			if(data.length>0) {
				$scope.keyrings = data;
				selectKeyRing();
			} else {
				$scope.keyrings = [];
			}
			$scope.isLoading = false;
		},function(data){
			handleError(data.message);
		})
	}
	
	var getKeyRingsAndSelectSpecificKeyRing = function(keyRingName) {
		$scope.isLoading = true;
		kmSettingsService.getAllKeyRings(function(data) {			
			if(data.length>0) {
				$scope.keyrings = data;
				selectSpecificKeyRing(keyRingName);
			}
			$scope.isLoading = false;
		},function(data){
			handleError(data.message);
		})
	}
	
	var getTenantDetails = function(){
		$scope.isLoading = true;
		kmSettingsService.getTenantDetails(
			function(data) {
				$scope.isLoading = false;
				$scope.kmProvider = data.tenantProvider;
				if($scope.kmProvider.toUpperCase()==="DEFAULT"){
					$scope.kmProvider = $filter('translate')('kmSettings.provider.default');
				}
			},function(data){
				$scope.kmProvider = $filter('translate')('not-applicable');
				handleError(data.message);
			})
	};
	
	var generateKey = function(){
		$scope.isLoading = true;
    	kmSettingsService.generateKey(
			$scope.selected_keyring.name,
			function(data) {
				handleSuccess($filter('translate')('kmSettings.genKey.success'));
				getKeyRings();
			},function(data){
				handleError(data.message);
			})
	}
	
	$scope.exportKeyRing = function(){
		kmSettingsService.exportKeyRing(
			$scope.selected_keyring.name,
			function(data) {
				if(data.filePath != null) {
					window.location.href = data.filePath;
				} else {
					handleError($filter('translate')('kmSettings.exportKeyRing.error'));
				}
			},function(data){
				handleError(data.message);
			})
	}

	$scope.generateKey = function(){
		if($scope.selected_keyring.keys.length == 0) {
			generateKey();
		} else {
			dialogService.confirm({
			    msg: $filter('translate')('kmSettings.genKey.confirm'),
			    ok: function(){
			    	generateKey();
			    }
			});
		}
	}
	
	$scope.onClickKeyRing = function(keyRingName){
		var i;
		for(i=0; i<$scope.keyrings.length; i++){
			if($scope.keyrings[i].name == keyRingName) {
				break;
			}
		}
		$scope.selected_keyring = $scope.keyrings[i];
	}
	
	var selectKeyRing = function(){
		if($scope.keyrings.length>0) {
			if($scope.selected_keyring == null) {
				$scope.selected_keyring = $scope.keyrings[0];
			} else {
				selectSpecificKeyRing($scope.selected_keyring.name);
			}
		} else {
			$scope.selected_keyring = null;
		}
	}
	
	function selectSpecificKeyRing (keyRingName){
		var krFound = false;
		for(i=0; i<$scope.keyrings.length; i++){
			if($scope.keyrings[i].name == keyRingName) {
				krFound = true;
				break;
			}
		}
		if (krFound == true) {
			$scope.selected_keyring = $scope.keyrings[i];
		} else {
			$scope.selected_keyring = $scope.keyrings[0];
		}
	}
	
	
	$scope.isCurrentlySelectedKeyRing = function(keyRingName) {
	    return $scope.selected_keyring.name == keyRingName;
	}
	
	$scope.isActiveKey = function(keyId) {
	    return $scope.selected_keyring.keys[0].id == keyId;
	}
	
	function handleSuccess(message){
		$scope.isLoading = false;
		$scope.messageStatus = 2;
		$scope.message = message;
	}
	
	function handleError(message){
		$scope.isLoading = false;
		$scope.messageStatus = 1;
		if(message == null) {
			$scope.message = $filter('translate')('kmSettings.default.error');
		} else {
			$scope.message = message;
		}
	}
	
	getTenantDetails();
	getKeyRings();

}]);