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
import {LogicalGridMatrixCell, LogicalGridMatrixCursor} from '../../../index';

export default class LogicalGridMatrix {

  constructor(cursor) {
    this._cursor = cursor;
    this._assignedCells = [];
  }

  getColumnCount() {
    return this._cursor.columnCount;
  }

  getRowCount() {
    return this._cursor.rowCount;
  }

  _setAssignedCell(index, val) {
    if (!this._assignedCells[index.x]) {
      this._assignedCells[index.x] = [];
    }
    this._assignedCells[index.x][index.y] = val;
  }

  _getAssignedCell(index) {
    if (!this._assignedCells[index.x]) {
      return null;
    }
    return this._assignedCells[index.x][index.y];
  }

  _nextFree(w, h) {
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

  _isAllCellFree(x, y, w, h) {
    if (x + w > this._cursor.startX + this._cursor.columnCount || y + h > this._cursor.startY + this._cursor.rowCount) {
      return false;
    }
    return this._assignedCells.slice(x, x + w).every(valX => {
      return (valX || []).slice(y, y + h).every(valY => {
        return !valY;
      });
    });
  }

  toString() {
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
