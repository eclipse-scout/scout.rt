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
/* global TableSpecHelper */
describe("TableKeyStrokes", function() {
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

  describe("key up", function() {

    it("selects the above row", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);

      var row2 = table.rows[2];
      table.render(session.$entryPoint);
      helper.selectRowsAndAssert(table, [row2]);

      table.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(table, [table.rows[1]]);
      table.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(table, [table.rows[0]]);
      table.$data.triggerKeyDown(scout.keys.UP);
      helper.assertSelection(table, [table.rows[0]]);
    });

    describe(" + shift", function() {

      it("adds the row above to the selection", function() {
        var model = helper.createModelFixture(2, 5);
        var table = helper.createTable(model);
        var row2 = table.rows[2];
        table.render(session.$entryPoint);
        helper.selectRowsAndAssert(table, [row2]);

        table.$data.triggerKeyDown(scout.keys.UP, 'shift');
        helper.assertSelection(table, [row2, table.rows[1]]);
        table.$data.triggerKeyDown(scout.keys.UP, 'shift');
        helper.assertSelection(table, [row2, table.rows[1], table.rows[0]]);
        table.$data.triggerKeyDown(scout.keys.UP, 'shift');
        helper.assertSelection(table, [row2, table.rows[1], table.rows[0]]);
      });

    });

  });

  describe("key down", function() {

    it("selects the row below", function() {
      var model = helper.createModelFixture(2, 5);
      var table = helper.createTable(model);
      var row2 = table.rows[2];
      table.render(session.$entryPoint);
      helper.selectRowsAndAssert(table, [row2]);

      table.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(table, [table.rows[3]]);
      table.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(table, [table.rows[4]]);
      table.$data.triggerKeyDown(scout.keys.DOWN);
      helper.assertSelection(table, [table.rows[4]]);
    });

    describe(" + shift", function() {

      it("adds the row below to the selection", function() {
        var model = helper.createModelFixture(2, 5);
        var table = helper.createTable(model);
        var row2 = table.rows[2];
        table.render(session.$entryPoint);
        helper.selectRowsAndAssert(table, [row2]);

        table.$data.triggerKeyDown(scout.keys.DOWN, 'shift');
        helper.assertSelection(table, [row2, table.rows[3]]);
        table.$data.triggerKeyDown(scout.keys.DOWN, 'shift');
        helper.assertSelection(table, [row2, table.rows[3], table.rows[4]]);
        table.$data.triggerKeyDown(scout.keys.DOWN, 'shift');
        helper.assertSelection(table, [row2, table.rows[3], table.rows[4]]);
      });

    });

  });

  describe("end", function() {

    it("selects last row", function() {
      var model = helper.createModelFixture(2, 4);
      var table = helper.createTable(model);
      var rows = table.rows;
      table.render(session.$entryPoint);
      helper.selectRowsAndAssert(table, [rows[1]]);

      table.$data.triggerKeyDown(scout.keys.END);
      helper.assertSelection(table, [rows[3]]);
    });

    describe(" + shift", function() {

      it("selects all rows from currently selected row to last row", function() {
        var model = helper.createModelFixture(2, 4);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render(session.$entryPoint);
        helper.selectRowsAndAssert(table, [rows[1]]);

        table.$data.triggerKeyDown(scout.keys.END, 'shift');
        helper.assertSelection(table, [rows[1], rows[2], rows[3]]);
      });

      it("preserves existing selection", function() {
        var model = helper.createModelFixture(2, 6);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render(session.$entryPoint);
        helper.selectRowsAndAssert(table, [rows[1], rows[3], rows[4]]);

        table.$data.triggerKeyDown(scout.keys.END, 'shift');
        helper.assertSelection(table, [rows[1], rows[3], rows[4], rows[5]]);
      });

      it("considers last action row as start row for new selection", function() {
        var model = helper.createModelFixture(2, 6);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render(session.$entryPoint);
        helper.selectRowsAndAssert(table, [rows[1], rows[3], rows[4]]);
        table.selectionHandler.lastActionRow = rows[1];

        table.$data.triggerKeyDown(scout.keys.END, 'shift');
        helper.assertSelection(table, [rows[1], rows[2], rows[3], rows[4], rows[5]]);
      });

      it("uses last row of selection as last action row if last action row is not visible anymore", function() {
        var model = helper.createModelFixture(2, 6);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render(session.$entryPoint);
        helper.selectRowsAndAssert(table, [rows[1], rows[3], rows[4]]);
        table.selectionHandler.lastActionRow = rows[1];

        table.addFilter({
          createKey: function() {
            return 1;
          },
          accept: function($row) {
            return $row.data('row') !== rows[1];
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
        table.render(session.$entryPoint);
        helper.selectRowsAndAssert(table, [rows[5]]);

        table.$data.triggerKeyDown(scout.keys.END, 'shift');
        helper.assertSelection(table, [rows[5]]);
      });

      it("does not add same rows to selectedRows twice", function() {
        var model = helper.createModelFixture(2, 3);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render(session.$entryPoint);
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
      table.render(session.$entryPoint);
      helper.selectRowsAndAssert(table, [rows[2]]);

      table.$data.triggerKeyDown(scout.keys.HOME);
      helper.assertSelection(table, [rows[0]]);
    });

    describe(" + shift", function() {

      it("selects all rows from currently selected row to first row", function() {
        var model = helper.createModelFixture(2, 4);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render(session.$entryPoint);
        helper.selectRowsAndAssert(table, [rows[2]]);

        table.$data.triggerKeyDown(scout.keys.HOME, 'shift');
        helper.assertSelection(table, [rows[0], rows[1], rows[2]]);
      });

      it("preserves existing selection", function() {
        var model = helper.createModelFixture(2, 6);
        var table = helper.createTable(model);
        var rows = table.rows;
        table.render(session.$entryPoint);
        helper.selectRowsAndAssert(table, [rows[2], rows[3], rows[5]]);

        table.$data.triggerKeyDown(scout.keys.HOME, 'shift');
        helper.assertSelection(table, [rows[0], rows[1], rows[2], rows[3], rows[5]]);
      });

    });

  });
});
