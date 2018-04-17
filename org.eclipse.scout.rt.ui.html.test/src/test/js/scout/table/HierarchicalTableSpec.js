/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
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
     * inital table
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
      rows[1].parentId = rows[0].id;
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

    it("a child row to a row which is already a parent row", function() {
      var newRow = helper.createModelRow(33, ['newRow']),
        parentRow = table.rows[0];
      newRow.parentId = parentRow.id;
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

    it("a child row to a row which is leaf", function() {
      var newRow = helper.createModelRow(33, ['newRow']),
        parentRow = table.rows[2];
      newRow.parentId = parentRow.id;
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
      newRow.parentId = parentRow.id;
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
     * inital table
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
      rows[1].parentId = rows[0].id;
      rows[2].parentId = rows[1].id;
      rows[4].parentId = rows[3].id;
      rows[5].parentId = rows[3].id;
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
     * inital table
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
      rows[1].parentId = rows[0].id;
      rows[2].parentId = rows[1].id;
      table.updateRows(rows);
      table.render();
    });

    it("is updated when insert a new child row", function() {
      var row = helper.createModelRow(33, ['newRow']),
        spyRenderViewPort = spyOn(table, '_renderViewport').and.callThrough();

      row.parentId = rows[0].id;
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
     * inital table
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
      rows[1].parentId = rows[0].id;
      rows[2].parentId = rows[1].id;
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
     * inital table
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
      rows[1].parentId = rows[0].id;
      rows[2].parentId = rows[1].id;
      rows[4].parentId = rows[3].id;
      rows[5].parentId = rows[3].id;
      table.updateRows(rows);
      table.render();
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

    it("of a row is still teh same if the row gets collapsed. ", function() {
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
      newRow01.parentId = rows[1].id;
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

    it("will be changed when deleting a selected row", function() {
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

  describe("filtered visible", function() {
    var table, rowIds, rows;
    /**
     * inital table
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
      rows[1].parentId = rows[0].id;
      rows[2].parentId = rows[1].id;
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

});
