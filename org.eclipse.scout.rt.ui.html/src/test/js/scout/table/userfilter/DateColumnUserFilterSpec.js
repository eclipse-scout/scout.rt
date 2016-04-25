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

  it('acceptByFields works with time', function() {
    var filter = new scout.DateColumnUserFilter(),
      filterDateFrom = scout.dates.create('2015-12-21'),
      filterDateTo = scout.dates.create('2015-12-21'),
      dateTimePrevDayMax = scout.dates.create('2015-12-20 23:59:59.999'),
      dateTimeMin = scout.dates.create('2015-12-21 00:00:00.000'),
      dateTimeMax = scout.dates.create('2015-12-21 23:59:59.999'),
      dateTimeNextDayMin = scout.dates.create('2015-12-22 00:00:00.000');

    filter.dateFrom = filterDateFrom;
    filter.dateTo = filterDateTo;
    expect(filter.acceptByFields(dateTimePrevDayMax)).toBe(false);
    expect(filter.acceptByFields(dateTimeMin)).toBe(true);
    expect(filter.acceptByFields(dateTimeMax)).toBe(true);
    expect(filter.acceptByFields(dateTimeNextDayMin)).toBe(false);
    expect(filter.acceptByFields(null)).toBe(false);

    filter.dateFrom = filterDateFrom;
    filter.dateTo = null;
    expect(filter.acceptByFields(dateTimePrevDayMax)).toBe(false);
    expect(filter.acceptByFields(dateTimeMin)).toBe(true);
    expect(filter.acceptByFields(dateTimeMax)).toBe(true);
    expect(filter.acceptByFields(dateTimeNextDayMin)).toBe(true);
    expect(filter.acceptByFields(null)).toBe(false);

    filter.dateFrom = null;
    filter.dateTo = filterDateTo;
    expect(filter.acceptByFields(dateTimePrevDayMax)).toBe(true);
    expect(filter.acceptByFields(dateTimeMin)).toBe(true);
    expect(filter.acceptByFields(dateTimeMax)).toBe(true);
    expect(filter.acceptByFields(dateTimeNextDayMin)).toBe(false);
    expect(filter.acceptByFields(null)).toBe(false);
  });

  it('addFilterFields must not create date fields with time', function() {
    // In case this test case fails, the date filter fields are created with time.
    // If this is intended, the acceptByFields() implementation for DateColumnUserFilter has to be checked/adjusted to ensure correct filter functionality.
    var model = createSimpleModel('DateColumnUserFilter', session),
      filter = new scout.DateColumnUserFilter(),
      box = new scout.FilterFieldsGroupBox();
    model.filter = filter;
    box._init(model);
    filter.addFilterFields(box);
    expect(filter.dateFromField.hasTime).toBe(false);
    expect(filter.dateToField.hasTime).toBe(false);
  });

});
