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
describe("TableKeyStrokes", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe("key up", function() {

    it("selects the above row", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);

      var row2 = table.rows[2];
      table.render();
      helper.selectRowsAndAssert(table, [row2]);

      table.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(table, [table.rows[1]]);
      table.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(table, [table.rows[0]]);
      table.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(table, [table.rows[0]]);
    });

    it("selects the last row if no row is selected yet", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);

      table.render();

      table.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(table, [table.rows[4]]);
    });

    it("selects the second last row if all rows are selected", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);

      table.render();
      table.selectAll();

      table.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(table, [table.rows[3]]);
    });

    it("selects the only row if there is only one", function() {
      var model = helper.createModelFixture(2, 1);
      var table = helper.createTable(model);

      table.render();

      table.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(table, [table.rows[0]]);

      table.deselectAll();
      table.selectionHandler.lastActionRow = table.rows[0];

      table.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(table, [table.rows[0]]);
    });

    it("does nothing if first row already is selected", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      var rows = table.rows;

      table.render();
      helper.selectRowsAndAssert(table, [rows[0], rows[1]]);

      table.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(table, [rows[0], rows[1]]);
    });

    it("if first row already is selected but is not the last action row, the row above the last action row gets selected", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      var rows = table.rows;

      table.render();
      helper.selectRowsAndAssert(table, [rows[0], rows[1], rows[2]]);

      table.selectionHandler.lastActionRow = rows[2];

      table.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(table, [rows[1]]);
    });

    it("if there is a last action row, selects the row above last last action row", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);

      var rows = table.rows;
      table.render();
      helper.selectRowsAndAssert(table, [rows[2], rows[4]]);

      table.selectionHandler.lastActionRow = rows[4];

      table.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(table, [rows[3]]);
    });

    it("selects the row above the last action row even if the row above already is selected", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);

      var rows = table.rows;
      table.render();
      helper.selectRowsAndAssert(table, [rows[2], rows[3], rows[4]]);

      table.selectionHandler.lastActionRow = rows[4];

      table.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(table, [rows[3]]);
    });

    it("uses last row of selection as last action row if last action row is not visible anymore", function() {
      var model = helper.createModelFixture(2, 6);
      var table = helper.createTable(model);
      var rows = table.rows;
      table.render();
      helper.selectRowsAndAssert(table, [rows[1], rows[3], rows[4]]);
      table.selectionHandler.lastActionRow = rows[4];

      table.addFilter({
        createKey: function() {
          return 1;
        },
        accept: function(row) {
          return row !== rows[4];
        }
      });
      table.filter();

      table.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(table, [rows[0]]);
    });

    describe(" + shift", function() {

      it("adds the row above to the selection", function() {
        var model = helper.createModelFixture(2, 5);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[2]]);

        table.$data.triggerKeyDown(scout.keys.UP, 'shift');
        helper.assertSelection(table, [rows[1], rows[2]]);
        table.$data.triggerKeyDown(scout.keys.UP, 'shift');
        helper.assertSelection(table, [rows[0], rows[1], rows[2]]);
        table.$data.triggerKeyDown(scout.keys.UP, 'shift');
        helper.assertSelection(table, [rows[0], rows[1], rows[2]]);
      });

      it("removes the row above from the selection if the last action row is the last row of the selection", function() {
        var model = helper.createModelFixture(2, 6);
        var table = helper.createTable(model);

        var rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[2], rows[3], rows[4]]);

        table.selectionHandler.lastActionRow = rows[4];

        table.$data.triggerKeyDown(scout.keys.UP, 'shift');
        helper.assertSelection(table, [rows[2], rows[3]]);
        table.$data.triggerKeyDown(scout.keys.UP, 'shift');
        helper.assertSelection(table, [rows[2]]);
        table.$data.triggerKeyDown(scout.keys.UP, 'shift');
        helper.assertSelection(table, [rows[1], rows[2]]);
      });

      it("if the row above the last action row is not selected, adds the row above to the selection", function() {
        var model = helper.createModelFixture(2, 6);
        var table = helper.createTable(model);

        var rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[2], rows[5]]);

        table.selectionHandler.lastActionRow = rows[5];

        table.$data.triggerKeyDown(scout.keys.UP, 'shift');
        helper.assertSelection(table, [rows[2], rows[4], rows[5]]);
        table.$data.triggerKeyDown(scout.keys.UP, 'shift');
        helper.assertSelection(table, [rows[2], rows[3], rows[4], rows[5]]);
        table.$data.triggerKeyDown(scout.keys.UP, 'shift');
        helper.assertSelection(table, [rows[1], rows[2], rows[3], rows[4], rows[5]]);
      });

    });

  });

  describe("key down", function() {

    it("selects the row below", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      var row2 = table.rows[2];
      table.render();
      helper.selectRowsAndAssert(table, [row2]);

      table.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(table, [table.rows[3]]);
      table.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(table, [table.rows[4]]);
      table.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(table, [table.rows[4]]);
    });

    it("selects the first row if no row is selected yet", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);

      table.render();

      table.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(table, [table.rows[0]]);
    });

    it("selects the second row if all rows are selected", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);

      table.render();
      table.selectAll();

      table.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(table, [table.rows[1]]);
    });

    it("selects the only row if there is only one", function() {
      var model = helper.createModelFixture(2, 1);
      var table = helper.createTable(model);

      table.render();

      table.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(table, [table.rows[0]]);

      table.deselectAll();
      table.selectionHandler.lastActionRow = table.rows[0];

      table.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(table, [table.rows[0]]);
    });

    it("does nothing if last row already is selected", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      var rows = table.rows;

      table.render();
      helper.selectRowsAndAssert(table, [rows[3], rows[4]]);

      table.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(table, [rows[3], rows[4]]);
    });

    it("if there is a last action row, selects the row below the last action row", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);

      var rows = table.rows;
      table.render();
      helper.selectRowsAndAssert(table, [rows[2], rows[4]]);

      table.selectionHandler.lastActionRow = rows[2];

      table.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(table, [rows[3]]);
    });

    it("selects the row below the last action row even if the row below already is selected", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);

      var rows = table.rows;
      table.render();
      helper.selectRowsAndAssert(table, [rows[2], rows[3], rows[4]]);

      table.selectionHandler.lastActionRow = rows[2];

      table.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(table, [rows[3]]);
    });

    it("uses last row of selection as last action row if last action row is not visible anymore", function() {
      var model = helper.createModelFixture(2, 6);
      var table = helper.createTable(model);
      var rows = table.rows;
      table.render();
      helper.selectRowsAndAssert(table, [rows[1], rows[3], rows[4]]);
      table.selectionHandler.lastActionRow = rows[1];

      table.addFilter({
        createKey: function() {
          return 1;
        },
        accept: function(row) {
          return row !== rows[1];
        }
      });
      table.filter();

      table.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(table, [rows[5]]);
    });

    describe(" + shift", function() {

      it("adds the row below to the selection", function() {
        var model = helper.createModelFixture(2, 5);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[2]]);

        table.$data.triggerKeyDown(scout.keys.DOWN, 'shift');
        helper.assertSelection(table, [rows[2], rows[3]]);
        table.$data.triggerKeyDown(scout.keys.DOWN, 'shift');
        helper.assertSelection(table, [rows[2], rows[3], rows[4]]);
        table.$data.triggerKeyDown(scout.keys.DOWN, 'shift');
        helper.assertSelection(table, [rows[2], rows[3], rows[4]]);
      });

      it("removes the row below from the selection if the last action row is the first row of the selection", function() {
        var model = helper.createModelFixture(2, 6);
        var table = helper.createTable(model);

        var rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[2], rows[3], rows[4]]);

        table.selectionHandler.lastActionRow = rows[2];

        table.$data.triggerKeyDown(scout.keys.DOWN, 'shift');
        helper.assertSelection(table, [rows[3], rows[4]]);
        table.$data.triggerKeyDown(scout.keys.DOWN, 'shift');
        helper.assertSelection(table, [rows[4]]);
        table.$data.triggerKeyDown(scout.keys.DOWN, 'shift');
        helper.assertSelection(table, [rows[4], rows[5]]);
      });

      it("if the row below the last action row is not selected, adds the row below to the selection", function() {
        var model = helper.createModelFixture(2, 6);
        var table = helper.createTable(model);

        var rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[2], rows[5]]);

        table.selectionHandler.lastActionRow = rows[2];

        table.$data.triggerKeyDown(scout.keys.DOWN, 'shift');
        helper.assertSelection(table, [rows[2], rows[3], rows[5]]);
        table.$data.triggerKeyDown(scout.keys.DOWN, 'shift');
        helper.assertSelection(table, [rows[2], rows[3], rows[4], rows[5]]);
      });

    });

  });

  describe("end", function() {

    it("selects last row", function() {
      var model = helper.createModelFixture(2, 4);
      var table = helper.createTable(model);
      var rows = table.rows;
      table.render();
      helper.selectRowsAndAssert(table, [rows[1]]);

      table.$data.triggerKeyDown(scout.keys.END);
      helper.assertSelection(table, [rows[3]]);
    });

    describe(" + shift", function() {

      it("selects all rows from currently selected row to last row", function() {
        var model = helper.createModelFixture(2, 4);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[1]]);

        table.$data.triggerKeyDown(scout.keys.END, 'shift');
        helper.assertSelection(table, [rows[1], rows[2], rows[3]]);
      });

      it("preserves existing selection", function() {
        var model = helper.createModelFixture(2, 6);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[1], rows[3], rows[4]]);

        table.$data.triggerKeyDown(scout.keys.END, 'shift');
        helper.assertSelection(table, [rows[1], rows[3], rows[4], rows[5]]);
      });

      it("considers last action row as start row for new selection", function() {
        var model = helper.createModelFixture(2, 6);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[1], rows[3], rows[4]]);
        table.selectionHandler.lastActionRow = rows[1];

        table.$data.triggerKeyDown(scout.keys.END, 'shift');
        helper.assertSelection(table, [rows[1], rows[2], rows[3], rows[4], rows[5]]);
      });

      it("uses last row of selection as last action row if last action row is not visible anymore", function() {
        var model = helper.createModelFixture(2, 6);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[1], rows[3], rows[4]]);
        table.selectionHandler.lastActionRow = rows[1];

        table.addFilter({
          createKey: function() {
            return 1;
          },
          accept: function(row) {
            return row !== rows[1];
          }
        });
        table.filter();

        table.$data.triggerKeyDown(scout.keys.END, 'shift');
        helper.assertSelection(table, [rows[3], rows[4], rows[5]]);
      });

      it("does nothing if last row is already selected", function() {
        var model = helper.createModelFixture(2, 6);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[5]]);

        table.$data.triggerKeyDown(scout.keys.END, 'shift');
        helper.assertSelection(table, [rows[5]]);
      });

      it("does not add same rows to selectedRows twice", function() {
        var model = helper.createModelFixture(2, 3);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[0], rows[2]]);
        table.selectionHandler.lastActionRow = rows[0];

        table.$data.triggerKeyDown(scout.keys.END, 'shift');
        helper.assertSelection(table, [rows[0], rows[1], rows[2]]);
      });

    });

  });

  describe("home", function() {

    it("selects first row", function() {
      var model = helper.createModelFixture(2, 4);
      var table = helper.createTable(model);
      var rows = table.rows;
      table.render();
      helper.selectRowsAndAssert(table, [rows[2]]);

      table.$data.triggerKeyDown(scout.keys.HOME);
      helper.assertSelection(table, [rows[0]]);
    });

    describe(" + shift", function() {

      it("selects all rows from currently selected row to first row", function() {
        var model = helper.createModelFixture(2, 4);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[2]]);

        table.$data.triggerKeyDown(scout.keys.HOME, 'shift');
        helper.assertSelection(table, [rows[0], rows[1], rows[2]]);
      });

      it("preserves existing selection", function() {
        var model = helper.createModelFixture(2, 6);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[2], rows[3], rows[5]]);

        table.$data.triggerKeyDown(scout.keys.HOME, 'shift');
        helper.assertSelection(table, [rows[0], rows[1], rows[2], rows[3], rows[5]]);
      });

      it("considers last action row as start row for new selection", function() {
        var model = helper.createModelFixture(2, 6);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[1], rows[2], rows[4]]);
        table.selectionHandler.lastActionRow = rows[4];

        table.$data.triggerKeyDown(scout.keys.HOME, 'shift');
        helper.assertSelection(table, [rows[0], rows[1], rows[2], rows[3], rows[4]]);
      });

      it("uses first row of selection as last action row if last action row is not visible anymore", function() {
        var model = helper.createModelFixture(2, 6);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[1], rows[3], rows[4]]);
        table.selectionHandler.lastActionRow = rows[4];

        table.addFilter({
          createKey: function() {
            return 1;
          },
          accept: function(row) {
            return row !== rows[4];
          }
        });
        table.filter();

        table.$data.triggerKeyDown(scout.keys.HOME, 'shift');
        helper.assertSelection(table, [rows[0], rows[1], rows[3]]);
      });

      it("does nothing if first row is already selected", function() {
        var model = helper.createModelFixture(2, 6);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[0]]);

        table.$data.triggerKeyDown(scout.keys.HOME, 'shift');
        helper.assertSelection(table, [rows[0]]);
      });

      it("does not add same rows to selectedRows twice", function() {
        var model = helper.createModelFixture(2, 3);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render();
        helper.selectRowsAndAssert(table, [rows[0], rows[2]]);
        table.selectionHandler.lastActionRow = rows[2];

        table.$data.triggerKeyDown(scout.keys.HOME, 'shift');
        helper.assertSelection(table, [rows[0], rows[1], rows[2]]);
      });

    });

  });

  describe("space", function() {

    it("does nothing if no rows are selected", function() {
      var model = helper.createModelFixture(2, 4);
      model.checkable = true;
      var table = helper.createTable(model);
      var rows = table.rows;
      table.checkRow(rows[2], true);
      table.render();

      table.$data.triggerKeyDown(scout.keys.SPACE);
      expect(rows[0].checked).toBe(false);
      expect(rows[1].checked).toBe(false);
      expect(rows[2].checked).toBe(true);
      expect(rows[3].checked).toBe(false);
    });

    it("checks the selected rows if first row is unchecked", function() {
      var model = helper.createModelFixture(2, 4);
      model.checkable = true;
      var table = helper.createTable(model);
      var rows = table.rows;
      table.render();
      helper.selectRowsAndAssert(table, [rows[1], rows[2]]);

      table.$data.triggerKeyDown(scout.keys.SPACE);
      expect(rows[0].checked).toBe(false);
      expect(rows[1].checked).toBe(true);
      expect(rows[2].checked).toBe(true);
      expect(rows[3].checked).toBe(false);
    });

    it("does not modify already checked rows when checking", function() {
      var model = helper.createModelFixture(2, 4);
      model.checkable = true;
      var table = helper.createTable(model);
      var rows = table.rows;
      table.render();
      table.checkRow(rows[2], true);
      helper.selectRowsAndAssert(table, [rows[1], rows[2]]);

      table.$data.triggerKeyDown(scout.keys.SPACE);
      expect(rows[0].checked).toBe(false);
      expect(rows[1].checked).toBe(true);
      expect(rows[2].checked).toBe(true);
      expect(rows[3].checked).toBe(false);

      table.$data.triggerKeyUp(scout.keys.SPACE);
      table.$data.triggerKeyDown(scout.keys.SPACE);
      expect(rows[0].checked).toBe(false);
      expect(rows[1].checked).toBe(false);
      expect(rows[2].checked).toBe(false);
      expect(rows[3].checked).toBe(false);
    });

    it("unchecks the selected rows if first row is checked", function() {
      var model = helper.createModelFixture(2, 4);
      model.checkable = true;
      var table = helper.createTable(model);
      var rows = table.rows;
      table.render();
      table.checkRow(rows[0], true);
      table.checkRow(rows[1], true);
      table.checkRow(rows[2], true);
      table.checkRow(rows[3], true);
      helper.selectRowsAndAssert(table, [rows[1], rows[2]]);

      table.$data.triggerKeyDown(scout.keys.SPACE);
      expect(rows[0].checked).toBe(true);
      expect(rows[1].checked).toBe(false);
      expect(rows[2].checked).toBe(false);
      expect(rows[3].checked).toBe(true);
    });

  });

  describe("page up", function() {

    it("selects the only row if there is only one", function() {
      var model = helper.createModelFixture(2, 1);
      var table = helper.createTable(model);

      table.render();

      table.$data.triggerKeyDown(scout.keys.PAGE_UP);
      helper.assertSelection(table, [table.rows[0]]);

      table.deselectAll();
      table.selectionHandler.lastActionRow = table.rows[0];

      table.$data.triggerKeyDown(scout.keys.PAGE_UP);
      helper.assertSelection(table, [table.rows[0]]);
    });

  });

  describe("page down", function() {

    it("selects the only row if there is only one", function() {
      var model = helper.createModelFixture(2, 1);
      var table = helper.createTable(model);

      table.render();

      table.$data.triggerKeyDown(scout.keys.PAGE_DOWN);
      helper.assertSelection(table, [table.rows[0]]);

      table.deselectAll();
      table.selectionHandler.lastActionRow = table.rows[0];

      table.$data.triggerKeyDown(scout.keys.PAGE_DOWN);
      helper.assertSelection(table, [table.rows[0]]);
    });

  });
});
