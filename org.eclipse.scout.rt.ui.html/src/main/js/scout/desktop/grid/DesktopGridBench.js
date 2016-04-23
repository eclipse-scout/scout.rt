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
scout.DesktopGridBench = function() {
  scout.DesktopGridBench.parent.call(this);
  this.htmlComp;
  this.VIEW_AREA_COLUMN_INDEX = {
    LEFT: 0,
    CENTER: 1,
    RIGHT: 2
  };
  this.viewAreaColumns = [];
  this.components;
  //  this.splitters = {
  //    left: undefined,
  //    right: undefined
  //  };
  this._viewTabMap = {}; // [key=viewId, value=ViewArea instance]
  this._desktopOutlineChangedHandler = this._onDesktopOutlineChanged.bind(this);
  this._outlineNodesSelectedHandler = this._onOutlineNodesSelected.bind(this);
  this._outlinePageChangedHandler = this._onOutlinePageChanged.bind(this);
  this._outlinePropertyChangeHandler = this._onOutlinePropertyChange.bind(this);

  // event listener functions
  this._viewAddedHandler = this._onViewAdded.bind(this);
  this._viewRemovedHandler = this._onViewRemoved.bind(this);
  this._viewActivatedHandler = this._onViewActivated.bind(this);
  this._viewDeactivatedHandler = this._onViewDeactivated.bind(this);

  this._addEventSupport();
};
scout.inherits(scout.DesktopGridBench, scout.Widget);

scout.DesktopGridBench.VIEW_MIN_HEIGHT; // Configured in sizes.css
scout.DesktopGridBench.VIEW_MIN_WIDTH; // Configured in sizes.css

scout.DesktopGridBench.prototype._init = function(model) {
  scout.DesktopGridBench.parent.prototype._init.call(this, model);

  scout.DesktopGridBench.VIEW_MIN_HEIGHT = $.pxToNumber(scout.styles.get('view-box', 'min-height').minHeight);
  scout.DesktopGridBench.VIEW_MIN_WIDTH = $.pxToNumber(scout.styles.get('view-box', 'min-width').minWidth);

  this._createViewAreaColumns();
  this.desktop = this.session.desktop;
  this.headerViewTabBox = model.headerViewTabBox;
  this.outlineContentVisible = scout.nvl(model.outlineContentVisible, true);
  this.setOutline(this.desktop.outline);
  // controller for headerViewTabBox
  this.headerViewTabBoxController = new scout.HeaderViewTabBoxController(this, this.headerViewTabBox);
};

scout.DesktopGridBench.prototype._createViewAreaColumns = function() {
  for (var i = 0; i < 3; i++) {
    var viewAreaCol = scout.create('ViewAreaColumn', {
      parent: this
    });
    viewAreaCol.on('viewAdded', this._viewAddedHandler);
    viewAreaCol.on('viewRemoved', this._viewRemovedHandler);
    viewAreaCol.on('viewActivated', this._viewActivatedHandler);
    viewAreaCol.on('viewDeactivated', this._viewDeactivatedHandler);
    this.viewAreaColumns.push(viewAreaCol);
  }
};

scout.DesktopGridBench.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.DesktopGridBench.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  // Bound to desktop
  this.desktopKeyStrokeContext = new scout.KeyStrokeContext();
  this.desktopKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.desktopKeyStrokeContext.$bindTarget = this.desktop.$container;
  //  function(){
  //    return
  //  };
  this.desktopKeyStrokeContext.$scopeTarget = this.$container;
  this.desktopKeyStrokeContext.registerKeyStroke(this.desktop.keyStrokes);
};

scout.DesktopGridBench.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('desktop-grid-bench');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);

  //  this._renderOrAttachOutlineContent();
  this.htmlComp.setLayout(new scout.DesktopGridBenchLayout(this));

  this._renderViewColumns();
  this._revalidateSplitters();
  this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
  //TODO CGU maybe better create destroy(), call setOutline in init and attach outline listener in init/destroy
  this.desktop.on('outlineChanged', this._desktopOutlineChangedHandler);
};

