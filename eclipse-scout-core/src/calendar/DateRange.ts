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
import {dates} from '../index';

export default class DateRange {

  constructor(from, to) {
    this.from = from;
    this.to = to;
  }

  equals(other) {
    if (!other) {
      return false;
    }
    return dates.equals(this.from, other.from) &&
      dates.equals(this.to, other.to);
  }

  toString() {
    return 'scout.DateRange[' +
      'from=' + (this.from === null ? 'null' : this.from.toUTCString()) +
      ' to=' + (this.to === null ? 'null' : this.to.toUTCString()) + ']';
  }

  static ensure(dateRange) {
    if (!dateRange) {
      return dateRange;
    }
    if (dateRange instanceof DateRange) {
      return dateRange;
    }
    return new DateRange(
      dates.ensure(dateRange.from),
      dates.ensure(dateRange.to));
  }
}
