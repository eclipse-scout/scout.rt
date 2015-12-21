/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Outline = function() {
  scout.Outline.parent.call(this);
  this._addAdapterProperties(['defaultDetailForm', 'views', 'dialogs', 'messageBoxes', 'fileChoosers']);
  this.navigateUpInProgress = false; // see NavigateUpMenu.js
  this._additionalContainerClasses += ' outline';
  this._treeItemPaddingLeft = 37;
  this._treeItemPaddingLevel = 20;
  this._detailTableListener;
  this.inBackground = false;
  this.formController;
  this.messageBoxController;
  this.fileChooserController;
  this._nodeIdToRowMap = {};
};
scout.inherits(scout.Outline, scout.Tree);

scout.Outline.prototype._init = function(model) {
  scout.Outline.parent.prototype._init.call(this, model);

  this.formController = new scout.FormController(this, this.session);
  this.messageBoxController = new scout.MessageBoxController(this, this.session);
  this.fileChooserController = new scout.FileChooserController(this, this.session);
  this.addFilter(new scout.DetailTableTreeFilter());
  this.titleVisible = true;
  this._syncDefaultDetailForm(this.defaultDetailForm);
};

scout.Outline.prototype._createKeyStrokeContext = function() {
  return new scout.OutlineKeyStrokeContext(this);
};

/**
 * @override Tree.js
 */
scout.Outline.prototype._initTreeKeyStrokeContext = function(keyStrokeContext) {
  keyStrokeContext.registerKeyStroke([
      new scout.TreeSpaceKeyStroke(this),
      new scout.OutlineNavigationUpKeyStroke(this),
      new scout.OutlineNavigationDownKeyStroke(this),
      new scout.OutlineNavigateToTopKeyStroke(this),
      new scout.OutlineCollapseOrDrillUpKeyStroke(this),
      new scout.OutlineExpandOrDrillDownKeyStroke(this)
    ]
    .concat(this.menus));
  keyStrokeContext.$bindTarget = function() {
    return this.session.$entryPoint;
  }.bind(this);
};

/**
 * @override
 */
scout.Outline.prototype._render = function($parent) {
  scout.Outline.parent.prototype._render.call(this, $parent);

  // Override layout
  this.htmlComp.setLayout(new scout.OutlineLayout(this));

  if (this.selectedNodes.length === 0) {
    if (this.defaultDetailForm) {
      this._showDefaultDetailForm();
    } else if (this.outlineOverview) {
      this._showOutlineOverview();
    }
  }

};

scout.Outline.prototype._renderProperties = function() {
  this._renderTitleVisible();
  scout.Outline.parent.prototype._renderProperties.call(this);
};

/**
 * @override
 */
scout.Outline.prototype._remove = function() {
  scout.Outline.parent.prototype._remove.call(this);
  this._removeTitle();
};

scout.Outline.prototype._renderTitle = function() {
  if (!this.$title) {
    this.$title = this.$container.prependDiv('outline-title');
    this.$titleText = this.$title.prependDiv('outline-title-text');
  }
  this.$titleText.text(this.title).on('click', this._onTitleClick.bind(this));
  this._renderTitleMenuBar();
};

scout.Outline.prototype._renderTitleMenuBar = function() {
  this.titleMenuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.GroupBoxMenuItemsOrder()
  });
  if (this.$title) {
    this.titleMenuBar.render(this.$title);
    this.titleMenuBar.$container.toggleClass('prevent-initial-focus', true);
  }
};

scout.Outline.prototype._removeTitleMenuBar = function() {
  this.titleMenuBar.remove();
  this.titleMenuBar = null;
};

scout.Outline.prototype._removeTitle = function() {
  if (this.$title) {
    this.$title.remove();
    this.$title = null;
    this._removeTitleMenuBar();
  }
};

scout.Outline.prototype.setTitleVisible = function(visible) {
  this.titleVisible = visible;
  if (this.rendered) {
    this._renderTitleVisible();
  }
};

