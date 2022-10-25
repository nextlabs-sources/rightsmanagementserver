mainApp.controller('settingsController',['$scope', '$rootScope','$state', 'networkService','configService','loggerService','$location','settingsService', 
function($scope,$rootScope,$state,networkService,configService,loggerService,$location,settingsService){
	var configurations = null;
	//Load the data from server
	$scope.isLoading = false;
	$scope.cachedConfigurations = {};

	$scope.dismissStatus = function() {
		$scope.messageStatus=0;
	}

	$scope.resetDataTypes = function(){
		if($scope.configurations.KM_RMI_KEYSTORE_PASSWORD && $scope.configurations.KM_RMI_KEYSTORE_PASSWORD.length>0){
			$scope.configurations.showKeyStorePassword=false;
		}else{
			$scope.configurations.showKeyStorePassword=true;
		}
		if($scope.configurations.KM_RMI_TRUSTSTORE_PASSWORD && $scope.configurations.KM_RMI_TRUSTSTORE_PASSWORD.length>0){
			$scope.configurations.showTrustStorePassword=false;
		}else{
			$scope.configurations.showTrustStorePassword=true;
		}
		if($scope.configurations.SMTP_PASSWORD && $scope.configurations.SMTP_PASSWORD.length>0){		
			$scope.configurations.showSMTPPassword=false;
		}else{
			$scope.configurations.showSMTPPassword=true;
		}

		$scope.messageStatus=0;

		if($.parseJSON($scope.configurations.ALLOW_REGN_REQUEST)){
			$scope.configurations.ALLOW_REGN_REQUEST = true;
		}else{
			$scope.configurations.ALLOW_REGN_REQUEST = false;
		}

		if($.parseJSON($scope.configurations.SMTP_AUTH)){
			$scope.configurations.SMTP_AUTH = true;
		}else{
			$scope.configurations.SMTP_AUTH = false;
		}

		if($.parseJSON($scope.configurations.SMTP_ENABLE_TTLS)){
			$scope.configurations.SMTP_ENABLE_TTLS = true;
		}else{
			$scope.configurations.SMTP_ENABLE_TTLS = false;
		}

		if($.parseJSON($scope.configurations.ENABLE_USER_LOCATION)){
			$scope.configurations.ENABLE_USER_LOCATION = true;
		}else{
			$scope.configurations.ENABLE_USER_LOCATION = false;
		}

		if($.parseJSON($scope.configurations.ENABLE_REMOTE_PC)){
			$scope.configurations.ENABLE_REMOTE_PC = true;
		}else{
			$scope.configurations.ENABLE_REMOTE_PC = false;
		}
	}

	var loadConfigurationSetting = function(){
		settingsService.getSettings(function(data){
			$scope.configurations = data;
			$scope.cachedConfigurations= angular.copy(data);
			$scope.resetDataTypes();
		});
	}

	var isValidInput = function(data){

		if(data){
			return true;
		}
		return false;
	}

	var isValidNumber = function(data){
		data=data.trim();
		if(!isNaN(data)){
			var input = parseInt(data);
			if(input>0){
				return true;
			}
		}
		return false;
	}

	var isSMTPAuthValid = function(auth,username,password){
		if(!auth){
			return true;
		}
		if(isValidInput(username) && isValidInput(password)){
			return true;
		}
		return false;
	}

   	$scope.scrollTo = function(id) {
		var pos = $("#"+id).position();
		$(".container-fluid.rms-container-fluid").scrollTop(pos.top);
		$(".container-fluid.rms-container-fluid").scrollLeft(pos.left);
   	};
   
   	$scope.doReset = function() {
   		$scope.configurations= angular.copy($scope.cachedConfigurations);
   		$scope.resetDataTypes();
   	};
   	
  	$scope.doSave = function() {
		$scope.isLoading = true;
		var params = {		
			"ALLOW_REGN_REQUEST" : $scope.configurations.ALLOW_REGN_REQUEST,
			"SMTP_HOST" : ($scope.configurations.ALLOW_REGN_REQUEST)?$scope.configurations.SMTP_HOST:"",
			"SMTP_PORT" : ($scope.configurations.ALLOW_REGN_REQUEST)?$scope.configurations.SMTP_PORT:"",
			"SMTP_AUTH" : ($scope.configurations.ALLOW_REGN_REQUEST)?$scope.configurations.SMTP_AUTH:false,
			"SMTP_USER_NAME" : ($scope.configurations.SMTP_AUTH && $scope.configurations.ALLOW_REGN_REQUEST)?$scope.configurations.SMTP_USER_NAME:"",
			"SMTP_PASSWORD" : ($scope.configurations.SMTP_AUTH && $scope.configurations.ALLOW_REGN_REQUEST)?$scope.configurations.SMTP_PASSWORD:"",
			"SMTP_ENABLE_TTLS" : ($scope.configurations.ALLOW_REGN_REQUEST)?$scope.configurations.SMTP_ENABLE_TTLS:false,
			"REGN_EMAIL_SUBJECT" : ($scope.configurations.ALLOW_REGN_REQUEST)?$scope.configurations.REGN_EMAIL_SUBJECT:"",
			"RMS_ADMIN_EMAILID" : ($scope.configurations.ALLOW_REGN_REQUEST)?$scope.configurations.RMS_ADMIN_EMAILID:"",
			"ICENET_URL" : ($scope.configurations.ICENET_URL)?$scope.configurations.ICENET_URL:"",
			"RMC_CURRENT_VERSION" : ($scope.configurations.RMC_CURRENT_VERSION)?$scope.configurations.RMC_CURRENT_VERSION:"",
			"RMC_UPDATE_URL_32BITS" : ($scope.configurations.RMC_UPDATE_URL_32BITS)?$scope.configurations.RMC_UPDATE_URL_32BITS:"",
			"RMC_CHECKSUM_32BITS" : ($scope.configurations.RMC_CHECKSUM_32BITS)?$scope.configurations.RMC_CHECKSUM_32BITS:"",
			"RMC_UPDATE_URL_64BITS" : ($scope.configurations.RMC_UPDATE_URL_64BITS)?$scope.configurations.RMC_UPDATE_URL_64BITS:"",
			"RMC_CHECKSUM_64BITS" : ($scope.configurations.RMC_CHECKSUM_64BITS)?$scope.configurations.RMC_CHECKSUM_64BITS:"",
			"SESSION_TIMEOUT_MINS" : ($scope.configurations.SESSION_TIMEOUT_MINS)?$scope.configurations.SESSION_TIMEOUT_MINS:"",
			"ENABLE_USER_LOCATION" : $scope.configurations.ENABLE_USER_LOCATION,
			"POLICY_USER_LOCATION_IDENTIFIER" : ($scope.configurations.ENABLE_USER_LOCATION)?$scope.configurations.POLICY_USER_LOCATION_IDENTIFIER:"",
			"LOCATION_UPDATE_FREQUENCY" : ($scope.configurations.ENABLE_USER_LOCATION)?$scope.configurations.LOCATION_UPDATE_FREQUENCY:"",
			"ENABLE_REMOTE_PC" : $scope.configurations.ENABLE_REMOTE_PC,
			"KM_POLICY_CONTROLLER_HOSTNAME" : ($scope.configurations.ENABLE_REMOTE_PC)?$scope.configurations.KM_POLICY_CONTROLLER_HOSTNAME:"",
			"KM_RMI_PORT_NUMBER" : ($scope.configurations.ENABLE_REMOTE_PC)?$scope.configurations.KM_RMI_PORT_NUMBER:"",
			"KM_RMI_KEYSTOREFILE" : ($scope.configurations.ENABLE_REMOTE_PC)?$scope.configurations.KM_RMI_KEYSTOREFILE:"",
			"KM_RMI_KEYSTORE_PASSWORD" : ($scope.configurations.ENABLE_REMOTE_PC)?$scope.configurations.KM_RMI_KEYSTORE_PASSWORD:"",
			"KM_RMI_TRUSTSTORE_PASSWORD" : ($scope.configurations.ENABLE_REMOTE_PC)?$scope.configurations.KM_RMI_TRUSTSTORE_PASSWORD:"",
			"KM_RMI_TRUSTSTOREFILE" : ($scope.configurations.ENABLE_REMOTE_PC)?$scope.configurations.KM_RMI_TRUSTSTOREFILE:"",
			"EVAL_RMI_PORT_NUMBER" : ($scope.configurations.ENABLE_REMOTE_PC)?$scope.configurations.EVAL_RMI_PORT_NUMBER:""
		};

		settingsService.saveSettings(function(data){
			if(data.message){
				$scope.message = data.message;
				$scope.messageStatus = 1;
			}else{
				$scope.configurations = data;
				$scope.cachedConfigurations= angular.copy(data);
				$scope.resetDataTypes();
				$scope.message = 'Your settings have been saved successfully.';
				$scope.messageStatus = 2;
			}
			$scope.isLoading = false;
			$scope.scrollTo('settings-pane');
		},params,function(response){
			$scope.isLoading = false;
		});
   	}

   	$scope.showKeyStorePassword = function(){
   		$scope.configurations.showKeyStorePassword = true;
   	}

   	$scope.checkPCConnection = function(){
   		$scope.connectionLoading=true;
   		var params = {
			"km_policy_controller_hostname" : $scope.configurations.KM_POLICY_CONTROLLER_HOSTNAME,
			"km_rmi_port_number" : $scope.configurations.KM_RMI_PORT_NUMBER,
			"km_rmi_keystoreFile" : $scope.configurations.KM_RMI_KEYSTOREFILE,
			"km_rmi_keystorePass" : $scope.configurations.KM_RMI_KEYSTORE_PASSWORD,
			"km_rmi_truststoreFile" : $scope.configurations.KM_RMI_TRUSTSTOREFILE,
			"km_rmi_truststorePass" : $scope.configurations.KM_RMI_TRUSTSTORE_PASSWORD,
			"eval_policy_controller_hostname" : $scope.configurations.KM_POLICY_CONTROLLER_HOSTNAME,
			"eval_rmi_port_number" : $scope.configurations.EVAL_RMI_PORT_NUMBER
		};
   		settingsService.checkPCConnection(function(data){
   			result = data.result;
			if(data.result==null) {
				$scope.messageStatus=1;
				$scope.message = "Connection Error."
			}
			else if(result){
				$scope.messageStatus = 2; 
				$scope.message =  "Connected to the policy controller.";
			}
			else{
				$scope.messageStatus = 1;
				$scope.message = data.message;
			}
			$scope.connectionLoading=false;
			$scope.scrollTo('settings-pane');
   		},params);
   	}

   	$scope.showTrustStorePassword = function(){
   		$scope.configurations.showTrustStorePassword = true;
   	}
   	$scope.showSMTPPassword = function(){
   		$scope.configurations.showSMTPPassword = true;
   	}
   	$scope.frequency = ["Never","Daily","Weekly","Monthly"];
   	
   	$scope.setFormDirty = function(formName){   	   
   		formName.$setDirty();
   	}
		
	loadConfigurationSetting();
	
}]);
