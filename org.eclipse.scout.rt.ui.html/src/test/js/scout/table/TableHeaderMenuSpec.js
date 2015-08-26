/* global TableSpecHelper*/
describe("TableHeaderMenu", function() {
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

  function createAndRegisterColumnFilter(table, column, selectedValues) {
    var filter = new scout.ColumnUserFilter();
    helper.createAndRegisterColumnFilter({
      table: table,
      column: column,
      selectedValues: selectedValues
    }, session);
    return filter;
  }

  function find$FilterItems(table) {
    var $menu = table.header._tableHeaderMenu.$container;
    return $menu.find('.header-filter');
  }

  function createSingleColumnTableByTexts(texts) {
    var model = helper.createModelSingleColumnByTexts(texts);
    return helper.createTable(model);
  }

  function createSingleColumnTableByValues(values) {
    var model = helper.createModelSingleColumnByValues(values);
    return helper.createTable(model);
  }

  describe("filter", function() {

    describe("string column", function() {

      it("shows the unique string values", function() {
        var table = createSingleColumnTableByTexts(['Value', 'AnotherValue', 'Value']);
        var column = table.columns[0];
        table.render(session.$entryPoint);
        table.header.openTableHeaderMenu(column);
        var $filterItems = find$FilterItems(table);
        expect($filterItems.eq(0).text()).toBe('AnotherValue');
        expect($filterItems.eq(1).text()).toBe('Value');
        table.header.closeTableHeaderMenu();
      });

      it("converts multiline text to single line", function() {
        var table = createSingleColumnTableByTexts(['First line\nSecond line', 'AnotherValue']);
        var column = table.columns[0];
        table.render(session.$entryPoint);
        table.header.openTableHeaderMenu(column);
        var $filterItems = find$FilterItems(table);
        expect($filterItems.eq(0).text()).toBe('AnotherValue');
        expect($filterItems.eq(1).text()).toBe('First line Second line');
        table.header.closeTableHeaderMenu();
      });

      it("strips html tags if html is enabled", function() {
        var table = createSingleColumnTableByTexts(['<b>contains html</b>', '<ul><li>line 1</li><li>line 2</li></ul>']);
        table.rows[0].cells[0].htmlEnabled = true;
        table.rows[1].cells[0].htmlEnabled = true;
        var column = table.columns[0];
        table.render(session.$entryPoint);
        table.header.openTableHeaderMenu(column);
        var $filterItems = find$FilterItems(table);
        expect($filterItems.eq(0).text()).toBe('contains html');
        expect($filterItems.eq(1).text()).toBe('line 1 line 2');
        table.header.closeTableHeaderMenu();
      });

      it("reflects the state of the filter", function() {
        var table = createSingleColumnTableByTexts(['Value', 'AnotherValue', 'Value']);
        var column = table.columns[0];
        var filter = createAndRegisterColumnFilter(table, column, ['AnotherValue']);
        table.render(session.$entryPoint);
        expect(table.filteredRows().length).toBe(1);

        table.header.openTableHeaderMenu(column);
        var $filterItems = find$FilterItems(table);
        expect($filterItems.eq(0).text()).toBe('AnotherValue');
        expect($filterItems.eq(0)).toHaveClass('selected');
        expect($filterItems.eq(1).text()).toBe('Value');
        expect($filterItems.eq(1)).not.toHaveClass('selected');
        table.header.closeTableHeaderMenu();
      });

      it("correctly updates the list after inserting a new row, if a filter is applied", function() {
        var table = createSingleColumnTableByTexts(['Value', 'AnotherValue', 'Value']);
        var column = table.columns[0];
        var filter = createAndRegisterColumnFilter(table, column, ['AnotherValue']);
        table.render(session.$entryPoint);
        expect(table.filteredRows().length).toBe(1);

        table.header.openTableHeaderMenu(column);
        var $filterItems = find$FilterItems(table);
        expect($filterItems.eq(0).text()).toBe('AnotherValue');
        expect($filterItems.eq(1).text()).toBe('Value');
        table.header.closeTableHeaderMenu();

        var newRows = helper.createModelRows(2, 1);
        newRows[0].cells[0].value = 'NewValue';
        table._insertRows(newRows);

        table.header.openTableHeaderMenu(column);
        $filterItems = find$FilterItems(table);
        expect($filterItems.eq(0).text()).toBe('AnotherValue');
        expect($filterItems.eq(1).text()).toBe('NewValue');
        expect($filterItems.eq(2).text()).toBe('Value');
        table.header.closeTableHeaderMenu();
      });

      it("always displays the selected value, even if the table does not contain the value anymore", function() {
        var table = createSingleColumnTableByTexts(['Value', 'AnotherValue', 'Value']);
        var column = table.columns[0];
        var filter = createAndRegisterColumnFilter(table, column, ['AnotherValueNotInTable']);
        table.render(session.$entryPoint);
        expect(table.filteredRows().length).toBe(0);

        table.header.openTableHeaderMenu(column);
        var $filterItems = find$FilterItems(table);
        expect($filterItems.eq(0).text()).toBe('AnotherValue');
        expect($filterItems.eq(1).text()).toBe('AnotherValueNotInTable');
        expect($filterItems.eq(2).text()).toBe('Value');
        table.header.closeTableHeaderMenu();
      });
    });


    describe("boolean column", function() {

      it("shows the unique string values", function() {
        session.text = function(key) {
          if (key === 'ui.BooleanColumnGroupingTrue') {
            return 'marked';
          } else if (key === 'ui.BooleanColumnGroupingFalse') {
            return 'unmarked';
          }
        };
        var table = createSingleColumnTableByValues([true, false, true]);
        var column = table.columns[0];
        column.type = 'boolean';
        table.render(session.$entryPoint);
        table.header.openTableHeaderMenu(column);
        var $filterItems = find$FilterItems(table);
        expect($filterItems.eq(0).text()).toBe('unmarked');
        expect($filterItems.eq(1).text()).toBe('marked');
        table.header.closeTableHeaderMenu();
      });

    });
  });
});
