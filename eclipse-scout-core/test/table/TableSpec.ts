/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  BeanColumn, Column, ColumnModel, Device, graphics, IconColumn, icons, Menu, MenuDestinations, NumberColumn, ObjectFactory, Range, RemoteEvent, scout, scrollbars, Status, Table, TableField, TableRow, TableRowModel, Tooltip
} from '../../src/index';
import {JQueryTesting, LocaleSpecHelper, SpecTable, TableSpecHelper} from '../../src/testing/index';
import $ from 'jquery';

describe('Table', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;

  /**
   * TestBeanColumn that validates that the table is available in _init
   */
  class TestBeanColumn extends BeanColumn<any> {
    override _init(model) {
      super._init(model);
      expect(this.table).toBeDefined();
      expect(this.table).not.toBeNull();
    }
  }

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    session.locale = new LocaleSpecHelper().createLocale(LocaleSpecHelper.DEFAULT_LOCALE);
    helper = new TableSpecHelper(session);
    session.textMap.add('ui.Sorting', 'sorting');
    session.textMap.add('ui.ascending', 'ascending');
    $.fx.off = true; // generation of sum rows is animated. leads to misleading test failures.
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    helper.resetIntlCollator();
    $.fx.off = false;
  });

  describe('render', () => {

    it('renders CSS class', () => {
      // regular table
      let model = helper.createModelFixture(2, 1);
      let table = helper.createTable(model);
      table.render();
      expect(table.$container).not.toHaveClass('checkable');

      // checkable table (row style)
      model.checkable = true;
      model.checkableStyle = Table.CheckableStyle.TABLE_ROW;
      table = helper.createTable(model);
      table.render();
      expect(table.$container).toHaveClass('checkable');

      // row must have 'checked' class
      table.checkRow(table.rows[0], true);
      expect(table.$container.find('.table-row').first().hasClass('checked')).toBe(true);
    });

    it('renders a table header', () => {
      let model = helper.createModelFixture(2);
      let table = helper.createTable(model);
      table.render();

      expect(table.header).not.toBeUndefined();
    });

    describe('renders table rows', () => {

      it('accepts rows with cells', () => {
        let model = helper.createModelFixture(3, 1);
        model.rows[0] = helper.createModelRowByTexts('1', ['cell1', '', '0']);
        let table = helper.createTable(model);
        table.render();

        let $row0 = table.$rows().eq(0);
        let $cells = $row0.find('.table-cell');
        expect($cells.eq(0).text()).toBe('cell1');
        expect($cells.eq(1).html()).toBe('&nbsp;');
        expect($cells.eq(2).text()).toBe('0');
      });

      it('accepts rows with text only', () => {
        let model = helper.createModelFixture(3, 1);
        model.rows[0] = helper.createModelRowByTexts('1', ['cell1', '', '0'], true);
        let table = helper.createTable(model);
        table.render();

        let $row0 = table.$rows().eq(0);
        let $cells = $row0.find('.table-cell');
        expect($cells.eq(0).text()).toBe('cell1');
        expect($cells.eq(1).html()).toBe('&nbsp;');
        expect($cells.eq(2).text()).toBe('0');
      });

    });

  });

  describe('_calculateViewRangeForRowIndex', () => {
    it('returns a range based on viewRangeSize', () => {
      let model = helper.createModelFixture(2, 10);
      let table = helper.createTable(model);

      table.viewRangeSize = 4;
      expect(table._calculateViewRangeForRowIndex(0)).toEqual(new Range(0, 4));
      expect(table._calculateViewRangeForRowIndex(1)).toEqual(new Range(0, 4));
      expect(table._calculateViewRangeForRowIndex(2)).toEqual(new Range(1, 5));
      expect(table._calculateViewRangeForRowIndex(3)).toEqual(new Range(2, 6));
      expect(table._calculateViewRangeForRowIndex(6)).toEqual(new Range(5, 9));
      expect(table._calculateViewRangeForRowIndex(7)).toEqual(new Range(6, 10));
      expect(table._calculateViewRangeForRowIndex(8)).toEqual(new Range(6, 10));
      expect(table._calculateViewRangeForRowIndex(9)).toEqual(new Range(6, 10));

      table.viewRangeSize = 5;
      expect(table._calculateViewRangeForRowIndex(0)).toEqual(new Range(0, 5));
      expect(table._calculateViewRangeForRowIndex(1)).toEqual(new Range(0, 5));
      expect(table._calculateViewRangeForRowIndex(2)).toEqual(new Range(1, 6));
      expect(table._calculateViewRangeForRowIndex(3)).toEqual(new Range(2, 7));
      expect(table._calculateViewRangeForRowIndex(4)).toEqual(new Range(3, 8));
      expect(table._calculateViewRangeForRowIndex(5)).toEqual(new Range(4, 9));
      expect(table._calculateViewRangeForRowIndex(7)).toEqual(new Range(5, 10));
      expect(table._calculateViewRangeForRowIndex(8)).toEqual(new Range(5, 10));
      expect(table._calculateViewRangeForRowIndex(9)).toEqual(new Range(5, 10));

      table.viewRangeSize = 8;
      expect(table._calculateViewRangeForRowIndex(0)).toEqual(new Range(0, 8));
      expect(table._calculateViewRangeForRowIndex(1)).toEqual(new Range(0, 8));
      expect(table._calculateViewRangeForRowIndex(2)).toEqual(new Range(0, 8));
      expect(table._calculateViewRangeForRowIndex(3)).toEqual(new Range(1, 9));
      expect(table._calculateViewRangeForRowIndex(4)).toEqual(new Range(2, 10));
      expect(table._calculateViewRangeForRowIndex(7)).toEqual(new Range(2, 10));
      expect(table._calculateViewRangeForRowIndex(8)).toEqual(new Range(2, 10));
      expect(table._calculateViewRangeForRowIndex(9)).toEqual(new Range(2, 10));
    });
  });

  describe('rowIcons and checkable rows', () => {

    let model, table, row;

    it('creates an artificial cell when a rowIcon is set on a row', () => {
      model = helper.createModelFixture(1);
      model.rowIconVisible = true;
      table = helper.createTable(model);
      row = helper.createModelRow('1', ['Foo']);
      row.rowIcon = icons.WORLD;
      table.insertRow(row);

      let columns = table.columns;
      expect(columns.length).toBe(2);
      expect(columns[0] instanceof IconColumn).toBe(true);
      let cell = table.cell(table.columns[0], table.rows[0]);
      expect(cell.cssClass).toBe('row-icon-cell');
    });

  });

  describe('insertRows', () => {
    let model, table;

    beforeEach(() => {
      model = helper.createModelFixture(2);
      table = helper.createTable(model);
    });

    it('inserts rows at the end of the table', () => {
      expect(table.rows.length).toBe(0);

      let rows = helper.createModelRows(2, 5);
      table.insertRows(rows);

      expect(table.rows.length).toBe(5);
      expect(Object.keys(table.rowsMap).length).toBe(5);

      rows = helper.createModelRows(2, 3);
      table.insertRows(rows);

      expect(table.rows.length).toBe(5 + 3);
      expect(Object.keys(table.rowsMap).length).toBe(5 + 3);
    });

    it('renders rows only if view range is not full yet', () => {
      table.viewRangeSize = 2;
      table.render();
      expect(table.rows.length).toBe(0);
      expect(table.$rows().length).toBe(0);
      expect(table.viewRangeRendered).toEqual(new Range(0, 0));

      table.insertRows(helper.createModelRows(2, 1));
      expect(table.rows.length).toBe(1);
      expect(table.$rows().length).toBe(1);
      expect(table.viewRangeRendered).toEqual(new Range(0, 1));

      // 2 rows may get rendered, one row already is. Inserting another 2 rows
      // must only render 1 row
      table.insertRows(helper.createModelRows(2, 2));
      expect(table.rows.length).toBe(3);
      expect(table.$rows().length).toBe(2);
      expect(table.viewRangeRendered).toEqual(new Range(0, 2));
    });

    it('rowsInserted event must be triggered before rowOrderChanged event', () => {
      let events = '',
        rowsOnInsert;
      if (!Device.get().supportsInternationalization()) {
        return;
      }
      // we sort 1st column desc which means Z is before A
      model = helper.createModelFixture(1, 0);
      table = helper.createTable(model);
      table.sort(table.columns[0], 'desc');
      table.on('rowsInserted', event => {
        events += 'rowsInserted ';
        rowsOnInsert = event.rows;
      });
      table.on('rowOrderChanged', () => {
        events += 'rowOrderChanged';
      });
      table.insertRows([helper.createModelRow('1', ['A']), helper.createModelRow('1', ['Z'])]);

      // we expect exactly this order of events when new rows are inserted
      expect(events).toBe('rowsInserted rowOrderChanged');

      // when rowsInserted event occurs we expect the rows provided by the event
      // in the order they have been inserted (no sorting is done here)
      expect(rowsOnInsert[0].cells[0].text).toBe('A');
      expect(rowsOnInsert[1].cells[0].text).toBe('Z');

      // expect the rows in the table to be sorted as defined by the sort-column
      expect(table.rows[0].cells[0].text).toBe('Z');
      expect(table.rows[1].cells[0].text).toBe('A');
    });

  });

  describe('updateRows', () => {
    let model, table;

    beforeEach(() => {
      model = helper.createModelFixture(2, 2);
      model.rows[0].cells[0].text = 'cellText0';
      model.rows[0].cells[1].text = 'cellText1';
      table = helper.createTable(model);
    });

    it('updates the model cell texts', () => {
      expect(table.rows[0].cells[0].text).toBe('cellText0');
      expect(table.rows[0].cells[1].text).toBe('cellText1');

      let row = {
        id: table.rows[0].id,
        cells: ['newCellText0', 'newCellText1']
      };
      table.updateRows([row]);

      expect(table.rows[0].cells[0].text).toBe('newCellText0');
      expect(table.rows[0].cells[1].text).toBe('newCellText1');
    });

    it('updates the html cell texts', () => {
      table.render();
      table.validateLayout(); // make sure layout is valid

      let $rows = table.$rows();
      let $cells0 = table.$cellsForRow($rows.eq(0));
      expect($cells0.eq(0).text()).toBe('cellText0');
      expect($cells0.eq(1).text()).toBe('cellText1');

      let row = {
        id: table.rows[0].id,
        cells: ['newCellText0', 'newCellText1']
      };
      spyOn(table, 'updateScrollbars').and.callThrough();
      table.updateRows([row]);
      // simulate layout-validator, we test if invalidateLayoutTree is called in updateRows
      table.validateLayout();
      expect(table.updateScrollbars).toHaveBeenCalled();

      $rows = table.$rows();
      $cells0 = table.$cellsForRow($rows.eq(0));
      expect($cells0.eq(0).text()).toBe('newCellText0');
      expect($cells0.eq(1).text()).toBe('newCellText1');
    });

    it('does not fail if the row to update is the same instance as the existing one', () => {
      table.render();
      let $rows = table.$rows();
      let $cells0 = table.$cellsForRow($rows.eq(0));
      expect($cells0.eq(0).text()).toBe('cellText0');
      expect($cells0.eq(1).text()).toBe('cellText1');

      table.rows[0].cells[0].setText('newCellText0');
      table.rows[0].cells[1].setText('newCellText1');
      table.updateRows([table.rows[0]]);

      $rows = table.$rows();
      $cells0 = table.$cellsForRow($rows.eq(0));
      expect($cells0.eq(0).text()).toBe('newCellText0');
      expect($cells0.eq(1).text()).toBe('newCellText1');
    });

    it('does not destroy selection', () => {
      model = helper.createModelFixture(2, 3);
      model.rows[0].cells[0].text = 'cellText0';
      model.rows[0].cells[1].text = 'cellText1';
      table = helper.createTable(model);
      table.render();
      table.selectAll();

      expect(table.$selectedRows().length).toBe(3);
      expect(table.$selectedRows().eq(0)).toHaveClass('select-top');
      expect(table.$selectedRows().eq(1)).toHaveClass('select-middle');
      expect(table.$selectedRows().eq(2)).toHaveClass('select-bottom');
      let row = {
        id: table.rows[0].id,
        cells: ['newCellText0', 'newCellText1']
      };
      table.updateRows([row]);

      expect(table.$selectedRows().length).toBe(3);
      expect(table.$selectedRows().eq(0)).toHaveClass('select-top');
      expect(table.$selectedRows().eq(1)).toHaveClass('select-middle');
      expect(table.$selectedRows().eq(2)).toHaveClass('select-bottom');
    });

    it('silently updates rows which are not in view range', () => {
      table.viewRangeSize = 1;
      table.render();
      expect(table.viewRangeRendered).toEqual(new Range(0, 1));
      expect(table.$rows().length).toBe(1);
      expect(table.rows.length).toBe(2);
      let $rows = table.$rows();
      let $cells0 = table.$cellsForRow($rows.eq(0));
      expect($cells0.eq(0).text()).toBe('cellText0');

      let row0 = {
        id: table.rows[0].id,
        cells: ['newRow0Cell0', 'newRow0Cell1']
      };
      let row1 = {
        id: table.rows[1].id,
        cells: ['newRow1Cell0', 'newRow1Cell1']
      };
      table.updateRows([row0, row1]);

      // only row 0 is rendered but both rows need to be updated
      $rows = table.$rows();
      expect($rows.length).toBe(1);
      $cells0 = table.$cellsForRow($rows.eq(0));
      expect($cells0.eq(0).text()).toBe('newRow0Cell0');
      expect($cells0.eq(1).text()).toBe('newRow0Cell1');
      expect(table.rows[0].cells[0].text).toBe('newRow0Cell0');
      expect(table.rows[0].cells[1].text).toBe('newRow0Cell1');
      expect(table.rows[1].cells[0].text).toBe('newRow1Cell0');
      expect(table.rows[1].cells[1].text).toBe('newRow1Cell1');
    });
  });

  describe('deleteRows', () => {
    let model, table, rows, row0, row1, row2;

    beforeEach(() => {
      model = helper.createModelFixture(2, 3);
      table = helper.createTable(model);
      rows = table.rows;
      row0 = model.rows[0];
      row1 = model.rows[1];
      row2 = model.rows[2];
    });

    it('deletes single rows from model', () => {
      expect(table.rows.length).toBe(3);
      expect(table.rows[0]).toBe(row0);

      table.deleteRows([table.rows[0]]);
      expect(table.rows.length).toBe(2);
      expect(table.rows[0]).toBe(row1);

      table.deleteRows([table.rows[0], table.rows[1]]);
      expect(table.rows.length).toBe(0);
    });

    it('deletes single rows from html document', () => {
      table.render();
      expect(table.$rows().length).toBe(3);

      table.deleteRows([table.rows[0]]);
      expect(table.$rows().length).toBe(2);
      expect(table.$rows().eq(0).data('row').id).toBe(row1.id);
      expect(table.$rows().eq(1).data('row').id).toBe(row2.id);

      table.deleteRows([table.rows[0], table.rows[1]]);
      expect(table.$rows().length).toBe(0);
    });

    it('considers view range (distinguishes between rendered and non rendered rows, adjusts viewRangeRendered)', () => {
      model = helper.createModelFixture(2, 6);
      table = helper.createTable(model);
      let spy = spyOn(table, '_calculateCurrentViewRange').and.returnValue(new Range(1, 4));
      table.render();
      expect(table.viewRangeRendered).toEqual(new Range(1, 4));
      expect(table.$rows().length).toBe(3);
      expect(table.rows.length).toBe(6);

      // reset spy -> view range now starts from 0
      spy.and.callThrough();
      table.viewRangeSize = 3;

      // delete first (not rendered)
      table.deleteRows([table.rows[0]]);
      expect(table.viewRangeRendered).toEqual(new Range(0, 3));
      expect(table.$rows().length).toBe(3);
      expect(table.rows.length).toBe(5);

      // delete first rendered
      table.deleteRows([table.rows[0]]);
      expect(table.viewRangeRendered).toEqual(new Range(0, 3));
      expect(table.$rows().length).toBe(3);
      expect(table.rows.length).toBe(4);

      // delete last not rendered
      table.deleteRows([table.rows[3]]);
      expect(table.viewRangeRendered).toEqual(new Range(0, 3));
      expect(table.$rows().length).toBe(3);
      expect(table.rows.length).toBe(3);

      // delete remaining (rendered) rows
      table.deleteRows([table.rows[0], table.rows[1], table.rows[2]]);
      expect(table.viewRangeRendered).toEqual(new Range(0, 0));
      expect(table.$rows().length).toBe(0);
      expect(table.rows.length).toBe(0);
      expect(table.$fillBefore.height()).toBe(0);
      expect(table.$fillAfter.height()).toBe(0);
    });
  });

  describe('deleteAllRows', () => {
    let model, table;

    beforeEach(() => {
      model = helper.createModelFixture(2, 3);
      table = helper.createTable(model);
    });

    it('deletes all rows from model', () => {
      expect(table.rows.length).toBe(3);

      table.deleteAllRows();
      expect(table.rows.length).toBe(0);
    });

    it('deletes all rows from html document', () => {
      table.render();
      expect(table.$rows().length).toBe(3);

      table.deleteAllRows();
      expect(table.$rows().length).toBe(0);
    });

    it('silently removes not rendered rows', () => {
      table.viewRangeSize = 2;
      table.render();
      expect(table.viewRangeRendered).toEqual(new Range(0, 2));
      expect(table.$rows().length).toBe(2);
      expect(table.rows.length).toBe(3);
      expect(table.$fillBefore.height()).toBe(0);
      expect(table.$fillAfter.height()).not.toBe(0);

      table.deleteAllRows();
      expect(table.viewRangeRendered).toEqual(new Range(0, 0));
      expect(table.$rows().length).toBe(0);
      expect(table.rows.length).toBe(0);
      expect(table.$fillBefore.height()).toBe(0);
      expect(table.$fillAfter.height()).toBe(0);
    });
  });

  describe('updateRowOrder', () => {
    let model, table, row0, row1, row2;

    beforeEach(() => {
      model = helper.createModelFixture(2, 3);
      table = helper.createTable(model);
      row0 = table.rows[0];
      row1 = table.rows[1];
      row2 = table.rows[2];
    });

    it('reorders the model rows', () => {
      table.updateRowOrder([row2, row1, row0]);
      expect(table.rows.length).toBe(3);
      expect(table.rows[0]).toBe(row2);
      expect(table.rows[1]).toBe(row1);
      expect(table.rows[2]).toBe(row0);
    });

    it('reorders the html nodes', () => {
      table.render();
      table.updateRowOrder([row2, row1, row0]);
      let $rows = table.$rows();
      expect(true).toBe(true);
      expect($rows.eq(0).data('row').id).toBe(row2.id);
      expect($rows.eq(1).data('row').id).toBe(row1.id);
      expect($rows.eq(2).data('row').id).toBe(row0.id);
    });

    it('considers view range', () => {
      table.viewRangeSize = 2;
      table.render();

      let $rows = table.$rows();
      expect(table.viewRangeRendered).toEqual(new Range(0, 2));
      expect($rows.eq(0).data('row').id).toBe(model.rows[0].id);
      expect($rows.eq(1).data('row').id).toBe(model.rows[1].id);
      expect(table.$rows().length).toBe(2);
      expect(table.rows.length).toBe(3);

      table.updateRowOrder([row2, row1, row0]);
      $rows = table.$rows();
      expect($rows.eq(0).data('row').id).toBe(model.rows[2].id);
      expect($rows.eq(1).data('row').id).toBe(model.rows[1].id);
      expect(table.$rows().length).toBe(2);
      expect(table.rows.length).toBe(3);
    });
  });

  describe('checkRow', () => {

    function findCheckedRows(rows) {
      let checkedRows = [];
      for (let i = 0; i < rows.length; i++) {
        if (rows[i].checked) {
          checkedRows.push(rows[i]);
        }
      }
      return checkedRows;
    }

    it('checks the row, does not uncheck others if multiCheck is set to true', () => {
      let model = helper.createModelFixture(2, 5);
      model.checkable = true;
      model.multiCheck = true;
      let table = helper.createTable(model);
      table.render();

      let rows = table.rows;
      let checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);

      table.checkRow(rows[0], true);
      table.checkRow(rows[4], true);

      checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(2);

      table.checkRow(rows[4], false);

      checkedRows = [];
      for (let z = 0; z < rows.length; z++) {
        if (rows[z].checked) {
          checkedRows.push(rows[z]);
        }
      }
      expect(checkedRows.length).toBe(1);
    });

    it('unchecks other rows if multiCheck is set to false', () => {
      let model = helper.createModelFixture(2, 5);
      model.checkable = true;
      model.multiCheck = false;
      let table = helper.createTable(model);
      table.render();

      let rows = table.rows;
      let checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);

      table.checkRow(rows[0], true);
      table.checkRow(rows[4], true);

      checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(1);

      table.checkRow(rows[4], false);

      checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);
    });

    it('does not check the row if checkable is set to false', () => {
      let model = helper.createModelFixture(2, 5);
      model.checkable = false;
      model.multiCheck = false;
      let table = helper.createTable(model);
      table.render();

      let rows = table.rows;
      let checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);

      table.checkRow(rows[0], true);
      checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);
    });

    it('does not check the row if the row is disabled', () => {
      let model = helper.createModelFixture(2, 5);
      model.multiCheck = false;
      model.checkable = false;
      let table = helper.createTable(model);
      table.render();

      let rows = table.rows;
      let checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);
      rows[0].setEnabled(false);
      table.checkRow(rows[0], true);
      checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);
    });

    it('does not check the row if the table is disabled', () => {
      let model = helper.createModelFixture(2, 5);
      model.checkable = true;
      model.multiCheck = true;
      let table = helper.createTable(model);
      table.setEnabled(false);
      table.render();

      let rows = table.rows;
      let checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);

      table.checkRow(rows[0], true);
      checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);
    });

    it('considers view range', () => {
      let model = helper.createModelFixture(2, 5);
      model.checkable = true;
      model.multiCheck = true;
      let table = helper.createTable(model);
      table.viewRangeSize = 2;
      table.render();

      let rows = table.rows;
      let checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);

      table.checkRow(rows[0], true);
      table.checkRow(rows[2], true);

      checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(2);
      expect(table.$rows().length).toBe(2);
      expect(table.$rows().eq(0).data('row').checked).toBe(true);
      expect(table.$rows().eq(1).data('row').checked).toBe(false);
    });

    it('keeps added checkable column visible even when reloading factory settings', () => {
      let model = helper.createModelFixture(2, 5);
      model.checkable = true;
      model.multiCheck = true;
      let table = helper.createTable(model);
      table.render(session.$entryPoint);

      let rows = table.rows;
      let checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);

      table.checkRow(rows[0], true);
      checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(1);

      let colsDeepCopy = $.extend(true, [], table.columns);
      expect(table.columns.length).toBe(3);
      colsDeepCopy.shift();
      table.updateColumnStructure(colsDeepCopy);
      expect(table.columns.length).toBe(3);
    });

    it('does not add an additional checkable column if one is already configured', () => {
      let model = helper.createModelSingleConfiguredCheckableColumn(5);
      model.checkable = true;
      model.multiCheck = true;
      let table = helper.createTable(model);
      table.render(session.$entryPoint);

      let rows = table.rows;
      let checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);

      expect(table.columns.length).toBe(1);
    });

    it('checkablestyle.checbox_table_row checks row with click event', () => {
      let model = helper.createModelFixture(2, 5);
      model.checkableStyle = Table.CheckableStyle.CHECKBOX_TABLE_ROW;
      model.checkable = true;
      model.multiCheck = true;
      let table = helper.createTable(model);
      table.render();

      let rows = table.rows;
      let checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(0);

      JQueryTesting.triggerClick(table.$cell(0, table.rows[4].$row));
      JQueryTesting.triggerClick(table.$cell(0, table.rows[1].$row));

      checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(2);

      // click on row1 wouldn't work because of the doubleClickSupport!
      JQueryTesting.triggerClick(table.$cell(0, table.rows[4].$row));

      checkedRows = findCheckedRows(rows);
      expect(checkedRows.length).toBe(1);
    });

  });

  describe('selectRows', () => {

    it('updates model', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);
      table.render();

      let rows = [table.rows[0], model.rows[4]] as TableRow[];
      table.selectRows(rows);

      expect(table.selectedRows).toEqual(rows);
    });

    it('selects rendered rows and unselects others', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);
      table.render();

      let $selectedRows = table.$selectedRows();
      expect($selectedRows.length).toBe(0);

      helper.selectRowsAndAssert(table, [model.rows[0], model.rows[4]] as TableRow[]);
      helper.selectRowsAndAssert(table, [model.rows[2]] as TableRow[]);
    });

    it('considers view range', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);
      let rows = table.rows;
      table.viewRangeSize = 2;
      table.render();
      table.selectRows(rows[2]);
      expect(table.selectedRows.length).toBe(1);
      expect(table.$selectedRows().length).toBe(0);

      table.selectRows([rows[1], rows[2]]);
      expect(table.selectedRows.length).toBe(2);
      expect(table.$selectedRows().length).toBe(1);
    });

    it('triggers rowsSelected', () => {
      let model = helper.createModelFixture(2, 5);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as SpecTable;
      table.render();

      let rows = [table.rows[0], table.rows[4]];
      let eventTriggered = false;
      table.on('rowsSelected', () => {
        eventTriggered = true;
      });
      table.selectRows(rows);
      expect(eventTriggered).toBe(true);
    });

    it('selectedRow() returns first selected row or null when table has no selection', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);
      table.selectRows([table.rows[1], table.rows[2]]);
      expect(table.selectedRow()).toBe(table.rows[1]);

      table.selectRows([]);
      expect(table.selectedRow()).toBe(null);
    });

  });

  describe('toggle selection', () => {
    it('selects all if not all are selected', () => {
      let model = helper.createModelFixture(2, 5);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as SpecTable;
      table.render();

      let $selectedRows = table.$selectedRows();
      expect($selectedRows.length).toBe(0);

      table.toggleSelection();
      helper.assertSelection(table, table.rows);
      sendQueuedAjaxCalls();
      helper.assertSelectionEvent(model.id, helper.getRowIds(model.rows));
    });

    it('selects none if all are selected', () => {
      let model = helper.createModelFixture(2, 5);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as SpecTable;
      table.render();

      let $selectedRows = table.$selectedRows();
      expect($selectedRows.length).toBe(0);

      helper.selectRowsAndAssert(table, table.rows);

      table.toggleSelection();
      helper.assertSelection(table, []);
      sendQueuedAjaxCalls();
      helper.assertSelectionEvent(model.id, []);

      table.toggleSelection();
      helper.assertSelection(table, table.rows);
      sendQueuedAjaxCalls();
      helper.assertSelectionEvent(model.id, helper.getRowIds(table.rows));
    });
  });

  describe('selectAll', () => {
    it('selects all rows', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);
      table.render();

      expect(table.selectedRows.length).toBe(0);
      expect(table.$selectedRows().length).toBe(0);

      table.selectAll();
      expect(table.selectedRows.length).toBe(5);
      expect(table.$selectedRows().length).toBe(5);
    });

    it('considers view range -> renders selection only for rendered rows', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);
      table.viewRangeSize = 2;
      table.render();

      expect(table.selectedRows.length).toBe(0);
      expect(table.$selectedRows().length).toBe(0);

      table.selectAll();
      expect(table.selectedRows.length).toBe(5);
      expect(table.$selectedRows().length).toBe(2);
    });
  });

  describe('doRowAction', () => {

    it('sends rowAction event with row and column', () => {
      let model = helper.createModelFixture(2, 5);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as SpecTable;
      let row0 = table.rows[0];
      let column0 = table.columns[0];

      table.selectedRows = [row0];
      table.render();
      table.doRowAction(row0, column0);

      sendQueuedAjaxCalls();

      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(mostRecentJsonRequest().events.length).toBe(1);

      let event = new RemoteEvent(table.id, 'rowAction', {
        columnId: column0.id,
        rowId: row0.id
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it('does not send rowAction event if the row is not selected', () => {
      let model = helper.createModelFixture(2, 5);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as SpecTable;
      let row0 = table.rows[0];
      let column0 = table.columns[0];

      // no selection at all
      table.selectedRows = [];
      table.render();
      table.doRowAction(row0, column0);

      sendQueuedAjaxCalls();

      expect(jasmine.Ajax.requests.count()).toBe(0);

      // other row selected
      table.selectedRows = [table.rows[1]];
      table.doRowAction(row0, column0);

      sendQueuedAjaxCalls();

      expect(jasmine.Ajax.requests.count()).toBe(0);

      // correct row selected -> expect event
      table.selectedRows = [row0];
      table.doRowAction(row0, column0);

      sendQueuedAjaxCalls();

      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(mostRecentJsonRequest().events.length).toBe(1);

      let event = new RemoteEvent(table.id, 'rowAction', {
        columnId: column0.id,
        rowId: row0.id
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it('does not send rowAction event if it is not the only one selected row', () => {
      let model = helper.createModelFixture(2, 5);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as SpecTable;
      let row0 = table.rows[0];
      let column0 = table.columns[0];

      // no selection at all
      table.selectedRows = [row0, table.rows[1]];
      table.render();
      table.doRowAction(row0, column0);

      sendQueuedAjaxCalls();

      expect(jasmine.Ajax.requests.count()).toBe(0);
    });

  });

  describe('resizeColumn', () => {

    beforeEach(() => {
      $('<style>' +
        '.table-header-item { display: inline-flex; }' +
        '.table-header-resize { display: inline-block; }' +
        '.table-cell { display: table-cell; }' +
        '.tooltip { position: absolute; }' +
        '</style>').appendTo($('#sandbox'));
      ObjectFactory.get().register(Tooltip, () => new SpecTooltip());
    });

    afterEach(() => {
      ObjectFactory.get().register(Tooltip, () => new Tooltip());
    });

    it('updates column model and sends resize event', () => {
      let model = helper.createModelFixture(2, 5);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as SpecTable;
      table.render();

      expect(table.columns[0].width).not.toBe(100);
      table.resizeColumn(table.columns[0], 100);
      expect(table.columns[0].width).toBe(100);

      sendQueuedAjaxCalls(null, 1000);
      let event = new RemoteEvent(table.id, 'columnResized', {
        columnId: table.columns[0].id,
        width: 100,
        showBusyIndicator: false
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it('does not send resize event when resizing is in progress', () => {
      let model = helper.createModelFixture(2, 5);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as SpecTable;
      table.render();

      table.resizeColumn(table.columns[0], 50);
      table.resizeColumn(table.columns[0], 100);

      sendQueuedAjaxCalls();

      expect(jasmine.Ajax.requests.count()).toBe(0);
    });

    it('sends resize event when resizing is finished', () => {
      let model = helper.createModelFixture(2, 5);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as SpecTable;
      table.render();

      table.resizeColumn(table.columns[0], 50);
      table.resizeColumn(table.columns[0], 100);
      table.resizeColumn(table.columns[0], 150);

      sendQueuedAjaxCalls(null, 1000);

      expect(jasmine.Ajax.requests.count()).toBe(1);
      expect(mostRecentJsonRequest().events.length).toBe(1);

      let event = new RemoteEvent(table.id, 'columnResized', {
        columnId: table.columns[0].id,
        width: 150,
        showBusyIndicator: false
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it('always updates model width, but only resizes cells of visible columns', () => {
      let model = helper.createModelFixture(5, 1);
      model.columns[0].width = 100;
      model.columns[1].width = 101;
      model.columns[1].visible = false;
      model.columns[2].width = 102;
      (model.columns[2] as ColumnModel<any>).displayable = false;
      model.columns[3].width = 103;
      model.columns[4].width = 104;
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as SpecTable;
      table.render();
      // Manually set a "large" table width, because otherwise it will depend on the window size.
      // When running with PhantomJS, the window size would be too small, causing the test to fail.
      table.$container.width(9999);
      table.revalidateLayout();

      let $headerItems = table.header.$container.children('.table-header-item:not(.filler)');
      let $rowCells = table.rows[0].$row.children('.table-cell');
      expect($headerItems.length).toBe(3);
      expect($rowCells.length).toBe(3);
      expect($headerItems.eq(0).cssMinWidth()).toBe(100);
      expect($rowCells.eq(0).cssWidth()).toBe(100);
      expect($headerItems.eq(1).cssMinWidth()).toBe(103);
      expect($rowCells.eq(1).cssWidth()).toBe(103);
      expect($headerItems.eq(2).cssMinWidth()).toBe(104);
      expect($rowCells.eq(2).cssWidth()).toBe(104);

      table.resizeColumn(table.columns[0], 200);
      expect($headerItems.eq(0).cssMinWidth()).toBe(200);
      expect($rowCells.eq(0).cssWidth()).toBe(200);
      expect($headerItems.eq(1).cssMinWidth()).toBe(103);
      expect($rowCells.eq(1).cssWidth()).toBe(103);
      expect($headerItems.eq(2).cssMinWidth()).toBe(104);
      expect($rowCells.eq(2).cssWidth()).toBe(104);

      table.resizeColumn(table.columns[1], 201); // invisible column, nothing should happen
      expect($headerItems.eq(0).cssMinWidth()).toBe(200);
      expect($rowCells.eq(0).cssWidth()).toBe(200);
      expect($headerItems.eq(1).cssMinWidth()).toBe(103);
      expect($rowCells.eq(1).cssWidth()).toBe(103);
      expect($headerItems.eq(2).cssMinWidth()).toBe(104);
      expect($rowCells.eq(2).cssWidth()).toBe(104);

      table.resizeColumn(table.columns[2], 202); // not displayable column, nothing should happen
      expect($headerItems.eq(0).cssMinWidth()).toBe(200);
      expect($rowCells.eq(0).cssWidth()).toBe(200);
      expect($headerItems.eq(1).cssMinWidth()).toBe(103);
      expect($rowCells.eq(1).cssWidth()).toBe(103);
      expect($headerItems.eq(2).cssMinWidth()).toBe(104);
      expect($rowCells.eq(2).cssWidth()).toBe(104);

      table.resizeColumn(table.columns[3], 203);
      expect($headerItems.eq(0).cssMinWidth()).toBe(200);
      expect($rowCells.eq(0).cssWidth()).toBe(200);
      expect($headerItems.eq(1).cssMinWidth()).toBe(203);
      expect($rowCells.eq(1).cssWidth()).toBe(203);
      expect($headerItems.eq(2).cssMinWidth()).toBe(104);
      expect($rowCells.eq(2).cssWidth()).toBe(104);

      table.resizeColumn(table.columns[4], 204);
      expect($headerItems.eq(0).cssMinWidth()).toBe(200);
      expect($rowCells.eq(0).cssWidth()).toBe(200);
      expect($headerItems.eq(1).cssMinWidth()).toBe(203);
      expect($rowCells.eq(1).cssWidth()).toBe(203);
      expect($headerItems.eq(2).cssMinWidth()).toBe(204);
      expect($rowCells.eq(2).cssWidth()).toBe(204);
    });

    it('moves tooltip', () => {
      const model = helper.createModelFixture(2, 1);
      model.columns[0].width = 100;
      model.columns[1].width = 100;
      const table = helper.createTable(model);
      const [column0, column1] = table.columns;

      const calcOriginAndDiffs = t => {
        const anchorBounds = graphics.offsetBounds(t.$anchor);
        const tooltipBounds = graphics.offsetBounds(t.$container);
        const origin = anchorBounds.translate(anchorBounds.width / 2, 0).point();
        const xDiff = origin.x - tooltipBounds.x;
        const yDiff = origin.y - tooltipBounds.y;
        return {origin, xDiff, yDiff};
      };

      table.render();
      const row = table.rows[0];
      const $row = row.$row;
      const $cell1 = table.$cell(column1, $row);
      table._showCellError(row, $cell1, Status.error('I am an error!!!'));

      expect(table.tooltips.length).toBe(1);

      const tooltip = table.tooltips[0];

      expect(tooltip.rendered).toBeTrue();
      expect(tooltip.$container.isVisible()).toBeTrue();

      const originAndDiffs100100 = calcOriginAndDiffs(tooltip);

      table.resizeColumn(column1, 200);

      expect(tooltip.rendered).toBeTrue();
      expect(tooltip.$container.isVisible()).toBeTrue();

      const originAndDiffs100200 = calcOriginAndDiffs(tooltip);
      expect(originAndDiffs100200.origin).not.toEqual(originAndDiffs100100.origin);
      expect(originAndDiffs100200.xDiff).toBe(originAndDiffs100100.xDiff);
      expect(originAndDiffs100200.yDiff).toBe(originAndDiffs100100.yDiff);

      table.resizeColumn(column0, 50);

      expect(tooltip.rendered).toBeTrue();
      expect(tooltip.$container.isVisible()).toBeTrue();

      const originAndDiffs50200 = calcOriginAndDiffs(tooltip);
      expect(originAndDiffs50200.origin).not.toEqual(originAndDiffs100200.origin);
      expect(originAndDiffs50200.origin).toEqual(originAndDiffs100100.origin);
      expect(originAndDiffs50200.xDiff).toBe(originAndDiffs100100.xDiff);
      expect(originAndDiffs50200.yDiff).toBe(originAndDiffs100100.yDiff);
    });

    class SpecTooltip extends Tooltip {
      override position() {
        const origin = this._getOrigin();
        const offset = this._getOffset(origin);
        const {x, y} = origin.point().add(offset);
        this.$container
          .cssLeft(x)
          .cssTop(y);
      }
    }
  });

  describe('autoResizeColumns', () => {

    it('distributes the table columns using initialWidth as weight', () => {
      let model = helper.createModelFixture(2);
      model.columns[0].initialWidth = 100;
      model.columns[1].initialWidth = 200;
      let table = helper.createTable(model);
      table.render();
      table.$data.width(450);

      table.setProperty('autoResizeColumns', true);

      // Triggers TableLayout._layoutColumns()
      table.revalidateLayout();

      expect(table.columns[0].width).toBe(150);
      expect(table.columns[1].width).toBe(300);
    });

    it('excludes columns with fixed width', () => {
      let model = helper.createModelFixture(2);
      model.columns[0].initialWidth = 100;
      model.columns[0].width = model.columns[0].initialWidth;
      model.columns[0].fixedWidth = true;
      model.columns[1].initialWidth = 200;
      model.columns[1].width = model.columns[1].initialWidth;
      let table = helper.createTable(model);
      table.render();
      table.$data.width(450);

      table.setProperty('autoResizeColumns', true);

      // Triggers TableLayout._layoutColumns()
      table.revalidateLayout();

      expect(table.columns[0].width).toBe(100);
      expect(table.columns[1].width).toBe(350);
    });

    it('does not make the column smaller than the initial size', () => {
      let model = helper.createModelFixture(2);
      model.columns[0].initialWidth = 100;
      model.columns[1].initialWidth = 200;
      let table = helper.createTable(model);
      table.render();
      table.$data.width(240);

      table.setProperty('autoResizeColumns', true);

      // Triggers TableLayout._layoutColumns()
      table.revalidateLayout();

      expect(table.columns[0].width).toBe(100); // would be 80, but does not get
      // smaller than initialSize
      expect(table.columns[1].width).toBe(200); // would be 160, but does not
      // get smaller than initialSize
    });

    it('does not make the column smaller than a minimum size', () => {
      let model = helper.createModelFixture(2);
      model.columns[0].initialWidth = 1000;
      model.columns[1].initialWidth = Column.DEFAULT_MIN_WIDTH - 10;
      let table = helper.createTable(model);
      table.render();
      table.$data.width(450);

      table.setProperty('autoResizeColumns', true);

      // Triggers TableLayout._layoutColumns()
      table.revalidateLayout();

      expect(table.columns[0].width).toBe(1000);
      expect(table.columns[1].width).toBe(Column.DEFAULT_MIN_WIDTH);
    });

  });

  describe('sort', () => {
    let model, table, adapter, column0, column1, column2;
    let $colHeaders, $header0, $header1, $header2;

    function prepareTable() {
      model = helper.createModelFixture(3, 3);
      table = helper.createTable(model);
      column0 = table.columns[0];
      column1 = table.columns[1];
      column2 = table.columns[2];
    }

    function prepareTableWithAdapter() {
      model = helper.createModelFixture(3, 3);
      adapter = helper.createTableAdapter(model);
      table = adapter.createWidget(model, session.desktop);
      column0 = table.columns[0];
      column1 = table.columns[1];
      column2 = table.columns[2];
    }

    function render(table) {
      table.render();
      $colHeaders = table.header.$container.find('.table-header-item');
      $header0 = $colHeaders.eq(0);
      $header1 = $colHeaders.eq(1);
      $header2 = $colHeaders.eq(2);
    }

    it('updates column model', () => {
      prepareTable();
      render(table);
      table.sort(column0, 'desc');

      expect(table.columns[0].sortActive).toBe(true);
      expect(table.columns[0].sortAscending).toBe(false);
      expect(table.columns[0].sortIndex).toBe(0);
    });

    it('works if rows are passed in model', () => {
      let sortColumnIndex = 0;
      model = helper.createModelFixture(3, 3);
      model.columns[sortColumnIndex].sortActive = true;
      model.columns[sortColumnIndex].sortIndex = 0;
      model.columns[sortColumnIndex].sortAscending = false;
      table = helper.createTable(model);
      expect(table.rows[0].cells[sortColumnIndex].value).toBe('cell2_0');
      expect(table.rows[1].cells[sortColumnIndex].value).toBe('cell1_0');
      expect(table.rows[2].cells[sortColumnIndex].value).toBe('cell0_0');
    });

    describe('model update', () => {
      it('sets sortAscending according to direction param', () => {
        prepareTable();
        render(table);

        table.sort(column0, 'desc');
        expect(table.columns[0].sortAscending).toBe(false);

        table.sort(column0, 'asc');
        expect(table.columns[0].sortAscending).toBe(true);
      });

      it('resets properties on other columns', () => {
        prepareTable();
        render(table);

        // reset logic is only applied on columns used as sort-column, that's
        // why
        // we must set the sortActive flag here.
        table.columns[1].sortActive = true;

        table.sort(column0, 'desc');
        expect(table.columns[0].sortActive).toBe(true);
        expect(table.columns[0].sortAscending).toBe(false);
        expect(table.columns[0].sortIndex).toBe(0);
        expect(table.columns[1].sortActive).toBe(false);
        expect(table.columns[1].sortIndex).toBe(-1);

        table.sort(column1, 'desc');
        expect(table.columns[0].sortActive).toBe(false);
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortActive).toBe(true);
        expect(table.columns[1].sortAscending).toBe(false);
        expect(table.columns[1].sortIndex).toBe(0);
      });

      it('sets sortIndex', () => {
        prepareTable();
        render(table);

        // reset logic is only applied on columns used as sort-column, that's
        // why
        // we must set the sortActive flag here.
        table.columns[1].sortActive = true;

        table.sort(column0, 'desc');
        expect(table.columns[0].sortIndex).toBe(0);
        expect(table.columns[1].sortIndex).toBe(-1);

        table.sort(column1, 'desc', true);
        expect(table.columns[0].sortIndex).toBe(0);
        expect(table.columns[1].sortIndex).toBe(1);

        table.sort(column1, 'desc');
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(0);
      });

      it('does not remove sortIndex for columns always included at begin', () => {
        prepareTable();
        column1.initialAlwaysIncludeSortAtBegin = true;
        column1.sortActive = true;
        column1.sortIndex = 1;
        column2.initialAlwaysIncludeSortAtBegin = true;
        column2.sortActive = true;
        column2.sortIndex = 0;
        table.updateColumnStructure(table.columns); // (re)initialize columns,
        // have been initialized
        // already during init
        render(table);

        table.sort(table.columns[0], 'desc');
        expect(table.columns[0].sortIndex).toBe(2);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);

        table.sort(table.columns[1], 'desc');
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);

        table.sort(table.columns[1], 'desc', true, true);
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);

        table.sort(table.columns[2], 'desc', false, true);
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);
      });

      it('does not remove sortIndex for columns always included at end', () => {
        prepareTable();
        column1.initialAlwaysIncludeSortAtEnd = true;
        column1.sortActive = true;
        column1.sortIndex = 1;
        column2.initialAlwaysIncludeSortAtEnd = true;
        column2.sortActive = true;
        column2.sortIndex = 0;
        table.updateColumnStructure(table.columns); // (re)initialize columns,
        // have been initialized
        // already during init
        render(table);

        table.sort(table.columns[0], 'desc');
        expect(table.columns[0].sortIndex).toBe(0);
        expect(table.columns[1].sortIndex).toBe(2);
        expect(table.columns[2].sortIndex).toBe(1);

        table.sort(table.columns[1], 'desc');
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);

        table.sort(table.columns[2], 'desc', true, true);
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);

        table.sort(table.columns[1], 'desc', false, true);
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);
      });

      it('does not remove sortIndex for columns always included at begin and end (combination)', () => {
        prepareTable();
        column1.initialAlwaysIncludeSortAtEnd = true;
        column1.sortActive = true;
        column1.sortIndex = 1;
        column2.initialAlwaysIncludeSortAtBegin = true;
        column2.sortActive = true;
        column2.sortIndex = 0;
        table.updateColumnStructure(table.columns); // (re)initialize columns,
        // have been initialized
        // already during init
        render(table);

        table.sort(table.columns[0], 'desc');
        expect(table.columns[0].sortIndex).toBe(1);
        expect(table.columns[1].sortIndex).toBe(2);
        expect(table.columns[2].sortIndex).toBe(0);

        table.sort(table.columns[1], 'desc');
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);

        table.sort(table.columns[2], 'desc', true, true);
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);

        table.sort(table.columns[1], 'desc', false, true);
        expect(table.columns[0].sortIndex).toBe(-1);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(0);
      });

      it('removes column from sort columns', () => {
        prepareTable();
        render(table);

        // reset logic is only applied on columns used as sort-column, that's
        // why
        // we must set the sortActive flag here.
        table.columns[1].sortActive = true;

        table.sort(column0, 'desc');
        expect(table.columns[0].sortIndex).toBe(0);
        expect(table.columns[1].sortIndex).toBe(-1);

        table.sort(column1, 'desc', true);
        expect(table.columns[0].sortIndex).toBe(0);
        expect(table.columns[1].sortIndex).toBe(1);

        table.sort(column2, 'desc', true);
        expect(table.columns[0].sortIndex).toBe(0);
        expect(table.columns[1].sortIndex).toBe(1);
        expect(table.columns[2].sortIndex).toBe(2);

        // Remove second column -> sortIndex of 3rd column gets adjusted
        table.sort(column1, 'desc', false, true);
        expect(table.columns[0].sortIndex).toBe(0);
        expect(table.columns[1].sortIndex).toBe(-1);
        expect(table.columns[2].sortIndex).toBe(1);
      });
    });

    it('sends sort without sortingRequested event when client side sorting is possible', () => {
      prepareTableWithAdapter();
      render(table);
      // Make sure sorting is not executed because it does not work with
      // phantomJS
      spyOn(Device.get(), 'supportsInternationalization').and.returnValue(true);
      spyOn(table, '_sort').and.returnValue(true);

      table.sort(column0, 'desc');
      sendQueuedAjaxCalls();

      let event = new RemoteEvent(table.id, 'sort', {
        columnId: table.columns[0].id,
        sortAscending: false
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it('sends sort event with sortingRequested if client side sorting is not possible', () => {
      prepareTableWithAdapter();
      render(table);
      spyOn(Device.get(), 'supportsInternationalization').and.returnValue(false);

      table.sort(column0, 'desc');
      sendQueuedAjaxCalls();

      let event = new RemoteEvent(table.id, 'sort', {
        columnId: table.columns[0].id,
        sortAscending: false,
        sortingRequested: true
      });
      expect(mostRecentJsonRequest()).toContainEvents(event);
    });

    it('sorts the data', () => {
      prepareTable();
      render(table);
      spyOn(table, '_sort');

      table.sort(column0, 'desc');

      expect(table._sort).toHaveBeenCalled();
    });

    it('regroups the data if group by column is active', () => {
      if (!Device.get().supportsInternationalization()) {
        return;
      }

      prepareTable();
      render(table);

      // Make sure sorting is not executed because it does not work with
      // phantomJS
      spyOn(Device.get(), 'supportsInternationalization').and.returnValue(true);
      spyOn(table, '_sortImpl').and.returnValue(true);
      spyOn(table, '_group');

      column0.grouped = true;
      table.sort(column0, 'desc');

      expect(table._group).toHaveBeenCalled();
    });

    it('restores selection after sorting', () => {
      let model = helper.createModelSingleColumnByValues([5, 2, 1, 3, 4], 'NumberColumn'),
        table = helper.createTable(model),
        column0 = table.columns[0],
        rows = table.rows;
      table.render();

      let $rows = table.$rows();
      let $row0 = $rows.eq(0);
      let $row1 = $rows.eq(1);
      let $row2 = $rows.eq(2);
      let $row3 = $rows.eq(3);
      let $row4 = $rows.eq(4);

      expect([$row0, $row1, $row2, $row3, $row4]).not.anyToHaveClass('selected');

      table.selectRows([rows[1], rows[2], rows[3]]);

      expect([$row0, $row4]).not.anyToHaveClass('selected');
      expect([$row0, $row2, $row3, $row4]).not.anyToHaveClass('select-top');
      expect([$row0, $row1, $row3, $row4]).not.anyToHaveClass('select-middle');
      expect([$row0, $row1, $row2, $row4]).not.anyToHaveClass('select-bottom');
      expect([$row0, $row1, $row2, $row3, $row4]).not.anyToHaveClass('select-single');

      // sort table (descending)
      table.sort(column0, 'desc');

      // after sorting
      $rows = table.$rows();
      $row0 = $rows.eq(0);
      $row1 = $rows.eq(1);
      $row2 = $rows.eq(2);
      $row3 = $rows.eq(3);
      $row4 = $rows.eq(4);
      expect([$row2, $row3, $row4]).allToHaveClass('selected');
      expect($row2).toHaveClass('select-top');
      expect($row3).toHaveClass('select-middle');
      expect($row4).toHaveClass('select-bottom');

      expect([$row0, $row4]).not.allToHaveClass('selected');
      expect([$row0, $row1, $row3, $row4]).not.anyToHaveClass('select-top');
      expect([$row0, $row1, $row2, $row4]).not.anyToHaveClass('select-middle');
      expect([$row0, $row1, $row2, $row3]).not.anyToHaveClass('select-bottom');
      expect([$row0, $row1, $row2, $row3, $row4]).not.anyToHaveClass('select-single');
    });

    describe('sorting', () => {

      it('sorts text columns considering locale (if browser supports it)', () => {
        if (!Device.get().supportsInternationalization()) {
          return;
        }

        let model = helper.createModelSingleColumnByTexts(['Österreich', 'Italien', 'Zypern']);
        let table = helper.createTable(model);
        column0 = table.columns[0];
        table.render();

        table.sort(column0, 'desc');
        helper.assertTextsInCells(table.rows, 0, ['Zypern', 'Österreich', 'Italien']);

        table.sort(column0, 'asc');
        helper.assertTextsInCells(table.rows, 0, ['Italien', 'Österreich', 'Zypern']);

        // In order to change Collator at runtime, we must reset the "static"
        // property
        // since it is set only once
        session.locale = new LocaleSpecHelper().createLocale('sv');
        helper.resetIntlCollator();

        table.sort(column0, 'desc');
        helper.assertTextsInCells(table.rows, 0, ['Österreich', 'Zypern', 'Italien']);

        table.sort(column0, 'asc');
        helper.assertTextsInCells(table.rows, 0, ['Italien', 'Zypern', 'Österreich']);
      });

      it('sorts number columns', () => {
        let model = helper.createModelSingleColumnByValues([100, 90, 300], 'NumberColumn');
        let table = helper.createTable(model);
        column0 = table.columns[0];
        table.render();

        table.sort(column0, 'desc');
        helper.assertValuesInCells(table.rows, 0, [300, 100, 90]);

        table.sort(column0, 'asc');
        helper.assertValuesInCells(table.rows, 0, [90, 100, 300]);
      });

      it('sorts date columns', () => {
        let model = helper.createModelSingleColumnByValues([new Date('2012-08-10'), new Date('2014-03-01'), new Date('1999-01-10')], 'DateColumn');
        let table = helper.createTable(model);
        column0 = table.columns[0];
        table.render();

        table.sort(column0, 'desc');
        helper.assertDatesInCells(table.rows, 0, [new Date('2014-03-01'), new Date('2012-08-10'), new Date('1999-01-10')]);

        table.sort(column0, 'asc');
        helper.assertDatesInCells(table.rows, 0, [new Date('1999-01-10'), new Date('2012-08-10'), new Date('2014-03-01')]);
      });

      it('sorts columns with sortcode', () => {
        let model = helper.createModelSingleColumnByValues([0, 1, 2, 3, 4], 'NumberColumn');
        let table = helper.createTable(model);
        column0 = table.columns[0];

        let sortCodes = [13, 0, 42, 7, null];
        table.rows.forEach((row, index) => {
          column0.cell(row).sortCode = sortCodes[index];
        });

        table.render();

        table.sort(column0, 'desc');
        helper.assertValuesInCells(table.rows, 0, [2, 0, 3, 1, 4]);

        table.sort(column0, 'asc');
        helper.assertValuesInCells(table.rows, 0, [4, 1, 3, 0, 2]);
      });

      it('uses non sort columns as fallback', () => {
        if (!Device.get().supportsInternationalization()) {
          return;
        }

        let model = helper.createModelFixture(2, 4);
        let table = helper.createTable(model);

        column0 = table.columns[0];
        column1 = table.columns[1];

        column0.setCellValue(model.rows[0], 'zzz');
        column1.setCellValue(model.rows[0], 'same');
        column0.setCellValue(model.rows[1], 'aaa');
        column1.setCellValue(model.rows[1], 'other');
        column0.setCellValue(model.rows[2], 'ccc');
        column1.setCellValue(model.rows[2], 'other');
        column0.setCellValue(model.rows[3], 'qqq');
        column1.setCellValue(model.rows[3], 'same');

        table.render();

        expect(column0.sortAscending).toBe(true);
        table.sort(column1, 'asc');
        helper.assertValuesInCells(table.rows, 0, ['aaa', 'ccc', 'qqq', 'zzz']);
        helper.assertValuesInCells(table.rows, 1, ['other', 'other', 'same', 'same']);

        table.sort(column1, 'desc');
        helper.assertValuesInCells(table.rows, 0, ['qqq', 'zzz', 'aaa', 'ccc']);
        helper.assertValuesInCells(table.rows, 1, ['same', 'same', 'other', 'other']);

        // sortAscending of a column with sortActive = false shouldn't have any
        // effect
        column0.sortAscending = false;
        table.sort(column1, 'asc');
        helper.assertValuesInCells(table.rows, 0, ['aaa', 'ccc', 'qqq', 'zzz']);
        helper.assertValuesInCells(table.rows, 1, ['other', 'other', 'same', 'same']);
      });

    });

  });

  describe('row click', () => {

    function clickRowAndAssertSelection(table, $row) {
      JQueryTesting.triggerClick($row);

      let $selectedRows = table.$selectedRows();
      expect($selectedRows.length).toBe(1);

      let $selectedRow = $selectedRows.first();
      expect($selectedRow.innerText).toEqual($row.innerText);

      expect($selectedRow.hasClass('selected')).toBeTruthy();
      expect($selectedRow.hasClass('select-single')).toBeTruthy();
    }

    it('selects row and unselects others', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);
      table.render();

      let $selectedRows = table.$selectedRows();
      expect($selectedRows.length).toBe(0);

      let $rows = table.$rows();
      clickRowAndAssertSelection(table, $rows.eq(1));
      clickRowAndAssertSelection(table, $rows.eq(2));

      helper.selectRowsAndAssert(table, [model.rows[0], model.rows[4]] as TableRow[]);
      clickRowAndAssertSelection(table, $rows.eq(4));
    });

    it('sends selection and click events', () => {
      let model = helper.createModelFixture(2, 5);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as SpecTable;
      table.render();

      let $row = table.$rows().first();
      JQueryTesting.triggerClick($row);

      sendQueuedAjaxCalls();

      // clicked has to be after selected otherwise it is not possible to get
      // the selected row in execRowClick
      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['property', 'rowsSelected', 'rowClick']);
    });

    it('sends only click if row already is selected', () => {
      let model = helper.createModelFixture(2, 5);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as SpecTable;
      table.render();

      let $row = table.$rows().first();
      clickRowAndAssertSelection(table, $row);
      sendQueuedAjaxCalls();

      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['property', 'rowsSelected', 'rowClick']);

      // Reset internal state because there is no "sleep" in JS
      table._doubleClickSupport._lastTimestamp -= 5000; // simulate last click 5
      // seconds ago

      jasmine.Ajax.requests.reset();
      clickRowAndAssertSelection(table, $row);
      sendQueuedAjaxCalls();

      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['rowClick']);
    });

    it('sends selection, checked and click events if table is checkable and checkbox has been clicked', () => {
      let model = helper.createModelFixture(2, 5);
      model.checkable = true;
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as SpecTable;
      table.render();

      let $checkbox = table.$rows().first().find('.check-box').first();
      JQueryTesting.triggerClick($checkbox);

      sendQueuedAjaxCalls();

      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['property', 'rowsSelected', 'rowsChecked', 'rowClick']);
    });

  });

  describe('right click on row', () => {

    afterEach(() => {
      // Close context menus
      removePopups(session);
    });

    it('opens context menu', () => {
      let model = helper.createModelFixture(2, 2);
      let table = helper.createTable(model);
      table.selectedRows = [table.rows[0]];
      table.render();

      let menuModel = helper.createMenuModel('menu'),
        menu = helper.menuHelper.createMenu(menuModel);
      table.menus = [menu];
      let $row0 = table.$data.children('.table-row').eq(0);
      JQueryTesting.triggerContextMenu($row0);

      sendQueuedAjaxCalls();

      let $menu = helper.getDisplayingContextMenu(table);
      expect($menu.length).toBeTruthy();
    });

    it('context menu only shows items without header type also if there is a type singleSelection', () => {
      let model = helper.createModelFixture(2, 2);
      let table = helper.createTable(model);
      table.selectedRows = [table.rows[0]];
      table.render();

      let menuModel1 = helper.createMenuModel('menu'),
        menu1 = helper.menuHelper.createMenu(menuModel1),
        menuModel2 = helper.createMenuModelWithSingleAndHeader('menu'),
        menu2 = helper.menuHelper.createMenu(menuModel2);

      table.menus = [menu1, menu2];
      let $row0 = table.$data.children('.table-row').eq(0);
      JQueryTesting.triggerContextMenu($row0);

      sendQueuedAjaxCalls();

      let $menu = helper.getDisplayingContextMenu(table);
      expect($menu.find('.menu-item').length).toBe(1);
      expect($menu.find('.menu-item').eq(0).isVisible()).toBe(true);
      expect(menu2.$container).toBeNull();
      expect(menu1.$container).not.toBeNull();
    });

    it('context menu only shows visible menus', () => {
      let model = helper.createModelFixture(2, 2);
      let table = helper.createTable(model);
      table.selectedRows = [table.rows[0]];
      table.render();

      let menuModel1 = helper.createMenuModel('menu'),
        menu1 = helper.menuHelper.createMenu(menuModel1),
        menuModel2 = helper.createMenuModel('menu'),
        menu2 = helper.menuHelper.createMenu(menuModel2);
      menu2.setVisible(false);

      table.menus = [menu1, menu2];
      let $row0 = table.$data.children('.table-row').eq(0);
      JQueryTesting.triggerContextMenu($row0);

      sendQueuedAjaxCalls();

      let $menu = helper.getDisplayingContextMenu(table);
      expect($menu.find('.menu-item').length).toBe(1);
      expect($menu.find('.menu-item').eq(0).isVisible()).toBe(true);
    });

  });

  describe('_filterMenus', () => {
    let singleSelMenu, multiSelMenu, bothSelMenu, emptySpaceMenu, headerMenu, table;

    beforeEach(() => {
      let model = helper.createModelFixture(2, 2);
      singleSelMenu = helper.menuHelper.createMenu({
        menuTypes: [Table.MenuType.SingleSelection]
      });
      multiSelMenu = helper.menuHelper.createMenu({
        menuTypes: [Table.MenuType.MultiSelection]
      });
      emptySpaceMenu = helper.menuHelper.createMenu();
      headerMenu = helper.menuHelper.createMenu({
        menuTypes: [Table.MenuType.Header]
      });
      table = helper.createTable(model);
      table.menus = [singleSelMenu, multiSelMenu, emptySpaceMenu, headerMenu];
    });

    // context menu
    it('returns no menus for contextMenu if no row is selected', () => {
      table.selectRows([]);
      let menus = table._filterMenus(table.menus, MenuDestinations.CONTEXT_MENU);
      expect(menus).toEqual([]);
    });

    it('returns only single selection menus for contextMenu if one row is selected', () => {
      table.selectRows(table.rows[0]);
      let menus = table._filterMenus(table.menus, MenuDestinations.CONTEXT_MENU);
      expect(menus).toEqual([singleSelMenu]);
    });

    it('returns only multi selection menus for contextMenu if multiple rows are selected', () => {
      table.selectRows([table.rows[0], table.rows[1]]);
      let menus = table._filterMenus(table.menus, MenuDestinations.CONTEXT_MENU);
      expect(menus).toEqual([multiSelMenu]);
    });

    it('returns menus with single- and multi selection set for contextMenu if one or more rows are selected', () => {
      bothSelMenu = helper.menuHelper.createMenu({
        menuTypes: [Table.MenuType.SingleSelection, Table.MenuType.MultiSelection]
      });
      table.menus = [singleSelMenu, multiSelMenu, bothSelMenu];
      table.selectRows(table.rows[0]);
      let menus = table._filterMenus(table.menus, MenuDestinations.CONTEXT_MENU);
      expect(menus).toEqual([singleSelMenu, bothSelMenu]);

      table.selectRows([table.rows[0], table.rows[1]]);
      menus = table._filterMenus(table.menus, MenuDestinations.CONTEXT_MENU);
      expect(menus).toEqual([multiSelMenu, bothSelMenu]);

      table.selectRows([]);
      menus = table._filterMenus(table.menus, MenuDestinations.CONTEXT_MENU);
      expect(menus).toEqual([]);
    });

    // menuBar
    it('returns only empty space menus if no row is selected', () => {
      table.selectRows([]);
      let menus = table._filterMenus(table.menus, MenuDestinations.MENU_BAR);
      expect(menus).toEqual([emptySpaceMenu]);
    });

    it('returns empty space and single selection menus if one row is selected', () => {
      table.selectRows(table.rows[0]);
      let menus = table._filterMenus(table.menus, MenuDestinations.MENU_BAR);
      expect(menus).toEqual([singleSelMenu, emptySpaceMenu]);
    });

    it('returns empty space and multi selection menus if multiple rows are selected', () => {
      table.selectRows([table.rows[0], table.rows[1]]);
      let menus = table._filterMenus(table.menus, MenuDestinations.MENU_BAR);
      expect(menus).toEqual([multiSelMenu, emptySpaceMenu]);
    });

    it('returns menus with empty space, single- and multi selection set if one or more rows are selected', () => {
      bothSelMenu = helper.menuHelper.createMenu({
        menuTypes: [Table.MenuType.SingleSelection, Table.MenuType.MultiSelection]
      });
      table.menus = [singleSelMenu, multiSelMenu, emptySpaceMenu, bothSelMenu];
      table.selectRows(table.rows[0]);
      let menus = table._filterMenus(table.menus, MenuDestinations.MENU_BAR);
      expect(menus).toEqual([singleSelMenu, emptySpaceMenu, bothSelMenu]);

      table.selectRows([table.rows[0], table.rows[1]]);
      menus = table._filterMenus(table.menus, MenuDestinations.MENU_BAR);
      expect(menus).toEqual([multiSelMenu, emptySpaceMenu, bothSelMenu]);

      table.selectRows([]);
      menus = table._filterMenus(table.menus, MenuDestinations.MENU_BAR);
      expect(menus).toEqual([emptySpaceMenu]);
    });
  });

  describe('row menus', () => {
    let singleSelMenu: Menu;
    let multiSelMenu: Menu;
    let emptySpaceMenu: Menu;

    beforeEach(() => {
      singleSelMenu = scout.create(Menu, {
        parent: session.desktop,
        menuTypes: [Table.MenuType.SingleSelection]
      });
      multiSelMenu = scout.create(Menu, {
        parent: session.desktop,
        menuTypes: [Table.MenuType.MultiSelection]
      });
      emptySpaceMenu = scout.create(Menu, {
        parent: session.desktop
      });
    });

    it('are disabled if single selection and row is disabled', () => {
      let model = helper.createModelFixture(2, 0);
      helper.createTable($.extend(model, {
        rows: [{id: 'firstRow', enabled: false}, {}, {}], // 3 rows, first is disabled,
        selectedRows: ['firstRow'],
        menus: [singleSelMenu, emptySpaceMenu]
      }));
      expect(singleSelMenu.enabled).toBe(false);
      expect(emptySpaceMenu.enabled).toBe(true);
    });

    it('are disabled if multi selection and rows are disabled', () => {
      let model = helper.createModelFixture(2, 0);
      helper.createTable($.extend(model, {
        rows: [{id: 'firstRow', enabled: false}, {id: 'secondRow', enabled: false}, {}],
        selectedRows: ['firstRow', 'secondRow'],
        menus: [multiSelMenu, emptySpaceMenu]
      }));
      expect(multiSelMenu.enabled).toBe(false);
      expect(emptySpaceMenu.enabled).toBe(true);
    });

    it('are disabled if added dynamically and row is disabled', () => {
      let model = helper.createModelFixture(2, 0);
      let table = helper.createTable($.extend(model, {
        rows: [{id: 'firstRow', enabled: false}, {}, {}], // 3 rows, first is disabled,
        selectedRows: ['firstRow']
      }));
      expect(singleSelMenu.enabled).toBe(true);

      table.setMenus([singleSelMenu]);
      expect(singleSelMenu.enabled).toBe(false);
    });

    it('are enabled if inheritAccessibility is false even if row is disabled', () => {
      let model = helper.createModelFixture(2, 0);
      let table = helper.createTable($.extend(model, {
        rows: [{id: 'firstRow', enabled: false}, {}, {}], // 3 rows, first is disabled,
        selectedRows: ['firstRow'],
        menus: [{
          objectType: Menu,
          menuTypes: [Table.MenuType.SingleSelection],
          inheritAccessibility: false
        }]
      }));
      singleSelMenu = table.menus[0];
      expect(singleSelMenu.enabledComputed).toBe(true);

      singleSelMenu.setInheritAccessibility(true);
      expect(singleSelMenu.enabledComputed).toBe(false);
    });

    it('are disabled if multi selection but not all rows disabled', () => {
      let model = helper.createModelFixture(2, 0);
      helper.createTable($.extend(model, {
        rows: [{id: 'firstRow', enabled: false}, {id: 'secondRow', enabled: true}, {}],
        selectedRows: ['firstRow', 'secondRow'],
        menus: [multiSelMenu, emptySpaceMenu]
      }));
      expect(multiSelMenu.enabled).toBe(false);
      expect(emptySpaceMenu.enabled).toBe(true);
    });

    it('change enabled state if row changes enabled state', () => {
      let model = helper.createModelFixture(2, 0);
      let table = helper.createTable($.extend(model, {
        rows: [{id: 'firstRow', enabled: false}, {}, {}], // 3 rows, first is disabled,
        selectedRows: ['firstRow'],
        menus: [singleSelMenu, multiSelMenu, emptySpaceMenu]
      }));
      let row = table.rows[0];
      row.setEnabled(true);
      table.updateRows(row);
      expect(singleSelMenu.enabled).toBe(true);
      expect(emptySpaceMenu.enabled).toBe(true);

      row.setEnabled(false);
      table.updateRows(row);
      expect(singleSelMenu.enabled).toBe(false);
      expect(emptySpaceMenu.enabled).toBe(true);
    });

    it('adjust enabled state based on the selection', () => {
      let model = helper.createModelFixture(2, 0);
      let table = helper.createTable($.extend(model, {
        rows: [{id: 'firstRow', enabled: false}, {id: 'secondRow', enabled: false}, {}],
        menus: [singleSelMenu, multiSelMenu, emptySpaceMenu]
      }));
      expect(singleSelMenu.enabled).toBe(true);
      expect(multiSelMenu.enabled).toBe(true);
      expect(emptySpaceMenu.enabled).toBe(true);

      table.selectRow(table.rows[0]);
      expect(singleSelMenu.enabled).toBe(false);
      expect(multiSelMenu.enabled).toBe(true);
      expect(emptySpaceMenu.enabled).toBe(true);

      table.selectRows([table.rows[0], table.rows[1]]);
      expect(singleSelMenu.enabled).toBe(false);
      expect(multiSelMenu.enabled).toBe(false);
      expect(emptySpaceMenu.enabled).toBe(true);

      table.selectRow(table.rows[1]);
      expect(singleSelMenu.enabled).toBe(false);
      expect(multiSelMenu.enabled).toBe(false); // Menu is not visible so state is not adjusted because it does not matter
      expect(emptySpaceMenu.enabled).toBe(true);

      table.selectRows([]);
      expect(singleSelMenu.enabled).toBe(false); // Menu is not visible so state is not adjusted because it does not matter
      expect(multiSelMenu.enabled).toBe(false);
      expect(emptySpaceMenu.enabled).toBe(true);
    });

    it('stay disabled if menu is disabled but row enabled', () => {
      let model = helper.createModelFixture(2, 2);
      let table = helper.createTable($.extend(model, {
        rows: [{id: 'firstRow', enabled: false}, {}, {}], // 3 rows, first is disabled,
        menus: [singleSelMenu],
        selectedRows: ['firstRow']
      }));
      let row = table.rows[0];
      expect(singleSelMenu.enabled).toBe(false);

      singleSelMenu.setEnabled(false);
      expect(singleSelMenu.enabled).toBe(false);

      row.setEnabled(true);
      table.updateRows(row);
      expect(row.enabled).toBe(true);
      expect(singleSelMenu.enabled).toBe(false); // Still false
    });
  });

  describe('menu bar popup', () => {
    let menuBarMenu, singleSelMenu, singleMultiSelMenu, multiSelMenu, emptySpaceMenu, table;

    beforeEach(() => {
      let model = helper.createModelFixture(2, 2);
      table = helper.createTable(model);

      menuBarMenu = scout.create(Menu, {
        parent: table
      });
      singleSelMenu = scout.create(Menu, {
        parent: table,
        menuTypes: [Table.MenuType.SingleSelection]
      });
      singleMultiSelMenu = scout.create(Menu, {
        parent: table,
        menuTypes: [Table.MenuType.SingleSelection, Table.MenuType.MultiSelection]
      });
      multiSelMenu = scout.create(Menu, {
        parent: table,
        menuTypes: [Table.MenuType.MultiSelection]
      });
      emptySpaceMenu = scout.create(Menu, {
        parent: table
      });

      menuBarMenu.setChildActions([singleSelMenu, singleMultiSelMenu, multiSelMenu, emptySpaceMenu]);
      table.setMenus([menuBarMenu]);
    });

    it('shows no menus if no row is selected', () => {
      table.render();
      table.selectRows([]);
      let menuBarMenu = table.menuBar.orderedMenuItems.all[0];
      menuBarMenu.doAction();

      expect(scout.widget(menuBarMenu.popup.$menuItems()[0]).original()).toBe(emptySpaceMenu);
      expect(menuBarMenu.popup.$menuItems()[1]).toBe(undefined);
    });

    it('shows single selection and empty space menus if single row is selected', () => {
      table.render();
      table.selectRows([table.rows[0]]);
      let menuBarMenu = table.menuBar.orderedMenuItems.all[0];
      menuBarMenu.doAction();

      expect(scout.widget(menuBarMenu.popup.$menuItems()[0]).original()).toBe(singleSelMenu);
      expect(scout.widget(menuBarMenu.popup.$menuItems()[1]).original()).toBe(singleMultiSelMenu);
      expect(scout.widget(menuBarMenu.popup.$menuItems()[2]).original()).toBe(emptySpaceMenu);
      expect(menuBarMenu.popup.$menuItems()[3]).toBe(undefined);
    });

    it('shows multi selection and empty space menus if multiple rows are selected', () => {
      table.render();
      table.selectRows([table.rows[0], table.rows[1]]);
      let menuBarMenu = table.menuBar.orderedMenuItems.all[0];
      menuBarMenu.doAction();

      expect(scout.widget(menuBarMenu.popup.$menuItems()[0]).original()).toBe(singleMultiSelMenu);
      expect(scout.widget(menuBarMenu.popup.$menuItems()[1]).original()).toBe(multiSelMenu);
      expect(scout.widget(menuBarMenu.popup.$menuItems()[2]).original()).toBe(emptySpaceMenu);
      expect(menuBarMenu.popup.$menuItems()[3]).toBe(undefined);
    });

  });

  describe('setMenus', () => {

    it('updates the menubar with the relevant menus', () => {
      let table = helper.createTable(helper.createModelFixture(2, 2));
      let menus = [
        scout.create(Menu, {
          parent: table
        }),
        scout.create(Menu, {
          parent: table
        })
      ];
      expect(menus[0].parent).toBe(table);

      table.setMenus(menus);
      expect(menus[0]).toBe(table.menuBar.menuItems[0]);
      expect(menus[1]).toBe(table.menuBar.menuItems[1]);
      expect(menus[0].parent).toBe(table.menuBar.menuboxLeft);
      expect(menus[1].parent).toBe(table.menuBar.menuboxLeft);

      // Set the same menus again, expect the menus to still have the menu bar as parent.
      // Because the menus are actually managed by the table, setting new menus will change the parent.
      // But the parent should actually point to the menu box because the menus are used in that context.
      // The concrete use case: Find a clone for a certain menu. Finding the clone is done by finding the menu bar and visiting its children
      table.setMenus(menus);
      expect(menus[0].parent).toBe(table.menuBar.menuboxLeft);
      expect(menus[1].parent).toBe(table.menuBar.menuboxLeft);

      // Create a new menu which is not part of the menu bar -> the menu items of the menu bar are still the same
      menus = menus.concat([scout.create(Menu, {
        parent: table,
        menuTypes: [Table.MenuType.Header]
      })]);
      table.setMenus(menus);
      expect(menus[0].parent).toBe(table.menuBar.menuboxLeft);
      expect(menus[1].parent).toBe(table.menuBar.menuboxLeft);
      expect(menus[2].parent).toBe(table);
    });
  });

  describe('row mouse down / move / up', () => {

    it('selects multiple rows', () => {
      let model = helper.createModelFixture(2, 5);
      let table = helper.createTable(model);
      table.render();

      let $rows = table.$data.children('.table-row');
      let $row0 = $rows.eq(0);
      let $row1 = $rows.eq(1);
      let $row2 = $rows.eq(2);
      let $row3 = $rows.eq(3);
      let $row4 = $rows.eq(4);

      expect([$row0, $row1, $row2, $row3, $row4]).not.anyToHaveClass('selected');

      JQueryTesting.triggerMouseDown($row0);
      $row1.trigger('mouseover');
      $row2.trigger('mouseover');
      JQueryTesting.triggerMouseUp($row2);

      expect([$row0, $row1, $row2]).allToHaveClass('selected');
      expect($row0).toHaveClass('select-top');
      expect($row1).toHaveClass('select-middle');
      expect($row2).toHaveClass('select-bottom');

      expect([$row3, $row4]).not.allToHaveClass('selected');
      expect([$row1, $row2, $row3, $row4]).not.anyToHaveClass('select-top');
      expect([$row0, $row2, $row3, $row4]).not.anyToHaveClass('select-middle');
      expect([$row0, $row1, $row3, $row4]).not.anyToHaveClass('select-bottom');
      expect([$row0, $row1, $row2, $row3, $row4]).not.anyToHaveClass('select-single');
    });

    it('only sends selection event, no click', () => {
      let model = helper.createModelFixture(2, 5);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as SpecTable;
      table.render();

      let $rows = table.$data.children('.table-row');
      let $row0 = $rows.eq(0);
      let $row1 = $rows.eq(1);
      let $row2 = $rows.eq(2);

      expect($rows).not.toHaveClass('selected');

      JQueryTesting.triggerMouseDown($row0);
      $row1.trigger('mouseover');
      $row2.trigger('mouseover');
      JQueryTesting.triggerMouseUp($row2);

      sendQueuedAjaxCalls();

      let requestData = mostRecentJsonRequest();
      // first selection event for first row, second selection event for
      // remaining rows (including first row)
      expect(requestData).toContainEventTypesExactly(['property', 'rowsSelected']);

      let event = [new RemoteEvent(table.id, 'rowsSelected', {
        rowIds: [model.rows[0].id, model.rows[1].id, model.rows[2].id]
      })];
      expect(requestData).toContainEvents(event);
    });

    it('only send one event for mousedown and immediate mouseup on the same row', () => {
      let model = helper.createModelFixture(2, 5);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as SpecTable;
      table.render();

      let $rows = table.$data.children('.table-row');
      let $row0 = $rows.eq(0);

      expect($rows).not.toHaveClass('selected');

      JQueryTesting.triggerMouseDown($row0);
      JQueryTesting.triggerMouseUp($row0);

      sendQueuedAjaxCalls();

      let requestData = mostRecentJsonRequest();
      // exactly only one selection event for first row
      expect(requestData).toContainEventTypesExactly(['property', 'rowsSelected', 'rowClick']);

      let event = [new RemoteEvent(table.id, 'rowsSelected', {
        rowIds: [model.rows[0].id]
      })];
      expect(requestData).toContainEvents(event);
    });

    it('only selects first row if mouse move selection or multi selection is disabled', () => {
      let model = helper.createModelFixture(2, 4);
      let adapter = helper.createTableAdapter(model);
      let table = adapter.createWidget(model, session.desktop) as SpecTable;
      table.selectionHandler.mouseMoveSelectionEnabled = false;
      verifyMouseMoveSelectionIsDisabled(model, table, false);

      model = helper.createModelFixture(2, 4);
      model.multiSelect = false;
      adapter = helper.createTableAdapter(model);
      table = adapter.createWidget(model, session.desktop) as SpecTable;
      verifyMouseMoveSelectionIsDisabled(model, table, true);
    });

    it('can delete all rows during mouse down event', () => {
      let model = helper.createModelFixture(2, 4);
      let table = helper.createTable(model);
      table.render();
      table.on('rowsSelected', event => table.deleteAllRows());
      expect(() => {
        JQueryTesting.triggerMouseDown(table.rows[0].$row);
      }).not.toThrow();
    });

    function verifyMouseMoveSelectionIsDisabled(model, table, selectionMovable) {
      table.render();

      let $rows = table.$data.children('.table-row');
      let $row0 = $rows.eq(0);
      let $row1 = $rows.eq(1);
      let $row2 = $rows.eq(2);

      expect($rows).not.toHaveClass('selected');

      JQueryTesting.triggerMouseDown($row0);
      $row1.trigger('mouseover');
      $row2.trigger('mouseover');
      JQueryTesting.triggerMouseUp($row2);

      let expectedSelectedRowIndex = (selectionMovable ? 2 : 0);
      for (let i = 0; i < $rows.length; i++) {
        if (i === expectedSelectedRowIndex) {
          expect($rows.eq(i)).toHaveClass('selected');
        } else {
          expect($rows.eq(i)).not.toHaveClass('selected');
        }
      }

      sendQueuedAjaxCalls();

      let requestData = mostRecentJsonRequest();
      let event = new RemoteEvent(table.id, 'rowsSelected', {
        rowIds: [model.rows[expectedSelectedRowIndex].id]
      });
      expect(requestData).toContainEvents(event);
    }

  });

  describe('moveColumn', () => {
    let model, table;

    beforeEach(() => {
      model = helper.createModelFixture(3, 2);
      table = helper.createTable(model);
    });

    it('moves column from oldPos to newPos', () => {
      table.render();

      let $colHeaders = table.header.$container.find('.table-header-item');
      let $header0 = $colHeaders.eq(0);
      let $header1 = $colHeaders.eq(1);
      let $header2 = $colHeaders.eq(2);

      expect(table.columns.indexOf($header0.data('column'))).toBe(0);
      expect(table.columns.indexOf($header1.data('column'))).toBe(1);
      expect(table.columns.indexOf($header2.data('column'))).toBe(2);

      table.moveColumn($header0.data('column'), 0, 2);

      expect(table.columns.indexOf($header1.data('column'))).toBe(0);
      expect(table.columns.indexOf($header2.data('column'))).toBe(1);
      expect(table.columns.indexOf($header0.data('column'))).toBe(2);

      table.moveColumn($header2.data('column'), 1, 0);

      expect(table.columns.indexOf($header2.data('column'))).toBe(0);
      expect(table.columns.indexOf($header1.data('column'))).toBe(1);
      expect(table.columns.indexOf($header0.data('column'))).toBe(2);
    });

    it('considers view range (does not fail if not all rows are rendered)', () => {
      table.viewRangeSize = 1;
      table.render();

      let $rows = table.$rows();
      expect(table.viewRangeRendered).toEqual(new Range(0, 1));
      expect(table.$rows().length).toBe(1);
      expect(table.rows.length).toBe(2);
      let $cells0 = table.$cellsForRow($rows.eq(0));
      expect($cells0.eq(0).text()).toBe('0_0');
      expect($cells0.eq(1).text()).toBe('0_1');
      expect($cells0.eq(2).text()).toBe('0_2');

      table.moveColumn(table.columns[0], 0, 2);
      $rows = table.$rows();
      expect(table.viewRangeRendered).toEqual(new Range(0, 1));
      expect($rows.length).toBe(1);
      expect(table.rows.length).toBe(2);
      $cells0 = table.$cellsForRow($rows.eq(0));
      expect($cells0.eq(0).text()).toBe('0_1');
      expect($cells0.eq(1).text()).toBe('0_2');
      expect($cells0.eq(2).text()).toBe('0_0');
    });

  });

  describe('updateRowOrder', () => {
    let model, table, row0, row1, row2;

    beforeEach(() => {
      model = helper.createModelFixture(2, 3);
      table = helper.createTable(model);
      row0 = model.rows[0];
      row1 = model.rows[1];
      row2 = model.rows[2];
    });

    it('correct DOM order for newly inserted rows', () => {
      table.render();
      expect(table.rows.length).toBe(3);
      let newRows = [helper.createModelRow(null, helper.createModelCells(2)), helper.createModelRow(null, helper.createModelCells(2))];

      // Insert new rows and switch rows 0 and 1
      table.insertRows(newRows);
      let orderedRows = [table.rows[1], table.rows[0], table.rows[3], table.rows[4], table.rows[2]];
      table.updateRowOrder(orderedRows);

      // Check if rows were inserted
      expect(table.rows.length).toBe(5);

      // Check if order in the DOM is correct
      // Note: in a previous version of this test we checked if an animation was playing for certain DOM nodes,
      // but we must disable jQuery animations completely during test execution, otherwise test will fail, since
      // the complete/done function is scheduled and executed to a time when the test that started the animation
      // is already finished. So this will lead to unpredictable failures.
      let uiOrderedRows = [],
        $row;
      table.$rows().each(function() {
        $row = $(this);
        uiOrderedRows.push($row.data('row'));
      });
      expect(orderedRows).toEqual(uiOrderedRows);
    });

  });

  describe('initColumns', () => {

    it('table is available in _init', () => {
      scout.create(Table, {
        parent: session.desktop,
        columns: [{
          objectType: TestBeanColumn
        }]
      });
      // assertions are done in the TestBeanColumn
    });

    it('sets the column indices if not already set', () => {
      let table = scout.create(Table, {
        parent: session.desktop,
        columns: [{
          objectType: Column
        }, {
          objectType: NumberColumn
        }, {
          objectType: NumberColumn
        }]
      });
      expect(table.columns[0].index).toBe(0);
      expect(table.columns[1].index).toBe(1);
      expect(table.columns[2].index).toBe(2);
    });

    it('does not set the column indices if already set', () => {
      let table = scout.create(Table, {
        parent: session.desktop,
        columns: [{
          objectType: Column,
          index: 2
        }, {
          objectType: NumberColumn,
          index: 0
        }, {
          objectType: NumberColumn,
          index: 1
        }]
      });
      expect(table.columns[0].index).toBe(2);
      expect(table.columns[1].index).toBe(0);
      expect(table.columns[2].index).toBe(1);
    });
  });

  describe('updateColumnStructure', () => {
    let model, table, column0, column1, column2;

    beforeEach(() => {
      model = helper.createModelFixture(3, 2);
      table = helper.createTable(model);
      column0 = table.columns[0];
      column1 = table.columns[1];
      column2 = table.columns[2];
    });

    it('resets the model columns', () => {
      table.updateColumnStructure([column2, column1]);

      expect(table.columns.length).toBe(2);
      expect(table.columns[0].id).toBe(column2.id);
      expect(table.columns[1].id).toBe(column1.id);
    });

    it('redraws the header to reflect header cell changes (text)', () => {
      table.render();

      let $colHeaders = table.header.findHeaderItems();
      expect($colHeaders.eq(0).text()).toBe(column0.text);
      expect($colHeaders.eq(1).text()).toBe(column1.text);
      expect($colHeaders.eq(2).text()).toBe(column2.text);

      column0.text = 'newColText0';
      column1.text = 'newColText1';
      table.updateColumnStructure([column0, column1, column2]);

      // Check column header text
      $colHeaders = table.header.findHeaderItems();
      expect($colHeaders.eq(0).text()).toBe(column0.text);
      expect($colHeaders.eq(1).text()).toBe(column1.text);
      expect($colHeaders.eq(2).text()).toBe(column2.text);
    });

    it('updates width of empty data', () => {
      table.deleteAllRows();
      table.render();
      let rowWidth = table.rowWidth;
      expect(table.$emptyData.width()).toBe(rowWidth);

      table.updateColumnStructure([column0, column1]);
      expect(table.rowWidth < rowWidth).toBe(true); // row width should be smaller now
      expect(table.$emptyData.width()).toBe(table.rowWidth); // $empty data should be adjusted accordingly
    });
  });

  describe('updateColumnOrder', () => {
    let model, table, column0, column1, column2;

    beforeEach(() => {
      model = helper.createModelFixture(3, 2);
      table = helper.createTable(model);
      column0 = table.columns[0];
      column1 = table.columns[1];
      column2 = table.columns[2];
    });

    it('reorders the model columns', () => {
      table.updateColumnOrder([column2, column0, column1]);
      expect(table.columns.length).toBe(3);
      expect(table.columns[0]).toBe(column2);
      expect(table.columns[1]).toBe(column0);
      expect(table.columns[2]).toBe(column1);
    });

    it('reorders the model columns by using only their id', () => {
      table.updateColumnOrder([{id: column2.id}, {id: column0.id}, {id: column1.id}]);
      expect(table.columns.length).toBe(3);
      expect(table.columns[0]).toBe(column2);
      expect(table.columns[1]).toBe(column0);
      expect(table.columns[2]).toBe(column1);
    });

    it('reorders the html nodes', () => {
      table.render();

      let $colHeaders = table.header.findHeaderItems();
      expect($colHeaders.length).toBe(3);
      expect($colHeaders.eq(0).data('column')).toBe(column0);
      expect($colHeaders.eq(1).data('column')).toBe(column1);
      expect($colHeaders.eq(2).data('column')).toBe(column2);

      let $rows = table.$rows();
      let $cells0 = $rows.eq(0).find('.table-cell');
      let $cells1 = $rows.eq(1).find('.table-cell');

      expect($cells0.eq(0).text()).toBe('0_0');
      expect($cells0.eq(1).text()).toBe('0_1');
      expect($cells0.eq(2).text()).toBe('0_2');
      expect($cells1.eq(0).text()).toBe('1_0');
      expect($cells1.eq(1).text()).toBe('1_1');
      expect($cells1.eq(2).text()).toBe('1_2');

      table.updateColumnOrder([column2, column0, column1]);

      // Check column header order
      $colHeaders = table.header.findHeaderItems();
      expect($colHeaders.length).toBe(3);
      expect($colHeaders.eq(0).data('column')).toBe(column2);
      expect($colHeaders.eq(1).data('column')).toBe(column0);
      expect($colHeaders.eq(2).data('column')).toBe(column1);

      // Check cells order
      $rows = table.$rows();
      $cells0 = $rows.eq(0).find('.table-cell');
      $cells1 = $rows.eq(1).find('.table-cell');
      expect($cells0.eq(0).text()).toBe('0_2');
      expect($cells0.eq(1).text()).toBe('0_0');
      expect($cells0.eq(2).text()).toBe('0_1');
      expect($cells1.eq(0).text()).toBe('1_2');
      expect($cells1.eq(1).text()).toBe('1_0');
      expect($cells1.eq(2).text()).toBe('1_1');
    });

    it('silently moves cells which are not rendered in view range', () => {
      table.viewRangeSize = 1;
      table.render();
      expect(table.viewRangeRendered).toEqual(new Range(0, 1));

      let $colHeaders = table.header.findHeaderItems();
      let $rows = table.$rows();
      let $cells0 = $rows.eq(0).find('.table-cell');

      expect($rows.length).toBe(1);
      expect(table.rows.length).toBe(2);
      expect($cells0.eq(0).text()).toBe('0_0');
      expect($cells0.eq(1).text()).toBe('0_1');
      expect($cells0.eq(2).text()).toBe('0_2');

      table.updateColumnOrder([column2, column0, column1]);

      // Check column header order
      $colHeaders = table.header.findHeaderItems();
      expect($colHeaders.length).toBe(3);
      expect($colHeaders.eq(0).data('column')).toBe(column2);
      expect($colHeaders.eq(1).data('column')).toBe(column0);
      expect($colHeaders.eq(2).data('column')).toBe(column1);

      // Check cells order
      $rows = table.$rows();
      expect($rows.length).toBe(1);
      expect(table.rows.length).toBe(2);
      $cells0 = $rows.eq(0).find('.table-cell');
      expect($cells0.eq(0).text()).toBe('0_2');
      expect($cells0.eq(1).text()).toBe('0_0');
      expect($cells0.eq(2).text()).toBe('0_1');
    });

  });

  describe('updateColumnHeaders', () => {
    let model, table, column0, column1, column2;

    beforeEach(() => {
      model = helper.createModelFixture(3, 2);

    });

    it('updates the text and sorting state of model columns', () => {
      table = helper.createTable(model);
      column0 = table.columns[0];
      column1 = table.columns[1];
      column2 = table.columns[2];

      let text0 = table.columns[0].text;

      column1 = $.extend({}, table.columns[1]);
      column1.text = 'newText1';
      column1.sortActive = true;
      column1.sortAscending = true;
      column2 = $.extend({}, table.columns[2]);
      column2.text = 'newText2';

      table.updateColumnHeaders([column1, column2]);
      expect(table.columns.length).toBe(3);
      expect(table.columns[0].text).toBe(text0);
      expect(table.columns[1].text).toBe(column1.text);
      expect(table.columns[1].sortAscending).toBe(column1.sortAscending);
      expect(table.columns[1].sortActive).toBe(column1.sortActive);
      expect(table.columns[2].text).toBe(column2.text);
      expect(table.columns[2].sortAscending).toBe(column2.sortAscending);
      expect(table.columns[2].sortActive).toBe(column2.sortActive);
    });

    it('updates sort indices of the sort columns if a sort column got removed', () => {
      model.columns[1].sortActive = true;
      model.columns[1].sortAscending = true;
      model.columns[1].sortIndex = 1;
      model.columns[2].sortActive = true;
      model.columns[2].sortAscending = true;
      model.columns[2].sortIndex = 0;

      table = helper.createTable(model);
      column0 = table.columns[0];
      column1 = table.columns[1];
      column2 = table.columns[2];

      expect(table.columns[1].sortActive).toBe(true);
      expect(table.columns[1].sortAscending).toBe(true);
      expect(table.columns[1].sortIndex).toBe(1);
      expect(table.columns[2].sortActive).toBe(true);
      expect(table.columns[2].sortAscending).toBe(true);
      expect(table.columns[2].sortIndex).toBe(0);

      table.updateColumnHeaders([$.extend({}, table.columns[2], {
        sortActive: false
      })]);
      expect(table.columns[1].sortAscending).toBe(true);
      expect(table.columns[1].sortActive).toBe(true);
      expect(table.columns[1].sortIndex).toBe(0);
      expect(table.columns[2].sortAscending).toBe(true);
      expect(table.columns[2].sortActive).toBe(false);
      expect(table.columns[2].sortIndex).toBe(-1);
    });

    it('updates the text and sorting state of html table header nodes', () => {
      table = helper.createTable(model);
      column0 = table.columns[0];
      column1 = table.columns[1];
      column2 = table.columns[2];
      table.render();

      let $colHeaders = table.header.findHeaderItems();
      expect($colHeaders.eq(0).text()).toBe(column0.text);
      expect($colHeaders.eq(1).text()).toBe(column1.text);
      expect($colHeaders.eq(1)).not.toHaveClass('sort-asc');
      expect($colHeaders.eq(2).text()).toBe(column2.text);

      column1 = $.extend({}, table.columns[1]);
      column1.text = 'newText1';
      column1.sortActive = true;
      column1.sortAscending = true;
      column2 = $.extend({}, table.columns[2]);
      column2.text = 'newText2';

      table.updateColumnHeaders([column1, column2]);
      $colHeaders = table.header.findHeaderItems();
      expect($colHeaders.eq(0).text()).toBe(column0.text);
      expect($colHeaders.eq(1).text()).toBe(`${column1.text}sorting ascending`);
      expect($colHeaders.eq(1)).toHaveClass('sort-asc');
      expect($colHeaders.eq(2).text()).toBe(column2.text);
    });

    it('updates the custom css class of table header nodes', () => {
      table = helper.createTable(model);
      column0 = table.columns[0];
      column1 = table.columns[1];
      column2 = table.columns[2];
      table.render();

      let $colHeaders = table.header.findHeaderItems();
      expect($colHeaders.eq(1)).not.toHaveClass('custom-header');

      column1 = $.extend({}, table.columns[1]);
      column1.headerCssClass = 'custom-header';
      table.updateColumnHeaders([column1]);
      $colHeaders = table.header.findHeaderItems();
      expect($colHeaders.eq(0)).not.toHaveClass('custom-header');
      expect($colHeaders.eq(1)).toHaveClass('custom-header');

      column1 = $.extend({}, table.columns[1]);
      delete column1.headerCssClass;
      table.updateColumnHeaders([column1]);
      $colHeaders = table.header.findHeaderItems();
      expect($colHeaders.eq(0)).not.toHaveClass('custom-header');
      expect($colHeaders.eq(1)).not.toHaveClass('custom-header');
    });

    it('considers html enabled property of table header cells', () => {
      model = helper.createModelFixture(4, 2);
      table = helper.createTable(model);
      column0 = table.columns[0];
      column1 = table.columns[1];
      column2 = table.columns[2];
      let column3 = table.columns[3];

      column0 = helper.createModelColumn('test');
      column0.id = model.columns[0].id;
      column1 = helper.createModelColumn('test');
      column1.headerHtmlEnabled = true;
      column1.id = model.columns[1].id;
      column2 = helper.createModelColumn('<b>test</b>');
      column2.id = model.columns[2].id;
      column3 = helper.createModelColumn('<b>test</b>');
      column3.headerHtmlEnabled = true;
      column3.id = model.columns[3].id;
      table.updateColumnHeaders([column0, column1, column2, column3]);

      table.render();

      let $colHeaders = table.header.findHeaderItems();
      expect($colHeaders.eq(0).text()).toBe('test');
      expect($colHeaders.eq(1).text()).toBe('test');
      expect($colHeaders.eq(2).text()).toBe('<b>test</b>');
      expect($colHeaders.eq(3).text()).toBe('test');
    });
  });

  describe('headerVisible', () => {

    it('hides/shows the table header', () => {
      let model = helper.createModelFixture(2);
      let table = helper.createTable(model);
      table.render();

      expect(table.header).toBeTruthy();
      table.setHeaderVisible(false);
      expect(table.header).toBeFalsy();
    });

    it('adds empty div when set to true if there are no rows', () => {
      let model = helper.createModelFixture(0);
      let table = helper.createTable(model);
      table.setHeaderVisible(false);
      table.render();
      expect(table.$emptyData).toBe(null);

      table.setHeaderVisible(true);
      expect(table.$emptyData).not.toBe(null);

      table.setHeaderVisible(false);
      expect(table.$emptyData).toBe(null);

      let row = helper.createModelRow();
      table.insertRow(row);
      table.setHeaderVisible(true);
      // Still null because there are rows
      expect(table.$emptyData).toBe(null);
    });

    it('removes empty div when set to false even if there are no rows', () => {
      let model = helper.createModelFixture(0);
      let table = helper.createTable(model);
      table.render();
      expect(table.$emptyData).not.toBe(null);

      table.setHeaderVisible(false);
      expect(table.$emptyData).toBe(null);

      table.setHeaderVisible(true);
      expect(table.$emptyData).not.toBe(null);

      let row = helper.createModelRow();
      table.insertRow(row);
      table.setHeaderVisible(false);
      expect(table.$emptyData).toBe(null);

      table.deleteAllRows();
      // Still null even if there are no rows
      expect(table.$emptyData).toBe(null);
    });

  });

  describe('Column visibility', () => {

    it('update headers and rows when visibility of a column changes', () => {
      let model = helper.createModelFixture(2, 1);
      let table = helper.createTable(model);
      table.render();

      expect(table.columns[0].visible).toBe(true);
      expect(table.columns[1].visible).toBe(true);
      expect(table.$container.find('.table-header-item:not(.filler)').length).toBe(2);
      expect(table.$container.find('.table-cell').length).toBe(2);

      table.columns[1].setVisible(false);

      // when column is invisible it must be removed from the header
      // also the cells of this column must be removed from all table rows
      expect(table.columns[0].visible).toBe(true);
      expect(table.columns[1].visible).toBe(false);
      expect(table.$container.find('.table-header-item:not(.filler)').length).toBe(1);
      expect(table.$container.find('.table-cell').length).toBe(1);
    });

    it('visibleColumns() only return visible columns', () => {
      let model = helper.createModelFixture(2, 1);
      let table = helper.createTable(model);

      expect(table.columns.length).toBe(2);
      expect(table.visibleColumns().length).toBe(2);

      table.columns[0].setVisible(false);

      expect(table.columns.length).toBe(2);
      expect(table.visibleColumns().length).toBe(1);
    });

    it('moveColumn() must deal with different indices for visible and all columns', () => {
      let model = helper.createModelFixture(3, 1);
      let table = helper.createTable(model);
      let colA = table.columns[0];
      let colB = table.columns[1];
      let colC = table.columns[2];

      colB.setVisible(false); // column in the middle is invisible
      expect(table.visibleColumns().length).toBe(2);

      table.moveColumn(colC, 1, 0); // move C to be the first column
      expect(table.visibleColumns()).toEqual([colC, colA]);
      expect(table.columns).toEqual([colC, colA, colB]);

      table.moveColumn(colC, 0, 1); // move C to be the last column
      expect(table.visibleColumns()).toEqual([colA, colC]);
      expect(table.columns).toEqual([colA, colC, colB]);
    });

  });

  describe('moveRowUp', () => {

    it('moves row one up', () => {
      let model = helper.createModelFixture(1, 0);
      let table = helper.createTable(model);
      table.insertRows([
        helper.createModelRow('1', ['A']),
        helper.createModelRow('1', ['B']),
        helper.createModelRow('1', ['C'])
      ]);

      // Move row B one up
      let rowB = table.rows[1];
      table.moveRowUp(rowB);
      expect(table.rows[0].cells[0].text).toBe('B');
      expect(table.rows[1].cells[0].text).toBe('A');
      expect(table.rows[2].cells[0].text).toBe('C');

      // Move row B one up again (no effect)
      table.moveRowUp(rowB);
      expect(table.rows[0].cells[0].text).toBe('B');
      expect(table.rows[1].cells[0].text).toBe('A');
      expect(table.rows[2].cells[0].text).toBe('C');

      // Move row D one up
      let rowD = table.rows[2];
      table.moveRowUp(rowD);
      expect(table.rows[0].cells[0].text).toBe('B');
      expect(table.rows[1].cells[0].text).toBe('C');
      expect(table.rows[2].cells[0].text).toBe('A');

      // Move row D one up again
      table.moveRowUp(rowD);
      expect(table.rows[0].cells[0].text).toBe('C');
      expect(table.rows[1].cells[0].text).toBe('B');
      expect(table.rows[2].cells[0].text).toBe('A');
    });
  });

  describe('moveRowDown', () => {

    it('moves row one down', () => {
      let model = helper.createModelFixture(1, 0);
      let table = helper.createTable(model);
      table.insertRows([
        helper.createModelRow('1', ['A']),
        helper.createModelRow('1', ['B']),
        helper.createModelRow('1', ['C'])
      ]);

      // Move row B one down
      let rowB = table.rows[1];
      table.moveRowDown(rowB);
      expect(table.rows[0].cells[0].text).toBe('A');
      expect(table.rows[1].cells[0].text).toBe('C');
      expect(table.rows[2].cells[0].text).toBe('B');

      // Move row B one down again (no effect)
      table.moveRowDown(rowB);
      expect(table.rows[0].cells[0].text).toBe('A');
      expect(table.rows[1].cells[0].text).toBe('C');
      expect(table.rows[2].cells[0].text).toBe('B');

      // Move row A one down
      let rowA = table.rows[0];
      table.moveRowDown(rowA);
      expect(table.rows[0].cells[0].text).toBe('C');
      expect(table.rows[1].cells[0].text).toBe('A');
      expect(table.rows[2].cells[0].text).toBe('B');

      // Move row A one down again
      table.moveRowDown(rowA);
      expect(table.rows[0].cells[0].text).toBe('C');
      expect(table.rows[1].cells[0].text).toBe('B');
      expect(table.rows[2].cells[0].text).toBe('A');
    });
  });

  describe('moveRowToTop', () => {

    it('moves row to the top', () => {
      let model = helper.createModelFixture(1, 0);
      let table = helper.createTable(model);
      table.insertRows([
        helper.createModelRow('1', ['A']),
        helper.createModelRow('1', ['B']),
        helper.createModelRow('1', ['C'])
      ]);

      // Move row B to top
      let rowB = table.rows[1];
      table.moveRowToTop(rowB);
      expect(table.rows[0].cells[0].text).toBe('B');
      expect(table.rows[1].cells[0].text).toBe('A');
      expect(table.rows[2].cells[0].text).toBe('C');

      // Move row B to top again (no effect)
      table.moveRowToTop(rowB);
      expect(table.rows[0].cells[0].text).toBe('B');
      expect(table.rows[1].cells[0].text).toBe('A');
      expect(table.rows[2].cells[0].text).toBe('C');

      // Move row C to top
      let rowC = table.rows[2];
      table.moveRowToTop(rowC);
      expect(table.rows[0].cells[0].text).toBe('C');
      expect(table.rows[1].cells[0].text).toBe('B');
      expect(table.rows[2].cells[0].text).toBe('A');
    });
  });

  describe('moveRowToBottom', () => {

    it('moves row to the bottom', () => {
      let model = helper.createModelFixture(1, 0);
      let table = helper.createTable(model);
      table.insertRows([
        helper.createModelRow('1', ['A']),
        helper.createModelRow('1', ['B']),
        helper.createModelRow('1', ['C'])
      ]);

      // Move row B to bottom
      let rowB = table.rows[1];
      table.moveRowToBottom(rowB);
      expect(table.rows[0].cells[0].text).toBe('A');
      expect(table.rows[1].cells[0].text).toBe('C');
      expect(table.rows[2].cells[0].text).toBe('B');

      // Move row B to bottom again (no effect)
      table.moveRowToBottom(rowB);
      expect(table.rows[0].cells[0].text).toBe('A');
      expect(table.rows[1].cells[0].text).toBe('C');
      expect(table.rows[2].cells[0].text).toBe('B');

      // Move row A to bottom
      let rowA = table.rows[0];
      table.moveRowToBottom(rowA);
      expect(table.rows[0].cells[0].text).toBe('C');
      expect(table.rows[1].cells[0].text).toBe('B');
      expect(table.rows[2].cells[0].text).toBe('A');
    });
  });

  describe('moveVisibleRowUp', () => {

    it('moves row one up regarding filtered rows', () => {
      let model = helper.createModelFixture(1, 0);
      let table = helper.createTable(model);
      table.insertRows([
        helper.createModelRow('1', ['A']),
        helper.createModelRow('1', ['B-filtered']),
        helper.createModelRow('1', ['C']),
        helper.createModelRow('1', ['D-filtered']),
        helper.createModelRow('1', ['E']),
        helper.createModelRow('1', ['F-filtered']),
        helper.createModelRow('1', ['G'])
      ]);

      // Filter active
      helper.createAndRegisterColumnFilter({
        table: table,
        session: session,
        column: table.columns[0],
        selectedValues: ['B-filtered', 'D-filtered', 'F-filtered']
      });

      // Move row D one up
      let rowD = table.rows[3];
      table.moveVisibleRowUp(rowD);
      expect(table.rows[0].cells[0].text).toBe('A');
      expect(table.rows[1].cells[0].text).toBe('D-filtered');
      expect(table.rows[2].cells[0].text).toBe('B-filtered');
      expect(table.rows[3].cells[0].text).toBe('C');
      expect(table.rows[4].cells[0].text).toBe('E');
      expect(table.rows[5].cells[0].text).toBe('F-filtered');
      expect(table.rows[6].cells[0].text).toBe('G');

      // Move row D one up again (no effect)
      table.moveVisibleRowUp(rowD);
      expect(table.rows[0].cells[0].text).toBe('A');
      expect(table.rows[1].cells[0].text).toBe('D-filtered');
      expect(table.rows[2].cells[0].text).toBe('B-filtered');
      expect(table.rows[3].cells[0].text).toBe('C');
      expect(table.rows[4].cells[0].text).toBe('E');
      expect(table.rows[5].cells[0].text).toBe('F-filtered');
      expect(table.rows[6].cells[0].text).toBe('G');

      // Move row F one up
      let rowF = table.rows[5];
      table.moveVisibleRowUp(rowF);
      expect(table.rows[0].cells[0].text).toBe('A');
      expect(table.rows[1].cells[0].text).toBe('D-filtered');
      expect(table.rows[2].cells[0].text).toBe('F-filtered');
      expect(table.rows[3].cells[0].text).toBe('B-filtered');
      expect(table.rows[4].cells[0].text).toBe('C');
      expect(table.rows[5].cells[0].text).toBe('E');
      expect(table.rows[6].cells[0].text).toBe('G');

      // Move row F one up again
      table.moveVisibleRowUp(rowF);
      expect(table.rows[0].cells[0].text).toBe('A');
      expect(table.rows[1].cells[0].text).toBe('F-filtered');
      expect(table.rows[2].cells[0].text).toBe('D-filtered');
      expect(table.rows[3].cells[0].text).toBe('B-filtered');
      expect(table.rows[4].cells[0].text).toBe('C');
      expect(table.rows[5].cells[0].text).toBe('E');
      expect(table.rows[6].cells[0].text).toBe('G');
    });
  });

  describe('moveVisibleRowDown', () => {

    it('moves row one up regarding filtered rows', () => {
      let model = helper.createModelFixture(1, 0);
      let table = helper.createTable(model);
      table.insertRows([
        helper.createModelRow('1', ['A']),
        helper.createModelRow('1', ['B-filtered']),
        helper.createModelRow('1', ['C']),
        helper.createModelRow('1', ['D-filtered']),
        helper.createModelRow('1', ['E']),
        helper.createModelRow('1', ['F-filtered']),
        helper.createModelRow('1', ['G'])
      ]);

      // Filter active
      helper.createAndRegisterColumnFilter({
        table: table,
        session: session,
        column: table.columns[0],
        selectedValues: ['B-filtered', 'D-filtered', 'F-filtered']
      });

      // Move row D one down
      let rowD = table.rows[3];
      table.moveVisibleRowDown(rowD);
      expect(table.rows[0].cells[0].text).toBe('A');
      expect(table.rows[1].cells[0].text).toBe('B-filtered');
      expect(table.rows[2].cells[0].text).toBe('C');
      expect(table.rows[3].cells[0].text).toBe('E');
      expect(table.rows[4].cells[0].text).toBe('F-filtered');
      expect(table.rows[5].cells[0].text).toBe('D-filtered');
      expect(table.rows[6].cells[0].text).toBe('G');

      // Move row D one down again (no effect)
      table.moveVisibleRowDown(rowD);
      expect(table.rows[0].cells[0].text).toBe('A');
      expect(table.rows[1].cells[0].text).toBe('B-filtered');
      expect(table.rows[2].cells[0].text).toBe('C');
      expect(table.rows[3].cells[0].text).toBe('E');
      expect(table.rows[4].cells[0].text).toBe('F-filtered');
      expect(table.rows[5].cells[0].text).toBe('D-filtered');
      expect(table.rows[6].cells[0].text).toBe('G');

      // Move row B one down
      let rowB = table.rows[1];
      table.moveVisibleRowDown(rowB);
      expect(table.rows[0].cells[0].text).toBe('A');
      expect(table.rows[1].cells[0].text).toBe('C');
      expect(table.rows[2].cells[0].text).toBe('E');
      expect(table.rows[3].cells[0].text).toBe('F-filtered');
      expect(table.rows[4].cells[0].text).toBe('B-filtered');
      expect(table.rows[5].cells[0].text).toBe('D-filtered');
      expect(table.rows[6].cells[0].text).toBe('G');

      // Move row B one down again
      table.moveVisibleRowDown(rowB);
      expect(table.rows[0].cells[0].text).toBe('A');
      expect(table.rows[1].cells[0].text).toBe('C');
      expect(table.rows[2].cells[0].text).toBe('E');
      expect(table.rows[3].cells[0].text).toBe('F-filtered');
      expect(table.rows[4].cells[0].text).toBe('D-filtered');
      expect(table.rows[5].cells[0].text).toBe('B-filtered');
      expect(table.rows[6].cells[0].text).toBe('G');
    });

  });

  describe('rowStatus', () => {

    it('changes when updating the value', () => {
      let model = helper.createModelFixture(2, 4);
      let table = helper.createTable(model);

      // Check initial status
      expect(table.rows[0].status).toBe(TableRow.Status.NON_CHANGED);
      expect(table.rows[1].status).toBe(TableRow.Status.NON_CHANGED);
      expect(table.rows[2].status).toBe(TableRow.Status.NON_CHANGED);
      expect(table.rows[3].status).toBe(TableRow.Status.NON_CHANGED);

      let column0 = table.columns[0];

      // Update value --> should change status
      column0.setCellValue(table.rows[0], 77);
      expect(table.rows[0].status).toBe(TableRow.Status.UPDATED);
      expect(table.rows[1].status).toBe(TableRow.Status.NON_CHANGED);

      // Call setCellValue(), but with same value --> should not change status
      column0.setCellValue(table.rows[1], table.cellValue(column0, table.rows[1]));
      expect(table.rows[1].status).toBe(TableRow.Status.NON_CHANGED);

      // Change displayText --> should not change status
      column0.setCellText(table.rows[2], 'ABC');
      expect(table.rows[2].status).toBe(TableRow.Status.NON_CHANGED);

      // Change value via cell.setValue() --> does not update anything
      table.rows[3].cells[0].setValue(88);
      expect(table.rows[3].status).toBe(TableRow.Status.NON_CHANGED);

      // Inserted rows are "INSERTED"
      expect(table.rows[4]).toBeUndefined();
      table.insertRow({
        cells: [null, null]
      });
      expect(table.rows[4].status).toBe(TableRow.Status.INSERTED);
      column0.setCellValue(table.rows[4], 99);
      expect(table.rows[4].status).toBe(TableRow.Status.INSERTED); // Still inserted
    });
  });

  describe('ensureExpansionVisible', () => {
    let model, table, rows, $scrollable;

    beforeEach(() => {
      $('<style>' +
        '.table-row {height: 28px;}' +
        '.table {height: 170px; overflow: hidden;}' +
        '.table-data {height: 150px; overflow: hidden;}' +
        '</style>').appendTo($('#sandbox'));

      model = helper.createModelFixture(1, 0);
      table = helper.createTable(model);
      table.insertRows([
        helper.createModelRow('1', ['A']),
        helper.createModelRow('2', ['B']),
        helper.createModelRow('3', ['C']),
        helper.createModelRow('4', ['D']),
        helper.createModelRow('5', ['E']),
        helper.createModelRow('6', ['F']),
        helper.createModelRow('7', ['G'])
      ]);
      table.insertRows(helper.createModelRows(1, 2, '1'));
      table.insertRows(helper.createModelRows(1, 2, '2'));
      table.insertRows(helper.createModelRows(1, 2, '3'));
      table.insertRows(helper.createModelRows(1, 5, '4'));
      table.insertRows(helper.createModelRows(1, 2, '5'));
      table.insertRows(helper.createModelRows(1, 2, '6'));
      table.insertRows(helper.createModelRows(1, 2, '7'));
      rows = table.rows;

      table.render();
      expect(table.rowHeight).toBe(28);
      $scrollable = table.get$Scrollable();
    });

    it('scrolls current row to the top when expanding a large child set', () => {
      expect(scrollbars.isLocationInView(graphics.offsetBounds(table.rowById('1').$row), $scrollable)).toBe(true);
      expect(scrollbars.isLocationInView(graphics.offsetBounds(table.rowById('7').$row), $scrollable)).toBe(false);
      expect(table.rowById('4').expanded).toBe(false);
      table.expandRow(table.rowById('4'), true);
      expect(table.rowById('4').expanded).toBe(true);
      // first visible row should be row3 (one above the expanded node)
      expect(scrollbars.isLocationInView(graphics.offsetBounds(table.rowById('2').$row), $scrollable)).toBe(false);
      expect(scrollbars.isLocationInView(graphics.offsetBounds(table.rowById('3').$row), $scrollable)).toBe(true);
      expect(scrollbars.isLocationInView(graphics.offsetBounds(table.rowById('4').$row), $scrollable)).toBe(true);
      // node5 isn't visible anymore since node4's children use up all the space
      expect(scrollbars.isLocationInView(graphics.offsetBounds(table.rowById('5').$row), $scrollable)).toBe(false);
    });

    it('scrolls current row up so that the full expansion is visible plus half a row at the bottom', () => {
      expect(table.rowById('5').expanded).toBe(false);
      table.expandRow(table.rowById('5'), true);
      expect(table.rowById('5').expanded).toBe(true);
      expect(scrollbars.isLocationInView(graphics.offsetBounds(table.rowById('4').$row), $scrollable)).toBe(true);
      expect(scrollbars.isLocationInView(graphics.offsetBounds(table.rowById('5').$row), $scrollable)).toBe(true);
      expect(scrollbars.isLocationInView(graphics.offsetBounds(table.rowById('5').childRows[0].$row), $scrollable)).toBe(true);
      expect(scrollbars.isLocationInView(graphics.offsetBounds(table.rowById('5').childRows[1].$row), $scrollable)).toBe(true);
      // half of row6 should still be visible after the expansion
      expect(scrollbars.isLocationInView(graphics.offsetBounds(table.rowById('6').$row), $scrollable)).toBe(true);
      expect(scrollbars.isLocationInView(graphics.offsetBounds(table.rowById('7').$row), $scrollable)).toBe(false);
    });

  });

  describe('invisible', () => {
    it('does not try to read row height when invisible', () => {
      let tableField = scout.create(TableField, {
        parent: session.desktop,
        visible: false
      });
      let model = helper.createModelFixture(2, 1);
      let table = helper.createTable(model);
      tableField.render();

      tableField.setTable(table);
      expect(table.rendered).toBe(true);
      expect(table.rows[0].height).toBe(null);

      tableField.validateLayout();
      expect(tableField.htmlComp.valid).toBe(false);
      expect(table.htmlComp.valid).toBe(false);
      expect(table.rows[0].height).toBe(null);

      tableField.setVisible(true);
      expect(table.rows[0].height).toBe(null);

      tableField.validateLayout();
      expect(tableField.htmlComp.valid).toBe(true);
      expect(table.htmlComp.valid).toBe(true);
      expect(table.rows[0].height).toBeGreaterThan(0);
    });
  });

  describe('_unwrapText', () => {
    it('converts multi-line text to single-line text', () => {
      let model = helper.createModelFixture(2, 1);
      let table = helper.createTable(model);

      expect(table._unwrapText()).toBe('');
      expect(table._unwrapText(null)).toBe('');
      expect(table._unwrapText('')).toBe('');
      expect(table._unwrapText('abc')).toBe('abc');

      expect(table._unwrapText('abc')).toBe('abc');
      expect(table._unwrapText(' abc')).toBe('abc');
      expect(table._unwrapText('abc ')).toBe('abc');
      expect(table._unwrapText(' abc ')).toBe('abc');

      expect(table._unwrapText('abc')).toBe('abc');
      expect(table._unwrapText('\fabc')).toBe('abc');
      expect(table._unwrapText('abc\f')).toBe('abc');
      expect(table._unwrapText('\fabc\f')).toBe('abc');

      expect(table._unwrapText('a bc')).toBe('a bc');
      expect(table._unwrapText(' a bc')).toBe('a bc');
      expect(table._unwrapText('a bc ')).toBe('a bc');
      expect(table._unwrapText(' a bc ')).toBe('a bc');

      expect(table._unwrapText('a  bc')).toBe('a  bc');
      expect(table._unwrapText('  a  bc')).toBe('a  bc');
      expect(table._unwrapText('a  bc  ')).toBe('a  bc');
      expect(table._unwrapText('  a  bc  ')).toBe('a  bc');

      expect(table._unwrapText('a \tbc')).toBe('a  bc');
      expect(table._unwrapText(' \ta \tbc')).toBe('a  bc');
      expect(table._unwrapText('a \tbc \t')).toBe('a  bc');
      expect(table._unwrapText(' \ta \tbc \t')).toBe('a  bc');

      expect(table._unwrapText('a\tbc')).toBe('a bc');
      expect(table._unwrapText(' a\tbc')).toBe('a bc');
      expect(table._unwrapText('a\tbc ')).toBe('a bc');
      expect(table._unwrapText(' a\tbc ')).toBe('a bc');

      expect(table._unwrapText('a\tbc')).toBe('a bc');
      expect(table._unwrapText('\ta\tbc')).toBe('a bc');
      expect(table._unwrapText('a\tbc\t')).toBe('a bc');
      expect(table._unwrapText('\ta\tbc\t')).toBe('a bc');

      expect(table._unwrapText('abc')).toBe('abc');
      expect(table._unwrapText('\tabc')).toBe('abc');
      expect(table._unwrapText('abc\t')).toBe('abc');
      expect(table._unwrapText('\tabc\t')).toBe('abc');

      expect(table._unwrapText('abc')).toBe('abc');
      expect(table._unwrapText(' \tabc')).toBe('abc');
      expect(table._unwrapText('abc\t ')).toBe('abc');
      expect(table._unwrapText(' \tabc\t ')).toBe('abc');

      expect(table._unwrapText('abc')).toBe('abc');
      expect(table._unwrapText(' \t\nabc')).toBe('abc');
      expect(table._unwrapText('abc\n\t ')).toBe('abc');
      expect(table._unwrapText(' \t\nabc\n\t ')).toBe('abc');

      expect(table._unwrapText('abc\n123')).toBe('abc 123');
      expect(table._unwrapText(' \t\nabc\n123')).toBe('abc 123');
      expect(table._unwrapText('abc\n123\n\t ')).toBe('abc 123');
      expect(table._unwrapText(' \t\nabc\n123\n\t ')).toBe('abc 123');

      expect(table._unwrapText('abc\n 123')).toBe('abc 123');
      expect(table._unwrapText(' \t\nabc\n 123')).toBe('abc 123');
      expect(table._unwrapText('abc\n 123\n\t ')).toBe('abc 123');
      expect(table._unwrapText(' \t\nabc\n 123\n\t ')).toBe('abc 123');

      expect(table._unwrapText('abc\n \n123')).toBe('abc 123');
      expect(table._unwrapText(' \t\nabc\n \n123')).toBe('abc 123');
      expect(table._unwrapText('abc\n \n123\n\t ')).toBe('abc 123');
      expect(table._unwrapText(' \t\nabc\n \n123\n\t ')).toBe('abc 123');

      expect(table._unwrapText('a bc\n \n123')).toBe('a bc 123');
      expect(table._unwrapText(' \t\na bc\n \n123')).toBe('a bc 123');
      expect(table._unwrapText('a bc\n \n123\n\t ')).toBe('a bc 123');
      expect(table._unwrapText(' \t\na bc\n \n123\n\t ')).toBe('a bc 123');

      expect(table._unwrapText('a bc\r\n \r\n123')).toBe('a bc 123');
      expect(table._unwrapText(' \t\r\na bc\n \r\n123')).toBe('a bc 123');
      expect(table._unwrapText('a bc\r\n \r\n123\r\n\t ')).toBe('a bc 123');
      expect(table._unwrapText(' \t\r\na bc\r\n \r\n123\r\n\t ')).toBe('a bc 123');

      expect(table._unwrapText('a bc\n\r \n\r \n\r  12\t3')).toBe('a bc 12 3');
      expect(table._unwrapText(' \t\n\ra bc\n\r \n\r \n\r  12\t3')).toBe('a bc 12 3');
      expect(table._unwrapText('a bc\n\r \n\r \n\r  12\t3\n\r\t ')).toBe('a bc 12 3');
      expect(table._unwrapText(' \t\n\ra bc\n\r \n\r \n\r  12\t3\n\r\t ')).toBe('a bc 12 3');

      expect(table._unwrapText('a\t bc\n\r \n\r \n\r  12\t3')).toBe('a  bc 12 3');
      expect(table._unwrapText(' \t\n\ra\t bc\n\r \n\r \n\r  12\t3')).toBe('a  bc 12 3');
      expect(table._unwrapText('a\t bc\n\r \n\r \n\r  12\t3\n\r\t ')).toBe('a  bc 12 3');
      expect(table._unwrapText(' \t\n\ra\t bc\n\r \n\r \n\r  12\t3\n\r\t ')).toBe('a  bc 12 3');
    });

    it('can convert selected rows to text', () => {
      let model = helper.createModelFixture(2, 0);
      model.multilineText = true;
      model.rows = [
        helper.createModelRow('1', [
          '\n  \n\tline 1  \n line   2\n\n\nline 3  ',
          ' second\ncolumn '
        ]),
        helper.createModelRow('2', [
          '',
          'test'
        ])
      ];
      let table = helper.createTable(model);

      table.selectAll();
      let text = table._selectedRowsToText();
      expect(text).toBe('line 1 line   2 line 3\tsecond column\n\ttest');
    });
  });

  describe('scrollTo', () => {
    it('does not scroll if row is invisible due to filter', () => {
      let model = helper.createModelFixture(2, 4);
      let table = helper.createTable(model);
      let row = table.rows[1];
      table.render();
      table.addFilter({
        accept: r => row !== r
      });
      table.scrollTo(row);
      // Expect no error and no scrolling
      expect(table.$data[0].scrollTop).toBe(0);
    });
  });

  describe('addFilter', () => {
    it('accepts filter function', () => {
      let model = helper.createModelFixture(2, 4);
      let table = helper.createTable(model);
      let filter = row => row === table.rows[1];
      table.addFilter(filter);
      expect(table.filters.length).toBe(1);
      expect(table.filteredRows().length).toBe(1);
      expect(table.filteredRows()[0]).toBe(table.rows[1]);

      table.removeFilter(filter);
      expect(table.filters.length).toBe(0);
      expect(table.filteredRows().length).toBe(4);

      table.setFilters([filter]);
      expect(table.filters.length).toBe(1);
      expect(table.filteredRows().length).toBe(1);
      expect(table.filteredRows()[0]).toBe(table.rows[1]);

      table.setFilters([]);
      expect(table.filters.length).toBe(0);
      expect(table.filteredRows().length).toBe(4);
    });

    it('accepts filter object', () => {
      let model = helper.createModelFixture(2, 4);
      let table = helper.createTable(model);
      let filter = {
        accept: row => row === table.rows[1]
      };
      table.addFilter(filter);
      expect(table.filters.length).toBe(1);
      expect(table.filteredRows().length).toBe(1);
      expect(table.filteredRows()[0]).toBe(table.rows[1]);

      table.removeFilter(filter);
      expect(table.filters.length).toBe(0);
      expect(table.filteredRows().length).toBe(4);

      table.setFilters([filter]);
      expect(table.filters.length).toBe(1);
      expect(table.filteredRows().length).toBe(1);
      expect(table.filteredRows()[0]).toBe(table.rows[1]);

      table.setFilters([]);
      expect(table.filters.length).toBe(0);
      expect(table.filteredRows().length).toBe(4);
    });
  });

  describe('replaceRows', () => {

    it('keeps the selection if possible', () => {
      const rows = helper.createModelRows(3, 5);
      const table = helper.createTable(helper.createModel(helper.createModelColumns(3), rows));
      const loadAll = () => {
        table.replaceRows(rows);
      };
      const loadSome = () => {
        table.replaceRows(rows.slice(0, 3));
      };

      table.render();
      table.hasReloadHandler = true;
      table.on('reload', loadAll);

      expect(table.selectedRows).toEqual([]);

      table.selectRow(table.rows[0]);
      expect(table.selectedRows).toEqual([table.rows[0]]);

      table.reload();
      expect(table.selectedRows).toEqual([table.rows[0]]);

      table.selectRows([table.rows[1], table.rows[4]]);
      expect(table.selectedRows).toEqual([table.rows[1], table.rows[4]]);

      table.reload();
      expect(table.selectedRows).toEqual([table.rows[1], table.rows[4]]);

      table.off('reload', loadAll);
      table.on('reload', loadSome);
      table.reload();
      expect(table.selectedRows).toEqual([table.rows[1]]);
    });

    it('expands necessary rows if selection to keep is a collapsed child row', () => {
      const [rowA, rowB, rowC] = helper.createModelRows(0, 3);
      const [rowAA, rowAB, rowAC] = helper.createModelRows(0, 3, rowA.id);
      const [rowBA, rowBB, rowBC] = helper.createModelRows(0, 3, rowB.id);
      const [rowCA, rowCB, rowCC] = helper.createModelRows(0, 3, rowC.id);
      const [rowAAA, rowAAB] = helper.createModelRows(0, 2, rowAA.id);
      const [rowBBA, rowBBB] = helper.createModelRows(0, 2, rowBB.id);
      const [rowCCA, rowCCB] = helper.createModelRows(0, 2, rowCC.id);
      const rows = [
        rowA, rowB, rowC,
        rowAA, rowAB, rowAC,
        rowBA, rowBB, rowBC,
        rowCA, rowCB, rowCC,
        rowAAA, rowAAB,
        rowBBA, rowBBB,
        rowCCA, rowCCB
      ];
      rows.forEach(row => row.cells.push(helper.createModelCell(row.id, row.id)));
      const rowModels = () => $.extend(true, [], rows); // copy models as table modifies the given array
      const table = helper.createTable($.extend(helper.createModel(helper.createModelColumns(1), rowModels()), {
        hierarchical: true
      }));
      const load = () => table.replaceRows(rowModels());
      const findRow = (row: TableRowModel) => table.rows.find(r => r.id === row.id);

      table.render();
      table.hasReloadHandler = true;
      table.on('reload', load);

      expect(table.selectedRows).toEqual([]);
      table.rows.forEach(r => expect(r.expanded).toBeFalse());

      table.expandRows([findRow(rowA), findRow(rowAA), findRow(rowAB), findRow(rowC)]);
      expect(table.rows.filter(row => row.expanded).length).toBe(4);

      table.reload();
      table.rows.forEach(r => expect(r.expanded).toBeFalse());

      table.expandRows([findRow(rowA), findRow(rowAA), findRow(rowAB), findRow(rowC)]);
      expect(table.rows.filter(row => row.expanded).length).toBe(4);

      table.selectRows([findRow(rowAAB), findRow(rowC), findRow(rowCA)]);
      expect(table.selectedRows.length).toBe(3);

      table.reload();
      expect(table.selectedRows.length).toBe(3);
      expect(table.rows.filter(row => row.expanded).length).toBe(3);
      expect(findRow(rowA).expanded).toBeTrue();
      expect(findRow(rowAA).expanded).toBeTrue();
      expect(findRow(rowC).expanded).toBeTrue();

      table.selectRow(findRow(rowCA));
      expect(table.selectedRows.length).toBe(1);

      table.reload();
      expect(table.selectedRows.length).toBe(1);
      expect(table.rows.filter(row => row.expanded).length).toBe(1);
      expect(findRow(rowC).expanded).toBeTrue();
    });
  });

  describe('primaryKeyColumns', () => {

    it('returns the correct columns', () => {
      let table = helper.createTable(helper.createModel([], []));
      expect(table.primaryKeyColumns()).toEqual([]);

      table = helper.createTable(helper.createModel(helper.createModelColumns(3), []));
      expect(table.primaryKeyColumns()).toEqual([]);

      table = helper.createTable(helper.createModel($.extend(true, [], helper.createModelColumns(3), [{}, {primaryKey: true}, {primaryKey: false}]), []));
      expect(table.primaryKeyColumns()).toEqual([table.columns[1]]);

      table = helper.createTable(helper.createModel($.extend(true, [], helper.createModelColumns(3), [{primaryKey: true}, {}, {primaryKey: true}]), []));
      expect(table.primaryKeyColumns()).toEqual([table.columns[0], table.columns[2]]);
    });
  });

  describe('summaryColumns', () => {

    it('returns the correct columns', () => {
      let table = helper.createTable(helper.createModel([], []));
      expect(table.summaryColumns()).toEqual([]);

      table = helper.createTable(helper.createModel(helper.createModelColumns(3), []));
      expect(table.summaryColumns()).toEqual([]);

      table = helper.createTable(helper.createModel($.extend(true, [], helper.createModelColumns(3), [{}, {summary: true}, {summary: false}]), []));
      expect(table.summaryColumns()).toEqual([table.columns[1]]);

      table = helper.createTable(helper.createModel($.extend(true, [], helper.createModelColumns(3), [{summary: true}, {}, {summary: true}]), []));
      expect(table.summaryColumns()).toEqual([table.columns[0], table.columns[2]]);
    });
  });

  describe('aria properties', () => {

    it('has aria role grid', () => {
      let model = helper.createModelFixture(2, 2);
      let table = helper.createTable(model);
      table.render();
      expect(table.$container).toHaveAttr('role', 'grid');
    });

    it('has a header row with role row, with cells with role columnheader or separator', () => {
      let model = helper.createModelFixture(2, 4);
      let table = helper.createTable(model);
      table.render();

      let $headerColumns = table.header.$container.find('.table-header-item').not('.filler');
      expect($headerColumns.length).toBeGreaterThan(0);
      $headerColumns.each((index, $headerColumn) => {
        expect($headerColumn).toHaveAttr('role', 'columnheader');
        expect($headerColumn).not.toHaveAttr('role', 'none');
      });

      let $headerColumnSeparators = table.header.$container.find('.table-header-resize');
      expect($headerColumnSeparators.length).toBeGreaterThan(0);
      $headerColumnSeparators.each((index, $headerColumnSeparator) => {
        expect($headerColumnSeparator).toHaveAttr('role', 'none');
        expect($headerColumnSeparator).not.toHaveAttr('role', 'columnheader');
      });
    });

    it('has a data section with role rowgroup, the rows contained are of role row, the cells of role gridcell', () => {
      let model = helper.createModelFixture(2, 4);
      let table = helper.createTable(model);
      table.render();
      expect(table.$data).toHaveAttr('role', 'rowgroup');
      table.rows.forEach(row => {
        expect(row.$row).toHaveAttr('role', 'row');
        row.$row.children('.table-cell').each((index, cell) => {
          expect($(cell)).toHaveAttr('role', 'gridcell');
        });
      });
    });

    it('has selected rows and their cells set to aria-selected true', () => {
      let model = helper.createModelFixture(2, 4);
      let table = helper.createTable(model);
      table.render();

      function expectSelected(row: TableRow, selected: boolean) {
        if (selected) {
          expect(row.$row).toHaveAttr('aria-selected', 'true');
        } else {
          expect(row.$row.attr('aria-selected')).toBeFalsy();
        }
      }

      expectSelected(table.rows[0], false);
      expectSelected(table.rows[1], false);

      table.selectRow(table.rows[0]);

      expectSelected(table.rows[0], true);
      expectSelected(table.rows[1], false);

      table.selectAll();

      expectSelected(table.rows[0], true);
      expectSelected(table.rows[1], true);

      table.deselectAll();

      expectSelected(table.rows[0], false);
      expectSelected(table.rows[1], false);

      table.selectRow(table.rows[1]);

      expectSelected(table.rows[0], false);
      expectSelected(table.rows[1], true);
    });

    it('has aria-activedescendant set if a row is selected', () => {
      let model = helper.createModelFixture(2, 4);
      let table = helper.createTable(model);
      table.render();
      expect(table.$container.attr('aria-activedescendant')).toBeFalsy();

      table.selectRow(table.rows[0]);
      expect(table.$container.attr('aria-activedescendant')).toBe(table.rows[0].$row.attr('id'));

      table.selectRow(table.rows[1]);
      expect(table.$container.attr('aria-activedescendant')).toBe(table.rows[1].$row.attr('id'));
    });

    it('has a description for aggregation rows', () => {
      let model = helper.createModelFixture(2, 4);
      let table = helper.createTable(model);
      table.render();

      table.groupColumn(table.columns[0]);
      let $aggregateRows = table.$aggregateRows();
      expect($aggregateRows.length).toBeGreaterThan(0);
      $aggregateRows.each((index, aggregateRow) => {
        expect($(aggregateRow).attr('aria-description')).toBeTruthy();
        expect($(aggregateRow).attr('aria-describedby')).toBeFalsy();
      });
    });
  });
});
