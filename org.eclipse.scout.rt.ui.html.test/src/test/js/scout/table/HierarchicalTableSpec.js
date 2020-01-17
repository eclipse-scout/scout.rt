/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("HierarchicalTableSpec", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.TableSpecHelper(session);
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

  function createColumnFilterModel(columnId, selectedValues) {
    return {
      objectType: 'TextColumnUserFilter',
      column: columnId,
      selectedValues: selectedValues
    };
  }

  function createAndRegisterColumnFilter(table, column, selectedValues) {
    return helper.createAndRegisterColumnFilter({
      table: table,
      session: session,
      column: column,
      selectedValues: selectedValues
    }, session);
  }

  function expectRowIds(rows, ids) {
    expect(rows.map(function(row) {
      return row.id;
    })).toEqual(ids);
  }

  function getTreeRows(table) {
    var flatTreeRows = [];
    table.visitRows(function(row) {
      flatTreeRows.push(row);
    }.bind(this));
    return flatTreeRows;
  }

  function getUiRows(table) {
    return $.makeArray(table.$rows()).map(function(row) {
      return $(row).data('row');
    });
  }

  function getExpandedRows(table) {
    var expandedRows = [];
    table.visitRows(function(row) {
      if (row.expanded && row.childRows.length > 0) {
        expandedRows.push(row);
      }
    }.bind(this));
    return expandedRows;
  }

  describe("add", function() {
    var table, rowIds, rows;

    /**
     * initial table
     * -------------
     * 0
     * |-- 1
     * 2
     **/
    beforeEach(function() {
      rowIds = [0, 1, 2];
      rows = rowIds.map(function(id) {
        var rowData = helper.createModelRow(id, ['row' + id]);
        rowData.expanded = true;
        return rowData;
      });
      var model = helper.createModel(helper.createModelColumns(1), rows);
      table = helper.createTable(model),
        rows = table.rows;
      rows[1].parentRow = rows[0].id;
      table.updateRows(rows);
      table.render();
    });

    it("a root row", function() {
      table.insertRow(helper.createModelRow(33, ['newRow']));
      expect(table.rows.length).toBe(4);
      expect(table._filteredRows.length).toBe(4);
      expect(table.visibleRows.length).toBe(4);
      expect(table.rootRows.length).toBe(3);
      expect(table.rootRows[2].id).toBe(33);

      var expectedRowIds = rowIds.slice();
      expectedRowIds.splice(3, 0, 33);
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });

    it("a child row to a row which is already a parent row (by id)", function() {
      var newRow = helper.createModelRow(33, ['newRow']),
        parentRow = table.rows[0];
      newRow.parentRow = parentRow.id;
      table.insertRow(newRow);
      expect(table.rows.length).toBe(4);
      expect(table._filteredRows.length).toBe(4);
      expect(table.visibleRows.length).toBe(4);
      expect(table.rootRows.length).toBe(2);
      expect(table.rowsMap[parentRow.id].childRows.length).toBe(2);
      expect(table.rowsMap[33].parentRow).toBe(parentRow);

      var expectedRowIds = rowIds.slice();
      expectedRowIds.splice(2, 0, 33);
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);

    });

    it("a child row to a row which is already a parent row (by TableRow)", function() {
      var newRow = helper.createModelRow(33, ['newRow']),
        pseudoParentRow = {
          id: '0'
        };
      newRow.parentRow = pseudoParentRow;
      table.insertRow(newRow);
      expect(table.rows.length).toBe(4);
      expect(table._filteredRows.length).toBe(4);
      expect(table.visibleRows.length).toBe(4);
      expect(table.rootRows.length).toBe(2);
      expect(table.rowsMap[pseudoParentRow.id].childRows.length).toBe(2);
      expect(table.rowsMap[33].parentRow).toBe(table.rows[0]);

      var expectedRowIds = rowIds.slice();
      expectedRowIds.splice(2, 0, 33);
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });

    it("a child row to a row which is already a parent row (by pseudo row)", function() {
      var newRow = helper.createModelRow(33, ['newRow']),
        parentRow = table.rows[0];

      newRow.parentRow = parentRow;
      table.insertRow(newRow);
      expect(table.rows.length).toBe(4);
      expect(table._filteredRows.length).toBe(4);
      expect(table.visibleRows.length).toBe(4);
      expect(table.rootRows.length).toBe(2);
      expect(table.rowsMap[parentRow.id].childRows.length).toBe(2);
      expect(table.rowsMap[33].parentRow).toBe(table.rows[0]);

      var expectedRowIds = rowIds.slice();
      expectedRowIds.splice(2, 0, 33);
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });

    it("a child row to a row which is leaf", function() {
      var newRow = helper.createModelRow(33, ['newRow']),
        parentRow = table.rows[2];
      newRow.parentRow = parentRow.id;
      table.insertRow(newRow);
      expect(table.rows.length).toBe(4);
      expect(table._filteredRows.length).toBe(4);
      expect(table.visibleRows.length).toBe(4);
      expect(table.rootRows.length).toBe(2);
      expect(table.rowsMap[parentRow.id].childRows.length).toBe(1);
      expect(table.rowsMap[33].parentRow).toBe(parentRow);

      var expectedRowIds = rowIds.slice();
      expectedRowIds.splice(3, 0, 33);
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });

    it("a child row to a collapsed row", function() {
      var newRow = helper.createModelRow(33, ['newRow']),
        parentRow = table.rows[0];
      newRow.parentRow = parentRow.id;
      table.collapseRow(parentRow);
      table.insertRow(newRow);

      expect(table.rowsMap[parentRow.id].childRows.length).toBe(2);
      expect(table.rowsMap[33].parentRow).toBe(parentRow);

      expectRowIds(table.rows, [0, 1, 33, 2]);
      expectRowIds(getTreeRows(table), [0, 1, 33, 2]);
      expectRowIds(table._filteredRows, [0, 1, 33, 2]);
      expectRowIds(table.visibleRows, [0, 2]);
      expectRowIds(getUiRows(table), [0, 2]);
    });
  });

  describe("delete", function() {
    var table, rows, rowIds;
    /**
     * initial table
     * -------------
     * 0
     * |-- 1
     *     |-- 2
     * 3
     * |-- 4
     * |-- 5
     * 6
     **/
    beforeEach(function() {
      rowIds = [0, 1, 2, 3, 4, 5, 6];
      rows = rowIds.map(function(id) {
        var rowData = helper.createModelRow(id, ['row' + id]);
        rowData.expanded = true;
        return rowData;
      });
      var model = helper.createModel(helper.createModelColumns(1), rows);
      table = helper.createTable(model),
        rows = table.rows;
      rows[1].parentRow = rows[0].id;
      rows[2].parentRow = rows[1].id;
      rows[4].parentRow = rows[3].id;
      rows[5].parentRow = rows[3].id;
      table.updateRows(rows);
      table.render();
    });

    it("leaf row and expect the row structure to be valid", function() {
      table.deleteRow(rows[5]);
      expect(table.visibleRows.indexOf(rows[5])).toBe(-1);
      expect(rows[3].childRows.length).toBe(1);

      var expectedRowIds = rowIds.slice();
      expectedRowIds.splice(5, 1);
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });

    it("leaf row with collapsed parent and expect the structure to be valid", function() {
      table.collapseRow(rows[3]);
      expect(table.visibleRows.length).toBe(5);
      table.deleteRow(rows[5]);
      expect(rows[3].expanded).toBeFalsy();
      expect(table.visibleRows.indexOf(rows[5])).toBe(-1);
      expect(rows[3].childRows.length).toBe(1);

      // structure
      var expectedRowIds = rowIds.slice();
      expectedRowIds.splice(5, 1);
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectedRowIds.splice(4, 1);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });

    it("a parent row and expect all children are deleted cascading.", function() {
      table.deleteRow(rows[3]);
      expect(table.visibleRows.indexOf(rows[3])).toBe(-1);
      expect(table.visibleRows.indexOf(rows[4])).toBe(-1);
      expect(table.visibleRows.indexOf(rows[5])).toBe(-1);

      // structure
      var expectedRowIds = rowIds.slice();
      expectedRowIds.splice(3, 3);
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });
  });

  describe("structure", function() {
    var table, rows, rowIds;
    /**
     * initial table
     * -------------
     * 0
     * |-- 1
     *     |-- 2
     * 3
     **/
    beforeEach(function() {
      rowIds = [0, 1, 2, 3];
      rows = rowIds.map(function(id) {
        var rowData = helper.createModelRow(id, ['row' + id]);
        rowData.expanded = true;
        return rowData;
      });
      var model = helper.createModel(helper.createModelColumns(1), rows);
      table = helper.createTable(model),
        rows = table.rows;
      rows[1].parentRow = rows[0].id;
      rows[2].parentRow = rows[1].id;
      table.updateRows(rows);
      table.render();
    });

    it("is updated when insert a new child row", function() {
      var row = helper.createModelRow(33, ['newRow']),
        spyRenderViewPort = spyOn(table, '_renderViewport').and.callThrough();

      row.parentRow = rows[0].id;
      table.insertRow(row);
      row = table._rowById(33);
      expect(table._renderViewport.calls.count()).toBe(2);

      expectRowIds(table.visibleRows, [0, 1, 2, 33, 3]);
      expectRowIds(getExpandedRows(table), [0, 1]);
      expect(rows[0].childRows.length).toBe(2);
      expect(rows[0].childRows[1]).toBe(row);
      expect(row.parentRow).toBe(rows[0]);
    });

    it("is updated when deleting a child row", function() {
      var expectedRowIds = [0, 1, 3];
      table.deleteRow(rows[2]);
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });

    it("is updated when deleting a row and its children", function() {
      // cascade deleted row
      var childRowToBeDeleted = rows[2],
        expectedRowIds = [0, 3];
      table.deleteRow(rows[1]);

      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getUiRows(table), expectedRowIds);
    });

  });

  describe("expanded rows", function() {
    var table, rowIds, rows;
    /**
     * initial table
     * -------------
     * 0
     * |-- 1
     *     |-- 2
     * 3
     **/
    beforeEach(function() {
      rowIds = [0, 1, 2, 3];
      rows = rowIds.map(function(id) {
        var rowData = helper.createModelRow(id, ['row' + id]);
        rowData.expanded = true;
        return rowData;
      });
      var model = helper.createModel(helper.createModelColumns(1), rows);
      table = helper.createTable(model),
        rows = table.rows;
      rows[1].parentRow = rows[0].id;
      rows[2].parentRow = rows[1].id;
      table.updateRows(rows);
      table.render();
    });

    it("are valid after expand parent and its child row and expand parent again.", function() {
      var expectedRowIds = [0, 1, 2, 3];
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getExpandedRows(table), [0, 1]);
      expectRowIds(getUiRows(table), expectedRowIds);

      table.collapseRow(rows[1]);

      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, [0, 1, 3]);
      expectRowIds(getExpandedRows(table), [0]);
      expectRowIds(getUiRows(table), [0, 1, 3]);

      table.collapseRow(rows[0]);

      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, [0, 3]);
      expectRowIds(getExpandedRows(table), []);
      expectRowIds(getUiRows(table), [0, 3]);

      table.expandRow(rows[0]);

      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, [0, 1, 3]);
      expectRowIds(getExpandedRows(table), [0]);
      expectRowIds(getUiRows(table), [0, 1, 3]);

    });

    it("are valid after expand all and collapse all.", function() {

      var expectedRowIds = [0, 1, 2, 3];
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getExpandedRows(table), [0, 1]);
      expectRowIds(getUiRows(table), expectedRowIds);

      table.collapseAll();

      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, [0, 3]);
      expectRowIds(getExpandedRows(table), []);
      expectRowIds(getUiRows(table), [0, 3]);

      table.expandAll();
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getExpandedRows(table), [0, 1]);
      expectRowIds(getUiRows(table), expectedRowIds);

    });

  });

  describe("selection", function() {
    var table, rowIds, rows;
    /**
     * initial table
     * -------------
     * 0
     * |-- 1
     *     |-- 2
     * 3
     * |-- 4
     * |-- 5
     * 6
     **/
    beforeEach(function() {
      rowIds = [0, 1, 2, 3, 4, 5, 6];
      rows = rowIds.map(function(id) {
        var rowData = helper.createModelRow(id, ['row' + id]);
        rowData.expanded = true;
        return rowData;
      });
      var model = helper.createModel(helper.createModelColumns(1), rows);
      table = helper.createTable(model),
        rows = table.rows;
      rows[1].parentRow = rows[0].id;
      rows[2].parentRow = rows[1].id;
      rows[4].parentRow = rows[3].id;
      rows[5].parentRow = rows[3].id;
      table.updateRows(rows);
      table.render();
    });

    it("of all rows is valid if parent rows do not match a filter condition", function() {
      // expects 1 row and its parents are visible
      createAndRegisterColumnFilter(table, table.columns[0], ['row2']);
      table.filter();
      var expectedRowIds = [0, 1, 2];
      expectRowIds(table.visibleRows, expectedRowIds);
      expect(scout.arrays.equalsIgnoreOrder(expectedRowIds, Object.keys(table.visibleRowsMap).map(function(n) {
        return Number(n);
      }))).toBe(true);
      table.selectAll();
      expectRowIds(table.selectedRows, expectedRowIds);
    });

    it("a single row matching the filter", function() {
      // expects 1 row and its parents are visible
      createAndRegisterColumnFilter(table, table.columns[0], ['row4']);
      table.filter();
      var expectedRowIds = [3, 4];
      expectRowIds(table.visibleRows, expectedRowIds);
      table.selectRow(table.rows[4]);
      expectRowIds(table.selectedRows, [4]);
    });

    it("a single row which is a parent row of a row matching the filter", function() {
      // expects 1 row and its parents are visible
      createAndRegisterColumnFilter(table, table.columns[0], ['row4']);
      table.filter();
      var expectedRowIds = [3, 4];
      expectRowIds(table.visibleRows, expectedRowIds);
      table.selectRow(table.rows[3]);
      expectRowIds(table.selectedRows, [3]);
    });

    it("of a not visible row due to a filter", function() {
      // expects 1 row and its parents are visible
      createAndRegisterColumnFilter(table, table.columns[0], ['row4']);
      table.filter();
      var expectedRowIds = [3, 4];
      expectRowIds(table.visibleRows, expectedRowIds);
      table.selectRow(table.rows[2]);
      expectRowIds(table.selectedRows, []);
    });

    it("changes when selected rows gets invisible due to collapse of a parent row.", function() {
      var initialSelection = [rows[2], rows[5]];
      table.selectRows(initialSelection);
      expect(table.selectedRows).toEqual(initialSelection);

      table.collapseRow(rows[3]);
      expect(table.selectedRows).toEqual([rows[2]]);

      table.collapseRow(rows[0]);
      expect(table.selectedRows).toEqual([]);

      table.expandRow(rows[0]);
      table.collapseRow(rows[3]);
      expect(table.selectedRows).toEqual([]);
    });

    it("of a row is still the same if the row gets collapsed. ", function() {
      var initialSelection = [rows[1], rows[2]];
      table.selectRows(initialSelection);
      expect(table.selectedRows).toEqual(initialSelection);

      table.collapseRow(rows[1]);
      expect(table.selectedRows).toEqual([rows[1]]);

      table.expandRow(rows[1]);
      expect(table.selectedRows).toEqual([rows[1]]);
    });

    it("is still the same after inserting rows", function() {
      var initialSelection = [rows[1], rows[2]];
      table.selectRows(initialSelection);
      expect(table.selectedRows).toEqual(initialSelection);

      var newRow01 = helper.createModelRow(undefined, ['newRow01']);
      newRow01.parentRow = rows[1].id;
      table.insertRow(newRow01);

      expect(table.selectedRows).toEqual(initialSelection);
    });

    it("is still the same after deleting an not selected row", function() {
      var initialSelection = [rows[1], rows[2]];
      table.selectRows(initialSelection);
      expect(table.selectedRows).toEqual(initialSelection);
      expect(table.visibleRows.length).toBe(7);

      // delete a leaf
      table.deleteRow(rows[4]);
      expect(table.selectedRows).toEqual(initialSelection);
      expect(table.visibleRows.length).toBe(6);

      //delete a parent
      table.deleteRow(rows[3]);
      expect(table.selectedRows).toEqual(initialSelection);
      expect(table.visibleRows.length).toBe(4);
    });

    it("gets adjusted when deleting a selected row", function() {
      var initialSelection = [rows[2], rows[4]];
      table.selectRows(initialSelection);
      expect(table.selectedRows).toEqual(initialSelection);
      expect(table.visibleRows.length).toBe(7);

      // delete a leaf
      table.deleteRow(rows[2]);
      expect(table.selectedRows).toEqual([rows[4]]);
      expect(table.visibleRows.length).toBe(6);

      //delete a parent
      table.deleteRow(rows[3]);
      expect(table.selectedRows).toEqual([]);
      expect(table.visibleRows.length).toBe(3);
    });

  });

  describe("update row", function() {
    var table, rowIds, rows;
    /**
     * initial table
     * -------------
     * 0
     * |-- 1
     *     |-- 2
     * 3
     **/
    beforeEach(function() {
      rowIds = [0, 1, 2, 3];
      rows = rowIds.map(function(id) {
        var rowData = helper.createModelRow(id, ['row' + id]);
        rowData.expanded = true;
        return rowData;
      });
      var model = helper.createModel(helper.createModelColumns(1), rows);
      table = helper.createTable(model),
        rows = table.rows;
      // create hierarchie
      rows[1].parentRow = rows[0].id;
      rows[2].parentRow = rows[1].id;
      table.updateRows(rows);
      table.render();
    });

    it("by changing the parent key", function() {
      table.rows[1].parentRow = table.rows[3];
      table.updateRow(table.rows[1]);

      // expectations
      var expectedRowIds = [0, 3, 1, 2];
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getExpandedRows(table), [3, 1]);
      expectRowIds(getUiRows(table), expectedRowIds);
      expectRowIds(table.rootRows, [0, 3]);
    });

    it("by removing the parent key", function() {
      table.rows[1].parentRow = null;
      table.updateRow(table.rows[1]);

      // expectations
      var expectedRowIds = [0, 1, 2, 3];
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getExpandedRows(table), [1]);
      expectRowIds(getUiRows(table), expectedRowIds);
      expectRowIds(table.rootRows, [0, 1, 3]);
    });

    it("by adding the parent key", function() {
      table.rows[3].parentRow = table.rows[2];
      table.updateRow(table.rows[1]);

      // expectations
      var expectedRowIds = [0, 1, 2, 3];
      expectRowIds(table.rows, expectedRowIds);
      expectRowIds(getTreeRows(table), expectedRowIds);
      expectRowIds(table._filteredRows, expectedRowIds);
      expectRowIds(table.visibleRows, expectedRowIds);
      expectRowIds(getExpandedRows(table), [0, 1, 2]);
      expectRowIds(getUiRows(table), expectedRowIds);
      expectRowIds(table.rootRows, [0]);
    });

    it('applies expanded change correctly', function() {
      var rowIds = [0, 1, 2, 3];
      expectRowIds(table.rows, rowIds);
      expectRowIds(table.visibleRows, rowIds);
      expectRowIds(getExpandedRows(table), [0, 1]);
      expectRowIds(getUiRows(table), [0, 1, 2, 3]);

      var row0 = {
        id: table.rows[0].id,
        expanded: false,
        cells: ['newRow0Cell0']
      };
      table.updateRow(row0);

      expectRowIds(table.rows, rowIds);
      expectRowIds(table.visibleRows, [0, 3]);
      expectRowIds(getExpandedRows(table), [1]);
      expectRowIds(getUiRows(table), [0, 3]);
    });
  });

  describe("filtered visible", function() {
    var table, rowIds, rows;
    /**
     * initial table
     * -------------
     * 0
     * |-- 1
     *     |-- 2
     * 3
     **/
    beforeEach(function() {
      rowIds = [0, 1, 2, 3];
      rows = rowIds.map(function(id) {
        var rowData = helper.createModelRow(id, ['row' + id]);
        rowData.expanded = true;
        return rowData;
      });
      var model = helper.createModel(helper.createModelColumns(1), rows);
      table = helper.createTable(model),
        rows = table.rows;
      // create hierarchie
      rows[1].parentRow = rows[0].id;
      rows[2].parentRow = rows[1].id;
      table.updateRows(rows);
      table.render();
    });

    it("rows are correct after when a child row matches a filter in of a collapsed parent row.", function() {
      var column0 = table.columns[0];

      // expects 1 row and its parents are visible
      createAndRegisterColumnFilter(table, column0, ['row2']);
      table.filter();
      expect(table.getVisibleRows().length).toBe(3);

      table.collapseRow(rows[1]);

      // expect the collapsed row is visible even when it does not match to the filter
      expect(table.getVisibleRows().length).toBe(2);
      expect(table.getVisibleRows()[0]).toBe(rows[0]);
      expect(table.getVisibleRows()[1]).toBe(rows[1]);

      table.expandRow(rows[1]);
      table.collapseRow(rows[0]);
      expect(table.getVisibleRows().length).toBe(1);
      expect(table.getVisibleRows()[0]).toBe(rows[0]);
    });

  });

  describe("move", function() {
    var table, rowIds, rows;
    /**
     * initial table
     * -------------
     * 0
     * 1
     * |-- 2
     * |-- 3
     * 4
     * 5
     **/
    beforeEach(function() {
      rowIds = [0, 1, 2, 3, 4, 5];
      rows = rowIds.map(function(id) {
        var rowData = helper.createModelRow(id, ['row' + id]);
        rowData.expanded = true;
        return rowData;
      });
      var model = helper.createModel(helper.createModelColumns(1), rows);
      table = helper.createTable(model),
        rows = table.rows;
      // create hierarchie

      rows[2].parentRow = rows[1].id;
      rows[3].parentRow = rows[1].id;
      table.updateRows(rows);
      table.render();
    });

    it("row down and expect to be moved after the next sibling on the same level.", function() {
      expectRowIds(table.rows, [0, 1, 2, 3, 4, 5]);
      table.moveRowDown(rows[0]);
      expectRowIds(table.rows, [1, 2, 3, 0, 4, 5]);
    });

    it("row up and expect to be moved before the next sibling on the same level.", function() {
      expectRowIds(table.rows, [0, 1, 2, 3, 4, 5]);
      table.moveRowUp(rows[4]);
      expectRowIds(table.rows, [0, 4, 1, 2, 3, 5]);
    });

    it("child row down and expect it will not be moved away of its siblings.", function() {
      expectRowIds(table.rows, [0, 1, 2, 3, 4, 5]);
      table.moveRowDown(rows[2]);
      expectRowIds(table.rows, [0, 1, 3, 2, 4, 5]);
      // expect to not move away of its parent
      table.moveRowDown(rows[2]);
      expectRowIds(table.rows, [0, 1, 3, 2, 4, 5]);
    });

    it("child row up and expect it will not be moved away of its siblings.", function() {
      expectRowIds(table.rows, [0, 1, 2, 3, 4, 5]);
      table.moveRowUp(rows[3]);
      expectRowIds(table.rows, [0, 1, 3, 2, 4, 5]);
      // expect to not move away of its parent
      table.moveRowUp(rows[3]);
      expectRowIds(table.rows, [0, 1, 3, 2, 4, 5]);
    });

  });

  describe("move visible row", function() {
    var table, rowIds, rows;
    /**
     * initial table
     * -------------
     * 0 (b)
     * 1 (a)
     * 2 (b)
     * |-- 3 (a)
     * |-- 4 (b)
     * 5 (b)
     * 6 (a)
     * 7 (b)
     **/
    beforeEach(function() {
      rowIds = [0, 1, 2, 3, 4, 5, 6, 7];
      var rowTexts = ['b', 'a', 'b', 'a', 'b', 'b', 'a', 'b'];
      rows = rowIds.map(function(id) {
        var rowData = helper.createModelRow(id, [rowTexts[id]]);
        rowData.expanded = true;
        return rowData;
      }, this);
      var model = helper.createModel(helper.createModelColumns(1), rows);
      table = helper.createTable(model),
        rows = table.rows;
      // create hierarchie

      rows[3].parentRow = rows[2].id;
      rows[4].parentRow = rows[2].id;
      table.updateRows(rows);
      table.render();
    });

    it("up - expect the row gets moved above the previous visible row", function() {
      createAndRegisterColumnFilter(table, table.columns[0], ['a']);
      table.filter();
      expectRowIds(table.visibleRows, [1, 2, 3, 6]);
      table.moveVisibleRowUp(rows[6]);
      expectRowIds(table.rows, [0, 1, 6, 2, 3, 4, 5, 7]);
      expectRowIds(table.visibleRows, [1, 6, 2, 3]);

      // once again expect to be moved before 1
      table.moveVisibleRowUp(rows[6]);
      expectRowIds(table.rows, [0, 6, 1, 2, 3, 4, 5, 7]);
      expectRowIds(table.visibleRows, [6, 1, 2, 3]);

      // once again expect to move nothing since node 0 is invisible
      table.moveVisibleRowUp(rows[6]);
      expectRowIds(table.rows, [0, 6, 1, 2, 3, 4, 5, 7]);
      expectRowIds(table.visibleRows, [6, 1, 2, 3]);

    });

    it("down - expect the row gets moved below the next visible row", function() {
      createAndRegisterColumnFilter(table, table.columns[0], ['a']);
      table.filter();
      expectRowIds(table.visibleRows, [1, 2, 3, 6]);

      table.moveVisibleRowDown(rows[1]);
      expectRowIds(table.rows, [0, 2, 3, 4, 1, 5, 6, 7]);
      expectRowIds(table.visibleRows, [2, 3, 1, 6]);

      // once again expect to be moved after 6
      table.moveVisibleRowDown(rows[1]);
      expectRowIds(table.rows, [0, 2, 3, 4, 5, 6, 1, 7]);
      expectRowIds(table.visibleRows, [2, 3, 6, 1]);

      // once again expect to move nothing since node 7 is invisible
      table.moveVisibleRowDown(rows[1]);
      expectRowIds(table.rows, [0, 2, 3, 4, 5, 6, 1, 7]);
      expectRowIds(table.visibleRows, [2, 3, 6, 1]);
    });

  });

});
