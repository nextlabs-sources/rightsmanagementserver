var isFitToHeight=true;
var zoomLevels=[12.5,25,50,100,200,400];
var cache={};
var cacheMaxLen=20;
var printUrl="";
var metaData;
var docId;
var	rotation=0;
var zoomLevel=3;
var pageNumberDisplay=1;
var pageNumberReal=1;
var numPages;

$(window).resize(function() {
    clearTimeout($(this).data('timer'));
    $(this).data('timer', setTimeout(function() {
            resizeWindow();
            closeFileInfoPopUp();
        },70)
    );	
});

$(window).on('beforeunload', function(){
	$.get('/RMS/RMSViewer/RemoveFromCache?documentId='+docId);
});

function translateTo(language){
	$.getJSON("ui/app/i18n/"+language+".json")
		.done(function( data ) {
			$("#rms-print-button").attr('title',data['viewer.toolbar.print']);
			$("#rms-help-button").attr('title',data['viewer.toolbar.help']);
			$("#rms-download-button").attr('title',data['viewer.toolbar.download']);
			$("#rms-rights-button").attr('title',data['viewer.toolbar.rights']);
			$("#rms-info-button").attr('title',data['viewer.toolbar.info']);
			$("#rms-rotate-left-button").attr('title',data['viewer.toolbar.rotate.left']);
			$("#rms-rotate-right-button").attr('title',data['viewer.toolbar.rotate.right']);
			$("#rms-prev-button").attr('title',data['viewer.toolbar.prev']);
			$("#rms-next-button").attr('title',data['viewer.toolbar.next']);
			$("#rms-fit-height-button").attr('title',data['viewer.toolbar.fit.height']);
			$("#rms-fit-width-button").attr('title',data['viewer.toolbar.fit.width']);
			$("#rms-zoom-in-button").attr('title',data['viewer.toolbar.zoom.in']);
			$("#rms-zoom-out-button").attr('title',data['viewer.toolbar.zoom.out']);
			i18n_data = data;
		}).fail(function(jqxhr, textStatus, error) {
			console.log(textStatus + ", " + error);
		});
}

$(document).ready(function() {
	showLoading();
	var url="/RMS/RMSViewer/GetDocMetaData";
	var TITLE_MAX_LENGTH_DESKTOP = 50;
	var TITLE_MAX_LENGTH_MOBILE = 15;
	docId=getParameterByName("documentid");
	$.post(url,{documentId:docId}).then(function(data) {
		if((data.errMsg && data.errMsg.length>0) || data.numPages==undefined || data.numPages<=0){
			window.location.href = '/RMS/ShowError.jsp?errMsg='+data.errMsg;
            return;
        }
		metaData = data;
		numPages = data.numPages;
		
		loadPageFromServer(1, 1);
		$("#titleDesktop").text(getShortName(metaData.displayName, TITLE_MAX_LENGTH_DESKTOP));
		$("#titleDesktop").attr("title", metaData.displayName);
		$("#titleMobile").text(getShortName(metaData.displayName, TITLE_MAX_LENGTH_MOBILE));
		$("#titleMobile").attr("title", metaData.displayName);
		$("#pageNumberDisplay").text(1);
		$("#totalPageNum").text(numPages);
		if(numPages<=1){
			$("#next-btn").addClass("disabled");
		}
		checkPrintEnabled(metaData.isPrintAllowed);
		checkDownloadEnabled(metaData.repoType); 
	});
	translateTo(i18n_language);
});

function resizeWindow(){
	$('#pageWrapper').css("width", $(window).width());
	$('#pageWrapper').css("height", $(window).height());
	adjustImageWrapperSize();
	var containerWidth=$('#imageWrapper').width();   
	var containerHeight=$('#imageWrapper').height();
	var width = $('#mainImg').width();    // Current image width
	var height = $('#mainImg').height();  // Current image height
	if(isFitToHeight!=null){
		if((rotation && ((rotation/90)%4)==0 || ((rotation/90)%4)==2)|| !rotation){
			if(isFitToHeight){
				var scaleRatio=containerHeight/height;
				$('#mainImg').css("width",width * scaleRatio ); 
				$('#mainImg').css("height",containerHeight );  
			}
			else{
				var scaleRatio=containerWidth/width;
				$('#mainImg').css("width",containerWidth ); // Set new width
				$('#mainImg').css("height",height*scaleRatio);  // Scale height based on ratio
			}
		}
		else if((rotation && ((rotation/90)%4)==1 || ((rotation/90)%4)==3)){
			if(isFitToHeight){
				var scaleRatio=containerHeight/width;
				$('#mainImg').css("height",scaleRatio*height );
				$('#mainImg').css("width",containerHeight);  
			}
			else{
				var scaleRatio=containerWidth/height;
				$('#mainImg').css("height",containerWidth ); 
				$('#mainImg').css("width",width*scaleRatio);  
			} 
		}	
	}
};

