/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {MenuItemsOrder, NullWidget} from '../../src/index';

describe('MenuItemsOrder', () => {

  let session, menuItemsOrder;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    menuItemsOrder = new MenuItemsOrder(session, 'Table');
    let nullWidget = new NullWidget();
    nullWidget.session = session;
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