scout.Outline.prototype._renderTitleVisible = function() {
  if (this.titleVisible) {
    this._renderTitle();
  } else {
    this._removeTitle();
  }
};

scout.Outline.prototype.handleOutlineContentDebounced = function(bringToFront) {
  clearTimeout(this._handleOutlineTimeout);
  this._handleOutlineTimeout = setTimeout(function() {
    this.handleOutlineContent(bringToFront);
  }.bind(this), 300);
};

scout.Outline.prototype.handleOutlineContent = function(bringToFront) {
  // Outline does not support multi selection -> [0]
  var node = this.selectedNodes[0];
  if (node) {
    this._updateOutlineNode(node, bringToFront);
  } else {
    this._showDefaultDetailForm();
  }
};

scout.Outline.prototype._postRender = function() {
  scout.Outline.parent.prototype._postRender.call(this);

  // Ensure outline layout is valid (because DialogLayout relies on that)
  this.htmlComp.validateLayout();

  // Display attached forms, message boxes and file choosers.
  this.formController.render();
  this.messageBoxController.render();
  this.fileChooserController.render();
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
    this._initDetailTable(node);
  }
  if (node.detailForm) {
    node.detailForm = this.session.getOrCreateModelAdapter(node.detailForm, this);
    this._initDetailForm(node);
  }

  if (node.parentNode && node.parentNode.detailTable) {
    // link node with row, if it hasn't been linked yet
    if (node.id in this._nodeIdToRowMap) {
      node.row = this._nodeIdToRowMap[node.id];
      if (!node.row) {
        throw new Error('node.row is not defined');
      }
      delete this._nodeIdToRowMap[node.id];
    }
  }
};

scout.Outline.prototype._initDetailTable = function(node) {
  var menus = this._createOutlineNavigationButtons(node, node.detailTable.staticMenus),
    button = this._getMenu(menus, scout.NavigateDownMenu),
    that = this;
  node.detailTable.staticMenus = menus;

  // link already existing rows (rows which are inserted later are linked by _onDetailTableRowInitialized)
  node.detailTable.rows.forEach(function(row) {
    this._linkNodeWithRow(row);
  }, this);

  this._detailTableListener = {
    func: function(event) {
      event.detailTable = node.detailTable;
      that._onDetailTableEvent(event);
    }
  };
  node.detailTable.events.addListener(this._detailTableListener);
};

scout.Outline.prototype._initDetailForm = function(node) {
  var menus = this._createOutlineNavigationButtons(node, node.detailForm.staticMenus);
  node.detailForm.rootGroupBox.staticMenus = menus;
};

scout.Outline.prototype._linkNodeWithRow = function(row) {
  var node = this.nodesMap[row.nodeId];
  if (node) {
    node.row = row;
    if (!node.row) {
      throw new Error('node.row is not defined');
    }
  } else {
    // Prepare for linking later because node has not been inserted yet
    this._nodeIdToRowMap[row.nodeId] = row;
  }
};

/**
 * @override
 */
scout.Outline.prototype._decorateNode = function(node) {
  scout.Outline.parent.prototype._decorateNode.call(this, node);
  scout.inspector.applyInfo(node, node.$node);
};

