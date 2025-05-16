/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {TableSpecHelper} from '../../src/testing';
import {Menu, MoveTableRowMenuHelper, scout, Table} from '../../src';

describe('MoveTableRowMenuHelper', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;
  let table: Table;
  let moveUpMenu: Menu;
  let moveDownMenu: Menu;
  let moveTableRowMenuHelper: MoveTableRowMenuHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    table = helper.createTable(helper.createModelFixture(1, 3));
    table.setMenus([{
      objectType: Menu,
      text: 'move up'
    }, {
      objectType: Menu,
      text: 'move down'
    }]);
    moveUpMenu = table.menus[0];
    moveDownMenu = table.menus[1];
    moveTableRowMenuHelper = scout.create(MoveTableRowMenuHelper);
    moveTableRowMenuHelper.install({
      table: table,
      moveRowUpMenu: moveUpMenu,
      moveRowDownMenu: moveDownMenu
    });
  });

  afterEach(() => {
    session = null;
  });

  describe('moveRowUpMenu', () => {
    it('is visible if rows are selected', () => {
      expect(moveUpMenu.visible).toBe(false);

      table.selectRows(table.rows[0]);
      expect(moveUpMenu.visible).toBe(true);

      table.deleteAllRows();
      expect(moveUpMenu.visible).toBe(false);
    });

    it('is enabled if first row is not selected', () => {
      expect(moveUpMenu.enabled).toBe(false);

      table.selectRows(table.rows[0]);
      expect(moveUpMenu.enabled).toBe(false);

      table.selectRows(table.rows[1]);
      expect(moveUpMenu.enabled).toBe(true);
    });

    it('moves row up and updates state', () => {
      let row1 = table.rows[1];
      table.selectRows(table.rows[1]);
      moveUpMenu.doAction();
      expect(table.rows[0]).toBe(row1);
      expect(moveUpMenu.enabled).toBe(false);
    });

    it('updates state if rows change', () => {
      table.selectRows([table.rows[1], table.rows[2]]);
      expect(moveUpMenu.enabled).toBe(true);

      // Selected row is now the first row
      table.deleteRow(table.rows[0]);
      expect(moveUpMenu.enabled).toBe(false);

      table.insertRow({cells: ['new']});
      expect(moveUpMenu.enabled).toBe(false);

      table.moveRow(2, 0);
      expect(moveUpMenu.enabled).toBe(true);
    });

    it('does nothing if helper is not installed anymore', () => {
      table.selectRows(table.rows[1]);
      expect(moveUpMenu.enabled).toBe(true);

      moveTableRowMenuHelper.uninstall();

      let row1 = table.rows[1];
      table.selectRows(table.rows[1]);
      moveUpMenu.doAction();
      expect(table.rows[1]).toBe(row1); // not moved
      expect(moveUpMenu.enabled).toBe(true); // did not change state
    });
  });

  describe('moveRowDownMenu', () => {
    it('is visible if rows are selected', () => {
      expect(moveDownMenu.visible).toBe(false);

      table.selectRows(table.rows[0]);
      expect(moveDownMenu.visible).toBe(true);

      table.deleteAllRows();
      expect(moveDownMenu.visible).toBe(false);
    });

    it('is enabled if last row is not selected', () => {
      expect(moveDownMenu.enabled).toBe(false);

      table.selectRows(table.rows[2]);
      expect(moveDownMenu.enabled).toBe(false);

      table.selectRows(table.rows[1]);
      expect(moveDownMenu.enabled).toBe(true);
    });

    it('moves row up and updates state', () => {
      let row1 = table.rows[1];
      table.selectRows(table.rows[1]);
      moveDownMenu.doAction();
      expect(table.rows[2]).toBe(row1);
      expect(moveDownMenu.enabled).toBe(false);
    });

    it('updates state if rows change', () => {
      table.selectRows([table.rows[0], table.rows[1]]);
      expect(moveDownMenu.enabled).toBe(true);

      // Selected row is now the last row
      table.deleteRow(table.rows[2]);
      expect(moveDownMenu.enabled).toBe(false);

      // Last row is not selected anymore
      table.insertRow({cells: ['new']});
      expect(moveDownMenu.enabled).toBe(true);

      table.moveRow(2, 0);
      expect(moveDownMenu.enabled).toBe(false);
    });

    it('does nothing if helper is not installed anymore', () => {
      table.selectRows(table.rows[1]);
      expect(moveDownMenu.enabled).toBe(true);

      moveTableRowMenuHelper.uninstall();

      let row1 = table.rows[1];
      table.selectRows(table.rows[1]);
      moveDownMenu.doAction();
      expect(table.rows[1]).toBe(row1); // not moved
      expect(moveDownMenu.enabled).toBe(true); // did not change state
    });
  });
});
