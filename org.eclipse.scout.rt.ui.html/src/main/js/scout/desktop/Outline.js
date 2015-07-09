scout.Outline = function() {
  scout.Outline.parent.call(this);
  this._addAdapterProperties(['defaultDetailForm', 'views', 'dialogs', 'messageBoxes', 'fileChoosers']);
  this.navigateUpInProgress = false; // see NavigateUpButton.js
  this._additionalContainerClasses += ' outline';
  this._treeItemPaddingLeft = 37;
  this._treeItemPaddingLevel = 20;
  this._tableSelectionListener;
  this.inBackground = false;
  this._formController;
  this._messageBoxController;
};
scout.inherits(scout.Outline, scout.Tree);

scout.Outline.prototype.init = function(model, session) {
  scout.Outline.parent.prototype.init.call(this, model, session);

  this._formController = new scout.FormController(this, session);
  this._messageBoxController = new scout.MessageBoxController(this, session);
  this._fileChooserController = new scout.FileChooserController(this, session);
};

scout.Outline.prototype._createKeyStrokeAdapter = function() {
  return new scout.OutlineKeyStrokeAdapter(this);
};

scout.Outline.prototype._installKeyStrokeAdapter = function() {
  if (!scout.keyStrokeManager.isAdapterInstalled(this.keyStrokeAdapter)) {
    scout.keyStrokeManager.installAdapter(this.$container.closest('.scout'), this.keyStrokeAdapter);
  }
};

/**
 * @override
 */
scout.Outline.prototype._render = function($parent) {
  scout.Outline.parent.prototype._render.call(this, $parent);

  if (this.selectedNodeIds.length === 0) {
    this._showDefaultDetailForm();
  }
};

scout.Outline.prototype._postRender = function() {
  // Display attached forms, message boxes and file choosers.
  this._formController.render();
  this._messageBoxController.render();
  this._fileChooserController.render();
};

/**
 * @override
 */
scout.Outline.prototype._renderEnabled = function() {
  scout.Outline.parent.prototype._renderEnabled.call(this);
  this.$container.setTabbable(false);
};

/**
 * @override
 */
scout.Outline.prototype._initTreeNode = function(node, parentNode) {
  scout.Outline.parent.prototype._initTreeNode.call(this, node, parentNode);
  node.detailFormVisibleByUi = true;
  if (node.detailTable) {
    node.detailTable = this.session.getOrCreateModelAdapter(node.detailTable, this);
    this._addOutlineNavigationButtons(node.detailTable, node);
  }
  if (node.detailForm) {
    node.detailForm = this.session.getOrCreateModelAdapter(node.detailForm, this);
    this._addOutlineNavigationButtons(node.detailForm, node);
  }
};

/**
 * @override
 */
scout.Outline.prototype._decorateNode = function(node) {
  scout.Outline.parent.prototype._decorateNode.call(this, node);
  if (node.$node) {
    node.$node.toggleAttr('data-modelclass', node.modelClass, node.modelClass);
    node.$node.toggleAttr('data-classid', node.classId, node.classId);
  }
};

scout.Outline.prototype._addOutlineNavigationButtons = function(formOrTable, node) {
  var menus = scout.arrays.ensure(formOrTable.staticMenus);
  if (!this._hasMenu(menus, scout.NavigateUpButton)) {
    var upButton = new scout.NavigateUpButton(this, node);
    menus.push(upButton);
  }
  if (!this._hasMenu(menus, scout.NavigateDownButton)) {
    var downButton = new scout.NavigateDownButton(this, node);
    menus.push(downButton);
  }
  if (formOrTable instanceof scout.Form) {
    formOrTable.rootGroupBox.staticMenus = menus;
  } else {
    var table = formOrTable,
      button = this._getMenu(menus, scout.NavigateDownButton);
    table.staticMenus = menus;
    this._tableSelectionListener = table.events.on(scout.Table.GUI_EVENT_ROWS_SELECTED, function(event) {
      button.updateEnabled();
      table.menuBar.recalculateDefaultMenu();
    });
  }
};

scout.Outline.prototype._getMenu = function(menus, menuClass) {
  for (var i = 0; i < menus.length; i++) {
    if (menus[i] instanceof menuClass) {
      return menus[i];
    }
  }
  return null;
};

scout.Outline.prototype._hasMenu = function(menus, menuClass) {
  return this._getMenu(menus, menuClass) !== null;
};

scout.Outline.prototype._onNodeDeleted = function(node) {
  // Destroy table, which is attached at the root adapter. Form gets destroyed by form close event
  if (node.detailTable) {
    node.detailTable.events.off(this._tableSelectionListener);
    node.detailTable.destroy();
    node.detailTable = null;
  }
};

scout.Outline.prototype._renderSelection = function() {
  scout.Outline.parent.prototype._renderSelection.call(this);

  // Outline does not support multi selection -> [0]
  var node = this.nodesMap[this.selectedNodeIds[0]];
  if (node) {
    this._updateOutlineNode(node);
  } else {
    this._showDefaultDetailForm();
  }
};

scout.Outline.prototype.setNodesSelected = function(nodes) {
  scout.Outline.parent.prototype.setNodesSelected.call(this, nodes);
  if (this.navigateUpInProgress) {
    this.navigateUpInProgress = false;
  } else {
    nodes = scout.arrays.ensure(nodes);
    if (nodes.length === 1) {
      // When a node is selected, the detail form should never be hidden
      nodes[0].detailFormVisibleByUi = true;
    }
  }
};

