/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {GridData, LogicalGridMatrix, LogicalGridMatrixCell, LogicalGridMatrixCursor, LogicalGridWidget, Point} from '../../../index';

export class HorizontalGridMatrix extends LogicalGridMatrix {
  rowCount: number;

  constructor(columnCount: number) {
    super(new LogicalGridMatrixCursor(0, 0, columnCount, Number.MAX_SAFE_INTEGER, LogicalGridMatrixCursor.Orientation.HORIZONTAL));

    this.rowCount = 0;
  }

  computeGridData(widgets: LogicalGridWidget[]): boolean {
    widgets.forEach(widget => {
      let hints = GridData.createFromHints(widget, this.getColumnCount());
      let gridData = new GridData(hints);
      gridData.w = Math.min(hints.w, this.getColumnCount());
      this._add(widget, hints, gridData);
      widget._setGridData(gridData)
    });
    this._cursor.rowCount = this.rowCount;
    return true;
  }

  protected _add(widget: LogicalGridWidget, hints: GridData, data: GridData) {
    this._nextFree(data.w, data.h);
    let currentIndex = this._cursor.currentIndex();
    if (data.w <= (this.getColumnCount() - currentIndex.x)) {
      data.x = currentIndex.x;
      data.y = currentIndex.y;
      // add widget
      for (let xx = currentIndex.x; xx < currentIndex.x + data.w; xx++) {
        for (let yy = currentIndex.y; yy < currentIndex.y + data.h; yy++) {
          this._setAssignedCell(new Point(xx, yy), new LogicalGridMatrixCell(widget, data));
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
