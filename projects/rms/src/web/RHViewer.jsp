<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
        "http://www.w3.org/TR/html4/strict.dtd">
<html> 

<head>
	<meta http-equiv="Content-Type" content="text/html;charset=utf-8" >
	<title>NextLabs Rights Management</title>
	<link rel="shortcut icon" href="ui/css/img/favicon.ico" />
	<link rel="stylesheet" href="ui/lib/bootstrap/3.3.5/css/bootstrap.min.css" />     
	<link rel="stylesheet" href="ui/css/style.css" />
	<link rel="stylesheet" href="ui/css/viewer.css" />
	<link rel="stylesheet" href="ui/css/rhViewer.css" />
	<link rel="stylesheet" href="ui/css/font/fira.css" />
	
	<script src="ui/lib/3rdParty/jquery-1.10.2.min.js"></script>
	<script src="ui/lib/3rdParty/bootstrap.min.js"></script>
	<script src="ui/lib/3rdParty/shortcut.js"></script>
	<script src="ui/lib/3rdParty/jquery.blockUI.js"></script>
	<script src="ui/lib/3rdParty/dateformat.js"></script>
	<script src="ui/lib/rms/protect.js"></script> 
	<script src="ui/lib/rms/rmsUtil.js"></script>
	<script src="ui/app/viewers/Viewer.js"></script>
	<script src="ui/app/viewers/RHViewer.js"></script>

<script type="text/javascript">

	var g_MovementsCount = 0;
	
	var g_Action, g_Behavior, cadMetaData;

	function element(id) { return document.getElementById(id); }
	function rh() { return element("DeepView"); }

	function playFirstStep()
	{
		var step = rh().Scene.Steps.GetByIndex(0);
		step.Play();
	}

	

	function handleStepEvent(e) 
	{
		var eventReport = element("Results");
		if (eventReport == null) return;
		
	}

	function handleMouseEvent(e)
	{
		if (e.IsDoubleClick)
		
		if (e.IsMouseDown)
			
		if (e.IsMouseUp)
			
		if (e.IsMouseHit)
			
		if (e.IsMouseOut)
			
		if (e.IsMouseOver)
			
 		
		if (e.IsMouseMove)
		{
			g_MovementsCount++;
			var mm = element("MouseMove");
			if (mm != null) mm.innerHTML = g_MovementsCount;
		}
	}

	function handleNodesSelected(selection)
	{
		var output = 'NodesSelected: ';
		for (var i=0; i < selection.count; i++)
		{
			var node = selection.item(i);
			
			output += node.name + ', ';
			
			if (node.HotspotActionCount > 0)
			{
				var hid = rh().Scene.SelectedHotspotActionIndex;

				if (hid >= 0)
				{
					g_Action = node.HotspotAction(hid);
					g_Behavior = node.HotspotBehavior(hid)
					document.getElementById("hotspotAction").innerHTML = "Action: " + g_Action + "<br>Behavior: " + g_Behavior;
					document.getElementById("hotspotResult").innerHTML = "Not Executed";
				}
				else
				{
					g_Action = "";
					g_Behavior = "";
					document.getElementById("hotspotAction").innerHTML = "Action: -<br>Behavior: -";
					document.getElementById("hotspotResult").innerHTML = "Dialog cancelled";
				}
			}
		}
		
		
		
		
		return false;
	}

	function onSceneLoaded()
	{
	 

		var creator = rh().Creator;
		var runtime = rh().Runtime;

		// Create StepEventHandler and attach it to DeepView runtime
		if (creator.StepEventHandler)
		{
			var stepEventHandler = creator.StepEventHandler.Create();
			stepEventHandler.OnEvent = handleStepEvent;
			runtime.AddEventHandler(stepEventHandler);
		}
		
		// Create MouseEventHandler and attach it to DeepView runtime
		var mouseEventHandler = creator.MouseEventHandler.Create();
		mouseEventHandler.OnEvent = handleMouseEvent;
		mouseEventHandler.OnMouseDoubleClick = true;
		mouseEventHandler.OnMouseDown = true;
		mouseEventHandler.OnMouseUp = true;
		mouseEventHandler.OnMouseHit = true;
		mouseEventHandler.OnMouseMove = true;
		mouseEventHandler.OnMouseOut = true;
		mouseEventHandler.OnMouseOver = true;
		runtime.AddEventHandler(mouseEventHandler);

		// Attach nodesSelected event
		rh().NodesSelected = handleNodesSelected;
		
		rh().ShowGUIElement("right_toolbar",true);
		rh().ShowGUIElement("left_toolbar",true);
		rh().ShowGUIElement("right_click_menu",true);
		rh().ShowGUIElement("bottom_toolbar",false);
		rh().ShowMenuItem  ( "M3011" ,false);  
		rh().ShowMenuItem  ( "M3018" ,false);
		rh().ShowMenuItem  ( "M3105" ,false); 
		rh().ShowMenuItem  ( "M1171" ,false); 
		rh().ShowMenuItem  ( "M1214" ,false);
		rh().ShowMenuItem  ( "M1216" ,false);
		rh().ShowMenuItem  ( "M1218" ,false);
		rh().ShowMenuItem  ( "M1220" ,false);
		rh().ShowMenuItem  ( "M1350" ,false);
		rh().ShowMenuItem  ( "M4050" ,false);
		rh().ShowMenuItem  ( "M1297" ,false);
		rh().ShowMenuItem  ( "M3104" ,false); 	

		
		$('#DeepViewDiv').bind('contextmenu', function(e) {
		return true;
		}); 
		$('body').bind('contextmenu', function(e) {
		return false;
		}); 
		var menuEventHandler = creator.MenuEventHandler.Create();
		menuEventHandler.onEvent = handleMenuEvent;
		runtime.addEventHandler(menuEventHandler);
	}
	
	function handleMenuEvent(e)
	{
		var name = e.MenuItemName;
		var checked = e.MenuItemChecked;
	}

	function onProgress(int1, int2, message) {
	    
	}
	

	function initialisePage()
	{
		AXOrNull("SAP.rh");
		checkAuth();

		//document.getElementById("DeepView").LoadFile(filePath);
	}

	$(window).resize(function() {
		closeFileInfoPopUp();
	});
	
	var checkAuth = function(){
        var cacheId=getQueryVariable("cacheId");
        $.get('/RMS/RMSViewer/GetDocMetaData?documentId='+cacheId, function(data, status){
        	cadMetaData=data;
            if(data.errMsg && data.errMsg.length>0){
                window.location.href = '/RMS/ShowError.jsp?errMsg='+data.errMsg;
                return;
            }
			checkPrintEnabled(cadMetaData.isPrintAllowed);
			if(!cadMetaData.isPrintAllowed){
				 rh().ShowMenuItem  ( "M1279" ,false);
			}
			checkDownloadEnabled(cadMetaData.repoType); 
			if(!cadMetaData.isPMIAllowed){
				rh().ShowMenuItem ("M1181",false);
				rh().ShowMenuItem ("M3210",false);
				rh().ShowMenuItem ("M3211",false);
			}    
            var filePath=getQueryVariable("filePath");
			var fileName=cadMetaData.displayName;
			addTitle(fileName);
			if (rh() != null)
				rh().SceneLoaded = onSceneLoaded;
			rh().Progress = onProgress;
			rh().FileName=filePath;
        });
		translateTo(i18n_language);
    }
	function downloadFile(){
		var filePath = cadMetaData.filePath;
		var repoId = cadMetaData.repoId;
		var repoType = cadMetaData.repoType;
		downloadRepoFile(repoType, repoId, filePath);
	}

