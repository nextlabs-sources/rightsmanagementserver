var i18n_language = "en";
var i = null;
var prevPos = "";
var mouseOnToolbar = false;
var i18n_data;

// fix for viewport scaling bug on iOS
// http://webdesignerwall.com/tutorials/iphone-safari-viewport-scaling-bug
(function(doc) {
	var isiPad = navigator.userAgent.match(/iPad/i) != null;
	if (isiPad) {
		var addEvent = 'addEventListener',
			type = 'gesturestart',
			qsa = 'querySelectorAll',
			scales = [1, 1],
			meta = qsa in doc ? doc[qsa]('meta[name=viewport]') : [];

		function fix() {
			meta.content = 'width=device-width,minimum-scale=' + scales[0] + ',maximum-scale=' + scales[1];
			doc.removeEventListener(type, fix, true);
		}

		if ((meta = meta[meta.length - 1]) && addEvent in doc) {
			fix();
			scales = [.25, 1.6];
			doc[addEvent](type, fix, true);
		}
	}

}(document));

$(document).ready(function(){

	$(".pageWrapper").mousemove(function(e) {
		var currPos = e.pageX + '-' + e.pageY;
		if (prevPos != currPos && !mouseOnToolbar) {
			clearTimeout(i);
			$(".fade-div").fadeIn(500);
			i = setTimeout(function () {
				$(".fade-div").fadeOut(500);
			}, 3000);
			prevPos = currPos;
		}
	});
	
	$(".fade-div").mouseover(function(e) {
		mouseOnToolbar = true;
		clearTimeout(i);
	}).mouseleave(function(e) {
		mouseOnToolbar = false;
		clearTimeout(i);
	});

	$(document).mousedown(function() {
		if($(event.target).is("#fileInfoCloseBtn")) {
			closeFileInfoPopUp();
		}
		if($(event.target).is("#viewer-rms-info, #viewer-rms-info *")) {
			return;
		}
		if($("#viewer-rms-info").is(":visible")) {
			closeFileInfoPopUp();
		}
	});
	
});

function closeDialog() {
    document.getElementById("error").style.display = "none";
    document.getElementById("rms-viewer-content").style.opacity=1.0;
}

function handleError(message){
    if(document.getElementById("rms-viewer-content")!=undefined){ 
        document.getElementById("rms-viewer-content").style.opacity=0.4;
        document.getElementById("errmsg").innerHTML = message;
        document.getElementById("error").style.display='block';
		closeFileInfoPopUp();
    }
}

function showHelp(url){
    helpWindow=window.open(url,"NextlabsViewerHelp", "height=800,width=800, resizable=yes, scrollbars=yes");
    helpWindow.focus();
}

function checkDownloadEnabled(repoType){
	if (!repoType || repoType == null || repoType == ""){
		$("#rms-download-button").remove();
	}
}

function checkPrintEnabled(isPrintAllowed){
    if(!isPrintAllowed){
        $("#rms-print-button").addClass("disabled");
		$("#rms-print-button").attr("disabled","true");
    }
}

function displayInfo(metaData){
	
	var fileSize = getReadableFileSize(metaData.fileSize, 2);
	var lastModifiedDate = "";
	if(metaData.lastModifiedDate != 0)
		lastModifiedDate = getReadableDate(metaData.lastModifiedDate);
	else
		lastModifiedDate = "-";
	$("#fileTypeSpan").text(i18n_data['file.type']);
	$("#fileSizeSpan").text(i18n_data['file.size']);
	$("#fileDateSpan").text(i18n_data['file.modified.time']);
	$("#fileRightsSpan").text(i18n_data['file.rights']);
	$("#fileTagsSpan").text(i18n_data['file.classification']);
	$("#fileRepoNameValue").text(i18n_data['not-applicable']);
	$("#fileRepoPathValue").text(i18n_data['not-applicable']);
	
	$("#fileTitle").text(metaData.displayName);
	$("#fileTypeValue").text(metaData.displayName.split('.').pop());
	$("#fileSizeValue").text(fileSize);
	$("#fileDateValue").text(lastModifiedDate);
	$("#fileRepoNameValue").text(metaData.repoName);
	$("#fileRepoPathValue").text(metaData.displayPath);
	
	$("#fileTagsValue").html(getTagsFromMetaData(metaData));
	$("#fileRightsValue").html(getRightsFromMetaData(metaData));
	$("#viewer-rms-info").slideDown(400);
}


