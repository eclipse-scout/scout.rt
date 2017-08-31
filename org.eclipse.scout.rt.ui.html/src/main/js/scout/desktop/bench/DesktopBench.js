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
  this.changingCounter = 0;
  this.changed = false;
  this.layoutCacheKey = [];

  this._desktopOutlineChangeHandler = this._onDesktopOutlineChange.bind(this);
  this._desktopPropertyChangeHandler = this._onDesktopPropertyChange.bind(this);
  this._outlineNodesSelectedHandler = this._onOutlineNodesSelected.bind(this);
  this._outlinePageChangedHandler = this._onOutlinePageChanged.bind(this);
  this._outlinePropertyChangeHandler = this._onOutlinePropertyChange.bind(this);
  this._outlineContentDestroyHandler = this._onoutlineContentDestroy.bind(this);

  // event listener functions
  this._viewAddHandler = this._onViewAdd.bind(this);
  this._viewRemoveHandler = this._onViewRemove.bind(this);
  this._viewActivateHandler = this._onViewActivate.bind(this);
  this._viewDeactivateHandler = this._onViewDeactivate.bind(this);

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

scout.DesktopBench.VIEW_AREA_COLUMN_CLASSES = [
  'west',
  'center',
  'east'
];

scout.DesktopBench.prototype._init = function(model) {
  scout.DesktopBench.parent.prototype._init.call(this, model);

  scout.DesktopBench.VIEW_MIN_HEIGHT = $.pxToNumber(scout.styles.get('view-tab-box', 'min-height').minHeight);
  scout.DesktopBench.VIEW_MIN_WIDTH = $.pxToNumber(scout.styles.get('view-tab-box', 'min-width').minWidth);

  this.desktop = this.session.desktop;

  this.setLayoutData(this.desktop.benchLayoutData);
  this._createColumns();
  this.headerTabArea = model.headerTabArea;
  // controller for headerTabArea
  if (this.headerTabArea) {
    this.headerTabAreaController = scout.create('HeaderTabBoxController');
    this.headerTabAreaController.install(this, this.headerTabArea);
  }
  this.outlineContentVisible = scout.nvl(model.outlineContentVisible, true);
  this.setOutline(this.desktop.outline);
  this.updateNavigationHandleVisibility();
};

scout.DesktopBench.prototype._createColumns = function() {
  var layoutData = this.getLayoutData(),
    columnLayoutData = [];

  if (layoutData) {
    columnLayoutData = this.getLayoutData().getColumns();
  }
  for (var i = 0; i < 3; i++) {
    var cacheKey = this.layoutCacheKey.slice();
    if (cacheKey.length > 0) {
      cacheKey.push('column' + i);
    }
    var column = scout.create('BenchColumn', {
      parent: this,
      layoutData: columnLayoutData[i],
      cacheKey: cacheKey,
      cssClass: scout.DesktopBench.VIEW_AREA_COLUMN_CLASSES[i]
    });
    column.on('viewAdd', this._viewAddHandler);
    column.on('viewRemove', this._viewRemoveHandler);
    column.on('viewActivate', this._viewActivateHandler);
    column.on('viewDeactivate', this._viewDeactivateHandler);
    this.columns.push(column);
  }
};

scout.DesktopBench.prototype._initKeyStrokeContext = function() {
  scout.DesktopBench.parent.prototype._initKeyStrokeContext.call(this);

  // Bound to desktop
  this.desktopKeyStrokeContext = new scout.KeyStrokeContext();
  this.desktopKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.desktopKeyStrokeContext.$bindTarget = this.desktop.$container;
  this.desktopKeyStrokeContext.$scopeTarget = this.desktop.$container;
  this.desktopKeyStrokeContext.registerKeyStroke(new scout.DesktopTabSelectKeyStroke(this.desktop));
};

scout.DesktopBench.prototype._render = function() {
  this.$container = this.$parent.appendDiv('desktop-bench');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);

  this.htmlComp.setLayout(this._createLayout());
  this.htmlComp.layoutData = this.getLayoutData();

  this._renderColumns();
  this._revalidateSplitters();
  this._renderNavigationHandleVisible();

  this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
  this.desktop.on('propertyChange', this._desktopPropertyChangeHandler);
  this.desktop.on('outlineChange', this._desktopOutlineChangeHandler);
  this.desktop.on('animationEnd', this._desktopAnimationEndHandler);
};

scout.DesktopBench.prototype._createLayout = function() {
  return new scout.FlexboxLayout(scout.FlexboxLayout.Direction.ROW, this.layoutCacheKey);
};

scout.DesktopBench.prototype.visibleColumns = function() {
  return this.columns.filter(function(column) {
    return column.hasViews();
  });
};

scout.DesktopBench.prototype._renderColumns = function() {
  this.visibleColumns().forEach(function(column) {
    this._renderColumn(column);
  }, this);
  this.updateFirstLastMarker();
};

scout.DesktopBench.prototype._renderColumn = function(column) {
  if (!column || column.rendered) {
    return;
  }
  column.render();
};

