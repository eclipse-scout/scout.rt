/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

/**
 * JavaScript port from java.awt.Point.
 */
scout.Point = function(vararg, y) {
  if (vararg instanceof scout.Point) {
    this.x = vararg.x;
    this.y = vararg.y;
  } else {
    this.x = vararg || 0;
    this.y = y || 0;
  }
};

scout.Point.prototype.toString = function() {
  return 'Point[x=' + this.x + ' y=' + this.y + ']';
};

scout.Point.prototype.equals = function(o) {
  if (!o) {
    return false;
  }
  return (this.x === o.x && this.y === o.y);
};

scout.Point.prototype.clone = function(o) {
  return new scout.Point(this.x, this.y);
};

scout.Point.prototype.add = function(point) {
  return new scout.Point(this.x + point.x, this.y + point.y);
};

scout.Point.prototype.subtract = function(point) {
  return new scout.Point(this.x - point.x, this.y - point.y);
};

scout.Point.prototype.floor = function() {
  return new scout.Point(Math.floor(this.x), Math.floor(this.y));
};

scout.Point.prototype.ceil = function() {
  return new scout.Point(Math.ceil(this.x), Math.ceil(this.y));
};
