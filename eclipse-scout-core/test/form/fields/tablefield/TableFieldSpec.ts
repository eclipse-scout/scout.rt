/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {TableField, TableModel, TableRow} from '../../../../src/index';
import {FormSpecHelper, SpecTable, TableModelWithCells, TableSpecHelper} from '../../../../src/testing/index';

describe('TableField', () => {
  let session: SandboxSession;
  let helper: FormSpecHelper;
  let tableHelper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    tableHelper = new TableSpecHelper(session);
    helper = new FormSpecHelper(session);
    $.fx.off = true;
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $.fx.off = false;
  });

  function createTableFieldWithTable(): TableField {
    let table = createTableModel(2, 2);
    return createTableField({table: table});
  }

  function createTableField(tableModel?: TableModel): TableField {
    return helper.createField(TableField, session.desktop, tableModel);
  }

  function createTable(colCount: number, rowCount: number): SpecTable {
    return tableHelper.createTable(createTableModel(colCount, rowCount));
  }

  function createTableModel(colCount: number, rowCount: number): TableModelWithCells {
    return tableHelper.createModelFixture(colCount, rowCount);
  }

  describe('property table', () => {

    it('shows (renders) the table if the value is set', () => {
      let table = createTable(2, 2);
      let tableField = createTableField();
      tableField.render();

      expect(tableField.table).toBeNull();
      tableField.setTable(table);
      expect(tableField.table.rendered).toBe(true);

      // Field is necessary for the FormFieldLayout
      expect(tableField.$field).toBeTruthy();
    });

    it('destroys the table if value is changed to null', () => {
      let tableField = createTableFieldWithTable();
      let table = tableField.table;
      tableField.render();
      expect(table.rendered).toBe(true);
      expect(table.owner).toBe(tableField);
      expect(table.parent).toBe(tableField);

      tableField.setTable(null);
      expect(tableField.table).toBeFalsy();
      expect(tableField.$field).toBeFalsy();
      expect(table.rendered).toBe(false);
      expect(table.destroyed).toBe(true);
      expect(session.getModelAdapter(table.id)).toBeFalsy();
    });

    it('table gets class \'field\' to make it work with the form field layout', () => {
      let tableField = createTableFieldWithTable();
      tableField.render();

      expect(tableField.table.$container).toHaveClass('field');
    });

    it('table gets class \'field\' to make it work with the form field layout (also when table is set later)', () => {
      let table = createTable(2, 2);
      let tableField = createTableField();
      tableField.render();

      expect(tableField.table).toBeNull();
      tableField.setTable(table);
      expect(tableField.table.$container).toHaveClass('field');
    });
  });

  describe('saveNeeded', () => {

    let tableField, firstRow;

    beforeEach(() => {
      tableField = createTableFieldWithTable();
      firstRow = tableField.table.rows[0];
      expect(tableField.saveNeeded).toBe(false);
    });

    it('is set to true when row has been updated', () => {
      tableField.table.updateRow(firstRow);
      expect(tableField.saveNeeded).toBe(false); // <-- no change yet (because value was not changed)

      tableField.table.columns[0].setCellValue(firstRow, 77);
      expect(tableField.saveNeeded).toBe(true);
    });

    it('does not create a memory leak if same row is updated multiple times', () => {
      tableField.table.columns[0].setCellValue(firstRow, 77);
      tableField.table.updateRow(firstRow);
      expect(Object.keys(tableField._updatedRows).length).toBe(1);

      tableField.table.columns[0].setCellValue(firstRow, 88);
      tableField.table.updateRow(firstRow);
      expect(Object.keys(tableField._updatedRows).length).toBe(1);
    });

    it('is set to true when row has been deleted', () => {
      tableField.table.deleteRow(firstRow);
      expect(tableField.saveNeeded).toBe(true);
    });

    it('is set to true when row has been inserted', () => {
      let rowModel = tableHelper.createModelRow('new', ['foo', 'bar']);
      tableField.table.insertRow(rowModel);
      expect(tableField.saveNeeded).toBe(true);
    });

    it('is NOT set to true when row has been inserted and deleted again', () => {
      let rowModel = tableHelper.createModelRow('new', ['foo', 'bar']);
      tableField.table.insertRow(rowModel);
      let insertedRow = tableField.table.rowsMap['new'];
      tableField.table.deleteRow(insertedRow);
      expect(tableField.saveNeeded).toBe(false);
    });

    it('is NOT set to true when row has been inserted and deleted again even if it was updated or checked in the meantime', () => {
      let rowModel = tableHelper.createModelRow('new', ['foo', 'bar']);
      tableField.table.insertRow(rowModel);
      let insertedRow = tableField.table.rowsMap['new'];
      tableField.table.updateRow(insertedRow);
      tableField.table.checkRow(insertedRow);
      tableField.table.deleteRow(insertedRow);
      expect(tableField.saveNeeded).toBe(false);
    });

    it('is set to true when row has been checked', () => {
      tableField.table.setProperty('checkable', true);
      tableField.table.checkRow(firstRow);
      expect(tableField.saveNeeded).toBe(true);
    });

    it('is NOT set to true when row has been checked and unchecked again', () => {
      tableField.table.setProperty('checkable', true);
      tableField.table.checkRow(firstRow);
      tableField.table.uncheckRow(firstRow);
      expect(tableField.saveNeeded).toBe(false);
    });

    it('is set to true after a cell edit', () => {
      tableField.render();
      tableField.table.columns[0].setEditable(true);
      tableField.markAsSaved();
      expect(tableField.saveNeeded).toBe(false);
      tableField.table.prepareCellEdit(tableField.table.columns[0], tableField.table.rows[0]);
      jasmine.clock().tick(0);
      tableField.table.cellEditorPopup.cell.field.setValue('my new value');
      tableField.table.completeCellEdit();
      expect(tableField.saveNeeded).toBe(true);
    });

    it('is NOT set to true when open and close cell editor without any text change.', () => {
      tableField.render();
      tableField.table.columns[0].setEditable(true);
      tableField.markAsSaved();
      expect(tableField.saveNeeded).toBe(false);
      tableField.table.prepareCellEdit(tableField.table.columns[0], tableField.table.rows[0]);
      jasmine.clock().tick(0);
      tableField.table.completeCellEdit();
      expect(tableField.saveNeeded).toBe(false);
    });

    it('resets row status on markAsSaved', () => {
      expect(firstRow.status).toBe(TableRow.Status.NON_CHANGED);
      expect(tableField.saveNeeded).toBe(false);

      tableField.table.columns[0].setCellValue(firstRow, 77);
      expect(firstRow.status).toBe(TableRow.Status.UPDATED);
      expect(tableField.saveNeeded).toBe(true);

      tableField.markAsSaved();
      expect(firstRow.status).toBe(TableRow.Status.NON_CHANGED);
      expect(tableField.saveNeeded).toBe(false);

      tableField.table.insertRow({
        cells: [null, null]
      });
      let lastRow = tableField.table.rows[tableField.table.rows.length - 1];
      expect(lastRow.status).toBe(TableRow.Status.INSERTED);
      expect(tableField.saveNeeded).toBe(true);

      tableField.markAsSaved();
      expect(lastRow.status).toBe(TableRow.Status.NON_CHANGED);
      expect(tableField.saveNeeded).toBe(false);
    });
  });
});
