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
  this.navigateUpInProgress = false; // see NavigateUpButton.js
  this._additionalContainerClasses += ' outline';
  this._treeItemPaddingLeft = 37;
  this._treeItemPaddingLevel = 20;
  this._detailTableListener;
  this.inBackground = false;
  this.embedDetailContent = false;
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
  this.titleMenuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.GroupBoxMenuItemsOrder()
  });
  this._updateTitleMenuBar();
  this.detailMenuBar = scout.create('MenuBar', {
    parent: this,
    menuOrder: new scout.GroupBoxMenuItemsOrder(this.session)
  });
  this.detailMenuBar.bottom();
  this.updateDetailContent();
};

scout.Outline.prototype._createKeyStrokeContext = function() {
  return new scout.OutlineKeyStrokeContext(this);
};

scout.Outline.prototype._filterMenus = function(menus, destination, onlyVisible, enableDisableKeyStroke) {
  //show no contextmenues
  return [];
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
  if (this.mobile) {
    this.$container.addClass('mobile');
  }
  this._renderEmbedDetailContent();
  this._renderDetailContent();
  this._renderDetailMenuBarVisible();
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
  if (this.$title) {
    this.titleMenuBar.render(this.$title);
    this.titleMenuBar.$container.toggleClass('prevent-initial-focus', true);
  }
};

