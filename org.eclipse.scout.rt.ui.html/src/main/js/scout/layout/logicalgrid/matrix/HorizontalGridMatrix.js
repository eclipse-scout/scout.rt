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
scout.HorizontalGridMatrix = function(columnCount) {
  scout.HorizontalGridMatrix.parent.call(this, new scout.LogicalGridMatrixCursor(0, 0, columnCount, Number.MAX_SAFE_INTEGER, scout.LogicalGridMatrixCursor.HORIZONTAL));

  this.rowCount = 0;
};
scout.inherits(scout.HorizontalGridMatrix, scout.LogicalGridMatrix);

scout.HorizontalGridMatrix.prototype.computeGridData = function(widgets) {
  widgets.forEach(function(widget) {
    var hints = scout.GridData.createFromHints(widget, this.getColumnCount());
    var gridData = new scout.GridData(hints);
    gridData.w = Math.min(hints.w, this.getColumnCount());
    this._add(widget, hints, gridData);
    widget.gridData = gridData;
  }.bind(this));
  this._cursor.rowCount = this.rowCount;
  return true;
};

scout.HorizontalGridMatrix.prototype._add = function(widget, hints, data) {
  this._nextFree(data.w, data.h);
  var currentIndex = this._cursor.currentIndex();
  if (data.w <= (this.getColumnCount() - currentIndex.x)) {
    data.x = currentIndex.x;
    data.y = currentIndex.y;
    // add widget
    for (var xx = currentIndex.x; xx < currentIndex.x + data.w; xx++) {
      for (var yy = currentIndex.y; yy < currentIndex.y + data.h; yy++) {
        this._setAssignedCell({
          x: xx,
          y: yy
        }, new scout.LogicalGridMatrixCell(widget, data));
      }
    }
    this.rowCount = currentIndex.y + data.h;
  } else {
    // add dummy cell
    this._setAssignedCell(this._cursor.currentIndex(), new scout.LogicalGridMatrixCell());
    this._add(widget, hints, data);
  }
};
