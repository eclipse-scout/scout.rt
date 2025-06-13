/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {TableSpecHelper} from '../../../src/testing';
import {scout, ShowInvisibleColumnsForm, Table, TableOrganizerForm, TableOrganizerMenu} from '../../../src';

describe('TableOrganizerForm', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
  });

  afterEach(() => {
    session = null;
  });

  function createTable(numColumns: number) {
    let table = helper.createTable(helper.createModelFixture(numColumns));
    let menu = scout.create(TableOrganizerMenu, {parent: table});
    table.insertMenus([menu]);
    return table;
  }

  async function openOrganizerForm(table: Table): Promise<TableOrganizerForm> {
    let menu = table.menus[0] as TableOrganizerMenu;
    menu.setSelected(true);
    await menu.form.when('load');
    return menu.form;
  }

  describe('columns table', () => {
    it('shows rows for each column', async () => {
      let table = createTable(3);
      let form = await openOrganizerForm(table);
      expect(form.columnsTable.columnById('KeyColumn').cellValues()).toEqual(table.columns);
      expect(form.columnsTable.columnById('TitleColumn').cellValues()).toEqual(table.columns.map(column => column.text));
    });

    it('does not show guiOnly columns', async () => {
      let table = createTable(1);
      table.setCheckable(true);
      table.setRowIconVisible(true);
      let form = await openOrganizerForm(table);
      expect(form.columnsTable.columnById('KeyColumn').cellValues()).toEqual([table.columns[2]]);
      expect(form.columnsTable.columnById('TitleColumn').cellValues()).toEqual([table.columns[2].text]);
    });

    it('only adds visible columns', async () => {
      let table = createTable(3);
      table.columns[0].setDisplayable(false); // Not displayed at all
      table.columns[1].setVisible(false);
      let form = await openOrganizerForm(table);
      expect(form.columnsTable.columnById('KeyColumn').cellValues()).toEqual([table.columns[2]]);
    });

    it('uses tooltipText if text is empty', async () => {
      let table = createTable(3);
      table.columns[0].setText(null);
      table.columns[0].setHeaderTooltipText('tooltip');
      let form = await openOrganizerForm(table);
      expect(form.columnsTable.columnById('TitleColumn').cellValues()[0]).toBe('tooltip');
    });
  });

  describe('move menus', () => {
    it('are not enabled when column on top or bottom is fixed', async () => {
      let table = createTable(7);
      table.columns[1].setFixedPosition(true);
      table.columns[5].setFixedPosition(true);

      let form = await openOrganizerForm(table);
      let columnsTable = form.columnsTable;
      let moveUpMenu = form.widget('MoveColumnUpMenu');
      let moveDownMenu = form.widget('MoveColumnDownMenu');

      // column before is fixed
      columnsTable.selectRow(columnsTable.rows[2]);
      expect(moveUpMenu.enabledComputed).toBe(false);
      expect(moveDownMenu.enabledComputed).toBe(true);

      // column after is fixed
      columnsTable.selectRow(columnsTable.rows[4]);
      expect(moveUpMenu.enabledComputed).toBe(true);
      expect(moveDownMenu.enabledComputed).toBe(false);

      // no column before, column after is fixed
      columnsTable.selectRow(columnsTable.rows[0]);
      expect(moveUpMenu.enabledComputed).toBe(false);
      expect(moveDownMenu.enabledComputed).toBe(false);

      // no column after, column before is fixed
      columnsTable.selectRow(columnsTable.rows[6]);
      expect(moveUpMenu.enabledComputed).toBe(false);
      expect(moveDownMenu.enabledComputed).toBe(false);

      // neither before nor after is fixed
      columnsTable.selectRow(columnsTable.rows[3]);
      expect(moveUpMenu.enabledComputed).toBe(true);
      expect(moveDownMenu.enabledComputed).toBe(true);

      // move up, now column before is fixed
      moveUpMenu.doAction();
      expect(moveUpMenu.enabledComputed).toBe(false);
      expect(moveDownMenu.enabledComputed).toBe(true);

      // move down, neither is fixed
      moveDownMenu.doAction();
      expect(moveUpMenu.enabledComputed).toBe(true);
      expect(moveDownMenu.enabledComputed).toBe(true);

      // move down, now column after is fixed
      moveDownMenu.doAction();
      expect(moveUpMenu.enabledComputed).toBe(true);
      expect(moveDownMenu.enabledComputed).toBe(false);
    });
  });

  describe('add menu', () => {
    it('is visible if table is customizable', async () => {
      let table = createTable(0); // no columns
      let spy = spyOn(table, 'isCustomizable');
      spy.and.returnValue(true);
      let form = await openOrganizerForm(table);
      let addColumnMenu = form.widget('AddColumnMenu');
      expect(addColumnMenu.visible).toBe(true);

      spy.and.returnValue(false);
      await form.load();
      expect(addColumnMenu.visible).toBe(false);
    });

    it('is visible if there are invisible columns', async () => {
      let table = createTable(2);
      let spy = spyOn(table, 'isCustomizable');
      // not customizable
      spy.and.returnValue(false);
      let form = await openOrganizerForm(table);
      let addColumnMenu = form.widget('AddColumnMenu');
      let columnsTable = form.columnsTable;
      expect(addColumnMenu.visible).toBe(false); // all columns visible

      // one column invisible
      table.columns[0].setVisible(false);
      await form.load();
      columnsTable.selectRow(columnsTable.rows[0]);
      expect(addColumnMenu.visible).toBe(true);

      // all columns invisible
      table.columns[1].setVisible(false);
      await form.load();
      expect(columnsTable.rows.length).toBe(0);
      expect(addColumnMenu.visible).toBe(true);
    });

    it('can show all invisible columns even if there are fixed columns', async () => {
      let table = createTable(3);
      table.columns[1].setFixedPosition(true);
      spyOn(table, 'isCustomizable').and.returnValue(false);
      let form = await openOrganizerForm(table);
      let addColumnMenu = form.widget('AddColumnMenu');
      let removeColumnMenu = form.widget('RemoveColumnMenu');
      let columnsTable = form.columnsTable;

      // Remove all columns, only fixed column stays
      columnsTable.selectAll();
      removeColumnMenu.doAction();
      expect(columnsTable.columnById('KeyColumn').cellValues()).toEqual([table.columns[1]]);

      // Select remaining row
      columnsTable.selectAll();

      // Add all columns, all columns are shown again
      addColumnMenu.doAction();
      let event = await session.desktop.when('propertyChange:dialogs');
      let showInvisibleColumnsForm = scout.assertInstance(event.newValue[0], ShowInvisibleColumnsForm);
      showInvisibleColumnsForm.widget('ColumnsTable').checkAll();
      await showInvisibleColumnsForm.ok();
      expect(columnsTable.columnById('KeyColumn').cellValues()).toEqual([table.columns[0], table.columns[1], table.columns[2]]);
    });
  });

  describe('remove menu', () => {

    it('is visible if a selected column can be made invisible', async () => {
      let table = createTable(2);
      let spy = spyOn(table, 'isCustomizable');
      // not customizable
      spy.and.returnValue(false);
      let form = await openOrganizerForm(table);
      let removeColumnMenu = form.widget('RemoveColumnMenu');
      let columnsTable = form.columnsTable;

      // all columns visible, none selected
      expect(removeColumnMenu.visible).toBe(false);

      // one row selected
      columnsTable.selectRow(columnsTable.rows[0]);
      expect(removeColumnMenu.visible).toBe(true);

      // one column invisible
      table.columns[0].setVisible(false);
      await form.load();
      columnsTable.selectRow(columnsTable.rows[0]);
      expect(removeColumnMenu.visible).toBe(true);

      // all columns invisible
      table.columns[1].setVisible(false);
      await form.load();
      expect(columnsTable.rows.length).toBe(0);
      expect(removeColumnMenu.visible).toBe(false);
    });

    it('does not remove fixedPosition columns', async () => {
      let table = createTable(3);
      table.columns[0].setFixedPosition(true);
      let spy = spyOn(table, 'isCustomizable');
      // not customizable
      spy.and.returnValue(false);
      let form = await openOrganizerForm(table);
      let removeColumnMenu = form.widget('RemoveColumnMenu');
      let columnsTable = form.columnsTable;

      columnsTable.selectRows(columnsTable.rows[0]);
      expect(removeColumnMenu.visible).toBe(false);

      columnsTable.selectRows([columnsTable.rows[0], columnsTable.rows[1]]);
      expect(removeColumnMenu.visible).toBe(true);

      // fixed column stays, second column was removed
      removeColumnMenu.doAction();
      expect(columnsTable.columnById('KeyColumn').cellValues()).toEqual([table.columns[0], table.columns[2]]);
    });
  });
});
