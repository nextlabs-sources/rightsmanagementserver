var metaData;
$(document).ready(function () {
    if (checkWebGL()) {
    	checkAuth();
    }
});

$(window).resize(function() {
    hwv.resizeCanvas();
    addWaterMark();
});

$(window).on('beforeunload', function(){
     var docId=getQueryVariable("cacheId");
     $.get('/RMS/RMSViewer/RemoveFromCache?documentId='+docId);
 });

$(window).resize(function() {
    addWaterMark(); 
    closeFileInfoPopUp();
});

$(window).on('click', function(e){
    if (hwv.getContextMenuStatus() && e.target.id != "contextMenuButton") {
        hwv.setContextMenuStatus(false);
    }
});

window.onerror = function(msg, url, lineNo, columnNo, error) {
    var webGLError = "Failed to create SceneJS Shader: Shader program failed to compile";
    if (error == webGLError) {
        window.location.href = '/viewer/ShowError.jsp?errMsg=Error occured when rendering the file. Please check your WebGL setttings.';
    }
    return false;
};

function translateTo(language){
    $.getJSON("ui/app/i18n/"+language+".json")
        .done(function( data ) {
            $("#rms-print-button").attr('title',data['viewer.toolbar.print']);
            $("#rms-help-button").attr('title',data['viewer.toolbar.help']);
            $("#rms-download-button").attr('title',data['viewer.toolbar.download']);
            $("#rms-rights-button").attr('title',data['viewer.toolbar.rights']);
            $("#rms-info-button").attr('title',data['viewer.toolbar.info']);        
            $("#home-button").attr('title',data['viewer.toolbar.cad.reset']);
            $("#view-button").attr('title',data['viewer.toolbar.cad.view']);
            $("div[data-operatorclass='toolbar-left']").attr('title',data["viewer.toolbar.cad.view.left"]);
            $("div[data-operatorclass='toolbar-right']").attr('title',data["viewer.toolbar.cad.view.right"]);
            $("div[data-operatorclass='toolbar-bottom']").attr('title',data["viewer.toolbar.cad.view.bottom"]);
            $("div[data-operatorclass='toolbar-front']").attr('title',data["viewer.toolbar.cad.view.front"]);
            $("div[data-operatorclass='toolbar-back']").attr('title',data["viewer.toolbar.cad.view.back"]);
            $("div[data-operatorclass='toolbar-top']").attr('title',data["viewer.toolbar.cad.view.top"]);
            $("div[data-operatorclass='toolbar-iso']").attr('title',data["viewer.toolbar.cad.view.iso"]);           
            $("#edgeface-button").attr('title',data['viewer.toolbar.cad.edgeface.wireframeshaded']);
            $("div[data-operatorclass='toolbar-wireframe']").attr('title',data["viewer.toolbar.cad.edgeface.wireframe"]);
            $("div[data-operatorclass='toolbar-shaded']").attr('title',data["viewer.toolbar.cad.edgeface.shaded"]);
            $("div[data-operatorclass='toolbar-wireframeshaded']").attr('title',data["viewer.toolbar.cad.edgeface.wireframeshaded"]);
            $("div[data-operatorclass='toolbar-hidden-line']").attr('title',data["viewer.toolbar.cad.edgeface.hiddenline"]);        
            $("#click-button").attr('title',data['viewer.toolbar.cad.click.select']);
            $("div[data-operatorclass='toolbar-measure-angle']").attr('title',data["viewer.toolbar.cad.click.measureangle"]);
            $("div[data-operatorclass='toolbar-measure-distance']").attr('title',data["viewer.toolbar.cad.click.measuredistance"]);
            $("div[data-operatorclass='toolbar-measure-edge']").attr('title',data["viewer.toolbar.cad.click.measureedge"]);
            $("div[data-operatorclass='toolbar-measure-point']").attr('title',data["viewer.toolbar.cad.click.measurepoint"]);
            $("div[data-operatorclass='toolbar-select']").attr('title',data["viewer.toolbar.cad.click.select"]);                
            $("#camera-button").attr('title',data['viewer.toolbar.cad.camera.orbit']);
            $("div[data-operatorclass='toolbar-walk']").attr('title',data["viewer.toolbar.cad.camera.walk"]);
            $("div[data-operatorclass='toolbar-turntable']").attr('title',data["viewer.toolbar.cad.camera.turntable"]);
            $("div[data-operatorclass='toolbar-orbit']").attr('title',data["viewer.toolbar.cad.camera.orbit"]);     
            $("#explode-button").attr('title',data['viewer.toolbar.cad.explode']);
            $("#cuttingplane-button").attr('title',data['viewer.toolbar.cad.cuttingplane']);
            $("#settings-button").attr('title',data['viewer.toolbar.cad.settings']);
            $("#propertyAreaSpan").text(data['viewer.cad.property.plaeholder']);
            i18n_data = data;
        }).fail(function(jqxhr, textStatus, error) {
            console.log(textStatus + ", " + error);
        });
}

