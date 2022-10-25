var metaData;
var headerToolbarHeight;

$(document).ready(function(){
	addErrorDiv();
	$('#loading').hide();
	getMetaData();
	translateTo(i18n_language);
	$("ul.ISYS_TOC").addClass("fade-div");
	$(".toolbarContainerPlaceholder").css({	"background-color": "white"});
	$(".toolbarContainerPlaceholder").each(function () {
		this.style.setProperty( 'border-bottom', '3px solid white', 'important' );
	});
	$("ul.ISYS_TOC").css({"height":"auto"});
	headerToolbarHeight = $(".cc-header").height()+$(".toolbarContainer").height()+$("ul.ISYS_TOC").height();
	$(".ISYS_BODY_DIV").css('top', headerToolbarHeight+"px");
});

$(window).resize(function() {
     closeFileInfoPopUp();
});

$(window).on("hashchange", function (e) {
	var height = $("#pageWrapper").scrollTop()-headerToolbarHeight-5;
	$("#pageWrapper").scrollTop(height);
});

function translateTo(language){
	$.getJSON("../../../ui/app/i18n/"+language+".json")
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

function addErrorDiv(){
	$('body').prepend("<div id=\"error\" class=\"alert alert-danger alert-dismissable\" style=\"display:none; top:55%;\"><button type=\"button\" class=\"close\" onclick=\"closeDialog()\" aria-hidden=\"true\">x</button><span id=\"errmsg\"></span></div>");
}

function getRightsFromMetaData(metaData){
	var rights = '<li><div><img src="../../../ui/css/img/icn-fileinfo-view.png"/><br><h5>View</h5></br></div></li>';
	if(metaData.isPrintAllowed)
		rights += '<li><div><img src="../../../ui/css/img/icn-fileinfo-print.png"/><br><h5>Print</h5></br></div></li>';
	if(metaData.isPMIAllowed)
		rights += '<li><div><img src="../../../ui/css/img/icn-fileinfo-PMI.png"/><br><h5>PMI</h5></br></div></li>';
	return rights;
}

function getMetaData(){
	var docId=$( "#documentId").attr("value");
	$.get('/RMS/RMSViewer/GetDocMetaData?documentId='+docId, function(data, status){
         // alert(data.errMsg);
        if(data.errMsg && data.errMsg.length>0){
            window.location.href = '/RMS/ShowError.jsp?errMsg='+data.errMsg;
            return;
        }
        metaData=data;
		checkPrintEnabled(metaData.isPrintAllowed);
		checkDownloadEnabled(metaData.repoType); 
    });
}

function showInfo(){
	displayInfo(metaData);
}

function downloadFile(){
	var filePath = metaData.filePath;
	var repoId = metaData.repoId;
	var repoType = metaData.repoType;
	downloadRepoFile(repoType, repoId, filePath);
}


$(window).on('beforeunload', function(){
	var docId=$( "#documentId").attr("value");
	$.get('/RMS/RMSViewer/RemoveFromCache?documentId='+docId);
	
});

function printFile () {
	$('#pageWrapper').css({'position':'static', 'overflow':'visible'});
	var isWaterMarkPresent = document.getElementById("waterMarkRootContainer");
	if (isWaterMarkPresent) {
		printWatermarkRepeat();
		$('#waterMarkRootContainer').css('position','fixed');
	}
	window.scrollTo(0,0);	//move the viewport to origin
	window.print();
	if (isWaterMarkPresent) {
		$('#waterMarkRootContainer').css('position','absolute');
	}
	$('#pageWrapper').css({'position':'absolute', 'overflow':'auto'});
}
