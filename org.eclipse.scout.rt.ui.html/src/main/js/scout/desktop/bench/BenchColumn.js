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
scout.BenchColumn = function() {
  scout.BenchColumn.parent.call(this);
  this.htmlComp;
  this.TAB_BOX_INDEX = {
    TOP: 0,
    CENTER: 1,
    BOTTOM: 2
  };
  this.tabBoxs = [];
  this._widgetToTabBox = {}; // [key=viewId, value=SimpleTabBox instance]
  this.components;

  // event listener functions
  this._viewAddedHandler = this._onViewAdded.bind(this);
  this._viewRemovedHandler = this._onViewRemoved.bind(this);
  this._viewActivatedHandler = this._onViewActivated.bind(this);
  this._viewDeactivatedHandler = this._onViewDeactivated.bind(this);

  this._addEventSupport();
};
scout.inherits(scout.BenchColumn, scout.ModelAdapter);

scout.BenchColumn.prototype._init = function(model) {
  scout.BenchColumn.parent.prototype._init.call(this, model);

  this._createTabBoxes();
};

/**
 * @override ModelAdapter.js
 */
scout.BenchColumn.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.BenchColumn.parent.prototype._initKeyStrokeContext.call(this, this.keyStrokeContext);
  this.keyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.keyStrokeContext.$bindTarget = this._keyStrokeBindTarget.bind(this);
};

/**
 * Returns a $container used as a bind target for the key-stroke context of the group-box.
 * By default this function returns the container of the form, or when group-box is has no
 * form as a parent the container of the group-box.
 */
scout.BenchColumn.prototype._keyStrokeBindTarget = function() {
  return this.$container;
};

scout.BenchColumn.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('bench-column');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
};

scout.BenchColumn.prototype._renderProperties = function() {
  scout.BenchColumn.parent.prototype._renderProperties.call(this);
  this._renderTabBoxs();
  this._revalidateSplitters();
};

scout.BenchColumn.prototype._renderTabBoxs = function() {
  this.tabBoxs.forEach(function(tabBox) {
    if (tabBox.viewCount() > 0) {

      this._renderTabBox(tabBox);
    }
  }.bind(this));

};

scout.BenchColumn.prototype._renderTabBox = function(tabBox) {
  if (!tabBox.rendered) {
    tabBox.render(this.$container);
  }
};

scout.BenchColumn.prototype._remove = function() {
  scout.SimpleTabBox.parent.prototype._remove.call(this);
  if (this.scrollable) {
    scout.scrollbars.uninstall(this.$body);
  }
  this.htmlComp = null;
};

scout.BenchColumn.prototype.postRender = function() {
  this.tabBoxs.forEach(function(tabBox) {
    tabBox.postRender();
  });
};

scout.BenchColumn.prototype._createLayout = function() {
  return new scout.BenchColumnLayout(this);
};

scout.BenchColumn.prototype._onViewAdded = function(event) {
  this.trigger('viewAdded', {
    view: event.view
  });
};

scout.BenchColumn.prototype._onViewRemoved = function(event) {
  this.trigger('viewRemoved', {
    view: event.view
  });
};

scout.BenchColumn.prototype._onViewActivated = function(event) {
  this.trigger('viewActivated', {
    view: event.view
  });
};

scout.BenchColumn.prototype._onViewDeactivated = function(event) {
  this.trigger('viewDeactivated', {
    view: event.view
  });
};

scout.BenchColumn.prototype.activateView = function(view) {
  var tabBox = this.getTabBox(view.displayViewId);
  tabBox.activateView(view);
};

scout.BenchColumn.prototype._createTabBoxes = function() {
  for (var i = 0; i < 3; i++) {
    var tabBox = scout.create('SimpleTabBox', {
      parent: this
    });
    tabBox.on('viewAdded', this._viewAddedHandler);
    tabBox.on('viewRemoved', this._viewRemovedHandler);
    tabBox.on('viewActivated', this._viewActivatedHandler);
    tabBox.on('viewDeactivated', this._viewDeactivatedHandler);
    this.tabBoxs.push(tabBox);
  }
};