var checkAuth = function(){
    var cacheId=getQueryVariable("cacheId");
    
    $.blockUI({
        message: '<img src="ui/css/img/loading_48.gif" />',
        css: {
            width: '4%',
            textAlign: 'center',
            left: '50%',
            top: '50%',
            border: '0px solid #FFFFFF',
            cursor: 'wait',
            backgroundColor: '#FFFFFF'
        },
        overlayCSS: {
            backgroundColor: '#FFFFFF',
            opacity: 1.0,
            cursor: 'wait'
        }
    });
    
    $.get('/RMS/RMSViewer/GetDocMetaData?documentId='+cacheId, function(data, status){
        if(data.errMsg && data.errMsg.length>0){
            window.location.href = '/RMS/ShowError.jsp?errMsg='+data.errMsg;
            return;
        }
        metaData=data;
        
		var origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port : '');
        var file = getParameterByName('file');
        var url = origin + "/RMS/" + file;

        hwv = new Communicator.WebViewer({
            containerId: "viewerContainer",
            endpointUri: url
        });

        hwv.setCallbacks({
            sceneReady: function() {
                $.unblockUI();
            }
        });

        ui = new Communicator.Ui.Desktop.DesktopUi(hwv);
        hwv.start();
        
        var TITLE_MAX_LENGTH_DESKTOP = 50;
        var TITLE_MAX_LENGTH_MOBILE = 15;
        
        
        filename = metaData.displayName;
        if (filename != null) {
            var titleDesktopSpan = document.getElementById('titleDesktop');
            var titleMobileSpan = document.getElementById('titleMobile');
            titleDesktopSpan.innerHTML += getShortName(filename, TITLE_MAX_LENGTH_DESKTOP);
            titleMobileSpan.innerHTML += getShortName(filename, TITLE_MAX_LENGTH_MOBILE);
            $("#titleDesktop").attr("title", filename);
            $("#titleMobile").attr("title", filename);
            $('[data-toggle="tooltip"]').tooltip();
        }
        checkPrintEnabled(metaData.isPrintAllowed);
        checkDownloadEnabled(metaData.repoType); 
        checkPropertyWindowEnabled(metaData.isPMIAllowed);
        checkDWG(metaData.displayName);
        addWaterMark();
    });
    translateTo(i18n_language);
}

function addWaterMark() {
    var watermark = metaData.watermark;
    if (watermark == null)
        return;
    var body = document.body,
        html = document.documentElement;
    var height = document.getElementById("viewerContainer").clientHeight;
    //  var tempHeight=html.clientHeight;
    var width = document.getElementById("viewerContainer").clientWidth;
    var diagonal = Math.sqrt(width * width + height * height);
    var overlay = watermark.waterMarkStr;
    var split = watermark.waterMarkSplit;
    overlay = htmlEntityEncode(overlay);
    overlay = overlay.replace(/\n/gi, "<br/>");
    var lines = overlay.split("<br/>");
    var i, j, maxLineLength = 0;
    for (i = 0; i < lines.length; i++) {
        maxLineLength = Math.max(maxLineLength, lines[i].length);
    }
    var fontsize = watermark.waterMarkFontSize;
    var fontcolor = watermark.waterMarkFontColor;
    var transparency = watermark.waterMarkTransparency / 100;
    var fontname = watermark.waterMarkFontName;
    var density = watermark.waterMarkDensity;
    var rotation = watermark.waterMarkRotation;
    var watermarkWidth = 0;
    var watermarkHeight = 0;
    var watermarkHeightOffset = 0;
    var watermarkWidthOffset = 0;

    if (!new Detector().detect(fontname)) {
        fontname = "Arial";
    }
    if (density == "Dense") {
        watermarkHeightOffset = 150;
        watermarkWidthOffset = 150;
    } else {
        watermarkHeightOffset = 200;
        watermarkWidthOffset = 200;
    }
    watermarkWidthOffset = watermarkWidthOffset * fontsize / 36;
    watermarkHeightOffset = watermarkHeightOffset * fontsize / 36;
    var tempDiv = "<div  class=\"watermark\" style=\"font-size:" + fontsize + "px;color:" + fontcolor + ";position:absolute;pointer-events: none;z-index:1000;white-space:nowrap;opacity:" + transparency + "; font-family:" + fontname + ";\"><p><span style=\"display:inline-block; text-align:center\" id=\"Test\">" + overlay + "</span></p></div>";
    var rotation_css = "-ms-transform-origin:0px 0px; -webkit-transform-origin:0px 0px; -moz-webkit-transform-origin:0px 0px; transform-origin:0px 0px;";
    if (rotation == "Clockwise") {
        var translateX = watermarkHeight + 15;
        rotation_css = rotation_css + "-ms-transform: translate(" + translateX + "px,0px) rotate(45deg); -webkit-transform: translate(" + translateX + "px,0px) rotate(45deg); -moz-webkit-transform: translate(" + translateX + "px,0px) rotate(45deg); transform: translate(" + translateX + "px,0px) rotate(45deg);";
    } else {
        var translateX = -watermarkHeight + 15;
        var translateY = height;
        rotation_css = rotation_css + "-ms-transform: translate(" + translateX + "px," + translateY + "px) rotate(-45deg); -webkit-transform: translate(" + translateX + "px," + translateY + "px) rotate(-45deg); -moz-webkit-transform: translate(" + translateX + "px," + translateY + "px) rotate(-45deg); transform: translate(" + translateX + "px," + translateY + "px) rotate(-45deg);";
    }

    $("#waterMarkContainer").remove();
    var waterMarkContainer = "<div id=\"waterMarkContainer\" style=\"display:inline-block;\"> </div>";
    var waterMarkRow = "<div id=\"waterMarkRow\" style=\"white-space:nowrap;  z-index: 1000; position:absolute; display:inline-block;" + rotation_css + "; font-family:" + fontname + "\"> </div>";
    waterMarkContainer = $(waterMarkContainer).clone();
    $(".Wrapper").prepend(waterMarkContainer);
    $(".Wrapper").prepend($(waterMarkRow).clone());

    for (var j = 0; j < diagonal + 500; j = j + watermarkWidth + watermarkWidthOffset) {
        $("#waterMarkRow").append($(tempDiv).clone().css({
            'margin-top': '0px',
            'margin-left': j + 'px'
        }));
        if (watermarkHeight == 0) {
            watermarkHeight = $("#waterMarkRow")[0].children[0].clientHeight;
            watermarkWidth = $("#waterMarkRow")[0].children[0].clientWidth;
        }
    }
    for (var j = 0; j < diagonal + 500; j = j + watermarkWidth + watermarkWidthOffset) {
        $("#waterMarkRow").append($(tempDiv).clone().css({
            'margin-left': j + 'px'
        }));
    }
    for (var i = -diagonal; i < diagonal + 500; i = i + watermarkHeight + watermarkHeightOffset) {
        waterMarkRow = $("#waterMarkRow").clone().css('margin-top', i + 'px');
        $(waterMarkContainer).append(waterMarkRow);
    }

    waterMarkContainer.css({
        'height': height + "px",
        'position': 'fixed',
        'z-index': '1000'
    });
    $("#waterMarkRow").remove();
}

