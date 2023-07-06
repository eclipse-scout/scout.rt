/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Menu, MenuItemsOrder, NullWidget, Table} from '../../src/index';
import {MenuSpecHelper} from '../../src/testing';

describe('MenuItemsOrder', () => {

  let session: SandboxSession, menuHelper: MenuSpecHelper, menuItemsOrder: SpecMenuItemsOrder;

  class SpecMenuItemsOrder extends MenuItemsOrder {
    override _createSeparator(): Menu {
      return super._createSeparator();
    }

    override _menuTypes(types?: string[]): string[] {
      return super._menuTypes(types);
    }
  }

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    menuHelper = new MenuSpecHelper(session);
    menuItemsOrder = new SpecMenuItemsOrder(session, 'Table', [Table.MenuType.EmptySpace]);
    let nullWidget = new NullWidget();
    nullWidget.session = session;
    // @ts-expect-error
    menuItemsOrder.menuBar = nullWidget;
  });

  it('_createSeparator', () => {
    let separator = menuItemsOrder._createSeparator();
    expect(separator.separator).toBe(true);
    expect(separator.session).toBeTruthy();
    expect(separator.visible).toBe(true);
    expect(separator.enabled).toBe(true);
    expect(separator.selected).toBe(false);
  });

  it('_menuTypes', () => {
    let menuTypes = menuItemsOrder._menuTypes();
    expect(menuTypes.length).toBe(0);
    menuTypes = menuItemsOrder._menuTypes(['Foo']);
    expect(menuTypes.length).toBe(1);
    expect(menuTypes[0]).toBe('Table.Foo');
    menuTypes = menuItemsOrder._menuTypes(['Foo', 'Bar']);
    expect(menuTypes.length).toBe(2);
    expect(menuTypes[1]).toBe('Table.Bar');
  });

  it('order', () => {
    let emptySpaceImplicit = menuHelper.createMenu(),
      emptySpace = menuHelper.createMenu({menuTypes: [Table.MenuType.EmptySpace]}),
      singleSelection = menuHelper.createMenu({menuTypes: [Table.MenuType.SingleSelection]}),
      multiSelection = menuHelper.createMenu({menuTypes: [Table.MenuType.MultiSelection]}),
      header1 = menuHelper.createMenu({menuTypes: [Table.MenuType.Header]}),
      header2 = menuHelper.createMenu({menuTypes: [Table.MenuType.Header]}),
      separator = menuItemsOrder._createSeparator();

    menuItemsOrder._createSeparator = () => separator;

    let sorted = menuItemsOrder.order([header1, emptySpace, multiSelection, header2, emptySpaceImplicit, singleSelection]);
    expect(sorted.left.length).toBe(5);
    expect(sorted.right.length).toBe(0);
    expect(sorted.all.length).toBe(5);

    expect(sorted.left).toEqual([emptySpace, emptySpaceImplicit, separator, multiSelection, singleSelection]);
    expect(sorted.all).toEqual([emptySpace, emptySpaceImplicit, separator, multiSelection, singleSelection]);
  });
});
