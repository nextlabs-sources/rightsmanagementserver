<!DOCTYPE HTML>
<html>
<head>
    <meta charset="UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="keywords" content="Control Shell" />
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
    <!--<meta content="width=device-width, initial-scale= 1.0, user-scalable=no" name="viewport"> -->
	<meta name="viewport" content="width=device-width; initial-scale=0.5; maximum-scale=0.5; user-scalable=yes" />
    <meta name="HandheldFriendly" content="True" />
    <title>Nextlabs Rights Management</title>
    <link rel="shortcut icon" href="ui/css/img/favicon.ico" />
	<link rel="stylesheet" href="ui/lib/bootstrap/3.3.5/css/bootstrap.min.css" />     
	<link rel="stylesheet" href="ui/css/style.css" />
	<link rel="stylesheet" href="ui/css/viewer.css" />
	<link rel="stylesheet" href="ui/css/font/fira.css" />
	
    <script src="ui/app/viewers/SAPViewer/resources/sap-ui-core.js" type="text/javascript"
            id="sap-ui-bootstrap" 
            data-sap-ui-libs="sap.ui.commons, sap.m, sap.ve">
    </script>
	
	
	<script src="ui/lib/3rdParty/bootstrap.min.js"></script>
	<script src="ui/lib/3rdParty/shortcut.js"></script>
	<script src="ui/lib/3rdParty/jquery.blockUI.js"></script>
	<script src="ui/lib/rms/protect.js"></script> 
	<script src="ui/lib/rms/rmsUtil.js"></script>
    <script src="ui/lib/3rdParty/fontChecker.js"></script>
	<script src="ui/lib/3rdParty/dateformat.js"></script>
	<script src="ui/app/viewers/Viewer.js"></script>
	<script src="ui/app/viewers/VDSViewer.js"></script>

    <script>
        // HELPER: Get parameter from URL
        function getUrlParams() {
            var params = [], paramPair;
            var parameters = decodeURIComponent(window.location.href.slice(window.location.href.indexOf('?') + 1));
            var paramStrings=parameters.split('&');
            for (var i = 0; i < paramStrings.length; i++) {
                paramPair = paramStrings[i].split('=');
                params.push(paramPair[0]);
                params[paramPair[0]] = paramPair[1];
            }
            return params;
        }
    </script>
    <script type="text/javascript" defer>
        var params = getUrlParams();
        var sidePanelWidth = 250;
        var verticalSpacerWidth = 10;
        var approxMargin = 8;
        var bLoggingActive = false;
        var advanced = params["advanced"] ? params["advanced"] == "true" : false;
		//Increasing the memory to 64Mb
        var totalMemory=(64*1024 * 1024);
        jQuery.sap.require("sap.ve.js.dvl");
        jQuery.sap.require("sap.ve.Viewer");
        jQuery.sap.require("sap.ve.ViewerStepNavigation");
        var oViewer;
        var oStepNavigation;
        var filePath;
        var cadMetaData;
        var cacheId;
        $(window).resize(function() {
            oViewer.resize(); 
            addWaterMark();
			closeFileInfoPopUp();
           // addPrintDiv();
        });
        $(document).ready(function(){
            checkWebGL();
            checkAuth();
        });

        var checkAuth = function(){
			$.blockUI({ message: '<img src="ui/css/img/loading_48.gif" />' ,
						css: { width: '4%', textAlign:  'center',left:'50%',border:'0px solid #FFFFFF',cursor:'wait',backgroundColor:'#FFFFFF'},
						overlayCSS:  { backgroundColor: '#FFFFFF',opacity:1.0,cursor:'wait'} 
            });  
            $("#pageWrapper").hide();
			
            var cacheId=getQueryVariable("cacheId");
            $.get('/RMS/RMSViewer/GetDocMetaData?documentId='+cacheId, function(data, status){
                 // alert(data.errMsg);
                if(data.errMsg && data.errMsg.length>0){
                    window.location.href = '/RMS/ShowError.jsp?errMsg='+data.errMsg;
                    return;
                }
                cadMetaData=data;
				checkPrintEnabled(cadMetaData.isPrintAllowed);
				checkDownloadEnabled(cadMetaData.repoType); 
                initialize();
            });
			
        }

        function initialize(){
			//Create the viewer and Step Navigation control
            oViewer = new sap.ve.Viewer({ height: 512, width:512, totalMemory: totalMemory, autoResize:true });
            oStepNavigation = new sap.ve.ViewerStepNavigation({logEnabled:true, viewerInstance: oViewer});
            var params=getUrlParams();
            filePath=params.filePath;
            cacheId=params.cacheId;
            addTitle();
            //**********************************************
            //****  CREATE MAIN PAGE LAYOUT             ****
            //**********************************************
        
			oViewer.attachFileLoadProgressNotified(function (oEvent){
				var fProgress = oEvent.getParameter("fProgress"); 
				var percent = Math.round(fProgress*100);
				if(percent==100){
					addWaterMark();
					addFileInfoDiv();
					addErrorDiv();
					translateTo(i18n_language);
					$("#pageWrapper").show();
					$.unblockUI();
					setTimeout(checkSteps, 10); 
				}
				console.log("Progress:" + percent);
			});


            var oViewerSampleLayout = new sap.ui.layout.VerticalLayout("ViewerSampleLayout", {
                width: "100%",
                content: [
                    new sap.m.FlexBox({
                        alignItems: "Start",
                        justifyContent: "Start",
                        items: [
                            new sap.m.VBox({width: verticalSpacerWidth + "px"}),
                            new sap.ui.layout.VerticalLayout("ViewerSampleControls",{
                                content: [oViewer,oStepNavigation]  
                            })]
                    })]
            });
            
            // <fieldset><legend>Personalia:</legend>
            oViewerSampleLayout.placeAt("body");
			
        } 
        var loadFile=setInterval(function () {
			if(oViewer.loco!=null){
				oViewer.oDvl.Renderer.SetBackgroundColor(1,1,1,1,1,1);
				oViewer.loadFile(filePath);				
				clearInterval(loadFile);
			}
		}, 3000);

		function addErrorDiv() {
            var errorDiv="<div id=\"error\" class=\"alert alert-danger alert-dismissable\" style=\"display:none\" ><button type=\"button\" class=\"close\" onclick=\"closeDialog()\" aria-hidden=\"true\">x</button><span id=\"errmsg\"></span></div>";
            $("body").prepend(errorDiv);
        }
		
        function addStepsDiv(){
			var step=$("#__navigation0-stepscroller").clone();
			$(".toolbarContainer").prepend(step);
        }
    </script>