scout.Outline.prototype._showDefaultDetailForm = function() {
  if (this.defaultDetailForm) {
    this.session.desktop.setOutlineContent(this.defaultDetailForm);
  }
};

scout.Outline.prototype._updateOutlineNode = function(node) {
  if (!node) {
    throw new Error('called _updateOutlineNode without node');
  }

  // Unlink detail form if it was closed. May happen in the following case:
  // The form gets closed on execPageDeactivated. No pageChanged event will
  // be fired because the deactivated page is not selected anymore.
  if (node.detailForm && node.detailForm.destroyed) {
    node.detailForm = null;
  }

  var content;
  if (node.detailForm && node.detailFormVisible && node.detailFormVisibleByUi) {
    content = node.detailForm;
  } else if (node.detailTable && node.detailTableVisible) {
    content = node.detailTable;
  }

  this.session.desktop.setOutlineContent(content);
};

/**
 * Returns the selected row or null when no row is selected. When multiple rows are selected
 * the first selected row is returned.
 */
scout.Outline.prototype.selectedRow = function() {
  var table, node, nodes = this.selectedNodes();
  if (nodes.length === 0) {
    return null;
  }
  node = nodes[0];
  if (!node.detailTable) {
    return null;
  }
  table = node.detailTable;
  if (table.selectedRows.length === 0) {
    return null;
  }
  return table.selectedRows[0];
};

scout.Outline.prototype._applyUpdatedNodeProperties = function(oldNode, updatedNode) {
  var propertiesChanged = scout.Outline.parent.prototype._applyUpdatedNodeProperties.call(this, oldNode, updatedNode);
  if (oldNode.modelClass !== updatedNode.modelClass) {
    oldNode.modelClass = updatedNode.modelClass;
    propertiesChanged = true;
  }
  if (oldNode.classId !== updatedNode.classId) {
    oldNode.classId = updatedNode.classId;
    propertiesChanged = true;
  }
  return propertiesChanged;
};

/* event handling */

scout.Outline.prototype._onPageChanged = function(event) {
  if (event.nodeId) {
    var node = this.nodesMap[event.nodeId];

    node.detailFormVisible = event.detailFormVisible;
    node.detailForm = this.session.getOrCreateModelAdapter(event.detailForm, this);
    if (node.detailForm) {
      this._addOutlineNavigationButtons(node.detailForm, node);
    }

    node.detailTableVisible = event.detailTableVisible;
    node.detailTable = this.session.getOrCreateModelAdapter(event.detailTable, this);
    if (node.detailTable) {
      this._addOutlineNavigationButtons(node.detailTable, node);
    }

    // If the following condition is false, the selection state is not synchronized yet which
    // means there is a selection event in the queue which will be processed right afterwards.
    if (this.selectedNodeIds.indexOf(node.id) !== -1) {
      this._updateOutlineNode(node);
    }
  } else {
    this.defaultDetailForm = this.session.getOrCreateModelAdapter(event.detailForm, this);
    this._showDefaultDetailForm();
  }
};

scout.Outline.prototype.onModelAction = function(event) {
  if (event.type === 'pageChanged') {
    this._onPageChanged(event);
  } else if (event.type === 'formShow') {
    this._formController.registerAndRender(event.form);
  } else if (event.type === 'formHide') {
    this._formController.unregisterAndRemove(event.form);
  } else if (event.type === 'formActivate') {
    this._formController.activateForm(event.form);
  } else if (event.type === 'messageBoxShow') {
    this._messageBoxController.registerAndRender(event.messageBox);
  } else if (event.type === 'messageBoxHide') {
    this._messageBoxController.unregisterAndRemove(event.messageBox);
  } else if (event.type === 'fileChooserShow') {
    this._fileChooserController.registerAndRender(event.fileChooser);
  } else if (event.type === 'fileChooserHide') {
    this._fileChooserController.unregisterAndRemove(event.fileChooser);
  } else {
    scout.Outline.parent.prototype.onModelAction.call(this, event);
  }
};

scout.Outline.prototype.validateFocus = function() {
  scout.focusManager.validateFocus(this.session.uiSessionId);
};

scout.Outline.prototype.sendToBack = function() {
  this.inBackground = true;
  this._renderInBackground();

  // Detach child dialogs, message boxes and file choosers, not views.
  this._formController.detachDialogs();
  this._messageBoxController.detach();
  this._fileChooserController.detach();
};

scout.Outline.prototype.bringToFront = function() {
  this.inBackground = false;
  this._renderInBackground();

  // Attach child dialogs, message boxes and file choosers.
  this._formController.attachDialogs();
  this._messageBoxController.attach();
  this._fileChooserController.attach();
};

scout.Outline.prototype._renderInBackground = function() {
  this.$container.toggleClass('in-background', this.inBackground);
};

/**
 * Returns the DOM elements to paint a 'modality glassPane' over, once a modal Form, message-box or file-chooser is showed with this Outline as its 'displayParent'.
 *
 * This method is necessary because this Outline may act as 'displayParent'.
 */
scout.Outline.prototype.modalityElements = function() {
  var desktop = this.session.desktop;

  var elements = [];
  if (desktop.navigation) {
    elements.push(desktop.navigation.$container); // navigation container; not available if application has no navigation.
  }
  if (desktop._outlineContent) {
    elements.push(desktop._outlineContent.$container); // outline content; not available if application has no navigation.
  }

  return elements;
};

/**
 * Returns 'true' if this Outline is currently accessible to the user.
 *
 * This method is necessary because this Outline may act as 'displayParent'.
 */
scout.Outline.prototype.inFront = function() {
  return this.rendered && !this.inBackground;
};
