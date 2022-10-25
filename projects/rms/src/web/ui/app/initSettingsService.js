mainApp.factory('initSettingsService', ['$http', 'loggerService', function($http, loggerService){
	var initSettings = null;
	var RMSContextName = null;
	var onlineHelpPage = null;

	var getOnlineHelpPage = function(data){
		if(data === "Repositories"){
			onlineHelpPage = "Repositories.htm";
		} else if(data === "ManageRepositories"){
			onlineHelpPage = "Manage_Repositories.htm";
		} else if(data === "Configuration"){
			onlineHelpPage = "Configuration.htm";
		} else if(data === "ServiceProviders"){
			onlineHelpPage = "ServiceProviders.htm";
		} else if(data === "KeyManagement"){
			onlineHelpPage = "kmSettings.htm";
		}
		return onlineHelpPage;
	}
	
	var loadSettings = function(data) {		
		initSettings = data;
	};
	
	var getSettings = function() {
		return initSettings;
	};
	
	var setRMSContextName = function(data){
		RMSContextName = data;
	}
	
	var getRMSContextName = function(){
		return RMSContextName;
	}
	
	var setRightPanelMinHeight = function(){
		var containerHeight = $("#rms-main-container").height();
		var footerHeight = getFooterHeight();		
	    $("#rms-right-panel").css("min-height", containerHeight - footerHeight);
	}
	
	var getFooterHeight = function(){
		var footerHeight = null;
		if($("#rms-home-footer-desktop").height() != null && $("#rms-home-footer-desktop").height() != 0){
			var footerHeight = $("#rms-home-footer-desktop").height();			
		} 
		if($("#rms-home-footer-mobile").height() != null && $("#rms-home-footer-mobile").height() != 0){
			var footerHeight = $("#rms-home-footer-mobile").height();			
		}
		return footerHeight;
	}
	
	var getLogoutUrl = function(){
		return "/RMS/RMSViewer/Logout";
	}
	
  	return {
	    loadSettings: loadSettings,
	    getSettings: getSettings,
	    setRMSContextName:setRMSContextName,
	    getRMSContextName:getRMSContextName,
	    getOnlineHelpPage:getOnlineHelpPage,
	    setRightPanelMinHeight:setRightPanelMinHeight,
	    getLogoutUrl:getLogoutUrl,
	    getFooterHeight:getFooterHeight
  	};
}]);