function checkDWG(displayName){
    if(displayName.match(/.DWG$/i)!=null){
        $("#modelBrowserWindow").remove();
        $("#select-parts-button").addClass("disabled");
        $("#measure-point-button").addClass("disabled");    
    }
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

function downloadFile(){
    var filePath = metaData.filePath;
    var repoId = metaData.repoId;
    var repoType = metaData.repoType;
    
    downloadRepoFile(repoType, repoId, filePath);
}

function showRights(){
    displayRights(metaData);
}

function showInfo(){
    displayInfo(metaData);
}

function checkPropertyWindowEnabled(isPropertyWindowAllowed) {
    if (!isPropertyWindowAllowed) {
        $("#propertyWindow").remove();
    }
}

function checkDWG(displayName) {
    if (displayName.match(/.DWG$/i) != null) {
        $("#modelBrowserWindow").remove();
        $("#select-parts-button").addClass("disabled");
        $("#measure-point-button").addClass("disabled");
    }
}

function checkWebGL() {
    if (!window.WebGLRenderingContext) {
        // the browser doesn't even know what WebGL is
        window.location.href = '/viewer/ShowError.jsp?errMsg=Your browser does not support webGL. This is required for visualizing CAD files in the web browser.';
        return false;
    } else {
        var canvas = document.createElement('canvas');
        var context = canvas.getContext("webgl");
        var exp_context = canvas.getContext('experimental-webgl');
        if (context == null && exp_context == null) {
            // browser supports WebGL but initialization failed.
            window.location.href = '/viewer/ShowError.jsp?errMsg=Please enable <a href="https://get.webgl.org/"> WebGL</a> in your web browser.';
            return false;
        }
        var opts = {
            failIfMajorPerformanceCaveat: true
        };
        if ((context != null && document.createElement('canvas').getContext("webgl", opts) == null) ||
            (exp_context != null && document.createElement('canvas').getContext("experimental-webgl", opts) == null)) {
            handleError("Major Performance Caveats detected. Please ensure you have enabled GPU rendering in your browser.");
        }
    }
    return true;
}
/* Overwriting protect.js to enable panning */
if (typeof RightMouseDown !== "undefined" || typeof mouseDown !== "undefined") {
    var originalRightMouseDown = RightMouseDown;
    RightMouseDown = function(e) {
        if (e.target.id != "viewerContainer-canvas-container") {
            return false;
        }
        return true;
    };

    var originalMouseDown = mouseDown;
    var mouseDown = function(e) {
        if (e.which == 3) {
            if (e.target.id != "viewerContainer-canvas-container")
                return false;
        }
        return true;
    };

    document.oncontextmenu = RightMouseDown;
    document.onmousedown = mouseDown;
}