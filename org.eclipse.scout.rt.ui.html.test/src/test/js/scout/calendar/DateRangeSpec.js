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
describe('DateRange', function() {

  it('equals', function() {
    var range1 = new scout.DateRange(scout.dates.create('2015-24-11'), scout.dates.create('2015-25-11')),
      range2 = new scout.DateRange(scout.dates.create('2015-24-11'), scout.dates.create('2015-25-11')),
      range3 = new scout.DateRange(scout.dates.create('2014-24-11'), scout.dates.create('2014-25-11'));

    expect(range1.equals(range2)).toBe(true);
    expect(range2.equals(range1)).toBe(true);
    expect(range1.equals(range3)).toBe(false);
  });

});
