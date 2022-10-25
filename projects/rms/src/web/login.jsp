<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
  <head>
  <%
  String webContext=request.getContextPath();
  String action = request.getParameter("action");
  if(action!=null && action.equalsIgnoreCase("logout")){
	  session.invalidate();
  }
  %>
  

	<link rel="shortcut icon" href="ui/css/img/favicon.ico" />
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">    
    <meta name="description" content="">
    <meta name="author" content="">
    <meta name="viewport" content="width=device-width, initial-scale=1">    

    <title>NextLabs Rights Management</title>

    <!-- Bootstrap core CSS -->
	<link href="<%=webContext%>/ui/css/bootstrap.min.css" rel="stylesheet">
    <link href="<%=webContext%>/ui/lib/font-awesome/4.4.0/css/font-awesome.min.css" rel="stylesheet">
	<link href="<%=webContext%>/ui/css/font/fira.css" rel="stylesheet">
	<link href="<%=webContext%>/ui/css/style.css" rel="stylesheet">
	<link href="<%=webContext%>/ui/css/login.css" rel="stylesheet">
	<script>

		var existingSubFn;
		
	function initLoginPage(){
		validityCheck();
		addRandomInput();
	}
	
	function stripRandomInput() {
		var inp = document.getElementsByTagName('input');
		for(var i=0; i< inp.length; i++){
			if(inp[i].getAttribute('type') == "text" || inp[i].getAttribute('type') == "password"){
				inp[i].setAttribute("name",inp[i].getAttribute('name').split("_")[0]);
				inp[i].setAttribute("id",inp[i].getAttribute('id').split("_")[0]);
			}
		}
		if(eval(existingSubFn))
		document.forms[0].submit();
	}

	function validateInputs(){
        if (!checkCookiesAndMsg()) {
            return false;
        }

		if($("#domainName").val()==-1){
			alert("Please select a domain");
			$("#domainName").focus();
			return false;
		}
		stripRandomInput();
	}

	function addRandomInput() {
		checkActionAndMsg();
		checkCookiesAndMsg();
		document.forms[0].setAttribute("autocomplete","off");
		var t = new Date().getTime();
		var inp = document.getElementsByTagName('input');
		for(var i=0; i< inp.length; i++){
			if(inp[i].getAttribute('type') == "text" || inp[i].getAttribute('type') == "password"){
				inp[i].setAttribute("autocomplete","off");
				inp[i].setAttribute("name",inp[i].getAttribute('name')+"_"+t);
				inp[i].setAttribute("id",inp[i].getAttribute('id')+"_"+t);
				inp[i].focus();
			}
		}
		inp[0].focus();
		existingSubFn = document.forms[0].getAttribute("onsubmit");
		document.forms[0].onsubmit = validateInputs;
		}
	
	function checkActionAndMsg(){
		var action="<%=request.getParameter("action")%>";
		if (action!=null && action=="logout") {
			var msgDiv=document.getElementById("display-msg");
			msgDiv.innerHTML="<%= com.nextlabs.rms.locale.RMSMessageHandler.getClientString("login.logout_message") %>";
			msgDiv.style.visibility='visible';
			return;
		} else if(action!=null && action=="session_timed_out") {
			var msgDiv=document.getElementById("display-msg");
			msgDiv.innerHTML="<%= com.nextlabs.rms.locale.RMSMessageHandler.getClientString("login.session_expired") %>";
			msgDiv.style.visibility='visible';
			return;			
		}
		var mssg="<%=request.getParameter("msg")%>";
		if (mssg!=null && mssg!="null") {
			var error = document.getElementById("display-error");
			error.innerHTML += mssg;
			error.style.visibility = 'visible';
		}
	}

    function checkCookiesAndMsg() {
        if (!areCookiesEnabled()) {
            var error = document.getElementById("display-error");
            if (error.style.visibility != 'visible') {
                error.innerHTML += '<%= com.nextlabs.rms.locale.RMSMessageHandler.getClientString("login.enable_cookies") %>';
                error.style.visibility = 'visible';
            }

            return false;
        }
        else {
            return true;
        }
    }

    function areCookiesEnabled() {
        var cookieEnabled = (navigator.cookieEnabled) ? true : false;

        if (typeof navigator.cookieEnabled == "undefined" && !cookieEnabled) {
            document.cookie="testcookie";
            cookieEnabled = (document.cookie.indexOf("testcookie") != -1) ? true : false;
            if (cookieEnabled) {
                document.cookie = 'testcookie=;expires=Thu, 01 Jan 1970 00:00:01 GMT;'
            }
        }
        return (cookieEnabled);
    }

    window.onload = initLoginPage;
	
	function hasHtml5Validation () {
	 return typeof document.createElement('input').checkValidity === 'function';
	}
	
	function validityCheck(){
		if (hasHtml5Validation()) {		 		 		
		 $('.validate-form').submit(function (e) {
		   if (!this.checkValidity()) {
			 // Prevent default stops form from firing			 
			 e.preventDefault();
			 $(this).addClass('invalid');			 
			 $('form').find("input[type=text]").each(function(ev){
				     if(!$(this).val()) {
				     	$(this).attr("placeholder", '<%= com.nextlabs.rms.locale.RMSMessageHandler.getClientString("usernameRequired")%>');
				  	}
			 });
			 $('form').find("input[type=password]").each(function(ev){
				     if(!$(this).val()) { 
				     	$(this).attr("placeholder", '<%= com.nextlabs.rms.locale.RMSMessageHandler.getClientString("passwordRequired") %>');
				     }
			 });			 
		   } else {
			 $(this).removeClass('invalid');			 
		   }
		 });
		}
	}
	
	function oktaLogin() {
		window.location = '${pageContext.request.contextPath}/OktaAuth/AuthStart' + window.location.search;
	}
	
	function samlLogin(){
		window.location = '${pageContext.request.contextPath}/SAML/AuthStart' + window.location.search;
	}
	
