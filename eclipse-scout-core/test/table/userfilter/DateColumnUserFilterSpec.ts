/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DateColumnUserFilter, dates, FilterFieldsGroupBox} from '../../../src/index';
import {TableSpecHelper} from '../../../src/testing/index';
import {FullModelOf, InitModelOf} from '../../../src/scout';

describe('DateColumnUserFilter', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
  });

  afterEach(() => {
    session = null;
  });

  it('acceptByFields works', () => {
    let filter = new DateColumnUserFilter(),
      date21 = dates.create('2015-12-21'),
      date22 = dates.create('2015-12-22'),
      date23 = dates.create('2015-12-23'),
      date24 = dates.create('2015-12-24');

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

  it('acceptByFields works with time', () => {
    let filter = new DateColumnUserFilter(),
      filterDateFrom = dates.create('2015-12-21'),
      filterDateTo = dates.create('2015-12-21'),
      dateTimePrevDayMax = dates.create('2015-12-20 23:59:59.999'),
      dateTimeMin = dates.create('2015-12-21 00:00:00.000'),
      dateTimeMax = dates.create('2015-12-21 23:59:59.999'),
      dateTimeNextDayMin = dates.create('2015-12-22 00:00:00.000');

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

  class SpecFilterFieldsGroupBox extends FilterFieldsGroupBox {
    override _init(model: InitModelOf<this>) {
      super._init(model);
    }
  }

  it('addFilterFields must not create date fields with time', () => {
    // In case this test case fails, the date filter fields are created with time.
    // If this is intended, the acceptByFields() implementation for DateColumnUserFilter has to be checked/adjusted to ensure correct filter functionality.
    let model = createSimpleModel(SpecFilterFieldsGroupBox, session) as FullModelOf<FilterFieldsGroupBox>,
      filter = new DateColumnUserFilter(),
      box = new SpecFilterFieldsGroupBox();
    model.filter = filter;
    box._init(model);
    filter.addFilterFields(box);
    expect(filter.dateFromField.hasTime).toBe(false);
    expect(filter.dateToField.hasTime).toBe(false);
  });

  describe('aria properties', () => {

    it('has filter fields that are linked with a label', () => {
      let model = createSimpleModel(SpecFilterFieldsGroupBox, session) as FullModelOf<FilterFieldsGroupBox>,
        filter = new DateColumnUserFilter(),
        box = new SpecFilterFieldsGroupBox();
      model.filter = filter;
      model.label = 'boxLabel';
      box.init(model);
      filter.addFilterFields(box);
      filter.dateFromField.label = 'dateFromField';
      box.render();
      expect(filter.dateFromField.$field.attr('aria-labelledBy')).toBeTruthy();
      expect(filter.dateFromField.$field.attr('aria-labelledBy')).toBe(filter.dateFromField.$label.attr('id'));
      expect(filter.dateFromField.$field.attr('aria-label')).toBeFalsy();
    });
  });
});
