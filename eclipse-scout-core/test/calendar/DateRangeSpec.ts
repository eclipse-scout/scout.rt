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
