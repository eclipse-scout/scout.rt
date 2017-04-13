/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("T2ableField", function() {
  var session;
  var helper;
  /** @type {scout.TableSpecHelper} */
  var tableHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    tableHelper = new scout.TableSpecHelper(session);
    helper = new scout.FormSpecHelper(session);
    $.fx.off = true;
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $.fx.off = false;
  });

  function createTableFieldWithTable() {
    var table = createTableModel(2, 2);
    return createTableField({table: table});
  }

  function createTableField(tableModel) {
    return helper.createField('TableField', session.desktop, tableModel);
  }

  function createTable(colCount, rowCount) {
    return tableHelper.createTable(createTableModel(colCount, rowCount));
  }

  function createTableModel(colCount, rowCount) {
    return tableHelper.createModelFixture(colCount, rowCount);
  }

  describe('property table', function() {

    it('shows (renders) the table if the value is set', function() {
      var table = createTable(2, 2);
      var tableField = createTableField();
      tableField.render(session.$entryPoint);

      expect(tableField.table).toBeUndefined();
      tableField.setProperty('table', table);
      expect(tableField.table.rendered).toBe(true);

      // Field is necessary for the FormFieldLayout
      expect(tableField.$field).toBeTruthy();
    });

    it('destroys the table if value is changed to null', function() {
      var tableField = createTableFieldWithTable();
      var table = tableField.table;
      tableField.render(session.$entryPoint);
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

    it('table gets class \'field\' to make it work with the form field layout', function() {
      var tableField = createTableFieldWithTable();
      tableField.render(session.$entryPoint);

      expect(tableField.table.$container).toHaveClass('field');
    });

    it('table gets class \'field\' to make it work with the form field layout (also when table is set later)', function() {
      var table = createTable(2, 2);
      var tableField = createTableField();
      tableField.render(session.$entryPoint);

      expect(tableField.table).toBeUndefined();
      tableField.setProperty('table', table);
      expect(tableField.table.$container).toHaveClass('field');
    });
  });

  describe('requiresSave', function() {

    var tableField, firstRow;

    beforeEach(function() {
      tableField = createTableFieldWithTable();
      firstRow = tableField.table.rows[0];
      expect(tableField.requiresSave).toBe(false);
    });

    it('should require save when row has been updated', function() {
      tableField.table.updateRow(firstRow);
      tableField.updateRequiresSave();
      expect(tableField.requiresSave).toBe(true);
    });

    it('should require save when row has been deleted', function() {
      tableField.table.deleteRow(firstRow);
      tableField.updateRequiresSave();
      expect(tableField.requiresSave).toBe(true);
    });

    it('should require save when row has been inserted', function() {
      var rowModel = tableHelper.createModelRow('new', ['foo', 'bar']);
      tableField.table.insertRow(rowModel);
      tableField.updateRequiresSave();
      expect(tableField.requiresSave).toBe(true);
    });

    it('should NOT require save when row has been inserted and deleted again', function() {
      var rowModel = tableHelper.createModelRow('new', ['foo', 'bar']);
      tableField.table.insertRow(rowModel);
      var insertedRow = tableField.table.rowsMap['new'];
      tableField.table.deleteRow(insertedRow);
      tableField.updateRequiresSave();
      expect(tableField.requiresSave).toBe(false);
    });

    it('should require save when row has been checked', function() {
      tableField.table.setProperty('checkable', true);
      tableField.table.checkRow(firstRow);
      tableField.updateRequiresSave();
      expect(tableField.requiresSave).toBe(true);
    });

    it('should NOT require save when row has been checked and unchecked again', function() {
      tableField.table.setProperty('checkable', true);
      tableField.table.checkRow(firstRow);
      tableField.table.uncheckRow(firstRow);
      tableField.updateRequiresSave();
      expect(tableField.requiresSave).toBe(false);
    });

  });
});
