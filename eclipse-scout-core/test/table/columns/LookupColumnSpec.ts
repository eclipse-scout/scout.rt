/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {TableSpecHelper} from '../../../src/testing/index';
import {LookupCall, LookupColumn, LookupResult, scout, StaticLookupCall} from '../../../src';

describe('LookupColumn', () => {
  let session: SandboxSession, helper: TableSpecHelper, lookupCall: LookupCall<number>;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    lookupCall = scout.create(StaticLookupCall<number>, {
      session,
      data: [
        [1, 'A'],
        [2, 'B'],
        [3, 'C'],
        [4, 'D'],
        [5, 'E']
      ]
    });
  });

  describe('formatValue', () => {

    it('concatenates all selected values', async () => {
      const table = helper.createTable({
        columns: [{
          objectType: LookupColumn,
          lookupCall
        }]
      });

      table.insertRow({
        cells: [null]
      });
      const column = table.columns[0];
      const row = table.rows[0];

      expect(column.cellText(row)).toBe('');

      column.setCellValue(row, [2]);
      await table.when('rowsUpdated');
      expect(column.cellText(row)).toBe('B');

      column.setCellValue(row, [1, 3, 4]);
      await table.when('rowsUpdated');
      expect(column.cellText(row)).toBe('A, C, D');

      column.setCellValue(row, [5, 6]);
      await table.when('rowsUpdated');
      expect(column.cellText(row)).toBe('E');
    });
  });

  describe('isContentValid', () => {

    it('checks if values are distinct if distinct-flag is set', () => {
      const table = helper.createTable({
        columns: [{
          objectType: LookupColumn,
          lookupCall
        }]
      });

      table.insertRows([
        {cells: [null]},
        {cells: [[1]]},
        {cells: [[2, 3]]},
        {cells: [[4, 5]]}
      ]);
      const column = table.columns[0] as LookupColumn<number>;
      const [row0, row1, row2, row3] = table.rows;

      expect(column.isContentValid(row0).valid).toBeTrue();
      expect(column.isContentValid(row1).valid).toBeTrue();
      expect(column.isContentValid(row2).valid).toBeTrue();
      expect(column.isContentValid(row3).valid).toBeTrue();

      column.setDistinct(true);

      expect(column.isContentValid(row0).valid).toBeTrue();
      expect(column.isContentValid(row1).valid).toBeTrue();
      expect(column.isContentValid(row2).valid).toBeTrue();
      expect(column.isContentValid(row3).valid).toBeTrue();

      column.setDistinct(false);
      table.insertRow({cells: [[1, 5]]});
      const row4 = table.rows[4];

      expect(column.isContentValid(row0).valid).toBeTrue();
      expect(column.isContentValid(row1).valid).toBeTrue();
      expect(column.isContentValid(row2).valid).toBeTrue();
      expect(column.isContentValid(row3).valid).toBeTrue();
      expect(column.isContentValid(row4).valid).toBeTrue();

      column.setDistinct(true);

      expect(column.isContentValid(row0).valid).toBeTrue();
      expect(column.isContentValid(row1).valid).toBeFalse();
      expect(column.isContentValid(row2).valid).toBeTrue();
      expect(column.isContentValid(row3).valid).toBeFalse();
      expect(column.isContentValid(row4).valid).toBeFalse();
    });
  });

  describe('editor', () => {

    it('disables already selected rows if distinct-flag is set', async () => {
      const table = helper.createTable({
        columns: [{
          objectType: LookupColumn,
          editable: true,
          lookupCall
        }]
      });
      table.render();

      table.insertRows([
        {cells: [null]},
        {cells: [[1]]},
        {cells: [[2, 3]]},
        {cells: [[1]]}
      ]);
      const column = table.columns[0] as LookupColumn<number>;
      const [row0, row1, row2, row3] = table.rows;

      table.prepareCellEdit(column, row0);
      await expectLookupResultEnabledState(column, new Map([[1, true], [2, true], [3, true], [4, true], [5, true]]));
      table.completeCellEdit();

      table.prepareCellEdit(column, row1);
      await expectLookupResultEnabledState(column, new Map([[1, true], [2, true], [3, true], [4, true], [5, true]]));
      table.completeCellEdit();

      table.prepareCellEdit(column, row2);
      await expectLookupResultEnabledState(column, new Map([[1, true], [2, true], [3, true], [4, true], [5, true]]));
      table.completeCellEdit();

      table.prepareCellEdit(column, row3);
      await expectLookupResultEnabledState(column, new Map([[1, true], [2, true], [3, true], [4, true], [5, true]]));
      table.completeCellEdit();

      column.setDistinct(true);

      table.prepareCellEdit(column, row0);
      await expectLookupResultEnabledState(column, new Map([[1, false], [2, false], [3, false], [4, true], [5, true]]));
      table.completeCellEdit();

      table.prepareCellEdit(column, row1);
      await expectLookupResultEnabledState(column, new Map([[1, true], [2, false], [3, false], [4, true], [5, true]]));
      table.completeCellEdit();

      table.prepareCellEdit(column, row2);
      await expectLookupResultEnabledState(column, new Map([[1, false], [2, true], [3, true], [4, true], [5, true]]));
      table.completeCellEdit();

      table.prepareCellEdit(column, row3);
      await expectLookupResultEnabledState(column, new Map([[1, true], [2, false], [3, false], [4, true], [5, true]]));
      table.completeCellEdit();
    });

    async function expectLookupResultEnabledState(column: LookupColumn<number>, expectedEnabledState: Map<number, boolean>) {
      const lookupCallDoneEvent = await column.when('lookupCallDone');
      const enabledState = getLookupResultEnabledState(lookupCallDoneEvent.result);
      expect(enabledState).toEqual(expectedEnabledState);
    }

    function getLookupResultEnabledState(result: LookupResult<number>): Map<number, boolean> {
      const enabledState = new Map();
      result.lookupRows.forEach(row => enabledState.set(row.key, row.enabled));
      return enabledState;
    }
  });
});
