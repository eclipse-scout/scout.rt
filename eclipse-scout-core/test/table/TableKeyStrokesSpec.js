/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys} from '../../src/index';
import {TableSpecHelper} from '../../src/testing/index';

describe('TableKeyStrokes', () => {
  let session;
  let helper;

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

  describe('key up', () => {

    it('selects the above row', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);

      let row2 = table.rows[2];
      table.render();
      helper.selectRowsAndAssert(table, [row2]);

      table.$data.triggerKeyDown(keys.UP);
      helper.assertSelection(table, [table.rows[1]]);
      table.$data.triggerKeyDown(keys.UP);
      helper.assertSelection(table, [table.rows[0]]);
      table.$data.triggerKeyDown(keys.UP);
      helper.assertSelection(table, [table.rows[0]]);
    });

    it('selects the last row if no row is selected yet', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);

      table.render();

      table.$data.triggerKeyDown(keys.UP);
      helper.assertSelection(table, [table.rows[4]]);
    });

    it('selects the second last row if all rows are selected', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);

      table.render();
      table.selectAll();

      table.$data.triggerKeyDown(keys.UP);
      helper.assertSelection(table, [table.rows[3]]);
    });

    it('selects the only row if there is only one', () => {
      let model = helper.createModelFixture(2, 1);
      let table = helper.createTable(model);

      table.render();

      table.$data.triggerKeyDown(keys.UP);
      helper.assertSelection(table, [table.rows[0]]);

      table.deselectAll();
      table.selectionHandler.lastActionRow = table.rows[0];

      table.$data.triggerKeyDown(keys.UP);
      helper.assertSelection(table, [table.rows[0]]);
    });

    it('does nothing if first row already is selected', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);
      let rows = table.rows;

      table.render();
      helper.selectRowsAndAssert(table, [rows[0], rows[1]]);

      table.$data.triggerKeyDown(keys.UP);
      helper.assertSelection(table, [rows[0], rows[1]]);
    });

    it('if first row already is selected but is not the last action row, the row above the last action row gets selected', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);
      let rows = table.rows;

      table.render();
      helper.selectRowsAndAssert(table, [rows[0], rows[1], rows[2]]);

      table.selectionHandler.lastActionRow = rows[2];

      table.$data.triggerKeyDown(keys.UP);
      helper.assertSelection(table, [rows[1]]);
    });

    it('if there is a last action row, selects the row above last last action row', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);

      let rows = table.rows;
      table.render();
      helper.selectRowsAndAssert(table, [rows[2], rows[4]]);

      table.selectionHandler.lastActionRow = rows[4];

      table.$data.triggerKeyDown(keys.UP);
      helper.assertSelection(table, [rows[3]]);
    });

    it('selects the row above the last action row even if the row above already is selected', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);

      let rows = table.rows;
      table.render();
      helper.selectRowsAndAssert(table, [rows[2], rows[3], rows[4]]);

      table.selectionHandler.lastActionRow = rows[4];

      table.$data.triggerKeyDown(keys.UP);
      helper.assertSelection(table, [rows[3]]);
    });

    it('uses last row of selection as last action row if last action row is not visible anymore', () => {
      let model = helper.createModelFixture(2, 6);
      let table = helper.createTable(model);
      let rows = table.rows;
      table.render();
      helper.selectRowsAndAssert(table, [rows[1], rows[3], rows[4]]);
      table.selectionHandler.lastActionRow = rows[4];

      table.addFilter({
        createKey: () => 1,
        accept: row => row !== rows[4]
      });

      table.$data.triggerKeyDown(keys.UP);
      helper.assertSelection(table, [rows[0]]);
    });

    describe(' + shift', () => {

      it('adds the row above to the selection', () => {
        let model = helper.createModelFixture(2, 5);
        let table = helper.createTable(model);
        let rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[2]]);

        table.$data.triggerKeyDown(keys.UP, 'shift');
        helper.assertSelection(table, [rows[1], rows[2]]);
        table.$data.triggerKeyDown(keys.UP, 'shift');
        helper.assertSelection(table, [rows[0], rows[1], rows[2]]);
        table.$data.triggerKeyDown(keys.UP, 'shift');
        helper.assertSelection(table, [rows[0], rows[1], rows[2]]);
      });

      it('removes the row above from the selection if the last action row is the last row of the selection', () => {
        let model = helper.createModelFixture(2, 6);
        let table = helper.createTable(model);

        let rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[2], rows[3], rows[4]]);

        table.selectionHandler.lastActionRow = rows[4];

        table.$data.triggerKeyDown(keys.UP, 'shift');
        helper.assertSelection(table, [rows[2], rows[3]]);
        table.$data.triggerKeyDown(keys.UP, 'shift');
        helper.assertSelection(table, [rows[2]]);
        table.$data.triggerKeyDown(keys.UP, 'shift');
        helper.assertSelection(table, [rows[1], rows[2]]);
      });

      it('if the row above the last action row is not selected, adds the row above to the selection', () => {
        let model = helper.createModelFixture(2, 6);
        let table = helper.createTable(model);

        let rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[2], rows[5]]);

        table.selectionHandler.lastActionRow = rows[5];

        table.$data.triggerKeyDown(keys.UP, 'shift');
        helper.assertSelection(table, [rows[2], rows[4], rows[5]]);
        table.$data.triggerKeyDown(keys.UP, 'shift');
        helper.assertSelection(table, [rows[2], rows[3], rows[4], rows[5]]);
        table.$data.triggerKeyDown(keys.UP, 'shift');
        helper.assertSelection(table, [rows[1], rows[2], rows[3], rows[4], rows[5]]);
      });

    });

  });

  describe('key down', () => {

    it('selects the row below', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);
      let row2 = table.rows[2];
      table.render();
      helper.selectRowsAndAssert(table, [row2]);

      table.$data.triggerKeyDown(keys.DOWN);
      helper.assertSelection(table, [table.rows[3]]);
      table.$data.triggerKeyDown(keys.DOWN);
      helper.assertSelection(table, [table.rows[4]]);
      table.$data.triggerKeyDown(keys.DOWN);
      helper.assertSelection(table, [table.rows[4]]);
    });

    it('selects the first row if no row is selected yet', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);

      table.render();

      table.$data.triggerKeyDown(keys.DOWN);
      helper.assertSelection(table, [table.rows[0]]);
    });

    it('selects the second row if all rows are selected', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);

      table.render();
      table.selectAll();

      table.$data.triggerKeyDown(keys.DOWN);
      helper.assertSelection(table, [table.rows[1]]);
    });

    it('selects the only row if there is only one', () => {
      let model = helper.createModelFixture(2, 1);
      let table = helper.createTable(model);

      table.render();

      table.$data.triggerKeyDown(keys.DOWN);
      helper.assertSelection(table, [table.rows[0]]);

      table.deselectAll();
      table.selectionHandler.lastActionRow = table.rows[0];

      table.$data.triggerKeyDown(keys.DOWN);
      helper.assertSelection(table, [table.rows[0]]);
    });

    it('does nothing if last row already is selected', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);
      let rows = table.rows;

      table.render();
      helper.selectRowsAndAssert(table, [rows[3], rows[4]]);

      table.$data.triggerKeyDown(keys.DOWN);
      helper.assertSelection(table, [rows[3], rows[4]]);
    });

    it('if there is a last action row, selects the row below the last action row', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);

      let rows = table.rows;
      table.render();
      helper.selectRowsAndAssert(table, [rows[2], rows[4]]);

      table.selectionHandler.lastActionRow = rows[2];

      table.$data.triggerKeyDown(keys.DOWN);
      helper.assertSelection(table, [rows[3]]);
    });

    it('selects the row below the last action row even if the row below already is selected', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);

      let rows = table.rows;
      table.render();
      helper.selectRowsAndAssert(table, [rows[2], rows[3], rows[4]]);

      table.selectionHandler.lastActionRow = rows[2];

      table.$data.triggerKeyDown(keys.DOWN);
      helper.assertSelection(table, [rows[3]]);
    });

    it('uses last row of selection as last action row if last action row is not visible anymore', () => {
      let model = helper.createModelFixture(2, 6);
      let table = helper.createTable(model);
      let rows = table.rows;
      table.render();
      helper.selectRowsAndAssert(table, [rows[1], rows[3], rows[4]]);
      table.selectionHandler.lastActionRow = rows[1];

      table.addFilter({
        createKey: () => 1,
        accept: row => row !== rows[1]
      });

      table.$data.triggerKeyDown(keys.DOWN);
      helper.assertSelection(table, [rows[5]]);
    });

    describe(' + shift', () => {

      it('adds the row below to the selection', () => {
        let model = helper.createModelFixture(2, 5);
        let table = helper.createTable(model);
        let rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[2]]);

        table.$data.triggerKeyDown(keys.DOWN, 'shift');
        helper.assertSelection(table, [rows[2], rows[3]]);
        table.$data.triggerKeyDown(keys.DOWN, 'shift');
        helper.assertSelection(table, [rows[2], rows[3], rows[4]]);
        table.$data.triggerKeyDown(keys.DOWN, 'shift');
        helper.assertSelection(table, [rows[2], rows[3], rows[4]]);
      });

      it('removes the row below from the selection if the last action row is the first row of the selection', () => {
        let model = helper.createModelFixture(2, 6);
        let table = helper.createTable(model);

        let rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[2], rows[3], rows[4]]);

        table.selectionHandler.lastActionRow = rows[2];

        table.$data.triggerKeyDown(keys.DOWN, 'shift');
        helper.assertSelection(table, [rows[3], rows[4]]);
        table.$data.triggerKeyDown(keys.DOWN, 'shift');
        helper.assertSelection(table, [rows[4]]);
        table.$data.triggerKeyDown(keys.DOWN, 'shift');
        helper.assertSelection(table, [rows[4], rows[5]]);
      });

      it('if the row below the last action row is not selected, adds the row below to the selection', () => {
        let model = helper.createModelFixture(2, 7);
        let table = helper.createTable(model);

        let rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[2], rows[5]]);

        table.selectionHandler.lastActionRow = rows[2];

        table.$data.triggerKeyDown(keys.DOWN, 'shift');
        helper.assertSelection(table, [rows[2], rows[3], rows[5]]);
        table.$data.triggerKeyDown(keys.DOWN, 'shift');
        helper.assertSelection(table, [rows[2], rows[3], rows[4], rows[5]]);
        table.$data.triggerKeyDown(keys.DOWN, 'shift');
        helper.assertSelection(table, [rows[2], rows[3], rows[4], rows[5], rows[6]]);
      });

    });

  });

  describe('end', () => {

    it('selects last row', () => {
      let model = helper.createModelFixture(2, 4);
      let table = helper.createTable(model);
      let rows = table.rows;
      table.render();
      helper.selectRowsAndAssert(table, [rows[1]]);

      table.$data.triggerKeyDown(keys.END);
      helper.assertSelection(table, [rows[3]]);
    });

    describe(' + shift', () => {

      it('selects all rows from currently selected row to last row', () => {
        let model = helper.createModelFixture(2, 4);
        let table = helper.createTable(model);
        let rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[1]]);

        table.$data.triggerKeyDown(keys.END, 'shift');
        helper.assertSelection(table, [rows[1], rows[2], rows[3]]);
      });

      it('preserves existing selection', () => {
        let model = helper.createModelFixture(2, 6);
        let table = helper.createTable(model);
        let rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[1], rows[3], rows[4]]);

        table.$data.triggerKeyDown(keys.END, 'shift');
        helper.assertSelection(table, [rows[1], rows[3], rows[4], rows[5]]);
      });

      it('considers last action row as start row for new selection', () => {
        let model = helper.createModelFixture(2, 6);
        let table = helper.createTable(model);
        let rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[1], rows[3], rows[4]]);
        table.selectionHandler.lastActionRow = rows[1];

        table.$data.triggerKeyDown(keys.END, 'shift');
        helper.assertSelection(table, [rows[1], rows[2], rows[3], rows[4], rows[5]]);
      });

      it('uses last row of selection as last action row if last action row is not visible anymore', () => {
        let model = helper.createModelFixture(2, 6);
        let table = helper.createTable(model);
        let rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[1], rows[3], rows[4]]);
        table.selectionHandler.lastActionRow = rows[1];

        table.addFilter({
          createKey: () => 1,
          accept: row => row !== rows[1]
        });

        table.$data.triggerKeyDown(keys.END, 'shift');
        helper.assertSelection(table, [rows[3], rows[4], rows[5]]);
      });

      it('does nothing if last row is already selected', () => {
        let model = helper.createModelFixture(2, 6);
        let table = helper.createTable(model);
        let rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[5]]);

        table.$data.triggerKeyDown(keys.END, 'shift');
        helper.assertSelection(table, [rows[5]]);
      });

      it('does not add same rows to selectedRows twice', () => {
        let model = helper.createModelFixture(2, 3);
        let table = helper.createTable(model);
        let rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[0], rows[2]]);
        table.selectionHandler.lastActionRow = rows[0];

        table.$data.triggerKeyDown(keys.END, 'shift');
        helper.assertSelection(table, [rows[0], rows[1], rows[2]]);
      });

    });

  });

  describe('home', () => {

    it('selects first row', () => {
      let model = helper.createModelFixture(2, 4);
      let table = helper.createTable(model);
      let rows = table.rows;
      table.render();
      helper.selectRowsAndAssert(table, [rows[2]]);

      table.$data.triggerKeyDown(keys.HOME);
      helper.assertSelection(table, [rows[0]]);
    });

    describe(' + shift', () => {

      it('selects all rows from currently selected row to first row', () => {
        let model = helper.createModelFixture(2, 4);
        let table = helper.createTable(model);
        let rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[2]]);

        table.$data.triggerKeyDown(keys.HOME, 'shift');
        helper.assertSelection(table, [rows[0], rows[1], rows[2]]);
      });

      it('preserves existing selection', () => {
        let model = helper.createModelFixture(2, 6);
        let table = helper.createTable(model);
        let rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[2], rows[3], rows[5]]);

        table.$data.triggerKeyDown(keys.HOME, 'shift');
        helper.assertSelection(table, [rows[0], rows[1], rows[2], rows[3], rows[5]]);
      });

      it('considers last action row as start row for new selection', () => {
        let model = helper.createModelFixture(2, 6);
        let table = helper.createTable(model);
        let rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[1], rows[2], rows[4]]);
        table.selectionHandler.lastActionRow = rows[4];

        table.$data.triggerKeyDown(keys.HOME, 'shift');
        helper.assertSelection(table, [rows[0], rows[1], rows[2], rows[3], rows[4]]);
      });

      it('uses first row of selection as last action row if last action row is not visible anymore', () => {
        let model = helper.createModelFixture(2, 6);
        let table = helper.createTable(model);
        let rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[1], rows[3], rows[4]]);
        table.selectionHandler.lastActionRow = rows[4];

        table.addFilter({
          createKey: () => 1,
          accept: row => row !== rows[4]
        });

        table.$data.triggerKeyDown(keys.HOME, 'shift');
        helper.assertSelection(table, [rows[0], rows[1], rows[3]]);
      });

      it('does nothing if first row is already selected', () => {
        let model = helper.createModelFixture(2, 6);
        let table = helper.createTable(model);
        let rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[0]]);

        table.$data.triggerKeyDown(keys.HOME, 'shift');
        helper.assertSelection(table, [rows[0]]);
      });

      it('does not add same rows to selectedRows twice', () => {
        let model = helper.createModelFixture(2, 3);
        let table = helper.createTable(model);
        let rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[0], rows[2]]);
        table.selectionHandler.lastActionRow = rows[2];

        table.$data.triggerKeyDown(keys.HOME, 'shift');
        helper.assertSelection(table, [rows[0], rows[1], rows[2]]);
      });

    });

  });

  describe('space', () => {

    it('does nothing if no rows are selected', () => {
      let model = helper.createModelFixture(2, 4);
      model.checkable = true;
      let table = helper.createTable(model);
      let rows = table.rows;
      table.checkRow(rows[2], true);
      table.render();

      table.$data.triggerKeyDown(keys.SPACE);
      expect(rows[0].checked).toBe(false);
      expect(rows[1].checked).toBe(false);
      expect(rows[2].checked).toBe(true);
      expect(rows[3].checked).toBe(false);
    });

    it('checks the selected rows if first row is unchecked', () => {
      let model = helper.createModelFixture(2, 4);
      model.checkable = true;
      let table = helper.createTable(model);
      let rows = table.rows;
      table.render();
      helper.selectRowsAndAssert(table, [rows[1], rows[2]]);

      table.$data.triggerKeyDown(keys.SPACE);
      expect(rows[0].checked).toBe(false);
      expect(rows[1].checked).toBe(true);
      expect(rows[2].checked).toBe(true);
      expect(rows[3].checked).toBe(false);
    });

    it('does not modify already checked rows when checking', () => {
      let model = helper.createModelFixture(2, 4);
      model.checkable = true;
      let table = helper.createTable(model);
      let rows = table.rows;
      table.render();
      table.checkRow(rows[2], true);
      helper.selectRowsAndAssert(table, [rows[1], rows[2]]);

      table.$data.triggerKeyDownCapture(keys.SPACE);
      expect(rows[0].checked).toBe(false);
      expect(rows[1].checked).toBe(true);
      expect(rows[2].checked).toBe(true);
      expect(rows[3].checked).toBe(false);

      table.$data.triggerKeyUpCapture(keys.SPACE);
      table.$data.triggerKeyDownCapture(keys.SPACE);
      expect(rows[0].checked).toBe(false);
      expect(rows[1].checked).toBe(false);
      expect(rows[2].checked).toBe(false);
      expect(rows[3].checked).toBe(false);
    });

    it('unchecks the selected rows if first row is checked', () => {
      let model = helper.createModelFixture(2, 4);
      model.checkable = true;
      let table = helper.createTable(model);
      let rows = table.rows;
      table.render();
      table.checkRow(rows[0], true);
      table.checkRow(rows[1], true);
      table.checkRow(rows[2], true);
      table.checkRow(rows[3], true);
      helper.selectRowsAndAssert(table, [rows[1], rows[2]]);

      table.$data.triggerKeyDown(keys.SPACE);
      expect(rows[0].checked).toBe(true);
      expect(rows[1].checked).toBe(false);
      expect(rows[2].checked).toBe(false);
      expect(rows[3].checked).toBe(true);
    });

  });

  describe('page up', () => {

    it('selects the only row if there is only one', () => {
      let model = helper.createModelFixture(2, 1);
      let table = helper.createTable(model);

      table.render();

      table.$data.triggerKeyDown(keys.PAGE_UP);
      helper.assertSelection(table, [table.rows[0]]);

      table.deselectAll();
      table.selectionHandler.lastActionRow = table.rows[0];

      table.$data.triggerKeyDown(keys.PAGE_UP);
      helper.assertSelection(table, [table.rows[0]]);
    });

  });

  describe('page down', () => {

    it('selects the only row if there is only one', () => {
      let model = helper.createModelFixture(2, 1);
      let table = helper.createTable(model);

      table.render();

      table.$data.triggerKeyDown(keys.PAGE_DOWN);
      helper.assertSelection(table, [table.rows[0]]);

      table.deselectAll();
      table.selectionHandler.lastActionRow = table.rows[0];

      table.$data.triggerKeyDown(keys.PAGE_DOWN);
      helper.assertSelection(table, [table.rows[0]]);
    });

  });
});
