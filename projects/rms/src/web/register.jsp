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
    <link rel="shortcut icon" href="/ui/css/img/favicon.ico" />
    <title>NextLabs Rights Management - Request an account</title>

    <!-- Bootstrap core CSS -->
    
    <link href="<%=webContext%>/ui/css/bootstrap.min.css" rel="stylesheet">
    <script src="<%=webContext%>/ui/lib/3rdParty/jquery-1.10.2.min.js"></script>
    <script src="<%=webContext%>/ui/lib/3rdParty/bootstrap.min.js"></script>
	  <link href="<%=webContext%>/ui/css/style.css" rel="stylesheet">
    <!-- Custom styles for this template -->
    <link href="<%=webContext%>/ui/css/signin.css" rel="stylesheet">
    <link href="<%=webContext%>/ui/css/seccoll.css" rel="stylesheet">
    <script>
     window.onload = checkMsg;
	 var popupWindow = null;
	function openCenteredPopup(url,winName,w,h,scroll){
		LeftPosition = (screen.width) ? (screen.width-w)/2 : 0;
		TopPosition = (screen.height) ? (screen.height-h)/2 : 0;
		settings = 'height='+h+',width='+w+',top='+TopPosition+',left='+LeftPosition+',scrollbars='+scroll+',resizable';
		popupWindow = window.open(url,winName,settings);
		popupWindow.focus();
	}
	 
     function checkMsg()
 	 {
 		var err="<%=request.getParameter("error")%>";
 		var suc="<%=request.getParameter("success")%>";
 		var element;
 		if(err!=null && err!="null"){
 			element=document.getElementById("display-error");
 			element.innerHTML=err;
 		}
 		else if(suc!=null && suc!="null"){
 			element=document.getElementById("display-success");
 			element.innerHTML=suc;
 		}
		if(element!=null){
	 		element.style.visibility='visible';
			var content = document.getElementById("contentDiv");
			content.style.visibility = 'hidden';
			content.parentNode.removeChild(content);
 	 	} 	
 	 }
     function validateInputs(){
        var firstName=document.getElementById('firstName').value;
        if(firstName==""){
            document.getElementById("errmsg").innerHTML = "Please enter the First Name";
            document.getElementById("error").style.display='block';
            return false;
        }
        var lastName=document.getElementById('lastName').value;
        if(lastName==""){
            document.getElementById("errmsg").innerHTML = "Please enter the Last Name";
            document.getElementById("error").style.display='block';
            return false;
        }
        var emailId=document.getElementById('emailId').value;
        if(emailId==""){
            document.getElementById("errmsg").innerHTML = "Please enter the Email Id";
            document.getElementById("error").style.display='block';
            return false;
        }
        var company=document.getElementById('company').value;
        if(company==""){
            document.getElementById("errmsg").innerHTML = "Please enter the Company";
            document.getElementById("error").style.display='block';
            return false;
        }
        var sponsor=document.getElementById('sponsor').value;
        if(sponsor==""){
            document.getElementById("errmsg").innerHTML = "Please enter the Sponsor field";
            document.getElementById("error").style.display='block';
            return false;
        }
        var sponsorEmail=document.getElementById('sponsorEmail').value;
        if(sponsorEmail==""){
            document.getElementById("errmsg").innerHTML = "Please enter the Sponsor's Email Address";
            document.getElementById("error").style.display='block';
            return false;
        }
        var reason=document.getElementById('reason').value;
         if(reason==""){
            document.getElementById("errmsg").innerHTML = "Please enter the Reason";
            document.getElementById("error").style.display='block';
            return false;
        }
        return validEmail();
     }
     function validEmail() { 
    	 	var userEmail=document.getElementById('emailId').value;
    	 	var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    	    var result= re.test(userEmail);
    	    if(!result){
               document.getElementById("errmsg").innerHTML = "Please enter a valid Email Id";
               document.getElementById("error").style.display='block';
    	      	return false;
    	    }
    	    var sponsorEmail=document.getElementById('sponsorEmail').value;
    	    result=re.test(sponsorEmail);
    	    if(!result){
               document.getElementById("errmsg").innerHTML = "Please enter a valid Sponsor's Email Address";
               document.getElementById("error").style.display='block';
    	       return false;
    	    }
    	    return true;
      } 
     function closeDialog() {
        document.getElementById("error").style.display = "none";
     }
    </script>
 </head>
 <body>
 <div>
    <div  align="center" class="settings-sc">
      <div id="error" class="alert alert-danger alert-dismissable" style="display:none" >
         <button type="button" class="close" onclick="closeDialog()" aria-hidden="true">x</button><span id="errmsg"></span>
      </div>

        <div class="panel-group center-aligned-td" id="accordion">
            <div id="display-error" style="margin:auto; vertical-align: middle;max-width:100%;visibility:hidden;" > 
             </div>
	       <div id="display-success" style="margin:auto; vertical-align: middle;max-width:100%; visibility:hidden;" > 
	       </div>


            <form class="form-horizontal ng-pristine ng-invalid ng-invalid-required" method="post" action="RMSViewer/SendMail" onsubmit="return validateInputs()" accept-charset="UTF-8">
                <h4 class="form-signin-heading" align="center">Rights Management - Request an account</h4>
                <br/>
                <div class="form-group">
                    <label class="col-xs-4 ng-binding register-box">First Name<span class="mandatory">*</span></label>
                    <div class="col-xs-8 register-box">
                        <input type="text" id="firstName" name="firstName" required class="ng-pristine ng-invalid ng-invalid-required ng-touched register-box">
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-xs-4 ng-binding register-box">Last Name<span class="mandatory">*</span></label>
                    <div class="col-xs-8 register-box">
                        <input type="text" id="lastName" name="lastName" required class="ng-pristine ng-invalid ng-invalid-required ng-touched register-box">
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-xs-4 ng-binding register-box">Email ID<span class="mandatory">*</span></label>
                    <div class="col-xs-8 register-box">
                        <input type="text" id="emailId" name="emailId" required class="ng-pristine ng-invalid ng-invalid-required ng-touched register-box">
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-xs-4 ng-binding register-box">Sponsor<span class="mandatory">*</span></label>
                    <div class="col-xs-8 register-box">
                        <input type="text" id="sponsor" name="sponsor" required class="ng-pristine ng-invalid ng-invalid-required ng-touched register-box">
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-xs-4 ng-binding register-box">Sponsor's Email Address<span class="mandatory">*</span></label>
                    <div class="col-xs-8 register-box">
                        <input type="text" id="sponsorEmail" name="sponsorEmail" required class="ng-pristine ng-invalid ng-invalid-required ng-touched register-box">
                    </div>
                </div>
                <div class="form-group">
                    <label class="col-xs-4 ng-binding register-box">Reason for request<span class="mandatory">*</span></label>
                    <div class="col-xs-8 register-box">
                        <textarea class="form-control register-box" id="reason" name="reason" rows="5" ></textarea>
                        <button class="btn btn-default rms-login-button register-box" onclick="openCenteredPopup('help/Login_Page.htm','myWindow','1200','800','yes');return false">Help</button>
                        <button class="btn btn-default rms-request-acc-button register-box" type="submit">Request an account</button>
                     </div>
                </div>
            </form>
        </div>
</div>

</div>

     <script src="<%=webContext%>/ui/lib/3rdParty/jquery-1.10.2.min.js"></script>
     
</body>
</html>