function adjustImageWrapperSize(){
	var pageWrapperWidth=$('#pageWrapper').width();
	var pageWrapperHeight=$('#pageWrapper').height();
	var toolbarContainerHeight=$('.toolbarContainer').height();
	var nxlHeaderHeight=$('.cc-header').height();
	var imageWrapperHeight= pageWrapperHeight-toolbarContainerHeight-nxlHeaderHeight;
	$('#imageWrapper').css("height",imageWrapperHeight);
	$('#imageWrapper').css("width",pageWrapperWidth);
	/*@cc_on
		//run this only on IE
		var scrollHeight = document.getElementById('imageWrapper').scrollHeight;
		var clientHeight = document.getElementById('imageWrapper').clientHeight;
		if(scrollHeight-clientHeight>2){
			$('#imageWrapper').css("width",pageWrapperWidth+15);
		}
		var scrollWidth = document.getElementById('imageWrapper').scrollWidth;
		var clientWidth = document.getElementById('imageWrapper').clientWidth;
		if(scrollWidth-clientWidth>2){
			$('#imageWrapper').css("height",imageWrapperHeight+15);
		}
	@*/
}

function addPagesToCache(data, currentPage){
	if(data==null){
		return;
	}
	if(data.pageContent.length==0 || data.pageNum.length==0){
		return;
	}	
	for(var i=0;i<data.pageNum.length;i++){
		var keys=Object.keys(cache);
		if(keys.length>=cacheMaxLen){
			var keyToRemove = getFarthestElement(keys, currentPage);
			delete cache[keyToRemove];
		}
		cache[data.pageNum[i]]=data.pageContent[i];
	}
}

function getFarthestElement(keys, currentPage){
	keys.push(currentPage);
	keys.sort(function(a, b){return a-b});
	var lastDiff = keys[keys.length-1] - currentPage;
	var firstDiff = currentPage - keys[0];
	if(lastDiff>firstDiff){
		return keys[keys.length-1];
	}else{
		return keys[0];
	}
}

function getPageFromCache(pageNum){
	return cache[pageNum];
}

$("#pageNumberDisplay").click(function(){
	var $span =  $("#pageNumberDisplay");
	var textBoxSize = numPages.toString().length;
	$span.html('<input type="text" id="pageNumberDisplayTextBox" maxlength="'+ textBoxSize +'" value="' + pageNumberDisplay + '" style="width:' + textBoxSize + 'em"/>');
	var $textBox =  $("#pageNumberDisplayTextBox");
	$textBox.focus();
	$textBox.val($textBox.val());	//setting cursor at the end of text
	
	$textBox.keydown(function(e) {
		if (e.keyCode === 13) {
			var pgNumString = $textBox.val();
			pageNumberDisplay = parseInt($textBox.val());
			if(isNaN(pgNumString) || parseInt(Number(pgNumString)) != pgNumString || isNaN(pageNumberDisplay)){
				handleError(pgNumString + ' is not a valid page number.');
				pageNumberDisplay = pageNumberReal;
			}
			if(pageNumberDisplay>=1 && pageNumberDisplay<=numPages) {
				pageNumberReal = pageNumberDisplay;
				loadNewPage();
			} else {
				handleError('There is no page number '+ pgNumString +' in this document.');
				pageNumberDisplay = pageNumberReal;
			}
			convertTextboxToSpan();
		}
	});
	
	$("#pageNumberDisplayTextBox").blur(function(){
		convertTextboxToSpan();
	});
});

function convertTextboxToSpan(){
	var span = '<span id="pageNumberDisplay"></span>';
	$("#pageNumberDisplay").html(span);
	$("#pageNumberDisplay").text(pageNumberDisplay);
	checkNavigationButtons();
}

function navigate(param){
	if(!param){
		return;
	}
	else if (param=='first') {
		if(pageNumberReal==1){
			return;
		}
		pageNumberDisplay=1;
		pageNumberReal=1;
		loadNewPage(-1);
	}
	else if (param=='last') {
		if(numPages==pageNumberReal){
			return;
		}
		pageNumberDisplay=numPages;
		pageNumberReal=numPages;
		loadNewPage(1);
	}
	else if (param=='previous') {
		if(pageNumberDisplay!=pageNumberReal){
			pageNumberDisplay=pageNumberReal;
		}
		if(pageNumberDisplay>1){
			pageNumberDisplay--;
			pageNumberReal--;
			loadNewPage(-1);
		}
	}
	else if (param=='next') {
		if(pageNumberDisplay!=pageNumberReal){
			pageNumberDisplay=pageNumberReal;
		}
		if(pageNumberDisplay<numPages){
			pageNumberDisplay++;
			pageNumberReal++;
			loadNewPage(1);
		}
	}
	$("#pageNumberDisplay").text(pageNumberDisplay);
	checkNavigationButtons();
}