function getTagsFromMetaData(metaData){
	var tagsMap = generateTagListFromTagMap(metaData.tagsMap);
	var tags ="";
	Object.keys(tagsMap).forEach(function (tagKey) { 
		tags += '<li><span>'+tagKey+'</span></br><b>'+tagsMap[tagKey]+'</b></li>';
	})
	if ((Object.keys(tagsMap)) && ((Object.keys(tagsMap).length % 2) == 1)) {
		tags += '<li><span> </span></br><b> </b></li>';
	}
	if (tags.length == 0) {
		tags = '<span>' + i18n_data['file.noclassification'] + '</span>';
	}
    return tags;
}

function getRightsFromMetaData(metaData){
	var rights = '<li><div><img src="ui/css/img/icn-fileinfo-view.png"/><br><h5>View</h5></br></div></li>';
	if(metaData.isPrintAllowed)
		rights += '<li><div><img src="ui/css/img/icn-fileinfo-print.png"/><br><h5>Print</h5></br></div></li>';
	if(metaData.isPMIAllowed)
		rights += '<li><div><img src="ui/css/img/icn-fileinfo-PMI.png"/><br><h5>PMI</h5></br></div></li>';
	return rights;
}

function getReadableFileSize(bytes, precision) {
    var units = ['bytes','KB','MB','GB','TB','PB','EB','ZB','YB'];
	if ( isNaN( parseFloat( bytes )) || ! isFinite( bytes ) ) {
	  return '-';
	}
	if (typeof precision === 'undefined') {
		precision = 1;
	}
	var unit = 0;

	while ( bytes >= 1024 ) {
	  bytes /= 1024;
	  unit ++;
	}
	if(units[unit]=='KB'){
	  return bytes.toFixed() +' '+units[unit];
	}
	return bytes.toFixed( + precision ) + ' ' + units[ unit ];
}

function getReadableDate(time){
	var shortFormat = "h:MM TT";	//"h:m a"
	var fullFormat = "d mmm yyyy, h:MM TT";			//"d MMM yyyy,h:m a";
	
	var date = null;
	date = new Date(time);
	var now = new Date();
	var content = '';
	if (now.getFullYear() == date.getFullYear() && now.getMonth() == date.getMonth() && now.getDate() == date.getDate()) {
	// today
		if (now.getHours() > date.getHours()) {
		  var hrPassed = now.getHours() - date.getHours();
		  content = 'today, ' + hrPassed + ' ' + (hrPassed == 1 ? 'hour' : 'hours') + ' ago'
		} else if (now.getMinutes() > date.getMinutes()) {
		  var minPassed = now.getMinutes() - date.getMinutes();
		  content = 'today, ' + minPassed + ' ' + (minPassed == 1 ? 'min' : 'mins') + ' ago'
		} else {
		  content = 'a moment ago';
		}
	} else {
		var yesterday = new Date();
		yesterday.setDate(now.getDate() - 1);
		if (now.getFullYear() == date.getFullYear() && now.getMonth() == date.getMonth() && now.getDate() == date.getDate() + 1) {
		  // yesterday
		  content = 'yesterday, ' + dateFormat(time, shortFormat);
		} else {
		  content = dateFormat(time, fullFormat);
		}
	}
	return content;
}

function closeFileInfoPopUp(){
	$("#viewer-rms-info").hide();
}

function displayingOnMobile() {
  var check = false;
  (function(a){if(/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0,4)))check = true})(navigator.userAgent||navigator.vendor||window.opera);
  return check;
}

function getShortName (data, DATA_MAX_LENGTH) {
    if (data.length > DATA_MAX_LENGTH) {
        var str = data.slice(0, DATA_MAX_LENGTH - 1);
        str = str + "...";
    } else {
        str = data;
    }
    return str;
}

/*
taken from https://www.owasp.org/index.php/XSS_(Cross_Site_Scripting)_Prevention_Cheat_Sheet
*/
function htmlEntityEncode(str) {
    return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#x27;').replace(/\//g, '&#x2F;');
}