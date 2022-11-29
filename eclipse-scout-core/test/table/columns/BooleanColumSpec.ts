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
import {BooleanColumn} from '../../../src/index';
import {TableSpecHelper} from '../../../src/testing/index';
import {triggerClick} from '../../../src/testing/jquery-testing';

describe('BooleanColumn', () => {
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
      expect(table.checkableColumn).toBe(table.columns[0]);
    });

    it('displays the row.checked state as checkbox', () => {
      let model = helper.createModelFixture(2, 2);
      model.checkable = true;
      model.rows[0].checked = true;
      model.rows[1].checked = false;
      let table = helper.createTable(model);
      table.render();
      let $rows = table.$rows();
      let $checkbox = table.columns[0].$checkBox($rows.eq(0));
      expect($checkbox).toHaveClass('checked');
      $checkbox = table.columns[0].$checkBox($rows.eq(1));
      expect($checkbox).not.toHaveClass('checked');
    });

  });

  describe('boolean column', () => {

    it('displays the cell value as checkbox', () => {
      let model = helper.createModelFixture(2, 2);
      model.columns[0].objectType = BooleanColumn;
      model.rows[0].cells[0].value = true;
      model.rows[1].cells[0].value = false;
      let table = helper.createTable(model);
      table.render();
      let $rows = table.$rows();
      let $checkbox = table.columns[0].$checkBox($rows.eq(0));
      expect($checkbox).toHaveClass('checked');
      $checkbox = table.columns[0].$checkBox($rows.eq(1));
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
        expect(event.row).toBe(model.rows[1]);
        expect(event.mouseButton).toBe(1);
        expect(event.column).toBe(column0);
      });
      triggerClick(table.$cell(0, table.rows[1].$row));
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
          let column0 = table.columns[0];
          column0.setTriStateEnabled(true);
          column0.setEditable(true);
          let updateRowCount = 0;
          table.render();

          expect(column0.cell(table.rows[0]).text).toBe('X');
          expect(column0.cell(table.rows[0]).value).toBe(true);
          expect(table.$cell(column0, table.rows[0].$row).children('.check-box')).toHaveClass('checked');

          table.on('rowsUpdated', event => updateRowCount++);
          column0.onMouseUp({}, table.rows[0].$row);
          expect(column0.cell(table.rows[0]).text).toBe('?');
          expect(column0.cell(table.rows[0]).value).toBe(null);
          expect(table.$cell(column0, table.rows[0].$row).children('.check-box')).toHaveClass('undefined');
          expect(updateRowCount).toBe(1);

          column0.onMouseUp({}, table.rows[0].$row);
          expect(column0.cell(table.rows[0]).text).toBe('');
          expect(column0.cell(table.rows[0]).value).toBe(false);
          expect(table.$cell(column0, table.rows[0].$row).children('.check-box')).not.toHaveClass('undefined checked');
          expect(updateRowCount).toBe(2);

          column0.onMouseUp({}, table.rows[0].$row);
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
          let column0 = table.columns[0];
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
});
