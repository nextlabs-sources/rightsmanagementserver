	function AXOrNull(progId) {

		try {
			var browser = BrowserDetect.browser;
			var version = BrowserDetect.version;
			if (browser != "Explorer" && browser != "Mozilla") {
				window.location.href = '/RMS/ShowError.jsp?errMsg= This file can only be viewed from Internet Explorer 9 and above.';
				return null;
			}
			var control=new ActiveXObject(progId);
		}
		catch (ex) {
			window.location.href = '/RMS/ShowError.jsp?errMsg= This file requires <a href="https://store.sap.com/sap/cpa/ui/resources/store/html/SolutionDetails.html?pid=0000000233%26catID=%26pcntry=US%26sap-language=EN%26_cp_id=id-1431928966953-0" target="_blank"> SAP Visual Enterprise Viewer</a>. Please install SAP Visual Enterprise Viewer to view RH files in your web browser.';
			return null;
		}
	}
	function addTitle(fileName){
			var div = document.getElementById('title');
            div.innerHTML = div.innerHTML + fileName;
    }
	function resetStep(){
			var step=document.getElementById("DeepView").Scene.Steps.GetByIndex(0);
			step.Play();		
	}
	function play(){
			document.getElementById("DeepView").ExecuteCommand("M1251");
	}
	function pauseStep(){
			var isPaused=rh().Scene.IsCurrentStepPaused;
			if(isPaused){
				rh().Scene.PauseCurrentStep(false); 
			}
			else{
				rh().Scene.PauseCurrentStep(true);
			}
		
	}
	function playall(){ 
			document.getElementById("DeepView").ExecuteCommand("M1302");
			document.getElementById("DeepView").ExecuteCommand("M1252");
	}
	function measurePoint(){
			document.getElementById("DeepView").ExecuteCommand("M2800");
	}
	function measureAngle(){
			document.getElementById("DeepView").ExecuteCommand("M2801");
	}

	function print(){
			document.getElementById("DeepView").ExecuteCommand("M1279");
	}

	 function getQueryVariable(variable) {
            var query = decodeURIComponent(window.location.search.substring(1));
            var vars = query.split("&");
            for (var i=0;i<vars.length;i++) {
                var pair = vars[i].split("=");
                if (pair[0] == variable) {
                  return pair[1];
                }
            } 
        }
	function getFileName(name){
            var pathArray = name.split("/");
            var result =pathArray[pathArray.length-1]
        
            if (result == null)
                return null;
            else
                return decodeURIComponent(result.replace(/\+/g, " "));
            }
	function showHelp(){
            helpWindow=window.open("/RMS/help/Document_Viewer.htm","NextlabsViewerHelp", "height=800,width=800, resizable=yes, scrollbars=yes");
            helpWindow.focus();
        }
    function closeDialog() {
            document.getElementById("error").style.display = "none";
			$("#DeepViewDiv").show();
            document.getElementById("all").style.opacity=1.0;
        }

    function handleError(message){
            if(document.getElementById("all")!=undefined){ 
			
			$("#DeepViewDiv").hide();
                document.getElementById("all").style.opacity=0.4;
                document.getElementById("errmsg").innerHTML = message;
                document.getElementById("error").style.display='block';
            }
        }