scout.DesktopBench.prototype._remove = function() {
  this.desktop.off('propertyChange', this._desktopPropertyChangeHandler);
  this.desktop.off('outlineChange', this._desktopOutlineChangeHandler);
  this.desktop.off('animationEnd', this._desktopAnimationEndHandler);
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
  scout.DesktopBench.parent.prototype._remove.call(this);
};

scout.DesktopBench.prototype.updateFirstLastMarker = function() {
  this.visibleColumns().forEach(function(column, index, arr) {
    column.$container.removeClass('first last');
    if (index === 0) {
      column.$container.addClass('first');
    }
    if (index === arr.length - 1) {
      column.$container.addClass('last');
    }
  }, this);
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

};

scout.DesktopBench.prototype._removeOutlineContent = function() {
  if (!this.outlineContent) {
    return;
  }
  this.removeView(this.outlineContent, false);
};

scout.DesktopBench.prototype._createNavigationHandle = function() {
  return scout.create('DesktopNavigationHandle', {
    parent: this,
    leftVisible: false
  });
};

scout.DesktopBench.prototype._renderNavigationHandle = function() {
  if (this.navigationHandle) {
    return;
  }
  this.navigationHandle = this._createNavigationHandle();
  this.navigationHandle.render();
  this.navigationHandle.addCssClass('navigation-closed');
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
  this.$container.toggleClass('has-navigation-handle', this.navigationHandleVisible);
};

/**
 * is called in post render of desktop used to initialize the ui state. E.g. show default views
 */
scout.DesktopBench.prototype.postRender = function() {
  this.columns.forEach(function(column) {
    column.postRender();
  });
};

scout.DesktopBench.prototype.setChanging = function(changing) {
  if (changing) {
    this.changingCounter++;
  } else {
    this.changingCounter--;
  }
  if (this.changingCounter === 0 && this.changed && this.rendered) {
    this.htmlComp.layout.reset();
    this.htmlComp.invalidateLayoutTree();
    this.htmlComp.validateLayoutTree();
    this.changed = false;
  }
  this.chaningCounter = Math.max(this.changingCounter - 1, 0);
};

scout.DesktopBench.prototype.updateLayoutData = function(layoutData) {
  if (this.getLayoutData() === layoutData) {
    return;
  }
  this.setLayoutData(layoutData);

  // update columns
  var columnDatas = layoutData.getColumns();

  this.columns.forEach(function(c, i) {
    var cacheKey;
    if (this.layoutCacheKey && this.layoutCacheKey.length > 0) {
      cacheKey = this.layoutCacheKey.slice();
      cacheKey.push('column' + i);
    }
    c.updateLayoutData(columnDatas[i], cacheKey);
  }.bind(this));
  if (this.rendered) {
    this.htmlComp.layout.setCacheKey(this.layoutCacheKey);
    this.htmlComp.layout.reset();
    this.htmlComp.invalidateLayoutTree();
    this.htmlComp.validateLayoutTree();
  }
  this._updateSplitterMovable();
};

scout.DesktopBench.prototype.setLayoutData = function(layoutData) {
  if (this.layoutData === layoutData) {
    return;
  }
  scout.DesktopBench.parent.prototype.setLayoutData.call(this, layoutData);
  this.layoutData = layoutData;
  this.layoutCacheKey = [];
  if (layoutData.cacheKey) {
    this.layoutCacheKey.push(layoutData.cacheKey);
  }
};

