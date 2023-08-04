/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {TableSpecHelper} from '../../src/testing/index';
import {Menu, scout, Table} from '../../src';

describe('TableHeaderSpec', () => {
  let session: SandboxSession;
  let helper: TableSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new TableSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('render', () => {

    it('attaches listener to the table but only once', () => {
      let model = helper.createModelFixture(2);
      let table = helper.createTable(model);
      table.render();
      expect(table.header).not.toBeUndefined();
      let listenerCount = table.events._eventListeners.length;

      table.setHeaderVisible(false);
      table.setHeaderVisible(true);

      // Still same amount of listeners expected after header visibility changed
      expect(table.events._eventListeners.length).toBe(listenerCount);
    });

  });

  it('hiddenByUi', () => {
    let table = helper.createTableWithOneColumn();
    table.setMenus([scout.create(Menu, { // fake header menu required to properly calculate visibility
      parent: table,
      text: 'Foo',
      menuTypes: [Table.MenuType.Header]
    })]);
    table.render();

    expect(table.header.menuBar.hiddenByUi).toBe(false);
    expect(table.header.menuBar.visible).toBe(true);
    table.setHeaderEnabled(false);
    expect(table.header.menuBar.hiddenByUi).toBe(true);
    expect(table.header.menuBar.visible).toBe(false);
    table.setHeaderEnabled(true);
    expect(table.header.menuBar.hiddenByUi).toBe(false);
    expect(table.header.menuBar.visible).toBe(true);
  });
});
