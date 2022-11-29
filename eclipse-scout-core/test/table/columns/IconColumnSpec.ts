/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {icons} from '../../../src/index';
import {TableSpecHelper} from '../../../src/testing/index';

describe('IconColumn', () => {
  let session;
  let helper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
  });

  afterEach(() => {
    session = null;
  });

  describe('setCellValue', () => {
    it('updates the icon', () => {
      let model = helper.createModelSingleColumnByValues([icons.FOLDER], 'IconColumn');
      let table = helper.createTable(model);
      let column0 = table.columns[0];
      let updateRowCount = 0;
      table.render();

      expect(column0.cell(table.rows[0]).text).toBe(null);
      expect(column0.cell(table.rows[0]).value).toBe(icons.FOLDER);
      expect(column0.cell(table.rows[0]).iconId).toBe(icons.FOLDER);
      expect(table.$cell(column0, table.rows[0].$row).text()).toBe(icons.FOLDER.replace('font:', ''));

      table.on('rowsUpdated', event => updateRowCount++);
      column0.setCellValue(table.rows[0], icons.CALENDAR);
      expect(column0.cell(table.rows[0]).text).toBe(null);
      expect(column0.cell(table.rows[0]).value).toBe(icons.CALENDAR);
      expect(column0.cell(table.rows[0]).iconId).toBe(icons.CALENDAR);
      expect(table.$cell(column0, table.rows[0].$row).text()).toBe(icons.CALENDAR.replace('font:', ''));
      expect(updateRowCount).toBe(1);
    });
  });
});
