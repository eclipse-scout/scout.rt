/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, Column, ContextMenuPopup, DateColumn, DateColumnTableHeaderMenu, DateColumnUserFilter, DateGroupType, DateGroupTypeMenu, dates, icons, scout, Table, TableHeaderMenu, TableHeaderMenuGroup, TableRow} from '../../src/index';
import {JQueryTesting, TableSpecHelper} from '../../src/testing/index';

describe('DateColumnTableHeaderMenu', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);

    session.textMap.add('ColumnSorting', 'Sorting');
    session.textMap.add('ui.ascending', 'ascending');
    session.textMap.add('ui.EmptyCell', '-empty-');
    session.textMap.add('ui.CW', 'CW {0}');
    session.textMap.add('DateGroupTypeDate', 'Date');
    session.textMap.add('DateGroupTypeMonth', 'Month');
    session.textMap.add('DateGroupTypeMonthAndYear', 'Month and year');
    session.textMap.add('DateGroupTypeWeekOfYear', 'Week of year');
    session.textMap.add('DateGroupTypeWeekday', 'Weekday');
    session.textMap.add('DateGroupTypeYear', 'Year');
    session.textMap.add('ui.Grouping', 'Grouping');
    session.textMap.add('ui.GroupingBy', 'Grouping by');
    session.textMap.add('ui.remove', 'remove');
    session.textMap.add('ui.groupingApply', 'apply');
  });

  afterEach(() => {
    // Destroy all widgets, including still open popups and their global 'mouse down outside' listeners.
    session.desktop.destroy();
  });

  describe('filter', () => {
    let table: Table;
    let dateColumn: DateColumn;

    beforeEach(() => {
      table = helper.createTable(helper.createModelSingleColumnByValues([
        dates.create('2028-03-26'), // Sun
        null,
        dates.create('2008-04-14'), // Mon
        null,
        dates.create('2008-02-12'), // Tue
        dates.create('2016-02-16') // Tue
      ], DateColumn));
      table.render();
      dateColumn = scout.assertInstance(table.columns[0], DateColumn);
    });

    it('shows the dates formatted with the select group type', () => {
      table.header.openHeaderMenu(dateColumn);
      let headerMenu = scout.assertInstance(table.header.tableHeaderMenu, DateColumnTableHeaderMenu);
      headerMenu.animateRemoval = false;
      let filterTable = headerMenu.filterTable;

      let filterItems = filterTable.rows.map(row => filterTable.columns[0].cellText(row));
      expect(filterItems).toEqual([
        '2008',
        '2016',
        '2028',
        '-empty-'
      ]);

      // --------

      const checkCurrentGroupType = (expectedGroupType: DateGroupType) => {
        expect(headerMenu.filterGroupTypeAction).toBeInstanceOf(Action);
        headerMenu.filterGroupTypeAction.doAction();

        let contextMenu = headerMenu.findChild(ContextMenuPopup);
        expect(contextMenu).toBeInstanceOf(ContextMenuPopup);
        contextMenu.animateRemoval = false;

        let menus = contextMenu.menuItems.filter(menu => menu instanceof DateGroupTypeMenu) as DateGroupTypeMenu[];
        expect(menus.map(menu => menu.groupType)).toEqual([
          DateGroupType.YEAR,
          DateGroupType.MONTH,
          DateGroupType.MONTH_AND_YEAR,
          DateGroupType.CALENDAR_WEEK,
          DateGroupType.WEEKDAY,
          DateGroupType.DATE
        ]);
        let checkedMenus = menus.filter(menu => menu.iconId === icons.CHECKED_BOLD);
        expect(checkedMenus.length).toBe(1);
        expect(checkedMenus[0].groupType).toBe(expectedGroupType);

        // Toggle
        headerMenu.filterGroupTypeAction.doAction();
        expect(contextMenu.destroyed).toBe(true);
      };

      const changeGroupTypeAndCheckFilterItems = (groupType: DateGroupType, expectedFilterItems: string[]) => {
        expect(headerMenu.filterGroupTypeAction).toBeInstanceOf(Action);
        headerMenu.filterGroupTypeAction.doAction();

        let contextMenu = headerMenu.findChild(ContextMenuPopup);
        expect(contextMenu).toBeInstanceOf(ContextMenuPopup);
        contextMenu.animateRemoval = false;

        let menus = contextMenu.menuItems.filter(menu => menu instanceof DateGroupTypeMenu) as DateGroupTypeMenu[];

        let targetMenu = menus.find(menu => menu.groupType === groupType);
        expect(targetMenu).toBeInstanceOf(DateGroupTypeMenu);
        targetMenu.doAction();
        expect(contextMenu.destroyed).toBe(true);

        let filterItems = filterTable.rows.map(row => filterTable.columns[0].cellText(row));
        expect(filterItems).toEqual(expectedFilterItems);
      };

      // --------

      checkCurrentGroupType(DateGroupType.YEAR);
      changeGroupTypeAndCheckFilterItems(DateGroupType.YEAR, ['2008', '2016', '2028', '-empty-']);
      checkCurrentGroupType(DateGroupType.YEAR);
      changeGroupTypeAndCheckFilterItems(DateGroupType.MONTH, ['Februar', 'März', 'April', '-empty-']);
      checkCurrentGroupType(DateGroupType.MONTH);
      changeGroupTypeAndCheckFilterItems(DateGroupType.MONTH_AND_YEAR, ['Februar 2008', 'April 2008', 'Februar 2016', 'März 2028', '-empty-']);
      checkCurrentGroupType(DateGroupType.MONTH_AND_YEAR);
      changeGroupTypeAndCheckFilterItems(DateGroupType.CALENDAR_WEEK, ['CW 7', 'CW 12', 'CW 16', '-empty-']);
      checkCurrentGroupType(DateGroupType.CALENDAR_WEEK);
      changeGroupTypeAndCheckFilterItems(DateGroupType.WEEKDAY, ['Montag', 'Dienstag', 'Sonntag', '-empty-']);
      checkCurrentGroupType(DateGroupType.WEEKDAY);
      changeGroupTypeAndCheckFilterItems(DateGroupType.DATE, ['12.02.2008', '14.04.2008', '16.02.2016', '26.03.2028', '-empty-']);
      checkCurrentGroupType(DateGroupType.DATE);
    });

    it('reflects the state of the filter', () => {
      let filter = dateColumn.createFilter() as DateColumnUserFilter;
      filter.groupType = DateGroupType.MONTH;
      filter.selectedValues = [1, 7, null];
      table.addFilter(filter);
      expect(table.filteredRows().length).toBe(4);

      table.header.openHeaderMenu(dateColumn);
      let headerMenu = scout.assertInstance(table.header.tableHeaderMenu, DateColumnTableHeaderMenu);
      headerMenu.animateRemoval = false;
      let filterTable = headerMenu.filterTable;

      // --------

      let filterItems = filterTable.rows.map(row => filterTable.columns[0].cellText(row));
      expect(filterItems).toEqual([
        'Februar',
        'März',
        'April',
        'August',
        '-empty-'
      ]);
      let checkedItems = filterTable.checkedRows().map(row => filterTable.columns[0].cellText(row));
      expect(checkedItems).toEqual([
        'Februar',
        'August',
        '-empty-'
      ]);
    });

    it('saves the selected group type in the filter', () => {
      expect(dateColumn.filtered).toBe(false);
      expect(table.getFilter(dateColumn.id)).toBe(null);

      table.header.openHeaderMenu(dateColumn);
      let headerMenu = scout.assertInstance(table.header.tableHeaderMenu, DateColumnTableHeaderMenu);
      headerMenu.animateRemoval = false;
      let filterTable = headerMenu.filterTable;

      // --------

      expect(headerMenu.filterGroupTypeAction).toBeInstanceOf(Action);
      headerMenu.filterGroupTypeAction.doAction();

      let contextMenu = headerMenu.findChild(ContextMenuPopup);
      expect(contextMenu).toBeInstanceOf(ContextMenuPopup);
      contextMenu.animateRemoval = false;

      let menus = contextMenu.menuItems.filter(menu => menu instanceof DateGroupTypeMenu) as DateGroupTypeMenu[];

      let targetMenu = menus.find(menu => menu.groupType === DateGroupType.WEEKDAY);
      expect(targetMenu).toBeInstanceOf(DateGroupTypeMenu);
      targetMenu.doAction();
      expect(contextMenu.destroyed).toBe(true);

      // --------

      expect(dateColumn.filtered).toBe(false);
      expect(table.getFilter(dateColumn.id)).toBe(null);
      let itemsToCheck = filterTable.rows.filter(row => scout.isOneOf(filterTable.columns[0].cellText(row), 'Dienstag', '-empty-'));
      filterTable.checkRows(itemsToCheck);

      expect(dateColumn.filtered).toBe(true);
      expect(table.filteredRows().length).toBe(4);
      let filter = table.getFilter(dateColumn.id) as DateColumnUserFilter;
      expect(filter).toBeInstanceOf(DateColumnUserFilter);
      expect(filter.selectedValues).toEqual([2, null]);
      expect(filter.groupType).toBe(DateGroupType.WEEKDAY);

      table.header.closeHeaderMenu();

      // --------

      // Open again
      table.header.openHeaderMenu(dateColumn);
      headerMenu = scout.assertInstance(table.header.tableHeaderMenu, DateColumnTableHeaderMenu);
      headerMenu.animateRemoval = false;
      filterTable = headerMenu.filterTable;

      let checkedItems = filterTable.checkedRows().map(row => filterTable.columns[0].cellText(row));
      expect(checkedItems).toEqual([
        'Dienstag',
        '-empty-'
      ]);
    });

    it('preserves the last selected group type even if no filter is selected', () => {
      table.header.openHeaderMenu(dateColumn);
      let headerMenu = scout.assertInstance(table.header.tableHeaderMenu, DateColumnTableHeaderMenu);
      headerMenu.animateRemoval = false;
      let filterTable = headerMenu.filterTable;

      // --------

      expect(headerMenu.filterGroupTypeAction).toBeInstanceOf(Action);
      headerMenu.filterGroupTypeAction.doAction();

      let contextMenu = headerMenu.findChild(ContextMenuPopup);
      expect(contextMenu).toBeInstanceOf(ContextMenuPopup);
      contextMenu.animateRemoval = false;

      let menus = contextMenu.menuItems.filter(menu => menu instanceof DateGroupTypeMenu) as DateGroupTypeMenu[];

      let targetMenu = menus.find(menu => menu.groupType === DateGroupType.CALENDAR_WEEK);
      expect(targetMenu).toBeInstanceOf(DateGroupTypeMenu);
      targetMenu.doAction();
      expect(contextMenu.destroyed).toBe(true);

      // --------

      let filterItems = filterTable.rows.map(row => filterTable.columns[0].cellText(row));
      expect(filterItems).toEqual([
        'CW 7',
        'CW 12',
        'CW 16',
        '-empty-'
      ]);

      table.header.closeHeaderMenu();

      // --------

      // Open again
      table.header.openHeaderMenu(dateColumn);
      headerMenu = scout.assertInstance(table.header.tableHeaderMenu, DateColumnTableHeaderMenu);
      headerMenu.animateRemoval = false;
      filterTable = headerMenu.filterTable;

      filterItems = filterTable.rows.map(row => filterTable.columns[0].cellText(row));
      expect(filterItems).toEqual([
        'CW 7',
        'CW 12',
        'CW 16',
        '-empty-'
      ]);

      // --------

      expect(headerMenu.filterGroupTypeAction).toBeInstanceOf(Action);
      headerMenu.filterGroupTypeAction.doAction();

      contextMenu = headerMenu.findChild(ContextMenuPopup);
      expect(contextMenu).toBeInstanceOf(ContextMenuPopup);
      contextMenu.animateRemoval = false;

      menus = contextMenu.menuItems.filter(menu => menu instanceof DateGroupTypeMenu) as DateGroupTypeMenu[];
      let checkedMenus = menus.filter(menu => menu.iconId === icons.CHECKED_BOLD);
      expect(checkedMenus.length).toBe(1);
      expect(checkedMenus[0].groupType).toBe(DateGroupType.CALENDAR_WEEK);
    });
  });

  describe('grouping / sorting', () => {

    let table: Table;
    let dateColumn1: DateColumn;
    let dateColumn2: DateColumn;
    let stringColumn1: Column;
    let stringColumn2: Column;
    let row0: TableRow;
    let row1: TableRow;
    let row2: TableRow;
    let row3: TableRow;

    beforeEach(() => {
      table = helper.createTable(helper.createModel(
        [
          helper.createModelColumn('col0', DateColumn),
          helper.createModelColumn('col1', DateColumn),
          helper.createModelColumn('col2', Column),
          helper.createModelColumn('col3', Column)
        ],
        [
          // (Sun, CW 12) | (Mon, CW 8)
          helper.createModelRowByValues('row0', [dates.create('2028-03-26'), dates.create('2028-02-21'), 'aaa', 'zzz']),
          // (Mon, CW 16) | null
          helper.createModelRowByValues('row1', [dates.create('2008-04-14'), null, 'aaa', 'zzz']),
          // (Tue, CW 7) | (Wed, CW 8)
          helper.createModelRowByValues('row2', [dates.create('2008-02-12'), dates.create('2008-02-20'), 'bbb', 'yyy']),
          // (Tue, CW 7) | null
          helper.createModelRowByValues('row3', [dates.create('2016-02-16'), null, 'aaa', 'xxx'])
        ]
      ));
      table.render();
      dateColumn1 = scout.assertInstance(table.columns[0], DateColumn);
      dateColumn2 = scout.assertInstance(table.columns[1], DateColumn);
      stringColumn1 = scout.assertInstance(table.columns[2], Column<string>);
      stringColumn2 = scout.assertInstance(table.columns[3], Column<string>);
      row0 = scout.assertInstance(table.rows[0], TableRow);
      row1 = scout.assertInstance(table.rows[1], TableRow);
      row2 = scout.assertInstance(table.rows[2], TableRow);
      row3 = scout.assertInstance(table.rows[3], TableRow);
    });

    it('shows a popup to define the date group type', () => {
      table.header.openHeaderMenu(dateColumn1);
      let headerMenu = scout.assertInstance(table.header.tableHeaderMenu, DateColumnTableHeaderMenu);
      headerMenu.animateRemoval = false;

      expect(headerMenu.groupButton.visible).toBe(true);
      expect(headerMenu.groupButton.selected).toBe(false);
      expect(headerMenu.groupAddButton.visible).toBe(false);

      headerMenu.groupButton.doAction();
      let contextMenu = headerMenu.findChild(ContextMenuPopup);
      expect(contextMenu).toBeInstanceOf(ContextMenuPopup);
      contextMenu.animateRemoval = false;

      let menus = contextMenu.menuItems.filter(menu => menu instanceof DateGroupTypeMenu) as DateGroupTypeMenu[];
      expect(menus.map(menu => menu.groupType)).toEqual([
        DateGroupType.YEAR,
        DateGroupType.MONTH,
        DateGroupType.MONTH_AND_YEAR,
        DateGroupType.CALENDAR_WEEK,
        DateGroupType.WEEKDAY,
        DateGroupType.DATE
      ]);
      let checkedMenus = menus.filter(menu => menu.iconId === icons.CHECKED_BOLD);
      expect(checkedMenus.length).toBe(1);
      expect(checkedMenus[0].groupType).toBe(DateGroupType.YEAR);

      // Toggle
      headerMenu.groupButton.doAction();
      expect(contextMenu.destroyed).toBe(true);
    });

    it('shows a popup to define the date group type for additional columns', () => {
      table.group(dateColumn1);

      table.header.openHeaderMenu(dateColumn2);
      let headerMenu = scout.assertInstance(table.header.tableHeaderMenu, DateColumnTableHeaderMenu);
      headerMenu.animateRemoval = false;

      expect(headerMenu.groupButton.visible).toBe(true);
      expect(headerMenu.groupButton.selected).toBe(false);
      expect(headerMenu.groupAddButton.visible).toBe(true);
      expect(headerMenu.groupAddButton.selected).toBe(false);

      headerMenu.groupButton.doAction();
      let contextMenu1 = headerMenu.findChild(ContextMenuPopup);
      expect(contextMenu1).toBeInstanceOf(ContextMenuPopup);
      contextMenu1.animateRemoval = false;

      headerMenu.groupAddButton.doAction();
      let contextMenu2 = headerMenu.findChild(ContextMenuPopup);
      expect(contextMenu2).toBeInstanceOf(ContextMenuPopup);
      expect(contextMenu1.destroyed).toBe(true);
      contextMenu2.animateRemoval = false;

      let menus = contextMenu2.menuItems.filter(menu => menu instanceof DateGroupTypeMenu) as DateGroupTypeMenu[];
      expect(menus.map(menu => menu.groupType)).toEqual([
        DateGroupType.YEAR,
        DateGroupType.MONTH,
        DateGroupType.MONTH_AND_YEAR,
        DateGroupType.CALENDAR_WEEK,
        DateGroupType.WEEKDAY,
        DateGroupType.DATE
      ]);
      let checkedMenus = menus.filter(menu => menu.iconId === icons.CHECKED_BOLD);
      expect(checkedMenus.length).toBe(1);
      expect(checkedMenus[0].groupType).toBe(DateGroupType.YEAR);

      // Toggle
      headerMenu.groupButton.doAction();
      expect(contextMenu2.destroyed).toBe(true);
    });

    it('groups immediately without popup for non-date columns', () => {
      table.header.openHeaderMenu(stringColumn1);
      let headerMenu = scout.assertInstance(table.header.tableHeaderMenu, TableHeaderMenu);
      headerMenu.animateRemoval = false;

      expect(headerMenu.groupButton.visible).toBe(true);
      expect(headerMenu.groupButton.selected).toBe(false);
      expect(headerMenu.groupAddButton.visible).toBe(false);

      headerMenu.groupButton.doAction();
      let contextMenu = headerMenu.findChild(ContextMenuPopup);
      expect(contextMenu).toBe(null);
      expect(headerMenu.destroyed).toBe(true);
      expect(stringColumn1.grouped).toBe(true);
    });

    it('clears the grouping when clicking a selected group button', () => {
      table.group(stringColumn2);
      table.group(dateColumn2, undefined, true);

      table.header.openHeaderMenu(dateColumn2);
      let headerMenu = scout.assertInstance(table.header.tableHeaderMenu, TableHeaderMenu);
      headerMenu.animateRemoval = false;

      expect(headerMenu.groupButton.visible).toBe(true);
      expect(headerMenu.groupButton.selected).toBe(false);
      expect(headerMenu.groupAddButton.visible).toBe(true);
      expect(headerMenu.groupAddButton.selected).toBe(true);

      headerMenu.groupAddButton.doAction();
      let contextMenu = headerMenu.findChild(ContextMenuPopup);
      expect(contextMenu).toBe(null);
      expect(headerMenu.destroyed).toBe(true);
      expect(dateColumn2.grouped).toBe(false);
      expect(stringColumn2.grouped).toBe(true);
    });

    it('sorts table rows by selected group type', () => {
      expect(table.rows).toEqual([row0, row1, row2, row3]);

      table.group(dateColumn1); // default is DateGroupType.YEAR
      expect(table.rows).toEqual([row2, row1, row3, row0]);

      dateColumn1.setGroupType(DateGroupType.YEAR);
      table.group(dateColumn1);
      expect(table.rows).toEqual([row2, row1, row3, row0]);

      dateColumn1.setGroupType(DateGroupType.MONTH);
      table.group(dateColumn1);
      expect(table.rows).toEqual([row2, row3, row0, row1]);

      dateColumn1.setGroupType(DateGroupType.MONTH_AND_YEAR);
      table.group(dateColumn1);
      expect(table.rows).toEqual([row2, row1, row3, row0]);

      dateColumn1.setGroupType(DateGroupType.CALENDAR_WEEK);
      table.group(dateColumn1);
      expect(table.rows).toEqual([row2, row3, row0, row1]);

      dateColumn1.setGroupType(DateGroupType.WEEKDAY);
      table.group(dateColumn1);
      expect(table.rows).toEqual([row1, row2, row3, row0]);

      dateColumn1.setGroupType(DateGroupType.DATE);
      table.group(dateColumn1);
      expect(table.rows).toEqual([row2, row1, row3, row0]);
    });

    it('sorts table rows by selected group type and additional sort columns', () => {
      dateColumn1.setGroupType(DateGroupType.WEEKDAY);
      table.group(dateColumn1);
      expect(table.rows).toEqual([row1, row2, row3, row0]);

      table.sort(stringColumn1, 'asc', true);
      expect(table.rows).toEqual([row1, row3, row2, row0]);

      table.sort(stringColumn1, 'desc', true);
      expect(table.rows).toEqual([row1, row2, row3, row0]);

      // --------
      // nulls first

      dateColumn2.setGroupType(DateGroupType.MONTH);
      table.group(dateColumn2);
      expect(table.rows).toEqual([row1, row3, row2, row0]);

      table.sort(stringColumn2, 'asc', true);
      expect(table.rows).toEqual([row3, row1, row2, row0]);

      table.sort(stringColumn2, 'desc', true);
      expect(table.rows).toEqual([row1, row3, row2, row0]);

      table.sort(dateColumn2, 'desc', true);
      expect(table.rows).toEqual([row2, row0, row1, row3]);
    });

    it('allows changing the group type', () => {
      dateColumn1.setGroupType(DateGroupType.WEEKDAY);
      table.group(dateColumn1);
      expect(table.rows).toEqual([row1, row2, row3, row0]);

      table.header.openHeaderMenu(dateColumn1);
      let headerMenu = scout.assertInstance(table.header.tableHeaderMenu, DateColumnTableHeaderMenu);
      headerMenu.animateRemoval = false;

      expect(headerMenu.groupButton.visible).toBe(true);
      expect(headerMenu.groupButton.selected).toBe(true);
      expect(headerMenu.groupAddButton.visible).toBe(false);

      expect(headerMenu.groupingGroupTypeAction).toBeInstanceOf(Action);
      expect(headerMenu.groupingGroupTypeAction.text).toBe('Weekday');

      // ------

      headerMenu.groupingGroupTypeAction.doAction();
      let contextMenu = headerMenu.findChild(ContextMenuPopup);
      expect(contextMenu).toBeInstanceOf(ContextMenuPopup);
      contextMenu.animateRemoval = false;

      let menus = contextMenu.menuItems.filter(menu => menu instanceof DateGroupTypeMenu) as DateGroupTypeMenu[];
      expect(menus.map(menu => menu.groupType)).toEqual([
        DateGroupType.YEAR,
        DateGroupType.MONTH,
        DateGroupType.MONTH_AND_YEAR,
        DateGroupType.CALENDAR_WEEK,
        DateGroupType.WEEKDAY,
        DateGroupType.DATE
      ]);
      let checkedMenus = menus.filter(menu => menu.iconId === icons.CHECKED_BOLD);
      expect(checkedMenus.length).toBe(1);
      expect(checkedMenus[0].groupType).toBe(DateGroupType.WEEKDAY);

      // ------

      let targetMenu = menus.find(menu => menu.groupType === DateGroupType.CALENDAR_WEEK);
      expect(targetMenu).toBeInstanceOf(DateGroupTypeMenu);
      targetMenu.doAction();
      expect(contextMenu.destroyed).toBe(true);
      expect(headerMenu.destroyed).toBe(true);

      expect(dateColumn1.grouped).toBe(true);
      expect(dateColumn1.groupType).toBe(DateGroupType.CALENDAR_WEEK);
      expect(table.rows).toEqual([row2, row3, row0, row1]);

      // ------

      table.header.openHeaderMenu(dateColumn1);
      headerMenu = scout.assertInstance(table.header.tableHeaderMenu, DateColumnTableHeaderMenu);
      headerMenu.animateRemoval = false;

      expect(headerMenu.groupButton.visible).toBe(true);
      expect(headerMenu.groupButton.selected).toBe(true);
      expect(headerMenu.groupAddButton.visible).toBe(false);

      expect(headerMenu.groupingGroupTypeAction).toBeInstanceOf(Action);
      expect(headerMenu.groupingGroupTypeAction.text).toBe('Week of year');
    });

    it('shows an additional menu when the column specifies a non-standard groupFormat', () => {
      expect(table.rows).toEqual([row0, row1, row2, row3]);
      dateColumn1.setGroupFormat('MM-yy');

      table.header.openHeaderMenu(dateColumn1);
      let headerMenu = scout.assertInstance(table.header.tableHeaderMenu, DateColumnTableHeaderMenu);
      headerMenu.animateRemoval = false;

      expect(headerMenu.groupButton.visible).toBe(true);
      expect(headerMenu.groupButton.selected).toBe(false);
      expect(headerMenu.groupAddButton.visible).toBe(false);

      // ------

      headerMenu.groupButton.doAction();
      let contextMenu = headerMenu.findChild(ContextMenuPopup);
      expect(contextMenu).toBeInstanceOf(ContextMenuPopup);
      contextMenu.animateRemoval = false;

      let menus = contextMenu.menuItems.filter(menu => menu instanceof DateGroupTypeMenu) as DateGroupTypeMenu[];
      expect(menus.map(menu => menu.groupType)).toEqual([
        null,
        DateGroupType.YEAR,
        DateGroupType.MONTH,
        DateGroupType.MONTH_AND_YEAR,
        DateGroupType.CALENDAR_WEEK,
        DateGroupType.WEEKDAY,
        DateGroupType.DATE
      ]);
      let checkedMenus = menus.filter(menu => menu.iconId === icons.CHECKED_BOLD);
      expect(checkedMenus.length).toBe(1);
      expect(checkedMenus[0].groupType).toBe(null);
      expect(checkedMenus[0].text).toBe('MM-yy');

      // ------

      checkedMenus[0].doAction();
      expect(contextMenu.destroyed).toBe(true);
      expect(headerMenu.destroyed).toBe(true);

      expect(dateColumn1.grouped).toBe(true);
      expect(dateColumn1.groupType).toBe(null);
      expect(table.rows).toEqual([row2, row1, row3, row0]);

      // ------

      table.header.openHeaderMenu(dateColumn1);
      headerMenu = scout.assertInstance(table.header.tableHeaderMenu, DateColumnTableHeaderMenu);
      headerMenu.animateRemoval = false;

      expect(headerMenu.groupButton.visible).toBe(true);
      expect(headerMenu.groupButton.selected).toBe(true);
      expect(headerMenu.groupAddButton.visible).toBe(false);

      expect(headerMenu.groupingGroupTypeAction).toBeInstanceOf(Action);
      expect(headerMenu.groupingGroupTypeAction.text).toBe('MM-yy');

      // ------

      headerMenu.groupingGroupTypeAction.doAction();
      contextMenu = headerMenu.findChild(ContextMenuPopup);
      expect(contextMenu).toBeInstanceOf(ContextMenuPopup);
      contextMenu.animateRemoval = false;

      menus = contextMenu.menuItems.filter(menu => menu instanceof DateGroupTypeMenu) as DateGroupTypeMenu[];
      expect(menus.map(menu => menu.groupType)).toEqual([
        null,
        DateGroupType.YEAR,
        DateGroupType.MONTH,
        DateGroupType.MONTH_AND_YEAR,
        DateGroupType.CALENDAR_WEEK,
        DateGroupType.WEEKDAY,
        DateGroupType.DATE
      ]);
      checkedMenus = menus.filter(menu => menu.iconId === icons.CHECKED_BOLD);
      expect(checkedMenus.length).toBe(1);
      expect(checkedMenus[0].groupType).toBe(null);

      let targetMenu = menus.find(menu => menu.groupType === DateGroupType.MONTH_AND_YEAR);
      expect(targetMenu).toBeInstanceOf(DateGroupTypeMenu);
      targetMenu.doAction();
      expect(contextMenu.destroyed).toBe(true);
      expect(headerMenu.destroyed).toBe(true);

      expect(dateColumn1.grouped).toBe(true);
      expect(dateColumn1.groupType).toBe(DateGroupType.MONTH_AND_YEAR);
      expect(table.rows).toEqual([row2, row1, row3, row0]);

      // ------

      table.header.openHeaderMenu(dateColumn1);
      headerMenu = scout.assertInstance(table.header.tableHeaderMenu, DateColumnTableHeaderMenu);
      headerMenu.animateRemoval = false;

      expect(headerMenu.groupButton.visible).toBe(true);
      expect(headerMenu.groupButton.selected).toBe(true);
      expect(headerMenu.groupAddButton.visible).toBe(false);

      expect(headerMenu.groupingGroupTypeAction).toBeInstanceOf(Action);
      expect(headerMenu.groupingGroupTypeAction.text).toBe('Month and year');

      // ------

      headerMenu.groupingGroupTypeAction.doAction();
      contextMenu = headerMenu.findChild(ContextMenuPopup);
      expect(contextMenu).toBeInstanceOf(ContextMenuPopup);
      contextMenu.animateRemoval = false;

      menus = contextMenu.menuItems.filter(menu => menu instanceof DateGroupTypeMenu) as DateGroupTypeMenu[];
      expect(menus.map(menu => menu.groupType)).toEqual([
        null,
        DateGroupType.YEAR,
        DateGroupType.MONTH,
        DateGroupType.MONTH_AND_YEAR,
        DateGroupType.CALENDAR_WEEK,
        DateGroupType.WEEKDAY,
        DateGroupType.DATE
      ]);
      checkedMenus = menus.filter(menu => menu.iconId === icons.CHECKED_BOLD);
      expect(checkedMenus.length).toBe(1);
      expect(checkedMenus[0].groupType).toBe(DateGroupType.MONTH_AND_YEAR);
    });

    it('changes the label if a group button is hovered', () => {
      dateColumn1.setGroupType(DateGroupType.YEAR);
      dateColumn2.setGroupType(DateGroupType.MONTH);
      table.group(dateColumn1);
      table.group(dateColumn2, undefined, true);

      table.header.openHeaderMenu(dateColumn1);
      let headerMenu = scout.assertInstance(table.header.tableHeaderMenu, DateColumnTableHeaderMenu);
      headerMenu.animateRemoval = false;

      expect(headerMenu.groupButton.visible).toBe(true);
      expect(headerMenu.groupButton.selected).toBe(false);
      expect(headerMenu.groupAddButton.visible).toBe(true);
      expect(headerMenu.groupAddButton.selected).toBe(true);
      let menuGroup = headerMenu.groupButton.findParent(TableHeaderMenuGroup);
      expect(menuGroup).toBeInstanceOf(TableHeaderMenuGroup);

      expect(headerMenu.groupingGroupTypeAction).toBeInstanceOf(Action);
      expect(headerMenu.groupingGroupTypeAction.text).toBe('Year');
      expect(headerMenu.groupingGroupTypeAction.$container.isEveryParentVisible()).toBe(true);
      expect(menuGroup.$container.children('.table-header-menu-group-text:visible').text()).toBe('Grouping by Year');

      // ------

      // simulate focus, because 'focusin' event is not triggered in spec
      menuGroup.setFocusedGroupItem(headerMenu.groupButton);
      expect(headerMenu.groupingGroupTypeAction.$container.isEveryParentVisible()).toBe(false);
      expect(menuGroup.$container.children('.table-header-menu-group-text:visible').text()).toBe('Grouping apply');

      JQueryTesting.triggerMouseEnter(headerMenu.groupAddButton.$container);
      expect(headerMenu.groupingGroupTypeAction.$container.isEveryParentVisible()).toBe(false);
      expect(menuGroup.$container.children('.table-header-menu-group-text:visible').text()).toBe('Grouping remove');

      JQueryTesting.triggerMouseLeave(headerMenu.groupAddButton.$container);
      expect(headerMenu.groupingGroupTypeAction.$container.isEveryParentVisible()).toBe(false);
      expect(menuGroup.$container.children('.table-header-menu-group-text:visible').text()).toBe('Grouping apply');

      menuGroup.setFocusedGroupItem(null);
      expect(headerMenu.groupingGroupTypeAction.$container.isEveryParentVisible()).toBe(true);
      expect(menuGroup.$container.children('.table-header-menu-group-text:visible').text()).toBe('Grouping by Year');

      headerMenu.groupButton.doAction();
      let contextMenu = headerMenu.findChild(ContextMenuPopup);
      expect(contextMenu).toBeInstanceOf(ContextMenuPopup);
      contextMenu.animateRemoval = false;

      expect(headerMenu.groupingGroupTypeAction.$container.isEveryParentVisible()).toBe(false);
      expect(menuGroup.$container.children('.table-header-menu-group-text:visible').text()).toBe('Grouping apply');

      JQueryTesting.triggerMouseEnter(headerMenu.groupAddButton.$container);
      expect(headerMenu.groupingGroupTypeAction.$container.isEveryParentVisible()).toBe(false);
      expect(menuGroup.$container.children('.table-header-menu-group-text:visible').text()).toBe('Grouping remove');

      JQueryTesting.triggerMouseLeave(headerMenu.groupAddButton.$container);
      contextMenu.close();
      expect(headerMenu.groupingGroupTypeAction.$container.isEveryParentVisible()).toBe(true);
      expect(menuGroup.$container.children('.table-header-menu-group-text:visible').text()).toBe('Grouping by Year');
    });
  });
});
