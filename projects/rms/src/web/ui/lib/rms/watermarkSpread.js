var width=0, height=0, screen_width, screen_height, watermarkWidthOffset, watermarkHeightOffset;


$(window).resize(function() {
   checkFileSize();
});

$(document).ready(function(){
	$('#loading').hide();
	checkFileSize();
});


function checkFileSize(){
	$(document).ready(function(){
		if(checkIfBigFile())
			addViewOverlay();
		else
	  		addWatermark();
		$('#pageWrapper').css({'position':'absolute', 'overflow':'auto'});
	});
}
function checkIfBigFile(){
	var docLength=$("#documentlength").attr("value");
	var explorerVersion=isIE();
	if(docLength>500000&&(explorerVersion==9)){
		return true;
	}
	else{
		return false;
	}
}
function isIE () {
 	 var myNav = navigator.userAgent.toLowerCase();
 	 return (myNav.indexOf('msie') != -1) ? parseInt(myNav.split('msie')[1]) : false;
}
function printWatermarkRepeat () {
	if(checkIfBigFile()) {	
		addWatermark();
		removeViewOverlay();
	}
}

function addFullScreenWatermarkContainer(){

	var body = document.body;
	var html = document.documentElement;
	$(".ISYS_BODY_DIV").css({ 'position':'relative',
							  'overflow':'visible',
							  'display': 'inline-block'
	});
	
	height = $(".ISYS_BODY_DIV").height();
	width = $(".ISYS_BODY_DIV").width();
	if(width<=0) {	// file is corrupted and ISYS_WORKSHEET_DIV is absent
		width = screen_width;
	}
	if(height<=0){
		height = screen_height;
	}
	
	screen_height = screen.height - headerToolbarHeight;
	screen_width = screen.width;
	var waterMarkDiv = $(".watermark")[0];
	var density = document.getElementById("waterMarkDensity").value;
	var rotation = $('#rotation').attr('value');
	var fontname = $('#fontName').attr('value');

	if(!new Detector().detect(fontname)){
       fontname="Arial";
	}
	watermarkHeightOffset=0;
	watermarkWidthOffset=0;

	if(density=="Dense"){
		watermarkHeightOffset=150;
		watermarkWidthOffset=150;
	}
	else{
		watermarkHeightOffset=200;
		watermarkWidthOffset=200;
	}

	var fontsize = parseInt($(waterMarkDiv).css('font-size'));
	watermarkWidthOffset =  watermarkWidthOffset * fontsize/36;
	watermarkHeightOffset = watermarkHeightOffset * fontsize/36;

	// To get text width 
	var watermarkWidth=waterMarkDiv.children[0].children[0].offsetWidth;
	var watermarkHeight=waterMarkDiv.children[0].children[0].offsetHeight;
	var	tempDiv=$( waterMarkDiv).clone();
	
	$("#waterMarkContainer").remove();
	for(var i=0;i<$(".ISYS_WORKSHEET").length;i++){
		if(width<$(".ISYS_WORKSHEET")[i].children[0].clientWidth){
			width=$(".ISYS_WORKSHEET")[i].children[0].clientWidth;
		}
	}
	
	var rotation_css = "-ms-transform-origin:0px 0px; -webkit-transform-origin:0px 0px; -moz-webkit-transform-origin:0px 0px; transform-origin:0px 0px;";
	if(rotation=="Clockwise"){
		var translateX = watermarkHeight+15;
 		rotation_css = rotation_css + "-ms-transform: translate("+translateX+"px,0px) rotate(45deg); -webkit-transform: translate("+translateX+"px,0px) rotate(45deg); -moz-webkit-transform: translate("+translateX+"px,0px) rotate(45deg); transform: translate("+translateX+"px,0px) rotate(45deg);";
	}
	else {
		var translateX = -watermarkHeight+15;
		var translateY = screen_height;
		rotation_css = rotation_css + "-ms-transform: translate("+translateX+"px,"+translateY+"px) rotate(-45deg); -webkit-transform: translate("+translateX+"px,"+translateY+"px) rotate(-45deg); -moz-webkit-transform: translate("+translateX+"px,"+translateY+"px) rotate(-45deg); transform: translate("+translateX+"px,"+translateY+"px) rotate(-45deg);";
	}

	var rotatedWaterMarkWidth = Math.ceil((watermarkWidth+watermarkWidthOffset)*0.707);
	var rotatedWaterMarkHeight = Math.ceil((watermarkHeight+watermarkHeightOffset)*1.414);
	var numWaterMarksPerRow = Math.ceil(screen_width/rotatedWaterMarkWidth);
	var numWaterMarksPerCol = Math.ceil(screen_height/rotatedWaterMarkHeight)+1;
	screen_width = numWaterMarksPerRow*rotatedWaterMarkWidth;
	screen_height = numWaterMarksPerCol*(watermarkHeight+watermarkHeightOffset);

	var diagonal=Math.sqrt(screen_width*screen_width+screen_height*screen_height);
    var waterMarkRow = "<div id=\"waterMarkRow\" style=\"white-space:nowrap;  z-index: 1000; position:absolute; display:inline-block;"+rotation_css+"; font-family:"+fontname+"\"> </div>";
    var waterMarkContainer = "<div id=\"waterMarkContainer\" style=\"display:inline-block;\"> </div>";
    waterMarkContainer = $(waterMarkContainer).clone();
	$("#pageWrapper").prepend(waterMarkContainer);
    $(body).prepend($(waterMarkRow).clone());

	for(var j=0;j<diagonal+500;j=j+watermarkWidth+watermarkWidthOffset){
       $("#waterMarkRow").append($(tempDiv).clone().css({'margin-left':j+'px'}));
    }
	
	for(var i=-diagonal;i<diagonal+500;i=i+watermarkHeight+watermarkHeightOffset){
		waterMarkRow=$("#waterMarkRow").clone().css({'margin-top': i+'px', 'visibility':'visible'});
		$(waterMarkContainer).append(waterMarkRow);
	}
	
	waterMarkContainer.css({'height': screen_height+"px",                            
							'position':'fixed',
                            'z-index':'1000',
                            'clip':'rect('+headerToolbarHeight+'px,'+screen_width+'px,'+(screen_height+headerToolbarHeight)+'px,0px)'	//top, right, bot, left
    });

   $(waterMarkDiv).remove();
   $("#waterMarkRow").remove();
}