scout.DesktopGridBench.prototype._renderViewColumns = function() {
  this.viewAreaColumns.forEach(function(column) {
    if (column.viewCount() > 0) {
      this._renderViewColumn(column);

    }
  }.bind(this));
};

scout.DesktopGridBench.prototype._renderViewColumn = function(viewColumn) {
  if (!viewColumn || viewColumn.rendered) {
    return;
  }

  viewColumn.render(this.$container);
};

scout.DesktopGridBench.prototype._remove = function() {
  this.desktop.off('outlineChanged', this._desktopOutlineChangedHandler);
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
  scout.DesktopGridBench.parent.prototype._remove.call(this);

};

scout.DesktopGridBench.prototype._renderOrAttachOutlineContent = function() {
  if (!this.outlineContent || this.desktop.inBackground) {
    return;
  }
  //  this.showView(this.outlineContent);
  if (!this.outlineContent.rendered) {
    this._renderOutlineContent();
  } else if (!this.outlineContent.attached) {
    this.outlineContent.attach();
  }
};

scout.DesktopGridBench.prototype._renderOutlineContent = function() {
  if (!this.outlineContent || this.desktop.inBackground) {
    return;
  }

  this.showView(this.outlineContent);
  //  this.outlineContent.render(this.$container);
  //  this.outlineContent.htmlComp.validateRoot = true;
  //  this.outlineContent.setParent(this);
  //  this.outlineContent.invalidateLayoutTree(false);

  // Layout immediate to prevent 'laggy' form visualization,
  // but not initially while desktop gets rendered because it will be done at the end anyway
  if (this.desktop.rendered) {
    //    this.outlineContent.validateLayoutTree();

    // Request focus on first element in outline content
    this.session.focusManager.validateFocus();
  }
  if (this.outlineContent instanceof scout.Table) {
    this.outlineContent.restoreScrollPosition();
  }
};

scout.DesktopGridBench.prototype._removeOutlineContent = function() {
  if (!this.outlineContent) {
    return;
  }
  if (this.outlineContent instanceof scout.Table) {
    this.outlineContent.storeScrollPosition();
  }
  this.removeView(this.outlineContent, false);

};

scout.DesktopGridBench.prototype.setOutline = function(outline) {
  if (this.outline) {
    this.outline.off('nodesSelected', this._outlineNodesSelectedHandler);
    this.outline.off('pageChanged', this._outlinePageChangedHandler);
    this.outline.off('propertyChange', this._outlinePropertyChangeHandler);
  }
  this.outline = outline;
  if (this.outline) {
    this.outline.on('nodesSelected', this._outlineNodesSelectedHandler);
    this.outline.on('pageChanged', this._outlinePageChangedHandler);
    this.outline.on('propertyChange', this._outlinePropertyChangeHandler);
  }
  this.updateOutlineContent();
};

scout.DesktopGridBench.prototype.setOutlineContent = function(content) {
  var oldContent = this.outlineContent;
  if (this.outlineContent === content) {
    return;
  }

  if (this.rendered) {
    this._removeOutlineContent();
  }
  // reset view tab relevant properties.
  if (content) {
    delete content.title;
    delete content.subTitle;
    delete content.iconId;
  }
  this._setProperty('outlineContent', content);
  // Inform header that outline content has changed
  // (having a listener in the header is quite complex due to initialization phase, a direct call here is much easier to implement)
  //  if (this.desktop.header) {
  //    this.desktop.header.onBenchOutlineContentChange(content, oldContent);
  //  }
  if (content) {
    this.showView(content);
  }
  //  if (this.rendered) {
  //    this._renderOrAttachOutlineContent();
  //  }
};