</head>
<body id="body" style="overflow: hidden;" >
	<div class="pageWrapper" id="pageWrapper">
	
		<div class="cc-header">
			<div class="cc-header-logo"></div>
			<button id="rms-help-button" title="Help" onclick="showHelp('/RMS/help/VDS_Viewer.htm')" class="toolbar-button btn btn-default desktop-only spaced "> </button>
		</div>
		
		<div class = "toolbarContainerPlaceholder"> </div>
		
		<div class = "toolbarContainer fade-div">
		
			<div id="titleContainer" class="titleContainer">
				<h5 class="titleText"><b><span id="titleDesktop" class="hide-viewer-title-desktop"></span></b><b><span id="titleMobile" class="hide-viewer-title-mobile show-viewer-title-mobile"></span></b></h5>
			</div>
		
			<div id="toolBar">
				<div class="toolbar-tools">
				
					<button id="vds-toggleStepInFo" title="Toggle Step Info" class="toolbar-button btn btn btn-default spaced menu-button" onclick="toggleStepInFo()"></button>

					<button id="vds-resetStep" title="Reset" class="toolbar-button btn btn btn-default spaced menu-button" onclick="resetStep()"></button>
					
					<button id="vds-play" title="Play Current Scene" class="toolbar-button btn btn btn-default spaced menu-button" onclick="play()"></button>
					
					<button id="vds-pauseStep" title="Pause" class="toolbar-button btn btn btn-default spaced menu-button" onclick="pauseStep()"></button>
					
					<button id="vds-playAll" title="Play All" class="toolbar-button btn btn btn-default spaced menu-button dont-show-toolbar-vds" onclick="playall()"></button>
					
					<div class="tool-seperator "></div>
					
					<button id="rms-print-button" title="Print" onclick="printModel()" class="toolbar-button btn btn-default spaced print-enabled dont-show-toolbar-vds"> </button>
						
					<button id="rms-download-button" type="button" onclick="downloadFile()" class="toolbar-button download btn btn-default spaced" title="Download Protected File"></button>
        	            
					<button id="rms-info-button" type="button" onclick="showInfo()" class="toolbar-button info btn btn-default spaced" title="Info"> </button>
				</div>
			</div>
		</div>
		
		
		
		
		<div id="printTemp" style="visibility: hidden"></div>
	</div>
</body>
</html>
