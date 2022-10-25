function playall(){
	oStepNavigation.mProperties.settings.currentStepPaused=false;
	oStepNavigation.mProperties.settings.playAllActive=true;
	oViewer.playAllSteps();
}
function pauseStep(){
	oStepNavigation.mProperties.settings.currentStepPaused=true;
	oViewer.pauseStep();
}
function resetStep(){
	if(oStepNavigation.oModel.oData.procedures.length==0) {
		oViewer.resetView();
	} else {
		oStepNavigation.mProperties.settings.playAllActive=false;
		oStepNavigation.mProperties.settings.currentStepPaused=false;
		var firstStepId = oStepNavigation.oModel.oData.procedures[0].steps[0].id;
		oViewer.playStep(firstStepId,!oStepNavigation.mProperties.settings.currentStepPaused,  oStepNavigation.mProperties.settings.playAllActive);
	}
}
function play(){
	oViewer.playStep(oStepNavigation.mProperties.settings.currentStepId,!oStepNavigation.mProperties.settings.currentStepPaused,  oStepNavigation.mProperties.settings.playAllActive);
	oStepNavigation.mProperties.settings.currentStepPaused=false;
}

function toggleStepInFo(){
	if(oStepNavigation.mProperties.showStepInfo){
		oStepNavigation.mProperties.showStepInfo=false;
		$("#vds-toggleStepInFo").addClass("infoHide");
		$(".sapUiShd").css("visibility", "hidden");
	}
	else{
		$("#vds-toggleStepInFo").removeClass("infoHide");
		oStepNavigation.setShowStepInfo();
	}
}

function checkSteps(){
	if(oStepNavigation.oModel.oData.procedures.length==0){
		$("#vds-toggleStepInFo").addClass("disabled");
		$("#vds-play").addClass("disabled");
		$("#vds-pauseStep").addClass("disabled");
		$("#vds-playAll").addClass("disabled");
	}
}
function printModel() {  
	$("#printTemp").empty();
	var overlay=$( "#waterMarkContainer").clone();
	$("#printTemp").prepend(overlay);
	var dataUrl = $("#__viewer0-canvas")[0].toDataURL(); 
	var windowContent = '<div id="image">'+document.getElementById("printTemp").innerHTML+'<img src="' + dataUrl + '"></div>';
	handleError('Please close the print window to proceed.');
	$('#pageWrapper').block({message: '',overlayCSS: { backgroundColor: '#fff' }});
	var printWin = window.open('','Print-Window','width=1000,height=1000');
	printWin.document.open(); 
	printWin.document.write('<html><head><meta name="format-detection" content="date=no"><meta name="format-detection" content="address=no"><meta name="format-detection" content="telephone=no"></head><body onload="window.print()">'+windowContent+'</body></html>'); 
	printWin.document.close(); 
	setTimeout(function(){
	printWin.close();
	$("#printTemp").empty();
	closeDialog();
	$('#pageWrapper').unblock();
  },10);
}

function downloadFile(){
	var filePath = cadMetaData.filePath;
	var repoId = cadMetaData.repoId;
	var repoType = cadMetaData.repoType;
	downloadRepoFile(repoType, repoId, filePath);
}

