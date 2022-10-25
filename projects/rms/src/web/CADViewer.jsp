<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false" session="false" %>

<!doctype html>
<html>
    <head>
        <meta charset="utf-8"/>
        <meta http-equiv="Content-type" content="text/html; charset=utf-8" />
        <meta http-equiv="pragma" content="no-cache" />
        <meta http-equiv="X-UA-Compatible" content="chrome=1, IE=edge">
		<title>NextLabs Rights Management</title>
		<link rel="shortcut icon" href="ui/css/img/favicon.ico" />
        <link rel="stylesheet" href="ui/lib/bootstrap/3.3.5/css/bootstrap.min.css" />
        
		<link rel="stylesheet" href="ui/app/viewers/cadviewer/css/TreeControl.css" type="text/css" />
		<link rel="stylesheet" href="ui/app/viewers/cadviewer/css/Toolbar.css" type="text/css" />
		<link rel="stylesheet" href="ui/app/viewers/cadviewer/css/Common.css" type="text/css" />
		<link rel="stylesheet" href="ui/app/viewers/cadviewer/css/Desktop.css" type="text/css" />
		<link rel="stylesheet" href="ui/app/viewers/cadviewer/css/NoteText.css" type="text/css" />
		<link rel="stylesheet" href="ui/app/viewers/cadviewer/css/jquery-ui.min.css" type="text/css" />
		<link rel="stylesheet" href="ui/app/viewers/cadviewer/css/PropertyWindow.css" type="text/css" />
		<link rel="stylesheet" href="ui/app/viewers/cadviewer/css/ViewerSettings.css" type="text/css" />
		<link rel="stylesheet" href="ui/app/viewers/cadviewer/css/jquery.minicolors.css" type="text/css">
		
		<link rel="stylesheet" href="ui/app/viewers/cadviewer-modified/css/Toolbar.css" type="text/css" />
		<link rel="stylesheet" href="ui/app/viewers/cadviewer-modified/css/PropertyWindow.css" type="text/css" />
		<link rel="stylesheet" href="ui/app/viewers/cadviewer-modified/css/ModelBrowser.css" type="text/css" />
		<link rel="stylesheet" href="ui/app/viewers/cadviewer-modified/css/TreeControl.css" type="text/css" />
		<link rel="stylesheet" href="ui/app/viewers/cadviewer-modified/css/Common.css" type="text/css" />
		<link rel="stylesheet" href="ui/app/viewers/cadviewer-modified/css/Desktop.css" type="text/css" />
		
		<link rel="stylesheet" href="ui/css/style.css" />
		<link rel="stylesheet" href="ui/css/viewer.css" />
		<link rel="stylesheet" href="ui/css/font/fira.css" />
        <link href="ui/lib/font-awesome/4.4.0/css/font-awesome.min.css" rel="stylesheet">
    </head>
    <body style="overflow: hidden;">
	    <div id="content">

        <div id="error" class="alert alert-danger alert-dismissable" style="display:none; top:55%;" >
             <button type="button" class="close" onclick="closeDialog()" aria-hidden="true">x</button><span id="errmsg"></span>
        </div>
       
		<div class="pageWrapper" id="pageWrapper">
			<div class="cc-header">
				<div class="cc-header-logo"></div>
				<button id="rms-help-button" title="Help" onclick="showHelp('help/CAD_Viewer.htm')" class="toolbar-button btn btn-default desktop-only spaced "> </button>
			</div>
			
			<div class = "toolbarContainer fade-div">
			
				<div id="titleContainer" class="titleContainer">
					<h5 class="titleText"><b><span id="titleDesktop" data-toggle="tooltip" data-placement="top" class="hide-viewer-title-desktop"></span></b><b><span id="titleMobile" data-toggle="tooltip" data-placement="top" class="hide-viewer-title-mobile show-viewer-title-mobile"></span></b></h5>
				</div>
				
				<div id="toolBar">
					<div class="toolbar-tools" style="display:flex;">
						<div class="tool-seperator "></div>
						<div id="home-button" title="Reset View" data-operatorclass="toolbar-home" class="hoops-tool">
							<div class="tool-icon btn btn-default" style="padding: 0px;"></div>
						</div>
						<div class="tool-seperator "></div>
						<div id="view-button" title="Camera" data-operatorclass="toolbar-camera" data-submenu="view-submenu" class="hoops-tool toolbar-menu"> 
							<div class="tool-icon btn btn-default" style="padding: 0px;"></div>
						</div>
						<div id="edgeface-button" title="Wireframe on Shaded" data-operatorclass="toolbar-wireframeshaded" data-submenu="edgeface-submenu" class="hoops-tool toolbar-menu dont-show-toolbar-cad">
							<div class="tool-icon btn btn-default" style="padding: 0px;"></div>
						</div>
						<div class="tool-seperator "></div>
						<div id ="click-button" title="Select Part" data-operatorclass="toolbar-select" data-submenu="click-submenu" class="hoops-tool toolbar-menu dont-show-toolbar-cad">
							<div class="tool-icon btn btn-default" style="padding: 0px;"></div>
						</div>
						<div id="camera-button" title="Orbit Camera" data-operatorclass="toolbar-orbit" data-submenu="camera-submenu" class="hoops-tool toolbar-menu">
							<div class="tool-icon btn btn-default" style="padding: 0px;"></div>
						</div>
						<div class="tool-seperator "></div>
						<div id="explode-button" title="Explode" data-operatorclass="toolbar-explode" data-submenu="explode-slider" class="hoops-tool toolbar-menu-toggle dont-show-toolbar-cad">
							<div class="tool-icon btn btn-default" style="padding: 0px;"></div>
						</div>        
						<div id="cuttingplane-button" title="Cutting Plane" data-operatorclass="toolbar-cuttingplane" data-submenu="cuttingplane-submenu" class="hoops-tool toolbar-menu-toggle dont-show-toolbar-cad">
							<div class="tool-icon btn btn-default" style="padding: 0px;"></div>
						</div>        
						<div class="tool-seperator dont-show-toolbar-cad"></div>	
						<div id="settings-button" title="Settings" data-operatorclass="toolbar-settings" class="hoops-tool dont-show-toolbar-cad">
							<div class="tool-icon btn btn-default" style="padding: 0px;"></div>
						</div>

                        <div class="tool-seperator dont-show-toolbar-cad"></div>

                        <button id="rms-print-button" title="Print" data-operatorclass="toolbar-snapshot" class="hoops-tool tool-icon btn btn-default spaced print-enabled dont-show-toolbar-cad"> </button>		

						<button id="rms-download-button" type="button" onclick="downloadFile()" class="toolbar-button download btn btn-default spaced" title="Download File"></button>

                        <button id="rms-info-button" type="button" onclick="showInfo()" class="toolbar-button info btn btn-default spaced" title="View File Info"> </button>
	
					</div>
					<div id="submenus">
						<div id="camera-submenu" data-button="camera-button" class = "toolbar-submenu submenus">
							<table>
								<tr><td><div id="operator-camera-walk" title="Walk" data-operatorclass="toolbar-walk" class="submenu-icon btn btn-default" style="padding:0px;"></div></td></tr>
								<tr><td><div id="operator-camera-turntable" title="Turntable" data-operatorclass="toolbar-turntable" class="submenu-icon btn btn-default" style="padding:0px;"></div></td></tr>
								<tr><td><div id="operator-camera-orbit" title="Orbit Camera" data-operatorclass="toolbar-orbit" class="submenu-icon btn btn-default" style="padding:0px;"></div></td></tr>
							</table>
						</div>

						<div id="edgeface-submenu" data-button="edgeface-button" class = "toolbar-submenu submenus">
							<table>
								<tr><td><div id="viewport-wireframe-on-shaded" title="Shaded With Lines" data-operatorclass="toolbar-wireframeshaded" class="submenu-icon btn btn-default" style="padding:0px;"></div></td></tr>
								<tr><td><div id="viewport-shaded" title="Shaded" data-operatorclass="toolbar-shaded" class="submenu-icon btn btn-default" style="padding:0px;"></div></td></tr>
								<tr><td><div id="viewport-hidden-line"  title="Hidden Line" data-operatorclass="toolbar-hidden-line" class="submenu-icon btn btn-default" style="padding:0px;"></div></td></tr>
								<tr><td><div id="viewport-wireframe" title="Wireframe" data-operatorclass="toolbar-wireframe" class="submenu-icon btn btn-default" style="padding:0px;"></div></td></tr>
							</table>
						</div>
						<div id="view-submenu" class ="toolbar-submenu submenus">
							<table>
								<tr><td><div id="view-face" title="Orient Camera To Selected Face" data-operatorclass="toolbar-face" class="submenu-icon btn btn-default" style="padding:0px;"></div></td></tr>
								<tr>
									<td><div id="view-iso" title="Iso View" data-operatorclass="toolbar-iso" class="submenu-icon btn btn-default" style="padding:0px;"></div></td>
									<td><div id="view-ortho" title="Orthographic Projection" data-operatorclass="toolbar-ortho" class="submenu-icon btn btn-default" style="padding:0px;"></div></td>
									<td><div id="view-persp" title="Perspective Projection" data-operatorclass="toolbar-persp" class="submenu-icon btn btn-default" style="padding:0px;"></div></td>
								</tr>
								<tr>
									<td><div id="view-left" title="Left View" data-operatorclass="toolbar-left" class="submenu-icon btn btn-default" style="padding:0px;"></div></td>
									<td><div id="view-right" title="Right View" data-operatorclass="toolbar-right" class="submenu-icon btn btn-default" style="padding:0px;"></div></td>
									<td><div id="view-bottom" title="Bottom View" data-operatorclass="toolbar-bottom" class="submenu-icon btn btn-default" style="padding:0px;"></div></td>
								</tr>
								<tr>
									<td><div id="view-front" title="Front View" data-operatorclass="toolbar-front" class="submenu-icon btn btn-default" style="padding:0px;"></div></td>
									<td><div id="view-back" title="Back View" data-operatorclass="toolbar-back" class="submenu-icon btn btn-default" style="padding:0px;"></div></td>
									<td><div id="view-top" title="Top View" data-operatorclass="toolbar-top" class="submenu-icon btn btn-default" style="padding:0px;"></div></td>
								</tr>
							</table>
						</div>
						<div id="click-submenu" data-button="click-button"  class ="toolbar-submenu submenus">
							<table>
								<tr>
									<td><div id="measure-angle-between-faces" title="Measure Angle Between Faces" data-operatorclass="toolbar-measure-angle" class="submenu-icon btn btn-default" style="padding:0px;"></div></td>
									<td><div id="measure-distance-between-faces" title="Measure Distance Between Faces" data-operatorclass="toolbar-measure-distance" class="submenu-icon btn btn-default" style="padding:0px;"></div></td>
								</tr>
								<tr>
									<td><div id="measure-edges" title="Measure Edges" data-operatorclass="toolbar-measure-edge" class="submenu-icon btn btn-default" style="padding:0px;"></div></td>
									<td><div id="measure-point-to-point" title="Measure Point to Point" data-operatorclass="toolbar-measure-point" class="submenu-icon btn btn-default" style="padding:0px;"></div></td>
								</tr>
								<tr>
									<td><div title="Select Parts" data-operatorclass="toolbar-select" class="submenu-icon btn btn-default" style="padding:0px;"></div></td>
								</tr>
							</table>
						</div>

						<div id="explode-slider" class = "toolbar-submenu slider-menu submenus">
							<div id="explosion-slider" style="height:90px;" class="toolbar-slider"></div>
						</div>

						<div id="cuttingplane-submenu" class="toolbar-submenu cutting-plane submenus no-modal">
							<table>
								<tr><td><div id="cuttingplane-face" title="Create Cutting Plane On Selected Face" data-plane="face" data-operatorclass="toolbar-cuttingplane-face" class="toolbar-cp-plane submenu-icon"></div></td></tr>
								<tr>
									<td><div id="cuttingplane-x" title="Cutting Plane X" data-plane="x" data-operatorclass="toolbar-cuttingplane-x" class="toolbar-cp-plane submenu-icon"></div></td>
									<td><div id="cuttingplane-section" title="Cutting Plane Visibility Toggle" data-plane="section" data-operatorclass="toolbar-cuttingplane-section" class="toolbar-cp-plane submenu-icon disabled"></div></td>
								</tr>
								<tr>
									<td><div id="cuttingplane-y" title="Cutting Plane Y" data-plane="y" data-operatorclass="toolbar-cuttingplane-y" class="toolbar-cp-plane submenu-icon"></div></td>
									<td><div id="cuttingplane-toggle" title="Cutting Plane Section Toggle" data-plane="toggle" data-operatorclass="toolbar-cuttingplane-toggle" class="toolbar-cp-plane submenu-icon disabled"></div></td>
								</tr>
								<tr>
									<td><div id="cuttingplane-z" title="Cutting Plane Z" data-plane="z" data-operatorclass="toolbar-cuttingplane-z" class="toolbar-cp-plane submenu-icon"></div></td>
									<td><div id="cuttingplane-reset" title="Cutting Plane Reset" data-plane="reset" data-operatorclass="toolbar-cuttingplane-reset" class="toolbar-cp-plane submenu-icon disabled"></div></td>
								</tr>
							</table>
						</div>
					</div>
				</div>
            </div>
            <div id ="snapshot-dialog" class="hoops-ui-window">
                <div class="hoops-ui-window-body"><img id="snapshot-dialog-image" class="snapshot-image" alt="Snapshot"/></div>
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
				<div id="viewerContainer"> 				
					<div id="Wrapper" class="Wrapper">
					</div>					
				</div>
			</div>
        </div>
		
		<!-- Settings -->
		 <div id="viewer-settings-dialog" class="hoops-ui-window">
            <div class="hoops-ui-window-header">
                Viewer Settings
            </div>
            <div class="hoops-ui-window-body">
                <!--general-->
                <div class="settings-group-header">General</div>
                <div class="settings-group settings-group-general">
                    <div class="settings-block">
                        <label>Projection Mode:</label>
                        <select id="settings-projection-mode" class="right-align">
                            <option value="0">Orthographic</option>
                            <option value="1">Perspective</option>
                        </select>
                        <span class="clear-both"></span>
                    </div>
                    <div class="settings-block">
                        <span class="framerate-text">Framerate (fps):</span>
                        <input type="number" min="0" id="settings-framerate" class="right-align" />
                        <span class="clear-both"></span>
                    </div>
                    <div class="settings-block">
                        <span>Hidden Line Opacity (0-1): </span>
                        <input id="settings-hidden-line-opacity" class="right-align" type="number" min="0" max="1" step=".1" />
                        <span class="clear-both"></span>
                    </div>
                    <div class="settings-block">
                        <span>Enable Ambient Occlusion:</span>
                        <input type="checkbox" id="settings-ambient-occlusion" />
                        <input id="settings-ambient-occlusion-radius" type="number" min="0" max="10" step=".01" class="right-align" />
                        <div class="right-align">Radius: </div>
                        <span class="clear-both"></span>
                    </div>
                    <div class="settings-block">
                        <span>Enable Anti-Aliasing:</span>
                        <input type="checkbox" id="settings-anti-aliasing" class="right-align" />
                        <span class="clear-both"></span>
                    </div>
                    <div class="settings-block">
                        <label for="settings-show-backfaces">Show Backfaces:</label>
                        <input type="checkbox" id="settings-show-backfaces" class="right-align" />
                        <span class="clear-both"></span>
                    </div>
                    <div class="settings-block">
                        <label for="settings-show-capping-geometry">Show Capping Geometry:</label>
                        <input type="checkbox" id="settings-show-capping-geometry" class="right-align" />
                        <span class="clear-both"></span>
                    </div>
                    <div class="settings-block">
                        <span>Enable Face / Line Selection: </span>
                        <input type="checkbox" id="settings-enable-face-line-selection" class="right-align" />
                        <span class="clear-both"></span>
                    </div>
                    <div class="settings-block">
                        <span>Rotate around camera center:</span>
                        <input type="checkbox" id="settings-orbit-mode" class="right-align" />
                        <span class="clear-both"></span>
                    </div>
                </div>
                <!--color-->
                <div class="settings-group-header">Color</div>
                <div class="settings-group settings-group-colors">
                    <div class="settings-block">
                        <div>Background Color:</div>
                        <div>
                            <input type="text" id="settings-background-top" class='color-picker' data-position="top left" />
                            <label>Top</label>
                        </div>
                        <div>
                            <input type="text" id="settings-background-bottom" class='color-picker' data-position="top left" />
                            <label>Bottom</label>
                        </div>
                    </div>
                    <div class="settings-block">
                        <div>Capping Geometry:</div>
                        <div>
                            <input type="text" id="settings-capping-face-color" class='color-picker' data-position="top left" />
                            <label>Face</label>
                        </div>
                        <div>
                            <input type="text" id="settings-capping-line-color" class='color-picker' data-position="top left" />
                            <label>Line</label>
                        </div>
                    </div>
                    <div class="settings-block">
                        <div>Selection Color:</div>
                        <input type="text" id="settings-selection-color-body" class='color-picker' data-position="top left" />
                        <label>Body</label>
                        <div>
                            <span id="settings-selection-color-face-line-style">
                                <input type="text" id="settings-selection-color-face-line" class='color-picker' data-position="top left" />
                                <label>Faces and Lines</label>
                            </span>
                        </div>
                    </div>
                    <div class="settings-block">
                        <input type="text" id="settings-measurement-color" class='color-picker' data-position="top left" />
                        <label>Measurement Color</label>
                    </div>
                    <div class="settings-block">
                        <span id="settings-pmi-color-style" class="grayed-out">
                            <input type="text" id="settings-pmi-color" class='color-picker' data-position="top left" disabled />
                            <label>PMI Override Color</label>
                        </span>
                        <input type="checkbox" id="settings-pmi-enabled" />
                        <label>Enable</label>
                    </div>
                </div>
            </div>
            <div>
                <div class="hoops-ui-window-footer">
                    <div class="version">
                        Viewer Version: <span id="settings-viewer-version"></span>, Format Version: <span id="settings-format-version"></span>
                    </div>
                    <div class="hoops-ui-footer-buttons">
                        <button id="viewer-settings-ok-button">OK</button>
                        <button id="viewer-settings-cancel-button">Cancel</button>
                        <button id="viewer-settings-apply-button">Apply</button>
                    </div>
                </div>
            </div>
        </div>
		<!-- End of Settings -->
		
		</div>
		<script src="ui/lib/3rdParty/jquery-1.10.2.min.js"></script>
		<script src="ui/lib/jquery-ui/1.11.4/jquery-ui.min.js"></script>
		<script type="text/javascript" src="ui/app/viewers/cadviewer/js/hoops_web_viewer.js"></script>
		<script type="text/javascript" src="ui/app/viewers/cadviewer/js/jquery.minicolors.min.js"></script>
		<script type="text/javascript" src="ui/app/viewers/cadviewer/js/iscroll.min.js"></script>
		<script type="text/javascript" src="ui/app/viewers/cadviewer/js/web_viewer_ui.js"></script>
		<script type="text/javascript" src="ui/app/viewers/cadviewer-modified/js/web_viewer_ui.js"></script>
		<script type="text/javascript" src="ui/app/viewers/cadviewer-modified/js/hoops_web_viewer.js"></script>
		<script src="ui/lib/3rdParty/bootstrap.min.js"></script>
		<script src="ui/lib/3rdParty/jquery.blockUI.js"></script>
        <script src="ui/lib/3rdParty/shortcut.js"></script>
        <script src="ui/lib/3rdParty/fontChecker.js"></script>
		<script src="ui/lib/3rdParty/dateformat.js"></script>
		<script src="ui/lib/rms/protect.js"></script>
		<script src="ui/lib/rms/rmsUtil.js"></script>
		<script src="ui/app/viewers/Viewer.js"></script>
		<script src="ui/app/viewers/CADViewer.js"></script>
    </body>
</html>
