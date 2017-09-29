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
scout.VerticalGridMatrix = function(columnCount, rowCount, x, y) {
  scout.VerticalGridMatrix.parent.call(this, new scout.LogicalGridMatrixCursor(x || 0, y || 0, columnCount, rowCount, scout.LogicalGridMatrixCursor.VERTICAL));

  this._widgets = [];
  this._widgetGridDatas = [];
};
scout.inherits(scout.VerticalGridMatrix, scout.LogicalGridMatrix);

scout.VerticalGridMatrix.prototype.resetAll = function(columnCount, rowCount) {
  this._widgetGridDatas = [];
  this._assignedCells = [];
  this._widgetIndexes = [];
  this._cursor = new scout.LogicalGridMatrixCursor(this._cursor.startX, this._cursor.startY, columnCount, rowCount, scout.LogicalGridMatrixCursor.VERTICAL);
};

scout.VerticalGridMatrix.prototype.computeGridData = function(widgets) {
  this._widgets = widgets;
  return widgets.every(function(f, i) {
    this._widgetGridDatas[i] = scout.AbstractGrid.getGridDataFromHints(f, this._cursor.columnCount);
    return this._add(f, this._widgetGridDatas[i]);
  }.bind(this));
};

scout.VerticalGridMatrix.prototype.getGridData = function(f) {
  return this._widgetGridDatas[this._widgets.indexOf(f)];
};

scout.VerticalGridMatrix.prototype._addAssignedCells = function(cells) {
  cells.forEach(function(v, i) {
    if (v) {
      v.forEach(function(w, j) {
        if (w) {
          this._setAssignedCell({
            x: i,
            y: j
          }, w);
        }
      }.bind(this));
    }
  }.bind(this));
};

scout.VerticalGridMatrix.prototype._getAssignedCells = function() {
  return this._assignedCells;
};

scout.VerticalGridMatrix.prototype._add = function(f, gd) {
  var idx = this._cursor.currentIndex();
  if (gd.w > 1) {
    // try to reorganize widgets above
    var x = idx.x,
      y = idx.y;
    // try to move left if the right border of the widget is outside the column range
    while (x + gd.w > this._cursor.startX + this._cursor.columnCount) {
      // shift left and bottom
      x--;
      y = this._cursor.rowCount - 1;
    }
    this._reorganizeGridAbove(x, y, gd.w);
  }
  if (!this._nextFree(gd.w, gd.h)) {
    return false;
  }
  idx = this._cursor.currentIndex();
  gd.x = idx.x;
  gd.y = idx.y;
  // add widget
  for (var xx = idx.x; xx < idx.x + gd.w; xx++) {
    for (var yy = idx.y; yy < idx.y + gd.h; yy++) {
      this._setAssignedCell({
        x: xx,
        y: yy
      }, new scout.LogicalGridMatrixCell(f, gd));
    }
  }
  return true;
};

scout.VerticalGridMatrix.prototype._reorganizeGridAbove = function(x, y, w) {
  var widgetsToReorganize = [];
  var addWidgetToReorganize = function(f) {
    if (widgetsToReorganize.indexOf(f) === -1) {
      widgetsToReorganize.push(f);
    }
  };
  var occupiedCells = [];
  var setOccupiedCell = function(x, y, val) {
    if (!occupiedCells[x]) {
      occupiedCells[x] = [];
    }
    occupiedCells[x][y] = val;
  };
  var reorgBounds = {
    x: x,
    y: 0,
    w: w,
    h: y + 1
  }; // x, y, w, h

  var minY = y;
  var usedCells = 0;
  var continueLoop = true;
  for (var yi = y; yi >= 0 && continueLoop; yi--) {
    for (var xi = x; xi < x + w && continueLoop; xi++) {
      var idx = {
        x: xi,
        y: yi
      };
      var cell = this._getAssignedCell(idx);
      if (cell && !cell.isEmpty()) {
        var gd = cell.data;
        if (this._horizontalMatchesOrOverlaps(reorgBounds, gd)) {
          continueLoop = false;
        } else if (this._horizontalOverlapsOnSide(reorgBounds, gd)) {
          // freeze the cells for reorganization
          setOccupiedCell(idx.x, idx.y, cell);
          usedCells++;
          minY = Math.min(idx.y, minY);
        }
        // includes
        else {
          // add widget to reorganization
          this._setAssignedCell(idx, null);
          addWidgetToReorganize(cell.widget);
          usedCells++;
          minY = Math.min(idx.y, minY);
        }
      }
    }
  }
  if (widgetsToReorganize.length === 0) {
    return;
  }
  widgetsToReorganize.sort(function(a, b) {
    return this._widgets.indexOf(a) < this._widgets.indexOf(b) ? -1 : 1;
  }.bind(this));
  reorgBounds.y = minY;

  var reorgMatrix = new scout.VerticalGridMatrix(reorgBounds.w, Math.floor((usedCells + reorgBounds.w - 1) / reorgBounds.w), reorgBounds.x, reorgBounds.y);
  reorgMatrix._addAssignedCells(occupiedCells);
  while (!reorgMatrix.computeGridData(widgetsToReorganize)) {
    reorgMatrix.resetAll(reorgMatrix.getColumnCount(), reorgMatrix.getRowCount() + 1);
  }
  this._cursor.reset();
  this._addAssignedCells(reorgMatrix._getAssignedCells());
  reorgMatrix._widgetGridDatas.forEach(function(v, i) {
    this._widgetGridDatas[this._widgets.indexOf(reorgMatrix._widgets[i])] = v;
  }.bind(this));
};

scout.VerticalGridMatrix.prototype._horizontalMatchesOrOverlaps = function(bounds, gd) {
  return bounds.x >= gd.x && bounds.x + bounds.w <= gd.x + gd.w;
};

scout.VerticalGridMatrix.prototype._horizontalOverlapsOnSide = function(bounds, gd) {
  return bounds.x > gd.x || bounds.x + bounds.w < gd.x + gd.w;
};
