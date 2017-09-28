/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.BenchColumn = function() {
  scout.BenchColumn.parent.call(this);
  this.htmlComp;
  this.tabBoxes = [];
  this._widgetToTabBox = {}; // [key=viewId, value=SimpleTabBox instance]
  this.components;
  this._removeViewInProgress = 0;
  this.layoutData;

  // event listener functions
  this._viewAddHandler = this._onViewAdd.bind(this);
  this._viewRemoveHandler = this._onViewRemove.bind(this);
  this._viewActivateHandler = this._onViewActivate.bind(this);
  this._viewDeactivateHandler = this._onViewDeactivate.bind(this);
};
scout.inherits(scout.BenchColumn, scout.Widget);

scout.BenchColumn.TAB_BOX_INDEX = {
  TOP: 0,
  CENTER: 1,
  BOTTOM: 2
};

scout.DesktopBench.TAB_BOX_CLASSES = [
  'north',
  'center',
  'south'
];

scout.BenchColumn.prototype._init = function(model) {
  scout.BenchColumn.parent.prototype._init.call(this, model);
  this.layoutData = model.layoutData;
  this.layoutCacheKey = model.cacheKey;
  this.cssClass = model.cssClass;
  this._createTabBoxes();
};

/**
 * Returns a $container used as a bind target for the key-stroke context of the group-box.
 * By default this function returns the container of the form, or when group-box is has no
 * form as a parent the container of the group-box.
 */
scout.BenchColumn.prototype._keyStrokeBindTarget = function() {
  return this.$container;
};

scout.BenchColumn.prototype._render = function() {
  this.$container = this.$parent.appendDiv('bench-column');
  if (this.cssClass) {
    this.$container.addClass(this.cssClass);
  }
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());

  this.htmlComp.layoutData = this.getLayoutData();
};

scout.BenchColumn.prototype._renderProperties = function() {
  scout.BenchColumn.parent.prototype._renderProperties.call(this);
  this._renderTabBoxes();
  this._revalidateSplitters();
};

scout.BenchColumn.prototype._renderTabBoxes = function() {
  this.visibleTabBoxes().forEach(function(tabBox) {
    this._renderTabBox(tabBox);
  }.bind(this));
  this.updateFirstLastMarker();
};

scout.BenchColumn.prototype._renderTabBox = function(tabBox) {
  if (!tabBox.rendered) {
    tabBox.render();
  }
};

scout.BenchColumn.prototype.postRender = function() {
  this.tabBoxes.forEach(function(tabBox) {
    tabBox.postRender();
  });
};

scout.BenchColumn.prototype._createLayout = function() {
  return new scout.FlexboxLayout(scout.FlexboxLayout.Direction.COLUMN, this.layoutCacheKey);
};

scout.BenchColumn.prototype.updateLayoutData = function(layoutData, cacheKey) {
  if (this.getLayoutData() === layoutData) {
    return;
  }
  this.layoutCacheKey = cacheKey;
  this.setLayoutData(layoutData);

  // update columns
  var rowDatas = this.layoutData.getRows();
  this.tabBoxes.forEach(function(tb, i) {
    tb.setLayoutData(rowDatas[i]);
  });
  this._updateSplitterMovable();
  if (this.rendered) {
    this.htmlComp.layout.setCacheKey(this.layoutCacheKey);
    this.htmlComp.layout.reset();
    this.htmlComp.invalidateLayoutTree();
  }
};

scout.BenchColumn.prototype.setLayoutData = function(layoutData) {
  scout.BenchColumn.parent.prototype.setLayoutData.call(this, layoutData);
  this.layoutData = layoutData;
};

scout.BenchColumn.prototype.getLayoutData = function() {
  return this.layoutData;
};

scout.BenchColumn.prototype._onViewAdd = function(event) {
  this.trigger('viewAdd', {
    view: event.view
  });
};

scout.BenchColumn.prototype._onViewRemove = function(event) {
  this.trigger('viewRemove', {
    view: event.view
  });
};

scout.BenchColumn.prototype._onViewActivate = function(event) {
  this.trigger('viewActivate', {
    view: event.view
  });
};

scout.BenchColumn.prototype._onViewDeactivate = function(event) {
  this.trigger('viewDeactivate', {
    view: event.view
  });
};

scout.BenchColumn.prototype.activateView = function(view) {
  var tabBox = this.getTabBox(view.displayViewId);
  tabBox.activateView(view);
};

scout.BenchColumn.prototype._createTabBoxes = function() {
  var rowLayoutDatas = [];
  if (this.layoutData) {
    rowLayoutDatas = this.layoutData.getRows();
  }
  for (var i = 0; i < 3; i++) {
    var tabBox = scout.create('SimpleTabBox', {
      parent: this,
      cssClass: scout.DesktopBench.TAB_BOX_CLASSES[i],
      controller: scout.create('DesktopTabBoxController')
    });
    tabBox.setLayoutData(rowLayoutDatas[i]);
    tabBox.on('viewAdd', this._viewAddHandler);
    tabBox.on('viewRemove', this._viewRemoveHandler);
    tabBox.on('viewActivate', this._viewActivateHandler);
    tabBox.on('viewDeactivate', this._viewDeactivateHandler);
    this.tabBoxes.push(tabBox);
  }
};

