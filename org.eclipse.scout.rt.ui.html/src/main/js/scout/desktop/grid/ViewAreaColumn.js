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
scout.ViewAreaColumn = function() {
  scout.ViewAreaColumn.parent.call(this);
  this.htmlComp;
  this.VIEW_AREA_INDEX = {
    TOP: 0,
    CENTER: 1,
    BOTTOM: 2
  };
  this.viewAreas = [];
  this._viewToViewArea = {}; // [key=viewId, value=ViewArea instance]
  this.components;

};
scout.inherits(scout.ViewAreaColumn, scout.ModelAdapter);

scout.ViewAreaColumn.prototype._init = function(model) {
  scout.ViewAreaColumn.parent.prototype._init.call(this, model);

  this._createViewAreas();
  //  this.menuBar = scout.create('MenuBar', {
  //    parent: this,
  //    menuOrder: new scout.GroupBoxMenuItemsOrder()
  //  });
};

/**
 * @override ModelAdapter.js
 */
scout.ViewAreaColumn.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.ViewAreaColumn.parent.prototype._initKeyStrokeContext.call(this, this.keyStrokeContext);
  this.keyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.keyStrokeContext.$bindTarget = this._keyStrokeBindTarget.bind(this);
};

/**
 * Returns a $container used as a bind target for the key-stroke context of the group-box.
 * By default this function returns the container of the form, or when group-box is has no
 * form as a parent the container of the group-box.
 */
scout.ViewAreaColumn.prototype._keyStrokeBindTarget = function() {
  return this.$container;
};

scout.ViewAreaColumn.prototype._render = function($parent) {
  var htmlBody, i,
    env = scout.HtmlEnvironment;

  this.$container = $parent.appendDiv('viewColumn');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
};

scout.ViewAreaColumn.prototype._remove = function() {
  scout.ViewArea.parent.prototype._remove.call(this);
  if (this.scrollable) {
    scout.scrollbars.uninstall(this.$body);
  }
  this.htmlComp = null;
};

scout.ViewAreaColumn.prototype._renderProperties = function() {
  scout.ViewAreaColumn.parent.prototype._renderProperties.call(this);

  //  this._renderBorderVisible();
  //  this._renderExpandable();
  //  this._renderExpanded();
  //  this._renderMenuBarVisible();
};

scout.ViewAreaColumn.prototype._createLayout = function() {
  return new scout.ViewAreaColumnLayout(this);
};

scout.ViewAreaColumn.prototype.activateView = function(view) {

};

scout.ViewAreaColumn.prototype._createViewAreas = function() {
  for (var i = 0; i < 3; i++) {
    this.viewAreas.push(scout.create('ViewArea', {
      parent: this
    }));
  }
};

scout.ViewAreaColumn.prototype._revalidateSplitters = function(clearPosition) {
  // remove old splitters
  if (this.components) {
    this.components.forEach(function(comp) {
      if (comp instanceof scout.Splitter) {
        comp.remove();
      }
    });
  }
  var splitterParent = this;
  this.components = this.viewAreas.filter(function(viewArea) {
    return viewArea.hasViews();
  }).reduce(function(arr, col) {
    if (arr.length > 0) {
      // add sep
      var splitter = scout.create('Splitter', {
        parent: splitterParent,
        $anchor: arr[arr.length - 1].$container,
        $root: splitterParent.$container,
        splitHorizontal: false,
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
      return comp instanceof scout.ViewArea;
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

scout.ViewAreaColumn.prototype._onSplitterMove = function(event) {
  var splitterIndex = this.components.indexOf(event.source);
  if (splitterIndex > 0 /*cannot be 0 since first element is a ViewArea*/ ) {
    var $before = this.components[splitterIndex - 1].$container,
      $after = this.components[splitterIndex + 1].$container,
      diff = event.position - event.source.position;

    if (($before.outerHeight(true) + diff) < scout.DesktopGridBench.VIEW_MIN_HEIGHT) {
      // set to min
      event.setPosition($before.position().top + scout.DesktopGridBench.VIEW_MIN_HEIGHT);
    }
    if (($after.position().top + $after.outerHeight(true) - event.position) < scout.DesktopGridBench.VIEW_MIN_HEIGHT) {
      event.setPosition($after.position().top + $after.outerHeight(true) - scout.DesktopGridBench.VIEW_MIN_HEIGHT);
    }
  }
};
scout.ViewAreaColumn.prototype._onSplitterPositionChanged = function() {
  this.revalidateLayout();
};

scout.ViewAreaColumn.prototype.showView = function(view) {
  // render
  if (!this.rendered) {
    this.render(this.parent.$container);
  }

  var viewArea;
  switch (view.displayViewId) {
    case 'NW':
    case 'N':
    case 'NE':
      viewArea = this.viewAreas[this.VIEW_AREA_INDEX.TOP];
      break;
    case 'SW':
    case 'S':
    case 'SE':
      viewArea = this.viewAreas[this.VIEW_AREA_INDEX.BOTTOM];
      break;
    default:
      viewArea = this.viewAreas[this.VIEW_AREA_INDEX.CENTER];
      break;
  }
  this._viewToViewArea[view.id] = viewArea;

  viewArea.showView(view);

  if (viewArea.viewCount() === 1) {
    this._revalidateSplitters(true);
  }
};

scout.ViewAreaColumn.prototype.removeView = function(view) {
  var viewArea = this._viewToViewArea[view.id];
  if (viewArea) {
    viewArea.removeView(view);
    delete this._viewToViewArea[view.id];
  }

  if (viewArea.viewCount() === 0) {
    this._revalidateSplitters(true);
  }

  // remove if empty
  if (!this.hasViews() && this.rendered) {
    this.remove();
  }
};

scout.ViewAreaColumn.prototype.viewCount = function() {
  return this.viewAreas.map(function(viewArea) {
    return viewArea.viewCount();
  }).reduce(function(c1, c2) {
    return c1 + c2;
  }, 0);
};

scout.ViewAreaColumn.prototype.hasViews = function() {
  return this.viewCount() > 0;
};

scout.ViewAreaColumn.prototype.getComponents = function() {
  return this.components;
};
