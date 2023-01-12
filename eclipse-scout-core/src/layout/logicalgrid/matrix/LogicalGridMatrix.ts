/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LogicalGridMatrixCell, LogicalGridMatrixCursor, Point} from '../../../index';

export class LogicalGridMatrix {
  protected _cursor: LogicalGridMatrixCursor;
  protected _assignedCells: LogicalGridMatrixCell[][];

  constructor(cursor: LogicalGridMatrixCursor) {
    this._cursor = cursor;
    this._assignedCells = [];
  }

  getColumnCount(): number {
    return this._cursor.columnCount;
  }

  getRowCount(): number {
    return this._cursor.rowCount;
  }

  protected _setAssignedCell(index: Point, val: LogicalGridMatrixCell) {
    if (!this._assignedCells[index.x]) {
      this._assignedCells[index.x] = [];
    }
    this._assignedCells[index.x][index.y] = val;
  }

  protected _getAssignedCell(index: Point): LogicalGridMatrixCell {
    if (!this._assignedCells[index.x]) {
      return null;
    }
    return this._assignedCells[index.x][index.y];
  }

  protected _nextFree(w: number, h: number): boolean {
    if (!this._cursor.increment()) {
      return false;
    }
    let currentIndex = this._cursor.currentIndex();
    if (!this._isAllCellFree(currentIndex.x, currentIndex.y, w, h)) {
      if (!this._getAssignedCell(currentIndex)) {
        this._setAssignedCell(currentIndex, new LogicalGridMatrixCell());
      }
      return this._nextFree(w, h);
    }
    return true;
  }

  protected _isAllCellFree(x: number, y: number, w: number, h: number): boolean {
    if (x + w > this._cursor.startX + this._cursor.columnCount || y + h > this._cursor.startY + this._cursor.rowCount) {
      return false;
    }
    return this._assignedCells.slice(x, x + w).every(valX => {
      return (valX || []).slice(y, y + h).every(valY => {
        return !valY;
      });
    });
  }

  toString(): string {
    let ret = '----Group Box Grid Matrix [orientation=' + this._cursor.orientation + ', columnCount=' + this.getColumnCount() + ', rowCount=' + this.getRowCount() + ']--------------\n';
    let tempCursor = new LogicalGridMatrixCursor(0, 0, this.getColumnCount(), this.getRowCount(), this._cursor.orientation);
    while (tempCursor.increment()) {
      let cell = this._getAssignedCell(tempCursor.currentIndex());
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
  }
}
