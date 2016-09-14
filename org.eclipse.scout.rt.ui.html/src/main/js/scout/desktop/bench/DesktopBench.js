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
scout.DesktopBench = function() {
  scout.DesktopBench.parent.call(this);
  this.htmlComp;
  this.columns = [];
  this.components;
  this.tabBoxMap = {}; // [key=viewId, value=SimpleTabBox instance]
  this._removeViewInProgress = 0;

  this._desktopOutlineChangedHandler = this._onDesktopOutlineChanged.bind(this);
  this._desktopPropertyChangeHandler = this._onDesktopPropertyChange.bind(this);
  this._outlineNodesSelectedHandler = this._onOutlineNodesSelected.bind(this);
  this._outlinePageChangedHandler = this._onOutlinePageChanged.bind(this);
  this._outlinePropertyChangeHandler = this._onOutlinePropertyChange.bind(this);
  this._onOutlineContentDestroyedHandler = this._onOutlineContentDestroyed.bind(this);

  // event listener functions
  this._viewAddedHandler = this._onViewAdded.bind(this);
  this._viewRemovedHandler = this._onViewRemoved.bind(this);
  this._viewActivatedHandler = this._onViewActivated.bind(this);
  this._viewDeactivatedHandler = this._onViewDeactivated.bind(this);

  this._desktopAnimationEndHandler = this._onDesktopAnimationEnd.bind(this);
};
scout.inherits(scout.DesktopBench, scout.Widget);

scout.DesktopBench.VIEW_MIN_HEIGHT; // Configured in sizes.css
scout.DesktopBench.VIEW_MIN_WIDTH; // Configured in sizes.css
scout.DesktopBench.VIEW_AREA_COLUMN_INDEX = {
  LEFT: 0,
  CENTER: 1,
  RIGHT: 2
};

scout.DesktopBench.prototype._init = function(model) {
  scout.DesktopBench.parent.prototype._init.call(this, model);

  scout.DesktopBench.VIEW_MIN_HEIGHT = $.pxToNumber(scout.styles.get('view-tab-box', 'min-height').minHeight);
  scout.DesktopBench.VIEW_MIN_WIDTH = $.pxToNumber(scout.styles.get('view-tab-box', 'min-width').minWidth);

  this._createColumns();
  this.desktop = this.session.desktop;
  this.headerTabArea = model.headerTabArea;
  // controller for headerTabArea
  if (this.headerTabArea) {
    this.headerTabAreaController = new scout.HeaderTabBoxController(this, this.headerTabArea);
  }
  this.outlineContentVisible = scout.nvl(model.outlineContentVisible, true);
  this.setOutline(this.desktop.outline);
  this.updateNavigationHandleVisibility();
};

scout.DesktopBench.prototype._createColumns = function() {
  for (var i = 0; i < 3; i++) {
    var column = scout.create('BenchColumn', {
      parent: this
    });
    column.on('viewAdded', this._viewAddedHandler);
    column.on('viewRemoved', this._viewRemovedHandler);
    column.on('viewActivated', this._viewActivatedHandler);
    column.on('viewDeactivated', this._viewDeactivatedHandler);
    this.columns.push(column);
  }
};

scout.DesktopBench.prototype._initKeyStrokeContext = function() {
  scout.DesktopBench.parent.prototype._initKeyStrokeContext.call(this);

  // Bound to desktop
  this.desktopKeyStrokeContext = new scout.KeyStrokeContext();
  this.desktopKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.desktopKeyStrokeContext.$bindTarget = this.desktop.$container;
  this.desktopKeyStrokeContext.$scopeTarget = this.$container;
  this.desktopKeyStrokeContext.registerKeyStroke(this.desktop.keyStrokes);
  this.desktopKeyStrokeContext.registerKeyStroke(new scout.DesktopTabSelectKeyStroke(this.desktop));
};

scout.DesktopBench.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('desktop-bench');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);

  this.htmlComp.setLayout(this._createLayout());

  this._renderColumns();
  this._revalidateSplitters();
  this._renderNavigationHandleVisible();

  this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
  this.desktop.on('propertyChange', this._desktopPropertyChangeHandler);
  this.desktop.on('outlineChanged', this._desktopOutlineChangedHandler);
  this.desktop.on('animationEnd', this._desktopAnimationEndHandler);
};

