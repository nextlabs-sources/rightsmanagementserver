<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<html>
<head>
  <meta charset="UTF-8">
  <!-- <meta name="viewport" content="width=device-width, initial-scale=0.4, user-scalable=1"> -->
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <title>NextLabs Rights Management</title>
  <link rel="shortcut icon" href="/RMS/ui/css/img/favicon.ico" />
  <link href="ui/lib/bootstrap/3.3.5/css/bootstrap.min.css" rel="stylesheet">
  <link href="ui/lib/font-awesome/4.4.0/css/font-awesome.min.css" rel="stylesheet">
  <link href="ui/lib/angular-ui-switch/angular-ui-switch.css" rel="stylesheet">
  <link href="ui/lib/jstree/3.2.1/css/style.css" rel="stylesheet">
  <link href="ui/lib/ng-resizable/angular-resizable.min.css" rel="stylesheet">

  <link href="ui/css/style.css" rel="stylesheet">
  <link href="ui/css/jstreestyle.css" rel="stylesheet">
  <link href="ui/css/font/fira.css" rel="stylesheet">
 
 <%
 	com.nextlabs.rms.command.GetInitSettingsCommand command = new com.nextlabs.rms.command.GetInitSettingsCommand();
 	com.google.gson.JsonObject jsonObj = command.getInitSettingsJSON(request);
 	String userName = jsonObj.get("userName").isJsonNull() ? "" : jsonObj.get("userName").getAsString();
 	String rmsVersion = jsonObj.get("rmsVersion").isJsonNull() ? "" : jsonObj.get("rmsVersion").getAsString();
 	boolean isRMCConfigured = jsonObj.get("isRMCConfigured").isJsonNull() ? false : jsonObj.get("isRMCConfigured").getAsBoolean();
 %>  
 <script type="text/javascript" src="ui/config/config.js"></script>
</head>

<body data-ng-app="mainApp">
  <div class="cc-layout">
    <div class="cc-header" data-ng-controller="loginController">
      <a href="index.jsp#/home/repositories"><div class="cc-header-logo"></div></a>       
      <div id="rms-header-help-container" class="btn-group rms-login-wrapper user-header-color" style="display:none">
        <button id="rms-header-help" type="button" class="btn btn-sm user-header-color" data-ng-click="help()"></button> 
        <div uib-dropdown class="rms-profile-dropdown">
          <button type="button" class="btn btn-sm cc-login-button-override user-header-color" uib-dropdown-toggle>
            <span style="margin-right: 5px"><%=userName%></span>
            <span class="caret cc-login-caret-override"></span>
            <span class="sr-only">Split button!</span>
          </button>
          <ul class="uib-dropdown-menu rms-login-dropdown-menu-override" role="menu" aria-labelledby="split-button">
            <li role="menuitem"><a href="javascript:void(0)" data-ng-click="doLogout()">Logout</a></li>
          </ul>
        </div> 
      </div>          
    </div>
    <div id="index-loading-background">
      <div id="pgLoading-image-with-border">
              <img  src="ui/css/img/loading-icon.gif" alt="Loading..." />
       </div>
    </div>
    <div id="rms-main-container" class="container-fluid rms-container-fluid">      
      <div data-ng-controller="appController">
	     <div ui-view></div>
      </div>
    </div> 
  </div>
  <script type="text/javascript" src="ui/lib/jquery/1.8.2/jquery.js"></script>
  <script type="text/javascript" src="ui/lib/angular/1.4.7/angular.min.js"></script>
  <script type="text/javascript" src="ui/lib/angular/1.4.7/angular-sanitize.min.js"></script>
  <script type="text/javascript" src="ui/lib/angular/1.4.7/angular-animate.js"></script>
  <script type="text/javascript" src="ui/lib/angular/1.4.7/angular-messages.min.js"></script>
  <script type="text/javascript" src="ui/lib/angular-ui-router/0.2.15/angular-ui-router.min.js"></script>
  <script type="text/javascript" src="ui/lib/angular-ui/bootstrap/ui-bootstrap-tpls-0.14.3.min.js"></script>
  <script type="text/javascript" src="ui/lib/angular-ui-switch/angular-ui-switch.min.js"></script>
  <script type="text/javascript" src="ui/lib/angular-translate/2.8.1/angular-translate.min.js"></script>
  <script type="text/javascript" src="ui/lib/angular-translate/angular-translate-loader-static-files.min.js"></script>
  <script type="text/javascript" src="ui/lib/jstree/3.2.1/js/jstree.js"></script>
  <script type="text/javascript" src="ui/lib/ng-jstree/js/ngJsTree.js"></script>
  <script type="text/javascript" src="ui/lib/ng-file-upload/ng-file-upload.js"></script>
  <script type="text/javascript" src="ui/lib/ng-resizable/angular-resizable.min.js"></script>
  <script type="text/javascript" src="ui/lib/rms/rmsUtil.js"></script>
  <script type="text/javascript">
    var CONTEXT_PATH = "<%=request.getContextPath()%>";
    var initSettingsData = <%=jsonObj.toString()%>;
    $(document).ready(function(){
      $("div#index-loading-background").remove();
      $("div#rms-header-help-container").show();
    });
  </script>
  <script type="text/javascript" src="ui/app/templates.js"></script>
  <script type="text/javascript" src="ui/app/app.js"></script>
  

  
</body>

</html>
