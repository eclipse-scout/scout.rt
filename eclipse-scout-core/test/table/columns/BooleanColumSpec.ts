/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BooleanColumn, Cell, TableRow} from '../../../src/index';
import {JQueryTesting, TableSpecHelper} from '../../../src/testing/index';

describe('BooleanColumn', () => {
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

  describe('table checkable column', () => {

    it('a checkbox column gets inserted if table.checkable=true', () => {
      let model = helper.createModelFixture(2);
      model.checkable = true;
      expect(model.columns.length).toBe(2);
      let table = helper.createTable(model);
      expect(table.columns.length).toBe(3);

      table.render();
      expect(table.columns[0] instanceof BooleanColumn).toBeTruthy();
    });

    it('no checkbox column gets inserted if table.checkable=false', () => {
      let model = helper.createModelFixture(2);
      model.checkable = false;
      expect(model.columns.length).toBe(2);
      let table = helper.createTable(model);
      expect(table.columns.length).toBe(2);

      table.render();
      expect(table.columns[0] instanceof BooleanColumn).toBeFalsy();
    });

    it('this.checkableColumn is set to the new column', () => {
      let model = helper.createModelFixture(2);
      model.checkable = true;
      let table = helper.createTable(model);
      table.render();

      expect(table.checkableColumn).not.toBeUndefined();
      expect(table.checkableColumn).toBe(table.columns[0] as BooleanColumn);
    });

    it('displays the row.checked state as checkbox', () => {
      let model = helper.createModelFixture(2, 2);
      model.checkable = true;
      model.rows[0].checked = true;
      model.rows[1].checked = false;
      let table = helper.createTable(model);
      table.render();
      let $rows = table.$rows();
      let firstColumn = table.columns[0] as BooleanColumn;
      let $checkbox = firstColumn.$checkBox($rows.eq(0));
      expect($checkbox).toHaveClass('checked');
      $checkbox = firstColumn.$checkBox($rows.eq(1));
      expect($checkbox).not.toHaveClass('checked');
    });

  });

  describe('boolean column', () => {

    it('displays the cell value as checkbox', () => {
      let model = helper.createModelFixture(2, 2);
      model.columns[0].objectType = BooleanColumn;
      let cell0 = model.rows[0].cells[0] as Cell<boolean>;
      let cell1 = model.rows[1].cells[0] as Cell<boolean>;
      cell0.value = true;
      cell1.value = false;
      let table = helper.createTable(model);
      table.render();
      let $rows = table.$rows();
      let column = table.columns[0] as BooleanColumn;
      let $checkbox = column.$checkBox($rows.eq(0));
      expect($checkbox).toHaveClass('checked');
      $checkbox = column.$checkBox($rows.eq(1));
      expect($checkbox).not.toHaveClass('checked');
    });

    it('triggers rowClick event correctly', () => {
      let model = helper.createModelSingleColumnByValues([true, false], 'BooleanColumn');
      let table = helper.createTable(model);
      let column0 = table.columns[0];
      column0.setEditable(true);
      table.render();
      table.on('rowClick', event => {
        expect(event.row).toBeDefined();
        expect(event.row).toBe(model.rows[1] as TableRow);
        expect(event.mouseButton).toBe(1);
        expect(event.column).toBe(column0);
      });
      JQueryTesting.triggerClick(table.$cell(0, table.rows[1].$row));
    });

    describe('setCellValue', () => {
      it('rebuilds the cell', () => {
        let model = helper.createModelSingleColumnByValues([true], 'BooleanColumn');
        let table = helper.createTable(model);
        let column0 = table.columns[0];
        let updateRowCount = 0;
        table.render();

        expect(column0.cell(table.rows[0]).text).toBe('X');
        expect(column0.cell(table.rows[0]).value).toBe(true);
        expect(table.$cell(column0, table.rows[0].$row).children('.check-box')).toHaveClass('checked');

        table.on('rowsUpdated', event => updateRowCount++);
        column0.setCellValue(table.rows[0], false);
        expect(column0.cell(table.rows[0]).text).toBe('');
        expect(column0.cell(table.rows[0]).value).toBe(false);
        expect(table.$cell(column0, table.rows[0].$row).children('.check-box')).not.toHaveClass('checked');
        expect(updateRowCount).toBe(1);
      });
    });

    describe('triStateEnabled', () => {
      describe('onMouseUp', () => {
        it('toggles the check box if column is editable', () => {
          let model = helper.createModelSingleColumnByValues([true], 'BooleanColumn');
          let table = helper.createTable(model);
          let column0 = table.columns[0] as BooleanColumn;
          column0.setTriStateEnabled(true);
          column0.setEditable(true);
          let updateRowCount = 0;
          table.render();

          expect(column0.cell(table.rows[0]).text).toBe('X');
          expect(column0.cell(table.rows[0]).value).toBe(true);
          expect(table.$cell(column0, table.rows[0].$row).children('.check-box')).toHaveClass('checked');

          table.on('rowsUpdated', event => updateRowCount++);
          column0.onMouseUp({} as JQuery.MouseUpEvent, table.rows[0].$row);
          expect(column0.cell(table.rows[0]).text).toBe('?');
          expect(column0.cell(table.rows[0]).value).toBe(null);
          expect(table.$cell(column0, table.rows[0].$row).children('.check-box')).toHaveClass('undefined');
          expect(updateRowCount).toBe(1);

          column0.onMouseUp({} as JQuery.MouseUpEvent, table.rows[0].$row);
          expect(column0.cell(table.rows[0]).text).toBe('');
          expect(column0.cell(table.rows[0]).value).toBe(false);
          expect(table.$cell(column0, table.rows[0].$row).children('.check-box')).not.toHaveClass('undefined checked');
          expect(updateRowCount).toBe(2);

          column0.onMouseUp({} as JQuery.MouseUpEvent, table.rows[0].$row);
          expect(column0.cell(table.rows[0]).text).toBe('X');
          expect(column0.cell(table.rows[0]).value).toBe(true);
          expect(table.$cell(column0, table.rows[0].$row).children('.check-box')).not.toHaveClass('undefined');
          expect(table.$cell(column0, table.rows[0].$row).children('.check-box')).toHaveClass('checked');
          expect(updateRowCount).toBe(3);
        });
      });

      describe('cell edit', () => {
        it('updates the cell correctly', () => {
          let model = helper.createModelSingleColumnByValues([true], 'BooleanColumn');
          let table = helper.createTable(model);
          let column0 = table.columns[0] as BooleanColumn;
          column0.setTriStateEnabled(true);
          column0.setEditable(true);
          let updateRowCount = 0;
          table.render();

          table.on('rowsUpdated', event => updateRowCount++);
          table.prepareCellEdit(column0, table.rows[0]);
          jasmine.clock().tick(0);
          let field = table.cellEditorPopup.cell.field;
          field.setValue(null);
          expect(field.value).toBe(null);
          table.completeCellEdit();
          expect(column0.cell(table.rows[0]).text).toBe('?');
          expect(column0.cell(table.rows[0]).value).toBe(null);
          expect(table.$cell(column0, table.rows[0].$row).children('.check-box')).toHaveClass('undefined');
          expect(updateRowCount).toBe(1);

          table.prepareCellEdit(column0, table.rows[0]);
          jasmine.clock().tick(0);
          field = table.cellEditorPopup.cell.field;
          field.setValue(false);
          expect(field.value).toBe(false);
          table.completeCellEdit();
          expect(column0.cell(table.rows[0]).text).toBe('');
          expect(column0.cell(table.rows[0]).value).toBe(false);
          expect(table.$cell(column0, table.rows[0].$row).children('.check-box')).not.toHaveClass('undefined checked');
          expect(updateRowCount).toBe(2);
        });
      });
    });
  });

  describe('aria properties', () => {

    it('has cell content with aria role checkbox', () => {
      let model = helper.createModelFixture(2, 2);
      model.columns[0].objectType = BooleanColumn;
      let table = helper.createTable(model);
      table.render();
      let $rows = table.$rows();
      let column = table.columns[0] as BooleanColumn;
      let $checkbox = column.$checkBox($rows.eq(0));
      expect($checkbox).toHaveAttr('role', 'checkbox');
      $checkbox = column.$checkBox($rows.eq(1));
      expect($checkbox).toHaveAttr('role', 'checkbox');
    });

    it('renders aria-checked correctly', () => {
      let model = helper.createModelFixture(2, 2);
      model.columns[0].objectType = BooleanColumn;
      let cell0 = model.rows[0].cells[0] as Cell<boolean>;
      let cell1 = model.rows[1].cells[0] as Cell<boolean>;
      cell0.value = true;
      cell1.value = false;
      let table = helper.createTable(model);
      table.render();
      let $rows = table.$rows();
      let column = table.columns[0] as BooleanColumn;
      let $checkbox = column.$checkBox($rows.eq(0));
      expect($checkbox).toHaveAttr('aria-checked', 'true');
      $checkbox = column.$checkBox($rows.eq(1));
      expect($checkbox).toHaveAttr('aria-checked', 'false');
    });

    it('renders aria-checked correctly for tristate', () => {
      let model = helper.createModelSingleColumnByValues([true], 'BooleanColumn');
      let table = helper.createTable(model);
      let column0 = table.columns[0] as BooleanColumn;
      column0.setTriStateEnabled(true);
      column0.setEditable(true);
      table.render();

      expect(table.$cell(column0, table.rows[0].$row).children('.check-box')).toHaveAttr('aria-checked', 'true');

      column0.onMouseUp({} as JQuery.MouseUpEvent, table.rows[0].$row);
      expect(table.$cell(column0, table.rows[0].$row).children('.check-box')).toHaveAttr('aria-checked', 'mixed');

      column0.onMouseUp({} as JQuery.MouseUpEvent, table.rows[0].$row);
      expect(table.$cell(column0, table.rows[0].$row).children('.check-box')).toHaveAttr('aria-checked', 'false');
    });
  });
});
