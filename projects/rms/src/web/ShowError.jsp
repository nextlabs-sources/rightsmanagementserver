<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
  <head>
  <%
  String webContext=request.getContextPath();
  %>
  
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">
    <link rel="shortcut icon" href="ui/css/img/favicon.ico" />


    <title>NextLabs Rights Management</title>

    <!-- Bootstrap core CSS -->
    <link href="<%=webContext%>/ui/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom styles for this template -->
    <link href="<%=webContext%>/ui/css/signin.css" rel="stylesheet">
    <link href="<%=webContext%>/ui/css/seccoll.css" rel="stylesheet">

	<script>
	
		
		

	function checkErrorMsg(){
		var errId='<%=request.getParameter("errId")%>';
		var errMsg='<%=request.getParameter("errMsg")%>';
		
		$('div.container-error').block({message: '',overlayCSS: { backgroundColor: '#fff' }});
		if(errId!=null && errId!="null"){
			$.get('/RMS/RMSViewer/GetErrorMsg?errorId='+errId, function(data, status){
			errMsg=data.message;
			if(errMsg!=null && errMsg!="null"){
				var error=document.getElementById("display-error");
				error.innerHTML=errMsg;
				error.style.visibility='visible';
				}
			});	
		}
		if(errMsg!=null && errMsg!="null"){
			errMsg=decodeURIComponent(errMsg);
			var error=document.getElementById("display-error");
			error.innerHTML=errMsg;
			error.style.visibility='visible';
		}
	$('div.container-error').unblock();
	}

	function deleteCache(){
		if(errId!=null && errId!="null") {
			$.get('/RMS/RMSViewer/RemoveFromCache?documentId='+errId);
		}
	}

	window.onload = checkErrorMsg;
	window.onbeforeunload = deleteCache;
										
	
</script>
  </head>

  <body >
    <br>
      <div class="container-error">
	  	<div class="row">
       	 <div id="display-error" style="text-align: center; vertical-align: middle;margin-bottom:1px;margin-left:auto;margin-right:auto; visibility:hidden;" >
			<img src="ui/css/img/error.png" alt="Error" /> 
		 </div>
		</div>
	  </div>
	<style type="text/css">
		.display-error
	{	
	border: 1px solid #D8D8D8;
	padding: 5px;
	border-radius: 5px;
	font-family: Arial;
	font-size: 15px;
	background-color: rgb(255, 249, 242);
	color: rgb(211, 0, 0);
	text-align: center;
}
		</style>
 <div id="footer">
			<div class="container">
				<p class="text-muted credit text-center">&copy; <%= com.nextlabs.rms.config.GlobalConfigManager.getInstance().getCopyrightYear()%> <a href="http://www.nextlabs.com">NextLabs Inc.</a> All rights reserved.</p>
			</div>
 </div>
    <!-- Bootstrap core JavaScript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
	  <script src="<%=webContext%>/ui/lib/3rdParty/jquery-1.10.2.min.js"></script>
	  <script src="<%=webContext%>/ui/lib/3rdParty/jquery.blockUI.js"></script>
  
  </body>
</html>