function checkNavigationButtons(){
	if (pageNumberDisplay <= 1) {
		$("#prev-btn").addClass("disabled");
		$("#next-btn").removeClass("disabled");
	} else if(pageNumberDisplay >= numPages) {
		$("#next-btn").addClass("disabled");
		$("#prev-btn").removeClass("disabled");
	} else {
		$("#next-btn").removeClass("disabled");
		$("#prev-btn").removeClass("disabled");
	}
	if(numPages<=1){
		$("#prev-btn").addClass("disabled");
		$("#next-btn").addClass("disabled");
	}
}

function loadPageFromServer(pageNum, pageNumToShow){
	var url="/RMS/RMSViewer/GetDocContent";
	var totalPageCount = metaData.numPages;
	var pageNumArr= [];
	var self=this;
	var i=0;
	if(pageNum-1>0 && getPageFromCache(pageNum-1)==null){
		pageNumArr.push(pageNum-1);
	}
	if(pageNum>0 && pageNum<=totalPageCount && getPageFromCache(pageNum)==null){
		pageNumArr.push(pageNum);
	}
	if(pageNum+1<=totalPageCount && getPageFromCache(pageNum+1)==null){
		pageNumArr.push(pageNum+1);
	}
	if(pageNumArr.length==0){
		return;
	}
	$.post(url,{pageNum:pageNumArr,documentId:docId}).then(function(data) {
		if(data.errMsg!=null && data.errMsg.length>0){
			window.location.href = '/RMS/ShowError.jsp?errMsg='+data.errMsg;
			return;
		}
		addPagesToCache(data, pageNumberReal);
		if(pageNumToShow!=-1){
			var content = getPageFromCache(pageNumberReal);
			var imgContent = "data:image/png;base64,"+content;
			detectWidth(imgContent);
			$("#mainImg").attr("src", imgContent);
			resizeWindow();
			$("#imageWrapper").scrollTo(0);
		}
		self.removeLoading();
	});
}

function detectWidth(imgContent) {
	var img = new Image();
	img.onload = function() {
		if (this.width > 0 && ($(window).width() + 200 ) < this.width) {
			fitToWidth();
		}
	}
	img.src = imgContent;
}

function loadNewPage(increment){
	var self=this;
	var content = getPageFromCache(pageNumberReal);
	if(content!=null && content.length>0){
		var imgContent = "data:image/png;base64,"+content
		detectWidth(imgContent);
		$("#mainImg").attr("src", imgContent);
		self.removeLoading();
		resizeWindow();
		$("#imageWrapper").scrollTo(0);
		loadPageFromServer(pageNumberReal+increment, -1);
		return;
	}
	showLoading();
	loadPageFromServer(pageNumberReal, pageNumberReal);
	$("#imageWrapper").scrollTo(0);
}

function printAllPages(){
	url="/RMS/RMSViewer/PrintFile";
	showLoading();
	var self=this;	
	$.post(url,{documentId:docId}).then(function(data) {
		var settings = "scrollbars=yes, location=no, directories=no, status=no, menubar=no, toolbar=no, resizable=yes, dependent=no";
		var win = window.open(data.url, "PrintFile", settings);
		self.removeLoading();
		return;  
	});
}

function downloadFile(){
	var filePath = metaData.filePath;
	var repoId = metaData.repoId;
	var repoType = metaData.repoType;
	
	downloadRepoFile(repoType, repoId, filePath);
}

function showInfo(){
	displayInfo(metaData);
}

function fitToHeight(){
	zoomLevel = 3;
	isFitToHeight=true;
	resizeWindow(rotation);
	adjustImageWrapperSize();
}

function fitToWidth(){
	zoomLevel = 3;
	isFitToHeight=false;
	resizeWindow(rotation);
	adjustImageWrapperSize();
}

function zoomIn(){
	if(zoomLevel>=zoomLevels.length-1)
		return;
	zoomLevel++;
	var width=$('#mainImg').width();
	var height=$('#mainImg').height();
	$('#mainImg').css("width",width * (zoomLevels[zoomLevel]/zoomLevels[zoomLevel-1]) ); 
	$('#mainImg').css("height",height * (zoomLevels[zoomLevel]/zoomLevels[zoomLevel-1]) );
	isFitToHeight=null; 
	adjustImageWrapperSize();
}

function zoomOut(){
	if(zoomLevel<=0)
		return;
	zoomLevel--;
	var width=$('#mainImg').width();
	var height=$('#mainImg').height();
	$('#mainImg').css("width",width * (zoomLevels[zoomLevel]/zoomLevels[zoomLevel+1]) ); 
	$('#mainImg').css("height",height * (zoomLevels[zoomLevel]/zoomLevels[zoomLevel+1]) );
	isFitToHeight=null; 
	adjustImageWrapperSize();
}

function rotate(angle){
	var rotationAngle=Number(angle);
	if(rotationAngle){
		rotation += rotationAngle;
		$("#mainImg").rotate({animateTo:rotation});
	}
}

function showLoading(){
	$('#loading').show();
}

function removeLoading(){
	$('#loading').hide();
	$('#imageWrapper').show();
}