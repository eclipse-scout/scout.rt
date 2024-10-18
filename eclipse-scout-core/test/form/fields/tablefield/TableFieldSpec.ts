/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, Status, Table, TableField, TableModel, TableRow} from '../../../../src/index';
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

  describe('getValidationResult', () => {
    let tableField: TableField;
    let table: Table;
    let column0: Column;
    let column1: Column;

    beforeEach(() => {
      tableField = createTableFieldWithTable();
      tableField.setLabel('Table Field');
      table = tableField.table;
      table.columns.forEach(c => c.setEditable(true));
      column0 = table.columns[0];
      column0.setText('First Col');
      column1 = table.columns[1];
      column1.setText('Second Col');
    });

    describe('valid', () => {
      it('is false if there are cell errors', () => {
        expect(tableField.getValidationResult().valid).toBe(true);

        column0.setCellErrorStatus(table.rows[0], Status.error('error'));
        expect(tableField.getValidationResult().valid).toBe(false);

        column0.setCellErrorStatus(table.rows[0], null);
        expect(tableField.getValidationResult().valid).toBe(true);
      });

      it('is false if mandatory cells are empty', () => {
        column0.setMandatory(true);
        let result = tableField.getValidationResult();
        expect(result.valid).toBe(true);
        expect(result.validByMandatory).toBe(true);

        column0.setCellValue(table.rows[0], null);
        result = tableField.getValidationResult();
        expect(result.valid).toBe(false);
        expect(result.validByMandatory).toBe(false);

        column0.setCellValue(table.rows[0], 'asdf');
        result = tableField.getValidationResult();
        expect(result.valid).toBe(true);
        expect(result.validByMandatory).toBe(true);
      });
    });

    describe('label', () => {
      it('lists all columns containing cell errors', () => {
        expect(tableField.getValidationResult().label).toBe('Table Field');

        column0.setCellErrorStatus(table.rows[0], Status.error('error'));
        expect(tableField.getValidationResult().label).toBe('Table Field: First Col');

        // Don't add column name twice
        column0.setCellErrorStatus(table.rows[1], Status.error('error'));
        expect(tableField.getValidationResult().label).toBe('Table Field: First Col');

        // Add name of second column
        column1.setCellErrorStatus(table.rows[0], Status.error('error'));
        expect(tableField.getValidationResult().label).toBe('Table Field: First Col, Second Col');

        // Field doesn't have a label
        tableField.setLabel(null);
        expect(tableField.getValidationResult().label).toBe('First Col, Second Col');

        // Columns don't have a label
        tableField.setLabel('Field');
        column0.setText(null);
        column1.setText(null);
        expect(tableField.getValidationResult().label).toBe('Field');
      });

      it('includes mandatory columns with missing values', () => {
        expect(tableField.getValidationResult().label).toBe('Table Field');

        column0.setMandatory(true);
        column0.setCellValue(table.rows[0], null);
        expect(tableField.getValidationResult().label).toBe('Table Field: First Col');

        // Invalid columns are included
        column1.setCellErrorStatus(table.rows[0], Status.error('error'));
        expect(tableField.getValidationResult().label).toBe('Table Field: First Col, Second Col');
      });
    });

    describe('errorStatus', () => {
      it('contains no message if multiple cells are invalid and have different messages', () => {
        column0.setCellErrorStatus(table.rows[0], Status.error('error'));
        expect(tableField.getValidationResult().errorStatus.message).toBe('error');

        // Another error with same message
        column1.setCellErrorStatus(table.rows[1], Status.error('error'));
        expect(tableField.getValidationResult().errorStatus.message).toBe('error');

        // Another error with a different message
        column1.setCellErrorStatus(table.rows[1], Status.error('another error'));
        expect(tableField.getValidationResult().errorStatus.message).toBe('');

        // Column 2 has no error but is mandatory without a value -> also remove message
        column1.setCellErrorStatus(table.rows[1], null);
        column1.setMandatory(true);
        column1.setCellValue(table.rows[0], null);
        expect(tableField.getValidationResult().errorStatus.message).toBe('');
      });

      it('is null if only mandatory cells are empty', () => {
        column0.setMandatory(true);
        let result = tableField.getValidationResult();
        expect(result.errorStatus).toBe(null);
        expect(result.validByMandatory).toBe(true);

        column0.setCellValue(table.rows[0], null);
        result = tableField.getValidationResult();
        expect(result.errorStatus).toBe(null);
        expect(result.validByMandatory).toBe(false);
      });

      it('uses the highest severity of all cell errors', () => {
        column0.setCellErrorStatus(table.rows[0], Status.warning('warning'));
        expect(tableField.getValidationResult().errorStatus.severity).toBe(Status.Severity.WARNING);

        // Another error with higher severity
        column1.setCellErrorStatus(table.rows[1], Status.error('error'));
        expect(tableField.getValidationResult().errorStatus.severity).toBe(Status.Severity.ERROR);

        // Error is removed, warning is shown again
        column1.setCellErrorStatus(table.rows[1], null);
        expect(tableField.getValidationResult().errorStatus.severity).toBe(Status.Severity.WARNING);
      });
    });
  });
});