scout.BenchColumn.prototype._revalidateSplitters = function(clearPosition) {
  // remove old splitters
  if (this.components) {
    this.components.forEach(function(comp) {
      if (comp instanceof scout.Splitter) {
        comp.destroy();
      }
    });
  }
  this.components = this.visibleTabBoxes()
    .reduce(function(arr, col) {
      if (arr.length > 0) {
        // add sep
        var splitter = scout.create('Splitter', {
          parent: this,
          $anchor: arr[arr.length - 1].$container,
          $root: this.$container,
          splitHorizontal: false,
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
      return comp instanceof scout.SimpleTabBox;
    })
    .reduce(function(c1, c2, index) {
      if (index > 0) {
        c2.$container.insertAfter(c1.$container);
      }
      return c2;
    }, undefined);
  this._updateSplitterMovable();
};

scout.BenchColumn.prototype._updateSplitterMovable = function() {
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

scout.BenchColumn.prototype._onSplitterMove = function(event) {
  var splitter = event.source;
  var diff = event.position - splitter.htmlComp.location().y - splitter.htmlComp.margins().top - splitter.htmlComp.insets().top;
  splitter.getLayoutData().diff = diff;
  this.revalidateLayout();
  splitter.getLayoutData().diff = null;
  event.preventDefault();
};

scout.BenchColumn.prototype.addView = function(view, bringToFront) {
  var tabBox = this.getTabBox(view.displayViewId);
  this._widgetToTabBox[view.id] = tabBox;

  tabBox.addView(view, bringToFront);

  if (this.rendered && tabBox.viewCount() === 1) {
    if (!tabBox.rendered) {
      // lazy render if the first view is added.
      tabBox.render();
    }
    this._revalidateSplitters(true);
    this.updateFirstLastMarker();
    this.htmlComp.layout.reset();
    this.htmlComp.invalidateLayoutTree();
    // Layout immediate to prevent 'laggy' form visualization,
    // but not initially while desktop gets rendered because it will be done at the end anyway
    this.htmlComp.validateLayoutTree();
  }
};

scout.BenchColumn.prototype.getTabBox = function(displayViewId) {
  var tabBox;
  switch (displayViewId) {
    case 'NW':
    case 'N':
    case 'NE':
      tabBox = this.tabBoxes[scout.BenchColumn.TAB_BOX_INDEX.TOP];
      break;
    case 'SW':
    case 'S':
    case 'SE':
      tabBox = this.tabBoxes[scout.BenchColumn.TAB_BOX_INDEX.BOTTOM];
      break;
    default:
      tabBox = this.tabBoxes[scout.BenchColumn.TAB_BOX_INDEX.CENTER];
      break;
  }
  return tabBox;
};

scout.BenchColumn.prototype.removeView = function(view, showSiblingView) {
  var tabBox = this._widgetToTabBox[view.id];
  if (tabBox) {
    this._removeViewInProgress++;
    tabBox.removeView(view, showSiblingView);
    this._removeViewInProgress--;
    delete this._widgetToTabBox[view.id];
    if (this.rendered && tabBox.viewCount() === 0 && this._removeViewInProgress === 0) {
      // remove view area if no view is left.
      tabBox.remove();
      this._revalidateSplitters(true);
      this.updateFirstLastMarker();
      this.htmlComp.layout.reset();
      this.htmlComp.invalidateLayoutTree();
      // Layout immediate to prevent 'laggy' form visualization,
      // but not initially while desktop gets rendered because it will be done at the end anyway
      this.htmlComp.validateLayoutTree();
    }
  }
};

scout.BenchColumn.prototype.viewCount = function() {
  return this.tabBoxes.map(function(tabBox) {
    return tabBox.viewCount();
  }).reduce(function(c1, c2) {
    return c1 + c2;
  }, 0);
};

scout.BenchColumn.prototype.hasView = function(view) {
  return this.tabBoxes.filter(function(tabBox) {
    return tabBox.hasView(view);
  }).length > 0;
};

scout.BenchColumn.prototype.hasViews = function() {
  return this.viewCount() > 0;
};

scout.BenchColumn.prototype.getViews = function(displayViewId) {
  return this.tabBoxes.reduce(function(arr, tabBox) {
    scout.arrays.pushAll(arr, tabBox.getViews(displayViewId));
    return arr;
  }, []);
};

scout.BenchColumn.prototype.getComponents = function() {
  return this.components;
};

scout.BenchColumn.prototype.visibleTabBoxes = function() {
  return this.tabBoxes.filter(function(tabBox) {
    return tabBox.hasViews();
  });
};

scout.BenchColumn.prototype.updateFirstLastMarker = function() {
  this.visibleTabBoxes().forEach(function(tab, index, arr) {
    tab.$container.removeClass('first last');
    if (index === 0) {
      tab.$container.addClass('first');
    }
    if (index === arr.length - 1) {
      tab.$container.addClass('last');
    }
  }, this);
};
