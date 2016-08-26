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
scout.HorizontalGridMatrixGroupBox = function(columnCount) {
  this.columnCount = columnCount;
  this.rowCount = 0;

  // helper
  this._cursor = new scout.GroupBoxGridMatrixCursor(0, 0, columnCount, Number.MAX_SAFE_INTEGER, scout.GroupBoxGridMatrixCursor.HORIZONTAL);
  this._assignedCells = [];
};

scout.HorizontalGridMatrixGroupBox.prototype.computeGridData = function(fields) {
  fields.forEach(function(field) {
    var hints = scout.GridData.createFromHints(field, this.columnCount);
    var gridData = new scout.GridData(hints);
    gridData.w = Math.min(hints.w, this.columnCount);
    this._add(field, hints, gridData);
    field.gridData = gridData;
  }.bind(this));
  return true;
};

scout.HorizontalGridMatrixGroupBox.prototype.getRowCount = function() {
  return this.rowCount;
};

scout.HorizontalGridMatrixGroupBox.prototype.getColumnCount = function() {
  return this.columnCount;
};

scout.HorizontalGridMatrixGroupBox.prototype._add = function(field, hints, data) {
  this._nextFree(data.w, data.h);
  var currentIndex = this._cursor.currentIndex();
  if (data.w <= (this.columnCount - currentIndex.x)) {
    data.x = currentIndex.x;
    data.y = currentIndex.y;
    // add field
    for (var xx = currentIndex.x; xx < currentIndex.x + data.w; xx++) {
      for (var yy = currentIndex.y; yy < currentIndex.y + data.h; yy++) {
        this._setAssignedCell({
          x: xx,
          y: yy
        }, new scout.GroupBoxGridCell(field, data));
      }
    }
    this.rowCount = currentIndex.y + data.h;
  } else {
    // add dummy cell
    this._setAssignedCell(this._cursor.currentIndex(), new scout.GroupBoxGridCell());
    this._add(field, hints, data);
  }
};

scout.HorizontalGridMatrixGroupBox.prototype._nextFree = function(w, h) {
  this._cursor.increment();
  var currentIndex = this._cursor.currentIndex();
  if (!this._isAllCellFree(currentIndex.x, currentIndex.y, w, h)) {
    if (!this._getAssignedCell(currentIndex)) {
      this._setAssignedCell(currentIndex, new scout.GroupBoxGridCell());
    }
    this._nextFree(w, h);
  }
};

scout.HorizontalGridMatrixGroupBox.prototype._setAssignedCell = function(index, val) {
  if (!this._assignedCells[index.x]) {
    this._assignedCells[index.x] = [];
  }
  this._assignedCells[index.x][index.y] = val;
};

scout.HorizontalGridMatrixGroupBox.prototype._getAssignedCell = function(index) {
  if (!this._assignedCells[index.x]) {
    return null;
  }
  return this._assignedCells[index.x][index.y];
};

scout.HorizontalGridMatrixGroupBox.prototype._isAllCellFree = function(x, y, w, h) {
  if (x + w > this.getColumnCount()) {
    return false;
  }
  return this._assignedCells.slice(x, x + w).every(function(valX) {
    return (valX || []).slice(y, y + h).every(function(valY) {
      return !valY;
    }.bind(this));
  }.bind(this));
};

scout.HorizontalGridMatrixGroupBox.prototype.toString = function() {
  var ret = "----Horizontal Grid Matrix [columnCount=" + this.getColumnCount() + ",rowCount=" + this.getRowCount() + "]--------------\n";
  var tempCursor = new scout.GroupBoxGridMatrixCursor(0, 0, this.getColumnCount(), this.getRowCount(), scout.GroupBoxGridMatrixCursor.HORIZONTAL);
  while (tempCursor.increment()) {
    var cell = this._getAssignedCell(tempCursor.currentIndex());
    ret += "cell[" + tempCursor.currentIndex().x + ", " + tempCursor.currentIndex().y + "] ";
    if (!cell) {
      ret += "NULL";
    } else if (!cell.field) {
      ret += "Placeholder";
    } else {
      ret += cell.field;
    }
    ret += "\n";
  }
  return ret;
};
