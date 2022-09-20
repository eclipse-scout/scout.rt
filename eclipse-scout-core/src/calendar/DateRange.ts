/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {dates} from '../index';

export type JsonDateRange = { from: Date | string; to: Date | string };
export default class DateRange {
  from: Date;
  to: Date;

  constructor(from: Date, to: Date) {
    this.from = from;
    this.to = to;
  }

  equals(other: DateRange): boolean {
    if (!other) {
      return false;
    }
    return dates.equals(this.from, other.from) &&
      dates.equals(this.to, other.to);
  }

  toString(): string {
    return 'scout.DateRange[' +
      'from=' + (this.from === null ? 'null' : this.from.toUTCString()) +
      ' to=' + (this.to === null ? 'null' : this.to.toUTCString()) + ']';
  }

  static ensure(dateRange: DateRange | JsonDateRange): DateRange {
    if (!dateRange) {
      return dateRange as DateRange;
    }
    if (dateRange instanceof DateRange) {
      return dateRange;
    }
    return new DateRange(
      dates.ensure(dateRange.from),
      dates.ensure(dateRange.to));
  }
}
