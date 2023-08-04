/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Column, ColumnUserFilter, Table, TableHeaderMenu} from '../../src/index';
import {JQueryTesting, TableSpecHelper} from '../../src/testing/index';

describe('TableHeaderMenu', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();

    session.textMap.add('ColumnSorting', 'Sorting');
    session.textMap.add('ui.ascending', 'ascending');
  });

  afterEach(() => {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createAndRegisterColumnFilter(table: Table, column: Column<any>, selectedValues: (string | number)[]): ColumnUserFilter {
    let filter = new ColumnUserFilter();
    helper.createAndRegisterColumnFilter({
      session: session,
      table: table,
      column: column,
      selectedValues: selectedValues
    });
    return filter;
  }

  function find$FilterItems(table) {
    let $menu = table.header.tableHeaderMenu.$container;
    return $menu.find('.table-data > .table-row');
  }

  function expectTableRowText($row, index, expectedText) {
    let $cell = $row.eq(index).find('.table-cell');
    expect($cell.eq(0).text()).toBe(expectedText);
  }

  function createSingleColumnTableByTexts(texts) {
    let model = helper.createModelSingleColumnByTexts(texts);
    return helper.createTable(model);
  }

  function createSingleColumnTableByValues(values) {
    let model = helper.createModelSingleColumnByValues(values, 'BooleanColumn');
    return helper.createTable(model);
  }

  describe('filter', () => {

    describe('string column', () => {

      it('shows the unique string values', () => {
        let table = createSingleColumnTableByTexts(['Value', 'AnotherValue', 'Value']);
        let column = table.columns[0];
        table.render();
        table.header.openHeaderMenu(column);
        let $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'AnotherValue');
        expectTableRowText($filterItems, 1, 'Value');
        table.header.closeHeaderMenu();
      });

      it('converts multiline text to single line', () => {
        let table = createSingleColumnTableByTexts(['First line\nSecond line', 'AnotherValue']);
        let column = table.columns[0];
        table.render();
        table.header.openHeaderMenu(column);
        let $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'AnotherValue');
        expectTableRowText($filterItems, 1, 'First line Second line');
        table.header.closeHeaderMenu();
      });

      it('strips html tags if html is enabled', () => {
        let table = createSingleColumnTableByTexts(['<b>contains html</b>', '<ul><li>line 1</li><li>line 2</li></ul>']);
        table.rows[0].cells[0].htmlEnabled = true;
        table.rows[1].cells[0].htmlEnabled = true;
        let column = table.columns[0];
        table.render();
        table.header.openHeaderMenu(column);
        let $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'contains html');
        expectTableRowText($filterItems, 1, 'line 1 line 2');
        table.header.closeHeaderMenu();
      });

      it('reflects the state of the filter', () => {
        let table = createSingleColumnTableByTexts(['Value', 'AnotherValue', 'Value']);
        let column = table.columns[0];
        createAndRegisterColumnFilter(table, column, ['AnotherValue']);
        table.render();
        expect(table.filteredRows().length).toBe(1);

        table.header.openHeaderMenu(column);
        let $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'AnotherValue');
        expectTableRowText($filterItems, 1, 'Value');
        expect($filterItems.eq(0)).toHaveClass('checked');
        expect($filterItems.eq(1)).not.toHaveClass('checked');
        table.header.closeHeaderMenu();
      });

      it('correctly updates the list after inserting a new row, if a filter is applied', () => {
        let table = createSingleColumnTableByTexts(['Value', 'AnotherValue', 'Value']);
        let column = table.columns[0];
        createAndRegisterColumnFilter(table, column, ['AnotherValue']);
        table.render();
        expect(table.filteredRows().length).toBe(1);

        table.header.openHeaderMenu(column);
        let $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'AnotherValue');
        expectTableRowText($filterItems, 1, 'Value');
        table.header.closeHeaderMenu();

        let newRows = helper.createModelRows(2, 1);
        newRows[0].cells[0].text = 'NewValue';
        table.insertRows(newRows);

        table.header.openHeaderMenu(column);
        $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'AnotherValue');
        expectTableRowText($filterItems, 1, 'NewValue');
        expectTableRowText($filterItems, 2, 'Value');
        table.header.closeHeaderMenu();
      });

      it('always displays the selected value, even if the table does not contain the value anymore', () => {
        let table = createSingleColumnTableByTexts(['Value', 'AnotherValue', 'Value']);
        let column = table.columns[0];
        createAndRegisterColumnFilter(table, column, ['AnotherValueNotInTable']);
        table.render();
        expect(table.filteredRows().length).toBe(0);

        table.header.openHeaderMenu(column);
        let $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'AnotherValue');
        expectTableRowText($filterItems, 1, 'AnotherValueNotInTable');
        expectTableRowText($filterItems, 2, 'Value');
        table.header.closeHeaderMenu();
      });

      it('displays empty values as -empty-', () => {
        session.text = key => {
          if (key === 'ui.EmptyCell') {
            return '-empty-';
          }
        };
        let table = createSingleColumnTableByTexts(['', 'Value']);
        let column = table.columns[0];
        table.render();
        table.header.openHeaderMenu(column);
        let $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'Value');
        expectTableRowText($filterItems, 1, '-empty-');
        table.header.closeHeaderMenu();
      });

      it('stores selected text in filter.selectedValues', () => {
        let table = createSingleColumnTableByTexts(['Value', 'Value2']);
        let column = table.columns[0];
        table.render();
        table.header.openHeaderMenu(column);
        let $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'Value');
        expectTableRowText($filterItems, 1, 'Value2');
        JQueryTesting.triggerClick($filterItems.eq(0));
        let filter = table.getFilter(table.columns[0].id) as ColumnUserFilter;
        expect(filter.selectedValues).toEqual(['Value']);
        JQueryTesting.triggerClick($filterItems.eq(1));
        expect(filter.selectedValues).toEqual(['Value', 'Value2']);
        table.header.closeHeaderMenu();
      });

      it('stores empty as null and not \'-empty-\'', () => {
        session.text = key => {
          if (key === 'ui.EmptyCell') {
            return '-empty-';
          }
        };
        let table = createSingleColumnTableByTexts(['Value', '']);
        let column = table.columns[0];
        table.render();
        table.header.openHeaderMenu(column);
        let $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'Value');
        expectTableRowText($filterItems, 1, '-empty-');
        JQueryTesting.triggerClick($filterItems.eq(1));
        table.header.closeHeaderMenu();

        let filter = table.getFilter(table.columns[0].id) as ColumnUserFilter;
        expect(filter.selectedValues).toEqual([null]);
      });
    });

    describe('grouping / sorting', () => {

      let table, column;

      beforeEach(() => {
        table = createSingleColumnTableByTexts(['Foo']);
        column = table.columns[0];
        table.render();
        table.header.openHeaderMenu(column);
      });

      it('count sorted columns', () => {
        expect(table.header.tableHeaderMenu._sortColumnCount()).toBe(0);
        table.header.closeHeaderMenu();

        table.sort(column, 'asc');
        table.header.openHeaderMenu(column);
        expect(table.header.tableHeaderMenu._sortColumnCount()).toBe(1);
        table.header.closeHeaderMenu();
      });

      it('count grouped columns', () => {
        expect(table.header.tableHeaderMenu._groupColumnCount()).toBe(0);
        table.header.closeHeaderMenu();

        table.groupColumn(column, false, 'asc');
        table.header.openHeaderMenu(column);
        expect(table.header.tableHeaderMenu._groupColumnCount()).toBe(1);
        table.header.closeHeaderMenu();
      });

    });

    describe('boolean column', () => {

      it('shows the unique string values', () => {
        session.text = key => {
          if (key === 'ui.BooleanColumnGroupingTrue') {
            return 'marked';
          } else if (key === 'ui.BooleanColumnGroupingFalse') {
            return 'unmarked';
          }
        };
        let table = createSingleColumnTableByValues([true, false, true]);
        let column = table.columns[0];
        column.type = 'boolean';
        table.render();
        table.header.openHeaderMenu(column);
        let $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'marked');
        expectTableRowText($filterItems, 1, 'unmarked');
        table.header.closeHeaderMenu();
      });

    });

    describe('sort enabled', () => {

      it('option enabled shows sort options in table header menu', () => {
        let table = createSingleColumnTableByTexts(['First', 'Second']);
        let column = table.columns[0];
        table.sortEnabled = true;
        table.render();
        table.header.openHeaderMenu(column);
        let $menu = table.header.tableHeaderMenu.$container;
        expect($menu.find('.table-header-menu-command.sort-asc').length).toBe(1);
        expect($menu.find('.table-header-menu-command.sort-desc').length).toBe(1);
        table.header.closeHeaderMenu();
      });

      it('option disabled does not show sort options in table header menu', () => {
        let table = createSingleColumnTableByTexts(['First', 'Second']);
        let column = table.columns[0];
        table.sortEnabled = false;
        table.render();
        table.header.openHeaderMenu(column);
        let $menu = table.header.tableHeaderMenu.$container;
        expect($menu.find('.table-header-menu-command.sort-asc').length).toBe(0);
        expect($menu.find('.table-header-menu-command.sort-desc').length).toBe(0);
        table.header.closeHeaderMenu();
      });

    });

    describe('sort mode', () => {

      it('sorts alphabetically', () => {
        let table = createSingleColumnTableByTexts(['BValue', 'AValue']);
        let column = table.columns[0];
        table.render();
        table.header.openHeaderMenu(column);
        let tableHeaderMenu = table.header.tableHeaderMenu;
        let $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'AValue');
        expectTableRowText($filterItems, 1, 'BValue');
        expect(tableHeaderMenu.filterSortMode).toBe(TableHeaderMenu.SortMode.ALPHABETICALLY);
        table.header.closeHeaderMenu();
      });

      it('sorts by amount', () => {
        let table = createSingleColumnTableByTexts(['BValue', 'AValue', 'BValue']);
        let column = table.columns[0];
        table.render();
        table.header.openHeaderMenu(column);
        let tableHeaderMenu = table.header.tableHeaderMenu;
        // @ts-expect-error
        tableHeaderMenu._onSortModeClick(); // changes sort mode from 'alphabetically' (default) to 'amount'
        let $filterItems = find$FilterItems(table);
        expectTableRowText($filterItems, 0, 'BValue');
        expectTableRowText($filterItems, 1, 'AValue');
        expect(tableHeaderMenu.filterSortMode).toBe(TableHeaderMenu.SortMode.AMOUNT);
        table.header.closeHeaderMenu();
      });

    });

    describe('aria properties', () => {

      it('has table header menus with aria role button', () => {
        let table = createSingleColumnTableByTexts(['Value']);
        let column = table.columns[0];
        table.render();
        table.header.openHeaderMenu(column);
        let tableHeaderMenu = table.header.tableHeaderMenu;
        expect(tableHeaderMenu.groupButton.$container).toHaveAttr('role', 'button');
        table.header.closeHeaderMenu();
      });

      it('has aria-pressed set if selected', () => {
        let table = createSingleColumnTableByTexts(['Value']);
        let column = table.columns[0];
        table.render();
        table.header.openHeaderMenu(column);
        let tableHeaderMenu = table.header.tableHeaderMenu;
        expect(tableHeaderMenu.groupButton.$container).toHaveAttr('role', 'button');
        expect(tableHeaderMenu.groupButton.$container).toHaveAttr('aria-pressed', 'false');
        tableHeaderMenu.groupButton.setSelected(true);
        expect(tableHeaderMenu.groupButton.$container).toHaveAttr('aria-pressed', 'true');
        table.header.closeHeaderMenu();
      });

      it('has aria-labelledBy', () => {
        let table = createSingleColumnTableByTexts(['Value']);
        let column = table.columns[0];
        table.render();
        table.header.openHeaderMenu(column);
        let tableHeaderMenu = table.header.tableHeaderMenu;
        expect(tableHeaderMenu.groupButton.$container.attr('aria-labelledby')).toBeTruthy();
        expect(tableHeaderMenu.groupButton.$container.attr('aria-labelledby')).toBe(tableHeaderMenu.groupButton.parent.$text.attr('id'));
        expect(tableHeaderMenu.groupButton.$container.attr('aria-label')).toBeFalsy();
        table.header.closeHeaderMenu();
      });
    });
  });
});
