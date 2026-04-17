/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, DateColumn, DateGroupType, dates} from '../../../src/index';
import {TableSpecHelper} from '../../../src/testing/index';

describe('DateColumn', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('format', () => {
    it('updates the value and the display text if the format changes', () => {
      let testDate = dates.create('2017-01-01 13:01');
      let model = helper.createModelSingleColumnByValues([testDate], 'DateColumn');
      let table = helper.createTable(model);
      let column0 = table.columns[0] as DateColumn;
      column0.setFormat(undefined);
      table.render();

      expect(column0.cell(table.rows[0]).text).toBe('01.01.2017');
      expect(column0.cell(table.rows[0]).value).toBe(testDate);

      column0.setFormat('yyyy-MM-dd');
      expect(column0.cell(table.rows[0]).text).toBe('2017-01-01');
    });
  });

  describe('compare', () => {
    it('sorts grouped date columns by group type and date, unless there are additional sort columns', () => {
      let date0 = dates.create('2018-03-03'); // B
      let date1 = dates.create('2017-03-03'); // C
      let date2 = dates.create('2019-03-02'); // A
      let date3 = dates.create('2018-01-01'); // D

      let model = helper.createModel([
        {
          id: 'col0',
          objectType: DateColumn
        },
        {
          id: 'col1',
          objectType: Column
        }
      ], [
        {id: 'row0', cells: [date0, 'C']},
        {id: 'row1', cells: [date1, 'D']},
        {id: 'row_', cells: [null, null]},
        {id: 'row2', cells: [date2, 'B']},
        {id: 'row3', cells: [date3, 'A']}
      ]);
      let table = helper.createTable(model);
      let dateColumn = table.columns[0] as DateColumn;
      let stringColumn = table.columns[1] as Column;
      let [row0, row1, row_, row2, row3] = table.rows;
      table.render();

      expect(table.rows).toEqual([row0, row1, row_, row2, row3]); // insertion order
      expect(table.rows.map(row => dateColumn.cellValue(row))).toEqual([date0, date1, null, date2, date3]);

      table.group(dateColumn, 'asc');
      expect(table.rows).toEqual([row_, row1, row3, row0, row2]); // grouped by year, sorted by date

      dateColumn.setGroupType(DateGroupType.MONTH);
      expect(table.rows).toEqual([row_, row3, row1, row0, row2]); // grouped by month, sorted by date

      table.group(stringColumn, 'asc', true);
      expect(table.rows).toEqual([row_, row3, row2, row0, row1]); // grouped by month, sorted by string column
    });
  });
});