scout.DesktopBench.prototype._createLayout = function() {
  return new scout.DesktopBenchLayout(this);
};

scout.DesktopBench.prototype._renderColumns = function() {
  this.columns.forEach(function(column) {
    if (column.viewCount() > 0) {
      this._renderColumn(column);

    }
  }.bind(this));
};

scout.DesktopBench.prototype._renderColumn = function(column) {
  if (!column || column.rendered) {
    return;
  }

  column.render(this.$container);
};

scout.DesktopBench.prototype._remove = function() {
  this.desktop.off('propertyChange', this._desktopPropertyChangeHandler);
  this.desktop.off('outlineChanged', this._desktopOutlineChangedHandler);
  this.desktop.off('animationEnd', this._desktopAnimationEndHandler);
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
  scout.DesktopBench.parent.prototype._remove.call(this);
};

scout.DesktopBench.prototype._renderOutlineContent = function() {
  if (!this.outlineContent) {
    return;
  }

  // Reset view tab relevant properties to make sure no tab is visible for the outline content
  delete this.outlineContent.title;
  delete this.outlineContent.subTitle;
  delete this.outlineContent.iconId;

  // bring the view to top if the desktop is not in background.
  this.addView(this.outlineContent, !this.desktop.inBackground);

  if (this.desktop.rendered) {
    // Request focus on first element in outline content
    this.session.focusManager.validateFocus();
  }
  if (this.outlineContent instanceof scout.Table) {
    this.outlineContent.restoreScrollPosition();
  }
};

scout.DesktopBench.prototype._removeOutlineContent = function() {
  if (!this.outlineContent) {
    return;
  }
  if (this.outlineContent instanceof scout.Table) {
    this.outlineContent.storeScrollPosition();
  }
  this.removeView(this.outlineContent, false);
};

scout.DesktopBench.prototype._renderNavigationHandle = function() {
  if (this.navigationHandle) {
    return;
  }
  this.navigationHandle = scout.create('DesktopNavigationHandle', {
    parent: this,
    leftVisible: false
  });
  this.navigationHandle.render(this.$container);
  this.navigationHandle.$container.addClass('navigation-closed');
  this.navigationHandle.on('action', this._onNavigationHandleAction.bind(this));
};

scout.DesktopBench.prototype._removeNavigationHandle = function() {
  if (!this.navigationHandle) {
    return;
  }
  this.navigationHandle.destroy();
  this.navigationHandle = null;
};

scout.DesktopBench.prototype._renderNavigationHandleVisible = function() {
  if (this.navigationHandleVisible) {
    this._renderNavigationHandle();
  } else {
    this._removeNavigationHandle();
  }
};

/**
 * is called in post render of desktop used to initialize the ui state. E.g. show default views
 */
scout.DesktopBench.prototype.postRender = function() {
  this.columns.forEach(function(column) {
    column.postRender();
  });
};

scout.DesktopBench.prototype.setNavigationHandleVisible = function(visible) {
  this.setProperty('navigationHandleVisible', visible);
};

scout.DesktopBench.prototype.setOutline = function(outline) {
  if (this.outline) {
    this.outline.off('nodesSelected', this._outlineNodesSelectedHandler);
    this.outline.off('pageChanged', this._outlinePageChangedHandler);
    this.outline.off('propertyChange', this._outlinePropertyChangeHandler);
  }
  this._setProperty('outline', outline);
  if (this.outline) {
    this.outline.on('nodesSelected', this._outlineNodesSelectedHandler);
    this.outline.on('pageChanged', this._outlinePageChangedHandler);
    this.outline.on('propertyChange', this._outlinePropertyChangeHandler);
  }
  this.updateOutlineContent();
};

