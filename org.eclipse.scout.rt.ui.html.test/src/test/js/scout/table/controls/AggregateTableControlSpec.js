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
describe("AggregateTableControl", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    session.locale = new scout.LocaleSpecHelper().createLocale(scout.LocaleSpecHelper.DEFAULT_LOCALE);
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

  function createModel(table) {
    var model = createSimpleModel('TableControl', session);
    model.table = table;
    return model;
  }

  function createFormMock() {
    var form = {
      render: function() {},
      remove: function() {},
      $container: $('<div>'),
      rootGroupBox: {
        fields: []
      }
    };
    form.htmlComp = scout.HtmlComponent.install(form.$container, session);
    return form;
  }

  function createAggregateTC(model) {
    var action = new scout.AggregateTableControl();
    action.init(model);
    return action;
  }

  function $aggregateRow(tableControl) {
    return tableControl.$contentContainer;
  }

  function createFilter(acceptFunc) {
    return {
      accept: acceptFunc,
      createKey: function() {
        return 1; // dummy value
      },
      createLabel: function() {
        return '';
      }
    };
  }

  describe("aggregate", function() {
    var model, table, column0, column1, column2, rows, columns, tableControl;
    var $colHeaders, $header0, $header1;

    function prepareTable() {
      columns = [helper.createModelColumn('col1'),
        helper.createModelColumn('col2', 'NumberColumn'),
        helper.createModelColumn('col3', 'NumberColumn')
      ];
      columns[0].index = 0;
      columns[1].index = 1;
      rows = helper.createModelRows(columns, 3);
      model = helper.createModel(columns, rows);
      table = helper.createTable(model);

      tableControl = createAggregateTC(createModel(table));
      tableControl.selected = true;
      table._setTableControls([tableControl]);

      column0 = model.columns[0];
      column1 = model.columns[1];
      column1.setAggregationFunction('sum');
      column2 = model.columns[2];
      column2.setAggregationFunction('sum');
    }

    it("creates an aggregate row", function() {
      prepareTable();
      table.render(session.$entryPoint);

      expect($aggregateRow(tableControl).length).toBe(1);
    });

    it("sums up numbers in a number column", function() {
      prepareTable();
      rows[0].cells[1].value = 1;
      rows[1].cells[1].value = 2;
      rows[2].cells[1].value = 3;
      table.render(session.$entryPoint);

      var $aggrRow = $aggregateRow(tableControl);
      var $aggrCells = $aggrRow.children('.table-cell');
      $aggrCells.children('.table-cell-icon').remove();
      expect($aggrCells.eq(0).text()).toBe(' ');
      expect($aggrCells.eq(1).text()).toBe('6');
    });

    it("aggregation type none does not aggregate", function() {
      prepareTable();
      rows[0].cells[1].value = 1;
      rows[1].cells[1].value = 2;
      rows[2].cells[1].value = 3;
      table.render(session.$entryPoint);
      table.changeAggregation(table.columns[1], 'none');

      var $aggrRow = $aggregateRow(tableControl);
      var $aggrCells = $aggrRow.children('.table-cell');
      expect($aggrCells.eq(0).text()).toBe(' ');
      expect($aggrCells.eq(1).text()).toBe(' ');
    });

    it("sums up numbers in a number column but only on filtered rows", function() {
      prepareTable();
      rows[0].cells[1].value = 1;
      rows[1].cells[1].value = 2;
      rows[2].cells[1].value = 3;
      table.render(session.$entryPoint);

      table.addFilter(createFilter(
        function(row) {
          return row.id !== table.rows[2].id;
        }));
      table.filter();
      var $aggrRow = $aggregateRow(tableControl);
      var $aggrCells = $aggrRow.children('.table-cell');
      $aggrCells.children('.table-cell-icon').remove();
      expect($aggrCells.eq(0).text()).toBe(' ');
      expect($aggrCells.eq(1).text()).toBe('3');
    });

    it("sums up numbers in a number column and considers format pattern", function() {
      prepareTable();
      rows[0].cells[1].value = 1000;
      rows[1].cells[1].value = 1000;
      rows[2].cells[1].value = 2000;
      column1.decimalFormat = new scout.DecimalFormat(session.locale, {
        pattern: '#.00'
      });
      table.render(session.$entryPoint);

      var $aggrRow = $aggregateRow(tableControl);
      var $aggrCell = $aggrRow.children('.table-cell').eq(1);
      $aggrCell.children('.table-cell-icon').remove();
      expect($aggrCell.text()).toBe('4000.00');
    });

    it("sums up numbers in a number column and considers rounded values fo aggregation", function() {
      prepareTable();
      rows[0].cells[1].value = 0.005;
      rows[1].cells[1].value = 0.006;
      rows[2].cells[1].value = 0.005;
      column1.decimalFormat = new scout.DecimalFormat(session.locale, {
        pattern: '#.00'
      });
      table.render(session.$entryPoint);

      var $aggrRow = $aggregateRow(tableControl);
      var $aggrCell = $aggrRow.children('.table-cell').eq(1);
      $aggrCell.children('.table-cell-icon').remove();
      expect($aggrCell.text()).toBe('.03');
    });

  });

  describe("eanbled state", function() {
    var columns, rows, model, table, tableControl;

    function prepareTable() {
      columns = [helper.createModelColumn('col1'),
        helper.createModelColumn('col2')
      ];
      columns[0].index = 0;
      columns[1].index = 1;
      rows = helper.createModelRows(2, 3);
      model = helper.createModel(columns, rows);
      table = helper.createTable(model);
    }

    it("is false if there are no number columns", function() {
      prepareTable();
      var tcModel = createModel(table);
      tcModel.enabled = true;
      tcModel.selected = true;
      tableControl = createAggregateTC(createModel(table));
      table._setTableControls([tableControl]);
      table.render(session.$entryPoint);

      expect(tableControl.enabled).toBe(false);
      expect(tableControl.selected).toBe(false);
    });

    it("is true if there is at least one number column", function() {
      columns = [
        helper.createModelColumn('col1'),
        helper.createModelColumn('col2', 'NumberColumn')
      ];
      columns[0].index = 0;
      columns[1].index = 1;
      rows = helper.createModelRows(columns, 3);
      model = helper.createModel(columns, rows);
      table = helper.createTable(model);
      table.columns[1].setAggregationFunction('sum');

      var tcModel = createModel(table);
      tcModel.enabled = false;
      tcModel.selected = true;
      tableControl = createAggregateTC(tcModel);
      table._setTableControls([tableControl]);
      table.render(session.$entryPoint);

      expect(tableControl.enabled).toBe(true);
      expect(tableControl.selected).toBe(true);
    });

    it("is false if there is a number column but without an aggregate function", function() {
      columns = [
        helper.createModelColumn('col1'),
        helper.createModelColumn('col2', 'NumberColumn')
      ];
      columns[0].index = 0;
      columns[1].index = 1;
      rows = helper.createModelRows(columns, 3);
      model = helper.createModel(columns, rows);
      table = helper.createTable(model);
      table.columns[1].setAggregationFunction('none');

      var tcModel = createModel(table);
      tcModel.enabled = false;
      tcModel.selected = false;
      tableControl = createAggregateTC(tcModel);
      table._setTableControls([tableControl]);
      table.render(session.$entryPoint);

      expect(tableControl.enabled).toBe(false);
      expect(tableControl.selected).toBe(false);
    });

  });

});
