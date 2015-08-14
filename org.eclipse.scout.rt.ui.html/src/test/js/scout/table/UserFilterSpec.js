/* global TableSpecHelper*/
describe("UserFilter", function() {
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

  function createAndRegisterFilter(table, column, selectedValues) {
    return helper.createAndRegisterFilter(table, column, selectedValues);
  }

  describe("row filtering", function() {

    it("applies row filter when table gets rendered", function() {
      var model = helper.createModelFixture(2, 2),
        table = helper.createTable(model),
        column0 = table.columns[0];

      var filter = createAndRegisterFilter(table, column0, ['cell1_0']);
      table.render(session.$entryPoint);

      var filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);
      expect(filteredRows[0]).toBe(table.rows[1]);
    });

    it("doesn't filter anymore if filter gets removed", function() {
      var model = helper.createModelFixture(2, 2),
        table = helper.createTable(model),
        column0 = table.columns[0];

      var filter = createAndRegisterFilter(table, column0, ['cell1_0']);
      table.render(session.$entryPoint);

      var filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);
      expect(filteredRows[0]).toBe(table.rows[1]);

      table.unregisterFilter(filter.column.id);
      table.filter();

      filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(2);
    });

    it("applies row filter if a new row gets inserted", function() {
      var model = helper.createModelFixture(2, 2),
        table = helper.createTable(model),
        column0 = table.columns[0];

      var filter = createAndRegisterFilter(table, column0, ['cell1_0']);
      table.render(session.$entryPoint);
      expect(table.filteredRows().length).toBe(1);

      var rows = helper.createModelRows(2, 1);
      rows[0].cells[0].value = 'newCell';
      table._insertRows(rows);

      var filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(1);

      rows = helper.createModelRows(2, 1);
      rows[0].cells[0].value = 'cell1_0';
      table._insertRows(rows);

      filteredRows = table.filteredRows();
      expect(filteredRows.length).toBe(2);
      expect(filteredRows[0]).toBe(table.rows[1]);
      expect(filteredRows[1]).toBe(table.rows[3]);
    });

    it("applies row filter if a row gets updated", function() {
      var model = helper.createModelFixture(2, 2),
        table = helper.createTable(model),
        column0 = table.columns[0],
        row1 = table.rows[1];

      // expects 1 row to be visible
      var filter = createAndRegisterFilter(table, column0, ['cell1_0']);
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

      var filter = createAndRegisterFilter(table, column0, ['cell1_0']);
      table.filter();

      expect(table.selectedRows.length).toBe(1);
      expect(table.selectedRows[0]).toBe(table.rows[1]);
    });

    it("gets removed for non visible rows after filtering if a row has been updated", function() {
      var model = helper.createModelFixture(2, 3),
        table = helper.createTable(model),
        column0 = table.columns[0],
        row1 = table.rows[1];

      var filter = createAndRegisterFilter(table, column0, ['cell1_0', 'cell2_0']);
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

    describe("rowsFiltered", function() {
      var listener = {
        _onRowsFiltered: function() {}
      };

      it("gets fired if rows are filtered", function() {
        var model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0];

        table.render(session.$entryPoint);

        spyOn(listener, '_onRowsFiltered');
        table.on('rowsFiltered', listener._onRowsFiltered);

        var filter = createAndRegisterFilter(table, column0, ['cell1_0']);
        table.filter();

        expect(listener._onRowsFiltered).toHaveBeenCalled();
      });

      it("gets fired if rows are filtered", function() {
        var model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0];

        table.render(session.$entryPoint);

        spyOn(listener, '_onRowsFiltered');
        table.on('rowsFiltered', listener._onRowsFiltered);

        var filter = createAndRegisterFilter(table, column0, ['cell1_0']);
        table.filter();

        expect(listener._onRowsFiltered).toHaveBeenCalled();
      });

      it("gets fired if rows are filtered during updateRows", function() {
        var model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0],
          row1 = table.rows[1];

        var filter = createAndRegisterFilter(table, column0, ['cell1_0']);
        table.render(session.$entryPoint);

        spyOn(listener, '_onRowsFiltered');
        table.on('rowsFiltered', listener._onRowsFiltered);

        // updateRows applies filter which fire the event
        var rows = helper.createModelRows(2, 1);
        rows[0].id = row1.id;
        rows[0].cells[0].value = 'updatedCell';
        table._updateRows(rows);

        expect(table.filteredRows().length).toBe(0);
        expect(listener._onRowsFiltered).toHaveBeenCalled();
      });

      it("does not get fired if rows are updated but row filter state has not changed", function() {
        var model = helper.createModelFixture(2, 2),
          table = helper.createTable(model),
          column0 = table.columns[0],
          row1 = table.rows[1];

        var filter = createAndRegisterFilter(table, column0, ['cell1_0']);
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
    });
  });
});
