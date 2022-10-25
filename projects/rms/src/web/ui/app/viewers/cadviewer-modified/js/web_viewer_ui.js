var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
$(window).click(function(e) {
	if (e.target.id != "contextMenuButton") {
		$(".ui-contextmenu").hide();
        return false;
	}
});
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        var ViewTree = (function () {
            function ViewTree(elementId, viewer, treeScroll) {
                if (treeScroll === void 0) { treeScroll = null; }
                this._maxNodeChildrenSize = 300;
                this._tree = new Ui.Control.TreeControl(elementId, viewer, ViewTree.separator, treeScroll);
                this._viewer = viewer;
            }
            ViewTree.prototype.getElementId = function () {
                return this._tree.getElementId();
            };
            ViewTree.prototype.registerCallback = function (name, callback) {
                this._tree.registerCallback(name, callback);
            };
            ViewTree.prototype._splitIdStr = function (idstr) {
                return this._splitParts(idstr, ViewTree.separator);
            };
            ViewTree.prototype._splitParts = function (idstr, separator) {
                var splitPos = idstr.indexOf(separator);
                return [
                    idstr.substring(0, splitPos),
                    idstr.substring(splitPos + 1)
                ];
            };
            ViewTree.prototype.hideTab = function () {
                $("#" + this.getElementId() + "Tab").hide();
            };
            ViewTree.prototype.showTab = function () {
                $("#" + this.getElementId() + "Tab").show();
            };
            ViewTree.separator = '_';
            return ViewTree;
        }());
        Ui.ViewTree = ViewTree;
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        var CADViewTree = (function (_super) {
            __extends(CADViewTree, _super);
            function CADViewTree(elementId, viewer, cuttingPlaneController) {
                _super.call(this, elementId, viewer);
                this._internalID = 'cadviewtree';
                this._annotationViewsString = 'annotationViews';
                this._cuttingPlaneController = cuttingPlaneController;
                this._initEvents();
            }
            CADViewTree.prototype._initEvents = function () {
                var _this = this;
                this._viewer.setCallbacks({
                    modelStructureReady: function () {
                        _this._onModelStructureReady();
                    }
                });
                this._tree.registerCallback("selectItem", function (id, selectionMode) {
                    _this._onTreeSelectItem(id, selectionMode);
                });
            };
            CADViewTree.prototype._onModelStructureReady = function () {
                this._createCADViewNodes();
                var cadViews = this._viewer.getModel().getCADViews();
                if (Object.keys(cadViews).length <= 0) {
                    this.hideTab();
                }
                else {
                    this.showTab();
                }
                this._tree.expandInitialNodes(this._internalID);
                this._tree.expandInitialNodes(this._internalID + this._annotationViewsString);
            };
            CADViewTree.prototype._createCADViewNodes = function () {
                var _this = this;
                var cadviews = this._viewer.getModel().getCADViews();
                var self = this;
                if (Object.keys(cadviews).length > 0) {
                    this._tree.appendTopLevelElement("Views", this._internalID, "viewfolder", true);
                    $.each(cadviews, function (key, value) {
                        if (!self._viewer.getModel().isAnnotationView(parseInt(key))) {
                            _this._tree.addChild(value, _this._cadviewId(key), _this._internalID, "view", false);
                        }
                    });
                    $.each(cadviews, function (key, value) {
                        if (self._viewer.getModel().isAnnotationView(parseInt(key))) {
                            if (document.getElementById(_this._internalID + _this._annotationViewsString) == null) {
                                _this._tree.addChild("Annotation Views", _this._internalID + _this._annotationViewsString, _this._internalID, "viewfolder", true);
                            }
                        }
                    });
                    $.each(cadviews, function (key, value) {
                        if (self._viewer.getModel().isAnnotationView(parseInt(key))) {
                            var parsedValue = value.split("# Annotation View")[0];
                            _this._tree.addChild(parsedValue, _this._cadviewId(key), _this._internalID + _this._annotationViewsString, "view", false);
                        }
                    });
                }
            };
            CADViewTree.prototype._onTreeSelectItem = function (idstr, selectionMode) {
                if (selectionMode === void 0) { selectionMode = Communicator.SelectionMode.Set; }
                var idParts = this._splitIdStr(idstr);
                switch (idParts[0]) {
                    case this._internalID:
                        var handleOperator = this._viewer.getOperatorManager().getOperator(Communicator.OperatorId.Handle);
                        handleOperator.removeHandles();
                        this._viewer.getModel().activateCADView(parseInt(idParts[1]));
                        if (this._viewer.getCuttingManager().getCuttingSection(3).getCount()) {
                            this._cuttingPlaneController.cadViewActivated();
                        }
                        break;
                }
                ;
                var thisElement = document.getElementById(idstr);
                if (thisElement.tagName == "LI" && idstr != this._internalID && idstr != this._internalID + this._annotationViewsString) {
                    thisElement.classList.add("selected");
                }
                else {
                    thisElement.classList.remove("selected");
                }
            };
            CADViewTree.prototype._cadviewId = function (id) {
                return this._internalID + Ui.ViewTree.separator + id;
            };
            return CADViewTree;
        }(Ui.ViewTree));
        Ui.CADViewTree = CADViewTree;
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        var Context;
        (function (Context) {
            var ContextMenuItem = (function () {
                function ContextMenuItem(action, element) {
                    this.action = action;
                    this.element = element;
                }
                ContextMenuItem.prototype.setEnabled = function (enabled) {
                    if (enabled)
                        $(this.element).removeClass("disabled");
                    else
                        $(this.element).addClass("disabled");
                };
                ContextMenuItem.prototype.setText = function (text) {
                    this.element.innerHTML = text;
                };
                ContextMenuItem.prototype.show = function () {
                    $(this.element).show();
                };
                ContextMenuItem.prototype.hide = function () {
                    $(this.element).hide();
                };
                return ContextMenuItem;
            }());
            Context.ContextMenuItem = ContextMenuItem;
            var ContextMenu = (function () {
                function ContextMenu(menuClass, containerId, viewer, isolateZoomHelper) {
                    var _this = this;
                    this._contextItems = {};
                    this._activeItemId = null;
                    this._separatorCount = 0;
                    this._position = null;
                    this._viewer = viewer;
                    this._containerId = containerId;
                    this._isolateZoomHelper = isolateZoomHelper;
                    this._transparencyIdHash = new Array();
                    this._initElements(menuClass);
                    this._viewer.setCallbacks({
                        modelStructureReady: function () {
                            _this._onModelStructureReady();
                        }
                    });
                }
                ContextMenu.prototype._onModelStructureReady = function () {
                    if (this._viewer.getModel().isDrawing()) {
                        if (this._contextItems.hasOwnProperty("reset"))
                            this._contextItems["reset"].hide();
                        if (this._contextItems.hasOwnProperty("meshlevel0"))
                            this._contextItems["meshlevel0"].hide();
                        if (this._contextItems.hasOwnProperty("meshlevel0"))
                            this._contextItems["meshlevel1"].hide();
                        if (this._contextItems.hasOwnProperty("meshlevel0"))
                            this._contextItems["meshlevel2"].hide();
                        $(".contextmenu-separator-3").hide();
                    }
                };
                ContextMenu.prototype._isMenuItemEnabled = function () {
                    if (this._activeItemId != null && !Communicator.Markup.NoteText.checkForContextSelection(this._activeItemId)) {
                        return true;
                    }
                    var axisOverlay = 1;
                    var selectionItems = this._viewer.getSelectionManager().getResults();
                    var n = selectionItems.length;
                    for (var i = 0; i < n; ++i) {
                        var item = selectionItems[i];
                        if (item.overlayIndex() != axisOverlay) {
                            return true;
                        }
                    }
                    return false;
                };
                ContextMenu.prototype.setActiveItemId = function (activeItemId) {
                    this._activeItemId = activeItemId;
                    if (this._isMenuItemEnabled()) {
                        this._contextItems["visibility"].setText(this._isVisible(activeItemId) ? "Hide" : "Show");
                        this._contextItems["isolate"].setEnabled(true);
                        this._contextItems["zoom"].setEnabled(true);
                        this._contextItems["visibility"].setEnabled(true);
                        this._contextItems["transparent"].setEnabled(true);
                        var handleOperator = this._viewer.getOperatorManager().getOperator(Communicator.OperatorId.Handle);
                        if (handleOperator && handleOperator.isEnabled) {
                            this._contextItems["handles"].setEnabled((this.getContextItemIds(true, true, false).length > 0) && handleOperator.isEnabled());
                        }
                        else {
                            this._contextItems["handles"].setEnabled(false);
                        }
                        if (this._contextItems.hasOwnProperty("meshlevel0")) {
                            this._contextItems["meshlevel0"].setEnabled(true);
                            this._contextItems["meshlevel1"].setEnabled(true);
                            this._contextItems["meshlevel2"].setEnabled(true);
                        }
                    }
                    else {
                        this._contextItems["isolate"].setEnabled(false);
                        this._contextItems["zoom"].setEnabled(false);
                        this._contextItems["visibility"].setEnabled(false);
                        this._contextItems["transparent"].setEnabled(false);
                        this._contextItems["handles"].setEnabled(false);
                        if (this._contextItems.hasOwnProperty("meshlevel0")) {
                            this._contextItems["meshlevel0"].setEnabled(false);
                            this._contextItems["meshlevel1"].setEnabled(false);
                            this._contextItems["meshlevel2"].setEnabled(false);
                        }
                    }
                };
                ContextMenu.prototype.showElements = function (position) {
                    this._viewer.setContextMenuStatus(true);
                    var canvasSize = this._viewer.getView().getCanvasSize();
                    var menuElement = $(this._menuElement);
                    if (menuElement.height() > canvasSize.y) {
                        menuElement.addClass("small");
                    }
                    var menuWidth = menuElement.outerWidth();
                    var menuHeight = menuElement.outerHeight();
                    var positionY = position.y;
                    var positionX = position.x;
                    if ((positionY + menuHeight) > canvasSize.y) {
                        positionY = canvasSize.y - menuHeight - 1;
                    }
                    if ((positionX + menuWidth) > canvasSize.x) {
                        positionX = canvasSize.x - menuWidth - 1;
                    }
                    $(this._menuElement).css({
                        top: positionY + "px",
                        left: positionX + "px",
                        display: "block"
                    });
                    $(this._menuElement).show();
                    $(this._contextLayer).show();
                };
                ContextMenu.prototype._onContextLayerClick = function (event) {
                    if (event.button == 0)
                        this.hide();
                };
                ContextMenu.prototype.hide = function () {
                    this._viewer.setContextMenuStatus(false);
                    $(this._menuElement).hide();
                    $(this._contextLayer).hide();
                    $(this._menuElement).removeClass("small");
                };
                ContextMenu.prototype._doMenuClick = function (event) {
                    if ($(event.target).hasClass("disabled"))
                        return;
                    var itemId = $(event.target).attr("id");
                    var contextMenuItem = this._contextItems[itemId];
                    if (contextMenuItem != null) {
                        contextMenuItem.action(this._activeItemId);
                    }
                    this.hide();
                };
                ContextMenu.prototype._getSelectedPartIds = function () {
                    var selecteIds = [];
                    this._viewer.getSelectionManager().each(function (selectionItem) {
                        selecteIds.push(selectionItem.getNodeId());
                    });
                    return selecteIds;
                };
                ContextMenu.prototype._initElements = function (menuClass) {
                    var _this = this;
                    this._menuElement = document.createElement("div");
                    this._menuElement.oncontextmenu = function () { return false; };
                    this._menuElement.classList.add("ui-contextmenu");
                    if (menuClass != null)
                        this._menuElement.classList.add(menuClass);
                    this._menuElement.style.position = "absolute";
                    this._menuElement.style.zIndex = "10001";
                    this._menuElement.style.display = "none";
                    this._menuElement.ontouchmove = function (event) { event.preventDefault(); };
                    this._contextLayer = document.createElement("div");
                    this._contextLayer.style.position = "relative";
                    this._contextLayer.style.width = "100%";
                    this._contextLayer.style.height = "100%";
                    this._contextLayer.style.backgroundColor = "transparent";
                    this._contextLayer.style.zIndex = "10000";
                    this._contextLayer.style.display = "none";
                    this._contextLayer.oncontextmenu = function () { return false; };
                    this._contextLayer.ontouchmove = function (event) { event.preventDefault(); };
                    this._createDefaultMenuItems();
                    var container = document.getElementById(this._containerId);
                    container.appendChild(this._menuElement);
                    container.appendChild(this._contextLayer);
                    $(this._menuElement).on("click", ".ui-contextmenu-item", function (event) {
                        _this._doMenuClick(event);
                    });
                    $(this._contextLayer).bind("mousedown", function (event) {
                        _this._onContextLayerClick(event);
                    });
                };
                ContextMenu.prototype._isMenuItemExecutable = function () {
                    return this._activeItemId != null || this._viewer.getSelectionManager().getResults().length > 0;
                };
                ContextMenu.prototype._createDefaultMenuItems = function () {
                    var _this = this;
                    this.appendItem("isolate", "Isolate", function () {
                        if (_this._isMenuItemExecutable()) {
                            _this._isolateZoomHelper.isolateNodes(_this.getContextItemIds(true, true));
                        }
                    });
                    this.appendItem("zoom", "Zoom", function () {
                        if (_this._isMenuItemExecutable()) {
                            _this._isolateZoomHelper.fitNodes(_this.getContextItemIds(true, true));
                        }
                    });
                    this.appendItem("visibility", "Hide", function () {
                        if (_this._isMenuItemExecutable()) {
                            _this._viewer.getModel().setNodesVisibility(_this.getContextItemIds(true, true), !_this._isVisible(_this._activeItemId));
                        }
                    });
                    this.appendseparator();
                    this.appendItem("transparent", "Transparent", function () {
                        if (_this._isMenuItemExecutable()) {
                            var model = _this._viewer.getModel();
                            var contextItemIds = _this.getContextItemIds(true, true);
                            if (_this._transparencyIdHash[contextItemIds[0]] == undefined) {
                                for (var i = 0; i < contextItemIds.length; i++) {
                                    _this._transparencyIdHash[contextItemIds[i]] = 1;
                                }
                                model.setNodesTransparency(contextItemIds, 0.5);
                            }
                            else {
                                for (var i = 0; i < contextItemIds.length; i++) {
                                    _this._transparencyIdHash[contextItemIds[i]] = undefined;
                                }
                                model.resetNodesTransparency(contextItemIds);
                            }
                        }
                    });
                    this.appendseparator();
                    this.appendItem("handles", "Show Handles", function () {
                        if (_this._isMenuItemExecutable()) {
                            var handleOperator = _this._viewer.getOperatorManager().getOperator(Communicator.OperatorId.Handle);
                            var contextItemIds = _this.getContextItemIds(true, true, false);
                            if (contextItemIds.length > 0) {
                                handleOperator.addHandles(contextItemIds, _this._position);
                            }
                        }
                    });
                    this.appendItem("reset", "Reset Model", function () {
                        _this._viewer.getModel().reset();
                        var handleOperator = _this._viewer.getOperatorManager().getOperator(Communicator.OperatorId.Handle);
                        handleOperator.removeHandles();
                    });
                    var viewerParams = this._viewer.getCreationParameters();
                    if (viewerParams.hasOwnProperty("model")) {
                        this.appendseparator();
                        this.appendItem("meshlevel0", "Mesh Level 0", function () {
                            if (_this._isMenuItemExecutable()) {
                                _this._viewer.getModel().setMeshLevel(_this.getContextItemIds(true, true), 0);
                            }
                        });
                        this.appendItem("meshlevel1", "Mesh Level 1", function () {
                            if (_this._isMenuItemExecutable()) {
                                _this._viewer.getModel().setMeshLevel(_this.getContextItemIds(true, true), 1);
                            }
                        });
                        this.appendItem("meshlevel2", "Mesh Level 2", function () {
                            if (_this._isMenuItemExecutable()) {
                                _this._viewer.getModel().setMeshLevel(_this.getContextItemIds(true, true), 2);
                            }
                        });
                    }
                    this.appendseparator();
                    this.appendItem("showall", "Show all", function () {
                        _this._isolateZoomHelper.showAll();
                    });
                };
                ContextMenu.prototype.getContextItemIds = function (includeSelected, includeClicked, includeRoot) {
                    if (includeRoot === void 0) { includeRoot = true; }
                    var rootId = this._viewer.getModel().getRootNode();
                    var itemIds = [];
                    if (includeSelected) {
                        var selectedItems = this._viewer.getSelectionManager().getResults();
                        for (var i = 0; i < selectedItems.length; i++) {
                            var id = selectedItems[i].getNodeId();
                            if (id != null && (includeRoot || (!includeRoot && id != rootId))) {
                                itemIds.push(id);
                            }
                        }
                    }
                    var containsParent = this._viewer.getSelectionManager().containsParent(new Communicator.Selection.SelectionItem(this._activeItemId)) != null;
                    var containsItem = itemIds.indexOf(this._activeItemId) != -1;
                    if (includeClicked && this._activeItemId != null && (includeRoot || (!includeRoot && this._activeItemId != rootId) && (itemIds.length == 0 || (!containsItem && !containsParent)))) {
                        itemIds.push(this._activeItemId);
                    }
                    return itemIds;
                };
                ContextMenu.prototype.appendItem = function (itemId, label, action) {
                    var item = document.createElement("div");
                    item.classList.add("ui-contextmenu-item");
                    item.innerHTML = label;
                    item.id = itemId;
                    this._menuElement.appendChild(item);
                    var contextMenuItem = new ContextMenuItem(action, item);
                    this._contextItems[itemId] = contextMenuItem;
                    return contextMenuItem;
                };
                ContextMenu.prototype.appendseparator = function () {
                    var item = document.createElement("div");
                    item.classList.add('contextmenu-separator-' + this._separatorCount++);
                    item.classList.add("ui-contextmenu-separator");
                    item.style.width = "100%";
                    item.style.height = "1px";
                    this._menuElement.appendChild(item);
                };
                ContextMenu.prototype._appendMenuItem = function (name, menu) {
                    var item = document.createElement("div");
                    item.classList.add("ui-contextmenu-item");
                    item.innerHTML = name;
                    menu.appendChild(item);
                    return item;
                };
                ContextMenu.prototype._appendseparator = function (menu) {
                    var item = document.createElement("div");
                    item.classList.add("ui-contextmenu-separator");
                    item.style.width = "100%";
                    item.style.height = "1px";
                    menu.appendChild(item);
                };
                ContextMenu.prototype._isVisible = function (activeItemId) {
                    var selectionItems = this._viewer.getSelectionManager().getResults();
                    if (this._activeItemId != null) {
                        return this._viewer.getModel().getNodeVisibility(this._activeItemId);
                    }
                    else {
                        return this._viewer.getModel().getNodeVisibility(selectionItems[0].getNodeId());
                    }
                };
                return ContextMenu;
            }());
            Context.ContextMenu = ContextMenu;
        })(Context = Ui.Context || (Ui.Context = {}));
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        (function (Status) {
            Status[Status["Hidden"] = 0] = "Hidden";
            Status[Status["Visible"] = 1] = "Visible";
            Status[Status["Inverted"] = 2] = "Inverted";
        })(Ui.Status || (Ui.Status = {}));
        var Status = Ui.Status;
        (function (AxisIndex) {
            AxisIndex[AxisIndex["X"] = 0] = "X";
            AxisIndex[AxisIndex["Y"] = 1] = "Y";
            AxisIndex[AxisIndex["Z"] = 2] = "Z";
            AxisIndex[AxisIndex["FACE"] = 3] = "FACE";
        })(Ui.AxisIndex || (Ui.AxisIndex = {}));
        var AxisIndex = Ui.AxisIndex;
        var CuttingPlaneController = (function () {
            function CuttingPlaneController(viewer) {
                var _this = this;
                this._cuttingSections = [];
                this._cuttingPlanes = [];
                this._referenceGeometry = [];
                this._status = [];
                this._cadViewActivated = false;
                this._showReferenceGeometry = true;
                this._useIndividualCuttingSections = true;
                this._boundingBoxUpdate = false;
                this._updateReferenceGeometry = [];
                this._faceSelection = null;
                this._pendingFuncs = {
                    visibility: null,
                    inverted: null
                };
                this._viewer = viewer;
                this.resetCuttingPlanes();
                this._viewer.setCallbacks({
                    modelStructureReady: function () {
                        _this._initSection();
                    },
                    partsVisibilityHidden: function () {
                        if (!_this._boundingBoxUpdate) {
                            _this._updateBoundingBox();
                        }
                    },
                    partsVisibilityShown: function () {
                        if (!_this._boundingBoxUpdate) {
                            _this._updateBoundingBox();
                        }
                    },
                    hwfParseComplete: function () {
                        if (!_this._boundingBoxUpdate) {
                            _this._updateBoundingBox();
                        }
                    },
                    modelSwitched: function () {
                        if (!_this._boundingBoxUpdate) {
                            _this._updateBoundingBox();
                        }
                        _this.resetCuttingPlanes();
                    }
                });
            }
            CuttingPlaneController.prototype._setStatus = function (axis, status) {
                this._status[axis] = status;
            };
            CuttingPlaneController.prototype._updateBoundingBox = function () {
                var _this = this;
                this._boundingBoxUpdate = true;
                return this._viewer.getModel().getModelBounding(true, false).then(function (modelBounding) {
                    var minDiff = _this._modelBounding.min.equalsWithTolerance(modelBounding.min, .01);
                    var maxDiff = _this._modelBounding.max.equalsWithTolerance(modelBounding.max, .01);
                    var p;
                    if (!minDiff || !maxDiff) {
                        _this._modelBounding = modelBounding;
                        _this._updateReferenceGeometry[AxisIndex.X] = true;
                        _this._updateReferenceGeometry[AxisIndex.Y] = true;
                        _this._updateReferenceGeometry[AxisIndex.Z] = true;
                        _this._updateReferenceGeometry[AxisIndex.FACE] = true;
                        var activeStates = [
                            _this._isActive(AxisIndex.X),
                            _this._isActive(AxisIndex.Y),
                            _this._isActive(AxisIndex.Z),
                            _this._isActive(AxisIndex.FACE),
                        ];
                        _this._storePlanes();
                        p = _this._clearCuttingSections().then(function () {
                            return _this._restorePlanes(activeStates);
                        });
                    }
                    else {
                        p = Promise.resolve(null);
                    }
                    _this._boundingBoxUpdate = false;
                });
            };
            CuttingPlaneController.prototype._resetAxis = function (axis) {
                this._cuttingPlanes[axis] = null;
                this._referenceGeometry[axis] = null;
                this._setStatus(axis, Status.Hidden);
                this._updateReferenceGeometry[axis] = false;
                if (axis == AxisIndex.FACE) {
                    this._faceSelection = null;
                }
            };
            CuttingPlaneController.prototype.resetCuttingPlanes = function () {
                this._resetAxis(AxisIndex.X);
                this._resetAxis(AxisIndex.Y);
                this._resetAxis(AxisIndex.Z);
                this._resetAxis(AxisIndex.FACE);
                this._useIndividualCuttingSections = true;
                this._showReferenceGeometry = true;
                this._faceSelection = null;
                this._clearCuttingSections();
            };
            CuttingPlaneController.prototype._initSection = function () {
                var _this = this;
                this._viewer.getModel().getModelBounding(true, false).then(function (modelBounding) {
                    _this._modelBounding = modelBounding.copy();
                    _this._cuttingSections[AxisIndex.X] = _this._viewer.getCuttingManager().getCuttingSection(AxisIndex.X);
                    _this._cuttingSections[AxisIndex.Y] = _this._viewer.getCuttingManager().getCuttingSection(AxisIndex.Y);
                    _this._cuttingSections[AxisIndex.Z] = _this._viewer.getCuttingManager().getCuttingSection(AxisIndex.Z);
                    _this._cuttingSections[AxisIndex.FACE] = _this._viewer.getCuttingManager().getCuttingSection(AxisIndex.FACE);
                    _this._triggerPendingFuncs();
                });
            };
            CuttingPlaneController.prototype._triggerPendingFuncs = function () {
                if (this._pendingFuncs.inverted) {
                    this._pendingFuncs.inverted();
                    this._pendingFuncs.inverted = null;
                }
                if (this._pendingFuncs.visibility) {
                    this._pendingFuncs.visibility();
                    this._pendingFuncs.visibility = null;
                }
            };
            CuttingPlaneController.prototype.toggle = function (planeAxis) {
                var axis = this._getAxis(planeAxis);
                var faceSelection = false;
                if (axis == AxisIndex.FACE) {
                    var selectionItem = this._viewer.getSelectionManager().getLast();
                    faceSelection = (selectionItem != null && selectionItem.getFaceEntity() != null);
                    if (faceSelection) {
                        this._faceSelection = selectionItem;
                    }
                }
                switch (this._status[axis]) {
                    case Status.Hidden:
                        if (axis == AxisIndex.FACE) {
                            if (faceSelection) {
                                this._cuttingSections[axis].clear();
                                this._cadViewActivated = false;
                                this._setStatus(axis, Status.Visible);
                                this.setCuttingPlaneVisibility(true, axis);
                            }
                        }
                        else {
                            this._setStatus(axis, Status.Visible);
                            this.setCuttingPlaneVisibility(true, axis);
                        }
                        break;
                    case Status.Visible:
                        this._setStatus(axis, Status.Inverted);
                        this.setCuttingPlaneInverted(axis);
                        break;
                    case Status.Inverted:
                        this._setStatus(axis, Status.Hidden);
                        this.setCuttingPlaneVisibility(false, axis);
                        break;
                }
            };
            CuttingPlaneController.prototype.getCount = function () {
                var count = 0;
                for (var i = 0; i < this._cuttingSections.length; i++) {
                    count += this._cuttingSections[i].getCount();
                }
                return count;
            };
            CuttingPlaneController.prototype.setCuttingPlaneVisibility = function (visibility, axis) {
                var _this = this;
                if (!this._cuttingSections[this._getCuttingSectionIndex(axis)]) {
                    this._pendingFuncs.visibility = function () {
                        _this.setCuttingPlaneVisibility(visibility, axis);
                    };
                    return;
                }
                this._viewer.delayCapping();
                if (visibility) {
                    if (this._cuttingPlanes[axis] == null) {
                        this._cuttingPlanes[axis] = this._generateCuttingPlane(axis);
                        this._referenceGeometry[axis] = this._generateReferenceGeometry(axis);
                    }
                    this._setSection(axis);
                }
                else {
                    this.refreshPlaneGeometry();
                }
                var count = this.getCount();
                var active = this._isActive(axis);
                if (count > 0 && !active) {
                    this._activatePlanes();
                }
                else if (active && count == 0) {
                    this._deactivateAxis(axis);
                }
            };
            CuttingPlaneController.prototype.setCuttingPlaneInverted = function (axis) {
                var _this = this;
                if (!this._cuttingSections[this._getCuttingSectionIndex(axis)]) {
                    this._pendingFuncs.inverted = function () {
                        _this.setCuttingPlaneInverted(axis);
                    };
                    return;
                }
                this._viewer.delayCapping();
                var index = this._getPlaneIndex(axis);
                var plane = this._cuttingSections[this._getCuttingSectionIndex(axis)].getPlane(index);
                if (plane) {
                    plane.normal.negate();
                    plane.d *= -1;
                    this._cuttingSections[this._getCuttingSectionIndex(axis)].updatePlane(index, plane);
                }
            };
            CuttingPlaneController.prototype.toggleReferenceGeometry = function () {
                this._showReferenceGeometry = !this._showReferenceGeometry;
                this.refreshPlaneGeometry();
            };
            CuttingPlaneController.prototype.refreshPlaneGeometry = function () {
                this._storePlanes();
                this._clearCuttingSections();
                this._restorePlanes();
            };
            CuttingPlaneController.prototype.toggleCuttingMode = function () {
                this._storePlanes();
                this._clearCuttingSections();
                this._useIndividualCuttingSections = !this._useIndividualCuttingSections;
                this._restorePlanes();
            };
            CuttingPlaneController.prototype.cadViewActivated = function () {
                this._cadViewActivated = true;
                this._setStatus(AxisIndex.FACE, Status.Hidden);
            };
            CuttingPlaneController.prototype._isActive = function (axis) {
                return this._cuttingSections[this._getCuttingSectionIndex(axis)].isActive();
            };
            CuttingPlaneController.prototype._activateAxis = function (axis) {
                this._cuttingSections[this._getCuttingSectionIndex(axis)].activate();
            };
            CuttingPlaneController.prototype._deactivateAxis = function (axis) {
                this._cuttingSections[this._getCuttingSectionIndex(axis)].deactivate();
            };
            CuttingPlaneController.prototype._getCuttingSectionIndex = function (axis) {
                return this._useIndividualCuttingSections ? axis : 0;
            };
            CuttingPlaneController.prototype._clearCuttingSection = function (axis) {
                if (this._cuttingSections[axis]) {
                    return this._cuttingSections[axis].clear();
                }
                return Promise.resolve(null);
            };
            CuttingPlaneController.prototype._clearCuttingSections = function () {
                var ps = [];
                ps.push(this._clearCuttingSection(AxisIndex.X));
                ps.push(this._clearCuttingSection(AxisIndex.Y));
                ps.push(this._clearCuttingSection(AxisIndex.Z));
                if (!this._cadViewActivated) {
                    ps.push(this._clearCuttingSection(AxisIndex.FACE));
                }
                return Promise.all(ps);
            };
            CuttingPlaneController.prototype._activatePlane = function (axis) {
                var section = this._cuttingSections[axis];
                if (section.getCount()) {
                    var p = section.activate();
                    if (p === null) {
                        return Promise.resolve(null);
                    }
                    return p;
                }
                return Promise.resolve(null);
            };
            CuttingPlaneController.prototype._activatePlanes = function (activeStates) {
                var ps = [];
                if (!activeStates || activeStates[0])
                    ps.push(this._activatePlane(AxisIndex.X));
                if (!activeStates || activeStates[1])
                    ps.push(this._activatePlane(AxisIndex.Y));
                if (!activeStates || activeStates[2])
                    ps.push(this._activatePlane(AxisIndex.Z));
                if (!activeStates || activeStates[3])
                    ps.push(this._activatePlane(AxisIndex.FACE));
                return Promise.all(ps);
            };
            CuttingPlaneController.prototype._getAxis = function (planeAxis) {
                switch (planeAxis) {
                    case 'x':
                        return AxisIndex.X;
                    case 'y':
                        return AxisIndex.Y;
                    case 'z':
                        return AxisIndex.Z;
                    case 'face':
                        return AxisIndex.FACE;
                    default:
                        return null;
                }
            };
            CuttingPlaneController.prototype._getPlaneIndex = function (axis) {
                var plane;
                if (this._useIndividualCuttingSections) {
                    if (this._cuttingSections[this._getCuttingSectionIndex(axis)].getPlane(0)) {
                        return 0;
                    }
                }
                else {
                    for (var i = 0; i < this._cuttingSections[0].getCount(); i++) {
                        plane = this._cuttingSections[0].getPlane(i);
                        var normal = null;
                        if (this._faceSelection && this._faceSelection.getFaceEntity()) {
                            normal = this._faceSelection.getFaceEntity().getNormal();
                        }
                        if (plane) {
                            if ((plane.normal.x && axis == AxisIndex.X) ||
                                (plane.normal.y && axis == AxisIndex.Y) ||
                                (plane.normal.z && axis == AxisIndex.Z) ||
                                (axis == AxisIndex.FACE && normal && plane.normal.equals(normal))) {
                                return i;
                            }
                        }
                    }
                }
                return -1;
            };
            CuttingPlaneController.prototype._setSection = function (axis) {
                if (this._cuttingPlanes[axis] != null) {
                    this._cuttingSections[this._getCuttingSectionIndex(axis)].addPlane(this._cuttingPlanes[axis], this._showReferenceGeometry ? this._referenceGeometry[axis] : null);
                }
            };
            CuttingPlaneController.prototype._restorePlane = function (axis) {
                if (this._cuttingPlanes[axis] != null && this._status[axis] != Status.Hidden) {
                    if (!this._referenceGeometry[axis] || this._updateReferenceGeometry[axis]) {
                        this._referenceGeometry[axis] = this._generateReferenceGeometry(axis);
                    }
                    this._setSection(axis);
                }
            };
            CuttingPlaneController.prototype._restorePlanes = function (activeStates) {
                this._restorePlane(AxisIndex.X);
                this._restorePlane(AxisIndex.Y);
                this._restorePlane(AxisIndex.Z);
                this._restorePlane(AxisIndex.FACE);
                return this._activatePlanes(activeStates);
            };
            CuttingPlaneController.prototype._storePlane = function (axis) {
                var cuttingSection = this._cuttingSections[this._getCuttingSectionIndex(axis)];
                this._cuttingPlanes[axis] = null;
                this._referenceGeometry[axis] = null;
                if (cuttingSection.getCount() > 0 && (this._status[axis] != Status.Hidden)) {
                    var planeIndex = this._getPlaneIndex(axis);
                    var plane = cuttingSection.getPlane(planeIndex);
                    var referenceGeometry = cuttingSection.getReferenceGeometry(planeIndex);
                    if (!(this._cadViewActivated && axis == AxisIndex.FACE)) {
                        this._cuttingPlanes[axis] = plane;
                        if (referenceGeometry) {
                            this._referenceGeometry[axis] = referenceGeometry;
                        }
                    }
                }
            };
            CuttingPlaneController.prototype._storePlanes = function () {
                this._storePlane(AxisIndex.X);
                this._storePlane(AxisIndex.Y);
                this._storePlane(AxisIndex.Z);
                this._storePlane(AxisIndex.FACE);
            };
            CuttingPlaneController.prototype._generateReferenceGeometry = function (axisIndex) {
                var referenceGeometry = [];
                var axis;
                if (axisIndex == AxisIndex.FACE) {
                    if (this._faceSelection && this._faceSelection.getFaceEntity()) {
                        var normal = this._faceSelection.getFaceEntity().getNormal();
                        var position = this._faceSelection.getPosition();
                        referenceGeometry = this._viewer.getCuttingManager().createReferenceGeometryFromFaceNormal(normal, position, this._modelBounding);
                    }
                }
                else {
                    switch (axisIndex) {
                        case AxisIndex.X:
                            axis = Communicator.Axis.X;
                            break;
                        case AxisIndex.Y:
                            axis = Communicator.Axis.Y;
                            break;
                        case AxisIndex.Z:
                            axis = Communicator.Axis.Z;
                            break;
                    }
                    referenceGeometry = this._viewer.getCuttingManager().createReferenceGeometryFromAxis(axis, this._modelBounding);
                }
                return referenceGeometry;
            };
            CuttingPlaneController.prototype._generateCuttingPlane = function (axis) {
                var plane = new Communicator.Plane();
                switch (axis) {
                    case AxisIndex.X:
                        plane.normal.set(1, 0, 0);
                        plane.d = -this._modelBounding.max.x;
                        break;
                    case AxisIndex.Y:
                        plane.normal.set(0, 1, 0);
                        plane.d = -this._modelBounding.max.y;
                        break;
                    case AxisIndex.Z:
                        plane.normal.set(0, 0, 1);
                        plane.d = -this._modelBounding.max.z;
                        break;
                    case AxisIndex.FACE:
                        if (this._faceSelection && this._faceSelection.getFaceEntity()) {
                            this._faceSelection = this._faceSelection;
                            var normal = this._faceSelection.getFaceEntity().getNormal();
                            var position = this._faceSelection.getPosition();
                            plane.setFromPointAndNormal(position, normal);
                        }
                        else {
                            return null;
                        }
                }
                return plane;
            };
            return CuttingPlaneController;
        }());
        Ui.CuttingPlaneController = CuttingPlaneController;
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
var Communicator;
(function (Communicator) {
    var IsolateZoomHelper = (function () {
        function IsolateZoomHelper(viewer) {
            this._viewer = viewer;
            this._camera = null;
            this._cameraSet = false;
            this._deselectOnIsolate = true;
            this._deselectOnZoom = true;
            this._isolateStatus = false;
        }
        IsolateZoomHelper.prototype._setCamera = function (camera) {
            if (!this._cameraSet) {
                this._camera = camera;
                this._cameraSet = true;
            }
        };
        IsolateZoomHelper.prototype._getCamera = function () {
            this._cameraSet = false;
            return this._camera;
        };
        IsolateZoomHelper.prototype.setDeselectOnIsolate = function (deselect) {
            this._deselectOnIsolate = deselect;
        };
        IsolateZoomHelper.prototype.getIsolateStatus = function () {
            return this._isolateStatus;
        };
        IsolateZoomHelper.prototype.isolateNodes = function (nodeIds) {
            this._setCamera(this._viewer.getView().getCamera());
            this._viewer.getView().isolateNodes(nodeIds);
            if (this._deselectOnIsolate) {
                this._viewer.getSelectionManager().clear();
            }
            this._isolateStatus = true;
        };
        IsolateZoomHelper.prototype.fitNodes = function (nodeIds) {
            this._setCamera(this._viewer.getView().getCamera());
            this._viewer.getView().fitNodes(nodeIds);
            if (this._deselectOnZoom) {
                this._viewer.getSelectionManager().clear();
            }
        };
        IsolateZoomHelper.prototype.showAll = function () {
            if (this._viewer.getModel().isDrawing()) {
                var sheetId = this._viewer.getActiveSheetId();
                if (sheetId !== null) {
                    this.isolateNodes([sheetId]);
                }
            }
            else {
                this._viewer.getModel().resetNodesVisibility();
                if (this._cameraSet) {
                    this._viewer.getView().setCamera(this._getCamera(), 400);
                }
                this._isolateStatus = false;
                this._updatePinVisibility();
            }
        };
        IsolateZoomHelper.prototype._updatePinVisibility = function () {
            Communicator.Markup.NoteText.setIsolateActive(this._isolateStatus);
            Communicator.Markup.NoteText.updatePinVisibility();
        };
        return IsolateZoomHelper;
    }());
    Communicator.IsolateZoomHelper = IsolateZoomHelper;
})(Communicator || (Communicator = {}));
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        var Desktop;
        (function (Desktop) {
            var ModelBrowserContextMenu = (function (_super) {
                __extends(ModelBrowserContextMenu, _super);
                function ModelBrowserContextMenu(containerId, viewer, modelTree, isolateZoomHelper) {
                    _super.call(this, "modelbrowser", containerId, viewer, isolateZoomHelper);
                    this._modelTree = modelTree;
                    this._initEvents();
                }
                ModelBrowserContextMenu.prototype._initEvents = function () {
                    var _this = this;
                    this._modelTree.registerCallback("context", function (id, position) {
                        _this._onTreeContext(id, position);
                    });
                    if (this._viewer.getStreamingMode() == Communicator.StreamingMode.OnDemand) {
                        this.appendseparator();
                        this.appendItem("request", "Request", function () {
                            if (_this._viewer.getSelectionManager() != null) {
                                _this._viewer.getModel().requestNodes(_this.getContextItemIds(false, true));
                            }
                        });
                    }
                };
                ModelBrowserContextMenu.prototype._onTreeContext = function (id, position) {
                    var components = id.split(Ui.ModelTree.separator);
                    switch (components[0]) {
                        case "part":
                            this.setActiveItemId(parseInt(components[1]));
                            break;
                        default:
                            return;
                    }
                    ;
                    this._position = null;
                    this.showElements(position);
                };
                ModelBrowserContextMenu.prototype._onContextLayerClick = function (event) {
                    this.hide();
                };
                return ModelBrowserContextMenu;
            }(Ui.Context.ContextMenu));
            Desktop.ModelBrowserContextMenu = ModelBrowserContextMenu;
        })(Desktop = Ui.Desktop || (Ui.Desktop = {}));
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        var RightClickContextMenu = (function (_super) {
            __extends(RightClickContextMenu, _super);
            function RightClickContextMenu(containerId, viewer, isolateZoomHelper) {
                _super.call(this, "rightclick", containerId, viewer, isolateZoomHelper);
                this._initEvents();
            }
            RightClickContextMenu.prototype._initEvents = function () {
                var _this = this;
                this._viewer.setCallbacks({
                    contextMenu: function (position) {
                        _this.doContext(position);
                    }
                });
            };
            RightClickContextMenu.prototype.doContext = function (position) {
                var _this = this;
                var config = new Communicator.PickConfig(Communicator.SelectionMask.All);
                this._viewer.getView().pickFromPoint(position, config).then(function (selectionItem) {
                    var axisOverlay = 1;
                    var nodeType = _this._viewer.getModel().getNodeType(selectionItem.getNodeId());
                    if (nodeType == Communicator.NodeType.PMI || nodeType == Communicator.NodeType.PMIBody || selectionItem.overlayIndex() == axisOverlay) {
                        _this.setActiveItemId(null);
                    }
                    else {
                        _this.setActiveItemId(selectionItem.getNodeId());
                    }
                    _this._position = selectionItem.getPosition();
                    _this.showElements(position);
                });
            };
            RightClickContextMenu.prototype._onContextLayerClick = function (event) {
                if (event.button == 2)
                    this.doContext(new Communicator.Point2(event.pageX, event.pageY));
                else
                    _super.prototype._onContextLayerClick.call(this, event);
            };
            return RightClickContextMenu;
        }(Ui.Context.ContextMenu));
        Ui.RightClickContextMenu = RightClickContextMenu;
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        var SheetsTree = (function (_super) {
            __extends(SheetsTree, _super);
            function SheetsTree(elementId, viewer) {
                _super.call(this, elementId, viewer);
                this._internalID = 'sheetstree';
                this._currentSheetId = null;
                this._initEvents();
            }
            SheetsTree.prototype._initEvents = function () {
                var _this = this;
                this._viewer.setCallbacks({
                    modelStructureReady: function () {
                        _this._onModelStructureReady();
                    },
                    sheetActivated: function (sheetId) {
                        _this._onSheetActivated(sheetId);
                    },
                    modelSwitched: function () {
                        _this._onModelStructureReady();
                    }
                });
                this._tree.registerCallback("selectItem", function (id, selectionMode) {
                    _this._onTreeSelectItem(id, selectionMode);
                });
            };
            SheetsTree.prototype._onModelStructureReady = function () {
                this._tree.clear();
                var $sheetsTab = $("#sheetsTab");
                if (this._viewer.getModel().isDrawing()) {
                    var model = this._viewer.getModel();
                    var rootId = model.getRootNode();
                    var name = model.getNodeName(rootId);
                    var rootChild = model.getNodeChildren(rootId);
                    var rootChildChild = model.getNodeChildren(rootChild[0]);
                    var sheets = model.getNodeChildren(rootChildChild[0]);
                    for (var i = 0; i < sheets.length; i++) {
                        var id = sheets[i];
                        var name = model.getNodeName(id);
                        var sheetId = this._sheetId(id + '');
                        this._tree.appendTopLevelElement(name, sheetId, "sheet", false);
                    }
                    if (sheets.length > 0) {
                        this._onTreeSelectItem(this._sheetId(sheets[0] + ''));
                    }
                    $sheetsTab.show();
                }
                else {
                    $sheetsTab.hide();
                }
            };
            SheetsTree.prototype._sheetTreeId = function (sheetId) {
                return "sheetstree_" + sheetId;
            };
            SheetsTree.prototype._onSheetActivated = function (sheetId) {
                var idStr = this._sheetTreeId(sheetId);
                if (this._currentSheetId !== null && this._currentSheetId !== idStr) {
                    var $currentSheetNode = $("#" + this._currentSheetId);
                    if ($currentSheetNode !== null) {
                        $currentSheetNode.removeClass("selected-sheet");
                    }
                }
                var $sheetNode = $("#" + idStr);
                if ($sheetNode !== null) {
                    $sheetNode.addClass("selected-sheet");
                }
                this._currentSheetId = idStr;
            };
            SheetsTree.prototype._onTreeSelectItem = function (idstr, selectionMode) {
                if (selectionMode === void 0) { selectionMode = Communicator.SelectionMode.Set; }
                var idParts = this._splitIdStr(idstr);
                var id = parseInt(idParts[1]);
                this._viewer.setActiveSheetId(id);
            };
            SheetsTree.prototype._sheetId = function (id) {
                return this._internalID + Ui.ViewTree.separator + id;
            };
            return SheetsTree;
        }(Ui.ViewTree));
        Ui.SheetsTree = SheetsTree;
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
;
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        var NodeInfo = (function () {
            function NodeInfo(node) {
                this._node = node;
                this._clone = node.cloneNode(true);
            }
            return NodeInfo;
        }());
        ;
        var SnapShot = (function () {
            function SnapShot() {
                this._image = null;
                this._additionalNodes = [];
                this._clientBoundingRects = [];
                for (var i = 0; i < 4; i++) {
                    var rect = document.body.getBoundingClientRect();
                    this._clientBoundingRects.push(rect);
                }
            }
            SnapShot.prototype.start = function (viewer, width, height) {
                var _this = this;
                var promise = new Promise(function (resolve, reject) {
                    viewer.takeSnapshot(_this, width, height, resolve);
                });
                return promise;
            };
            SnapShot.prototype.addCaptureNodes = function (id) {
                var node = document.getElementById(id);
                if (node != null) {
                    var nodeInfo = new NodeInfo(node);
                    this._additionalNodes.push(nodeInfo);
                    var rect = node.getBoundingClientRect();
                    this._clientBoundingRects.push(rect);
                }
            };
            SnapShot.prototype.reformatMarkerStyle = function (xml) {
                var parser = new DOMParser();
                var doc = parser.parseFromString(xml, 'text/xml');
                var defs = doc.getElementsByTagName('defs');
                var markers = [];
                for (var i = 0; i < defs.length; i++) {
                    var currMarkers = defs[0].getElementsByTagName('marker');
                    for (var j = 0; j < currMarkers.length; j++) {
                        markers.push(currMarkers.item(j));
                    }
                }
                var linesWithMarkers = [];
                var lines = doc.getElementsByTagName('line');
                for (var i = 0; i < lines.length; i++) {
                    var line = lines[i];
                    if (line.style.marker.length > 0) {
                        var idStart = line.style.marker.indexOf('#') + 1;
                        var idEnd = line.style.marker.indexOf('"', idStart + 1);
                        var id = line.style.marker.substring(idStart, idEnd);
                        var markerStart = null;
                        var markerEnd = null;
                        for (var j = 0; j < markers.length; j++) {
                            if (markers[j].id == id) {
                                markerStart = markers[j];
                                markerEnd = markers[j + 1];
                                break;
                            }
                        }
                        var lineInfo = { line: line, start: markerStart, end: markerEnd };
                        linesWithMarkers.push(lineInfo);
                    }
                }
                var markerStyleString = 'marker: url';
                var markerStyleStartIndex = xml.indexOf(markerStyleString);
                var currStartIndex = 0;
                while (markerStyleStartIndex >= 0) {
                    var markerStyleEndIndex = xml.indexOf(')', markerStyleStartIndex) + 1;
                    var markerStyle = xml.substring(markerStyleStartIndex, markerStyleEndIndex);
                    var markerIDStartIndex = markerStyle.indexOf('#') + 1;
                    var markerIDEndIndex0 = markerStyle.indexOf('"', markerIDStartIndex);
                    var markerIDEndIndex1 = markerStyle.indexOf('&quot', markerIDStartIndex + markerStyleString.length + 1);
                    var markerIDEndIndex = markerIDEndIndex0;
                    if (markerIDEndIndex0 == -1 || markerIDEndIndex1 < markerIDEndIndex0) {
                        markerIDEndIndex = markerIDEndIndex1;
                    }
                    var markerID = markerStyle.substring(markerIDStartIndex, markerIDEndIndex);
                    for (var i = 0; i < linesWithMarkers.length; i++) {
                        if (linesWithMarkers[i].start.id == markerID) {
                            var newStyle = 'marker-start: url(\'#' + linesWithMarkers[i].start.id + '\');';
                            newStyle += 'marker-end: url(\'#' + linesWithMarkers[i].end.id + '\')';
                            xml = xml.slice(0, markerStyleStartIndex) + newStyle + xml.slice(markerStyleEndIndex);
                            break;
                        }
                    }
                    currStartIndex = markerStyleEndIndex;
                    markerStyleStartIndex = xml.indexOf(markerStyleString, markerStyleEndIndex + 1);
                }
                return xml;
            };
            SnapShot.prototype.convertToImage64 = function (name) {
                var ret = { svg: null, image64: null };
                ret.svg = document.getElementById(name);
                var xml = new XMLSerializer().serializeToString(ret.svg);
                xml = this.reformatMarkerStyle(xml);
                var svg64 = btoa(xml);
                var b64Start = 'data:image/svg+xml;base64,';
                ret.image64 = b64Start + svg64;
                return ret;
            };
            SnapShot.prototype.compositeScreenShot = function (destinationImage, canvas, canvasContext, images, boundingRects, callBack) {
                for (var i = 0; i < images.length; i++) {
                    var image = images[i];
                    if (i < this._clientBoundingRects.length) {
                        var rect = this._clientBoundingRects[i];
                        canvasContext.drawImage(image, rect.left, rect.top);
                    }
                    else {
                        canvasContext.drawImage(image, 0, 0);
                    }
                }
                destinationImage.src = canvas.toDataURL();
                destinationImage.onload =
                    function () {
                        callBack(destinationImage);
                    };
            };
            SnapShot.prototype.getAllChildren = function (nodeList, node) {
                if (node.childNodes.length <= 0) {
                    return;
                }
                for (var i = 0; i < node.childNodes.length; i++) {
                    var child = node.childNodes[i];
                    nodeList.push(child);
                    this.getAllChildren(nodeList, child);
                }
            };
            SnapShot.prototype.cleanupAfterSnap = function (self) {
                for (var i = 0; i < self._additionalNodes.length; i++) {
                    var currNodeInfo = self._additionalNodes[i];
                    var currNode = currNodeInfo._node;
                    var rect = currNodeInfo._clone.getBoundingClientRect();
                    var prop = null;
                    var element = currNodeInfo._clone;
                    for (prop in element.style) {
                        currNode.style[prop] = element.style[prop];
                    }
                }
                self._additionalNodes = [];
                self._clientBoundingRects = [];
                for (var i = 0; i < 4; i++) {
                    var rect = document.body.getBoundingClientRect();
                    self._clientBoundingRects.push(rect);
                }
            };
            SnapShot.prototype.captureAdditionalNodes = function (captureImages, canvas, ctx, callBack) {
                if (this._additionalNodes.length <= 0) {
                    this.compositeScreenShot(this._image, canvas, ctx, captureImages, this._clientBoundingRects, callBack);
                    this.cleanupAfterSnap(this);
                    return;
                }
                var self = this;
                var rects = [];
                var nodeInfo = null;
                for (var i = 0; i < this._additionalNodes.length; i++) {
                    nodeInfo = this._additionalNodes[i];
                    var thisNode = nodeInfo._node;
                    rects.push(thisNode.getBoundingClientRect());
                    thisNode.style.top = '0px';
                    thisNode.style.left = '0px';
                    html2canvas(thisNode, { background: undefined })
                        .then(function (domCanvas) {
                        var thisImage = new Image();
                        thisImage.onload = function (event) {
                            captureImages.push(this);
                            if (captureImages.length - 4 >= self._additionalNodes.length) {
                                for (var i = 0; i < self._additionalNodes.length; i++) {
                                    for (var j = i; j < self._additionalNodes.length; j++) {
                                        var thisImage = captureImages[j + 4];
                                        if (self._additionalNodes[i]._node == thisImage['userData']) {
                                            var tempImage = captureImages[i + 4];
                                            captureImages[i + 4] = captureImages[j + 4];
                                            captureImages[j + 4] = tempImage;
                                        }
                                    }
                                }
                                self.compositeScreenShot(self._image, canvas, ctx, captureImages, self._clientBoundingRects, callBack);
                                self.cleanupAfterSnap(self);
                            }
                        }.bind(thisImage);
                        thisImage['userData'] = this;
                        thisImage.src = domCanvas.toDataURL();
                    }.bind(thisNode));
                }
            };
            SnapShot.prototype.capture = function (mainCanvasContainer, callBack) {
                var mainCanvas = this._findCanvas(mainCanvasContainer);
                var imageDataBase64 = mainCanvas.toDataURL();
                var canvas = document.createElement('canvas');
                var ctx = canvas.getContext('2d');
                canvas.width = mainCanvas.clientWidth;
                canvas.height = mainCanvas.clientHeight;
                canvas.style.width = '100%';
                canvas.style.height = '100%';
                var svgImage = new Image();
                var glImage = new Image();
                var redlineImage = new Image();
                var redlineTextImage = new Image();
                this._image = new Image();
                var imageLoadCount = 0;
                var self = this;
                var redlineSVG = document.getElementById('viewerContainer-redline-svg');
                var svg = document.getElementById('viewerContainer-svg');
                redlineImage.onload = function () {
                    redlineImage.width = redlineSVG.clientWidth;
                    redlineImage.height = redlineSVG.clientHeight;
                    ++imageLoadCount;
                    if (imageLoadCount >= 4) {
                        self.captureAdditionalNodes([glImage, svgImage, redlineImage, redlineTextImage], canvas, ctx, callBack);
                    }
                };
                glImage.onload = function () {
                    ++imageLoadCount;
                    if (imageLoadCount >= 4) {
                        self.captureAdditionalNodes([glImage, svgImage, redlineImage, redlineTextImage], canvas, ctx, callBack);
                    }
                };
                svgImage.onload = function () {
                    svgImage.width = svg.clientWidth;
                    svgImage.height = svg.clientHeight;
                    ++imageLoadCount;
                    if (imageLoadCount >= 4) {
                        self.captureAdditionalNodes([glImage, svgImage, redlineImage, redlineTextImage], canvas, ctx, callBack);
                    }
                };
                glImage.src = imageDataBase64;
                var tempRedlineCanvas = document.createElement('canvas');
                var redlineXML = new XMLSerializer().serializeToString(redlineSVG);
                document.body.appendChild(tempRedlineCanvas);
                canvg(tempRedlineCanvas, redlineXML);
                redlineImage.src = tempRedlineCanvas.toDataURL();
                document.body.removeChild(tempRedlineCanvas);
                var tempSVGCanvas = document.createElement('canvas');
                var svgXML = new XMLSerializer().serializeToString(svg);
                svgXML = this.reformatMarkerStyle(svgXML);
                document.body.appendChild(tempSVGCanvas);
                canvg(tempSVGCanvas, svgXML);
                svgImage.src = tempSVGCanvas.toDataURL();
                document.body.removeChild(tempSVGCanvas);
                var node = document.getElementById('viewerContainer-redline');
                var self = this;
                html2canvas(node, { background: undefined }).then(function (redlineCanvas) {
                    var imageDataBase64 = redlineCanvas.toDataURL();
                    redlineTextImage.onload = function () {
                        ++imageLoadCount;
                        redlineTextImage.width = node.clientWidth;
                        redlineTextImage.height = node.clientHeight;
                        if (imageLoadCount >= 4) {
                            self.captureAdditionalNodes([glImage, svgImage, redlineImage, redlineTextImage], canvas, ctx, callBack);
                        }
                    };
                    redlineTextImage.src = imageDataBase64;
                });
            };
            SnapShot.prototype._findCanvas = function (node) {
                if (node instanceof HTMLCanvasElement) {
                    var canvas = node;
                    if (canvas.clientWidth > 0 && canvas.clientHeight > 0) {
                        return node;
                    }
                }
                for (var i = 0; i < node.childNodes.length; i++) {
                    var child = this._findCanvas(node.childNodes[i]);
                    if (child) {
                        return child;
                    }
                }
                return null;
            };
            return SnapShot;
        }());
        Ui.SnapShot = SnapShot;
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        var StreamingIndicator = (function () {
            function StreamingIndicator(elementId, viewer) {
                this._bottomLeftOffset = new Communicator.Point2(10, 10);
                this._opacity = 0.5;
                this._spinnerImageUrl = "ui/css/img/loading-icon.gif";
                this._spinnerSize = new Communicator.Point2(31, 31);
                this._isInit = false;
                this._container = document.getElementById(elementId);
                this._viewer = viewer;
                this._initElements();
                this._initEvents();
            }
            StreamingIndicator.prototype.show = function () {
                this._container.style.display = "block";
            };
            StreamingIndicator.prototype.hide = function () {
                this._container.style.display = "none";
            };
            StreamingIndicator.prototype.setBottomLeftOffset = function (point) {
                this._bottomLeftOffset.assign(point);
                if (this._isInit) {
                    this._container.style.bottom = this._bottomLeftOffset.y + "px";
                    this._container.style.left = this._bottomLeftOffset.x + "px";
                }
            };
            StreamingIndicator.prototype.getBottomLeftOffset = function () {
                return this._bottomLeftOffset.copy();
            };
            StreamingIndicator.prototype.setSpinnerImage = function (spinnerUrl, size) {
                this._spinnerImageUrl = spinnerUrl;
                this._spinnerSize.assign(size);
                if (this._isInit) {
                    this._container.style.backgroundImage = "url(" + this._spinnerImageUrl + ')';
                    this._container.style.width = this._spinnerSize.x + "px";
                    this._container.style.height = this._spinnerSize.y + "px";
                }
            };
            StreamingIndicator.prototype._initElements = function () {
                this._container.style.position = "absolute";
                this._container.style.width = this._spinnerSize.x + "px";
                this._container.style.height = this._spinnerSize.y + "px";
                this._container.style.bottom = this._bottomLeftOffset.y + "px";
                this._container.style.left = this._bottomLeftOffset.x + "px";
                this._container.style.backgroundImage = "url(" + this._spinnerImageUrl + ')';
                this._container.style.opacity = this._opacity.toString();
                this._isInit = true;
            };
            StreamingIndicator.prototype._initEvents = function () {
                var _this = this;
                this._viewer.setCallbacks({
                    streamingActivated: function () { _this._onStreamingActivated(); },
                    streamingDeactivated: function () { _this._onStreamingDeactivated(); }
                });
            };
            StreamingIndicator.prototype._onStreamingActivated = function () {
                this.show();
            };
            StreamingIndicator.prototype._onStreamingDeactivated = function () {
                this.hide();
            };
            return StreamingIndicator;
        }());
        Ui.StreamingIndicator = StreamingIndicator;
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        var UiDialog = (function () {
            function UiDialog(containerId) {
                this._containerId = containerId;
                this._initElements();
            }
            UiDialog.prototype._initElements = function () {
                var _this = this;
                this._windowElement = document.createElement("div");
                this._windowElement.classList.add("ui-timeout-window");
                this._windowElement.classList.add("desktop-ui-window");
                this._headerDiv = document.createElement("div");
                this._headerDiv.classList.add("desktop-ui-window-header");
                var contentDiv = document.createElement("div");
                contentDiv.classList.add("desktop-ui-window-content");
                this._textDiv = document.createElement("div");
                contentDiv.appendChild(this._textDiv);
                var br = document.createElement("div");
                br.classList.add("desktop-ui-window-divider");
                contentDiv.appendChild(br);
                var buttonDiv = document.createElement("div");
                var button = document.createElement("button");
                button.innerHTML = "Ok";
                $(button).button().click(function () {
                    _this._onOkButtonClick();
                });
                contentDiv.appendChild(button);
                this._windowElement.appendChild(this._headerDiv);
                this._windowElement.appendChild(contentDiv);
                document.getElementById(this._containerId).appendChild(this._windowElement);
            };
            UiDialog.prototype._onOkButtonClick = function () {
                this.hide();
            };
            UiDialog.prototype.show = function () {
                $(this._windowElement).show();
            };
            UiDialog.prototype.hide = function () {
                $(this._windowElement).hide();
            };
            UiDialog.prototype.setText = function (text) {
                $(this._textDiv).empty();
                this._textDiv.appendChild(document.createTextNode(text));
            };
            UiDialog.prototype.setTitle = function (title) {
                $(this._headerDiv).empty();
                this._headerDiv.appendChild(document.createTextNode(title));
            };
            return UiDialog;
        }());
        Ui.UiDialog = UiDialog;
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        var TimeoutWarningDialog = (function (_super) {
            __extends(TimeoutWarningDialog, _super);
            function TimeoutWarningDialog(containerId, viewer) {
                _super.call(this, containerId);
                this._viewer = viewer;
                this._initEvents();
                this.setTitle("Timeout Warning");
            }
            TimeoutWarningDialog.prototype._initEvents = function () {
                var _this = this;
                this._viewer.setCallbacks({
                    timeoutWarning: function () {
                        _this._onTimeoutWarning();
                    },
                    timeout: function () {
                        _this._onTimeout();
                    }
                });
            };
            TimeoutWarningDialog.prototype._onTimeoutWarning = function () {
                this.setText("Your session will expire soon. Press Ok to stay connected.");
                this.show();
            };
            TimeoutWarningDialog.prototype._onOkButtonClick = function () {
                this._viewer.resetClientTimeout();
                _super.prototype._onOkButtonClick.call(this);
            };
            TimeoutWarningDialog.prototype._onTimeout = function () {
                this.setText("Your session has been disconnected due to inactivity.");
                this.show();
            };
            return TimeoutWarningDialog;
        }(Ui.UiDialog));
        Ui.TimeoutWarningDialog = TimeoutWarningDialog;
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        var Toolbar = (function () {
            function Toolbar(viewer, cuttingPlaneController, axisTriad, navCube, screenConfiguration) {
                var _this = this;
                if (screenConfiguration === void 0) { screenConfiguration = Communicator.ScreenConfiguration.Desktop; }
                this._toolbarSelector = "#toolBar";
                this._screenElementSelector = "#content";
                this._submenuHeightOffset = 10;
                this._viewOrientationDuration = 500;
                this._activeSubmenu = null;
                this._hwvMethods = {};
                this._actions = {};
                this._isInit = false;
                this._viewer = viewer;
                this._screenConfiguration = screenConfiguration;
                this._cuttingPlaneController = cuttingPlaneController;
                this._viewerSettings = new Communicator.Ui.Desktop.ViewerSettings(viewer, axisTriad, navCube);
                this._viewer.setCallbacks({
                    selection: function (event) {
                        _this._onPartSelection(event);
                    }
                });
            }
            Toolbar.prototype.init = function () {
                var _this = this;
                if (this._isInit)
                    return;
                this._initIcons();
                this._removeNonApplicableIcons();
                $(".hoops-tool").bind("click", function (event) {
                    $(window).off('click');
                    event.preventDefault();
                    _this._processButtonClick(event);
                    return false;
                });
                $(".submenu-icon").bind("click", function (event) {
                    event.preventDefault();
                    _this._submenuIconClick(event.target);
                    return false;
                });
                $(this._toolbarSelector).bind("touchmove", function (event) {
                    event.originalEvent.preventDefault();
                });
                $(this._toolbarSelector).bind("mouseenter", function () {
                    _this._mouseEnter();
                });
                $(this._toolbarSelector).bind("mouseleave", function () {
                    _this._mouseLeave();
                });
                $(".tool-icon, .submenu-icon").bind("mouseenter", function (event) {
                    _this._mouseEnterItem(event);
                });
                $(".tool-icon, .submenu-icon").bind("mouseleave", function (event) {
                    _this._mouseLeaveItem(event);
                });
                $(window).resize(function () {
                    _this.reposition();
                });
                $(this._toolbarSelector).click(function () {
                    if (_this._activeSubmenu != null) {
                        _this._hideActiveSubmenu();
                    }
                });
                $(".toolbar-cp-plane").click(function (event) {
                    _this._cuttingPlaneButtonClick(event);
                });
                this._viewer.setCallbacks({
                    modelSwitched: function () {
                        _this._hideActiveSubmenu();
                    }
                });
                this._initSliders();
                this._initActions();
                this._initSnapshot();
                this.updateEdgeFaceButton();
                this.reposition();
                this.show();
                this._isInit = true;
            };
            Toolbar.prototype.disableSubmenuItem = function (item) {
                if (typeof (item) == "string") {
                    $("#submenus .toolbar-" + item).addClass("disabled");
                }
                else if (typeof (item) == "object") {
                    $.each(item, function (k, v) {
                        $("#submenus .toolbar-" + v).addClass("disabled");
                    });
                }
            };
            Toolbar.prototype.enableSubmenuItem = function (item) {
                if (typeof (item) == "string") {
                    $("#submenus .toolbar-" + item).removeClass("disabled");
                }
                else if (typeof (item) == "object") {
                    $.each(item, function (k, v) {
                        $("#submenus .toolbar-" + v).removeClass("disabled");
                    });
                }
            };
            Toolbar.prototype.setCorrespondingButtonForSubmenuItem = function (value) {
                var $item = $("#submenus .toolbar-" + value);
                this._activateSubmenuItem($item);
            };
            Toolbar.prototype._mouseEnterItem = function (event) {
                var $target = $(event.target);
                if (!$target.hasClass("disabled"))
                    $target.addClass("hover");
            };
            Toolbar.prototype._mouseLeaveItem = function (event) {
                $(event.target).removeClass("hover");
            };
            Toolbar.prototype.show = function () {
                $(this._toolbarSelector).show();
            };
            Toolbar.prototype.hide = function () {
                $(this._toolbarSelector).hide();
            };
            Toolbar.prototype._initSliders = function () {
                var _this = this;
                $("#explosion-slider").slider({
                    orientation: "vertical",
                    min: 0,
                    max: 200,
                    value: 0,
                    slide: function (event, ui) {
                        _this._onExplosionSilder(ui.value / 100);
                    }
                });
            };
            Toolbar.prototype._mouseEnter = function () {
                if (this._activeSubmenu == null) {
                    var $tools = $(this._toolbarSelector).find(".toolbar-tools");
                    $tools.stop();
                    $tools.css({
                        opacity: 1.0
                    });
                }
            };
            Toolbar.prototype._mouseLeave = function () {
                if (this._activeSubmenu == null) {
                    $(".toolbar-tools").animate({
                        opacity: 0.6
                    }, 500, function () {
                    });
                }
            };
            Toolbar.prototype.reposition = function () {
                var $toolbar = $(this._toolbarSelector);
                var $screen = $(this._screenElementSelector);
                var canvasCenterX = $screen.width() / 2;
                var toolbarX = canvasCenterX - ($toolbar.width() / 2);
            };
            Toolbar.prototype._processButtonClick = function (event) {
                if (this._activeSubmenu != null) {
                    this._hideActiveSubmenu();
                }
                else {
                    var $tool = $(event.target).closest(".hoops-tool");
                    if ($tool.hasClass("toolbar-radio")) {
                        if ($tool.hasClass("active-tool")) {
                            this._showSubmenu(event.target);
                        }
                        else {
                            $(this._toolbarSelector).find(".active-tool").removeClass("active-tool");
                            $tool.addClass("active-tool");
                            this._performAction($tool.data("operatorclass"));
                        }
                    }
                    else if ($tool.hasClass("toolbar-menu")) {
                        this._showSubmenu(event.target);
                    }
                    else if ($tool.hasClass("toolbar-menu-toggle")) {
                        this._toggleMenuTool($tool);
                    }
                    else {
                        this._performAction($tool.data("operatorclass"));
                    }
                }
            };
            Toolbar.prototype._toggleMenuTool = function ($tool) {
                var $toggleMenu = $("#" + $tool.data("submenu"));
                if ($toggleMenu.is(":visible")) {
                    $toggleMenu.hide();
                    this._performAction($tool.data("operatorclass"), false);
                }
                else {
                    this._alignMenuToTool($toggleMenu, $tool);
                    this._performAction($tool.data("operatorclass"), true);
                }
            };
            Toolbar.prototype._startModal = function () {
                var _this = this;
                $("body").append("<div id='toolbar-modal' class='toolbar-modal-overlay'></div>");
                $("#toolbar-modal").bind("click", function () {
                    _this._hideActiveSubmenu();
                });
            };
            Toolbar.prototype._alignMenuToTool = function ($submenu, $tool) {
                var position = $tool.position();
                var leftPositionOffset = position.left;
                if (this._screenConfiguration == Communicator.ScreenConfiguration.Mobile) {
                    var mobileScale = 1.74;
                    leftPositionOffset = leftPositionOffset / mobileScale;
                }
                var leftpos = leftPositionOffset - ($submenu.width() / 2) + 20;
                var topPos = -(this._submenuHeightOffset + $submenu.height());
                $submenu.css({
                    display: "block",
                    left: leftpos
                });
            };
            Toolbar.prototype._showSubmenu = function (item) {
                this._hideActiveSubmenu();
                var $tool = $(item).closest(".hoops-tool");
                var submenuId = $tool.data("submenu");
                if (submenuId != null) {
                    var $submenu = $(this._toolbarSelector + " #submenus #" + submenuId);
                    if (!$submenu.hasClass("disabled")) {
                        this._alignMenuToTool($submenu, $tool);
                        this._activeSubmenu = $submenu[0];
                        this._startModal();
                        $(this._toolbarSelector).find(".toolbar-tools").css({
                            opacity: 0.3
                        });
                    }
                }
            };
            Toolbar.prototype._hideActiveSubmenu = function () {
                $("#toolbar-modal").remove();
                if (this._activeSubmenu != null) {
                    $(this._activeSubmenu).hide();
                    $(this._toolbarSelector).find(".toolbar-tools").css({
                        opacity: 1.0
                    });
                }
                this._activeSubmenu = null;
            };
            Toolbar.prototype._activateSubmenuItem = function (submenuItem) {
                var $submenu = submenuItem.closest(".toolbar-submenu");
                var action = submenuItem.data("operatorclass");
                var $tool = $('#' + $submenu.data("button"));
                var $icon = $tool.find(".tool-icon");
                if ($icon.length) {
                    $icon.removeClass($tool.data("operatorclass").toString());
                    $icon.addClass(action);
                    $tool.data("operatorclass", action);
                    $tool.attr("title", submenuItem.attr("title"));
                }
                return action;
            };
            Toolbar.prototype._submenuIconClick = function (item) {
                var $selection = $(item);
                if ($selection.hasClass("disabled"))
                    return;
                var action = this._activateSubmenuItem($selection);
                this._hideActiveSubmenu();
                this._performAction(action);
            };
            Toolbar.prototype._initIcons = function () {
                $(this._toolbarSelector).find(".hoops-tool").each(function () {
                    var $element = $(this);
                    $element.find(".tool-icon").addClass($element.data("operatorclass").toString());
                });
                $(this._toolbarSelector).find(".submenu-icon").each(function () {
                    var $element = $(this);
                    $element.addClass($element.data("operatorclass").toString());
                });
            };
            Toolbar.prototype._removeNonApplicableIcons = function () {
                if (this._screenConfiguration == Communicator.ScreenConfiguration.Mobile) {
                    $("#snapshot-button").remove();
                }
            };
            Toolbar.prototype.setSubmenuEnabled = function (button, enabled) {
                var $button = $("#" + button);
                var $submenu = $('#' + $button.data("submenu"));
                if (enabled) {
                    $button.find(".smarrow").show();
                    $submenu.removeClass("disabled");
                }
                else {
                    $button.find(".smarrow").hide();
                    $submenu.addClass("disabled");
                }
            };
            Toolbar.prototype._performAction = function (action, arg) {
                if (arg === void 0) { arg = null; }
                var func = this._actions[action];
                if (func) {
                    func.apply(null, [action, arg]);
                }
            };
            Toolbar.prototype._renderModeClick = function (action) {
                var view = this._viewer.getView();
                switch (action) {
                    case "toolbar-shaded":
                        view.setDrawMode(Communicator.DrawMode.Shaded);
                        break;
                    case "toolbar-wireframe":
                        view.setDrawMode(Communicator.DrawMode.Wireframe);
                        break;
                    case "toolbar-hidden-line":
                        view.setDrawMode(Communicator.DrawMode.HiddenLine);
                        break;
                    default:
                        view.setDrawMode(Communicator.DrawMode.WireframeOnShaded);
                }
                ;
            };
            Toolbar.prototype._initSnapshot = function () {
                $("#snapshot-dialog-cancel-button").button().click(function () {
                    $("#snapshot-dialog").hide();
                });
            };
            Toolbar.prototype._doSnapshot = function () {
                var $screen = $("#content");
                var windowWidth = $screen.width();
                var windowHeight = $screen.height();
                var canvasSize = this._viewer.getView().getCanvasSize();
                var percentageOfWindow = .7;
                var windowAspect = canvasSize.x / canvasSize.y;
                var renderHeight = 480;
                var renderWidth = windowAspect * renderHeight;
                if (percentageOfWindow > 0) {
                    var renderHeight = windowHeight * percentageOfWindow;
                    var renderWidth = windowWidth * percentageOfWindow;
                }
                var dialogWidth = renderWidth + 40;
                var snapShot = new Communicator.Ui.SnapShot();
                var promise = snapShot.start(this._viewer, canvasSize.x, canvasSize.y)
                    .then(function (image) {
                    var xpos = parseInt((windowWidth - renderWidth) / 2);
                    var $dialog = $("#snapshot-dialog");
                    var overlay=$( "#waterMarkContainer").clone();
                    $("#snapshot-dialog-image").attr("src", image.src).attr("width", renderWidth).attr("height", renderHeight);
                    $dialog.css({
                        top: "45px",
                        left: xpos.toString() + "px",
                    });
                    var divToPrint=document.getElementById('snapshot-dialog');
                    overlay.css({'height': renderHeight+"px",
                                'position':'fixed',
                                'z-index':'1000',
                                'clip':'rect(0px,'+renderWidth+'px,'+renderHeight+'px,0px)'
                    });
                    $("#snapshot-dialog").prepend(overlay);	
                    handleError('Please close the print window to proceed.');
                    $('#pageWrapper').block({message: '',overlayCSS: { backgroundColor: '#fff' }});
                    var newWin=window.open('','Print-Window','width=1000,height=1000');
                    newWin.document.open();
                    newWin.document.write('<html><head><meta name="format-detection" content="date=no"><meta name="format-detection" content="address=no"><meta name="format-detection" content="telephone=no"></head><body onload="window.print()">'+divToPrint.innerHTML+'</body></html>');
                    newWin.document.close();
                    overlay.remove();
                    setTimeout(function(){
                        newWin.close();
                        closeDialog();
                        overlay.remove();
                        $('#pageWrapper').unblock();
                    },1000);
                });
            };
            Toolbar.prototype._setRedlineOperator = function (operatorId) {
                if (this._viewer.getMarkupManager().getActiveMarkupView() == null) {
                    this._viewer.getMarkupManager().createMarkupView();
                }
                this._viewer.getOperatorManager().set(operatorId, 1);
            };
            Toolbar.prototype._initActions = function () {
                var _this = this;
                this._actions["toolbar-home"] = function () {
                    if (_this._viewer.getModel().isDrawing()) {
                        var sheetId = _this._viewer.getActiveSheetId();
                        if (sheetId !== null) {
                            _this._viewer.getView().isolateNodes([sheetId]);
                        }
                    }
                    else {
                        _this._viewer.reset();
                        Communicator.Markup.NoteText.setIsolateActive(false);
                        Communicator.Markup.NoteText.updatePinVisibility();
                        var handleOperator = _this._viewer.getOperatorManager().getOperator(Communicator.OperatorId.Handle);
                        if (handleOperator !== null && handleOperator.removeHandles) {
                            handleOperator.removeHandles();
                        }
                    }
                };
                this._actions["toolbar-redline-circle"] = function () { _this._setRedlineOperator(Communicator.OperatorId.RedlineCircle); };
                this._actions["toolbar-redline-freehand"] = function () { _this._setRedlineOperator(Communicator.OperatorId.RedlinePolyline); };
                this._actions["toolbar-redline-rectangle"] = function () { _this._setRedlineOperator(Communicator.OperatorId.RedlineRectangle); };
                this._actions["toolbar-redline-note"] = function () { _this._setRedlineOperator(Communicator.OperatorId.RedlineText); };
                this._actions["toolbar-note"] = function () { _this._viewer.getOperatorManager().set(Communicator.OperatorId.Note, 1); };
                this._actions["toolbar-select"] = function () { _this._viewer.getOperatorManager().set(Communicator.OperatorId.Select, 1); };
                this._actions["toolbar-area-select"] = function () { _this._viewer.getOperatorManager().set(Communicator.OperatorId.AreaSelect, 1); };
                this._actions["toolbar-orbit"] = function () { _this._viewer.getOperatorManager().set(Communicator.OperatorId.Navigate, 0); };
                this._actions["toolbar-turntable"] = function () { _this._viewer.getOperatorManager().set(Communicator.OperatorId.Turntable, 0); };
                this._actions["toolbar-walk"] = function () { _this._viewer.getOperatorManager().set(Communicator.OperatorId.Walk, 0); };
                this._actions["toolbar-face"] = function () { _this._orientToFace(); };
                this._actions["toolbar-measure-point"] = function () { _this._viewer.getOperatorManager().set(Communicator.OperatorId.MeasurePointPointDistance, 1); };
                this._actions["toolbar-measure-edge"] = function () { _this._viewer.getOperatorManager().set(Communicator.OperatorId.MeasureEdgeLength, 1); };
                this._actions["toolbar-measure-distance"] = function () { _this._viewer.getOperatorManager().set(Communicator.OperatorId.MeasureFaceFaceDistance, 1); };
                this._actions["toolbar-measure-angle"] = function () { _this._viewer.getOperatorManager().set(Communicator.OperatorId.MeasureFaceFaceAngle, 1); };
                this._actions["toolbar-cuttingplane"] = function (action, visibility) { };
                this._actions["toolbar-explode"] = function (action, visibility) { _this._explosionButtonClick(visibility); };
                this._actions["toolbar-settings"] = function (action, visibility) { _this._settingsButtonClick(); };
                var _renderModeClick = function (action) { _this._renderModeClick(action); };
                this._actions["toolbar-wireframeshaded"] = _renderModeClick;
                this._actions["toolbar-shaded"] = _renderModeClick;
                this._actions["toolbar-wireframe"] = _renderModeClick;
                this._actions["toolbar-hidden-line"] = _renderModeClick;
                this._actions["toolbar-front"] = function () { _this._viewer.getView().setViewOrientation(Communicator.ViewOrientation.Front, _this._viewOrientationDuration); };
                this._actions["toolbar-back"] = function () { _this._viewer.getView().setViewOrientation(Communicator.ViewOrientation.Back, _this._viewOrientationDuration); };
                this._actions["toolbar-left"] = function () { _this._viewer.getView().setViewOrientation(Communicator.ViewOrientation.Left, _this._viewOrientationDuration); };
                this._actions["toolbar-right"] = function () { _this._viewer.getView().setViewOrientation(Communicator.ViewOrientation.Right, _this._viewOrientationDuration); };
                this._actions["toolbar-bottom"] = function () { _this._viewer.getView().setViewOrientation(Communicator.ViewOrientation.Bottom, _this._viewOrientationDuration); };
                this._actions["toolbar-top"] = function () { _this._viewer.getView().setViewOrientation(Communicator.ViewOrientation.Top, _this._viewOrientationDuration); };
                this._actions["toolbar-iso"] = function () { _this._viewer.getView().setViewOrientation(Communicator.ViewOrientation.Iso, _this._viewOrientationDuration); };
                this._actions["toolbar-ortho"] = function () { _this._viewer.getView().setProjectionMode(Communicator.Projection.Orthographic); };
                this._actions["toolbar-persp"] = function () { _this._viewer.getView().setProjectionMode(Communicator.Projection.Perspective); };
                this._actions["toolbar-snapshot"] = function () { _this._doSnapshot(); };
            };
            Toolbar.prototype._onExplosionSilder = function (value) {
                this._viewer.getExplodeManager().setMagnitude(value);
            };
            Toolbar.prototype._explosionButtonClick = function (visibility) {
                var explodeManager = this._viewer.getExplodeManager();
                if (visibility && !explodeManager.getActive()) {
                    explodeManager.start();
                }
            };
            Toolbar.prototype._settingsButtonClick = function () {
                this._viewerSettings.show();
            };
            Toolbar.prototype.updateEdgeFaceButton = function () {
                var view = this._viewer.getView();
                var edgeVisibility = view.getLineVisibility();
                var faceVisibility = view.getFaceVisibility();
                if (edgeVisibility && faceVisibility)
                    this.setCorrespondingButtonForSubmenuItem("wireframeshaded");
                else if (!edgeVisibility && faceVisibility)
                    this.setCorrespondingButtonForSubmenuItem("shaded");
                else
                    this.setCorrespondingButtonForSubmenuItem("wireframe");
            };
            Toolbar.prototype._cuttingPlaneButtonClick = function (event) {
                var $element = $(event.target).closest(".toolbar-cp-plane");
                var planeAction = $element.data("plane");
                var initialCount = this._cuttingPlaneController.getCount();
                if (planeAction == "x" || planeAction == "y" || planeAction == "z" || planeAction == "face") {
                    this._cuttingPlaneController.toggle(planeAction);
                    $element.removeClass("selected");
                    $element.removeClass("inverted");
                    var count = this._cuttingPlaneController.getCount();
                    if (count > initialCount) {
                        $element.addClass("selected");
                    }
                    else if (count == initialCount && count > 0 && planeAction != "face") {
                        $element.addClass("inverted");
                    }
                    if (count > 0) {
                        $("#cuttingplane-section").removeClass("disabled");
                        $("#cuttingplane-reset").removeClass("disabled");
                    }
                    else {
                        $("#cuttingplane-section").addClass("disabled");
                        $("#cuttingplane-reset").addClass("disabled");
                    }
                    if (count > 1) {
                        $("#cuttingplane-toggle").removeClass("disabled");
                    }
                    else {
                        $("#cuttingplane-toggle").addClass("disabled");
                    }
                }
                else if (planeAction == "section") {
                    this._cuttingPlaneController.toggleReferenceGeometry();
                    if ($element.hasClass("selected")) {
                        $element.removeClass("selected");
                    }
                    else {
                        $element.addClass("selected");
                    }
                }
                else if (planeAction == "toggle") {
                    this._cuttingPlaneController.toggleCuttingMode();
                    if ($element.hasClass("selected")) {
                        $element.removeClass("selected");
                    }
                    else {
                        $element.addClass("selected");
                    }
                }
                else if (planeAction == "reset") {
                    this._cuttingPlaneController.resetCuttingPlanes();
                    $("#cuttingplane-reset").addClass("disabled");
                    $("#cuttingplane-section").addClass("disabled");
                    $("#cuttingplane-section").removeClass("selected");
                    $("#cuttingplane-toggle").addClass("disabled");
                    $("#cuttingplane-toggle").removeClass("selected");
                    $("#cuttingplane-x").removeClass("selected");
                    $("#cuttingplane-y").removeClass("selected");
                    $("#cuttingplane-z").removeClass("selected");
                    $("#cuttingplane-face").removeClass("selected");
                    $("#cuttingplane-x").removeClass("inverted");
                    $("#cuttingplane-y").removeClass("inverted");
                    $("#cuttingplane-z").removeClass("inverted");
                    $("#cuttingplane-face").removeClass("inverted");
                }
            };
            ;
            Toolbar.prototype._orientToFace = function () {
                var selectionItem = this._viewer.getSelectionManager().getLast();
                if (selectionItem && selectionItem.getFaceEntity()) {
                    var normal = selectionItem.getFaceEntity().getNormal();
                    var position = selectionItem.getPosition();
                    var camera = this._viewer.getView().getCamera();
                    var up = Communicator.Point3.cross(normal, new Communicator.Point3(0, 1, 0));
                    if (up.length() < .001) {
                        up = Communicator.Point3.cross(normal, new Communicator.Point3(1, 0, 0));
                    }
                    var zoomDelta = camera.getPosition().subtract(camera.getTarget()).length();
                    camera.setTarget(position);
                    camera.setPosition(Communicator.Point3.add(position, Communicator.Point3.scale(normal, zoomDelta)));
                    camera.setUp(up);
                    this._viewer.getView().fitBounding(selectionItem.getFaceEntity().getBounding(), 400, camera);
                }
            };
            Toolbar.prototype._onPartSelection = function (event) {
                if (event.getSelection().getNodeId() == null) {
                    $("#cuttingplane-face").addClass("disabled");
                    $("#view-face").addClass("disabled");
                }
                else {
                    $("#cuttingplane-face").removeClass("disabled");
                    $("#view-face").removeClass("disabled");
                }
            };
            return Toolbar;
        }());
        Ui.Toolbar = Toolbar;
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        var Desktop;
        (function (Desktop) {
            var ViewerSettings = (function () {
                function ViewerSettings(viewer, axisTriad, navCube) {
                    this._versionInfo = true;
                    this._backgroundColorTop = null;
                    this._backgroundColorBottom = null;
                    this._measurementColor = null;
                    this._selectionColorBody = null;
                    this._selectionColorFaceLine = null;
                    this._enableFaceLineSelection = null;
                    this._cappingGeometryFaceColor = null;
                    this._cappingGeometryLineColor = null;
                    this._PMIColor = Communicator.Color.black();
                    this._PMIEnabled = false;
                    this._projectionMode = null;
                    this._showBackfaces = null;
                    this._minFramerate = null;
                    this._hiddenLineOpacity = null;
                    this._showCappingGeometry = null;
                    this._orbitCameraTarget = false;
                    this._ambientOcclusionEnabled = null;
                    this._ambientOcclusionRadius = null;
                    this._antiAliasingEnabled = null;
                    this._axisTriadEnabled = true;
                    this._navCubeEnabled = true;
                    this._splatRenderingEnabled = false;
                    this._splatRenderingSize = .003;
                    this._splatRenderingPointSizeUnit = Communicator.PointSizeUnit.ProportionOfBoundingDiagonal;
                    this._eyeDomeLightingEnabled = false;
                    this._viewer = viewer;
                    this._axisTriad = axisTriad;
                    this._navCube = navCube;
                    this._initElements();
                }
                ViewerSettings.prototype.show = function () {
                    this._readSettings();
                    this._centerWindow();
                    $(this._viewerSettingsSelector).show();
                };
                ViewerSettings.prototype.hide = function () {
                    $(this._viewerSettingsSelector).hide();
                };
                ViewerSettings.prototype._centerWindow = function () {
                    var $settingsDialog = $(this._viewerSettingsSelector);
                    var width = $settingsDialog.width() + 1;
                    var height = $settingsDialog.height();
                    var canvasSize = this._viewer.getView().getCanvasSize();
                    var leftPos = (canvasSize.x - width) / 2;
                    var topPos = (canvasSize.y - height) / 2;
                    $settingsDialog.css({
                        top: topPos.toString() + "px",
                        left: leftPos.toString() + "px"
                    });
                };
                ViewerSettings.prototype._initElements = function () {
                    var self = this;
                    this._viewerSettingsSelector = "#viewer-settings-dialog";
                    $(this._viewerSettingsSelector).draggable({
                        handle: ".hoops-ui-window-header"
                    });
                    $("INPUT.color-picker").each(function () {
                        $(this).minicolors({
                            position: $(this).attr('data-position') || 'bottom left',
                            format: "rgb",
                            control: "hue"
                        });
                    });
                    $("#viewer-settings-ok-button").click(function () {
                        self._applySettings();
                        self.hide();
                    });
                    $("#viewer-settings-cancel-button").click(function () {
                        self.hide();
                    });
                    $("#viewer-settings-apply-button").click(function () {
                        self._applySettings();
                    });
                    $("#settings-pmi-enabled").click(function () {
                        if ($("#settings-pmi-enabled").prop("checked")) {
                            $("#settings-pmi-color").prop("disabled", false);
                            $("#settings-pmi-color-style").removeClass("grayed-out");
                        }
                        else {
                            $("#settings-pmi-color").prop("disabled", true);
                            $("#settings-pmi-color-style").addClass("grayed-out");
                        }
                    });
                    $("#settings-enable-face-line-selection").click(function () {
                        if ($("#settings-enable-face-line-selection").prop("checked")) {
                            $("#settings-selection-color-face-line").prop("disabled", false);
                            $("#settings-selection-color-face-line-style").removeClass("grayed-out");
                        }
                        else {
                            $("#settings-selection-color-face-line").prop("disabled", true);
                            $("#settings-selection-color-face-line-style").addClass("grayed-out");
                        }
                    });
                    $("#settings-splat-rendering-enabled").click(function () {
                        if ($("#settings-splat-rendering-enabled").prop("checked")) {
                            $("#settings-splat-enabled-style").removeClass("grayed-out");
                            $("#settings-splat-rendering-size").prop("disabled", false);
                            $("#settings-splat-rendering-point-size-unit").prop("disabled", false);
                        }
                        else {
                            $("#settings-splat-enabled-style").addClass("grayed-out");
                            $("#settings-splat-rendering-size").prop("disabled", true);
                            $("#settings-splat-rendering-point-size-unit").prop("disabled", true);
                        }
                    });
                };
                ViewerSettings.prototype._readSettings = function () {
                    var _this = this;
                    var view = this._viewer.getView();
                    var model = this._viewer.getModel();
                    var selectionManager = this._viewer.getSelectionManager();
                    var cuttingManager = this._viewer.getCuttingManager();
                    var measureManager = this._viewer.getMeasureManager();
                    var operatorManager = this._viewer.getOperatorManager();
                    if (this._versionInfo) {
                        $("#settings-format-version").html(this._viewer.getFormatVersionString());
                        $("#settings-viewer-version").html(this._viewer.getViewerVersionString());
                        this._versionInfo = false;
                    }
                    var backgroundColor = view.getBackgroundColor();
                    if (backgroundColor.top === null) {
                        this._backgroundColorTop = this._colorFromRgbString("rgb(192,220,248)");
                    }
                    else {
                        this._backgroundColorTop = backgroundColor.top;
                    }
                    if (backgroundColor.bottom === null) {
                        this._backgroundColorBottom = this._colorFromRgbString("rgb(192,220,248)");
                    }
                    else {
                        this._backgroundColorBottom = backgroundColor.bottom;
                    }
                    this._selectionColorBody = selectionManager.getNodeSelectionColor();
                    this._selectionColorFaceLine = selectionManager.getNodeElementSelectionColor();
                    this._measurementColor = measureManager.getMeasurementColor();
                    this._projectionMode = view.getProjectionMode();
                    this._showBackfaces = view.getBackfacesVisible();
                    this._hiddenLineOpacity = view.getHiddenLineSettings().getObscuredLineTransparency();
                    this._showCappingGeometry = cuttingManager.getCappingGeometryVisibility();
                    this._enableFaceLineSelection = selectionManager.getHighlightFaceElementSelection() && selectionManager.getHighlightLineElementSelection();
                    this._cappingGeometryFaceColor = cuttingManager.getCappingFaceColor();
                    this._cappingGeometryLineColor = cuttingManager.getCappingLineColor();
                    this._ambientOcclusionEnabled = view.getAmbientOcclusionEnabled();
                    this._ambientOcclusionRadius = view.getAmbientOcclusionRadius();
                    this._antiAliasingEnabled = view.getAntiAliasingMode() == Communicator.AntiAliasingMode.SMAA;
                    this._PMIColor = model.getPMIColor();
                    this._PMIEnabled = model.getPMIColorOverride();
                    var orbitOperator = operatorManager.getOperator(Communicator.OperatorId.Orbit);
                    this._orbitCameraTarget = orbitOperator.getOrbitFallbackMode() == Communicator.OrbitFallbackMode.CameraTarget ? true : false;
                    this._axisTriadEnabled = this._axisTriad.getEnabled();
                    this._navCubeEnabled = this._navCube.getEnabled();
                    $("#settings-selection-color-body").minicolors("value", this._rgbStringFromColor(this._selectionColorBody));
                    $("#settings-selection-color-face-line").minicolors("value", this._rgbStringFromColor(this._selectionColorFaceLine));
                    $("#settings-background-top").minicolors("value", this._rgbStringFromColor(this._backgroundColorTop));
                    $("#settings-background-bottom").minicolors("value", this._rgbStringFromColor(this._backgroundColorBottom));
                    $("#settings-measurement-color").minicolors("value", this._rgbStringFromColor(this._measurementColor));
                    $("#settings-capping-face-color").minicolors("value", this._rgbStringFromColor(this._cappingGeometryFaceColor));
                    $("#settings-capping-line-color").minicolors("value", this._rgbStringFromColor(this._cappingGeometryLineColor));
                    $("#settings-projection-mode").val(this._projectionMode + '');
                    $("#settings-show-backfaces").prop("checked", this._showBackfaces);
                    $("#settings-show-capping-geometry").prop("checked", this._showCappingGeometry);
                    $("#settings-enable-face-line-selection").prop("checked", this._enableFaceLineSelection);
                    $("#settings-orbit-mode").prop("checked", this._orbitCameraTarget);
                    $("#settings-ambient-occlusion").prop("checked", this._ambientOcclusionEnabled);
                    $("#settings-ambient-occlusion-radius").val(this._ambientOcclusionRadius + '');
                    $("#settings-anti-aliasing").prop("checked", this._antiAliasingEnabled);
                    $("#settings-axis-triad").prop("checked", this._axisTriadEnabled);
                    $("#settings-nav-cube").prop("checked", this._navCubeEnabled);
                    $("#settings-pmi-color").minicolors("value", this._rgbStringFromColor(this._PMIColor));
                    if (this._PMIEnabled != $("#settings-pmi-enabled").prop("checked")) {
                        $("#settings-pmi-enabled").click();
                    }
                    this._viewer.getMinimumFramerate().then(function (minFramerate) {
                        _this._minFramerate = minFramerate;
                        $("#settings-framerate").val(_this._minFramerate + '');
                    });
                    if (this._hiddenLineOpacity != null) {
                        $("#settings-hidden-line-opacity").val(this._hiddenLineOpacity + '');
                    }
                    else {
                        $("#settings-hidden-line-opacity").val("");
                    }
                    view.getPointSize().then(function (value) {
                        var splatRenderingSize = value[0];
                        var splatRenderingPointSizeUnit = value[1];
                        _this._splatRenderingEnabled = splatRenderingSize !== 1 || splatRenderingPointSizeUnit !== Communicator.PointSizeUnit.ScreenPixels;
                        if (_this._splatRenderingEnabled != $("#settings-splat-rendering-enabled").prop("checked")) {
                            $("#settings-splat-rendering-enabled").click();
                        }
                        if (_this._splatRenderingEnabled) {
                            _this._splatRenderingSize = splatRenderingSize;
                            _this._splatRenderingPointSizeUnit = splatRenderingPointSizeUnit;
                        }
                        $("#settings-splat-rendering-size").val(_this._splatRenderingSize + '');
                        $("#settings-splat-rendering-point-size-unit").val(_this._splatRenderingPointSizeUnit + '');
                    });
                    view.getEyeDomeLightingEnabled().then(function (enabled) {
                        _this._eyeDomeLightingEnabled = enabled;
                        $("#settings-eye-dome-lighting-enabled").prop("checked", _this._eyeDomeLightingEnabled);
                    });
                };
                ViewerSettings.prototype._applySettings = function () {
                    var view = this._viewer.getView();
                    var backgroundTop = this._colorFromRgbString($("#settings-background-top").val());
                    var backgroundBottom = this._colorFromRgbString($("#settings-background-bottom").val());
                    this._viewer.getView().setBackgroundColor(backgroundTop, backgroundBottom);
                    var selectionColorBody = this._colorFromRgbString($("#settings-selection-color-body").val());
                    this._viewer.getSelectionManager().setNodeSelectionColor(selectionColorBody);
                    this._viewer.getSelectionManager().setNodeSelectionOutlineColor(selectionColorBody);
                    var selectionColorFaceLine = this._colorFromRgbString($("#settings-selection-color-face-line").val());
                    this._viewer.getSelectionManager().setNodeElementSelectionColor(selectionColorFaceLine);
                    this._viewer.getSelectionManager().setNodeElementSelectionOutlineColor(selectionColorFaceLine);
                    var enableFaceLineSelection = $("#settings-enable-face-line-selection").prop("checked");
                    this._viewer.getSelectionManager().setHighlightFaceElementSelection(enableFaceLineSelection);
                    this._viewer.getSelectionManager().setHighlightLineElementSelection(enableFaceLineSelection);
                    this._viewer.getMeasureManager().setMeasurementColor(this._colorFromRgbString($("#settings-measurement-color").val()));
                    this._PMIColor = this._colorFromRgbString($("#settings-pmi-color").val());
                    this._PMIEnabled = $("#settings-pmi-enabled").prop("checked");
                    if (this._PMIColor && this._PMIEnabled) {
                        this._viewer.getModel().setPMIColor(this._PMIColor);
                        this._viewer.getModel().setPMIColorOverride(true);
                    }
                    else {
                        this._viewer.getModel().setPMIColorOverride(false);
                    }
                    this._viewer.getCuttingManager().setCappingFaceColor(this._colorFromRgbString($("#settings-capping-face-color").val()));
                    this._viewer.getCuttingManager().setCappingLineColor(this._colorFromRgbString($("#settings-capping-line-color").val()));
                    this._viewer.getView().setProjectionMode(parseInt($("#settings-projection-mode").val()));
                    var showBackfaces = $("#settings-show-backfaces").prop("checked");
                    this._viewer.getView().setBackfacesVisible(showBackfaces);
                    var showCappingGeometry = $("#settings-show-capping-geometry").prop("checked");
                    this._viewer.getCuttingManager().setCappingGeometryVisibility(showCappingGeometry);
                    var minFramerate = parseInt($("#settings-framerate").val());
                    if (minFramerate && minFramerate > 0) {
                        this._minFramerate = minFramerate;
                        this._viewer.setMinimumFramerate(this._minFramerate);
                    }
                    var hiddenLineOpacity = parseFloat($("#settings-hidden-line-opacity").val());
                    this._viewer.getView().getHiddenLineSettings().setObscuredLineTransparency(hiddenLineOpacity);
                    if (this._viewer.getView().getDrawMode() == Communicator.DrawMode.HiddenLine) {
                        this._viewer.getView().setDrawMode(Communicator.DrawMode.HiddenLine);
                    }
                    var orbitOperator = this._viewer.getOperatorManager().getOperator(Communicator.OperatorId.Orbit);
                    var orbitCameraTarget = $("#settings-orbit-mode").prop("checked");
                    orbitOperator.setOrbitFallbackMode(orbitCameraTarget ? Communicator.OrbitFallbackMode.CameraTarget : Communicator.OrbitFallbackMode.ModelCenter);
                    this._viewer.getView().setAmbientOcclusionEnabled($("#settings-ambient-occlusion").prop("checked"));
                    this._viewer.getView().setAmbientOcclusionRadius(parseFloat($("#settings-ambient-occlusion-radius").val()));
                    if ($("#settings-anti-aliasing").prop("checked"))
                        this._viewer.getView().setAntiAliasingMode(Communicator.AntiAliasingMode.SMAA);
                    else
                        this._viewer.getView().setAntiAliasingMode(Communicator.AntiAliasingMode.None);
                    /*if ($("#settings-axis-triad").prop("checked"))
                        this._axisTriad.enable();
                    else
                        this._axisTriad.disable();
                    if ($("#settings-nav-cube").prop("checked"))
                        this._navCube.enable();
                    else
                        this._navCube.disable();*/
                    if ($("#settings-splat-rendering-enabled").prop("checked")) {
                        this._splatRenderingEnabled = true;
                        this._splatRenderingSize = parseFloat($("#settings-splat-rendering-size").val());
                        this._splatRenderingPointSizeUnit = parseInt($("#settings-splat-rendering-point-size-unit").val());
                        view.setPointSize(this._splatRenderingSize, this._splatRenderingPointSizeUnit);
                    }
                    else {
                        this._splatRenderingEnabled = false;
                        view.setPointSize(1, Communicator.PointSizeUnit.ScreenPixels);
                    }
                    view.setEyeDomeLightingEnabled($("#settings-eye-dome-lighting-enabled").prop("checked"));
                };
                ViewerSettings.prototype._colorFromRgbString = function (rgb) {
                    rgb = rgb.replace(/[^\d,]/g, '').split(',');
                    return new Communicator.Color(rgb[0], rgb[1], rgb[2]);
                };
                ViewerSettings.prototype._rgbStringFromColor = function (color) {
                    if (color == null) {
                        return "";
                    }
                    return "rgb(" + color.r + ',' + color.g + ',' + color.b + ')';
                };
                return ViewerSettings;
            }());
            Desktop.ViewerSettings = ViewerSettings;
        })(Desktop = Ui.Desktop || (Ui.Desktop = {}));
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        var Desktop;
        (function (Desktop) {
            var ModelBrowser = (function () {
                function ModelBrowser(elementId, containerId, viewer, isolateZoomHelper, cuttingPlaneController, screenConfiguration) {
                    this._browserWindowMargin = 3;
                    this._elementId = elementId;
                    this._containerId = containerId;
                    this._viewer = viewer;
                    this._isolateZoomHelper = isolateZoomHelper;
                    this._cuttingPlaneController = cuttingPlaneController;
                    this._screenConfiguration = screenConfiguration;
                    this._canvasSize = this._viewer.getView().getCanvasSize();
                    this._initElements();
                    this._initEvents();
                }
                ModelBrowser.prototype._initEvents = function () {
                    var _this = this;
                    this._viewer.setCallbacks({
                        modelStructureLoadBegin: function () {
                            _this._onModelStructureLoadBegin();
                        },
                        modelStructureParseBegin: function () {
                            _this._onModelStructureParsingBegin();
                        },
                        modelStructureReady: function () {
                            _this._onModelStructureReady();
                        },
                        modelSwitched: function () {
                            _this._showModelTree();
                        },
                        frameDrawn: function () {
                            _this._canvasSize = _this._viewer.getView().getCanvasSize();
                            _this.onResize(_this._canvasSize.x, _this._canvasSize.y);
                        }
                    });
                    this._modelTree.registerCallback("expand", function () {
                        _this._refreshBrowserScroll();
                    });
                    this._modelTree.registerCallback("collapse", function () {
                        _this._refreshBrowserScroll();
                    });
                    this._modelTree.registerCallback("addChild", function () {
                        _this._refreshBrowserScroll();
                    });
                    $("#contextMenuButton").on("click", function (event) {
                        var position = new Communicator.Point2(event.clientX, event.clientY);
                        _this._viewer.triggerEvent("contextMenu", [position]);
                    });
                };
                ModelBrowser.prototype._refreshBrowserScroll = function () {
                    var _this = this;
                    window.clearTimeout(this._scrollRefreshTimeout);
                    this._scrollRefreshTimeout = window.setTimeout(function () {
                        if (_this._modelTreeScroll) {
                            _this._modelTreeScroll.refresh();
                        }
                        if (_this._cadViewTreeScroll) {
                            _this._cadViewTreeScroll.refresh();
                        }
                        if (_this._sheetsTreeScroll) {
                            _this._sheetsTreeScroll.refresh();
                        }
                    }, 300);
                };
                ModelBrowser.prototype._showCadViewTree = function () {
                    var $sheetsDiv = $("#sheetsDivScrollContainer");
                    var $browserDivContainer = $("#browserDivScrollContainer");
                    var $cadViewDivScrollContainer = $("#cadViewDivScrollContainer");
                    $sheetsDiv.hide();
                    $browserDivContainer.hide();
                    $cadViewDivScrollContainer.show();
                    var browserTab = document.getElementById("modelBrowserTab");
                    var cadViewTab = document.getElementById("cadViewTab");
                    var sheetsTab = document.getElementById("sheetsTab");
                    if (sheetsTab) {
                        sheetsTab.classList.remove('browser-tab-selected');
                    }
                    if (browserTab) {
                        browserTab.classList.remove('browser-tab-selected');
                    }
                    if (cadViewTab) {
                        cadViewTab.classList.add('browser-tab-selected');
                    }
                    this._refreshBrowserScroll();
                };
                ModelBrowser.prototype._showModelTree = function () {
                    var $sheetsDiv = $("#sheetsDivScrollContainer");
                    var $browserDivContainer = $("#browserDivScrollContainer");
                    var $cadViewDivScrollContainer = $("#cadViewDivScrollContainer");
                    $sheetsDiv.hide();
                    $browserDivContainer.show();
                    $cadViewDivScrollContainer.hide();
                    var browserTab = document.getElementById("modelBrowserTab");
                    var cadViewTab = document.getElementById("cadViewTab");
                    var sheetsTab = document.getElementById("sheetsTab");
                    if (sheetsTab) {
                        sheetsTab.classList.remove('browser-tab-selected');
                    }
                    if (browserTab) {
                        browserTab.classList.add('browser-tab-selected');
                    }
                    if (cadViewTab) {
                        cadViewTab.classList.remove('browser-tab-selected');
                    }
                    this._refreshBrowserScroll();
                };
                ModelBrowser.prototype._showSheetsTab = function () {
                    var $sheetsDiv = $("#sheetsDivScrollContainer");
                    var $browserDivContainer = $("#browserDivScrollContainer");
                    var $cadViewDivScrollContainer = $("#cadViewDivScrollContainer");
                    $sheetsDiv.show();
                    $browserDivContainer.hide();
                    $cadViewDivScrollContainer.hide();
                    var browserTab = document.getElementById("modelBrowserTab");
                    var cadViewTab = document.getElementById("cadViewTab");
                    var sheetsTab = document.getElementById("sheetsTab");
                    if (sheetsTab) {
                        sheetsTab.classList.add('browser-tab-selected');
                    }
                    if (browserTab) {
                        browserTab.classList.remove('browser-tab-selected');
                    }
                    if (cadViewTab) {
                        cadViewTab.classList.remove('browser-tab-selected');
                    }
                    this._refreshBrowserScroll();
                };
                ModelBrowser.prototype._initElements = function () {
                    var _this = this;
                    this._browserWindow = document.getElementById(this._elementId);
                    $(this._browserWindow).bind("touchmove", function (event) {
                        event.originalEvent.preventDefault();
                    });
                    this._browserWindow.classList.add("ui-modelbrowser-window");
                    this._browserWindow.classList.add("desktop-ui-window");
                    this._browserWindow.classList.add("ui-modelbrowser-small");
                    this._browserWindow.style.position = "absolute";
                    this._browserWindow.style.width = Math.max(($(window).width() / 4), 400) + "px";
                    this._browserWindow.style.top = "90px";
                    this._browserWindow.style.left = "10px";
                    this._header = document.createElement("div");
                    this._header.classList.add("ui-modelbrowser-header");
                    this._header.classList.add("desktop-ui-window-header");
                    var t = document.createElement("table");
                    var tr = document.createElement("tr");
                    t.appendChild(tr);
                    var minimizetd = document.createElement("td");
                    minimizetd.classList.add("ui-modelbrowser-minimizetd");
                    this._minimizeButton = document.createElement("div");
                    this._minimizeButton.classList.add("ui-modelbrowser-minimizebutton");
                    this._minimizeButton.classList.add("minimized");
                    this._minimizeButton.onclick = function () {
                        _this._onMinimizeButtonClick();
                    };
                    minimizetd.appendChild(this._minimizeButton);
                    tr.appendChild(minimizetd);
                    var modelBrowserLabel = document.createElement("td");
                    modelBrowserLabel.id = "modelBrowserLabel";
                    modelBrowserLabel.innerHTML = "";
                    tr.appendChild(modelBrowserLabel);
                    var menuNode = document.createElement("div");
                    menuNode.classList.add("ui-modeltree-icon");
                    menuNode.classList.add("menu");
                    menuNode.id = "contextMenuButton";
                    tr.appendChild(menuNode);
                    this._header.appendChild(t);
                    this._content = document.createElement("div");
                    this._content.id = "modelTreeContainer";
                    this._content.classList.add("ui-modelbrowser-content");
                    this._content.classList.add("desktop-ui-window-content");
                    this._content.style.overflow = "auto";
                    var loadingDiv = document.createElement("div");
                    loadingDiv.id = "modelBrowserLoadingDiv";
                    loadingDiv.innerHTML = "Loading...";
                    this._content.appendChild(loadingDiv);
                    var browserDiv = document.createElement("div");
                    browserDiv.id = "modelTree";
                    var browserDivScrollContainer = document.createElement("div");
                    browserDivScrollContainer.id = "browserDivScrollContainer";
                    browserDivScrollContainer.appendChild(browserDiv);
                    this._content.appendChild(browserDivScrollContainer);
                    var cadViewDiv = document.createElement("div");
                    cadViewDiv.id = "cadViewTree";
                    var cadViewDivScrollContainer = document.createElement("div");
                    cadViewDivScrollContainer.id = "cadViewDivScrollContainer";
                    cadViewDivScrollContainer.appendChild(cadViewDiv);
                    this._content.appendChild(cadViewDivScrollContainer);
                    var sheetsDiv = document.createElement("div");
                    sheetsDiv.id = "sheetsTree";
                    var sheetsDivScrollContainer = document.createElement("div");
                    sheetsDivScrollContainer.id = "sheetsScrollContainer";
                    sheetsDivScrollContainer.appendChild(sheetsDiv);
                    this._content.appendChild(sheetsDivScrollContainer);
                    this._modelBrowserTabs = document.createElement("div");
                    this._modelBrowserTabs.id = "modelBrowserTabs";
                    var modelBrowserTab = document.createElement('label');
                    modelBrowserTab.id = "modelBrowserTab";
                    modelBrowserTab.textContent = "Model Tree";
                    modelBrowserTab.classList.add("ui-modelbrowser-tab");
                    modelBrowserTab.classList.add("browser-tab-selected");
                    modelBrowserTab.onclick = function (event) {
                        _this._showModelTree();
                    };
					/*
                    var cadViewTab = document.createElement("label");
                    cadViewTab.id = "cadViewTab";
                    cadViewTab.textContent = "Views, etc.";
                    cadViewTab.classList.add("ui-modelbrowser-tab");
                    cadViewTab.onclick = function (event) {
                        _this._showCadViewTree();
                    };
                    var sheetsTab = document.createElement("label");
                    sheetsTab.id = "sheetsTab";
                    sheetsTab.textContent = "Sheets";
                    sheetsTab.classList.add("ui-modelbrowser-tab");
                    sheetsTab.onclick = function (event) {
                        _this._showSheetsTab();
                    };*/
                    this._modelBrowserTabs.appendChild(modelBrowserTab);
                    //this._modelBrowserTabs.appendChild(cadViewTab);
                    //this._modelBrowserTabs.appendChild(sheetsTab);
                    this._header.appendChild(this._modelBrowserTabs);
                    this._browserWindow.appendChild(this._header);
                    this._propertyWindow = document.createElement("div");
                    this._propertyWindow.classList.add("propertyWindow");
                    this._propertyWindow.id = "propertyWindow";
                    var container = document.createElement("div");
                    container.id = "propertyContainer";
                    this._propertyWindow.appendChild(container);
                    this._treePropertyContainer = document.createElement("div");
                    this._treePropertyContainer.id = "treePropertyContainer";
                    this._treePropertyContainer.appendChild(this._content);
                    this._treePropertyContainer.appendChild(this._propertyWindow);
                    this._browserWindow.appendChild(this._treePropertyContainer);
                    $(this._propertyWindow).resizable({
                        resize: function (event, ui) {
                            _this.onResize(ui.size.width, _this._viewer.getView().getCanvasSize().y);
                        },
                        handles: "n"
                    });
                    $(this._browserWindow).resizable({
                        resize: function (event, ui) {
                            _this.onResize(ui.size.width, ui.size.height);
                        },
                        minWidth: 35,
                        minHeight: 37,
                        handles: "e"
                    });
                    var browserDivWrapper = $("#browserDivScrollContainer").get(0);
                    this._modelTreeScroll = new IScroll(browserDivWrapper, {
                        mouseWheel: true,
                        scrollbars: true,
                        interactiveScrollbars: true,
                        preventDefault: false
                    });
                    var cadViewDivWrapper = $("#cadViewDivScrollContainer").get(0);
                    this._cadViewTreeScroll = new IScroll(cadViewDivWrapper, {
                        mouseWheel: true,
                        scrollbars: true,
                        interactiveScrollbars: true,
                        preventDefault: false
                    });
                    this._modelTree = new Ui.ModelTree("modelTree", this._viewer, this._cuttingPlaneController, this._modelTreeScroll);
                    this._contextMenu = new Desktop.ModelBrowserContextMenu(this._containerId, this._viewer, this._modelTree, this._isolateZoomHelper);
                    this._cadViewTree = new Ui.CADViewTree("cadViewTree", this._viewer, this._cuttingPlaneController);
                    this._sheetsTree = new Ui.SheetsTree("sheetsTree", this._viewer);
					_this._onMinimizeButtonClick();
                };
                ModelBrowser.prototype._onMinimizeButtonClick = function () {
                    var _this = this;
                    var $minimizeButton = jQuery(this._minimizeButton);
                    if ($minimizeButton.hasClass("maximized")) {
						$("#contextMenuButton").hide();
						$(".ui-modelbrowser-header").css({ 'background-color' : 'white' });
						$(".ui-modelbrowser-minimizetd").css({'border-right' : '.0625em solid white'});
                        $minimizeButton.removeClass("maximized");
                        $minimizeButton.addClass("minimized");
                        jQuery(this._content).slideUp({
                            progress: function () {
                                _this._onSlide();
                                $("#modelBrowserWindow").addClass("ui-modelbrowser-small");
                            },
                            complete: function () {
                                $(_this._browserWindow).children(".ui-resizable-handle").hide();
                            }
                        });
                    }
                    else {
						$("#contextMenuButton").show();
						$(".ui-modelbrowser-header").css({ 'background-color' : '#39974A' });
						$(".ui-modelbrowser-minimizetd").css({'border-right' : '.0625em solid gray'});
                        $minimizeButton.addClass("maximized");
                        $minimizeButton.removeClass("minimized");
                        jQuery(this._content).slideDown({
                            progress: function () {
                                _this._onSlide();
                                $("#modelBrowserWindow").removeClass("ui-modelbrowser-small");
                            },
                            complete: function () {
                                $(_this._browserWindow).children(".ui-resizable-handle").show();
                            }
                        });
                    }
                    this._refreshBrowserScroll();
                };
                ModelBrowser.prototype.onResize = function (width, height) {
                    var $header = $(this._header);
                    var $propertyWindow = $(this._propertyWindow);
                    this._treePropertyContainer.style.height = (height - $header.outerHeight() - this._browserWindowMargin * 2) + "px";
                    var contentHeight = height - $header.outerHeight() - $propertyWindow.outerHeight() - this._browserWindowMargin * 2;
                    this._browserWindow.style.height = (height - this._browserWindowMargin * 2) + "px";
                    this._content.style.height = contentHeight + "px";
                    this._refreshBrowserScroll();
                };
                ModelBrowser.prototype._onSlide = function () {
                    var $header = $(this._header);
                    var $content = $(this._content);
                    var $propertyWindow = $(this._propertyWindow);
                    var browserWindowHeight = $content.outerHeight() + $header.outerHeight() + $propertyWindow.outerHeight();
                    this._browserWindow.style.height = browserWindowHeight + "px";
                };
                ModelBrowser.prototype._onModelStructureParsingBegin = function () {
                    var $loadingDiv = $("#modelBrowserLoadingDiv");
                    $loadingDiv.html("Parsing...");
                };
                ModelBrowser.prototype._onModelStructureLoadBegin = function () {
                    var $containerDiv = $('#' + this._elementId);
                    $containerDiv.show();
                };
                ModelBrowser.prototype._onModelStructureReady = function () {
                    var $loadingDiv = $("#modelBrowserLoadingDiv");
                    $loadingDiv.remove();
                    var $cadViewDivContainer = $("#browserDivScrollContainer");
                    $cadViewDivContainer.show();
                    var $modelBrowserWindow = $(document.getElementById(this._elementId));
                    this.onResize($modelBrowserWindow.width(), $modelBrowserWindow.height());
                };
                ModelBrowser.prototype.freeze = function (freeze) {
                    this._modelTree.freezeExpansion(freeze);
                };
                ModelBrowser.prototype.enablePartSelection = function (enable) {
                    this._modelTree.enablePartSelection(enable);
                };
                ModelBrowser.prototype.updateSelection = function () {
                    this._modelTree.updateSelection();
                };
                return ModelBrowser;
            }());
            Desktop.ModelBrowser = ModelBrowser;
        })(Desktop = Ui.Desktop || (Ui.Desktop = {}));
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        var Desktop;
        (function (Desktop) {
            var DesktopUi = (function () {
                function DesktopUi(viewer, screenConfiguration) {
                    if (screenConfiguration === void 0) { screenConfiguration = Communicator.ScreenConfiguration.Desktop; }
                    this._viewer = viewer;
                    this._screenConfiguration = screenConfiguration;
                    this._initElements();
                    this._initEvents();
                }
                DesktopUi.prototype._initElements = function () {
                    this._cuttingPlaneController = new Communicator.Ui.CuttingPlaneController(this._viewer);
                    this._axisTriad = this._viewer.getView().getAxisTriad();
                    this._navCube = this._viewer.getView().getNavCube();
                    if (this._screenConfiguration == Communicator.ScreenConfiguration.Mobile) {
                        this._axisTriad.setAnchor(Communicator.OverlayAnchor.UpperRightCorner);
                        this._navCube.setAnchor(Communicator.OverlayAnchor.UpperLeftCorner);
                    }
                    this._toolbar = new Ui.Toolbar(this._viewer, this._cuttingPlaneController, this._axisTriad, this._navCube, this._screenConfiguration);
                    this._toolbar.init();
                    var handleOperator = this._viewer.getOperatorManager().getOperator(Communicator.OperatorId.Handle);
                    if (handleOperator) {
                        handleOperator.setHandleSize(this._screenConfiguration == Communicator.ScreenConfiguration.Mobile ? 3 : 1);
                    }
                    var content = document.getElementById("content");
                    content.oncontextmenu = function () { return false; };
                    this._isolateZoomHelper = new Communicator.IsolateZoomHelper(this._viewer);
                    var modelBrowserDiv = document.createElement("div");
                    modelBrowserDiv.id = "modelBrowserWindow";
                    content.appendChild(modelBrowserDiv);
                    this._modelBrowser = new Desktop.ModelBrowser(modelBrowserDiv.id, content.id, this._viewer, this._isolateZoomHelper, this._cuttingPlaneController, this._screenConfiguration);
                    this._propertyWindow = new Desktop.PropertyWindow(this._viewer, this._isolateZoomHelper);
                    var streamingIndicatorDiv = document.createElement("div");
                    streamingIndicatorDiv.id = "streamingIndicator";
                    content.appendChild(streamingIndicatorDiv);
                    if (this._viewer.getRendererType() == Communicator.RendererType.Client)
                        this._streamingIndicator = new Ui.StreamingIndicator(streamingIndicatorDiv.id, this._viewer);
                    this._contextMenu = new Ui.RightClickContextMenu(content.id, this._viewer, this._isolateZoomHelper);
                    this._timeoutWarningDialog = new Ui.TimeoutWarningDialog(content.id, this._viewer);
                };
                DesktopUi.prototype._initEvents = function () {
                    var _this = this;
                    if (!this.webgl_detect()) {
                        var WebGLDialog = new Communicator.Ui.UiDialog("content");
                        WebGLDialog.setTitle("WebGL not detected");
                        WebGLDialog.setText("Unable to detect WebGL");
                        WebGLDialog.show();
                    }
                    this._viewer.setCallbacks({
                        sceneReady: function () {
                            _this._onSceneReady();
                        },
                        modelStructureHeaderParsed: function () {
                            _this._updateDrawingsUI();
                        },
                        modelLoadFailure: function (modelName, reason) {
                            var errorDialog = new Communicator.Ui.UiDialog("content");
                            errorDialog.setTitle("Model Load Error");
                            var text = "Unable to load ";
                            if (modelName === null) {
                                text += "model";
                            }
                            else {
                                text += "'" + modelName + "'";
                            }
                            text += ": " + reason;
                            errorDialog.setText(text);
                            errorDialog.show();
                        },
                        volumeSelectionLoadBegin: function () {
                            _this.freezeModelBrowser(true);
                            _this.enableModelBrowserPartSelection(false);
                        },
                        volumeSelectionLoadEnd: function () {
                            _this.freezeModelBrowser(false);
                            _this.enableModelBrowserPartSelection(true);
                        },
                        volumeSelectionEnd: function () {
                            _this._modelBrowser.updateSelection();
                        },
                        XHRonloadend: function (e, status, uri) {
                            if (status === 404) {
                                var errorDialog = new Communicator.Ui.UiDialog("content");
                                errorDialog.setTitle("404 Error");
                                errorDialog.setText("Unable to load " + uri);
                                errorDialog.show();
                            }
                        },
                        modelSwitched: function () {
                            _this._updateDrawingsUI();
                        }
                    });
                };
                DesktopUi.prototype._updateDrawingsUI = function () {
                    if (this._viewer.getModel().isDrawing()) {
                        $("#cuttingplane-button").hide();
                        $("#explode-button").hide();
                        $("#view-button").hide();
                        $("#camera-button").hide();
                        $("#tool_separator_4").hide();
                        $("#tool_separator_1").hide();
                        $("#edgeface-button").hide();
                        this._toolbar.reposition();
                        $(".ui-modeltree").addClass("drawing");
                        this._axisTriad.disable();
                        this._navCube.disable();
                    }
                    else {
                        $("#cuttingplane-button").show();
                        $("#explode-button").show();
                        $("#view-button").show();
                        $("#camera-button").show();
                        $("#tool_separator_4").show();
                        $("#tool_separator_1").show();
                        $("#edgeface-button").show();
                        this._toolbar.reposition();
                        $(".ui-modeltree").removeClass("drawing");
                        this._viewer.enableBackgroundSheet(false);
                        this._axisTriad.enable();
                        this._navCube.enable();
                    }
                };
                DesktopUi.prototype._onSceneReady = function () {
                    var _this = this;
                    this._viewer.focusInput(true);
                    this._viewer.getSelectionManager().setNodeSelectionColor(DesktopUi._defaultPartSelectionColor);
                    this._viewer.getSelectionManager().setNodeSelectionOutlineColor(DesktopUi._defaultPartSelectionOutlineColor);
                    var view = this._viewer.getView();
                    view.setXRayColor(Communicator.ElementType.Faces, DesktopUi._defaultXRayColor);
                    view.setXRayColor(Communicator.ElementType.Lines, DesktopUi._defaultXRayColor);
                    view.setXRayColor(Communicator.ElementType.Points, DesktopUi._defaultXRayColor);
                    view.setBackgroundColor(DesktopUi._defaultBackgroundColor, DesktopUi._defaultBackgroundColor);
                    var canvas = this._viewer.getViewElement();
                    canvas.addEventListener("mouseenter", function () {
                        _this._viewer.focusInput(true);
                    });
                };
                DesktopUi.prototype.setDeselectOnIsolate = function (deselect) {
                    this._isolateZoomHelper.setDeselectOnIsolate(deselect);
                };
                DesktopUi.prototype.webgl_detect = function () {
                    if (!!window["WebGLRenderingContext"]) {
                        var canvas = document.createElement("canvas"), names = ["webgl", "experimental-webgl", "moz-webgl"], gl = false;
                        for (var i in names) {
                            try {
                                gl = canvas.getContext(names[i]);
                                if (gl && typeof gl.getParameter == "function") {
                                    return true;
                                }
                            }
                            catch (e) { }
                        }
                        return false;
                    }
                    return false;
                };
                DesktopUi.prototype.freezeModelBrowser = function (freeze) {
                    this._modelBrowser.freeze(freeze);
                };
                DesktopUi.prototype.enableModelBrowserPartSelection = function (enable) {
                    this._modelBrowser.enablePartSelection(enable);
                };
                DesktopUi._defaultBackgroundColor = Communicator.Color.white();
                DesktopUi._defaultPartSelectionColor = Communicator.Color.createFromFloat(0, 0.8, 0);
                DesktopUi._defaultPartSelectionOutlineColor = Communicator.Color.createFromFloat(0, 0.8, 0);
                DesktopUi._defaultXRayColor = Communicator.Color.createFromFloat(0, 0.9, 0);
                return DesktopUi;
            }());
            Desktop.DesktopUi = DesktopUi;
        })(Desktop = Ui.Desktop || (Ui.Desktop = {}));
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        var Desktop;
        (function (Desktop) {
            var IsolateButtonAction;
            (function (IsolateButtonAction) {
                IsolateButtonAction[IsolateButtonAction["Isolate"] = 0] = "Isolate";
                IsolateButtonAction[IsolateButtonAction["ShowAll"] = 1] = "ShowAll";
            })(IsolateButtonAction || (IsolateButtonAction = {}));
            var PropertyWindow = (function () {
                function PropertyWindow(viewer, isolateZoomHelper) {
                    var _this = this;
                    this._modelStructureReady = false;
                    this._volumeSelectionActive = false;
                    this._viewer = viewer;
                    this._propertyWindow = $("#propertyContainer");
                    this._viewer.setCallbacks({
                        modelStructureReady: function () {
                            _this._onModelStructureReady();
                        },
                        selection: function (event) {
                            _this._onPartSelection(event);
                        },
                        modelSwitched: function () {
                            _this._update();
                        },
                        volumeSelectionLoadBegin: function () {
                            _this._volumeSelectionActive = true;
                        },
                        volumeSelectionLoadEnd: function () {
                            _this._volumeSelectionActive = false;
                        }
                    });
                }
                PropertyWindow.prototype._update = function (text) {
                    if (text === void 0) { text = "&lt;no properties to display&gt;"; }
                    this._propertyWindow.html(text);
                };
                PropertyWindow.prototype._onModelStructureReady = function () {
                    this._modelStructureReady = true;
                    this._update();
                };
                PropertyWindow.prototype._createRow = function (key, property, classStr) {
                    if (classStr === void 0) { classStr = ""; }
                    var tableRow = document.createElement("tr");
                    tableRow.id = "propertyTableRow_" + key + "_" + property;
                    if (classStr.length > 0) {
                        tableRow.classList.add(classStr);
                    }
                    var keyDiv = document.createElement("td");
                    keyDiv.id = "propertyDiv_" + key;
                    keyDiv.innerHTML = key;
                    var propertyDiv = document.createElement("td");
                    propertyDiv.id = "propertyDiv_" + property;
                    propertyDiv.innerHTML = property;
                    tableRow.appendChild(keyDiv);
                    tableRow.appendChild(propertyDiv);
                    return tableRow;
                };
                PropertyWindow.prototype._onPartSelection = function (event) {
                    var _this = this;
                    if (!this._modelStructureReady || this._volumeSelectionActive)
                        return;
                    this._update();
                    var id = event.getSelection().getNodeId();
                    if (id == null) {
                        return;
                    }
                    var nodename = this._viewer.getModel().getNodeName(id);
                    var propertyTable = document.createElement("table");
                    propertyTable.id = "propertyTable";
                    var props_promise = this._viewer.getModel().getNodeProperties(id);
                    if (props_promise) {
                        props_promise.then(function (props) {
                            if (props && Object.keys(props).length) {
                                propertyTable.appendChild(_this._createRow("Property", "Value", "headerRow"));
                                propertyTable.appendChild(_this._createRow("Name", nodename));
                                for (var key in props) {
                                    propertyTable.appendChild(_this._createRow(key, props[key]));
                                }
                                _this._update("");
                                _this._propertyWindow.append(propertyTable);
                            }
                        });
                    }
                };
                return PropertyWindow;
            }());
            Desktop.PropertyWindow = PropertyWindow;
        })(Desktop = Ui.Desktop || (Ui.Desktop = {}));
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        var ModelTree = (function (_super) {
            __extends(ModelTree, _super);
            function ModelTree(elementId, viewer, cuttingPlaneController, modelTreeScroll) {
                if (modelTreeScroll === void 0) { modelTreeScroll = null; }
                _super.call(this, elementId, viewer, modelTreeScroll);
                this._lastModelRoot = null;
                this._partSelectionEnabled = true;
                this._currentSheetId = null;
                this._measurementFolderId = "measurementitems";
                this._cuttingPlaneController = cuttingPlaneController;
                this._modelTreeScroll = modelTreeScroll;
                this._initEvents();
            }
            ModelTree.prototype.freezeExpansion = function (freeze) {
                this._tree.freezeExpansion(freeze);
            };
            ModelTree.prototype.modelStructurePresent = function () {
                var model = this._viewer.getModel();
                return model.getNodeName(model.getRootNode()) != "No product structure";
            };
            ModelTree.prototype.enablePartSelection = function (enable) {
                this._partSelectionEnabled = enable;
            };
            ModelTree.prototype._initEvents = function () {
                var _this = this;
                this._viewer.setCallbacks({
                    modelStructureReady: function () {
                        _this._onModelStructureReady();
                    },
                    selection: function (event) {
                        _this._onPartSelection(event);
                    },
                    partsVisibilityHidden: function (nodeIdArray) {
                        _this._tree.getVisibilityControl().updateModelTreeVisibilityState();
                    },
                    partsVisibilityShown: function (nodeIdArray) {
                        _this._tree.getVisibilityControl().updateModelTreeVisibilityState();
                    },
                    viewCreated: function (view) {
                        _this._onNewView(view);
                    },
                    viewLoaded: function (view) {
                        _this._onNewView(view);
                    },
                    subtreeLoaded: function (nodeIdArray) {
                        _this._onSubtreeLoaded(nodeIdArray);
                    },
                    subtreeDeleted: function (nodeIdArray) {
                        _this._onSubtreeDeleted(nodeIdArray);
                    },
                    hwfParseComplete: function () {
                        _this._reset();
                    },
                    modelSwitched: function () {
                        _this._reset();
                    },
                    measurementCreated: function (measurement) {
                        _this._onNewMeasurement(measurement);
                    },
                    measurementLoaded: function (measurement) {
                        _this._onNewMeasurement(measurement);
                    },
                    measurementDeleted: function (measurement) {
                        _this._onDeleteMeasurement(measurement);
                    },
                    sheetActivated: function (id) {
                        _this._selectSheet(id);
                    }
                });
                this._tree.registerCallback("loadChildren", function (id) {
                    _this._loadNodeChildren(id);
                });
                this._tree.registerCallback("selectItem", function (id, selectionMode) {
                    _this._onTreeSelectItem(id, selectionMode);
                });
            };
            ModelTree.prototype._selectSheet = function (id) {
                this._tree.clear(true);
                this._currentSheetId = id;
                var model = this._viewer.getModel();
                var rootId = model.getRootNode();
                var name = model.getNodeName(rootId);
                var modelRoot = this._tree.appendTopLevelElement(name, this._partId(rootId.toString()), "modelroot", model.getNodeChildren(rootId).length > 0, false);
                this._tree.addChild(model.getNodeName(id), this._partId(id + ''), this._partId(rootId.toString()), "part", true);
                this._tree.expandChildren(this._partId(rootId.toString()), false);
            };
            ModelTree.prototype._reset = function () {
                this._tree.clear();
                this._onModelStructureReady();
            };
            ModelTree.prototype._onModelStructureReady = function () {
                var model = this._viewer.getModel();
                var rootId = model.getRootNode();
                var name = model.getNodeName(rootId);
                this._startedWithoutModelStructure = !this.modelStructurePresent();
                this._lastModelRoot = this._tree.appendTopLevelElement(name, this._partId(rootId.toString()), "modelroot", model.getNodeChildren(rootId).length > 0);
                this._tree.expandInitialNodes("part_" + rootId.toString());
            };
            ModelTree.prototype._createMarkupViewFolderIfNecessary = function () {
                var $markupViewFolder = $('#markupviews');
                if ($markupViewFolder.length == 0)
                    this._tree.appendTopLevelElement("Markup Views", "markupviews", "viewfolder", false);
            };
            ModelTree.prototype._createMeasurementFolderIfNecessary = function () {
                var $measurementsFolder = $('#' + this._measurementFolderId);
                if ($measurementsFolder.length == 0)
                    this._tree.appendTopLevelElement("Measurements", this._measurementFolderId, "measurement", false);
            };
            ModelTree.prototype._onSubtreeLoaded = function (idArray) {
                var model = this._viewer.getModel();
                for (var i = 0; i < idArray.length; i++) {
                    var nodeId = idArray[i];
                    var parent = model.getNodeParent(nodeId);
                    if (parent == null) {
                        this._lastModelRoot = this._tree.insertNodeAfter(model.getNodeName(nodeId), this._partId(nodeId.toString()), "modelroot", this._lastModelRoot, true);
                    }
                    else {
                        if (this._tree.childrenAreLoaded(this._partId(parent.toString()))) {
                            this._tree.addChild(model.getNodeName(nodeId), this._partId(nodeId.toString()), this._partId(parent.toString()), "assembly", true);
                            this._tree.preloadChildrenIfNecessary(this._partId(nodeId.toString()));
                        }
                    }
                }
                if (this._startedWithoutModelStructure) {
                    var treeRoot = this._tree.getRoot();
                    treeRoot.removeChild(treeRoot.firstChild);
                    var visibilityRoot = this._tree.getPartVisibilityRoot();
                    visibilityRoot.removeChild(visibilityRoot.firstChild);
                }
            };
            ModelTree.prototype._onSubtreeDeleted = function (idArray) {
                var model = this._viewer.getModel();
                for (var i = 0; i < idArray.length; i++) {
                    var nodeId = idArray[i];
                    this._tree.deleteNode(this._partId(nodeId.toString()));
                }
            };
            ModelTree.prototype._nodeHasContainers = function (partId) {
                var $node = $(this._partId(partId.toString()));
                return true;
            };
            ModelTree.prototype._buildTreePathForNode = function (id) {
                var model = this._viewer.getModel();
                var parents = [];
                var isDrawing = this._viewer.getModel().isDrawing();
                var parentId = model.getNodeParent(id);
                while (parentId != null) {
                    if (isDrawing && this._currentSheetId !== null && (parentId === this._currentSheetId || id === this._currentSheetId)) {
                        break;
                    }
                    parents.push(parentId);
                    parentId = model.getNodeParent(parentId);
                }
                parents.reverse();
                return parents;
            };
            ModelTree.prototype._expandCorrectContainerForNodeid = function (nodeId) {
                var model = this._viewer.getModel();
                var parentId = model.getNodeParent(nodeId);
                var nodes = model.getNodeChildren(parentId);
                var index = nodes.indexOf(nodeId);
                if (index > 0) {
                    var containerId = Math.floor(index / this._maxNodeChildrenSize);
                    this._tree.expandChildren(this._containerId(parentId.toString(), containerId));
                }
            };
            ModelTree.prototype._onPartSelection = function (event) {
                if (!this._partSelectionEnabled) {
                    return;
                }
                var id = event.getSelection().getNodeId();
                if (id == null) {
                    this._tree.selectItem(null, false);
                }
                else {
                    var parents = this._buildTreePathForNode(id);
                    for (var i = 0; i < parents.length; i++) {
                        var $node = $("#" + this._partId(parents[i].toString()));
                        if ($node.length == 0) {
                            this._expandCorrectContainerForNodeid(parents[i]);
                            $node = $("#" + this._partId(parents[i].toString()));
                        }
                        this._tree.expandChildren($node.attr("id"));
                    }
                    this._tree.selectItem(this._partId(id.toString()), false);
                }
            };
            ModelTree.prototype._createContainerNodes = function (partId, childNodes) {
                var containerStartIndex = 0;
                var containerEndIndex = this._maxNodeChildrenSize - 1;
                var containerCount = 0;
                while (true) {
                    var rangeEnd = Math.min(containerEndIndex, childNodes.length);
                    var name = "Child Nodes " + (containerStartIndex + 1) + " - " + (rangeEnd + 1);
                    this._tree.addChild(name, this._containerId(partId, containerCount), this._partId(partId), "container", true);
                    containerStartIndex += this._maxNodeChildrenSize;
                    containerCount += 1;
                    if (containerEndIndex >= childNodes.length)
                        break;
                    else
                        containerEndIndex += this._maxNodeChildrenSize;
                }
            };
            ModelTree.prototype._loadAssemblyNodeChildren = function (id) {
                var model = this._viewer.getModel();
                var children = model.getNodeChildren(parseInt(id));
                if (children.length > this._maxNodeChildrenSize) {
                    this._createContainerNodes(id, children);
                }
                else {
                    var partId = this._partId(id);
                    this._processNodeChildren(children, partId);
                }
            };
            ModelTree.prototype._loadContainerChildren = function (containerId) {
                var model = this._viewer.getModel();
                var idParts = this._splitIdStr(containerId);
                var containerData = this._splitContainerId(idParts[1]);
                var children = model.getNodeChildren(parseInt(containerData[0]));
                var startIndex = this._maxNodeChildrenSize * parseInt(containerData[1]);
                var childrenSlice = children.slice(startIndex, startIndex + this._maxNodeChildrenSize);
                this._processNodeChildren(childrenSlice, containerId);
            };
            ModelTree.prototype._processNodeChildren = function (children, parentId) {
                var _this = this;
                var model = this._viewer.getModel();
                var pmiFolder = null;
                for (var i = 0; i < children.length; i++) {
                    var name = model.getNodeName(children[i]);
                    var itemId = children[i].toString();
                    var currParentId = parentId;
                    var itemType = null;
                    switch (model.getNodeType(children[i])) {
                        case Communicator.NodeType.Body:
                        case Communicator.NodeType.BodyInstance:
                            itemType = "body";
                            break;
                        case Communicator.NodeType.PMI:
                            if (pmiFolder == null) {
                                pmiFolder = this._tree.addChild("PMI", "pmi" + this._partId(itemId), parentId, "modelroot", true);
                            }
                            currParentId = pmiFolder.id;
                            itemType = "assembly";
                            break;
                        default:
                            itemType = "assembly";
                    }
                    ;
                    this._tree.addChild(name, this._partId(itemId), currParentId, itemType, model.getNodeChildren(children[i]).length > 0);
                }
                if (children.length > 0) {
                    clearTimeout(this._updateVisibilityStateTimeout);
                    this._updateVisibilityStateTimeout = window.setTimeout(function () {
                        _this._tree.getVisibilityControl().updateModelTreeVisibilityState();
                    }, 50);
                }
            };
            ModelTree.prototype._loadNodeChildren = function (idstr) {
                var idParts = this._splitIdStr(idstr);
                switch (idParts[0]) {
                    case "part":
                        this._loadAssemblyNodeChildren(idParts[1]);
                        break;
                    case "container":
                        this._loadContainerChildren(idstr);
                        break;
                }
            };
            ModelTree.prototype._onTreeSelectItem = function (idstr, selectionMode) {
                if (selectionMode === void 0) { selectionMode = Communicator.SelectionMode.Set; }
                var thisElement = document.getElementById(idstr);
                if (thisElement.tagName == "LI" && idstr != "markupviews") {
                    thisElement.classList.add("selected");
                }
                else {
                    var viewTree = document.getElementById("markupviews");
                    viewTree.classList.remove("selected");
                }
                if (idstr.indexOf("pmi") == 0 && thisElement.classList.contains("ui-modeltree-item")) {
                    thisElement.classList.remove("selected");
                }
                var idParts = this._splitIdStr(idstr);
                switch (idParts[0]) {
                    case "part":
                        this._viewer.selectPart(parseInt(idParts[1]), selectionMode);
                        break;
                    case "markupview":
                        this._viewer.getMarkupManager().activateMarkupView(idParts[1]);
                        break;
                    case "container":
                        this._onContainerClick(idParts[1]);
                        break;
                }
                ;
            };
            ModelTree.prototype._onContainerClick = function (containerId) {
            };
            ModelTree.prototype._onNewView = function (view) {
                this._createMarkupViewFolderIfNecessary();
                var viewId = this._viewId(view.getUniqueId());
                this._tree.addChild(view.getName(), viewId, "markupviews", "view", false);
            };
            ModelTree.prototype._onNewMeasurement = function (measurement) {
                this._createMeasurementFolderIfNecessary();
                var measurementId = this._measurementId(measurement.getId());
                this._tree.addChild(measurement.getName(), measurementId, this._measurementFolderId, "measurement", false);
                this._updateMeasurementsFolderVisibility();
            };
            ModelTree.prototype._onDeleteMeasurement = function (measurement) {
                var measurementId = this._measurementId(measurement.getId());
                this._tree.deleteNode(measurementId);
                this._updateMeasurementsFolderVisibility();
            };
            ModelTree.prototype._updateMeasurementsFolderVisibility = function () {
                var measurements = this._viewer.getMeasureManager().getAllMeasurements();
                var measurementItems = document.getElementById(this._measurementFolderId);
                if (measurementItems) {
                    measurementItems.style['display'] = measurements.length ? 'inherit' : 'none';
                }
            };
            ModelTree.prototype._measurementId = function (id) {
                return "measurement" + ModelTree.separator + id;
            };
            ModelTree.prototype._partId = function (id) {
                return "part" + ModelTree.separator + id;
            };
            ModelTree.prototype._viewId = function (id) {
                return "markupview" + ModelTree.separator + id;
            };
            ModelTree.prototype._containerId = function (partId, containerId) {
                return "container" + ModelTree.separator + partId + '-' + containerId;
            };
            ModelTree.prototype._splitContainerId = function (idstr) {
                return this._splitParts(idstr, '-');
            };
            ModelTree.prototype.updateSelection = function () {
                this._tree.updateSelection();
            };
            return ModelTree;
        }(Ui.ViewTree));
        Ui.ModelTree = ModelTree;
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        var Mobile;
        (function (Mobile) {
            var Ribbon = (function () {
                function Ribbon(elementId, viewer, cuttingPlaneController) {
                    this._sliderVisible = false;
                    this._elementId = elementId;
                    this._cuttingPlaneController = cuttingPlaneController;
                    this._viewer = viewer;
                    this._initElements();
                    this._initEvents();
                }
                Ribbon.prototype._initEvents = function () {
                    var _this = this;
                    this._viewer.setCallbacks({
                        selection: function () {
                            _this._onViewerSelection();
                        }
                    });
                    window.addEventListener("resize", function () {
                        _this._onOrientationChange;
                    });
                };
                Ribbon.prototype._onOrientationChange = function () {
                    var width = $(window).width();
                    var height = $(window).height();
                    if (width < height)
                        this._slidePanel.style.width = ($(window).width() * 0.75) + "px";
                    else
                        this._slidePanel.style.width = ($(window).width() * 0.3) + "px";
                };
                Ribbon.prototype._onViewerSelection = function () {
                    if (this._sliderVisible) {
                        this.hideSlider();
                    }
                };
                Ribbon.prototype.hideSlider = function () {
                    var $treeElement = $('#slider');
                    $treeElement.animate({
                        left: "-150%"
                    });
                    this._sliderVisible = false;
                };
                Ribbon.prototype.showSlider = function () {
                    var $treeElement = $('#slider');
                    $treeElement.animate({
                        left: "0%"
                    });
                    this._sliderVisible = true;
                };
                Ribbon.prototype._initElements = function () {
                    var _this = this;
                    var ribbon = document.getElementById("ribbon");
                    this._slidePanel = document.getElementById("slider");
                    this._slidePanel.style.position = "absolute";
                    this._onOrientationChange();
                    this._slidePanel.style.height = "100%";
                    this._slidePanel.style.background = "white";
                    this._slidePanel.style.overflow = "scroll";
                    var modelTreeDiv = document.createElement("div");
                    modelTreeDiv.id = "modelTree";
                    this._slidePanel.appendChild(modelTreeDiv);
                    this._modelTree = new Communicator.Ui.ModelTree("modelTree", this._viewer, this._cuttingPlaneController);
                    this._modelTreeButton = document.createElement("div");
                    this._modelTreeButton.classList.add("ui-ribbon-modeltree");
                    this._modelTreeButton.onclick = function () {
                        _this._onModelTreeButtonClick();
                    };
                    ribbon.appendChild(this._modelTreeButton);
                    var $treeElement = $('#slider');
                    $treeElement.css("left", "-150%");
                };
                Ribbon.prototype._onModelTreeButtonClick = function () {
                    var $treeElement = $('#slider');
                    if (this._sliderVisible) {
                        this.hideSlider();
                    }
                    else {
                        this.showSlider();
                    }
                };
                return Ribbon;
            }());
            Mobile.Ribbon = Ribbon;
        })(Mobile = Ui.Mobile || (Ui.Mobile = {}));
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
var Communicator;
(function (Communicator) {
    var Ui;
    (function (Ui) {
        var Control;
        (function (Control) {
            var VisibilityControl = (function () {
                function VisibilityControl(viewer) {
                    this._hiddenNodes = [];
                    this._fullHiddenParentIds = [];
                    this._partialHiddenParentIds = [];
                    this._viewer = viewer;
                }
                VisibilityControl.prototype._clearStyles = function () {
                    for (var i = 0; i < this._fullHiddenParentIds.length; i++) {
                        this._removeVisibilityHiddenClass(this._fullHiddenParentIds[i], "partHidden");
                    }
                    this._fullHiddenParentIds = [];
                    for (var i = 0; i < this._partialHiddenParentIds.length; i++) {
                        this._removeVisibilityHiddenClass(this._partialHiddenParentIds[i], "partialHidden");
                    }
                    this._partialHiddenParentIds = [];
                };
                VisibilityControl.prototype._applyStyles = function () {
                    for (var i = 0; i < this._fullHiddenParentIds.length; i++) {
                        this._addVisibilityHiddenClass(this._fullHiddenParentIds[i], "partHidden");
                    }
                    for (var i = 0; i < this._partialHiddenParentIds.length; i++) {
                        this._addVisibilityHiddenClass(this._partialHiddenParentIds[i], "partialHidden");
                    }
                };
                VisibilityControl.prototype.updateModelTreeVisibilityState = function () {
                    this._clearStyles();
                    var model = this._viewer.getModel();
                    var nodeQueue = [];
                    nodeQueue.push(model.getRootNode());
                    var i = 0;
                    while (i < nodeQueue.length) {
                        var nodeId = nodeQueue[i];
                        var nodeStatus = model.getBranchVisibility(nodeId);
                        if (nodeStatus == Communicator.BranchVisibility.Hidden) {
                            this._fullHiddenParentIds.push(nodeId);
                        }
                        else if (nodeStatus == Communicator.BranchVisibility.Mixed) {
                            this._partialHiddenParentIds.push(nodeId);
                            var nodeChildren = model.getNodeChildren(nodeId);
                            for (var j = 0; j < nodeChildren.length; j++) {
                                nodeQueue.push(nodeChildren[j]);
                            }
                        }
                        i++;
                    }
                    this._applyStyles();
                };
                VisibilityControl.prototype._getVisibilityItem = function (id) { return $("#visibility_part_" + id); };
                VisibilityControl.prototype._getItem = function (id) { return $("#part_" + id); };
                VisibilityControl.prototype._addVisibilityHiddenClass = function (id, className) {
                    if (className === void 0) { className = "partHidden"; }
                    this._getVisibilityItem(id).addClass(className);
                };
                VisibilityControl.prototype._removeVisibilityHiddenClass = function (id, className) {
                    if (className === void 0) { className = "partHidden"; }
                    this._getVisibilityItem(id).removeClass(className);
                };
                return VisibilityControl;
            }());
            Control.VisibilityControl = VisibilityControl;
            var TreeControl = (function () {
                function TreeControl(elementId, viewer, separator, modelTreeScroll) {
                    if (modelTreeScroll === void 0) { modelTreeScroll = null; }
                    this._selectedItems = [];
                    this._futureHighlightIds = new Communicator.Internal.Utility.HashMap();
                    this._futureMixedIds = new Communicator.Internal.Utility.HashMap();
                    this._selectedItemsParentIds = [];
                    this._callbacks = {};
                    this._childrenLoaded = {};
                    this._indentSize = 10;
                    this._slideSpeed = 100;
                    this._freezeExpansion = false;
                    this._elementId = elementId;
                    this._viewer = viewer;
                    this._modelTreeScroll = modelTreeScroll;
                    this._separator = separator;
                    this._visibilityControl = new VisibilityControl(viewer);
                    this._init();
                }
                TreeControl.prototype.getElementId = function () {
                    return this._elementId;
                };
                TreeControl.prototype.getRoot = function () {
                    return this._listRoot;
                };
                TreeControl.prototype.getPartVisibilityRoot = function () {
                    return this._partVisibilityRoot;
                };
                TreeControl.prototype.getVisibilityControl = function () {
                    return this._visibilityControl;
                };
                TreeControl.prototype.registerCallback = function (name, func) {
                    if (!this._callbacks.hasOwnProperty(name))
                        this._callbacks[name] = [];
                    this._callbacks[name].push(func);
                };
                TreeControl.prototype._triggerCallback = function (name, args) {
                    var callbacks = this._callbacks[name];
                    if (callbacks) {
                        for (var i = 0; i < callbacks.length; i++) {
                            callbacks[i].apply(null, args);
                        }
                    }
                };
                TreeControl.prototype.deleteNode = function (id) {
                    if (id.charAt(0) == '#')
                        jQuery(id).remove();
                    else
                        jQuery('#' + id).remove();
                };
                TreeControl.prototype.addChild = function (name, id, parent, itemType, hasChildren) {
                    if (!this._viewer.getModel().isDrawing()) {
                        this._addVisibilityToggleChild(name, id, parent, itemType, hasChildren);
                    }
                    var $parent = jQuery('#' + parent);
                    $parent.children(".ui-modeltree-container").children(".ui-modeltree-expandNode").css("visibility", "visible");
                    var $childList = $parent.children("ul");
                    var nodeId = this._parseNodeId(id);
                    var selected = this._futureHighlightIds.contains(nodeId);
                    var mixed = this._futureMixedIds.contains(nodeId);
                    if (selected) {
                        this._futureHighlightIds.remove(nodeId);
                    }
                    if (mixed) {
                        this._futureMixedIds.remove(nodeId);
                    }
                    var node = this._buildNode(name, id, itemType, hasChildren, selected, mixed);
                    if ($childList.length == 0) {
                        var target = document.createElement("ul");
                        target.classList.add("ui-modeltree-children");
                        $parent.append(target);
                        target.appendChild(node);
                    }
                    else {
                        $childList.get(0).appendChild(node);
                    }
                    if (selected) {
                        var $listItem = this._getListItem(id);
                        if ($listItem !== null) {
                            this._selectedItems.push($listItem);
                        }
                    }
                    this._triggerCallback("addChild", null);
                    return node;
                };
                TreeControl.prototype._addVisibilityToggleChild = function (name, id, parent, itemType, hasChildren) {
                    var $parent = jQuery('#visibility' + this._separator + parent);
                    $parent.children(".ui-modeltree-visibility-container").css("visibility", "visible");
                    var $childList = $parent.children("ul");
                    var node = null;
                    if ($childList.length == 0) {
                        var target = document.createElement("ul");
                        target.classList.add("ui-modeltree-visibility-children");
                        $parent.append(target);
                        node = this._buildPartVisibilityNode(name, id, itemType, hasChildren);
                        target.appendChild(node);
                    }
                    else {
                        node = this._buildPartVisibilityNode(name, id, itemType, hasChildren);
                        $childList.get(0).appendChild(node);
                    }
                };
                TreeControl.prototype._buildPartVisibilityNode = function (name, id, itemType, hasChildren) {
                    var itemNode = document.createElement("div");
                    itemNode.classList.add("ui-modeltree-partVisibility-icon");
                    var childItem = document.createElement("li");
                    childItem.classList.add("ui-modeltree-item");
                    childItem.classList.add("visibility");
                    childItem.id = "visibility_" + id;
                    childItem.appendChild(itemNode);
                    var nodeId = parseInt(id.split(this._separator).pop());
                    var nodeType = this._viewer.getModel().getNodeType(nodeId);
                    if (nodeType == Communicator.NodeType.PMI || nodeType == Communicator.NodeType.PMIBody) {
                        childItem.style.visibility = "hidden";
                    }
                    return childItem;
                };
                TreeControl.prototype.freezeExpansion = function (freeze) {
                    this._freezeExpansion = freeze;
                };
                TreeControl.prototype.updateSelection = function () {
                    this._updateTreeSelectionHighlight();
                    this._doUnfreezeSelection();
                };
                TreeControl.prototype.expandChildren = function (id, preloadChildren) {
                    if (preloadChildren === void 0) { preloadChildren = true; }
                    var $item = $('#' + id);
                    if (preloadChildren) {
                        this.preloadChildrenIfNecessary(id);
                    }
                    if (!this._freezeExpansion) {
                        if ($item.length > 0) {
                            $item.children(".ui-modeltree-children").slideDown(this._slideSpeed);
                            $item.children(".ui-modeltree-container").children(".ui-modeltree-expandNode").addClass("expanded");
                        }
                        this._expandVisibilityChildren(id);
                    }
                };
                TreeControl.prototype._expandVisibilityChildren = function (id) {
                    var $item = $('#visibility' + this._separator + id);
                    if ($item.length > 0) {
                        $item.children(".ui-modeltree-visibility-children").addClass("visible");
                        $item.children(".ui-modeltree-visibility-children").slideDown(this._slideSpeed);
                    }
                };
                TreeControl.prototype.collapseChildren = function (id) {
                    this._collapseVisibilityChildren(id);
                    var $item = $('#' + id);
                    if ($item.length > 0)
                        $item.children(".ui-modeltree-children").slideUp(this._slideSpeed);
                };
                TreeControl.prototype._collapseVisibilityChildren = function (id) {
                    var $item = $('#visibility' + this._separator + id);
                    if ($item.length > 0)
                        $item.children(".ui-modeltree-visibility-children").slideUp(this._slideSpeed);
                };
                TreeControl.prototype._buildNode = function (name, id, itemType, hasChildren, selected, mixed) {
                    if (selected === void 0) { selected = false; }
                    if (mixed === void 0) { mixed = false; }
                    var childItem = document.createElement("li");
                    childItem.classList.add("ui-modeltree-item");
                    if (selected) {
                        childItem.classList.add("selected");
                    }
                    if (mixed) {
                        childItem.classList.add("mixed");
                    }
                    childItem.id = id;
                    var itemNode = document.createElement("div");
                    itemNode.classList.add("ui-modeltree-container");
                    itemNode.style.whiteSpace = "nowrap";
                    var expandNode = document.createElement("div");
                    expandNode.classList.add("ui-modeltree-expandNode");
                    if (!hasChildren)
                        expandNode.style.visibility = "hidden";
                    itemNode.appendChild(expandNode);
                    var iconNode = document.createElement("div");
                    iconNode.classList.add("ui-modeltree-icon");
                    iconNode.classList.add(itemType);
                    itemNode.appendChild(iconNode);
                    var labelNode = document.createElement("div");
                    labelNode.classList.add("ui-modeltree-label");
                    labelNode.innerHTML = name;
                    labelNode.title = name;
                    itemNode.appendChild(labelNode);
                    var mixedSelection = document.createElement("div");
                    mixedSelection.classList.add("ui-mixedselection-icon");
                    itemNode.appendChild(mixedSelection);
                    childItem.appendChild(itemNode);
                    return childItem;
                };
                TreeControl.prototype.childrenAreLoaded = function (id) {
                    return this._childrenLoaded.hasOwnProperty(id);
                };
                TreeControl.prototype.preloadChildrenIfNecessary = function (id) {
                    if ((id != null) && !this._childrenLoaded.hasOwnProperty(id)) {
                        this._triggerCallback("loadChildren", [id]);
                        this._childrenLoaded[id] = true;
                    }
                };
                TreeControl.prototype._processExpandClick = function (event) {
                    var $target = jQuery(event.target);
                    var $listItem = $target.parents(".ui-modeltree-item");
                    var id = $listItem.get(0).id;
                    if ($target.hasClass("expanded")) {
                        this._collapseListItem(id, $target);
                    }
                    else {
                        this._expandListItem(id, $target);
                    }
                };
                TreeControl.prototype._collapseListItem = function (id, $target) {
                    this.collapseChildren(id);
                    $target.removeClass("expanded");
                    this._triggerCallback("collapse", [id]);
                };
                TreeControl.prototype._expandListItem = function (id, $target) {
                    this.preloadChildrenIfNecessary(id);
                    this.expandChildren(id);
                    $target.addClass("expanded");
                    this._triggerCallback("expand", [id]);
                };
                TreeControl.prototype.selectItem = function (id, triggerEvent) {
                    if (triggerEvent === void 0) { triggerEvent = true; }
                    if (id != null) {
                        var $listItem = this._getListItem(id);
                        if ($listItem !== null) {
                            this._doSelection($listItem, triggerEvent);
                        }
                    }
                    else {
                        this._doSelection(null, triggerEvent);
                    }
                };
                TreeControl.prototype._getListItem = function (id) {
                    var $listItem = $(this._listRoot).find('#' + id);
                    if ($listItem.length > 0) {
                        return $listItem;
                    }
                    return null;
                };
                TreeControl.prototype._updateNonSelectionHighlight = function ($listItem) {
                    if (this._$lastNonSelectionItem != null) {
                        this._$lastNonSelectionItem.removeClass("selected");
                    }
                    $listItem.addClass("selected");
                    this._$lastNonSelectionItem = $listItem;
                };
                TreeControl.prototype._doUnfreezeSelection = function () {
                    var selectionSet = this._viewer.getSelectionManager().getResults();
                    var selectionIds = selectionSet.map(function (item) { return item.getNodeId(); });
                    for (var i = 0; i < selectionIds.length; i++) {
                        var id = selectionIds[i];
                        var $listItem = this._getListItem('part_' + id);
                        if ($listItem !== null && !$listItem.hasClass("selected")) {
                            $listItem.addClass("selected");
                            this._selectedItems.push($listItem);
                        }
                        else if ($listItem === null) {
                            this._futureHighlightIds.insert(id, null);
                        }
                    }
                };
                TreeControl.prototype._doSelection = function ($listItem, triggerEvent) {
                    var _this = this;
                    if (triggerEvent === void 0) { triggerEvent = true; }
                    if ($listItem != null) {
                        var id = $listItem.get(0).id;
                        if (id.slice(0, 4) == 'part') {
                            $listItem.addClass("selected");
                            var contains = false;
                            for (var i = 0; i < this._selectedItems.length; i++) {
                                if (id == this._selectedItems[i].get(0).id) {
                                    contains = true;
                                    break;
                                }
                            }
                            if (!contains) {
                                this._selectedItems.push($listItem);
                            }
                        }
                        else if (id.slice(0, 5) == 'sheet') {
                        }
                        else {
                            if (id.slice(0, 9) == 'container') {
                                return;
                            }
                            else {
                                this._updateNonSelectionHighlight($listItem);
                            }
                        }
                        if (triggerEvent) {
                            this._$lastItem = $listItem;
                            this._triggerCallback("selectItem", [id, (typeof key != "undefined" && (key.ctrl || key.command) ? Communicator.SelectionMode.Toggle : Communicator.SelectionMode.Set)]);
                        }
                        var newListItem = (this._$lastItem && this._$lastItem.get(0).id != $listItem.get(0).id);
                        if (!this._freezeExpansion && (!triggerEvent && (newListItem || !this._$lastItem))) {
                            var offsetTop = $listItem.offset().top;
                            var itemHeight = $listItem.outerHeight();
                            var containerHeight = $("#modelTreeContainer").innerHeight();
                            var scrollTop = $("#modelTreeContainer").scrollTop();
                            var hiddenTop = offsetTop < 6;
                            var hiddenBottom = offsetTop > containerHeight;
                            if (hiddenTop || hiddenBottom) {
                                var targetOffsetTop = containerHeight / 2;
                                clearTimeout(this._scrollTimeout);
                                this._scrollTimeout = window.setTimeout(function () {
                                    if (_this._modelTreeScroll) {
                                        _this._modelTreeScroll.refresh();
                                        _this._modelTreeScroll.scrollToElement($listItem.get(0), 400, true, true);
                                    }
                                }, 10);
                            }
                        }
                    }
                    this._$lastItem = $listItem;
                    clearTimeout(this._selectionLabelHighlightTimeout);
                    this._selectionLabelHighlightTimeout = window.setTimeout(function () {
                        _this._updateTreeSelectionHighlight();
                    }, 30);
                };
                TreeControl.prototype._parseNodeId = function (id) {
                    return parseInt(id.split(this._separator).pop());
                };
                TreeControl.prototype._updateTreeSelectionHighlight = function () {
                    var selectionSet = this._viewer.getSelectionManager().getResults();
                    var selectionIds = selectionSet.map(function (item) { return item.getNodeId(); });
                    var keys = this._futureHighlightIds.keys();
                    for (var i = 0; i < keys.length; i++) {
                        if (selectionIds.indexOf(keys[i]) === -1) {
                            this._futureHighlightIds.remove(keys[i]);
                        }
                    }
                    for (var i = 0; i < this._selectedItemsParentIds.length; i++) {
                        $("#part_" + this._selectedItemsParentIds[i]).removeClass("mixed");
                    }
                    this._selectedItemsParentIds = [];
                    this._futureMixedIds.clear();
                    var i = 0;
                    while (i < this._selectedItems.length) {
                        var id = this._parseNodeId(this._selectedItems[i].get(0).id);
                        if (selectionIds.indexOf(id) === -1) {
                            this._selectedItems[i].removeClass("selected");
                            this._selectedItems.splice(i, 1);
                        }
                        else {
                            i++;
                        }
                    }
                    for (var i = 0; i < selectionIds.length; i++) {
                        this._updateParentIdList(selectionIds[i]);
                    }
                    for (var i = 0; i < this._selectedItemsParentIds.length; i++) {
                        var $listItem = this._getListItem('part_' + this._selectedItemsParentIds[i]);
                        if ($listItem !== null && !$listItem.hasClass("mixed")) {
                            $listItem.addClass("mixed");
                        }
                        else {
                            this._futureMixedIds.insert(this._selectedItemsParentIds[i], null);
                        }
                    }
                };
                TreeControl.prototype._updateParentIdList = function (childId) {
                    var parentId = this._viewer.getModel().getNodeParent(childId);
                    while (parentId != null && this._selectedItemsParentIds.indexOf(parentId) === -1) {
                        this._selectedItemsParentIds.push(parentId);
                        parentId = this._viewer.getModel().getNodeParent(parentId);
                    }
                };
                TreeControl.prototype.triggerCallback = function (id, position) {
                    this._triggerCallback("context", [id, position]);
                };
                TreeControl.prototype._doContext = function ($listItem, position) {
                    var id = $listItem.get(0).id;
                    this.triggerCallback(id, position);
                };
                TreeControl.prototype._processLabelContext = function (event, position) {
                    var $target = jQuery(event.target);
                    var $listItem = $target.closest(".ui-modeltree-item");
                    if (!position) {
                        var position = new Communicator.Point2(event.clientX, event.clientY);
                    }
                    this._doContext($listItem, position);
                };
                TreeControl.prototype._processLabelClick = function (event) {
                    var $target = jQuery(event.target);
                    var $listItem = $target.closest(".ui-modeltree-item");
                    this._doSelection($listItem, true);
                };
                TreeControl.prototype.appendTopLevelElement = function (name, id, itemType, hasChildren, loadChildren) {
                    if (loadChildren === void 0) { loadChildren = true; }
                    var childItem = this._buildNode(name, id, itemType, hasChildren);
                    if (id.substring(0, 4) === 'part' && this._listRoot.firstChild) {
                        this._listRoot.insertBefore(childItem, this._listRoot.firstChild);
                    }
                    else {
                        this._listRoot.appendChild(childItem);
                    }
                    if (!this._viewer.getModel().isDrawing()) {
                        var childVisibilityItem = this._buildPartVisibilityNode(name, id, itemType, hasChildren);
                        this._partVisibilityRoot.appendChild(childVisibilityItem);
                    }
                    if (loadChildren) {
                        this.preloadChildrenIfNecessary(id);
                    }
                    return childItem;
                };
                TreeControl.prototype.insertNodeAfter = function (name, id, itemType, element, hasChildren) {
                    var childItem = this._buildNode(name, id, itemType, hasChildren);
                    if (element.nextSibling)
                        element.parentNode.insertBefore(childItem, element.nextSibling);
                    else
                        element.parentNode.appendChild(childItem);
                    this.preloadChildrenIfNecessary(id);
                    return childItem;
                };
                TreeControl.prototype.clear = function (onlyParts) {
                    if (onlyParts === void 0) { onlyParts = false; }
                    var listRootChildren = this._listRoot.children;
                    for (var i = 0; i < listRootChildren.length; i++) {
                        var listRootChild = listRootChildren[i];
                        if (!onlyParts || listRootChild.id.substring(0, 4) === 'part') {
                            this._listRoot.removeChild(listRootChild);
                        }
                    }
                    while (this._partVisibilityRoot.firstChild) {
                        this._partVisibilityRoot.removeChild(this._partVisibilityRoot.firstChild);
                    }
                    this._childrenLoaded = {};
                };
                TreeControl.prototype.expandInitialNodes = function (id) {
                    var currentNodeId = id;
                    var childNodes = [];
                    while (true) {
                        childNodes = this._getChildItemsFromModelTreeItem($("#" + currentNodeId));
                        this.expandChildren(currentNodeId);
                        if (childNodes.length != 1) {
                            break;
                        }
                        currentNodeId = childNodes[0].id;
                        this.preloadChildrenIfNecessary(currentNodeId);
                    }
                };
                TreeControl.prototype.processPartVisibilityClick = function (event, position) {
                    var $target = jQuery(event.target);
                    var $listItem = $target.closest(".ui-modeltree-item");
                    var id = this._parseNodeId($listItem[0].id);
                    var visibility = this._viewer.getModel().getNodeVisibility(id);
                    this._viewer.getModel().setNodesVisibility([id], !visibility);
                };
                TreeControl.prototype._init = function () {
                    var _this = this;
                    var container = document.getElementById(this._elementId);
                    this._partVisibilityRoot = document.createElement("ul");
                    this._partVisibilityRoot.classList.add("ui-visibility-toggle");
                    if (!this._viewer.getModel().isDrawing()) {
                        container.appendChild(this._partVisibilityRoot);
                    }
                    this._listRoot = document.createElement("ul");
                    this._listRoot.classList.add("ui-modeltree");
                    this._listRoot.classList.add("ui-modeltree-item");
                    container.appendChild(this._listRoot);
                    $(container).on("click", ".ui-modeltree-label", function (event) {
                        _this._processLabelClick(event);
                    });
                    $(container).on("click", ".ui-modeltree-expandNode", function (event) {
                        _this._processExpandClick(event);
                    });
                    $(container).on("click", ".ui-modeltree-partVisibility-icon", function (event) {
                        _this.processPartVisibilityClick(event);
                    });
                    $(container).on("click", "#contextMenuButton", function (event) {
                        _this._processLabelContext(event);
                    });
                    $(container).on("mouseup", ".ui-modeltree-label, .ui-modeltree-icon", function (event) {
                        if (event.which == 3)
                            _this._processLabelContext(event);
                    });
                    $(container).on("touchstart", function (event) {
                        _this._touchTimer = setTimeout(function () {
                            var e = event.originalEvent;
                            var x = e.touches[0].pageX;
                            var y = e.touches[0].pageY;
                            var position = new Communicator.Point2(x, y);
                            _this._processLabelContext(event, position);
                        }, 1000);
                    });
                    $(container).on("touchmove", function (event) {
                        if (_this._touchTimer) {
                            clearTimeout(_this._touchTimer);
                        }
                    });
                    $(container).on("touchend", function (event) {
                        if (_this._touchTimer) {
                            clearTimeout(_this._touchTimer);
                        }
                    });
                    $(container).on("contextmenu", ".ui-modeltree-label", function (event) { event.preventDefault(); });
                };
                TreeControl.prototype._getListItemLabel = function ($listItem) {
                    return $listItem.children(".ui-modeltree-container").children(".ui-modeltree-label").get(0);
                };
                TreeControl.prototype._getListItemContainer = function ($listItem) {
                    return $listItem.children(".ui-modeltree-container").get(0);
                };
                TreeControl.prototype._getParentModelTreeItem = function ($modeltreeItem) {
                    return $modeltreeItem.parent().parent().get(0);
                };
                TreeControl.prototype._getChildItemsFromModelTreeItem = function ($modeltreeItem) {
                    var $childItems = $modeltreeItem.children(".ui-modeltree-children").children(".ui-modeltree-item");
                    return $.makeArray($childItems);
                };
                return TreeControl;
            }());
            Control.TreeControl = TreeControl;
        })(Control = Ui.Control || (Ui.Control = {}));
    })(Ui = Communicator.Ui || (Communicator.Ui = {}));
})(Communicator || (Communicator = {}));
