/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {EnumObject, Point} from '../../../index';

export type LogicalGridMatrixOrientation = EnumObject<typeof LogicalGridMatrixCursor.Orientation>;

export class LogicalGridMatrixCursor {
  startX: number;
  startY: number;
  columnCount: number;
  rowCount: number;
  orientation: LogicalGridMatrixOrientation;
  protected _currentIndex: Point;

  constructor(x: number, y: number, columnCount: number, rowCount: number, orientation: LogicalGridMatrixOrientation) {
    this.startX = x;
    this.startY = y;
    this.columnCount = columnCount;
    this.rowCount = rowCount;
    this.orientation = orientation;

    this.reset();
  }

  static Orientation = {
    HORIZONTAL: 0,
    VERTICAL: 1
  } as const;

  reset() {
    this._currentIndex = new Point(-1, -1);
  }

  currentIndex(): Point {
    return new Point(this._currentIndex.x, this._currentIndex.y);
  }

  increment(): boolean {
    if (this._currentIndex.x < 0 || this._currentIndex.y < 0) {
      // initial
      this._currentIndex.x = this.startX;
      this._currentIndex.y = this.startY;
    } else if (this.orientation === LogicalGridMatrixCursor.Orientation.HORIZONTAL) {
      this._currentIndex.x++;
      if (this._currentIndex.x >= this.startX + this.columnCount) {
        this._currentIndex.x = this.startX;
        this._currentIndex.y++;
      }
    } else {
      // vertical
      this._currentIndex.y++;
      if (this._currentIndex.y >= this.startY + this.rowCount) {
        this._currentIndex.y = this.startY;
        this._currentIndex.x++;
      }
    }
    if (this._currentIndex.x >= this.startX + this.columnCount || this._currentIndex.y >= this.startY + this.rowCount) {
      return false;
    }
    return true;
  }

  decrement(): boolean {
    if (this._currentIndex.x < 0 || this._currentIndex.y < 0) {
      return false;
    } else if (this._currentIndex.x >= this.startX + this.columnCount || this._currentIndex.y >= this.startY + this.rowCount) {
      this._currentIndex.x = this.startX + this.columnCount - 1;
      this._currentIndex.y = this.startY + this.rowCount - 1;
    } else if (this.orientation === LogicalGridMatrixCursor.Orientation.HORIZONTAL) {
      this._currentIndex.x--;
      if (this._currentIndex.x < this.startX) {
        this._currentIndex.x = this.startX + this.columnCount - 1;
        this._currentIndex.y--;
      }
    } else {
      // vertical
      this._currentIndex.y--;
      if (this._currentIndex.y < this.startY) {
        this._currentIndex.y = this.startY + this.rowCount - 1;
        this._currentIndex.x--;
      }
    }
    if (this._currentIndex.x < this.startX || this._currentIndex.y < this.startY) {
      return false;
    }
    return true;
  }

  toString(): string {
    let builder = [];
    builder.push('MatrixCursor [');
    builder.push('orientation=' + this.orientation);
    builder.push(', startX=' + this.startX);
    builder.push(', startY=' + this.startY);
    builder.push(', columnCount=' + this.columnCount);
    builder.push(', currentIndex=' + this._currentIndex.x + ', ' + this._currentIndex.y);
    builder.push(']');
    return builder.join('');
  }
}
