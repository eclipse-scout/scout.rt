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

  describe("delete", function() {
    var table, rows, rowIds;
    beforeEach(function() {
      var model = helper.createModelFixture(1, 7);
      table = helper.createTable(model),
        rows = table.rows,
        rowIds = table.rows.map(function(r) {
          return r.id;
        });
      rows[1].parentId = rows[0].id;
      rows[2].parentId = rows[1].id;
      rows[4].parentId = rows[3].id;
      rows[5].parentId = rows[3].id;
      table.updateRows(rows);
      table.render();
    });

    it("leaf row and expect the row structure to be valid", function() {
      table.deleteRow(rows[5]);
      expect(table.rows.length).toBe(6);
      expect(table._filteredRows.length).toBe(6);
      expect(table.visibleRows.length).toBe(6);
      expect(table.visibleRows.indexOf(rows[5])).toBe(-1);
      expect(rows[3].childRows.length).toBe(1);
    });

    it("leaf row with collapsed parent and expect the structure to be valid", function() {
      table.collapseRow(rows[3]);
      table.deleteRow(rows[5]);
      expect(table.rows.length).toBe(6);
      expect(table._filteredRows.length).toBe(6);
      expect(table.visibleRows.length).toBe(6);
      expect(table.visibleRows.indexOf(rows[5])).toBe(-1);
      expect(rows[3].childRows.length).toBe(1);
    });

    it("a parent row and expect all children are deleted cascading.", function() {
      table.deleteRow(rows[3]);
      expect(table.rows.length).toBe(4);
      expect(table._filteredRows.length).toBe(4);
      expect(table.visibleRows.length).toBe(4);
      expect(table.visibleRows.indexOf(rows[4])).toBe(-1);
      expect(table.visibleRows.indexOf(rows[5])).toBe(-1);
    });
  });

  describe("structure", function() {
    var table, rows, rowIds;
    beforeEach(function() {
      var model = helper.createModelFixture(1, 4);
      table = helper.createTable(model),
        rows = table.rows,
        rowIds = table.rows.map(function(r) {
          return r.id;
        });
      rows[1].parentId = rows[0].id;
      rows[2].parentId = rows[1].id;
      table.updateRows(rows);
      table.render();
    });

    it("is updated when insert a new child row", function() {
      var row = helper.createModelRow(undefined, ['newRow']),
        spyRenderViewPort = spyOn(table, '_renderViewport').and.callThrough();

      row.parentId = rows[0].id;
      table.insertRow(row);
      expect(table._renderViewport.calls.count()).toBe(1);

      row = scout.arrays.find(table.rows, function(r) {
        return r.cells[0].text === 'newRow';
      });
      expect(row).not.toBe(null);
      expect(table.visibleRows.length).toBe(5);
      expect(table.expandedRows.length).toBe(2);
      expect(rows[0].childRows.length).toBe(2);
      expect(rows[0].childRows[1]).toBe(row);
      expect(row.parentRow).toBe(rows[0]);
    });

    it("is updated when deleting a child row", function() {
      table.deleteRow(rows[2]);
      expect(table.visibleRows.length).toBe(3);
      expect(table.expandedRows.length).toBe(1);
    });

    it("is updated when deleting a row and its children", function() {
      // cascade deleted row
      var childRowToBeDeleted = rows[2];
      table.deleteRow(rows[1]);
      expect(scout.arrays.find(table.visibleRows, function(r) {
        return r.id === childRowToBeDeleted.id;
      }.bind(this))).toBe(null);
      expect(table.visibleRows.length).toBe(2);
      expect(table.expandedRows.length).toBe(0);
    });

  });

  describe("expanded rows", function() {
    var table, rows;
    beforeEach(function() {
      var model = helper.createModelFixture(1, 4);
      table = helper.createTable(model),
        rows = table.rows;
      rows[1].parentId = rows[0].id;
      rows[2].parentId = rows[1].id;
      table.updateRows(rows);
      table.render();
    });

    it("are valid after expand parent and its child row and expand parent again.", function() {
      expect(table.visibleRows.length).toBe(4);
      expect(table.expandedRows.length).toBe(2);
      table.collapseRow(rows[1]);
      expect(table.visibleRows.length).toBe(3);
      expect(table.expandedRows.length).toBe(1);
      table.collapseRow(rows[0]);
      expect(table.visibleRows.length).toBe(2);
      expect(table.expandedRows.length).toBe(0);
      table.expandRow(rows[0]);
      expect(table.visibleRows.length).toBe(3);
      expect(table.expandedRows.length).toBe(1);
    });

  });

  describe("selection", function() {
    var table, rows;
    beforeEach(function() {
      var model = helper.createModelFixture(1, 7);
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
    var table, rows;

    beforeEach(function() {
      var model = helper.createModelFixture(1, 4);
      table = helper.createTable(model);
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
      createAndRegisterColumnFilter(table, column0, ['2_0']);
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
