/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('GroupBoxMenuItemsOrder', function() {

  beforeEach(function() {
    setFixtures(sandbox());
  });

  function createMenu(label, horizontalAlignment, isButton) {
    return scout.create('Menu', {
      parent: new scout.NullWidget(),
      session: sandboxSession(),
      text: label,
      actionStyle: isButton ? scout.Action.ActionStyle.BUTTON : scout.Action.ActionStyle.DEFAULT,
      horizontalAlignment: horizontalAlignment
    });
  }

  it('order', function() {
    var b1 = createMenu('left-button', -1, true);
    var b2 = createMenu('right-button', 1, true);
    var m1 = createMenu('left-menu', -1, false);
    var m2 = createMenu('right-menu', 1, false);

    var sorted = new scout.GroupBoxMenuItemsOrder().order([b1, b2, m1, m2]);
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
