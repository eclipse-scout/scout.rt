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
scout.DateRange = function(from, to) {
  this.from = from;
  this.to = to;
};

scout.DateRange.prototype.equals = function(other) {
  if (!other) {
    return false;
  }
  return scout.dates.equals(this.from, other.from) &&
    scout.dates.equals(this.to, other.to);
};

scout.DateRange.prototype.toString = function() {
  return 'scout.DateRange[' +
    'from=' + (this.from === null ? 'null' : this.from.toUTCString()) +
    ' to=' + (this.to === null ? 'null' : this.to.toUTCString()) + ']';
};

scout.DateRange.ensure = function(dateRange) {
  if (!dateRange) {
    return dateRange;
  }
  if (dateRange instanceof scout.DateRange) {
    return dateRange;
  }
  return new scout.DateRange(
    scout.dates.ensure(dateRange.from),
    scout.dates.ensure(dateRange.to));
};