scout.Outline.prototype._removeTitleMenuBar = function() {
  this.titleMenuBar.remove();
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

scout.Outline.prototype._postRender = function() {
  //used to render glasspane
  this._trigger('rendered');
  scout.Outline.parent.prototype._postRender.call(this);

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
  var that = this;

  if (this.navigateButtonsVisible) {
    this._appendNavigateButtonsForDetailTable(node);
  }

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
  if (this.navigateButtonsVisible) {
    this._appendNavigateButtonsForDetailForm(node);
  }

  node.detailForm.one('destroy', function() {
    // Unlink detail form if it was closed. May happen in the following case:
    // The form gets closed on execPageDeactivated. No pageChanged event will
    // be fired because the deactivated page is not selected anymore.
    node.detailForm = null;
    // Also make sure other objects hold no reference to a destroyed form (e.g. bench)
    this._triggerPageChanged(node);
  }.bind(this));
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

scout.Outline.prototype._createNavigateButtons = function(node, staticMenus) {
  var menus = scout.arrays.ensure(staticMenus);
  if (!this._hasMenu(menus, scout.NavigateUpButton)) {
    var upButton = scout.create('NavigateUpButton', {
      parent: this,
      outline: this,
      node: node
    });
    menus.push(upButton);
  }
  if (!this._hasMenu(menus, scout.NavigateDownButton)) {
    var downButton = scout.create('NavigateDownButton', {
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
  this.handleInitialExpanded();
};

scout.Outline.prototype.handleInitialExpanded = function() {
  this._visitNodes(this.nodes, function(node) {
    if (node.initialExpanded) {
      this.expandNode(node, {
        renderExpansion: true
      });
    }
  }.bind(this));
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
  nodes = scout.arrays.ensure(nodes);
  if (nodes.length > 0 && this.isNodeSelected(nodes[0])) {
    // Already selected, do nothing
    return;
  }
  if (nodes.length === 0 && this.selectedNodes.length === 0) {
    // Already unselected, do nothing
    return;
  }
  if (this.navigateUpInProgress) {
    this.navigateUpInProgress = false;
  } else {
    if (nodes.length === 1) {
      // When a node is selected, the detail form should never be hidden
      this.setDetailFormVisibleByUi(nodes[0], true);
    }
  }
  scout.Outline.parent.prototype.selectNodes.call(this, nodes, notifyServer, debounceSend);
  this.updateDetailContent();
};

scout.Outline.prototype._renderDefaultDetailForm = function() {
  // nop
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

scout.Outline.prototype._syncNavigateButtonsVisible = function(navigateButtonsVisible) {
  this.navigateButtonsVisible = navigateButtonsVisible;
  this._visitNodes(this.nodes, this._syncNavigateButtonsVisibleForNode.bind(this));
};

scout.Outline.prototype._syncNavigateButtonsVisibleForNode = function(node, parentNode) {
  if (this.navigateButtonsVisible) {
    if (node.detailForm) {
      this._appendNavigateButtonsForDetailForm(node);
    }
    if (node.detailTable) {
      this._appendNavigateButtonsForDetailTable(node);
    }
  } else {
    if (node.detailForm) {
      this._removeNavigateButtonsForDetailForm(node);
    }
    if (node.detailTable) {
      this._removeNavigateButtonsForDetailTable(node);
    }
  }
};

scout.Outline.prototype._appendNavigateButtonsForDetailForm = function(node) {
  var menus = this._createNavigateButtons(node, node.detailForm.staticMenus);
  node.detailForm.rootGroupBox.setStaticMenus(menus);
};

scout.Outline.prototype._appendNavigateButtonsForDetailTable = function(node) {
  var menus = this._createNavigateButtons(node, node.detailTable.staticMenus);
  node.detailTable.setStaticMenus(menus);
};

scout.Outline.prototype._removeNavigateButtonsForDetailForm = function(node) {
  var staticMenus = [];
  node.detailForm.rootGroupBox.staticMenus.forEach(function(menu) {
    if (menu instanceof scout.NavigateUpButton || menu instanceof scout.NavigateDownButton) {
      menu.destroy();
    } else {
      staticMenus.push(menu);
    }
  });
  node.detailForm.rootGroupBox.setStaticMenus(staticMenus);
};

scout.Outline.prototype._removeNavigateButtonsForDetailTable = function(node) {
  var menus = [];
  var staticMenus = [];
  node.detailTable.staticMenus.forEach(function(menu) {
    if (menu instanceof scout.NavigateUpButton || menu instanceof scout.NavigateDownButton) {
      menu.destroy();
    } else {
      staticMenus.push(menu);
    }
  });
  node.detailTable.setStaticMenus(staticMenus);
};

scout.Outline.prototype._renderNavigateButtonsVisible = function() {
  // nop
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

scout.Outline.prototype.setDetailFormVisibleByUi = function(node, visible) {
  node.detailFormVisibleByUi = visible;
  this._triggerPageChanged(node);
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

/**
 * === Method required for objects that act as 'displayParent' ===
 *
 * Returns 'true' if this Outline is currently accessible to the user.
 */
scout.Outline.prototype.inFront = function() {
  return this.rendered && !this.inBackground;
};

scout.Outline.prototype._renderInBackground = function() {
  this.$container.toggleClass('in-background', this.inBackground);
};


scout.Outline.prototype._renderEmbedDetailContent = function() {
  this.$data.toggleClass('has-detail-content', this.embedDetailContent);
};

scout.Outline.prototype._renderDetailContent = function() {
  if (!this.detailContent) {
    return;
  }

  var page = this.selectedNodes[0];
  this.detailContent.render(page.$node);
  if (this.detailContent.htmlComp) {
    this.detailContent.htmlComp.validateRoot = false;
    this.detailContent.htmlComp.pixelBasedSizing = true;
  }
  this._ensurePageLayout(page);
  this.$data.addClass('detail-content-visible');
};

scout.Outline.prototype._ensurePageLayout = function(page) {
  // selected page now has content (menubar and form) -> needs a layout
  // always create new htmlComp, otherwise we would have to remove them when $node or outline gets remvoed
  page.htmlComp = new scout.HtmlComponent(page.$node, this.session);
  page.htmlComp.setLayout(new scout.PageLayout(this, page));
};

scout.Outline.prototype._removeDetailContent = function() {
  if (!this.detailContent) {
    return;
  }
  this.detailContent.remove();
  this.$data.removeClass('detail-content-visible');
};

scout.Outline.prototype.setEmbedDetailContent = function(embed) {
  this.embedDetailContent = embed;
  if (this.rendered) {
    this._renderEmbedDetailContent();
  }
  this.updateDetailContent();
};

scout.Outline.prototype.setDetailContent = function(content) {
  if (this.detailContent === content) {
    return;
  }
  if (this.rendered) {
    this._removeDetailContent();
  }
  this.detailContent = content;
  if (this.rendered) {
    this._renderDetailContent();
  }
  this.invalidateLayoutTree();
};

scout.Outline.prototype.updateDetailContent = function() {
  if (!this.embedDetailContent) {
    this.setDetailContent(null);
    this.setDetailMenus([]);
    return;
  }

  this.setDetailMenuBarVisible(false);
  this.setDetailContent(this._computeDetailContent());
  this.updateDetailMenus();

  // Layout immediate to prevent 'laggy' form visualization,
  // but not initially while desktop gets rendered because it will be done at the end anyway
  if (this.rendered) {
    this.validateLayoutTree();
  }
};

scout.Outline.prototype._computeDetailContent = function() {
  var selectedPage = this.selectedNodes[0];
  if (!selectedPage) {
    // Detail content is shown for the selected node only
    return null;
  }
  if (selectedPage.nodeType === 'virtual') {
    // If node is virtual it is not known yet whether there is a detail form or not -> wait until node gets resolved
    return null;
  }

  // if there is a detail form, use this
  if (selectedPage.detailForm && selectedPage.detailFormVisible && selectedPage.detailFormVisibleByUi) {
    return selectedPage.detailForm;
    // otherwise show the content of the table row
    // but never if parent is a node page -> the table contains only one column with no essential information
  } else if (selectedPage.row && selectedPage.parentNode.nodeType === 'table') {
    return scout.create('TableRowDetail', {
      parent: this,
      table: selectedPage.parentNode.detailTable,
      tableRow: selectedPage.row
    });
  }
  return null;
};

scout.Outline.prototype.updateDetailMenus = function() {
  if (!this.embedDetailContent) {
    return;
  }
  var selectedPages = this.selectedNodes;
  var selectedPage = selectedPages[0];
  var menuItems = [];
  if (this.detailContent) {
    // get menus from detail form
    if (this.detailContent instanceof scout.Form) {
      var rootGroupBox = this.detailContent.rootGroupBox;
      menuItems = rootGroupBox.processMenus.concat(rootGroupBox.menus);
      rootGroupBox.setMenuBarVisible(false);
    } else {
      // get single selection menus from detail table for table row detail
      menuItems = selectedPage.parentNode.detailTable.menus;
      menuItems = scout.menus.filter(menuItems, ['Table.SingleSelection'], false, true);
    }
  }
  // get empty space menus from detail table
  else if (selectedPages.length > 0) {
    if (selectedPage.detailTable) {
      menuItems = selectedPage.detailTable.menus;
      menuItems = scout.menus.filter(menuItems, ['Table.EmptySpace'], false, true);
    }
  }
  this.setDetailMenus(menuItems);
};

scout.Outline.prototype.setDetailMenus = function(detailMenus) {
  this.detailMenuBar.setMenuItems(detailMenus);
  this.setDetailMenuBarVisible(this.detailMenuBar.menuItems.length > 0);
};

scout.Outline.prototype._renderDetailMenuBarVisible = function() {
  if (this.detailMenuBarVisible) {
    this._renderDetailMenuBar();
  } else {
    this._removeDetailMenuBar();
  }
  this.invalidateLayoutTree();
};

scout.Outline.prototype._renderDetailMenuBar = function() {
  if (this.detailMenuBar.rendered) {
    return;
  }
  if (this.selectedNodes.length === 0) {
    return;
  }

  var node = this.selectedNodes[0];
  this.detailMenuBar.render(node.$node);
  if (this.detailContent && this.detailContent.rendered) {
    // move before content (e.g. form)
    this.detailMenuBar.$container.insertBefore(this.detailContent.$container);
  }
  this._ensurePageLayout(node);
};

scout.Outline.prototype._removeDetailMenuBar = function() {
  if (!this.detailMenuBar.rendered) {
    return;
  }
  this.detailMenuBar.remove();
};

scout.Outline.prototype.setDetailMenuBarVisible = function(visible) {
  this.detailMenuBarVisible = visible;
  if (this.rendered) {
    this._renderDetailMenuBarVisible();
  }
};

/**
 * === Method required for objects that act as 'displayParent' ===
 *
 * Returns the DOM elements to paint a glassPanes over, once a modal Form, message-box or file-chooser is showed with this Outline as its 'displayParent'.
 */
scout.Outline.prototype.glassPaneTargets = function() {
  if (this.rendered) {
    var desktop = this.session.desktop;
    var elements = [];
    if (desktop.navigation) {
      elements.push(desktop.navigation.$body);
    }
    if (desktop.bench) {
      elements.push(desktop.bench.$container);
    }
    return elements;
  } else {
    var deferred = new scout.DeferredGlassPaneTarget();
    var renderedHandler = function(event) {
      var desktop = event.eventOn.session.desktop;
      var elements = [];
      if (desktop.navigation) {
        elements.push(desktop.navigation.$body);
      }
      if (desktop.bench) {
        elements.push(desktop.bench.$container);
      }
      deferred.ready(elements);
    };
    this.one('rendered', renderedHandler);
    this.one('destroy', function() {
      this.off('rendered', renderedHandler);
    }.bind(this));
    return [deferred];
  }
};

scout.Outline.prototype._syncMenus = function(menus, oldMenus) {
  this._keyStrokeSupport.syncMenus(menus, oldMenus);
  if (this.titleMenuBar) {
    // menuBar is not created yet when synMenus is called initially
    this._updateTitleMenuBar();
  }
};

scout.Outline.prototype._updateTitleMenuBar = function() {
  var menuItems = scout.menus.filter(this.menus, ['Tree.Header']);
  this.titleMenuBar.setMenuItems(menuItems);
};

/* event handling */

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
  }
};

scout.Outline.prototype._onPageChanged = function(event) {
  var node;
  if (event.nodeId) {
    node = this.nodesMap[event.nodeId];

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
  } else {
    this.defaultDetailForm = this.session.getOrCreateModelAdapter(event.detailForm, this);
  }

  var selectedPage = this.selectedNodes[0];
  if (!node && !selectedPage || node === selectedPage) {
    this.updateDetailContent();
  }

  this._triggerPageChanged(node);
};

scout.Outline.prototype._triggerPageChanged = function(page) {
  this.trigger('pageChanged', {
    page: page
  });
};

scout.Outline.prototype.onModelAction = function(event) {
  if (event.type === 'pageChanged') {
    this._onPageChanged(event);
  } else {
    scout.Outline.parent.prototype.onModelAction.call(this, event);
  }
};
