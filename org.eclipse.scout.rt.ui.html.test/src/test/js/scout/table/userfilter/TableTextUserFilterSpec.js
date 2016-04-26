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
describe('TableTextUserFilter', function() {
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

  function createFilter(table) {
    var filter = scout.create('TableTextUserFilter', {
      session: session,
      table: table
    });
    return filter;
  }

  var ASpecBeanColumn = function() {
    ASpecBeanColumn.parent.call(this);
  };
  scout.inherits(ASpecBeanColumn, scout.BeanColumn);
  ASpecBeanColumn.prototype._renderValue = function($cell, value) {
    $cell.appendDiv().text(value.a);
  };

  describe('filter', function() {

    beforeEach(function() {
      scout.objectFactory.register('ASpecBeanColumn', function() {
        return new ASpecBeanColumn();
      });
    });

    afterEach(function() {
      scout.objectFactory.unregister('ASpecBeanColumn');
    });

    it('filters rows based on cell text', function() {
      var model = helper.createModelFixture(2, 0),
        table = helper.createTable(model),
        filter = createFilter(table);

      var rows = [{
        cells: ['cell00', 'cell01']
      }, {
        cells: ['cell10', 'cell11']
      }];
      table.insertRows(rows);

      expect(table.filteredRows().length).toBe(2);

      filter.text = 'cell11';
      table.addFilter(filter);
      table.filter();

      expect(table.filteredRows().length).toBe(1);
      expect(table.filteredRows()[0]).toBe(table.rows[1]);

      filter.text = 'asdf';
      table.addFilter(filter);
      table.filter();

      expect(table.filteredRows().length).toBe(0);

      table.removeFilter(filter);
      table.filter();

      expect(table.filteredRows().length).toBe(2);
    });

    it('separates cell values with whitepace', function() {
      var model = helper.createModelFixture(2, 0),
        table = helper.createTable(model),
        filter = createFilter(table);

      var rows = [{
        cells: ['cell00', 'cell01']
      }, {
        cells: ['cell10', 'cell11']
      }];
      table.insertRows(rows);

      expect(table.filteredRows().length).toBe(2);

      filter.text = 'cell10cell';
      table.addFilter(filter);
      table.filter();

      expect(table.filteredRows().length).toBe(0);

      filter.text = 'cell10 cell';
      table.addFilter(filter);
      table.filter();

      expect(table.filteredRows().length).toBe(1);
      expect(table.filteredRows()[0]).toBe(table.rows[1]);
    });

    it('works with bean columns', function() {
      var table = scout.create('Table', {
        parent: new scout.NullWidget(),
        session: session,
        columns: [
          scout.create('ASpecBeanColumn', {
            session: session,
            index: 0,
            width: 100
          })
        ]
      });
      var filter = createFilter(table);
      var bean0 = {
        a: 'bean0 text'
      };
      var bean1 = {
        a: 'bean1 text'
      };
      var rows = [{
        cells: [{
          value: bean0
        }]
      }, {
        cells: [{
          value: bean1
        }]
      }];
      table.insertRows(rows);

      expect(table.filteredRows().length).toBe(2);

      filter.text = 'bean1';
      table.addFilter(filter);
      table.filter();

      expect(table.filteredRows().length).toBe(1);
      expect(table.filteredRows()[0]).toBe(table.rows[1]);

      filter.text = 'asdf';
      table.addFilter(filter);
      table.filter();

      expect(table.filteredRows().length).toBe(0);

      table.removeFilter(filter);
      table.filter();

      expect(table.filteredRows().length).toBe(2);
    });

  });
});
