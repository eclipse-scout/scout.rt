/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DateRange, dates} from '../../src/index';

describe('DateRange', () => {

  it('equals', () => {
    let range1 = new DateRange(dates.create('2015-24-11'), dates.create('2015-25-11')),
      range2 = new DateRange(dates.create('2015-24-11'), dates.create('2015-25-11')),
      range3 = new DateRange(dates.create('2014-24-11'), dates.create('2014-25-11'));

    expect(range1.equals(range2)).toBe(true);
    expect(range2.equals(range1)).toBe(true);
    expect(range1.equals(range3)).toBe(false);
    expect(new DateRange().equals(new DateRange())).toBe(true);
    expect(new DateRange().equals(null)).toBe(false);
    expect(new DateRange().equals(undefined)).toBe(false);
  });

});