</script>
  
</head>
<body onload="initialisePage()" style="background-color: #F3F3F0">
	<div id="error" class="alert alert-danger alert-dismissable" style="display:none;" >
         <button type="button" class="close" onclick="closeDialog()" aria-hidden="true">x</button><span id="errmsg"></span>
    </div> 
	<div class="pageWrapper" id="pageWrapper" style="position: absolute; overflow: auto;">
		<div class="cc-header">
			<div class="cc-header-logo"></div>
			<button id="rms-help-button" title="Help" onclick="showHelp('/RMS/help/RH_Viewer.htm')" class="toolbar-button btn btn-default desktop-only spaced "> </button>
		</div>
		
		<div class = "toolbarContainerPlaceholder"> </div>
		
		<div class = "toolbarContainer fade-div">
		
			<div id="titleContainer" class="titleContainer">
				<h5 class="titleText"><b><span id="titleDesktop" class="hide-viewer-title-desktop"></span></b><b><span id="titleMobile" class="hide-viewer-title-mobile show-viewer-title-mobile"></span></b></h5>
			</div>
			
			<div id="toolBar">
				<div class="toolbar-tools">
					<button id="rms-print-button" title="Print" onclick="print()" class="toolbar-button btn btn-default desktop-only spaced print-enabled"> </button>
						
					<button id="rms-download-button" type="button" onclick="downloadFile()" class="toolbar-button download btn btn-default spaced" title="Download Protected File"></button>
        	            
					<button id="rms-info-button" type="button" onclick="showInfo()" class="toolbar-button info btn btn-default spaced" title="Info"> </button>
				</div>
			</div>					
		</div>
		
		<div id ="viewer-rms-info" class="modal-dialog modal-content">
			<div class="rms-info-filedetails">
				<div>
					<a class="rms-info-closeLink" id="fileInfoCloseBtn" onclick="closeFileInfoPopUp()">x</a>
					<div class="rms-info-fileName"><span id="fileTitle" style="font-family: fira sans;"></span>
					</div>
					<br><br>
					<table class="rms-info-fileProperties">
						<tr>
							<td width="30%"><span id="fileTypeSpan">File Type</span></td>
							<td width="30%"><span id="fileSizeSpan">Size</span></td>
							<td width="40%"><span id="fileDateSpan">Last Modified On</span></td>
						</tr>
						<tr>
							<td><b><span id="fileTypeValue">-</span></b></td>
							<td><b><span id="fileSizeValue">-</span></b></td>
							<td><b><span id="fileDateValue">-</span></b></td>
						</tr>
					</table>
				</div>
			</div>
			<div class="rms-info-repoDetails">
				<h4><span id="fileRepoNameValue">N/A</span></h4>
				<h5><span id="fileRepoPathValue">N/A</span></h5>
			</div>
			<div id="rms-info-tags" class="rms-info-tags" style="background-color:white">
				<br><br>
				<center>
					<label><span id="fileTagsSpan" style="color:black;">File Classification</span></label>
					<br>
					<div id="fileTagsValue"></div>
					<br>
				</center>
			</div>
			<div class="rms-info-rights" style="background-color:white">
				<br><br>
				<center>
					<label><span id="fileRightsSpan">File Rights</span></label>
					<br>
					<div id="fileRightsValue"></div>
					<br>
				</center>
			</div>
			<div><br><br><br></div>
		</div>
		
		<iframe id="overlay-iframe" style="display: none; left: 25%; position: absolute; top: 45%; width: 50%; z-index:1; height:100%;" src="javascript:false;" frameBorder="0" scrolling="no"></iframe>
		
		<div id="rms-viewer-content">
			<div id="all">
				<div id="DeepViewDiv">
					<object id="DeepView" type="application/rh">
					</object>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