scout.DesktopBench.prototype.setOutlineContent = function(content) {
  var oldContent = this.outlineContent;
  if (this.outlineContent === content) {
    return;
  }
  if (oldContent) {
    oldContent.off('destroy', this._onOutlineContentDestroyedHandler);
  }
  if (this.rendered) {
    this._removeOutlineContent();
  }
  // add a destroy listener to the outline-content, so we can reset the property - otherwise we'd work
  // with a potentially destroyed content which would cause an error later, when we try to render the
  // bench with the outline-content.
  if (content) {
    content.one('destroy', this._onOutlineContentDestroyedHandler);
  }

  this._setProperty('outlineContent', content);

  // Inform header that outline content has changed
  // (having a listener in the header is quite complex due to initialization phase, a direct call here is much easier to implement)
  if (this.desktop.header) {
    this.desktop.header.onBenchOutlineContentChange(content, oldContent);
  }
  this._renderOutlineContent();
};

scout.DesktopBench.prototype.setOutlineContentVisible = function(visible) {
  if (visible === this.outlineContentVisible) {
    return;
  }
  this._setProperty('outlineContentVisible', visible);
  this.updateOutlineContent();
};

scout.DesktopBench.prototype.bringToFront = function() {
  if (!this.outlineContent) {
    return;
  }
  this._renderOutlineContent();
};

scout.DesktopBench.prototype.sendToBack = function() {
  // nop
};

scout.DesktopBench.prototype._computeDefaultDetailForm = function() {
  return this.outline.defaultDetailForm;
};

scout.DesktopBench.prototype._computeOutlineOverview = function() {
  return this.outline.outlineOverview;
};

scout.DesktopBench.prototype._computeDetailContentForPage = function(node) {
  if (!node) {
    throw new Error('called _showDetailContentForPage without node');
  }

  var content;
  if (node.detailForm && node.detailFormVisible && node.detailFormVisibleByUi) {
    content = node.detailForm;
    content.uiCssClass = 'detail-form';
  } else if (node.detailTable && node.detailTableVisible) {
    content = node.detailTable;
    content.uiCssClass = 'detail-table';
  }

  return content;
};

scout.DesktopBench.prototype.updateOutlineContent = function() {
  if (!this.outlineContentVisible || !this.outline) {
    return;
  }
  var content;
  var selectedPages = this.outline.selectedNodes;
  if (selectedPages.length === 0) {
    if (this.outline.defaultDetailForm) {
      content = this._computeDefaultDetailForm();
    } else if (this.outline.outlineOverview) {
      content = this._computeOutlineOverview();
    }
  } else {
    // Outline does not support multi selection -> [0]
    var selectedPage = selectedPages[0];
    content = this._computeDetailContentForPage(selectedPage);
  }
  if (content) {
    if (content instanceof scout.Table) {
      content.menuBar.top();
      content.menuBar.large();
    }
    content.displayViewId = 'C';
  }
  this.setOutlineContent(content);
};

scout.DesktopBench.prototype.updateOutlineContentDebounced = function() {
  clearTimeout(this._updateOutlineContentTimeout);
  this._updateOutlineContentTimeout = setTimeout(function() {
    this.updateOutlineContent();
  }.bind(this), 300);
};

scout.DesktopBench.prototype.updateNavigationHandleVisibility = function() {
  // Don't show handle if desktop says handle must not be visible
  // Only show handle if navigation is invisible
  this.setNavigationHandleVisible(this.desktop.navigationHandleVisible && !this.desktop.navigationVisible);
};

scout.DesktopBench.prototype._onDesktopOutlineChanged = function(event) {
  this.setOutline(this.desktop.outline);
  this.updateNavigationHandleVisibility();
};

scout.DesktopBench.prototype._onOutlineContentDestroyed = function(event) {
  this.setOutlineContent(null);
};

scout.DesktopBench.prototype._onOutlineNodesSelected = function(event) {
  if (event.debounce) {
    this.updateOutlineContentDebounced();
  } else {
    this.updateOutlineContent();
  }
};

scout.DesktopBench.prototype._onOutlinePageChanged = function(event) {
  var selectedPage = this.outline.selectedNodes[0];
  if (!event.page && !selectedPage || event.page === selectedPage) {
    this.updateOutlineContent();
  }
};

scout.DesktopBench.prototype._onOutlinePropertyChange = function(event) {
  if(scout.arrays.containsAny(event.changedProperties, ['defaultDetailForm', 'outlineOverview'])){
    this.updateOutlineContent();
  }
};

