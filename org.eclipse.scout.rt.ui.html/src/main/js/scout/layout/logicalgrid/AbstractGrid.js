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
/**
 * @abstract
 */
scout.AbstractGrid = function() {
  scout.AbstractGrid.parent.call(this);
  this.gridRows = 0;
  this.gridColumns = 0;
};
scout.inherits(scout.AbstractGrid, scout.LogicalGrid);

/**
 * Expects this.gridConfig to be set
 * @override
 */
scout.AbstractGrid.prototype._validate = function(gridContainer) {
  // reset old state
  this.gridRows = 0;
  this.gridConfig.setWidget(gridContainer);
  // step 0: column count
  this.gridColumns = this.gridConfig.getGridColumnCount();
  var containingGridXYCount = 0;
  var notContainingGridXYCount = 0;
  // build
  var widgets = [];
  this.gridConfig.getGridWidgets().forEach(function(widget) {
    if (widget.visible) {
      widgets.push(widget);
      var hints = widget.gridDataHints;
      if (hints && hints.x >= 0 && hints.y >= 0) {
        containingGridXYCount++;
      } else {
        notContainingGridXYCount++;
      }
    } else {
      var gd = scout.GridData.createFromHints(widget, 1);
      widget.gridData = gd;
    }
  }.bind(this));
  if (containingGridXYCount > 0 && notContainingGridXYCount === 0) {
    this.layoutAllStatic(widgets);
  } else {
    this.layoutAllDynamic(widgets);
  }
};

scout.AbstractGrid.prototype.layoutAllStatic = function(widgets) {
  var hints = [];
  widgets.forEach(function(v) {
    hints.push(scout.GridData.createFromHints(v, 1));
  });
  var totalGridW = hints.reduce(function(x, y) {
    var y1 = y.x + y.w;
    return y1 > x ? y1 : x;
  }, 1);
  var totalGridH = hints.reduce(function(x, y) {
    var y1 = y.y + y.h;
    return y1 > x ? y1 : x;
  }, 0);
  widgets.forEach(function(v) {
    v.gridData = scout.GridData.createFromHints(v, totalGridW);
  });
  this.gridRows = totalGridH;
  this.gridColumns = totalGridW;
};

scout.AbstractGrid.prototype.layoutAllDynamic = function(widgets) {
  // abstract, must be implemented by sub classes
};

scout.AbstractGrid.prototype.getGridColumnCount = function() {
  return this.gridColumns;
};

scout.AbstractGrid.prototype.getGridRowCount = function() {
  return this.gridRows;
};

/**
 * If grid w is greater than column count, grid w will be set to the column count.
 */
scout.AbstractGrid.getGridDataFromHints = function(widget, groupBoxColumnCount) {
  var data = scout.GridData.createFromHints(widget, groupBoxColumnCount);
  data.w = Math.min(groupBoxColumnCount, data.w);
  return data;
};