function showInfo(){
	displayInfo(cadMetaData);
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

function addFileInfoDiv(){

	var fileInfoDiv = '<div id ="viewer-rms-info" class="modal-dialog modal-content">	\
						<div class="rms-info-filedetails">	\
							<div>	\
								<a class="rms-info-closeLink" id="fileInfoCloseBtn" onclick="closeFileInfoPopUp()">x</a> \
								<div class="rms-info-fileName"><span id="fileTitle" style="font-family: fira sans;"></span>	\
								</div>	\
								<br><br>	\
								<table class="rms-info-fileProperties">	\
									<tr>	\
										<td width="30%"><span id="fileTypeSpan">File Type</span></td>	\
										<td width="30%"><span id="fileSizeSpan">Size</span></td>	\
										<td width="40%"><span id="fileDateSpan">Last Modified On</span></td>	\
									</tr>	\
									<tr>	\
										<td><b><span id="fileTypeValue">-</span></b></td>	\
										<td><b><span id="fileSizeValue">-</span></b></td>	\
										<td><b><span id="fileDateValue">-</span></b></td>	\
									</tr>	\
								</table>	\
							</div>	\
						</div>	\
						<div class="rms-info-repoDetails">	\
							<h4><span id="fileRepoNameValue">N/A</span></h4>	\
							<h5><span id="fileRepoPathValue">N/A</span></h5>	\
						</div>	\
						<div id="rms-info-tags" class="rms-info-tags" style="background-color:white">	\
							<br><br>	\
							<center>	\
								<label><span id="fileTagsSpan" style="color:black;">File Classification</span></label>	\
								<br>	\
								<div id="fileTagsValue"></div>	\
								<br>	\
							</center>	\
						</div>	\
						<div class="rms-info-rights" style="background-color:white">	\
							<br><br>	\
							<center>	\
								<label><span id="fileRightsSpan">File Rights</span></label>	\
								<br>	\
								<div id="fileRightsValue"></div>	\
								<br>	\
							</center>	\
						</div>			\
						<div><br><br><br></div>		\
					</div>';

	$("body").prepend(fileInfoDiv);
}

 function addWaterMark(){
	var body = document.body,html = document.documentElement;
	var height = $("#ViewerSampleLayout")[0].clientHeight;
	var width=$("#ViewerSampleLayout")[0].clientWidth;
	var diagonal = Math.sqrt(width*width+height*height);
	var watermark=cadMetaData.watermark;
	if(watermark == null)
		return;
	var overlay=watermark.waterMarkStr;
	var split=watermark.waterMarkSplit;
	overlay=overlay.replace(/\n/gi,"<br/>");
	var lines = overlay.split("<br/>");
	var i,j,maxLineLength = 0;
	for (i=0; i<lines.length; i++){
		maxLineLength=Math.max(maxLineLength,lines[i].length);
	}
	var fontsize=watermark.waterMarkFontSize;
	var fontcolor=watermark.waterMarkFontColor;
	var transparency=watermark.waterMarkTransparency/100;
	var fontname=watermark.waterMarkFontName;
	var density=watermark.waterMarkDensity;                
	var rotation = watermark.waterMarkRotation;
	var watermarkWidth=0;
	var watermarkHeight=0;
	var watermarkHeightOffset=0;
	var watermarkWidthOffset=0;
	
	if(!new Detector().detect(fontname)){
		fontname="Arial";
	}		
	if(density=="Dense"){
		watermarkHeightOffset=150;
		watermarkWidthOffset=150;
	}
	else{
		watermarkHeightOffset=200;
		watermarkWidthOffset=200;
	}		
	watermarkWidthOffset =  watermarkWidthOffset * fontsize/36;
	watermarkHeightOffset = watermarkHeightOffset * fontsize/36;
	 var tempDiv="<div  class=\"watermark\" style=\"font-size:"+fontsize+"px;color:"+fontcolor+";position:absolute;pointer-events: none;z-index:1000;white-space:nowrap;opacity:"+transparency+"; font-family:" + fontname + ";\"><p><span style=\"display:inline-block; text-align:center\" id=\"Test\">"+overlay+"</span></p></div>";
	var rotation_css = "-ms-transform-origin:0px 0px; -webkit-transform-origin:0px 0px; -moz-webkit-transform-origin:0px 0px; transform-origin:0px 0px;";
	if(rotation=="Clockwise"){
		var translateX = watermarkHeight+15;
		rotation_css = rotation_css + "-ms-transform: translate("+translateX+"px,0px) rotate(45deg); -webkit-transform: translate("+translateX+"px,0px) rotate(45deg); -moz-webkit-transform: translate("+translateX+"px,0px) rotate(45deg); transform: translate("+translateX+"px,0px) rotate(45deg);";
	}
	else {
		var translateX = -watermarkHeight+15;
		var translateY = height;
		rotation_css = rotation_css + "-ms-transform: translate("+translateX+"px,"+translateY+"px) rotate(-45deg); -webkit-transform: translate("+translateX+"px,"+translateY+"px) rotate(-45deg); -moz-webkit-transform: translate("+translateX+"px,"+translateY+"px) rotate(-45deg); transform: translate("+translateX+"px,"+translateY+"px) rotate(-45deg);";
	} 
	
	$("#waterMarkContainer").remove();
	var waterMarkContainer = "<div id=\"waterMarkContainer\" style=\"display:inline-block;\"> </div>";
	var waterMarkRow = "<div id=\"waterMarkRow\" style=\"white-space:nowrap;  z-index: 1000; position:absolute; display:inline-block;"+rotation_css+"; font-family:"+fontname+"\"> </div>";
	waterMarkContainer = $(waterMarkContainer).clone();
	$(".wrapper").prepend(waterMarkContainer);       
	$(".wrapper").prepend($(waterMarkRow).clone());
	
	for(var j=0;j<diagonal+500;j=j+watermarkWidth+watermarkWidthOffset){
	   $("#waterMarkRow").append($(tempDiv).clone().css({'margin-top':'0px','margin-left':j+'px'}));
	   if(watermarkHeight==0){
			watermarkHeight=$("#waterMarkRow")[0].children[0].clientHeight;
			watermarkWidth=$("#waterMarkRow")[0].children[0].clientWidth;
		}
	}
	for(var j=0;j<diagonal+500;j=j+watermarkWidth+watermarkWidthOffset){
		$("#waterMarkRow").append($(tempDiv).clone().css({'margin-left':j+'px'}));
	}	
	for(var i=-diagonal;i<diagonal+500;i=i+watermarkHeight+watermarkHeightOffset){
		waterMarkRow=$("#waterMarkRow").clone().css('margin-top', i+'px');
		$(waterMarkContainer).append(waterMarkRow);
	}
	
	waterMarkContainer.css({'height': height+"px",
							'position':'fixed',
							'z-index':'1000',
							'clip':'rect(0px,'+width+'px,'+height+'px,0px)'
	});
	$("#waterMarkRow").remove();
}

function addTitle(){
	$(".sapVeViewerStepNavigationToolbar").hide();
	var TITLE_MAX_LENGTH_DESKTOP = 50;
	var TITLE_MAX_LENGTH_MOBILE = 15;
	var fileName = cadMetaData.displayName;
	var titleDesktopSpan = document.getElementById('titleDesktop');
	var titleMobileSpan = document.getElementById('titleMobile');
	titleDesktopSpan.innerHTML += getShortName(fileName, TITLE_MAX_LENGTH_DESKTOP);
	titleMobileSpan.innerHTML += getShortName(fileName, TITLE_MAX_LENGTH_MOBILE);
	$("#titleDesktop").attr("title", fileName);
	$("#titleMobile").attr("title", fileName);
}

function getFileName(name){
	var pathArray = name.split("/");
	var result =pathArray[pathArray.length-1]
	if (result == null)
		return null;
	else
		return decodeURIComponent(result.replace(/\+/g, " "));
}

 function checkWebGL(){
	if(!window.WebGLRenderingContext) {
		// the browser doesn't even know what WebGL is
		window.location.href = '/RMS/ShowError.jsp?errMsg=Your browser does not support webGL. This is required for visualizing CAD files in the web browser.';
	}else {
		var canvas = document.createElement('canvas');
		var context = canvas.getContext("webgl")||canvas.getContext('experimental-webgl');
		if (!context) {
			// browser supports WebGL but initialization failed.
			window.location.href = '/RMS/ShowError.jsp?errMsg=Please enable <a href="https://get.webgl.org/"> WebGL</a> in your web browser.';
		}
	}
}

$(window).on('beforeunload', function(){
	var cacheId=getQueryVariable("cacheId");
	$.get('/RMS/RMSViewer/RemoveFromCache?documentId='+cacheId);
 });

function closeDialog() {
	document.getElementById("error").style.display = "none";
	document.getElementById("ViewerSampleControls").style.opacity=1.0;
}

function handleError(message){
	if(document.getElementById("ViewerSampleControls")!=undefined){ 
		document.getElementById("ViewerSampleControls").style.opacity=0.4;
		document.getElementById("errmsg").innerHTML = message;
		document.getElementById("error").style.display='block';
		closeFileInfoPopUp();
	}
}

function translateTo(language){
	$.getJSON("ui/app/i18n/"+language+".json")
		.done(function( data ) {
			$("#rms-print-button").attr('title',data['viewer.toolbar.print']);
			$("#rms-help-button").attr('title',data['viewer.toolbar.help']);
			$("#rms-download-button").attr('title',data['viewer.toolbar.download']);
			$("#rms-rights-button").attr('title',data['viewer.toolbar.rights']);
			$("#vds-toggleStepInFo").attr('title',data['viewer.toolbar.vds.showStep']);
			$("#vds-resetStep").attr('title',data['viewer.toolbar.vds.resetStep']);
			$("#vds-play").attr('title',data['viewer.toolbar.vds.play']);
			$("#vds-pauseStep").attr('title',data['viewer.toolbar.vds.pauseStep']);
			$("#vds-playAll").attr('title',data['viewer.toolbar.vds.playAll']);
			i18n_data = data;
		}).fail(function(jqxhr, textStatus, error) {
			console.log(textStatus + ", " + error);
		});
}
 