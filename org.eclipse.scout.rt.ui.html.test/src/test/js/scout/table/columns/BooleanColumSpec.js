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
describe('BooleanColumn', function() {
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

  describe('table checkable column', function() {

    it('a checkbox column gets inserted if table.checkable=true', function() {
      var model = helper.createModelFixture(2);
      model.checkable = true;
      expect(model.columns.length).toBe(2);
      var table = helper.createTable(model);
      expect(table.columns.length).toBe(3);

      table.render();
      expect(table.columns[0] instanceof scout.BooleanColumn).toBeTruthy();
    });

    it('no checkbox column gets inserted if table.checkable=false', function() {
      var model = helper.createModelFixture(2);
      model.checkable = false;
      expect(model.columns.length).toBe(2);
      var table = helper.createTable(model);
      expect(table.columns.length).toBe(2);

      table.render();
      expect(table.columns[0] instanceof scout.BooleanColumn).toBeFalsy();
    });

    it('this.checkableColumn is set to the new column', function() {
      var model = helper.createModelFixture(2);
      model.checkable = true;
      var table = helper.createTable(model);
      table.render();

      expect(table.checkableColumn).not.toBeUndefined();
      expect(table.checkableColumn).toBe(table.columns[0]);
    });

    it('displays the row.checked state as checkbox', function() {
      var model = helper.createModelFixture(2, 2);
      model.checkable = true;
      model.rows[0].checked = true;
      model.rows[1].checked = false;
      var table = helper.createTable(model);
      table.render();
      var $rows = table.$rows();
      var $checkbox = table.columns[0].$checkBox($rows.eq(0));
      expect($checkbox).toHaveClass('checked');
      $checkbox = table.columns[0].$checkBox($rows.eq(1));
      expect($checkbox).not.toHaveClass('checked');
    });

  });

  describe('boolean column', function() {

    it('displays the cell value as checkbox', function() {
      var model = helper.createModelFixture(2, 2);
      model.columns[0].objectType = 'BooleanColumn';
      model.rows[0].cells[0].value = true;
      model.rows[1].cells[0].value = false;
      var table = helper.createTable(model);
      table.render();
      var $rows = table.$rows();
      var $checkbox = table.columns[0].$checkBox($rows.eq(0));
      expect($checkbox).toHaveClass('checked');
      $checkbox = table.columns[0].$checkBox($rows.eq(1));
      expect($checkbox).not.toHaveClass('checked');
    });

  });

});