function addViewOverlay(){

	if ($('#waterMarkOverlay').length)
		return;

	$('#loading').show();
	addFullScreenWatermarkContainer();
	removeWatermark();		//remove watermark from print div if it exists !important
	$("#waterMarkContainer").attr("id", "waterMarkOverlay");
	$("#waterMarkContainer").css("top",headerToolbarHeight+"px");
	$('#loading').hide();
}

function addWatermark(){
	if ($('#waterMarkRootContainer').length)
		return;

	$('#loading').show();
	addFullScreenWatermarkContainer();
	$("#waterMarkContainer").css('position','absolute');
	var waterMarkRootContainer = "<div id=\"waterMarkRootContainer\"> </div>";
	waterMarkRootContainer = $(waterMarkRootContainer).clone();
	$('#pageWrapper').prepend(waterMarkRootContainer);  
	var containerID=0;
	var jj=0;
	for(var j=0; j<height+screen_height; j=j+screen_height) {
		var ii=0;
		for(var i=0; i<width+screen_width; i=i+screen_width){
			waterMarkContainer=$("#waterMarkContainer").clone().prop('id', 'waterMarkContainer'+ii+jj ).css({
																	 'margin-top': j+'px', 
																	 'margin-left':i+'px',
			});
			ii++;
			$(waterMarkRootContainer).append(waterMarkContainer);
			containerID++;		
		}
		jj++;
	}
	
	$("#waterMarkContainer").remove();
	$("#waterMarkRootContainer").css({'height': height+"px",
                          		'position':'absolute',
                            	'z-index':'1000',
								'clip':'rect('+headerToolbarHeight+'px,'+width+'px,'+(height+2*headerToolbarHeight)+'px,0px)'
    });
	$('#loading').hide();
}

function removeViewOverlay(){
	$('#waterMarkOverlay').remove();
}

function removeWatermark(){
	$('#waterMarkRootContainer').remove();
}
