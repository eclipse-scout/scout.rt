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
  //  this._desktopOutlineChangedHandler = this._onDesktopOutlineChanged.bind(this);
  //  this._outlineNodesSelectedHandler = this._onOutlineNodesSelected.bind(this);
  //  this._outlinePageChangedHandler = this._onOutlinePageChanged.bind(this);
  //  this._outlinePropertyChangeHandler = this._onOutlinePropertyChange.bind(this);
  this._addEventSupport();
};
scout.inherits(scout.DesktopGridBench, scout.Widget);

scout.DesktopGridBench.prototype._init = function(model) {

  scout.DesktopGridBench.parent.prototype._init.call(this, model);
  this._createViewAreaColumns();
  this.desktop = this.session.desktop;
  this.outlineContentVisible = scout.nvl(model.outlineContentVisible, true);
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
  this.htmlComp.setLayout(new scout.DesktopGridBenchLayout(this));
  //  this.setOutline(this.desktop.outline); //TODO CGU maybe better create destroy(), call setOutline in init and attach outline listener in init/destroy
  //  this._renderOrAttachOutlineContent();
  //  this._renderOrAttachViewAreaColumns();
  //  this._revalidateSplitters(true);
  this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
  //  this.desktop.on('outlineChanged', this._desktopOutlineChangedHandler);
};

scout.DesktopGridBench.prototype._remove = function() {
  this.desktop.off('outlineChanged', this._desktopOutlineChangedHandler);
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
  scout.DesktopGridBench.parent.prototype._remove.call(this);
};

scout.DesktopGridBench.prototype.setOutlineContentVisible = function(outlineContentVisible) {

};


scout.DesktopGridBench.prototype._revalidateSplitters = function(clearPosition) {
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
      //      splitter.$container.insertAfter(this.viewAreaColumns.LEFT.$container);
      splitter.on('resize', splitterParent._onSplitterResize.bind(splitterParent));
      arr.push(splitter);
    }
    arr.push(col);
    return arr;
  }, []);
  // well order the dom elements (reduce is used for simple code reasons, the result of reduce is not of interest).
  this.components.reduce(function(c1, c2, index) {
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

scout.DesktopGridBench.prototype._onSplitterResize = function() {
  this.revalidateLayout();
};

scout.DesktopGridBench.prototype._createViewAreaColumns = function() {
  for (var i = 0; i < 3; i++) {
    this.viewAreaColumns.push(scout.create('ViewAreaColumn', {
      parent: this
    }));
  }
};

scout.DesktopGridBench.prototype.renderView = function(view) {
  if (view.rendered) {
    throw new Error('view already rendered');
  }
  //  if (!this._desktop.bench) {
  //    throw new Error('Bench not available');
  //  }
  var viewAreaColumn;
  switch (view.displayViewId) {
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
  this._viewTabMap[view.id] = viewAreaColumn;
  viewAreaColumn.showView(view);
  if (viewAreaColumn.viewCount() === 1) {
    this._revalidateSplitters(true);
  }
};

scout.DesktopGridBench.prototype.removeView = function(view) {
  var viewAreaColumn = this._viewTabMap[view.id];
  if (viewAreaColumn) {
    viewAreaColumn.removeView(view);
    delete this._viewTabMap[view.id];
    if (viewAreaColumn.viewCount() === 0) {
      this._revalidateSplitters(true);
    }
  }

};


scout.DesktopGridBench.prototype.getComponents = function() {
  return this.components;
};
