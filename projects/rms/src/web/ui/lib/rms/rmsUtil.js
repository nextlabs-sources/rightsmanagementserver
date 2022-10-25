var i18n_language = "en";
var i18n_data;

$(document).ready(function(){
  translateTo(i18n_language);
})

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regexS = "[\\?&]" + name + "=([^&#]*)";
    var regex = new RegExp(regexS);
    var results = regex.exec(decodeURIComponent(window.location.href));
    if (results == null)
        return "";
    else
        return decodeURIComponent(results[1].replace(/\+/g, " "));
}

function generateTagListFromTagMap(tags){
    var tagList ={};
    var tagPair={};
	if(tags!=null) {
		Object.keys(tags).forEach(function(key,index) {
			var value = "";
			var values = tags[key];
			for(var i = 0; i<values.length;i++){
			  value+=values[i];
			  if(i!=values.length-1){
				value+=",";
			  }
			}
			tagList[key] = value;
		});
    }
	return tagList;
}

function downloadRepoFile(repoType, repoId, filePath){
	window.open("/RMS/RMSViewer/DownloadFile?filePath="+encodeURIComponent(filePath)+
		"&repoType="+encodeURIComponent(repoType)+"&repoId="+encodeURIComponent(repoId));
}

function openSecurePopup(url){ 
      settings = "scrollbars=yes, location=no, directories=no, status=no, menubar=no, toolbar=no, resizable=yes, dependent=no";   
      win = window.open(url, "NextLabsRMS", settings);
      if(win==undefined){
        handleError(i18n_data['err.popup.blocked']);
      }
      else{
        win.focus();
      }    
  }

  function handleError(message){
    if(document.getElementById("rms-main-view")!=undefined){ 
        document.getElementById("rms-main-view").style.opacity=0.4;
        document.getElementById("errmsg").innerHTML = message;
        document.getElementById("error").style.display='block'
        document.getElementById("error").style.position='fixed';
      }
   }

  function translateTo(language){
     $.getJSON("ui/app/i18n/"+i18n_language+".json")
        .done(function( data ) {
         i18n_data = data;       
        }).fail(function(jqxhr, textStatus, error) {
         console.log(textStatus + ", " + error);
     });
  }
  
  function closeDialog() {
    document.getElementById("error").style.display = "none";
    document.getElementById("rms-main-view").style.opacity=1.0;
}