scout.DesktopBench.prototype._onDesktopNavigationVisibleChange = function(event) {
  // If navigation gets visible: Hide handle immediately
  // If navigation gets hidden using animation: Show handle when animation ends
  if (this.desktop.navigationVisible) {
    this.updateNavigationHandleVisibility();
  }
};

scout.DesktopBench.prototype._onDesktopNavigationHandleVisibleChange = function(event) {
  this.updateNavigationHandleVisibility();
};

scout.DesktopBench.prototype._onDesktopAnimationEnd = function(event) {
  if (!this.desktop.navigationVisible) {
    this.updateNavigationHandleVisibility();
  }
};

scout.DesktopBench.prototype._onDesktopPropertyChange = function(event) {
  if (event.changedProperties.indexOf('navigationVisible') !== -1) {
    this._onDesktopNavigationVisibleChange();
  } else if (event.changedProperties.indexOf('navigationHandleVisible') !== -1) {
    this._onDesktopNavigationHandleVisibleChange();
  }
};

scout.DesktopBench.prototype._onNavigationHandleAction = function(event) {
  this.desktop.enlargeNavigation();
};

scout.DesktopBench.prototype._revalidateSplitters = function() {
  // remove old splitters
  if (this.components) {
    this.components.forEach(function(comp) {
      if (comp instanceof scout.Splitter) {
        comp.destroy();
      }
    });
  }
  var splitterParent = this;
  this.components = this.columns.filter(function(column) {
    return column.hasViews();
  }).reduce(function(arr, col) {
    if (arr.length > 0) {
      // add sep
      var splitter = scout.create('Splitter', {
        parent: splitterParent,
        $anchor: arr[arr.length - 1].$container,
        $root: splitterParent.$container,
        maxRatio: 1
      });
      splitter.render(splitterParent.$container);
      splitter.$container.addClass('line');
      splitter.on('move', splitterParent._onSplitterMove.bind(splitterParent));
      splitter.on('positionChanged', splitterParent._onSplitterPositionChanged.bind(splitterParent));
      arr.push(splitter);
    }
    arr.push(col);
    return arr;
  }, []);
  // well order the dom elements (reduce is used for simple code reasons, the result of reduce is not of interest).
  this.components.filter(function(comp) {
      return comp instanceof scout.BenchColumn;
    })
    .reduce(function(c1, c2, index) {
      if (index > 0) {
        c2.$container.insertAfter(c1.$container);
      }
      return c2;
    }, undefined);

};

scout.DesktopBench.prototype._onSplitterMove = function(event) {
  var splitterIndex = this.components.indexOf(event.source);
  if (splitterIndex > 0 /*cannot be 0 since first element is a BenchColumn*/ ) {
    var $before = this.components[splitterIndex - 1].$container,
      $after = this.components[splitterIndex + 1].$container,
      diff = event.position - event.source.position;

    if (($before.width() + diff) < scout.DesktopBench.VIEW_MIN_WIDTH) {
      // set to min
      event.setPosition($before.position().left + scout.DesktopBench.VIEW_MIN_WIDTH);
    }
    if (($after.position().left + $after.width() - event.position) < scout.DesktopBench.VIEW_MIN_WIDTH) {
      event.setPosition($after.position().left + $after.width() - scout.DesktopBench.VIEW_MIN_WIDTH);
    }
  }
};

scout.DesktopBench.prototype._onSplitterPositionChanged = function(event) {
  this.revalidateLayout();
};

scout.DesktopBench.prototype._onViewAdded = function(event) {
  this.trigger('viewAdded', {
    view: event.view
  });
};

scout.DesktopBench.prototype._onViewRemoved = function(event) {
  this.trigger('viewRemoved', {
    view: event.view
  });
};

scout.DesktopBench.prototype._onViewActivated = function(event) {
  var view = event.view;
  if (this.outlineContent === view) {
    this.desktop.bringOutlineToFront(this.desktop.outline);
  }
  this.trigger('viewActivated', {
    view: view
  });
};

scout.DesktopBench.prototype._onViewDeactivated = function(event) {
  if (this.outlineContent === event.view) {
    this.desktop.sendOutlineToBack();
  }
  this.trigger('viewDeactivated', {
    view: event.view
  });
};

