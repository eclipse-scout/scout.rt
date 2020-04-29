/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {GridData, LogicalGridMatrix, LogicalGridMatrixCell, LogicalGridMatrixCursor} from '../../../index';

export default class HorizontalGridMatrix extends LogicalGridMatrix {

  constructor(columnCount) {
    super(new LogicalGridMatrixCursor(0, 0, columnCount, Number.MAX_SAFE_INTEGER, LogicalGridMatrixCursor.HORIZONTAL));

    this.rowCount = 0;
  }

  computeGridData(widgets) {
    widgets.forEach(widget => {
      let hints = GridData.createFromHints(widget, this.getColumnCount());
      let gridData = new GridData(hints);
      gridData.w = Math.min(hints.w, this.getColumnCount());
      this._add(widget, hints, gridData);
      widget.gridData = gridData;
    });
    this._cursor.rowCount = this.rowCount;
    return true;
  }

  _add(widget, hints, data) {
    this._nextFree(data.w, data.h);
    let currentIndex = this._cursor.currentIndex();
    if (data.w <= (this.getColumnCount() - currentIndex.x)) {
      data.x = currentIndex.x;
      data.y = currentIndex.y;
      // add widget
      for (let xx = currentIndex.x; xx < currentIndex.x + data.w; xx++) {
        for (let yy = currentIndex.y; yy < currentIndex.y + data.h; yy++) {
          this._setAssignedCell({
            x: xx,
            y: yy
          }, new LogicalGridMatrixCell(widget, data));
        }
      }
      this.rowCount = currentIndex.y + data.h;
    } else {
      // add dummy cell
      this._setAssignedCell(this._cursor.currentIndex(), new LogicalGridMatrixCell());
      this._add(widget, hints, data);
    }
  }
}
