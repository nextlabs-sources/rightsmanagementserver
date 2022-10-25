<!doctype html>
<html>
    <head>
        <meta charset="utf-8"/>
        <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
        <meta http-equiv="pragma" content="no-cache" />
        <meta http-equiv="X-UA-Compatible" content="chrome=1, IE=edge">
		<meta name="viewport" content="width=device-width, initial-scale=0.5, maximum-scale=0.5, user-scalable=yes" />
        <title>NextLabs Rights Management</title>
        <link rel="shortcut icon" href="ui/css/img/favicon.ico" />
        <link rel="stylesheet" href="ui/lib/bootstrap/3.3.5/css/bootstrap.min.css" />     
		<link rel="stylesheet" href="ui/css/style.css" />
		<link rel="stylesheet" href="ui/css/viewer.css" />
		<link rel="stylesheet" href="ui/css/font/fira.css" />
  
    </head>
	
    <body style="overflow: hidden;">
        <div id="error" class="alert alert-danger alert-dismissable" style="display:none" >
             <button type="button" class="close" onclick="closeDialog()" aria-hidden="true">x</button><span id="errmsg"></span>
        </div>
       
		<div class="pageWrapper" id="pageWrapper">
			<div class="cc-header">
				<div class="cc-header-logo"></div>
				<button id="rms-help-button" title="Help" onclick="showHelp('/RMS/help/Document_Viewer.htm')" class="toolbar-button btn btn-default desktop-only spaced "> </button>
			</div>
			
			<div class = "toolbarContainerPlaceholder"> </div>
			
			<div class = "toolbarContainer fade-div">
			
				<div id="titleContainer" class="titleContainer">
					<h5 class="titleText"><b><span id="titleDesktop" class="hide-viewer-title-desktop"></span></b><b><span id="titleMobile" class="hide-viewer-title-mobile show-viewer-title-mobile"></span></b></h5>
				</div>
				
				<div id="toolBar">
					<div class="toolbar-tools">

						<button id="rms-rotate-left-button" onclick="rotate(-90)" class="toolbar-button btn btn-default spaced dont-show-toolbar-doc"  title="Rotate Anticlockwise"></button>
													
						<button id="rms-rotate-right-button" onclick="rotate(90)" class="toolbar-button btn btn-default spaced dont-show-toolbar-doc" title="Rotate Clockwise"></button>
						
						<div class="tool-seperator dont-show-toolbar-doc"></div>
						
						<button id="rms-print-button" title="Print" onclick="printAllPages()" class="toolbar-button btn btn-default spaced print-enabled dont-show-toolbar-doc"></button>
                    
                        <button id="rms-download-button" type="button" onclick="downloadFile()" class="toolbar-button download btn btn-default spaced" title="Download Protected File"></button>
        	            
                        <button id="rms-info-button" type="button" onclick="showInfo()" class="toolbar-button info btn btn-default spaced" title="Info"> </button>

					</div>
				</div>		

				<div id="pageNumContainer">
					<b><span id="pageNumberDisplay"></span></b>
					<span>/</span>
					<span id="totalPageNum"></span>
				</div>
				
            </div>
			
			<div id="loading">
				<img id="loading-image" src="ui/css/img/loading_48.gif" alt="Loading..." />
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

			<div id="rms-viewer-content">
				<div id="imageWrapper" class="imageWrapper noselect">
					<img id="mainImg" src=""/>
				</div>
				
				<div id="prev-btn" class="fade-div disabled" style="position: absolute; top: 50%; left:20px;">
					<button id="rms-prev-button" onclick='navigate("previous")' class="btn btn-default spaced" data-toggle="tooltip" data-placement="bottom" title="Previous Page" style="height:60px;"></button>       		
				</div>
				
				<div id="next-btn" class="fade-div" style="position: absolute; top: 50%; right:20px;">
					<button id="rms-next-button" onclick='navigate("next")' class="btn btn-default spaced" data-toggle="tooltip" data-placement="bottom" title="Next Page" style="height:60px;"></button>
				</div>
				
				<div class="fade-div" style = "position: absolute; bottom: 50px; right:20px;">
					<button id="rms-fit-height-button" onclick="fitToHeight()" class="toolbar-button btn btn-default spaced" title="Fit to Height"></button>
					<button id="rms-fit-width-button" onclick="fitToWidth()" class="toolbar-button btn btn-default spaced" title="Fit to Width"></button>
					<button id="rms-zoom-in-button" onclick="zoomIn()" class="toolbar-button btn btn-default spaced" title="Zoom in"></button>
					<button id="rms-zoom-out-button" onclick="zoomOut()" class="toolbar-button btn btn-default spaced" title="Zoom out"></button>
				</div>
			</div>
        </div>

        <div id="printDiv"></div>
      
		<script src="ui/lib/3rdParty/jquery-1.10.2.min.js"></script>
		<script src="ui/lib/3rdParty/jqueryRotate.js"></script>
		<script src="ui/lib/3rdParty/jquery.scrollTo.min.js"></script>
		<script src="ui/lib/3rdParty/bootstrap.min.js"></script>
        <script src="ui/lib/3rdParty/shortcut.js"></script>
        <script src="ui/lib/3rdParty/jquery.blockUI.js"></script>
		<script src="ui/lib/3rdParty/dateformat.js"></script>
		<script src="ui/lib/rms/protect.js"></script>
        <script src="ui/lib/rms/rmsUtil.js"></script>
		<script src="ui/app/viewers/Viewer.js"></script>
		<script src="ui/app/viewers/DocViewer.js"></script>
    </body>
</html>