scout.DesktopBench.prototype.addView = function(view, activate) {
  // normalize displayViewId
  switch (view.displayViewId) {
    case 'NW':
    case 'W':
    case 'SW':
    case 'N':
    case 'C':
    case 'S':
    case 'NE':
    case 'E':
    case 'SE':
      break;
    default:
      // map all other displayViewIds to center
      view.displayViewId = 'C';
      break;
  }
  var column = this._getColumn(view.displayViewId);
  this.tabBoxMap[view.id] = column;
  column.addView(view, activate);

  if (this.rendered) {
    if (column.viewCount() === 1) {
      this._renderColumn(column);
      this._revalidateSplitters();
      this.htmlComp.invalidateLayoutTree();
      // Layout immediate to prevent 'laggy' form visualization,
      // but not initially while desktop gets rendered because it will be done at the end anyway
      this.htmlComp.validateLayoutTree();
    }
  }
};

scout.DesktopBench.prototype.activateView = function(view) {
  var column = this._getColumn(view.displayViewId);
  if (column) {
    column.activateView(view);
  }
};

scout.DesktopBench.prototype._getColumn = function(displayViewId) {
  var column;

  switch (displayViewId) {
    case 'NW':
    case 'W':
    case 'SW':
      column = this.columns[scout.DesktopBench.VIEW_AREA_COLUMN_INDEX.LEFT];
      break;
    case 'NE':
    case 'E':
    case 'SE':
      column = this.columns[scout.DesktopBench.VIEW_AREA_COLUMN_INDEX.RIGHT];
      break;
    default:
      column = this.columns[scout.DesktopBench.VIEW_AREA_COLUMN_INDEX.CENTER];
      break;
  }
  return column;
};

scout.DesktopBench.prototype.removeView = function(view, showSiblingView) {
  var column = this.tabBoxMap[view.id];
  if (column) {
    this._removeViewInProgress++;
    column.removeView(view, showSiblingView);
    this._removeViewInProgress--;
    delete this.tabBoxMap[view.id];
    // remove if empty
    if (this.rendered && column.viewCount() === 0 && this._removeViewInProgress === 0) {
      column.remove();
      this._revalidateSplitters(true);
      this.htmlComp.invalidateLayoutTree();
      // Layout immediate to prevent 'laggy' form visualization,
      // but not initially while desktop gets rendered because it will be done at the end anyway
      this.htmlComp.validateLayoutTree();
    }
  }
};

scout.DesktopBench.prototype.getComponents = function() {
  return this.components;
};

scout.DesktopBench.prototype.getTabBox = function(displayViewId) {
  var viewColumn = this._getColumn(displayViewId);
  if (!viewColumn) {
    return;
  }
  return viewColumn.getTabBox(displayViewId);
};

scout.DesktopBench.prototype.getViews = function(displayViewId) {
  return this.columns.reduce(function(arr, column) {
    scout.arrays.pushAll(arr, column.getViews(displayViewId));
    return arr;
  }, []);
};

scout.DesktopBench.prototype.getViewTab = function(view) {
  var viewTab;
  this.getTabs().some(function(vt) {
    if (vt.view === view) {
      viewTab = vt;
      return true;
    }
    return false;
  });
  return viewTab;
};

scout.DesktopBench.prototype.getTabs = function() {
  var tabs = [];
  // consider right order
  tabs = tabs.concat(this.getTabBox('NW').getController().getTabs());
  tabs = tabs.concat(this.getTabBox('W').getController().getTabs());
  tabs = tabs.concat(this.getTabBox('SW').getController().getTabs());
  tabs = tabs.concat(this.getTabBox('N').getController().getTabs());
  if (this.headerTabAreaController) {
    tabs = tabs.concat(this.headerTabAreaController.getTabs());
  } else {
    tabs = tabs.concat(this.getTabBox('C').getController().getTabs());
  }
  tabs = tabs.concat(this.getTabBox('S').getController().getTabs());
  tabs = tabs.concat(this.getTabBox('NE').getController().getTabs());
  tabs = tabs.concat(this.getTabBox('E').getController().getTabs());
  tabs = tabs.concat(this.getTabBox('SE').getController().getTabs());
  return tabs;
};
