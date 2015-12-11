/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.Range = function(from, to) {
  this.from = from;
  this.to = to;
};

scout.Range.prototype.equals = function(other) {
  if (other.from === undefined || other.to === undefined) {
    return false;
  }
  var fromEquals = scout.Range.dateEquals(this.from, other.from);
  var toEquals = scout.Range.dateEquals(this.to, other.to);
  return fromEquals && toEquals;
};

scout.Range.dateEquals = function(a, b) {
  if (a === null && b === null) {
    return true;
  }
  if (a === null && b !== null) {
    return false;
  }
  if (a !== null && b === null) {
    return false;
  }
  return a.valueOf() === b.valueOf();
};

scout.Range.prototype.subtract = function(other) {
  // other is empty
  if (other.size() === 0) {
    return [new scout.Range(this.from, this.to)];
  }
  // other is greater
  if (this.from >= other.from && this.to <= other.to) {
    return [new scout.Range(0, 0)];
  }
  // other is contained completely
  if (other.from >= this.from && other.to <= this.to) {
    var range1 = new scout.Range(this.from, other.from);
    var range2 = new scout.Range(other.to, this.to);
    if (range1.size() === 0) {
      return [range2];
    }
    if (range2.size() === 0) {
      return [range1];
    }
    return [range1, range2];
  }
  // other overlaps on the bottom
  if (other.from > this.from && other.from < this.to) {
    return [new scout.Range(this.from, other.from)];
  }
  // other overlaps on the top
  if (this.from > other.from && this.from < other.to) {
    return [new scout.Range(other.to, this.to)];
  }
  // other is outside
  return [new scout.Range(this.from, this.to)];
};

scout.Range.prototype.union = function(other) {
  if (this.to < other.from || other.to < this.from) {
    var range1 = new scout.Range(this.from, this.to);
    var range2 = new scout.Range(other.from, other.to);
    if (range1.size() === 0) {
      return [range2];
    }
    if (range2.size() === 0) {
      return [range1];
    }
    return [range1, range2];
  }
  return [new scout.Range(Math.min(this.from, other.from), Math.max(this.to, other.to))];
};

scout.Range.prototype.intersect = function(other) {
  if (this.to <= other.from || other.to <= this.from) {
    return new scout.Range(0, 0);
  }
  return new scout.Range(Math.max(this.from, other.from), Math.min(this.to, other.to));
};

scout.Range.prototype.size = function(other) {
  return this.to - this.from;
};

scout.Range.prototype.contains = function(value) {
  return this.from <= value && value < this.to;
};

scout.Range.prototype.toString = function() {
  return 'scout.Range[' +
    'from=' + (this.from === null ? 'null' : this.from) +
    ' to=' + (this.to === null ? 'null' : this.to) + ']';
  //FIXME CGU this is a date range
//  'from=' + (this.from === null ? 'null' : this.from.toUTCString()) +
//  ' to=' + (this.to === null ? 'null' : this.to.toUTCString()) + ']';
};
