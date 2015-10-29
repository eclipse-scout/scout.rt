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

    it("adds the row above to the selection if shift is used as modifier", function() {
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

    it("adds the row below to the selection if shift is used as modifier", function() {
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
