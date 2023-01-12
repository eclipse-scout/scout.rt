/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {dates, JsonDateRange} from '../index';

export class DateRange {
  from: Date;
  to: Date;

  constructor(from?: Date, to?: Date) {
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
