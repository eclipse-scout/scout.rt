/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {icons} from '../../../src/index';
import {TableSpecHelper} from '../../../src/testing/index';

describe('IconColumn', () => {
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
