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

$(window).on('beforeunload', function(){
	var cacheId=getQueryVariable("cacheId");
	$.get('/RMS/RMSViewer/RemoveFromCache?documentId='+cacheId);
});

function addTitle(fileName){
	var TITLE_MAX_LENGTH_DESKTOP = 50;
	var TITLE_MAX_LENGTH_MOBILE = 15;
	var titleDesktopSpan = document.getElementById('titleDesktop');
	var titleMobileSpan = document.getElementById('titleMobile');
	titleDesktopSpan.innerHTML += getShortName(fileName, TITLE_MAX_LENGTH_DESKTOP);
	titleMobileSpan.innerHTML += getShortName(fileName, TITLE_MAX_LENGTH_MOBILE);
	$("#titleDesktop").attr("title", fileName);
	$("#titleMobile").attr("title", fileName);
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
function translateTo(language){
	$.getJSON("ui/app/i18n/"+language+".json")
		.done(function( data ) {
			$("#rms-print-button").attr('title',data['viewer.toolbar.print']);
			$("#rms-help-button").attr('title',data['viewer.toolbar.help']);
			$("#rms-download-button").attr('title',data['viewer.toolbar.download']);
			$("#rms-rights-button").attr('title',data['viewer.toolbar.rights']);
			$("#rms-info-button").attr('title',data['viewer.toolbar.info']);
			i18n_data = data;
		}).fail(function(jqxhr, textStatus, error) {
			console.log(textStatus + ", " + error);
		});
}
function closeDialog() {
	var fromDiv = document.getElementById("error");
	var toDiv = document.getElementById("overlay-iframe");
	fromDiv.style.display = "none";
	cloneCSSProperties(fromDiv, toDiv);
}

function handleError(message){
	if(document.getElementById("all")!=undefined){ 
		closeFileInfoPopUp();
		var fromDiv = document.getElementById("error");
		var toDiv = document.getElementById("overlay-iframe");
		document.getElementById("errmsg").innerHTML = message;
		fromDiv.style.display='block';
		cloneCSSProperties(fromDiv, toDiv);
	}
}

function showInfo(){
	displayInfo(cadMetaData);
	var fromDiv = document.getElementById("viewer-rms-info");
	var toDiv = document.getElementById("overlay-iframe");
	cloneCSSProperties(fromDiv, toDiv);
	toDiv.style.height = "100%";
}

function closeFileInfoPopUp(){
	$("#viewer-rms-info").hide();
	$("#overlay-iframe").hide();
}

function cloneCSSProperties(fromDiv, toDiv) {	
	var computed_style_object = window.getComputedStyle(fromDiv,null);
	if(!computed_style_object) {
		return null;
	}
	var stylePropertyValid = function(name,value){
				//checking that the value is not a undefined
		return typeof value !== 'undefined' &&
				//checking that the value is not a object
				typeof value !== 'object' &&
				//checking that the value is not a function
				typeof value !== 'function' &&
				//checking that we dosent have empty string
				value.length > 0 &&
				//checking that the property is not int index ( happens on some browser
				value != parseInt(value)

	};

    //we iterating the computed style object and compy the style props and the values 
    for(property in computed_style_object)
    {
        //checking if the property and value we get are valid sinse browser have different implementations
		if(stylePropertyValid(property,computed_style_object[property]))
		{
			//applying the style property to the target element
				toDiv.style[property] = computed_style_object[property];

		}   
    }   
}