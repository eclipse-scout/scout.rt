/* global TableSpecHelper, LocaleSpecHelper */
describe("AggregateTableControl", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    session.locale = new LocaleSpecHelper().createLocale('de-CH');
    helper = new TableSpecHelper(session);

    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createModel() {
    var model = createSimpleModel('TableControl', session);
    $.extend({
      "enabled": true,
      "visible": true
    });

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
    form.htmlComp = new scout.HtmlComponent(form.$container, session);
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
    var model, table, column0, column1, rows, columns, tableControl;
    var $colHeaders, $header0, $header1;

    function prepareTable() {
      columns = [helper.createModelColumn(null, 'col1'),
        helper.createModelColumn(null, 'col2', 'number')
      ];
      columns[0].index = 0;
      columns[1].index = 1;
      rows = helper.createModelRows(2, 3);
      model = helper.createModel(columns, rows);
      table = helper.createTable(model);

      tableControl = createAggregateTC(createModel());
      tableControl.selected = true;
      table.tableControls = [tableControl];

      column0 = model.columns[0];
      column1 = model.columns[1];
    }

    function render(table) {
      table.render(session.$entryPoint);
      $colHeaders = table.header.$container.find('.table-header-item');
      $header0 = $colHeaders.eq(0);
      $header1 = $colHeaders.eq(1);
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
      expect($aggrCells.eq(0).text()).toBe(' ');
      expect($aggrCells.eq(1).text()).toBe('6');
    });

    it("sums up numbers in a number column but only on filtered rows", function() {
      prepareTable();
      rows[0].cells[1].value = 1;
      rows[1].cells[1].value = 2;
      rows[2].cells[1].value = 3;
      table.render(session.$entryPoint);

      table.addFilter(createFilter(
        function($row) {
          return $row.data('row').id !== table.rows[2].id;
        }));
      table.filter();
      var $aggrRow = $aggregateRow(tableControl);
      var $aggrCells = $aggrRow.children('.table-cell');
      expect($aggrCells.eq(0).text()).toBe(' ');
      expect($aggrCells.eq(1).text()).toBe('3');
    });

    it("sums up numbers in a number column and considers format pattern", function() {
      prepareTable();
      rows[0].cells[1].value = 1000;
      rows[1].cells[1].value = 1000;
      rows[2].cells[1].value = 2000;
      column1.format = '#.00';
      table.render(session.$entryPoint);

      var $aggrRow = $aggregateRow(tableControl);
      var $aggrCell = $aggrRow.children('.table-cell').eq(1);
      expect($aggrCell.text()).toBe('4000.00');
    });

  });

});