scout.BenchColumn.prototype._revalidateSplitters = function(clearPosition) {
  // remove old splitters
  if (this.components) {
    this.components.forEach(function(comp) {
      if (comp instanceof scout.Splitter) {
        comp.remove();
      }
    });
  }
  var splitterParent = this;
  this.components = this.tabBoxs.filter(function(tabBox) {
    return tabBox.hasViews();
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
      splitter.on('move', splitterParent._onSplitterMove.bind(splitterParent));
      splitter.on('positionChanged', splitterParent._onSplitterPositionChanged.bind(splitterParent));
      arr.push(splitter);
    }
    arr.push(col);
    return arr;
  }, []);
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
};

scout.BenchColumn.prototype._onSplitterMove = function(event) {
  var splitterIndex = this.components.indexOf(event.source);
  if (splitterIndex > 0 /*cannot be 0 since first element is a SimpleTabBox*/ ) {
    var $before = this.components[splitterIndex - 1].$container,
      $after = this.components[splitterIndex + 1].$container,
      diff = event.position - event.source.position;

    if (($before.outerHeight(true) + diff) < scout.DesktopBench.VIEW_MIN_HEIGHT) {
      // set to min
      event.setPosition($before.position().top + scout.DesktopBench.VIEW_MIN_HEIGHT);
    }
    if (($after.position().top + $after.outerHeight(true) - event.position) < scout.DesktopBench.VIEW_MIN_HEIGHT) {
      event.setPosition($after.position().top + $after.outerHeight(true) - scout.DesktopBench.VIEW_MIN_HEIGHT);
    }
  }
};
scout.BenchColumn.prototype._onSplitterPositionChanged = function() {
  this.revalidateLayout();
};

scout.BenchColumn.prototype.addView = function(view, activate) {
  var tabBox = this.getTabBox(view.displayViewId);
  this._widgetToTabBox[view.id] = tabBox;

  tabBox.addView(view, activate);

  if (this.rendered && tabBox.viewCount() === 1) {
    if (!tabBox.rendered) {
      // lazy render if the first view is added.
      tabBox.render(this.$container);
    }
    this._revalidateSplitters(true);
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
      tabBox = this.tabBoxs[this.TAB_BOX_INDEX.TOP];
      break;
    case 'SW':
    case 'S':
    case 'SE':
      tabBox = this.tabBoxs[this.TAB_BOX_INDEX.BOTTOM];
      break;
    default:
      tabBox = this.tabBoxs[this.TAB_BOX_INDEX.CENTER];
      break;
  }
  return tabBox;
};

scout.BenchColumn.prototype.removeView = function(view, showSiblingView) {
  var tabBox = this._widgetToTabBox[view.id];
  if (tabBox) {
    tabBox.removeView(view, showSiblingView);
    delete this._widgetToTabBox[view.id];
    if (this.rendered && tabBox.viewCount() === 0) {
      // remove view area if no view is left.
      tabBox.remove();
      this._revalidateSplitters(true);
      this.htmlComp.invalidateLayoutTree();
      // Layout immediate to prevent 'laggy' form visualization,
      // but not initially while desktop gets rendered because it will be done at the end anyway
      this.htmlComp.validateLayoutTree();
    }
  }
};

scout.BenchColumn.prototype.viewCount = function() {
  return this.tabBoxs.map(function(tabBox) {
    return tabBox.viewCount();
  }).reduce(function(c1, c2) {
    return c1 + c2;
  }, 0);
};

scout.BenchColumn.prototype.hasViews = function() {
  return this.viewCount() > 0;
};
scout.BenchColumn.prototype.getViews = function(displayViewId) {
  return this.tabBoxs.reduce(function(arr, tabBox) {
    scout.arrays.pushAll(arr, tabBox.getViews(displayViewId));
    return arr;
  }, []);
};

scout.BenchColumn.prototype.getComponents = function() {
  return this.components;
};
