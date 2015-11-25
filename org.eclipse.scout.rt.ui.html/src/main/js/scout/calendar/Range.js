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

scout.Range.prototype.toString = function() {
  return 'scout.Range[' +
    'from=' + (this.from === null ? 'null' : this.from.toUTCString()) +
    ' to=' + (this.to === null ? 'null' : this.to.toUTCString()) + ']';
};
