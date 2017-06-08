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
scout.VerticalGridMatrixGroupBox = function(columnCount, rowCount, x, y) {
  scout.VerticalGridMatrixGroupBox.parent.call(this, new scout.GroupBoxGridMatrixCursor(x || 0, y || 0, columnCount, rowCount, scout.GroupBoxGridMatrixCursor.VERTICAL));

  this._fields = [];
  this._fieldGridDatas = [];
};
scout.inherits(scout.VerticalGridMatrixGroupBox, scout.AbstractGridMatrixGroupBox);

scout.VerticalGridMatrixGroupBox.prototype.resetAll = function(columnCount, rowCount) {
  this._fieldGridDatas = [];
  this._assignedCells = [];
  this._fieldIndexes = [];
  this._cursor = new scout.GroupBoxGridMatrixCursor(this._cursor.startX, this._cursor.startY, columnCount, rowCount, scout.GroupBoxGridMatrixCursor.VERTICAL);
};

scout.VerticalGridMatrixGroupBox.prototype.computeGridData = function(fields) {
  this._fields = fields;
  return fields.every(function(f, i) {
    this._fieldGridDatas[i] = scout.GroupBoxBodyGrid.getGridDataFromHints(f, this._cursor.columnCount);
    return this._add(f, this._fieldGridDatas[i]);
  }.bind(this));
};

scout.VerticalGridMatrixGroupBox.prototype.getGridData = function(f) {
  return this._fieldGridDatas[this._fields.indexOf(f)];
};

scout.VerticalGridMatrixGroupBox.prototype._addAssignedCells = function(cells) {
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

scout.VerticalGridMatrixGroupBox.prototype._getAssignedCells = function() {
  return this._assignedCells;
};

scout.VerticalGridMatrixGroupBox.prototype._add = function(f, gd) {
  var idx = this._cursor.currentIndex();
  if (gd.w > 1) {
    // try to reorganize fields above
    var x = idx.x,
      y = idx.y;
    // try to move left if the right border of the field is outside the column range
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
  // add field
  for (var xx = idx.x; xx < idx.x + gd.w; xx++) {
    for (var yy = idx.y; yy < idx.y + gd.h; yy++) {
      this._setAssignedCell({
        x: xx,
        y: yy
      }, new scout.GroupBoxGridCell(f, gd));
    }
  }
  return true;
};

scout.VerticalGridMatrixGroupBox.prototype._reorganizeGridAbove = function(x, y, w) {
  var fieldsToReorganize = [];
  var addFieldToReorganize = function(f) {
    if (fieldsToReorganize.indexOf(f) === -1) {
      fieldsToReorganize.push(f);
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
          // add field to reorganization
          this._setAssignedCell(idx, null);
          addFieldToReorganize(cell.field);
          usedCells++;
          minY = Math.min(idx.y, minY);
        }
      }
    }
  }
  if (fieldsToReorganize.length === 0) {
    return;
  }
  fieldsToReorganize.sort(function(a, b) {
    return this._fields.indexOf(a) < this._fields.indexOf(b) ? -1 : 1;
  }.bind(this));
  reorgBounds.y = minY;

  var reorgMatrix = new scout.VerticalGridMatrixGroupBox(reorgBounds.w, Math.floor((usedCells + reorgBounds.w - 1) / reorgBounds.w), reorgBounds.x, reorgBounds.y);
  reorgMatrix._addAssignedCells(occupiedCells);
  while (!reorgMatrix.computeGridData(fieldsToReorganize)) {
    reorgMatrix.resetAll(reorgMatrix.getColumnCount(), reorgMatrix.getRowCount() + 1);
  }
  this._cursor.reset();
  this._addAssignedCells(reorgMatrix._getAssignedCells());
  reorgMatrix._fieldGridDatas.forEach(function(v, i) {
    this._fieldGridDatas[this._fields.indexOf(reorgMatrix._fields[i])] = v;
  }.bind(this));
};

scout.VerticalGridMatrixGroupBox.prototype._horizontalMatchesOrOverlaps = function(bounds, gd) {
  return bounds.x >= gd.x && bounds.x + bounds.w <= gd.x + gd.w;
};

scout.VerticalGridMatrixGroupBox.prototype._horizontalOverlapsOnSide = function(bounds, gd) {
  return bounds.x > gd.x || bounds.x + bounds.w < gd.x + gd.w;
};
