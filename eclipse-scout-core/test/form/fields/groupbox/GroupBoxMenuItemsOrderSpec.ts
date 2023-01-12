/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, GroupBoxMenuItemsOrder, Menu, NullWidget, scout} from '../../../../src/index';

describe('GroupBoxMenuItemsOrder', () => {

  beforeEach(() => {
    setFixtures(sandbox());
  });

  function createMenu(label, horizontalAlignment, isButton) {
    return scout.create(Menu, {
      parent: new NullWidget(),
      session: sandboxSession(),
      text: label,
      actionStyle: isButton ? Action.ActionStyle.BUTTON : Action.ActionStyle.DEFAULT,
      horizontalAlignment: horizontalAlignment
    });
  }

  it('order', () => {
    let b1 = createMenu('left-button', -1, true);
    let b2 = createMenu('right-button', 1, true);
    let m1 = createMenu('left-menu', -1, false);
    let m2 = createMenu('right-menu', 1, false);

    let sorted = new GroupBoxMenuItemsOrder().order([b1, b2, m1, m2]);
    expect(sorted.left[0]).toBe(b1);
    expect(sorted.left[1]).toBe(m1);

    expect(sorted.right[0]).toBe(m2);
    expect(sorted.right[1]).toBe(b2);

    expect(sorted.all[0]).toBe(b1);
    expect(sorted.all[1]).toBe(m1);
    expect(sorted.all[2]).toBe(m2);
    expect(sorted.all[3]).toBe(b2);
  });

});
