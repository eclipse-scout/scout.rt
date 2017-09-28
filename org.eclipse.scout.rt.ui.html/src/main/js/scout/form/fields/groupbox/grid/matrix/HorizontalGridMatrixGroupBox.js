/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.HorizontalGridMatrixGroupBox = function(columnCount) {
  scout.HorizontalGridMatrixGroupBox.parent.call(this, new scout.GroupBoxGridMatrixCursor(0, 0, columnCount, Number.MAX_SAFE_INTEGER, scout.GroupBoxGridMatrixCursor.HORIZONTAL));

  this.rowCount = 0;
};
scout.inherits(scout.HorizontalGridMatrixGroupBox, scout.AbstractGridMatrixGroupBox);

scout.HorizontalGridMatrixGroupBox.prototype.computeGridData = function(fields) {
  fields.forEach(function(field) {
    var hints = scout.GridData.createFromHints(field, this.getColumnCount());
    var gridData = new scout.GridData(hints);
    gridData.w = Math.min(hints.w, this.getColumnCount());
    this._add(field, hints, gridData);
    field.gridData = gridData;
  }.bind(this));
  this._cursor.rowCount = this.rowCount;
  return true;
};

scout.HorizontalGridMatrixGroupBox.prototype._add = function(field, hints, data) {
  this._nextFree(data.w, data.h);
  var currentIndex = this._cursor.currentIndex();
  if (data.w <= (this.getColumnCount() - currentIndex.x)) {
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
