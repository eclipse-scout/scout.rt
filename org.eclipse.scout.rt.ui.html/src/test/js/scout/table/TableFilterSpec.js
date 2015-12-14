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
/* global TableSpecHelper*/
describe("Table Filter", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
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

  describe("row filtering", function() {

    it("applies row filter when table gets initialized", function() {
      var model = helper.createModelFixture(2, 2);
      var filter = createColumnFilterModel(model.columns[0].id, ['cell1_0']);
      model.filters = [filter];
      var table = helper.createTable(model);

      var filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);
      expect(filteredRows[0]).toBe(table.rows[1]);
    });

    it("doesn't filter anymore if filter gets removed", function() {
      var model = helper.createModelFixture(2, 2);
      var filter = createColumnFilterModel(model.columns[0].id, ['cell1_0']);
      model.filters = [filter];
      var table = helper.createTable(model);

      var filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);
      expect(filteredRows[0]).toBe(table.rows[1]);

      table.removeFilterByKey(filter.column.id);
      table.filter();

      filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(2);
    });

    it("applies row filter if a new row gets inserted", function() {
      var model = helper.createModelFixture(2, 2),
        table = helper.createTable(model),
        column0 = table.columns[0];

      var filter = createAndRegisterColumnFilter(table, column0, ['cell1_0']);
      table.filter();
      table.render(session.$entryPoint);
      expect(table.filteredRows().length).toBe(1);

      var rows = helper.createModelRows(2, 1);
      rows[0].cells[0].value = 'newCell';
      table._insertRows(rows);

      var filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);
      expect(table.rows[2].filterAccepted).toBe(false);
      expect(table.rows[2].$row.isVisible()).toBe(false);

      rows = helper.createModelRows(2, 1);
      rows[0].cells[0].value = 'cell1_0';
      table._insertRows(rows);

      filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(2);
      expect(filteredRows[0]).toBe(table.rows[1]);
      expect(filteredRows[1]).toBe(table.rows[3]);
    });

    it("applies row filter if a new row gets inserted, even if table is not rendered", function() {
      var model = helper.createModelFixture(2, 2),
        table = helper.createTable(model),
        column0 = table.columns[0];

      var filter = createAndRegisterColumnFilter(table, column0, ['cell1_0']);
      table.filter();
      expect(table.filteredRows().length).toBe(1);

      var rows = helper.createModelRows(2, 1);
      rows[0].cells[0].value = 'newCell';
      table._insertRows(rows);

      var filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);
      expect(table.rows[2].filterAccepted).toBe(false);
      expect(table.rows[2].$row).toBeFalsy();

      table.render(session.$entryPoint);
      expect(table.rows[2].$row.isVisible()).toBe(false);
    });

    it("applies row filter if a row gets updated", function() {
      var model = helper.createModelFixture(2, 2),
        table = helper.createTable(model),
        column0 = table.columns[0],
        row1 = table.rows[1];

      // expects 1 row to be visible
      var filter = createAndRegisterColumnFilter(table, column0, ['cell1_0']);
      table.filter();
      table.render(session.$entryPoint);
      expect(table.filteredRows().length).toBe(1);

      var rows = helper.createModelRows(2, 1);
      rows[0].id = row1.id;
      rows[0].cells[0].value = 'updatedCell';
      table._updateRows(rows);

      // expects no row to be visible
      var filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(0);

      rows = helper.createModelRows(2, 1);
      rows[0].id = row1.id;
      rows[0].cells[0].value = 'cell1_0';
      table._updateRows(rows);

      // expects 1 row to be visible
      filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);
      expect(filteredRows[0]).toBe(table.rows[1]);

      // change cell 2 of row 1, filter state should not change
      rows = helper.createModelRows(2, 1);
      rows[0].id = row1.id;
      rows[0].cells[0].value = 'cell1_0';
      rows[0].cells[1].value = 'new cell1_1';
      table._updateRows(rows);

      // still expects 1 row to be visible
      filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);
      // if this check fails, filteredRow cache has not been updated
      expect(filteredRows[0]).toBe(table.rows[1]);
    });

    it("applies row filter if a row gets updated, even if table is not rendered", function() {
      var model = helper.createModelFixture(2, 2),
        table = helper.createTable(model),
        column0 = table.columns[0],
        row1 = table.rows[1];

      // expects 1 row to be visible
      var filter = createAndRegisterColumnFilter(table, column0, ['cell1_0']);
      table.filter();
      expect(table.filteredRows().length).toBe(1);

      var rows = helper.createModelRows(2, 1);
      rows[0].id = row1.id;
      rows[0].cells[0].value = 'updatedCell';
      table._updateRows(rows);

      // expects no row to be visible
      var filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(0);
      expect(table.rows[0].$row).toBeFalsy();
      expect(table.rows[1].$row).toBeFalsy();

      table.render(session.$entryPoint);
      expect(table.rows[0].$row.isVisible()).toBe(false);
      expect(table.rows[1].$row.isVisible()).toBe(false);
    });

    it("properly handles successive row insertion and updates", function() {
      var model = helper.createModelFixture(2, 2),
        table = helper.createTable(model),
        column0 = table.columns[0],
        row1 = table.rows[1];

      // expects 1 row to be visible
      var filter = createAndRegisterColumnFilter(table, column0, ['cell1_0']);
      table.filter();
      table.render(session.$entryPoint);
      expect(table.filteredRows().length).toBe(1);

      // insert new row -> not visible
      var rows = helper.createModelRows(2, 1);
      rows[0].cells[0].value = 'newCell';
      table._insertRows(rows);

      var filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);
      expect(table.rows[2].filterAccepted).toBe(false);
      expect(table.rows[2].$row.isVisible()).toBe(false);

      // update new row -> still not visible
      rows = helper.createModelRows(2, 1);
      rows[0].id = table.rows[2].id;
      rows[0].cells[0].value = 'updatedCell';
      table._updateRows(rows);

      filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);
      expect(table.rows[2].filterAccepted).toBe(false);
      expect(table.rows[2].$row.isVisible()).toBe(false);
    });

    it("properly handles block loading", function() {
      var model = helper.createModelFixture(2, 7),
        table = helper.createTable(model),
        column0 = table.columns[0],
        row1 = table.rows[1];

      table._blockLoadThreshold = 2;

      // expects 1 row to be visible
      var filter = createAndRegisterColumnFilter(table, column0, ['cell1_0']);
      table.filter();
      table.render(session.$entryPoint);

      expect(table.rows.length).toBe(7);
      expect(table.filteredRows().length).toBe(1);
    });

  });

  describe("selection", function() {

    it("gets removed for non visible rows after filtering", function() {
      var model = helper.createModelFixture(2, 2),
        table = helper.createTable(model),
        column0 = table.columns[0];

      table.render(session.$entryPoint);
      table.selectAll();
      expect(table.selectedRows.length).toBe(2);

      var filter = createAndRegisterColumnFilter(table, column0, ['cell1_0']);
      table.filter();

      expect(table.selectedRows.length).toBe(1);
      expect(table.selectedRows[0]).toBe(table.rows[1]);
    });

    it("gets removed for non visible rows after filtering if a row has been updated", function() {
      var model = helper.createModelFixture(2, 3),
        table = helper.createTable(model),
        column0 = table.columns[0],
        row1 = table.rows[1];

      var filter = createAndRegisterColumnFilter(table, column0, ['cell1_0', 'cell2_0']);
      table.filter();
      table.render(session.$entryPoint);
      table.selectAll();
      expect(table.selectedRows.length).toBe(2);
      expect(table.selectedRows[0]).toBe(table.rows[1]);
      expect(table.selectedRows[1]).toBe(table.rows[2]);

      // updateRows applies filter which should consider selection removal
      var rows = helper.createModelRows(2, 1);
      rows[0].id = row1.id;
      rows[0].cells[0].value = 'updatedCell';
      table._updateRows(rows);

      expect(table.selectedRows.length).toBe(1);
      expect(table.selectedRows[0]).toBe(table.rows[2]);
    });

  });

  describe("events", function() {

    beforeEach(function() {
      // filter animates -> disable
      $.fx.off = true;
    });

    afterEach(function() {
      $.fx.off = false;
    });


    describe("rowsFiltered", function() {
      var listener = {
        _onRowsFiltered: function() {}
      };

      it("gets fired when table with a filter is initializing", function() {
        var model = helper.createModelFixture(2, 2);
        var table = new scout.Table();
        var filter = createColumnFilterModel(model.columns[0].id, ['cell1_0']);
        model.filters = [filter];

        spyOn(listener, '_onRowsFiltered');
        table.on('rowsFiltered', listener._onRowsFiltered);

        table.init(model);
        expect(listener._onRowsFiltered).toHaveBeenCalled();
      });

      it("does not get fired when table with no filters is initializing", function() {
        var model = helper.createModelFixture(2, 2);
        var table = new scout.Table();

        spyOn(listener, '_onRowsFiltered');
        table.on('rowsFiltered', listener._onRowsFiltered);

        table.init(model);
        expect(listener._onRowsFiltered).not.toHaveBeenCalled();
      });

      it("gets fired if filter() is called", function() {
        var model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0];

        table.render(session.$entryPoint);

        spyOn(listener, '_onRowsFiltered');
        table.on('rowsFiltered', listener._onRowsFiltered);

        var filter = createAndRegisterColumnFilter(table, column0, ['cell1_0']);
        table.filter();

        expect(listener._onRowsFiltered).toHaveBeenCalled();
      });

      it("gets fired if filter() is called, even if table is not rendered", function() {
        var model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0];

        spyOn(listener, '_onRowsFiltered');
        table.on('rowsFiltered', listener._onRowsFiltered);

        var filter = createAndRegisterColumnFilter(table, column0, ['cell1_0']);
        table.filter();

        expect(listener._onRowsFiltered).toHaveBeenCalled();
      });

      it("gets not fired if rows are filtered again but the filtered rows have not changed", function() {
        var model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0];

        table.render(session.$entryPoint);

        var filter = createAndRegisterColumnFilter(table, column0, ['cell1_0']);
        table.filter();

        spyOn(listener, '_onRowsFiltered');
        table.on('rowsFiltered', listener._onRowsFiltered);
        table.filter();

        expect(listener._onRowsFiltered).not.toHaveBeenCalled();
      });

      it("gets fired if rows are filtered during updateRows", function() {
        var model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0],
          row1 = table.rows[1];

        var filter = createAndRegisterColumnFilter(table, column0, ['cell1_0']);
        table.filter();
        table.render(session.$entryPoint);

        spyOn(listener, '_onRowsFiltered');
        table.on('rowsFiltered', listener._onRowsFiltered);

        var rows = helper.createModelRows(2, 1);
        rows[0].id = row1.id;
        rows[0].cells[0].value = 'updatedCell';
        table._updateRows(rows);

        expect(table.filteredRows().length).toBe(0);
        expect(listener._onRowsFiltered).toHaveBeenCalled();
      });

      it("gets fired if rows are filtered during insertRows", function() {
        var model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0],
          row1 = table.rows[1];

        var filter = createAndRegisterColumnFilter(table, column0, ['cell1_0']);
        table.filter();
        table.render(session.$entryPoint);
        expect(table.filteredRows().length).toBe(1);

        spyOn(listener, '_onRowsFiltered');
        table.on('rowsFiltered', listener._onRowsFiltered);

        var rows = helper.createModelRows(2, 1);
        rows[0].cells[0].value = 'cell1_0';
        table._insertRows(rows);

        expect(table.filteredRows().length).toBe(2);
        expect(listener._onRowsFiltered).toHaveBeenCalled();

        rows = helper.createModelRows(2, 1);
        rows[0].cells[0].value = 'wont accept';
        table._insertRows(rows);

        expect(table.filteredRows().length).toBe(2);
        expect(listener._onRowsFiltered).toHaveBeenCalled();
      });

      it("gets fired if rows are filtered during deleteRows", function() {
        var model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0],
          row1 = table.rows[1];

        var filter = createAndRegisterColumnFilter(table, column0, ['cell1_0']);
        table.filter();
        table.render(session.$entryPoint);
        expect(table.filteredRows().length).toBe(1);

        spyOn(listener, '_onRowsFiltered');
        table.on('rowsFiltered', listener._onRowsFiltered);

        table._deleteRows([row1]);

        expect(table.filteredRows().length).toBe(0);
        expect(listener._onRowsFiltered).toHaveBeenCalled();
      });

      it("gets fired if rows are filtered during deleteAllRows", function() {
        var model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0],
          row1 = table.rows[1];

        var filter = createAndRegisterColumnFilter(table, column0, ['cell1_0']);
        table.filter();
        table.render(session.$entryPoint);
        expect(table.filteredRows().length).toBe(1);

        spyOn(listener, '_onRowsFiltered');
        table.on('rowsFiltered', listener._onRowsFiltered);

        table._deleteAllRows();

        expect(table.filteredRows().length).toBe(0);
        expect(listener._onRowsFiltered).toHaveBeenCalled();
      });

      it("does not get fired if rows are updated but row filter state has not changed", function() {
        var model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0],
          row1 = table.rows[1];

        var filter = createAndRegisterColumnFilter(table, column0, ['cell1_0']);
        table.filter();
        table.render(session.$entryPoint);

        spyOn(listener, '_onRowsFiltered');
        table.on('rowsFiltered', listener._onRowsFiltered);

        // update cell 1 of row -> row still accepted by filter
        var rows = helper.createModelRows(2, 1);
        rows[0].id = row1.id;
        rows[0].cells[0].value = row1.cells[0].value;
        rows[0].cells[1].value = 'updatedCell1';
        table._updateRows(rows);

        expect(table.filteredRows().length).toBe(1);
        expect(listener._onRowsFiltered).not.toHaveBeenCalled();
      });

      it("gets sent to server containing rowIds when rows are filtered", function() {
        var model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0];
        table.render(session.$entryPoint);

        var filter = createAndRegisterColumnFilter(table, column0, ['cell1_0']);
        table.filter();

        expect(table.rows[0].filterAccepted).toBe(false);
        expect(table.rows[1].filterAccepted).toBe(true);

        sendQueuedAjaxCalls('', 400);

        expect(jasmine.Ajax.requests.count()).toBe(1);

        var event = new scout.Event(table.id, 'rowsFiltered', {
          rowIds: [table.rows[1].id]
        });
        expect(mostRecentJsonRequest()).toContainEvents(event);
      });
    });
  });
});
