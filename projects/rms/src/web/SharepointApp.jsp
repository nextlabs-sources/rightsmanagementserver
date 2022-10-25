<!DOCTYPE html>
<html>

<head>
<meta charset="utf-8">
<title>NextLabs Rights Management</title>
  <link rel="shortcut icon" href="/ui/css/img/favicon.ico" />
  <!--<link rel="stylesheet" href="css/normalize.css">-->
  <link rel="stylesheet" href="/ui/css/bootstrap.min.css">
  <link rel="stylesheet" href="/ui/css/seccoll.css">
  <link rel="stylesheet" href="/ui/css/style.css">
</head>
<body style="margin:0px">
<div id="pageWrapper" class="pageWrapper" >
    <div id="header" class="pageheader">
        <span class="verticalAlignHelper"></span>
        <a href="#/home/repo/displayRepoContents"><img src="img/NextLabs.svg" style="vertical-align:middle" height="46px"/></a>
      	<span class="pull-right">      
        
    </div> <!--header-->
    <br/>
    <h3>Rights Management SharePoint App</h3>
    <div style="display: inline-block;white-space: nowrap;">
	    <div style="display: inline-block;white-space: nowrap;">
	    	<img src="/ui/css/img/SampleImage.png"></img>
	    </div>
	    <div style="display: inline-block;white-space: nowrap;">
	    	<img src="/ui/css/img/SampleImage2.png"></img>
	    </div>
    </div>
    <div>
		<h4>Details</h4>
		<br/>
		Description:
		The Rights Management App gives you the ability to view .nxl (encrypted) files via NextLabs' web based Rights Management application. You can install this app for any SharePoint Site and then right-click on an encrypted file to select the option to view it using Secure Collaboration.
		Rights Management evaluates each request based on your deployed information risk policy and determines which Users are permitted to view an encrypted file. Each file is displayed in a secure external web browser window. 
	</div>
	<br/>
    <div id="footer">
        <div style="text-align:center">
          <p class="text-muted credit text-center">&copy; <%= com.nextlabs.rms.config.GlobalConfigManager.getInstance().getCopyrightYear()%> <a href="http://www.nextlabs.com">NextLabs Inc.</a> All rights reserved.</p>
        </div>
	</div><!--footer-->
</div><!--pageWrapper-->
 </body>
</html>
