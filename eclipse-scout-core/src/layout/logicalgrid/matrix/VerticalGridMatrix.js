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
import {AbstractGrid, LogicalGridMatrix, LogicalGridMatrixCell, LogicalGridMatrixCursor} from '../../../index';

export default class VerticalGridMatrix extends LogicalGridMatrix {

  constructor(columnCount, rowCount, x, y) {
    super(new LogicalGridMatrixCursor(x || 0, y || 0, columnCount, rowCount, LogicalGridMatrixCursor.VERTICAL));

    this._widgets = [];
    this._widgetGridDatas = [];
  }

  resetAll(columnCount, rowCount) {
    this._widgetGridDatas = [];
    this._assignedCells = [];
    this._widgetIndexes = [];
    this._cursor = new LogicalGridMatrixCursor(this._cursor.startX, this._cursor.startY, columnCount, rowCount, LogicalGridMatrixCursor.VERTICAL);
  }

  computeGridData(widgets) {
    this._widgets = widgets;
    return widgets.every((f, i) => {
      this._widgetGridDatas[i] = AbstractGrid.getGridDataFromHints(f, this._cursor.columnCount);
      return this._add(f, this._widgetGridDatas[i]);
    });
  }

  getGridData(f) {
    return this._widgetGridDatas[this._widgets.indexOf(f)];
  }

  _addAssignedCells(cells) {
    cells.forEach((v, i) => {
      if (v) {
        v.forEach((w, j) => {
          if (w) {
            this._setAssignedCell({
              x: i,
              y: j
            }, w);
          }
        });
      }
    });
  }

  _getAssignedCells() {
    return this._assignedCells;
  }

  _add(f, gd) {
    let idx = this._cursor.currentIndex();
    if (gd.w > 1) {
      // try to reorganize widgets above
      let x = idx.x,
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
    for (let xx = idx.x; xx < idx.x + gd.w; xx++) {
      for (let yy = idx.y; yy < idx.y + gd.h; yy++) {
        this._setAssignedCell({
          x: xx,
          y: yy
        }, new LogicalGridMatrixCell(f, gd));
      }
    }
    return true;
  }

  _reorganizeGridAbove(x, y, w) {
    let widgetsToReorganize = [];
    let addWidgetToReorganize = f => {
      if (widgetsToReorganize.indexOf(f) === -1) {
        widgetsToReorganize.push(f);
      }
    };
    let occupiedCells = [];
    let setOccupiedCell = (x, y, val) => {
      if (!occupiedCells[x]) {
        occupiedCells[x] = [];
      }
      occupiedCells[x][y] = val;
    };
    let reorgBounds = {
      x: x,
      y: 0,
      w: w,
      h: y + 1
    }; // x, y, w, h

    let minY = y;
    let usedCells = 0;
    let continueLoop = true;
    for (let yi = y; yi >= 0 && continueLoop; yi--) {
      for (let xi = x; xi < x + w && continueLoop; xi++) {
        let idx = {
          x: xi,
          y: yi
        };
        let cell = this._getAssignedCell(idx);
        if (cell && !cell.isEmpty()) {
          let gd = cell.data;
          if (this._horizontalMatchesOrOverlaps(reorgBounds, gd)) {
            continueLoop = false;
          } else if (this._horizontalOverlapsOnSide(reorgBounds, gd)) {
            // freeze the cells for reorganization
            setOccupiedCell(idx.x, idx.y, cell);
            usedCells++;
            minY = Math.min(idx.y, minY);
          } else {
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
    widgetsToReorganize.sort((a, b) => {
      return this._widgets.indexOf(a) < this._widgets.indexOf(b) ? -1 : 1;
    });
    reorgBounds.y = minY;

    let reorgMatrix = new VerticalGridMatrix(reorgBounds.w, Math.floor((usedCells + reorgBounds.w - 1) / reorgBounds.w), reorgBounds.x, reorgBounds.y);
    reorgMatrix._addAssignedCells(occupiedCells);
    while (!reorgMatrix.computeGridData(widgetsToReorganize)) {
      reorgMatrix.resetAll(reorgMatrix.getColumnCount(), reorgMatrix.getRowCount() + 1);
    }
    this._cursor.reset();
    this._addAssignedCells(reorgMatrix._getAssignedCells());
    reorgMatrix._widgetGridDatas.forEach((v, i) => {
      this._widgetGridDatas[this._widgets.indexOf(reorgMatrix._widgets[i])] = v;
    });
  }

  _horizontalMatchesOrOverlaps(bounds, gd) {
    return bounds.x >= gd.x && bounds.x + bounds.w <= gd.x + gd.w;
  }

  _horizontalOverlapsOnSide(bounds, gd) {
    return bounds.x > gd.x || bounds.x + bounds.w < gd.x + gd.w;
  }
}
