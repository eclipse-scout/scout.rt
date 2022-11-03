/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Menu, MenuItemsOrder, NullWidget} from '../../src/index';

describe('MenuItemsOrder', () => {

  let session: SandboxSession, menuItemsOrder: SpecMenuItemsOrder;

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
    menuItemsOrder = new SpecMenuItemsOrder(session, 'Table');
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

});