scout.Outline.prototype._createOutlineNavigationButtons = function(node, staticMenus) {
  var menus = scout.arrays.ensure(staticMenus);
  if (!this._hasMenu(menus, scout.NavigateUpMenu)) {
    var upButton = scout.create('NavigateUpMenu', {
      parent: this,
      outline: this,
      node: node
    });
    menus.push(upButton);
  }
  if (!this._hasMenu(menus, scout.NavigateDownMenu)) {
    var downButton = scout.create('NavigateDownMenu', {
      parent: this,
      outline: this,
      node: node
    });
    menus.push(downButton);
  }
  return menus;
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

scout.Outline.prototype._onTitleClick = function(event) {
  this.navigateToTop();
};

scout.Outline.prototype.navigateToTop = function() {
  this.deselectAll();
  this.collapseAll();
  if (this.defaultDetailForm) {
    this._showDefaultDetailForm();
  } else if (this.outlineOverview) {
    this._showOutlineOverview();
  }

  this.handleOutlineContentDebounced(true);
};

scout.Outline.prototype._onNodeDeleted = function(node) {
  // Destroy table, which is attached at the root adapter. Form gets destroyed by form close event
  if (node.detailTable) {
    node.detailTable.events.removeListener(this._detailTableListener);
    node.detailTable.destroy();
    node.detailTable = null;
  }
};

scout.Outline.prototype.selectNodes = function(nodes, notifyServer, debounceSend) {
  scout.Outline.parent.prototype.selectNodes.call(this, nodes, notifyServer, debounceSend);
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

scout.Outline.prototype._renderDefaultDetailForm = function() {
  if (!this.inBackground) {
    this._showDefaultDetailForm();
  }
};

scout.Outline.prototype._syncDefaultDetailForm = function(defaultDetailForm) {
  this.defaultDetailForm = defaultDetailForm;
  if (this.defaultDetailForm) {
    if (this.outlineOverview) {
      this.outlineOverview.destroy();
      this.outlineOverview = null;
    }
  } else {
    if (!this.outlineOverview) {
      // Create outlineOverview if no defaultDetailForm is available
      this.outlineOverview = scout.create('OutlineOverview', {
        parent: this,
        outline: this
      });
    }
  }
};

scout.Outline.prototype._showDefaultDetailForm = function() {
  if (this.defaultDetailForm) {
    this.session.desktop.setOutlineContent(this.defaultDetailForm, true);
  }
};

scout.Outline.prototype._showOutlineOverview = function() {
  this.session.desktop.setOutlineContent(this.outlineOverview, true);
};

/**
 * @override Tree.js
 */
scout.Outline.prototype._onNodeMouseDown = function(event) {
  if (scout.Outline.parent.prototype._onNodeMouseDown.call(this, event)) {
    this.handleOutlineContent(true);
  }
};

/**
 * @override Tree.js
 */
scout.Outline.prototype._onNodeControlMouseDown = function(event) {
  if (scout.Outline.parent.prototype._onNodeControlMouseDown.call(this, event)) {
    this.handleOutlineContent(true);
  }
};

scout.Outline.prototype._updateOutlineNode = function(node, bringToFront) {
  bringToFront = scout.nvl(bringToFront, true);
  if (!node) {
    throw new Error('called _updateOutlineNode without node');
  }

  // Unlink detail form if it was closed. May happen in the following case:
  // The form gets closed on execPageDeactivated. No pageChanged event will
  // be fired because the deactivated page is not selected anymore.
  if (node.detailForm && node.detailForm.destroyed) {
    node.detailForm = null;
  }

  if (this.session.desktop.outline !== this || !scout.isOneOf(node, this.selectedNodes)) {
    return;
  }

  var content;
  if (node.detailForm && node.detailFormVisible && node.detailFormVisibleByUi) {
    content = node.detailForm;
  } else if (node.detailTable && node.detailTableVisible) {
    content = node.detailTable;
  }

  this.session.desktop.setOutlineContent(content, bringToFront);
};

/**
 * Returns the selected row or null when no row is selected. When multiple rows are selected
 * the first selected row is returned.
 */
scout.Outline.prototype.selectedRow = function() {
  var table, node,
    nodes = this.selectedNodes;
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
  if (oldNode.nodeType !== updatedNode.nodeType) {
    oldNode.nodeType = updatedNode.nodeType;
    propertiesChanged = true;
  }
  return propertiesChanged;
};

/**
 * Called by updateItemPath.
 *
 * @override
 */
scout.Outline.prototype._isGroupingEnd = function(node) {
  return node.nodeType == 'table';
};

/**
 * Disabled for outlines because outline may be resized.
 */
scout.Outline.prototype._isTruncatedNodeTooltipEnabled = function() {
  return false;
};

/* event handling */

scout.Outline.prototype._onDetailTableRowsSelected = function(event) {
  var button = this._getMenu(event.detailTable.staticMenus, scout.NavigateDownMenu);
  button.updateEnabled();
};

scout.Outline.prototype._onDetailTableRowsFiltered = function(event) {
  this.filter();
};

scout.Outline.prototype._onDetailTableRowInitialized = function(event) {
  this._linkNodeWithRow(event.row);
};

scout.Outline.prototype._onDetailTableEvent = function(event) {
  if (event.type === 'rowInitialized') {
    this._onDetailTableRowInitialized(event);
  } else if (event.type === 'rowsFiltered') {
    this._onDetailTableRowsFiltered(event);
  } else if (event.type === 'rowsSelected') {
    this._onDetailTableRowsSelected(event);
  }
};

scout.Outline.prototype._onPageChanged = function(event) {
  if (event.nodeId) {
    var node = this.nodesMap[event.nodeId];

    node.detailFormVisible = event.detailFormVisible;
    node.detailForm = this.session.getOrCreateModelAdapter(event.detailForm, this);
    if (node.detailForm) {
      this._initDetailForm(node);
    }

    node.detailTableVisible = event.detailTableVisible;
    node.detailTable = this.session.getOrCreateModelAdapter(event.detailTable, this);
    if (node.detailTable) {
      this._initDetailTable(node);
    }

    // If the following condition is false, the selection state is not synchronized yet which
    // means there is a selection event in the queue which will be processed right afterwards.
    if (this.selectedNodes.indexOf(node) !== -1) {
      this._updateOutlineNode(node, false);
    }
  } else {
    this.defaultDetailForm = this.session.getOrCreateModelAdapter(event.detailForm, this);
    this._showDefaultDetailForm();
  }
};

scout.Outline.prototype._onNodesSelected = function(nodeIds) {
  scout.Outline.parent.prototype._onNodesSelected.call(this, nodeIds);
  this.handleOutlineContent(this.inFront());
};

scout.Outline.prototype.onModelAction = function(event) {
  if (event.type === 'pageChanged') {
    this._onPageChanged(event);
  } else {
    scout.Outline.parent.prototype.onModelAction.call(this, event);
  }
};

scout.Outline.prototype.validateFocus = function() {
  this.session.focusManager.validateFocus();
};

scout.Outline.prototype.sendToBack = function() {
  this.inBackground = true;
  this._renderInBackground();

  // Detach child dialogs, message boxes and file choosers, not views.
  this.formController.detachDialogs();
  this.messageBoxController.detach();
  this.fileChooserController.detach();
};

scout.Outline.prototype.bringToFront = function() {
  this.inBackground = false;
  this._renderInBackground();

  // Attach child dialogs, message boxes and file choosers.
  this.formController.attachDialogs();
  this.messageBoxController.attach();
  this.fileChooserController.attach();
};

scout.Outline.prototype._renderInBackground = function() {
  this.$container.toggleClass('in-background', this.inBackground);
};

/**
 * === Method required for objects that act as 'displayParent' ===
 *
 * Returns the DOM elements to paint a glassPanes over, once a modal Form, message-box or file-chooser is showed with this Outline as its 'displayParent'.
 */
scout.Outline.prototype.glassPaneTargets = function() {
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

scout.Outline.prototype._renderMenus = function() {
  var menuItems = scout.menus.filter(this.menus, ['Tree.Header']);
  this.titleMenuBar.updateItems(menuItems);
};

/**
 * === Method required for objects that act as 'displayParent' ===
 *
 * Returns 'true' if this Outline is currently accessible to the user.
 */
scout.Outline.prototype.inFront = function() {
  return this.rendered && !this.inBackground;
};