scout.DesktopGridBench.prototype.setOutlineContentVisible = function(visible) {
  if (visible === this.outlineContentVisible) {
    return;
  }
  this.outlineContentVisible = visible;
  this.updateOutlineContent();
};

scout.DesktopGridBench.prototype.bringToFront = function() {
  if (!this.outlineContent) {
    return;
  }
  this.showView(this.outlineContent);
  //  this._renderOrAttachOutlineContent();
};

scout.DesktopGridBench.prototype.sendToBack = function() {
  if (this.outlineContent) {
    this.outlineContent.detach();
  }
};

scout.DesktopGridBench.prototype._computeDefaultDetailForm = function() {
  return this.outline.defaultDetailForm;
};

scout.DesktopGridBench.prototype._computeOutlineOverview = function() {
  return this.outline.outlineOverview;
};

scout.DesktopGridBench.prototype._computeDetailContentForPage = function(node) {
  if (!node) {
    throw new Error('called _showDetailContentForPage without node');
  }

  var content;
  if (node.detailForm && node.detailFormVisible && node.detailFormVisibleByUi) {
    content = node.detailForm;
  } else if (node.detailTable && node.detailTableVisible) {
    content = node.detailTable;
  }

  return content;

};

scout.DesktopGridBench.prototype.updateOutlineContent = function() {
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

scout.DesktopGridBench.prototype.updateOutlineContentDebounced = function() {
  clearTimeout(this._updateOutlineContentTimeout);
  this._updateOutlineContentTimeout = setTimeout(function() {
    this.updateOutlineContent();
  }.bind(this), 300);
};

scout.DesktopGridBench.prototype._onDesktopOutlineChanged = function(event) {
  this.setOutline(this.desktop.outline);
};

scout.DesktopGridBench.prototype._onOutlineNodesSelected = function(event) {
  if (event.debounce) {
    this.updateOutlineContentDebounced();
  } else {
    this.updateOutlineContent();
  }
};

scout.DesktopGridBench.prototype._onOutlinePageChanged = function(event) {
  var selectedPage = this.outline.selectedNodes[0];
  if (!event.page && !selectedPage || event.page === selectedPage) {
    this.updateOutlineContent();
  }
};

scout.DesktopGridBench.prototype._onOutlinePropertyChange = function(event) {
  if (event.changedProperties.indexOf('defaultDetailForm') !== -1) {
    this.updateOutlineContent();
  }
};

scout.DesktopGridBench.prototype._revalidateSplitters = function() {
  // remove old splitters
  if (this.components) {
    this.components.forEach(function(comp) {
      if (comp instanceof scout.Splitter) {
        comp.remove();
      }
    });
  }
  var splitterParent = this;
  this.components = this.viewAreaColumns.filter(function(viewAreaCol) {
    return viewAreaCol.hasViews();
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
      splitter.on('splitterMove', splitterParent._onSplitterMove.bind(splitterParent));
      splitter.on('splitterPositionChanged', splitterParent._onSplitterPositionChanged.bind(splitterParent));
      arr.push(splitter);
    }
    arr.push(col);
    return arr;
  }, []);
  // well order the dom elements (reduce is used for simple code reasons, the result of reduce is not of interest).
  this.components.filter(function(comp) {
      return comp instanceof scout.ViewAreaColumn;
    })
    .reduce(function(c1, c2, index) {
      if (index > 0) {
        c2.$container.insertAfter(c1.$container);
      }
      return c2;
    }, undefined);

  this.htmlComp.invalidateLayoutTree();
  // Layout immediate to prevent 'laggy' form visualization,
  // but not initially while desktop gets rendered because it will be done at the end anyway
  this.htmlComp.validateLayoutTree();
};

