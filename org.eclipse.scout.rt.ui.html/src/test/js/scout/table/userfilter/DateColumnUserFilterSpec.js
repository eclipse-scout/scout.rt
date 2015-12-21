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
/* global TableSpecHelper*/
describe('DateColumnUserFilter', function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
  });

  afterEach(function() {
    session = null;
  });

  function createFilter(table) {
    return scout.create('TableTextUserFilter', {
      session: session,
      table: table
    });
  }

  it('acceptByFields works', function() {
    var filter = new scout.DateColumnUserFilter(),
      date21 = scout.dates.create('2015-12-21'),
      date22 = scout.dates.create('2015-12-22'),
      date23 = scout.dates.create('2015-12-23'),
      date24 = scout.dates.create('2015-12-24');

    filter.dateFrom = date21;
    filter.dateTo = date23;
    expect(filter.acceptByFields(date22)).toBe(true);
    expect(filter.acceptByFields(date24)).toBe(false);
    expect(filter.acceptByFields(null)).toBe(false);

    filter.dateFrom = date21;
    filter.dateTo = null;
    expect(filter.acceptByFields(date24)).toBe(true);
    expect(filter.acceptByFields(null)).toBe(false);

    filter.dateFrom = null;
    filter.dateTo = date24;
    expect(filter.acceptByFields(date21)).toBe(true);
    expect(filter.acceptByFields(null)).toBe(false);
  });

});
