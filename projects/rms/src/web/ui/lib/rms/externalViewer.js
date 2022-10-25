$(document).ready(function() {
  var HostUrl= getParameterByName("siteName");
  var ItemUrl=getParameterByName("path");
  var clientId=getParameterByName("clientID");
  var tenantId=getParameterByName("tenantID");
  var url="/RMS/SharePointAuth/AuthStart?siteName="+HostUrl+"&path="+ItemUrl+"&clientID="+clientId+"&tenantID="+tenantId;
  openSecurePopup(url);
})

function getParameterByName( name ){
  name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");  
  var regexS = "[\\?&]"+name+"=([^&#]*)";  
  var regex = new RegExp( regexS );  
  var results = regex.exec( window.location.href ); 
  if( results == null )    
    return "";  
  else    
    return results[1];
}