</script>
	<style type="text/css">						
		.arrow_box {
			display: block;
			position: relative;							
			background: #ffffff;						
			border: 1px solid grey;						
			position: absolute;							
			color: black;								
			z-index: 1;									
			white-space: nowrap;						
			padding: 12px;								
			margin-left: 5px;							
			float: right;								
		}												
		.arrow_box:after, .arrow_box:before {			
			bottom: 100%;								
			left: 50%;									
			border: solid transparent;					
			content: " ";								
			height: 0;									
			width: 0;									
			position: absolute;							
			pointer-events: none;						
		}												
		.arrow_box:after {								
			border-color: rgba(255, 255, 255, 0);		
			border-bottom-color: #ffffff;				
			border-width: 7px;							
			margin-left: -7px;							
		}												
		.arrow_box:before {								
			border-color: rgba(0, 0, 0, 0);				
			border-bottom-color: #000000;				
			border-width: 8px;							
			margin-left: -8px;							
		}	

		/* .invalid class prevents CSS from automatically applying */
		.invalid input:required:invalid {
		  background: #BE4C54;
		}
		 
		/* Mark valid inputs during .invalid state */
		.invalid input:required:valid {
		  background: #17D654 ;
		}		
	</style>
  </head>

  <body>
  	<div id="cont">
      <div class="left-column small-screen-hide">
        <div class="login-left-column">
            <div class="inline-block cc-layout-full-width" style="padding: 50px 0 0 50px;">
                <div class="logo"></div>
            </div>
            <div>
                <img class="login-left-col" src="ui/css/img/login-screen-graphics.png"/>
                <div class="welcome-msg"><%= com.nextlabs.rms.locale.RMSMessageHandler.getClientString("login.welcome") %></div>
                <div class="description-msg"><%= com.nextlabs.rms.locale.RMSMessageHandler.getClientString("login.description") %></div>
            </div>
        </div>
      </div>
	  <div class="right-column">
	  	<div class="login-right-column">
		    <div class="wrapper"> 
				<img class="rms-logo" src="ui/css/img/rms-logo.png"/>
				<div class="title"> <%= com.nextlabs.rms.locale.RMSMessageHandler.getClientString("login.account") %></div>
				<div class="login-box">
					<form method="post" action="RMSViewer/Login" class="validate-form">
						<div id="display-msg" style="text-align:left;margin-bottom:5px;max-width:100%; visibility:hidden;" >
						</div>
						<input type="text" name="userName" id="userName" placeholder='<%= com.nextlabs.rms.locale.RMSMessageHandler.getClientString("login.username") %>' required="required" autocomplete="off" oninvalid="this.setCustomValidity('<%= com.nextlabs.rms.locale.RMSMessageHandler.getClientString("usernameRequired") %>')" oninput="this.setCustomValidity('')"/>
						<br>
						<input type="password" name="password" id="password" placeholder='<%= com.nextlabs.rms.locale.RMSMessageHandler.getClientString("login.password") %>' required="required" oninvalid="this.setCustomValidity('<%= com.nextlabs.rms.locale.RMSMessageHandler.getClientString("passwordRequired") %>')" oninput="this.setCustomValidity('')"/>
						<br>
						<%
						String[] domainNames = com.nextlabs.rms.config.GlobalConfigManager.getInstance().getDomainNames();
						%>
						<select name="domainName" id="domainName" data-ng-model="domain" required="required" onmousedown="$(this).focus()">
							<option value="-1">select a domain</option>
							<%
							for(String domainName:domainNames){ 
							if((domainName!="")){
							%>
								<option value="<%=domainName%>" <%=domainNames.length==1?"selected":""%>><%=domainName%></option>
							<%} } %>
						</select>
						<br>
						<div id="display-error" style="text-align:left;margin-bottom:5px;margin-top:5px;max-width:100%; visibility:hidden;" >
							<img src="ui/css/img/error.png" alt="Error" /> 
						</div>
						
						<button class="btn btn-default rms-login-button" style="float: left" type="submit"><%= com.nextlabs.rms.locale.RMSMessageHandler.getClientString("login.login_btn") %></button>
						<a href="<%=webContext%>/help/Login_Page.htm" onclick="openCenteredPopup(this.href,'myWindow','1200','800','yes');return false"><i style="margin:10px" class="fa fa-question-circle" aria-hidden="true"></i></a>
						<div class="clearfix"></div>
						<%
							com.nextlabs.rms.config.GlobalConfigManager configManager = com.nextlabs.rms.config.GlobalConfigManager.getInstance();
							if(configManager.getOktaServerInfo()!= null || configManager.getSAMLConfig() != null){
						%>
						
						<div id="or-divider-div" style="margin-top: 20px">
							<hr id="left-divider" style="width:44%; float:left">
							<p id="or-para-tag" style="display: inline-block; margin-top: 10px"><span id="loginPage-divider" style="padding-left: 9px; font-size: 17px; padding-top: 20px;">OR</span></p>
							<hr id="right-divider" style="width:44%; float:right">
						</div>
						<% if(configManager.getOktaServerInfo() != null) { %>
						<button class="btn btn-default" style="background: #5d97f4; color: white; height: 40px; width: 190px; padding-top: 10px; margin: 15px 0px 0 5px;" type="button" onclick="oktaLogin()"><%= com.nextlabs.rms.locale.RMSMessageHandler.getClientString("login.okta_btn") %></button>
						<% } %>
						<% if(configManager.getSAMLConfig() != null) { %>
						<button class="btn btn-default" style="background: #5d97f4; color: white; height: 40px; padding-top: 10px; margin: 15px 0px 0 5px;" type="button" onclick="samlLogin()">
							<span style="min-width: 165px; max-width: 275px; overflow: hidden; white-space: nowrap; display: block; text-overflow: ellipsis;">
							<% String btn = configManager.getStringProperty(com.nextlabs.rms.config.GlobalConfigManager.SAML_LOGIN_BTN_TEXT); %>
							<%= btn.length() == 0 ? com.nextlabs.rms.locale.RMSMessageHandler.getClientString("login.saml_btn") : btn %>
							</span>
						</button>
						<% } %>
						<%
							}
						%>
						<%if(com.nextlabs.rms.config.ConfigManager.getInstance(com.nextlabs.rms.config.GlobalConfigManager.DEFAULT_TENANT_ID).getBooleanProperty(com.nextlabs.rms.config.ConfigManager.ALLOW_REGN_REQUEST)){%>
						<br/>
						<div class="login-request-account">
							<a class="pointer-click underline" onclick="openCenteredPopup('register.jsp','NextLabs Rights Management','800','600','yes');return false" ><%= com.nextlabs.rms.locale.RMSMessageHandler.getClientString("login.register") %></a>
						</div>
						<%}%>								
					</form>	
				</div>
			</div>
		</div>
        <div id="footer" class="login-footer">
            <center id="footer-links">
            <span>
                <p class="text-muted credit text-center" style="padding: 10px; font-size:10px;">THIS PROGRAM IS CONFIDENTIAL AND PROPRIETARY TO NEXTLABS, INC. AND MAY NOT BE REPRODUCED, PUBLISHED OR DISCLOSED TO OTHERS WITHOUT COMPANY AUTHORIZATION.</p>
                <p class="text-muted credit text-center">&copy; <%= com.nextlabs.rms.config.GlobalConfigManager.getInstance().getCopyrightYear()%> <a href="http://www.nextlabs.com">NextLabs Inc.</a> All rights reserved.</p>
            </span>
            </center>   
        </div>
	  </div>
	</div>
	<!-- Bootstrap core JavaScript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
	  <script src="<%=webContext%>/ui/lib/3rdParty/jquery-1.10.2.min.js"></script>
  	  <script type="text/javascript">
		var popupWindow = null;
		function openCenteredPopup(url,winName,w,h,scroll){
		LeftPosition = (screen.width) ? (screen.width-w)/2 : 0;
		TopPosition = (screen.height) ? (screen.height-h)/2 : 0;
		settings = 'height='+h+',width='+w+',top='+TopPosition+',left='+LeftPosition+',scrollbars='+scroll+',resizable';
		popupWindow = window.open(url,winName,settings);
		popupWindow.focus();
		}

        function adjustRightColumnHeight(){
            var wrapper = document.getElementsByClassName("wrapper")[0];
            if(!wrapper){
                return;
            }
            var footerHeight = $('#footer').outerHeight() + (isIE10 ? 0 : 40);
            var windowHeight = window.innerHeight;
            var totalHeight = $('.wrapper').first().height() + footerHeight;
            if(totalHeight > 0 && totalHeight < windowHeight) {
                var padding = (windowHeight - totalHeight)/2;
                if(padding > 100){
                    wrapper.style.paddingTop = "100px";
                    wrapper.style.paddingBottom =  (2*padding-100)+"px";
                } else {
                    wrapper.style.paddingTop = padding + "px";
                    wrapper.style.paddingBottom = padding + "px";
                }
            } else {
                wrapper.style.paddingTop = "10px";
                wrapper.style.paddingBottom = "10px";
            }
        }
        
        $(window).resize(function() {
            adjustRightColumnHeight();
        });
        
        var isIE10 = false;
        $(function() {
            /*@cc_on
            if (/^10/.test(@_jscript_version)) {
                isIE10 = true;
            }
            @*/
            adjustRightColumnHeight();
        });

		// fix for IE9 lack of support for "required" and "placeholder"
		if ($("<input />").prop("required") === undefined) {	
		
			$('<span class="arrow_box" id="userName_arrow_box"><%= com.nextlabs.rms.locale.RMSMessageHandler.getClientString("usernameRequired") %></span>').insertAfter( "#userName" );
			$('<span class="arrow_box" id="password_arrow_box"><%= com.nextlabs.rms.locale.RMSMessageHandler.getClientString("passwordRequired") %></span>').insertAfter("#password");
		
			$("#userName").focus(function(){
				//Check to see if the user has modified the input, if not then remove the placeholder text
				if($(this).val() == $(this).attr("placeholder")){
					$(this).val("").css("color","black");
				}
				$("#userName_arrow_box").hide();
				$("#password_arrow_box").hide();
			});
			$("#userName").blur(function(){
				//Check to see if the use has modified the input, if not then populate the placeholder back into the input
				if( $(this).val() == ""){
					$(this).val($(this).attr("placeholder")).css("color","#aaa");
				}
			});
			$("#password").focus(function(){
				if($(this).val() == $(this).attr("placeholder")){
					$(this).val("").css("color","black");
				}
				$("#userName_arrow_box").hide();
				$("#password_arrow_box").hide();

			});
			$("#password").blur(function(){
				if( $(this).val() == ""){
					$(this).val($(this).attr("placeholder")).css("color","#aaa");
				}
			});
			
			$("#userName_arrow_box").hide();
			$("#password_arrow_box").hide();
		
			$(document).on("submit", function(e) {
				$(this).find("input")
					   .filter("[required]")
					   .filter(function() { return this.value == '' || this.value==$(this).attr("placeholder"); })
					   .each(function() {
							e.preventDefault();
							if(this.id == "userName" ) {
								$("#userName_arrow_box").show();
								return false;
							}
							else if (this.id == "password" ) {
								$("#password_arrow_box").show();
								return false;
							}
						});
			});
		}
	</script>	
  </body>
</html>
