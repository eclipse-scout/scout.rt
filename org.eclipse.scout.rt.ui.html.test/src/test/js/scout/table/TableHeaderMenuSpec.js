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
describe('TableHeaderMenu', function() {
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
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createAndRegisterColumnFilter(table, column, selectedValues) {
    var filter = new scout.ColumnUserFilter();
    helper.createAndRegisterColumnFilter({
      session: session,
      table: table,
      column: column,
      selectedValues: selectedValues
    });
    return filter;
  }

  function find$FilterItems(table) {
    var $menu = table.header._tableHeaderMenu.$container;
    return $menu.find('.table-data > .table-row');
  }

  function expectTableRowText($row, index, expectedText) {
    var $cell = $row.eq(index).find('.table-cell');
    expect($cell.eq(0).text()).toBe(expectedText);
  }

  function createSingleColumnTableByTexts(texts) {
    var model = helper.createModelSingleColumnByTexts(texts);
    return helper.createTable(model);
  }

  function createSingleColumnTableByValues(values) {
    var model = helper.createModelSingleColumnByValues(values, 'BooleanColumn');
    return helper.createTable(model);
  }

  describe('filter', function() {

    describe('string column', function() {

      it('shows the unique string values', function() {
        var table = createSingleColumnTableByTexts(['Value', 'AnotherValue', 'Value']);
        var column = table.columns[0];
        table.render();
        table.header.openTableHeaderMenu(column);
        var $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'AnotherValue');
        expectTableRowText($filterItems, 1, 'Value');
        table.header.closeTableHeaderMenu();
      });

      it('converts multiline text to single line', function() {
        var table = createSingleColumnTableByTexts(['First line\nSecond line', 'AnotherValue']);
        var column = table.columns[0];
        table.render();
        table.header.openTableHeaderMenu(column);
        var $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'AnotherValue');
        expectTableRowText($filterItems, 1, 'First line Second line');
        table.header.closeTableHeaderMenu();
      });

      it('strips html tags if html is enabled', function() {
        var table = createSingleColumnTableByTexts(['<b>contains html</b>', '<ul><li>line 1</li><li>line 2</li></ul>']);
        table.rows[0].cells[0].htmlEnabled = true;
        table.rows[1].cells[0].htmlEnabled = true;
        var column = table.columns[0];
        table.render();
        table.header.openTableHeaderMenu(column);
        var $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'contains html');
        expectTableRowText($filterItems, 1, 'line 1 line 2');
        table.header.closeTableHeaderMenu();
      });

      it('reflects the state of the filter', function() {
        var table = createSingleColumnTableByTexts(['Value', 'AnotherValue', 'Value']);
        var column = table.columns[0];
        var filter = createAndRegisterColumnFilter(table, column, ['AnotherValue']);
        table.filter();
        table.render();
        expect(table.filteredRows().length).toBe(1);

        table.header.openTableHeaderMenu(column);
        var $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'AnotherValue');
        expectTableRowText($filterItems, 1, 'Value');
        expect($filterItems.eq(0)).toHaveClass('checked');
        expect($filterItems.eq(1)).not.toHaveClass('checked');
        table.header.closeTableHeaderMenu();
      });

      it('correctly updates the list after inserting a new row, if a filter is applied', function() {
        var table = createSingleColumnTableByTexts(['Value', 'AnotherValue', 'Value']);
        var column = table.columns[0];
        var filter = createAndRegisterColumnFilter(table, column, ['AnotherValue']);
        table.filter();
        table.render();
        expect(table.filteredRows().length).toBe(1);

        table.header.openTableHeaderMenu(column);
        var $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'AnotherValue');
        expectTableRowText($filterItems, 1, 'Value');
        table.header.closeTableHeaderMenu();

        var newRows = helper.createModelRows(2, 1);
        newRows[0].cells[0].text = 'NewValue';
        table.insertRows(newRows);

        table.header.openTableHeaderMenu(column);
        $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'AnotherValue');
        expectTableRowText($filterItems, 1, 'NewValue');
        expectTableRowText($filterItems, 2, 'Value');
        table.header.closeTableHeaderMenu();
      });

      it('always displays the selected value, even if the table does not contain the value anymore', function() {
        var table = createSingleColumnTableByTexts(['Value', 'AnotherValue', 'Value']);
        var column = table.columns[0];
        var filter = createAndRegisterColumnFilter(table, column, ['AnotherValueNotInTable']);
        table.filter();
        table.render();
        expect(table.filteredRows().length).toBe(0);

        table.header.openTableHeaderMenu(column);
        var $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'AnotherValue');
        expectTableRowText($filterItems, 1, 'AnotherValueNotInTable');
        expectTableRowText($filterItems, 2, 'Value');
        table.header.closeTableHeaderMenu();
      });

      it('displays empty values as -empty-', function() {
        session.text = function(key) {
          if (key === 'ui.EmptyCell') {
            return '-empty-';
          }
        };
        var table = createSingleColumnTableByTexts(['', 'Value']);
        var column = table.columns[0];
        table.render();
        table.header.openTableHeaderMenu(column);
        var $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'Value');
        expectTableRowText($filterItems, 1, '-empty-');
        table.header.closeTableHeaderMenu();
      });

      it('stores selected text in filter.selectedValues', function() {
        var table = createSingleColumnTableByTexts(['Value', 'Value2']);
        var column = table.columns[0];
        table.render();
        table.header.openTableHeaderMenu(column);
        var $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'Value');
        expectTableRowText($filterItems, 1, 'Value2');
        $filterItems.eq(0).triggerClick();
        expect(table.getFilter(table.columns[0].id).selectedValues).toEqual(['Value']);
        $filterItems.eq(1).triggerClick();
        expect(table.getFilter(table.columns[0].id).selectedValues).toEqual(['Value', 'Value2']);
        table.header.closeTableHeaderMenu();
      });

      it('stores empty as null and not \'-empty-\'', function() {
        session.text = function(key) {
          if (key === 'ui.EmptyCell') {
            return '-empty-';
          }
        };
        var table = createSingleColumnTableByTexts(['Value', '']);
        var column = table.columns[0];
        table.render();
        table.header.openTableHeaderMenu(column);
        var $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'Value');
        expectTableRowText($filterItems, 1, '-empty-');
        $filterItems.eq(1).triggerClick();
        table.header.closeTableHeaderMenu();

        expect(table.getFilter(table.columns[0].id).selectedValues).toEqual([null]);
      });
    });

    describe('grouping / sorting', function() {

      var table, column;

      beforeEach(function() {
        table = createSingleColumnTableByTexts(['Foo']);
        column = table.columns[0];
        table.render();
        table.header.openTableHeaderMenu(column);
      });

      it('count sorted columns', function() {
        expect(table.header._tableHeaderMenu._sortColumnCount()).toBe(0);
        table.header.closeTableHeaderMenu();

        table.sort(column, 'asc');
        table.header.openTableHeaderMenu(column);
        expect(table.header._tableHeaderMenu._sortColumnCount()).toBe(1);
        table.header.closeTableHeaderMenu();
      });

      it('count grouped columns', function() {
        expect(table.header._tableHeaderMenu._groupColumnCount()).toBe(0);
        table.header.closeTableHeaderMenu();

        table.groupColumn(column, false, 'asc');
        table.header.openTableHeaderMenu(column);
        expect(table.header._tableHeaderMenu._groupColumnCount()).toBe(1);
        table.header.closeTableHeaderMenu();
      });

    });

    describe('boolean column', function() {

      it('shows the unique string values', function() {
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
        table.render();
        table.header.openTableHeaderMenu(column);
        var $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'marked');
        expectTableRowText($filterItems, 1, 'unmarked');
        table.header.closeTableHeaderMenu();
      });

    });

    describe('sort enabled', function() {

      it('option enabled shows sort options in table header menu', function() {
        var table = createSingleColumnTableByTexts(['First', 'Second']);
        var column = table.columns[0];
        table.sortEnabled = true;
        table.render();
        table.header.openTableHeaderMenu(column);
        var $menu = table.header._tableHeaderMenu.$container;
        expect($menu.find('.table-header-menu-command.sort-asc').length).toBe(1);
        expect($menu.find('.table-header-menu-command.sort-desc').length).toBe(1);
        table.header.closeTableHeaderMenu();
      });

      it('option disabled does not show sort options in table header menu', function() {
        var table = createSingleColumnTableByTexts(['First', 'Second']);
        var column = table.columns[0];
        table.sortEnabled = false;
        table.render();
        table.header.openTableHeaderMenu(column);
        var $menu = table.header._tableHeaderMenu.$container;
        expect($menu.find('.table-header-menu-command.sort-asc').length).toBe(0);
        expect($menu.find('.table-header-menu-command.sort-desc').length).toBe(0);
        table.header.closeTableHeaderMenu();
      });

    });

    describe('sort mode', function() {

      it('sorts alphabetically', function() {
        var table = createSingleColumnTableByTexts(['BValue', 'AValue']);
        var column = table.columns[0];
        table.render();
        table.header.openTableHeaderMenu(column);
        var tableHeaderMenu = table.header._tableHeaderMenu;
        var $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'AValue');
        expectTableRowText($filterItems, 1, 'BValue');
        expect(tableHeaderMenu.filterSortMode).toBe(scout.TableHeaderMenu.SortMode.ALPHABETICALLY);
        table.header.closeTableHeaderMenu();
      });

      it('sorts by amount', function() {
        var table = createSingleColumnTableByTexts(['BValue', 'AValue', 'BValue']);
        var column = table.columns[0];
        table.render();
        table.header.openTableHeaderMenu(column);
        var tableHeaderMenu = table.header._tableHeaderMenu;
        tableHeaderMenu._onSortModeClick(); // changes sort mode from 'alphabetically' (default) to 'amount'
        var $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'BValue');
        expectTableRowText($filterItems, 1, 'AValue');
        expect(tableHeaderMenu.filterSortMode).toBe(scout.TableHeaderMenu.SortMode.AMOUNT);
        table.header.closeTableHeaderMenu();
      });

    });
  });
});
