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
scout.GroupBoxGridMatrixCursor = function(x, y, columnCount, rowCount, orientation) {
  this.startX = x;
  this.startY = y;
  this.columnCount = columnCount;
  this.rowCount = rowCount;
  this.orientation = orientation;

  this.reset();
};

scout.GroupBoxGridMatrixCursor.HORIZONTAL = 0;
scout.GroupBoxGridMatrixCursor.VERTICAL = 1;

scout.GroupBoxGridMatrixCursor.prototype.reset = function() {
  this._currentIndex = {
    x: -1,
    y: -1
  };
};

scout.GroupBoxGridMatrixCursor.prototype.currentIndex = function() {
  return {
    x: this._currentIndex.x,
    y: this._currentIndex.y
  };
};

scout.GroupBoxGridMatrixCursor.prototype.increment = function() {
  if (this._currentIndex.x < 0 || this._currentIndex.y < 0) {
    // initial
    this._currentIndex.x = this.startX;
    this._currentIndex.y = this.startY;
  } else if (this.orientation === scout.GroupBoxGridMatrixCursor.HORIZONTAL) {
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
};

scout.GroupBoxGridMatrixCursor.prototype.decrement = function() {
  if (this._currentIndex.x < 0 || this._currentIndex.y < 0) {
    return false;
  } else if (this._currentIndex.x >= this.startX + this.columnCount || this._currentIndex.y >= this.startY + this.rowCount) {
    this._currentIndex.x = this.startX + this.columnCount - 1;
    this._currentIndex.y = this.startY + this.rowCount - 1;
  } else if (this.orientation === scout.GroupBoxGridMatrixCursor.HORIZONTAL) {
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
};

scout.GroupBoxGridMatrixCursor.prototype.toString = function() {
  var builder = [];
  builder.push('MatrixCursor [');
  builder.push('orientation=' + this.orientation);
  builder.push(', startX=' + this.startX);
  builder.push(', startY=' + this.startY);
  builder.push(', columnCount=' + this.columnCount);
  builder.push(', currentIndex=' + this._currentIndex.x + ', ' + this._currentIndex.y);
  builder.push(']');
  return builder.join('');
};