scout.DesktopBench.prototype.getLayoutData = function() {
  return this.layoutData;
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
    oldContent.off('destroy', this._outlineContentDestroyHandler);
  }
  if (this.rendered) {
    this._removeOutlineContent();
  }
  // add a destroy listener to the outline-content, so we can reset the property - otherwise we'd work
  // with a potentially destroyed content which would cause an error later, when we try to render the
  // bench with the outline-content.
  if (content) {
    content.one('destroy', this._outlineContentDestroyHandler);
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
  var content,
    selectedPage = this.outline.selectedNode();
  if (selectedPage) {
    // Outline does not support multi selection
    content = this._computeDetailContentForPage(selectedPage);
  } else {
    if (this.outline.defaultDetailForm) {
      content = this._computeDefaultDetailForm();
    } else if (this.outline.outlineOverview) {
      content = this._computeOutlineOverview();
    }
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

scout.DesktopBench.prototype._onDesktopOutlineChange = function(event) {
  this.setOutline(this.desktop.outline);
  this.updateNavigationHandleVisibility();
};

scout.DesktopBench.prototype._onoutlineContentDestroy = function(event) {
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
  var selectedPage = this.outline.selectedNode();
  if (!event.page && !selectedPage || event.page === selectedPage) {
    this.updateOutlineContent();
  }
};

scout.DesktopBench.prototype._onOutlinePropertyChange = function(event) {
  if (scout.isOneOf(event.propertyName, ['defaultDetailForm', 'outlineOverview'])) {
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
scout.DesktopBench.prototype._onBenchLayoutDataChange = function(event) {
  this.updateLayoutData(this.desktop.benchLayoutData);
};

scout.DesktopBench.prototype._onDesktopPropertyChange = function(event) {
  if (event.propertyName === 'navigationVisible') {
    this._onDesktopNavigationVisibleChange();
  } else if (event.propertyName === 'navigationHandleVisible') {
    this._onDesktopNavigationHandleVisibleChange();
  }
  if (event.propertyName === 'benchLayoutData') {
    this._onBenchLayoutDataChange();
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
  this.components = this.visibleColumns()
    .reduce(function(arr, col) {
      if (arr.length > 0) {
        // add sep
        var splitter = scout.create('Splitter', {
          parent: this,
          $anchor: arr[arr.length - 1].$container,
          $root: this.$container,
          maxRatio: 1
        });
        splitter.render();
        splitter.setLayoutData(scout.FlexboxLayoutData.fixed().withOrder(col.getLayoutData().order - 1));
        splitter.$container.addClass('line');

        arr.push(splitter);
      }
      arr.push(col);
      return arr;
    }.bind(this), []);
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
  this._updateSplitterMovable();
};

scout.DesktopBench.prototype._updateSplitterMovable = function() {
  if (!this.components) {
    return;
  }
  this.components.forEach(function(c, i) {
    if (c instanceof scout.Splitter) {
      var componentsBefore = this.components.slice(0, i).reverse();
      var componentsAfter = this.components.slice(i + 1);
      // shrink
      if (
        componentsBefore.filter(function(c) {
          return c.getLayoutData().shrink > 0;
        }).length > 0 &&
        componentsAfter.filter(function(c) {
          return c.getLayoutData().grow > 0;
        }).length > 0
      ) {
        c.setEnabled(true);
        c.on('move', this._onSplitterMove.bind(this));
        return;
      }
      // grow
      if (
        componentsBefore.filter(function(c) {
          return c.getLayoutData().grow > 0;
        }).length > 0 &&
        componentsAfter.filter(function(c) {
          return c.getLayoutData().shrink > 0;
        }).length > 0
      ) {
        c.setEnabled(true);
        c.on('move', this._onSplitterMove.bind(this));
        return;
      }
      c.setEnabled(false);

    }
  }.bind(this));
};

scout.DesktopBench.prototype._onSplitterMove = function(event) {
  var splitter = event.source;
  var diff = event.position - splitter.htmlComp.location().x - splitter.htmlComp.margins().left - splitter.htmlComp.insets().left;
  splitter.getLayoutData().diff = diff;
  this.revalidateLayout();
  splitter.getLayoutData().diff = null;
  event.preventDefault();
};

scout.DesktopBench.prototype._onViewAdd = function(event) {
  this.trigger('viewAdd', {
    view: event.view
  });
};

scout.DesktopBench.prototype._onViewRemove = function(event) {
  this.trigger('viewRemove', {
    view: event.view
  });
};

scout.DesktopBench.prototype._onViewActivate = function(event) {
  var view = event.view;
  if (this.outlineContent === view) {
    this.desktop.bringOutlineToFront(this.desktop.outline);
  }
  this.trigger('viewActivate', {
    view: view
  });
};

scout.DesktopBench.prototype._onViewDeactivate = function(event) {
  if (this.outlineContent === event.view) {
    this.desktop.sendOutlineToBack();
  }
  this.trigger('viewDeactivate', {
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
      this.updateFirstLastMarker();
      if (this.changingCounter > 0) {
        this.changed = true;
      } else {
        this.htmlComp.layout.reset();
        this.htmlComp.invalidateLayoutTree();
        // Layout immediate to prevent 'laggy' form visualization,
        // but not initially while desktop gets rendered because it will be done at the end anyway
        this.htmlComp.validateLayoutTree();
      }
    }
  }
};

scout.DesktopBench.prototype.activateView = function(view) {
  // activate views is only for existing views allowed.
  if (!this.hasView(view)) {
    return;
  }
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
      this._revalidateSplitters();
      this.updateFirstLastMarker();
      if (this.changingCounter > 0) {
        this.changed = true;
      } else {
        this.htmlComp.layout.reset();
        this.htmlComp.invalidateLayoutTree();
        // Layout immediate to prevent 'laggy' form visualization,
        // but not initially while desktop gets rendered because it will be done at the end anyway
        this.htmlComp.validateLayoutTree();
      }
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

scout.DesktopBench.prototype.hasView = function(view) {
  return this.columns.filter(function(column) {
    return column.hasView(view);
  }).length > 0;
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

/**
 * @returns all the currently active views (the selected ones) of all the visible tab boxes
 */
scout.DesktopBench.prototype.activeViews = function() {
  var activeViews = [];
  this.visibleColumns().forEach(function(column) {
    column.visibleTabBoxes().forEach(function(tabBox) {
      activeViews.push(tabBox.currentView);
    });
  });
  return activeViews;
};