scout.DesktopGridBench.prototype._onSplitterMove = function(event) {
  var splitterIndex = this.components.indexOf(event.source);
  if (splitterIndex > 0 /*cannot be 0 since first element is a ViewAreaColumn*/ ) {
    var $before = this.components[splitterIndex - 1].$container,
      $after = this.components[splitterIndex + 1].$container,
      diff = event.position - event.source.position;

    if (($before.width() + diff) < scout.DesktopGridBench.VIEW_MIN_WIDTH) {
      // set to min
      event.setPosition($before.position().left + scout.DesktopGridBench.VIEW_MIN_WIDTH);
    }
    if (($after.position().left + $after.width() - event.position) < scout.DesktopGridBench.VIEW_MIN_WIDTH) {
      event.setPosition($after.position().left + $after.width() - scout.DesktopGridBench.VIEW_MIN_WIDTH);
    }
  }
};
scout.DesktopGridBench.prototype._onSplitterPositionChanged = function(event) {
  this.revalidateLayout();
};

scout.DesktopGridBench.prototype._onViewAdded = function(event) {
  this.trigger('viewAdded', {
    view: event.view
  });
};

scout.DesktopGridBench.prototype._onViewRemoved = function(event) {
  this.trigger('viewRemoved', {
    view: event.view
  });
};

scout.DesktopGridBench.prototype._onViewActivated = function(event) {
  if (this.outlineContent === event.view) {
    this.desktop.bringOutlineToFront(this.desktop.outline);
  }
  this.trigger('viewActivated', {
    view: event.view
  });
};

scout.DesktopGridBench.prototype._onViewDeactivated = function(event) {
  if (this.outlineContent === event.view) {
    this.desktop.sendOutlineToBack();
  }
  this.trigger('viewDeactivated', {
    view: event.view
  });
};

scout.DesktopGridBench.prototype.showView = function(view) {
  if (!view.displayViewId || view.displayViewId === '') {
    view.displayViewId = 'C';
  }
  var viewAreaColumn = this._getViewColumn(view.displayViewId);
  this._viewTabMap[view.id] = viewAreaColumn;
  viewAreaColumn.showView(view);

  if (this.rendered) {
    if (viewAreaColumn.viewCount() === 1) {
      this._renderViewColumn(viewAreaColumn);
      this._revalidateSplitters();
    }
  }
};

scout.DesktopGridBench.prototype._getViewColumn = function(displayViewId) {
  var viewAreaColumn;

  switch (displayViewId) {
    case 'NW':
    case 'W':
    case 'SW':
      viewAreaColumn = this.viewAreaColumns[this.VIEW_AREA_COLUMN_INDEX.LEFT];
      break;
    case 'NE':
    case 'E':
    case 'SE':
      viewAreaColumn = this.viewAreaColumns[this.VIEW_AREA_COLUMN_INDEX.RIGHT];
      break;
    default:
      viewAreaColumn = this.viewAreaColumns[this.VIEW_AREA_COLUMN_INDEX.CENTER];
      break;
  }
  return viewAreaColumn;
};
scout.DesktopGridBench.prototype.removeView = function(view, showSiblingView) {
  var viewAreaColumn = this._viewTabMap[view.id];
  if (viewAreaColumn) {
    viewAreaColumn.removeView(view, showSiblingView);
    delete this._viewTabMap[view.id];
    // remove if empty
    if (this.rendered && viewAreaColumn.viewCount() === 0) {
      viewAreaColumn.remove();
      this._revalidateSplitters(true);
    }
  }
};

scout.DesktopGridBench.prototype.getComponents = function() {
  return this.components;
};

scout.DesktopGridBench.prototype.getViewArea = function(displayViewId) {
  var viewColumn = this._getViewColumn(displayViewId);
  if (!viewColumn) {
    return;
  }
  return viewColumn.getViewArea(displayViewId);
};

scout.DesktopGridBench.prototype.getViews = function(displayViewId) {
  return this.viewAreaColumns.reduce(function(arr, column) {
    Array.prototype.push.apply(arr, column.getViews(displayViewId));
    return arr;
  }, []);
};
