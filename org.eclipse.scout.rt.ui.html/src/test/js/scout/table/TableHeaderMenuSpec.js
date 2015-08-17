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
    var filter = new scout.ColumnUserTableFilter();
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

  describe("filter", function() {

    describe("string column", function() {
      var model, table, column;

      beforeEach(function() {
        model = helper.createModelSingleColumnByTexts(['Value', 'AnotherValue', 'Value']);
        table = helper.createTable(model);
        column = table.columns[0];
      });

      it("shows the unique string values", function() {
        table.render(session.$entryPoint);
        table.header.openTableHeaderMenu(column);
        var $filterItems = find$FilterItems(table);
        expect($filterItems.eq(0).text()).toBe('AnotherValue');
        expect($filterItems.eq(1).text()).toBe('Value');
        table.header.closeTableHeaderMenu();
      });

      it("reflects the state of the filter", function() {
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
    });

  });
});
