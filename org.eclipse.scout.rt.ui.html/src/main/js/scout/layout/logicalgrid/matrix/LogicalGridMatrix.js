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
scout.LogicalGridMatrix = function(cursor) {
  this._cursor = cursor;
  this._assignedCells = [];
};

scout.LogicalGridMatrix.prototype.getColumnCount = function() {
  return this._cursor.columnCount;
};

scout.LogicalGridMatrix.prototype.getRowCount = function() {
  return this._cursor.rowCount;
};

scout.LogicalGridMatrix.prototype._setAssignedCell = function(index, val) {
  if (!this._assignedCells[index.x]) {
    this._assignedCells[index.x] = [];
  }
  this._assignedCells[index.x][index.y] = val;
};

scout.LogicalGridMatrix.prototype._getAssignedCell = function(index) {
  if (!this._assignedCells[index.x]) {
    return null;
  }
  return this._assignedCells[index.x][index.y];
};

scout.LogicalGridMatrix.prototype._nextFree = function(w, h) {
  if (!this._cursor.increment()) {
    return false;
  }
  var currentIndex = this._cursor.currentIndex();
  if (!this._isAllCellFree(currentIndex.x, currentIndex.y, w, h)) {
    if (!this._getAssignedCell(currentIndex)) {
      this._setAssignedCell(currentIndex, new scout.LogicalGridMatrixCell());
    }
    return this._nextFree(w, h);
  }
  return true;
};

scout.LogicalGridMatrix.prototype._isAllCellFree = function(x, y, w, h) {
  if (x + w > this._cursor.startX + this._cursor.columnCount || y + h > this._cursor.startY + this._cursor.rowCount) {
    return false;
  }
  return this._assignedCells.slice(x, x + w).every(function(valX) {
    return (valX || []).slice(y, y + h).every(function(valY) {
      return !valY;
    }.bind(this));
  }.bind(this));
};

scout.LogicalGridMatrix.prototype.toString = function() {
  var ret = '----Group Box Grid Matrix [orientation=' + this._cursor.orientation + ', columnCount=' + this.getColumnCount() + ', rowCount=' + this.getRowCount() + ']--------------\n';
  var tempCursor = new scout.LogicalGridMatrixCursor(0, 0, this.getColumnCount(), this.getRowCount(), this._cursor.orientation);
  while (tempCursor.increment()) {
    var cell = this._getAssignedCell(tempCursor.currentIndex());
    ret += 'cell[' + tempCursor.currentIndex().x + ', ' + tempCursor.currentIndex().y + '] ';
    if (!cell) {
      ret += 'NULL';
    } else if (!cell.widget) {
      ret += 'Placeholder';
    } else {
      ret += cell.widget + (cell.widget ? ', [' + cell.widget.label + ']' : '');
    }
    ret += '\n';
  }
  return ret;
};
